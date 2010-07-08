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

package com.funambol.json.gui.html.conversion;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.CalendarContent;
import com.funambol.common.pim.calendar.Event;
import com.funambol.common.pim.calendar.ExceptionToRecurrenceRule;
import com.funambol.common.pim.calendar.Reminder;
import com.funambol.common.pim.calendar.Task;
import com.funambol.common.pim.converter.VCalendarConverter;
import com.funambol.common.pim.converter.VComponentWriter;
import com.funambol.common.pim.icalendar.ICalendarParser;
import com.funambol.common.pim.model.VCalendar;
import com.funambol.common.pim.sif.SIFCalendarParser;
import com.funambol.common.pim.xvcalendar.XVCalendarParser;
import com.funambol.json.domain.JsonAppointmentModel;
import com.funambol.json.domain.JsonContactModel;
import com.funambol.json.domain.JsonTaskModel;
import com.funambol.json.exception.JsonConversionException;
import com.funambol.json.gui.html.NameValuePair;
import com.funambol.json.util.Utility;
import com.funambol.json.utility.Definitions;
import com.funambol.json.utility.FakeDevice;
import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;


/**
 * Converter for Calendar objects
 * 
 * @version $Id$
 */
public class CalendarConverter implements Converter {
    public static final String JSON_EXTENDED = "application/json-appointment";
    public static final String JSON_ICAL     = "application/json-ical";
    public static final String JSON_VCAL     = "application/json-vcal";

    private final static Logger log = Logger.getLogger(Definitions.LOG_NAME);
    
    public static final String VCAL = "VCAL";
    public static final String ICAL = "ICAL";
    
    public static final String I_VCAL_FORMAT =VCAL;
    public static final String I_VCAL_LABEL  ="VCal";
    
    public static final String I_ICAL_FORMAT =ICAL;
    public static final String I_ICAL_LABEL  ="ICal";
    
    public static final String O_VCAL_FORMAT =VCAL;
    public static final String O_VCAL_LABEL  ="VCal";

    public static final String O_ICAL_FORMAT =ICAL;
    public static final String O_ICAL_LABEL  ="ICal";

  
    //    outputFormats.put(JsonServlet.APPOINTMENT, NameValuePair.parseFromStrings(RFC_ICAL_NOTATION_LABEL,RFC_ICAL_NOTATION,RFC_VCAL_NOTATION_LABEL,RFC_VCAL_NOTATION,EXTENDED_NOTATION_LABEL,EXTENDED_NOTATION));
    //    outputFormats.put(JsonServlet.TASK,        NameValuePair.parseFromStrings(RFC_ICAL_NOTATION_LABEL,RFC_ICAL_NOTATION,RFC_VCAL_NOTATION_LABEL,RFC_VCAL_NOTATION,EXTENDED_NOTATION_LABEL,EXTENDED_NOTATION));
    
    
    private final static NameValuePair[] inputFormat    = NameValuePair.parseFromStrings(I_RAW_LABEL,I_RAW_FORMAT,I_SIF_LABEL,I_SIF_FORMAT,I_VCAL_LABEL,I_VCAL_FORMAT,I_ICAL_LABEL,I_ICAL_FORMAT);
    private final static NameValuePair[] outputFormat   = NameValuePair.parseFromStrings(O_ICAL_LABEL,O_ICAL_FORMAT,O_VCAL_LABEL,O_VCAL_FORMAT,O_EXTENDED_LABEL,O_EXTENDED_FORMAT);//O_EXTENDED_LABEL,O_EXTENDED_FORMAT);
  
    
    public String applyConversion(String inputFormat, String outputFormat, String inputString) throws ConversionException {
        log.debug("Converting form ["+inputFormat+"] to ["+outputFormat+"].");
        // conversion from sif
        if(I_SIF_FORMAT.equals(inputFormat)) {
           Calendar calendar = sif2Calendar(inputString);
            if(O_VCAL_FORMAT.equals(outputFormat)) {
                String vcal = calendar2vcal(calendar,FakeDevice.getTimezone(),FakeDevice.getCharset());
                return toRFC("vcal",vcal,"",STATE_NEW,JSON_VCAL);
            } else if(O_ICAL_FORMAT.equals(outputFormat)) {
                String vcal = calendar2ical(calendar,FakeDevice.getTimezone(),FakeDevice.getCharset());
                return toRFC("vcal",vcal,"",STATE_NEW,JSON_ICAL);
            } else if(O_EXTENDED_FORMAT.equals(outputFormat)) {
                return toJSON(calendar, "", STATE_NEW,JSON_EXTENDED);
            }
        } else if(I_ICAL_FORMAT.equals(inputFormat) || I_VCAL_FORMAT.equals(inputFormat) ) {
                return vcal2json(inputString,inputFormat,outputFormat);
        }
        
        
        
        return null;
    }

