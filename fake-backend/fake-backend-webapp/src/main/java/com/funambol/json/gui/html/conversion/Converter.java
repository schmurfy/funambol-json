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
package com.funambol.json.gui.html.conversion;

import com.funambol.json.gui.html.NameValuePair;

/**
 * This interface provides method to converting string into different objects.
 * 
 * @version $Id$
 */
public interface Converter {

    // New state
    public final static String STATE_NEW       = "N";
    
    // Input formats key
    public final static String I_RAW_FORMAT    = "RAW";
    public final static String I_RAW_LABEL     = "Raw format";

    public final static String I_SIF_FORMAT    = "ISIF";
    public final static String I_SIF_LABEL     = "Sif";
    
    public static final String O_EXTENDED_FORMAT ="O_EXT";
    public static final String O_EXTENDED_LABEL  ="Extended";


    
    /**
     * Using this method you can apply a conversion from an input string whose format is
     * specified by the inputFormat parameter to an output string whose format is specified
     * by the outputFormat parameter.
     * 
     * @param inputFormat is the format of the input string.
     * @param outputFormat is the desired format of the conversion result
     * @param inputString is the input string
     * 
     * @return the string containing the result of conversion.
     * @throws ConversionException if something goes wrong
     */
     
    String applyConversion(String inputFormat, String outputFormat, String inputString) throws ConversionException;
    
    
    /**
     * This method can be used to obtai an array of NameValuePair. Each object in this
     * array defines a valid input format. The name specifies a human readable id of the format,
     * while the value specifies a format id.
     * 
     * @return an array of input formats.
     */
    
    NameValuePair[] getAvailableInputFormat();
    
    /**
     * This method can be used to obtai an array of NameValuePair. Each object in this
     * array defines a valid output format. The name specifies a human readable name of the format,
     * while the value specifies a format id.
     * 
     * @return an array of output formats.
     */
    
    NameValuePair[] getAvailableOutputFormat();
    
}
