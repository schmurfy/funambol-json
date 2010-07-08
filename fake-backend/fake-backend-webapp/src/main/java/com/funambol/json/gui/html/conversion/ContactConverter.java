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

import com.funambol.common.pim.common.TypifiedProperty;
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.contact.Email;
import com.funambol.common.pim.contact.Note;
import com.funambol.common.pim.contact.SIFC;
import com.funambol.common.pim.contact.Title;
import com.funambol.common.pim.contact.WebPage;
import com.funambol.common.pim.converter.ContactToVcard;
import com.funambol.common.pim.sif.SIFCParser;
import com.funambol.common.pim.vcard.VcardParser;
import com.funambol.json.domain.JsonContactModel;
import com.funambol.json.gui.html.NameValuePair;
import com.funambol.json.util.Utility;
import com.funambol.json.utility.Definitions;
import com.funambol.json.utility.FakeDevice;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

/**
 * Converter for contact objects.
 * 
 * @version $Id$
 */
public class ContactConverter implements Converter {

    private final static Logger log = Logger.getLogger(Definitions.LOG_NAME);
    
    public static final String RFC_CTYPE        = "application/json-vcard";
    public static final String EXTENDED_CTYPE   = "application/json-card";
    
    public static final String I_VCARD_FORMAT ="I_VCARD";
    public static final String I_VCARD_LABEL  ="VCard";
    
    public static final String O_RFC_FORMAT       ="O_RFC";
    public static final String O_RFC_LABEL        ="Rfc";

    
    private final static NameValuePair[] inputFormat = NameValuePair.parseFromStrings(I_RAW_LABEL,I_RAW_FORMAT,I_SIF_LABEL,I_SIF_FORMAT,I_VCARD_LABEL,I_VCARD_FORMAT);
    private final static NameValuePair[] outputFormat = NameValuePair.parseFromStrings(O_RFC_LABEL,O_RFC_FORMAT,O_EXTENDED_LABEL,O_EXTENDED_FORMAT);
    
    public String applyConversion(String inputFormat, String outputFormat, String inputString) throws ConversionException {
        log.debug("Converting form ["+inputFormat+"] to ["+outputFormat+"].");
        if(O_RFC_FORMAT.equals(outputFormat)) {
            String result = null;
            if(I_SIF_FORMAT.equals(inputFormat)) {
                Contact contact = sif2Contact(inputString);
                result          = contact2vcard(contact);
            } else if(I_VCARD_FORMAT.equals(inputFormat)) {
                result          = inputString;
            }
            return toRFC("vcard", inputString, result, STATE_NEW, RFC_CTYPE) ;
            
            
        } else if(O_EXTENDED_FORMAT.equals(outputFormat)) {
            Contact contact = null;
            if(I_SIF_FORMAT.equals(inputFormat)) {
                contact = sif2Contact(inputString);
            } else if(I_VCARD_FORMAT.equals(inputFormat)) {
                contact = vcard2Contact(inputString);
            }
            return toJSON(contact, "",STATE_NEW,EXTENDED_CTYPE);
            
        }
        
        throw new ConversionException("IO formats non recognized ["+inputFormat+", "+outputFormat+"]");
    }

    public NameValuePair[] getAvailableInputFormat() {
        return inputFormat;
    }

    public NameValuePair[] getAvailableOutputFormat() {
        return outputFormat;
    }
    
   public Contact sif2Contact(String sifc) throws ConversionException {

        ByteArrayInputStream buffer = null;
        SIFCParser parser = null;
        Contact contact = null;
        try {
            contact = new Contact();
            buffer = new ByteArrayInputStream(sifc.getBytes());
            if ((sifc.getBytes()).length > 0) {
                parser = new SIFCParser(buffer);
                contact = (Contact) parser.parse();
            }
        } catch (Exception e) {
            throw new ConversionException("Error converting SIF-C to Contact. ", e);
        }

        return contact;
    }
      
