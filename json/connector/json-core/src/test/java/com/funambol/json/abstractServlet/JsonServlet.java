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
package com.funambol.json.abstractServlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class for Servlet: JsonServlet
 * 
 */
public class JsonServlet extends HttpServlet implements javax.servlet.Servlet {
    
    public final static long serialVersionUID = 1L;

    // action for the Servlet
    public final static String BEGINSYNC     = "beginSync";
    public final static String ENDSYNC       = "endSync";
    public final static String LOGIN         = "login";
    public final static String LOGIN_2       = "login2";
    public final static String LOGIN_2_USER  = "user-111";
    public final static String EMPTY         = "empty";
    public final static String ITEMS         = "items";
    public final static String ADD_ITEM_ERR  = "addItemsError";
    public final static String KEYS          = "keys";
    
    public final static String BAD_REQUEST           = "406";
    public final static String UNAUTHORIZED          = "401";
    public final static String INTERNAL_SERVER_ERROR = "500";

    private String doReturn = "empty";

    public void setDoReturn(String doReturn) {
        this.doReturn = doReturn;
    }    
    
    private String errorResponse = 
            "{\"error\":{\"code\":\"item-1004\"," +
                        "\"message\":\"runtime exception\"," +
                        "\"parameters\":[{\"param\":\"value\"},{\"param2\":\"value2\"}]}}";
    
    private String keyNotFoundResponse = "key not found on repository";
    private String responseString = "";
    private Map<String, String> repository;
    private int counter;

    //------------------------------------------------------------- Construnctor
    
