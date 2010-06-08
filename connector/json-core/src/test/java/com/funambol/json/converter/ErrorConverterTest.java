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

import com.funambol.json.converter.ErrorConverter;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import net.sf.json.test.JSONAssert;
import com.funambol.json.domain.JsonError;

public class ErrorConverterTest extends TestCase {

    public ErrorConverterTest(String testName) {
        super(testName);
    }

    public void testErrorConversion() {
        
        JsonError jsonError = new JsonError();
        jsonError.setCode("item-1004");
        jsonError.setMessage("runtime exception");

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");
        jsonError.setParameters(parameters);

        ErrorConverter converter = new ErrorConverter();
        String jsonResult = converter.toJSON(jsonError);
        
        JsonError jsonError2 = converter.fromJSON(jsonResult);
        String jsonResult2 = converter.toJSON(jsonError2);
        
        JSONAssert.assertEquals(jsonResult2, jsonResult);
    }

    /**
     * 
     */
    public void test_toJSON_without_parameters() {
        
        JsonError jsonError = new JsonError();
        
        jsonError.setCode("item-1004");
        jsonError.setMessage("runtime exception");


        ErrorConverter converter = new ErrorConverter();

        String jsonResult = converter.toJSON(jsonError);

        //System.out.println("......" + jsonResult);
        //JsonError jsonError2 = converter.fromJSON(jsonResult);
        //String jsonResult2 = converter.toJSON(jsonError2);
        //JSONAssert.assertEquals(jsonResult2, jsonResult);
    }

    
    
//-----> statusCode: 406 responseBody: {
//    "error": {
//        "code": "ERR_UNKNOWN_USER",
//        "message": "Unknown user"
//    }    
}
