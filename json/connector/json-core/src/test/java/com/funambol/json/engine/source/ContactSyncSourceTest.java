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

import java.sql.Timestamp;

import net.sf.json.JSONException;

import com.funambol.framework.core.AlertCode;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.source.ContentType;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.engine.source.SyncSourceInfo;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.Sync4jDevice;

import com.funambol.json.abstractServlet.AbstractHttpTransportTest;
import com.funambol.json.abstractServlet.JsonServlet;
import com.funambol.json.security.JsonUser;

/**
 * Test cases for ContactSyncSource class.
 * @version $Id$
 */
public class ContactSyncSourceTest extends AbstractHttpTransportTest {

    // ------------------------------------------------------------ Private data
    private static JsonServlet jsonServlet = new JsonServlet();

    // ------------------------------------------------------------- Constructor
    public ContactSyncSourceTest() {
        super(jsonServlet);
    }

    // ------------------------------------------------------- Protected methods
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    // -------------------------------------------------------------- Test cases
    /**
     * 
     * @throws net.sf.json.JSONException
     */
    public void test_Add() throws JSONException {

        String sifcCard = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<contact><FirstName>Contact100</FirstName><LastName>100Contact</LastName>" +
                "<Folder>Contact</Folder></contact>";

        String sifcCardUpdate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<contact><FirstName>Contact100Update</FirstName><LastName>100ContactUpdate</LastName>" +
                "<Folder>ContactUpdate</Folder></contact>";

        ContactSyncSource source = null;
        SyncContext context = null;
        
        try {

            source = new ContactSyncSource();
            
            JsonUser user = new JsonUser("pippo", "pippo");
            user.setUsername("pippo");
            user.setPassword("pippo");
            Sync4jDevice device = new Sync4jDevice("deviceID");
            Sync4jPrincipal principal = new Sync4jPrincipal(user, device);

            context = new SyncContext(principal, 
                                      200, 
                                      null, 
                                      "localhost", 
                                      2, 
                                      new Timestamp(System.currentTimeMillis()),
                                      new Timestamp(System.currentTimeMillis()));

            ContentType[] contentTypes = null;
            contentTypes = new ContentType[1];
            contentTypes[0] = new ContentType("text/x-s4j-sifc", "1.0");
            source.setBackendType(new SyncSourceInfo(contentTypes, 0));
            
            source.init();
            
            jsonServlet.setDoReturn(JsonServlet.BEGINSYNC);
            source.beginSync(context);

            jsonServlet.setDoReturn(JsonServlet.ITEMS);
            
            Object anyKey = String.valueOf(System.currentTimeMillis()); // dummy
            SyncItem syncItem = new SyncItemImpl(source, anyKey, null, 
                    null, SyncItemState.NEW, sifcCard.getBytes(), null, "text/x-s4j-sifc", null);

            SyncItemKey key = source.addSyncItem(syncItem).getKey();
            assertTrue(key.getKeyAsString().equals("0"));
            
            SyncItem syncItemUpdated = new SyncItemImpl(source, key.getKeyValue(), null, 
                    null, SyncItemState.UPDATED, sifcCardUpdate.getBytes(), null,"text/x-s4j-sifc", null);
            assertTrue(syncItemUpdated.getKey().getKeyAsString().equals("0"));
            
            SyncItem syncItemUpdatedReturned = source.updateSyncItem(syncItemUpdated);
            assertTrue(syncItemUpdatedReturned.getKey().getKeyAsString().equals("0"));
            
            source.removeSyncItem(key, null, true);
            // @todo 
            // check if the key doesn't exist
            //assertNull(source.getSyncItemFromId(key));
            
            jsonServlet.setDoReturn(JsonServlet.ENDSYNC);
            source.endSync();            
            
        } catch (SyncSourceException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     *
     * @throws net.sf.json.JSONException
     */
    public void test_Add_vcard() throws JSONException {


        // Note: this is the vcard before the synclclet
        //BEGIN:VCARD
        //VERSION:2.1
        //REV:20090119T100034Z
        //N:Cher;Shannon;;;
        //ADR;HOME:;;;London;;;
        //ADR;WORK:;;;London;;XHN 8BA;
        //ORG:MisterMind;
        //BDAY:19830530
        //TEL;CELL:07846069419
        //EMAIL;INTERNET;ENCODING=QUOTED-PRINTABLE:shannon.cher=40mistermind.com
        //EMAIL;INTERNET;HOME;ENCODING=QUOTED-PRINTABLE:shannon.cher=40gmail.com
        //URL:http://www.zangbezang.com
        //TEL;VOICE;WORK:020111172
        //TEL;VOICE:0211139
        //TEL;FAX:0201111066
        //END:VCARD
        // after the synclet
        String vCard =
                    "BEGIN:VCARD\n"+
                    "VERSION:2.1\n"+
                    "REV:20090119T100034Z\n"+
                    "N:Cher;Shannon;;;\n"+
                    "ADR;HOME:;;;London;;;\n"+
                    "ADR;WORK:;;;London;;XHN 8BA;\n"+
                    "ORG:MisterMind;\n"+
                    "BDAY:19830530\n"+
                    "TEL;CELL:071111111\n"+
                    "EMAIL;INTERNET;ENCODING=QUOTED-PRINTABLE:shannon.cher=40mistermind.com\n"+
                    "EMAIL;INTERNET;HOME;ENCODING=QUOTED-PRINTABLE:shannon.cher=40gmail.com\n"+
                    "URL:http://www.zangbezang.com\n"+
                    "TEL;VOICE;WORK:021111111\n"+
                    "TEL;VOICE:020111839\n"+
                    "TEL;FAX:02031166\n"+
                    "PHOTO:\n"+
                    "TEL;CELL;HOME:\n"+
                    "TEL;VOICE;HOME:\n"+
                    "TEL;CELL;WORK:\n"+
                    "TEL;VOICE;CAR:\n"+
                    "ROLE:\n"+
                    "NOTE:\n"+
                    "TEL;FAX;WORK:\n"+
                    "TITLE:\n"+
                    "TEL;PAGER:\n"+
                    "URL;HOME:\n"+
                    "TEL;VOICE;PREF:\n"+
                    "EMAIL;INTERNET;WORK:\n"+
                    "NICKNAME:\n"+
                    "TEL;WORK;PREF:\n"+
                    "TEL;FAX;HOME:\n"+
                    "END:VCARD\n";

        String vCardUpdate =
                    "BEGIN:VCARD\n"+
                    "VERSION:2.1\n"+
                    "REV:20090119T100034Z\n"+
                    "N:Cher;Shannon;;;\n"+
                    "ADR;HOME:;;;London;;;\n"+
                    "ADR;WORK:;;;London;;XHN 8BA;\n"+
                    "ORG:MisterMind2;\n"+
                    "BDAY:19830530\n"+
                    "TEL;CELL:072222222\n"+
                    "EMAIL;INTERNET;ENCODING=QUOTED-PRINTABLE:shannon.cher=40mistermind.com\n"+
                    "EMAIL;INTERNET;HOME;ENCODING=QUOTED-PRINTABLE:shannon.cher=40gmail.com\n"+
                    "URL:http://www.zangbezang.com\n"+
                    "TEL;VOICE;WORK:022222272\n"+
                    "TEL;VOICE:022222839\n"+
                    "TEL;FAX:022222066\n"+
                    "PHOTO:\n"+
                    "TEL;CELL;HOME:\n"+
                    "TEL;VOICE;HOME:\n"+
                    "TEL;CELL;WORK:\n"+
                    "TEL;VOICE;CAR:\n"+
                    "ROLE:\n"+
                    "NOTE:\n"+
                    "TEL;FAX;WORK:\n"+
                    "TITLE:\n"+
                    "TEL;PAGER:\n"+
                    "URL;HOME:\n"+
                    "TEL;VOICE;PREF:\n"+
                    "EMAIL;INTERNET;WORK:\n"+
                    "NICKNAME:\n"+
                    "TEL;WORK;PREF:\n"+
                    "TEL;FAX;HOME:\n"+
                    "END:VCARD\n";


        ContactSyncSource source = null;
        SyncContext context = null;

        try {

            source = new ContactSyncSource();

            JsonUser user = new JsonUser("pippo", "pippo");
            user.setUsername("pippo");
            user.setPassword("pippo");
            Sync4jDevice device = new Sync4jDevice("deviceID");
            Sync4jPrincipal principal = new Sync4jPrincipal(user, device);

            context = new SyncContext(principal,
                                      200,
                                      null,
                                      "localhost",
                                      2,
                                      new Timestamp(System.currentTimeMillis()),
                                      new Timestamp(System.currentTimeMillis()));

            ContentType[] contentTypes = new ContentType[1];
            contentTypes[0] = new ContentType("text/x-vcard", "2.1");
            source.setInfo(new SyncSourceInfo(contentTypes,0));
            source.setBackendType(new SyncSourceInfo(contentTypes, 0));
            
            source.init();

            jsonServlet.setDoReturn(JsonServlet.BEGINSYNC);
            source.beginSync(context);

            jsonServlet.setDoReturn(JsonServlet.ITEMS);

            Object anyKey = String.valueOf(System.currentTimeMillis()); // dummy
            SyncItem syncItem = new SyncItemImpl(source,
                                                 anyKey,
                                                 null,
                                                 null,
                                                 SyncItemState.NEW,
                                                 vCard.getBytes(),
                                                 null,
                                                 "text/x-vcard",
                                                 null);

            SyncItemKey key = source.addSyncItem(syncItem).getKey();
            assertTrue(key.getKeyAsString().equals("1"));


            SyncItem syncItemAdded = source.getSyncItemFromId(key);

            byte[] itemContent = syncItemAdded.getContent();
            String resultItem = "";
            if (itemContent != null){
                resultItem = new String(itemContent);
            } else {
                resultItem = "null";
            }

            String expectedItem =
                    "BEGIN:VCARD\n"+
                    "VERSION:2.1\n"+
                    "N:Cher;Shannon;;;\n"+
                    "NICKNAME:\n"+
                    "ADR;HOME:;;;London;;;\n"+
                    "ADR:;;;;;;\n"+
                    "BDAY:1983-05-30\n"+
                    "TEL;CELL:071111111\n"+
                    "EMAIL;INTERNET;X-FUNAMBOL-INSTANTMESSENGER:\n"+
                    "EMAIL;INTERNET:shannon.cher@mistermind.com\n"+
                    "EMAIL;INTERNET;HOME:shannon.cher@gmail.com\n"+
                    "URL:http://www.zangbezang.com\n"+
                    "ADR;WORK:;;;London;;XHN 8BA;\n"+
                    "TITLE:\n"+
                    "ORG:MisterMind;\n"+
                    "TEL;VOICE;WORK:021111111\n"+
                    "TEL;VOICE;WORK:\n"+
                    "TEL;VOICE;HOME:\n"+
                    "TEL;VOICE;HOME:\n"+
                    "TEL;FAX;HOME:\n"+
                    "TEL;WORK;PREF:\n"+
                    "TEL;FAX;WORK:\n"+
                    "TEL:\n"+
                    "TEL;CAR;VOICE:\n"+
                    "TEL:\n"+
                    "TEL;VOICE:020111839\n"+
                    "TEL;FAX:02031166\n"+
                    "TEL;PREF;VOICE:\n"+
                    "TEL;PAGER:\n"+
                    "TEL:\n"+
                    "TEL:\n"+
                    "EMAIL;INTERNET;WORK:\n"+
                    "NOTE:\n"+
                    "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\n"+
                    "END:VCARD\n";

            //System.out.println("............. " + expectedItem);
            //System.out.println("............. " + resultItem);
            //assertEquals(expectedItem, resultItem);

            SyncItem syncItemUpdated = new SyncItemImpl(source,
                                                        key.getKeyValue(),
                                                        null,
                                                        null,
                                                        SyncItemState.UPDATED,
                                                        vCardUpdate.getBytes(),
                                                        null,
                                                        "text/x-vcard",
                                                        null);
            assertTrue(syncItemUpdated.getKey().getKeyAsString().equals("1"));

            SyncItem syncItemUpdatedReturned = source.updateSyncItem(syncItemUpdated);
            assertTrue(syncItemUpdatedReturned.getKey().getKeyAsString().equals("1"));

            source.removeSyncItem(key, null, true);
            // @todo
            // check if the key doesn't exist
            //assertNull(source.getSyncItemFromId(key));

            jsonServlet.setDoReturn(JsonServlet.ENDSYNC);
            source.endSync();

        } catch (SyncSourceException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * 
     * @throws net.sf.json.JSONException
     */
    public void test_RefreshFromClient() throws JSONException {


        jsonServlet.setDoReturn(JsonServlet.ITEMS);

        ContactSyncSource source = null;
        SyncContext context = null;
        
        try {

            source = new ContactSyncSource();
            
            JsonUser user = new JsonUser("pippo", "pippo");
            user.setUsername("pippo");
            user.setPassword("pippo");
            Sync4jDevice device = new Sync4jDevice("deviceID");
            Sync4jPrincipal principal = new Sync4jPrincipal(user, device);

            context = new SyncContext(principal, 
                                      AlertCode.REFRESH_FROM_CLIENT, 
                                      null, 
                                      "localhost", 
                                      2, 
                                      new Timestamp(System.currentTimeMillis()),
                                      new Timestamp(System.currentTimeMillis()));

            ContentType[] contentTypes = null;
            contentTypes = new ContentType[1];
            contentTypes[0] = new ContentType("text/x-s4j-sifc", "1.0");
            source.setBackendType(new SyncSourceInfo(contentTypes, 0));

            source.init();
            
            jsonServlet.setDoReturn(JsonServlet.BEGINSYNC);
            source.beginSync(context);


            jsonServlet.setDoReturn(JsonServlet.ENDSYNC);
            source.endSync();
            
        } catch (SyncSourceException e) {
            e.printStackTrace();
            fail();
        }
    }
}
