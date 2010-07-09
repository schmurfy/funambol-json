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
package com.funambol.json.gui;

import com.funambol.foundation.items.model.EntityWrapper;
import com.funambol.json.coredb.dao.DBManager;
import com.funambol.json.data.FakeJsonItem;
import com.funambol.json.data.Repository;
import com.funambol.json.gui.html.HTMLManager;
import com.funambol.json.gui.html.NameValuePair;
import com.funambol.json.gui.html.ParentTag;
import com.funambol.json.gui.html.Tag;
import com.funambol.json.gui.html.conversion.ConversionException;
import com.funambol.json.gui.html.conversion.HTMLConversionManager;
import com.funambol.json.gui.html.conversion.Converter;

import com.funambol.json.security.JsonUser;
import com.funambol.json.utility.Definitions;
import com.funambol.json.utility.Util;
import com.funambol.json.utility.ServletProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * This servlet handle user interface requests to the fake backend.
 *
 */
public class GuiServlet extends HttpServlet implements javax.servlet.Servlet {

    public final static long serialVersionUID = 1L;
    public final static String APPNAME = "gui";
    private final static Logger log = Logger.getLogger(Definitions.LOG_NAME);
    // Fake Backend parameters name
    public final static String ACTION = "ACTION";
    public final static String ACTIONID = "actionId";
    public final static String STEP = "STEP";
    public final static String STEPID = "stepId";
    public final static String GROUP = "GROUP";
    public final static String KEY = "KEY";
    public final static String KEYID = "keyId";
    public final static String JSONCONTENT = "JSONCONTENT";
    public final static String FORMAT = "FORMAT";
    public final static String NOTATION = "NOTATION";
    public final static String FILENAME = "FILENAME";
    public final static String CONFIGURATION_TARGET = "CONFIGURATION_TARGET";
    public final static String DRIVER = "DRIVER";
    public final static String URL = "URL";
    public final static String USER = "USER";
    public final static String PASSWORD = "PASSWORD";
    // Step names
    public final static String CHOOSE = "CHOOSE";
    public final static String PREPARE = "PREPARE";
    public final static String COMMIT = "COMMIT";
    // Fake backend supported actions
    public final static String UPLOADEXAMPLE = "UPLOADEXAMPLE";
    public final static String LIST = "LIST";
    public final static String LOGIN = "LOGIN";
    public final static String ADD = "ADD";
    public final static String CONFIGURE = "CONFIGURE";
    public final static String DELETE = "DELETE";
    public final static String MODIFY = "MODIFY";
    public final static String VIEW = "VIEW";
    public final static String EMPTYALL = "EMPTYALL";
    public final static String EMPTYGROUP = "EMPTYGROUP";
    // Calendar formats
    public final static String CALENDAR_SIF_FORMAT = "CALENDAR_SIF_FORMAT";
    public final static String CALENDAR_VCAL_FORMAT = "CALENDAR_VCAL_FORMAT";
    public final static String CALENDAR_VCAL_FORMAT_LABEL = "VCal";
    public final static String CALENDAR_ICAL_FORMAT = "CALENDAR_ICAL_FORMAT";
    public final static String CALENDAR_ICAL_FORMAT_LABEL = "ICal";
    public final static String SIF_FORMAT_LABEL = "Sif";
    public final static String RFC_NOTATION = "RFC_NOTATION";
    public final static String RFC_NOTATION_LABEL = "Rfc";
    public final static String RFC_VCAL_NOTATION = "RFC_VCAL_NOTATION";
    public final static String RFC_VCAL_NOTATION_LABEL = "VCal";
    public final static String RFC_ICAL_NOTATION = "RFC_ICAL_NOTATION";
    public final static String RFC_ICAL_NOTATION_LABEL = "ICal";
    public final static String EXTENDED_NOTATION = "EXTENDED_NOTATION";
    public final static String EXTENDED_NOTATION_LABEL = "Extended";
    public final static String DATASOURCE = "DATASOURCE";
    public final static String UNKNOWN_VALUE = "???";
    private final static DateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd  kk:mm.ss:SSS");

    @Override
    public void init() throws ServletException {
        super.init();
        ServletProperties.setPath(this.getServletContext().getRealPath(Definitions.PROPERTIES_PATH));
    }


