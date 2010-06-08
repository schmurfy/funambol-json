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
package com.funambol.json.manager;


import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.Event;
import com.funambol.framework.tools.merge.MergeResult;
import net.sf.json.JSONException;
import com.funambol.json.abstractServlet.AbstractHttpTransportTest;
import com.funambol.json.abstractServlet.JsonServlet;
import com.funambol.json.converter.AppointmentConverter;
import com.funambol.json.dao.JsonDAO;
import com.funambol.json.dao.JsonDAOImpl;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.engine.source.UtilitySyncSource;
import java.util.TimeZone;
import junitx.util.PrivateAccessor;

public class AppointmentManagerTest extends AbstractHttpTransportTest {

    JsonAppointmentManager manager = null;
    
    private String sessionID = "asdasdasdasd";

    
    private static JsonServlet jsonServlet = new JsonServlet();
    
    //-------------------------------------------------------------- Constructor
    
    public AppointmentManagerTest()    {
        super(jsonServlet);
        try {
            JsonDAO dao = new JsonDAOImpl("contact");
            AppointmentConverter converter = new AppointmentConverter();
            manager = new JsonAppointmentManager(dao, converter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //---------------------------------------------------- Public Methods (test)
    
    /**
     *
     * supppose that from the Backend we have a vcal 2.0 and
     * from the client we have a vcal 1.0
     * 
     * @throws net.sf.json.JSONException
     */
    public void test_mergeRFC_1() throws JSONException {

        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        String   dc = "UTF-8";


        String clientItem =
                "BEGIN:VCALENDAR\n"+
                "VERSION:1.0\n"+
                "TZ:-0500\n"+
                "DAYLIGHT:TRUE;-0400;20090308T020000;20091101T020000;;\n"+
                "BEGIN:VEVENT\n"+
                "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\n"+
                "X-FUNAMBOL-ALLDAY:0\n"+
                "DTSTART:20091117T130000Z\n"+
                "DTEND:20091117T133000Z\n"+
                "CATEGORIES:\n"+
                "DESCRIPTION;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=0D=0A\n"+
                "LOCATION:Pavia\n"+
                "PRIORITY:1\n"+
                "STATUS:0\n"+
                "SUMMARY:Client2\n"+
                "CLASS:PUBLIC\n"+
                "AALARM:20091117T124500Z;;0;\n"+
                "RRULE:D1 #3\n"+
                "EXDATE:\n"+
                "RDATE:\n"+
                "END:VEVENT\n"+
                "END:VCALENDAR\n";

        String serverItem =
                "BEGIN:VCALENDAR\n"+
                "VERSION:2.0\n"+
                "BEGIN:VTIMEZONE\n"+
                "TZID:America/New_York\n"+
                "BEGIN:DAYLIGHT\n"+
                "DTSTART:20090308T020000\n"+
                "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=+2SU;BYMONTH=3\n"+
                "TZOFFSETFROM:-0500\n"+
                "TZOFFSETTO:-0400\n"+
                "TZNAME:America/New_York\n"+
                "END:DAYLIGHT\n"+
                "BEGIN:STANDARD\n"+
                "DTSTART:20091101T020000\n"+
                "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=+1SU;BYMONTH=11\n"+
                "TZOFFSETFROM:-0400\n"+
                "TZOFFSETTO:-0500\n"+
                "TZNAME:America/New_York\n"+
                "END:STANDARD\n"+
                "END:VTIMEZONE\n"+
                "BEGIN:VEVENT\n"+
                "SUMMARY:Client2\n"+
                "DESCRIPTION:\\N\n"+
                "LOCATION:\n"+
                "CATEGORIES:\n"+
                "CLASS:PUBLIC\n"+
                "DTSTART;TZID=America/New_York:20091117T080000\n"+
                "DTEND;TZID=America/New_York:20091117T083000\n"+
                "PRIORITY:1\n"+
                "STATUS:0\n"+
                "RRULE;TZID=America/New_York:FREQ=DAILY;INTERVAL=1;COUNT=3\n"+
                "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\n"+
                "X-FUNAMBOL-ALLDAY:0\n"+
                "BEGIN:VALARM\n"+
                "TRIGGER;TZID=America/New_York;VALUE=DATE-TIME:20091117T074500\n"+
                "REPEAT:0\n"+
                "END:VALARM\n"+
                "END:VEVENT\n"+
                "END:VCALENDAR\n";


        try {

            String rfcType = UtilitySyncSource.VCAL_FORMAT;

            Calendar clientCal =
                    UtilitySyncSource.webCalendar2Calendar(clientItem, rfcType, tz, dc);
            Calendar serverCal =
                    UtilitySyncSource.webCalendar2Calendar(serverItem, rfcType, tz, dc);

            JsonItem<Event> serverEventItem = new JsonItem<Event>();
            serverEventItem.setKey("appointment-1");
            serverEventItem.setContentType(rfcType);
            serverEventItem.setState("N");
            serverEventItem.setItem(serverCal.getEvent());

            JsonItem<Event> clientEventItem = new JsonItem<Event>();
            clientEventItem.setKey("appointment-1");
            clientEventItem.setContentType(rfcType);
            clientEventItem.setState("N");
            clientEventItem.setItem(clientCal.getEvent());

            boolean vcardIcalBackend = true;
            boolean vcalFormat       = false; // -> ical


            MergeResult mergeResult = clientEventItem.getItem().merge(serverEventItem.getItem());

            //System.out.println("mergeResult " + mergeResult);

            if (mergeResult.isSetBRequired()) {

                Object objRFC = PrivateAccessor.invoke(manager,
                                                       "setItemForB" ,
                                                       new Class[]
                {Event.class, boolean.class, boolean.class, String.class, TimeZone.class, String.class },
                                                       new Object[]
                {serverEventItem.getItem(), vcardIcalBackend, vcalFormat, rfcType, tz, dc});

                //System.out.println("objRFC " + objRFC);
                int version = ((String)objRFC).indexOf("VERSION:2.0");

                //System.out.println("objRFC version index: " + version);
                assertEquals(17, version);
            }

        } catch (Throwable t){
            fail("Error: " + t.getMessage());
        } 
        
    } 
    
    /**
     *
     * supppose that from the Backend we have a vcal 1.0 and
     * from the client we have a vcal 1.0
     *
     * @throws net.sf.json.JSONException
     */
    public void test_mergeRFC_2() throws JSONException {

        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        String   dc = "UTF-8";

        String clientItem =
                "BEGIN:VCALENDAR\n"+
                "VERSION:1.0\n"+
                "TZ:-0500\n"+
                "DAYLIGHT:TRUE;-0400;20090308T020000;20091101T020000;;\n"+
                "BEGIN:VEVENT\n"+
                "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\n"+
                "X-FUNAMBOL-ALLDAY:0\n"+
                "DTSTART:20091117T130000Z\n"+
                "DTEND:20091117T133000Z\n"+
                "CATEGORIES:\n"+
                "DESCRIPTION;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=0D=0A\n"+
                "LOCATION:Pavia\n"+
                "PRIORITY:1\n"+
                "STATUS:0\n"+
                "SUMMARY:Client2\n"+
                "CLASS:PUBLIC\n"+
                "AALARM:20091117T124500Z;;0;\n"+
                "RRULE:D1 #3\n"+
                "EXDATE:\n"+
                "RDATE:\n"+
                "END:VEVENT\n"+
                "END:VCALENDAR\n";

        String serverItem =
                "BEGIN:VCALENDAR\n"+
                "VERSION:1.0\n"+
                "TZ:-0500\n"+
                "DAYLIGHT:TRUE;-0400;20090308T020000;20091101T020000;;\n"+
                "BEGIN:VEVENT\n"+
                "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\n"+
                "X-FUNAMBOL-ALLDAY:0\n"+
                "DTSTART:20091117T130000Z\n"+
                "DTEND:20091117T133000Z\n"+
                "CATEGORIES:\n"+
                "DESCRIPTION;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=0D=0A\n"+
                "LOCATION:\n"+
                "PRIORITY:1\n"+
                "STATUS:0\n"+
                "SUMMARY:Client2\n"+
                "CLASS:PUBLIC\n"+
                "AALARM:20091117T124500Z;;0;\n"+
                "RRULE:D1 #3\n"+
                "EXDATE:\n"+
                "RDATE:\n"+
                "END:VEVENT\n"+
                "END:VCALENDAR\n";


        try {

            String rfcType = UtilitySyncSource.VCAL_FORMAT;

            Calendar clientCal =
                    UtilitySyncSource.webCalendar2Calendar(clientItem, rfcType, tz, dc);
            Calendar serverCal =
                    UtilitySyncSource.webCalendar2Calendar(serverItem, rfcType, tz, dc);

            JsonItem<Event> serverEventItem = new JsonItem<Event>();
            serverEventItem.setKey("appointment-1");
            serverEventItem.setContentType(rfcType);
            serverEventItem.setState("N");
            serverEventItem.setItem(serverCal.getEvent());

            JsonItem<Event> clientEventItem = new JsonItem<Event>();
            clientEventItem.setKey("appointment-1");
            clientEventItem.setContentType(rfcType);
            clientEventItem.setState("N");
            clientEventItem.setItem(clientCal.getEvent());

            boolean vcardIcalBackend = true;
            boolean vcalFormat       = true; // -> vcal

            MergeResult mergeResult = clientEventItem.getItem().merge(serverEventItem.getItem());

            //System.out.println("mergeResult " + mergeResult);

            if (mergeResult.isSetBRequired()) {

                Object objRFC = PrivateAccessor.invoke(manager,
                                                       "setItemForB" ,
                                                       new Class[]
                {Event.class, boolean.class, boolean.class, String.class, TimeZone.class, String.class },
                                                       new Object[]
                {serverEventItem.getItem(), vcardIcalBackend, vcalFormat, rfcType, tz, dc});

                //System.out.println("objRFC " + objRFC);
                int version = ((String)objRFC).indexOf("VERSION:1.0");

                //System.out.println("objRFC version index: " + version);
                assertEquals(17, version);
            }

        } catch (Throwable t){
            fail("Error: " + t.getMessage());
        }

    }


    /**
     *
     * supppose that from the Backend we have a vcal 2.0 and
     * from the client we have a vcal 1.0
     * Test an incorrect scenario
     *
     * @throws net.sf.json.JSONException
     */
    public void test_mergeRFC_3() throws JSONException {

        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        String   dc = "UTF-8";


        String clientItem =
                "BEGIN:VCALENDAR\n"+
                "VERSION:1.0\n"+
                "TZ:-0500\n"+
                "DAYLIGHT:TRUE;-0400;20090308T020000;20091101T020000;;\n"+
                "BEGIN:VEVENT\n"+
                "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\n"+
                "X-FUNAMBOL-ALLDAY:0\n"+
                "DTSTART:20091117T130000Z\n"+
                "DTEND:20091117T133000Z\n"+
                "CATEGORIES:\n"+
                "DESCRIPTION;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=0D=0A\n"+
                "LOCATION:Pavia\n"+
                "PRIORITY:1\n"+
                "STATUS:0\n"+
                "SUMMARY:Client2\n"+
                "CLASS:PUBLIC\n"+
                "AALARM:20091117T124500Z;;0;\n"+
                "RRULE:D1 #3\n"+
                "EXDATE:\n"+
                "RDATE:\n"+
                "END:VEVENT\n"+
                "END:VCALENDAR\n";

        String serverItem =
                "BEGIN:VCALENDAR\n"+
                "VERSION:2.0\n"+
                "BEGIN:VTIMEZONE\n"+
                "TZID:America/New_York\n"+
                "BEGIN:DAYLIGHT\n"+
                "DTSTART:20090308T020000\n"+
                "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=+2SU;BYMONTH=3\n"+
                "TZOFFSETFROM:-0500\n"+
                "TZOFFSETTO:-0400\n"+
                "TZNAME:America/New_York\n"+
                "END:DAYLIGHT\n"+
                "BEGIN:STANDARD\n"+
                "DTSTART:20091101T020000\n"+
                "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=+1SU;BYMONTH=11\n"+
                "TZOFFSETFROM:-0400\n"+
                "TZOFFSETTO:-0500\n"+
                "TZNAME:America/New_York\n"+
                "END:STANDARD\n"+
                "END:VTIMEZONE\n"+
                "BEGIN:VEVENT\n"+
                "SUMMARY:Client2\n"+
                "DESCRIPTION:\\N\n"+
                "LOCATION:\n"+
                "CATEGORIES:\n"+
                "CLASS:PUBLIC\n"+
                "DTSTART;TZID=America/New_York:20091117T080000\n"+
                "DTEND;TZID=America/New_York:20091117T083000\n"+
                "PRIORITY:1\n"+
                "STATUS:0\n"+
                "RRULE;TZID=America/New_York:FREQ=DAILY;INTERVAL=1;COUNT=3\n"+
                "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\n"+
                "X-FUNAMBOL-ALLDAY:0\n"+
                "BEGIN:VALARM\n"+
                "TRIGGER;TZID=America/New_York;VALUE=DATE-TIME:20091117T074500\n"+
                "REPEAT:0\n"+
                "END:VALARM\n"+
                "END:VEVENT\n"+
                "END:VCALENDAR\n";


        try {

            String rfcType = UtilitySyncSource.VCAL_FORMAT;

            Calendar clientCal =
                    UtilitySyncSource.webCalendar2Calendar(clientItem, rfcType, tz, dc);
            Calendar serverCal =
                    UtilitySyncSource.webCalendar2Calendar(serverItem, rfcType, tz, dc);

            JsonItem<Event> serverEventItem = new JsonItem<Event>();
            serverEventItem.setKey("appointment-1");
            serverEventItem.setContentType(rfcType);
            serverEventItem.setState("N");
            serverEventItem.setItem(serverCal.getEvent());

            JsonItem<Event> clientEventItem = new JsonItem<Event>();
            clientEventItem.setKey("appointment-1");
            clientEventItem.setContentType(rfcType);
            clientEventItem.setState("N");
            clientEventItem.setItem(clientCal.getEvent());

            boolean vcardIcalBackend = true;
            boolean vcalFormat       = true; // -> the backend should receive the ical

            MergeResult mergeResult = clientEventItem.getItem().merge(serverEventItem.getItem());

            //System.out.println("mergeResult " + mergeResult);

            if (mergeResult.isSetBRequired()) {

                Object objRFC = PrivateAccessor.invoke(manager,
                                                       "setItemForB" ,
                                                       new Class[]
                {Event.class, boolean.class, boolean.class, String.class, TimeZone.class, String.class },
                                                       new Object[]
                {serverEventItem.getItem(), vcardIcalBackend, vcalFormat, rfcType, tz, dc});

                //System.out.println("objRFC " + objRFC);
                int version = ((String)objRFC).indexOf("VERSION:2.0");

                //System.out.println("objRFC version index: " + version);
                assertEquals(-1, version);
            }

        } catch (Throwable t){
            fail("Error: " + t.getMessage());
        }

    }

    
    
}
