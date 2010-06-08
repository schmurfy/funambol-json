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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.log4j.Logger;
import com.funambol.json.api.util.Def;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import com.funambol.json.api.util.ConfigLoader;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
import net.sf.json.JSONException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;

/**
 * This class interacts with the json api server, performing CRUD and sync operations
 * 
 */
public class FunambolJSONApiDAO {

    public static final String CHARSET = "UTF-8";
    
    // is set true when inside a sync session
    protected boolean isInsideASync = false;
    
    // a flag used to get only one notification when configuration method is not provided (to keep log files clear)
    protected boolean errorOnGetTimeNotified = false;
    
    //this value should be false if the sync type is  â€œone-way-from-clientâ€? or â€œrefresh-from-clientâ€?
    protected boolean expectItemsFromServer = true;

    // the items on server must be deleted when a new test starts
    protected boolean resetAtNextLogin = false;
    // as some of the items are moved to temp directory (in order to avoid detecting them twice 
    // as new items) we need to keep track of how many were inserted in order to test the 
    // getAllSyncItemKeys
    protected int itemsMovedToTemp = 0;
    protected SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd'T'HH:mm.ss:SSS");
    
    // Time references
    protected long startTestTime    = 0;
    protected long beginSyncTime    = 0;
    protected long lastSyncTime     = 0;
    
    protected String sourceName = null;  // Name of the source being executed  
    protected String testName = null;   // Name of the test being executed
    protected String sessionid = null; // Authorization token
    protected ConfigLoader configLoader = null; // config loader
    protected static Logger log = Logger.getLogger(Def.LOG_NAME); //logger

    public FunambolJSONApiDAO(ConfigLoader configLoader) {
        this.configLoader = configLoader;
        if (log.isTraceEnabled()) {
            log.trace("SERVER_URI:" + configLoader.getServerProperty(Def.SERVER_URI));
            log.trace("USERNAME:" + configLoader.getServerProperty(Def.USERNAME));
            log.trace("PASSWORD:" + configLoader.getServerProperty(Def.PASSWORD));

        }
        this.startTestTime = System.currentTimeMillis();
    }

