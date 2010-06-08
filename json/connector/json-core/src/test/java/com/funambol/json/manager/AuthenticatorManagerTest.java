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

import com.funambol.json.manager.JsonAuthenticatorManagerImpl;
import net.sf.json.JSONException;
import com.funambol.json.abstractServlet.AbstractHttpTransportTest;
import com.funambol.json.abstractServlet.JsonServlet;
import com.funambol.json.converter.AuthenticatorRequestConverter;
import com.funambol.json.converter.Converter;
import com.funambol.json.dao.AuthenticatorDAO;
import com.funambol.json.dao.AuthenticatorDAOImpl;
import com.funambol.json.domain.JsonAuthRequest;
import com.funambol.json.domain.JsonAuthResponse;
import com.funambol.json.exception.HttpException;

public class AuthenticatorManagerTest extends AbstractHttpTransportTest {

	private JsonAuthRequest authRequest = new JsonAuthRequest();
	private static JsonServlet jsonServlet = new JsonServlet();
	private AuthenticatorDAO dao;
	private Converter<JsonAuthRequest> converter;
	private JsonAuthenticatorManagerImpl manager;
	
	public AuthenticatorManagerTest() {
		super(jsonServlet);
		authRequest.setUser("user");
		authRequest.setPass("user");
		
		dao = new AuthenticatorDAOImpl("http://localhost:" +
                        com.funambol.json.util.Utility.PORT_DEFAULT + "/syncapi");
		converter = new AuthenticatorRequestConverter();
		manager = new JsonAuthenticatorManagerImpl(dao, converter);
	}
	
	public void test_AuthenticatorManagerLogin() throws JSONException {
		jsonServlet.setDoReturn(JsonServlet.LOGIN);
		
		try {
			JsonAuthResponse login = manager.login(authRequest);
			assertTrue(login.getSessionID() != null && !login.getSessionID().equals(""));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public void test_AuthenticatorDaoError() throws JSONException {
		jsonServlet.setDoReturn(JsonServlet.INTERNAL_SERVER_ERROR);
		try {
			JsonAuthResponse login = manager.login(authRequest);
		} catch (HttpException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail();
		}
	}

	public void test_AuthenticatorDaoBadRequest() throws JSONException {
		jsonServlet.setDoReturn(JsonServlet.BAD_REQUEST);
		try {
			JsonAuthResponse login = manager.login(authRequest);
		} catch (HttpException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail();
		}
	}

	public void test_AuthenticatorDaoUnauthorized() throws JSONException {
		jsonServlet.setDoReturn(JsonServlet.UNAUTHORIZED);
		try {
			JsonAuthResponse login = manager.login(authRequest);
		} catch (HttpException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail();
		}
	}

}
