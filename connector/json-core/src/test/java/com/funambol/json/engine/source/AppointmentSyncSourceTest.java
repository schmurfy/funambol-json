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
import net.sf.json.JSONObject;
import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.Event;
import com.funambol.framework.core.CTInfo;
import com.funambol.framework.core.DSMem;
import com.funambol.framework.core.DataStore;
import com.funambol.framework.core.DevInf;
import com.funambol.framework.core.SourceRef;
import com.funambol.framework.core.SyncCap;
import com.funambol.framework.core.VerDTD;
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
import com.funambol.json.converter.AppointmentConverter;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.security.JsonUser;
import com.funambol.json.util.Utility;
import java.util.TimeZone;

public class AppointmentSyncSourceTest extends AbstractHttpTransportTest {

    private static JsonServlet jsonServlet = new JsonServlet();

    public AppointmentSyncSourceTest() {
        super(jsonServlet);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

    }

    /**
     * 
     * @throws Exception
     */
    public void test_Add() throws Exception {

        CalendarSyncSource source = createAppointmentSyncSource();

        SyncContext context = createContext();

        ContentType[] contentTypes = null;
        contentTypes = new ContentType[1];
        contentTypes[0] = new ContentType("text/x-s4j-sifc", "1.0");
        source.setBackendType(new SyncSourceInfo(contentTypes, 0));

        source.init();

        source.beginSync(context);

        jsonServlet.setDoReturn(JsonServlet.ITEMS);

        String sife =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><appointment>"
                + "<Subject>Ten past ten to quarter past eleven in the morning (UTC)</Subject>"
                + "<Location>London</Location><Start>20080320T101000Z</Start><End>20080320T111500Z</End>"
                + "<Folder>Appointment</Folder></appointment>";

        String sifeUpdate =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><appointment>"
                + "<Subject>Updated Ten past ten to quarter past eleven in the morning (UTC)</Subject>"
                + "<Location>London updated</Location><Start>20080320T101000ZUP</Start><End>20080320T111500ZUP</End>"
                + "<Folder>Appointment</Folder></appointment>";


        try {

            Object anyKey = String.valueOf(System.currentTimeMillis()); // dummy

            SyncItem syncItem = new SyncItemImpl(source, anyKey, null, null,
                    SyncItemState.NEW,
                    sife.getBytes(),
                    null, "text/x-s4j-sife", null);

            SyncItemKey itemKey = source.addSyncItem(syncItem).getKey();

            String guid = itemKey.getKeyAsString();
            assertTrue(guid.equals(Utility.APPOINTMENT_OBJECT + Utility.GUID_SEP + "0"));


            SyncItem syncItem2 = new SyncItemImpl(source, guid, null, null,
                    SyncItemState.UPDATED,
                    sifeUpdate.getBytes(),
                    null, "text/x-s4j-sife", null);

            String guid2 = syncItem2.getKey().getKeyAsString();
            assertTrue(guid2.equals(Utility.APPOINTMENT_OBJECT + Utility.GUID_SEP + "0"));

            SyncItem syncItemUpdated = source.updateSyncItem(syncItem2);
            String guid3 = syncItemUpdated.getKey().getKeyAsString();
            assertTrue(guid3.equals(Utility.APPOINTMENT_OBJECT + Utility.GUID_SEP + "0"));

        } catch (SyncSourceException e) {
            e.printStackTrace();
            fail();
        }

        jsonServlet.setDoReturn(JsonServlet.ENDSYNC);
        source.endSync();
    }

