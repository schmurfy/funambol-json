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
import com.funambol.json.converter.BeginConverter;
import com.funambol.json.converter.Converter;
import com.funambol.json.converter.KeyConverter;
import com.funambol.json.converter.KeysConverter;

import com.funambol.json.converter.TimeConfigurationConverter;
import com.funambol.json.dao.JsonDAO;
import com.funambol.json.domain.JsonError;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.domain.JsonKeys;
import com.funambol.json.domain.JsonResponse;
import com.funambol.json.domain.JsonTimeConfiguration;
import com.funambol.json.exception.DaoException;
import com.funambol.json.exception.HttpException;
import com.funambol.json.exception.MalformedJsonContentException;
import com.funambol.json.util.ExceptionUtil;
import com.funambol.json.util.Utility;
import java.util.Map;
import net.sf.json.JSONObject;

public abstract class JsonManagerImpl<T> implements JsonManager<T> {

    protected static final FunambolLogger log = FunambolLoggerFactory.getLogger(Utility.LOG_NAME);
    protected JsonDAO jsonDAO;
    protected Converter<JsonItem<T>> converter;
    protected KeysConverter keysConverter;
    protected ExceptionUtil exceptionUtil;

    //------------------------------------------------------------- Constructors

    private JsonManagerImpl() {
        this.exceptionUtil = new ExceptionUtil();
        this.keysConverter = new KeysConverter();
    }

    public JsonManagerImpl(JsonDAO dao, Converter<JsonItem<T>> converter) {
        this();
        this.jsonDAO = dao;
        this.converter = converter;
    }

    
    //----------------------------------------------------------- public Methods
    
    /**
     * 
     * @param sessionID
     * @param since
     * @param syncMode
     * @param tzid
     * @throws com.funambol.json.exception.DaoException
     */
    public final void beginSync(String sessionID, long since, String syncMode, String tzid) throws DaoException {
        if (log.isTraceEnabled()) {
            log.trace("JsonManagerImpl: start beginSync ");
        }
        try {

            converter.setServerTimeZoneID(tzid); 

            Converter<String> beginConverter = new BeginConverter();

            JSONObject jsonData = new JSONObject();
            jsonData.element("since", since);
            jsonData.element("synctype", syncMode);

            String jsonContent = beginConverter.toJSON(jsonData.toString());

            JsonResponse response = jsonDAO.beginSync(sessionID, jsonContent);

            // the begin can return an empty string
            exceptionUtil.throwExceptionOnJsonResponseError(response);
            
        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        }
    }

    /**
     * 
     * @param sessionID
     * @throws com.funambol.json.exception.DaoException
     */
    public final void endSync(String sessionID) throws DaoException {
        if (log.isTraceEnabled()) {
            log.trace("JsonManagerImpl: start endSync ");
        }
        try {
            JsonResponse response = jsonDAO.endSync(sessionID);
        if (log.isTraceEnabled()) {
            log.trace("JsonManagerImpl: endSync response " + response);
        }
        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        }
    }

