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
package com.funambol.json.server;

import com.funambol.framework.server.Sync4jUser;
import com.funambol.json.data.FakeJsonItem;
import com.funambol.json.data.Repository;
import com.funambol.json.utility.Definitions;
import com.funambol.json.utility.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class for Servlet: JsonServlet
 *
 */
public class JsonServlet extends HttpServlet implements javax.servlet.Servlet {

    public final static long serialVersionUID = 1L;
    public Logger log = Logger.getLogger(Definitions.LOG_NAME);
    public static final String CHARSET = "UTF-8";
    /**
     * allowed source names and types - start
     */
    /**
     * allowed source names and types - end
     */
    public final static String LOGIN = "login";
    public final static String EMPTY = "empty";
    public final static String ITEMS = "items";
    public final static String KEYS = "keys";
    public final static String SINCE = "since";
    public final static String BAD_REQUEST = "406";
    public final static String UNAUTHORIZED = "401";
    public final static String INTERNAL_SERVER_ERROR = "500";
    public final static String ENABLE_CONFIGURATION = "enableConfiguration";
    private String loginResponse = "{\"data\":{\"sessionid\":\"dasdsadsad\"}}";
    private String errorResponse = "{\"error\":{\"code\":\"item-1004\",\"message\":\"runtime exception\",\"parameters\":[{\"param\":\"value\"},{\"param2\":\"value2\"}]}}";
    private String keyNotFoundResponse = "{\"error\":{\"code\":\"ERR_INT_FOUND\",\"message\":\"Item not found\"}}";
    private String responseString = "";
    private boolean enableConfiguration = false;
    private Repository rep;

    public JsonServlet() {
        super();

    }

    @Override
    public void init() {
        String temp = getInitParameter(ENABLE_CONFIGURATION);
        this.enableConfiguration = temp != null ? Boolean.parseBoolean(temp) : false;
    }

