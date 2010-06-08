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
package com.funambol.json.util;

import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.json.converter.ErrorConverter;
import com.funambol.json.domain.JsonError;
import com.funambol.json.domain.JsonErrorModel;
import com.funambol.json.domain.JsonGUID;
import com.funambol.json.domain.JsonResponse;
import com.funambol.json.exception.BadRequestException;
import com.funambol.json.exception.InternalServerErrorException;
import com.funambol.json.exception.MalformedJsonContentException;
import com.funambol.json.exception.UnauthorizedException;
import com.funambol.json.exception.UnknowException;
import net.sf.json.JSONObject;

public class ExceptionUtil {

    /**
     * throws the exceptions on related status code or malformed json content
     * 
     * @param jsonResponse
     * @throws BadRequestException
     * @throws UnauthorizedException
     * @throws InternalServerErrorException
     * @throws MalformedJsonContentException
     */
    public void throwExceptionOnJsonResponseError(JsonResponse jsonResponse) 
       throws BadRequestException, UnauthorizedException, InternalServerErrorException, MalformedJsonContentException {

        if (jsonResponse.getResponseStatusCode() == 200) {

            return;

        } else if (jsonResponse.getResponseStatusCode() == 406) {
            
            JsonError jsonError = getError(jsonResponse, 406);
            throw new BadRequestException(jsonError);

        } else if (jsonResponse.getResponseStatusCode() == 401) {

            JsonError jsonError = getError(jsonResponse, 401);
            throw new UnauthorizedException(jsonError);

        } else if (jsonResponse.getResponseStatusCode() == 500) {

            JsonError jsonError = getError(jsonResponse, 500);
            throw new InternalServerErrorException(jsonError);

        } else {

            throw new UnknowException("Unknow Error code : " + jsonResponse.getResponseStatusCode());

        }

    }

    /**
     * 
     * @param response
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public void throwExceptionOnResponseBodyNull(JsonResponse response) throws MalformedJsonContentException {
        if (response.getResponseBody() == null || response.getResponseBody().trim().equals("")) {
            throw new MalformedJsonContentException("The Json content is null!");
        }
    }
    
    /**
     * 
     * @param syncItemKey
     * @throws com.funambol.framework.engine.source.SyncSourceException
     */
    public void throwExceptionOnInvalidObjectKey(SyncItemKey syncItemKey) throws SyncSourceException {
        if(!(syncItemKey.getKeyValue() instanceof JsonGUID)){
            throw new SyncSourceException("itemKey must contain a JsonGUID");
        }
    }
    
    
    //---------------------------------------------------------- Private Methods
    
    /**
     * 
     * 
     * @param jsonResponse
     * @return
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public JsonError getError(JsonResponse jsonResponse, int code)throws MalformedJsonContentException {
        JsonError jsonError = null;
        ErrorConverter converter = new ErrorConverter();
        try {
            if (jsonResponse.getResponseBody() != null){

                if (jsonResponse.getResponseBody().trim().equals("")){
                    jsonError = new JsonError();
                    jsonError.setCode(String.valueOf(code));
                    jsonError.setMessage("The Response doesn't contain any message");
                } else {
                    
                    // TMP PATCH
                    // sometimes the API returns the 406 code without the error                
                    JSONObject jsonRoot = JSONObject.fromObject(jsonResponse.getResponseBody());

                    if (jsonRoot.containsKey(JsonErrorModel.ERROR.getValue())){
                        jsonError = converter.fromJSON(jsonResponse.getResponseBody());
                    } else {
                        // original
                        //throw new MalformedJsonContentException("The Response doesn't contain an Error");
                        // patch in order to avoid the error in the procedure 
                        jsonError = new JsonError();
                        jsonError.setCode(String.valueOf(code));
                        jsonError.setMessage("The Response doesn't contain an Error");
                    }                
                    
                }
            
            } else {
                throw new MalformedJsonContentException("The Response doesn't contain a neither Json Object nor Error");
            }
        } catch (RuntimeException e) {
            throw new MalformedJsonContentException(e.getMessage());
        }
        return jsonError;
    }

}
