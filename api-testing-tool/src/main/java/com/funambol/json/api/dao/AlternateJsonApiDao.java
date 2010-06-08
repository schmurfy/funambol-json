/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2008 Funambol, Inc.
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
package com.funambol.json.api.dao;

import com.funambol.json.api.exception.IOOperationException;
import com.funambol.json.api.exception.JSONTestAPIDAOException;
import com.funambol.json.api.exception.OperationFailureException;
import net.sf.json.JSONObject;
import com.funambol.json.api.util.ConfigLoader;
import com.funambol.json.api.util.Def;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import net.sf.json.JSONArray;
import org.apache.commons.httpclient.NameValuePair;

/**
 *
 */
public class AlternateJsonApiDao extends FunambolJSONApiDAO {

    public AlternateJsonApiDao(ConfigLoader loader) {
        super(loader);
    }

    /**
     * tell the server that a sync session is going to begin.
     * @return
     */
    @Override
    public String beginSync() throws OperationFailureException {
        try {
            beginSyncTime = System.currentTimeMillis();

            if (log.isInfoEnabled()) {
                log.info("Executing method: beginSync() : " + configLoader.getServerTimeStr(beginSyncTime));
            }


            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/sync/begin";
            String body = "{ \"data\":" +
                    " { \"since\":" + configLoader.getServerTime(beginSyncTime) + "}" +
                    "}";
            String response = sendPostRequest(req, body);

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE beginSync: \n" + response);
            }

            return response;

        } catch (IOOperationException ex) {
            throw new OperationFailureException("Error at beginSync ", ex);
        }

    }

    /**
     * get all the keys of the updated items since a specified time.
     * @return
     */
    @Override
    public String getUpdatedSyncItemKeys() throws OperationFailureException {

        if (log.isInfoEnabled()) {
            log.info("Executing method: getUpdatedSyncItemKeys() from:" + configLoader.getServerTimeStr(lastSyncTime) + " to  :" +
                    configLoader.getServerTimeStr(beginSyncTime));
        }

        try {
            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + getSourceName() + "/keys/updated";

            String response = sendGetRequest(req, configLoader.getServerTime(lastSyncTime), configLoader.getServerTime(beginSyncTime));

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE getUpdatedSyncItemKeys: \n" + response);
            }
            JSONArray arrayOfKeys = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("keys");

            if (arrayOfKeys.size() > 0) {
                log.info("ERROR Expected :0 found " + arrayOfKeys.size());
            }
            return response;
        } catch (IOOperationException ex) {
            throw new OperationFailureException("Error retrieving item ", ex);
        }
    }

    /**
     *  get all the keys of the new items since a specified time.
     * @return
     */
    @Override
    public String getNewSyncItemKeys() throws OperationFailureException {
        if (log.isInfoEnabled()) {
            log.info("Executing method: getNewSyncItemKeys() from:" + configLoader.getServerTimeStr(lastSyncTime) + " to  :" +
                    configLoader.getServerTimeStr(beginSyncTime));
        }
        try {
            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + getSourceName() + "/keys/new";

            String response = sendGetRequest(req, configLoader.getServerTime(lastSyncTime), configLoader.getServerTime(beginSyncTime));

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE getNewSyncItemKeys: \n" + response);
            }
            JSONArray arrayOfKeys = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("keys");

            LinkedList<String> newItems = configLoader.getNewItems(sourceName, testName);
            LinkedList<String> updatedItems = configLoader.getUpdatedItems(sourceName, testName);
            int itemsCount = newItems.size() + updatedItems.size();
            if (itemsCount != arrayOfKeys.size()) {
                log.info("ERROR Expected :" + itemsCount + " found " + arrayOfKeys.size());
                log.info("ERROR client N:" + newItems + " U:" + updatedItems);
                log.info("ERROR server  :" + arrayOfKeys);
            }

            return response;
        } catch (IOOperationException ex) {
            throw new OperationFailureException("Error retrieving new items ", ex);
        }
    }

    /**
     *  search for application defined equivalent items for a specified item.
     * @return
     */
    @Override
    public String getSyncItemKeysFromTwin(String itemToGetTwins, String itemKeyExpected) throws JSONTestAPIDAOException {

        if (log.isInfoEnabled()) {
            log.info("Executing method: getSyncItemKeysFromTwin()");
        }

        try {
            String content = configLoader.getJSONFile(this.getSourceName(), this.getTestName(), itemToGetTwins);

            if (log.isTraceEnabled()) {
                log.trace("content:" + content);
            }
            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + this.getSourceName() + "/keys/twins";
            JSONObject jsonForTwins = JSONObject.fromObject(content).getJSONObject("data");
            String contentType = jsonForTwins.getString("content-type");
            jsonForTwins = jsonForTwins.getJSONObject("item");

            ArrayList<NameValuePair> requestProps = new ArrayList<NameValuePair>();
            if (contentType != null) {
                if (contentType.equals(Def.CONTENT_CARD)) {
                    if (jsonForTwins.containsKey(Def.TWIN_FIRSTNAME)) {
                        requestProps.add(new NameValuePair("firstname", jsonForTwins.optString(Def.TWIN_FIRSTNAME)));
                    }
                    if (jsonForTwins.containsKey(Def.TWIN_LASTNAME)) {
                        requestProps.add(new NameValuePair("lastname", jsonForTwins.optString(Def.TWIN_LASTNAME)));
                    }
                    if (jsonForTwins.containsKey(Def.TWIN_EMAIL)) {
                        requestProps.add(new NameValuePair("email", jsonForTwins.optString(Def.TWIN_EMAIL)));
                    }
                    if (jsonForTwins.containsKey(Def.TWIN_COMPANY)) {
                        requestProps.add(new NameValuePair(Def.TWIN_COMPANY, jsonForTwins.optString(Def.TWIN_COMPANY)));
                    }
                } else if (contentType.equals(Def.CONTENT_APPOINTMENT)) {
                    if (jsonForTwins.containsKey(Def.TWIN_SUBJECT)) {
                        requestProps.add(new NameValuePair(Def.TWIN_SUBJECT, jsonForTwins.optString(Def.TWIN_SUBJECT)));
                    }
                    if (jsonForTwins.containsKey(Def.TWIN_STARTDATE)) {
                        requestProps.add(new NameValuePair("dtstart", jsonForTwins.optString(Def.TWIN_STARTDATE)));
                    }
                    if (jsonForTwins.containsKey(Def.TWIN_ENDDATE)) {
                        requestProps.add(new NameValuePair("dtend", jsonForTwins.optString(Def.TWIN_ENDDATE)));
                    }
                } else if (contentType.equals(Def.CONTENT_TASK)) {
                    if (jsonForTwins.containsKey(Def.TWIN_SUBJECT)) {
                        requestProps.add(new NameValuePair("subject", jsonForTwins.optString(Def.TWIN_SUBJECT)));
                    }
                    if (jsonForTwins.containsKey(Def.TWIN_STARTDATE)) {
                        requestProps.add(new NameValuePair("dstart", jsonForTwins.optString(Def.TWIN_STARTDATE)));
                    }
                    if (jsonForTwins.containsKey(Def.TWIN_DUEDATE)) {
                        requestProps.add(new NameValuePair("dtend", jsonForTwins.optString(Def.TWIN_DUEDATE)));
                    }
                } else if (contentType.equals(Def.CONTENT_NOTE)) {
                    if (jsonForTwins.containsKey(Def.TWIN_SUBJECT)) {
                        requestProps.add(new NameValuePair("subject", jsonForTwins.optString(Def.TWIN_SUBJECT)));
                    }
                }
            }
            if (requestProps.size() > 0) {
                NameValuePair[] n = new NameValuePair[requestProps.size()];
                Iterator it = requestProps.iterator();
                int i = 0;
                while (it.hasNext()) {
                    n[i] = (NameValuePair) it.next();
                    i++;
                }
                String response =
                        sendGetRequestWithNameValuePairs(req, n);
                if (log.isTraceEnabled()) {
                    log.trace("RESPONSE twins : \n" + response);
                }
                JSONArray twinItems = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("keys");
                String keyExpected = configLoader.getKeyFromFile(this.getSourceName(), this.getTestName(), itemKeyExpected, false);
                if (!twinItems.contains(keyExpected)) {
                    if (log.isInfoEnabled()) {
                        log.info("ERROR the expected twin:" + keyExpected + " does not exist");
                    }
                }
                return response;
            }

        } catch (IOOperationException ex) {
            throw new JSONTestAPIDAOException("Error retrieving twin items " + ex);
        }
        return "";
    }

    @Override
    protected void parseLoginResponse(String response) throws OperationFailureException {
        try {
            JSONObject jo = JSONObject.fromObject(response);
            JSONObject jdata = jo.getJSONObject("data");
            this.sessionid = jdata.getString("token");
        } catch (Exception e) {
            throw new OperationFailureException("Login failure\nresponse:" + response, e);
        }

    }
}