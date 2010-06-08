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
package com.funambol.json.dao;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.funambol.framework.core.Cred;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;
import com.funambol.json.admin.JsonConnectorConfig;
import com.funambol.json.domain.JsonAuthResponseModel;
import com.funambol.json.domain.JsonResponse;
import com.funambol.json.exception.JsonConfigException;
import com.funambol.json.security.JsonUser;
import com.funambol.json.util.Utility;
import net.sf.json.JSONObject;

public class AuthenticatorDAOImpl implements AuthenticatorDAO {

    private static final FunambolLogger log = FunambolLoggerFactory.getLogger(Utility.LOG_NAME);
    public final static String USER = "user";
    public final static String PASSWORD = "password";
    public final static String LOGIN_URL = "auth/login";
    public final static String LOGOUT_URL = "auth/logout";
    public final String NAME = Cred.AUTH_TYPE_BASIC;
    private String serverUrl = null;

    private HttpClient httpClient = null;
    
    public AuthenticatorDAOImpl() {
        try {
            //Get the connection parameters...
            this.serverUrl = JsonConnectorConfig.getConfigInstance().getJsonServerUrl();
        } catch (JsonConfigException e) {
            log.trace("Error getting information from the configuration file.", e);
        }

        HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        this.httpClient = new HttpClient(httpConnectionManager);
    }

    /**
     * @param server the server name. For example: jsonservice.com
     * @param port the server port
     * @param schema http or https
     */
    public AuthenticatorDAOImpl(String serverUrl) {
        super();
        this.serverUrl = serverUrl;

        HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        this.httpClient = new HttpClient(httpConnectionManager);
    }


    /* (non-Javadoc)
     * @see com.funambol.json.dao.AuthenticatorDAO#login(java.lang.String)
     */
    public JsonResponse login(String jsonObject) throws HttpException, IOException {

        String request = Utility.getUrl(serverUrl, LOGIN_URL);

        if (log.isTraceEnabled()) {
            log.trace("Login Request: " + request);
        }

        PostMethod post = new PostMethod(request);
        post.setRequestEntity(new StringRequestEntity(jsonObject));


        int statusCode = 0;
        String responseBody = null;
        try {
            statusCode = httpClient.executeMethod(post);
            responseBody = post.getResponseBodyAsString();
        } finally {
            post.releaseConnection();
        }

        if (log.isTraceEnabled()) {
            log.trace("---------> statusCode: " + statusCode + " responseBody: " + responseBody);
        }

        JsonResponse jsonServerResponse = new JsonResponse(statusCode, responseBody);
        return jsonServerResponse;
    }

    /* (non-Javadoc)
     * @see com.funambol.json.dao.AuthenticatorDAO#logout(com.funambol.json.security.JsonUser)
     */
    public JsonResponse logout(JsonUser user) throws HttpException, IOException {

        String request = Utility.getUrl(serverUrl, LOGOUT_URL);

        if (log.isTraceEnabled()) {
            log.trace("Logout Request: " + request);
        }

        String t = user.getSessionID();

        PostMethod post = new PostMethod(request);

        post.setRequestHeader(Utility.TOKEN_HEADER_NAME, t);
        
        // 
        //{"data":{       
        //   "sessionid":"4B339C8F5437B7A9506D8C69901833BF"
        //        }
        //}
        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        jsonData.element(JsonAuthResponseModel.SESSIONID.getValue(), t);
        jsonRoot.element(JsonAuthResponseModel.DATA.getValue(),      jsonData);
        
        post.setRequestEntity(new StringRequestEntity(jsonRoot.toString()));

        int statusCode = 0;
        String responseBody = null;
        try {
            statusCode = httpClient.executeMethod(post);
            responseBody = post.getResponseBodyAsString();
        } finally {
            post.releaseConnection();
        }

        if (log.isTraceEnabled()) {
            log.trace("---------> statusCode: " + statusCode + " responseBody: " + responseBody);
        }

        JsonResponse jsonServerResponse = new JsonResponse(statusCode, responseBody);
        return jsonServerResponse;

    }

    public String getName() {
        return NAME;
    }
}
