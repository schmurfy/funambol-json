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
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.TimeZone;

import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.converter.ContactToSIFC;
import com.funambol.common.pim.sif.SIFCParser;
import com.funambol.common.pim.vcard.VcardParser;

import com.funambol.framework.core.AlertCode;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.source.AbstractSyncSource;
import com.funambol.framework.engine.source.MergeableSyncSource;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.engine.source.SyncSourceInfo;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.tools.beans.LazyInitBean;

import com.funambol.server.config.Configuration;

import com.funambol.json.admin.JsonConnectorConfig;
import com.funambol.json.converter.ContactConverter;
import com.funambol.json.dao.JsonDAO;
import com.funambol.json.dao.JsonDAOImpl;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.domain.JsonKeys;
import com.funambol.json.exception.DaoException;
import com.funambol.json.exception.InternalServerErrorException;
import com.funambol.json.exception.JsonConfigException;
import com.funambol.json.exception.MalformedJsonContentException;
import com.funambol.json.manager.JsonContactManager;
import com.funambol.json.manager.JsonManager;
import com.funambol.json.security.JsonUser;
import com.funambol.json.util.SyncSourceUtil;
import com.funambol.json.util.Utility;

/**
 * SyncSource for contact synchronization.
 *
 * @version $Id$
 */
