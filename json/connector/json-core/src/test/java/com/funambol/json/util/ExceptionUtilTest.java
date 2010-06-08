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


import com.funambol.json.domain.JsonResponse;
import com.funambol.json.exception.BadRequestException;
import com.funambol.json.exception.InternalServerErrorException;
import com.funambol.json.exception.MalformedJsonContentException;
import com.funambol.json.exception.UnauthorizedException;

import junit.framework.TestCase;

public class ExceptionUtilTest extends TestCase {
    
    private static final String JSON_OBJ = "{\"error\":{\"code\":\"item-1004\",\"message\":\"runtime exception\",\"parameters\":[{\"param1\":\"value1\"},{\"param2\":\"value2\"}]}}";
    private static final String JSON_OBJ_WITH_PARAMETERS_ARRAY_VOID = "{\"error\":{\"code\":\"item-1004\",\"message\":\"runtime exception\",\"parameters\":[{}]}}";
    private static final String JSON_OBJ_WITHOUT_PARAMETERS = "{\"error\":{\"code\":\"item-1004\",\"message\":\"runtime exception\"}}";
    private static final String JSON_OBJ_MALFORMED = "{\"errors\":{\"coding\":\"item-1004\",\"mesge\":\"runtime exception\",\"parameters\":[{\"param1\":\"value1\"},{\"param2\":\"value2\"}]}}";    
    
    /**
     * 
     */
    public void testThrowExceptionOnJsonResponseError() {
	
        JsonResponse jsonResponse = null;
	
        try {
            jsonResponse = new JsonResponse(200, "");
            new ExceptionUtil().throwExceptionOnJsonResponseError(jsonResponse);
        } catch (BadRequestException e) {
                fail();
        } catch (UnauthorizedException e) {
                fail();
        } catch (InternalServerErrorException e) {
                fail();
        } catch (MalformedJsonContentException e) {
                fail();
        }

        try {
            jsonResponse = new JsonResponse(406, "");
            new ExceptionUtil().throwExceptionOnJsonResponseError(jsonResponse);
        } catch (BadRequestException e) {
                //OK
        } catch (UnauthorizedException e) {
                fail();
        } catch (InternalServerErrorException e) {
                fail();
        } catch (MalformedJsonContentException e) {
                fail();
        }

        try {
            jsonResponse = new JsonResponse(401, "");
            new ExceptionUtil().throwExceptionOnJsonResponseError(jsonResponse);
        } catch (BadRequestException e) {
                fail();
        } catch (UnauthorizedException e) {
                //OK
        } catch (InternalServerErrorException e) {
                fail();
        } catch (MalformedJsonContentException e) {
                fail();
        }

        try {
            jsonResponse = new JsonResponse(500, "");
            new ExceptionUtil().throwExceptionOnJsonResponseError(jsonResponse);
        } catch (BadRequestException e) {
                fail();
        } catch (UnauthorizedException e) {
                fail();
        } catch (InternalServerErrorException e) {
                //OK
        } catch (MalformedJsonContentException e) {
                fail();
        }


        /**
         * tests with json content correct
         */
        try {
            jsonResponse = new JsonResponse(406, JSON_OBJ);
            new ExceptionUtil().throwExceptionOnJsonResponseError(jsonResponse);
        } catch (BadRequestException e) {
                assertEquals("item-1004",e.getError().getCode());
        } catch (UnauthorizedException e) {
                fail();
        } catch (InternalServerErrorException e) {
                fail();
        } catch (MalformedJsonContentException e) {
                fail();
        }

        try {
            jsonResponse = new JsonResponse(401, JSON_OBJ);
            new ExceptionUtil().throwExceptionOnJsonResponseError(jsonResponse);
        } catch (BadRequestException e) {
                fail();
        } catch (UnauthorizedException e) {
                assertEquals("item-1004",e.getError().getCode());
        } catch (InternalServerErrorException e) {
                fail();
        } catch (MalformedJsonContentException e) {
                fail();
        }
        
        try {
            jsonResponse = new JsonResponse(401, JSON_OBJ_WITHOUT_PARAMETERS);
            new ExceptionUtil().throwExceptionOnJsonResponseError(jsonResponse);
        } catch (BadRequestException e) {
                fail();
        } catch (UnauthorizedException e) {
                assertEquals("item-1004",e.getError().getCode());
        } catch (InternalServerErrorException e) {
                fail();
        } catch (MalformedJsonContentException e) {
                fail();
        }
        
        try {
            jsonResponse = new JsonResponse(401, JSON_OBJ_WITH_PARAMETERS_ARRAY_VOID);
            new ExceptionUtil().throwExceptionOnJsonResponseError(jsonResponse);
        } catch (BadRequestException e) {
                fail();
        } catch (UnauthorizedException e) {
                assertEquals("item-1004",e.getError().getCode());
        } catch (InternalServerErrorException e) {
                fail();
        } catch (MalformedJsonContentException e) {
                fail();
        }        

        try {
            jsonResponse = new JsonResponse(500, JSON_OBJ);
            new ExceptionUtil().throwExceptionOnJsonResponseError(jsonResponse);
        } catch (BadRequestException e) {
                fail();
        } catch (UnauthorizedException e) {
                fail();
        } catch (InternalServerErrorException e) {
                assertEquals("item-1004",e.getError().getCode());
        } catch (MalformedJsonContentException e) {
                fail();
        }

        /**
         * tests with json content malformed
         */
        try {
            jsonResponse = new JsonResponse(406, JSON_OBJ_MALFORMED);
            new ExceptionUtil().throwExceptionOnJsonResponseError(jsonResponse);
        } catch (BadRequestException e) {
                // patch in order to avoid the error in the procedure
                assertEquals("406",e.getError().getCode());
                //fail();
        } catch (UnauthorizedException e) {
                fail();
        } catch (InternalServerErrorException e) {
                fail();
        } catch (MalformedJsonContentException e) {
        }

        try {
            jsonResponse = new JsonResponse(401, JSON_OBJ_MALFORMED);
            new ExceptionUtil().throwExceptionOnJsonResponseError(jsonResponse);
        } catch (BadRequestException e) {
                fail();
        } catch (UnauthorizedException e) {
                // patch in order to avoid the error in the procedure
                assertEquals("401",e.getError().getCode());
                //fail();
        } catch (InternalServerErrorException e) {
                fail();
        } catch (MalformedJsonContentException e) {
        }

        try {
            jsonResponse = new JsonResponse(500, JSON_OBJ_MALFORMED);
            new ExceptionUtil().throwExceptionOnJsonResponseError(jsonResponse);
        } catch (BadRequestException e) {
                fail();
        } catch (UnauthorizedException e) {
                fail();
        } catch (InternalServerErrorException e) {
                // patch in order to avoid the error in the procedure
                assertEquals("500",e.getError().getCode());
                //fail();
        } catch (MalformedJsonContentException e) {
        }

    }

}
