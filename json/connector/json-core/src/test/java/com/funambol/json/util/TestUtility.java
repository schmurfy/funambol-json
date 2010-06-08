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
package com.funambol.json.util;

import java.io.ByteArrayInputStream;
import java.util.TimeZone;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.Task;
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.converter.BaseConverter;
import com.funambol.common.pim.converter.CalendarToSIFE;
import com.funambol.common.pim.converter.ContactToVcard;
import com.funambol.common.pim.converter.ContactToSIFC;
import com.funambol.common.pim.converter.TaskToSIFT;
import com.funambol.common.pim.converter.VCalendarConverter;
import com.funambol.common.pim.converter.VComponentWriter;
import com.funambol.common.pim.icalendar.ICalendarParser;
import com.funambol.common.pim.model.VCalendar;
import com.funambol.common.pim.sif.SIFCParser;
import com.funambol.common.pim.sif.SIFCalendarParser;
import com.funambol.common.pim.vcard.VcardParser;
import com.funambol.common.pim.xvcalendar.XVCalendarParser;
import java.util.ArrayList;
import java.util.Iterator;
import net.sf.json.JSONObject;
import net.sf.json.test.JSONAssert;

/**
 * This class contains useful methods for testing purpose.
 *
 * @version $Id: TestUtility.java 51920 2010-06-04 05:10:49Z gazzaniga $
 */
public class TestUtility  {

    // ------------------------------------------------------------- Public data
    public static final int SIFE_FORMAT = 0; // To be used as index for SIF-Event
    public static final int SIFT_FORMAT = 1; // To be used as index for SIF-Task
    public static final int VCAL_FORMAT = 2; // To be used as index for VCal
    public static final int ICAL_FORMAT = 3; // To be used as index for ICal

    public static final String[] TYPE = {
        "text/x-s4j-sife", // SIF-Event
        "text/x-s4j-sift", // SIF-Task
        "text/x-vcalendar", // VCal
        "text/calendar",    // ICal
    };

    // ---------------------------------------------------------- Public methods
    public static String getType(int format) {
        return TYPE[format];
    }

    /**
     *
     * @param xml
     * @return
     * @throws java.lang.Exception
     */
    public static Calendar sif2Calendar(String xml)throws Exception {
        ByteArrayInputStream buffer = null;
        Calendar calendar = null;
        try {
            calendar = new Calendar();
            buffer = new ByteArrayInputStream(xml.getBytes());
            if ((xml.getBytes()).length > 0) {
                SIFCalendarParser parser = new SIFCalendarParser(buffer);
                calendar = parser.parse();
            }
        } catch (Exception e){
            throw new Exception("Error converting Calendar. ", e);
        }

        return calendar;
    }

    /**
     *
     * @param calendar
     * @param sifType
     * @throws com.funambol.ox.exception.EntityException
     * @return
     */
    public static String calendar2sif(Calendar calendar) throws Exception {

        String xml = null;
        Object thing;

        try {
            BaseConverter c2xml = new CalendarToSIFE(null, null);
            thing = calendar;
            xml = c2xml.convert(thing);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return xml;
    }

    public String calendar2sif(Calendar calendar,
                               String sifType,
                               TimeZone deviceTimeZone,
                               String deviceCharset)
    throws Exception {

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

        } catch (Exception e) {
            throw new Exception("Error converting Calendar to " + sifType, e);
        }
        return xml;
    }

