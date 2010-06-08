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
package com.funambol.json.converter;

import java.util.TimeZone;

import junit.framework.TestCase;

import net.sf.json.test.JSONAssert;
import net.sf.json.JSONObject;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.Task;
import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.converter.TimeZoneHelper;
import com.funambol.common.pim.converter.VCalendarConverter;
import com.funambol.common.pim.model.VCalendar;
import com.funambol.common.pim.xvcalendar.XVCalendarParser;

import com.funambol.json.domain.JsonItem;
import com.funambol.json.util.TestUtility;
import java.io.ByteArrayInputStream;

/**
 * Test cases for converters.
 *
 * @version $Id: TaskConverterTest.java 52034 2010-06-08 12:59:28Z derose $
 */
public class TaskConverterTest extends TestCase {

    TaskConverter converter = null;

    // --------------------------------------------------------------- Constants
    private static final String CHARSET = "UTF-8";

    // -------------------------------------------------------------- Properties
    final boolean debugPrintOut = false;

    // ------------------------------------------------------- Protected methods
    @Override
    protected void setUp() throws Exception {
        // Sets noon, January 1st, 2009 as reference time, thus making the tests
        // time-independent
        TimeZoneHelper.setReferenceTime(1230811200000L);
        converter = new TaskConverter();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // Restores current time as reference time
        TimeZoneHelper.setReferenceTime(-1);
    }

