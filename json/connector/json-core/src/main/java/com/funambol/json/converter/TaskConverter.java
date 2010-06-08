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

import net.sf.json.JSONObject;

import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.calendar.Reminder;
import com.funambol.common.pim.calendar.Task;
import com.funambol.common.pim.converter.BaseConverter;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.utility.TimeUtils;

import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;

import com.funambol.json.domain.JsonItem;
import com.funambol.json.domain.JsonTaskModel;
import com.funambol.json.util.Utility;
import java.util.TimeZone;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Converter for JsonItem<Task>
 *
 * @version $Id: TaskConverter.java 52034 2010-06-08 12:59:28Z derose $
 */
public class TaskConverter implements Converter<JsonItem<Task>> {

    public final static int TYPE_ERROR = -1;

    protected static final FunambolLogger log = FunambolLoggerFactory.getLogger(Utility.LOG_NAME);

    // -------------------------------------------------------------- Properties
    private String serverTimeZoneID = null;
    public void setServerTimeZoneID(String serverTimeZoneID) {
        this.serverTimeZoneID = serverTimeZoneID;
    }

    // ---------------------------------------------------------- Public methods
    /* (non-Javadoc)
     * @see com.funambol.Json.converter.Converter#toJSON(java.lang.Object)
     */
    public String toJSON(JsonItem<Task> item) {

        Task task = item.getItem();

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        jsonData.elementOpt(JsonTaskModel.CONTENT_TYPE.getValue(),
                            Utility.CONTENT_TYPE_TASK_EXT);

        jsonItem.elementOpt(JsonTaskModel.KEY.getValue(), item.getKey());
        jsonItem.elementOpt(JsonTaskModel.STATE.getValue(), item.getState());

        // folder         
        String folder = getPropertyValueOrNull(task.getFolder());
        folder = Utility.folderConverterC2S(folder, Utility.BACKEND_TASKS_FOLDER_PREFIX);
        jsonItem.elementOpt(JsonTaskModel.FOLDER.getValue(), folder);

        String subject = Utility.getPropertyValue(task.getSummary());
        jsonItem.elementOpt(JsonTaskModel.SUBJECT.getValue(), subject);

        // get the timezone from the dtstart
        String tmzid = task.getDtStart().getTimeZone();
        if (tmzid == null){
            tmzid = this.serverTimeZoneID;
        }
        jsonItem.elementOpt(JsonTaskModel.TZID.getValue(), tmzid);

        Boolean isAllDay = task.isAllDay();

        String sd = getPropertyValueOrNull(task.getDtStart());
        //String ed = Utility.getPropertyValue(task.getDueDate());
        String ed = getPropertyValueOrNull(task.getDueDate());
        if (isAllDay){
            // <DATE PATCH>
            // If all-day the Converter receives:
            // - from SIF:  format yyyy-MM-DD (from Foundation framework)
            // - from VCAL: format yyyy-MM-DD (from Foundation framework)
            // We have to convert into yyyyMMdd
                sd = Utility.changeDateFormat(sd, tmzid, Utility.TIME_ALLDAY_START, Utility.DATETIME_YYYYMMDD);
                jsonItem.elementOpt(JsonTaskModel.START_DATE.getValue(), sd);
                ed = Utility.changeDateFormat(ed, tmzid, Utility.TIME_ALLDAY_END, Utility.DATETIME_YYYYMMDD);
                jsonItem.elementOpt(JsonTaskModel.DUE_DATE.getValue(), ed);
        } else {
            jsonItem.elementOpt(JsonTaskModel.START_DATE.getValue(), sd);
            jsonItem.elementOpt(JsonTaskModel.DUE_DATE.getValue(), ed);
        }

        jsonItem.elementOpt(JsonTaskModel.ALL_DAY.getValue(), isAllDay);

        jsonItem.elementOpt(JsonTaskModel.CATEGORIES.getValue(),
                Utility.getPropertyValueOrNull(task.getCategories()));


        jsonItem.elementOpt(JsonTaskModel.BODY.getValue(),
                            Utility.getPropertyValueOrNull(task.getDescription()));

        String accessClass = Utility.getPropertyValue(task.getAccessClass());
        Short sensitivity = Utility.SENSITIVITY_NORMAL; //default value "PUBLIC"
        try {
            sensitivity = Short.valueOf(accessClass);
        } catch(Exception e) {
            // do nothing
        }
        jsonItem.elementOpt(JsonTaskModel.SENSITIVITY.getValue(),
                            Utility.accessClassFrom03(sensitivity));

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
            if(log.isTraceEnabled()){
                log.trace("Error handling task importance property - item:" + subject , e);
            }
        }

