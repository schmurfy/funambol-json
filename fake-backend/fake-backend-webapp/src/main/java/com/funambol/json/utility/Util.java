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

import com.funambol.json.security.JsonUser;
import com.funambol.json.server.JsonServlet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

/**
 *
 * @author sergio
 */
public class Util {

    private final static Logger log = Logger.getLogger(Definitions.LOG_NAME);

    public static String getJsonkeysFromList(List list) {

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONArray jsonKeys = new JSONArray();

        Iterator it = list.iterator();
        while (it.hasNext()) {
            jsonKeys.add(it.next());

        }
        jsonData.put("keys", jsonKeys);
        jsonRoot.put("data", jsonData);
        log.trace("reponse:" + jsonRoot.toString());
        return jsonRoot.toString();

    }

    public static JsonUser getCredentialFromJson(String jsonContent) {
        //  {"data":{"credentials":{"user":"paulo","pass":"paulo"}}}
        JSONObject json = JSONObject.fromObject(jsonContent);
        return new JsonUser(json.getJSONObject("data").getJSONObject("credentials").getString("user"), json.getJSONObject("data").getJSONObject("credentials").getString("pass"));
    }

    /**
     * Extracts the since parameter from the request and parses it,
     * returning a long value.
     * If no parameter is found or its value is incorrect, the current
     * time is returned.
     *
     * @param request the httprequest object containing the since parameter.
     *
     * @return a long meaning the since value
     */
    public static long getSince(HttpServletRequest request) {
        String tmp = request.getParameter(JsonServlet.SINCE);
        if (tmp != null) {
            try {
                return Long.parseLong(tmp);
            } catch (NumberFormatException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("An error occurred while parsing [" + tmp + "].");
                }
            }
        }
        return System.currentTimeMillis();
    }

    public static String buildErrorMessage(String code, String message) {

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();


        jsonData.put("code", code);
        jsonData.put("message", message);
        jsonRoot.put("error", jsonData);

        return jsonRoot.toString();
    }

    public static String getDateFromLong(long time) {
        Date date = new Date(time);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

        return sdf.format(date);

    }
}
