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
package com.funambol.json.server;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Request {

    private String resource = null;
    private String action = null;
    private String type = null;
    private String key = null;
    private String url = null;
    public static final int KEYS = 1;
    public static final int ITEMS = 2;
    public static final int BEGIN = 3;
    public static final int LOGOUT = 4;
    public static final int LOGIN = 5;
    public static final int KEYS_ALL = 6;
    public static final int KEYS_NEW = 7;
    public static final int KEYS_UPDATED = 8;
    public static final int KEYS_DELETED = 9;
    public static final int KEYS_TWINS = 10;
    public static final int AUTH = 11;
    public static final int SYNC = 12;
    public static final int END = 13;
    public static final int TIME = 14;
    public static final int CONFIG = 15;
    public static final int EMPTY = -1;
    private Map<String, Integer> map = null;

    /**
     * GET    /{resource-type}/keys/all
     * GET    /{resource-type}/keys/new
     * GET    /{resource-type}/keys/updated
     * GET    /{resource-type}/keys/deleted
     * GET    /{resource-type}/keys/twins
     * GET    /{resource-type}/items/{resource-key}
     * POST   /{resource-type}/items
     * POST   /{resource-type}/sync/end
     * POST   /{resource-type}/sync/begin
     * POST                   /auth/logout
     * POST                   /auth/login
     * PUT    /{resource-type}/items/{resource-key}
     * DELETE /{resource-type}/items/{resource-key}
     * @param url
     */
    public Request(String url) {

        map = new HashMap<String, Integer>();

        map.put("keys", KEYS);
        map.put("items", ITEMS);
        map.put("begin", BEGIN);
        map.put("logout", LOGOUT);
        map.put("login", LOGIN);
        map.put("all", KEYS_ALL);
        map.put("new", KEYS_NEW);
        map.put("updated", KEYS_UPDATED);
        map.put("deleted", KEYS_DELETED);
        map.put("twins", KEYS_TWINS);
        map.put("auth", AUTH);
        map.put("sync", SYNC);
        map.put("end", END);
        map.put("time", TIME);
        map.put("config", CONFIG);

        this.url=url;
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }

        String fixedUrl=url.replaceAll("//", "/");

        String split[] = fixedUrl.split("/");


        if (split.length > 2) {
            this.resource = split[2];
        }

        if (split.length > 3) {
            this.action = split[3];

        }
        //has 2 meanings
        if (split.length > 4) {
            this.type = split[4];
            this.key = split[4];
        }


    }

    /**
     * @return the resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * when the auth request is performed (the only case where the first
     * item in path is not a source name)
     * @return
     */
    public int getResourceID() {
        if (map.containsKey(this.getResource())) {
            return map.get(this.getResource());
        } else {
            return -1;
        }

    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @return the action
     */
    public int getActionID() {
        return map.get(this.getAction());
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the action
     */
    public int getTypeID() {
        return map.get(this.getType());
    }

    public int getID(String key) {
        return map.get(key);
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