        // COMPLETE
        String com = Utility.getPropertyValue(task.getComplete());
        if ("1".equals(com)) {
            jsonItem.elementOpt(JsonTaskModel.COMPLETE.getValue(), true);
        } else {
            jsonItem.elementOpt(JsonTaskModel.COMPLETE.getValue(), false);
        }

        // STATUS
        s = Utility.getPropertyValueOrNull(task.getStatus());
        try {
            Short status = null;
            try {
                status = Short.parseShort(s);
            } catch (Exception e) {
                // do nothing
            }
            jsonItem.elementOpt(JsonTaskModel.STATUS.getValue(), status);

        } catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.trace("Error handling task status property for '" +
                           subject + "'", e);
            }
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
            jsonItem.elementOpt(JsonTaskModel.REMINDER_SOUNDFILE.getValue(), reminder.getSoundFile());
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

        /**
         * 7.0 issue 
         * get company from Organizer field
         */
        jsonItem.elementOpt(JsonTaskModel.COMPANIES.getValue(),
                getPropertyValueOrNull(task.getOrganizer()));

        jsonData.element(JsonTaskModel.ITEM.getValue(), jsonItem);
        jsonRoot.element(JsonTaskModel.DATA.getValue(), jsonData);

        return jsonRoot.toString();
    }

    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#fromJSON(java.lang.String)
     */
    public JsonItem<Task> fromJSON(String jsonContent) {

        JSONObject jsonRoot = JSONObject.fromObject(jsonContent);
        JSONObject jsonData = jsonRoot.getJSONObject(JsonTaskModel.DATA.getValue());
        JSONObject jsonItem = jsonData.getJSONObject(JsonTaskModel.ITEM.getValue());

        JsonItem<Task> item = new JsonItem<Task>();

        item.setContentType(Utility.getJsonValue(jsonData, JsonTaskModel.CONTENT_TYPE.getValue()));

        item.setKey(Utility.getJsonValue(jsonItem, JsonTaskModel.KEY.getValue()));

        item.setState(Utility.getJsonValue(jsonItem, JsonTaskModel.STATE.getValue()));

        Task task = new Task();

        // folder
        String folder = Utility.getJsonValue(jsonItem, JsonTaskModel.FOLDER.getValue());
        if (folder!=null){
            folder = Utility.folderConverterS2C(folder, Utility.BACKEND_TASKS_FOLDER_PREFIX);
            task.getFolder().setPropertyValue(folder);
        }
        String subject = Utility.getJsonValue(jsonItem, JsonTaskModel.SUBJECT.getValue());
        task.getSummary().setPropertyValue(subject);

        // Start and end Date; we need to know if it is "all day"
        boolean isAllDay =
            jsonItem.optBoolean(JsonTaskModel.ALL_DAY.getValue(), false);

        String tmzid = jsonItem.optString(JsonTaskModel.TZID.getValue(), null);
        if (tmzid == null){
            tmzid = this.serverTimeZoneID;
        }

        String startDate =
            Utility.getJsonValue(jsonItem, JsonTaskModel.START_DATE.getValue());
        String dueDate   =
            Utility.getJsonValue(jsonItem, JsonTaskModel.DUE_DATE.getValue());

        if (isAllDay){
            // from the back-end system we receive the format: yyyy-MM-dd
            task.getDtStart().setPropertyValue(
                Utility.changeDateFormat(startDate,
                                         tmzid,
                                         Utility.TIME_ALLDAY_START,
                                         Utility.DATETIME_YYYYMMDDTHHMMSS));
            task.getDueDate().setPropertyValue(
                Utility.changeDateFormat(dueDate,
                                         tmzid,
                                         Utility.TIME_ALLDAY_END,
                                         Utility.DATETIME_YYYYMMDDTHHMMSS));
        } else {
            // otherwise we receive the format: yyyyMMdd'T'HHmmss'Z'
            if (startDate != null) {
                task.getDtStart().setPropertyValue(startDate);
                task.getDtStart().setTimeZone(tmzid);
            }
            if (dueDate != null) {
                task.getDueDate().setPropertyValue(dueDate);
                task.getDueDate().setTimeZone(tmzid);
            }
        }

        task.setAllDay(isAllDay);

        task.getDescription().setPropertyValue(
                jsonItem.optString(JsonTaskModel.BODY.getValue(), null));

        String accessClass =
            Utility.getJsonValue(jsonItem, JsonTaskModel.SENSITIVITY.getValue());
        short sensitivity = Utility.accessClassTo03(accessClass);
        task.getAccessClass().setPropertyValue(sensitivity);

        // temporary patch
        String importance =
            Utility.getJsonValue(jsonItem, JsonTaskModel.IMPORTANCE.getValue());
        if (importance != null) {
            int patchedImportance = 0;
            try {
                patchedImportance =
                    Utility.importanceServer2Client(Integer.parseInt(importance));
                task.getImportance().setPropertyValue(""+patchedImportance);
            } catch (Exception e){
                if(log.isTraceEnabled()){
                    log.trace("Error handling task importance for ["
                              + subject + "]", e);
                }
            }
        }

        // reminder
        Reminder reminder = new Reminder();
        boolean active       =
            Utility.getJsonValueBoolean(jsonItem, JsonTaskModel.REMINDER.getValue());
        String  reminderDate =
            Utility.getJsonValue(jsonItem, JsonTaskModel.REMINDER_DATE.getValue());
        if (active) {
            reminder.setActive(true);
            if (Utility.getDateFormat(reminderDate).equals("yyyyMMdd'T'HHmmss")) {
                // if it it expressed in localtime then we need to set the
                // timezone as well so that it will be preserved
                reminder.setTimeZone(tmzid);
            }

            try {
                // The reminder time needs to be set as offset with respect to the
                // event start (and not only as absolute time), otherwise the DAO
                // won't persist it correctly.
                setAlarmTimeAndMinutes(reminder, reminderDate, task.getDtStart(), isAllDay, tmzid);
            } catch (Exception e) {
                log.error(e);
            }

            reminder.setTime(reminderDate);

        } else {
            reminder.setActive(false);
            reminder.setTime(null);
        }
        reminder.setSoundFile(Utility.getJsonValue(jsonItem, JsonTaskModel.REMINDER_SOUNDFILE.getValue()));
        task.setReminder(reminder);

        task.getStatus().setPropertyValue(
            Utility.getJsonValue(jsonItem, JsonTaskModel.STATUS.getValue()));

        task.getComplete().setPropertyValue(
            Utility.getJsonValueBoolean(jsonItem, JsonTaskModel.COMPLETE.getValue()));

        String percent =
            Utility.getJsonValue(jsonItem, JsonTaskModel.PERCENT_COMPLETE.getValue());
        if ("".equals(percent)){
            task.getPercentComplete().setPropertyValue("0");
        } else if (percent == null) {
            task.getPercentComplete().setPropertyValue(null);
        } else {
            int value = truncPercentComplete(percent);
            task.getPercentComplete().setPropertyValue(""+value);
        }

        // details
        task.getDateCompleted().setPropertyValue(
            Utility.getJsonValue(jsonItem, JsonTaskModel.DATE_COMPLETED.getValue()));

        String actualWork =
            Utility.getJsonValue(jsonItem, JsonTaskModel.ACTUAL_WORK.getValue());
        if ("".equals(actualWork)){
            task.getActualWork().setPropertyValue("0");
        } else {
            task.getActualWork().setPropertyValue(actualWork);
        }

        String totalWork =
            Utility.getJsonValue(jsonItem, JsonTaskModel.TOTAL_WORK.getValue());
        if ("".equals(totalWork)){
            task.getTotalWork().setPropertyValue("0");
        } else {
            task.getTotalWork().setPropertyValue(totalWork);
        }

        task.getBillingInformation().setPropertyValue(
            Utility.getJsonValue(jsonItem, JsonTaskModel.BILLING_INFORMATION.getValue()));

        if(jsonItem.has(JsonTaskModel.MILEAGE.getValue())) {
            int mil = Utility.getJsonValueInt(jsonItem, JsonTaskModel.MILEAGE.getValue());
            task.setMileage(mil);
        } else {
            task.setMileage(null);
        }

        task.getCategories().setPropertyValue(
                Utility.getJsonValue(jsonItem, JsonTaskModel.CATEGORIES.getValue()));


        /**
         * 7.0 issue
         * set companies in organizer field
         */
        task.getOrganizer().setPropertyValue(
            Utility.getJsonValue(jsonItem, JsonTaskModel.COMPANIES.getValue()));

        item.setItem(task);

        return item;
    }

    /**
     * this method converts from a json contact item object to a json vcard text representation
     * @param item
     * @return
     * @throws java.lang.Exception
     */
    public String toRFC(JsonItem<String> item) {

        String vcal = item.getItem();

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        // the content-type for the internet specification format is 
        jsonData.elementOpt(JsonTaskModel.CONTENT_TYPE.getValue(), Utility.CONTENT_TYPE_TASK_RFC);

        jsonItem.elementOpt(JsonTaskModel.KEY.getValue(), item.getKey());
        jsonItem.elementOpt(JsonTaskModel.STATE.getValue(), item.getState());

        //jsonItem.elementOpt(JsonContactModel.FOLDER.getValue(), contact.getFolder());

        jsonItem.elementOpt(JsonTaskModel.VCAL.getValue(), vcal);

        jsonData.element(JsonTaskModel.ITEM.getValue(), jsonItem);
        jsonRoot.element(JsonTaskModel.DATA.getValue(), jsonData);

        return jsonRoot.toString();
    }

    /**
     * this method converts an item received in vcard format to an item in Funambol data model (contact)
     * @param vcardItem
     * @return
     */
    public JsonItem<String> fromRFC(String jsonRFC) {

        JSONObject jsonRoot = JSONObject.fromObject(jsonRFC);
        JSONObject jsonData = jsonRoot.getJSONObject(JsonTaskModel.DATA.getValue());
        JSONObject jsonItem = jsonData.getJSONObject(JsonTaskModel.ITEM.getValue());

        JsonItem<String> item = new JsonItem<String>();

        item.setContentType(jsonData.optString(JsonTaskModel.CONTENT_TYPE.getValue()));
        item.setKey(jsonItem.optString(JsonTaskModel.KEY.getValue()));
        item.setState(jsonItem.optString(JsonTaskModel.STATE.getValue()));

        String vcalItem = jsonItem.optString(JsonTaskModel.VCAL.getValue());
        vcalItem = vcalItem.replace("\\", "\\\\");

        item.setItem(vcalItem);

        return item;
    }


    //--------------------------------------------------------- Private Methods


    /**
     * 
     * @param value
     * @return
     */
    private int truncPercentComplete(String value){
        int percent = 0;
        try {
            double d = Double.parseDouble(value);
            percent = (int)d;
        } catch (Exception e){
            // do nothing
        }
        return percent;
    }

     /**
     * @param property
     * @return null if property is null otherwise return the property value as String
     */
    public static String getPropertyValueOrNull(Property property) {
    	if(property != null) {
            if (property.getPropertyValue() != null) {
                return property.getPropertyValueAsString();
            }
    	}
        return null;
    }

