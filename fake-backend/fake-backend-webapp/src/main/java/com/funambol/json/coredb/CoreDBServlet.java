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
package com.funambol.json.coredb;

import com.funambol.json.coredb.dao.DBManager;
import com.funambol.json.coredb.dao.Long2FormattedDateFilter;
import com.funambol.json.coredb.dao.String2FormattedDateFilter;
import com.funambol.json.gui.GuiServlet;
import com.funambol.json.gui.html.HTMLManager;
import com.funambol.json.gui.html.NameValuePair;
import com.funambol.json.gui.html.Tag;
import com.funambol.json.utility.Definitions;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

/**
 * This servlet is used to handle the access to the DS Server database.
 *
 */
public class CoreDBServlet extends HttpServlet implements javax.servlet.Servlet {

    // Fake Backend parameters name
    public final static String ACTION       = "ACTION";
    public final static String ACTIONID     = "actionId";
    public final static String STEP         = "STEP";
    public final static String STEPID       = "stepId";
    public final static String GROUP        = "GROUP";
    public final static String KEY          = "KEY";
    public final static String KEYID        = "keyId";
    
    
    // Step names
    public final static String PREPARE = "PREPARE";
    public final static String COMMIT = "COMMIT";    
    
    // Fake backend supported actions
    public final static String LIST = "LIST";
    public final static String ADD = "ADD";
    public final static String DELETE = "DELETE";
    public final static String VIEW = "VIEW";
    
    public final static long serialVersionUID = 1L;
    public static String APPNAME = "coredb";
    private static final Logger log = Logger.getLogger(Definitions.LOG_NAME);




    
    public void init() throws ServletException {
        DBManager.getManager().initDataSource();
    }

    private String doDelete(HttpServletRequest request, String action) throws Exception {
        String[] keys               = new String[]{"principal","sync_source"};
        Map<String,String> item     = fill(request,keys);
        
        
        
        if(DBManager.getManager().deleteItem("fnbl_last_sync", item, keys))
            return operationExecuted(action);
        
        throw new Exception("Cancellazione non eseguita.");
    }

    private Map<String,String> fill(HttpServletRequest request, String[] keys) throws Exception {
        if(keys!=null) {
            Map<String, String> item= new   HashMap<String, String>();
            for(String key:keys) {
                if(key!=null) {
                    String value = getRequestData(request, key);
                    item.put(key, value);
                }
            }
            return item;
        }
        return null;

    }

  
          

    

   private String operationExecuted(String action) {
        String result = HTMLManager.getHtmlHeaderFor("Operation [" + action + "] successfully completed!");
        result += HTMLManager.closePage(buildCommonFooter());
        return result;
    }
   
