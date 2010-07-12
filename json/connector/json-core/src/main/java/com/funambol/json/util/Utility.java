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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.sf.json.JSONObject;

import com.funambol.common.pim.note.Note;
import com.funambol.common.pim.common.Property;

import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;

import com.funambol.json.domain.JsonKeys;

/**
 * @version $Id$
 */ 
public class Utility {
    
    //---------------------------------------------------------------- Constants
    public static final String LOG_NAME = "json-connector";

    // default port for the unit test
    public final static int    PORT_DEFAULT  = 9292;

    public final static String URL_SEP = "/";
    public final static String GUID_SEP = "-";
    public final static String TOKEN_HEADER_NAME = "Authorization";
    public final static String APPOINTMENT_OBJECT = "A";
    public final static String TASK_OBJECT = "T";

    public final static String CONTENT_TYPE_CONTACT_EXT = "application/json-card";
    public final static String CONTENT_TYPE_CONTACT_RFC = "application/json-vcard";
    public final static String CONTENT_TYPE_APPOINTMENT_EXT = "application/json-appointment";
    public final static String CONTENT_TYPE_APPOINTMENT_RFC = "application/json-vcal";
    public final static String CONTENT_TYPE_TASK_EXT = "application/json-task";
    public final static String CONTENT_TYPE_TASK_RFC = "application/json-vcal";
    public final static String CONTENT_TYPE_NOTE_EXT = "application/json-note";    
    
    //
    // for the outlook there is just the DEFAULT_FOLDER
    //
    // for contact: DEFAULT_FOLDER = \\Personal Folders\Contacts (language dependent)
    // for contact: DEFAULT_FOLDER = \\Personal Folders\Calendar (language dependent)
    // for contact: DEFAULT_FOLDER = \\Personal Folders\Tasks    (language dependent)
    // for contact: DEFAULT_FOLDER = \\Personal Folders\Notes    (language dependent)
    //
    public final static String OUTLOOK_FOLDER_PREFIX          = "DEFAULT_FOLDER";
    
    
    // prefix for back-end
    // 
    public final static String BACKEND_CONTACT_FOLDER_PREFIX  = "";
    public final static String BACKEND_CALENDAR_FOLDER_PREFIX = "Root\\Calendar";
    public final static String BACKEND_TASKS_FOLDER_PREFIX    = "Root\\Tasks";
    public final static String BACKEND_NOTES_FOLDER_PREFIX    = "Root\\Notes";

    public final static int DATETIME_YYYYMMDDTHHMMSSZ = 0;
    public final static int DATETIME_YYYYMMDDTHHMMSS  = 1;
    public final static int DATETIME_YYYYMMDD         = 2;
    public final static int DATETIME_YYYY_MM_DD       = 3;
    public final static String TIME_ALLDAY_START      = "000000";
    public final static String TIME_ALLDAY_END        = "235900";

    public static final short SENSITIVITY_NORMAL       = 0             ;
    public static final short SENSITIVITY_PERSONAL     = 1             ;
    public static final short SENSITIVITY_PRIVATE      = 2             ;
    public static final short SENSITIVITY_CONFIDENTIAL = 3             ;
    public static final String CLASS_PUBLIC            = "PUBLIC"      ;
    public static final String CLASS_PRIVATE           = "PRIVATE"     ;
    public static final String CLASS_CONFIDENTIAL      = "CONFIDENTIAL";
    public static final String CLASS_CUSTOM            = "X-PERSONAL"  ;

    // ---------------------------------------------------------- Protected data
    protected static final FunambolLogger log =
        FunambolLoggerFactory.getLogger(Utility.LOG_NAME);

    // ---------------------------------------------------------- Public methods

    /**
     * 
     * @param key
     * @return
     */
    public static String getUrl(String serverUrl,
            String resourceType,
            String key) {
        return serverUrl + URL_SEP + resourceType + URL_SEP + key;
    }
        
    /**
     * 
     * @param key
     * @return
     */
    public static String getUrl(String serverUrl,
            String key) {
        return serverUrl + URL_SEP + key;
    }

   /**
     *
     * @param key
     * @return
     */
    public static String getUrl(String schema,
                                String server,
                                int port,
                                String key) {
        return schema + "://" + server + ":" + port + URL_SEP + "syncapi" + URL_SEP + key;
    }
    