    private String applyConversion(String group, String format, String notation, File file) throws IOException, Exception {
        String buffer = IOUtils.toString(new FileInputStream(file));
        if (Converter.I_RAW_FORMAT.equals(format)) {
            return buffer;
        } else {
            Converter converter = HTMLConversionManager.getConverter(group);
            if (converter == null) {
                throw new ConversionException("Converter not found for group [" + group + "].");
            }
            return converter.applyConversion(format, notation, buffer);
        }
    }

    private String configure(HttpServletRequest request, String objectToConfigure) throws Exception {
        if (DATASOURCE.equals(objectToConfigure)) {
            String url = getRequestData(request, URL, false);
            String driver = getRequestData(request, DRIVER, false);
            String user = getRequestData(request, USER, false);
            String password = getRequestData(request, PASSWORD, false);
            DBManager.getManager().configureDatasource(user, password, url, driver);
        }

        return operationExecuted("configuring " + objectToConfigure, request);
    }

    private String doConfigure(HttpServletRequest request) throws Exception {
        String step = getRequestData(request, STEP, true);
        if (CHOOSE.equals(step)) {
            return showConfigurationIndex(request);
        } else if (PREPARE.equals(step)) {
            String objectToConfigure = getRequestData(request, CONFIGURATION_TARGET, true);
            return showConfigurationPage(request, objectToConfigure);
        } else if (COMMIT.equals(step)) {
            String objectToConfigure = getRequestData(request, CONFIGURATION_TARGET, true);
            return configure(request, objectToConfigure);
        }

        throw new Exception("Step [" + step + "] not supported.");
    }

    private String doEmptyGroup(HttpServletRequest request) throws Exception {
        String group = getRequestData(request, GROUP, true);

        Repository rep = new Repository((String) request.getSession().getAttribute("username"), TimeZone.getDefault(), "UTF-8");
        rep.deleteAllItems(group);

        return operationExecuted(EMPTYALL, request);
    }

    private String doListAll(HttpServletRequest request) {

        String result = HTMLManager.getHtmlHeaderFor("Choose a group");

        Repository rep = new Repository((String) request.getSession().getAttribute("username"), TimeZone.getDefault(), "UTF-8");
        Iterator<String> it = rep.getGroupKeySet();
        while (it.hasNext()) {
            String groupName = it.next();
            result += " - " + HTMLManager.buildHRef(APPNAME, groupName, NameValuePair.parseFromStrings(ACTION, LIST, GROUP, groupName)) + " - " +
                    HTMLManager.buildHRef(APPNAME, "Delete All", NameValuePair.parseFromStrings(ACTION, EMPTYGROUP, GROUP, groupName)) + "<br>";
        }
        result += "<br>" + HTMLManager.createOneRowTable(HTMLManager.buildHRef(APPNAME, "Empty All", NameValuePair.parseFromStrings(ACTION, EMPTYALL)), HTMLManager.buildHRef("Home", APPNAME)) + "<br>";
        result += HTMLManager.closePage();
        return result;
    }

    private String doUploadExample(Map<String, String> parameters, Map<String, File> files, String action) throws Exception {
        String step = parameters.get(STEP);
        if (COMMIT.equals(step)) {
            String group = parameters.get(GROUP);
            String format = parameters.get(FORMAT);
            String notation = parameters.get(NOTATION);
            File file = files.remove(FILENAME);
            if (file != null) {
                String fileContent = applyConversion(group, format, notation, file);
                file.delete();
                return showAddPage(group, fileContent);
            }

        }
        throw new Exception("Invalid step [" + step + "] for action [Upload].");

    }

    private String doUploadExample(HttpServletRequest request) throws Exception {
        String step = getRequestData(request, STEP, true);
        String group = getRequestData(request, GROUP, true);
        if (PREPARE.equals(step)) {
            return showUploadExamplePage(group);
        }
        throw new Exception("Invalid step [" + step + "] for action [Upload].");
    }

