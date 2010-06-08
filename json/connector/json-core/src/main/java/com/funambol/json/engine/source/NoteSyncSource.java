/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2010 Funambol, Inc.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */
package com.funambol.json.engine.source;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.TimeZone;
import java.io.Serializable;
import com.funambol.common.pim.note.Note;
import com.funambol.common.pim.converter.NoteToSIFN;
import com.funambol.common.pim.sif.SIFNParser;
import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.CTInfo;
import com.funambol.framework.core.DataStore;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.source.AbstractSyncSource;
import com.funambol.framework.engine.source.MergeableSyncSource;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.tools.beans.LazyInitBean;
import com.funambol.json.admin.JsonConnectorConfig;
import com.funambol.json.converter.NoteConverter;
import com.funambol.json.dao.JsonDAO;
import com.funambol.json.dao.JsonDAOImpl;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.domain.JsonKeys;
import com.funambol.json.exception.DaoException;
import com.funambol.json.exception.InternalServerErrorException;
import com.funambol.json.exception.JsonConfigException;
import com.funambol.json.exception.MalformedJsonContentException;
import com.funambol.json.manager.JsonManager;
import com.funambol.json.manager.JsonNoteManager;
import com.funambol.json.security.JsonUser;
import com.funambol.json.util.Utility;
import com.funambol.server.config.Configuration;
import java.util.List;

/**
 * It implements the methods required by the SyncSource interface.
 * 
 * @version $Id:$
 */
