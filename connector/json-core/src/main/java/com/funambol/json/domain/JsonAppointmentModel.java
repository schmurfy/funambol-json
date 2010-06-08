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
package com.funambol.json.domain;

/**
 * @version $Id: JsonAppointmentModel.java 51920 2010-06-04 05:10:49Z gazzaniga $
 */
public enum JsonAppointmentModel {
    
    DATA("data"),
    CONTENT_TYPE("content-type"),
    ITEM("item"),
    KEY("key"),
    STATE("state"),
    FOLDER("folder"),
    VCAL("vcal"),

    ALL_DAY("allDay"),
    START_DATE("startDate"),
    END_DATE("endDate"),
    TZID("tzid"),
    SUBJECT("subject"),
    BODY("body"),
    LOCATION("location"),
    REMINDER("reminder"),
    REMINDER_TIME("reminderTime"),
    REMINDER_SOUNDFILE("reminderSoundFile"),
    SHOW_TIME_AS("busyStatus"),
    CATEGORIES("categories"),
    SENSITIVITY("sensitivity"),
    IMPORTANCE("importance"),
    ORGANIZER("organizer"),
    
    DAY_OF_MONTH("dayOfMonth"),
    DAY_OF_WEEK_MASK("dayOfWeekMask"),
    INSTANCE("instance"),
    INTERVAL("interval"),
    IS_RECURRING("isRecurring"),
    MONTH_OF_YEAR("monthOfYear"),
    NO_END_DATE("noEndDate"),
    OCCURRENCES("occurrences"),
    EXCEPTIONS_EXCLUDED("exceptionsExcluded"),
    EXCEPTIONS_INCLUDED("exceptionsIncluded"),
    PATTERN_END_DATE("patternEndDate"),
    PATTERN_START_DATE("patternStartDate"),
    RECURRENCE_TYPE("recurrenceType");
    
    private final String value;
    
    private JsonAppointmentModel(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
