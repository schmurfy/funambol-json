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

package com.funambol.json.security;

import com.funambol.framework.core.Authentication;
import com.funambol.framework.core.Cred;
import com.funambol.framework.server.Sync4jUser;
import com.funambol.framework.tools.Base64;
import com.funambol.json.abstractServlet.AbstractHttpTransportTest;
import com.funambol.json.abstractServlet.JsonServlet;

/**
 *
 * @author gibi
 */
public class JsonOfficerTest extends AbstractHttpTransportTest {


    private static JsonServlet jsonServlet = new JsonServlet();


    public JsonOfficerTest() {
        super(jsonServlet);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    /**
     * Test of getDeviceBuilders method, of class JsonOfficer.
     */
    public void test_AuthenticateUser() throws Exception {

        JsonOfficer officer = new JsonOfficer();

        Authentication auth = new Authentication();
        auth.setType(Cred.AUTH_TYPE_BASIC);
        auth.setUsername("guest");
        auth.setPassword("guest");
        auth.setDeviceId("portal-ui");
        Cred credential = new Cred(auth);

        String data    = "guest:guest";
        String datab64 = new String(Base64.encode(data.getBytes()));
        auth.setData(datab64);

        jsonServlet.setDoReturn(jsonServlet.LOGIN);

        Sync4jUser user = officer.authenticateUser(credential);

        assertEquals("guest", user.getUsername());

    }

    /**
     * Test of getDeviceBuilders method, of class JsonOfficer.
     */
    public void test_AuthenticateUser_2() throws Exception {

        setUpTest_AuthenticateUser_2(jsonServlet.LOGIN_2_USER);

        JsonOfficer officer = new JsonOfficer();

        Authentication auth = new Authentication();
        auth.setType(Cred.AUTH_TYPE_BASIC);
        auth.setUsername("guest");
        auth.setPassword("guest");
        auth.setDeviceId("portal-ui");
        Cred credential = new Cred(auth);

        String data    = "guest:guest";
        String datab64 = new String(Base64.encode(data.getBytes()));
        auth.setData(datab64);

        jsonServlet.setDoReturn(jsonServlet.LOGIN_2);

        officer.setCustomerUserIdEnable(true);
        officer.setCustomerUserIdLabel("fakecustid"); // user-111

        Sync4jUser user = officer.authenticateUser(credential);

        assertEquals(jsonServlet.LOGIN_2_USER, user.getUsername());

    }
    
}