    /**
     * 
     * @throws Exception
     */
    public void test_getRecurring() throws Exception {

        CalendarSyncSource source = createAppointmentSyncSource();

        SyncContext context = createContext();

        jsonServlet.setDoReturn(JsonServlet.EMPTY);

        ContentType[] contentTypes = null;
        contentTypes = new ContentType[1];
        contentTypes[0] = new ContentType("text/x-s4j-sifc", "1.0");
        source.setBackendType(new SyncSourceInfo(contentTypes, 0));

        source.init();

        source.beginSync(context);

        jsonServlet.setDoReturn(JsonServlet.ITEMS);

        String sife = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<appointment>"
                + "<SIFVersion>1.1</SIFVersion>"
                + "<Subject>Daily</Subject>"
                + "<Location>Ogni 2 giorni</Location>"
                + "<Start>20081027T180000Z</Start>"
                + "<End>20081027T190000Z</End>"
                + "<MeetingStatus>0</MeetingStatus>"
                + "<ReminderMinutesBeforeStart>15</ReminderMinutesBeforeStart>"
                + "<ReminderOptions>4</ReminderOptions>"
                + "<ReminderSet>1</ReminderSet>"
                + "<Body/>"
                + "<Sensitivity>0</Sensitivity>"
                + "<BusyStatus>2</BusyStatus>"
                + "<Categories/>"
                + "<AllDayEvent>0</AllDayEvent>"
                + "<IsRecurring>1</IsRecurring>"
                + "<DayOfMonth>0</DayOfMonth>"
                + "<DayOfWeekMask>0</DayOfWeekMask>"
                + "<Instance>0</Instance>"
                + "<Interval>2</Interval>"
                + "<MonthOfYear>0</MonthOfYear>"
                + "<NoEndDate>0</NoEndDate>"
                + "<Occurrences>18</Occurrences>"
                + "<PatternEndDate>20081130T120000</PatternEndDate>"
                + "<PatternStartDate>20081027T120000</PatternStartDate>"
                + "<RecurrenceType>0</RecurrenceType>"
                + "<Exceptions/>"
                + "<Timezone>"
                + "<BasicOffset>-0600</BasicOffset>"
                + "<DayLight>"
                + "<DSTOffset>-0500</DSTOffset>"
                + "<DSTStart>20080406T020000</DSTStart><DSTEnd>20081026T020000</DSTEnd><StandardName/><DSTName/>"
                + "</DayLight>"
                + "</Timezone>"
                + "</appointment>";

        try {

            // add a recurring appoint to the repository
            Object anyKey = String.valueOf(System.currentTimeMillis()); // dummy
            SyncItem syncItem = new SyncItemImpl(source,
                    anyKey,
                    null,
                    null,
                    SyncItemState.NEW,
                    sife.getBytes(),
                    null,
                    "text/x-s4j-sife",
                    null);

            SyncItemKey itemKey = source.addSyncItem(syncItem).getKey();

            SyncItem item = source.getSyncItemFromId(itemKey);

            byte[] itemContent = item.getContent();
            String content = new String(itemContent == null ? new byte[0] : itemContent);

            Calendar cal = source.sif2Calendar(content);

            Event e = cal.getEvent();

            //System.out.println(" .... timezone " + e.getDtStart().getTimeZone());

            // TMP
            // until the backend sets th tzid = U.I. timezome in the json object
            assertEquals("America/Mexico_City", e.getDtStart().getTimeZone());
            //assertEquals("America/Chicago", e.getDtStart().getTimeZone());


        } catch (SyncSourceException e) {
            e.printStackTrace();
            fail();
        }

        jsonServlet.setDoReturn(JsonServlet.ENDSYNC);
        source.endSync();

    }

    /**
     * Note: this SIF is not correct;
     * 
     * 
     * @throws Exception
     */
    public void test_getRecurring_without_TZ() throws Exception {

        CalendarSyncSource source = createAppointmentSyncSource();

        SyncContext context = createContext();

        jsonServlet.setDoReturn(JsonServlet.EMPTY);

        ContentType[] contentTypes = null;
        contentTypes = new ContentType[1];
        contentTypes[0] = new ContentType("text/x-s4j-sifc", "1.0");
        source.setBackendType(new SyncSourceInfo(contentTypes, 0));

        source.init();

        source.beginSync(context);

        jsonServlet.setDoReturn(JsonServlet.ITEMS);

        String sife = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<appointment>"
                + "<SIFVersion>1.1</SIFVersion>"
                + "<Subject>Daily</Subject>"
                + "<Location>Ogni 2 giorni</Location>"
                + "<Start>20081027T180000Z</Start>"
                + "<End>20081027T190000Z</End>"
                + "<MeetingStatus>0</MeetingStatus>"
                + "<ReminderMinutesBeforeStart>15</ReminderMinutesBeforeStart>"
                + "<ReminderOptions>4</ReminderOptions>"
                + "<ReminderSet>1</ReminderSet>"
                + "<Body/>"
                + "<Sensitivity>0</Sensitivity>"
                + "<BusyStatus>2</BusyStatus>"
                + "<Categories/>"
                + "<AllDayEvent>0</AllDayEvent>"
                + "<IsRecurring>1</IsRecurring>"
                + "<DayOfMonth>0</DayOfMonth>"
                + "<DayOfWeekMask>0</DayOfWeekMask>"
                + "<Instance>0</Instance>"
                + "<Interval>2</Interval>"
                + "<MonthOfYear>0</MonthOfYear>"
                + "<NoEndDate>0</NoEndDate>"
                + "<Occurrences>18</Occurrences>"
                + "<PatternEndDate>20081130T120000</PatternEndDate>"
                + "<PatternStartDate>20081027T120000</PatternStartDate>"
                + "<RecurrenceType>0</RecurrenceType>"
                + "<Exceptions/>"
                + "</appointment>";

        try {

            // add a recurring appointment to the repository
            Object anyKey = String.valueOf(System.currentTimeMillis()); // dummy
            SyncItem syncItem = new SyncItemImpl(source,
                    anyKey,
                    null,
                    null,
                    SyncItemState.NEW,
                    sife.getBytes(),
                    null,
                    "text/x-s4j-sife",
                    null);

            SyncItemKey itemKey = source.addSyncItem(syncItem).getKey();

            SyncItem item = source.getSyncItemFromId(itemKey);

            byte[] itemContent = item.getContent();
            String content = new String(itemContent == null ? new byte[0] : itemContent);

            Calendar cal = source.sif2Calendar(content);
            Event e = cal.getEvent();
            //System.out.println(" .... timezone " + e.getDtStart().getTimeZone());
            //assertEquals("America/Chicago", e.getDtStart().getTimeZone());
            assertEquals(null, e.getDtStart().getTimeZone());

        } catch (SyncSourceException e) {
            e.printStackTrace();
            fail();
        }

        jsonServlet.setDoReturn(JsonServlet.ENDSYNC);
        source.endSync();

    }

