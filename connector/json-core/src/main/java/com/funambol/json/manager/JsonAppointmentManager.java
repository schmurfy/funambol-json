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
import com.funambol.common.pim.calendar.CalendarContent;
import com.funambol.common.pim.calendar.Event;
import com.funambol.framework.tools.merge.MergeResult;
import com.funambol.json.converter.Converter;
import com.funambol.json.dao.JsonDAO;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.engine.source.UtilitySyncSource;
import com.funambol.json.exception.DaoException;
import com.funambol.json.exception.HttpException;
import com.funambol.json.exception.MalformedJsonContentException;
import com.funambol.json.util.Utility;
import java.util.TimeZone;

/**
 * @version $Id: JsonAppointmentManager.java 51920 2010-06-04 05:10:49Z gazzaniga $
 */
public class JsonAppointmentManager extends JsonManagerImpl<Event> {

    public JsonAppointmentManager(JsonDAO dao, Converter<JsonItem<Event>> converter) {
        super(dao, converter);
    }

    /*
    @Override
    public Map<String, String> extractInformation(JsonItem<Event> item) {

    Map<String, String> parameters = new HashMap<String, String>();

    Event event = item.getItem();

    String summary   = event.getSummary().getPropertyValueAsString();
    if (summary != null) {
    parameters.put(JsonAppointmentModel.SUBJECT.getValue(), summary);
    } else {
    parameters.put(JsonAppointmentModel.SUBJECT.getValue(), "");
    }

    String start   = event.getDtStart().getPropertyValueAsString();
    if (start != null) {
    parameters.put("dtstart", start);
    } else {
    parameters.put("dtstart", "");
    }

    String end   = event.getDtEnd().getPropertyValueAsString();
    if (end != null) {
    parameters.put("dtend", end);
    } else {
    parameters.put("dtend", "");
    }

    String location   = event.getLocation().getPropertyValueAsString();
    if (location != null) {
    parameters.put(JsonAppointmentModel.LOCATION.getValue(), location);
    } else {
    parameters.put(JsonAppointmentModel.LOCATION.getValue(), "");
    }

    return parameters;
    }
     */
    /**
     * merges 2 items, and updates (if needed) the item in the backend
     * @param sessionID
     * @param serverItem
     * @param clientItem
     * @param since
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public boolean mergeExtendedItem(String token, JsonItem<Event> serverItem, JsonItem<Event> clientItem, long since) throws DaoException, MalformedJsonContentException {
        try {

            Event serverEvent = (Event) serverItem.getItem();
            Event clientEvent = (Event) clientItem.getItem();

            MergeResult mergeResult = clientEvent.merge(serverEvent);

            if (log.isTraceEnabled()) {
                log.trace("Merge procedure end. MergeResult: " + mergeResult);
            }

            if (mergeResult.isSetBRequired()) {
                // at this point the key value is A-123123
                String id = serverItem.getKey();
                id = Utility.removePrefix(id);
                serverItem.setKey(id);
                updateExtendedItem(token, serverItem, since);
            }

            return mergeResult.isSetARequired();

        } catch (HttpException re) {
            log.error("Failed the connection to the Json backend", re);
            throw new DaoException(re.getMessage(), re);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }

    /**
     * merges 2 items, and updates (if needed) the item in the backend
     *
     * @param sessionID
     * @param serverItem
     * @param clientItem
     * @param since
     * @param vcardIcalBackend
     * @param vcalFormat
     * @param rfcType
     * @param timezone
     * @param charset
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public boolean mergeRFCItem(String sessionID,
            JsonItem<Event> serverItem,
            JsonItem<Event> clientItem,
            long since,
            boolean vcardIcalBackend,
            boolean vcalFormat,
            String rfcType, // content type from client (usually vcal 1.0)
            TimeZone timezone,
            String charset) throws DaoException, MalformedJsonContentException {
        try {

            Event serverEvent = (Event) serverItem.getItem();
            Event clientEvent = (Event) clientItem.getItem();

            MergeResult mergeResult = clientEvent.merge(serverEvent);

            if (log.isTraceEnabled()) {
                log.trace("Merge procedure end. MergeResult: " + mergeResult);
            }

            if (mergeResult.isSetBRequired()) {

                // at this point the key value is A-123123
                String GUID = serverItem.getKey();
                String id = Utility.removePrefix(GUID);

                String status = String.valueOf(serverItem.getState());

                String objRFC = setItemForB(serverEvent,
                        vcardIcalBackend,
                        vcalFormat,
                        rfcType,
                        timezone,
                        charset);

                JsonItem<String> eventItem = new JsonItem<String>();
                eventItem.setKey(id);
                eventItem.setItem(objRFC);
                eventItem.setContentType(rfcType);
                eventItem.setState(status);

                updateRFCItem(sessionID, eventItem, since);

            }

            return mergeResult.isSetARequired();


        } catch (HttpException re) {
            log.error("Failed the connection to the Json backend", re);
            throw new DaoException(re.getMessage(), re);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new MalformedJsonContentException("The Json content is malformed!", ex);
        }

    }

    //---------------------------------------------------------- Private Methods
    /**
     *
     * @param serverEvent
     * @param since
     * @param vcardIcalBackend
     * @param vcalFormat
     * @param rfcType
     * @param timezone
     * @param charset
     * @throws java.lang.Exception
     */
    private String setItemForB(Event serverEvent,
            boolean vcardIcalBackend,
            boolean vcalFormat,
            String rfcType, // content type from client (usually vcal 1.0)
            TimeZone timezone,
            String charset) throws Exception {


        if (log.isTraceEnabled()) {
            log.trace("rfcType is          :" + rfcType);
            log.trace("vcardIcalBackend is :" + vcardIcalBackend); // here is true
            log.trace("vcalFormat is       :" + vcalFormat);
        }

        Calendar calendar = new Calendar();
        calendar.setEvent(serverEvent);

        // verify wich RFC should be crerated vcal or ical
        if (vcardIcalBackend && vcalFormat) {
            rfcType = UtilitySyncSource.VCAL_FORMAT;  // VCAL FORMAT - text/x-vcalendar
        } else {
            rfcType = UtilitySyncSource.ICAL_FORMAT;  // ICAL FORMAT - text/calendar
        }

        String objRFC = UtilitySyncSource.calendar2webCalendar(calendar, rfcType, timezone, charset);

        return objRFC;

    }

    /**
     *
     * @param item the event we want to check.
     *
     * @return true if at least one field used for the twin search in the given
     * event contains meaningful data, false otherwise
     */
    public boolean isTwinSearchAppliableOn(JsonItem<Event> item) {
        Event ee = (Event) item.getItem();
        if (ee != null) {

            if (ee == null) {
                return false;
            }


            boolean dtEnd = ee.getDtEnd() != null
                    && ee.getDtEnd().getPropertyValue() != null
                    && ee.getDtEnd().getPropertyValueAsString().length() > 0;


            boolean dtStart = ee.getDtStart() != null
                    && ee.getDtStart().getPropertyValue() != null
                    && ee.getDtStart().getPropertyValueAsString().length() > 0;
            return (dtEnd || dtStart);

        }
        return false;


    }
}