   protected String buildCommonFooter() {
       return HTMLManager.createOneRowTable(HTMLManager.buildHRef(" Home ", GuiServlet.APPNAME),HTMLManager.buildHRef("LastSync", APPNAME));
   }

    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        String action = null;
        String result = null;
        if(isMultipart) {
            Map<String, String> parameters = new HashMap<String, String>();
            Map<String, File> files = new HashMap<String, File>();

            try {
               // manageUpload(request, parameters, files);
            } catch (Exception ex) {
                writeError(ex, response, "Managing multi part form.");
            }
            
            action = parameters.get(ACTION);
            try {
                log.info("Handling multi request action ["+action+"]");
                result =  doAction(parameters,files,action);
            } catch (Exception ex) {
                 writeError(ex, response, action);
            }
                
        } else {        
        
            try {
                log.info("Handling action ["+action+"]");
                action = getAction(request);
            } catch (Exception ex) {
                log.warn("No action found, performing default operation.");
            }

        
            try {
                result = doAction(request, action);
            } catch (Exception ex) {
                writeError(ex, response, action);
            }
        }
        sendHtmlPage(response, result);
    }

    /**
     *
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }


    private String doAction(Map<String, String> parameters,Map<String, File> files, String action) throws Exception {
          return null;
    }
    
    private String doAction(HttpServletRequest request, String action) throws Exception {
        if(DELETE.equals(action)) {
            return doDelete(request, action);
        }
        return doDefault(request, action);
    }
    
    private String doDefault(HttpServletRequest request, String action) throws Exception {
        String result = HTMLManager.getHtmlHeaderFor("Last Sync Content:");
        
        String[] columns = new String[]{"principal","sync_source","sync_type","status","last_anchor_client","start_sync","end_sync"};
        String[] keys    = new String[]{"principal","sync_source"};
        
        result+=buildHtmlTable("SELECT * FROM fnbl_last_sync", columns, keys);
        result+="<br>"+HTMLManager.buildHRef(GuiServlet.APPNAME,"Home", (NameValuePair[])null ) + "<br>";;
        result += HTMLManager.closePage();
        return result;
    
    }




    private <T> T getAttribute(String attributeName, HttpServletRequest request, Class<T> clazz) {
        if (request != null && clazz != null) {
            Object attribute = request.getAttribute(attributeName);
            if (attribute != null && clazz.isAssignableFrom(attribute.getClass())) {
                return clazz.cast(attribute);
            }
        }

        return null;

    }

    private String getRequestData(HttpServletRequest request, String key) throws Exception {
        return getRequestData(request, key, false);
    }

    private String getRequestData(HttpServletRequest request, String key, boolean required) throws Exception {
        if (request != null) {
            String value = request.getParameter(key);
            if (value != null) {
                return value;
            }
            value = getAttribute(key, request, String.class);
            return value;
        }

        if (required) {
            throw new Exception("Required parameter/attribute [" + key + "] not found.");
        }
        return null;
    }

    private String getAction(HttpServletRequest request) throws Exception {
        return getRequestData(request, ACTION);
    }


    private void sendHtmlPage(HttpServletResponse response, String result) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write(result);
        writer.flush();
    }


    private void writeError(Exception ex, HttpServletResponse response, String action) throws IOException {
        log.error("An error occurred while handling action ["+action+"].", ex);
        String errorPage = HTMLManager.getHtmlHeaderFor("An error occurred processing request:");
        response.setStatus(500);
        if (action != null) {
            errorPage += "Action was [" + action + "]<b>";
        }
        if (ex != null) {
            errorPage += "Exception was <b>]";
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            errorPage += sw.getBuffer();
        }
        errorPage += HTMLManager.closePage(buildCommonFooter());
        sendHtmlPage(response, errorPage);
    }
    
    public String buildHtmlTable(String query,String[] columnCaptions, String[] keys) throws Exception {
        String result = HTMLManager.openTag(Tag.TABLE);
        Set<String> keySet = new TreeSet<String>(Arrays.asList(keys));
        result+=HTMLManager.openTag(Tag.TR);
        for(String caption:columnCaptions) {
            result+=HTMLManager.buildHtmlTag(Tag.TD, "<b>"+caption+"</b>",(NameValuePair[]) null);
        }
        result+=HTMLManager.buildHtmlTag(Tag.TD, "<b>ACTION</b>",(NameValuePair[]) null);
        result+=HTMLManager.closeTag(Tag.TR);
        List<Map<String,String>> queryResult = DBManager.getManager().executeQuery(query,new String2FormattedDateFilter("LAST_ANCHOR_CLIENT"),new Long2FormattedDateFilter("START_SYNC","END_SYNC"));
        Iterator<Map<String,String>> qrIt = queryResult.iterator();
        while(qrIt.hasNext()) {
            Map<String,String> item = qrIt.next();
            result+=HTMLManager.openTag(Tag.TR);
            List<String> dataOnTheUrl = new ArrayList<String>();
            dataOnTheUrl.add(ACTION);
            dataOnTheUrl.add(DELETE);
            for(String caption:columnCaptions) {
                String value = item.get(caption);
                if(keySet.contains(caption))  {
                       dataOnTheUrl.add(caption);
                       dataOnTheUrl.add(value);
                }
                result+=HTMLManager.buildHtmlTag(Tag.TD, value,(NameValuePair[]) null);
            }
            result+=HTMLManager.buildHtmlTag(Tag.TD, HTMLManager.buildHRef(APPNAME,"Delete", NameValuePair.parseFromStrings(dataOnTheUrl.toArray(new String[]{}))),(NameValuePair[]) null);
            result+=HTMLManager.closeTag(Tag.TR);
        }
        
        result+=HTMLManager.closeTag(Tag.TABLE);
        return result;
    }
    
}