public class NoteSyncSource extends AbstractSyncSource         
        implements MergeableSyncSource, Serializable, LazyInitBean {


    private static final long serialVersionUID = 2454307189271131431L;

    protected static final FunambolLogger log = FunambolLoggerFactory.getLogger(Utility.LOG_NAME);

    private NoteConverter converter;
    private JsonManager<Note> manager;
    private JsonDAO dao;
    
    public static final int SIFN_FORMAT = 0;      // To be used as index for SIF-Note
    public static final int PLAINTEXT_FORMAT = 1; // To be used as index for plain/text notes
    
    public static final String[] TYPE = {
        "text/x-s4j-sifn", // SIF-Note
        "text/plain"       //plain text note
    };

    protected static final String VERSION_SIFN      = "1.0";
    protected static final String VERSION_PLAINTEXT = "1.0";    
    
    private String rxContentType; // preferred content type as derived from the
                                  // analysis of the DevInf (RXPref)

    //specifies if the sync source should catch a backend server internal error
    private boolean stopSyncOnFatalError = false;

    private Sync4jPrincipal principal;    
    private String username;
    private String sessionID;
    
    private long since = 0;
    
    protected String serverTimeZoneID = null;           
    
    private TimeZone deviceTimeZone = null;
    
    public TimeZone getDeviceTimeZone() {
        return deviceTimeZone;
    }

    public void setDeviceTimeZone(TimeZone deviceTimeZone) {
        this.deviceTimeZone = deviceTimeZone;
    }
    private String deviceTimeZoneDescription = null;
    
    private String deviceCharset = null;

    public String getDeviceCharset() {
        return deviceCharset;
    }

    public void setDeviceCharset(String deviceCharset) {
        this.deviceCharset = deviceCharset;
    }
    

    //----------------------------------------------------------- Public Methods
    
    /**
     * 
     */
    public void init() {
    }

    /**
     * Called before any other synchronization method. To interrupt the sync
     * process, throw a SyncSourceException.
     * 
     * @param syncContext
     *                the context of the sync.
     * @throws SyncSourceException 
     * 
     * @see SyncContext
     */
    public void beginSync(SyncContext syncContext) throws SyncSourceException {
        
        try {
            
            this.dao = new JsonDAOImpl("note");
            this.converter = new NoteConverter();
            this.manager = new JsonNoteManager(dao, converter);

            rxContentType = findRXContentType(syncContext);

            try {
                stopSyncOnFatalError = JsonConnectorConfig.getConfigInstance().getStopSyncOnFatalError();
            } catch (JsonConfigException ex) {
                stopSyncOnFatalError = false;
            }

            setDeviceInfo(syncContext);

            this.principal = syncContext.getPrincipal();
            this.username = principal.getUsername();

            int syncMode = syncContext.getSyncMode();

            this.since = 0;
            Timestamp sinceValue = syncContext.getTo();
            if (sinceValue != null) {
                this.since = sinceValue.getTime();
            } else {
                // the sync is already started 
                this.since = System.currentTimeMillis() - 1000;
            }

            JsonUser jsonUser = ((JsonUser) syncContext.getPrincipal().getUser());

            this.sessionID = jsonUser.getSessionID();
            
            if (log.isTraceEnabled()) {
                log.trace("Starting synchronization" +
                        "\nUser : " + username +
                        "\nUI timezone : " + serverTimeZoneID +
                        "\nSince(gmt ): " + syncContext.getTo() +
                        "\nSince(long): " + since);
            }
            
            manager.beginSync(sessionID, since, AlertCode.getAlertDescription(syncMode), serverTimeZoneID);
            
            if (syncMode == AlertCode.REFRESH_FROM_CLIENT) {
                if (log.isTraceEnabled()) {
                    log.trace("Refresh From Client Sync Method");
                }
                manager.removeAllItems(sessionID, since);
            } 
            
            
        } catch (ClassCastException e) {
            throw new SyncSourceException("Sync4jUser must be a JsonUser. Check if the JsonOfficer is configured in ds-server. ", e);
        } catch (DaoException e) {
            throw new SyncSourceException("beginSync failed on backend.", e);
        } catch (JsonConfigException e) {
            throw new SyncSourceException("Configuration of the SyncSource failed.", e);
        }

    }

    /**
     * Called after the modifications have been applied.
     * 
     * @throws SyncSourceException
     *                 to interrupt the process with an error
     */
    public void endSync() throws SyncSourceException {
        if (log.isTraceEnabled()) {
            log.trace("Ending synchronization");
        }
        try {
            manager.endSync(sessionID);
        } catch (DaoException e) {
            throw new SyncSourceException("endSync failed on backend.", e);
        }
    }

    /**
     * Called to get the keys of all items accordingly with the parameters used
     * in the beginSync call.
     * 
     * @return an array of all <code>SyncItemKey</code>s stored in this
     *         source. If there are no items an empty array is returned.
     * 
     * @throws SyncSourceException
     *                 in case of error (for instance if the underlying data
     *                 store runs into problems)
     */
    public SyncItemKey[] getAllSyncItemKeys() throws SyncSourceException {
        try {
            JsonKeys keys = manager.getAllItemsKey(sessionID);
            return extractKeyArrayFromKeys(keys);
        } catch (DaoException e) {
            log.error("Error in dao module for getAllSyncItemKeys!");
            throw new SyncSourceException("Error retrieving all item keys. ", e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for getAllSyncItemKeys");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error retrieving all item keys. ", e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content for getAllSyncItemKeys");
            throw new SyncSourceException("Error retrieving all item keys. ", e);
        }
    }

    /**
     * Called to get the keys of the items updated in the time frame sinceTs -
     * untilTs. <br>
     * <code>sinceTs</code> null means all keys of the items updated until
     * <code>untilTs</code>. <br>
     * <code>untilTs</code> null means all keys of the items updated since
     * <code>sinceTs</code>.
     * 
     * @param sinceTs
     *                consider the changes since this point in time.
     * @param untilTs
     *                consider the changes until this point in time.
     * 
     * @return an array of keys containing the <code>SyncItemKey</code>'s key
     *         of the updated items in the given time frame. It MUST NOT return
     *         null for no keys, but instad an empty array.
     */
    public SyncItemKey[] getUpdatedSyncItemKeys(Timestamp sinceTs, Timestamp untilTs) throws SyncSourceException {
        try {
            JsonKeys keys = manager.getUpdatedItemKeys(sessionID, sinceTs.getTime(), untilTs.getTime());
            return extractKeyArrayFromKeys(keys);
        } catch (DaoException e) {
            log.error("Error in dao module for getUpdatedSyncItemKeys!");
            throw new SyncSourceException("Error retrieving updated item keys. ", e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for getUpdatedSyncItemKeys");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error retrieving updated item keys. ", e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content for getUpdatedSyncItemKeys");
            throw new SyncSourceException("Error retrieving updated item keys. ", e);
        }
    }

    /**
     * Called to get the keys of the items deleted in the time frame sinceTs -
     * untilTs. <br>
     * <code>sinceTs</code> null means all keys of the items deleted until
     * <code>untilTs</code>. <br>
     * <code>untilTs</code> null means all keys of the items deleted since
     * <code>sinceTs</code>.
     * 
     * @param sinceTs
     *                consider the changes since this point in time.
     * @param untilTs
     *                consider the changes until this point in time.
     * 
     * @return an array of keys containing the <code>SyncItemKey</code>'s key
     *         of the deleted items in the given time frame. It MUST NOT return
     *         null for no keys, but instad an empty array.
     */
    public SyncItemKey[] getDeletedSyncItemKeys(Timestamp sinceTs, Timestamp untilTs) throws SyncSourceException {
        try {
            JsonKeys keys = manager.getDeletedItemKeys(sessionID, sinceTs.getTime(), untilTs.getTime());
            return extractKeyArrayFromKeys(keys);
        } catch (DaoException e) {
            log.error("Error in dao module for getDeletedSyncItemKeys!");
            throw new SyncSourceException("Error retrieving deleted item keys. ", e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for getDeletedSyncItemKeys");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error retrieving deleted item keys. ", e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content for getDeletedSyncItemKeys");
            throw new SyncSourceException("Error retrieving deleted item keys. ", e);
        }
    }

    /**
     * Called to get the keys of the items created in the time frame sinceTs -
     * untilTs. <br>
     * <code>sinceTs</code> null means all keys of the items created until
     * <code>untilTs</code>. <br>
     * <code>untilTs</code> null means all keys of the items created since
     * <code>sinceTs</code>.
     * 
     * @param sinceTs
     *                consider the changes since this point in time.
     * @param untilTs
     *                consider the changes until this point in time.
     * 
     * @return an array of keys containing the <code>SyncItemKey</code>'s key
     *         of the created items in the given time frame. It MUST NOT return
     *         null for no keys, but instad an empty array.
     */
    public SyncItemKey[] getNewSyncItemKeys(Timestamp sinceTs, Timestamp untilTs) throws SyncSourceException {
        try {
            JsonKeys keys = manager.getNewItemKeys(sessionID, sinceTs.getTime(), untilTs.getTime());
            return extractKeyArrayFromKeys(keys);
        } catch (DaoException e) {
            log.error("Error in dao module for getNewSyncItemKeys!");
            throw new SyncSourceException("Error retrieving new item keys. ", e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for getNewSyncItemKeys");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error retrieving new item keys. ", e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content for getNewSyncItemKeys");
            throw new SyncSourceException("Error retrieving new item keys. ", e);
        }
    }

    /**
     * Adds a new <code>SyncItem</code>. The item is also returned giving the
     * opportunity to the source to modify its content and return the updated
     * item (i.e. updating the id to the GUID).
     * 
     * @param syncInstance
     *                the item to add
     * 
     * @return the inserted item
     * 
     * @throws SyncSourceException
     *                 in case of error (for instance if the underlying data
     *                 store runs into problems)
     */
    public SyncItem addSyncItem(SyncItem syncInstance) throws SyncSourceException {

        Note note = null;
        JsonItem<Note> noteItem = new JsonItem<Note>();
        String content = null;

        try {

            content = getContentFromSyncItem(syncInstance);
            String contentType = syncInstance.getType();

            note = convert(content, contentType);
            noteItem.setItem(note);
            noteItem.setKey(null);

            noteItem.setState("N");

            String GUID = manager.addExtendedItem(sessionID, noteItem, since);

             // Checking if at least one field used for the twin search in the
            // note we are adding contains meaningful data, otherwise the
            // add operation is not allowed since there's no way to prevent
            // duplication.
            Note noteTwin = convert(content, contentType);
            JsonItem<Note> noteForSearch = new JsonItem<Note>();
            noteForSearch.setItem(noteTwin);
            if(!manager.isTwinSearchAppliableOn(noteForSearch)) {
                if(log.isTraceEnabled()) {
                    log.trace("Rejecting add of note with key ["+
                              ((syncInstance!=null && syncInstance.getKey()!=null)?
                                syncInstance.getKey().getKeyAsString():"N/A"
                             )+"] since that note doesn't contain any field " +
                             "usable for twin search.");
                }
                throw new SyncSourceException("Adding a note without any field " +
                                               "usable for twin search set is not allowed.");
            }

            SyncItemImpl newSyncItem = new SyncItemImpl(this, // syncSource
                    GUID, // key
                    null, // mappedKey
                    SyncItemState.NEW, // state
                    content.getBytes(), // content
                    null, // format
                    contentType, // type
                    null // timestamp @todo
                    );

            return newSyncItem;

        } catch (DaoException e) {
            log.error("Error in dao module!");
            throw new SyncSourceException("Error adding the item " + syncInstance, e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for addSyncItem");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error adding the item " + syncInstance, e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content");
            throw new SyncSourceException("Error adding the item " + syncInstance, e);
        } catch (Exception e) {
            log.error("Error converting the json content");
            throw new SyncSourceException("Error adding the item " + syncInstance, e);
        }
    }

    /**
     * Update a <code>SyncItem</code>. The item is also returned giving the
     * opportunity to the source to modify its content and return the updated
     * item (i.e. updating the id to the GUID).
     * 
     * @param syncInstance
     *                the item to replace
     * 
     * @return the updated item
     * 
     * @throws SyncSourceException
     *                 in case of error (for instance if the underlying data
     *                 store runs into problems)
     */
    public SyncItem updateSyncItem(SyncItem syncInstance) throws SyncSourceException {

        Note note = null;
        JsonItem<Note> noteItem = new JsonItem<Note>();
        JsonItem<Note> noteItemUpdated = null;
        String content = null;

        try {
                        
            
            content = getContentFromSyncItem(syncInstance);
            String contentType = syncInstance.getType();

            note = convert(content, contentType);

            noteItem.setItem(note);
            noteItem.setContentType(contentType);
            noteItem.setState(String.valueOf(syncInstance.getState()));
            String GUID = syncInstance.getKey().getKeyAsString();
            String id   = Utility.removePrefix(GUID);
            noteItem.setKey(id);

            noteItemUpdated = manager.updateExtendedItem(sessionID, noteItem, since);
            String updatedKey = noteItemUpdated.getKey();

            SyncItemImpl newSyncItem = new SyncItemImpl(this, // syncSource
                    updatedKey, // key
                    null, // mappedKey
                    SyncItemState.UPDATED, // state
                    content.getBytes(), // content
                    null, // format
                    contentType, // type
                    null // timestamp @todo
                    );

            return newSyncItem;

        } catch (DaoException e) {
            log.error("Error in dao module!");
            throw new SyncSourceException("Error updating the item " + syncInstance, e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for updateSyncItem");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error updating the item " + syncInstance, e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content");
            throw new SyncSourceException("Error updating the item " + syncInstance, e);
        } catch (Exception e) {
            log.error("Error converting the json content");
            throw new SyncSourceException("Error updating the item " + syncInstance, e);
        }
    }

    /**
     * Removes a SyncItem given its key.
     * 
     * @param itemKey
     *                the key of the item to remove
     * @param time
     *                the time of the deletion
     * @param softDelete
     *                is a soft delete ?
     * 
     * @throws SyncSourceException
     *                 in case of error (for instance if the underlying data
     *                 store runs into problems)
     */
    public void removeSyncItem(SyncItemKey itemKey, Timestamp time, boolean softDelete) throws SyncSourceException {
        try {
            String guid = itemKey.getKeyAsString();
            manager.removeItem(sessionID, guid, since);
        } catch (DaoException e) {
            log.error("Error in dao module!");
            throw new SyncSourceException("Error removing the item with key " + itemKey.getKeyAsString(), e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for removeSyncItem");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error removing the item with key " + itemKey.getKeyAsString(), e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content");
            throw new SyncSourceException("Error removing the item with key " + itemKey.getKeyAsString(), e);
        }
    }

    /**
     * Called to get the item with the given key.
     * 
     * @return return the <code>SyncItem</code> corresponding to the given
     *         key. If no item is found, null is returned.
     * 
     * @param syncItemKey
     *                the key of the SyncItem to return
     * 
     * @throws SyncSourceException
     *                 in case of errors (for instance if the underlying data
     *                 store runs into problems)
     */
    public SyncItem getSyncItemFromId(SyncItemKey syncItemKey) throws SyncSourceException {

        String guid = null;
        SyncItem syncItem = null;

        guid = syncItemKey.getKeyAsString();

        try {
            JsonItem<Note> noteItem = manager.getExtendedItem(sessionID, guid);
            if (noteItem != null) {
                char status = ' ';
                // @todo
                //status = contactItem.getState().charAt(0)
                syncItem = createSyncItem(guid, noteItem.getItem(), status);
            }
        } catch (DaoException e) {
            log.error("Error in dao module for getNewSyncItemKeys!");
            throw new SyncSourceException("Error retrieving item with key: " + guid, e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for getSyncItemFromId");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error retrieving item with key: " + syncItemKey.getKeyAsString(), e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content for getNewSyncItemKeys");
            throw new SyncSourceException("Error retrieving item with key: " + guid, e);
        } catch (Exception e) {
            log.error("Error converting the json content for getNewSyncItemKeys");
            throw new SyncSourceException("Error retrieving item with key: " + guid, e);
        }

        return syncItem;
    }

    /**
     * Called to retrive the keys of the twins of the given item
     * 
     * @param syncItem
     *                the twin item
     * 
     * @return the keys of the twin. Each source implementation is free to
     *         interpret this as it likes (i.e.: comparing all fields).
     * 
     * 
     * @throws SyncSourceException
     *                 in case of errors (for instance if the underlying data
     *                 store runs into problems)
     */
    public SyncItemKey[] getSyncItemKeysFromTwin(SyncItem syncItem) throws SyncSourceException {
        try {
            
            Note note = convert(syncItem);
            
            JsonItem<Note> noteItem = new JsonItem<Note>();
            noteItem.setKey(syncItem.getKey().getKeyAsString());
            noteItem.setItem(note);
            noteItem.setState(String.valueOf(syncItem.getState()));

            JsonKeys keys = manager.getExtendedItemKeysFromTwin(sessionID, noteItem);

            return extractKeyArrayFromKeys(keys);
            
        } catch (DaoException e) {
            log.error("Error in dao module for getSyncItemKeysFromTwin!");
            throw new SyncSourceException("Error retrieving all item keys from twin. ", e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for getSyncItemKeysFromTwin");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error retrieving all item keys from twin. ", e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content for getSyncItemKeysFromTwin");
            throw new SyncSourceException("Error retrieving all item keys from twin. ", e);
        } catch (Exception e) {
            log.error("Error converting the syncItem to contact for getSyncItemKeysFromTwin");
            throw new SyncSourceException("Error retrieving all item keys from twin. ", e);
        }
                
    }
    
    /**
     *
     * 
     * @param syncItemKey
     * @param syncItem
     * @return
     * @throws com.funambol.framework.engine.source.SyncSourceException
     */
    public boolean mergeSyncItems(SyncItemKey syncItemKey, SyncItem syncItem)
    throws SyncSourceException {           
        
        try {
            
            boolean clientUpdateRequired = false;
            
            String content = getContentFromSyncItem(syncItem);
            String contentType = syncItem.getType();

            
            String serverItemKey = syncItemKey.getKeyAsString();
            
            Note clientNote = convert(content, contentType);
            JsonItem<Note> clientNoteItem = new JsonItem<Note>();
            clientNoteItem.setItem(clientNote);            
            clientNoteItem.setContentType(contentType);
            clientNoteItem.setKey(syncItem.getKey().getKeyAsString());
            clientNoteItem.setState(String.valueOf(syncItem.getState()));
                        
            JsonItem<Note> serverNoteItem = manager.getExtendedItem(sessionID, serverItemKey);

            clientUpdateRequired = manager.mergeExtendedItem(sessionID, serverNoteItem, clientNoteItem, since);
                            
            if(clientUpdateRequired) {
                syncItem = getSyncItemFromId(syncItemKey);
            }
            
            return clientUpdateRequired;
                                
        } catch (DaoException e) {
            log.error("Error in dao module for mergeSyncItems!");
            throw new SyncSourceException("Error merging items. ", e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for mergeSyncItems");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error merging items. ", e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content for mergeSyncItems");
            throw new SyncSourceException("Error merging items. ", e);
        } catch (Exception e) {
            log.error("Error converting the syncItem to calendar for mergeSyncItems");
            throw new SyncSourceException("Error merging items. ", e);
        }        
        
    }
    

    /**
     * Called by the engine to notify an operation status.
     * 
     * @param operationName
     *                the name of the operation. One between: - Add - Replace -
     *                Delete
     * @param status
     *                the status of the operation
     * @param keys
     *                the keys of the items
     */
    public void setOperationStatus(String operationName, int status, SyncItemKey[] keys) {
        //
        // Put here your code to handle the status code returned by the client
        // for a particular operation of the given keys
        //
        log.info("setOperationStatus()");
    }

    //-------------------------------------------------------- Protected Methods
    
    /**
     * Extracts the content from a syncItem.
     * 
     * @param syncItem
     * @return as a String object
     */
    protected String getContentFromSyncItem(SyncItem syncItem) {

        byte[] itemContent = syncItem.getContent();

        // Add content processing here, if needed

        return new String(itemContent == null ? new byte[0] : itemContent);
    }

    /**
     * 
     * @param idList
     * @return
     */
    protected SyncItemKey[] extractKeyArrayFromKeys(JsonKeys keys) {

        SyncItemKey[] keyList = new SyncItemKey[keys.getKeys().length];
        for (int i = 0; i < keys.getKeys().length; i++) {
            keyList[i] = new SyncItemKey((String) keys.getKeys()[i]);
        }
        return keyList;
    }
    //---------------------------------------------------------- Private Methods
    /**
     * Converts a SyncItem to a Note object, provided it represents a note
     * item in VNote or SIF-N format.
     * 
     * @param syncItem
     * @throws Exception
     *                 if the contentType is wrong or the conversion attempt
     *                 doesn't succeed.
     * @return a Note object
     */
    private Note convert(SyncItem syncItem) throws Exception {
        return convert(getContentFromSyncItem(syncItem), syncItem.getType());
    }

    /**
     * Converts a note in vNote or SIF-N format to a Note object. (client
     * --> server directrion) we have to set the original format of the input
     * data
     * 
     * @param content
     *                as a String
     * @param contentType
     * @throws EntityException
     *                 if the contentType is wrong or the conversion attempt
     *                 doesn't succeed.
     * @return a Note object
     */
    private Note convert(String content, String contentType) throws Exception {

        Note note = null;

        // Finds out which target type is required
        for (int i = 0; i < TYPE.length; i++) {

            if (contentType.equals(TYPE[i])) { // Bingo!

                // Uses the proper converter method
                switch (i) {
                    case PLAINTEXT_FORMAT:
                        note = plaintext2note(content);
                        return note;
                    case SIFN_FORMAT:
                        note = sif2note(content);
                        return note;
                    default:
                        throw new Exception("Can't make a Note " + "out of a " + TYPE[i] + "!");
                }
            }
        }
        throw new Exception("Content type unknown: " + contentType);
    }

    /**
     * 
     * @param vnote
     * @return
     * @throws java.lang.Exception
     */
    private Note vnote2note(String vnote) throws Exception {

        if (Configuration.getConfiguration().isDebugMode()) {
            if (log.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder(vnote.length() + 60);
                sb.append("Converting: VNote => JsonNote").append("\nINPUT = {").append(vnote).append('}');
                log.trace(sb.toString());
            }
        }

        Note note = new Note();
        /*
        ByteArrayInputStream buffer = null;
        VNoteParser parser = null;
        VNote note = null;
        try {
        note = new Note();
        
        buffer = new ByteArrayInputStream(vnote.getBytes());
        if ((vnote.getBytes()).length > 0) {
        parser = new VNoteParser(buffer);
        note = (VNote) parser.VNote();
        }
        } catch (Exception e) {
        throw new Exception("Error converting VNote to Note. ", e);
        }
        
        if (log.isTraceEnabled()) {
        log.trace("Conversion done.");
        }
         */
        return note;
    }

    /**
     * 
     * @param content
     * @param contentType
     * @return
     */
    private Note plaintext2note(String content) {
        if (Configuration.getConfiguration().isDebugMode()) {
            if (log.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder(content.length() + 60);
                sb.append("Converting: TextPlain => Note").append("\nINPUT = {").append(content).append('}');
                log.trace(sb.toString());
            }
        }
        Note note = new Note();
        if (log.isTraceEnabled()) {
            log.trace("textplain2Note");
        }
        if (content.contains("\n")) {
            note.getSubject().setPropertyValue(content.substring(0, content.indexOf("\n")));
        }
        note.getTextDescription().setPropertyValue(content);
        return note;
    }

    /**
     * 
     * @param sifn
     * @return
     * @throws java.lang.Exception
     */
    private Note sif2note(String sifn) throws Exception {

        if (Configuration.getConfiguration().isDebugMode()) {
            if (log.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder(sifn.length() + 60);
                sb.append("Converting: SIF-N => Note").append("\nINPUT = {").append(sifn).append('}');
                log.trace(sb.toString());
            }
        }

        ByteArrayInputStream buffer = null;
        SIFNParser parser = null;
        Note note = null;
        try {
            note = new Note();
            buffer = new ByteArrayInputStream(sifn.getBytes());
            if ((sifn.getBytes()).length > 0) {
                parser = new SIFNParser(buffer);
                note = (Note) parser.parse();
            }
        } catch (Exception e) {
            throw new Exception("Error converting SIF-N to Note. ", e);
        }

        if (log.isTraceEnabled()) {
            log.trace("Conversion done.");
        }
        return note;
    }

    /**
     * Create a new SyncItem from a Note. The target contentType and status
     * are passed as arguments.
     *
     * @param note the Note object representing the input information
     * @param contentType chosen among the TYPE array's elements
     * @param status
     * @throws EntityException if the content type is wrong or any problem
     *                         occurs while creating a new SyncItem
     * @return a newly created SyncItem object
     */
    private SyncItem createSyncItem(String id, Note note, char status)
            throws Exception {

        String contentType;
        if (rxContentType != null) {
            // Use device capabilities
            contentType = rxContentType;
        } else {
            // Use user-set default
            contentType = getInfo().getPreferredType().getType();
        }

        SyncItem syncItem = null;
        String stream = convert(note, contentType);

        try {
            syncItem = new SyncItemImpl(this, id, status);
        } catch (Exception e) {
            throw new Exception(e);
        }

        syncItem.setType(contentType);
        syncItem.setContent(stream.getBytes());

        if (log.isTraceEnabled()) {
            log.trace("ContactSyncSource created SyncItem");
        }

        return syncItem;
    }
    
    /**
     * 
     * @param context
     * @throws com.funambol.framework.engine.source.SyncSourceException
     */
    private void setDeviceInfo(SyncContext context) throws SyncSourceException {
        try {
            Sync4jDevice device = context.getPrincipal().getDevice();
            String timezone = device.getTimeZone();
            if (device.getConvertDate()) {
                if (timezone != null && timezone.length() > 0) {
                    deviceTimeZoneDescription = timezone;
                    setDeviceTimeZone(TimeZone.getTimeZone(deviceTimeZoneDescription));
                }
            }
            setDeviceCharset(device.getCharset());
        } catch (Exception e) {
            throw new SyncSourceException("Error settings the device information." + e.getMessage());
        }
    }
    

    /**
     * Converts a Note back to a streamable (vNote, SIF-N) format.
     * (server --> client directrion)
     *
     * @param note
     * @param contentType
     * @throws EntityException if the contentType is wrong or the conversion
     *                         attempt doesn't succeed.
     * @return the result in the required format
     */
    private String convert(Note note, String contentType)
            throws Exception {

        // Finds out which target type is required
        for (int i = 0; i < TYPE.length; i++) {
            if (contentType.equals(TYPE[i])) { // Bingo!

                // Uses the proper converter method
                switch (i) {
                    case PLAINTEXT_FORMAT:
                        return note2plaintext(note);
                    case SIFN_FORMAT:
                        return note2sif(note);
                    default:
                        throw new Exception("Can't make a " + TYPE[i] + "out of a Note!");
                }
            }
        }
        throw new Exception("Content type unknown: " + contentType);
    }


    /**
     * 
     * @param note
     * @return
     * @throws java.lang.Exception
     */
    private String note2sif(Note note) throws Exception {

        if (log.isTraceEnabled()) {
            log.trace("Converting: Note => SIF-N");
        }

        String xml = null;
        try {
            //this.deviceTimeZone, this.deviceCharset
            NoteToSIFN n2xml = new NoteToSIFN(null, null);
            xml = n2xml.convert(note);

            if (Configuration.getConfiguration().isDebugMode()) {
                if (log.isTraceEnabled()) {
                    log.trace("OUTPUT = {" + xml + "}. Conversion done.");
                }
            }
        } catch (Exception e) {
            throw new Exception("Error converting Note to SIF-N. ", e);
        }
        return xml;
    }
    
    /**
     * 
     * @param note
     * @param contentType
     * @return
     */
    private String note2plaintext(Note note) {
        if (log.isTraceEnabled()) {
            log.trace("Converting: Note => TextPlain");
        }
        if (note.getTextDescription() != null) {
            return note.getTextDescription().getPropertyValueAsString();
        }
        return "";
    }

   /**
    * Finds the preferred RX content type, looking through all the datastores.
    *
    * @param context the SyncContext of the current synchronization session.
    * @return a string containing the preferred MIME type ("text/x-s4j-sifn" or
    *         "text/plain"), or null if no preferred MIME type could be found
    *         out.
    */
   protected String findRXContentType(SyncContext context) {
       List<DataStore> dataStores;
       try {
        dataStores = context.getPrincipal()
                            .getDevice()
                            .getCapabilities()
                            .getDevInf()
                            .getDataStores();
       } catch (NullPointerException e) { // something is missing
           return null;
       }
       if (dataStores == null) {
           return null;
       }

       boolean sifN = false;
       boolean textPlain = false;
       for (DataStore dataStore : dataStores) {
           CTInfo rxPref = dataStore.getRxPref();
           if (rxPref != null) {
               if (TYPE[SIFN_FORMAT].equals(rxPref.getCTType())) {
                   sifN = true;
               } else if (TYPE[PLAINTEXT_FORMAT].equals(rxPref.getCTType())) {
                   textPlain = true;
               }
           }
           if (sifN && textPlain) {
               break; // It's useless to cycle again
           }
       }
       if (sifN && !textPlain) {
           return TYPE[SIFN_FORMAT]; // "text/x-s4j-sifn"
       }
       if (!sifN && textPlain) {
           return TYPE[PLAINTEXT_FORMAT]; // "text/plain"
       }

       // sifN  && textPlain  -> ambiguous case
       // !sifN && !textPlain -> no information
       return null;
   }
}
