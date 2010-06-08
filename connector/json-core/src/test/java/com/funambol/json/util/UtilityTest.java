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

package com.funambol.json.util;

import junit.framework.TestCase;

/**
 *
 * @author Vanetti
 */
public class UtilityTest extends TestCase {
    
    public UtilityTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of changeDateFormat method, of class Utility.
     */
    public void testChangeDateFormat_1() {
        String result = Utility.changeDateFormat("2009-01-17",
                                                 "Europe/Rome",
                                                 Utility.TIME_ALLDAY_START,
                                                 Utility.DATETIME_YYYYMMDD);
        assertEquals("20090117", result);
    }

    /**
     * Test of changeDateFormat method, of class Utility.
     */
    public void testChangeDateFormat_2() {
        String result = Utility.changeDateFormat("20090117",
                                                 "Europe/Rome",
                                                 Utility.TIME_ALLDAY_START,
                                                 Utility.DATETIME_YYYYMMDD);
        assertEquals("20090117", result);
    }

    /**
     * Test of changeDateFormat method, of class Utility.
     */
    public void testChangeDateFormat_3() {
        String result = Utility.changeDateFormat("2009-01-17",
                                                 "Europe/Rome",
                                                 Utility.TIME_ALLDAY_END,
                                                 Utility.DATETIME_YYYYMMDD);
        assertEquals("20090117", result);
    }

    /**
     * Test of changeDateFormat method, of class Utility.
     */
    public void testChangeDateFormat_4() {
        String result = Utility.changeDateFormat("20090117",
                                                 "Europe/Rome",
                                                 Utility.TIME_ALLDAY_END,
                                                 Utility.DATETIME_YYYYMMDD);
        assertEquals("20090117", result);
    }

    /**
     * Test of changeDateFormat method, of class Utility.
     */
    public void testChangeDateFormat_5() {
        String result = Utility.changeDateFormat("20090117T123456Z",
                                                 "Europe/Rome",
                                                 null,
                                                 Utility.DATETIME_YYYYMMDDTHHMMSSZ);
        assertEquals("20090117T123456Z", result);
    }

    /**
     * Test of changeDateFormat method, of class Utility.
     */
    public void testChangeDateFormat_6() {
        String result = Utility.changeDateFormat("20090117T123456Z",
                                                 "Asia/Manila",
                                                 null,
                                                 Utility.DATETIME_YYYYMMDDTHHMMSSZ);
        assertEquals("20090117T123456Z", result);
    }

    /**
     * Test of changeDateFormat method, of class Utility.
     */
    public void testChangeDateFormat_7() {
        String result = Utility.changeDateFormat("20090117T123456Z",
                                                 "Europe/Rome",
                                                 null,
                                                 Utility.DATETIME_YYYYMMDDTHHMMSS);
        assertEquals("20090117T133456", result);
    }

    /**
     * Test of changeDateFormat method, of class Utility.
     */
    public void testChangeDateFormat_8() {
        String result = Utility.changeDateFormat("20090117T133456",
                                                 "Europe/Rome",
                                                 null,
                                                 Utility.DATETIME_YYYYMMDDTHHMMSSZ);
        assertEquals("20090117T123456Z", result);
    }
     /**
     * Test of changeDateFormat method, of class Utility.
     */
    public void testChangeDateFormat_9() {
        String result = Utility.changeDateFormat(null,
                                                 "Europe/Rome",
                                                 null,
                                                 Utility.DATETIME_YYYYMMDDTHHMMSSZ);
        assertEquals(null, result);
    }
     /**
     * Test of changeDateFormat method, of class Utility.
     */
    public void testChangeDateFormat_10() {
        String result = Utility.changeDateFormat("",
                                                 "Europe/Rome",
                                                 null,
                                                 Utility.DATETIME_YYYYMMDDTHHMMSSZ);
        assertEquals("", result);
    }

}
