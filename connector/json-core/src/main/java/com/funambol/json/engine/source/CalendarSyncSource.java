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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.CalendarContent;
import com.funambol.common.pim.calendar.Event;
import com.funambol.common.pim.calendar.Task;
import com.funambol.common.pim.converter.BaseConverter;
import com.funambol.common.pim.converter.CalendarToSIFE;
import com.funambol.common.pim.converter.TaskToSIFT;
import com.funambol.common.pim.sif.SIFCalendarParser;

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

import com.funambol.server.config.Configuration;

import com.funambol.json.admin.JsonConnectorConfig;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.domain.JsonKeys;
import com.funambol.json.domain.JsonTimeConfiguration;
import com.funambol.json.exception.DaoException;
import com.funambol.json.exception.InternalServerErrorException;
import com.funambol.json.exception.JsonConfigException;
import com.funambol.json.exception.MalformedJsonContentException;
import com.funambol.json.security.JsonUser;
import com.funambol.json.util.Utility;

/**
 * This class implements the methods required by the SyncSource interface.
 * 
 * @version $Id: CalendarSyncSource.java 51920 2010-06-04 05:10:49Z gazzaniga $
 */
public class CalendarSyncSource extends AbstractSyncSource
        implements MergeableSyncSource, Serializable, LazyInitBean {

    private static final long serialVersionUID = 2454307189271131431L;
    protected static final FunambolLogger log = FunambolLoggerFactory.getLogger(Utility.LOG_NAME);
    private Map<String, CalendarSyncSourceStrategy> strategies = null;
    public static final int SIFE_FORMAT = 0; // To be used as index for SIF-Event
    public static final int SIFT_FORMAT = 1; // To be used as index for SIF-Task
    public static final int VCAL_FORMAT = 2; // To be used as index for VCal
    public static final int ICAL_FORMAT = 3; // To be used as index for ICal
    public static final String[] TYPE = {
        "text/x-s4j-sife", // SIF-Event
        "text/x-s4j-sift", // SIF-Task
        "text/x-vcalendar", // VCal
        "text/calendar", // ICal
    };
    public static final String TYPE_ANYSIF = "text/x-s4j-sif?";
    private String rxContentType; // preferred content type as derived from the
    // analysis of the DevInf (RXPref)
    //specifies if the sync source should catch a backend server internal error
    private boolean stopSyncOnFatalError = false;
    //specifies if the backend used webcalendar (ical/vcal) or json extended format
    private boolean vcalIcalBackend = false;
    // specifies if the backend requires to receive items in vcal format
    // (used only when vcardIcalBackend =true)
    private boolean vcalFormat = false;
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
    private Class entityType;

    public Class getEntityType() {
        return entityType;
    }

    public void setEntityType(Class entityType) {
        this.entityType = entityType;
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
     * @param syncContext the context of the sync.
     * @throws SyncSourceException
     * 
     * @see SyncContext
     */
    @Override
    public void beginSync(SyncContext syncContext) throws SyncSourceException {

        try {
            strategies = new HashMap<String, CalendarSyncSourceStrategy>();
            strategies.put(Event.class.getName(), new AppointmentSyncSourceStrategy());
            strategies.put(Task.class.getName(), new TaskSyncSourceStrategy());

            rxContentType = findRXContentType(syncContext);

            try {
                vcalIcalBackend = JsonConnectorConfig.getConfigInstance().getVcardIcalBackend();
                vcalFormat = JsonConnectorConfig.getConfigInstance().getVcalFormat();
                stopSyncOnFatalError = JsonConnectorConfig.getConfigInstance().getStopSyncOnFatalError();
            } catch (JsonConfigException ex) {
                vcalIcalBackend = false;
                vcalFormat = false;
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

            JsonTimeConfiguration configuration = null;

            // begin sync must be executed for both types (event and task) 
            // when the syncsource requires it
            for (CalendarSyncSourceStrategy strategy : selectSyncSources()) {

                // the backend server returns the timezone of the UI
                if (configuration == null) {
                    // executes just 1 time
                    /*
                     * @todo .. How to get the timezone of the UI
                     */
                    try {
                        configuration = strategy.getManager().getTimeConfiguration(this.sessionID);
                        if (configuration != null) {
                            this.serverTimeZoneID = configuration.getTimeZoneID();
                        } else {
                            this.serverTimeZoneID = "UTC";
                        }
                    } catch (Exception e) {
                        log.error("Cannot get the time from the Backend Server; Set the default TZ!");
                        this.serverTimeZoneID = "UTC";
                    }
                }

                if (log.isTraceEnabled()) {
                    log.trace("Starting synchronization"
                            + "\nUser : " + username
                            + "\nCalendar Operation : " + strategy.getClass().getName()
                            + "\nUI timezone : " + serverTimeZoneID
                            + "\nSince(gmt ): " + syncContext.getTo()
                            + "\nSince(long): " + since);
                }

                strategy.getManager().beginSync(sessionID,
                        since,
                        AlertCode.getAlertDescription(syncMode),
                        serverTimeZoneID);

                if (syncMode == AlertCode.REFRESH_FROM_CLIENT) {
                    if (log.isTraceEnabled()) {
                        log.trace("Refresh From Client Sync Method");
                    }
                    strategy.getManager().removeAllItems(sessionID, since);
                }

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
     * @throws SyncSourceException to interrupt the process with an error
     */
    @Override
    public void endSync() throws SyncSourceException {
        if (log.isTraceEnabled()) {
            log.trace("Ending synchronization");
        }
        try {
            //Invoke endSync on backend server....
            // endSync must be executed for both types (event and task) when the syncsource requires it
            for (CalendarSyncSourceStrategy strategy : selectSyncSources()) {
                strategy.getManager().endSync(sessionID);
            }
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

            JsonKeys keys;

            SyncItemKey[] keyArrayAll = new SyncItemKey[0];

            for (CalendarSyncSourceStrategy strategy : selectSyncSources()) {
                keys = strategy.getAllItemsKey(sessionID);
                SyncItemKey[] keyArray = extractKeyArrayFromKeys(keys);
                keyArrayAll = mergeTwoArrayOfSyncItemKey(keyArrayAll, keyArray);
            }

            return keyArrayAll;

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
            JsonKeys keys;

            SyncItemKey[] keyArrayAll = new SyncItemKey[0];

            for (CalendarSyncSourceStrategy strategy : selectSyncSources()) {
                keys = strategy.getUpdatedItemKeys(sessionID, sinceTs.getTime(), untilTs.getTime());
                SyncItemKey[] keyArray = extractKeyArrayFromKeys(keys);
                keyArrayAll = mergeTwoArrayOfSyncItemKey(keyArrayAll, keyArray);
            }

            return keyArrayAll;

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

            JsonKeys keys;

            SyncItemKey[] keyArrayAll = new SyncItemKey[0];

            for (CalendarSyncSourceStrategy strategy : selectSyncSources()) {
                keys = strategy.getDeletedItemKeys(sessionID, sinceTs.getTime(), untilTs.getTime());
                SyncItemKey[] keyArray = extractKeyArrayFromKeys(keys);
                keyArrayAll = mergeTwoArrayOfSyncItemKey(keyArrayAll, keyArray);
            }

            return keyArrayAll;

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
            JsonKeys keys;

            SyncItemKey[] keyArrayAll = new SyncItemKey[0];

            for (CalendarSyncSourceStrategy strategy : selectSyncSources()) {
                keys = strategy.getNewItemKeys(sessionID, sinceTs.getTime(), untilTs.getTime());
                SyncItemKey[] keyArray = extractKeyArrayFromKeys(keys);
                keyArrayAll = mergeTwoArrayOfSyncItemKey(keyArrayAll, keyArray);
            }

            return keyArrayAll;

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
     * @param syncItem the item to add
     * @return the inserted item
     * @throws SyncSourceException if an error occurs
     */
    public SyncItem addSyncItem(SyncItem syncItem) throws SyncSourceException {

        Calendar calendar = null;
        String content = null;
        String contentToServer = null;

        try {
            String contentType = syncItem.getType();

            if (log.isTraceEnabled()) {
                log.trace("Adding an item type [" + contentType + "] for a "
                        + " backend that uses only ical/vcal [" + vcalIcalBackend
                        + "] since the item format is [" + vcalFormat + "]");
            }

            content = getContentFromSyncItem(syncItem);

            calendar = convert(content, contentType);


            contentToServer = convertCalendarToBackendRFC(calendar, content);

            CalendarSyncSourceStrategy cstrategy =
                    CalendarStrategyFactory.get(strategies, calendar.getCalendarContent());

            // Checking if at least one field used for the twin search in the
            // calendar we are adding contains meaningful data, otherwise the
            // add operation is not allowed since there's no way to prevent
            // duplication.
            CalendarContent cc = calendar.getCalendarContent();
            boolean isTwinSearchValue = false;
            if (cc instanceof Event) {
                JsonItem<Event> eventForSearch = new JsonItem<Event>();
                eventForSearch.setItem(calendar.getEvent());
                isTwinSearchValue = cstrategy.getManager().isTwinSearchAppliableOn(eventForSearch);
            } else { //else it's a Task
                JsonItem<Task> taskForSearch = new JsonItem<Task>();
                taskForSearch.setItem(calendar.getTask());
                isTwinSearchValue = cstrategy.getManager().isTwinSearchAppliableOn(taskForSearch);
            }

            if (!isTwinSearchValue) {
                if (log.isTraceEnabled()) {
                    log.trace("Rejecting add of calendar with key ["
                            + ((syncItem != null && syncItem.getKey() != null)
                            ? syncItem.getKey().getKeyAsString() : "N/A") + "] since that calendar doesn't contain any field "
                            + "usable for twin search.");
                }
                throw new SyncSourceException("Adding a calendar without any field "
                        + "usable for twin search set is not allowed.");
            }



            String guid =
                    cstrategy.add(sessionID, calendar, contentToServer, vcalIcalBackend, since);

            if (guid != null) {
                SyncItemImpl newSyncItem = new SyncItemImpl(this, // syncSource
                        guid, // key
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
            throw new SyncSourceException("Error adding the item " + syncItem, e);
        }
    }

    /**
     * Updates a <code>SyncItem</code>. The item is also returned giving the
     * opportunity to the source to modify its content and return the updated
     * item (i.e. updating the id to the GUID).
     * 
     * @param syncItem the item to replace
     * @return the updated item
     * @throws SyncSourceException if an error occurs
     */
    public SyncItem updateSyncItem(SyncItem syncItem) throws SyncSourceException {

        Calendar calendar = null;
        String content = null;
        String contentToServer = null;
        try {
            content = getContentFromSyncItem(syncItem);
            String contentType = syncItem.getType();

            if (log.isTraceEnabled()) {
                log.trace("Updating the item type [" + contentType + "] for a "
                        + " backend that uses only ical/vcal [" + vcalIcalBackend
                        + "] since the item format is [" + vcalFormat + "]");
            }

            calendar = convert(content, contentType);
            contentToServer = convertCalendarToBackendRFC(calendar, content);
            CalendarSyncSourceStrategy cstrategy =
                    CalendarStrategyFactory.get(strategies, calendar.getCalendarContent());

            String guid = cstrategy.update(sessionID,
                    calendar,
                    syncItem,
                    contentToServer,
                    vcalIcalBackend,
                    since);

            SyncItemImpl newSyncItem = new SyncItemImpl(this, // syncSource
                    guid, // key
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

            CalendarSyncSourceStrategy strategy = CalendarStrategyFactory.getById(strategies, guid);

            strategy.remove(sessionID, itemKey.getKeyAsString(), since);

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
        SyncItem syncItem = null;

        try {

            String guid = syncItemKey.getKeyAsString();

            CalendarSyncSourceStrategy strategy = CalendarStrategyFactory.getById(strategies, guid);


            //if the backend requires items in vcard/ical formart
            if (vcalIcalBackend) {
                JsonItem<String> item = strategy.getRFCItem(sessionID, guid);
                //converts from ical to vcal or vice versa (if needed)
                item.setItem(convertFromServerRFCToClientFormat(item.getItem()));

                if (item != null) {
                    char status = ' ';
                    // @todo
                    //status = item.getState().charAt(0);
                    syncItem = createSyncItem(guid, item.getItem(), status);
                }
            } else {
                JsonItem<CalendarContent> item = strategy.getExtendedItem(sessionID, guid);
                if (item != null) {
                    Calendar calendar = new Calendar();
                    calendar.setCalendarContent(item.getItem());
                    char status = ' ';
                    // @todo
                    //status = item.getState().charAt(0);
                    syncItem = createSyncItem(guid, calendar, status);
                }
            }

        } catch (DaoException e) {
            log.error("Error in dao module for getSyncItemFromId!");
            throw new SyncSourceException("Error retrieving item with key: " + syncItemKey.getKeyAsString(), e);
        } catch (InternalServerErrorException e) {
            log.error("Internal server error for getSyncItemFromId");
            if (this.stopSyncOnFatalError) {
                throw new RuntimeException(e.getMessage());
            } else {
                throw new SyncSourceException("Error retrieving item with key: " + syncItemKey.getKeyAsString(), e);
            }
        } catch (MalformedJsonContentException e) {
            log.error("Error converting the json content for getSyncItemFromId");
            throw new SyncSourceException("Error retrieving item with key: " + syncItemKey.getKeyAsString(), e);
        } catch (Exception e) {
            log.error("Error converting the json content for getSyncItemFromId");
            throw new SyncSourceException("Error retrieving item with key: " + syncItemKey.getKeyAsString(), e);
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

            String content = getContentFromSyncItem(syncItem);

            Calendar calendar = convert(content, syncItem.getType());

            String contentToServer = convertCalendarToBackendRFC(calendar, content);

            CalendarSyncSourceStrategy cstrategy = CalendarStrategyFactory.get(strategies, calendar.getCalendarContent());

            JsonKeys keys = cstrategy.getKeysFromTwin(sessionID,
                    calendar,
                    syncItem,
                    contentToServer,
                    vcalIcalBackend);

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
            log.error("Error converting the syncItem to event for getSyncItemKeysFromTwin");
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

            String content = getContentFromSyncItem(syncItem);
            // this is the content type from the client (usually vcal 1.0)
            String contentType = syncItem.getType();
            Calendar calendar = convert(content, contentType);

            CalendarSyncSourceStrategy cstrategy = CalendarStrategyFactory.get(strategies, calendar.getCalendarContent());

            String GUID = syncItemKey.getKeyAsString();

            boolean clientUpdateRequired = cstrategy.merge(sessionID,
                    GUID,
                    calendar,
                    syncItem,
                    vcalIcalBackend,
                    vcalFormat,
                    contentType, // client content type
                    deviceTimeZone,
                    deviceCharset,
                    since);

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

    /**
     * 
     * @param arrayAll
     * @param arrayToImport
     * @return
     */
    protected SyncItemKey[] mergeTwoArrayOfSyncItemKey(SyncItemKey[] arrayAll, SyncItemKey[] arrayToImport) {
        List<SyncItemKey> listAll = new ArrayList<SyncItemKey>(Arrays.asList(arrayAll));
        List<SyncItemKey> listToImport = new ArrayList<SyncItemKey>(Arrays.asList(arrayToImport));
        listAll.addAll(listToImport);
        SyncItemKey[] newAll = new SyncItemKey[listAll.size()];
        for (int i = 0; i < listAll.size(); i++) {
            newAll[i] = listAll.get(i);
        }
        return newAll;
    }

    /**
     * Create a new SyncItem from a Calendar. The status is passed as an
     * argument.
     * 
     * @param calendar
     *                the Calendar object representing the input information
     * @param status
     * @throws EntityException
     *                 if the content type is wrong or any problem occurs while
     *                 creating a new SyncItem
     * @return a newly created SyncItem object
     */
    private SyncItem createSyncItem(String id, Calendar calendar, char status) throws Exception {

        String contentType;
        if (rxContentType != null) { // device capabilities can be used
            if (TYPE_ANYSIF.equals(rxContentType)) {
                if (entityType.isAssignableFrom(Event.class)) {
                    contentType = TYPE[SIFE_FORMAT];
                } else if (entityType.isAssignableFrom(Task.class)) {
                    contentType = TYPE[SIFT_FORMAT];
                } else { // This should not happen
                    // Uses user-set default
                    contentType = getInfo().getPreferredType().getType();
                }
            } else {
                contentType = rxContentType;
            }

        } else { // user-set default must be used
            contentType = getInfo().getPreferredType().getType();
        }

        SyncItem syncItem = null;
        String stream = convert(calendar, contentType);

        try {
            syncItem = new SyncItemImpl(this, id, status);
        } catch (Exception e) {
            throw new Exception(e);
        }

        syncItem.setType(contentType);
        syncItem.setContent(stream.getBytes());

        if (log.isTraceEnabled()) {
            log.trace("CalendarSyncSource created SyncItem");
        }

        return syncItem;
    }

    /**
     * Create a new SyncItem from a Calendar. The status is passed as an
     * argument.
     * 
     * @param calendar
     *                the Calendar object representing the input information
     * @param status
     * @throws EntityException
     *                 if the content type is wrong or any problem occurs while
     *                 creating a new SyncItem
     * @return a newly created SyncItem object
     */
    private SyncItem createSyncItem(String id, String vcal, char status) throws Exception {

        String contentType;
        if (rxContentType != null) { // device capabilities can be used
            if (TYPE_ANYSIF.equals(rxContentType)) {
                if (entityType.isAssignableFrom(Event.class)) {
                    contentType = TYPE[SIFE_FORMAT];
                } else if (entityType.isAssignableFrom(Task.class)) {
                    contentType = TYPE[SIFT_FORMAT];
                } else { // This should not happen
                    // Uses user-set default
                    contentType = getInfo().getPreferredType().getType();
                }
            } else {
                contentType = rxContentType;
            }

        } else { // user-set default must be used
            contentType = getInfo().getPreferredType().getType();
        }

        SyncItem syncItem = null;

        try {
            syncItem = new SyncItemImpl(this, id, status);
        } catch (Exception e) {
            throw new Exception(e);
        }

        syncItem.setType(contentType);
        syncItem.setContent(vcal.getBytes());

        if (log.isTraceEnabled()) {
            log.trace("CalendarSyncSource created SyncItem");
        }

        return syncItem;
    }

    /**
     * Converts a calendar in vCalendar/iCalendar, SIF-E or SIF-T format to a
     * Calendar object.
     * 
     * @param content
     *                as a String
     * @param contentType
     * @throws EntityException
     *                 if the contentType is wrong or the conversion attempt
     *                 doesn't succeed.
     * @return a Calendar object
     */
    private Calendar convert(String content, String contentType) throws Exception {

        // Finds out which target type is required
        for (int i = 0; i < TYPE.length; i++) {
            if (contentType.equals(TYPE[i])) {

                // Uses the proper converter method
                switch (i) {
                    case VCAL_FORMAT:
                    case ICAL_FORMAT:
                        return UtilitySyncSource.webCalendar2Calendar(content, contentType, deviceTimeZone, deviceCharset);
                    case SIFE_FORMAT:
                    case SIFT_FORMAT:
                        return sif2Calendar(content);
                    default:
                        throw new Exception("Can't make a event " + "out of a " + TYPE[i] + "!");
                }
            }
        }
        throw new Exception("Content type unknown: " + contentType);
    }

    /**
     * converts from ical to vcal or vice versa
     * @param content
     * @param sourceContenType
     * @param targetContentType
     * @return
     * @throws java.lang.Exception
     */
    private String convert(String content, String sourceContenType, String targetContentType) throws Exception {
        Calendar calendar = convert(content, sourceContenType);
        return convert(calendar, targetContentType);
    }

    /**
     * Converts a Calendar back to a streamable (vCalendar/iCalendar, SIF-E or
     * SIF-T) format.
     * 
     * @param calendar
     * @param contentType
     * @throws EntityException
     *                 if the contentType is wrong or the conversion attempt
     *                 doesn't succeed.
     * @return the result in the required format
     */
    private String convert(Calendar calendar, String contentType) throws Exception {

        // Finds out which target type is required
        for (int i = 0; i < TYPE.length; i++) {
            if (contentType.equals(TYPE[i])) {

                // Uses the proper converter method
                switch (i) {
                    case VCAL_FORMAT:
                    case ICAL_FORMAT:
                        return UtilitySyncSource.calendar2webCalendar(calendar, contentType, this.deviceTimeZone, this.deviceCharset);
                    case SIFE_FORMAT:
                    case SIFT_FORMAT:
                        return calendar2sif(calendar, contentType, this.deviceTimeZone, this.deviceCharset);
                    default:
                        throw new Exception("Can't make a " + TYPE[i] + "out of a Event!");
                }
            }
        }
        throw new Exception("Content type unknown: " + contentType);
    }

    /**
     * 
     * @param calendar 
     * @param sifType 
     * @throws com.funambol.ox.exception.EntityException 
     * @return 
     */
    public String calendar2sif(Calendar calendar,
            String sifType,
            TimeZone deviceTimeZone,
            String deviceCharset)
            throws Exception {

        if (log.isTraceEnabled()) {
            log.trace("Converting: Calendar => " + sifType);
        }

        String xml = null;
        BaseConverter c2xml;
        Object thing;

        try {
            if (sifType.equals(TYPE[SIFE_FORMAT])) { // SIF-E
                c2xml = new CalendarToSIFE(deviceTimeZone, deviceCharset);
                thing = calendar;
                // NB: A CalendarToSIFE converts a Calendar into a SIF-E
            } else { // SIF-T
                c2xml = new TaskToSIFT(deviceTimeZone, deviceCharset);
                thing = calendar.getTask();
                // NB: A TaskToSIFT converts just a Task into a SIF-T
            }

            xml = c2xml.convert(thing);

            // patch for complete field
            xml = xml.replace("<Complete>false</Complete>", "<Complete>0</Complete>");
            xml = xml.replace("<Complete>true</Complete>", "<Complete>1</Complete>");

            if (Configuration.getConfiguration().isDebugMode()) {
                if (log.isTraceEnabled()) {
                    log.trace("OUTPUT = {" + xml + "}. Conversion done.");
                }
            }
        } catch (Exception e) {
            throw new Exception("Error converting Calendar to " + sifType, e);
        }
        return xml;
    }

    /**
     * 
     * 
     * @param xml
     * @return
     * @throws com.funambol.ox.exception.EntityException
     */
    public Calendar sif2Calendar(String xml)
            throws Exception {

        if (Configuration.getConfiguration().isDebugMode()) {
            if (log.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder(xml.length() + 60);
                sb.append("Converting Calendar: ").append("\nINPUT = {").append(xml).append('}');
                log.trace(sb.toString());
            }
        }

        ByteArrayInputStream buffer = null;
        Calendar calendar = null;
        try {
            calendar = new Calendar();
            buffer = new ByteArrayInputStream(xml.getBytes());
            if ((xml.getBytes()).length > 0) {
                SIFCalendarParser parser = new SIFCalendarParser(buffer);
                calendar = parser.parse();
            }
        } catch (Exception e) {
            throw new Exception("Error converting Calendar. ", e);
        }

        if (log.isTraceEnabled()) {
            log.trace("Conversion done.");
        }
        return calendar;
    }

    //----------------------------------------------------------- Private Methods
    /**
     * 
     * @return
     */
    private Collection<CalendarSyncSourceStrategy> selectSyncSources() {
        Collection<CalendarSyncSourceStrategy> c =
                CalendarStrategyFactory.getStrategies(strategies, getEntityType().getName());
        return c;
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
     * converts a calendar object to RFC (vcal or ical)
     * @param calendar
     * @param content
     * @return
     * @throws java.lang.Exception
     */
    private String convertCalendarToBackendRFC(Calendar calendar, String content) throws Exception {
        if (vcalIcalBackend) {
            if (vcalFormat) {
                return convert(calendar, TYPE[VCAL_FORMAT]);
            } else {
                return convert(calendar, TYPE[ICAL_FORMAT]);
            }
        }
        return content;
    }

    /**
     * converts the item in rfc (vcal or ical) format received from the backend
     * to a given format
     * @param content
     * @return
     * @throws java.lang.Exception
     */
    private String convertFromServerRFCToClientFormat(String content) throws Exception {

        //backend is returning vcal and client expects vcal
        if (TYPE[VCAL_FORMAT].equals(getInfo().getPreferredType().getType()) && (vcalFormat)) {
            return content;
        }

        //backend is returning ical and client expects ical
        if (TYPE[ICAL_FORMAT].equals(getInfo().getPreferredType().getType()) && (!vcalFormat)) {
            return content;
        }

        //backend is returning ical and client expects vcal
        if ((TYPE[VCAL_FORMAT].equals(getInfo().getPreferredType().getType()) && (!vcalFormat))) {
            //convert vcal to ical
            return convert(content, TYPE[ICAL_FORMAT], TYPE[VCAL_FORMAT]);
        }

        //backend is returning vcal and client expects ical
        if ((TYPE[ICAL_FORMAT].equals(getInfo().getPreferredType().getType()) && (vcalFormat))) {
            //convert from ical to vcal
            return convert(content, TYPE[VCAL_FORMAT], TYPE[ICAL_FORMAT]);
        }


        //backend is returning vcal and client expects sif-e
        if ((TYPE[SIFE_FORMAT].equals(getInfo().getPreferredType().getType()) && (vcalFormat))) {
            //convert from ical to vcal
            return convert(content, TYPE[VCAL_FORMAT], TYPE[SIFE_FORMAT]);
        }

        //backend is returning vcal and client expects sif-t
        if ((TYPE[SIFT_FORMAT].equals(getInfo().getPreferredType().getType()) && (vcalFormat))) {
            //convert from ical to vcal
            return convert(content, TYPE[VCAL_FORMAT], TYPE[SIFT_FORMAT]);
        }

        //backend is returning ical and client expects sif-e
        if ((TYPE[SIFE_FORMAT].equals(getInfo().getPreferredType().getType()) && (!vcalFormat))) {
            //convert from ical to vcal
            return convert(content, TYPE[ICAL_FORMAT], TYPE[SIFE_FORMAT]);
        }

        //backend is returning ical and client expects sif-t
        if ((TYPE[SIFT_FORMAT].equals(getInfo().getPreferredType().getType()) && (!vcalFormat))) {
            //convert from ical to vcal
            return convert(content, TYPE[ICAL_FORMAT], TYPE[SIFT_FORMAT]);
        }



        return content;

    }

    /**
     * Finds the preferred RX content type, looking through all the datastores.
     *
     * @param context the SyncContext of the current synchronization session.
     * @return a string containing the preferred MIME type ("text/x-s4j-sife",
     *         "text/x-s4j-sift", "text/x-vcalendar", "text/calendar") or null
     *         if no preferred MIME type could be found out.
     */
    protected String findRXContentType(SyncContext context) {
        List<DataStore> dataStores;
        try {
            dataStores = context.getPrincipal().getDevice().getCapabilities().getDevInf().getDataStores();
        } catch (NullPointerException e) { // something is missing
            return null;
        }
        if (dataStores == null) {
            return null;
        }

        boolean xvCalendar = false;
        boolean iCalendar = false;
        boolean sif = false;
        for (DataStore dataStore : dataStores) {
            CTInfo rxPref = dataStore.getRxPref();
            if (rxPref != null) {
                if (TYPE[VCAL_FORMAT].equals(rxPref.getCTType())) {
                    xvCalendar = true;
                } else if (TYPE[ICAL_FORMAT].equals(rxPref.getCTType())) {
                    iCalendar = true;
                } else if (TYPE[SIFE_FORMAT].equals(rxPref.getCTType())
                        || TYPE[SIFT_FORMAT].equals(rxPref.getCTType())) {
                    sif = true;
                }
            }
            if (xvCalendar && iCalendar && sif) {
                break; // It's useless to cycle again
            }
        }
        if (xvCalendar && !iCalendar && !sif) {
            return TYPE[VCAL_FORMAT]; // "text/x-vcalendar"
        }
        if (!xvCalendar && iCalendar && !sif) {
            return TYPE[ICAL_FORMAT]; // "text/calendar"
        }
        if (!xvCalendar && !iCalendar && sif) {
            return TYPE_ANYSIF; // "text/x-s4j-sif?"
        }


        // more than one type  -> ambiguous case
        // no type             -> no information
        return null;
    }
}
