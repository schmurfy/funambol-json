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

import com.funambol.json.domain.JsonError;
import com.funambol.json.domain.JsonErrorModel;
import com.funambol.json.domain.JsonItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ErrorConverter implements Converter<JsonError> {

    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#toJSON(java.lang.Object)
     */
    public String toJSON(JsonError jsonError) {
        
        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonErrorObject = new JSONObject();
        
        jsonErrorObject.element(JsonErrorModel.CODE.getValue(), jsonError.getCode());
        jsonErrorObject.element(JsonErrorModel.MESSAGE.getValue(), jsonError.getMessage());
        
        List<JSONObject> parameters = new ArrayList<JSONObject>();
        for (Map.Entry<String, String> entry : jsonError.getParameters().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            parameters.add(new JSONObject().element(key, value));
        }
        jsonErrorObject.element(JsonErrorModel.PARAMETERS.getValue(), parameters);
        
        jsonRoot.element(JsonErrorModel.ERROR.getValue(), jsonErrorObject);

        return jsonRoot.toString();
    }

    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#fromJSON(java.lang.String)
     */
    public JsonError fromJSON(String jsonContent) {
        
        JSONObject jsonRoot = JSONObject.fromObject(jsonContent);
        
        JSONObject jsonErrorObject = jsonRoot.getJSONObject(JsonErrorModel.ERROR.getValue());
        
        JsonError jsonError = new JsonError();
        jsonError.setCode(jsonErrorObject.getString(JsonErrorModel.CODE.getValue()));
        jsonError.setMessage(jsonErrorObject.getString(JsonErrorModel.MESSAGE.getValue()));
             
        Map<String, String> parameters = new HashMap<String, String>();
        
        if (jsonErrorObject.containsKey(JsonErrorModel.PARAMETERS.getValue())) {
            
            JSONArray jsonArray = jsonErrorObject.getJSONArray(JsonErrorModel.PARAMETERS.getValue());
       
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject parameter = (JSONObject) jsonArray.get(i);
                for (Iterator iterator = parameter.keys(); iterator.hasNext();) {
                    String key = (String) iterator.next();
                    String value = parameter.getString(key);
                    parameters.put(key, value);
                }
    
            }
            
        }
            
        jsonError.setParameters(parameters);
        
        return jsonError;
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
