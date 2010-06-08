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
package com.funambol.json.manager;

import java.io.IOException;

import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;
import com.funambol.json.converter.AuthenticatorRequestConverter;
import com.funambol.json.converter.Converter;
import com.funambol.json.converter.AuthenticatorResponseConverter;
import com.funambol.json.dao.AuthenticatorDAO;
import com.funambol.json.dao.AuthenticatorDAOImpl;
import com.funambol.json.domain.JsonAuthRequest;
import com.funambol.json.domain.JsonAuthResponse;
import com.funambol.json.domain.JsonResponse;
import com.funambol.json.exception.DaoException;
import com.funambol.json.security.JsonUser;
import com.funambol.json.util.ExceptionUtil;
import com.funambol.json.util.Utility;

public class JsonAuthenticatorManagerImpl implements JsonAuthenticatorManager {

    protected static final FunambolLogger log = FunambolLoggerFactory.getLogger(Utility.LOG_NAME);

    private ExceptionUtil exceptionUtil = new ExceptionUtil();

    private Converter<JsonAuthRequest> converter;
    private AuthenticatorDAO authenticatorDAO;

    private String customerUserIdLabel = "";

    public void setCustomerUserIdLabel(String customerUserIdLabel) {
        this.customerUserIdLabel = customerUserIdLabel;
    }


    public JsonAuthenticatorManagerImpl() {
        this.authenticatorDAO = new AuthenticatorDAOImpl();
        this.converter = new AuthenticatorRequestConverter();
    }

    public JsonAuthenticatorManagerImpl(AuthenticatorDAO authenticatorDAO, Converter<JsonAuthRequest> converter) {
        this.authenticatorDAO = authenticatorDAO;
        this.converter = converter;
    }

    /* (non-Javadoc)
     * @see com.funambol.json.manager.JsonAuthenticatorManager#login(com.funambol.json.domain.JsonAuthRequest)
     */
    public JsonAuthResponse login(JsonAuthRequest request) throws DaoException {
        try {
            String jsonObject = converter.toJSON(request);

            JsonResponse jsonResponse = authenticatorDAO.login(jsonObject);
            exceptionUtil.throwExceptionOnJsonResponseError(jsonResponse);
            exceptionUtil.throwExceptionOnResponseBodyNull(jsonResponse);

            AuthenticatorResponseConverter converter = new AuthenticatorResponseConverter();
            converter.customerUserIdLabel = this.customerUserIdLabel;
            JsonAuthResponse jsonAuthResponse = converter.fromJSON(jsonResponse.getResponseBody());

            return jsonAuthResponse;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        }
    }

    /* (non-Javadoc)
     * @see com.funambol.json.manager.JsonAuthenticatorManager#logout(com.funambol.json.security.JsonUser)
     */
    public void logout(JsonUser user) throws DaoException {
        try {
            JsonResponse jsonResponse = authenticatorDAO.logout(user);
            exceptionUtil.throwExceptionOnJsonResponseError(jsonResponse);

        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        }
    }


}
