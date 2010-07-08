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

package com.funambol.json.gui.html;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to represnt name - value pair. Objects of this kind
 * can be used, for example, to define html tag attributes.
 * 
 * 
 * @version $Id$
 */

public class NameValuePair {
    
        private String name = "";
        private String value = "";
        
        /**
         * Builds an object wrapping the name - value pair
         * passed as parameters.
         * 
         * @param name parameter name
         * @param value parameter value
         */
        
        public NameValuePair(String name, String value) {
            this.name  = name;
            this.value = value;
        }
        
        /**
         * This method is used to append this parameter to an url
         * It assumes that the urls already contais parameter.
         * 
         * @return
         */
        
        public String appendToUrl() {
            return "&"+getName()+"="+getValue();
        }
        
        public String appendToUrlAsFirst() {
            return "?"+getName()+"="+getValue();
        }

        
        /**
         * @return parameter name attribute
         */
        public String getName() {
            return name;
        }

       /**
        * @return parameter value attribute
        */
       
        public String getValue() {
            return value;
        }
        
     public static  NameValuePair[] parseFromStrings(String...token) {
        if(token!=null && token.length>0) 
            return parseFromStrings(null, token);
        return null;
    }
     
     public static  NameValuePair[] parseFromStrings(NameValuePair[] further,String...token) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if(token!=null && token.length>0) {
           for(int i=0;i<token.length;i++) {
               String name = token[i];
               String value = "";
               if((i++)<token.length) {
                    value = token[i];
               }
               params.add(new NameValuePair(name, value));
           }
        }
        
        if(further!=null && further.length>0) {
            for(NameValuePair toAdd:further)
                if(toAdd!=null)
                   params.add(toAdd);
        }
        
        if(params.size()>0)
           return params.toArray(new NameValuePair[]{});

        
        return null;
    }
        

}
