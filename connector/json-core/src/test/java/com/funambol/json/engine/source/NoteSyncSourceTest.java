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
package com.funambol.json.engine.source;

import com.funambol.framework.core.CTInfo;
import com.funambol.framework.core.DSMem;
import com.funambol.framework.core.DataStore;
import com.funambol.framework.core.DevInf;
import com.funambol.framework.core.SourceRef;
import com.funambol.framework.core.SyncCap;
import com.funambol.framework.core.VerDTD;
import net.sf.json.JSONException;

import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.json.abstractServlet.AbstractHttpTransportTest;
import com.funambol.json.abstractServlet.JsonServlet;


public class NoteSyncSourceTest extends AbstractHttpTransportTest {

    private static JsonServlet jsonServlet = new JsonServlet();

    public NoteSyncSourceTest() {
        super(jsonServlet);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    /**
     * Create a fake device and a sync context, then tests the smart sync
     * source by providing a preferred note format into the device
     * capabilities.
     *
     * @throws net.sf.json.JSONException
     */
    public void test_findRXContentType() {

        NoteSyncSource source = new NoteSyncSource();

        DevInf deviceInfo = new DevInf(
                new VerDTD("1.2"),
                "Funambol", "Funambol Outlook Sync Client", "",
                "", "", "",
                "", "",
                true, true, true);
        Sync4jPrincipal principal = Sync4jPrincipal.createPrincipal(
                "fakeuser", "fakeuser_fakedevice");
        principal.getDevice().getCapabilities().setDevInf(deviceInfo);

        SyncContext context = new SyncContext(
                principal, 200, null, "localhost", 2);

        CTInfo[] rxs = new CTInfo[] {
            new CTInfo(NoteSyncSource.TYPE[NoteSyncSource.SIFN_FORMAT], "1.0"),
            new CTInfo(NoteSyncSource.TYPE[NoteSyncSource.PLAINTEXT_FORMAT], "1.0")
        };
        CTInfo[] txs = rxs;

        DataStore dataStorePreferSifContact = new DataStore(
                new SourceRef("contact"), "contact", 0,
                rxs[0], rxs, txs[0], txs,
                new DSMem(false),
                new SyncCap());

        principal.getDevice().getCapabilities().getDevInf().setDataStores(
                new DataStore[] { dataStorePreferSifContact });

        String actual = source.findRXContentType(context);
        assertEquals(NoteSyncSource.TYPE[NoteSyncSource.SIFN_FORMAT], actual);

        DataStore dataStorePreferVCard = new DataStore(
                new SourceRef("contact"), "contact", 0,
                rxs[1], rxs, txs[1], txs,
                new DSMem(false),
                new SyncCap());

        principal.getDevice().getCapabilities().getDevInf().setDataStores(
                new DataStore[] { dataStorePreferVCard });

        actual = source.findRXContentType(context);
        assertEquals(NoteSyncSource.TYPE[NoteSyncSource.PLAINTEXT_FORMAT], actual);
    }

}
