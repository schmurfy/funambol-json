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

import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import net.sf.json.test.JSONAssert;
import net.sf.json.JSONObject;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.Event;
import com.funambol.common.pim.calendar.ExceptionToRecurrenceRule;
import com.funambol.common.pim.calendar.Reminder;
import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.converter.TimeZoneHelper;

import com.funambol.json.domain.JsonItem;
import com.funambol.json.util.TestUtility;

/**
 * Test cases for AppointmentConverter class.
 *
 * @version $Id: AppointmentConverterTest.java 51920 2010-06-04 05:10:49Z gazzaniga $
 */
public class AppointmentConverterTest extends TestCase {

    AppointmentConverter converter = null;

    // --------------------------------------------------------------- Constants
    private static final String CHARSET = "UTF-8";

    // ------------------------------------------------------------ Constructors
    public AppointmentConverterTest(String testName) {
        super(testName);
    }

    // ------------------------------------------------------- Protected methods
    @Override
    protected void setUp() throws Exception {
        System.setProperty("file.encoding", CHARSET);
        // Sets noon, January 1st, 2009 as reference time, thus making the tests
        // time-independent
        TimeZoneHelper.setReferenceTime(1230811200000L);
        converter = new AppointmentConverter();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // Restores current time as reference time
        TimeZoneHelper.setReferenceTime(-1);
    }