    /**
     * performs the login in the json server
     * @throws com.funambol.json.api.exception.OperationFailureException
     */
    public void login() throws OperationFailureException {

        if (log.isInfoEnabled()) {
            log.info("Executing method: login() " + configLoader.getServerTimeStr(startTestTime));
        }

        try {

            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/auth/login";

            String body =
                    "{" +
                    " \"data\": " +
                    " { " +
                    "   \"credentials\": " +
                    "   { " +
                    "     \"user\":\"" + configLoader.getServerProperty(Def.USERNAME) + "\"," +
                    "     \"pass\":\"" + configLoader.getServerProperty(Def.PASSWORD) + "\" " +
                    "   } " +
                    " } " +
                    "} ";

            String response = sendPostRequest(req, body);

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE login: \n" + response);
            }

            parseLoginResponse(response);
            if (resetAtNextLogin) {
                if (log.isInfoEnabled()) {
                    log.info("Deleting all items for source :" + sourceName);
                }
                try {
                    deleteAllItems();
                } catch (Exception ex) {
                    if (log.isInfoEnabled()) {
                        log.info("It was not possible to delete all the items for this source, may be not implemented yet");
                    }
                }
                //the items deleted should not appear on the sync results
                this.startTestTime = System.currentTimeMillis();
                this.lastSyncTime  = startTestTime;
                resetAtNextLogin = false;
            }
        } catch (Exception e) {
            throw new OperationFailureException("Login failled. Username \"" + configLoader.getServerProperty(Def.USERNAME) + "\"" + "password \"" + configLoader.getServerProperty(Def.PASSWORD) + "\"", e);
        }


    }

    /**
     * logs out from the json server
     * @throws com.funambol.json.api.exception.OperationFailureException
     */
    public void logout() throws OperationFailureException {
        if (log.isInfoEnabled()) {
            log.info("Executing method: logout()");
        }
        try {
            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/auth/logout";
            String body = "{\"data\":{\"sessionid\":\"" + this.sessionid + "\"}}";



            String response = sendPostRequest(req, body);
            if (log.isTraceEnabled()) {
                log.trace("RESPONSE logout: \n" + response);
            }
        } catch (Exception e) {
            throw new OperationFailureException("logout failled.", e);
        }


    }

    /**
     * tell the server that a sync session is going to begin.
     * @return
     * @deprecated
     */
    public String beginSync() throws OperationFailureException {
        isInsideASync = false;
        try {
            beginSyncTime = System.currentTimeMillis();
            if (log.isInfoEnabled()) {
                log.info("Executing method: beginSync() : " + configLoader.getServerTimeStr(beginSyncTime));
            }
            expectItemsFromServer = true;
            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/sync/begin";
            String body = null;
            String response = sendPostRequest(req, body);

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE beginSync: \n" + response);
            }
            isInsideASync = true;
            return response;

        } catch (IOOperationException ex) {
            throw new OperationFailureException("Error at beginSync ", ex);
        }

    }

    /**
     * tell the server that a sync session is going to begin. indicating the timestamp and the synctype
     * @return
     */
    public String beginSync(String syncType) throws OperationFailureException {
        isInsideASync = false;
        try {
            beginSyncTime = System.currentTimeMillis();
            if (log.isInfoEnabled()) {
                log.info("Executing method: beginSync(" + syncType + ") : " + configLoader.getServerTimeStr(beginSyncTime));
            }
            if (syncType.equals("one-way-from-client") || syncType.equals("refresh-from-client")) {
                expectItemsFromServer = false;
            } else {// â€œtwo-wayâ€?, â€œfullâ€?, â€œone-way-from-serverâ€?, â€œrefresh-from-serverâ€?
                expectItemsFromServer = true;
            }

            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + getSourceName() + "/sync/begin";
            String body = "{ \"data\":" +
                    " { \"synctype\":\"" + syncType + "\"," +
                    "\"since\":" + configLoader.getServerTime(beginSyncTime) + "}" +
                    "}";
            String response = sendPostRequest(req, body);

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE beginSync: \n" + response);
            }
            isInsideASync = true;
            return response;

        } catch (IOOperationException ex) {
            throw new OperationFailureException("Error at beginSync ", ex);
        }

    }

    /**
     *  tell the server that a sync session had ended.
     * @return
     */
    public String endSync() throws OperationFailureException {
        isInsideASync = false;
        try {
            if (log.isInfoEnabled()) {
                log.info("Executing method: endSync()");
            }
            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + getSourceName() + "/sync/end";
            String response = sendPostRequest(req, null);

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE endSync: \n" + response);
            }
            lastSyncTime       = beginSyncTime + 1;
            itemsMovedToTemp += configLoader.getNewItems(sourceName, testName).size();
            configLoader.storeSynchedItems(sourceName, testName);
            return response;
        } catch (IOOperationException ex) {
            throw new OperationFailureException("Error at beginSync ", ex);
        }
    }

    /**
     * get all the keys for the items in the server.
     * @return
     */
    public String getAllSyncItemKeys() throws OperationFailureException {
        if (log.isInfoEnabled()) {
            log.info("Executing method: getAllSyncItemKeys()");
        }
        try {
            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + getSourceName() + "/keys/all";
            String response = sendGetRequest(req, 0, 0);
            if (log.isTraceEnabled()) {
                log.trace("RESPONSE getAllSyncItemKeys: \n" + response);
            }
            JSONArray itemsOnServer = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("keys");

            int expectedItems = configLoader.getNewItems(sourceName, testName).size() - configLoader.getDeletedItems(sourceName, testName).size() + itemsMovedToTemp;
            if (itemsOnServer.size() != expectedItems) {
                log.error("------------- ERROR ["+sourceName+":"+testName+"]");
                log.error("ERROR: expected " + expectedItems + " but found on server " + itemsOnServer.size());
            }
            return response;
        } catch (IOOperationException ex) {
            throw new OperationFailureException("Error retrieving item ", ex);
        }

    }

    /**
     * get all the keys of the deleted items since a specified time.
     * @return
     */
    public String getDeletedSyncItemKeys() throws OperationFailureException {
        if (log.isInfoEnabled()) {
            log.info("Executing method: getDeletedSyncItemKeys() from:" + configLoader.getServerTimeStr(lastSyncTime) + " to  :" +
                    configLoader.getServerTimeStr(beginSyncTime));

        }

        try {
            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + getSourceName() + "/keys/deleted";

            //String response = sendGetRequest(req, configLoader.getServerTime(startTestTime), configLoader.getServerTime(beginSyncTestTime));
            String response = sendGetRequest(req, configLoader.getServerTime(lastSyncTime), configLoader.getServerTime(beginSyncTime));

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE getDeletedSyncItemKeys: \n" + response);
            }
            JSONArray arrayOfKeys = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("keys");

            LinkedList<String> items = configLoader.getDeletedItems(sourceName, testName);
            if (expectItemsFromServer) {
                if (items.size() != arrayOfKeys.size()) {
                    log.error("------------- ERROR ["+sourceName+":"+testName+"]");
                    log.error("ERROR Expected :" + items.size() + " found " + arrayOfKeys.size());
                    log.error("ERROR client :" + items);
                    log.error("ERROR server :" + arrayOfKeys);
                }
            } else {
                if (arrayOfKeys.size() != 0) {
                    log.error("------------- ERROR ["+sourceName+":"+testName+"]");
                    log.error("ERROR : the syncType specified does not expect any keys returned");
                }
            }
            return response;
        } catch (IOOperationException ex) {
            throw new OperationFailureException("Error retrieving item ", ex);
        }
    }

    /**
     * get all the keys of the updated items since a specified time.
     * @return
     */
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

            LinkedList<String> items = configLoader.getUpdatedItems(sourceName, testName);
            if (expectItemsFromServer) {
                if (items.size() != arrayOfKeys.size()) {
                    log.error("------------- ERROR ["+sourceName+":"+testName+"]");
                    log.error("ERROR Expected :" + items.size() + " found " + arrayOfKeys.size());
                    log.error("ERROR Expected :" + items);
                    log.error("ERROR Found    :" + arrayOfKeys);
                }
            } else {
                if (arrayOfKeys.size() != 0) {
                    log.error("------------- ERROR ["+sourceName+":"+testName+"]");
                    log.error("ERROR : the syncType specified does not expect any keys returned");
                }
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

            LinkedList<String> items = configLoader.getNewItems(sourceName, testName);
            if (expectItemsFromServer) {
                if (items.size() != arrayOfKeys.size()) {
                    log.error("------------- ERROR ["+sourceName+":"+testName+"]");
                    log.error("ERROR Expected :" + items.size() + " found " + arrayOfKeys.size());
                    log.error("ERROR Expected :" + items);
                    log.error("ERROR Found    :" + arrayOfKeys);
                }
            } else {
                if (arrayOfKeys.size() != 0) {
                    log.error("------------- ERROR ["+sourceName+":"+testName+"]");
                    log.error("ERROR : the syncType specified does not expect any keys returned");
                }
            }



            return response;
        } catch (IOOperationException ex) {
            throw new OperationFailureException("Error retrieving new items ", ex);
        }
    }

    /**
     *  get the item associated with a specific key.
     * @return
     */
    public String getSyncItemFromId(String itemToGet) throws JSONTestAPIDAOException {
        try {
            if (log.isInfoEnabled()) {
                log.info("Executing method: getSyncItemFromId()");
            }
            String key = configLoader.getKeyFromFile(this.getSourceName(), this.getTestName(), itemToGet, false);

            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + getSourceName() + "/items/" + key;
            String response = sendGetRequest(req, 0, 0);

            if (log.isInfoEnabled()) {
                log.info("RESPONSE getSyncItemFromId: \n" + response);
            }

            return null;
        } catch (IOOperationException ex) {
            throw new JSONTestAPIDAOException("Error retrieving item " + ex);
        }
    }

    /**
     *   remove the item with a specific key.
     * @return
     */
    public String removeSyncItem(String itemToRemove) throws JSONTestAPIDAOException {
        long operationTime = getOperationTime();
        try {
            if (log.isInfoEnabled()) {
                log.info("Executing method: removeSyncItem() " + configLoader.getServerTimeStr(operationTime));
            }
            String key = configLoader.getKeyFromFile(this.getSourceName(), this.getTestName(), itemToRemove, false);
            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + this.getSourceName() + "/items/" + key;
            String response = null;
            response = sendDeleteRequest(req, operationTime, -1);

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE DELETE : \n" + response);
            }

            if (log.isTraceEnabled()) {
                log.trace("deleted key: " + key);
            }

            configLoader.storeFile(this.getSourceName() + File.separator + this.getTestName() + File.separator + getItemNameFromAdded(itemToRemove) + "_removed.json", key);
            configLoader.deleteFile(this.getSourceName() + File.separator + this.getTestName() + File.separator + itemToRemove + ".json");
            return response;
        } catch (IOOperationException ex) {
            throw new JSONTestAPIDAOException("Error removing item " + ex);
        }
    }

    /**
     *  insert a specified item in the server and return it with a key.
     * @return
     */
    public String addSyncItem(String itemName) throws JSONTestAPIDAOException {
        long operationTime = getOperationTime();
        try {
            if (log.isInfoEnabled()) {
                log.info("Executing method: addSyncItem() " + configLoader.getServerTimeStr(operationTime));
            }

            String content = configLoader.getJSONFile(this.getSourceName(), this.getTestName(), itemName);
            if (log.isTraceEnabled()) {
                log.trace("content:" + content);
            }

            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + this.getSourceName() + "/items";
            String response = null;
            response = sendPostRequest(req,content, operationTime, -1);

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE add : \n" + response);
            }
            if (log.isTraceEnabled()) {
                log.trace("added key: " + parseAddItemResponse(response));
            }
            configLoader.storeFile(this.getSourceName() + File.separator +
                    this.getTestName() + File.separator + itemName + "_added.json", parseAddItemResponse(response));
            return response;
        } catch (IOOperationException ex) {
            throw new JSONTestAPIDAOException("Error adding item " + ex);
        }

    }

    /**
     * update a specified item in the server for a specific key.
     * @return
     */
    public String updateSyncItem(String itemToReplace, String newItem) throws JSONTestAPIDAOException {
        long operationTime = getOperationTime();
        try {
            if (log.isInfoEnabled()) {
                log.info("Executing method: updateSyncItem()" + configLoader.getServerTimeStr(operationTime));
            }
            String content = configLoader.getJSONFile(this.getSourceName(), this.getTestName(), newItem);
            if (log.isTraceEnabled()) {
                log.trace("new content:" + content);
            }
            String key = configLoader.getKeyFromFile(this.getSourceName(), this.getTestName(), itemToReplace, false);

            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + this.getSourceName() + "/items/" + key;
            String response = null;

            response = sendPutRequest(req, content,operationTime, -1);

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE update : \n" + response);
            }
            if (log.isTraceEnabled()) {
                log.trace("updated key: " + key);
            }
            
            configLoader.storeFile(this.getSourceName() + File.separator + this.getTestName() + File.separator + getItemNameFromAdded(itemToReplace) + "_updated.json", key);

            // If I'm updating an added element, I need to delete the added file but if I'm updating a previous updated element, 
            if(!itemToReplace.endsWith("_updated")) {
                configLoader.deleteFile(this.getSourceName() + File.separator + this.getTestName() + File.separator + itemToReplace + ".json"); 
            }
            return response;

        } catch (IOOperationException ex) {
            throw new JSONTestAPIDAOException("Error updating item " + ex);

        }
    }

    /**
     *  search for application defined equivalent items for a specified item.
     * @return
     */
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


            String response =
                    sendPostRequest(req, content);
            if (log.isTraceEnabled()) {
                log.trace("RESPONSE twins : \n" + response);
            }
            JSONArray twinItems = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("keys");
            String keyExpected = configLoader.getKeyFromFile(this.getSourceName(), this.getTestName(), itemKeyExpected, false);
            if (!twinItems.contains(keyExpected)) {
                if (log.isInfoEnabled()) {
                    log.error("------------- ERROR ["+sourceName+":"+testName+"]");
                    log.info("ERROR the expected twin:" + keyExpected + " does not exist");
                }
            }
            return response;

        } catch (IOOperationException ex) {
            throw new JSONTestAPIDAOException("Error retrieving twin items " + ex);
        }

    }

    /**
     * compares the item on client with the item on the server
     * the item returned by the server must have all the same keys and values as the item
     * on client side
     * @param itemKeyInServer
     * @param itemToCompare
     * @return
     * @throws com.funambol.json.api.exception.JSONTestAPIDAOException
     */
    public String compareClientServer(String itemKeyInServer, String itemToCompare) throws JSONTestAPIDAOException {
        try {
            if (log.isInfoEnabled()) {
                log.info("Executing method: compareClientServer()");
            }
            String key = configLoader.getKeyFromFile(this.getSourceName(), this.getTestName(), itemKeyInServer, false);

            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + getSourceName() + "/items/" + key;
            String response = sendGetRequest(req, 0, 0);
            String content = configLoader.getJSONFile(this.getSourceName(), this.getTestName(), itemToCompare);
            if (log.isTraceEnabled()) {
                log.trace("content local item          : \n" + content);
            }
            if (log.isTraceEnabled()) {
                log.trace("RESPONSE contact from server: \n" + response);

            }


            JSONObject client = null;
            JSONObject server = null;
            try {
                client = JSONObject.fromObject(content).getJSONObject("data").getJSONObject("item");
            } catch (JSONException ex) {
                throw new JSONTestAPIDAOException("Local json object is not valid " + ex);
            }
            try {
                server = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("item");
            } catch (JSONException ex) {
                throw new JSONTestAPIDAOException("Remote json object is not valid " + ex);
            }
            // checking if the object from retrieved from server has all the properties sent, 
            // and if they have the same value
            Iterator en = client.keys();
            String keyServerObject = null;
            while (en.hasNext()) {
                keyServerObject = (String) en.next();
                if (client.has(keyServerObject)) {
                    if (!client.optString(keyServerObject).equals(server.optString(keyServerObject))) {
                        if (log.isInfoEnabled()) {
                            log.error("------------- ERROR ["+sourceName+":"+testName+"]");
                            log.error("ERROR: " + keyServerObject + " : (" + client.optString(keyServerObject) + " <-> " + server.optString(keyServerObject) + ")");
                        }
                    } else {
                        if (log.isTraceEnabled()) {
                            log.trace("Correct: " + keyServerObject + " : (" + client.optString(keyServerObject) + " <-> " + server.optString(keyServerObject) + ")");
                        }
                    }


                } else {
                    if (log.isInfoEnabled()) {
                        log.error("Error: Server item has no " + keyServerObject + " key");
                    }
                }
            }
            return response;
        } catch (JSONException ex) {
            throw new JSONTestAPIDAOException("Error compareClientServer  " + ex);
        } catch (IOOperationException ex) {
            throw new JSONTestAPIDAOException("Error compareClientServer  " + ex);
        }

    }
    
        public void deleteAllItems() throws JSONTestAPIDAOException {
        long aLongTimeAgo = new Date(0).getTime();
        try {
            if (log.isInfoEnabled()) {
                log.info("Executing method: deleteAllItems() ");
            }
            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + this.getSourceName() + "/items";
            String response = null;
            response = sendDeleteRequest(req, aLongTimeAgo, -1);

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE DELETE : \n" + response);
            }

        } catch (IOOperationException ex) {
            throw new JSONTestAPIDAOException("Error removing item " + ex);
        }
    }

    /*
    public void deleteAllItems() throws JSONTestAPIDAOException {
        long aLongTimeAgo = new Date(0).getTime();
        try {
            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + getSourceName() + "/keys/all";
            String response = sendGetRequest(req, 0, 0);
            if (log.isTraceEnabled()) {
                log.trace("RESPONSE deleteAllItems: \n" + response);
            }

            JSONArray keysArray = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("keys");
            for (int i = 0; i < keysArray.size(); i++) {
                if (log.isInfoEnabled()) {
                    log.info("removing " + keysArray.getString(i));
                }
                req = configLoader.getServerProperty(Def.SERVER_URI) + "/" + this.getSourceName() + "/items/" + keysArray.getString(i);
                sendDeleteRequest(req,aLongTimeAgo,aLongTimeAgo);
            }


        } catch (IOOperationException ex) {
            throw new JSONTestAPIDAOException("Error removing all items from server ");
        }
    }
     * */

    /**
     *
     */
    protected String sendPutRequest(
            String REQUEST, String body) throws IOOperationException {
    
        return sendPutRequest(REQUEST, body, -1, -1);
    }

    /**
     *
     */
    protected String sendPostRequest(String REQUEST, String body) throws IOOperationException {
        return sendPostRequest(REQUEST, body, -1, -1);
    }
    
    
    protected String sendPostRequest(String REQUEST, String body, long since, long until) throws IOOperationException {
        String response = null;
        PostMethod method = null;
        try {

            HttpClient httpClient = new HttpClient();

            method = new PostMethod(REQUEST);
            
            addSinceUntil(method, since, until);

            if (log.isTraceEnabled()) {
                log.trace("\nREQUEST: " + REQUEST + "");
            }

            if (this.sessionid != null) {
                method.setRequestHeader("Authorization", this.sessionid);
            }

            if (body != null) {
                byte[] raw = body.getBytes(CHARSET);
                method.setRequestEntity(new ByteArrayRequestEntity(raw,"text/plain; charset="+CHARSET));
                //method.setRequestContentLength(raw.length);
                //method.setRequestEntity(new StringRequestEntity(body));
                //method.setRequestBody(body);
                if (log.isTraceEnabled()) {
                    log.trace("body: " + body);
                }
            }

            printHeaderFields(method.getRequestHeaders(), "REQUEST");

            int code = httpClient.executeMethod(method);

            response =
                    method.getResponseBodyAsString();

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE code: " + code);
            }
            printHeaderFields(method.getResponseHeaders(), "RESPONSE");
        } catch (Exception e) {
            throw new IOOperationException("Error GET Request ", e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }

        return response;
    }

    /**
     *
     */
    protected String sendGetRequest(String REQUEST, long since, long until) throws IOOperationException {

        String response = null;
        GetMethod method = null;
        try {
            if (log.isTraceEnabled()) {
                log.trace("\nREQUEST:" + REQUEST);
            }
            method = new GetMethod(REQUEST);
            HttpClient httpClient = new HttpClient();
            if (since != 0 && until != 0) {
                NameValuePair nvp_since = new NameValuePair();
                nvp_since.setName("since");
                nvp_since.setValue("" + since);
                NameValuePair nvp_until = new NameValuePair();
                nvp_until.setName("until");
                nvp_until.setValue("" + until);
                NameValuePair[] nvp = {nvp_since, nvp_until};
                method.setQueryString(nvp);
            }

            if (this.sessionid != null) {
                method.setRequestHeader("Authorization", this.sessionid);
            }

            printMethodParams(method);
            printHeaderFields(method.getRequestHeaders(), "REQUEST");

            int code = httpClient.executeMethod(method);

            response =
                    method.getResponseBodyAsString();

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE code: " + code);
            }
            printHeaderFields(method.getResponseHeaders(), "RESPONSE");




        } catch (Exception e) {
            throw new IOOperationException("Error GET Request ", e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }

        return response;
    }

    /**
     *
     */
    protected String sendGetRequestWithNameValuePairs(String REQUEST, NameValuePair[] pairs) throws IOOperationException {

        String response = null;
        GetMethod method = null;
        try {
            if (log.isTraceEnabled()) {
                log.trace("\nREQUEST:" + REQUEST);
            }
            method = new GetMethod(REQUEST);
            HttpClient httpClient = new HttpClient();
            method.setQueryString(pairs);


            if (this.sessionid != null) {
                method.setRequestHeader("Authorization", this.sessionid);
            }

            printMethodParams(method);
            printHeaderFields(method.getRequestHeaders(), "REQUEST");

            int code = httpClient.executeMethod(method);

            response =
                    method.getResponseBodyAsString();

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE code: " + code);
            }
            printHeaderFields(method.getResponseHeaders(), "RESPONSE");

        } catch (Exception e) {
            throw new IOOperationException("Error GET Request ", e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }

        return response;
    }

    /**
     *
     */
    protected String sendDeleteRequest(String REQUEST,String ciao) throws IOOperationException {
        return sendDeleteRequest(REQUEST, -1, -1);
    }
    
     protected String sendDeleteRequest(String REQUEST, long since, long until) throws IOOperationException {

        String response = null;
        DeleteMethod method = null;
        try {

            HttpClient httpClient = new HttpClient();

            method = new DeleteMethod(REQUEST);

            addSinceUntil(method, since, until);
            
            if (log.isTraceEnabled()) {
                log.trace("\nREQUEST: " + REQUEST + "");
            }
            if (this.sessionid != null) {
                method.setRequestHeader("Authorization", this.sessionid);
            }

            printHeaderFields(method.getRequestHeaders(), "REQUEST");

            int code = httpClient.executeMethod(method);

            response =
                    method.getResponseBodyAsString();

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE code: " + code);
            }
            printHeaderFields(method.getResponseHeaders(), "RESPONSE");

        } catch (Exception e) {
            throw new IOOperationException("Error GET Request ", e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }

        return response;
    }

    /**
     *
     */
    protected void parseLoginResponse(String response) throws OperationFailureException {
        try {
            JSONObject jo = JSONObject.fromObject(response);
            JSONObject jdata = jo.getJSONObject("data");
            this.sessionid = jdata.getString("sessionid");
        } catch (Exception e) {
            throw new OperationFailureException("Login failure\nresponse:" + response, e);
        }

    }

    /**
     *
     */
    protected String parseAddItemResponse(String response) {
        String id = null;
        try {
            JSONObject jo = JSONObject.fromObject(response);
            JSONObject jdata = jo.getJSONObject("data");
            id = jdata.getString("key");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return id;

    }

    /**
     *
     */
    protected List parseGetAllResponse(String response) {

        List<String> ids = new ArrayList<String>();

        try {

            if (response != null) {
                if (!response.equals("null")) {

                    JSONObject jo = JSONObject.fromObject(response);

                    if (!isError(jo)) {
                        JSONObject jdata = jo.getJSONObject("data");
                        JSONArray jkeys = jdata.getJSONArray("keys");
                        for (int i = 0; i < jkeys.size(); i++) {
                            ids.add(jkeys.getString(i));
                        }

                    } else {
                        log.error("Error from the server");
                    }

                } else {
                    log.error("No Data from the server");
                }

            } else {
                log.error("No Data from the server");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ids;

    }

    // this method is used to add since and until parameters to the http method
    // passed as parameters. Both parameters are set only if they are greater than 0
    private void addSinceUntil(HttpMethodBase method, long since, long until) {
        List<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
        if(since>=0) 
            queryParameters.add(new NameValuePair("since", ""+since));
        if(until>=0) 
            queryParameters.add(new NameValuePair("until", ""+until));
        if(queryParameters.size()>0)
            method.setQueryString(queryParameters.toArray(new NameValuePair[]{}));
    }

    // this method allows to get the operation time. If you're working inside a sync,
    // the operation time is the beginSyncTime reference. On the contrary, you'll obtain
    // a current time reference.
    private long getOperationTime() {
        if(isInsideASync)
            return configLoader.getServerTime(beginSyncTime);
        return System.currentTimeMillis();
    }

    /**
     * 
     */
    private void printHeaderFields(Header[] h, String label) {
        if (log.isTraceEnabled()) {
            log.trace("" + label + " HEADER:");



            for (int i = 0; i <
                    h.length; i++) {
                String name = h[i].getName();
                String value = h[i].getValue();
                log.trace("       " + name + " : " + value);
            }
        }
//System.out.println("");
    }

    /**
     * 
     */
    private void printMethodParams(GetMethod method) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("      params : " + method.getQueryString());
            log.trace("     param 'since' : " + method.getParams().getParameter("since"));
            log.trace("     param 'until' : " + method.getParams().getParameter("until"));
        }
    }

    /**
     *
     */
    protected boolean isError(JSONObject response) {
        if (response.has("error")) {
            return true;
        }

        return false;
    }

    /**
     * replaces item2_added by item2
     * @param itemName
     * @return
     */
    protected String getItemNameFromAdded(String itemName) {
        if (itemName.contains("_")) {
            return itemName.substring(0, itemName.indexOf("_"));
        }
        return itemName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setNewTestInfo(String sourceName, String testName) {
        if (this.testName == null || !this.testName.equals(testName)) {
            resetAtNextLogin = true;
            itemsMovedToTemp = 0;
        }
        this.sourceName = sourceName;
        this.testName = testName;
        startTestTime = System.currentTimeMillis();
        lastSyncTime  = startTestTime;
        configLoader.removeAllTestFiles(sourceName, testName);

    }

    public String getTestName() {
        return testName;
    }

    public long getTimeDiff() {
        String time = null;
        String tzid = null;
        try {

            String req = configLoader.getServerProperty(Def.SERVER_URI) + "/config/time";

            String response = sendGetRequest(req, 0, 0);
            if (log.isTraceEnabled()) {
                log.trace("RESPONSE getTime: \n" + response);
            }
            JSONObject jo = JSONObject.fromObject(response);
            JSONObject jdata = jo.getJSONObject("data");

            // it should be in Local time US/Central but it return the 'Z' at the end
            time = jdata.getString("time");

            tzid = jdata.getString("tzid");

            //DateFormat f = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            DateFormat f = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
            f.setTimeZone(TimeZone.getTimeZone(tzid));//("CST")); 
            Date d = f.parse(time);


            long fmTime = d.getTime();

            long myTime = configLoader.getServerTime(System.currentTimeMillis());

            return Math.abs(fmTime - myTime);

        } catch (Exception e) {
            if(!errorOnGetTimeNotified) {
                log.error("Error getting time, not supported!",e);
                errorOnGetTimeNotified =true;
            }
            return 0;
        }


    }

    private String sendPutRequest(String REQUEST, String body, long since, long until) throws IOOperationException {
        String response = null;
        PutMethod method = null;
        try {

            HttpClient httpClient = new HttpClient();

            method = new PutMethod(REQUEST);
            
            addSinceUntil(method,since,until);
            
            if (log.isTraceEnabled()) {
                log.trace("\nREQUEST: " + REQUEST + "");
            }
            if (this.sessionid != null) {
                method.setRequestHeader("Authorization", this.sessionid);
            }

            if (body != null) {
                byte[] raw = body.getBytes(CHARSET);
                method.setRequestEntity(new ByteArrayRequestEntity(raw,"text/plain; charset="+CHARSET));
          
                if (log.isTraceEnabled()) {
                    log.trace("body: " + body);
                }
            }

            printHeaderFields(method.getRequestHeaders(), "REQUEST");

            int code = httpClient.executeMethod(method);

            response =
                    method.getResponseBodyAsString();

            if (log.isTraceEnabled()) {
                log.trace("RESPONSE code: " + code);
            }
            printHeaderFields(method.getResponseHeaders(), "RESPONSE");

        } catch (Exception e) {
            throw new IOOperationException("Error GET Request ", e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }

        return response;
    }
}
    
    