public class ContactSyncSource
extends AbstractSyncSource
implements MergeableSyncSource, Serializable, LazyInitBean {

    //---------------------------------------------------------------- Constants
    public static final int SIFC_FORMAT  = 0; // To be used as index for SIF-Contact
    public static final int VCARD_FORMAT = 1; // To be used as index for VCard
    public static final String[] TYPE = {
        "text/x-s4j-sifc", // SIF-Contact
        "text/x-vcard", // VCard
    };
    protected static final String VERSION_SIFC = "1.0";
    protected static final String VERSION_VCARD = "2.1";
    
    //------------------------------------------------------------- Private data
    private static final long serialVersionUID = 2454307189271131431L;
    private static final FunambolLogger log =
        FunambolLoggerFactory.getLogger(Utility.LOG_NAME);
    private JsonManager<Contact> manager;
    private ContactConverter converter;
    private JsonDAO dao;

    private String rxContentType; // preferred content type as derived from the
                                  // analysis of the DevInf (RXPref)

    //specifies if the sync source should catch a backend server internal error
    private boolean stopSyncOnFatalError = false;
    private boolean vcardIcalBackend;
    private Sync4jPrincipal principal;
    private String username;
    private String sessionID;
    private long since = 0;
    protected String serverTimeZoneID = null;
    private String deviceTimeZoneDescription = null;

    //--------------------------------------------------------------- Properties
    private SyncSourceInfo backendType;
    public SyncSourceInfo getBackendType() {
        return backendType;
    }

    public void setBackendType(SyncSourceInfo backendType) {
        this.backendType = backendType;
    }

    private TimeZone deviceTimeZone = null;
    public TimeZone getDeviceTimeZone() {
        return deviceTimeZone;
    }

    public void setDeviceTimeZone(TimeZone deviceTimeZone) {
        this.deviceTimeZone = deviceTimeZone;
    }
    
    private String deviceCharset = null;
    public String getDeviceCharset() {
        return deviceCharset;
    }

    public void setDeviceCharset(String deviceCharset) {
        this.deviceCharset = deviceCharset;
    }

    //----------------------------------------------------------- Public Methods
    /**
     * Invoked after class instantiation when a server bean is loaded.
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

            this.dao = new JsonDAOImpl("contact");

            this.converter = new ContactConverter();
            this.manager = new JsonContactManager(dao, converter);

            rxContentType = SyncSourceUtil.getContactPreferredType(syncContext);

            //retrieve backend type
            String backend = backendType.getPreferredType().getType();

            try {
                if (TYPE[VCARD_FORMAT].equals(backend)) {
                    vcardIcalBackend = true;
                }
                stopSyncOnFatalError = JsonConnectorConfig.getConfigInstance().getStopSyncOnFatalError();
            } catch (JsonConfigException ex) {
                vcardIcalBackend = false;
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
                log.trace("Starting synchronization"
                        + "\nUser : " + username
                        + "\nUI timezone : " + serverTimeZoneID
                        + "\nSince(gmt ): " + sinceValue
                        + "\nSince(long): " + since);
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
            throw new SyncSourceException("beginSync failed on json backend.", e);
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
            throw new SyncSourceException("endSync failed on json backend.", e);
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
    public SyncItem addSyncItem(SyncItem syncItem) throws SyncSourceException {

        try {

            String key = null;

            String content = getContentFromSyncItem(syncItem);
            String contentType = syncItem.getType();

            Contact contact = convert(content, contentType);

            //retrieve backend type
            String backend = backendType.getPreferredType().getType();

            // Checking if at least one field used for the twin search in the
            // contact we are adding contains meaningful data, otherwise the
            // add operation is not allowed since there's no way to prevent
            // duplication.
            Contact contactTwin = convert(content, contentType);
            JsonItem<Contact> contactForSearch = new JsonItem<Contact>();
            contactForSearch.setItem(contactTwin);
            if (!manager.isTwinSearchAppliableOn(contactForSearch)) {
                if (log.isTraceEnabled()) {
                    log.trace("Rejecting add of contact with key ["
                            + ((syncItem != null && syncItem.getKey() != null)
                            ? syncItem.getKey().getKeyAsString() : "N/A") + "] since the contact doesn't contain any field "
                            + "usable for the twin search.");
                }
                throw new SyncSourceException("Adding a contact without any field "
                        + "usable for twin search set is not allowed.");
            }
            
            if (vcardIcalBackend) {
                //String objRFC = content;
                String objRFC = convert(contact, backend);

                JsonItem<String> contactItem = new JsonItem<String>();
                contactItem.setItem(objRFC);
                contactItem.setKey(null);
                contactItem.setState("N");

                key = manager.addRFCItem(sessionID, contactItem, since);
            } else {


                JsonItem<Contact> contactItem = new JsonItem<Contact>();
                contactItem.setItem(contact);
                contactItem.setKey(null);
                contactItem.setState("N");

                key = manager.addExtendedItem(sessionID, contactItem, since);

            }

            if (key != null) {
                SyncItemImpl newSyncItem = new SyncItemImpl(this, // syncSource
                        key, // key
                        null, // mappedKey
                        SyncItemState.NEW, // state
                        content.getBytes(), // content
                        null, // format
                        contentType, // type
                        null // timestamp @todo
                        );

                return newSyncItem;
            } else {
                throw new Exception("Error adding the item; key is null");
            }

        } catch (DaoException e) {
            log.error("Error in dao module!");
            throw new SyncSourceException("Error adding the item " + syncItem, e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for addSyncItem");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error adding the item " + syncItem, e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content");
            throw new SyncSourceException("Error adding the item " + syncItem, e);
        } catch (Exception e) {
            log.error("Error converting the json content");
            throw new SyncSourceException(e.getMessage(), e);
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
    public SyncItem updateSyncItem(SyncItem syncItem) throws SyncSourceException {


        try {

            String updatedKey = null;

            String content = getContentFromSyncItem(syncItem);
            String contentType = syncItem.getType();
            Contact contact = convert(content, contentType);
            
            //retrieve backend type
            String backend = backendType.getPreferredType().getType();

            if (vcardIcalBackend) {

                //String objRFC = content;
                String objRFC = convert(contact, backend);

                JsonItem<String> contactItem = new JsonItem<String>();
                contactItem.setItem(objRFC);
                contactItem.setContentType(backend);
                contactItem.setKey(syncItem.getKey().getKeyAsString());
                contactItem.setState(String.valueOf(syncItem.getState()));

                JsonItem<String> contactItemUpdated = manager.updateRFCItem(sessionID, contactItem, since);

                updatedKey = contactItemUpdated.getKey();

            } else {

                //Contact contact = convert(content, contentType);

                JsonItem<Contact> contactItem = new JsonItem<Contact>();
                contactItem.setItem(contact);
                contactItem.setContentType(contentType);
                contactItem.setKey(syncItem.getKey().getKeyAsString());
                contactItem.setState(String.valueOf(syncItem.getState()));

                JsonItem<Contact> contactItemUpdated = manager.updateExtendedItem(sessionID, contactItem, since);

                updatedKey = contactItemUpdated.getKey();

            }

            if (updatedKey != null) {
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
            } else {
                throw new Exception("Error updating the item; key is null");
            }

        } catch (DaoException e) {
            log.error("Error in dao module!");
            throw new SyncSourceException("Error updating the item " + syncItem, e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for updateSyncItem");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error updating the item " + syncItem, e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content");
            throw new SyncSourceException("Error updating the item " + syncItem, e);
        } catch (Exception e) {
            log.error("Error converting the json content");
            throw new SyncSourceException("Error updating the item " + syncItem, e);
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

            Contact clientContact = convert(content, contentType);
            JsonItem<Contact> clientContactItem = new JsonItem<Contact>();
            clientContactItem.setItem(clientContact);
            clientContactItem.setContentType(contentType);
            clientContactItem.setKey(syncItem.getKey().getKeyAsString());
            clientContactItem.setState(String.valueOf(syncItem.getState()));

            // the conversion operations are executed on the Foundation Contact Object 

            if (vcardIcalBackend) {

                JsonItem<String> serverItemRFC = manager.getRFCItem(sessionID, serverItemKey);

                JsonItem<Contact> serverContactItem = new JsonItem<Contact>();
                serverContactItem.setContentType(serverItemRFC.getContentType());
                serverContactItem.setKey(serverItemRFC.getKey());
                serverContactItem.setState(String.valueOf(serverItemRFC.getState()));

                String vcard = serverItemRFC.getItem();
                serverContactItem.setItem(vcard2Contact(vcard));

                clientUpdateRequired = manager.mergeRFCItem(sessionID,
                        serverContactItem,
                        clientContactItem,
                        since,
                        false, // not used in this context
                        false, // not used in this context
                        contentType,
                        getDeviceTimeZone(),
                        getDeviceCharset());

            } else {

                JsonItem<Contact> serverContactItem = manager.getExtendedItem(sessionID, serverItemKey);

                clientUpdateRequired = manager.mergeExtendedItem(sessionID, serverContactItem, clientContactItem, since);

            }

            if (clientUpdateRequired) {
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
            String id = itemKey.getKeyAsString();
            manager.removeItem(sessionID, id, since);
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
             //retrieve backend type
            String backend = backendType.getPreferredType().getType();

            //if the backend requires items in vcard/ical formart
            if (vcardIcalBackend) {
                JsonItem<String> contactItem = manager.getRFCItem(sessionID, guid);
                Contact contact = convert(contactItem.getItem(), backend);

                if (contactItem != null) {
                    char status = ' ';
                    // @todo
                    //status = contactItem.getState().charAt(0);
                    syncItem = createSyncItem(guid, contact, status);
                }

            } else {
                JsonItem<Contact> contactItem = manager.getExtendedItem(sessionID, guid);
                if (contactItem != null) {
                    char status = ' ';
                    // @todo
                    //status = contactItem.getState().charAt(0);
                    syncItem = createSyncItem(guid, contactItem.getItem(), status);
                }
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

            JsonKeys keys = null;

            String content = getContentFromSyncItem(syncItem);
            String contentType = syncItem.getType();

            Contact contact = convert(content, contentType);
            
             //retrieve backend type
            String backend = backendType.getPreferredType().getType();

            //if the backend requires items in vcard/ical formart
            if (vcardIcalBackend) {
                //String objRFC = content;
                String objRFC = convert(contact, backend);

                JsonItem<String> contactItem = new JsonItem<String>();
                contactItem.setItem(objRFC);
                contactItem.setKey(null);
                contactItem.setState("N");

                keys = manager.getRFCItemKeysFromTwin(sessionID, contactItem);

            } else {
                

                JsonItem<Contact> contactItem = new JsonItem<Contact>();
                contactItem.setItem(contact);
                contactItem.setKey(null);
                contactItem.setState("N");

                keys = manager.getExtendedItemKeysFromTwin(sessionID, contactItem);
            }

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

    //---------------------------------------------------------- Private Methods
    /**
     * Converts a SyncItem to a Contact object, provided it represents a contact
     * item in VCard or SIF-C format.
     * 
     * @param syncItem
     * @throws Exception
     *                 if the contentType is wrong or the conversion attempt
     *                 doesn't succeed.
     * @return a Contact object
     */
    private Contact convert(SyncItem syncItem) throws Exception {
        return convert(getContentFromSyncItem(syncItem), syncItem.getType());
    }

    /**
     * Converts a contact in vCard or SIF-C format to a Contact object. (client
     * --> server directrion) we have to set the original format of the input
     * data
     * 
     * @param content
     *                as a String
     * @param contentType
     * @throws EntityException
     *                 if the contentType is wrong or the conversion attempt
     *                 doesn't succeed.
     * @return a Contact object
     */
    private Contact convert(String content, String contentType) throws Exception {

        Contact contact = null;

        // Finds out which target type is required
        for (int i = 0; i < TYPE.length; i++) {

            if (contentType.equals(TYPE[i])) { // Bingo!

                // Uses the proper converter method
                switch (i) {
                    case VCARD_FORMAT:
                        contact = vcard2Contact(content);
                        return contact;
                    case SIFC_FORMAT:
                        contact = sif2Contact(content);
                        return contact;
                    default:
                        throw new Exception("Can't make a Contact " + "out of a " + TYPE[i] + "!");
                }
            }
        }
        throw new Exception("Content type unknown: " + contentType);
    }

    /**
     * 
     * @param vcard
     * @return
     * @throws java.lang.Exception
     */
    private Contact vcard2Contact(String vcard) throws Exception {

        if (Configuration.getConfiguration().isDebugMode()) {
            if (log.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder(vcard.length() + 60);
                sb.append("Converting: VCARD => JsonContact").append("\nINPUT = {").append(vcard).append('}');
                log.trace(sb.toString());
            }
        }

        ByteArrayInputStream buffer = null;
        VcardParser parser = null;
        Contact contact = null;
        try {
            contact = new Contact();

            buffer = new ByteArrayInputStream(vcard.getBytes());
            if ((vcard.getBytes()).length > 0) {
                parser = new VcardParser(buffer);
                contact = (Contact) parser.vCard();
            }
        } catch (Exception e) {
            throw new Exception("Error converting VCARD to Contact. ", e);
        }

        if (log.isTraceEnabled()) {
            log.trace("Conversion done.");
        }
        return contact;
    }

    /**
     * 
     * @param sifc
     * @return
     * @throws java.lang.Exception
     */
    private Contact sif2Contact(String sifc) throws Exception {

        if (Configuration.getConfiguration().isDebugMode()) {
            if (log.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder(sifc.length() + 60);
                sb.append("Converting: SIF-C => Contact").append("\nINPUT = {").append(sifc).append('}');
                log.trace(sb.toString());
            }
        }

        ByteArrayInputStream buffer = null;
        SIFCParser parser = null;
        Contact contact = null;
        try {
            contact = new Contact();
            buffer = new ByteArrayInputStream(sifc.getBytes());
            if ((sifc.getBytes()).length > 0) {
                parser = new SIFCParser(buffer);
                contact = (Contact) parser.parse();
            }
        } catch (Exception e) {
            throw new Exception("Error converting SIF-C to Contact. ", e);
        }

        if (log.isTraceEnabled()) {
            log.trace("Conversion done.");
        }
        return contact;
    }

    /**
     * Create a new SyncItem from a Contact. The target contentType and status
     * are passed as arguments.
     *
     * @param contact the Contact object representing the input information
     * @param contentType chosen among the TYPE array's elements
     * @param status
     * @throws EntityException if the content type is wrong or any problem
     *                         occurs while creating a new SyncItem
     * @return a newly created SyncItem object
     */
    private SyncItem createSyncItem(String id, Contact contact, char status)
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
        String stream = convert(contact, contentType);

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
     * Create a new SyncItem from a Contact. The target contentType and status
     * are passed as arguments.
     *
     * @param contact the Contact object representing the input information
     * @param contentType chosen among the TYPE array's elements
     * @param status
     * @throws EntityException if the content type is wrong or any problem
     *                         occurs while creating a new SyncItem
     * @return a newly created SyncItem object
     */
    private SyncItem createSyncItem(String id, String vcard, char status)
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

        try {
            syncItem = new SyncItemImpl(this, id, status);
        } catch (Exception e) {
            throw new Exception(e);
        }

        syncItem.setType(contentType);
        syncItem.setContent(vcard.getBytes());

        if (log.isTraceEnabled()) {
            log.trace("ContactSyncSource created SyncItem");
        }

        return syncItem;
    }

    /**
     * Converts a Contact back to a streamable (vCard, SIF-C) format.
     * (server --> client directrion)
     *
     * @param contact
     * @param contentType
     * @throws EntityException if the contentType is wrong or the conversion
     *                         attempt doesn't succeed.
     * @return the result in the required format
     */
    private String convert(Contact contact, String contentType)
            throws Exception {

        // Finds out which target type is required
        for (int i = 0; i < TYPE.length; i++) {
            if (contentType.equals(TYPE[i])) { // Bingo!

                // Uses the proper converter method
                switch (i) {
                    case VCARD_FORMAT:
                        return UtilitySyncSource.contact2vcard(contact, getDeviceTimeZone(), getDeviceCharset());
                    case SIFC_FORMAT:
                        return contact2sif(contact);
                    default:
                        throw new Exception("Can't make a " + TYPE[i] + "out of a Contact!");
                }
            }
        }
        throw new Exception("Content type unknown: " + contentType);
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
     * 
     * @param contact
     * @return
     * @throws java.lang.Exception
     */
    private String contact2sif(Contact contact) throws Exception {

        if (log.isTraceEnabled()) {
            log.trace("Converting: Contact => SIF-C");
        }
        String xml = null;
        try {
            ContactToSIFC c2xml = new ContactToSIFC(null, null);
            xml = c2xml.convert(contact);
            if (Configuration.getConfiguration().isDebugMode()) {
                if (log.isTraceEnabled()) {
                    log.trace("OUTPUT = {" + xml + "}. Conversion done.");
                }
            }
        } catch (Exception e) {
            throw new Exception("Error converting Contact to SIF-C. ", e);
        }
        return xml;
    }
}
