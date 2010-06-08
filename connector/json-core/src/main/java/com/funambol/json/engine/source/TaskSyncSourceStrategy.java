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
import com.funambol.common.pim.calendar.Task;
import com.funambol.framework.engine.SyncItem;
import com.funambol.json.converter.TaskConverter;
import com.funambol.json.dao.JsonDAO;
import com.funambol.json.dao.JsonDAOImpl;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.domain.JsonKeys;
import com.funambol.json.exception.DaoException;
import com.funambol.json.exception.JsonConfigException;
import com.funambol.json.exception.MalformedJsonContentException;
import com.funambol.json.manager.JsonTaskManager;
import com.funambol.json.util.Utility;
import java.util.TimeZone;


/**
 * @version $Id: TaskSyncSourceStrategy.java 51920 2010-06-04 05:10:49Z gazzaniga $
 */
public class TaskSyncSourceStrategy implements CalendarSyncSourceStrategy {

    /**
     * 
     */
    private JsonTaskManager manager;

    public JsonTaskManager getManager() {
	return manager;
    }
    
    
    public TaskSyncSourceStrategy() throws JsonConfigException {
        JsonDAO dao = new JsonDAOImpl("task");
        TaskConverter converter = new TaskConverter();
        manager = new JsonTaskManager(dao, converter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#add(com.funambol.common.pim.calendar.Calendar)
     */
    public String add(String sessionID, 
                      Calendar calendar, 
                      String content, boolean 
                      vcardIcalBackend, 
                      long since) 
            throws MalformedJsonContentException, DaoException {
           
        String id   = null;
        String GUID = null;

        //if the backend requires items in vcard/ical formart
        if (vcardIcalBackend) {

            String objRFC = content;
            
            JsonItem<String> taskItem = new JsonItem<String>();
            taskItem.setItem(objRFC);
            taskItem.setKey(null);
            taskItem.setState("N");

            id = manager.addRFCItem(sessionID, taskItem, since);


        } else {

            JsonItem<Task> taskItem = new JsonItem<Task>();
            taskItem.setItem(calendar.getTask());
            taskItem.setKey(null);
            taskItem.setState("N");

            id = manager.addExtendedItem(sessionID, taskItem, since);
        }

        GUID = Utility.addPrefix(Utility.TASK_OBJECT, id);
            
        return GUID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#getKeysFromTwin(com.funambol.common.pim.calendar.Calendar,
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
            
            JsonItem<String> taskItem = new JsonItem<String>();
            taskItem.setItem(objRFC);
            taskItem.setKey(syncItem.getKey().getKeyAsString());
            taskItem.setState(String.valueOf(syncItem.getState()));

            keys = manager.getRFCItemKeysFromTwin(token, taskItem);

        } else {

            JsonItem<Task> taskItem = new JsonItem<Task>();
            taskItem.setItem(calendar.getTask());
            taskItem.setKey(syncItem.getKey().getKeyAsString());
            taskItem.setState(String.valueOf(syncItem.getState()));

            keys = manager.getExtendedItemKeysFromTwin(token, taskItem);
        }
        
        Utility.addPrefix(Utility.TASK_OBJECT, keys);

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
        JsonItem<String> taskItem = manager.getRFCItem(sessionID, id);
        return taskItem;        
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#get(java.lang.String)
     */
    public JsonItem getExtendedItem(String sessionID, String GUID) 
        throws MalformedJsonContentException, DaoException, Exception {
        String id = Utility.removePrefix(GUID);
        JsonItem<Task> taskItem = manager.getExtendedItem(sessionID, id);
        return taskItem;        
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

            JsonItem<String> taskItem = new JsonItem<String>();
            taskItem.setItem(objRFC);
            taskItem.setContentType(syncItem.getType());
            taskItem.setState(String.valueOf(syncItem.getState()));

            String GUID = syncItem.getKey().getKeyAsString();
            String id   = Utility.removePrefix(GUID);
            taskItem.setKey(id);
        
            JsonItem<String> taskItemUpdated = manager.updateRFCItem(sessionID, taskItem, since);

            //Get the id from the syncInstance to update
            id   = taskItemUpdated.getKey();
            updatedKey = Utility.addPrefix(Utility.TASK_OBJECT, id);
            

        } else {

            JsonItem<Task> taskItem = new JsonItem<Task>();
            
            taskItem.setItem(calendar.getTask());
            taskItem.setContentType(syncItem.getType());
            taskItem.setState(String.valueOf(syncItem.getState()));

            String GUID = syncItem.getKey().getKeyAsString();
            String id   = Utility.removePrefix(GUID);
            taskItem.setKey(id);           

            JsonItem<Task> taskItemUpdated = manager.updateExtendedItem(sessionID, taskItem, since);

            //Get the id from the syncInstance to update
            id   = taskItemUpdated.getKey();
            updatedKey = Utility.addPrefix(Utility.TASK_OBJECT, id);

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
                         String vCalType,
                         TimeZone deviceTimeZone, 
                         String deviceCharset,
                         long since) 
       throws MalformedJsonContentException, DaoException, Exception {
        
        JsonItem<Task> taskClientItem = new JsonItem<Task>();    
        taskClientItem.setItem(calendar.getTask());        
        taskClientItem.setContentType(syncItem.getType());
        taskClientItem.setState(String.valueOf(syncItem.getState()));
                        
        boolean clientUpdateRequired = false;
        
        String id   = Utility.removePrefix(GUID);

        if (vcardIcalBackend) {            
            
            JsonItem<String> taskServerRFC = manager.getRFCItem(token, id);
            
            JsonItem<Task> serverTaskItem = new JsonItem<Task>();
            serverTaskItem.setContentType(taskServerRFC.getContentType());
            serverTaskItem.setState(String.valueOf(taskServerRFC.getState()));
            
            String vcal = taskServerRFC.getItem();
            Calendar c = UtilitySyncSource.webCalendar2Calendar(vcal, vCalType, deviceTimeZone, deviceCharset);
            serverTaskItem.setItem(c.getTask()); 
            

            serverTaskItem.setKey(Utility.addPrefix(Utility.TASK_OBJECT, id));

            clientUpdateRequired = manager.mergeRFCItem(token,
                                                        serverTaskItem,
                                                        taskClientItem,
                                                        since,
                                                        false, // not used in this context
                                                        false, // not used in this context
                                                        vCalType,
                                                        deviceTimeZone,
                                                        deviceCharset);
            
        } else {
            
            JsonItem<Task> serverTaskItem = manager.getExtendedItem(token, id);

            id = serverTaskItem.getKey();
            serverTaskItem.setKey(Utility.addPrefix(Utility.TASK_OBJECT, id));
            
            clientUpdateRequired = manager.mergeExtendedItem(token, serverTaskItem, taskClientItem, since);
            
        }
                
        return clientUpdateRequired;
        
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#remove(java.lang.String)
     */
    public void remove(String token, String GUID, long since) throws MalformedJsonContentException, DaoException {        
        String id = Utility.removePrefix(GUID);
        manager.removeItem(token, id, since);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#getAllItemsKey()
     */
    public JsonKeys getAllItemsKey(String token)  throws DaoException, MalformedJsonContentException {
        JsonKeys keys = manager.getAllItemsKey(token);
        Utility.addPrefix(Utility.TASK_OBJECT, keys);
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
        Utility.addPrefix(Utility.TASK_OBJECT, keys);
        return keys;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#getNewItemKeys(long,
     *      long)
     */
    public JsonKeys getNewItemKeys(String token, long since, long until) throws DaoException, MalformedJsonContentException {
        JsonKeys keys = manager.getNewItemKeys(token, since, until);
        Utility.addPrefix(Utility.TASK_OBJECT, keys);
        return keys;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.funambol.json.engine.CalendarSyncSourceStrategy#getUpdatedItemKeys(long,
     *      long)
     */
    public JsonKeys getUpdatedItemKeys(String token, long since, long until) throws DaoException, MalformedJsonContentException {
        JsonKeys keys = manager.getUpdatedItemKeys(token, since, until);
        Utility.addPrefix(Utility.TASK_OBJECT, keys);
        return keys;
    }
    
    
}
