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
package com.funambol.json.dao;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import com.funambol.json.domain.JsonResponse;

public interface JsonDAO {

    /** 
     * Notifies the API that a sync is going to begin.
     * 
     * @param token the user token returned from Json server
     * @param jsonObject the json's content
     * @return a JsonResponse
     * @throws HttpException
     * @throws IOException
     */
    public JsonResponse beginSync(String token, String jsonObject)
    throws HttpException, IOException;

    /** 
     * Notifies the API that a sync has ended.
     *
     * @param token the user token returned from Json server
     * @return a JsonResponse
     * @throws HttpException
     * @throws IOException
     */
    public JsonResponse endSync(String token) throws HttpException, IOException;

    /**
     * Adds a new resource item.
     *
     * @param token the user token returned from Json server
     * @param jsonObject the json content to add
     * @param since consider the changes since this point in time
     * @return a JsonResponse
     * @throws HttpException
     * @throws IOException
     */
    public JsonResponse addItem(String token, String jsonObject, long since)
    throws HttpException, IOException;

    /**
     * Gets a resource item.
     *
     * @param id the item identifier
     * @param token the user token returned from Json server
     * @return a JsonResponse that contains the item
     * @throws HttpException
     * @throws IOException
     */
    public JsonResponse getItem(String token, String id)
    throws HttpException, IOException;

    /**
     * Updates a resource item.
     *
     * @param id the item identifier
     * @param token the user token returned from Json server
     * @param jsonObject the new content item
     * @param since consider the changes since this point in time
     * @return a JsonResponse
     * @throws HttpException
     * @throws IOException
     */
    public JsonResponse updateItem(String token, 
                                   String id,
                                   String jsonObject,
                                   long since)
    throws HttpException, IOException;

    /**
     * Removes a resource item.
     *
     * @param id the item identifier to remove
     * @param token the user token returned from Json server
     * @param since consider the changes since this point in time
     * @return a JsonResponse
     * @throws HttpException
     * @throws IOException
     */
    public JsonResponse removeItem(String token, String id, long since)
    throws HttpException, IOException;

    /**
     * Removes all resource items.
     *
     * @param token the user token returned from Json server
     * @param since consider the changes since this point in time
     * @return a JsonResponse
     * @throws HttpException
     * @throws IOException
     */
    public JsonResponse removeAllItems(String token, long since)
     throws HttpException, IOException;
    
    /**
     * Retrieves all keys related to a given resource type.
     *
     * @param token the user token returned from Json server
     * @return a JsonResponse
     * @throws HttpException
     * @throws IOException
     */
    public JsonResponse getAllItemKeys(String token)
    throws HttpException, IOException;

    /**
     * Retrieves all new keys related to a given resource type, newly created
     * since a specified time.
     *
     * @param token the user token returned from Json server
     * @param since consider the changes since this point in time
     * @param until consider the changes until this point in time
     * @return a JsonResponse that contains the new keys
     * @throws HttpException
     * @throws IOException
     */
    public JsonResponse getNewItemKeys(String token, long since, long until)
    throws HttpException, IOException;

    /**
     * Retrieves all updated keys related to a given resource type, updated
     * since a specified time.
     *
     * @param token the user token returned from Json server
     * @param since consider the changes since this point in time
     * @param until consider the changes until this point in time
     * @return a JsonResponse that contains the updated keys
     * @throws HttpException
     * @throws IOException
     */
    public JsonResponse getUpdatedItemKeys(String token, long since, long until)
    throws HttpException, IOException;

    /**
     * Retrieves all deleted keys related to a given resource type, deleted
     * since a specified time.
     *
     * @param token the user token returned from Json server
     * @param since consider the changes since this point in time
     * @param until consider the changes until this point in time
     * @return a JsonResponse that contains the deleted keys
     * @throws HttpException
     * @throws IOException
     */
    public JsonResponse getDeletedItemKeys(String token, long since, long until)
    throws HttpException, IOException;

    /**
     * Retrieves keys that are twins of the resource identified by a business
     * specific combination of the passed request parameters.
     *
     * @param token the user token returned from Json server
     * @param jsonObject the json content whom find twins
     * @return a JsonResponse that contains the twin's keys
     * @throws HttpException
     * @throws IOException
     */
    public JsonResponse getItemKeysFromTwin(String token, String jsonObject)
    throws HttpException, IOException;

    /**
     * Retrieves the time and timezone id of the UI.
     *
     * @param token the user token returned from Json server
     * @return a JsonResponse that contains the time and tz id information
     * @throws org.apache.commons.httpclient.HttpException
     * @throws java.io.IOException
     */
    public JsonResponse getTimeConfiguration(String token)
    throws HttpException, IOException;

}
