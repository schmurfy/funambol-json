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
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.converter.ContactToVcard;
import com.funambol.common.pim.converter.VCalendarConverter;
import com.funambol.common.pim.converter.VComponentWriter;
import com.funambol.common.pim.icalendar.ICalendarParser;
import com.funambol.common.pim.model.VCalendar;
import com.funambol.common.pim.vcard.VcardParser;
import com.funambol.common.pim.xvcalendar.XVCalendarParser;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;
import com.funambol.json.util.Utility;
import com.funambol.server.config.Configuration;
import java.io.ByteArrayInputStream;
import java.util.TimeZone;

public class UtilitySyncSource {

    protected static final FunambolLogger log = FunambolLoggerFactory.getLogger(Utility.LOG_NAME);

    public static final String VCAL_FORMAT = "text/x-vcalendar"; 
    public static final String ICAL_FORMAT = "text/calendar"; 
    
    
    
    /**
     *
     * 
     * @param text
     * @param vCalType
     * @return
     * @throws com.funambol.ox.exception.EntityException
     */
    public static Calendar webCalendar2Calendar(String text,
                                         String vCalType,
                                         TimeZone deviceTimeZone,
                                         String deviceCharset)
      throws Exception {

        try {

            ByteArrayInputStream buffer =
                    new ByteArrayInputStream(text.getBytes());

            VCalendar vcalendar;
            String version;

            if (Configuration.getConfiguration().isDebugMode()) {
                if (log.isTraceEnabled()) {
                    StringBuilder sb = new StringBuilder(text.length() + 60);
                    sb.append("Converting: ").append(vCalType).append(" => Calendar ").append("\nINPUT = {").append(text).append('}');
                    log.trace(sb.toString());
                }
            }

            if (vCalType.equals(VCAL_FORMAT)) { // VCAL_FORMAT

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
            if (retrievedVersion == null) {
                if (log.isTraceEnabled()) {
                    log.trace("No version property was found in the vCal/iCal " + "data: version set to " + version);
                }
            } else if (!retrievedVersion.equals(version)) {
                if (log.isTraceEnabled()) {
                    log.trace("The version in the data was " + retrievedVersion + " but it's been changed to " + version);
                }
            }

            VCalendarConverter vcf = new VCalendarConverter(deviceTimeZone, deviceCharset);

            Calendar c = vcf.vcalendar2calendar(vcalendar);

            if (log.isTraceEnabled()) {
                log.trace("Conversion done.");
            }

            return c;

        } catch (Exception e) {
            throw new Exception("Error converting " + vCalType + " to Calendar. ", e);
        }
    }
    /**
     *
     * @param calendar
     * @param vCalType
     * @param deviceTimeZone
     * @param deviceCharset
     * @return
     * @throws com.funambol.ox.exception.EntityException
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

            if (log.isTraceEnabled()) {
                log.trace("Converting: Calendar => " + vCalType);
            }

            if (vCalType.equals(VCAL_FORMAT)) { // VCAL_FORMAT

                vcalendar =
                        vcf.calendar2vcalendar(calendar, true); // text/x-vcalendar

            } else { // ICAL_FORMAT

                vcalendar =
                        vcf.calendar2vcalendar(calendar, false); // text/calendar
            }

            VComponentWriter writer =
                    new VComponentWriter(VComponentWriter.NO_FOLDING);
            vcal = writer.toString(vcalendar);

            if (Configuration.getConfiguration().isDebugMode()) {
                if (log.isTraceEnabled()) {
                    log.trace("OUTPUT = {" + vcal + "}. Conversion done.");
                }
            }

            return vcal;

        } catch (Exception e) {
            throw new Exception("Error converting Calendar to " + vCalType, e);
        }
    }

     /**
     *
     * @param contact
     * @return
     * @throws java.lang.Exception
     */
    public static String contact2vcard(Contact contact, TimeZone deviceTimezone,String deviceChartset) throws Exception {

        if (log.isTraceEnabled()) {
            log.trace("Converting: Contact => VCARD");
        }
        String vcard = null;
        try {
            ContactToVcard c2vc = new ContactToVcard(deviceTimezone, deviceChartset);
            vcard = c2vc.convert(contact);
            if (Configuration.getConfiguration().isDebugMode()) {
                if (log.isTraceEnabled()) {
                    log.trace("OUTPUT = {" + vcard + "}. Conversion done.");
                }
            }
        } catch (Exception e) {
            throw new Exception("Error converting Contact to VCARD. ", e);
        }
        return vcard;
    }
/**
     *
     * @param vcard
     * @return
     * @throws java.lang.Exception
     */
     public static Contact vcard2Contact(String vcard) throws Exception {

        if (Configuration.getConfiguration().isDebugMode()) {
            if (log.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder(vcard.length() + 60);
                sb.append("Converting: VCARD => JsonContact").append("\nINPUT = {").append(vcard).append('}');
                log.trace(sb.toString());
            }
        }

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

        if (log.isTraceEnabled()) {
            log.trace("Conversion done.");
        }
        return contact;
    }
    /**
     * returns the time of the starting sync session
     * 
     * @return since the time of the starting sync session
     */
    /*
    public static long getSince(SyncContext syncContext){
        long since = 0;
        
        // this is not an error
        Timestamp sinceValue = syncContext.getTo();
        
        if (sinceValue != null) {
            since = sinceValue.getTime();
        } else {
            //@todo to be verified 
            since = System.currentTimeMillis() - 1000;
        }
        return since;
    }
    */
    
    /**
     * 
     * @param context
     * @throws com.funambol.framework.engine.source.SyncSourceException
     */
   /*
   public static DeviceConfiguration getDeviceConfiguration(SyncContext context) throws SyncSourceException {
        
       DeviceConfiguration deviceConfiguration  = new DeviceConfiguration();
    
       try {
          TimeZone deviceTimeZone = null;
          String deviceTimeZoneDescription = null;
          Sync4jDevice device    = context.getPrincipal().getDevice();
          String       timezone  = device.getTimeZone()  ;
          if (device.getConvertDate()) {
              if (timezone != null && timezone.length() > 0) {
                  deviceTimeZoneDescription = timezone;
                  deviceTimeZone = TimeZone.getTimeZone(deviceTimeZoneDescription);
              }
          }
           
          deviceConfiguration.setDeviceCharset(device.getCharset());
          deviceConfiguration.setDeviceTimeZone(deviceTimeZone);
           
       } catch(Exception e) {
           throw new SyncSourceException("Error settings the device information." + e.getMessage());
       }
       
       return deviceConfiguration;
       
   }
   */
   
    
}
