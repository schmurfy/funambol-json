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
package com.funambol.json.api.util;

import com.funambol.json.api.exception.IOOperationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * This class loads the config and test files
 */
public class ConfigLoader {
    //date formater    
    protected SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd'T'HH:mm.ss:SSS");
    // logger
    private static Logger log = Logger.getLogger(Def.LOG_NAME);
    // test cases directory
    private static final String TESTCASES_DIR = "testcases";
    // List of sources to be executed
    public HashMap<String, String[]> sourcesList = new HashMap<String, String[]>();
    // Server properties
    private Properties serverproperties = new Properties();

    public ConfigLoader() throws IOException {
        serverproperties.load(new FileInputStream(new File("config" + File.separator + "server.properties")));
    }

    /**
     * This method loads all the sources to be executed
     * eg. contacts, calendar etc
     * @return
     */
    public String[] getSources() {
        File sources = new File(TESTCASES_DIR);
        File tests = null;
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return !name.startsWith("_");
            }
        };
        String[] children = sources.list(filter);



        for (int i = 0; i < children.length; i++) {

            tests = new File(TESTCASES_DIR + File.separator + children[i]);
            if (log.isTraceEnabled()) {
                log.trace("->" + children[i] + " has " + tests.list(filter).length + " enabled tests");
            }
            if (tests.list(filter).length > 0) {
                sourcesList.put(children[i], tests.list(filter));

                for (int j = 0; j < tests.list(filter).length; j++) {
                    log.trace("--->" + tests.list(filter)[j]);
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("->" + children[i]);
            }
        }
        return children;
    }

    /**
     * Returns the list of tests for a source 
     * @param sourceName
     * @return
     */
    public String[] getTestForSource(String sourceName) {
        return sourcesList.get(sourceName);
    }

    /**
     * Returns the content of the operations file,
     * witch contains the tests to execute
     * @param sourceName
     * @param testName
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public String getOperationsFile(String sourceName, String testName) throws FileNotFoundException, IOException {
        if (log.isTraceEnabled()) {
            log.trace("operations for :" + TESTCASES_DIR + File.separator +
                    sourceName + File.separator +
                    testName + File.separator + "operation.properties");
        }
        String opFile = TESTCASES_DIR + File.separator +
                sourceName + File.separator +
                testName + File.separator + "operation.properties";
        return getFileContentAsString(opFile);
    }

    /**
     * returns the content of a file as a string
     * @param file
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    private String getFileContentAsString(String file) throws FileNotFoundException, IOException {
        File f = new File(file);
        BufferedReader input = new BufferedReader(new FileReader(f));
        String line = null;
        StringBuffer buffer = new StringBuffer();
        while ((line = input.readLine()) != null) {
            buffer.append(line);
            buffer.append("\n");
        }
        return buffer.toString();
    }

    /**
     * Stores a file , the path to store the file will be the one from the test beeing executed
     * @param fileName
     * @param content
     * @throws com.funambol.exception.IOOperationException
     */
    public void storeFile(String fileName, String content) throws IOOperationException {
        BufferedWriter output = null;
        try {


            if (log.isTraceEnabled()) {
                log.trace("creating file:" + TESTCASES_DIR + File.separator + fileName + " content:" + content);
            }
            File f = new File(TESTCASES_DIR + File.separator + fileName);
            output = new BufferedWriter(new FileWriter(f));
            output.write(content);
            output.flush();
        } catch (IOException ex) {
            throw new IOOperationException("Error Storing file :", ex);
        } finally {
            try {
                output.close();
            } catch (IOException ex) {
                throw new IOOperationException("Error Storing file, closing output stream :", ex);
            }
        }

    }

    /**
     * returns a Json file, a representation of an item
     * @param sourceName
     * @param testName
     * @param fileName
     * @return
     * @throws com.funambol.exception.IOOperationException
     */
    public String getJSONFile(String sourceName, String testName, String fileName) throws IOOperationException {
        try {

            String opFile = TESTCASES_DIR + File.separator + sourceName + File.separator + testName + File.separator + fileName + ".json";
            return getFileContentAsString(opFile);
        } catch (FileNotFoundException ex) {
            throw new IOOperationException("Error loading json file :", ex);
        } catch (IOException ex) {
            throw new IOOperationException("Error loading json file :", ex);
        }
    }

    /**
     * Read a key from a file (the file should only contain the key, and on the first line)
     * @param sourceName
     * @param testName
     * @param fileName
     * @return
     * @throws com.funambol.exception.IOOperationException
     */
    public String getKeyFromFile(String sourceName, String testName, String fileName, boolean isFromGetNUD) throws IOOperationException {
        BufferedReader input = null;
        try {
            String opFile = TESTCASES_DIR + File.separator + sourceName + File.separator + testName + File.separator + fileName + ".json";

            File f = new File(opFile);
            if (!f.exists()) {
                if (isFromGetNUD) {
                    return null;
                } else {
                    f = new File(TESTCASES_DIR + File.separator + sourceName + File.separator + testName + File.separator + "temp" + File.separator + fileName + ".json");
                }
            }

            input = new BufferedReader(new FileReader(f));
            String line = null;

            if ((line = input.readLine()) != null) {
                input.close();
                return line;
            }
        } catch (FileNotFoundException ex) {
            throw new IOOperationException("Error reading key from file :", ex);
        } catch (IOException ex) {
            throw new IOOperationException("Error reading key from file :", ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    throw new IOOperationException("Error reading key from file :", ex);
                }
            }
        }
        throw new IOOperationException("Error reading key from file, no key in the file :");
    }

    /**
     * returns a server property
     * @param key
     * @return
     */
    public String getServerProperty(String key) {
        return serverproperties.getProperty(key);

    }

    public long getServerTime(long time) {
//        if (log.isInfoEnabled()) {
//            log.info("converting from " + TimeZone.getDefault().getID() + " " + formatter.format(new Date(time)));
//        }
//        GregorianCalendar calendar = new GregorianCalendar();
//        calendar.setTimeInMillis(time);
//        GregorianCalendar remoteCalendar = new GregorianCalendar();
//        remoteCalendar.setTimeInMillis(calendar.getTimeInMillis());
//        remoteCalendar.setTimeZone(TimeZone.getTimeZone(getServerProperty(Def.SERVER_TZ)));
//
//        long convertedTime = remoteCalendar.getTimeInMillis() + remoteCalendar.getTimeZone().getRawOffset();
//        if (log.isInfoEnabled()) {
//            log.info("converted to " + getServerProperty(Def.SERVER_TZ) + " " + formatter.format(convertedTime));
//        }
//        return convertedTime;
        return time;
    }

    /**
     * returns server time as string
     * @param time
     * @return
     */
    public String getServerTimeStr(long time) {
        return formatter.format(getServerTime(time));
    }

    /**
     * returns the list of new items based on the files on the test dir
     * ( _added.json files)
     * @param sourceName
     * @param testName
     * @return
     * @throws com.funambol.json.api.exception.IOOperationException
     */
    public LinkedList<String> getNewItems(String sourceName, String testName) throws IOOperationException {
        File sources = new File(TESTCASES_DIR + File.separator + sourceName + File.separator + testName + File.separator);
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith("_added.json");
            }
        };
        String[] children = sources.list(filter);
        LinkedList<String> keys = new LinkedList<String>();
        String key = null;
        for (int i = 0; i < children.length; i++) {
            key = getKeyFromFile(sourceName, testName, children[i].split("\\.")[0], true);
            if (key != null) {
                keys.add(key);
            }
        }
        return keys;
    }

    /**
     * returns the list of updated items based on the files on the test dir
     * ( _updated.json files)
     * @param sourceName
     * @param testName
     * @return
     * @throws com.funambol.json.api.exception.IOOperationException
     */
    public LinkedList<String> getUpdatedItems(String sourceName, String testName) throws IOOperationException {
        File sources = new File(TESTCASES_DIR + File.separator + sourceName + File.separator + testName + File.separator);
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith("_updated.json");
            }
        };
        String[] children = sources.list(filter);
        LinkedList<String> keys = new LinkedList<String>();
        String key = null;
        for (int i = 0; i < children.length; i++) {
            key = getKeyFromFile(sourceName, testName, children[i].split("\\.")[0], true);
            if (key != null) {
                keys.add(key);
            }
        }
        return keys;
    }

    /**
     * returns the list of deleted items based on the files on the test dir
     * ( _removed.json files)
     * @param sourceName
     * @param testName
     * @return
     * @throws com.funambol.json.api.exception.IOOperationException
     */
    public LinkedList<String> getDeletedItems(String sourceName, String testName) throws IOOperationException {
        File sources = new File(TESTCASES_DIR + File.separator + sourceName + File.separator + testName + File.separator);

        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith("_removed.json");
            }
        };

        String[] children = sources.list(filter);
        LinkedList<String> keys = new LinkedList<String>();
        String key = null;

        for (int i = 0; i < children.length; i++) {
            key = getKeyFromFile(sourceName, testName, children[i].split("\\.")[0], true);
            if (key != null) {
                keys.add(key);
            }
        }
        return keys;
    }

    /**
     * deletes a file on disk path from testcases dir is required
     * @param fileName
     */
    public void deleteFile(String fileName) {

        if (log.isTraceEnabled()) {
            log.trace("removing:" + TESTCASES_DIR + File.separator + fileName);
        }
        // A File object to represent the filename
        File f = new File(TESTCASES_DIR + File.separator + fileName);

        // Make sure the file or directory exists and isn't write protected
        if (!f.exists()) {
            if (log.isTraceEnabled()) {
                log.trace("Keys was read from temp dir, no need to delete file");

            }
            return;
        }
        if (!f.canWrite()) {
            throw new IllegalArgumentException("Delete: write protected: " + fileName);        // If it is a directory, make sure it is empty
        }
        if (f.isDirectory()) {
            String[] files = f.list();
            if (files.length > 0) {
                throw new IllegalArgumentException(
                        "Delete: directory not empty: " + fileName);
            }
        }

        // Attempt to delete it
        boolean success = f.delete();

        if (!success) {
            throw new IllegalArgumentException("Delete: deletion failed");
        }
    }

    /**
     * removes all the files for a test dir
     * @param sourceName
     * @param testName
     */
    public void removeAllTestFiles(String sourceName, String testName) {
        String dirStr = TESTCASES_DIR + File.separator + sourceName + File.separator + testName + File.separator;
        File dir = new File(dirStr);


        if (!dir.exists()) {
            return;
        }
        String[] info = dir.list();
        for (int i = 0; i < info.length; i++) {
            File n = new File(dirStr + File.separator + info[i]);
            if (!n.isFile()) { // skip ., .., other directories, etc.
                continue;
            }
            if (info[i].endsWith("_added.json")) {
                n.delete();
            }
            if (info[i].endsWith("_removed.json")) {
                n.delete();
            }
            if (info[i].endsWith("_updated.json")) {
                n.delete();
            }
        }
        dirStr = TESTCASES_DIR + File.separator + sourceName + File.separator + testName + File.separator + "temp" + File.separator;
        dir = new File(dirStr);


        if (!dir.exists()) {
            return;
        }
        info = dir.list();
        for (int i = 0; i < info.length; i++) {
            File n = new File(dirStr + File.separator + info[i]);
            if (!n.isFile()) { // skip ., .., other directories, etc.
                continue;
            }
            if (info[i].endsWith("_added.json")) {
                n.delete();
            }
            if (info[i].endsWith("_removed.json")) {
                n.delete();
            }
            if (info[i].endsWith("_updated.json")) {
                n.delete();
            }
        }
    }

    /**
     * moves all the test files to a temp dir
     * this is done because when the endsyc occurs the items previouly detected
     * as changes should not be detected again
     * @param sourceName
     * @param testName
     * @throws com.funambol.json.api.exception.IOOperationException
     */
    public void storeSynchedItems(String sourceName, String testName) throws IOOperationException {
        String dirStr = TESTCASES_DIR + File.separator + sourceName + File.separator + testName + File.separator;
        File dir = new File(dirStr);

        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith("_removed.json") || name.endsWith("_updated.json") || name.endsWith("_added.json");
            }
        };

        String[] children = dir.list(filter);
        for (int i = 0; i < children.length; i++) {
            FileReader in = null;
            FileWriter out = null;
            try {
                File n = new File(dirStr + File.separator + children[i]);
                if (!n.isFile()) {
                    continue;
                }
                in = new FileReader(n);
                File output = new File(dirStr + File.separator + "temp");

                if (!output.exists()) {
                    output.mkdir();
                }
                output = new File(dirStr + File.separator + "temp" + File.separator + children[i]);
                out = new FileWriter(output);

                int c;

                while ((c = in.read()) != -1) {
                    out.write(c);
                }
                in.close();
                out.close();
                n.delete();
            } catch (IOException ex) {
                log.error("Error movin file to temp dir:", ex);
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    log.error(ex);
                }
                try {
                    out.close();
                } catch (IOException ex) {
                    log.error(ex);
                }
            }
        }
    }
}


