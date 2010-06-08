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
import com.funambol.common.pim.calendar.Task;
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
 * @version $Id: JsonTaskManager.java 51920 2010-06-04 05:10:49Z gazzaniga $
 */
public class JsonTaskManager extends JsonManagerImpl<Task> {

    public JsonTaskManager(JsonDAO dao, Converter<JsonItem<Task>> converter) {
        super(dao, converter);
    }

    /*
    @Override
    public Map<String, String> extractInformation(JsonItem<Task> item) {        

    Map<String, String> parameters = new HashMap<String, String>();

    Task task = item.getItem();

    String summary   = task.getSummary().getPropertyValueAsString();
    if (summary != null) {
    parameters.put(JsonAppointmentModel.SUBJECT.getValue(), summary);
    } else {
    parameters.put(JsonAppointmentModel.SUBJECT.getValue(), "");
    }

    String start   = task.getDtStart().getPropertyValueAsString();
    if (start != null) {
    parameters.put("dtstart", start);
    } else {
    parameters.put("dtstart", "");
    }

    //String end   = task.getDtEnd().getPropertyValueAsString();
    String end   = task.getDueDate().getPropertyValueAsString();
    if (end != null) {
    parameters.put("dtend", end);
    } else {
    parameters.put("dtend", "");
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
    public boolean mergeExtendedItem(String token, JsonItem<Task> serverItem, JsonItem<Task> clientItem, long since) throws DaoException, MalformedJsonContentException {
        try {

            Task serverTask = (Task) serverItem.getItem();
            Task clientTask = (Task) clientItem.getItem();

            MergeResult mergeResult = clientTask.merge(serverTask);

            if (log.isTraceEnabled()) {
                log.trace("Merge procedure end. MergeResult: " + mergeResult);
            }

            if (mergeResult.isSetBRequired()) {
                // at this point the key value is T-123123
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
     * @param sessionID
     * @param serverItem
     * @param clientItem
     * @param since
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public boolean mergeRFCItem(String sessionID,
            JsonItem<Task> serverItem,
            JsonItem<Task> clientItem,
            long since,
            boolean vcardIcalBackend,
            boolean vcalFormat,
            String rfcType,
            TimeZone timezone,
            String charset) throws DaoException, MalformedJsonContentException {
        try {

            Task serverTask = (Task) serverItem.getItem();
            Task clientTask = (Task) clientItem.getItem();

            MergeResult mergeResult = clientTask.merge(serverTask);

            if (log.isTraceEnabled()) {
                log.trace("Merge procedure end. MergeResult: " + mergeResult);
            }

            if (mergeResult.isSetBRequired()) {
                Calendar calendar = new Calendar();
                calendar.setTask(serverTask);
                String objRFC = UtilitySyncSource.calendar2webCalendar(calendar, rfcType, timezone, charset);

                JsonItem<String> eventItem = new JsonItem<String>();
                // at this point the key value is A-123123
                String GUID = serverItem.getKey();
                String id = Utility.removePrefix(GUID);
                eventItem.setKey(id);
                eventItem.setItem(objRFC);
                eventItem.setContentType(rfcType);
                eventItem.setState(String.valueOf(serverItem.getState()));
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

    /**
     *
     * @param item the task we want to check.
     *
     * @return true if at least one field used for the twin search in the given
     * task contains meaningful data, false otherwise
     */
    public boolean isTwinSearchAppliableOn(JsonItem<Task> item) {
        Task task = (Task) item.getItem();
        boolean dtEnd = task.getDtEnd() != null
                && task.getDtEnd().getPropertyValue() != null
                && task.getDtEnd().getPropertyValueAsString().length() > 0;

        boolean subject = task.getSummary() != null
                && task.getSummary().getPropertyValue() != null
                && task.getSummary().getPropertyValueAsString().length() > 0;

        return (dtEnd || subject);

    }
}