    private boolean isConversionRequired(String inputFormat, String outputFormat) throws ConversionException {
        if(inputFormat!=null && outputFormat!=null) {
            return !inputFormat.equals(outputFormat);
        }
        throw new ConversionException("Input/Output format not valid.");
    }
    
     private String vcal2json(String inputString, String inputFormat, String outputFormat) throws ConversionException {
        if (O_EXTENDED_FORMAT.equals(outputFormat)) {
          Calendar calendar = webCalendar2Calendar(inputString, inputFormat, FakeDevice.getTimezone(), FakeDevice.getCharset());
          return toJSON(calendar, "", STATE_NEW, JSON_EXTENDED);
        } else if(O_VCAL_FORMAT.equals(outputFormat) || O_ICAL_FORMAT.equals(outputFormat)) {
            String result = null;
            String type   = O_ICAL_FORMAT.equals(outputFormat)?JSON_ICAL:JSON_VCAL;
            if(isConversionRequired(inputFormat,outputFormat)) {
                Calendar calendar = webCalendar2Calendar(inputString, inputFormat, FakeDevice.getTimezone(), FakeDevice.getCharset());
                if(O_ICAL_FORMAT.equals(outputFormat)) {
                    result = calendar2ical(calendar,FakeDevice.getTimezone(),FakeDevice.getCharset());
                } else if(O_VCAL_FORMAT.equals(outputFormat)) {
                    result = calendar2vcal(calendar,FakeDevice.getTimezone(),FakeDevice.getCharset());
                }
            }
            return toRFC("vcal",result,"",STATE_NEW,type);
            
        }
        throw new ConversionException("Output Format ["+outputFormat+"] not supported.");

    }
    
    

    public NameValuePair[] getAvailableInputFormat() {
        return inputFormat;
    }

    public NameValuePair[] getAvailableOutputFormat() {
        return outputFormat;
    }
    
    private static String serverTimeZoneID = null;

     /**
     * 
     * @param vcard
     * @return
     * @throws java.lang.Exception
     */
   public static String toRFC(String jsonContentKey, String jsonContentValue, String key,String state, String contentType) {
        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        jsonData.elementOpt(JsonContactModel.CONTENT_TYPE.getValue(), contentType);

        jsonItem.elementOpt(JsonContactModel.KEY.getValue(), key);
        jsonItem.elementOpt(JsonContactModel.STATE.getValue(), state);
        
        jsonItem.elementOpt(jsonContentKey,jsonContentValue);
        
        jsonData.element(JsonContactModel.ITEM.getValue(), jsonItem);
        jsonRoot.element(JsonContactModel.DATA.getValue(), jsonData);

        return jsonRoot.toString();
    }
     
     
        public static String toJSON(Calendar calendar, String key,String state, String contentType) {
             JSONObject jsonRoot = new JSONObject();
             JSONObject jsonData = new JSONObject(); 
             JSONObject jsonItem = new JSONObject();

             jsonData.elementOpt(JsonAppointmentModel.CONTENT_TYPE.getValue(), contentType);
             jsonItem.elementOpt(JsonAppointmentModel.KEY.getValue(), key);
             //JsonItem.elementOpt(JsonAppointmentModel.STATE.getValue(),state);
             
             CalendarContent cc = calendar.getCalendarContent();
             if(cc!=null)
                 if(cc instanceof Event) {
                     fillJsonWithEvent(jsonItem,(Event)cc);
                 } else if(cc instanceof Task) {
                     fillJsonWithTask(jsonItem,(Task)cc);
                 }
             
 
            jsonData.element(JsonAppointmentModel.ITEM.getValue(), jsonItem);
            jsonRoot.element(JsonAppointmentModel.DATA.getValue(), jsonData);
            return jsonRoot.toString();
        }
     
     
     

   
     
