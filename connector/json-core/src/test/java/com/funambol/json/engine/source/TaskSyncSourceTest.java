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

import java.sql.Timestamp;
import java.util.TimeZone;

import net.sf.json.JSONException;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.Task;

import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.source.ContentType;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.engine.source.SyncSourceInfo;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.Sync4jDevice;

import com.funambol.json.abstractServlet.AbstractHttpTransportTest;
import com.funambol.json.abstractServlet.JsonServlet;
import com.funambol.json.converter.TaskConverter;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.security.JsonUser;
import com.funambol.json.util.Utility;

/**
 * Test cases for TaskSyncSource class.
 * @version $Id$
 */
public class TaskSyncSourceTest extends AbstractHttpTransportTest {

    CalendarSyncSource source = null;

    // ------------------------------------------------------------ Private data
    private static JsonServlet jsonServlet = new JsonServlet();

    // ------------------------------------------------------------ Constructors
    public TaskSyncSourceTest() {
        super(jsonServlet);
    }

    // ------------------------------------------------------- Protected methods
    @Override
    protected void setUp() throws Exception {
        source = createTaskSyncSource();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    // -------------------------------------------------------------- Test cases
    /**
     * 
     * @throws net.sf.json.JSONException
     */
    public void test_Add() throws JSONException {

        try {

            SyncContext context = createContext();

            jsonServlet.setDoReturn(JsonServlet.EMPTY);

            ContentType[] contentTypes = null;
            contentTypes = new ContentType[1];
            contentTypes[0] = new ContentType("text/x-s4j-sifc", "1.0");
            source.setBackendType(new SyncSourceInfo(contentTypes, 0));

            source.init();

            source.beginSync(context);

            jsonServlet.setDoReturn(JsonServlet.ITEMS);

            String sifT =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><task>" +
                    "<Subject>Tre</Subject>" +
                    "<Categories></Categories>" +
                    "<ReminderSoundFile></ReminderSoundFile><Body></Body>" +
                    "<ReminderSet>0</ReminderSet>" +
                    "<Complete>0</Complete><TeamTask>0</TeamTask>" +
                    "<StartDate>2008-05-22</StartDate><DueDate>2008-05-22</DueDate>" +
                    "<ReminderTime></ReminderTime>" +
                    "<Sensitivity>0</Sensitivity>" +
                    "<ReminderOptions>4</ReminderOptions>" +
                    "<Importance>1</Importance>" +
                    "<IsRecurring>1</IsRecurring><RecurrenceType>1</RecurrenceType>" +
                    "<Interval>1</Interval><DayOfMonth>0</DayOfMonth><MonthOfYear>0</MonthOfYear>" +
                    "<DayOfWeekMask>16</DayOfWeekMask><Instance>0</Instance>" +
                    "<PatternStartDate>2008-05-22</PatternStartDate><NoEndDate>1</NoEndDate>" +
                    "<PatternEndDate></PatternEndDate><Occurrences>0</Occurrences><Exceptions/>" +
                    "<Folder>Task</Folder></task>";

            String sifTUpdate =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><task><Subject>Tre</Subject>" +
                    "<Categories></Categories><ReminderSoundFile></ReminderSoundFile><Body></Body>" +
                    "<ReminderSet>0</ReminderSet><Complete>0</Complete><TeamTask>0</TeamTask>" +
                    "<StartDate>2008-05-22</StartDate><DueDate>2008-05-22</DueDate><ReminderTime>" +
                    "</ReminderTime><Sensitivity>0</Sensitivity><ReminderOptions>4</ReminderOptions>" +
                    "<Importance>1</Importance><IsRecurring>1</IsRecurring><RecurrenceType>1</RecurrenceType>" +
                    "<Interval>1</Interval><DayOfMonth>0</DayOfMonth><MonthOfYear>0</MonthOfYear>" +
                    "<DayOfWeekMask>16</DayOfWeekMask><Instance>0</Instance>" +
                    "<PatternStartDate>2008-05-22</PatternStartDate><NoEndDate>1</NoEndDate>" +
                    "<PatternEndDate></PatternEndDate><Occurrences>0</Occurrences><Exceptions/>" +
                    "<Folder>Task</Folder></task>";

            Object anyKey = String.valueOf(System.currentTimeMillis()); // dummy

            SyncItem syncItem = new SyncItemImpl(source, anyKey, null, null,
                    SyncItemState.NEW,
                    sifT.getBytes(),
                    null, "text/x-s4j-sift", null);

            SyncItemKey itemKey = source.addSyncItem(syncItem).getKey();

            String guid = itemKey.getKeyAsString();
            assertTrue(guid.equals(Utility.TASK_OBJECT + Utility.GUID_SEP + "0"));

            SyncItem syncItem2 = new SyncItemImpl(source, guid, null, null,
                    SyncItemState.UPDATED, sifTUpdate.getBytes(), null, "text/x-s4j-sift", null);

            String guid2 = syncItem2.getKey().getKeyAsString();
            assertTrue(guid2.equals(Utility.TASK_OBJECT + Utility.GUID_SEP + "0"));

            SyncItem syncItemUpdated = source.updateSyncItem(syncItem2);
            String guid3 = syncItemUpdated.getKey().getKeyAsString();
            assertTrue(guid3.equals(Utility.TASK_OBJECT + Utility.GUID_SEP + "0"));

            jsonServlet.setDoReturn(JsonServlet.ENDSYNC);
            source.endSync();

        } catch (SyncSourceException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * 
     * @throws Exception
     */
    public void test_getItem() throws Exception {

        jsonServlet.setDoReturn(JsonServlet.ITEMS);

        StringBuilder json = new StringBuilder("{\"data\":{")
            .append("\"content-type\":\"application/json-task\",\"item\":{")
            .append("\"key\":\"87860\",")
            .append("\"folder\":\"Root\\\\Tasks\",")
            .append("\"subject\":\"task from server\",")
            .append("\"body\":\"from server\",")
            .append("\"startDate\":\"20081027T000000Z\",")
            .append("\"dueDate\":\"20081027T000000Z\",")
            .append("\"importance\":\"2\",")
            .append("\"status\":\"0\",")
            .append("\"complete\":false,")
            .append("\"percentComplete\":\"0.000\",")
            .append("\"dateCompleted\":\"\",")
            .append("\"reminder\":true,")
            .append("\"reminderDate\":\"20081027T000000\",")
            .append("\"totalWork\":\"100\",")
            .append("\"actualWork\":\"23\",")
            .append("\"mileage\":\"1\",")
            .append("\"billingInformation\":\"info for billing\",")
            .append("\"companies\":\"snc e srl\",")
            .append("\"SessionID\":\"85954\",")
            .append("\"categories\":\"\",")
            .append("\"contacts\":\"\",")
            .append("\"SentReminder\":\"No\",")
            .append("\"customerID\":\"406940\",")
            .append("\"Timestamp\":\"2008-10-17 05:54:04\",")
            .append("\"uuid\":\"4700A0E248F86ECBD046840C00098467\"}}}");
             
        TaskConverter converter = new TaskConverter();

        JsonItem<Task> task = converter.fromJSON(json.toString());
        
        Calendar calendar = new Calendar();
        calendar.setCalendarContent(task.getItem());

        String result = source.calendar2sif(calendar, 
                                            "text/x-s4j-sift", 
                                            null,
                                            "UTF-8");
        
        StringBuilder expected =
            new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<task>\n")
            .append("<SIFVersion>1.1</SIFVersion>")
            .append("<ActualWork>23</ActualWork>")
            .append("<Categories/>")
            .append("<Companies>snc e srl</Companies>")
            .append("<Complete>0</Complete>")
            .append("<StartDate>2008-10-27</StartDate>")
            .append("<DueDate>2008-10-27</DueDate>")
            .append("<Importance>2</Importance>")
            .append("<Mileage>1</Mileage>")
            .append("<PercentComplete>0</PercentComplete>")
            .append("<ReminderSet>1</ReminderSet>")
            .append("<ReminderMinutesBeforeStart>0</ReminderMinutesBeforeStart>")
            .append("<ReminderTime>20081027T000000</ReminderTime>")
            .append("<Sensitivity>0</Sensitivity>")
            .append("<Status>0</Status>")
            .append("<Subject>task from server</Subject>")
            .append("<Body>from server</Body>")
            .append("<Folder>DEFAULT_FOLDER</Folder>")
            .append("<BillingInformation>info for billing</BillingInformation>")
            .append("<TotalWork>100</TotalWork>")
            .append("<IsRecurring>0</IsRecurring>")
            .append("<Exceptions/>")
            .append("<Attendees/>\n")
            .append("</task>");

        assertEquals(expected.toString(), result);
    }

    /**
     * 
     * @throws net.sf.json.JSONException
     */
    public void test_setItem() throws JSONException {

        try {
            
            String sifT =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><task>" +
                    "<Folder>Task</Folder>" +
                    "<Subject>Tre</Subject>" +
                    "<Body></Body>" +
                    "<Importance>1</Importance>" +
                    "<StartDate>2008-05-22</StartDate>" +
                    "<DueDate>2008-05-22</DueDate>" +
                    "<Status>4</Status>" +
                    "<Complete>0</Complete>" +
                    
                    "<ReminderSet>0</ReminderSet>" +
                    "<ReminderTime></ReminderTime>" +
                    "<ReminderOptions>4</ReminderOptions>" +
                    
                    "<IsRecurring>0</IsRecurring>" +
                    "<Categories></Categories>" +
                    "<TeamTask>0</TeamTask>" +
                    "<Sensitivity>0</Sensitivity>" +
                    "<ReminderSoundFile></ReminderSoundFile>" +
                    "</task>";

            Calendar calendar = source.sif2Calendar(sifT);
       
            assertEquals("0", calendar.getTask().getComplete().getPropertyValueAsString());
                        
            JsonItem<Task> item = new JsonItem<Task>();
            item.setContentType("type");
            item.setKey("0");
            item.setState("A");
            item.setItem(calendar.getTask());
            
            TaskConverter converter = new TaskConverter();
            
            // patch
            // this conversion method uses the BOOLEAN
            item.getItem().getComplete().setPropertyValue(false);            
            String jsonResult = converter.toJSON(item);

            // double check
            JsonItem<Task> task2 = converter.fromJSON(jsonResult);

            System.out.println("task2:"+task2.getItem().getStatus().getPropertyValueAsString());
            Calendar calendar2 = new Calendar();
            calendar2.setCalendarContent(task2.getItem());
            TimeZone deviceTimeZone = null;
            String deviceCharset  = "UTF-8";        
            String result = source.calendar2sif(calendar, 
                                                "text/x-s4j-sift", 
                                                deviceTimeZone, 
                                                deviceCharset);
            
            String expectedResutl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                "<task>\n" +
                "<SIFVersion>1.1</SIFVersion>" +
                "<Categories/>" +               
                "<Complete>0</Complete>" + 
                "<StartDate>2008-05-22</StartDate>" +
                "<DueDate>2008-05-22</DueDate>" +
                "<Importance>1</Importance>" +
                "<ReminderSet>0</ReminderSet>" +
                "<ReminderMinutesBeforeStart>0</ReminderMinutesBeforeStart>" +
                "<ReminderTime/>" +
                "<Sensitivity>0</Sensitivity>" +
                "<Status>4</Status>"+
                "<Subject>Tre</Subject>" +
                "<Body/>" +
                "<Folder>Task</Folder>" +
                "<TeamTask>0</TeamTask>" +
                "<IsRecurring>0</IsRecurring>" +
                "<Exceptions/>" +
                "<Attendees/>\n" +
                "</task>";

            assertEquals(expectedResutl, result);            

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    // --------------------------------------------------------- Private methods
    private CalendarSyncSource createTaskSyncSource() {

        CalendarSyncSource source = new CalendarSyncSource();

        SyncSourceInfo ssi = new SyncSourceInfo();
        ContentType[] contentType = new ContentType[1];
        contentType[0] = new ContentType("text/x-s4j-sift", "1.0");
        ssi.setSupportedTypes(contentType);
        source.setInfo(ssi);

        source.setEntityType(Task.class);

        return source;
    }

    private SyncContext createContext() {
        JsonUser user = new JsonUser("pippo", "pippo");
        user.setUsername("pippo");
        user.setPassword("pippo");
        Sync4jDevice device = new Sync4jDevice("deviceID");
        Sync4jPrincipal principal = new Sync4jPrincipal(user, device);
        SyncContext context = new SyncContext(principal,
                200,
                null,
                "localhost",
                2,
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()));

        return context;
    }

}