    /**
     * "IMPORTANCE" for appointment is saved in the "Priority" property
     * 
     * @throws java.lang.Exception
     */
    public void test_Importance() throws Exception {

        CalendarSyncSource source = createAppointmentSyncSource();

        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<appointment>"
                + "<SIFVersion>1.1</SIFVersion>"
                + "<AllDayEvent>0</AllDayEvent>"
                + "<BillingInformation/>"
                + "<Body>note per item 1 3</Body>"
                + "<BusyStatus>2</BusyStatus>"
                + "<Categories/>"
                + "<Companies/>"
                + "<End>20081126T233000Z</End>"
                + "<Folder>DEFAULT_FOLDER</Folder>"
                + "<Importance>2</Importance>"
                + "<IsRecurring>0</IsRecurring>"
                + "<Location>location3</Location>"
                + "<MeetingStatus>0</MeetingStatus>"
                + "<Mileage/>"
                + "<NoAging>0</NoAging>"
                + "<ReminderMinutesBeforeStart>15</ReminderMinutesBeforeStart>"
                + "<ReminderSet>1</ReminderSet>"
                + "<ReminderSoundFile/>"
                + "<ReplyTime/>"
                + "<Sensitivity>0</Sensitivity>"
                + "<Start>20081126T230000Z</Start>"
                + "<Subject>item1</Subject>"
                + "</appointment>";


        Calendar calendar = source.sif2Calendar(xml);

        // there is the FOUNDATION converter that  
        assertEquals("1", calendar.getEvent().getPriority().getPropertyValueAsString());

        JsonItem<Event> item = new JsonItem<Event>();
        item.setContentType("type");
        item.setKey("0");
        item.setState("A");
        item.setItem(calendar.getEvent());

        AppointmentConverter converter = new AppointmentConverter();

        String jsonResult = converter.toJSON(item);

        JSONObject jsonRoot = JSONObject.fromObject(jsonResult);
        JSONObject jsonData = jsonRoot.getJSONObject("data");
        JSONObject jsonItem = jsonData.getJSONObject("item");


        String imp = Utility.getJsonValue(jsonItem, "importance");

        // high = 2
        // normal = 1
        // low = 0
        assertEquals("2", imp);

    }

