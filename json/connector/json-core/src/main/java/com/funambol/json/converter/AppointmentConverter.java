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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.funambol.common.pim.calendar.Event;
import com.funambol.common.pim.calendar.ExceptionToRecurrenceRule;
import com.funambol.common.pim.calendar.RecurrencePattern;
import com.funambol.common.pim.calendar.RecurrencePatternException;
import com.funambol.common.pim.calendar.Reminder;

import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;

import com.funambol.json.domain.JsonAppointmentModel;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.exception.JsonConversionException;
import com.funambol.json.util.Utility;

/**
 * @version $Id: AppointmentConverter.java 51920 2010-06-04 05:10:49Z gazzaniga $
 */
public class AppointmentConverter implements Converter<JsonItem<Event>> {

    public final static int TYPE_ERROR = -1;
    protected static final FunambolLogger log =
        FunambolLoggerFactory.getLogger(Utility.LOG_NAME);

    // -------------------------------------------------------------- Properties
    private String serverTimeZoneID = null;
    public void setServerTimeZoneID(String serverTimeZoneID) {
        this.serverTimeZoneID = serverTimeZoneID;
    }    

    // ---------------------------------------------------------- Public methods

    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#toJSON(java.lang.Object)
     */
    public String toJSON(JsonItem<Event> item) {
        
        Event event = item.getItem();
        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        // the content-type for the extended format is 
        jsonData.elementOpt(JsonAppointmentModel.CONTENT_TYPE.getValue(),
                            Utility.CONTENT_TYPE_APPOINTMENT_EXT);
        
        jsonItem.elementOpt(JsonAppointmentModel.KEY.getValue(), item.getKey());

        jsonItem.elementOpt(JsonAppointmentModel.STATE.getValue(), item.getState());
        
        // folder         
        String folder = Utility.getPropertyValueOrNull(event.getFolder());
        if (folder != null) {
            folder = Utility.folderConverterC2S(folder, Utility.BACKEND_CALENDAR_FOLDER_PREFIX);
        }
        jsonItem.elementOpt(JsonAppointmentModel.FOLDER.getValue(), folder);

        // get the timezone from the dtstart
        String tmzid = event.getDtStart().getTimeZone();
        // Siemens S55
        jsonItem.elementOpt(JsonAppointmentModel.TZID.getValue(), tmzid);
        if (tmzid == null){
            tmzid = this.serverTimeZoneID;
        }

        Boolean isAllDay = event.isAllDay();

        String sd = Utility.getPropertyValue(event.getDtStart());
        String ed = Utility.getPropertyValue(event.getDtEnd());
        if (isAllDay){
            // <DATE PATCH>
            // If all-day the Converter receives:
            // - from SIF:  format yyyy-MM-DD (from Foundation framework)
            // - from VCAL: format yyyy-MM-DD (from Foundation framework)
            // We have to convert into yyyyMMdd
            sd = Utility.changeDateFormat(sd, tmzid, Utility.TIME_ALLDAY_START, Utility.DATETIME_YYYYMMDD);
            jsonItem.elementOpt(JsonAppointmentModel.START_DATE.getValue(), sd);
            ed = Utility.changeDateFormat(ed, tmzid, Utility.TIME_ALLDAY_END, Utility.DATETIME_YYYYMMDD);
            jsonItem.elementOpt(JsonAppointmentModel.END_DATE.getValue(), ed);
        } else {
            jsonItem.elementOpt(JsonAppointmentModel.START_DATE.getValue(), sd);
            jsonItem.elementOpt(JsonAppointmentModel.END_DATE.getValue(), ed);
        }
        

        String subject = Utility.getPropertyValue(event.getSummary());
        jsonItem.elementOpt(JsonAppointmentModel.SUBJECT.getValue(), subject);
        
        jsonItem.elementOpt(JsonAppointmentModel.BODY.getValue(),
                            Utility.getPropertyValueOrNull(event.getDescription()));
        
        jsonItem.elementOpt(JsonAppointmentModel.LOCATION.getValue(),
                            Utility.getPropertyValueOrNull(event.getLocation()));

        jsonItem.elementOpt(JsonAppointmentModel.ALL_DAY.getValue(), isAllDay);
        
        jsonItem.elementOpt(JsonAppointmentModel.CATEGORIES.getValue(),
                            Utility.getPropertyValueOrNull(event.getCategories()));

        String accessClass = Utility.getPropertyValue(event.getAccessClass());
        Short sensitivity = Utility.SENSITIVITY_NORMAL; //default value "PUBLIC"
        try {
            sensitivity = Short.valueOf(accessClass);
        } catch(Exception e) {
            // do nothing
        }
        jsonItem.elementOpt(JsonAppointmentModel.SENSITIVITY.getValue(),
                            Utility.accessClassFrom03(sensitivity));


        jsonItem.elementOpt(JsonAppointmentModel.SHOW_TIME_AS.getValue(),
                            Utility.getPropertyValueShortOrNull(event.getBusyStatus()));
        
        // temporary patch
        //
        // IMPORTANCE
        String s = Utility.getPropertyValueOrNull(event.getPriority());
        if (s != null) {
            try {
                int tmp = 6; // default value; importance = "normal"
                try {
                   tmp = Integer.parseInt(s);
                } catch (Exception e){
                    // do nothing
                }
                int patchedImportance = Utility.importanceClient2Server(tmp);
                jsonItem.elementOpt(JsonAppointmentModel.IMPORTANCE.getValue(), patchedImportance );
            } catch (Exception e){
                if(log.isTraceEnabled()){
                    log.trace("Error handling event importance for '" +
                              subject + "'", e);
                }
            }
        }

        jsonItem.elementOpt(JsonAppointmentModel.ORGANIZER.getValue(),
                    Utility.getPropertyValueOrNull(event.getOrganizer()));
                
        // remider
        if(event.getReminder()!=null){
            if(event.getReminder().isActive()){
                jsonItem.elementOpt(JsonAppointmentModel.REMINDER.getValue(), 1);
                int reminderTime = getMinutesBeforeC2S(event);            
                jsonItem.elementOpt(JsonAppointmentModel.REMINDER_TIME.getValue(), reminderTime);
            } else {
                jsonItem.elementOpt(JsonAppointmentModel.REMINDER.getValue(), 0);
            }
            jsonItem.elementOpt(JsonAppointmentModel.REMINDER_SOUNDFILE.getValue(), event.getReminder().getSoundFile());
        } else {
            jsonItem.elementOpt(JsonAppointmentModel.REMINDER.getValue(), 0);
        }

        // recurring
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

            // <DATE PATCH>
            // PATTERN_START_DATE
            // from SIF clients (7.0) we get the date in local format: YYYYMMDDThhmmss
            // from VCAL clients and if "allday" we get the date in different formats
            // (usually: YYYY-MM-DD) so that we have to convert into
            // local format: YYYYMMDDThhmmss
            String psd = event.getRecurrencePattern().getStartDatePattern();
            if (event.isAllDay()){
                if (psd != null && !psd.equals("")){
                    psd = Utility.changeDateFormat(psd, tmzid, Utility.TIME_ALLDAY_START, Utility.DATETIME_YYYYMMDDTHHMMSS);
                    jsonItem.elementOpt(JsonAppointmentModel.PATTERN_START_DATE.getValue(),psd);
                }
            } else {
                jsonItem.elementOpt(JsonAppointmentModel.PATTERN_START_DATE.getValue(),psd);
            }

            // <DATE PATCH>
            // PATTERN_END_DATE
            // from SIF clients (7.0) we get the date in local format: YYYYMMDDThhmmss
            // from VCAL clients and if "allday" we get the date in different formats
            // (usually: YYYY-MM-DD) so that we have to convert into
            // local format: YYYYMMDDThhmmss
            String ped = event.getRecurrencePattern().getEndDatePattern();
            if (event.isAllDay()){
                if (ped != null && !ped.equals("")){
                    ped = Utility.changeDateFormat(ped, tmzid, Utility.TIME_ALLDAY_END, Utility.DATETIME_YYYYMMDDTHHMMSS);
                    jsonItem.elementOpt(JsonAppointmentModel.PATTERN_END_DATE.getValue(),ped);
                }
            } else {
                jsonItem.elementOpt(JsonAppointmentModel.PATTERN_END_DATE.getValue(),ped);
            }
            
            //
            // @todo
            // the backend has just the EXCLUDED; investigating;
            //
            // exceptions
            // from Calendar to JSON (.. to backend)
            // in the exceptions can I have both: excluded and included
            List<ExceptionToRecurrenceRule> exceptions = event.getRecurrencePattern().getExceptions();
            if (exceptions != null && exceptions.size()>0){
                JSONArray[] jsonExceptions = getExceptionsToJSON(exceptions, tmzid);
                jsonItem.elementOpt(JsonAppointmentModel.EXCEPTIONS_INCLUDED.getValue(), jsonExceptions[0]);
                jsonItem.elementOpt(JsonAppointmentModel.EXCEPTIONS_EXCLUDED.getValue(), jsonExceptions[1]);
            }
        
        }