    // -------------------------------------------------------------- Test cases
    /**
     * 
     * @throws java.lang.Exception
     */
    public void testSIFTask01() throws Exception{
        // xml from the device
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                
                "<task>" +
                
                "<Subject>Tre</Subject>" +
                "<StartDate>2008-05-22</StartDate>" +
                "<DueDate>2008-05-22</DueDate>" +
                "<Body></Body>" +

                "<ReminderSet>0</ReminderSet>" +
                "<ReminderSoundFile></ReminderSoundFile>" +
                "<ReminderTime></ReminderTime>" +
                "<ReminderOptions>4</ReminderOptions>" +
                 "<Status>0</Status>" +
                "<Complete>0</Complete>" +
                "<PercentComplete>0</PercentComplete>" +

                "<ActualWork>0</ActualWork>" +
                "<TotalWork>0</TotalWork>" +
                                
                "<Categories></Categories>" +
                "<Sensitivity>0</Sensitivity>" +
                "<TeamTask>0</TeamTask>" +
                "<Importance>1</Importance>" +
                
                "<IsRecurring>1</IsRecurring>" +
                "<RecurrenceType>1</RecurrenceType>" +
                "<Interval>1</Interval>" +
                "<DayOfMonth>0</DayOfMonth>" +
                "<MonthOfYear>0</MonthOfYear>" +
                "<DayOfWeekMask>16</DayOfWeekMask>" +
                "<Instance>0</Instance>" +
                "<PatternStartDate>2008-05-22</PatternStartDate>" +
                "<NoEndDate>1</NoEndDate>" +
                "<PatternEndDate></PatternEndDate>" +
                "<Occurrences>0</Occurrences>" +
                "<Exceptions/>" +
                
                "</task>";
        
        // note this converter use the methods of the 
        // common pim framework in order to manage the vcal/sif convesion
        Calendar c = TestUtility.sif2Calendar(xml);
        
        String dtStart = c.getTask().getDtStart().getPropertyValueAsString();
        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();
        String startDatePattern = c.getTask().getRecurrencePattern().getStartDatePattern();

        // Foundation bug (or just inconsistency). It should be:
        //   assertEquals("20080522T000000", dtStart);
        //   assertEquals("20080522T235900", dtEnd);
        //   assertEquals("20080522T000000", startDatePattern);
        // but it is:
        assertEquals("2008-05-22", dtStart);
        assertEquals("2008-05-22", dueDate);
        assertEquals("2008-05-22", startDatePattern);
        
        JsonItem<Task> item = new JsonItem<Task>();

        item.setContentType("type");
        item.setKey("0");
        item.setState("A");

        Task t = c.getTask();
        t.setFolder(new Property("pippo"));
        t.getComplete().setPropertyValue(false); // patch
        item.setItem(t);

        String jsonResult = converter.toJSON(item);

        String expectedResult = "{\"data\":{\"content-type\":\"application/json-task\",\"item\":{" +
                "\"key\":\"0\"," +
                "\"state\":\"A\"," +
                "\"folder\":\"pippo\"," +
                "\"subject\":\"Tre\"," +
                "\"startDate\":\"20080522\"," +
                "\"dueDate\":\"20080522\"," +

                "\"allDay\":true," +

                "\"body\":\"\",\"importance\":1,\"percentComplete\":\"0\",\"reminder\":false,\"dateCompleted\":\"\"," +
                "\"actualWork\":\"0\",\"totalWork\":\"0\",\"billingInformation\":\"\",\"mileage\":0,\"companies\":\"\"}}}";

        JsonItem<Task> item2 = converter.fromJSON(jsonResult);
        
        String jsonResult2 = converter.toJSON(item2);

        JSONAssert.assertEquals(jsonResult2, jsonResult);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void testSIFTask02() throws Exception{

        // json from server
        String jsonResult =
                "{\"data\":" +
                "{\"content-type\":\"application/json-task\"," +
                "\"item\":" +
                "{" +
                "\"key\":\"87301\"," +
                "\"Timestamp\":\"2008-10-09 04:53:39\"," +
                "\"folder_id\":\"1832447\"," +
                "\"subject\":\"Eeriwo\"," +
                "\"body\":\"\"," +
                "\"startDate\":\"0000-00-00 00:00:00\"," +
                "\"dueDate\":\"0000-00-00 00:00:00\"," +
                "\"status\":\"0\"," +
                "\"importance\":\"2\"," +
                "\"completed\":\"0\"," +
                "\"customerID\":\"406940\"," +
                "\"reminder\":\"0\"," +
                "\"reminder_date\":\"0000-00-00 00:00:00\"," +
                "\"details_date_completed\":\"\"," +
                "\"details_total_work\":\"\"," +
                "\"details_actual_work\":\"\"," +
                "\"details_mileage\":\"\"," +
                "\"details_billing_information\":\"\"," +
                "\"details_companies\":\"\"," +
                "\"SessionID\":\"0\",\"categories\":\"\"," +
                "\"contacts\":\"\"," +
                "\"percent_complete\":\"0.000\"," +
                "\"SentReminder\":\"No\"," +
                "\"uuid\":\"\"" +
                "}" +
                "}" +
                "}";

        JsonItem<Task> task = converter.fromJSON(jsonResult);

        String importance = task.getItem().getImportance().getPropertyValueAsString();

        // NOTE: this is the value before the common pim framework
        // see the method TaskConverter.importanceFromServerToClient()
        JSONAssert.assertEquals(importance, "1");
    }

    /**
     * Tests the conversion from JSON Object to SIF-Task object in the case in
     * which there are no some fields.
     * @throws Exception
     */
    public void testJSONToSIFTask_NoFields() throws Exception {
        StringBuilder expected =
            new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        expected.append("<task>\n<SIFVersion>1.1</SIFVersion>")
            .append("<Complete>0</Complete>")
            .append("<StartDate>2009-01-01</StartDate>")
            .append("<ReminderSet>0</ReminderSet>")
            .append("<ReminderMinutesBeforeStart>0</ReminderMinutesBeforeStart>")
            .append("<Sensitivity>0</Sensitivity>")
            .append("<Status>0</Status>")
            .append("<Subject>Test Task</Subject>")
            .append("<Body>Prozent Erledigt: 50% (% Complete)\nMileage: 1000 (Reisekilometer)\nNote added from the Communicator</Body>")
            .append("<IsRecurring>0</IsRecurring><Exceptions/><Attendees/>")
            .append("\n</task>");

        // convert json into SIF-T
        StringBuilder jsonTask = new StringBuilder("{\"data\":");
            jsonTask.append("{\"content-type\":\"application/json-task\",")
                    .append("\"item\":{\"subject\":\"Test Task\"," )
                    .append("\"body\":\"Prozent Erledigt: 50% (% Complete)\\r\\nMileage: 1000 (Reisekilometer)\\r\\nNote added from the Communicator\",")
                    .append("\"startDate\":\"20090101T000000\",")
                    .append("\"sensitivity\":\"PUBLIC\",")
                    .append("\"status\":\"0\",")
                    .append("\"reminder\":\"0\",")
                    .append("\"lastUpdate\":\"06.11.2009 17:58:05\"}}}");

        JsonItem<Task> itemTask = converter.fromJSON(jsonTask.toString());
        String result = TestUtility.task2SifT(itemTask.getItem());

        assertEquals(expected.toString(), result.replaceAll("\\r", ""));
    }

    /**
     * Tests the conversion from JSON Object to SIF-Task object in the case in
     * which there are no some fields.
     * @throws Exception
     */
    public void testJSONToSIFTask_AllFields() throws Exception {
        StringBuilder expected =
            new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        expected.append("<task>\n<SIFVersion>1.1</SIFVersion>")
            .append("<ActualWork>10</ActualWork>")
            .append("<Companies>company</Companies>")
            .append("<Complete>0</Complete>")
            .append("<StartDate>2009-01-01</StartDate>")
            .append("<DueDate>2009-01-01</DueDate>")
            .append("<Importance>2</Importance>")
            .append("<Mileage>10</Mileage>")
            .append("<PercentComplete>50</PercentComplete>")
            .append("<ReminderSet>0</ReminderSet>")
            .append("<ReminderMinutesBeforeStart>0</ReminderMinutesBeforeStart>")
            .append("<Sensitivity>3</Sensitivity>")
            .append("<Status>0</Status>")
            .append("<Subject>Test Task</Subject>")
            .append("<Body>Note added</Body>")
            .append("<Folder>folder</Folder>")
            .append("<BillingInformation>1000</BillingInformation>")
            .append("<TotalWork>100</TotalWork>")
            .append("<IsRecurring>0</IsRecurring><Exceptions/><Attendees/>")
            .append("\n</task>");

        // convert json into SIF-T
        StringBuilder jsonTask = new StringBuilder("{\"data\":");
            jsonTask.append("{\"content-type\":\"application/json-task\",")
                    .append("\"item\":{\"folder\":\"folder\",")
                    .append("\"subject\":\"Test Task\",")
                    .append("\"body\":\"Note added\",")
                    .append("\"startDate\":\"20090101\",")
                    .append("\"dueDate\":\"20090101\",")
                    .append("\"allDay\":true,")
                    .append("\"sensitivity\":\"CONFIDENTIAL\",")
                    .append("\"importance\":2,")
                    .append("\"complete\":false,")
                    .append("\"status\":\"1\",")
                    .append("\"percentComplete\":\"50\",")
                    .append("\"reminder\":\"0\",")
                    .append("\"dateCompleted\":\"\",")
                    .append("\"actualWork\":\"10\",")
                    .append("\"totalWork\":\"100\",")
                    .append("\"billingInformation\":\"1000\",")
                    .append("\"mileage\":10,")
                    .append("\"companies\":\"company\",")
                    .append("\"lastUpdate\":\"06.11.2009 17:58:05\"}}}");

        JsonItem<Task> itemTask = converter.fromJSON(jsonTask.toString());
        String result = TestUtility.task2SifT(itemTask.getItem());

        assertEquals(expected.toString(), result.replaceAll("\\r", ""));
    }

    /**
     * From OUTLOOK to Json
     *
     * i.e.
     *
     * ..
     * <Folder>DEFAULT_FOLDER\Funambol</Folder>
     * ..
     *
     */
    public void test_FolderParser_C2S_00() {

        JsonItem<Task> item = new JsonItem<Task>();

        item.setContentType("type");
        item.setKey("0");
        item.setState("A");

        Task task = new Task();

        // the outlook path is
        // \\Personal Folders\Tasks\Funambol
        // the
        // DEFAULT_FOLDER <==> \\Personal Folders\Tasks
        task.setFolder(new Property("DEFAULT_FOLDER\\Funambol"));

        task.setSummary(new Property("andare dal dentista"));
        task.setDtStart(new Property("2008-12-12"));
        task.setDueDate(new Property("2008-12-12"));

        item.setItem(task);

        String jsonResult = converter.toJSON(item);

        JSONObject jo = JSONObject.fromObject(jsonResult);

        JSONObject jodata = jo.getJSONObject("data");
        JSONObject joitem = jodata.getJSONObject("item");
        String folder = joitem.getString("folder");

        assertEquals("Root\\Tasks\\Funambol", folder);
    }

    /**
     * From WM to Json
     *
     * the WM doesn't send the property <Folder>
     *
     */
    public void test_FolderParser_C2S_01() {

        JsonItem<Task> item = new JsonItem<Task>();

        item.setContentType("type");
        item.setKey("0");
        item.setState("A");

        Task task = new Task();

        task.setSummary(new Property("go to dentist"));
        task.setDtStart(new Property("2008-12-12"));
        task.setDueDate(new Property("2008-12-12"));

        item.setItem(task);

        String jsonResult = converter.toJSON(item);

        JSONObject jo = JSONObject.fromObject(jsonResult);

        JSONObject jodata = jo.getJSONObject("data");
        JSONObject joitem = jodata.getJSONObject("item");
        String folder = joitem.optString("folder");

        assertEquals("", folder);
    }

    /**
     *
     * @throws Exception
     */
    public void test_VTODO_2_JSON() throws Exception {


        // before the synclet
        /*
        BEGIN:VCALENDAR
        VERSION:1.0
        BEGIN:VTODO
        UID:1503
        SUMMARY:todo  N9500
        X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0
        STATUS:NEEDS ACTION
        X-EPOCAGENDAENTRYTYPE:TODO
        CLASS:PUBLIC
        DCREATED:20070223T000000
        LAST-MODIFIED:20070224T032300Z
        PRIORITY:3
        END:VTODO
        END:VCALENDAR
        */
        // after the synclet
        StringBuilder vcalTest = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("UID:1503\n")
            .append("SUMMARY:todo  N9500\n")
            .append("X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0\n")
            .append("STATUS:NEEDS-ACTION\n")
            .append("X-EPOCAGENDAENTRYTYPE:TODO\n")
            .append("CLASS:PUBLIC\n")
            .append("DCREATED:20070223T000000\n")
            .append("LAST-MODIFIED:20070224T032300Z\n")
            .append("PRIORITY:3\n")
            .append("RRULE:\n")
            .append("AALARM:;;;\n")
            .append("DUE:\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR\n");

        String tzid = "America/Mexico_City";

        Calendar c = TestUtility.webCalendar2Calendar(vcalTest.toString(),
                                          "text/x-vcalendar",
                                          TimeZone.getTimeZone(tzid),
                                          "UTF-8");

        Task e = c.getTask();
        e.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(e);

        converter.setServerTimeZoneID(tzid);
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":{")
            .append("\"content-type\":\"application/json-task\",\"item\":{")
            .append("\"folder\":\"12\",")
            .append("\"subject\":\"todo  N9500\",")
            .append("\"tzid\":\"America/Mexico_City\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":0,")
            .append("\"complete\":false,")
            .append("\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}")
            .append("}}}");


        assertEquals(jsonExpected.toString(), jsonResult);
    }

    /**
     * From Json to clients
     *
     * the WM doesn't send the property <Folder>
     * i.e.
     * ..
     * <Email1Address>1@funambol.com</Email1Address>
     * <FirstName>Gamma Zuse</FirstName>
     *
     * ..
     *
     */
    public void test_FolderParser_S2C_00() {

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        jsonData.elementOpt("content-type", "application/json-task");

        jsonItem.elementOpt("key", "0");
        jsonItem.elementOpt("state", "A");
        jsonItem.elementOpt("folder", "Root\\Tasks\\Json");
        jsonItem.elementOpt("subject", "andare dal dentista");
        jsonItem.elementOpt("startDate", "2008-12-12");
        jsonItem.elementOpt("dueDate", "2008-12-12");

        jsonData.element("item", jsonItem);
        jsonRoot.element("data", jsonData);

        JsonItem<Task> taskItem = converter.fromJSON(jsonRoot.toString());

        Task item = taskItem.getItem();

        String folder = item.getFolder().getPropertyValueAsString();

        assertEquals("DEFAULT_FOLDER\\Json", folder);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void test_Mileage() throws Exception{

        // json from server
        String jsonResult =
                "{\"data\":" +
                "{\"content-type\":\"application/json-task\"," +
                "\"item\":" +
                "{" +
                "\"key\":\"87301\"," +
                "\"folder\":\"Root\\Tasks\"," +
                "\"subject\":\"Eeriwo\"," +
                "\"body\":\"\"," +
                "\"startDate\":\"20081128T000000Z\"," +
                "\"dueDate\":\"20081128T000000\"," +
                "\"status\":\"0\"," +
                "\"importance\":\"2\"," +

                "\"complete\":\"0\"," +
                "\"percentComplete\":\"\"," +

                "\"reminder\":\"0\"," +
                "\"reminderDate\":\"0000-00-00 00:00:00\"," +

                "\"dateCompleted\":\"\"," +
                "\"totalWork\":\"\"," +
                "\"actualWork\":\"\"," +
                "\"mileage\":\"\"," +
                "\"billingInformation\":\"\"," +
                "\"companies\":\"\"," +

                "\"SessionID\":\"0\",\"categories\":\"\"," +
                "\"contacts\":\"\"," +
                "\"customerID\":\"406940\"," +
                "\"SentReminder\":\"No\"," +
                "\"Timestamp\":\"2008-10-09 04:53:39\"," +
                "\"uuid\":\"\"" +
                "}" +
                "}" +
                "}";

        JsonItem<Task> task = converter.fromJSON(jsonResult);

        Integer mileage = task.getItem().getMileage();

        assertEquals(Integer.valueOf(0), mileage);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void test_PercentComplete() throws Exception{

        // json from server
        String jsonResult =
                "{\"data\":" +
                "{\"content-type\":\"application/json-task\"," +
                "\"item\":" +
                "{" +
                "\"key\":\"87301\"," +
                "\"folder\":\"Root\\Tasks\"," +
                "\"subject\":\"Eeriwo\"," +
                "\"body\":\"\"," +
                "\"startDate\":\"20081128T000000Z\"," +
                "\"dueDate\":\"20081128T000000Z\"," +
                "\"status\":\"0\"," +
                "\"importance\":\"2\"," +

                "\"complete\":\"0\"," +
                "\"percentComplete\":\"34.000\"," +

                "\"reminder\":\"0\"," +
                "\"reminderDate\":\"20081128T000000\"," +

                "\"dateCompleted\":\"\"," +
                "\"totalWork\":\"\"," +
                "\"actualWork\":\"\"," +
                "\"mileage\":\"\"," +
                "\"billingInformation\":\"\"," +
                "\"companies\":\"\"," +

                "\"SessionID\":\"0\",\"categories\":\"\"," +
                "\"contacts\":\"\"," +
                "\"customerID\":\"406940\"," +
                "\"SentReminder\":\"No\"," +
                "\"Timestamp\":\"2008-10-09 04:53:39\"," +
                "\"uuid\":\"\"" +
                "}" +
                "}" +
                "}";

        JsonItem<Task> task = converter.fromJSON(jsonResult);

        String percent = task.getItem().getPercentComplete().getPropertyValueAsString();

        assertEquals(34, Integer.parseInt(percent));
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void test_ALARM_VTODO_2_JSON() throws Exception{

        // before synclet
        /*
        BEGIN:VCALENDAR
        VERSION:1.0
        BEGIN:VTODO
        UID:356DWEE74EE0b72U697YS3
        SUMMARY:Task n78
        DTSTART:20090129T000000
        DUE:20090129T000000
        X-EPOCTODOLIST:TODO
        X-EPOCAGENDAENTRYTYPE:TODO
        CLASS:PRIVATE
        SEQUENCE:0
        X-METHOD:NONE
        AALARM;TYPE=X-EPOCSOUND:20090129T080000;;;
        LAST-MODIFIED:20090128T142823Z
        PRIORITY:2
        X-SYMBIAN-LUID:964
        END:VTODO
        END:VCALENDAR
        */

        // after synclet

        // vCalendar from the device
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("UID:356DWEE74EE0b72U697YS3\n")
            .append("SUMMARY:Task n78\n")
            .append("DTSTART:20090129T000000\n")
            .append("DUE:20090129T000000\n")
            .append("X-EPOCTODOLIST:TODO\n")
            .append("X-EPOCAGENDAENTRYTYPE:TODO\n")
            .append("CLASS:PRIVATE\n")
            .append("SEQUENCE:0\n")
            .append("X-METHOD:NONE\n")
            .append("AALARM;TYPE=X-EPOCSOUND:20090129T080000;;;\n")
            .append("LAST-MODIFIED:20090128T142823Z\n")
            .append("PRIORITY:2\n")
            .append("X-SYMBIAN-LUID:964\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR\n");

        String tzid = "America/Mexico_City";

        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      TimeZone.getTimeZone(tzid),
                                                      "UTF-8");

        String dtStart = c.getTask().getDtStart().getPropertyValueAsString();
        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();
        boolean allday = c.getTask().isAllDay();

        // in the old version the "0 time" task was an allday
        // now we assume that "0 time" task is not an allday
        assertEquals("20090129T060000Z", dtStart);
        assertEquals("20090129T060000Z", dueDate);
        assertEquals(false, allday);

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        // Task to JSON
        converter.setServerTimeZoneID(tzid);
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":{")
            .append("\"folder\":\"12\",")
            .append("\"subject\":\"Task n78\",")
            .append("\"tzid\":\"America/Mexico_City\",")
            .append("\"startDate\":\"20090129T060000Z\",")
            .append("\"dueDate\":\"20090129T060000Z\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PRIVATE\",")
            .append("\"importance\":1,")
            .append("\"complete\":false,")
//            .append("\"status\":0,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":true,")
            .append("\"reminderDate\":\"20090129T140000Z\",")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}")
            .append("}}}");


        assertEquals(jsonExpected.toString(), jsonResult);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void test_ALARM_SIF_2_JSON() throws Exception{

        // xml from the device
        StringBuilder xml =
            new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            .append("<task>")
            .append("<SIFVersion>1.1</SIFVersion>")
            .append("<Folder>DEFAULT_FOLDER</Folder>")
            .append("<Subject>task from outlook</Subject>")
            .append("<StartDate>2009-01-30</StartDate>")
            .append("<DueDate>2009-01-30</DueDate>")
            .append("<ReminderSet>1</ReminderSet>")
            .append("<ReminderSoundFile/>")
            .append("<ReminderTime>20090130T080000</ReminderTime>")
            .append("<ActualWork>0</ActualWork>")
            .append("<BillingInformation/>")
            .append("<Body/>")
            .append("<Categories/>")
            .append("<Companies/>")
            .append("<Complete>0</Complete>")
            .append("<DateCompleted/>")
            .append("<Importance>1</Importance>")
            .append("<Mileage/>")
            .append("<PercentComplete>0</PercentComplete>")
            .append("<Sensitivity>0</Sensitivity>")
            .append("<Status>0</Status>")
            .append("<IsRecurring>0</IsRecurring>")
            .append("<TeamTask>0</TeamTask>")
            .append("<TotalWork>0</TotalWork>")
            .append("</task>");

        String tzid = "America/Mexico_City";

        Calendar c = TestUtility.sif2Calendar(xml.toString());

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        // Task to JSON
        converter.setServerTimeZoneID(tzid);
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"task from outlook\",")
            .append("\"tzid\":\"America/Mexico_City\",")
            .append("\"startDate\":\"20090130\",")
            .append("\"dueDate\":\"20090130\",")
            .append("\"allDay\":true,")
            .append("\"categories\":\"\",")
            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":1,")
            .append("\"complete\":false,")
            .append("\"status\":0,")
            .append("\"percentComplete\":\"0\",")
            .append("\"reminder\":true,")
            .append("\"reminderDate\":\"20090130T080000\",")
            .append("\"reminderSoundFile\":\"\",")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"0\",")
            .append("\"totalWork\":\"0\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0,")
            .append("\"companies\":\"\"}}}");