    // -------------------------------------------------------------- Test cases
    public void testToJSONFromJSON() throws Exception{

        JsonItem<Event> item = new JsonItem<Event>();

        item.setKey("0");
        item.setState("A");
        item.setContentType("type");

        Event event = new Event();

        event.setFolder(new Property("12"));
        event.setDtStart(new Property("20081121T153000Z"));
        event.setDtEnd(new Property("20081121T163000Z"));
        event.setAllDay(false);

        Reminder reminder = new Reminder();
        reminder.setActive(true);
        reminder.setMinutes(15);
        // <ReminderSet>                  --> 1 = setActive(true);
        // <ReminderMinutesBeforeStart>   --> reminder.setMinutes(..)
        // <ReminderTime>                 --> reminder.setTime(..)
        event.setReminder(reminder);

        item.setItem(event);

        // verify
        Calendar calendar = new Calendar();
        calendar.setCalendarContent(event);
        String xml = TestUtility.calendar2sif(calendar);

        String jsonResult = converter.toJSON(item);

        JsonItem<Event> item2 = converter.fromJSON(jsonResult);
        String jsonResult2 = converter.toJSON(item2);

        JSONAssert.assertEquals(jsonResult2, jsonResult);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void testAppointmentSingle() throws Exception{
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<appointment>" +
                    "<Subject>Meeting with Maga developers</Subject>"+
                    "<Body/>"+
                    "<Location>Milan</Location>"+
                    "<Start>20040930T133000Z</Start>"+
                    "<End>20040930T140000Z</End>"+
                    "<ReminderMinutesBeforeStart>10</ReminderMinutesBeforeStart>"+
                    "<AllDay>0</AllDay>"+
                    "<Categories/>"+
                    "<Importance>1</Importance>"+
                    "<IsRecurring>0</IsRecurring>"+
                    "<RecurrenceType>1</RecurrenceType>"+
                    "<DayOfMonth>0</DayOfMonth>"+
                    "<DayOfWeekMask>16</DayOfWeekMask>"+
                    "<Instance>0</Instance>"+
                    "<Interval>1</Interval>"+
                    "<MonthOfYear>2</MonthOfYear>"+
                    "<NoEndDate>1</NoEndDate>"+
                    "<Occurrences>1</Occurrences>"+
                    "<PatternEndDate></PatternEndDate>"+
                    "<PatternStartDate>20040930T230000Z</PatternStartDate>"+
                    "</appointment >";

        Calendar c = TestUtility.sif2Calendar(xml);
        Event e = c.getEvent();
        e.setFolder(new Property("12"));

        JsonItem<Event> item = new JsonItem<Event>();
        item.setKey("0");
        item.setState("A");
        item.setContentType("type");

        item.setItem(e);

        String jsonResult = converter.toJSON(item);

        JsonItem<Event> item2 = converter.fromJSON(jsonResult);
        String jsonResult2 = converter.toJSON(item2);

        JSONAssert.assertEquals(jsonResult2, jsonResult);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void test_Recurring_Daily() throws Exception{
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<appointment>" +
                    "<SIFVersion>1.1</SIFVersion>" +
                    "<Subject>Daily</Subject>" +
                    "<Location>Ogni 2 giorni</Location>" +
                    "<Start>20081027T180000Z</Start>" +
                    "<End>20081027T190000Z</End>" +
                    "<MeetingStatus>0</MeetingStatus>" +
                    "<ReminderMinutesBeforeStart>15</ReminderMinutesBeforeStart>" +
                    "<ReminderOptions>4</ReminderOptions>" +
                    "<ReminderSet>1</ReminderSet>" +
                    "<Body/>" +
                    "<Sensitivity>0</Sensitivity>" +
                    "<BusyStatus>2</BusyStatus>" +
                    "<Categories/>" +
                    "<AllDayEvent>0</AllDayEvent>" +
                    "<IsRecurring>1</IsRecurring>" +
                    "<DayOfMonth>0</DayOfMonth>" +
                    "<DayOfWeekMask>0</DayOfWeekMask>" +
                    "<Instance>0</Instance>" +
                    "<Interval>2</Interval>" +
                    "<MonthOfYear>0</MonthOfYear>" +
                    "<NoEndDate>0</NoEndDate>" +
                    "<Occurrences>18</Occurrences>" +
                    "<PatternEndDate>20081130T120000</PatternEndDate>" +
                    "<PatternStartDate>20081027T120000</PatternStartDate>" +
                    "<RecurrenceType>0</RecurrenceType>" +
                    "<Exceptions/>" +
                    "<Timezone>" +
                    "<BasicOffset>-0600</BasicOffset>" +
                    "<DayLight>" +
                    "<DSTOffset>-0500</DSTOffset>" +
                    "<DSTStart>20080406T020000</DSTStart><DSTEnd>20081026T020000</DSTEnd><StandardName/><DSTName/>" +
                    "</DayLight>" +
                    "</Timezone>" +
                    "</appointment>" ;

        Calendar c = TestUtility.sif2Calendar(xml);
        Event e = c.getEvent();
        e.setFolder(new Property("12"));
        JsonItem<Event> item = new JsonItem<Event>();
        item.setItem(e);

        // Event to JSON
        String jsonResult = converter.toJSON(item);
        JSONObject jo = JSONObject.fromObject(jsonResult);

        // check the value
        JSONObject jsonData = jo.getJSONObject("data");
        JSONObject jsonItem = jsonData.getJSONObject("item");
        String tzid = jsonItem.optString("tzid");
        assertEquals("America/Mexico_City", tzid);

        // JSON to Event
        //

        // TMP
        // until the Json sets th tzid = U.I. timezome in the json object
        converter.setServerTimeZoneID(tzid);

        JsonItem<Event> eventRes = converter.fromJSON(jsonResult);
        Calendar calendar = new Calendar();
        calendar.setCalendarContent(eventRes.getItem());
        String resultXML = TestUtility.calendar2sif(calendar);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void test_JSON_2_SIF() throws Exception{
        
        StringBuilder jsonItem = new StringBuilder("{\"data\":{")
            .append("\"content-type\":\"application/json-appointment\",")
            .append("\"item\":{")
            .append("\"key\":\"10757538\",")
            .append("\"folder_id\":\"1832444\",")
            .append("\"startDate\":\"20081104T230000Z\",")
            .append("\"endDate\":\"20081105T000000Z\",")
            .append("\"subject\":\"every Tuesday\",")
            .append("\"reminder\":\"0\",")
            .append("\"reminderTime\":\"0\",")
            .append("\"sensitivity\":\"PRIVATE\",")
            .append("\"importance\":\"0\",")
            .append("\"SentReminder\":\"No\",")
            .append("\"inviteContacts\":\"No\",")
            .append("\"allDay\":\"false\",")
            .append("\"isRecurring\":\"true\",")
            .append("\"recurrenceType\":1,")
            .append("\"patternStartDate\":\"20081104T170000\",")
            .append("\"noEndDate\":\"false\",")
            .append("\"patternEndDate\":\"20081130T000000\",")
            .append("\"interval\":\"1\",")
            .append("\"dayOfWeekMask\":0,")
            .append("\"Timestamp\":\"20081021T084751\",")
            .append("\"customerID\":\"406940\",")
            .append("\"repeatAppointmentID\":\"0\",")
            .append("\"show_time_as\":\"0\",")
            .append("\"label\":\"0\",")
            .append("\"PublicAction\":\"None\",")
            .append("\"PublicStatus\":\"None\",")
            .append("\"PublicUpdateID\":\"0\"}}}");

        // until the Json sets th tzid = U.I. timezome in the json object
        converter.setServerTimeZoneID("America/Mexico_City");
        
        JsonItem<Event> eventRes = converter.fromJSON(jsonItem.toString());

        Calendar calendar = new Calendar();
        calendar.setCalendarContent(eventRes.getItem());

        String dtStart = calendar.getCalendarContent().getDtStart().getPropertyValueAsString();
        String dtEnd = calendar.getCalendarContent().getDtEnd().getPropertyValueAsString();
        String startDatePattern = calendar.getCalendarContent().getRecurrencePattern().getStartDatePattern();
        String endDatePattern = calendar.getCalendarContent().getRecurrencePattern().getEndDatePattern();

        assertEquals("20081104T230000Z", dtStart);
        assertEquals("20081105T000000Z", dtEnd);
        assertEquals("20081104T170000", startDatePattern);
        assertEquals("20081130T000000", endDatePattern);

        String resultXML = TestUtility.calendar2sif(calendar);
        StringBuilder expectedResult =
            new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<appointment>\n")
            .append("<SIFVersion>1.1</SIFVersion>")
            .append("<Timezone>")
            .append("<BasicOffset>-0600</BasicOffset>")
            .append("<DayLight><DSTOffset>-0500</DSTOffset><DSTStart>20080406T020000</DSTStart>")
            .append("<DSTEnd>20081026T020000</DSTEnd><DSTName>America/Mexico_City</DSTName>")
            .append("<StandardName>America/Mexico_City</StandardName></DayLight>")
            .append("<DayLight><DSTOffset>-0500</DSTOffset><DSTStart>20090405T020000</DSTStart>")
            .append("<DSTEnd>20091025T020000</DSTEnd><DSTName>America/Mexico_City</DSTName>")
            .append("<StandardName>America/Mexico_City</StandardName></DayLight>")
            .append("<DayLight><DSTOffset>-0500</DSTOffset><DSTStart>20100404T020000</DSTStart>")
            .append("<DSTEnd>20101031T020000</DSTEnd><DSTName>America/Mexico_City</DSTName>")
            .append("<StandardName>America/Mexico_City</StandardName></DayLight>")
            .append("</Timezone>")
            .append("<AllDayEvent>0</AllDayEvent><IsRecurring>1</IsRecurring>")
            .append("<Start>20081104T230000Z</Start><End>20081105T000000Z</End>")
            .append("<BusyStatus>0</BusyStatus><Importance>0</Importance>")
            .append("<Sensitivity>2</Sensitivity>")
            .append("<Subject>every Tuesday</Subject><ReminderSet>0</ReminderSet>")
            .append("<ReminderMinutesBeforeStart>0</ReminderMinutesBeforeStart>")
            .append("<ReminderOptions>0</ReminderOptions><ReminderInterval>0</ReminderInterval>")
            .append("<ReminderRepeatCount>0</ReminderRepeatCount><DayOfMonth>0</DayOfMonth>")
            .append("<DayOfWeekMask>0</DayOfWeekMask><Interval>1</Interval><Instance>0</Instance>")
            .append("<MonthOfYear>0</MonthOfYear><NoEndDate>0</NoEndDate>")
            .append("<PatternStartDate>20081104T170000</PatternStartDate>")
            .append("<PatternEndDate>20081130T000000</PatternEndDate>")
            .append("<RecurrenceType>1</RecurrenceType>")
            .append("<Exceptions>")
            .append("<ExcludeDate/>")
            .append("<IncludeDate/>")
            .append("</Exceptions>")
            .append("<Attendees/>")
            .append("</appointment>");

            //assertEquals(expectedResult.toString(), resultXML);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void testExceptions_SIF_2_JSON() throws Exception{
        StringBuilder xml =
            new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            .append("<appointment>")
            .append("<SIFVersion>1.1</SIFVersion>")
            .append("<Subject>Tree</Subject>")
            .append("<Start>20081103T150000Z</Start>")
            .append("<End>20081103T160000Z</End>")
            .append("<Location/>")
            .append("<ReminderSet>1</ReminderSet>")
            .append("<ReminderMinutesBeforeStart>15</ReminderMinutesBeforeStart>")
            .append("<ReminderOptions>4</ReminderOptions>")
            .append("<Sensitivity>2</Sensitivity>")
            .append("<AllDayEvent>0</AllDayEvent>")
            .append("<MeetingStatus>0</MeetingStatus>")
            .append("<Body/>")
            .append("<BusyStatus>2</BusyStatus>")
            .append("<Categories/>")
            .append("<IsRecurring>1</IsRecurring>")
            .append("<Interval>7</Interval>")
            .append("<Instance>0</Instance>")
            .append("<MonthOfYear>0</MonthOfYear>")
            .append("<NoEndDate>0</NoEndDate>")
            .append("<DayOfMonth>0</DayOfMonth>")
            .append("<DayOfWeekMask>0</DayOfWeekMask>")
            .append("<Occurrences>4</Occurrences>")
            .append("<PatternEndDate>20081130T090000</PatternEndDate>")
            .append("<PatternStartDate>20081103T090000</PatternStartDate>")
            .append("<RecurrenceType>0</RecurrenceType>")
            .append("<Exceptions>")
            .append("<ExcludeDate>20081110T150000Z</ExcludeDate>")
            .append("<ExcludeDate>20081117T150000Z</ExcludeDate>")
            .append("</Exceptions>")
            .append("<Timezone>")
            .append("<BasicOffset>-0600</BasicOffset>")
            .append("<DayLight>")
            .append("<DSTOffset>-0500</DSTOffset><DSTStart>20080406T020000</DSTStart>")
            .append("<DSTEnd>20081026T020000</DSTEnd><StandardName/><DSTName/>")
            .append("</DayLight>")
            .append("</Timezone>")
            .append("</appointment>");

        Calendar c = TestUtility.sif2Calendar(xml.toString());

        String dtStart = c.getCalendarContent().getDtStart().getPropertyValueAsString();
        String dtEnd = c.getCalendarContent().getDtEnd().getPropertyValueAsString();
        String startDatePattern = c.getCalendarContent().getRecurrencePattern().getStartDatePattern();
        String endDatePattern = c.getCalendarContent().getRecurrencePattern().getEndDatePattern();

        assertEquals("20081103T150000Z", dtStart);
        assertEquals("20081103T160000Z", dtEnd);
        assertEquals("20081103T090000", startDatePattern);
        assertEquals("20081130T090000", endDatePattern);

        Event e = c.getEvent();
        e.setFolder(new Property("12"));
        JsonItem<Event> item = new JsonItem<Event>();
        item.setItem(e);

        String jsonResult = converter.toJSON(item);

        StringBuilder jsonExpected = new StringBuilder("{\"data\":{")
            .append("\"content-type\":\"application/json-appointment\",")
            .append("\"item\":{")
            .append("\"folder\":\"12\",")
            .append("\"tzid\":\"America/Mexico_City\",")
            .append("\"startDate\":\"20081103T150000Z\",")
            .append("\"endDate\":\"20081103T160000Z\",")
            .append("\"subject\":\"Tree\",")
            .append("\"body\":\"\",")
            .append("\"location\":\"\",")
            .append("\"allDay\":false,")
            .append("\"categories\":\"\",")
            .append("\"sensitivity\":\"PRIVATE\",")
            .append("\"busyStatus\":2,")
//            .append("\"importance\":0,")
            .append("\"reminder\":1,")
            .append("\"reminderTime\":15,")
            .append("\"isRecurring\":true,")
            .append("\"recurrenceType\":0,")
            .append("\"dayOfMonth\":0,")
            .append("\"dayOfWeekMask\":0,")
            .append("\"instance\":0,")
            .append("\"interval\":7,")
            .append("\"monthOfYear\":0,")
            .append("\"noEndDate\":false,")
            .append("\"occurrences\":4,")
            .append("\"patternStartDate\":\"20081103T090000\",")
            .append("\"patternEndDate\":\"20081130T090000\",")
            .append("\"exceptionsIncluded\":[],")
            .append("\"exceptionsExcluded\":[\"20081110T090000\",\"20081117T090000\"]}}}");

        assertEquals(jsonExpected.toString(), jsonResult);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void testExceptions_SIF_2_JSON_02() throws Exception{
        String xml =
                "<appointment>" +
                "<SIFVersion>1.1</SIFVersion>" +
                "<AllDayEvent>0</AllDayEvent>" +
                "<Body/>" +
                "<BusyStatus>1</BusyStatus>" +
                "<Categories/>" +
                "<End>20081108T003000Z</End>" +
                "<IsRecurring>1</IsRecurring>" +
                "<Location/>" +
                "<MeetingStatus>0</MeetingStatus>" +

                "<ReminderSet>1</ReminderSet>" +
                "<ReminderMinutesBeforeStart>15</ReminderMinutesBeforeStart>" +
                "<ReminderOptions>4</ReminderOptions>" +

                "<Sensitivity>0</Sensitivity>" +
                "<Start>20081107T233000Z</Start>" +
                "<Subject>Fera</Subject>" +
                "<DayOfMonth>0</DayOfMonth>" +
                "<DayOfWeekMask>32</DayOfWeekMask>" +
                "<Instance>0</Instance>" +
                "<Interval>1</Interval>" +
                "<MonthOfYear>0</MonthOfYear>" +
                "<NoEndDate>0</NoEndDate>" +
                "<Occurrences>4</Occurrences>" +
                "<PatternEndDate>20081130T173000</PatternEndDate>" +
                "<PatternStartDate>20081107T173000</PatternStartDate>" +
                "<RecurrenceType>1</RecurrenceType>" +
                "<Exceptions>" +
                "<ExcludeDate>20081121T233000Z</ExcludeDate>" +
                "</Exceptions>" +
                "<Timezone>" +
                "<BasicOffset>-0600</BasicOffset>" +
                "<DayLight>" +
                "<DSTOffset>-0500</DSTOffset><DSTStart>20080406T020000</DSTStart><DSTEnd>20081026T020000</DSTEnd><StandardName/><DSTName/></DayLight>" +
                "</Timezone>" +
                "</appointment>" ;

        Calendar c = TestUtility.sif2Calendar(xml);

        String dtStart = c.getCalendarContent().getDtStart().getPropertyValueAsString();
        String dtEnd = c.getCalendarContent().getDtEnd().getPropertyValueAsString();
        String startDatePattern = c.getCalendarContent().getRecurrencePattern().getStartDatePattern();
        String endDatePattern = c.getCalendarContent().getRecurrencePattern().getEndDatePattern();

        assertEquals("20081107T233000Z", dtStart);
        assertEquals("20081108T003000Z", dtEnd);
        assertEquals("20081107T173000", startDatePattern);
        assertEquals("20081130T173000", endDatePattern);

        Event e = c.getEvent();
        e.setFolder(new Property("12"));
        JsonItem<Event> item = new JsonItem<Event>();
        item.setItem(e);

        // Event to JSON
        String jsonResult = converter.toJSON(item);

        String jsonExpected = "{\"data\":{\"content-type\":\"application/json-appointment\",\"item\":{" +
                "\"folder\":\"12\"," +
                "\"tzid\":\"America/Mexico_City\"," +
                "\"startDate\":\"20081107T233000Z\"," +
                "\"endDate\":\"20081108T003000Z\"," +
                "\"subject\":\"Fera\"," +
                "\"body\":\"\"," +
                "\"location\":\"\"," +
                "\"allDay\":false," +
                "\"categories\":\"\"," +
                "\"sensitivity\":\"PUBLIC\"," +
                "\"busyStatus\":1," +
//                "\"importance\":0," +
                "\"reminder\":1," +
                "\"reminderTime\":15," +
                "\"isRecurring\":true," +
                "\"recurrenceType\":1," +
                "\"dayOfMonth\":0," +
                "\"dayOfWeekMask\":32," +
                "\"instance\":0," +
                "\"interval\":1," +
                "\"monthOfYear\":0," +
                "\"noEndDate\":false," +
                "\"occurrences\":4," +
                "\"patternStartDate\":\"20081107T173000\"," +
                "\"patternEndDate\":\"20081130T173000\"," +
                "\"exceptionsIncluded\":[]," +
                "\"exceptionsExcluded\":[\"20081121T173000\"]}}}";

        assertEquals(jsonResult, jsonExpected);
    }

    /**
     *
     * @throws Exception
     */
    public void test_Anniversary_Allday_without_end_VCAL_2_JSON() throws Exception {

        // before the synclet
        // BEGIN:VCALENDAR
        // VERSION:1.0
        // BEGIN:VEVENT
        // UID:I8HRX88l4EEhX76ibFFgh2
        // SUMMARY:Christmas Day
        // DTSTART:20081225T000000
        // DTEND:20081225T000000
        // X-EPOCAGENDAENTRYTYPE:ANNIVERSARY
        // CLASS:PRIVATE
        // SEQUENCE:0
        // X-METHOD:NONE
        // RRULE:YM1 12 #0
        // LAST-MODIFIED:20080922T214752Z
        // PRIORITY:2
        // X-SYMBIAN-LUID:199
        // END:VEVENT
        // END:VCALENDAR
        // after the synclet
        String vcalTest =
            "BEGIN:VCALENDAR\n"+
            "VERSION:1.0\n"+
            "BEGIN:VEVENT\n"+
            "UID:I8HRX88l4EEhX76ibFFgh2\n"+
            "SUMMARY:Christmas Day\n"+
            "DTSTART:20081225T000000\n"+
            "DTEND:20081225T235900\n"+    // modified by synclet
            "X-EPOCAGENDAENTRYTYPE:ANNIVERSARY\n"+
            "CLASS:PRIVATE\n"+
            "SEQUENCE:0\n"+
            "X-METHOD:NONE\n"+
            "RRULE:YM1 12 #0\n"+
            "LAST-MODIFIED:20080922T214752Z\n"+
            "PRIORITY:2\n"+
            "X-SYMBIAN-LUID:199\n"+
            "DESCRIPTION:\n"+   // modified by synclet
            "ORGANIZER:\n"+     // modified by synclet
            "AALARM:;;;\n"+     // modified by synclet
            "LOCATION:\n"+      // modified by synclet
            "END:VEVENT\n"+
            "END:VCALENDAR\n";


        String tzid = "America/Mexico_City";

        Calendar c = TestUtility.webCalendar2Calendar(vcalTest,
                                          "text/x-vcalendar",
                                          TimeZone.getTimeZone(tzid),
                                          CHARSET);

        String dtStart = c.getCalendarContent().getDtStart().getPropertyValueAsString();
        String dtEnd = c.getCalendarContent().getDtEnd().getPropertyValueAsString();
        String startDatePattern = c.getCalendarContent().getRecurrencePattern().getStartDatePattern();

        // Foundation bug (or just inconsistency). It should be:
        //   assertEquals("20081225T000000", dtStart);
        //   assertEquals("20081225T235900", dtEnd);
        //   assertEquals("20081225T000000", startDatePattern);
        // but it is:
        assertEquals("2008-12-25", dtStart);
        assertEquals("2008-12-25", dtEnd);
        assertEquals("2008-12-25", startDatePattern);

        Event e = c.getEvent();
        e.setFolder(new Property("12"));
        JsonItem<Event> item = new JsonItem<Event>();
        item.setItem(e);

        // Event to JSON
        converter.setServerTimeZoneID(tzid);
        String jsonResult = converter.toJSON(item);

        String jsonExpected = "{\"data\":{\"content-type\":\"application/json-appointment\"," +
                "\"item\":{\"folder\":\"12\"," +
                "\"tzid\":\"America/Mexico_City\"," +

                // with patch
                "\"startDate\":\"20081225\"," +
                "\"endDate\":\"20081225\"," +
                // without patch
                //"\"startDate\":\"2008-12-25\"," +
                //"\"endDate\":\"2008-12-25\"," +

                "\"subject\":\"Christmas Day\",\"body\":\"\"," +
                "\"location\":\"\"," +
                "\"allDay\":true," +
//                "\"categories\":\"\"," +
                "\"sensitivity\":\"PRIVATE\"," +
//                "\"busyStatus\":0,\"importance\":1,\"reminder\":0," +
                "\"importance\":1," +
                "\"organizer\":\"\"," +
                "\"reminder\":0," +

                "\"isRecurring\":true," +
                "\"recurrenceType\":5," +
                "\"dayOfMonth\":25," +
                "\"dayOfWeekMask\":0," +
                "\"instance\":0," +
                "\"interval\":1," +
                "\"monthOfYear\":12," +
                "\"noEndDate\":true," +
                "\"occurrences\":-1," +
                "\"patternStartDate\":\"20081225T000000\"}}}";
               // without patch "\"patternStartDate\":\"2008-12-25\"}}}";

        assertEquals(jsonExpected, jsonResult);

        JsonItem<Event> item1  = converter.fromJSON(jsonResult);
        Calendar calendar = new Calendar();
        calendar.setCalendarContent(item1.getItem());
        String vcal = TestUtility.calendar2webCalendar(calendar,
                                                        "text/x-vcalendar",
                                                        TimeZone.getTimeZone(tzid),
                                                        vcalTest);


        String tmp = "";
        int a = 0;

        a = vcal.indexOf("DTSTART");
        tmp = vcal.substring(a,vcal.length());
        a = tmp.indexOf("\n");
        String startdate = tmp.substring(0,a-1);

        a = vcal.indexOf("DTEND");  // this value can be modified by the synclet
        tmp = vcal.substring(a,vcal.length());
        a = tmp.indexOf("\n");
        String enddate = tmp.substring(0,a-1);

        a = vcal.indexOf("RRULE");
        tmp = vcal.substring(a,vcal.length());
        a = tmp.indexOf("\n");
        String rrule = tmp.substring(0,a-1);

        // original after the synclet
        //           "DTSTART:20081225T000000\n"+
        // with patch
        assertEquals("DTSTART:20081225T000000", startdate);
        // without patch
        //assertEquals("DTSTART:2008-12-25", startdate);

        // original after the synclet
        //           "DTEND:20081225T235900\n"+
        assertEquals("DTEND:20081225T235900", enddate);

        // original after the synclet
        //           "RRULE:YM1 12 #0\n"+
        assertEquals("RRULE:YM1 12 #0", rrule);

    }

    /**
     *
     * @throws Exception
     */
    public void test_Anniversary_VCAL_2_JSON() throws Exception {

            // before synclet
          /*
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VEVENT
            SUMMARY:Anniversary
            DTSTART:20070331
            DTEND:20070331
            END:VEVENT
            END:VCALENDAR
          */
          // after synclet
           String vcalTest =
            "BEGIN:VCALENDAR\n"+
            "VERSION:1.0\n"+
            "BEGIN:VEVENT\n"+
            "SUMMARY:Anniversary\n"+
            "DTSTART:20070331\n"+
            "DTEND:20070331\n"+
            "AALARM:;;;\n"+
            "END:VEVENT\n"+
            "END:VCALENDAR\n";

        String tzid = "America/Mexico_City";

        Calendar c = TestUtility.webCalendar2Calendar(vcalTest,
                                                      "text/x-vcalendar",
                                                      TimeZone.getTimeZone(tzid),
                                                      CHARSET);

        String dtStart = c.getCalendarContent().getDtStart().getPropertyValueAsString();
        String dtEnd = c.getCalendarContent().getDtEnd().getPropertyValueAsString();

        // Foundation bug (or just inconsistency). It should be:
        //   assertEquals("20070331T000000", dtStart);
        //   assertEquals("20070331T235900", dtEnd);
        // but it is:
        assertEquals("2007-03-31", dtStart);
        assertEquals("2007-03-31", dtEnd);

        Event e = c.getEvent();
        e.setFolder(new Property("12"));
        JsonItem<Event> item = new JsonItem<Event>();
        item.setItem(e);

        // Event to JSON
        converter.setServerTimeZoneID(tzid);
        String jsonResult = converter.toJSON(item);

        String jsonExpected = "{\"data\":{\"content-type\":\"application/json-appointment\",\"item\":{" +
                "\"folder\":\"12\"," +
                "\"tzid\":\"America/Mexico_City\"," +

                // with patch
                "\"startDate\":\"20070331\"," +
                "\"endDate\":\"20070331\"," +

                "\"subject\":\"Anniversary\"," +
//                "\"body\":\"\"," +
//                "\"location\":\"\"," +
                "\"allDay\":true," +
//                "\"categories\":\"\"," +
                "\"sensitivity\":\"PUBLIC\"," +
//                "\"busyStatus\":0," +
//                "\"importance\":0," +
                "\"reminder\":0,\"isRecurring\":false}}}";

        assertEquals(jsonExpected, jsonResult);

        JsonItem<Event> item1  = converter.fromJSON(jsonResult);
        Calendar calendar = new Calendar();
        calendar.setCalendarContent(item1.getItem());


        dtStart = calendar.getCalendarContent().getDtStart().getPropertyValueAsString();
        dtEnd = calendar.getCalendarContent().getDtEnd().getPropertyValueAsString();

        assertEquals("20070331T000000", dtStart);
        assertEquals("20070331T235900", dtEnd);

        String vcal = TestUtility.calendar2webCalendar(calendar,
                                                        "text/x-vcalendar",
                                                        TimeZone.getTimeZone(tzid),
                                                        vcalTest);

        String tmp = "";
        int a = 0;

        a = vcal.indexOf("DTSTART");
        tmp = vcal.substring(a,vcal.length());
        a = tmp.indexOf("\n");
        String startdate = tmp.substring(0,a-1);

        a = vcal.indexOf("DTEND");  // this value can be modified by the synclet
        tmp = vcal.substring(a,vcal.length());
        a = tmp.indexOf("\n");
        String enddate = tmp.substring(0,a-1);

        // original after the synclet
        //           "DTSTART:20070331\n"+
        // with patch
        assertEquals("DTSTART:20070331T000000", startdate);

        // original after the synclet
        //           "DTEND:20070331\n"+
        assertEquals("DTEND:20070331T235900", enddate);
    }

    /**
     *
     * @throws Exception
     */
    public void test_Memo_VCAL_2_JSON() throws Exception {

            // before synclet
            /*
            BEGIN:VCALENDAR
            VERSION:1.0
            BEGIN:VEVENT
            UID:8A78y0CU4EEYe53MHB39Q3
            SUMMARY:Memo 2 days
            DTSTART:20070601T000000
            DTEND:20070603T000000
            X-EPOCAGENDAENTRYTYPE:EVENT
            CLASS:PRIVATE
            SEQUENCE:0
            X-METHOD:NONE
            LAST-MODIFIED:20070525T080443Z
            PRIORITY:2
            X-SYMBIAN-LUID:642
            END:VEVENT
            END:VCALENDAR
            */
          // after synclet
           String vcalTest =
            "BEGIN:VCALENDAR\n"+
            "VERSION:1.0\n"+
            "BEGIN:VEVENT\n"+
            "UID:8A78y0CU4EEYe53MHB39Q3\n"+
            "SUMMARY:Memo 2 days\n"+
            "DTSTART:20070601T000000\n"+
            "DTEND:20070602T235900\n"+
            "X-EPOCAGENDAENTRYTYPE:EVENT\n"+
            "CLASS:PRIVATE\n"+
            "SEQUENCE:0\n"+
            "X-METHOD:NONE\n"+
            "LAST-MODIFIED:20070525T080443Z\n"+
            "PRIORITY:2\n"+
            "X-SYMBIAN-LUID:642\n"+
            "RRULE:\n"+
            "LOCATION:\n"+
            "ORGANIZER:\n"+
            "AALARM:;;;\n"+
            "DESCRIPTION:\n"+
            "END:VEVENT\n"+
            "END:VCALENDAR\n";

        String tzid = "America/Mexico_City";

        Calendar c = TestUtility.webCalendar2Calendar(vcalTest,
                                                      "text/x-vcalendar",
                                                      TimeZone.getTimeZone(tzid),
                                                      CHARSET);

        String dtStart = c.getCalendarContent().getDtStart().getPropertyValueAsString();
        String dtEnd = c.getCalendarContent().getDtEnd().getPropertyValueAsString();

        // Foundation bug (or just inconsistency). It should be:
        //   assertEquals("20070601T000000", dtStart);
        //   assertEquals("20070602T235900", dtEnd);
        // but it is:
        assertEquals("2007-06-01", dtStart);
        assertEquals("2007-06-02", dtEnd);


        Event e = c.getEvent();
        e.setFolder(new Property("12"));
        JsonItem<Event> item = new JsonItem<Event>();
        item.setItem(e);

        // Event to JSON
        converter.setServerTimeZoneID(tzid);
        String jsonResult = converter.toJSON(item);

        String jsonExpected = "{\"data\":{\"content-type\":\"application/json-appointment\",\"item\":{" +
                "\"folder\":\"12\"," +
                "\"tzid\":\"America/Mexico_City\"," +

                // note: allday but for 2 days
                "\"startDate\":\"20070601\"," +
                "\"endDate\":\"20070602\"," +

                "\"subject\":\"Memo 2 days\"," +
                "\"body\":\"\"," +
                "\"location\":\"\"," +
                "\"allDay\":true," +
//                "\"categories\":\"\"," +
                "\"sensitivity\":\"PRIVATE\"," +
//                "\"busyStatus\":0," +
                "\"importance\":1," +
                "\"organizer\":\"\"," +
                "\"reminder\":0,\"isRecurring\":false}}}";

        assertEquals(jsonExpected, jsonResult);

        JsonItem<Event> item1  = converter.fromJSON(jsonResult);
        Calendar calendar = new Calendar();
        calendar.setCalendarContent(item1.getItem());

        dtStart = calendar.getCalendarContent().getDtStart().getPropertyValueAsString();
        dtEnd = calendar.getCalendarContent().getDtEnd().getPropertyValueAsString();

        assertEquals("20070601T000000", dtStart);
        assertEquals("20070602T235900", dtEnd);

        String vcal = TestUtility.calendar2webCalendar(calendar,
                                                        "text/x-vcalendar",
                                                        TimeZone.getTimeZone(tzid),
                                                        vcalTest);

        String tmp = "";
        int a = 0;

        a = vcal.indexOf("DTSTART");
        tmp = vcal.substring(a,vcal.length());
        a = tmp.indexOf("\n");
        String startdate = tmp.substring(0,a-1);
        //System.out.println(".................. " + startdate);

        a = vcal.indexOf("DTEND");  // this value can be modified by the synclet
        tmp = vcal.substring(a,vcal.length());
        a = tmp.indexOf("\n");
        String enddate = tmp.substring(0,a-1);
        //System.out.println(".................. " + enddate);


        // original after the synclet
        //           "DTSTART:20070601T000000\n"+
        // with patch
        assertEquals("DTSTART:20070601T000000", startdate);

        // original after the synclet
        //           "DTEND:20070602T235900\n"+
        assertEquals("DTEND:20070602T235900", enddate);

    }


    /**
     *
     * @throws Exception
     */
    public void test_Allday_with_end_PatternDate_VCAL_2_JSON() throws Exception {


        String vcalTest =
            "BEGIN:VCALENDAR\n"+
            "VERSION:1.0\n"+
            "TZ:+01\n"+
            "DAYLIGHT:TRUE;+02;20090329T010000Z;20091025T010000Z;;\n"+
            "DAYLIGHT:TRUE;+02;20100328T010000Z;20101031T010000Z;;\n"+
            "DAYLIGHT:TRUE;+02;20110327T010000Z;20111030T010000Z;;\n"+
            "DAYLIGHT:TRUE;+02;20120325T010000Z;20121028T010000Z;;\n"+
            "BEGIN:VEVENT\n"+
            "UID:n6Ad8GDJ4EE0i56ur66PG3\n"+
            "SUMMARY:All day\n"+
            //
            // our system creates an all day --> see patternStartDate
            // there is a problem on the patternEndDate that is not in YYYY-MM-DD
            "DTSTART:20090125T230000Z\n"+
            "DTEND:20090126T225900Z\n"+

            "X-EPOCAGENDAENTRYTYPE:APPOINTMENT\n"+
            "CLASS:PRIVATE\n"+
            "SEQUENCE:0\n"+
            "X-METHOD:NONE\n"+
            //
            // recurring with end
            "RRULE:YM1 1 20120127T000000\n"+

            "LAST-MODIFIED:20090126T103526Z\n"+
            "PRIORITY:2\n"+
            "X-SYMBIAN-LUID:123\n"+
            "END:VEVENT\n"+
            "END:VCALENDAR\n";


        String tzid = "America/Mexico_City";

        Calendar c = TestUtility.webCalendar2Calendar(vcalTest,
                                          "text/x-vcalendar",
                                          TimeZone.getTimeZone(tzid),
                                          CHARSET);

        String dtStart = c.getCalendarContent().getDtStart().getPropertyValueAsString();
        String dtEnd = c.getCalendarContent().getDtEnd().getPropertyValueAsString();
        String startDatePattern = c.getCalendarContent().getRecurrencePattern().getStartDatePattern();
        String endDatePattern = c.getCalendarContent().getRecurrencePattern().getEndDatePattern();

        // Foundation bug (or just inconsistency). It should be:
        // assertEquals("20090125T230000Z", dtStart);
        // assertEquals("20090126T225900Z", dtEnd);
        // assertEquals("20090125T170000", startDatePattern);
        // assertEquals("20120127T000000", endDatePattern);
        // but it is:
        assertEquals("2009-01-26", dtStart);
        assertEquals("2009-01-26", dtEnd);
        assertEquals("2009-01-26", startDatePattern);
        assertEquals("20120126T230000Z", endDatePattern);

        Event e = c.getEvent();
        e.setFolder(new Property("12"));
        JsonItem<Event> item = new JsonItem<Event>();
        item.setItem(e);

        // Event to JSON
        converter.setServerTimeZoneID(tzid);
        String jsonResult = converter.toJSON(item);

        String jsonExpected = "{\"data\":{\"content-type\":\"application/json-appointment\",\"item\":{" +
                "\"folder\":\"12\"," +
                "\"tzid\":\"Europe/Berlin\"," +

                // with patch
                "\"startDate\":\"20090126\"," +
                "\"endDate\":\"20090126\"," +

                // without patch
                //"\"startDate\":\"2009-01-26\"," +
                //"\"endDate\":\"2009-01-26\"," +

                "\"subject\":\"All day\"," +
//                "\"body\":\"\"," +
//                "\"location\":\"\"," +
                "\"allDay\":true," +

//                "\"categories\":\"\"," +
                "\"sensitivity\":\"PRIVATE\"," +
//                "\"busyStatus\":0," +
                "\"importance\":1,\"reminder\":0," +
                "\"isRecurring\":true," +
                "\"recurrenceType\":5," +
                "\"dayOfMonth\":26," +
                "\"dayOfWeekMask\":0," +
                "\"instance\":0," +
                "\"interval\":1," +
                "\"monthOfYear\":1," +
                "\"noEndDate\":false," +
                "\"occurrences\":-1," +

                // with patch
                "\"patternStartDate\":\"20090126T000000\"," +
                "\"patternEndDate\":\"20120127T000000\"}}}";

                // without patch
                //"\"patternStartDate\":\"2009-01-26\"," +
                //"\"patternEndDate\":\"2012-01-26\"}}}";

        //System.out.println("................."+jsonExpected);
        //System.out.println("................."+jsonResult);

        assertEquals(jsonExpected, jsonResult);

        JsonItem<Event> item1  = converter.fromJSON(jsonResult);
        Calendar calendar = new Calendar();
        calendar.setCalendarContent(item1.getItem());

        dtStart = calendar.getCalendarContent().getDtStart().getPropertyValueAsString();
        dtEnd = calendar.getCalendarContent().getDtEnd().getPropertyValueAsString();
        startDatePattern = calendar.getCalendarContent().getRecurrencePattern().getStartDatePattern();
        endDatePattern = calendar.getCalendarContent().getRecurrencePattern().getEndDatePattern();

        assertEquals("20090126T000000", dtStart);
        assertEquals("20090126T235900", dtEnd);
        assertEquals("20090126T000000", startDatePattern);
        assertEquals("20120127T000000", endDatePattern);

        String vcard = TestUtility.calendar2webCalendar(calendar,
                                                        "text/x-vcalendar",
                                                        TimeZone.getTimeZone(tzid),
                                                        vcalTest);


        String tmp = "";
        int a = 0;

        //System.out.println(".................. Allday_with_end_PatternDate_VCAL_2_JSON");
        a = vcard.indexOf("DTSTART");
        tmp = vcard.substring(a,vcard.length());
        a = tmp.indexOf("\n");
        String startdate = tmp.substring(0,a-1);
        //System.out.println(".................. " + startdate);

        a = vcard.indexOf("DTEND");  // this value can be modified by the synclet
        tmp = vcard.substring(a,vcard.length());
        a = tmp.indexOf("\n");
        String enddate = tmp.substring(0,a-1);
        //System.out.println(".................. " + enddate);

        a = vcard.indexOf("RRULE");
        tmp = vcard.substring(a,vcard.length());
        a = tmp.indexOf("\n");
        String patternEndDate = tmp.substring(0,a-1);
        //System.out.println(".................. " + patternEndDate);


        // original after the synclet
        //           "DTSTART:20090125T230000Z\n"+
        // with patch
        assertEquals("DTSTART:20090126T000000", startdate);
        // without patch
        //assertEquals("DTSTART:2009-01-26", startdate);

        // original after the synclet
        //           "DTEND:20090126T225900Z\n"+
        assertEquals("DTEND:20090126T235900", enddate);

        // original after the synclet
        //           "RRULE:YM1 1 20120127T000000\n"+
        // from convertion
        //            RRULE:YM1 1 20120126T230000
        assertEquals("RRULE:YM1 1 20120127T000000", patternEndDate);


    }

    /**
     *
     * @throws Exception
     */
    public void test_Recurring_VCAL_2_JSON() throws Exception {


        // before the synclet
        //
        // after the synclet
        String vcalTest =
            "BEGIN:VCALENDAR\n"+
            "VERSION:1.0\n"+
            "TZ:+01\n"+
            "DAYLIGHT:TRUE;+02;20090329T010000Z;20091025T010000Z;;\n"+
            "BEGIN:VEVENT\n"+
            "UID:w3AY4EAf4EGJX52U697YS3\n"+
            "SUMMARY:Prova n78\n"+
            "DTSTART:20090202T150000Z\n"+
            "DTEND:20090202T153000Z\n"+
            "X-EPOCAGENDAENTRYTYPE:APPOINTMENT\n"+
            "CLASS:PRIVATE\n"+
            "LOCATION:Ja\n"+
            "SEQUENCE:0\n"+
            "X-METHOD:NONE\n"+
            "RRULE:W1 MO 20090216T160000\n"+
            "LAST-MODIFIED:20090124T152326Z\n"+
            "PRIORITY:2\n"+
            "X-SYMBIAN-LUID:962\n"+
            "END:VEVENT\n"+
            "END:VCALENDAR\n";


        String tzid = "America/Mexico_City";

        Calendar c = TestUtility.webCalendar2Calendar(vcalTest,
                                          "text/x-vcalendar",
                                          TimeZone.getTimeZone(tzid),
                                          CHARSET);

        Event e = c.getEvent();
        e.setFolder(new Property("12"));
        JsonItem<Event> item = new JsonItem<Event>();
        item.setItem(e);

        // Event to JSON
        converter.setServerTimeZoneID(tzid);
        String jsonResult = converter.toJSON(item);

        //System.out.println("................."+jsonResult);
        String jsonExpected =
                "{\"data\":{\"content-type\":\"application/json-appointment\",\"item\":{" +
                "\"folder\":\"12\"," +
                "\"tzid\":\"Europe/Berlin\"," +
                "\"startDate\":\"20090202T150000Z\"," +
                "\"endDate\":\"20090202T153000Z\"," +
                "\"subject\":\"Prova n78\"," +
                
                "\"location\":\"Ja\"," +
//                "\"body\":\"\"," +
                "\"allDay\":false," +

                "\"sensitivity\":\"PRIVATE\"," +
//                "\"categories\":\"\"," +
//                "\"busyStatus\":0," +
                "\"importance\":1," +
                "\"reminder\":0," +
                "\"isRecurring\":true," +
                "\"recurrenceType\":1," +
                "\"dayOfMonth\":0," +
                "\"dayOfWeekMask\":2," +
                "\"instance\":0," +
                "\"interval\":1," +
                "\"monthOfYear\":0," +
                "\"noEndDate\":false," +
                "\"occurrences\":-1," +
                "\"patternStartDate\":\"20090202T150000Z\"," +
                "\"patternEndDate\":\"20090216T150000Z\"}}}";

        assertEquals(jsonExpected, jsonResult);

    }

    /**
     *
     * the item has been sent by the outllok 7.0.7
     * the user select the end date (he didn't select the number of occurrences)
     */
    public void test_Allday_with_end_PatternDate_SIF_2_JSON() throws Exception{
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                "<appointment>"+
                "<SIFVersion>1.1</SIFVersion>"+
                "<Folder>DEFAULT_FOLDER</Folder>"+

                "<Subject>ogni lune, mercole, venere</Subject>"+
                "<Start>2009-02-02</Start>"+
                "<End>2009-02-02</End>"+

                "<AllDayEvent>1</AllDayEvent>"+

                "<BillingInformation/>"+
                "<Body/>"+
                "<BusyStatus>0</BusyStatus>"+
                "<Categories/>"+
                "<Companies/>"+
                "<MeetingStatus>0</MeetingStatus>"+
                "<Mileage/>"+
                "<NoAging>0</NoAging>"+
                "<ReminderMinutesBeforeStart>1080</ReminderMinutesBeforeStart>"+
                "<ReminderSet>0</ReminderSet>"+
                "<ReminderSoundFile/>"+
                "<ReplyTime/>"+
                "<Sensitivity>0</Sensitivity>"+

                "<IsRecurring>1</IsRecurring>"+
                "<RecurrenceType>1</RecurrenceType>"+
                "<Importance>1</Importance>"+
                "<Location>ma dove vuoi tu</Location>"+
                "<DayOfMonth>0</DayOfMonth>"+
                "<DayOfWeekMask>26</DayOfWeekMask>"+
                "<Instance>0</Instance>"+
                "<Interval>1</Interval>"+
                "<MonthOfYear>0</MonthOfYear>"+
                "<NoEndDate>0</NoEndDate>"+
                "<Occurrences>12</Occurrences>"+
                "<PatternEndDate>20090227T000000</PatternEndDate>"+
                "<PatternStartDate>20090202T000000</PatternStartDate>"+
                "<Exceptions/>"+

                "<Timezone>"+
                "<BasicOffset>+0100</BasicOffset>"+
                "<DayLight>"+
                "<DSTOffset>+0200</DSTOffset><DSTStart>20090329T020000</DSTStart>" +
                "<DSTEnd>20091025T030000</DSTEnd><StandardName/><DSTName/></DayLight>"+
                "</Timezone>"+

                "</appointment>" ;


        Calendar c = TestUtility.sif2Calendar(xml);

        String dtStart = c.getCalendarContent().getDtStart().getPropertyValueAsString();
        String dtEnd = c.getCalendarContent().getDtEnd().getPropertyValueAsString();
        String startDatePattern = c.getCalendarContent().getRecurrencePattern().getStartDatePattern();
        String endDatePattern = c.getCalendarContent().getRecurrencePattern().getEndDatePattern();

        // Foundation bug (or just inconsistency). It should be:
        // assertEquals("20090202T000000", dtStart);
        // assertEquals("20090202T235900", dtEnd);
        // but it is:
        assertEquals("2009-02-02", dtStart);
        assertEquals("2009-02-02", dtEnd);

        assertEquals("20090202T000000", startDatePattern);
        assertEquals("20090227T000000", endDatePattern);

        Event e = c.getEvent();
        JsonItem<Event> item = new JsonItem<Event>();
        item.setItem(e);

        // Event to JSON
        String jsonResult = converter.toJSON(item);

        String jsonExpected = "{\"data\":{\"content-type\":\"application/json-appointment\",\"item\":{" +
                "\"folder\":\"Root\\\\Calendar\"," +  // verify the slash number
                "\"tzid\":\"Europe/Berlin\"," +

                // with patch
                "\"startDate\":\"20090202\"," +
                "\"endDate\":\"20090202\"," +
                // without patch
                //"\"startDate\":\"2009-02-02\"," +
                //"\"endDate\":\"2009-02-02\"," +

                "\"subject\":\"ogni lune, mercole, venere\"," +
                "\"body\":\"\",\"location\":\"ma dove vuoi tu\"," +
                "\"allDay\":true," +
                "\"categories\":\"\"," +
                "\"sensitivity\":\"PUBLIC\"," +
                "\"busyStatus\":0,\"importance\":1," +
                "\"organizer\":\"\"," +
                "\"reminder\":0," +
                "\"reminderSoundFile\":\"\"," +
                "\"isRecurring\":true," +
                "\"recurrenceType\":1," +
                "\"dayOfMonth\":0," +
                "\"dayOfWeekMask\":26," +
                "\"instance\":0," +
                "\"interval\":1," +
                "\"monthOfYear\":0," +
                "\"noEndDate\":false," +
                "\"occurrences\":12," +
                "\"patternStartDate\":\"20090202T000000\"," +
                "\"patternEndDate\":\"20090227T000000\"}}}";

        //System.out.println(".................result   "+jsonResult);
        //System.out.println(".................expected "+jsonExpected);
        assertEquals(jsonExpected, jsonResult);

        JsonItem<Event> item1  = converter.fromJSON(jsonResult);
        Calendar calendar = new Calendar();
        calendar.setCalendarContent(item1.getItem());

        dtStart = calendar.getCalendarContent().getDtStart().getPropertyValueAsString();
        dtEnd = calendar.getCalendarContent().getDtEnd().getPropertyValueAsString();
        startDatePattern = calendar.getCalendarContent().getRecurrencePattern().getStartDatePattern();
        endDatePattern = calendar.getCalendarContent().getRecurrencePattern().getEndDatePattern();

        assertEquals("20090202T000000", dtStart);
        assertEquals("20090202T235900", dtEnd);
        assertEquals("20090202T000000", startDatePattern);
        assertEquals("20090227T000000", endDatePattern);

        String xml1 = TestUtility.calendar2sif(calendar);

        String tmp = "";
        int a = 0;

        //System.out.println(".................. Allday_with_end_PatternDate_SIF_2_JSON");
        //System.out.println(".................. " + xml1);

        a = xml1.indexOf("<Start>");
        tmp = xml1.substring(a,xml1.length());
        a = tmp.indexOf("</Start>");
        String startdate = tmp.substring(0,a);
        //System.out.println(".................. " + startdate);

        a = xml1.indexOf("<End>");  // this value can be modified by the synclet
        tmp = xml1.substring(a,xml1.length());
        a = tmp.indexOf("</End>");
        String enddate = tmp.substring(0,a);
        //System.out.println(".................. " + enddate);

        a = xml1.indexOf("<PatternStartDate>");
        tmp = xml1.substring(a,xml1.length());
        a = tmp.indexOf("</PatternStartDate>");
        String patternStartDate = tmp.substring(0,a);
        //System.out.println(".................. " + patternStartDate);

        a = xml1.indexOf("<PatternEndDate>");
        tmp = xml1.substring(a,xml1.length());
        a = tmp.indexOf("</PatternEndDate>");
        String patternEndDate = tmp.substring(0,a);
        //System.out.println(".................. " + patternEndDate);


        // original after the synclet
        //           "<Start>2009-02-02"+
        assertEquals("<Start>2009-02-02", startdate);

        // original after the synclet
        //           "<End>2009-02-02"+
        assertEquals("<End>2009-02-02"    , enddate);

        // original after the synclet
        //           "<PatternStartDate>20090202T000000"+
        assertEquals("<PatternStartDate>20090202T000000", patternStartDate);

        // original after the synclet
        //           "<PatternEndDate>20090227T000000"+
        assertEquals("<PatternEndDate>20090227T000000", patternEndDate);
    }


    /**
     *
     * the item has been sent by the outllok 7.0.7
     * the user select the end date (he didn't select the number of occurrences)
     */
    public void test_Allday_without_end_PatternDate_SIF_2_JSON() throws Exception{
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                "<appointment>"+
                "<SIFVersion>1.1</SIFVersion>"+
                "<AllDayEvent>1</AllDayEvent>"+
                "<BillingInformation/>"+
                "<Body/>"+
                "<BusyStatus>0</BusyStatus>"+
                "<Categories/>"+
                "<Companies/>"+
                "<End>2009-04-06</End>"+
                "<Folder>DEFAULT_FOLDER</Folder>"+
                "<Importance>1</Importance>"+
                "<IsRecurring>1</IsRecurring>"+
                "<Location>xxxxxxxxxxxxxxx</Location>"+
                "<MeetingStatus>0</MeetingStatus>"+
                "<Mileage/>"+
                "<NoAging>0</NoAging>"+
                "<ReminderMinutesBeforeStart>1080</ReminderMinutesBeforeStart>"+
                "<ReminderSet>0</ReminderSet>"+
                "<ReminderSoundFile/>"+
                "<ReplyTime/>"+
                "<Sensitivity>0</Sensitivity>"+
                "<Start>2009-04-06</Start>"+
                "<Subject>xxxxxxxxxxx</Subject>"+
                "<DayOfMonth>0</DayOfMonth>"+
                "<DayOfWeekMask>42</DayOfWeekMask>"+
                "<Instance>0</Instance>"+
                "<Interval>1</Interval>"+
                "<MonthOfYear>0</MonthOfYear>"+
                "<NoEndDate>1</NoEndDate>"+
                "<Occurrences/>"+
                "<PatternEndDate/>"+
                "<PatternStartDate>20090406T000000</PatternStartDate>"+
                "<RecurrenceType>1</RecurrenceType>"+
                "<Exceptions/>"+
                "<Timezone>"+
                "<BasicOffset>+0100</BasicOffset>"+
                "<DayLight>"+
                "<DSTOffset>+0200</DSTOffset><DSTStart>20090329T020000</DSTStart><DSTEnd>20091025T030000</DSTEnd><StandardName/><DSTName/></DayLight>"+
                "<DayLight>"+
                "<DSTOffset>+0200</DSTOffset><DSTStart>20100328T020000</DSTStart><DSTEnd>20101031T030000</DSTEnd><StandardName/><DSTName/></DayLight>"+
                "<DayLight>"+
                "<DSTOffset>+0200</DSTOffset><DSTStart>20110327T020000</DSTStart><DSTEnd>20111030T030000</DSTEnd><StandardName/><DSTName/></DayLight>"+
                "<DayLight>"+
                "<DSTOffset>+0200</DSTOffset><DSTStart>20120325T020000</DSTStart><DSTEnd>20121028T030000</DSTEnd><StandardName/><DSTName/></DayLight>"+
                "<DayLight>"+
                "<DSTOffset>+0200</DSTOffset><DSTStart>20130331T020000</DSTStart><DSTEnd>20131027T030000</DSTEnd><StandardName/><DSTName/></DayLight>"+
                "<DayLight>"+
                "<DSTOffset>+0200</DSTOffset><DSTStart>20140330T020000</DSTStart><DSTEnd>20141026T030000</DSTEnd><StandardName/><DSTName/></DayLight>"+
                "<DayLight>"+
                "<DSTOffset>+0200</DSTOffset><DSTStart>20150329T020000</DSTStart><DSTEnd>20151025T030000</DSTEnd><StandardName/><DSTName/></DayLight>"+
                "</Timezone>"+
                "</appointment>" ;


        Calendar c = TestUtility.sif2Calendar(xml);

        String dtStart = c.getCalendarContent().getDtStart().getPropertyValueAsString();
        String dtEnd = c.getCalendarContent().getDtEnd().getPropertyValueAsString();
        String startDatePattern = c.getCalendarContent().getRecurrencePattern().getStartDatePattern();

        // Foundation bug (or just inconsistency). It should be:
        // assertEquals("20090406T000000", dtStart);
        // assertEquals("20090406T235900", dtEnd);
        // but it is:
        assertEquals("2009-04-06", dtStart);
        assertEquals("2009-04-06", dtEnd);

        assertEquals("20090406T000000", startDatePattern);

        Event e = c.getEvent();
        JsonItem<Event> item = new JsonItem<Event>();
        item.setItem(e);

        // Event to JSON
        String jsonResult = converter.toJSON(item);

        String jsonExpected = "{\"data\":{\"content-type\":\"application/json-appointment\",\"item\":{" +
                "\"folder\":\"Root\\\\Calendar\"," +
                "\"tzid\":\"Europe/Berlin\"," +

                // with patch
                "\"startDate\":\"20090406\"," +
                "\"endDate\":\"20090406\"," +
                // without patch
                //"\"startDate\":\"2009-04-06\"," +
                //"\"endDate\":\"2009-04-06\"," +

                "\"subject\":\"xxxxxxxxxxx\",\"body\":\"\",\"location\":\"xxxxxxxxxxxxxxx\"," +
                "\"allDay\":true," +
                "\"categories\":\"\"," +
                "\"sensitivity\":\"PUBLIC\"," +
                "\"busyStatus\":0," +
                "\"importance\":1," +
                "\"organizer\":\"\"," +
                "\"reminder\":0," +
                "\"reminderSoundFile\":\"\"," +
                "\"isRecurring\":true,\"recurrenceType\":1,\"dayOfMonth\":0," +
                "\"dayOfWeekMask\":42,\"instance\":0,\"interval\":1,\"monthOfYear\":0," +
                "\"noEndDate\":true," +
                "\"occurrences\":-1," +
                "\"patternStartDate\":\"20090406T000000\"}}}";


        //System.out.println("................."+jsonResult);
        assertEquals(jsonExpected, jsonResult);

        JsonItem<Event> item1  = converter.fromJSON(jsonResult);
        Calendar calendar = new Calendar();
        calendar.setCalendarContent(item1.getItem());

        dtStart = calendar.getCalendarContent().getDtStart().getPropertyValueAsString();
        dtEnd = calendar.getCalendarContent().getDtEnd().getPropertyValueAsString();
        startDatePattern = calendar.getCalendarContent().getRecurrencePattern().getStartDatePattern();

        assertEquals("20090406T000000", dtStart);
        assertEquals("20090406T235900", dtEnd);
        assertEquals("20090406T000000", startDatePattern);

        String xml1 = TestUtility.calendar2sif(calendar);

        String tmp = "";
        int a = 0;

        //System.out.println(".................. Allday_without_end_PatternDate_SIF_2_JSON");
        //System.out.println(".................. " + xml1);

        a = xml1.indexOf("<Start>");
        tmp = xml1.substring(a,xml1.length());
        a = tmp.indexOf("</Start>");
        String startdate = tmp.substring(0,a);
        //System.out.println(".................. " + startdate);

        a = xml1.indexOf("<End>");  // this value can be modified by the synclet
        tmp = xml1.substring(a,xml1.length());
        a = tmp.indexOf("</End>");
        String enddate = tmp.substring(0,a);
        //System.out.println(".................. " + enddate);

        a = xml1.indexOf("<PatternStartDate>");
        tmp = xml1.substring(a,xml1.length());
        a = tmp.indexOf("</PatternStartDate>");
        String patternStartDate = tmp.substring(0,a);
        //System.out.println(".................. " + patternStartDate);

        //a = xml1.indexOf("<PatternEndDate>");
        //tmp = xml1.substring(a,xml1.length());
        //a = tmp.indexOf("</PatternEndDate>");
        //String patternEndDate = tmp.substring(0,a);
        //System.out.println(".................. " + patternEndDate);

        // original after the synclet
        //           "<Start>2009-04-06"+
        assertEquals("<Start>2009-04-06", startdate);

        // original after the synclet
        //           "<End>2009-04-06"+
        assertEquals("<End>2009-04-06"    , enddate);

        // original after the synclet
        //           "<PatternStartDate>20090406T000000"+
        assertEquals("<PatternStartDate>20090406T000000", patternStartDate);

    }

    public void test_Allday_Rec_with_Exception_SIF_2_JSON() throws Exception {
        // Allday yearly recurrent event with an exception
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                "<appointment>"+
                "<SIFVersion>1.1</SIFVersion>"+
                "<Folder>DEFAULT_FOLDER</Folder>"+
                "<Subject>Convert SIF to json</Subject>"+
                "<Start>2009-08-05</Start>"+
                "<End>2009-08-05</End>"+
                "<AllDayEvent>1</AllDayEvent>"+
                "<BillingInformation/>"+
                "<Body/>"+
                "<BusyStatus>0</BusyStatus>"+
                "<Categories/>"+
                "<Companies/>"+
                "<MeetingStatus>0</MeetingStatus>"+
                "<Mileage/>"+
                "<NoAging>0</NoAging>"+
                "<ReminderMinutesBeforeStart>1080</ReminderMinutesBeforeStart>"+
                "<ReminderSet>0</ReminderSet>"+
                "<ReminderSoundFile/>"+
                "<ReplyTime/>"+
                "<Sensitivity>0</Sensitivity>"+

                "<IsRecurring>1</IsRecurring>"+
                "<RecurrenceType>5</RecurrenceType>"+
                "<Importance>1</Importance>"+
                "<Location>London</Location>"+
                "<DayOfMonth>5</DayOfMonth>"+
                "<DayOfWeekMask>0</DayOfWeekMask>"+
                "<Instance>0</Instance>"+
                "<Interval>1</Interval>"+
                "<MonthOfYear>8</MonthOfYear>"+
                "<NoEndDate>0</NoEndDate>"+
                "<Occurrences>4</Occurrences>"+
                "<PatternEndDate>20120805T000000</PatternEndDate>"+
                "<PatternStartDate>20090805T000000</PatternStartDate>"+
                "<Exceptions>" +
                "<ExcludeDate>2010-08-05</ExcludeDate>" +
                "</Exceptions>" +
                "<Timezone>"+
                "<BasicOffset>+0100</BasicOffset>"+
                "<DayLight>"+
                "<DSTOffset>+0200</DSTOffset><DSTStart>20090329T020000</DSTStart>" +
                "<DSTEnd>20091025T030000</DSTEnd><StandardName/><DSTName/></DayLight>"+
                "</Timezone>"+

                "</appointment>" ;

        Calendar c = TestUtility.sif2Calendar(xml);

        String dtStart = c.getCalendarContent().getDtStart().getPropertyValueAsString();
        String dtEnd = c.getCalendarContent().getDtEnd().getPropertyValueAsString();
        String startDatePattern = c.getCalendarContent().getRecurrencePattern().getStartDatePattern();
        String endDatePattern = c.getCalendarContent().getRecurrencePattern().getEndDatePattern();

        assertEquals("2009-08-05", dtStart);
        assertEquals("2009-08-05", dtEnd);

        assertEquals("20090805T000000", startDatePattern);
        assertEquals("20120805T000000", endDatePattern);

        Event e = c.getEvent();
        JsonItem<Event> item = new JsonItem<Event>();
        item.setItem(e);

        // Event to JSON
        String jsonResult = converter.toJSON(item);

        String jsonExpected = "{\"data\":{\"content-type\":\"application/json-appointment\",\"item\":{" +
                "\"folder\":\"Root\\\\Calendar\"," +  // verify the slash number
                "\"tzid\":\"Europe/Berlin\"," +

                // with patch
                "\"startDate\":\"20090805\"," +
                "\"endDate\":\"20090805\"," +

                "\"subject\":\"Convert SIF to json\"," +
                "\"body\":\"\",\"location\":\"London\"," +
                "\"allDay\":true," +
                "\"categories\":\"\"," +
                "\"sensitivity\":\"PUBLIC\"," +
                "\"busyStatus\":0,\"importance\":1," +
                "\"organizer\":\"\"," +
                "\"reminder\":0," +
                "\"reminderSoundFile\":\"\"," +
                "\"isRecurring\":true," +
                "\"recurrenceType\":5," +
                "\"dayOfMonth\":5," +
                "\"dayOfWeekMask\":0," +
                "\"instance\":0," +
                "\"interval\":1," +
                "\"monthOfYear\":8," +
                "\"noEndDate\":false," +
                "\"occurrences\":4," +
                "\"patternStartDate\":\"20090805T000000\"," +
                "\"patternEndDate\":\"20120805T000000\"," +
                "\"exceptionsIncluded\":[]," +
                "\"exceptionsExcluded\":[\"20100805\"]}}}";
        assertEquals(jsonExpected, jsonResult);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void testExceptions_JSON_2_VCAL() throws Exception{
        String jsonItem =
              "{\"data\":{\"content-type\":\"application/json-appointment\",\"item\":{"+
              "\"key\":\"10757538\"," +
              "\"folder_id\":\"1832444\"," +

              "\"subject\":\"ogni marted\"," +
              "\"startDate\":\"20081104T230000Z\"," +
              "\"endDate\":\"20081105T000000Z\"," +
              "\"reminder\":\"0\"," +
              "\"reminderTime\":\"0\"," +
              "\"importance\":\"0\"," +
              "\"SentReminder\":\"No\"," +
              "\"inviteContacts\":\"No\"," +
              "\"allDay\":\"false\"," +

              "\"isRecurring\":\"true\"," +
              "\"recurrenceType\":1," +
              "\"patternStartDate\":\"20081104T170000\"," +
              "\"noEndDate\":\"false\"," +
              "\"patternEndDate\":\"20081130T000000\"," +
              "\"interval\":\"1\"," +
              "\"dayOfWeekMask\":0," +

              "\"exceptionsExcluded\":[\"20081104T170000\",\"20081105T170000\"]," +

              "\"Timestamp\":\"20081021T084751\"," +
              "\"customerID\":\"406940\"," +
              "\"repeatAppointmentID\":\"0\"," +
              "\"show_time_as\":\"0\"," +
              "\"label\":\"0\"," +
              "\"PublicAction\":\"None\"," +
              "\"PublicStatus\":\"None\"," +
              "\"PublicUpdateID\":\"0\"" +
              "}}}" ;

        // Event to JSON
        // TMP: until the Json sets th tzid = U.I. timezome in the json object
        String tzid = "America/Mexico_City";
        converter.setServerTimeZoneID(tzid);
        JsonItem<Event> event = converter.fromJSON(jsonItem);

        List<ExceptionToRecurrenceRule> exceptions = event.getItem().getRecurrencePattern().getExceptions();
        assertEquals("20081104T160000Z", exceptions.get(0).getDate());
        assertEquals("20081105T160000Z", exceptions.get(1).getDate());
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void testReminder() throws Exception {

        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<appointment>" +
                "<SIFVersion>1.1</SIFVersion>" +
                "<Subject>Tre</Subject>" +
                "<Start>20081103T150000Z</Start>" +
                "<End>20081103T160000Z</End>" +
                "<Location>terme di miradolo</Location>" +
                "<Folder>DEFAULT_FOLDER</Folder>" +
                "<ReminderSet>1</ReminderSet>" +
                "<ReminderMinutesBeforeStart>15</ReminderMinutesBeforeStart>" +
                "<ReminderOptions>4</ReminderOptions>" +
                "<ReminderSoundFile/>" +
                "<Sensitivity>0</Sensitivity>" +
                "<AllDayEvent>0</AllDayEvent>" +
                "<MeetingStatus>0</MeetingStatus>" +
                "<Body/>" +
                "<BusyStatus>2</BusyStatus>" +
                "<Categories/>" +
                "<IsRecurring>0</IsRecurring>" +
                "<Timezone>" +
                "<BasicOffset>-0600</BasicOffset>" +
                "<DayLight>" +
                "<DSTOffset>-0500</DSTOffset><DSTStart>20080406T020000</DSTStart>" +
                "<DSTEnd>20081026T020000</DSTEnd><StandardName/><DSTName/" +
                "></DayLight>" +
                "</Timezone>" +
                "</appointment>" ;

        Calendar c = TestUtility.sif2Calendar(xml);
        Event e = c.getEvent();
        JsonItem<Event> item = new JsonItem<Event>();
        item.setItem(e);

        // Event to JSON
        String jsonResult = converter.toJSON(item);
        JSONObject jo = JSONObject.fromObject(jsonResult);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public void testExceptions_JSON_2_SIF() throws Exception{
        String jsonItem =
              "{\"data\":{\"content-type\":\"application/json-appointment\",\"item\":{"+
              "\"key\":\"10757538\"," +
              "\"folder_id\":\"1832444\"," +

              "\"subject\":\"ogni marted\"," +
              "\"startDate\":\"20081104T230000Z\"," +
              "\"endDate\":\"20081105T000000Z\"," +
              "\"reminder\":\"0\"," +
              "\"reminderTime\":\"0\"," +
              "\"importance\":\"0\"," +
              "\"SentReminder\":\"No\"," +
              "\"inviteContacts\":\"No\"," +
              "\"allDay\":\"false\"," +

              "\"isRecurring\":\"true\"," +
              "\"recurrenceType\":1," +
              "\"patternStartDate\":\"20081104T170000\"," +
              "\"noEndDate\":\"false\"," +
              "\"patternEndDate\":\"20081130T000000\"," +
              "\"interval\":\"1\"," +
              "\"dayOfWeekMask\":0," +

              "\"exceptionsExcluded\":[\"20081104T170000\",\"20081105T170000\"]," +

              "\"Timestamp\":\"20081021T084751\"," +
              "\"customerID\":\"406940\"," +
              "\"repeatAppointmentID\":\"0\"," +
              "\"show_time_as\":\"0\"," +
              "\"label\":\"0\"," +
              "\"PublicAction\":\"None\"," +
              "\"PublicStatus\":\"None\"," +
              "\"PublicUpdateID\":\"0\"" +
              "}}}" ;

        // JSON Object to Event
        // TMP: until the Json sets th tzid = U.I. timezome in the json object
        converter.setServerTimeZoneID("America/Mexico_City");
        JsonItem<Event> event = converter.fromJSON(jsonItem);

        Calendar calendar = new Calendar();
        calendar.setCalendarContent(event.getItem());

        String dtStart = calendar.getCalendarContent().getDtStart().getPropertyValueAsString();
        String dtEnd = calendar.getCalendarContent().getDtEnd().getPropertyValueAsString();
        String startDatePattern = calendar.getCalendarContent().getRecurrencePattern().getStartDatePattern();
        String endDatePattern = calendar.getCalendarContent().getRecurrencePattern().getEndDatePattern();

        assertEquals("20081104T230000Z", dtStart);
        assertEquals("20081105T000000Z", dtEnd);
        assertEquals("20081104T170000", startDatePattern);
        assertEquals("20081130T000000", endDatePattern);

        List<ExceptionToRecurrenceRule> exceptions = event.getItem().getRecurrencePattern().getExceptions();
        assertEquals("20081104T160000Z", exceptions.get(0).getDate());
        assertEquals("20081105T160000Z", exceptions.get(1).getDate());

        /*
        String resultXML = calendar2sif(calendar);

        // test result
        //System.out.println(".................testExceptions_JSON_2_SIF"+resultXML);
        Calendar calendarRes = sif2Calendar(resultXML);
        Event eventRes = calendarRes.getEvent();
        List<ExceptionToRecurrenceRule> exceptions = eventRes.getRecurrencePattern().getExceptions();

        assertEquals("20081104T170000", exceptions.get(0).getDate());
        assertEquals("20081105T170000", exceptions.get(1).getDate());
        */


    }

    /**
     * From OUTLOOK to Json
     *
     * i.e.
     *
     * ..
     * <Email1Address>1@funambol.com</Email1Address>
     * <FirstName>Gamma Zuse</FirstName>
     * <Folder>DEFAULT_FOLDER\Funambol</Folder>
     * ..
     *
     */
    public void test_FolderParser_C2S_00() {

        JsonItem<Event> item = new JsonItem<Event>();

        item.setKey("0");
        item.setState("A");
        item.setContentType("type");

        Event event = new Event();

        // the outlook path is
        // \\Personal Folders\Calendar\Funambol
        // the
        // DEFAULT_FOLDER <==> \\Personal Folders\Calendar
        event.setFolder(new Property("DEFAULT_FOLDER\\Funambol"));

        event.setSummary(new Property("il sommario per l'appointment"));
        event.setDtStart(new Property("20081121T153000Z"));
        event.setDtEnd(new Property("20081121T163000Z"));
        event.setAllDay(false);

        item.setItem(event);

        String jsonResult = converter.toJSON(item);

        JSONObject jo = JSONObject.fromObject(jsonResult);

        JSONObject jodata = jo.getJSONObject("data");
        JSONObject joitem = jodata.getJSONObject("item");
        String folder = joitem.getString("folder");

        //System.out.println("........... from OUTLOOK to Json: '" + folder + "'");
        //System.out.println("[folder="+folder+"]=[folder="+"Root\\Calendar\\Funambol"+"]");
        //System.out.flush();
        assertEquals(folder, "Root\\Calendar\\Funambol");

    }

    /**
     * From WM to Json
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
    public void test_FolderParser_C2S_01() {

        JsonItem<Event> item = new JsonItem<Event>();

        item.setKey("0");
        item.setState("A");
        item.setContentType("type");

        Event event = new Event();

        event.setSummary(new Property("il sommario per l'appointment"));
        event.setDtStart(new Property("20081121T153000Z"));
        event.setDtEnd(new Property("20081121T163000Z"));
        event.setAllDay(false);

        item.setItem(event);

        String jsonResult = converter.toJSON(item);

        JSONObject jo = JSONObject.fromObject(jsonResult);

        JSONObject jodata = jo.getJSONObject("data");
        JSONObject joitem = jodata.getJSONObject("item");
        String folder = joitem.optString("folder");

        //System.out.println("........... from WM to Json: '" + folder + "'");
        assertEquals(folder, "");
    }


    /**
     * From Json to clients
     *
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

        jsonData.elementOpt("content-type", "application/json-appointment");

        jsonItem.elementOpt("key", "0");
        jsonItem.elementOpt("state", "A");
        jsonItem.elementOpt("folder", "Root\\Calendar\\Json");

        jsonItem.elementOpt("subject", "appuntamento da Json");
        jsonItem.elementOpt("startDate", "20081212T170000Z");
        jsonItem.elementOpt("endDate", "20081212T170000Z");

        jsonData.element("item", jsonItem);
        jsonRoot.element("data", jsonData);

        JsonItem<Event> calendarItem = converter.fromJSON(jsonRoot.toString());

        Event item = calendarItem.getItem();

        String folder = item.getFolder().getPropertyValueAsString();
        assertEquals(folder, "DEFAULT_FOLDER\\Json");
    }

    /**
     * tests the case where a backend sends only some of the fields in the json,
     * in this case the item returned should include only the fields that came in the json
     * @throws java.lang.Exception
     */
    public void test_NotReturnJsonFields() throws Exception {

        String expectedVcal = "BEGIN:VCALENDAR\n" +
                "VERSION:1.0\n" +
                "BEGIN:VEVENT\n" +
                "SUMMARY:appuntamento da Json\n" +
                "CLASS:PUBLIC\n" +
                "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\\\\Json\n" +
//                "X-MICROSOFT-CDO-BUSYSTATUS:FREE\n" +
                "X-FUNAMBOL-ALLDAY:0\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR\n";

        String expectIcal = "BEGIN:VCALENDAR\n" +
                "VERSION:2.0\n" +
                "BEGIN:VEVENT\n" +
                "SUMMARY:appuntamento da Json\n" +
                "CLASS:PUBLIC\n" +
                "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\\\\Json\n" +
//                "X-MICROSOFT-CDO-BUSYSTATUS:FREE\n" +
                "X-FUNAMBOL-ALLDAY:0\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR\n";

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        jsonData.elementOpt("content-type", "application/json-appointment");

        jsonItem.elementOpt("key", "0");
        jsonItem.elementOpt("state", "A");
        jsonItem.elementOpt("folder", "Root\\Calendar\\Json");

        jsonItem.elementOpt("subject", "appuntamento da Json");

        jsonData.element("item", jsonItem);
        jsonRoot.element("data", jsonData);

        JsonItem<Event> calendarItem = converter.fromJSON(jsonRoot.toString());

        Event item = calendarItem.getItem();

        Calendar cal = new Calendar(item);

        assertEquals(expectedVcal,
                TestUtility.calendar2webCalendar(cal, TestUtility.getType(TestUtility.VCAL_FORMAT), null, CHARSET).replaceAll("\\r", ""));
        assertEquals(expectIcal,
                TestUtility.calendar2webCalendar(cal, TestUtility.getType(TestUtility.ICAL_FORMAT), null, CHARSET).replaceAll("\\r", ""));

    }

    /**
     * tests if the converter handles correctly a json item without folder
     * @throws java.lang.Exception
     */
    public void test_JsonWithoutFolderToIcalVcal() throws Exception {

        String expectedVcal = "BEGIN:VCALENDAR\n" +
                "VERSION:1.0\n" +
                "BEGIN:VEVENT\n" +
                "SUMMARY:appuntamento da Json\n" +
                "CLASS:PUBLIC\n" +
//                "X-MICROSOFT-CDO-BUSYSTATUS:FREE\n" +
                "X-FUNAMBOL-ALLDAY:0\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR\n";

        String expectIcal = "BEGIN:VCALENDAR\n" +
                "VERSION:2.0\n" +
                "BEGIN:VEVENT\n" +
                "SUMMARY:appuntamento da Json\n" +
                "CLASS:PUBLIC\n" +
//                "X-MICROSOFT-CDO-BUSYSTATUS:FREE\n" +
                "X-FUNAMBOL-ALLDAY:0\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR\n";

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        jsonData.elementOpt("content-type", "application/json-appointment");

        jsonItem.elementOpt("key", "0");
        jsonItem.elementOpt("state", "A");

        jsonItem.elementOpt("subject", "appuntamento da Json");

        jsonData.element("item", jsonItem);
        jsonRoot.element("data", jsonData);

        JsonItem<Event> calendarItem = converter.fromJSON(jsonRoot.toString());

        Event item = calendarItem.getItem();

        Calendar cal = new Calendar(item);

       assertEquals(expectedVcal,
               TestUtility.calendar2webCalendar(cal, TestUtility.getType(TestUtility.VCAL_FORMAT), null, CHARSET)
                               .replaceAll("\\r", ""));
       assertEquals(expectIcal,
               TestUtility.calendar2webCalendar(cal, TestUtility.getType(TestUtility.ICAL_FORMAT), null, CHARSET)
                               .replaceAll("\\r", ""));
    }

    public void testToJSON_VCalWithClass() throws Exception {

        StringBuilder vcal = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
            .append("BEGIN:VEVENT\r\n")
            .append("X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("DTSTART:20100217T100000Z\r\n")
            .append("DTEND:20100217T103000Z\r\n")
            .append("CATEGORIES:categories\r\n")
            .append("DESCRIPTION:event description\r\n")
            .append("LOCATION:San Francisco\r\n")
            .append("PRIORITY:2\r\n")
            .append("STATUS:0\r\n")
            .append("SUMMARY:meeting\r\n")
            .append("CLASS:PRIVATE\r\n")
            .append("AALARM:20100217T094500Z;;0;\r\n")
            .append("END:VEVENT\r\n")
            .append("END:VCALENDAR");

        Calendar cal = TestUtility.webCalendar2Calendar(
            vcal.toString(),
            TestUtility.getType(TestUtility.VCAL_FORMAT),
            null,
            CHARSET
        );

        JsonItem<Event> item = new JsonItem<Event>();
        item.setKey("0");
        item.setState("A");
        item.setContentType("type");
        item.setItem(cal.getEvent());

        String result = converter.toJSON(item);

        StringBuilder expected = new StringBuilder("{\"data\":{")
            .append("\"content-type\":\"application/json-appointment\",")
            .append("\"item\":{\"key\":\"0\",")
            .append("\"state\":\"A\",")
            .append("\"folder\":\"Root\\\\Calendar\",")
            .append("\"startDate\":\"20100217T100000Z\",")
            .append("\"endDate\":\"20100217T103000Z\",")
            .append("\"subject\":\"meeting\",")
            .append("\"body\":\"event description\",")
            .append("\"location\":\"San Francisco\",")
            .append("\"allDay\":false,")
            .append("\"categories\":\"categories\",")
            .append("\"sensitivity\":\"PRIVATE\",")
//            .append("\"busyStatus\":0,")
            .append("\"importance\":1,")
            .append("\"reminder\":1,\"reminderTime\":15,")
            .append("\"isRecurring\":false}}}");
        assertEquals("Wrong JSON content converted from Event", expected.toString(), result);

        JsonItem<Event> item2 = converter.fromJSON(result);
        String result2 = converter.toJSON(item2);
        assertEquals("Wrong JSON object converted from JSONItem", result, result2);
        
    }

    public void testToJSON_VCalWithoutClass() throws Exception {

        StringBuilder vcal = new StringBuilder("BEGIN:VCALENDAR\r\n")
            .append("VERSION:1.0\r\n")
            .append("BEGIN:VEVENT\r\n")
            .append("X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\r\n")
            .append("X-FUNAMBOL-ALLDAY:0\r\n")
            .append("DTSTART:20100217T100000Z\r\n")
            .append("DTEND:20100217T103000Z\r\n")
            .append("CATEGORIES:categories\r\n")
            .append("DESCRIPTION:event description\r\n")
            .append("LOCATION:San Francisco\r\n")
            .append("PRIORITY:2\r\n")
            .append("STATUS:0\r\n")
            .append("SUMMARY:meeting\r\n")
            .append("AALARM:20100217T094500Z;;0;\r\n")
            .append("END:VEVENT\r\n")
            .append("END:VCALENDAR");

        Calendar cal = TestUtility.webCalendar2Calendar(
            vcal.toString(),
            TestUtility.getType(TestUtility.VCAL_FORMAT),
            null,
            CHARSET
        );

        JsonItem<Event> item = new JsonItem<Event>();
        item.setKey("0");
        item.setState("A");
        item.setContentType("type");
        item.setItem(cal.getEvent());

        String result = converter.toJSON(item);

        StringBuilder expected = new StringBuilder("{\"data\":{")
            .append("\"content-type\":\"application/json-appointment\",")
            .append("\"item\":{\"key\":\"0\",")
            .append("\"state\":\"A\",")
            .append("\"folder\":\"Root\\\\Calendar\",")
            .append("\"startDate\":\"20100217T100000Z\",")
            .append("\"endDate\":\"20100217T103000Z\",")
            .append("\"subject\":\"meeting\",")
            .append("\"body\":\"event description\",")
            .append("\"location\":\"San Francisco\",")
            .append("\"allDay\":false,")
            .append("\"categories\":\"categories\",")
            .append("\"sensitivity\":\"PUBLIC\",")
//            .append("\"busyStatus\":0,")
            .append("\"importance\":1,")
            .append("\"reminder\":1,\"reminderTime\":15,")
            .append("\"isRecurring\":false}}}");
        assertEquals("Wrong JSON content converted from Event", expected.toString(), result);

        JsonItem<Event> item2 = converter.fromJSON(result);
        String result2 = converter.toJSON(item2);
        assertEquals("Wrong JSON object converted from JSONItem", result, result2);

    }

}