    /**
     * Used by the
     * - getAll
     * - getNew
     * - getUpdated
     * - getDeleted
     * - getTwin
     * 
     * @param prefix
     * @param keys
     * @return
     */
    public static JsonKeys addPrefix(String prefix, JsonKeys keys){
        for (int i=0; i<keys.getKeys().length; i++){
            keys.getKeys()[i] = prefix + GUID_SEP + keys.getKeys()[i];
        }
        return keys;
    }

    /**
     * Used by the
     * - addItem
     * 
     * @param prefix
     * @param key
     * @return
     */
    public static String addPrefix(String prefix, String key){
        if (key == null){
            return null;
        }
        key = prefix + GUID_SEP + key;
        return key;
    }

    
    /**
     * Used by the
     * - getItem
     * - removeItem
     * 
     * @param key
     * @return
     */
    public static String removePrefix(String guid){
        int index = guid.indexOf(GUID_SEP);
        String id = guid.substring(index+1);        
        return id;
    }
    
    
    /**
     * converts a note object in plain text note
     * @param note
     * @return
     */
    public static String noteToPlainText(Note note) {
        StringBuilder buffer = new StringBuilder();
        if (note.getSubject() != null && note.getSubject().getPropertyValueAsString() != null) {
            buffer.append(note.getSubject().getPropertyValueAsString());
        }
        if (note.getTextDescription() != null && note.getTextDescription().getPropertyValueAsString() != null) {
            if (buffer.length() > 0) {
                buffer.append("\n");
            }
            buffer.append(note.getTextDescription().getPropertyValueAsString());
        }
        return buffer.toString();
    }

    /**
     * convertsa  plaintext note to a note object
     * @param content
     * @return
     */
    public static Note plainTextToNote(String content) {
        Note note = new Note();
        if (content.contains("\n")) {
            String[] parts = content.split("\n");
            if (parts.length > 1) {
                note.getSubject().setPropertyValue(parts[0]);
                note.getTextDescription().setPropertyValue(parts[1]);
            } else {
                note.getSubject().setPropertyValue(parts[0]);
            }
        } else {
            note.getSubject().setPropertyValue(content);
        }
        return note;
    }
    
    /**
     * @param property 
     * @return empty string if property is null otherwise return the property value as String
     */
    public static String getPropertyValue(String property) {
    	if(property != null) {
            return property;
    	}
        return "";
    }

     /**
     * 
     * 
     * @param folder
     * @param prefix
     * @return
     */
    public static String folderConverterC2S(String folder, String prefix) {
        if (!"".equals(prefix) && folder != null) {
            if (folder.startsWith(OUTLOOK_FOLDER_PREFIX)) {
                folder = folder.substring(OUTLOOK_FOLDER_PREFIX.length(), folder.length());
            folder = prefix + folder;

        }        
        }
        return folder;
    }
    
    /**
     * 
     * 
     * @param folder
     * @param prefix
     * @return
     */
    public static String folderConverterS2C(String folder, String prefix){        
        if (!"".equals(prefix) && folder!=null){
            if (folder.startsWith(prefix)){
                folder = folder.substring(prefix.length(),folder.length());
                folder = OUTLOOK_FOLDER_PREFIX + folder;
            }        
        }
        return folder;
    } 
    
    /**
     * the Foundation object must have all the fields
     * in order to allow the reset of the fields when the field is empty
     * 
     * 
     * @param json
     * @param label
     * @return value
     */
    public static String getJsonValue(JSONObject json, String label) {
        String value = null;
        value = json.optString(label,null);
        return value;
    }
    
    /**
     * the Foundation object must have all the fields
     * in order to allow the reset of the fields when the field is empty
     * 
     * @param json
     * @param label
     * @return value
     */
    public static boolean getJsonValueBoolean(JSONObject json, String label) {
        String value = null;
        value = json.optString(label);
        if ( value == null ||  value.equals("") || !value.equals("true") ){
            return false;
        } 
        return Boolean.parseBoolean(value);
    }
    
    /**
     * the Foundation object must have all the fields
     * in order to allow the reset of the fields when the field is empty
     * 
     * @param json
     * @param label
     * @return value the value of the field; 0 if there is an error
     */
    public static int getJsonValueInt(JSONObject json, String label) {
        int value = 0;
        try {
            String v = json.optString(label);
            if ( v == null || v.equals("") ){
                return 0;
            } else {
                value = Integer.parseInt(v);
            }
        } catch (Exception e) {
            // do nothing
        }
        return value;
    }