     public static Calendar webCalendar2Calendar(String inputString,
                                         String inputFormat,
                                         TimeZone deviceTimeZone,
                                         String deviceCharset)
      throws ConversionException {

        try {

            ByteArrayInputStream buffer =
                    new ByteArrayInputStream(inputString.getBytes());

            VCalendar vcalendar;
            String version;

            if (VCAL.equals(inputFormat)) { // VCAL_FORMAT

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

            Calendar c = vcf.vcalendar2calendar(vcalendar);


            return c;

        } catch (Exception e) {
            throw new ConversionException("Error converting " + inputFormat + " to Calendar. ", e);
        }
    }

    private static void fillJsonWithEvent(JSONObject jsonItem,Event event) {
         // folder         
        String folder = Utility.getPropertyValue(event.getFolder());
        folder = Utility.folderConverterC2S(folder, Utility.BACKEND_CALENDAR_FOLDER_PREFIX);
        jsonItem.elementOpt(JsonAppointmentModel.FOLDER.getValue(), folder);
        
        jsonItem.elementOpt(JsonAppointmentModel.START_DATE.getValue(), Utility.getPropertyValue(event.getDtStart()));
        jsonItem.elementOpt(JsonAppointmentModel.END_DATE.getValue(), Utility.getPropertyValue(event.getDtEnd()));
        
        // get the timezone from the dtstart
        String tmzid = event.getDtStart().getTimeZone();
        if (tmzid == null){
            tmzid = serverTimeZoneID;
        }
        jsonItem.elementOpt(JsonAppointmentModel.TZID.getValue(), tmzid);

        String subject = Utility.getPropertyValue(event.getSummary());
        jsonItem.elementOpt(JsonAppointmentModel.SUBJECT.getValue(), subject);
        
        jsonItem.elementOpt(JsonAppointmentModel.BODY.getValue(), Utility.getPropertyValue(event.getDescription()));
        
        jsonItem.elementOpt(JsonAppointmentModel.LOCATION.getValue(), Utility.getPropertyValue(event.getLocation()));

        jsonItem.elementOpt(JsonAppointmentModel.ALL_DAY.getValue(), event.isAllDay());
        
        jsonItem.elementOpt(JsonAppointmentModel.CATEGORIES.getValue(), Utility.getPropertyValue(event.getCategories()));
        
        //
        jsonItem.elementOpt(JsonAppointmentModel.SHOW_TIME_AS.getValue(), 
                Utility.getPropertyValueShort(event.getBusyStatus()));
        
        
        // temporary patch
        // IMPORTANCE
        String s = Utility.getPropertyValue(event.getPriority());
        try {
            int tmp = 6; // default value; importance = "normal" 
            try {
               tmp = Integer.parseInt(s);
            } catch (Exception e){
                // do nothing
            }
            int patchedImportance = Utility.importanceClient2Server(tmp);
            //jsonItem.elementOpt(FusemailTaskModel.IMPORTANCE.getValue(), patchedImportance);
            jsonItem.elementOpt(JsonAppointmentModel.IMPORTANCE.getValue(), patchedImportance );
        } catch (Exception e){
                //log.trace("Error handling task importance property - item:" + subject , e);
        }                
                
        // remider
        if(event.getReminder()!=null){
            if(event.getReminder().isActive()){
                jsonItem.elementOpt(JsonAppointmentModel.REMINDER.getValue(), 1);
                int reminderTime = getMinutesBeforeC2S(event);            
                jsonItem.elementOpt(JsonAppointmentModel.REMINDER_TIME.getValue(), reminderTime);
            } else {
                jsonItem.elementOpt(JsonAppointmentModel.REMINDER.getValue(), 0);
            }
        } else {
            jsonItem.elementOpt(JsonAppointmentModel.REMINDER.getValue(), 0);
        }
        
        jsonItem.elementOpt(JsonAppointmentModel.IS_RECURRING.getValue(), event.isRecurrent());        
        if(event.isRecurrent() && event.getRecurrencePattern()!=null) {
        	
            jsonItem.elementOpt(JsonAppointmentModel.RECURRENCE_TYPE.getValue()   , 
                    event.getRecurrencePattern().getTypeId());
            jsonItem.elementOpt(JsonAppointmentModel.DAY_OF_MONTH.getValue()      , 
                    event.getRecurrencePattern().getDayOfMonth());
            jsonItem.elementOpt(JsonAppointmentModel.DAY_OF_WEEK_MASK.getValue()  ,
                    event.getRecurrencePattern().getDayOfWeekMask());
            jsonItem.elementOpt(JsonAppointmentModel.INSTANCE.getValue()          , 
                    event.getRecurrencePattern().getInstance());
            jsonItem.elementOpt(JsonAppointmentModel.INTERVAL.getValue()          , 
                    event.getRecurrencePattern().getInterval());
            jsonItem.elementOpt(JsonAppointmentModel.MONTH_OF_YEAR.getValue()     , 
                    event.getRecurrencePattern().getMonthOfYear());
            jsonItem.elementOpt(JsonAppointmentModel.NO_END_DATE.getValue()       , 
                    event.getRecurrencePattern().isNoEndDate());
            jsonItem.elementOpt(JsonAppointmentModel.OCCURRENCES.getValue()       , 
                    event.getRecurrencePattern().getOccurrences());

            // from clients (7.0) we get the date in local format: YYYYMMDDThhmmss
            String psd = event.getRecurrencePattern().getStartDatePattern();
            jsonItem.elementOpt(JsonAppointmentModel.PATTERN_START_DATE.getValue(),psd);

            // from clients (7.0) we get the date in local format: YYYYMMDDThhmmss
            String ped = event.getRecurrencePattern().getEndDatePattern();
            jsonItem.elementOpt(JsonAppointmentModel.PATTERN_END_DATE.getValue(),ped);
            
            //
            // @todo
            // the fusemail backend has just the EXCLUDED; investigating;
            //
            // exceptions
            // from Calendar to JSON (.. to fusemail)
            // in the exceptions can I have both: excluded and included
            List<ExceptionToRecurrenceRule> exceptions = event.getRecurrencePattern().getExceptions();
            if (exceptions != null && exceptions.size()>0){
                JSONArray[] jsonExceptions = getExceptionsToJSON(exceptions, tmzid);
                jsonItem.elementOpt(JsonAppointmentModel.EXCEPTIONS_INCLUDED.getValue(), jsonExceptions[0]);
                jsonItem.elementOpt(JsonAppointmentModel.EXCEPTIONS_EXCLUDED.getValue(), jsonExceptions[1]);
            }
        
        }

    }

    private static void fillJsonWithTask(JSONObject jsonItem, Task task) {
         // folder         
        String folder = Utility.getPropertyValue(task.getFolder());
        folder = Utility.folderConverterC2S(folder, Utility.BACKEND_TASKS_FOLDER_PREFIX);
        jsonItem.elementOpt(JsonTaskModel.FOLDER.getValue(), folder);
        
        String subject = Utility.getPropertyValue(task.getSummary());
        jsonItem.elementOpt(JsonTaskModel.SUBJECT.getValue(), subject);

        jsonItem.elementOpt(JsonTaskModel.START_DATE.getValue(), Utility.getPropertyValue(task.getDtStart()));
        
        jsonItem.elementOpt(JsonTaskModel.DUE_DATE.getValue(), Utility.getPropertyValue(task.getDtEnd()));
        
        jsonItem.elementOpt(JsonTaskModel.BODY.getValue(), Utility.getPropertyValue(task.getDescription()));
        
        // temporary patch
        // IMPORTANCE
        String s = Utility.getPropertyValue(task.getImportance());
        try {
            int tmp = 6; // default value; importance = "normal" 
            try {
               tmp = Integer.parseInt(s);
            } catch (Exception e){
                // do nothing
            }
            int patchedImportance = Utility.importanceClient2Server(tmp);
            jsonItem.elementOpt(JsonTaskModel.IMPORTANCE.getValue(), patchedImportance);
        } catch (Exception e){
    //                log.trace("Error handling task importance property - item:" + subject , e);
        }

        // COMPLETE
        // STATUS
        String com = Utility.getPropertyValue(task.getComplete());
        String sta = Utility.getPropertyValue(task.getStatus());
        try {
            int complete = Integer.parseInt(com);
            if (complete == 1){
                jsonItem.elementOpt(JsonTaskModel.COMPLETE.getValue(), true);
                jsonItem.elementOpt(JsonTaskModel.STATUS.getValue(), 2);
            } else {
                int status = Integer.parseInt(sta);
                jsonItem.elementOpt(JsonTaskModel.COMPLETE.getValue(), false);
                jsonItem.elementOpt(JsonTaskModel.STATUS.getValue(), status);
            }
        } catch (Exception e){
      //        log.trace("Error handling task complete/status property - item:" + subject , e);
      
        }            

        jsonItem.elementOpt(JsonTaskModel.PERCENT_COMPLETE.getValue(), 
                Utility.getPropertyValue(task.getPercentComplete()));        
        
        
        // reminder
        Reminder reminder = task.getReminder();
        if (reminder != null){
            boolean active = reminder.isActive();
            String reminderDate = reminder.getTime();
            if (active){
                jsonItem.elementOpt(JsonTaskModel.REMINDER.getValue(), true);
                jsonItem.elementOpt(JsonTaskModel.REMINDER_DATE.getValue(), reminderDate);
        } else {
                jsonItem.elementOpt(JsonTaskModel.REMINDER.getValue(), false); 
            }
        } else {
            jsonItem.elementOpt(JsonTaskModel.REMINDER.getValue(), false); 
        }
        
        
        jsonItem.elementOpt(JsonTaskModel.DATE_COMPLETED.getValue(), 
                Utility.getPropertyValue(task.getDateCompleted()));
        
        jsonItem.elementOpt(JsonTaskModel.ACTUAL_WORK.getValue(), 
                Utility.getPropertyValue(task.getActualWork()));
        
        jsonItem.elementOpt(JsonTaskModel.TOTAL_WORK.getValue(), 
                Utility.getPropertyValue(task.getTotalWork()));
        
        jsonItem.elementOpt(JsonTaskModel.BILLING_INFORMATION.getValue(), 
                Utility.getPropertyValue(task.getBillingInformation()));

        jsonItem.elementOpt(JsonTaskModel.MILEAGE.getValue(), 
                Utility.getPropertyValueInt(task.getMileage()));
    }
     
    
      private static int getMinutesBeforeC2S(Event event){
        // default 15 Mins
        //double reminderTime = 0.004167;
        int reminderTime = 15;
        try {
            //String minutesBeforeStartS = event.getReminder().getTime();
            //int minutesBeforeStart = Integer.parseInt(minutesBeforeStartS);
            int minutesBeforeStart = event.getReminder().getMinutes();
            //double den = 3600 / minutesBeforeStart;
            //reminderTime = 1 / den;
            reminderTime = minutesBeforeStart;
        } catch (Exception e){
//                log.trace("Error creating the reminder time before start.", e);
  
        }
        return reminderTime;
    }
    
       public static Calendar sif2Calendar(String inputString)
            throws ConversionException {


        ByteArrayInputStream buffer = null;
        Calendar calendar = null;
        try {
            calendar = new Calendar();
            buffer = new ByteArrayInputStream(inputString.getBytes());
            if ((inputString.getBytes()).length > 0) {
                SIFCalendarParser parser = new SIFCalendarParser(buffer);
                calendar = parser.parse();
            }
        } catch (Exception e) {
            throw new ConversionException("Error converting Calendar from sif.", e);
        }

        return calendar;
    }
       
        private static JSONArray[] getExceptionsToJSON(List<ExceptionToRecurrenceRule> exceptions, String tmzid){
        
        JSONArray[] jsonExceptions = new JSONArray[2];
        
        JSONArray excluded = new JSONArray();
        JSONArray included = new JSONArray();
        
        ExceptionToRecurrenceRule exception = null;
        String date = null;
        
        if (exceptions != null){
            for (int i=0; i<exceptions.size(); i++){
                exception = exceptions.get(i);
                if (exception.isAddition()){
                    // into included array
                    // TMP
                    // is a String: YYYYMMDDThhmmssZ .. it needs conversion
                    date = zuluToLocalConversion(exception.getDate(), tmzid);
                    included.add(date); 
                } else {
                    // into excluded array
                    // TMP
                    // is a String: YYYYMMDDThhmmssZ .. it needs conversion
                    date = zuluToLocalConversion(exception.getDate(), tmzid);
                    excluded.add(date);
                }
            }
        }
        
        jsonExceptions[0] = included;
        jsonExceptions[1] = excluded;
        
        return jsonExceptions;
    }
        
        /**
     * converts from "yyyyMMdd'T'HHmmss'Z'" to "yyyyMMdd'T'HHmmss"
     * using the timezone ID
     * 
     * @param patternDate
     * @param tmzid
     * @return
     * @throws com.funambol.json.JsonConversionException
     */
    private static String zuluToLocalConversion(String patternDate, String tmzid) 
        throws JsonConversionException{

        String output = null;
        
        if (patternDate == null){
            return output;
        }
        
        try {
            SimpleDateFormat ZULU_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            ZULU_DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date datePatterDate = ZULU_DATE_FORMATTER.parse(patternDate);

            SimpleDateFormat LOCAL_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
            LOCAL_DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone(tmzid));
            output = LOCAL_DATE_FORMATTER.format(datePatterDate);
        } catch (ParseException pe){
            throw new JsonConversionException("Error in the zuluToLocalConversion." + pe.getMessage(), pe);
        }

        return output;
    }    
    

