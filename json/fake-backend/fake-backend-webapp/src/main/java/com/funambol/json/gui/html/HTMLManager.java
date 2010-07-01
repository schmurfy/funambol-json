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

import com.funambol.json.gui.GuiServlet;
import com.funambol.json.utility.Definitions;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is used to produce html tags and to build html pages.
 */
public class HTMLManager {

    private static final String JSON_LABEL  = "Json-Extended" ;
    private static final String VCARD_LABEL = "vCard" ;
    private static final String VCAL_LABEL  = "vCalendar" ;
    private static final String ICAL_LABEL  = "iCalendar" ;

    public static String addTextfield(String name, String value, int size, int maxlenght) {
        return openTag(Tag.INPUT, NameValuePair.parseFromStrings("name",name,"value",value==null?"":value,"maxlenght",""+maxlenght,"size",""+size));
    }

    public static String addPassword(String name, String value, int size, int maxlenght) {
        return openTag(Tag.INPUT, NameValuePair.parseFromStrings("name",name,"value",value==null?"":value,"type","password","maxlenght",""+maxlenght,"size",""+size));
    }

    public static String createOneRowTable(String...cellValues) {
        if(cellValues!=null && cellValues.length>0) {
            String result ="<table><tr>\n";
            result+=buildTableRow(cellValues);
            result +="</tr></table>\n";
            return result;
        } else {
            return "";
        }

    }


    public static String buildTableRow(String...cellValues) {
       String result = "<tr>";
       if(cellValues!=null && cellValues.length>0) {
           for(String cellValue:cellValues)
               if(cellValue!=null) {
                    result+="<td>"+cellValue+"</td>";
               }
       }
       result+="</tr>";
       return result;
    }


    public static String closePage() {
         return closePage(null);
    }


    public static String closePage(String footer) {
        String result = "</body>\n</html>\n";
        if(footer!=null)
            result = footer+result;
        return result;
    }

    public static String[] addCheckBoxes(String name, NameValuePair[] values, NameValuePair...attributes) {
        if(values!=null && values.length>0) {
            List<String> checks = new ArrayList<String>();
            for(NameValuePair value:values) {
                if(value!=null)
                    checks.add(addCheckBox(name, value.getValue(),value.getName(), attributes));
            }
            if(checks.size()>0)
                return checks.toArray(new String[]{});
        }
        return null;
    }

    public static String addCheckBox(String name, String value,String label, NameValuePair...attributes) {
        return openTag(Tag.INPUT, NameValuePair.parseFromStrings(attributes, "type","checkbox","name",name,"value",value))+ " "+label;
    }

   public static String[] addRadios(String name, NameValuePair[] values, NameValuePair...attributes) {
        if(values!=null && values.length>0) {
            List<String> checks = new ArrayList<String>();
            for(int i=0;i<values.length;i++) {
                NameValuePair value = values[i];
                if(value!=null) {
                    checks.add(addRadio(name, value.getValue(),value.getName(),i==0, attributes));
                }
            }
            if(checks.size()>0)
                return checks.toArray(new String[]{});
        }
        return null;
    }

    public static String addRadio(String name, String value,String label,boolean checked, NameValuePair...attributes) {
        if(checked)
            return openTag(Tag.INPUT, NameValuePair.parseFromStrings(attributes, "type","radio","name",name,"value",value,"checked","true"))+ " "+label;
        return openTag(Tag.INPUT, NameValuePair.parseFromStrings(attributes, "type","radio","name",name,"value",value))+ " "+label;
    }


    public static String addAction(String formName, String buttonName, String buttonAction, NameValuePair[] furterParameters, ParentTag mode) {
        return addAction(formName,buttonName, buttonAction, furterParameters,mode,true);

    }

    public static String addAction(String formName, String buttonName, String buttonAction, NameValuePair[] furtherParameters, ParentTag mode, boolean createForm) {
        String result = "";
        if(ParentTag.BOTH.equals(mode) || ParentTag.OPEN.equals(mode))
             result+=openTag(Tag.FORM,NameValuePair.parseFromStrings("name",formName,"method","get","action","./gui"));


        result+=addFormField(GuiServlet.ACTION,buttonAction);
        if(furtherParameters!=null && furtherParameters.length>0) {
            for(NameValuePair param:furtherParameters) {
                result+=addFormField(param.getName(), param.getValue());
            }
        }
        result +=buildHtmlTag(Tag.BUTTON,buttonName,NameValuePair.parseFromStrings("type","submit"));
        if(ParentTag.BOTH.equals(mode) || ParentTag.CLOSE.equals(mode))
            result +=closeTag(Tag.FORM);
        return result;
    }

     public static String addFormField(String name,String value) {
        return addFormField(name, value, null);
     }

