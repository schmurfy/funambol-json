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
package com.funambol.json.admin;

import com.funambol.framework.tools.beans.BeanException;
import com.funambol.framework.tools.beans.BeanInitializationException;
import com.funambol.framework.tools.beans.BeanInstantiationException;
import com.funambol.framework.tools.beans.BeanNotFoundException;
import com.funambol.json.exception.JsonConfigException;
import com.funambol.server.config.Configuration;

/**
 * This class contain all Json connection parameters
 *
 */
public class JsonConnectorConfig {

    public final static String beanName = "jsonconnector/jsonconnector/jsonconnector.xml";
    /** 
     * back end server         
     */
    private String jsonServerUrl;

    public String getJsonServerUrl() {
        return jsonServerUrl;
    }

    public void setJsonServerUrl(String jsonServerUrl) {
        this.jsonServerUrl = jsonServerUrl;
    }
    
    private boolean stopSyncOnFatalError;

    public boolean getStopSyncOnFatalError() {
        return stopSyncOnFatalError;
    }

    public void setStopSyncOnFatalError(boolean stopSyncOnFatalError) {
        this.stopSyncOnFatalError = stopSyncOnFatalError;
    }

    //-------------------------------------------------------------- Constructor
    /**
     * 
     */
    public JsonConnectorConfig() {
    }

    //----------------------------------------------------------- Public Methods
    /**
     * 
     * @return
     * @throws com.funambol.json.exception.JsonConfigException
     */
    public static JsonConnectorConfig getConfigInstance() throws JsonConfigException {

        JsonConnectorConfig jsonConf;
        try {
            jsonConf = (JsonConnectorConfig) Configuration.getConfiguration().getBeanInstanceByName(beanName);
        } catch (BeanInstantiationException bie) {
            throw new JsonConfigException("Error instantiating bean '" + beanName + "'", bie);
        } catch (BeanInitializationException bie) {
            throw new JsonConfigException("Error initializing bean '" + beanName + "'", bie);
        } catch (BeanNotFoundException bnfe) {
            throw new JsonConfigException("Error looking for bean '" + beanName + "'", bnfe);
        } catch (BeanException be) {
            throw new JsonConfigException("Error for bean '" + beanName + "'", be);
        } catch (Exception e) {
            //TODO This is needed only for junit tests. Remove when use integration
            //tests with ds-server
            jsonConf = new JsonConnectorConfig();
            jsonConf.setJsonServerUrl("http://localhost:" +
                    com.funambol.json.util.Utility.PORT_DEFAULT + "/syncapi");

        //throw new JsonConfigException("Generic error handling bean '"+beanName+"'", e);
        }

        return jsonConf;

    }
}
