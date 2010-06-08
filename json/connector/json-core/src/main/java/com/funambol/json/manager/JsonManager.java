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

import com.funambol.json.domain.JsonItem;
import com.funambol.json.domain.JsonKeys;
import com.funambol.json.domain.JsonTimeConfiguration;
import com.funambol.json.exception.DaoException;
import com.funambol.json.exception.MalformedJsonContentException;
import java.util.TimeZone;

/**
 * This class is the manager layer
 * @param <T>
 */
public interface JsonManager<T> {
    
    /**
     * Notify the REST API that a sync is going to begin
     * 
     * @param sessionID the user sessionID returned from json server
     * @param since indicates the start of the sync
     * @param syncMode indicates the syncmode to the server
     * @param tzid indicates the server timezone
     * @throws com.funambol.json.exception.DaoException
     */
    public void beginSync(String sessionID, long since, String syncMode, String tzid) throws DaoException;

    /** 
     * Notify the REST API that a sync has ended
     * 
     * @param sessionID the user sessionID returned from json server
     * @return
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public void endSync(String sessionID) throws DaoException;
	
    /**
     * Add a new resource item.
     * 
     * @param sessionID the user sessionID returned from json server
     * @param item the domain object
     * @return
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public String addRFCItem(String sessionID, JsonItem<String> item, long since) throws DaoException, MalformedJsonContentException;

    /**
     * Add a new resource item.
     * 
     * @param sessionID the user sessionID returned from json server
     * @param item the domain object
     * @return
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public String addExtendedItem(String sessionID, JsonItem<T> item, long since) throws DaoException, MalformedJsonContentException;
    
    /**
     * Get a resource item.
     * 
     * @param sessionID the user sessionID returned from json server
     * @param id
     * @return the object domain
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public JsonItem<T> getExtendedItem(String sessionID, String id) throws DaoException, MalformedJsonContentException;

    /**Get a resource item.
     * @param sessionID the user sessionID returned from json server
     * @param id
     * @return the object domain
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public JsonItem<String> getRFCItem(String sessionID, String id) throws DaoException, MalformedJsonContentException;
    
    /**
     * Update a resource item.
     * 
     * @param item the object domain
     * @param sessionID the user sessionID returned from json server
     * @return
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public JsonItem<T> updateExtendedItem(String sessionID, JsonItem<T> item, long since) throws DaoException, MalformedJsonContentException;

    /**
     * Update a resource item.
     * 
     * @param item the object domain
     * @param sessionID the user sessionID returned from json server
     * @return
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public JsonItem<String> updateRFCItem(String sessionID, JsonItem<String> item, long since) throws DaoException, MalformedJsonContentException;
    
    /**
     * merge two json extended resource items.
     * 
     * @param item the object domain
     * @param sessionID the user sessionID returned from json server
     * @return
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public boolean mergeExtendedItem(String sessionID, JsonItem<T> serverItem, JsonItem<T> clientItem, long since ) throws DaoException, MalformedJsonContentException;


     /**
     * merge two RFC resource items.
     *
     * @param item the object domain
     * @param sessionID the user sessionID returned from json server
     * @return
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    //public boolean mergeRFCItem(String sessionID, JsonItem<T> serverItem, JsonItem<T> clientItem, long since,String rfcType,TimeZone timezone, String charset ) throws DaoException, MalformedJsonContentException;
    public boolean mergeRFCItem(String sessionID,
                                JsonItem<T> serverItem,
                                JsonItem<T> clientItem,
                                long since,
                                boolean vcardIcalBackend,
                                boolean vcalFormat,
                                String rfcType,
                                TimeZone timezone,
                                String charset ) throws DaoException, MalformedJsonContentException;


    /**
     * Remove a resource item.
     * 
     * @param id
     * @param sessionID the user sessionID returned from json server
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public void removeItem(String sessionID, String id, long since) throws DaoException, MalformedJsonContentException;

    /**
     * Remove all resource items.
     * 
     * @param id
     * @param sessionID the user sessionID returned from json server
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public void removeAllItems(String sessionID, long since) throws DaoException, MalformedJsonContentException;
    
    /**
     * Retrieve all keys related to a given resource type.
     * 
     * @param sessionID the user sessionID returned from json server
     * @param id
     * @return a JsonKeys object domain
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public JsonKeys getAllItemsKey(String sessionID) throws DaoException, MalformedJsonContentException;

    /** Retrieve all new keys related to a given resource type,
     *  newly created since a specified time
     * @param sessionID the user sessionID returned from json server
     * @param since
     * @param until
     * @return a JsonKeys object domain
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public JsonKeys getNewItemKeys(String sessionID, long since, long until) throws DaoException, MalformedJsonContentException;

    /**
     * 
     * @param since
     * @param until
     * @return
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public JsonKeys getUpdatedItemKeys(String sessionID, long since, long until) throws DaoException, MalformedJsonContentException;

    /**
     * Retrieve all deleted keys related to a given resource type,
     * deleted since a specified time
     * 
     * @param sessionID the user sessionID returned from json server
     * @param since
     * @param until
     * @return a JsonKeys object domain
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public JsonKeys getDeletedItemKeys(String sessionID, long since, long until) throws DaoException, MalformedJsonContentException;
    
    /**
     * Retrieve keys that are twins of the resource identified by a business specific
     *  combination of the passed request parameters.
     * 
     * @param sessionID the user sessionID returned from json server
     * @param item the object domain
     * @return a JsonKeys object domain
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public JsonKeys getExtendedItemKeysFromTwin(String sessionID, JsonItem<T> item) throws DaoException, MalformedJsonContentException;

    /**
     * Retrieve keys that are twins of the resource identified by a business specific
     *  combination of the passed request parameters.
     * 
     * @param sessionID the user sessionID returned from json server
     * @param item the object domain
     * @return a JsonKeys object domain
     * @throws DaoException
     * @throws MalformedJsonContentException
     */
    public JsonKeys getRFCItemKeysFromTwin(String sessionID, JsonItem<String> item) throws DaoException, MalformedJsonContentException;
    
    /**
     * 
     * @param token
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public JsonTimeConfiguration getTimeConfiguration(String token) throws Exception ;


        /**
     *
     * returns the EntityDAO specific for every subclass
     * @return
     */
    public boolean isTwinSearchAppliableOn(JsonItem<T> item);
    
}