     public static String addFormField(String name,String value, String id) {
        if(id==null)
            return buildHtmlTag(Tag.INPUT, null, NameValuePair.parseFromStrings("type","hidden","name",name,"value",value));
        else
            return buildHtmlTag(Tag.INPUT, null, NameValuePair.parseFromStrings("type","hidden","name",name,"value",value,"id",id));
     }


    public static String addTextArea(String name, String value, int cols, int rows, boolean readonly) {
        if(readonly)
            return buildHtmlTag(Tag.TEXTAREA,value,NameValuePair.parseFromStrings("name",name,"rows",""+rows,"cols",""+cols,"readonly",""+readonly));
        else
            return buildHtmlTag(Tag.TEXTAREA,value,NameValuePair.parseFromStrings("name",name,"rows",""+rows,"cols",""+cols));
    }

    public static String buildHtmlTag(Tag tag, String innerHtml,NameValuePair...attributes) {
       String result = openTag(tag, attributes);
       if(innerHtml!=null)
           result+=innerHtml+"\n";
        result+=closeTag(tag);
        return result;
    }

    public static String closeTag(Tag tag) {
       return "</"+tag.tagValue+">\n";
    }

    public static String openTag(Tag tag) {
        String result = "<"+tag.tagValue+">\n";
        return result;
    }

    public static String openTag(Tag tag,NameValuePair...attributes) {
        String result = "<"+tag.tagValue;

        if(attributes!=null && attributes.length>0) {
            for(NameValuePair attrib:attributes)  {
                if(attrib!=null) {
                    result+=" "+attrib.getName()+"=\""+attrib.getValue()+"\"";
                }
            }

        }

        result+=">\n";
        return result;
    }

    public static String buildHRef(String link,String url) {
       return buildHtmlTag(Tag.LINK, link, NameValuePair.parseFromStrings("href",url));
    }

    public static String buildHRef(String application,String link,NameValuePair...parameters) {
        String params = "";
        if(parameters!=null && parameters.length>0) {
            params = parameters[0].appendToUrlAsFirst();
            for(int i=1;i<parameters.length;i++) {
                if(parameters[i]!=null)
                    params+=parameters[i].appendToUrl();
            }
        }
        return "<a href=\"./"+application+params+"\">"+link+"</a>";
    }


    public static String getHtmlHeaderFor(String title) {
        return getHtmlHeaderFor(title,false);
    }

    public static String getHtmlHeaderFor(String title, boolean javascript) {
        String result = "<html>\n";
        if(javascript) {
            result+="<head>\n";
            result+="   <script type=\"text/javascript\">\n";
            result+="       function doAction(actionId,actionValue,keyId,keyValue) {\n";
            result+="           document.getElementById(actionId).value=actionValue;\n";
            result+="           document.getElementById(keyId).value=keyValue;\n";
            result+="       }\n";
            result+="   </script>\n";
            result+="   <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n";
            result+="</head>\n";
        }
        result+="<body>\n";
        result+=buildHtmlTag(Tag.H2,title);
        return result;
    }

    public static String getHtmlForDatastoreType(String group, String defaultOption) {
        StringBuffer result = new StringBuffer();
        List<NameValuePair> options = new ArrayList<NameValuePair>();

        if (group.equals("appointment")) {
            options.add(new NameValuePair(JSON_LABEL, Definitions.JSON_EXTENDED));
            options.add(new NameValuePair(VCAL_LABEL, Definitions.VCAL_FORMAT));
            options.add(new NameValuePair(ICAL_LABEL, Definitions.ICAL_FORMAT));
        } else if (group.equals("task")) {
            options.add(new NameValuePair(JSON_LABEL, Definitions.JSON_EXTENDED));
            options.add(new NameValuePair(VCAL_LABEL, Definitions.VCAL_FORMAT));
            options.add(new NameValuePair(ICAL_LABEL, Definitions.ICAL_FORMAT));            
        } else if (group.equals("contact")) {
            options.add(new NameValuePair(JSON_LABEL, Definitions.JSON_EXTENDED));
            options.add(new NameValuePair(VCARD_LABEL, Definitions.CONTACT_VCARD_TYPE));
        }

        if (options.size() > 0) {
            result.append("<hr/>" + "<form method=\"post\">\n");
            result.append(HTMLManager.addFormField(GuiServlet.ACTION, GuiServlet.LIST));
            result.append(HTMLManager.addFormField(GuiServlet.GROUP, group));
            result.append("Datastore type:");
            result.append(String.format("<select name=\"%s\">\n", Definitions.DATASTORETYPE));

            for (NameValuePair opt : options) {
                String isSelected = opt.getValue().equals(defaultOption) ? "selected=\"selected\"" : "";
                result.append(String.format("<option %s value=\"%s\">%s</option>\n", isSelected, opt.getValue(), opt.getName()));
            }

            result.append("</select>\n");
            result.append("<input type=\"submit\" value=\"Change\" />" + "</form>\n");
            result.append("<hr/>");
        }
        return result.toString();
    }

}