        assertEquals(jsonExpected.toString(), jsonResult);
    }

    public void testVTodo_1() throws Exception {

        /* Before the synclet:
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VTODO
            UID:1507
            SUMMARY:todo with enddate N9500
            DUE:20070223T000000
            X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0
            STATUS:NEEDS ACTION
            X-EPOCAGENDAENTRYTYPE:TODO
            CLASS:PUBLIC
            DCREATED:20070223T000000
            LAST-MODIFIED:20070224T034300Z
            PRIORITY:3
            END:VTODO
            END:VCALENDAR
         */

        // After the synclet:
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("UID:1507\n")
            .append("SUMMARY:todo with enddate N9500\n")
            .append("DUE:20070223T000000\n")
            .append("X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0\n")
            .append("STATUS:NEEDS ACTION\n")
            .append("X-EPOCAGENDAENTRYTYPE:TODO\n")
            .append("CLASS:PUBLIC\n")
            .append("DCREATED:20070223T000000\n")
            .append("LAST-MODIFIED:20070224T034300Z\n")
            .append("PRIORITY:3\n")
            .append("RRULE:\n")
            .append("AALARM:;;;\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();

        assertEquals("20070222T230000Z", dueDate);

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"todo with enddate N9500\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
            //.append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20070222T230000Z\",")
            .append("\"allDay\":false,")
            //.append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":0,")
            .append("\"complete\":false,")
            .append("\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
            //.append("\"companies\":\"\"")
            .append("}}}")
            ;

        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = TestUtility.calendar2webCalendar(c,
                                                              "text/x-vcalendar",
                                                              tz,
                                                              "UTF-8");
        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
//            .append("TZ:-0800\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20070311T020000;20071104T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20080309T020000;20081102T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20090308T020000;20091101T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20100314T020000;20101107T020000;America/Los_Angeles;America/Los_Angeles\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:todo with enddate N9500\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:20070222T230000Z\r\n")
            .append("PRIORITY:3\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
            .append("STATUS:NEEDS ACTION\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    public void testVTodo_2() throws Exception {

        /* Before the synclet:
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VTODO
            UID:1503
            SUMMARY:todo  N9500
            X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0
            STATUS:NEEDS ACTION
            X-EPOCAGENDAENTRYTYPE:TODO
            CLASS:PUBLIC
            DCREATED:20070223T000000
            LAST-MODIFIED:20070224T032300Z
            PRIORITY:3
            END:VTODO
            END:VCALENDAR
         */

        // After the synclet:
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("UID:1503\n")
            .append("SUMMARY:todo N9500\n")
            .append("X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0\n")
            .append("STATUS:NEEDS ACTION\n")
            .append("X-EPOCAGENDAENTRYTYPE:TODO\n")
            .append("CLASS:PUBLIC\n")
            .append("DCREATED:20070223T000000\n")
            .append("LAST-MODIFIED:20070224T032300Z\n")
            .append("PRIORITY:3\n")
            .append("RRULE:\n")
            .append("AALARM:;;;\n")
            .append("DUE:\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"todo N9500\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":0,")
            .append("\"complete\":false,")
            .append("\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}")
            .append("}}}");


        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = 
            TestUtility.calendar2webCalendar(c, "text/x-vcalendar",tz, "UTF-8");

        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
//            .append("TZ:-0800\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20080309T020000;20081102T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20090308T020000;20091101T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20100314T020000;20101107T020000;America/Los_Angeles;America/Los_Angeles\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:todo N9500\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:\r\n")
            .append("PRIORITY:3\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
            .append("STATUS:NEEDS ACTION\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    public void testVTodo_3() throws Exception {

        /* Before the synclet:
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VTODO
            DUE:20070530T110000
            DTEND:20070530T110000
            DALARM:20070530T110000
            PRIORITY:1
            STATUS:NEEDS ACTION
            SUMMARY:Todo with no alarm
            END:VTODO
            END:VCALENDAR
         */

        // After the synclet:
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("DUE:20070530T110000\n")
            .append("DTEND:20070530T110000\n")
            .append("DALARM:20070530T110000\n")
            .append("PRIORITY:1\n")
            .append("STATUS:NEEDS ACTION\n")
            .append("SUMMARY:Todo with no alarm\n")
            .append("AALARM:\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();

        assertEquals("20070530T090000Z", dueDate);

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"Todo with no alarm\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20070530T090000Z\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":2,")
            .append("\"complete\":false,")
            .append("\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = 
            TestUtility.calendar2webCalendar(c, "text/x-vcalendar", tz,"UTF-8");
        
        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
//            .append("TZ:-0800\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20070311T020000;20071104T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20080309T020000;20081102T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20090308T020000;20091101T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20100314T020000;20101107T020000;America/Los_Angeles;America/Los_Angeles\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:Todo with no alarm\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:20070530T090000Z\r\n")
            .append("PRIORITY:1\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
            .append("STATUS:NEEDS ACTION\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    public void testVTodo_4() throws Exception {

        /* Before the synclet:
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VTODO
            DUE:20070531T220000
            DTEND:20070531T220000
            PRIORITY:2
            STATUS:NEEDS ACTION
            SUMMARY:Todo without alarm
            END:VTODO
            END:VCALENDAR
         */

        // After the synclet:
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("DUE:20070531T220000\n")
            .append("DTEND:20070531T220000\n")
            .append("PRIORITY:2\n")
            .append("STATUS:NEEDS ACTION\n")
            .append("SUMMARY:Todo without alarm\n")
            .append("AALARM:\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Europe/Moscow");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();

        assertEquals("20070531T180000Z", dueDate);

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"Todo without alarm\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20070531T180000Z\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":1,")
            .append("\"complete\":false,")
            .append("\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = TestUtility.calendar2webCalendar(c,
                                                              "text/x-vcalendar",
                                                              tz,
                                                              "UTF-8");
        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
//            .append("TZ:-0800\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20070311T020000;20071104T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20080309T020000;20081102T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20090308T020000;20091101T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20100314T020000;20101107T020000;America/Los_Angeles;America/Los_Angeles\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:Todo without alarm\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:20070531T180000Z\r\n")
            .append("PRIORITY:2\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
            .append("STATUS:NEEDS ACTION\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    public void testVTodo_5() throws Exception {

        /* Before the synclet:
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VTODO
            DUE:20070605T220000
            DTEND:20070605T220000
            DALARM:20070605T220000
            AALARM:20070605T220000
            PRIORITY:1
            STATUS:NEEDS ACTION
            SUMMARY:Todo with alarm
            END:VTODO
            END:VCALENDAR
         */

        // After the synclet:
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("DUE:20070605T220000\n")
            .append("DTEND:20070605T220000\n")
            .append("DALARM:20070605T220000\n")
            .append("AALARM:20070605T220000\n")
            .append("PRIORITY:1\n")
            .append("STATUS:NEEDS ACTION\n")
            .append("SUMMARY:Todo with alarm\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Europe/Moscow");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();

        assertEquals("20070605T180000Z", dueDate);

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"Todo with alarm\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20070605T180000Z\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":2,")
            .append("\"complete\":false,")
            .append("\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":true,")
            .append("\"reminderDate\":\"20070605T180000Z\",")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = 
            TestUtility.calendar2webCalendar(c, "text/x-vcalendar", tz,"UTF-8");

        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
//            .append("TZ:-0800\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20070311T020000;20071104T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20080309T020000;20081102T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20090308T020000;20091101T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20100314T020000;20101107T020000;America/Los_Angeles;America/Los_Angeles\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:Todo with alarm\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:20070605T180000Z\r\n")
            .append("PRIORITY:1\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
            .append("STATUS:NEEDS ACTION\r\n")
            .append("AALARM:20070605T180000Z;;0;\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    public void testVTodo_6() throws Exception {

        /* Before the synclet:
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VTODO
            DUE:20070517T090000
            DTEND:20070517T090000
            PRIORITY:2
            STATUS:NEEDS ACTION
            SUMMARY:Todo
            END:VTODO
            END:VCALENDAR
         */

        // After the synclet:
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("DUE:20070517T090000\n")
            .append("DTEND:20070517T090000\n")
            .append("PRIORITY:2\n")
            .append("STATUS:NEEDS ACTION\n")
            .append("SUMMARY:Todo\n")
            .append("AALARM:\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Europe/London");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();

        assertEquals("20070517T080000Z", dueDate);

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"Todo\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20070517T080000Z\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":1,")
            .append("\"complete\":false,")
            .append("\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");


        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = TestUtility.calendar2webCalendar(c,
                                                              "text/x-vcalendar",
                                                              tz,
                                                              "UTF-8");
        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
//            .append("TZ:-0800\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20070311T020000;20071104T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20080309T020000;20081102T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20090308T020000;20091101T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20100314T020000;20101107T020000;America/Los_Angeles;America/Los_Angeles\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:Todo\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:20070517T080000Z\r\n")
            .append("PRIORITY:2\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
            .append("STATUS:NEEDS ACTION\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    public void testVTodo_7() throws Exception {

        /* Before the synclet:
            BEGIN:VCALENDAR
            PRODID:-//ABC Corporation//NONSGML My Product//EN
            VERSION:1.0
            BEGIN:VTODO
            UID:103
            SUMMARY:Update the Balance
            STATUS:NEEDS ACTION
            CLASS:PUBLIC
            DCREATED:20071014T000000
            LAST-MODIFIED:20071015T054000Z
            PRIORITY:1
            DUE:20071015T054000Z
            END:VTODO
            END:VCALENDAR
         */

        // After the synclet:
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("UID:1507\n")
            .append("SUMMARY:todo with enddate N9500\n")
            .append("DUE:20070223T000000\n")
            .append("X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0\n")
            .append("STATUS:NEEDS ACTION\n")
            .append("X-EPOCAGENDAENTRYTYPE:TODO\n")
            .append("CLASS:PUBLIC\n")
            .append("DCREATED:20070223T000000\n")
            .append("LAST-MODIFIED:20070224T034300Z\n")
            .append("PRIORITY:3\n")
            .append("RRULE:\n")
            .append("AALARM:;;;\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();

        assertEquals("20070222T230000Z", dueDate);

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":{")
            .append("\"folder\":\"12\",")
            .append("\"subject\":\"todo with enddate N9500\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20070222T230000Z\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":0,")
            .append("\"complete\":false,")
            .append("\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");
        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = 
            TestUtility.calendar2webCalendar(c, "text/x-vcalendar",tz, "UTF-8");

        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
//            .append("TZ:-0800\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20070311T020000;20071104T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20080309T020000;20081102T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20090308T020000;20091101T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20100314T020000;20101107T020000;America/Los_Angeles;America/Los_Angeles\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:todo with enddate N9500\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:20070222T230000Z\r\n")
            .append("PRIORITY:3\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
            .append("STATUS:NEEDS ACTION\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    public void testVTodo_8() throws Exception {

        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("DUE:2007-02-23\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();

        assertEquals("2007-02-23", dueDate); // Foundation bug, perhaps

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20070223\",")
            .append("\"allDay\":true,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":0,")
            .append("\"complete\":false,")
//            .append("\"status\":0,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = 
            TestUtility.calendar2webCalendar(c, "text/x-vcalendar",tz, "UTF-8");

        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:20070223T235900\r\n")
            .append("PRIORITY:3\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
//            .append("STATUS:ACCEPTED\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:1\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    public void testVTodo_9() throws Exception {

        /* Before the synclet:
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VTODO
            UID:1507
            SUMMARY:todo with enddate N9500
            DUE:20070223T000000
            X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0
            STATUS:NEEDS ACTION
            X-EPOCAGENDAENTRYTYPE:TODO
            CLASS:PUBLIC
            DCREATED:20070223T000000
            LAST-MODIFIED:20070224T034300Z
            PRIORITY:3
            END:VTODO
            END:VCALENDAR
         */

        // After the synclet:
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("UID:1507\n")
            .append("SUMMARY:todo with enddate N9500\n")
            .append("DUE:20070223T000000\n")
            .append("X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0\n")
            .append("STATUS:NEEDS ACTION\n")
            .append("X-EPOCAGENDAENTRYTYPE:TODO\n")
            .append("CLASS:PUBLIC\n")
            .append("DCREATED:20070223T000000\n")
            .append("LAST-MODIFIED:20070224T034300Z\n")
            .append("PRIORITY:3\n")
            .append("RRULE:\n")
            .append("AALARM:;;;\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();

        assertEquals("20070222T230000Z", dueDate);

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"todo with enddate N9500\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20070222T230000Z\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":0,\"complete\":false,\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = 
            TestUtility.calendar2webCalendar(c,"text/x-vcalendar", tz, "UTF-8");

        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
//            .append("TZ:-0800\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20070311T020000;20071104T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20080309T020000;20081102T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20090308T020000;20091101T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20100314T020000;20101107T020000;America/Los_Angeles;America/Los_Angeles\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:todo with enddate N9500\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:20070222T230000Z\r\n")
            .append("PRIORITY:3\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
            .append("STATUS:NEEDS ACTION\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    public void testVTodo_10() throws Exception {

        /* Before the synclet:
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VTODO
            UID:1507
            SUMMARY:todo with enddate N9500
            DUE:20070223T000000
            X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0
            STATUS:NEEDS ACTION
            X-EPOCAGENDAENTRYTYPE:TODO
            CLASS:PUBLIC
            DCREATED:20070223T000000
            LAST-MODIFIED:20070224T034300Z
            PRIORITY:3
            END:VTODO
            END:VCALENDAR
         */

        // After the synclet:
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("UID:1507\n")
            .append("SUMMARY:todo with enddate N9500\n")
            .append("DUE:20070223T000000\n")
            .append("X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0\n")
            .append("STATUS:NEEDS ACTION\n")
            .append("X-EPOCAGENDAENTRYTYPE:TODO\n")
            .append("CLASS:PUBLIC\n")
            .append("DCREATED:20070223T000000\n")
            .append("LAST-MODIFIED:20070224T034300Z\n")
            .append("PRIORITY:3\n")
            .append("RRULE:\n")
            .append("AALARM:;;;\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();

        assertEquals("20070222T230000Z", dueDate);

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"todo with enddate N9500\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20070222T230000Z\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":0,")
            .append("\"complete\":false,")
            .append("\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = TestUtility.calendar2webCalendar(c,
                                                              "text/x-vcalendar",
                                                              tz,
                                                              "UTF-8");

        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
//            .append("TZ:-0800\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20070311T020000;20071104T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20080309T020000;20081102T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20090308T020000;20091101T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20100314T020000;20101107T020000;America/Los_Angeles;America/Los_Angeles\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:todo with enddate N9500\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:20070222T230000Z\r\n")
            .append("PRIORITY:3\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
            .append("STATUS:NEEDS ACTION\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    public void testVTodo_11() throws Exception {

        /* Before the synclet:
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VTODO
            UID:1507
            SUMMARY:todo with enddate N9500
            DUE:20070223T000000
            X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0
            STATUS:NEEDS ACTION
            X-EPOCAGENDAENTRYTYPE:TODO
            CLASS:PUBLIC
            DCREATED:20070223T000000
            LAST-MODIFIED:20070224T034300Z
            PRIORITY:3
            END:VTODO
            END:VCALENDAR
         */

        // After the synclet:
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("UID:1507\n")
            .append("SUMMARY:todo with enddate N9500\n")
            .append("DUE:20070223T000000\n")
            .append("X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0\n")
            .append("STATUS:NEEDS ACTION\n")
            .append("X-EPOCAGENDAENTRYTYPE:TODO\n")
            .append("CLASS:PUBLIC\n")
            .append("DCREATED:20070223T000000\n")
            .append("LAST-MODIFIED:20070224T034300Z\n")
            .append("PRIORITY:3\n")
            .append("RRULE:\n")
            .append("AALARM:;;;\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();

        assertEquals("20070222T230000Z", dueDate);

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"todo with enddate N9500\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20070222T230000Z\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":0,")
            .append("\"complete\":false,")
            .append("\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = TestUtility.calendar2webCalendar(c,
                                                              "text/x-vcalendar",
                                                              tz,
                                                              "UTF-8");

        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
//            .append("TZ:-0800\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20070311T020000;20071104T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20080309T020000;20081102T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20090308T020000;20091101T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20100314T020000;20101107T020000;America/Los_Angeles;America/Los_Angeles\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:todo with enddate N9500\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:20070222T230000Z\r\n")
            .append("PRIORITY:3\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
            .append("STATUS:NEEDS ACTION\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    public void testVTodo_12() throws Exception {

        /* Before the synclet:
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VTODO
            UID:1507
            SUMMARY:todo with enddate N9500
            DUE:20070223T000000
            X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0
            STATUS:NEEDS ACTION
            X-EPOCAGENDAENTRYTYPE:TODO
            CLASS:PUBLIC
            DCREATED:20070223T000000
            LAST-MODIFIED:20070224T034300Z
            PRIORITY:3
            END:VTODO
            END:VCALENDAR
         */

        // After the synclet:
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("UID:1507\n")
            .append("SUMMARY:todo with enddate N9500\n")
            .append("DUE:20070223T000000\n")
            .append("X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0\n")
            .append("STATUS:NEEDS ACTION\n")
            .append("X-EPOCAGENDAENTRYTYPE:TODO\n")
            .append("CLASS:PUBLIC\n")
            .append("DCREATED:20070223T000000\n")
            .append("LAST-MODIFIED:20070224T034300Z\n")
            .append("PRIORITY:3\n")
            .append("RRULE:\n")
            .append("AALARM:;;;\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();

        assertEquals("20070222T230000Z", dueDate);

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"todo with enddate N9500\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20070222T230000Z\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":0,")
            .append("\"complete\":false,")
            .append("\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = TestUtility.calendar2webCalendar(c,
                                                              "text/x-vcalendar",
                                                              tz,
                                                              "UTF-8");
        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
//            .append("TZ:-0800\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20070311T020000;20071104T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20080309T020000;20081102T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20090308T020000;20091101T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20100314T020000;20101107T020000;America/Los_Angeles;America/Los_Angeles\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:todo with enddate N9500\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:20070222T230000Z\r\n")
            .append("PRIORITY:3\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
            .append("STATUS:NEEDS ACTION\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    public void testVTodo_13() throws Exception {

        /* Before the synclet:
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VTODO
            UID:1507
            SUMMARY:todo with enddate N9500
            DUE:20070223T000000
            X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0
            STATUS:NEEDS ACTION
            X-EPOCAGENDAENTRYTYPE:TODO
            CLASS:PUBLIC
            DCREATED:20070223T000000
            LAST-MODIFIED:20070224T034300Z
            PRIORITY:3
            END:VTODO
            END:VCALENDAR
         */

        // After the synclet:
        StringBuilder vtodo = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("UID:1507\n")
            .append("SUMMARY:todo with enddate N9500\n")
            .append("DUE:20070223T000000\n")
            .append("X-EPOCTODOLIST;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Attivit=C3=A0\n")
            .append("STATUS:NEEDS ACTION\n")
            .append("X-EPOCAGENDAENTRYTYPE:TODO\n")
            .append("CLASS:PUBLIC\n")
            .append("DCREATED:20070223T000000\n")
            .append("LAST-MODIFIED:20070224T034300Z\n")
            .append("PRIORITY:3\n")
            .append("RRULE:\n")
            .append("AALARM:;;;\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR");

        TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
        Calendar c = TestUtility.webCalendar2Calendar(vtodo.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        String dueDate = c.getTask().getDueDate().getPropertyValueAsString();

        assertEquals("20070222T230000Z", dueDate);

        Task t = c.getTask();
        t.setFolder(new Property("12"));
        JsonItem<Task> item = new JsonItem<Task>();
        item.setItem(t);

        converter.setServerTimeZoneID("America/Los_Angeles");
        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-task\",\"item\":")
            .append("{\"folder\":\"12\",")
            .append("\"subject\":\"todo with enddate N9500\",")
            .append("\"tzid\":\"America/Los_Angeles\",")
//            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20070222T230000Z\",")
            .append("\"allDay\":false,")
//            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":0,")
            .append("\"complete\":false,")
            .append("\"status\":6,")
            .append("\"percentComplete\":\"\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals(jsonExpected.toString(), jsonResult);

        item = converter.fromJSON(jsonResult);
        c.setTask(item.getItem());
        String vtodoResult = 
            TestUtility.calendar2webCalendar(c, "text/x-vcalendar",tz, "UTF-8");

        StringBuilder vtodoExpected = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
//            .append("TZ:-0800\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20070311T020000;20071104T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20080309T020000;20081102T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20090308T020000;20091101T020000;America/Los_Angeles;America/Los_Angeles\r\n")
//            .append("DAYLIGHT:TRUE;-0700;20100314T020000;20101107T020000;America/Los_Angeles;America/Los_Angeles\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:todo with enddate N9500\r\n")
//            .append("DESCRIPTION:\r\n")
            .append("CLASS:PUBLIC\r\n")
//            .append("DTSTART:\r\n")
            .append("DUE:20070222T230000Z\r\n")
            .append("PRIORITY:3\r\n")
//            .append("ORGANIZER:\r\n")
            .append("PERCENT-COMPLETE:0\r\n")
            .append("STATUS:NEEDS ACTION\r\n")
            .append("X-FUNAMBOL-FOLDER:12\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        assertEquals(vtodoExpected.toString(), vtodoResult);
    }

    /**
     * Tests the case where a backend sends only some of the fields in the json,
     * in this case the item returned should include only the fields that came 
     * in the json.
     * @throws java.lang.Exception
     */
    public void test_convertJsonTaskWithOnefield() throws Exception {
        StringBuilder expectedVcal = new StringBuilder("BEGIN:VCALENDAR\n");
        expectedVcal.append("VERSION:1.0\n")
                    .append("BEGIN:VTODO\n")
                    .append("SUMMARY:Eeriwo\n")
                    .append("CLASS:PUBLIC\n")
                    .append("X-FUNAMBOL-FOLDER:\n")
                    .append("X-FUNAMBOL-ALLDAY:0\n")
                    .append("END:VTODO\n")
                    .append("END:VCALENDAR\n");

        StringBuilder expectedIcal = new StringBuilder("BEGIN:VCALENDAR\n");
        expectedIcal.append("VERSION:2.0\n")
                    .append("BEGIN:VTODO\n")
                    .append("SUMMARY:Eeriwo\n")
                    .append("CLASS:PUBLIC\n")
                    .append("X-FUNAMBOL-FOLDER:\n")
                    .append("X-FUNAMBOL-ALLDAY:0\n")
                    .append("END:VTODO\n")
                    .append("END:VCALENDAR\n");

        // json from server
        StringBuilder jsonResult = new StringBuilder("{\"data\":");
        jsonResult.append("{\"content-type\":\"application/json-task\",")
                  .append("\"item\":{\"key\":\"87301\",\"folder\": \"\",")
                  .append("\"Timestamp\":\"2008-10-09 04:53:39\",")
                  .append("\"folder_id\":\"1832447\",")
                  .append("\"subject\":\"Eeriwo\"}}}");

        JsonItem<Task> task = converter.fromJSON(jsonResult.toString());

        Calendar cal = new Calendar(task.getItem());

        String vcalResult =
            TestUtility.calendar2webCalendar(cal, "text/x-vcalendar", null, "UTF-8");
        String icalResult =
            TestUtility.calendar2webCalendar(cal, "text/vcalendar", null, "UTF-8");

       assertEquals("Wrong vcal returned",
                    expectedVcal.toString(), vcalResult.replaceAll("\\r", ""));
       assertEquals("Wrong ical returned",
                    expectedIcal.toString(), icalResult.replaceAll("\\r", ""));

    }

    /**
     * Tests if the converter handles correctly a json item without folder.
     * @throws java.lang.Exception
     */
    public void test_JsonWithoutFolderToIcalVcal() throws Exception {
        StringBuilder expectedVcal = new StringBuilder("BEGIN:VCALENDAR\n");
        expectedVcal.append("VERSION:1.0\n")
                    .append("BEGIN:VTODO\n")
                    .append("SUMMARY:Eeriwo\n")
                    .append("CLASS:PUBLIC\n")
                    .append("X-FUNAMBOL-ALLDAY:0\n")
                    .append("END:VTODO\n")
                    .append("END:VCALENDAR\n");

        StringBuilder expectedIcal = new StringBuilder("BEGIN:VCALENDAR\n");
        expectedIcal.append("VERSION:2.0\n")
                    .append("BEGIN:VTODO\n")
                    .append("SUMMARY:Eeriwo\n")
                    .append("CLASS:PUBLIC\n")
                    .append("X-FUNAMBOL-ALLDAY:0\n")
                    .append("END:VTODO\n")
                    .append("END:VCALENDAR\n");

        // json from server
        StringBuilder jsonResult = new StringBuilder("{\"data\":");
        jsonResult.append("{\"content-type\":\"application/json-task\",")
                  .append("\"item\":{\"key\":\"87301\",")
                  .append("\"Timestamp\":\"2008-10-09 04:53:39\",")
                  .append("\"folder_id\":\"1832447\",")
                  .append("\"subject\":\"Eeriwo\"}}}");

        JsonItem<Task> task = converter.fromJSON(jsonResult.toString());

        Calendar cal = new Calendar(task.getItem());
        String vcalResult = 
            TestUtility.calendar2webCalendar(cal, "text/x-vcalendar", null, "UTF-8");
        String iCalResult =
            TestUtility.calendar2webCalendar(cal, "text/vcalendar", null, "UTF-8");

        assertEquals("Wrong vcal returned",
                     expectedVcal.toString(), vcalResult.replaceAll("\\r", ""));
        assertEquals("Wrong iCal returned",
                     expectedIcal.toString(), iCalResult.replaceAll("\\r", ""));

    }

    public void test_CompleteFieldParsing() throws Exception{
        StringBuilder vcal = new StringBuilder("BEGIN:VCALENDAR\n")
                .append("VERSION:1.0\n")
                .append("BEGIN:VTODO\n")
                .append("X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\n")
                .append("X-FUNAMBOL-ALLDAY:1\n")
                .append("SUMMARY:conflicttest - changed inBT\n")
                .append("DESCRIPTION;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:\n")
                .append("PRIORITY:1\n")
                .append("X-FUNAMBOL-ACTUALWORK:0\n")
                .append("PERCENT-COMPLETE:0\n")
                .append("DUE:20090924\n")
                .append("STATUS:IN-PROCESS\n")
                .append("DTSTART:\n")
                .append("CATEGORIES:\n")
                .append("CLASS:PUBLIC\n")
                .append("RRULE:\n")
                .append("AALARM:\n")
                .append("X-FUNAMBOL-ACTUALWORK:0\n")
                .append("X-FUNAMBOL-BILLINGINFO:\n")
                .append("X-FUNAMBOL-COMPANIES:\n")
                .append("X-FUNAMBOL-MILEAGE:\n")
                .append("X-FUNAMBOL-TEAMTASK:0\n")
                .append("X-FUNAMBOL-TOTALWORK:0\n")
                .append("END:VTODO\n")
                .append("END:VCALENDAR\n");

        StringBuilder expectedJson = new StringBuilder("{\"data\":{")
            .append("\"content-type\":\"application/json-task\",\"item\":{")
            .append("\"key\":\"0\",")
            .append("\"state\":\"A\",")
            .append("\"folder\":\"pippo\",")
            .append("\"subject\":\"conflicttest - changed inBT\",")
            .append("\"tzid\":\"Europe/Rome\",")
            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20090924\",")
            .append("\"allDay\":true,")
            .append("\"categories\":\"\",")
            .append("\"body\":\"\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":2,")
            .append("\"complete\":false,")
            .append("\"status\":3,")
            .append("\"percentComplete\":\"0\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
        Calendar c = TestUtility.webCalendar2Calendar(vcal.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        JsonItem<Task> item = new JsonItem<Task>();
        item.setContentType("type");
        item.setKey("0");
        item.setState("A");

        Task t = c.getTask();
        t.setFolder(new Property("pippo"));
        t.getComplete().setPropertyValue(""); // patch
        item.setItem(t);
        
        String jsonResult = converter.toJSON(item);

        assertEquals(expectedJson.toString(), jsonResult);

    }

    public void testToJSON_VTodoWithClass() throws Exception {
        StringBuilder cal = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\n")
            .append("X-FUNAMBOL-ALLDAY:1\n")
            .append("SUMMARY:remember to call Gary\n")
            .append("DESCRIPTION:reminder\n")
            .append("COMPLETED:\n")
            .append("DUE:20100924\n")
            .append("DTSTART:\n")
            .append("CATEGORIES:\n")
            .append("STATUS:NEED\n")
            .append("PRIORITY:3\n")
            .append("PERCENT-COMPLETE:10\n")
            .append("CLASS:PRIVATE\n")
            .append("RRULE:\n")
            .append("AALARM:\n")
            .append("X-FUNAMBOL-ACTUALWORK:0\n")
            .append("X-FUNAMBOL-BILLINGINFO:\n")
            .append("X-FUNAMBOL-COMPANIES:\n")
            .append("X-FUNAMBOL-MILEAGE:\n")
            .append("X-FUNAMBOL-TEAMTASK:0\n")
            .append("X-FUNAMBOL-TOTALWORK:0\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR\n");

        TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
        Calendar c = TestUtility.webCalendar2Calendar(cal.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        JsonItem<Task> item = new JsonItem<Task>();
        item.setContentType("type");
        item.setKey("0");
        item.setState("A");
        item.setItem(c.getTask());

        String result = converter.toJSON(item);

        StringBuilder expected = new StringBuilder("{\"data\":{")
            .append("\"content-type\":\"application/json-task\",\"item\":{")
            .append("\"key\":\"0\",")
            .append("\"state\":\"A\",")
            .append("\"folder\":\"Root\\\\Tasks\",")
            .append("\"subject\":\"remember to call Gary\",")
            .append("\"tzid\":\"Europe/Rome\",")
            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20100924\",")
            .append("\"allDay\":true,")
            .append("\"categories\":\"\",")
            .append("\"body\":\"reminder\",")
            .append("\"sensitivity\":\"PRIVATE\",")
            .append("\"importance\":0,")
            .append("\"complete\":false,")
//            .append("\"status\":0,")
            .append("\"percentComplete\":\"10\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals("Wrong JSON content converted from Task",
                     expected.toString(), result);

        JsonItem<Task> item2 = converter.fromJSON(result);
        String result2 = converter.toJSON(item2);


        StringBuilder expected2 = new StringBuilder("{\"data\":{")
            .append("\"content-type\":\"application/json-task\",\"item\":{")
            .append("\"key\":\"0\",")
            .append("\"state\":\"A\",")
            .append("\"folder\":\"Root\\\\Tasks\",")
            .append("\"subject\":\"remember to call Gary\",")
            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20100924\",")
            .append("\"allDay\":true,")
            .append("\"categories\":\"\",")
            .append("\"body\":\"reminder\",")
            .append("\"sensitivity\":\"PRIVATE\",")
            .append("\"importance\":0,")
            .append("\"complete\":false,")
//            .append("\"status\":0,")
            .append("\"percentComplete\":\"10\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"0\",")
            .append("\"totalWork\":\"0\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals("Wrong JSON object converted from JSONItem", 
                     expected2.toString(), result2);
    }

    public void testToJSON_VTodoWithoutClass() throws Exception {
        StringBuilder cal = new StringBuilder("BEGIN:VCALENDAR\n")
            .append("VERSION:1.0\n")
            .append("BEGIN:VTODO\n")
            .append("X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\n")
            .append("X-FUNAMBOL-ALLDAY:1\n")
            .append("SUMMARY:remember to call Gary\n")
            .append("DESCRIPTION:reminder\n")
            .append("COMPLETED:\n")
            .append("DUE:20100925\n")
            .append("DTSTART:\n")
            .append("CATEGORIES:\n")
            .append("STATUS:NEED\n")
            .append("PRIORITY:2\n")
            .append("PERCENT-COMPLETE:20\n")
            .append("RRULE:\n")
            .append("AALARM:\n")
            .append("X-FUNAMBOL-ACTUALWORK:0\n")
            .append("X-FUNAMBOL-BILLINGINFO:\n")
            .append("X-FUNAMBOL-COMPANIES:\n")
            .append("X-FUNAMBOL-MILEAGE:\n")
            .append("X-FUNAMBOL-TEAMTASK:0\n")
            .append("X-FUNAMBOL-TOTALWORK:0\n")
            .append("END:VTODO\n")
            .append("END:VCALENDAR\n");

        TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
        Calendar c = TestUtility.webCalendar2Calendar(cal.toString(),
                                                      "text/x-vcalendar",
                                                      tz,
                                                      "UTF-8");

        JsonItem<Task> item = new JsonItem<Task>();
        item.setContentType("type");
        item.setKey("0");
        item.setState("A");
        item.setItem(c.getTask());

        String result = converter.toJSON(item);

        StringBuilder expected = new StringBuilder("{\"data\":{")
            .append("\"content-type\":\"application/json-task\",\"item\":{")
            .append("\"key\":\"0\",")
            .append("\"state\":\"A\",")
            .append("\"folder\":\"Root\\\\Tasks\",")
            .append("\"subject\":\"remember to call Gary\",")
            .append("\"tzid\":\"Europe/Rome\",")
            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20100925\",")
            .append("\"allDay\":true,")
            .append("\"categories\":\"\",")
            .append("\"body\":\"reminder\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":1,")
            .append("\"complete\":false,")
//            .append("\"status\":0,")
            .append("\"percentComplete\":\"20\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"\",")
            .append("\"totalWork\":\"\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals("Wrong JSON content converted from Task",
                     expected.toString(), result);

        JsonItem<Task> item2 = converter.fromJSON(result);
        String result2 = converter.toJSON(item2);

        StringBuilder expected2 = new StringBuilder("{\"data\":{")
            .append("\"content-type\":\"application/json-task\",\"item\":{")
            .append("\"key\":\"0\",")
            .append("\"state\":\"A\",")
            .append("\"folder\":\"Root\\\\Tasks\",")
            .append("\"subject\":\"remember to call Gary\",")
            .append("\"startDate\":\"\",")
            .append("\"dueDate\":\"20100925\",")
            .append("\"allDay\":true,")
            .append("\"categories\":\"\",")
            .append("\"body\":\"reminder\",")
            .append("\"sensitivity\":\"PUBLIC\",")
            .append("\"importance\":1,")
            .append("\"complete\":false,")
//            .append("\"status\":0,")
            .append("\"percentComplete\":\"20\",")
            .append("\"reminder\":false,")
            .append("\"dateCompleted\":\"\",")
            .append("\"actualWork\":\"0\",")
            .append("\"totalWork\":\"0\",")
            .append("\"billingInformation\":\"\",")
            .append("\"mileage\":0")
//            .append("\"companies\":\"\"}}}");
            .append("}}}");

        assertEquals("Wrong JSON object converted from JSONItem",
                     expected2.toString(), result2);

    }

    /**
     * Bug 8543 - [Bluetie] - Server to Outlook - backslash in task title and
     * description is removed
     *
     * When syncing from Server to Outlook a task with a backslash in the title
     * and/or the description is removed.
     *
     * @throws Exception
     */
    public void test_bug8543() throws Exception
    {
        String expected = "{\"data\":\n" +
            "{\"content-type\": \"application/json-vcal\", \n" +
            "\"item\": {\"key\":\"415\", \"state\":\"N\", \n" +
            "\"vcal\":\"BEGIN:VCALENDAR\\r\\nVERSION:1.0\\r\\nBEGIN:VTODO\\r\\n" +
            "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\\r\\nX-FUNAMBOL-ALLDAY:1\\r\\n" +
            "SUMMARY:Back\\\\slash\\r\\n" +
            "DESCRIPTION;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Back\\\\slash\\r\\n" +
            "PRIORITY:3\\r\\nPERCENT-COMPLETE:0\\r\\nDUE:20100120\\r\\n" +
            "DTSTART:\\r\\nCATEGORIES:\\r\\nCLASS:PUBLIC\\r\\nRRULE:\\r\\n" +
            "AALARM:\\r\\nX-FUNAMBOL-ACTUALWORK:0\\r\\n" +
            "X-FUNAMBOL-BILLINGINFO:\\r\\nX-FUNAMBOL-COMPANIES:\\r\\n" +
            "X-FUNAMBOL-MILEAGE:\\r\\nX-FUNAMBOL-TEAMTASK:0\\r\\n" +
            "X-FUNAMBOL-TOTALWORK:0\\r\\nEND:VTODO\\r\\nEND:VCALENDAR\\r\\n\"}}}\";";

        JsonItem<String> jsonItem = converter.fromRFC(expected);

        ByteArrayInputStream buffer =
                    new ByteArrayInputStream(jsonItem.getItem().getBytes());

        XVCalendarParser parser = new XVCalendarParser(buffer);
        VCalendar vcalendar = (VCalendar) parser.XVCalendar();
        VCalendarConverter vcf = new VCalendarConverter(TimeZone.getDefault(), "UTF-8");
        Calendar c = vcf.vcalendar2calendar(vcalendar);
        
        String summary = c.getTask().getSummary().getPropertyValueAsString();
        assertEquals("Back\\slash", summary);

        String description = c.getTask().getDescription().getPropertyValueAsString();
        assertEquals("Back\\slash", description);
    }

    // --------------------------------------------------------- Private methods

}