    /**
     * 
     * @param sessionID
     * @param item
     * @param since
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final String addRFCItem(String sessionID, JsonItem<String> item, long since) throws DaoException, MalformedJsonContentException {
        try {

            Converter<String> keyConverter = new KeyConverter();
            
            String jsonContent = converter.toRFC(item);
            JsonResponse response = jsonDAO.addItem(sessionID, jsonContent, since);

            exceptionUtil.throwExceptionOnJsonResponseError(response);
            exceptionUtil.throwExceptionOnResponseBodyNull(response);

            String id = keyConverter.fromJSON(response.getResponseBody());
            return id;

        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }

    }

    /**
     * 
     * @param sessionID
     * @param item
     * @param since
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final String addExtendedItem(String sessionID, JsonItem<T> item, long since) throws DaoException, MalformedJsonContentException {
        try {

            Converter<String> keyConverter = new KeyConverter();
            
            String jsonContent = converter.toJSON(item);
            JsonResponse response = jsonDAO.addItem(sessionID, jsonContent, since);
            
            if (response.getResponseStatusCode() == 406) {
                // some API can have a check when it adds the item
                JsonError jsonError = exceptionUtil.getError(response, 406);
                String code = jsonError.getCode();
                if ("ERR_ALREADY_EXISTS".equals(code)){                    
                    Map<String, String> parameters = jsonError.getParameters();
                    String itemID = parameters.get("key");
                    return itemID;
                }
            } else {
                // usual check
                exceptionUtil.throwExceptionOnJsonResponseError(response);
                exceptionUtil.throwExceptionOnResponseBodyNull(response);
            }
            
            String id = keyConverter.fromJSON(response.getResponseBody());
            return id;

        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }

    }

    /**
     * 
     * @param sessionID
     * @param id
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final JsonItem<T> getExtendedItem(String sessionID, String id) throws DaoException, MalformedJsonContentException {
        try {
            JsonResponse response = jsonDAO.getItem(sessionID, id);
            exceptionUtil.throwExceptionOnJsonResponseError(response);
            exceptionUtil.throwExceptionOnResponseBodyNull(response);

            JsonItem<T> itemResponse = converter.fromJSON(response.getResponseBody());

            //the item returned might not include the id
            itemResponse.setKey(id);

            return itemResponse;

        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }


    /**
     * 
     * @param sessionID
     * @param id
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final JsonItem<String> getRFCItem(String sessionID, String id) throws DaoException, MalformedJsonContentException {
        try {
            JsonResponse response = jsonDAO.getItem(sessionID, id);
            exceptionUtil.throwExceptionOnJsonResponseError(response);
            exceptionUtil.throwExceptionOnResponseBodyNull(response);
            
            JsonItem<String> itemResponse = converter.fromRFC(response.getResponseBody());

            //the item returned might not include the id
            itemResponse.setKey(id);
            
            return itemResponse;

        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }

    /**
     * 
     * @param sessionID
     * @param item
     * @param since
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final JsonItem<T> updateExtendedItem(String sessionID, JsonItem<T> item, long since) throws DaoException, MalformedJsonContentException {
        try {
            String jsonContent = converter.toJSON(item);
            JsonResponse response = jsonDAO.updateItem(sessionID, item.getKey(), jsonContent, since);
            exceptionUtil.throwExceptionOnJsonResponseError(response);
            return item;
        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }

    /**
     * 
     * @param sessionID
     * @param item
     * @param since
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final JsonItem<String> updateRFCItem(String sessionID, JsonItem<String> item, long since) throws DaoException, MalformedJsonContentException {
        try {
            String jsonContent = converter.toRFC(item);
            JsonResponse response = jsonDAO.updateItem(sessionID, item.getKey(), jsonContent, since);
            exceptionUtil.throwExceptionOnJsonResponseError(response);
            return item;
        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }

    /**
     * 
     * @param sessionID
     * @param id
     * @param since
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final void removeItem(String sessionID, String id, long since) throws DaoException, MalformedJsonContentException {
        try {
            JsonResponse response = jsonDAO.removeItem(sessionID, id, since);

            exceptionUtil.throwExceptionOnJsonResponseError(response);

        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage());
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }

    /**
     * 
     * @param sessionID
     * @param since
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final void removeAllItems(String sessionID, long since) throws DaoException, MalformedJsonContentException {
        try {
            JsonResponse response = jsonDAO.removeAllItems(sessionID, since);

            exceptionUtil.throwExceptionOnJsonResponseError(response);

        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage());
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }

    /**
     * 
     * @param sessionID
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final JsonKeys getAllItemsKey(String sessionID) throws DaoException, MalformedJsonContentException {
        try {
            JsonResponse response = jsonDAO.getAllItemKeys(sessionID);

            exceptionUtil.throwExceptionOnJsonResponseError(response);
            exceptionUtil.throwExceptionOnResponseBodyNull(response);

            JsonKeys keys = keysConverter.fromJSON(response.getResponseBody());

            return keys;

        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }

    /**
     * 
     * @param sessionID
     * @param since
     * @param until
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final JsonKeys getNewItemKeys(String sessionID, long since, long until) throws DaoException, MalformedJsonContentException {
        try {
            JsonResponse response = jsonDAO.getNewItemKeys(sessionID, since, until);

            exceptionUtil.throwExceptionOnJsonResponseError(response);
            exceptionUtil.throwExceptionOnResponseBodyNull(response);

            JsonKeys keys = keysConverter.fromJSON(response.getResponseBody());

            return keys;

        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }


    /**
     * 
     * @param sessionID
     * @param since
     * @param until
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final JsonKeys getUpdatedItemKeys(String sessionID, long since, long until) throws DaoException, MalformedJsonContentException {
        try {
            JsonResponse response = jsonDAO.getUpdatedItemKeys(sessionID, since, until);

            exceptionUtil.throwExceptionOnJsonResponseError(response);
            exceptionUtil.throwExceptionOnResponseBodyNull(response);

            JsonKeys keys = keysConverter.fromJSON(response.getResponseBody());

            return keys;

        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }

    /**
     * 
     * @param sessionID
     * @param since
     * @param until
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final JsonKeys getDeletedItemKeys(String sessionID, long since, long until) throws DaoException, MalformedJsonContentException {
        try {
            JsonResponse response = jsonDAO.getDeletedItemKeys(sessionID, since, until);

            exceptionUtil.throwExceptionOnJsonResponseError(response);
            exceptionUtil.throwExceptionOnResponseBodyNull(response);

            JsonKeys keys = keysConverter.fromJSON(response.getResponseBody());

            return keys;

        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }

    
    /**
     * 
     * @param sessionID
     * @param item
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final JsonKeys getExtendedItemKeysFromTwin(String sessionID, JsonItem<T> item) 
       throws DaoException, MalformedJsonContentException {
        try {
            
            String jsonContent    = converter.toJSON(item);
            JsonResponse response = jsonDAO.getItemKeysFromTwin(sessionID, jsonContent);

            exceptionUtil.throwExceptionOnJsonResponseError(response);
            exceptionUtil.throwExceptionOnResponseBodyNull(response);

            JsonKeys keys = keysConverter.fromJSON(response.getResponseBody());

            return keys;

        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }
    
    /**
     * 
     * 
     * @param sessionID
     * @param item
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final JsonKeys getRFCItemKeysFromTwin(String sessionID, JsonItem<String> item) 
       throws DaoException, MalformedJsonContentException {
        try {
            
            String jsonContent    = converter.toRFC(item);
            JsonResponse response = jsonDAO.getItemKeysFromTwin(sessionID, jsonContent);

            exceptionUtil.throwExceptionOnJsonResponseError(response);
            exceptionUtil.throwExceptionOnResponseBodyNull(response);

            JsonKeys keys = keysConverter.fromJSON(response.getResponseBody());

            return keys;

        } catch (HttpException re) {
            throw re;
        } catch (org.apache.commons.httpclient.HttpException httpEx) {
            log.error("Failed the connection to the Json backend", httpEx);
            throw new DaoException(httpEx.getMessage(), httpEx);
        } catch (IOException ioEx) {
            log.error("Failed the connection to the Json backend", ioEx);
            throw new DaoException(ioEx.getMessage(), ioEx);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }

    /**
     * 
     * @param token
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public final JsonTimeConfiguration getTimeConfiguration(String token) throws Exception {
        try {

            Converter<JsonTimeConfiguration> configurationConverter = new TimeConfigurationConverter();

            JsonResponse response = jsonDAO.getTimeConfiguration(token);

            exceptionUtil.throwExceptionOnJsonResponseError(response);
            exceptionUtil.throwExceptionOnResponseBodyNull(response);

            JsonTimeConfiguration config = configurationConverter.fromJSON(response.getResponseBody());

            return config;

        } catch (Exception e) {
            log.error("Failed the connection to the backend " + e.getMessage());
            throw e;
        }
       
    }
    
}
