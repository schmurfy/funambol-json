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

import java.util.Collection;
import java.util.Map;
import com.funambol.common.pim.calendar.CalendarContent;
import com.funambol.common.pim.calendar.Event;
import com.funambol.common.pim.calendar.Task;
import com.funambol.json.util.Utility;


/**
 * @version $Id: CalendarStrategyFactory.java 51920 2010-06-04 05:10:49Z gazzaniga $
 */
public class CalendarStrategyFactory {

    //private static Map<String, CalendarSyncSourceStrategy> strategies;
    
    /**
     * 
     * @param _strategies
     */
   // public static void setStrategies(Map<String, CalendarSyncSourceStrategy> _strategies){
   //     strategies = _strategies;
   // } 
    
    
    /**
     * get the strategy using the content
     * used in the 
     * - add
     * - update
     * - getTwin
     * 
     * @param calendarContent
     * @return
     */
    public static CalendarSyncSourceStrategy get( Map<String, CalendarSyncSourceStrategy> strategies,CalendarContent calendarContent) {
        String calendarClassName = calendarContent.getClass().getName();
        CalendarSyncSourceStrategy cstrategy = strategies.get(calendarClassName);
        return cstrategy;
    }
    
    /**
     * get the strategy using the GUID
     * used in the
     * - get
     * - remove
     * 
     * @param guid Map<String, CalendarSyncSourceStrategy> strategies,
     * @return
     */
    public static CalendarSyncSourceStrategy getById( Map<String, CalendarSyncSourceStrategy> strategies,String guid) {
        String prefix = guid.substring(0, 1);
        String calendarClassName = null;
        if (prefix.equals(Utility.APPOINTMENT_OBJECT)){
            // appointment
            calendarClassName = Event.class.getName();
        } else {
            // task          
            calendarClassName = Task.class.getName();
        }
        CalendarSyncSourceStrategy cstrategy = strategies.get(calendarClassName);
        return cstrategy;
    }
    
    
    /**
     * 
     * used in the
     * - beginSync
     * - endSync
     * - getAll
     * - getNew
     * - getUpdated
     * - getDeleted
     * 
     * @param calendarClass
     * @return
     */
    public static Collection<CalendarSyncSourceStrategy> getStrategies( Map<String, CalendarSyncSourceStrategy> strategies,String calendarClass) {

        if (calendarClass.equals(Event.class.getName())) {
            // AppointmentSyncSourceStrategy if the syncsource is the 
            // fmvevent (Internet Cal Format) - fmsevent (SIF)
            strategies.remove(Task.class.getName());
        } else if (calendarClass.equals(Task.class.getName())) {
            // TaskSyncSourceStrategy if the sync source is the 
            // fmvtask (Iternet Cal Format) - fmstask (SIF)
            strategies.remove(Event.class.getName());
        } else {
            // if calendarClass.equals(CalendarContent.class.getName()
            // both if the syncsource is fmvcal (it keeps vevent and vtodo)
            // note: there is no a SIF object with both appoint. and task
        }
            
        return strategies.values();
    }
    
}
