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
package com.funambol.json.data;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.Event;
import com.funambol.common.pim.calendar.Task;
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.note.Note;
import com.funambol.foundation.exception.DAOException;
import com.funambol.foundation.exception.EntityException;
import com.funambol.foundation.items.manager.PIMCalendarManager;
import com.funambol.foundation.items.manager.PIMContactManager;
import com.funambol.foundation.items.manager.PIMEntityManager;
import com.funambol.foundation.items.manager.PIMNoteManager;
import com.funambol.foundation.items.model.CalendarWrapper;
import com.funambol.foundation.items.model.ContactWrapper;
import com.funambol.foundation.items.model.EntityWrapper;
import com.funambol.foundation.items.model.NoteWrapper;

import com.funambol.framework.filter.Clause;
import com.funambol.framework.filter.LogicalClause;
import com.funambol.framework.filter.WhereClause;
import com.funambol.framework.server.Sync4jUser;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.tools.encryption.EncryptionException;
import com.funambol.framework.tools.encryption.EncryptionTool;
import com.funambol.json.converter.Converter;

import com.funambol.json.domain.JsonItem;
import com.funambol.json.engine.source.UtilitySyncSource;
import com.funambol.json.security.JsonUser;
import com.funambol.json.utility.Definitions;
import com.funambol.json.utility.ServletProperties;
import com.funambol.json.utility.Util;
import com.funambol.server.admin.AdminException;
import com.funambol.server.admin.UserManager;
import com.funambol.server.config.Configuration;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

/**
 * This class allows to access the static repository where all json objects
 * are stored.
 *
 * @version $Id$
 */
public class Repository {

    private String userid = null;
    public static Logger log = Logger.getLogger(Definitions.LOG_NAME);
    private Map<String, PIMEntityManager> instance = new HashMap<String, PIMEntityManager>();
    private TimeZone deviceTZ = null;
    private String deviceCharset = null;
    private ArrayList<String> sources = null;
    private HashMap<String, String> sourceTypes = null;
    public static boolean customerUserid = false;

    public Repository(String userid, TimeZone deviceTZ, String deviceCharset) {
        this.userid = userid;
        this.deviceTZ = deviceTZ;
        this.deviceCharset = deviceCharset;
        instance.put(Definitions.CONTACT, new PIMContactManager(userid));
        instance.put(Definitions.APPOINTMENT, new PIMCalendarManager(userid, Event.class));
        instance.put(Definitions.NOTE, new PIMNoteManager(userid));
        instance.put(Definitions.TASK, new PIMCalendarManager(userid, Task.class));

        sources = new ArrayList<String>();
        //allowed sources

        sources.add(Definitions.CONTACT);
        sources.add(Definitions.APPOINTMENT);
        sources.add(Definitions.NOTE);
        sources.add(Definitions.TASK);
        sources.add(Definitions.CONTACT_VCARD);
        sources.add(Definitions.APPOINTMENT_VCAL);
        sources.add(Definitions.NOTE_PLAIN);
        sources.add(Definitions.TASK_VCAL);

        sourceTypes = new HashMap<String, String>();
        //allowed types for each source
        sourceTypes.put(Definitions.CONTACT, Definitions.CONTACT_TYPE);
        sourceTypes.put(Definitions.APPOINTMENT, Definitions.APPOINTMENT_TYPE);
        sourceTypes.put(Definitions.NOTE, Definitions.NOTE_TYPE);
        sourceTypes.put(Definitions.TASK, Definitions.TASK_TYPE);
        sourceTypes.put(Definitions.CONTACT_VCARD, Definitions.CONTACT_VCARD_TYPE);
        sourceTypes.put(Definitions.APPOINTMENT_VCAL, Definitions.APPOINTMENT_VCAL_TYPE);
        sourceTypes.put(Definitions.NOTE_PLAIN, Definitions.NOTE_PLAIN_TYPE);
        sourceTypes.put(Definitions.TASK_VCAL, Definitions.TASK_VCAL_TYPE);

    }