      public static String calendar2vcal(Calendar calendar,
            TimeZone deviceTimeZone,
            String deviceCharset)
            throws ConversionException {

        try {

            VCalendarConverter vcf = new VCalendarConverter(deviceTimeZone, deviceCharset);

            VCalendar vcalendar;
            String vcal;

            vcalendar =  vcf.calendar2vcalendar(calendar, true); // text/x-vcalendar


            VComponentWriter writer =
                    new VComponentWriter(VComponentWriter.NO_FOLDING);
            vcal = writer.toString(vcalendar);

            return vcal;

        } catch (Exception e) {
            throw new ConversionException("Error converting Calendar to vcal.", e);
        }
    }

    public static String calendar2ical(Calendar calendar,
            TimeZone deviceTimeZone,
            String deviceCharset)
            throws ConversionException {

        try {

            VCalendarConverter vcf = new VCalendarConverter(deviceTimeZone, deviceCharset);

            VCalendar vcalendar;
            String vcal;

             vcalendar =
                        vcf.calendar2vcalendar(calendar, false); // text/calendar
 
            VComponentWriter writer =
                    new VComponentWriter(VComponentWriter.NO_FOLDING);
            vcal = writer.toString(vcalendar);

            return vcal;

        } catch (Exception e) {
            throw new ConversionException("Error converting Calendar to ical", e);
        }
    }
    

}
