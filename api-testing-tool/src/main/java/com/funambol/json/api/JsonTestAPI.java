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
package com.funambol.json.api;

import com.funambol.json.api.dao.AlternateJsonApiDao;
import com.funambol.json.api.dao.FunambolJSONApiDAO;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.log4j.Logger;
import com.funambol.json.api.util.Def;
import com.funambol.json.api.util.ConfigLoader;

/**
 * This is the main class
 */
public class JsonTestAPI {
    //logger
    private static Logger log       = Logger.getLogger(Def.LOG_NAME);
    

    public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (log.isTraceEnabled()) {
            log.trace("Initiating tests...");
        }
        
        System.setProperty("file.encoding", "UTF-8");   
        
        //initiates the config loader
        ConfigLoader loader = new ConfigLoader();
        String[] sources = loader.getSources();
        String[] tests;
        FunambolJSONApiDAO dao=null;
        if (loader.getServerProperty(Def.METHOD).equals(Def.METHOD_STANDARD)){
            dao = new FunambolJSONApiDAO(loader);
        }else if(loader.getServerProperty(Def.METHOD).equals(Def.METHOD_ALTERNATE)){
            dao = new AlternateJsonApiDao(loader);
        }else
            log.error("ERROR not method specified, please check configuration file");

        Method method = null;
        Object[] invokeParam = null;


        for (int i = 0; i < sources.length; i++) {
            tests = loader.getTestForSource(sources[i]);
            if (tests != null) {
                for (int j = 0; j < tests.length; j++) {
                    if (log.isInfoEnabled()) {
                        log.info("-------------Executing test \"" + tests[j] + "\" for source \"" + sources[i] + "\"");
                    }
                    // Specifying the source and test beeing executed
                    dao.setNewTestInfo(sources[i], tests[j]);
                    //  loader.getOperationsResources(sources[i], tests[j]);

                    //reading operations file
                    String operationsContent = loader.getOperationsFile(sources[i], tests[j]);

                    //spliting operations file in lines
                    String[] instructions = operationsContent.split("\n");

                    //Iterating operations
                    for (int x = 0; x < instructions.length; x++) {

                        if (log.isTraceEnabled()) {
                            log.trace("instruction: " + instructions[x]);
                        }
                        try {
                            long sleepTime = dao.getTimeDiff();
                            if (log.isTraceEnabled()) {
                                log.trace("Diference between servers " + sleepTime + "  seconds");
                            }
                            sleepTime = sleepTime + 1000;
                            if (log.isTraceEnabled()) {
                                log.trace("Sleeping " + sleepTime + "  seconds");
                            }
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException ex) {
                            log.info(ex);
                        }

                        try {
                            // if the instruction contains a ":" then it has input parameters
                            if (instructions[x].contains(":")) {
                                if (log.isTraceEnabled()) {
                                    log.trace("command has input parameters");
                                }
                                String[] methodParts = instructions[x].split(":");
                                if (log.isTraceEnabled()) {
                                    log.trace(instructions[x] + " has " + instructions[x].split(":").length + " parameters");
                                }

                                switch (methodParts.length) {
                                    case 2:   // the instruction has just 1 parameter
                                        method = dao.getClass().getMethod(methodParts[0], String.class);
                                        invokeParam = new Object[1];
                                        invokeParam[0] = methodParts[1];
                                        method.invoke(dao, invokeParam);
                                        break;

                                    case 3:// the instruction has 2 parameters
                                        method = dao.getClass().getMethod(methodParts[0], String.class, String.class);
                                        invokeParam = new Object[2];
                                        invokeParam[0] = methodParts[1];
                                        invokeParam[1] = methodParts[2];
                                        method.invoke(dao, invokeParam);
                                        break;
                                    default:
                                        if (log.isTraceEnabled()) {
                                            log.trace("error on operations file");
                                        }
                                }

                            } else {//the instruction has no parameters
                                method = dao.getClass().getMethod(instructions[x]);
                                invokeParam = new Object[0];
                                method.invoke(dao, invokeParam);
                            }

                        } catch (NoSuchMethodException ex) {

                            log.error("-------------------------------- ERROR ");
                            log.error("Tource   :" + sources[i]);
                            log.error("Test     :" + tests[i]);
                            log.error("Method   :" + instructions[x]);
                            log.error("This method does not exist! Please fix the test");
                            log.error("-------------------------------- ERROR ");
                        } catch (Exception ex) {
                            log.error("-------------------------------- ERROR ");
                            if (ex.getCause() != null) {
                                log.error("message:" + ex.getCause().getMessage());
                                if (log.isTraceEnabled()) {
                                    log.trace(ex);
                                }
                            } else {
                                if (log.isTraceEnabled()) {
                                    log.trace(ex);
                                }
                            }
                            log.error("-------------------------------- ERROR ");
                        }
                    }
                }
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Source \"" + sources[i] + "\" has no enabled tests");
                }
            }
        }
    }
}
