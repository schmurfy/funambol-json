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
import com.funambol.json.domain.JsonAuthResponseModel;
import com.funambol.json.domain.JsonAuthResponse;
import com.funambol.json.domain.JsonItem;

public class AuthenticatorResponseConverter implements Converter<JsonAuthResponse> {

    public String customerUserIdLabel = "";


    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#fromJSON(java.lang.String)
     */
    public JsonAuthResponse fromJSON(String jsonContent) {
        JSONObject jsonRoot = JSONObject.fromObject(jsonContent);
        JSONObject jsonData = jsonRoot.getJSONObject(JsonAuthResponseModel.DATA.getValue());
        
        JsonAuthResponse jsonAuthResponse = new JsonAuthResponse(); 
        jsonAuthResponse.setSessionID(jsonData.getString(JsonAuthResponseModel.SESSIONID.getValue()));

        // this property will be used when the customer needs to
        // use the unique ID instead of the username that comes from the client;
        // this happens when the username from the client is a sort of alias
        String customerUserID = jsonData.optString(customerUserIdLabel);
        if (customerUserID != null){
            jsonAuthResponse.setCustomerUserID(customerUserID);
        }

        return jsonAuthResponse;
    }

    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#toJSON(java.lang.Object)
     */
    public String toJSON(JsonAuthResponse jsonAuthResponse) {
        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        jsonData.element(JsonAuthResponseModel.SESSIONID.getValue(), jsonAuthResponse.getSessionID());
        jsonRoot.element(JsonAuthResponseModel.DATA.getValue(), jsonData);
        
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
