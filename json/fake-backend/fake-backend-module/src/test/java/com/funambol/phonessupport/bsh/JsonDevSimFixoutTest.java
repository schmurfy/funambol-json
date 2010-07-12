/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2009 Funambol, Inc.
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

package com.funambol.phonessupport.bsh;

import com.funambol.tools.test.BeanShellTestCase;
import com.funambol.framework.core.*;
import com.funambol.framework.tools.IOTools;
import com.funambol.framework.tools.SyncMLUtil;

import java.io.File;
import java.util.List;

/**
 * JsonDevSimFixoutTest
 */
public class JsonDevSimFixoutTest extends BeanShellTestCase {

    // ------------------------------------------------------------ Private data

    private String bshFileName =
            "src/main/config/com/funambol/server/engine/pipeline/phones-support/bsh/JsonDevSimFixout.bsh";
    private static final String XML_BASE_PATH =
            "src/test/resources/data/com/funambol/phonessupport/bsh/JsonDevSimFixout";

    // ---------------------------------------------------------- Public methods
    public JsonDevSimFixoutTest(String testName) throws Exception {
        super(testName);
        setBshFileName(bshFileName);
    }

    // -------------------------------------------------------------- Test cases


    /**
     * Perform the test on method removePrefixFromURIs, with the given input
     * and expected result.
     */
    public void testRemovePrefixFromURI() throws Throwable {
        final String filePath = XML_BASE_PATH + "/vcal1-response.xml";
        
        SyncML syncml = readMessage(filePath);

        assertTrue("Message doesn't contain prefixes", checkPrefixesInMessage(syncml));
        //remove prefixes
        execWithoutReturn("removePrefixFromURIs", syncml);

        assertFalse("Message contains prefixes", checkPrefixesInMessage(syncml));
    }

    /**
     * This test takes the same device simulator message failing due to the
     * item ids, and verify that the synclet makes it pass.
     */
    public void DISABLE_testVCAL1ResponseReference() throws Throwable {
        SyncML response = readMessage(XML_BASE_PATH + "/vcal1-response.xml");
        SyncML reference = readMessage(XML_BASE_PATH + "/vcal1-reference.xml");

        //remove prefixes
        execWithoutReturn("postProcessMessage", null, response);

        assertEquals("Response differs from reference",
                    SyncMLUtil.toXML(response),
                    SyncMLUtil.toXML(reference) );

    }

    // --------------------------------------------------------- Private methods

    private SyncML readMessage(final String filePath) {
        //unmarshal the syncml message into a SyncML object
        SyncML syncml = null;
        try {
            File f = new File(".", filePath);
            String xml = IOTools.readFileString(filePath);
            syncml = SyncMLUtil.fromXML(xml);
        } catch(Exception e) {
            fail("Error unmarshalling " + filePath + ":\n " + e);
        }
        return syncml;
    }

    /**
     * Checks that the Item Ids in the message do not contain '-'.
     *
     * @param syncml the parsed SyncML message.
     * @return true if there is at least one item ID containing a '-'.
     */
    private boolean checkPrefixesInMessage(SyncML syncml) {
        if (null == syncml) return false;

        List commands = syncml.getSyncBody().getCommands();
        for (Object command : commands) {
            if (command instanceof Sync) {
                for (Object cObj : ((Sync)command).getCommands()) {
                    for (Object iObj : ((ItemizedCommand)cObj).getItems() ) {
                        Item item = (Item)iObj;
                        if (null != item.getSource() && 
                            item.getSource().getLocURI().charAt(1) == '-') {
                            return true;
                        }
                        if (null != item.getTarget() &&
                            item.getTarget().getLocURI().charAt(1) == '-') {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}