/*
 * This method was based on the analoguous defined in VCalendarContentConverter.
 * The main difference consists in the fact that this accepts a parameter
 * String of type String for specifying the timesone,
 * while the one defined in VCalendarContentConverter refers to an instance
 * property of type TimeZone.
 */

    private static void setAlarmTimeAndMinutes(Reminder reminder,
            String time,
            Property dtStart,
            boolean allDay,
            String tzid)
            throws ConverterException {

        String alarmStart;
        TimeZone reminderTimeZone = TimeZone.getTimeZone(tzid);

        String taskStartUTC = BaseConverter.handleConversionToUTCDate(dtStart.getPropertyValueAsString(), reminderTimeZone);

        // If the calendar is an all day event (or task) then
        // the aalarm time is considered as a local time
        // information consistently with the way start and end
        // dates are processed.
        if (allDay) {

            // Converts aalarm in local date and time to preserve
            // the distance from aalarm time to midnigth of the
            // start date.
            alarmStart =
                    BaseConverter.handleConversionToLocalDate(time, reminderTimeZone);

        } else {

            // Converts aalarm in UTC date and time to preserve
            // the absolute moment of the aalarm.
            alarmStart =
                    BaseConverter.handleConversionToUTCDate(time, reminderTimeZone);
        }
        reminder.setTime(alarmStart);

        if (dtStart != null) {
            reminder.setMinutes(
                    TimeUtils.getAlarmMinutes(
                    taskStartUTC,
                    reminder.getTime(),
                    null));
        } else {
            reminder.setMinutes(0);
        }
        reminder.setActive(true);
    }

}