    private String doView(HttpServletRequest request) throws Exception {
        String group = getRequestData(request, GROUP);
        String key = getRequestData(request, KEY);
        String result = HTMLManager.getHtmlHeaderFor("Inspecting " + group + " object for key [" + key + "].");
        Repository rep = new Repository((String) request.getSession().getAttribute("username"), TimeZone.getDefault(), "UTF-8");
        String jsonContent = rep.getContent(group, key);
        if (jsonContent == null) {
            throw new Exception(group + " not found for key [" + key + "].");
        }
        result += HTMLManager.addTextArea(JSONCONTENT, jsonContent, 80, 30, true);
        result += HTMLManager.addAction("backForm", "List again", LIST, NameValuePair.parseFromStrings(GROUP, group), ParentTag.OPEN);
        result += HTMLManager.closePage(buildCommonFooter(request));
        return result;


    }

    private String long2String(long creationTime) {
        if (creationTime < 0) {
            return UNKNOWN_VALUE;
        }
        return dateFormatter.format(new Date(creationTime));
    }

    private void manageUpload(HttpServletRequest request, Map<String, String> parameters, Map<String, File> files) throws Exception {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            // Create a factory for disk-based file items
            FileItemFactory factory = new DiskFileItemFactory();

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);

            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            // Process the uploaded items
            Iterator iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (item.isFormField()) {
                    parameters.put(item.getFieldName(), item.getString());
                } else {
                    String fieldName = item.getFieldName();
                    String fileName = item.getName();
                    File output = File.createTempFile(fieldName, fileName);
                    item.write(output);
                    files.put(fieldName, output);
                }
            }
        } else {
            throw new Exception("File upload failed.");
        }
    }

    private String operationExecuted(String action, HttpServletRequest request) {
        String result = HTMLManager.getHtmlHeaderFor("Operation [" + action + "] successfully completed!");
        result += HTMLManager.closePage(buildCommonFooter(request));
        return result;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (log.isTraceEnabled()) {
            log.trace("do get");
        }
        handleRequest(request, response);
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        String action = null;
        String result = null;
        if (isMultipart) {
            Map<String, String> parameters = new HashMap<String, String>();
            Map<String, File> files = new HashMap<String, File>();

            try {
                manageUpload(request, parameters, files);
            } catch (Exception ex) {
                writeError(ex, response, "Managing multi part form.", request);
            }

            action = parameters.get(ACTION);
            try {
                log.debug("Handling action [" + action + "].");
                result = doAction(parameters, files, action);
            } catch (Exception ex) {
                writeError(ex, response, action, request);
            }

        } else {

            try {
                action = getAction(request);
            } catch (Exception ex) {
                log.warn("No action found, performing default operation.");
            }


            try {
                log.debug("Handling action [" + action + "].");
                result = doAction(request, action);
            } catch (Exception ex) {
                writeError(ex, response, action, request);
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
        if (log.isTraceEnabled()) {
            log.trace("do post");
        }
        handleRequest(request, response);
    }

    private String doAction(Map<String, String> parameters, Map<String, File> files, String action) throws Exception {
        if (UPLOADEXAMPLE.equals(action)) {
            return doUploadExample(parameters, files, action);
        }

        String result = HTMLManager.getHtmlHeaderFor("Json Fake Backend Home Page");
        result += showWelcomePage("");
        result += HTMLManager.closePage();
        return result;
    }

    private String doAction(HttpServletRequest request, String action) throws Exception {
        if (LOGIN.equals(action)) {
            if (log.isTraceEnabled()) {
                log.trace("username:" + request.getParameter("username"));
                log.trace("password:" + request.getParameter("password"));
                log.trace("vcalical:" + request.getParameter("vcalical"));
                log.trace("customeruserid:" + request.getParameter("customeruserid"));
            }



            if ("on".equals(request.getParameter("customeruserid"))) {
                Repository.customerUserid = true;
            }

            if (Repository.login(new JsonUser((String) request.getParameter("username"), (String) request.getParameter("password"))) != null) {
                request.getSession().setAttribute("username", request.getParameter("username"));
                return doList(request);
            } else {
                String result = HTMLManager.getHtmlHeaderFor("Json Fake Backend Home Page");
                result += showWelcomePage("ERROR LOGGIN IN ...");
                result += HTMLManager.closePage();
                return result;
            }

        }

        if (LIST.equals(action)) {
            return doList(request);
        }
        if (ADD.equals(action)) {
            return doAdd(request);
        }

        if (DELETE.equals(action)) {
            return doDelete(request);
        }

        if (MODIFY.equals(action)) {
            return doModify(request);
        }

        if (VIEW.equals(action)) {
            return doView(request);
        }

        if (EMPTYGROUP.equals(action)) {
            return doEmptyGroup(request);
        }

        if (UPLOADEXAMPLE.equals(action)) {
            return doUploadExample(request);
        }

        if (CONFIGURE.equals(action)) {
            return doConfigure(request);
        }

        String result = HTMLManager.getHtmlHeaderFor("Json Fake Backend Home Page");
        result += showWelcomePage("");
        result += HTMLManager.closePage();
        return result;
    }

    private String doAdd(HttpServletRequest request) throws Exception {
        String group = getRequestData(request, GROUP, true);
        String step = getRequestData(request, STEP, true);

        if (PREPARE.equals(step)) {
            return showAddPage(group, "Put your Json object here");
        } else if (COMMIT.equals(step)) {
            Repository rep = new Repository((String) request.getSession().getAttribute("username"), TimeZone.getDefault(), "UTF-8");
            if (rep == null) {
                throw new Exception("No repository found for group [" + group + "].");
            }
            String jsonContent = getRequestData(request, JSONCONTENT);

            rep.addItem(group, new FakeJsonItem(jsonContent, Util.getSince(request)));

            return operationExecuted(ADD, request);
        }


        throw new Exception("Invalid step [" + step + "] for action [Add].");
    }

    private String doDelete(HttpServletRequest request) throws Exception {
        String group = getRequestData(request, GROUP, true);
        String key = getRequestData(request, KEY, true);

        Repository rep = new Repository((String) request.getSession().getAttribute("username"), TimeZone.getDefault(), "UTF-8");
        rep.delete(group, key);

        return operationExecuted(DELETE, request);

    }

    private String doList(HttpServletRequest request) throws Exception {
        String group = getRequestData(request, GROUP, false);
        String dataStoreTypeKey = group + "." + Definitions.DATASTORETYPE;

        if (group == null) {
            return doListAll(request);
        }

        if (request.getMethod().equalsIgnoreCase("post")) {
            String newDataStoreType = getRequestData(request, Definitions.DATASTORETYPE);
            if (newDataStoreType == null) newDataStoreType = "";
            
            if (!newDataStoreType.equals("")) {
                ServletProperties.getProperties().setProperty(dataStoreTypeKey, newDataStoreType);
                ServletProperties.saveProperties();
            }
        }

        String dataStoreType = ServletProperties.getProperties().getProperty(dataStoreTypeKey, "");

        String result = HTMLManager.getHtmlHeaderFor("Listing group [" + group + "]: <br>", true);
        Repository rep = new Repository((String) request.getSession().getAttribute("username"), TimeZone.getDefault(), "UTF-8");

        if (!rep.isEmpty(group)) {
            result += HTMLManager.openTag(Tag.FORM, NameValuePair.parseFromStrings("method", "get", "action", "./gui"));
            result += HTMLManager.addFormField(KEY, "", KEYID);
            result += HTMLManager.addFormField(GROUP, group);
            result += HTMLManager.addFormField(STEP, PREPARE);
            result += HTMLManager.addFormField(ACTION, "", ACTIONID);
            result += "<table><tr><td><b>Status</b></td><td><b>Update Time</b></td><td><b>Item key</b></td><td colspan=\"2\"><b>Operations</b></td></tr>";
            Iterator<String> it = rep.getAllKeys(group, new Timestamp(0), new Timestamp(System.currentTimeMillis())).iterator();
            while (it.hasNext()) {
                String itemKey = it.next();
                if (itemKey != null) {
                    EntityWrapper item = rep.getItemForListing(group, itemKey);
                    char status = '?';

                    long updateTime = -1;

                    if (item != null) {
                        status = item.getStatus();
                        updateTime = item.getLastUpdate().getTime();


                    }

                    result += "<tr><td>[" + status + "]</td><td>" + long2String(updateTime) + "</td><td>" + HTMLManager.buildHRef(APPNAME, itemKey, NameValuePair.parseFromStrings(ACTION, VIEW, GROUP, group, KEY, itemKey)) + "</td><td> " + HTMLManager.buildHtmlTag(Tag.BUTTON, "Modify", NameValuePair.parseFromStrings("onclick", "doAction('" + ACTIONID + "','" + MODIFY + "','" + KEYID + "','" + itemKey + "');", "type", "submit")) + "</td><td> " + (('D' == status) ? "Marked as deleted" : HTMLManager.buildHtmlTag(Tag.BUTTON, "Delete", NameValuePair.parseFromStrings("onclick", "doAction('" + ACTIONID + "','" + DELETE + "','" + KEYID + "','" + itemKey + "');", "type", "submit"))) + "</td></tr>";
                }

            }
            result += "</table>";
            result += HTMLManager.closeTag(Tag.FORM);
        } else {
            result += "No items for the group [" + group + "]";
        }

        result += "<p>";

        result += HTMLManager.addAction("addForm", "Add element", ADD, NameValuePair.parseFromStrings(STEP, PREPARE, GROUP, group), ParentTag.BOTH) + "<br>";
        result += HTMLManager.getHtmlForDatastoreType(group, dataStoreType);
        result += HTMLManager.closePage(buildCommonFooter(request));
        return result;
    }

    private String doModify(HttpServletRequest request) throws Exception {
        String group = getRequestData(request, GROUP, true);
        String step = getRequestData(request, STEP, true);
        String key = getRequestData(request, KEY, true);

        if (PREPARE.equals(step)) {
            return showModifyPage(group, key, request);
        } else if (COMMIT.equals(step)) {

            Repository rep = new Repository((String) request.getSession().getAttribute("username"), TimeZone.getDefault(), "UTF-8");
            rep.getItem(group, key);
            String jsonContent = getRequestData(request, JSONCONTENT, true);

            rep.updateItem(group, new FakeJsonItem(jsonContent, System.currentTimeMillis()), key);

            return operationExecuted(MODIFY, request);
        }


        throw new Exception("Invalid step [" + step + "] for action [Add].");
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

        response.setContentType("text/html");

        PrintWriter writer = response.getWriter();
        writer.write(result);
        writer.flush();
    }

    private String showAddPage(String group, String jsonContent) {
        String result = HTMLManager.getHtmlHeaderFor("Adding new element in group <b>" + group + "</b>:", true);
        result += HTMLManager.openTag(Tag.FORM, NameValuePair.parseFromStrings("method", "post", "action", "./gui"));
        result += HTMLManager.addFormField(STEP, COMMIT, STEPID);
        result += HTMLManager.addFormField(GROUP, group);
        result += HTMLManager.addFormField(ACTION, "", ACTIONID);
        result += HTMLManager.addTextArea(JSONCONTENT, jsonContent, 80, 30, false);
        result += "<br><table><tr><td>";

        result += HTMLManager.buildHtmlTag(Tag.BUTTON, "Add", NameValuePair.parseFromStrings("onclick", "doAction('" + ACTIONID + "','" + ADD + "','" + STEPID + "','" + COMMIT + "');", "type", "submit"));
        result += "</td><td>";
        result += HTMLManager.buildHtmlTag(Tag.BUTTON, "Cancel", NameValuePair.parseFromStrings("onclick", "doAction('" + ACTIONID + "','" + LIST + "','','');", "type", "submit"));
        result += "</td><td>";
        result += HTMLManager.buildHtmlTag(Tag.BUTTON, "Upload", NameValuePair.parseFromStrings("onclick", "doAction('" + ACTIONID + "','" + UPLOADEXAMPLE + "','" + STEPID + "','" + PREPARE + "');", "type", "submit"));
        result += "</td></tr></table>";
        result += HTMLManager.closeTag(Tag.FORM);
        result += HTMLManager.closePage();
        return result;
    }

    private String showModifyPage(String group, String key, HttpServletRequest request) throws Exception {
        String result = HTMLManager.getHtmlHeaderFor("Updating element [" + key + "] in group <b>" + group + "</b>:", true);
        result += HTMLManager.openTag(Tag.FORM, NameValuePair.parseFromStrings("method", "post", "action", "./gui"));
        result += HTMLManager.addFormField(STEP, COMMIT);
        result += HTMLManager.addFormField(GROUP, group);
        result += HTMLManager.addFormField(KEY, key);
        result += HTMLManager.addFormField(ACTION, "", ACTIONID);
        Repository rep = new Repository((String) request.getSession().getAttribute("username"), TimeZone.getDefault(), "UTF-8");
        result += HTMLManager.addTextArea(JSONCONTENT, rep.getContent(group, key), 80, 30, false) + "<br>";
        result += "<br><table><tr><td>";
        result += HTMLManager.buildHtmlTag(Tag.BUTTON, "Update", NameValuePair.parseFromStrings("onclick", "doAction('" + ACTIONID + "','" + MODIFY + "','','');", "type", "submit"));
        result += "</td><td>";
        result += HTMLManager.buildHtmlTag(Tag.BUTTON, "Cancel", NameValuePair.parseFromStrings("onclick", "doAction('" + ACTIONID + "','" + LIST + "','','');", "type", "submit"));
        result += "</td></tr></table>";
        result += HTMLManager.closeTag(Tag.FORM);
        result += HTMLManager.closePage();
        return result;
    }

    private String showConfigurationPage(HttpServletRequest request, String objectToConfigure) throws Exception {
        if (DATASOURCE.equals(objectToConfigure)) {
            String result = HTMLManager.getHtmlHeaderFor("Configuring datasource:", true);
            result += HTMLManager.openTag(Tag.FORM, NameValuePair.parseFromStrings("method", "post", "action", "./gui"));
            result += HTMLManager.addFormField(STEP, COMMIT);
            result += HTMLManager.addFormField(CONFIGURATION_TARGET, DATASOURCE);
            result += HTMLManager.addFormField(ACTION, "", ACTIONID);
            result += "<br><table>";
            result += HTMLManager.buildTableRow(HTMLManager.buildHtmlTag(Tag.H3, "Driver class", (NameValuePair[]) null));
            result += HTMLManager.buildTableRow(HTMLManager.addTextfield(DRIVER, DBManager.getManager().getDriver(), 40, 80));
            result += HTMLManager.buildTableRow(HTMLManager.buildHtmlTag(Tag.H3, "Database url", (NameValuePair[]) null));
            result += HTMLManager.buildTableRow(HTMLManager.addTextfield(URL, DBManager.getManager().getUrl(), 40, 80));
            result += HTMLManager.buildTableRow(HTMLManager.buildHtmlTag(Tag.H3, "Database user", (NameValuePair[]) null));
            result += HTMLManager.buildTableRow(HTMLManager.addTextfield(USER, DBManager.getManager().getUser(), 40, 80));
            result += HTMLManager.buildTableRow(HTMLManager.buildHtmlTag(Tag.H3, "Database password", (NameValuePair[]) null));
            result += HTMLManager.buildTableRow(HTMLManager.addPassword(PASSWORD, DBManager.getManager().getPassword(), 40, 80));
            result += HTMLManager.buildTableRow(HTMLManager.buildHtmlTag(Tag.BUTTON, "Commit", NameValuePair.parseFromStrings("onclick", "doAction('" + ACTIONID + "','" + CONFIGURE + "','','');", "type", "submit")), HTMLManager.buildHtmlTag(Tag.BUTTON, "Cancel", NameValuePair.parseFromStrings("onclick", "doAction('" + ACTIONID + "','','','');", "type", "submit")));//,
            result += "</table>";
            result += HTMLManager.closeTag(Tag.FORM);
            result += HTMLManager.closePage();
            return result;
        }
        throw new Exception("Object to configure [" + objectToConfigure + "] not supported.");
    }

    private String showConfigurationIndex(HttpServletRequest request) {
        String result = HTMLManager.getHtmlHeaderFor("Configure: ");
        result += "<br>" + HTMLManager.buildHRef(APPNAME, "Datasource", NameValuePair.parseFromStrings(ACTION, CONFIGURE, STEP, PREPARE, CONFIGURATION_TARGET, DATASOURCE));
        result += HTMLManager.closePage();
        return result;
    }

    private String showUploadExamplePage(String group) {
        String result = HTMLManager.getHtmlHeaderFor("Upload new element for group <b>" + group + "</b>:", true);
        result += HTMLManager.openTag(Tag.FORM, NameValuePair.parseFromStrings("method", "post", "action", "./gui", "enctype", "multipart/form-data"));
        result += HTMLManager.addFormField(STEP, COMMIT);
        result += HTMLManager.addFormField(GROUP, group);
        result += HTMLManager.addFormField(ACTION, "", ACTIONID);
        NameValuePair[] availableFormats = HTMLConversionManager.getConverter(group).getAvailableInputFormat();
        if (availableFormats != null && availableFormats.length > 0) {
            result += HTMLManager.buildHtmlTag(Tag.H3, "Select input format", (NameValuePair[]) null);
            result += HTMLManager.createOneRowTable(HTMLManager.addRadios(FORMAT, availableFormats, (NameValuePair[]) null));
        }
        availableFormats = HTMLConversionManager.getConverter(group).getAvailableOutputFormat();
        if (availableFormats != null && availableFormats.length > 0) {
            result += HTMLManager.buildHtmlTag(Tag.H3, "Select output format", (NameValuePair[]) null);
            result += HTMLManager.createOneRowTable(HTMLManager.addRadios(NOTATION, availableFormats, (NameValuePair[]) null));
        }
        result += HTMLManager.buildHtmlTag(Tag.H3, "Choose input file", (NameValuePair[]) null);
        result += HTMLManager.openTag(Tag.INPUT, NameValuePair.parseFromStrings("type", "file", "name", FILENAME));
        result += "<br><table><tr><td>";
        result += HTMLManager.buildHtmlTag(Tag.BUTTON, "Upload", NameValuePair.parseFromStrings("onclick", "doAction('" + ACTIONID + "','" + UPLOADEXAMPLE + "','','');", "type", "submit"));
        result += "</td><td>";
        result += HTMLManager.buildHtmlTag(Tag.BUTTON, "Cancel", NameValuePair.parseFromStrings("onclick", "doAction('" + ACTIONID + "','" + LIST + "','','');", "type", "submit"));
        result += "</td></tr></table>";
        result += HTMLManager.closeTag(Tag.FORM);
        result += HTMLManager.closePage();
        return result;
    }

    private String showWelcomePage(String message) throws Exception {
 
        return "<FORM action=\"./gui?ACTION=LOGIN\" method=\"post\"><P>" +
                "<LABEL for=\"username\">username: </LABEL><INPUT type=\"text\" name=\"username\"><BR>" +
                "<LABEL for=\"password\">password: </LABEL><INPUT type=\"password\" name=\"password\"><BR>" +
                "<INPUT TYPE=CHECKBOX NAME=\"customeruserid\">" +
                "Return customerid in login requests<br>" +
                "(useful when datastore supports multiple aliases to the same user, <br>" +
                "the connector will create the user in funambol database using the field returned as customeruserid, <br>" +
                "witch will be send together with the session id if the login is successfull)<BR>" +
                "<INPUT type=\"submit\" value=\"Send\"></P></FORM>" + "<br>" + message;


    }

    private void writeError(Exception ex, HttpServletResponse response, String action, HttpServletRequest request) throws IOException {
        log.error("An error occurred handling action [" + action + "]", ex);
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
            errorPage +=
                    sw.getBuffer();
        }

        errorPage += HTMLManager.closePage(buildCommonFooter(request));
        sendHtmlPage(response, errorPage);
    }

    protected String buildCommonFooter(HttpServletRequest request) {
        Repository rep = new Repository((String) request.getSession().getAttribute("username"), TimeZone.getDefault(), "UTF-8");
        String temp = "Logged as :" + request.getSession().getAttribute("username") + " | " + HTMLManager.buildHRef("Logout ", APPNAME) + " <table><tr><td>";
        temp +=
                "<td>" + HTMLManager.buildHRef(GuiServlet.APPNAME, " Repository ", NameValuePair.parseFromStrings(GuiServlet.ACTION, GuiServlet.LIST)) + "</td>";
        Iterator<String> it = rep.getGroupKeySet();
        while (it.hasNext()) {
            String groupName = it.next();
            temp +=
                    "<td>" + HTMLManager.buildHRef(GuiServlet.APPNAME, groupName, NameValuePair.parseFromStrings(GuiServlet.ACTION, GuiServlet.LIST, GuiServlet.GROUP, groupName)) + "</td>";
        }

        temp += "</tr></table>";
        return temp;
    }
}