    /**
     *
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (log.isTraceEnabled()) {
            log.trace("doGet  -------------------------------------------------------");
        }

        request.setCharacterEncoding("UTF-8");
        Request req = getRequestParsed(request.getRequestURI());
        logRequestInfo(req, null);
        responseString = "";

        long since = 0;
        long until = 0;

        try {
            if (rep.sourceContains(req.getResource())) {
                switch (req.getActionID()) {
                    /**
                     * GET    /{resource-type}/keys/all
                     * GET    /{resource-type}/keys/new
                     * GET    /{resource-type}/keys/updated
                     * GET    /{resource-type}/keys/deleted
                     * GET    /{resource-type}/keys/twins
                     * GET    /{resource-type}/items/{resource-key}
                     * @param url
                     */
                    case Request.ITEMS:
                        responseString = rep.getContent(req.getResource(), req.getKey());
                        break;
                    case Request.SYNC:
                        break;
                    case Request.KEYS:
                        switch (req.getTypeID()) {
                            case Request.KEYS_ALL:
                                responseString = Util.getJsonkeysFromList(rep.getAllKeys(req.getResource(), new Timestamp(since), new Timestamp(until)));

                                break;
                            case Request.KEYS_NEW:
                                since = Long.valueOf(request.getParameter("since"));
                                until = Long.valueOf(request.getParameter("until"));

                                responseString = Util.getJsonkeysFromList(rep.getNewKeys(req.getResource(), new Timestamp(since), new Timestamp(until)));

                                break;
                            case Request.KEYS_UPDATED:
                                since = Long.valueOf(request.getParameter("since"));
                                until = Long.valueOf(request.getParameter("until"));
                                responseString = Util.getJsonkeysFromList(rep.getUpdatedKeys(req.getResource(), new Timestamp(since), new Timestamp(until)));

                                break;
                            case Request.KEYS_DELETED:
                                since = Long.valueOf(request.getParameter("since"));
                                until = Long.valueOf(request.getParameter("until"));
                                responseString = Util.getJsonkeysFromList(rep.getDeletedKeys(req.getResource(), new Timestamp(since), new Timestamp(until)));

                                break;

                        }
                        break;
                    default:
                        break;
                }

            } else {
                switch (req.getResourceID()) {
                    /**
                     * request server time
                     */
                    case Request.CONFIG:
                        if (req.getActionID() == Request.TIME) {
                            if (!this.enableConfiguration) {
                                responseString = errorResponse;
                            } else {
                                JSONObject jo = new JSONObject();
                                JSONObject jdata = new JSONObject();
                                DateFormat f = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
                                jdata.put("time", f.format(new Date(System.currentTimeMillis())));
                                jdata.put("tzid", TimeZone.getDefault().getID());
                                jo.elementOpt("data", jdata);
                                responseString = jo.toString();
                            }
                        }
                        break;
                    /**
                     * empty request eg host:<port>/syncapi
                     */
                    case Request.EMPTY:
                        responseString = "Request to " + request.getRequestURI();
                        break;
                    default:
                        break;
                }
            }

        } catch (Exception ex) {
            responseString = errorResponse;
        } finally {
            writeResponse(response);
        }

    }

    /**
     *
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (log.isTraceEnabled()) {
            log.trace("doPost  -------------------------------------------------------");
        }
        
        request.setCharacterEncoding("UTF-8");
        Request req = getRequestParsed(request.getRequestURI());
        String content = getContent(request.getReader());
        logRequestInfo(req, content);
        responseString = "";
        
        try {
            if (rep != null && rep.sourceContains(req.getResource())) {
                switch (req.getActionID()) {

                    /**
                     * POST   /{resource-type}/items
                     * POST   /{resource-type}/sync/end
                     * POST   /{resource-type}/sync/begin
                     * POST                   /auth/logout
                     * POST                   /auth/login
                     * @param url
                     */
                    case Request.SYNC:
                        if (req.getTypeID() == Request.BEGIN) {
                            responseString = "{\"data\":{\"preferred-content-type\":\"" + rep.getSourceTypeByResource(req.getResource()) + "\"}}";
                        } else if (req.getTypeID() == Request.END) {
                            //say nothing
                            responseString = "";
                        }
                        break;

                    /**
                     *
                     * inserts an item
                     */
                    case Request.ITEMS:
                        try {
                            String key = rep.addItem(req.getResource(), new FakeJsonItem(content, Util.getSince(request)));
                            responseString = "{\"data\":{\"key\": \"" + key + "\"}}";
                        } catch (Exception ex) {
                            response.setStatus(500);
                            responseString = Util.buildErrorMessage("code-0", ex.getMessage());
                        }
                        break;

                    case Request.KEYS:
                        if (req.getTypeID() == Request.KEYS_TWINS) {
                            responseString = Util.getJsonkeysFromList(rep.getTwins(req.getResource(), new FakeJsonItem(content, Util.getSince(request))));
                        } else {
                            responseString = errorResponse;
                        }
                        break;

                    default:
                        break;
                }
            } else {

                /*
                 * POST                   /auth/logout
                 * POST                   /auth/login
                 */
                switch (req.getResourceID()) {
                    case Request.AUTH:
                        if (req.getActionID() == Request.LOGIN) {

                            Sync4jUser user = Repository.login(Util.getCredentialFromJson(content));

                            if (user != null) {

                                if (user != null) {
                                    JSONObject obj = JSONObject.fromObject(loginResponse);
                                    obj.getJSONObject("data").put("sessionid", user.getUsername() + ":" + Util.getDateFromLong(System.currentTimeMillis()));
                                    if (Repository.customerUserid) {
                                        obj.getJSONObject("data").put("customeruserid", "customeruserid:" + user.getUsername());
                                    }
                                    responseString = obj.toString();
                                    rep = new Repository(user.getUsername(), TimeZone.getDefault(), "UTF-8");
                                } else {
                                    responseString = errorResponse;
                                }
                            }

                        } else if (req.getActionID() == Request.LOGOUT) {
                            //say nothing
                            responseString = "";
                        }
                        break;

                    default:
                        break;

                }
            }
        } catch (Exception ex) {
            responseString = errorResponse;
        } finally {
            writeResponse(response);
        }
    }

    /**
     *
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (log.isTraceEnabled()) {
            log.trace("doPut  -------------------------------------------------------");
        }

        request.setCharacterEncoding("UTF-8");
        Request req = getRequestParsed(request.getRequestURI());
        String content = getContent(request.getReader());
        logRequestInfo(req, content);
        responseString = "";

        try {
            if (rep != null && rep.sourceContains(req.getResource())) {
                switch (req.getActionID()) {

                    /**
                     * PUT    /{resource-type}/items/{resource-key}
                     * @param url
                     */
                    case Request.ITEMS:
                        try {
                            rep.updateItem(req.getResource(), new FakeJsonItem(content, Util.getSince(request)), req.getKey());
                            responseString = "";
                        } catch (Exception ex) {
                            response.setStatus(500);
                            responseString = keyNotFoundResponse;
                        }
                        break;

                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            responseString = errorResponse;
        } finally {
            writeResponse(response);
        }
    }

    /**
     *
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (log.isTraceEnabled()) {
            log.trace("doDelete  -------------------------------------------------------");
        }

        request.setCharacterEncoding("UTF-8");
        Request req = getRequestParsed(request.getRequestURI());
        logRequestInfo(req, null);
        responseString = "";

        if (rep.sourceContains(req.getResource())) {
            switch (req.getActionID()) {
                /**
                 * DELETE /{resource-type}/items/{resource-key}
                 * @param url
                 */
                case Request.ITEMS:

                    String key = req.getKey();


                    // No repository found for requested resource
                    if (!rep.repositoryExists(req.getResource())) {
                        response.setStatus(406);
                        responseString = keyNotFoundResponse;
                    } else {
                        try {
                            // When no key is provided, delete operation means delete all items
                            if (key == null) {
                                rep.deleteAllItems(req.getResource());
                            } else {
                                rep.delete(req.getResource(), key);
                            }
                        } catch (Exception ex) {
                            response.setStatus(500);
                            responseString =
                                    keyNotFoundResponse;

                        }
                    }
                    break;
                default:
                    break;
            }

        }

        writeResponse(response);
    }

    public Request getRequestParsed(
            String url) {
        return new Request(url);
    }

    private void writeResponse(HttpServletResponse response) throws IOException {

        response.setContentType("text/plain; charset=" + CHARSET);
        response.getWriter().write(responseString);
        response.getWriter().flush();
        response.getWriter().close();
        if (log.isTraceEnabled()) {
            try {
                log.trace("response    :" + JSONObject.fromObject(responseString).toString(1));
            } catch (Exception ex) {
                log.trace("response    :" + responseString);
            }
            log.trace("------------------------------------------------");
        }
    }

    private String getContent(BufferedReader reader) {
        try {
            String line = null;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException ex) {
            log.error("ERROR: reading request content");
            return "";
        }
    }

    private void logRequestInfo(Request req, String content) {
        if (log.isTraceEnabled()) {
            log.trace("url         :" + req.getUrl());
            log.trace("getAction   :" + req.getAction());
            log.trace("getKey      :" + req.getKey());
            log.trace("getResource :" + req.getResource());
            log.trace("getType     :" + req.getType());
            try {
                log.trace("content     :" + JSONObject.fromObject(content).toString(1));
            } catch (Exception ex) {
                log.error("content     :" + content);
            }
        }
    }
}