    public static String task2SifT(Task task)
      throws Exception {

        String xml = null;
        Object thing;

        try {
            BaseConverter c2xml = new TaskToSIFT(null, null);
            thing = task;
            xml = c2xml.convert(thing);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return xml;
    }
    
    /**
     * Converts the given model object to a sif representation.
     * @param contact the contact we want to convert
     * @throws Exception if any error occurs.
     * @return the sif representation of the given contact
     */
    public static String contact2sif(Contact contact) throws Exception {

        String xml = null;
        Object thing;

        try {
            BaseConverter c2xml = new ContactToSIFC(null, null);
            thing = contact;
            xml = c2xml.convert(thing);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return xml;
    }

    /**
     *
     *
     * @param text
     * @param vCalType
     * @param deviceTimeZone 
     * @param deviceCharset 
     * @return
     * @throws Exception
     */
    public static Calendar webCalendar2Calendar(String text,
                                                String vCalType,
                                                TimeZone deviceTimeZone,
                                                String deviceCharset)
    throws Exception {


        Calendar c = null;

        try {
            ByteArrayInputStream buffer = new ByteArrayInputStream(text.getBytes());
            String version;
            VCalendar vcalendar;

            if (vCalType.equals(TYPE[VCAL_FORMAT])) { // VCAL_FORMAT
                XVCalendarParser parser = new XVCalendarParser(buffer);
                vcalendar = (VCalendar) parser.XVCalendar();
                version = "1.0";
            } else { // ICAL_FORMAT
                ICalendarParser parser = new ICalendarParser(buffer);
                vcalendar = (VCalendar) parser.ICalendar();
                version = "2.0";
            }
            String retrievedVersion = null;
            if (vcalendar.getProperty("VERSION") != null) {
                retrievedVersion = vcalendar.getProperty("VERSION").getValue();
            }
            vcalendar.addProperty("VERSION", version);
            VCalendarConverter vcf = new VCalendarConverter(deviceTimeZone, deviceCharset);

            c = vcf.vcalendar2calendar(vcalendar);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return  c;
    }

    /**
     *
     * @param calendar
     * @param vCalType
     * @param deviceTimeZone
     * @param deviceCharset
     * @return
     */
    public static String calendar2webCalendar(Calendar calendar,
                                              String vCalType,
                                              TimeZone deviceTimeZone,
                                              String deviceCharset)
    throws Exception {

        try {

            VCalendarConverter vcf = new VCalendarConverter(deviceTimeZone, deviceCharset);

            VCalendar vcalendar;
            String vcal;

            //"text/x-vcalendar", // VCal
            //"text/calendar",    // ICal
            if (vCalType.equals(TYPE[VCAL_FORMAT])) { // VCAL_FORMAT
                vcalendar = vcf.calendar2vcalendar(calendar, true); // text/x-vcalendar
            } else { // ICAL_FORMAT
                vcalendar = vcf.calendar2vcalendar(calendar, false); // text/calendar
            }
            VComponentWriter writer = new VComponentWriter(VComponentWriter.NO_FOLDING);
            vcal = writer.toString(vcalendar);

            return vcal;

        } catch (Exception e) {
            throw new Exception("Error converting Calendar to " + vCalType, e);
        }
    }

    // ------------------------------------------------------ Contact converters
    /**
     * Converts vcard into Contact object.
     *
     * @param vcard the String vcard to convert
     * @return Contact object
     * @throws java.lang.Exception
     */
    public static Contact vcard2Contact(String vcard) throws Exception {

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

        return contact;
    }

    /**
     * Converts a Contact object into vcard string.
     *
     * @param contact The Contact object to convert
     * @return a String containing the vcard
     * @throws Exception
     */
    public static String contact2vcard(Contact contact) throws Exception {

        String vcard = null;
        try {
            ContactToVcard c2vc = new ContactToVcard(null, null);
            vcard = c2vc.convert(contact);
        } catch (Exception e) {
            throw new Exception("Error converting Contact to VCARD. ", e);
        }
        return vcard;
    }

    /**
     * Converts SIF-C string into Contact object.
     *
     * @param sifc the SIF-C to convert
     * @return Contact object
     * @throws java.lang.Exception
     */
    public static Contact sif2Contact(String sifc) throws Exception {

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

        return contact;
    }

    /**
     * The JSONAssert.assertEquals() doesn't show what was wrong in the
     * assertion.
     * This method check that two JSON objects are equals and if not stamps the
     * two complete objects and the differences between them.
     *
     * @param expected the first JSON object
     * @param actual the second JSON object
     */
    public static void JSONAssert_assertEquals(JSONObject expected,
                                               JSONObject actual)
    {
        if (actual.equals(expected)) {
            return;
        }

        ArrayList<String> diff = new ArrayList<String>();
        JSONObject_compareRecursive(expected, actual, diff);

        for (Iterator<String> it = expected.keys(); it.hasNext();) {
            String key = it.next();
            if (!actual.containsKey(key)) {
                diff.add("<<<< " + key);
            }
        }

        for (int i = 0; i < diff.size(); ++i)
            System.out.println(diff.get(i));
        System.out.println("object1 = " + expected.toString());
        System.out.println("object2 = " + actual.toString());

        JSONAssert.assertEquals(expected, actual);
    }

    private static void JSONObject_compareRecursive(JSONObject expected,
                                                    JSONObject actual,
                                                    ArrayList<String> diff)
    {
        for (Iterator<String> it = actual.keys(); it.hasNext();) {
            String key = it.next();
            if (!expected.containsKey(key)) {
                diff.add(">>>> " + key);
                continue;
            }
            JSONObject actualChild = actual.optJSONObject(key);
            if (actualChild != null) {
                JSONObject expectedChild = expected.optJSONObject(key);
                if (expectedChild != null) {
                    JSONObject_compareRecursive(expectedChild, actualChild,
                                                diff);
                    continue;
                }
            }
            if (!actual.get(key).equals(expected.get(key))) {
                String actualValue = actual.get(key).toString();
                actualValue = actualValue.replace("\n", "\\n");
                String expectedValue = expected.get(key).toString();
                expectedValue = expectedValue.replace("\n", "\\n");
                diff.add("!!!! " + key + " (\"" + actualValue
                         + "\" != \"" + expectedValue + "\")");
            }
        }
    }

}