    public static Sync4jUser login(JsonUser jsonUser) {
        Sync4jUser user = null;


        try {
            Sync4jUser[] users;

            Clause wcUsername;
            String valueUsername[] = new String[]{jsonUser.getUsername()};
            wcUsername = new WhereClause("username", valueUsername, WhereClause.OPT_EQ, true);

            Clause wcPassword;
            String valuePassword[] = new String[]{EncryptionTool.encrypt(jsonUser.getPassword())};
            wcPassword = new WhereClause("password", valuePassword, WhereClause.OPT_EQ, true);

            Clause wcFinal = new LogicalClause(LogicalClause.OPT_AND,
                    new Clause[]{wcUsername, wcPassword});

            UserManager userManager = Configuration.getConfiguration().getUserManager();

            users = userManager.getUsers(wcFinal);

            if (users.length > 0) {
                user = users[0];
            }

            return user;
        } catch (EncryptionException ex) {
            log.error(ex);
        } catch (AdminException ex) {
            log.error(ex);
        } catch (PersistentStoreException ex) {
            log.error(ex);
        }

        return null;
    }

    public FakeJsonItem getItem(String group, String key) throws Exception {
        String type = sourceTypes.get(group);
        if (log.isTraceEnabled()) {
            log.trace(" resource:" + group + " type:" + type);
        }
        PIMEntityManager manager = instance.get(group);
        if (Definitions.CONTACT.equals(group)) {

            ContactWrapper wrp = ((PIMContactManager) manager).getItem(key, true);
            return getJsonFromContact(wrp, group, wrp.getLastUpdate().getTime());

        } else if (Definitions.APPOINTMENT.equals(group)) {

            CalendarWrapper wrp = ((PIMCalendarManager) manager).getItem(key);
            return getJsonFromCalendar(wrp, group, wrp.getLastUpdate().getTime());

        } else if (Definitions.NOTE.equals(group)) {

            NoteWrapper wrp = ((PIMNoteManager) manager).getItem(key);
            return getJsonFromNote(wrp, group, wrp.getLastUpdate().getTime());

        } else if (Definitions.TASK.equals(group)) {

            CalendarWrapper wrp = ((PIMCalendarManager) manager).getItem(key);
            return getJsonFromTask(wrp, group, wrp.getLastUpdate().getTime());

        }
        return null;

    }

    public EntityWrapper getItemForListing(String group, String key) throws Exception {

        PIMEntityManager manager = instance.get(group);
        if (Definitions.CONTACT.equals(group)) {

            return ((PIMContactManager) manager).getItem(key, true);


        } else if (Definitions.APPOINTMENT.equals(group)) {

            return ((PIMCalendarManager) manager).getItem(key);


        } else if (Definitions.NOTE.equals(group)) {

            return ((PIMNoteManager) manager).getItem(key);


        } else if (Definitions.TASK.equals(group)) {

            return ((PIMCalendarManager) manager).getItem(key);


        }
        return null;

    }

    public String addItem(String group, FakeJsonItem item) throws DAOException, Exception {

        PIMEntityManager manager = instance.get(group);

        if (Definitions.CONTACT.equals(group)) {
            return ((PIMContactManager) manager).addItem(getContactFromJson(group, item, item.getContentType()), new Timestamp(item.getCreateTime()));

        } else if (Definitions.APPOINTMENT.equals(group)) {
            return ((PIMCalendarManager) manager).addItem(getEventFromJson(group, item, item.getContentType()), new Timestamp(item.getCreateTime()));

        } else if (Definitions.TASK.equals(group)) {
            return ((PIMCalendarManager) manager).addItem(getTaskFromJson(group, item, item.getContentType()), new Timestamp(item.getCreateTime()));

        } else if (Definitions.NOTE.equals(group)) {
            return ((PIMNoteManager) manager).addItem(getNoteFromJson(group, item, item.getContentType()), new Timestamp(item.getCreateTime()));
        }

        return null;
    }

