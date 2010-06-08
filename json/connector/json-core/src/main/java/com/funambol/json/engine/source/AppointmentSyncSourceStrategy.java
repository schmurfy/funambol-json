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

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.Event;
import com.funambol.framework.engine.SyncItem;
import com.funambol.json.converter.AppointmentConverter;
import com.funambol.json.dao.JsonDAO;
import com.funambol.json.dao.JsonDAOImpl;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.domain.JsonKeys;
import com.funambol.json.exception.DaoException;
import com.funambol.json.exception.JsonConfigException;
import com.funambol.json.exception.MalformedJsonContentException;
import com.funambol.json.manager.JsonAppointmentManager;
import com.funambol.json.util.Utility;
import java.util.TimeZone;


/**
 * @version $Id: AppointmentSyncSourceStrategy.java 51920 2010-06-04 05:10:49Z gazzaniga $
 */
public class AppointmentSyncSourceStrategy implements CalendarSyncSourceStrategy {

    /**
     * 
     */
    private JsonAppointmentManager manager;

    public JsonAppointmentManager getManager() {
	return manager;
    }

    
    //------------------------------------------------------------- Constructors
    
    public AppointmentSyncSourceStrategy() throws JsonConfigException {
        JsonDAO dao = new JsonDAOImpl("appointment");
        AppointmentConverter converter = new AppointmentConverter();
        manager = new JsonAppointmentManager(dao,converter);
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#add(com.funambol.common.pim.calendar.Calendar)
     */
    public String add(String sessionID, 
                      Calendar calendar, 
                      String content, 
                      boolean vcardIcalBackend, 
                      long since) 
      throws MalformedJsonContentException, DaoException {
        
        String id = null;
        String GUID = null;

        //if the backend requires items in vcard/ical formart
        if (vcardIcalBackend) {

            String objRFC = content;
            
            JsonItem<String> eventItem = new JsonItem<String>();
            eventItem.setItem(objRFC);
            eventItem.setKey(null);
            eventItem.setState("N");

            id = manager.addRFCItem(sessionID, eventItem, since);

        } else {

            JsonItem<Event> eventItem = new JsonItem<Event>();
            eventItem.setItem(calendar.getEvent());
            eventItem.setKey(null);
            eventItem.setState("N");

            id = manager.addExtendedItem(sessionID, eventItem, since);
        }
        
        GUID = Utility.addPrefix(Utility.APPOINTMENT_OBJECT, id);
                    
        return GUID;
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.Json.engine.CalendarSyncSourceStrategy#getKeysFromTwin(com.funambol.common.pim.calendar.Calendar,
     *      com.funambol.framework.engine.SyncItem)
     */
    public JsonKeys getKeysFromTwin(String token, 
                                    Calendar calendar,
                                    SyncItem syncItem,
                                    String content, 
                                    boolean vcardIcalBackend)
       throws MalformedJsonContentException, DaoException {
        
        JsonKeys keys = null;
        
        
        //if the backend requires items in vcard/ical formart
        if (vcardIcalBackend) {

            String objRFC = content;
            
            JsonItem<String> eventItem = new JsonItem<String>();
            eventItem.setItem(objRFC);
            eventItem.setKey(syncItem.getKey().getKeyAsString());
            eventItem.setState(String.valueOf(syncItem.getState()));

            keys = manager.getRFCItemKeysFromTwin(token, eventItem);

        } else {

            JsonItem<Event> eventItem = new JsonItem<Event>();
            eventItem.setItem(calendar.getEvent());
            eventItem.setKey(syncItem.getKey().getKeyAsString());
            eventItem.setState(String.valueOf(syncItem.getState()));

            keys = manager.getExtendedItemKeysFromTwin(token, eventItem);
        }
        
        Utility.addPrefix(Utility.APPOINTMENT_OBJECT, keys);

        return keys;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#get(java.lang.String)
     */
    public JsonItem<String> getRFCItem(String sessionID, String GUID) 
        throws MalformedJsonContentException, DaoException, Exception {        
        String id = Utility.removePrefix(GUID);
        JsonItem<String> eventItem = manager.getRFCItem(sessionID, id);
        return eventItem;        
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#get(java.lang.String)
     */
    public JsonItem getExtendedItem(String sessionID, String GUID) 
        throws MalformedJsonContentException, DaoException, Exception {
        String id = Utility.removePrefix(GUID);
        JsonItem<Event> eventItem = manager.getExtendedItem(sessionID, id);
        return eventItem;        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#update(com.funambol.common.pim.calendar.Calendar,
     *      com.funambol.framework.engine.SyncItem)
     */
    public String update(String sessionID, 
                         Calendar calendar, 
                         SyncItem syncItem, 
                         String content, 
                         boolean vcardIcalBackend,
                         long since) 
       throws MalformedJsonContentException, DaoException {
        
        String updatedKey = null;
        
        if (vcardIcalBackend) {

            String objRFC = content;

            JsonItem<String> eventItem = new JsonItem<String>();
            eventItem.setItem(objRFC);
            eventItem.setContentType(syncItem.getType());
            eventItem.setState(String.valueOf(syncItem.getState()));

            String GUID = syncItem.getKey().getKeyAsString();
            String id   = Utility.removePrefix(GUID);
            eventItem.setKey(id);
        
            JsonItem<String> eventItemUpdated = manager.updateRFCItem(sessionID, eventItem, since);

            //Get the id from the syncInstance to update
            id   = eventItemUpdated.getKey();
            updatedKey = Utility.addPrefix(Utility.APPOINTMENT_OBJECT, id);
            

        } else {

            JsonItem<Event> eventItem = new JsonItem<Event>();
            
            eventItem.setItem(calendar.getEvent());
            eventItem.setContentType(syncItem.getType());
            eventItem.setState(String.valueOf(syncItem.getState()));

            String GUID = syncItem.getKey().getKeyAsString();
            String id   = Utility.removePrefix(GUID);
            eventItem.setKey(id);           

            JsonItem<Event> eventItemUpdated = manager.updateExtendedItem(sessionID, eventItem, since);

            //Get the id from the syncInstance to update
            id   = eventItemUpdated.getKey();
            updatedKey = Utility.addPrefix(Utility.APPOINTMENT_OBJECT, id);

        }
        
        return updatedKey;
        
    }
       

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#update(com.funambol.common.pim.calendar.Calendar,
     *      com.funambol.framework.engine.SyncItem)
     */
    public boolean merge(String token, 
                         String GUID, 
                         Calendar calendar, 
                         SyncItem syncItem, 
                         boolean vcardIcalBackend,
                         boolean vcalFormat,
                         String vCalType,   // content type from client (usually vcal 1.0)
                         TimeZone deviceTimeZone, 
                         String deviceCharset,
                         long since) 
       throws MalformedJsonContentException, DaoException, Exception {
        
        JsonItem<Event> eventClientItem = new JsonItem<Event>();    
        eventClientItem.setItem(calendar.getEvent());        
        eventClientItem.setContentType(syncItem.getType());
        eventClientItem.setState(String.valueOf(syncItem.getState()));
                        
        boolean clientUpdateRequired = false;

        String id   = Utility.removePrefix(GUID);

        if (vcardIcalBackend) {
            
            JsonItem<String> eventServerRFC = manager.getRFCItem(token, id);
            
            JsonItem<Event> serverEventItem = new JsonItem<Event>();
            serverEventItem.setContentType(eventServerRFC.getContentType());
            serverEventItem.setState(String.valueOf(eventServerRFC.getState()));
            
            String vcal = eventServerRFC.getItem();
            // create a calendar using the value from the client
            Calendar c = UtilitySyncSource.webCalendar2Calendar(vcal, vCalType, deviceTimeZone, deviceCharset);
            serverEventItem.setItem(c.getEvent()); 

    
            serverEventItem.setKey(Utility.addPrefix(Utility.APPOINTMENT_OBJECT, id));

           
            clientUpdateRequired = manager.mergeRFCItem(token,
                                                        serverEventItem,
                                                        eventClientItem,
                                                        since,
                                                        vcardIcalBackend,
                                                        vcalFormat,
                                                        vCalType,
                                                        deviceTimeZone,
                                                        deviceCharset);
            
        } else {
            
            JsonItem<Event> serverEventItem = manager.getExtendedItem(token, id);
            
            id = serverEventItem.getKey();
            serverEventItem.setKey(Utility.addPrefix(Utility.APPOINTMENT_OBJECT, id));
            
            clientUpdateRequired = manager.mergeExtendedItem(token, serverEventItem, eventClientItem, since);
        }
                
        return clientUpdateRequired;
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#remove(java.lang.String)
     */
    public void remove(String token, String GUID, long since) throws MalformedJsonContentException, DaoException {
        String id   = Utility.removePrefix(GUID);
        manager.removeItem(token, id, since);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#getAllItemsKey()
     */
    public JsonKeys getAllItemsKey(String token) throws DaoException, MalformedJsonContentException {
        JsonKeys keys = manager.getAllItemsKey(token);
        Utility.addPrefix(Utility.APPOINTMENT_OBJECT, keys);
        return keys;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#getDeletedItemKeys(long,
     *      long)
     */
    public JsonKeys getDeletedItemKeys(String token, long since, long until) throws DaoException, MalformedJsonContentException {
        JsonKeys keys = manager.getDeletedItemKeys(token, since, until);
        Utility.addPrefix(Utility.APPOINTMENT_OBJECT, keys);
        return keys;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.Json.engine.CalendarSyncSourceStrategy#getNewItemKeys(long,
     *      long)
     */
    public JsonKeys getNewItemKeys(String token, long since, long until) throws DaoException, MalformedJsonContentException {
        JsonKeys keys = manager.getNewItemKeys(token, since, until);
        Utility.addPrefix(Utility.APPOINTMENT_OBJECT, keys);
        return keys;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#getUpdatedItemKeys(long,
     *      long)
     */
    public JsonKeys getUpdatedItemKeys(String token, long since, long until) throws DaoException, MalformedJsonContentException {
        JsonKeys keys = manager.getUpdatedItemKeys(token, since,until);
        Utility.addPrefix(Utility.APPOINTMENT_OBJECT, keys);
        return keys;
    }

    
}