        jsonData.element(JsonAppointmentModel.ITEM.getValue(), jsonItem);
        jsonRoot.element(JsonAppointmentModel.DATA.getValue(), jsonData);

        return jsonRoot.toString();
    }

    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#fromJSON(java.lang.String)
     */
    public JsonItem<Event> fromJSON(String jsonContent) {       
        
        JSONObject jsonRoot = JSONObject.fromObject(jsonContent);
        JSONObject jsonData = jsonRoot.getJSONObject(JsonAppointmentModel.DATA.getValue());
        JSONObject jsonItem = jsonData.getJSONObject(JsonAppointmentModel.ITEM.getValue());

        JsonItem<Event> item = new JsonItem<Event>();
        
        item.setContentType(Utility.getJsonValue(jsonData, JsonAppointmentModel.CONTENT_TYPE.getValue()));
        
        item.setKey(Utility.getJsonValue(jsonItem, JsonAppointmentModel.KEY.getValue()));

        item.setState(Utility.getJsonValue(jsonItem, JsonAppointmentModel.STATE.getValue()));

        Event event = new Event();
        
        // folder
        String folder = Utility.getJsonValue(jsonItem, JsonAppointmentModel.FOLDER.getValue());
        if (folder != null) {
            folder = Utility.folderConverterS2C(folder, Utility.BACKEND_CALENDAR_FOLDER_PREFIX);
            event.getFolder().setPropertyValue(folder);
        }

        // Start and end Date; we need to know if it is "all day"
        boolean isAllDay = jsonItem.optBoolean(JsonAppointmentModel.ALL_DAY.getValue(), false);

        String tmzid = jsonItem.optString(JsonAppointmentModel.TZID.getValue(), null);
        if (tmzid == null){
            //tmzid = this.serverTimeZoneID;
        }

        String startDate = Utility.getJsonValue(jsonItem, JsonAppointmentModel.START_DATE.getValue());
        String endDate   = Utility.getJsonValue(jsonItem, JsonAppointmentModel.END_DATE.getValue());
        if (isAllDay){
            // from the back-end system we receive the format: yyyy-MM-dd
            event.getDtStart().setPropertyValue(Utility.changeDateFormat(startDate, tmzid, Utility.TIME_ALLDAY_START, Utility.DATETIME_YYYYMMDDTHHMMSS));
            event.getDtEnd().setPropertyValue(Utility.changeDateFormat(endDate, tmzid, Utility.TIME_ALLDAY_END, Utility.DATETIME_YYYYMMDDTHHMMSS));
        } else {
            // otherwise we receive the format: yyyyMMdd'T'HHmmss'Z'
            event.getDtStart().setPropertyValue(startDate);
            event.getDtStart().setTimeZone(tmzid);
            event.getDtEnd().setPropertyValue(endDate);
            event.getDtEnd().setTimeZone(tmzid);
        }
        
        String subject = Utility.getJsonValue(jsonItem, JsonAppointmentModel.SUBJECT.getValue());
        event.getSummary().setPropertyValue(subject);
        
        event.getDescription().setPropertyValue(
    //        Utility.getJsonValue(jsonItem, JsonAppointmentModel.BODY.getValue())
                jsonItem.optString(JsonAppointmentModel.BODY.getValue(), null)
        );
        
        event.getLocation().setPropertyValue(
            Utility.getJsonValue(jsonItem, JsonAppointmentModel.LOCATION.getValue())
        );

        event.setAllDay(isAllDay);
        
        event.getCategories().setPropertyValue(
            Utility.getJsonValue(jsonItem, JsonAppointmentModel.CATEGORIES.getValue())
        );

        String accessClass = 
            Utility.getJsonValue(jsonItem, JsonAppointmentModel.SENSITIVITY.getValue());
        short sensitivity = Utility.accessClassTo03(accessClass);
        event.getAccessClass().setPropertyValue(sensitivity);

        Short showTimeAs = Utility.getJsonValueShortOrNull(jsonItem, JsonAppointmentModel.SHOW_TIME_AS.getValue());
        event.setBusyStatus(showTimeAs);
        
        // temporary patch
        String s = Utility.getJsonValue(jsonItem, JsonAppointmentModel.IMPORTANCE.getValue());
        if (s != null && !s.equals("")) {
            int patchedImportance = 0;
            try {
                patchedImportance = Utility.importanceServer2Client(Integer.parseInt(s));
                event.getPriority().setPropertyValue(""+patchedImportance);
            } catch (Exception e){
                if(log.isTraceEnabled()){
                    log.trace("Event '" + subject + "'", e);
                }
            }
        }

        event.getOrganizer().setPropertyValue(
                jsonItem.optString(JsonAppointmentModel.ORGANIZER.getValue(), null)
                );
        
        // Reminder
        Reminder reminder = null;
        if(jsonItem.has(JsonAppointmentModel.REMINDER.getValue())){
            String rem = jsonItem.optString(JsonAppointmentModel.REMINDER.getValue(), null);
            if (rem != null && rem.equals("1")){
                reminder = new Reminder();
                reminder.setActive(true);
                int reminderTime = getMinutesBeforeS2C(jsonItem);
                reminder.setMinutes(reminderTime);
                event.setReminder(reminder);
            } else {
                reminder = new Reminder();
                reminder.setActive(false);
                event.setReminder(reminder);
            }

            reminder.setSoundFile(Utility.getJsonValue(jsonItem, JsonAppointmentModel.REMINDER_SOUNDFILE.getValue()));

        } else {
            reminder = new Reminder();
            reminder.setActive(false);
            event.setReminder(reminder);
        }
        
        // set the complex object RecurrencePattern
        // @todo TEMPORARY: it will be changed in according with the API
        // "\"isRecurring\":\"true\"," 
        //if(jsonItem.optBoolean(JsonAppointmentModel.IS_RECURRING.getValue(),false)) {
        String isRecurring = jsonItem.optString(JsonAppointmentModel.IS_RECURRING.getValue(), "false");
        if (Boolean.parseBoolean(isRecurring)) {
            event.setRecurrencePattern(getRecurrencePatternFromJsonItem(jsonItem, tmzid, startDate));
            
            // we set the recurrence pattern to be expressed in the same tz as the dtstart
            event.getRecurrencePattern().setTimeZone(event.getDtStart().getTimeZone());
        } else {
            event.setRecurrencePattern(null);
        }

        item.setItem(event);

        return item;
    }

    /**
     * Converts the given json item into a calendar text representation.
     *
     * @param item the JsonItem object to convert
     * @return the calendar text representation
     */
    public String toRFC(JsonItem<String> item) {

        String vcal = item.getItem();

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        jsonData.elementOpt(JsonAppointmentModel.CONTENT_TYPE.getValue(),
                            Utility.CONTENT_TYPE_APPOINTMENT_RFC);

        jsonItem.elementOpt(JsonAppointmentModel.KEY.getValue(), item.getKey());
        jsonItem.elementOpt(JsonAppointmentModel.STATE.getValue(), item.getState());

        jsonItem.elementOpt(JsonAppointmentModel.VCAL.getValue(), vcal);

        jsonData.element(JsonAppointmentModel.ITEM.getValue(), jsonItem);
        jsonRoot.element(JsonAppointmentModel.DATA.getValue(), jsonData);

        return jsonRoot.toString();
    }

    /**
     * Converts the given calendar text representation into a JsonItem object.
     *
     * @param jsonRFC the calendar text representation
     * @return the JsonItem object
     */
    public JsonItem<String> fromRFC(String jsonRFC) {

        JSONObject jsonRoot = JSONObject.fromObject(jsonRFC);
        JSONObject jsonData = jsonRoot.getJSONObject(JsonAppointmentModel.DATA.getValue());
        JSONObject jsonItem = jsonData.getJSONObject(JsonAppointmentModel.ITEM.getValue());

        JsonItem<String> item = new JsonItem<String>();

        item.setContentType(jsonData.optString(JsonAppointmentModel.CONTENT_TYPE.getValue()));
        item.setKey(jsonItem.optString(JsonAppointmentModel.KEY.getValue()));
        item.setState(jsonItem.optString(JsonAppointmentModel.STATE.getValue()));

        String vcalItem = jsonItem.optString(JsonAppointmentModel.VCAL.getValue());
        vcalItem = vcalItem.replace("\\", "\\\\");

        item.setItem(vcalItem);

        return item;
    }

    //---------------------------------------------------------- Private methods
    
    /**
     * 
     * @param jsonItem
     * @return
     */
    private RecurrencePattern getRecurrencePatternFromJsonItem(JSONObject jsonItem, 
                                                               String tmzid, 
                                                               String startDate) {
        
        RecurrencePattern rp = null;
        
        try {
            
            int type = jsonItem.optInt(JsonAppointmentModel.RECURRENCE_TYPE.getValue(), 0);
            
            int dayOfMonth    = jsonItem.optInt(JsonAppointmentModel.DAY_OF_MONTH.getValue(), 0);
            int dayOfWeekMask = jsonItem.optInt(JsonAppointmentModel.DAY_OF_WEEK_MASK.getValue(), 0);
            int instance      = jsonItem.optInt(JsonAppointmentModel.INSTANCE.getValue(), 0);
            int interval      = jsonItem.optInt(JsonAppointmentModel.INTERVAL.getValue(), 0);
            int monthOfYear   = jsonItem.optInt(JsonAppointmentModel.MONTH_OF_YEAR.getValue(), 0);
            
            int occurrences   = jsonItem.optInt(JsonAppointmentModel.OCCURRENCES.getValue(), 0);
            
            // @todo TEMPORARY: it will be changed in according with the API
            // "\"noEndDate\":\"false\"," +
            //boolean noEndDate       = jsonItem.optBoolean(JsonAppointmentModel.NO_END_DATE.getValue(), false);
            String noEndDateS = jsonItem.optString(JsonAppointmentModel.NO_END_DATE.getValue(), "false");
            boolean noEndDate = Boolean.parseBoolean(noEndDateS);

            // from backend we have the date in zulu format: YYYYMMDDThhmmssZ
            // check the toJSON method.
            // note: the startDate is used in order to set the patternStartDate if PATTERN_START_DATE == null
            String psd = jsonItem.optString(JsonAppointmentModel.PATTERN_START_DATE.getValue(), null);
            String patternStartDate;
            if (psd == null){
                patternStartDate = Utility.changeDateFormat(startDate, tmzid, null, Utility.DATETIME_YYYYMMDDTHHMMSS);
            } else {
                patternStartDate = Utility.changeDateFormat(psd, tmzid, null, Utility.DATETIME_YYYYMMDDTHHMMSS);
            }
            
            // from backend we have the date in zulu format: YYYYMMDDThhmmssZ
            // check the toJSON method.
            String ped = jsonItem.optString(JsonAppointmentModel.PATTERN_END_DATE.getValue(), null);
            String patternEndDate = Utility.changeDateFormat(ped, tmzid, null, Utility.DATETIME_YYYYMMDDTHHMMSS);
            
            switch (type) {
                case TYPE_ERROR:
                    break;

                case RecurrencePattern.TYPE_DAYLY: {
                    if (noEndDate && occurrences < 1) {
                        rp = RecurrencePattern.getDailyRecurrencePattern(interval, 
                                patternStartDate, noEndDate, (short) dayOfWeekMask);
                    } else if (noEndDate && occurrences > 0){ 
                        rp = RecurrencePattern.getDailyRecurrencePattern(interval, 
                                patternStartDate, noEndDate, occurrences, (short) dayOfWeekMask);
                    } else if (!noEndDate && occurrences < 1){ 
                        rp = RecurrencePattern.getDailyRecurrencePattern(interval, 
                                patternStartDate, patternEndDate, noEndDate, (short) dayOfWeekMask);
                    } else if (!noEndDate && occurrences > 0){
                        rp = RecurrencePattern.getDailyRecurrencePattern(interval, 
                                patternStartDate, patternEndDate, noEndDate, occurrences, (short) dayOfWeekMask);
                    }
                }
                break;

                case RecurrencePattern.TYPE_WEEKLY: {
                    if (noEndDate && occurrences < 1) {
                        rp = RecurrencePattern.getWeeklyRecurrencePattern(interval, 
                                (short) dayOfWeekMask, patternStartDate, noEndDate);
                    } else if (noEndDate && occurrences > 0){ 
                        rp = RecurrencePattern.getWeeklyRecurrencePattern(interval, 
                                (short) dayOfWeekMask, patternStartDate, "", noEndDate, occurrences);
                    } else if (!noEndDate && occurrences < 1){ 
                        rp = RecurrencePattern.getWeeklyRecurrencePattern(interval, 
                                (short) dayOfWeekMask, patternStartDate, patternEndDate, noEndDate);
                    } else if (!noEndDate && occurrences > 0){
                        rp = RecurrencePattern.getWeeklyRecurrencePattern(interval, 
                                (short) dayOfWeekMask, patternStartDate, patternEndDate, noEndDate, occurrences);
                    }

                }
                break;

                case RecurrencePattern.TYPE_MONTH_NTH: {
                    if (noEndDate && occurrences < 1) {
                        rp = RecurrencePattern.getMonthNthRecurrencePattern(interval, 
                                (short) dayOfWeekMask, (short) instance, patternStartDate, noEndDate);
                    } else if (noEndDate && occurrences > 0){ 
                        rp = RecurrencePattern.getMonthNthRecurrencePattern(interval, 
                                (short) dayOfWeekMask, (short) instance, patternStartDate, "", noEndDate, occurrences);
                    } else if (!noEndDate && occurrences < 1){ 
                        rp = RecurrencePattern.getMonthNthRecurrencePattern(interval, 
                                (short) dayOfWeekMask, (short) instance, patternStartDate, patternEndDate, noEndDate, -1);
                    } else if (!noEndDate && occurrences > 0){
                        rp = RecurrencePattern.getMonthNthRecurrencePattern(interval, 
                                (short) dayOfWeekMask, (short) instance, patternStartDate, patternEndDate, noEndDate, occurrences);
                    }

                }
                break;

                case RecurrencePattern.TYPE_MONTHLY: {
                    if (noEndDate && occurrences < 1) {
                        rp = RecurrencePattern.getMonthlyRecurrencePattern(interval, 
                                (short) dayOfMonth, patternStartDate, noEndDate);
                    } else if (noEndDate && occurrences > 0){ 
                        rp = RecurrencePattern.getMonthlyRecurrencePattern(interval, 
                                (short) dayOfMonth, patternStartDate, "", noEndDate, occurrences);
                    } else if (!noEndDate && occurrences < 1){ 
                        rp = RecurrencePattern.getMonthlyRecurrencePattern(interval, 
                                (short) dayOfMonth, patternStartDate, patternEndDate, noEndDate);
                    } else if (!noEndDate && occurrences > 0){
                        rp = RecurrencePattern.getMonthlyRecurrencePattern(interval, 
                                (short) dayOfMonth, patternStartDate, patternEndDate, noEndDate, occurrences);
                    }
                }
                break;
                
                case RecurrencePattern.TYPE_YEAR_NTH: {
                    if (noEndDate && occurrences < 1) {
                        rp = RecurrencePattern.getYearNthRecurrencePattern(interval, 
                                (short) dayOfWeekMask, (short) monthOfYear, (short) dayOfMonth, patternStartDate, noEndDate);
                    } else if (noEndDate && occurrences > 0){ 
                        rp = RecurrencePattern.getYearNthRecurrencePattern(interval, 
                                (short) dayOfWeekMask, (short) monthOfYear, (short) dayOfMonth, patternStartDate, "", noEndDate, occurrences);
                    } else if (!noEndDate && occurrences < 1){ 
                        rp = RecurrencePattern.getYearNthRecurrencePattern(interval, 
                                (short) dayOfWeekMask, (short) monthOfYear, (short) dayOfMonth, patternStartDate, patternEndDate, noEndDate);
                    } else if (!noEndDate && occurrences > 0){
                        rp = RecurrencePattern.getYearNthRecurrencePattern(interval, 
                                (short) dayOfWeekMask, (short) monthOfYear, (short) dayOfMonth, patternStartDate, patternEndDate, noEndDate, occurrences);
                    }
                }
                break;
                
                case RecurrencePattern.TYPE_YEARLY: {
                    if (noEndDate && occurrences < 1) {
                        rp = RecurrencePattern.getYearlyRecurrencePattern(interval, 
                                (short) dayOfMonth, (short) monthOfYear, patternStartDate, noEndDate);
                    } else if (noEndDate && occurrences > 0){ 
                        rp = RecurrencePattern.getYearlyRecurrencePattern(interval, 
                                (short) dayOfMonth, (short) monthOfYear, patternStartDate, "", noEndDate, occurrences);
                    } else if (!noEndDate && occurrences < 1){ 
                        rp = RecurrencePattern.getYearlyRecurrencePattern(interval, 
                                (short) dayOfMonth, (short) monthOfYear, patternStartDate, patternEndDate, noEndDate);
                    } else if (!noEndDate && occurrences > 0){
                        rp = RecurrencePattern.getYearlyRecurrencePattern(interval, 
                                (short) dayOfMonth, (short) monthOfYear, patternStartDate, patternEndDate, noEndDate, occurrences);
                    }
                }
                break;

                default:
                break;
            }

            // exceptions
            JSONArray jsonIncludedExceptions = 
                    jsonItem.optJSONArray(JsonAppointmentModel.EXCEPTIONS_INCLUDED.getValue());
            JSONArray jsonExcludedExceptions = 
                    jsonItem.optJSONArray(JsonAppointmentModel.EXCEPTIONS_EXCLUDED.getValue());
            
            JSONArray[] jsonExceptions = {jsonIncludedExceptions, jsonExcludedExceptions};
            
            List<ExceptionToRecurrenceRule> exceptions = getExceptionsFromJSON(jsonExceptions, tmzid);
            rp.setExceptions(exceptions);

        } catch (RecurrencePatternException e) {
            throw new JsonConversionException("Error in the zuluToLocalConversion.", e);
        }

        return rp;
    }
    
    /**
     * 
     * @todo 
     * the backend sends just the 
     * "exceptions" - "excluded date"
     * So that the isAddiction = false
     * 
     * @param jsonExceptions
     * @return
     */
    private List<ExceptionToRecurrenceRule> getExceptionsFromJSON(JSONArray[] jsonExceptions, String tmzid){
        
        // this list could contains the Excluded and the Included
        List<ExceptionToRecurrenceRule> exceptions = new ArrayList<ExceptionToRecurrenceRule>();

        ExceptionToRecurrenceRule exception = null;
        String date = null; // string in either YYYYMMDDThhmmssZ or YYYYMMDDThhmmss (localtime) format

        try {
            
            JSONArray included = jsonExceptions[0];
            if (included != null){
                for (int i=0; i<included.size(); i++){
                    date = included.getString(i);
                    if (Utility.getDateFormat(date).equals("yyyyMMdd'T'HHmmss")) {
                    // if date is in localtime we need to convert it to UTC
                        date = Utility.changeDateFormat(date, tmzid, Utility.TIME_ALLDAY_START, Utility.DATETIME_YYYYMMDDTHHMMSSZ);
                    }
                    exception = new ExceptionToRecurrenceRule(true, date); 
                    exceptions.add(exception);
                }
            }
            
            JSONArray excluded = jsonExceptions[1];
            if (excluded != null){
                for (int i=0; i<excluded.size(); i++){
                    date = excluded.getString(i);
                    if (Utility.getDateFormat(date).equals("yyyyMMdd'T'HHmmss")) {
                        // if date is in localtime we need to convert it to UTC
                        date = Utility.changeDateFormat(date, tmzid, Utility.TIME_ALLDAY_START, Utility.DATETIME_YYYYMMDDTHHMMSSZ);
                    }
                    exception = new ExceptionToRecurrenceRule(false, date); 
                    exceptions.add(exception);
                }
            }
                        
        } catch (ParseException pe){
            throw new JsonConversionException("Error in the createExceptionsFromJSON.", pe);
        }
        
        return exceptions;
    }

    /**
     * @todo 
     * the backend accepts the 
     * "exceptions" - "excluded date"
     * we have to verify the 
     * "exceptions" - "included date"
     * The input exceptions contains both included and excluded
     * 
     * @param exceptions
     * @return
     */
    private JSONArray[] getExceptionsToJSON(List<ExceptionToRecurrenceRule> exceptions, String tmzid){
        
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
     * Converts from "yyyyMMdd'T'HHmmss'Z'" to "yyyyMMdd'T'HHmmss" using the 
     * timezone ID. If the given date is in standard allday format 'yyyyMMdd', 
     * no convertion is needed. If it is in not standard allday format
     * 'yyyy-MM-dd', it's needed to remove the '-'.
     * 
     * @param patternDate the date to convert
     * @param tmzid the timezone id to use in the convertion
     * @return String the converted date in local time
     * @throws com.funambol.json.exception.JsonConversionException
     */
    private String zuluToLocalConversion(String patternDate, String tmzid) 
    throws JsonConversionException{

        String output = null;
        
        try {

            if (patternDate == null){
                return output;
            }

            String dateFormat = Utility.getDateFormat(patternDate);
            
            if ("yyyyMMdd".equals(dateFormat)) {
                return patternDate;
            }

            if ("yyyy-MM-dd".equals(dateFormat)) {
                return patternDate.replaceAll("-", "");
            }

            if ("yyyyMMdd'T'HHmmss".equals(dateFormat) && patternDate.matches(".*T000000") ) {
                // if "Z" is not specified and HHmmss == 000000 then it is an all-day
                // See bug #6724
                return patternDate.replace("T000000", "");
            }
            
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
    
    /**
     * 
     * @param event
     * @return
     */
    private int getMinutesBeforeC2S(Event event){
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
            if (log.isTraceEnabled()) {
                log.trace("Error creating the reminder time before start.", e);
            }
        }
        return reminderTime;
    }

    /**
     * 
     * @param jsonItem
     * @return
     */
    private int getMinutesBeforeS2C(JSONObject jsonItem){
        // default 15 Mins
        //double reminderTime = 15;
        int reminderTime = 15;
        try {
            String xs = jsonItem.getString(JsonAppointmentModel.REMINDER_TIME.getValue());
            //double x = Double.parseDouble(xs);
            //reminderTime  = 3600 * x;
            reminderTime  = Integer.parseInt(xs);
        } catch (Exception e){
            if (log.isTraceEnabled()) {
                log.trace("Error creating the reminder time before start.", e);
            }
        }
        return reminderTime;
    }
    
}
