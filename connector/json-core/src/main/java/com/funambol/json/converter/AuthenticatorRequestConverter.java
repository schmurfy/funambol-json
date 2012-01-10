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
package com.funambol.json.converter;

import net.sf.json.JSONObject;

import com.funambol.json.domain.JsonAuthRequest;
import com.funambol.json.domain.JsonAuthRequestModel;
import com.funambol.json.domain.JsonItem;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.server.Capabilities;
import com.funambol.framework.core.DevInf;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;
import com.funambol.json.util.Utility;

public class AuthenticatorRequestConverter implements Converter<JsonAuthRequest> {

    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#fromJSON(java.lang.String)
     */
    public JsonAuthRequest fromJSON(String jsonContent) {
        JSONObject jsonRoot = JSONObject.fromObject(jsonContent);
        JSONObject jsonData = jsonRoot.getJSONObject(JsonAuthRequestModel.DATA.getValue());
        JSONObject jsonCredentials = jsonData.getJSONObject(JsonAuthRequestModel.CREDENTIAL.getValue());
        JsonAuthRequest authRequest = new JsonAuthRequest();
        authRequest.setPass(jsonCredentials.getString(JsonAuthRequestModel.PASS.getValue()));
        authRequest.setUser(jsonCredentials.getString(JsonAuthRequestModel.USER.getValue()));
        return authRequest;
    }

    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#toJSON(java.lang.Object)
     */
    public String toJSON(JsonAuthRequest jsonAuthRequest) {
        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonCredetials = new JSONObject();
        jsonCredetials.element(JsonAuthRequestModel.USER.getValue(), jsonAuthRequest.getUser());
        jsonCredetials.element(JsonAuthRequestModel.PASS.getValue(), jsonAuthRequest.getPass());
        jsonData.element(JsonAuthRequestModel.CREDENTIAL.getValue(), jsonCredetials);
        
        
        Sync4jDevice device = jsonAuthRequest.getDevice();
        
        if( device != null ){
          JSONObject jsonDevice = new JSONObject();
          jsonDevice.element("id", device.getDeviceId());
          
          Capabilities caps = device.getCapabilities();
          if( caps != null ){
            DevInf infos = caps.getDevInf();

            jsonDevice.element("type", infos.getMod());
            jsonDevice.element("swv", infos.getSwV());            
          }
          
          jsonData.element("device", jsonDevice);
        }
        jsonRoot.element("data", jsonData);
        
        return jsonRoot.toString();
    }
    
    /**
     * required by the interface but never used
     * @param item
     * @return
     * @throws java.lang.Exception
     */
    public String toRFC(JsonItem<String> item) {
        return null;
    }

    /**
     * required by the interface but never used
     * @param vcardItem
     * @return
     */
    public JsonItem<String> fromRFC(String jsonRFC) {
        return null;
    }
    
    /**
     * do nothing
     * @param serverTimeZoneID
     */
    public void setServerTimeZoneID(String serverTimeZoneID) {
    }    

}
