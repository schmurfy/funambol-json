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
package com.funambol.json.utility;

/**
 * Contains some constants used inside this project.
 *
 * @version $Id$
 */
public interface Definitions {

    public final String LOG_NAME = "fake-json";
    public static final String PROPERTIES_PATH = "WEB-INF/classes";
    
    public final static String CONTACT = "contact";
    public final static String CONTACT_TYPE = "application/json-card";
    public final static String CONTACT_VCARD = "contactvcard";
    public final static String CONTACT_VCARD_TYPE = "application/json-vcard";

    public final static String APPOINTMENT = "appointment";
    public final static String APPOINTMENT_TYPE = "application/json-appointment";
    public final static String APPOINTMENT_VCAL = "appointmentvcal";
    public final static String APPOINTMENT_VCAL_TYPE = "application/json-vcal";

    public final static String NOTE = "note";
    public final static String NOTE_TYPE = "application/json-note";
    public final static String NOTE_PLAIN = "noteplain";
    public final static String NOTE_PLAIN_TYPE = "application/json-plain";

    public final static String TASK = "task";
    public final static String TASK_TYPE = "application/json-task";
    public final static String TASK_VCAL = "taskvcal";
    public final static String TASK_VCAL_TYPE = "application/json-vcal";

    public static final String SIFE_FORMAT = "text/x-s4j-sife"; // To be used as index for SIF-Event
    public static final String SIFT_FORMAT = "text/x-s4j-sift"; // To be used as index for SIF-Task
    public static final String VCAL_FORMAT = "text/x-vcalendar"; // To be used as index for VCal
    public static final String ICAL_FORMAT = "text/calendar"; // To be used as index for ICal

    public static final String JSON_EXTENDED = "Json-Extended";
    public final static String DATASTORETYPE = "DATASTORE_TYPE";

}