    public String updateItem(String group, FakeJsonItem item, String key) throws DAOException, Exception {

        PIMEntityManager manager = instance.get(group);

        if (Definitions.CONTACT.equals(group)) {
            return ((PIMContactManager) manager).updateItem(key, getContactFromJson(group, item, item.getContentType()), new Timestamp(System.currentTimeMillis()));

        } else if (Definitions.APPOINTMENT.equals(group)) {
            return ((PIMCalendarManager) manager).updateItem(key, getEventFromJson(group, item, item.getContentType()), new Timestamp(System.currentTimeMillis()));

        } else if (Definitions.TASK.equals(group)) {
            return ((PIMCalendarManager) manager).updateItem(key, getTaskFromJson(group, item, item.getContentType()), new Timestamp(System.currentTimeMillis()));

        } else if (Definitions.NOTE.equals(group)) {
            return ((PIMNoteManager) manager).updateItem(key, getNoteFromJson(group, item, item.getContentType()), new Timestamp(System.currentTimeMillis()));
        }

        return null;
    }

    public String getContent(String group, String key) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("getContent group:" + group + " type:" + sourceTypes.get(group));
        }
        FakeJsonItem item = getItem(group, key);

        return JSONObject.fromObject(item.getContent()).toString(1);

    }

    public void deleteAllItems(String group) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("deleteAllItems from group:" + group);
        }
        instance.get(group).removeAllItems(null);

    }

    public void delete(String group, String key) throws Exception {
        instance.get(group).removeItem(key, null);
    }

    public List getUpdatedKeys(String group, Timestamp since, Timestamp until) throws DAOException, EntityException {
        List list = instance.get(group).getUpdatedItems(since, until);
        if (log.isTraceEnabled()) {
            log.trace("getUpdatedKeys from group:" + group);
            log.trace("since :" + Util.getDateFromLong(since.getTime()));
            log.trace("until :" + Util.getDateFromLong(until.getTime()));
            log.trace(instance.get(group).getUpdatedItems(since, until));
        }
        return list;

    }

    public List getNewKeys(String group, Timestamp since, Timestamp until) throws DAOException, EntityException {
        List list = instance.get(group).getNewItems(since, until);
        if (log.isTraceEnabled()) {
            log.trace("getNewKeys from group:" + group);
            log.trace("since :" + Util.getDateFromLong(since.getTime()));
            log.trace("until :" + Util.getDateFromLong(until.getTime()));
            log.trace(instance.get(group).getNewItems(since, until));
        }
        return list;

    }

    public List getDeletedKeys(String group, Timestamp since, Timestamp until) throws DAOException, EntityException {
        List list = instance.get(group).getDeletedItems(since, until);
        if (log.isTraceEnabled()) {
            log.trace("getDeletedKeys from group:" + group);
            log.trace("since :" + Util.getDateFromLong(since.getTime()));
            log.trace("until :" + Util.getDateFromLong(until.getTime()));
            log.trace(instance.get(group).getDeletedItems(since, until));
        }
        return list;

    }

    public List getAllKeys(String group, Timestamp since, Timestamp until) throws DAOException, EntityException {
        List list = instance.get(group).getAllItems();
        if (log.isTraceEnabled()) {
            log.trace("getAllKeys from group:" + group);
            log.trace("since :" + Util.getDateFromLong(since.getTime()));
            log.trace("until :" + Util.getDateFromLong(until.getTime()));
            log.trace(instance.get(group).getAllItems());
        }
        return list;

    }

    public List getTwins(String group, FakeJsonItem item) {

        PIMEntityManager manager = instance.get(group);
        try {
            if (Definitions.CONTACT.equals(group)) {
                return ((PIMContactManager) manager).getTwins(getContactFromJson(group, item, item.getContentType()));

            } else if (Definitions.APPOINTMENT.equals(group)) {
                return ((PIMCalendarManager) manager).getTwins(getEventFromJson(group, item, item.getContentType()));

            } else if (Definitions.TASK.equals(group)) {
                return ((PIMCalendarManager) manager).getTwins(getTaskFromJson(group, item, item.getContentType()));

            } else if (Definitions.NOTE.equals(group)) {
                return ((PIMNoteManager) manager).getTwins(getNoteFromJson(group, item, item.getContentType()));
            }
        } catch (Exception ex) {
        }
        return new ArrayList();
    }

    public char getState(String group, String key, String type) throws Exception {
        FakeJsonItem item = getItem(group, key);
        return item.getState();
    }

    private Contact getContactFromJson(String group, FakeJsonItem item, String type) throws Exception {
        Converter converter = ConverterManager.getConverter(group);

        if (Definitions.CONTACT_VCARD_TYPE.equals(type)) {
            return UtilitySyncSource.vcard2Contact(((JsonItem<String>) converter.fromRFC(item.getContent())).getItem());
        } else if (Definitions.CONTACT_TYPE.equals(type)) {
            return ((JsonItem<Contact>) converter.fromJSON(item.getContent())).getItem();
        } else {
            return null;
        }
    }

    private Calendar getEventFromJson(String group, FakeJsonItem item, String type) throws Exception {
        Converter converter = ConverterManager.getConverter(group);

        if (Definitions.APPOINTMENT_VCAL_TYPE.equals(type)) {
            return UtilitySyncSource.webCalendar2Calendar(
                    ((JsonItem<String>) converter.fromRFC(item.getContent())).getItem(),
                    Definitions.VCAL_FORMAT,
                    this.deviceTZ,
                    this.deviceCharset);

        } else if (Definitions.APPOINTMENT_TYPE.equals(type)) {
            return new Calendar(((JsonItem<Event>) converter.fromJSON(item.getContent())).getItem());
        } else {
            return null;
        }
    }

    private Calendar getTaskFromJson(String group, FakeJsonItem item, String type) throws Exception {
        Converter converter = ConverterManager.getConverter(group);

        if (Definitions.TASK_VCAL_TYPE.equals(type)) {
            return UtilitySyncSource.webCalendar2Calendar(
                    ((JsonItem<String>) converter.fromRFC(item.getContent())).getItem(),
                    Definitions.VCAL_FORMAT,
                    this.deviceTZ,
                    this.deviceCharset);

        } else if (Definitions.TASK_TYPE.equals(type)) {
            return new Calendar(((JsonItem<Task>) converter.fromJSON(item.getContent())).getItem());
        } else {
            return null;
        }
    }

    private Note getNoteFromJson(String group, FakeJsonItem item, String type) throws Exception {
        Converter converter = ConverterManager.getConverter(group);

        return ((JsonItem<Note>) converter.fromJSON(item.getContent())).getItem();

    }

    private FakeJsonItem getJsonFromContact(ContactWrapper contact, String group, long since) throws Exception {
        String dataStoreTypeKey = group + "." + Definitions.DATASTORETYPE;
        String dataStoreType = ServletProperties.getProperties().getProperty(dataStoreTypeKey, "");
        if (dataStoreType.equals("")) {
            dataStoreType = Definitions.JSON_EXTENDED;
        }

        if (log.isTraceEnabled()) {
            log.trace("getJsonFromContact group:" + group + " datastoreType:" + dataStoreType + " since:" + since + " contact:" + contact);
        }
        //getJsonFromContact group:contact type:application/json-card since:0
        Converter converter = ConverterManager.getConverter(group);
        String content = null;
        
//        if (icalVcal) {
        if (!dataStoreType.equals(Definitions.JSON_EXTENDED)) {
            try {
                JsonItem<String> jsonItem = new JsonItem<String>();
                jsonItem.setContentType(dataStoreType);
                jsonItem.setKey(contact.getId());
                jsonItem.setItem(UtilitySyncSource.contact2vcard(contact.getContact(), this.deviceTZ, this.deviceCharset));
                content = converter.toRFC(jsonItem);
            } catch (Exception ex) {

                log.error("Error converting contact to json:" + ex.getStackTrace());
            }
        } else {
            try {
                JsonItem<Contact> jsonItem = new JsonItem<Contact>();
                jsonItem.setItem(contact.getContact());
                content = converter.toJSON(jsonItem);
            } catch (Exception ex) {
                log.error("Error converting contact to json - ", ex);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("getJsonFromContact content:" + content);
        }
        return new FakeJsonItem(content, since);
    }

    private FakeJsonItem getJsonFromCalendar(CalendarWrapper calendar, String group, long since) throws Exception {
        String dataStoreTypeKey = group + "." + Definitions.DATASTORETYPE;
        String dataStoreType = ServletProperties.getProperties().getProperty(dataStoreTypeKey, "");
        if (dataStoreType.equals("")) {
            dataStoreType = Definitions.JSON_EXTENDED;
        }


        if (log.isTraceEnabled()) {
            log.trace("getJsonFromCalendar group:" + group + " datastoreType:" + dataStoreType + " since:" + since);
        }

        Converter converter = ConverterManager.getConverter(group);
        String content = null;
        //if (icalVcal) {
        if (!dataStoreType.equals(Definitions.JSON_EXTENDED)) {
            try {
                JsonItem<String> jsonItem = new JsonItem<String>();
                jsonItem.setContentType(dataStoreType);
                jsonItem.setKey(calendar.getId());
                jsonItem.setItem(UtilitySyncSource.calendar2webCalendar(calendar.getCalendar(), dataStoreType, this.deviceTZ, this.deviceCharset));
                content = converter.toRFC(jsonItem);
            } catch (Exception ex) {

                log.error("Error converting event to json:" + ex.getStackTrace());
            }
        } else {
            try {
                JsonItem<Event> jsonItem = new JsonItem<Event>();
                jsonItem.setItem(calendar.getCalendar().getEvent());
                content = converter.toJSON(jsonItem);
            } catch (Exception ex) {
                log.error("Error converting event to json - ", ex);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("getJsonFromCalendar content:" + content);
        }
        return new FakeJsonItem(content, since);
    }

    private FakeJsonItem getJsonFromTask(CalendarWrapper calendar, String group, long since) throws Exception {
        String dataStoreTypeKey = group + "." + Definitions.DATASTORETYPE;
        String dataStoreType = ServletProperties.getProperties().getProperty(dataStoreTypeKey, "");
        if (dataStoreType.equals("")) {
            dataStoreType = Definitions.JSON_EXTENDED;
        }

        if (log.isTraceEnabled()) {
            log.trace("getJsonFromTask group:" + group + " dataStoreType:" + dataStoreType + " since:" + since);
        }
        //getJsonFromContact group:contact type:application/json-card since:0
        Converter converter = ConverterManager.getConverter(group);
        String content = null;

        //if (icalVcal) {
        if (!dataStoreType.equals(Definitions.JSON_EXTENDED)) {
            try {
                JsonItem<String> jsonItem = new JsonItem<String>();
                jsonItem.setContentType(dataStoreType);
                jsonItem.setKey(calendar.getId());
                jsonItem.setItem(UtilitySyncSource.calendar2webCalendar(calendar.getCalendar(), dataStoreType, this.deviceTZ, this.deviceCharset));
                content = converter.toRFC(jsonItem);
            } catch (Exception ex) {

                log.error("Error converting task to json:" + ex.getStackTrace());
            }
        } else {
            try {
                JsonItem<Task> jsonItem = new JsonItem<Task>();
                jsonItem.setItem(calendar.getCalendar().getTask());
                content = converter.toJSON(jsonItem);
            } catch (Exception ex) {
                log.error("Error converting task to json - ", ex);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("getJsonFromTask content:" + content);
        }
        return new FakeJsonItem(content, since);
    }

    private FakeJsonItem getJsonFromNote(NoteWrapper note, String group, long since) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("getJsonFromNote group:" + group + " since:" + since);
        }
        //getJsonFromContact group:contact type:application/json-card since:0
        Converter converter = ConverterManager.getConverter(group);
        String content = null;

        try {
            JsonItem<Note> jsonItem = new JsonItem<Note>();
            jsonItem.setItem(note.getNote());
            content = converter.toJSON(jsonItem);
        } catch (Exception ex) {
            log.trace("Error converting note to json - ", ex);
        }

        if (log.isTraceEnabled()) {
            log.trace("getJsonFromNote content:" + content);
        }
        return new FakeJsonItem(content, since);
    }

    public boolean repositoryExists(String repo) {
        return instance.containsKey(repo);
    }

    public Iterator<String> getGroupKeySet() {
        return instance.keySet().iterator();
    }

    public boolean isEmpty(String group) {
        try {
            return !(getAllKeys(group, new Timestamp(0), new Timestamp(System.currentTimeMillis())).size() > 0);
        } catch (DAOException ex) {
        } catch (EntityException ex) {
        }
        return true;
    }


    public boolean sourceContains(String resource) {
        return sources.contains(resource);
    }

    public String getSourceTypeByResource(String resource) {
        return sourceTypes.get(resource);
    }
}