        /**
     * the Foundation object must have all the fields
     * in order to allow the reset of the fields when the field is empty
     *
     * @param json
     * @param label
     * @return value the value of the field; 0 if there is an error
     */
    public static short getJsonValueShort(JSONObject json, String label) {
        short value = 0;
        try {
            String v = json.optString(label);
            if ( v == null || v.equals("") ){
                return 0;
            } else {
                value = Short.parseShort(v);
            }
        } catch (Exception e) {
            // do nothing
        }
        return value;
    }

            /**
     * the Foundation object must have all the fields
     * in order to allow the reset of the fields when the field is empty
     *
     * @param json
     * @param label
     * @return value the value of the field; null if it does not exist
     */
    public static Short getJsonValueShortOrNull(JSONObject json, String label) {
        short value = 0;
        try {
            String v = json.optString(label);
            if ( v == null || v.equals("") ){
                return null;
            } else {
                value = Short.parseShort(v);
            }
        } catch (Exception e) {
            // do nothing
        }
        return value;
    }

    
    /**
     * @param property 
     * @return null if property is null otherwise return the property value as String
     */
    public static String getPropertyValue(Property property) {
    	if(property != null) {
            if (property.getPropertyValue() != null) {
                return property.getPropertyValueAsString();
            } 
    	}
        return "";
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

    
    /**
     * @param property 
     * @return null if property is null otherwise return the property value as String
     */
    public static int getPropertyValueInt(Integer property) {
    	if(property != null) {
            return property.intValue();
    	} 
    	return 0;
    }

    /**
     * @param property 
     * @return null if property is null otherwise return the property value as String
     */
    public static short getPropertyValueShort(Short property) {
    	if(property != null) {
            return property.shortValue();
    	} 
    	return 0;
    }

        /**
     * @param property
     * @return null if property is null otherwise return the property value as Short
     */
    public static Short getPropertyValueShortOrNull(Short property) {
    	if(property != null) {
            return property.shortValue();
    	}
    	return null;
    }
    
    
    /**
     * 
     * 
     * 
     * 
     * @param in
     * @return
     * @throws java.lang.NumberFormatException
     */
    public static int importanceClient2Server(int in) throws NumberFormatException {
        switch (in) {
            // some nokia phone sends the 0 (undefined)
            case 0:
            // Hi priority
            case 1:
            case 2:
            case 3:
            case 4:
                return 2;
            // Middle priority
            case 5:
                return 1;
            // Low priority
            case 6:
            case 7:
            case 8:
            case 9:
                return 0;
            default:
                throw new NumberFormatException(); // will be caught
        }
    }
    
    /**
     * 
     * TEMPORARY PATCH. 
     * this patch is used because the common pim framework
     * handles the priority of a task
     *
     *  protected static int importance19To02(int oneToNine) throws NumberFormatException {
     *   switch (oneToNine) {
     *      case 1:
     *      case 2:
     *      case 3:
     *      case 4:
     *          return 2;
     *      case 5:
     *          return 1;
     *      case 6:
     *      case 7:
     *      case 8:
     *      case 9:
     *          return 0;
     *      default:
     *          throw new NumberFormatException(); // will be caught
     *   }
     *  }
     * 
     * 
     * @param in
     * @return
     * @throws java.lang.NumberFormatException
     */
    public static int importanceServer2Client(int in) throws NumberFormatException {
        switch (in) {
            case 0:
                return 9;
            case 1:
                return 5;
            case 2:
                return 1;
            default:
                throw new NumberFormatException("Error converting importance " + in); // will be caught
        }
    }

    /**
     *
     * convert the format of the date
     * startdate
     * endDate
     * patternStartDate
     * patternEndDate
     * available formats:
     *  - "yyyyMMdd'T'HHmmss'Z'" format (zulu time)
     *  - "yyyyMMdd'T'HHmmss" format (local time)
     *  - "yyyyMMdd" format (all day local time)
     *  - "yyyy-MM-dd" format; this is not standard time format
     *
     * @param inDate date that must be parsed
     * @param tmzid timezone ID
     * @param time optional time substring ("00000" or "235900")
     * @param format (1,2,3,4 formats)
     * @return
     */
    public static String changeDateFormat(String inDate, String tmzid, String time, int format)  {

        if(inDate == null){
            return null;
        }

        DateFormat[] DATETIME_FORMATS = new DateFormat[4];

        DATETIME_FORMATS[DATETIME_YYYYMMDDTHHMMSSZ] = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        DATETIME_FORMATS[DATETIME_YYYYMMDDTHHMMSSZ].setTimeZone(TimeZone.getTimeZone("UTC"));

        DATETIME_FORMATS[DATETIME_YYYYMMDDTHHMMSS] = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

        DATETIME_FORMATS[DATETIME_YYYYMMDD]        = new SimpleDateFormat("yyyyMMdd");

        DATETIME_FORMATS[DATETIME_YYYY_MM_DD]      = new SimpleDateFormat("yyyy-MM-dd");


        if ("".equals(inDate)) {
            return "";
        }
        
        String inDateWithTime = inDate;
        if ((time != null) && (inDate.length() < 15)) {
            inDateWithTime = inDate.replaceAll("-", "") + 'T' + time;
        }

        if (tmzid != null) {
            TimeZone tz = TimeZone.getTimeZone(tmzid);
            // the Locale is not important because we have no language
            // dependent date (i.e. "Thu, 25 Dec 2008 19:25:29 PST"
            // in this case we should put Locale.US)
            DATETIME_FORMATS[DATETIME_YYYYMMDDTHHMMSS].setTimeZone(tz);
            DATETIME_FORMATS[DATETIME_YYYYMMDD].setTimeZone(tz);
            DATETIME_FORMATS[DATETIME_YYYY_MM_DD].setTimeZone(tz);
        }

        Date date = null;
        Exception exception = null;
        for (DateFormat p : DATETIME_FORMATS) {
            p.setLenient(false); // strict!
            try {
                date = p.parse(inDateWithTime);
                break;
            } catch (Exception e) {
                exception = e;
                continue;
            }
        }
        if (date == null) {
            log.error("Error converting date [" + inDateWithTime + "]", exception);
            return inDate; // returns the input unchanged
        } else {
            String outDate = DATETIME_FORMATS[format].format(date);
            if (outDate == null) { // conversion failed
                return inDate; // returns the input unchanged
            }
            return outDate;
        }
    }

    /**
     * Retrieves the date pattern.
     *
     * @param date the date to get the format
     * @return String the pattern
     */
    public static String getDateFormat(String date) {

        if (date == null || date.equals("")) {
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        String[] patterns = new String[] {"yyyyMMdd'T'HHmmss'Z'",
                                          "yyyyMMdd'T'HHmmss",
                                          "yyyyMMdd",
                                          "yyyy-MM-dd"};
        int[] patternsLength = new int[] {16, 15, 8, 10};

        int s = patterns.length;
        Date d = null;
        for (int i=0; i<s; i++) {
            try {
                dateFormat.applyPattern(patterns[i]);
                dateFormat.setLenient(true);
                d = dateFormat.parse(date);

                if (date.length() == patternsLength[i]) {
                    return patterns[i];
                }

            } catch(Exception e) {
                continue;
            }
        }
        return null;
    }


    public static short accessClassTo03(String accessClass) {
        if (accessClass == null) {
            return SENSITIVITY_NORMAL; // default
        }

        if (accessClass.equals(CLASS_PUBLIC)) {
            return SENSITIVITY_NORMAL;
        }

        if (accessClass.equals(CLASS_PRIVATE)) {
            return SENSITIVITY_PRIVATE;
        }

        if (accessClass.equals(CLASS_CONFIDENTIAL)) {
            return SENSITIVITY_CONFIDENTIAL;
        }

        return SENSITIVITY_PERSONAL; // custom value
    }

    public static String accessClassFrom03(Short zeroToThree) {

        if (zeroToThree == null) {
            return CLASS_PUBLIC;
        }

        switch(zeroToThree.shortValue()) {
            case SENSITIVITY_PRIVATE:
                return CLASS_PRIVATE;

            case SENSITIVITY_CONFIDENTIAL:
                return CLASS_CONFIDENTIAL;

            case SENSITIVITY_PERSONAL:
                return CLASS_CUSTOM;

            case SENSITIVITY_NORMAL:
            default:
                return CLASS_PUBLIC;
        }
    }

}