    /**
     * "IMPORTANCE" for appointment is saved in the "Priority" property
     *
     * @throws java.lang.Exception
     */
    public void test_Importance_vcal() throws Exception {

        CalendarSyncSource source = createAppointmentSyncSource();

        String VCAL_FORMAT = "text/x-vcalendar";
        String ICAL_FORMAT = "text/calendar";

        String vcal =
                "BEGIN:VCALENDAR\n"
                + "VERSION:1.0\n"
                + "BEGIN:VEVENT\n"
                + "UID:bC8MS7CB4EDme46ibFFgh2\n"
                + "SUMMARY:Bring luggage for ross\n"
                + "DTSTART:20080908T063000Z\n"
                + "DTEND:20080908T063000Z\n"
                + "X-EPOCAGENDAENTRYTYPE:APPOINTMENT\n"
                + "CLASS:PRIVATE\n"
                + "X-SYMBIAN-DTSTAMP:20080908T081313Z\n"
                + "SEQUENCE:0\n"
                + "X-METHOD:NONE\n"
                + "ATTENDEE;ROLE=ORGANIZER;STATUS=NEEDS ACTION;RSVP=NO;EXPECT=FYI;X-CN=Shannon Carver;ENCODING=QUOTED-PRINTABLE:=\n"
                + "Shannon.Carver=40mistermind.com\n"
                + "LAST-MODIFIED:20080908T081513Z\n"
                + "PRIORITY:0\n"
                + "X-SYMBIAN-LUID:172\n"
                + "DESCRIPTION:\n"
                + "RRULE:\n"
                + "ORGANIZER:\n"
                + "AALARM:;;;\n"
                + "LOCATION:\n"
                + "END:VEVENT\n"
                + "END:VCALENDAR";


        Calendar calendar = UtilitySyncSource.webCalendar2Calendar(vcal, VCAL_FORMAT, TimeZone.getTimeZone("GMT"), "UTF-8");

        // there is the FOUNDATION converter that
        assertEquals("0", calendar.getEvent().getPriority().getPropertyValueAsString());

        JsonItem<Event> item = new JsonItem<Event>();
        item.setContentType("type");
        item.setKey("0");
        item.setState("A");
        item.setItem(calendar.getEvent());

        AppointmentConverter converter = new AppointmentConverter();

        String jsonResult = converter.toJSON(item);

        JSONObject jsonRoot = JSONObject.fromObject(jsonResult);
        JSONObject jsonData = jsonRoot.getJSONObject("data");
        JSONObject jsonItem = jsonData.getJSONObject("item");


        String imp = Utility.getJsonValue(jsonItem, "importance");

        // high = 2
        // normal = 1
        // low = 0
        assertEquals("2", imp);

    }

    //---------------------------------------------------------- Private Methods
    /**
     * 
     * 
     */
    private CalendarSyncSource createAppointmentSyncSource() {

        CalendarSyncSource source = new CalendarSyncSource();

        SyncSourceInfo ssi = new SyncSourceInfo();
        ContentType[] contentType = new ContentType[1];
        contentType[0] = new ContentType("text/x-s4j-sife", "1.0");
        ssi.setSupportedTypes(contentType);
        source.setInfo(ssi);

        source.setEntityType(Event.class);

        return source;
    }

    /**
     * 
     */
    private SyncContext createContext() {
        // Cred credentials = new Cred(authentication);
        JsonUser user = new JsonUser("pippo", "pippo");
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

    /**
     * Create a fake device and a sync context, then tests the smart sync
     * source by providing a preferred appointment format into the device
     * capabilities.
     *
     * @throws net.sf.json.JSONException
     */
    public void test_findRXContentType() {

        CalendarSyncSource source = new CalendarSyncSource();

        DevInf deviceInfo = new DevInf(
                new VerDTD("1.2"),
                "Funambol", "Funambol Outlook Sync Client", "",
                "", "", "",
                "", "",
                true, true, true);
        Sync4jPrincipal principal = Sync4jPrincipal.createPrincipal(
                "fakeuser", "fakeuser_fakedevice");
        principal.getDevice().getCapabilities().setDevInf(deviceInfo);

        SyncContext context = new SyncContext(
                principal, 200, null, "localhost", 2);

        CTInfo[] rxs = new CTInfo[]{
            new CTInfo(CalendarSyncSource.TYPE[CalendarSyncSource.SIFE_FORMAT], "1.0"),
            new CTInfo(CalendarSyncSource.TYPE[CalendarSyncSource.VCAL_FORMAT], "1.0")
        };
        CTInfo[] txs = rxs;

        DataStore dataStorePreferSifContact = new DataStore(
                new SourceRef("appointment"), "appointment", 0,
                rxs[0], rxs, txs[0], txs,
                new DSMem(false),
                new SyncCap());

        principal.getDevice().getCapabilities().getDevInf().setDataStores(
                new DataStore[]{dataStorePreferSifContact});

        String actual = source.findRXContentType(context);
        assertEquals(CalendarSyncSource.TYPE_ANYSIF, actual);

        DataStore dataStorePreferVCard = new DataStore(
                new SourceRef("appointment"), "appointment", 0,
                rxs[1], rxs, txs[1], txs,
                new DSMem(false),
                new SyncCap());

        principal.getDevice().getCapabilities().getDevInf().setDataStores(
                new DataStore[]{dataStorePreferVCard});

        actual = source.findRXContentType(context);
        assertEquals(CalendarSyncSource.TYPE[CalendarSyncSource.VCAL_FORMAT], actual);
    }
}
