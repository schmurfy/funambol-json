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
import com.funambol.common.pim.calendar.CalendarContent;
import com.funambol.framework.engine.SyncItem;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.domain.JsonKeys;
import com.funambol.json.exception.DaoException;
import com.funambol.json.exception.MalformedJsonContentException;
import com.funambol.json.manager.JsonManager;
import java.util.TimeZone;


/**
 * @version $Id: CalendarSyncSourceStrategy.java 51920 2010-06-04 05:10:49Z gazzaniga $
 */
public interface CalendarSyncSourceStrategy {

    public String add(String token, Calendar calendar, String content, boolean vcardIcalBackend, long since) throws MalformedJsonContentException, DaoException;

    public String update(String sessionID, Calendar calendar, SyncItem syncItem, String content, boolean vcardIcalBackend, long since) throws MalformedJsonContentException, DaoException;

    public void remove(String token, String GUID, long since) throws MalformedJsonContentException, DaoException;

    public boolean merge(String token, String GUID, Calendar calendar, SyncItem syncItem, boolean vcardIcalBackend, boolean vcalFormat, String vCalType, TimeZone deviceTimeZone, String deviceCharset, long since) throws DaoException, MalformedJsonContentException, Exception;

    public JsonKeys getAllItemsKey(String token) throws DaoException, MalformedJsonContentException;

    public JsonKeys getNewItemKeys(String token, long since, long until) throws DaoException, MalformedJsonContentException;

    public JsonKeys getUpdatedItemKeys(String token, long since, long until) throws DaoException, MalformedJsonContentException;

    public JsonKeys getDeletedItemKeys(String token, long since, long until) throws DaoException, MalformedJsonContentException;

    public JsonKeys getKeysFromTwin(String token, Calendar calendar, SyncItem syncItem, String content, boolean vcardIcalBackend) throws MalformedJsonContentException, DaoException;

    public JsonItem<String> getRFCItem(String token, String GUID) throws MalformedJsonContentException, DaoException, Exception;

    public JsonItem<CalendarContent> getExtendedItem(String token, String GUID) throws MalformedJsonContentException, DaoException, Exception;

    public JsonManager getManager();
    
}