      public Contact vcard2Contact(String vcard) throws ConversionException {

        ByteArrayInputStream buffer = null;
        VcardParser parser = null;
        Contact contact = null;
        try {
            contact = new Contact();

            buffer = new ByteArrayInputStream(vcard.getBytes());
            if ((vcard.getBytes()).length > 0) {
                parser = new VcardParser(buffer);
                contact = (Contact) parser.vCard();
            }
        } catch (Exception e) {
            throw new ConversionException("Error converting VCARD to Contact. ", e);
        }

        return contact;
    }
      
//    private String contactSif2json(String buffer, String notation) throws Exception {
//        Contact contact = ImportUtility.sif2Contact(buffer);
//        if(RFC_NOTATION.equals(notation)) {
//            String vcard = ImportUtility.contact2vcard(contact);
//            return ImportUtility.toRFC(JsonContactModel.VCARD.getValue(),vcard,"",STATE_NEW,EXTENDED_CTYPE);
//        } else if(EXTENDED_NOTATION.equals(notation)) {
//            return ImportUtility.toJSON(contact, "", STATE_NEW, RFC_CTYPE);
//        }
//        
//         throw new Exception("Notation ["+notation+"] not supported.");
//        
//    }
    
      
      public String toJSON(Contact contact, String key,String state, String contentType) {

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();
        
        jsonData.elementOpt(JsonContactModel.CONTENT_TYPE.getValue(), contentType);
        
        jsonItem.elementOpt(JsonContactModel.KEY.getValue(), key);
        jsonItem.elementOpt(JsonContactModel.STATE.getValue(), state);
        
        // folder         
        String folder = Utility.getPropertyValue(contact.getFolder());
        if (!folder.equals("")){
            folder = Utility.folderConverterC2S(folder, Utility.BACKEND_CONTACT_FOLDER_PREFIX);
            jsonItem.elementOpt(JsonContactModel.FOLDER.getValue(), folder);
        } else {
            jsonItem.elementOpt(JsonContactModel.FOLDER.getValue(), Utility.BACKEND_CONTACT_FOLDER_PREFIX);
        }
        
                
        if( contact.getName() != null && contact.getName().getFirstName() != null)
        	jsonItem.elementOpt(JsonContactModel.FIRSTNAME.getValue(), contact.getName().getFirstName().getPropertyValueAsString());
        
        if( contact.getName() != null && contact.getName().getLastName() != null)
        	jsonItem.elementOpt(JsonContactModel.LASTNAME.getValue(), contact.getName().getLastName().getPropertyValueAsString());
        
          if( contact.getName() != null && contact.getName().getNickname() != null){
            jsonItem.elementOpt(JsonContactModel.NICKNAME.getValue(), 
                    contact.getName().getNickname().getPropertyValueAsString());
        }
        
            
        if( contact.getBusinessDetail() != null && 
            contact.getBusinessDetail().getCompany()!=null)
        	jsonItem.elementOpt(JsonContactModel.COMPANY.getValue(), contact.getBusinessDetail().getCompany().getPropertyValueAsString());

        if ((contact.getBusinessDetail().getTitles() != null) && 
            (!contact.getBusinessDetail().getTitles().isEmpty())) {
            jsonItem.elementOpt(JsonContactModel.JOB_TITLE.getValue(), 
                    ((Title)contact.getBusinessDetail().getTitles().get(0)).getPropertyValueAsString());
        }
    
        if ( contact.getPersonalDetail().getEmails()!= null && 
            !contact.getPersonalDetail().getEmails().isEmpty()) {
            try {
                Email e = (Email) contact.getPersonalDetail().getEmails().get(0);                
                jsonItem.element(JsonContactModel.EMAIL.getValue(), e.getPropertyValueAsString());                
            } catch (Exception ex){
                // do nothing
            }
            try {
                Email e1 = (Email) contact.getPersonalDetail().getEmails().get(1);                
                jsonItem.element(JsonContactModel.EMAIL2.getValue(), e1.getPropertyValueAsString());                
            } catch (Exception ex){
                // do nothing
            }
        }
        
        if ( contact.getBusinessDetail().getEmails()!= null && 
            !contact.getBusinessDetail().getEmails().isEmpty()) {
            Email e = (Email) contact.getBusinessDetail().getEmails().get(0);
            jsonItem.element(JsonContactModel.EMAIL3.getValue(), e.getPropertyValueAsString());
        }
                

        if ((contact.getNotes() != null) && (!contact.getNotes().isEmpty())) {
           jsonItem.elementOpt(JsonContactModel.BODY.getValue(), ((Note)contact.getNotes().get(0)).getPropertyValueAsString());
        }

        
        if ((contact.getPersonalDetail() != null) && 
            (contact.getPersonalDetail().getWebPages() != null)  && 
            !contact.getPersonalDetail().getWebPages().isEmpty()) {
           jsonItem.elementOpt(JsonContactModel.URL_WEB.getValue(), 
                   ((WebPage)contact.getPersonalDetail().getWebPages().get(0)).getPropertyValueAsString());
        }
        
        // IM_ADDRESS("instantMessenger1"),
        // @todo
        
        if( contact.getBusinessDetail() != null && contact.getBusinessDetail().getAddress() != null && contact.getBusinessDetail().getAddress().getStreet()!=null)
           jsonItem.elementOpt(JsonContactModel.BUSINESS_ADDR_STREET.getValue(), contact.getBusinessDetail().getAddress().getStreet().getPropertyValueAsString());
                
        if( contact.getBusinessDetail() != null && contact.getBusinessDetail().getAddress() != null && contact.getBusinessDetail().getAddress().getCity()!=null)
           jsonItem.elementOpt(JsonContactModel.BUSINESS_ADDR_CITY.getValue(), contact.getBusinessDetail().getAddress().getCity().getPropertyValueAsString());
        
        if( contact.getBusinessDetail() != null && contact.getBusinessDetail().getAddress() != null && contact.getBusinessDetail().getAddress().getCountry()!=null)
           jsonItem.elementOpt(JsonContactModel.BUSINESS_ADDR_COUNTRY.getValue(), contact.getBusinessDetail().getAddress().getCountry().getPropertyValueAsString());
        
        if( contact.getBusinessDetail() != null && contact.getBusinessDetail().getAddress() != null && contact.getBusinessDetail().getAddress().getState()!=null)
           jsonItem.elementOpt(JsonContactModel.BUSINESS_ADDR_STATE.getValue(), contact.getBusinessDetail().getAddress().getState().getPropertyValueAsString());
        
        if( contact.getPersonalDetail() != null && contact.getBusinessDetail().getAddress() != null && contact.getBusinessDetail().getAddress().getPostalCode()!=null)
           jsonItem.elementOpt(JsonContactModel.BUSINESS_ADDR_POSTALCODE.getValue(), contact.getBusinessDetail().getAddress().getPostalCode().getPropertyValueAsString());
        
        if( contact.getBusinessDetail() != null && contact.getPersonalDetail().getAddress() != null && contact.getPersonalDetail().getAddress().getStreet()!=null)
           jsonItem.elementOpt(JsonContactModel.HOME_ADDR_STREET.getValue(), contact.getPersonalDetail().getAddress().getStreet().getPropertyValueAsString());
        
        if( contact.getBusinessDetail() != null && contact.getPersonalDetail().getAddress() != null && contact.getPersonalDetail().getAddress().getCity()!=null)
           jsonItem.elementOpt(JsonContactModel.HOME_ADDR_CITY.getValue(), contact.getPersonalDetail().getAddress().getCity().getPropertyValueAsString());
        
        if( contact.getBusinessDetail() != null && contact.getPersonalDetail().getAddress() != null && contact.getPersonalDetail().getAddress().getCountry()!=null)
           jsonItem.elementOpt(JsonContactModel.HOME_ADDR_COUNTRY.getValue(), contact.getPersonalDetail().getAddress().getCountry().getPropertyValueAsString());
        
        if( contact.getBusinessDetail() != null && contact.getPersonalDetail().getAddress() != null && contact.getPersonalDetail().getAddress().getState()!=null)
           jsonItem.elementOpt(JsonContactModel.HOME_ADDR_STATE.getValue(), contact.getPersonalDetail().getAddress().getState().getPropertyValueAsString());
        
        if( contact.getBusinessDetail() != null && contact.getPersonalDetail().getAddress() != null && contact.getPersonalDetail().getAddress().getPostalCode()!=null)
           jsonItem.elementOpt(JsonContactModel.HOME_ADDR_POSTALCODE.getValue(), contact.getPersonalDetail().getAddress().getPostalCode().getPropertyValueAsString());
                              
        if( contact.getPersonalDetail() != null && contact.getPersonalDetail().getOtherAddress() != null && contact.getPersonalDetail().getOtherAddress().getStreet()!=null)
           jsonItem.elementOpt(JsonContactModel.OTHER_ADDR_STREET.getValue(), contact.getPersonalDetail().getOtherAddress().getStreet().getPropertyValueAsString());
        
        if( contact.getPersonalDetail() != null && contact.getPersonalDetail().getOtherAddress() != null && contact.getPersonalDetail().getOtherAddress().getCity()!=null)
           jsonItem.elementOpt(JsonContactModel.OTHER_ADDR_CITY.getValue(), contact.getPersonalDetail().getOtherAddress().getCity().getPropertyValueAsString());
        
        if( contact.getPersonalDetail() != null && contact.getPersonalDetail().getOtherAddress() != null && contact.getPersonalDetail().getOtherAddress().getCountry()!=null)
           jsonItem.elementOpt(JsonContactModel.OTHER_ADDR_COUNTRY.getValue(), contact.getPersonalDetail().getOtherAddress().getCountry().getPropertyValueAsString());
        
        if( contact.getPersonalDetail() != null && contact.getPersonalDetail().getOtherAddress() != null && contact.getPersonalDetail().getOtherAddress().getState()!=null)
           jsonItem.elementOpt(JsonContactModel.OTHER_ADDR_STATE.getValue(), contact.getPersonalDetail().getOtherAddress().getState().getPropertyValueAsString());
        
        if( contact.getPersonalDetail() != null && contact.getPersonalDetail().getOtherAddress() != null && contact.getPersonalDetail().getOtherAddress().getPostalCode()!=null)
           jsonItem.elementOpt(JsonContactModel.OTHER_ADDR_POSTALCODE.getValue(), contact.getPersonalDetail().getOtherAddress().getPostalCode().getPropertyValueAsString());
         

        //phones
        List<TypifiedProperty> typifiedProperties = new ArrayList<TypifiedProperty>();
        typifiedProperties.addAll(contact.getPersonalDetail().getPhones());
        typifiedProperties.addAll(contact.getBusinessDetail().getPhones());
            
        for (TypifiedProperty p : typifiedProperties) {
            
            String s    = p.getPropertyValueAsString();
            String type = p.getPropertyType();
            
            if (SIFC.BUSINESS_TELEPHONE_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_BUSINESS.getValue(), s);            
            } else if (SIFC.BUSINESS2_TELEPHONE_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_BUSINESS_2.getValue(), s);
            } else if (SIFC.HOME_TELEPHONE_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_HOME.getValue(), s);
            } else if (SIFC.HOME2_TELEPHONE_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_HOME_2.getValue(), s);
            } else if (SIFC.HOME_FAX_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_HOME_FAX.getValue(), s);
            } else if (SIFC.COMPANY_MAIN_TELEPHONE_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_COMPANY.getValue(), s);
            } else if (SIFC.BUSINESS_FAX_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_BUSINESS_FAX.getValue(), s);
            } else if (SIFC.MOBILE_TELEPHONE_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_MOBILE.getValue(), s);
            } else if (SIFC.CALLBACK_TELEPHONE_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_CALLBACK.getValue(), s);
            } else if (SIFC.CAR_TELEPHONE_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_CAR.getValue(), s);
            } else if (SIFC.ASSISTANT_TELEPHONE_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_ASSISTANT.getValue(), s);
            } else if (SIFC.OTHER_TELEPHONE_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_OTHER.getValue(), s);
            } else if (SIFC.OTHER_FAX_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_OTHER_FAX.getValue(), s);
            } else if (SIFC.PRIMARY_TELEPHONE_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_PRIMARY.getValue(), s);
            } else if (SIFC.PAGER_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_PAGER.getValue(), s);
            } else if (SIFC.RADIO_TELEPHONE_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_RADIO.getValue(), s);
            } else if (SIFC.TELEX_NUMBER.equals(type)) {
                jsonItem.elementOpt(JsonContactModel.PHONE_TELEX.getValue(), s);
            }
            // ISDN
            // @todo

            //PHONE_TTY_TDD
            // @todo
            
        }        
        
        if( contact.getBusinessDetail() != null && 
            contact.getBusinessDetail().getDepartment() !=null) {
        	jsonItem.elementOpt(JsonContactModel.DEPARTMENT.getValue(), 
                        contact.getBusinessDetail().getDepartment().getPropertyValueAsString());
        }

        if( contact.getBusinessDetail() != null && 
            contact.getBusinessDetail().getOfficeLocation() !=null) {
        	jsonItem.elementOpt(JsonContactModel.OFFICE.getValue(), 
                        contact.getBusinessDetail().getOfficeLocation());
        }

        // PROFESSION("profession"),
        // @todo
        
        if( contact.getBusinessDetail() != null && 
            contact.getBusinessDetail().getManager() !=null) {
        	jsonItem.elementOpt(JsonContactModel.MANAGER_NAME.getValue(), 
                        contact.getBusinessDetail().getManager());
        }
        
        if( contact.getBusinessDetail() != null && 
            contact.getBusinessDetail().getAssistant() !=null) {
        	jsonItem.elementOpt(JsonContactModel.ASSISTANT_NAME.getValue(), 
                        contact.getBusinessDetail().getAssistant());
        }
        
        // NICKNAME("nickName"),
        // @todo

        if( contact.getPersonalDetail() != null && 
            contact.getPersonalDetail().getBirthday() !=null) {
        	jsonItem.elementOpt(JsonContactModel.BIRTHDAY.getValue(), 
                        contact.getPersonalDetail().getBirthday());
        }
        
        if( contact.getPersonalDetail() != null && 
            contact.getPersonalDetail().getSpouse() !=null) {
        	jsonItem.elementOpt(JsonContactModel.SPOUSE_NAME.getValue(), 
                        contact.getPersonalDetail().getSpouse());
        }
        
        if( contact.getPersonalDetail() != null && 
            contact.getPersonalDetail().getAnniversary() !=null) {
        	jsonItem.elementOpt(JsonContactModel.ANNIVERSARY.getValue(), 
                        contact.getPersonalDetail().getAnniversary());
        }


        // DIRECTORY_SERVER("directoryServer"),
        // not available in the 7.0
        
        // EMAIL_ALIAS("emailAlias"),
        // not available in the 7.0
        
        // FREEBUSY_ADDRESS("freeBusyAddress"),
        // not available in the 7.0

        
        
        jsonData.element(JsonContactModel.ITEM.getValue(), jsonItem);
        jsonRoot.element(JsonContactModel.DATA.getValue(), jsonData);

        return jsonRoot.toString();
    } 
      
          /**
     * 
     * @param sifc
     * @return
     * @throws java.lang.Exception
     */
  
    
     public String contact2vcard(Contact contact) throws ConversionException {
        String vcard = null;
        try {
            ContactToVcard c2vc = new ContactToVcard(FakeDevice.getTimezone(), FakeDevice.getCharset());
            vcard = c2vc.convert(contact);
        } catch (Exception e) {
            throw new ConversionException("Error converting Contact to VCARD. ", e);
        }
        return vcard;
    }
     
        /**
     * 
     * @param vcard
     * @return
     * @throws java.lang.Exception
     */
   public String toRFC(String jsonContentKey, String jsonContentValue, String key,String state, String contentType) {
        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        jsonData.elementOpt(JsonContactModel.CONTENT_TYPE.getValue(), contentType);

        jsonItem.elementOpt(JsonContactModel.KEY.getValue(), key);
        jsonItem.elementOpt(JsonContactModel.STATE.getValue(), state);
        
        jsonItem.elementOpt(jsonContentKey,jsonContentValue);
        
        jsonData.element(JsonContactModel.ITEM.getValue(), jsonItem);
        jsonRoot.element(JsonContactModel.DATA.getValue(), jsonData);

        return jsonRoot.toString();
    }

    

}