    public JsonServlet() {
        super();
        repository = new HashMap<String, String>();
        counter = 0;
    }
    
    
    //------------------------------------------------------------- Methods

    
    /**
     * 
     * methods:
     * - getAll     (../contact/keys/all) 
     * - getNew     (../contact/keys/new?since=123456789&until=133456789)
     * - getUpdated (../contact/keys/updated?since=123456789&until=133456789)
     * - getDeleted (../contact/keys/deleted?since=123456789&until=133456789)
     * - getItem    (../contact/items/1342)
     * 
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        responseString = "";

        if (doReturn.equals(EMPTY)) {// empty
            
            if (request.getRequestURI().endsWith("config/time")){
                responseString = "{\"data\":{\"time\":\"20081020T082922\",\"tzid\":\"US/Central\"}}";
            } else {
                responseString = "";
            }       
                
        } else if (doReturn.equals(ITEMS)) {// get item
            
            
            String key = request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1, 
                                                           request.getRequestURI().length());

            if (repository.containsKey(key)) {
                responseString = repository.get(key);
            } else {
                responseString = keyNotFoundResponse;
            }
            
        } else if (doReturn.equals(KEYS)) {// list of keys
            
            JSONObject jsonRoot = new JSONObject();
            JSONObject jsonData = new JSONObject();
            JSONArray jsonKeys = new JSONArray();
            
            for (Iterator iterator = repository.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                jsonKeys.add(key);
            }
            
            jsonData.put("keys", jsonKeys);
            jsonRoot.put("data", jsonData);
            responseString = jsonRoot.toString();
            
        } else {
            
            checkForError(response);            
        }
        
        response.getWriter().write(responseString);
        response.getWriter().flush();
        response.getWriter().close();
        
    }

    /**
     * 
     * methods:
     * - login
     * - logout
     * - beginSync
     * - endSync
     * - getTwin
     * - addItem  ( ../contact/items  +  body)
     * 
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        System.out.println(".......doPost START .... " + request.getRequestURI());
        System.out.println(".......doPost START .... " + doReturn);
        
        String responseForRepositoryString = null;
        
        if (doReturn.equals(LOGIN)) {// login
        
            //System.out.println(".......doPost LOGIN");
            String loginResponse = "{\"data\":{\"sessionid\":\"88495150285f346ccf54a7f83c0630035e0e1198\"}}";
            responseString = loginResponse;
            
        } else if (doReturn.equals(LOGIN_2)) {// login

            //System.out.println(".......doPost LOGIN");
            String loginResponse = "{\"data\":" +
                    "{\"sessionid\":\"88495150285f346ccf54a7f83c0630035e0e1198\",\"fakecustid\":\""+LOGIN_2_USER+"\"}}";
            responseString = loginResponse;

        } else if (doReturn.equals(ITEMS)) {// add item
            
            //System.out.println(".......doPost ITEMS");
            
            String jsonRequest = request.getReader().readLine();
            
            //System.out.println(".......doPost " + jsonRequest);
                       
            // we need two option
            // 1 save the item in the run-time repository 
            // 2 keep the key
            // this problem because we have different response from addItem and getItem
            // from the addItem we have
            // {"data":{"key":12132}}
            // 
            // from the getItem we acheive
            // {"data":{"item":{"key":12132, "firstName:"maria", .... }}}            
            //
            // we should change the specs (devife better with example). the
            // addItem should returns
            // {"data":{"item":{"key":12132}}}            
            //
            
            // 1
            JSONObject jsonRoot4Rep = JSONObject.fromObject(jsonRequest);            
            
            JSONObject jsonData4Rep = jsonRoot4Rep.getJSONObject("data");
            
            JSONObject jsonItem4Rep = jsonData4Rep.getJSONObject("item");
            
            jsonItem4Rep.put("key", "" + counter);
            jsonData4Rep.put("item", jsonItem4Rep);
            jsonRoot4Rep.put("data", jsonData4Rep);
            
            responseForRepositoryString = jsonRoot4Rep.toString();

            //System.out.println(".......doPost responseForRepositoryString " + responseForRepositoryString);

            repository.put("" + counter, responseForRepositoryString);

            // 2
            JSONObject jsonRoot = JSONObject.fromObject(jsonRequest);            
            JSONObject jsonData = jsonRoot.getJSONObject("data");
            jsonData.put("key", "" + counter);            

            //System.out.println(".......doPost response for client " + jsonRoot.toString());

            counter++;

            responseString = jsonRoot.toString();
            
        } else if (doReturn.equals(ADD_ITEM_ERR)) {// add item but with the error: "The item already exists"
            
            //System.out.println(".......doPost ADD_ITEM_ERR");
            
            String jsonRequest = request.getReader().readLine();
            
            //System.out.println(".......doPost " + jsonRequest);
                
            response.setStatus(406);
            String res = 
                    "{\"error\":" +
                    "{" +
                        "\"code\":\"ERR_ALREADY_EXISTS\"," +
                        "\"message\":\"The item already exists\"," +
                        "\"parameters\":" +
                            "[" +
                                "{\"key\":\"12345\"}" +
                            "]" +
                    "}" +
                    "}";
            responseString = res;
                        
        } else if (doReturn.equals(BEGINSYNC)) {
            
            //System.out.println(".......doPost BEGIN SYNC");
            responseString = "";
            
        } else if (doReturn.equals(ENDSYNC)) {

            //System.out.println(".......doPost BEGIN SYNC");
            responseString = "";
            
        } else if (doReturn.equals(INTERNAL_SERVER_ERROR)) {
            
            //System.out.println(".......doPost INTERNAL_SERVER_ERROR");
            response.setStatus(500);
            responseString = errorResponse;
            
        } else {
            
            //System.out.println(".......doPost ELSE");
            checkForError(response);
            
        }
        
        //System.out.println(".......doPost END .... " + request.getRequestURI());

        response.getWriter().write(responseString);
        response.getWriter().flush();
        response.getWriter().close();

    }

    /**
     * 
     * methods:
     * - updateItem (.../contact/items/1323  +  body)
     * 
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        //System.out.println("doPut");
        
        if (doReturn.equals(ITEMS)) {// update item
        
            responseString = "";
            
            String jsonRequest = request.getReader().readLine();
            
            // we need two option
            // 1 save the item in the run-time repository 
            // 2 keep the key
            // this problem because we have different response from addItem and getItem
            // from the addItem we have
            // {"data":{"key":12132}}
            // 
            // from the getItem we acheive
            // {"data":{"item":{"key":12132, "firstName:"maria", .... }}}            
            //
            // we should change the specs (devife better with example). the
            // addItem should returns
            // {"data":{"item":{"key":12132}}}            
            //
            
            // 1
            JSONObject jsonRoot4Rep = JSONObject.fromObject(jsonRequest);            
            JSONObject jsonData4Rep = jsonRoot4Rep.getJSONObject("data");
            JSONObject jsonItem4Rep = jsonData4Rep.getJSONObject("item");
            String key = jsonItem4Rep.optString("key");
            String responseForRepositoryString = jsonRoot4Rep.toString();
            repository.put(key,responseForRepositoryString);  
            
            // 2 
            
        }
        
        response.getWriter().write(responseString);
        response.getWriter().flush();
        response.getWriter().close();
        
    }

    /**
     * 
     * methods:
     * - deleteItem   (.../contact/items/1342)
     * 
     * 
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        //System.out.println(".............."+request.getRequestURI());
        
        if (request.getRequestURI().endsWith("/"+ITEMS)){
            
            // remove all items
            
            responseString = "";
            
        } else {
            
            // remove single item
            
            String key = request.getRequestURI().substring(request.getRequestURI().lastIndexOf('/')+1, 
                                                           request.getRequestURI().length());

            if(repository.containsKey(key)){
                repository.remove(key);
                responseString = "";
            } else {
                response.setStatus(500);
                responseString = "key not found on repository";
            }
            
        }
        
        
        response.getWriter().write(responseString);
        response.getWriter().flush();
        response.getWriter().close();
    }


    //---------------------------------------------------------- Private Methods
    
    
    /**
     * 
     * @param response
     */
    private void checkForError(HttpServletResponse response){
        if (doReturn.equals(BAD_REQUEST)) {// error 406
            response.setStatus(Integer.parseInt(BAD_REQUEST));
            responseString = "";
        } else if (doReturn.equals(UNAUTHORIZED)) {// error 401
            response.setStatus(Integer.parseInt(UNAUTHORIZED));
            responseString = "";
        } else if (doReturn.equals(INTERNAL_SERVER_ERROR)) {// error 500
            response.setStatus(Integer.parseInt(INTERNAL_SERVER_ERROR));
            responseString = errorResponse;
        }
    }

}