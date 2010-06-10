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
package com.funambol.json.converter;

import java.util.ArrayList;

import junit.framework.TestCase;

import net.sf.json.test.JSONAssert;
import net.sf.json.JSONObject;

import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.contact.BusinessDetail;
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.contact.Email;
import com.funambol.common.pim.contact.Name;
import com.funambol.common.pim.contact.Note;
import com.funambol.common.pim.contact.PersonalDetail;
import com.funambol.common.pim.contact.Phone;
import com.funambol.common.pim.contact.Photo;
import com.funambol.common.pim.contact.SIFC;
import com.funambol.common.pim.contact.Title;
import com.funambol.common.pim.contact.WebPage;

import com.funambol.json.domain.JsonContactModel;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.util.TestUtility;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Test cases for contact converters.
 *
 * @version $Id$
 */
public class ContactConverterTest extends TestCase {

    ContactConverter converter = null;

    // ------------------------------------------------------------ Constructors
    public ContactConverterTest(String testName) {
        super(testName);
    }

    // ------------------------------------------------------- Protected methods
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        converter = new ContactConverter();
    }

    // -------------------------------------------------------------- Test cases
    public void testToJSONFromJSON() {

        StringBuilder expected = new StringBuilder("{\"data\":{");
        expected.append("\"content-type\":\"application/json-card\",")
            .append("\"item\":{\"key\":\"0\",\"state\":\"A\",")
            .append("\"folder\":\"folder\",\"title\":\"salut\",")
            .append("\"firstName\":\"Alexander\",\"middleName\":\"Tom\",")
            .append("\"lastName\":\"Parker\",\"suffix\":\"Mr\",")
            .append("\"nickName\":\"Alex\",\"company\":\"company srl\",")
            .append("\"jobTitle\":\"jobTitle\",")
            .append("\"imAddress\":\"imemail@fun.it4\",")
            .append("\"email\":\"email1@fun.it1\",")
            .append("\"email2\":\"email2@fun.it2\",")
            .append("\"email3\":\"email3@fun.it3\",")
            .append("\"body\":\"body of the note\",")
            .append("\"url\":\"www.webpage.com\",")
            .append("\"businessAddressStreet\":\"Street 10th\",")
            .append("\"businessAddressCity\":\"Las Vegas\",")
            .append("\"businessAddressCountry\":\"LV\",")
            .append("\"businessAddressState\":\"USA\",")
            .append("\"businessAddressPostalCode\":\"67890\",")
            .append("\"businessAddressPostOfficeBox\":\"11-PX\",")
            .append("\"businessAddressExtendedAddress\":\"extended business\",")
            .append("\"homeAddressStreet\":\"Central Park\",")
            .append("\"homeAddressCity\":\"New York\",")
            .append("\"homeAddressCountry\":\"NY\",")
            .append("\"homeAddressState\":\"USA\",")
            .append("\"homeAddressPostalCode\":\"12345\",")
            .append("\"homeAddressPostOfficeBox\":\"13-PX\",")
            .append("\"homeAddressExtendedAddress\":\"extended home\",")
            .append("\"otherAddressStreet\":\"Other Street 9th\",")
            .append("\"otherAddressCity\":\"Los Angeles\",")
            .append("\"otherAddressCountry\":\"USA\",")
            .append("\"otherAddressState\":\"LA\",")
            .append("\"otherAddressPostalCode\":\"54321\",")
            .append("\"otherAddressPostOfficeBox\":\"12-PX\",")
            .append("\"otherAddressExtendedAddress\":\"extended other\",")
            .append("\"phoneMobile\":\"88888888\",")
            .append("\"phoneMobileBusiness\":\"8181818181\",")
            .append("\"phoneMobileHome\":\"8282828282\",")
            .append("\"phoneBusiness\":\"11111111111\",")
            .append("\"phoneBusiness2\":\"2222222222\",")
            .append("\"phoneHome\":\"33333333333\",")
            .append("\"phoneHome2\":\"44444444\",")
            .append("\"phoneHomeFAX\":\"55555555\",")
            .append("\"phoneCompany\":\"6666666666\",")
            .append("\"phoneBusinessFAX\":\"777777777\",")
            .append("\"phoneCallback\":\"999999999\",")
            .append("\"phoneCar\":\"1010101010\",")
            .append("\"phoneAssistant\":\"12121212\",")
            .append("\"phoneOther\":\"1313131313\",")
            .append("\"phoneOtherFAX\":\"14141414\",")
            .append("\"phonePrimary\":\"15151515\",")
            .append("\"phonePager\":\"1616161616\",")
            .append("\"phoneRadio\":\"17171717\",")
            .append("\"phoneTelex\":\"18181818\",")
            .append("\"sensitivity\":0,\"department\":\"Departmet 1\",")
            .append("\"office\":\"Office A\",")
            .append("\"managerName\":\"Manager B\",")
            .append("\"assistantName\":\"Assistant C\",")
            .append("\"birthday\":\"2010-11-11\",\"spouseName\":\"Wife\",")
            .append("\"anniversary\":\"2008-11-11\"}}}");

        JsonItem<Contact> item = new JsonItem<Contact>();
        item.setContentType("type");
        item.setKey("0");
        item.setState("A");

        Contact contact = new Contact();
        
        Name name = new Name();
        name.setSalutation(new Property("salut"));
        name.setFirstName(new Property("Alexander"));
        name.setMiddleName(new Property("Tom"));
        name.setLastName(new Property("Parker"));
        name.setSuffix(new Property("Mr"));
        name.setNickname(new Property("Alex"));
        contact.setName(name);
        contact.setFolder("folder");
        
        PersonalDetail pd = new PersonalDetail();
        pd.getAddress().getCity().setPropertyValue("New York");
        pd.getAddress().getCountry().setPropertyValue("NY");
        pd.getAddress().getState().setPropertyValue("USA");
        pd.getAddress().getStreet().setPropertyValue("Central Park");
        pd.getAddress().getPostalCode().setPropertyValue("12345");
        pd.getAddress().getPostOfficeAddress().setPropertyValue("13-PX");
        pd.getAddress().getExtendedAddress().setPropertyValue("extended home");

        pd.getOtherAddress().getStreet().setPropertyValue("Other Street 9th");
        pd.getOtherAddress().getCity().setPropertyValue("Los Angeles");
        pd.getOtherAddress().getState().setPropertyValue("LA");
        pd.getOtherAddress().getCountry().setPropertyValue("USA");
        pd.getOtherAddress().getPostalCode().setPropertyValue("54321");
        pd.getOtherAddress().getPostOfficeAddress().setPropertyValue("12-PX");
        pd.getOtherAddress().getExtendedAddress().setPropertyValue("extended other");

        contact.setPersonalDetail(pd);
        
        BusinessDetail bd = new BusinessDetail();
        bd.getAddress().getCity().setPropertyValue("Las Vegas");
        bd.getAddress().getCountry().setPropertyValue("LV");
        bd.getAddress().getState().setPropertyValue("USA");
        bd.getAddress().getStreet().setPropertyValue("Street 10th");
        bd.getAddress().getPostalCode().setPropertyValue("67890");
        bd.getAddress().getPostOfficeAddress().setPropertyValue("11-PX");
        bd.getAddress().getExtendedAddress().setPropertyValue("extended business");

        bd.getCompany().setPropertyValue("company srl");

        contact.setBusinessDetail(bd);
       
        contact.setNotes(new ArrayList<Note>(1));
        Note note = new Note();
        note.setNoteType(SIFC.BODY);
        note.setPropertyValue("body of the note");
        contact.getNotes().add(note);

        WebPage wp = new WebPage("www.webpage.com");
        wp.setWebPageType(SIFC.WEB_PAGE);
        contact.getPersonalDetail().getWebPages().add(wp);

        contact.getBusinessDetail().setTitles(new ArrayList(1));
        Title title=new Title();
        title.setPropertyType(SIFC.JOB_TITLE);
        title.setPropertyValue("jobTitle");
        contact.getBusinessDetail().addTitle(title);
        
        Email emailp4 = new Email(); 
        emailp4.setEmailType(SIFC.INSTANT_MESSENGER);
        emailp4.setPropertyValue("imemail@fun.it4");
        contact.getPersonalDetail().addEmail(emailp4);

        Email emailp1 = new Email(); 
        emailp1.setEmailType(SIFC.EMAIL1_ADDRESS);
        emailp1.setPropertyValue("email1@fun.it1");
        contact.getPersonalDetail().addEmail(emailp1);
        
        Email emailp2 = new Email(); 
        emailp2.setEmailType(SIFC.EMAIL2_ADDRESS);
        emailp2.setPropertyValue("email2@fun.it2");
        contact.getPersonalDetail().addEmail(emailp2);
        
        contact.getPersonalDetail()
               .getWebPages().add(new WebPage("http://www.otherwebpage.it"));
        
        Email emailp3 = new Email(); 
        emailp3.setEmailType(SIFC.EMAIL3_ADDRESS);
        emailp3.setPropertyValue("email3@fun.it3");
        contact.getBusinessDetail().addEmail(emailp3);

        // phones
        contact.getBusinessDetail().setPhones(new ArrayList<Phone>());
                
        Phone pn = new Phone("11111111111");
        pn.setPhoneType(SIFC.BUSINESS_TELEPHONE_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);
        
        pn = new Phone("2222222222");
        pn.setPhoneType(SIFC.BUSINESS2_TELEPHONE_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);
        
        pn = new Phone("33333333333");
        pn.setPhoneType(SIFC.HOME_TELEPHONE_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);

        pn = new Phone("44444444");
        pn.setPhoneType(SIFC.HOME2_TELEPHONE_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);

        pn = new Phone("55555555");
        pn.setPhoneType(SIFC.HOME_FAX_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);

        pn = new Phone("6666666666");
        pn.setPhoneType(SIFC.COMPANY_MAIN_TELEPHONE_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);

        pn = new Phone("777777777");
        pn.setPhoneType(SIFC.BUSINESS_FAX_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);

        pn = new Phone("88888888");
        pn.setPhoneType(SIFC.MOBILE_TELEPHONE_NUMBER);
        contact.getPersonalDetail().getPhones().add(pn);

        pn = new Phone("8181818181");
        pn.setPhoneType(SIFC.MOBILE_BUSINESS_TELEPHONE_NUMBER);
        contact.getPersonalDetail().getPhones().add(pn);

        pn = new Phone("8282828282");
        pn.setPhoneType(SIFC.MOBILE_HOME_TELEPHONE_NUMBER);
        contact.getPersonalDetail().getPhones().add(pn);

        pn = new Phone("999999999");
        pn.setPhoneType(SIFC.CALLBACK_TELEPHONE_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);

        pn = new Phone("1010101010");
        pn.setPhoneType(SIFC.CAR_TELEPHONE_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);
        
        pn = new Phone("12121212");
        pn.setPhoneType(SIFC.ASSISTANT_TELEPHONE_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);
                        
        pn = new Phone("1313131313");
        pn.setPhoneType(SIFC.OTHER_TELEPHONE_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);

        pn = new Phone("14141414");
        pn.setPhoneType(SIFC.OTHER_FAX_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);
        
        pn = new Phone("15151515");
        pn.setPhoneType(SIFC.PRIMARY_TELEPHONE_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);
        
        pn = new Phone("1616161616");
        pn.setPhoneType(SIFC.PAGER_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);
        
        pn = new Phone("17171717");
        pn.setPhoneType(SIFC.RADIO_TELEPHONE_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);
        
        pn = new Phone("18181818");
        pn.setPhoneType(SIFC.TELEX_NUMBER);
        contact.getBusinessDetail().getPhones().add(pn);

        contact.setSensitivity(new Short("0"));
        
        contact.getBusinessDetail().getDepartment().setPropertyValue("Departmet 1");
        contact.getBusinessDetail().setOfficeLocation("Office A");
        contact.getBusinessDetail().setManager("Manager B");
        contact.getBusinessDetail().setAssistant("Assistant C");

        contact.getPersonalDetail().setBirthday("2010-11-11");
        contact.getPersonalDetail().setSpouse("Wife");
        contact.getPersonalDetail().setAnniversary("2008-11-11");
        
        item.setItem(contact);

        String jsonResult = converter.toJSON(item);

        JSONObject o1, o2;

        o1 = JSONObject.fromObject(expected.toString()).getJSONObject("data").getJSONObject("item");
        o2 = JSONObject.fromObject(jsonResult).getJSONObject("data").getJSONObject("item");
        JSONAssert.assertEquals(o1, o2);

        JsonItem<Contact> contactItem2 = converter.fromJSON(jsonResult);
        String jsonResult2 = converter.toJSON(contactItem2);

        o1 = JSONObject.fromObject(expected.toString()).getJSONObject("data").getJSONObject("item");
        o2 = JSONObject.fromObject(jsonResult2).getJSONObject("data").getJSONObject("item");
        JSONAssert.assertEquals(o1, o2);
    }

    public void testVCardToJSONFromJSON() throws Exception {
        StringBuilder vcard = new StringBuilder("BEGIN:VCARD\n")
            .append("VERSION:2.1\n")
            .append("REV:20100316T090000Z\n")
            .append("N:Parker;Lucy;;;\n")
            .append("ORG:organization;\n")
            .append("X-CLASS:private\n")
            .append("TEL;CELL;HOME:123456789\n")
            .append("TEL;CELL:234567890\n")
            .append("TEL;CELL;WORK:345678901\n")
            .append("TEL;VOICE:987654321\n")
            .append("TEL:876543210\n")
            .append("END:VCARD\n");

        StringBuilder expected = new StringBuilder("{\"data\":");
        expected.append("{\"content-type\":\"application/json-card\",")
            .append("\"item\":{\"key\":\"0\",\"state\":\"A\",")
            .append("\"title\":\"\",\"firstName\":\"Lucy\",")
            .append("\"middleName\":\"\",\"lastName\":\"Parker\",")
            .append("\"suffix\":\"\",\"company\":\"organization\",")
            .append("\"phoneMobileHome\":\"123456789\",")
            .append("\"phoneMobile\":\"234567890\",")
            .append("\"phoneOther\":\"987654321\",")
            .append("\"phoneMobileBusiness\":\"345678901\",")
            .append("\"department\":\"\"}}}");
        
        Contact contact = TestUtility.vcard2Contact(vcard.toString());

        JsonItem<Contact> contactItem = new JsonItem<Contact>();
        contactItem.setContentType("type");
        contactItem.setKey("0");
        contactItem.setState("A");
        contactItem.setItem(contact);

        String result = converter.toJSON(contactItem);

        JSONObject o1, o2;

        o1 = JSONObject.fromObject(expected.toString()).getJSONObject("data").getJSONObject("item");
        o2 = JSONObject.fromObject(result).getJSONObject("data").getJSONObject("item");

        TestUtility.JSONAssert_assertEquals(o1, o2);
    }

    public void testSifCToJSONFromJSON() throws Exception {
        StringBuilder sifc =
            new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sifc.append("<contact>")
            .append("<SIFVersion>1.1</SIFVersion>")
            .append("<Folder>DEFAULT_FOLDER</Folder>")
            .append("<FileAs>Test one</FileAs>")
            .append("<Title>Mr.</Title>")
            .append("<FirstName>Test</FirstName>")
            .append("<LastName>one</LastName>")
            .append("<Subject>Test business cell</Subject>")
            .append("<JobTitle>Job Title</JobTitle>")
            .append("<BusinessFaxNumber>123456789</BusinessFaxNumber>")
            .append("<BusinessTelephoneNumber>223345</BusinessTelephoneNumber>")
            .append("<Business2TelephoneNumber>3456</Business2TelephoneNumber>")
            .append("<Home2TelephoneNumber>987654321</Home2TelephoneNumber>")
            .append("<HomeFaxNumber>765321</HomeFaxNumber>")
            .append("<HomeTelephoneNumber>98765433</HomeTelephoneNumber>")
            .append("<MobileBusinessTelephoneNumber>2356879</MobileBusinessTelephoneNumber>")
            .append("<MobileTelephoneNumber>1144779900</MobileTelephoneNumber>")
            .append("<OtherFaxNumber>3456789</OtherFaxNumber>")
            .append("<OtherTelephoneNumber>34567974</OtherTelephoneNumber>")
            .append("</contact>");

        StringBuilder expected = new StringBuilder("{\"data\":{");
        expected.append("\"content-type\":\"application/json-card\",")
            .append("\"item\":{\"key\":\"0\",\"state\":\"A\",")
            .append("\"folder\":\"DEFAULT_FOLDER\",")
            .append("\"displayName\":\"Test one\",\"title\":\"Mr.\",")
            .append("\"firstName\":\"Test\",\"lastName\":\"one\",")
            .append("\"subject\":\"Test business cell\",")
            .append("\"jobTitle\":\"Job Title\",")
            .append("\"phoneHome2\":\"987654321\",\"phoneHomeFAX\":\"765321\",")
            .append("\"phoneHome\":\"98765433\",")
            .append("\"phoneMobile\":\"1144779900\",")
            .append("\"phoneOtherFAX\":\"3456789\",")
            .append("\"phoneOther\":\"34567974\",")
            .append("\"phoneBusinessFAX\":\"123456789\",")
            .append("\"phoneBusiness\":\"223345\",")
            .append("\"phoneBusiness2\":\"3456\",")
            .append("\"phoneMobileBusiness\":\"2356879\"}}}");

        Contact contact = TestUtility.sif2Contact(sifc.toString());

        JsonItem<Contact> contactItem = new JsonItem<Contact>();
        contactItem.setContentType("type");
        contactItem.setKey("0");
        contactItem.setState("A");
        contactItem.setItem(contact);

        String result = converter.toJSON(contactItem);

        JSONObject o1, o2;

        o1 = JSONObject.fromObject(expected.toString()).getJSONObject("data").getJSONObject("item");
        o2 = JSONObject.fromObject(result).getJSONObject("data").getJSONObject("item");
        TestUtility.JSONAssert_assertEquals(o1, o2);

        StringBuilder expected2 = new StringBuilder("{\"data\":{");
        expected2.append("\"content-type\":\"application/json-card\",")
            .append("\"item\":{\"key\":\"0\",\"state\":\"A\",")
            .append("\"folder\":\"DEFAULT_FOLDER\",")
            .append("\"displayName\":\"Test one\",\"title\":\"Mr.\",")
            .append("\"firstName\":\"Test\",\"lastName\":\"one\",")
            .append("\"subject\":\"Test business cell\",")
            .append("\"jobTitle\":\"Job Title\",")
            .append("\"phoneMobile\":\"1144779900\",")
            .append("\"phoneMobileBusiness\":\"2356879\",")
            .append("\"phoneBusiness\":\"223345\",\"phoneBusiness2\":\"3456\",")
            .append("\"phoneHome\":\"98765433\",")
            .append("\"phoneHome2\":\"987654321\",")
            .append("\"phoneHomeFAX\":\"765321\",")
            .append("\"phoneBusinessFAX\":\"123456789\",")
            .append("\"phoneOther\":\"34567974\",")
            .append("\"phoneOtherFAX\":\"3456789\"}}}");

        JsonItem<Contact> contactItem2 = converter.fromJSON(result);
        String jsonResult2 = converter.toJSON(contactItem2);

        o1 = JSONObject.fromObject(expected2.toString()).getJSONObject("data").getJSONObject("item");
        o2 = JSONObject.fromObject(jsonResult2).getJSONObject("data").getJSONObject("item");
        TestUtility.JSONAssert_assertEquals(o1, o2);
    }
    
    /**
     * this test converts a contactItem to jsonVcard format then back to contactitem,
     * and finally to jsonvcard again,the jsoncards must be equal
     *
     * @throws java.lang.Exception
     */
    public void test_Vcard2Json_Addresses() throws Exception {

        String content =
                "BEGIN:VCARD\n"+
                "VERSION:2.1\n"+
                "REV:20090121T122455Z\n"+
                "N:Caffrey (MOURA);Kelly;;;\n"+
                "ORG:MOURA;\n"+
                "X-CLASS:private\n"+
                "PHOTO:\n"+

                "EMAIL;INTERNET;WORK;ENCODING=QUOTED-PRINTABLE:k.caffrey=40moura.ru\n"+
                "EMAIL;INTERNET:\n"+
                "EMAIL;INTERNET;HOME:\n"+

                //        |PoBox|Extended |  Street             | city  |state| zip   |country
                "ADR;WORK:px-166;bbbbbbbbb;6544 Battleford Drive;Raleigh;NC;27613-3502;U.S.A.\n"+
                "ADR;HOME:;;;;;;\n"+
                "ADR:;;;;;;\n"+

                "TEL;CELL;HOME:\n"+
                "TEL;CELL:\n"+
                "TEL;VOICE;HOME:\n"+
                "TEL;VOICE;WORK:\n"+
                "TEL;CELL;WORK:\n"+
                "TEL;VOICE;CAR:\n"+
                "TEL;FAX;WORK:\n"+
                "TEL;VOICE;PREF:\n"+
                "TEL;WORK;PREF:\n"+
                "TEL;FAX:\n"+
                "TEL;VOICE:\n"+
                "TEL;FAX;HOME:\n"+
                "TEL:\n"+
                "TEL;PAGER:\n"+

                "URL:\n"+
                "ROLE:\n"+
                "NOTE:\n"+
                "BDAY:\n"+
                "TITLE:\n"+
                "URL;HOME:\n"+
                "NICKNAME:\n"+
                "END:VCARD\n";
        
        Contact contact = TestUtility.vcard2Contact(content);

        JsonItem<Contact> contactItem = new JsonItem<Contact>();
        contactItem.setContentType("type");
        contactItem.setKey("0");
        contactItem.setState("A");
        contactItem.setItem(contact);

        String result1 = converter.toJSON(contactItem);
        JSONObject jsonRoot = JSONObject.fromObject(result1);
        JSONObject jsonData = jsonRoot.getJSONObject("data");
        JSONObject jsonItem = jsonData.getJSONObject("item");

        assertEquals("bbbbbbbbb", jsonItem.optString("businessAddressExtendedAddress"));

    }

    /**
     * this unit test verifies:
     *  - full name: title
     *  - full name: middle_name 
     *  - full name: suffix
     * 
     *  - phone number: ISDN 
     *  - phone number: TTY/TDD  
     * 
     *  - Web address 
     *  - IM address
     * 
     *
     * @throws java.lang.Exception
     */
    public void test_SIF_ContactWithMinorFields() throws Exception {
        
        StringBuilder content =
            new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.append("<contact>")
                .append("<SIFVersion>1.1</SIFVersion>")
                .append("<Folder>DEFAULT_FOLDER</Folder>")
                .append("<FileAs>Rossi, Giovanni Middle</FileAs>")
                .append("<Title>Mr.</Title>")
                .append("<FirstName>Giovanni</FirstName>")
                .append("<MiddleName>Middle</MiddleName>")
                .append("<LastName>Rossi</LastName>")
                .append("<Suffix>II</Suffix>")
                .append("<Subject>Giovanni Middle Rossi II</Subject>")
                .append("<Initials>C.M.S.</Initials>")
                .append("<JobTitle>Job Title</JobTitle>")
                .append("<TelexNumber>888888888</TelexNumber>")
                .append("<WebPage>http://www.prova.it</WebPage>")
                .append("<IMAddress>im@email.com</IMAddress>")
                .append("<Email1Address>mail@mail.it</Email1Address>")
                .append("<Email1AddressType>SMTP</Email1AddressType>")
                .append("<Email2Address/>")
                .append("<Email2AddressType/>")
                .append("<Email3Address/>")
                .append("<Email3AddressType/>")
                .append("<Anniversary/>")
                .append("<AssistantName/>")
                .append("<ManagerName/>")
                .append("<AssistantTelephoneNumber/>")
                .append("<BillingInformation/>")
                .append("<Birthday/>")
                .append("<Body/>")
                .append("<Business2TelephoneNumber/>")
                .append("<BusinessAddressCity/>")
                .append("<BusinessAddressCountry/>")
                .append("<BusinessAddressPostOfficeBox/>")
                .append("<BusinessAddressPostalCode/>")
                .append("<BusinessAddressState/>")
                .append("<BusinessAddressStreet/>")
                .append("<BusinessFaxNumber/>")
                .append("<BusinessTelephoneNumber/>")
                .append("<Categories/>")
                .append("<Children/>")
                .append("<Companies/>")
                .append("<CompanyName/>")
                .append("<Department/>")
                .append("<Gender>0</Gender>")
                .append("<Hobby/>")
                .append("<CallbackTelephoneNumber/>")
                .append("<CarTelephoneNumber/>")
                .append("<CompanyMainTelephoneNumber/>")
                .append("<Home2TelephoneNumber/>")
                .append("<HomeAddressCity/>")
                .append("<HomeAddressCountry/>")
                .append("<HomeAddressPostOfficeBox/>")
                .append("<HomeAddressPostalCode/>")
                .append("<HomeAddressState/>")
                .append("<HomeAddressStreet/>")
                .append("<HomeFaxNumber/>")
                .append("<HomeTelephoneNumber/>")
                .append("<HomeWebPage/>")
                .append("<Importance>1</Importance>")
                .append("<Language/>")
                .append("<Mileage/>")
                .append("<MobileTelephoneNumber/>")
                .append("<NickName/>")
                .append("<OfficeLocation/>")
                .append("<OrganizationalIDNumber/>")
                .append("<OtherAddressCity/>")
                .append("<OtherAddressCountry/>")
                .append("<OtherAddressPostOfficeBox/>")
                .append("<OtherAddressPostalCode/>")
                .append("<OtherAddressState/>")
                .append("<OtherAddressStreet/>")
                .append("<OtherFaxNumber/>")
                .append("<OtherTelephoneNumber/>")
                .append("<PagerNumber/>")
                .append("<Photo/>")
                .append("<PrimaryTelephoneNumber/>")
                .append("<Profession/>")
                .append("<RadioTelephoneNumber/>")
                .append("<Sensitivity>0</Sensitivity>")
                .append("<Spouse/>")
                .append("<YomiCompanyName/>")
                .append("<YomiFirstName/>")
                .append("<YomiLastName/>")
                .append("</contact>");

        Contact contact = TestUtility.sif2Contact(content.toString());

        JsonItem<Contact> contactItem = new JsonItem<Contact>();
        contactItem.setContentType("type");
        contactItem.setKey("0");
        contactItem.setState("A");
        contactItem.setItem(contact);
        
        String result1 = converter.toJSON(contactItem);

        JSONObject jsonRoot = JSONObject.fromObject(result1);
        JSONObject jsonData = jsonRoot.getJSONObject("data");
        JSONObject jsonItem = jsonData.getJSONObject("item");
        
        assertEquals("Mr.", jsonItem.optString("title"));
        assertEquals("Giovanni", jsonItem.optString("firstName"));      
        assertEquals("Middle", jsonItem.optString("middleName"));        
        assertEquals("Rossi", jsonItem.optString("lastName"));           
        assertEquals("II", jsonItem.optString("suffix"));            
        assertEquals("Job Title", jsonItem.optString("jobTitle"));
        assertEquals("http://www.prova.it", jsonItem.optString("url"));        
        assertEquals("im@email.com", jsonItem.optString("imAddress"));            
        
        JsonItem<Contact> contactItem2 = converter.fromJSON(result1);
        String result2 = converter.toJSON(contactItem2);

        StringBuilder expected = new StringBuilder("{\"data\":")
            .append("{\"content-type\":\"application/json-card\",\"item\":{")
            .append("\"key\":\"0\",\"state\":\"A\",")
            .append("\"folder\":\"DEFAULT_FOLDER\",")
            .append("\"displayName\":\"Rossi, Giovanni Middle\",")
            .append("\"title\":\"Mr.\",\"firstName\":\"Giovanni\",")
            .append("\"middleName\":\"Middle\",\"lastName\":\"Rossi\",")
            .append("\"suffix\":\"II\",\"subject\":\"Giovanni Middle Rossi II\",")
            .append("\"nickName\":\"\",\"company\":\"\",")
            .append("\"categories\":\"\",\"children\":\"\",\"companies\":\"\",")
            .append("\"jobTitle\":\"Job Title\",\"imAddress\":\"im@email.com\",")
            .append("\"gender\":\"0\",\"hobbies\":\"\",")
            .append("\"photo\":\"\",")
            .append("\"email\":\"mail@mail.it\",\"email2\":\"\",")
            .append("\"email3\":\"\",\"body\":\"\",")
            .append("\"url\":\"http://www.prova.it\",\"urlHome\":\"\",")
            .append("\"businessAddressStreet\":\"\",")
            .append("\"businessAddressCity\":\"\",")
            .append("\"businessAddressCountry\":\"\",")
            .append("\"businessAddressState\":\"\",")
            .append("\"businessAddressPostalCode\":\"\",")
            .append("\"businessAddressPostOfficeBox\":\"\",")
            .append("\"homeAddressStreet\":\"\",\"homeAddressCity\":\"\",")
            .append("\"homeAddressCountry\":\"\",\"homeAddressState\":\"\",")
            .append("\"homeAddressPostalCode\":\"\",")
            .append("\"homeAddressPostOfficeBox\":\"\",")
            .append("\"otherAddressStreet\":\"\",")
            .append("\"otherAddressCity\":\"\",")
            .append("\"otherAddressCountry\":\"\",")
            .append("\"otherAddressState\":\"\",")
            .append("\"otherAddressPostalCode\":\"\",")
            .append("\"otherAddressPostOfficeBox\":\"\",")
            .append("\"phoneMobile\":\"\",\"phoneBusiness\":\"\",")
            .append("\"phoneBusiness2\":\"\",\"phoneHome\":\"\",")
            .append("\"phoneHome2\":\"\",\"phoneHomeFAX\":\"\",")
            .append("\"phoneCompany\":\"\",\"phoneBusinessFAX\":\"\",")
            .append("\"phoneCallback\":\"\",\"phoneCar\":\"\",")
            .append("\"phoneAssistant\":\"\",\"phoneOther\":\"\",")
            .append("\"phoneOtherFAX\":\"\",\"phonePrimary\":\"\",")
            .append("\"phonePager\":\"\",\"phoneRadio\":\"\",")
            .append("\"phoneTelex\":\"888888888\",\"sensitivity\":0,")
            .append("\"importance\":1,\"language\":\"\",\"mileage\":\"\",")
            .append("\"department\":\"\",\"office\":\"\",\"managerName\":\"\",")
            .append("\"assistantName\":\"\",\"birthday\":\"\",")
            .append("\"profession\":\"\",\"spouseName\":\"\",\"anniversary\":\"\"}}}");

        JSONObject o1, o2;

        o1 = JSONObject.fromObject(expected.toString()).getJSONObject("data").getJSONObject("item");
        o2 = JSONObject.fromObject(result2).getJSONObject("data").getJSONObject("item");
        TestUtility.JSONAssert_assertEquals(o1, o2);
    }  

    /**
     * this unit test verifies:
     *  - full name: title
     *  - full name: middle_name
     *  - full name: suffix
     *
     *  - phone number: ISDN
     *  - phone number: TTY/TDD
     *
     *  - Web address
     *  - IM address
     *
     *
     * @throws java.lang.Exception
     */
    public void test_SIF_Addresses() throws Exception {

        String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                "<contact>"+
                "<SIFVersion>1.1</SIFVersion>"+

                "<Folder>DEFAULT_FOLDER</Folder>"+
                "<FileAs>Rossi, Giovanni Middle</FileAs>"+
                "<Title>Mr.</Title>"+
                "<FirstName>Giovanni</FirstName>"+
                "<MiddleName>Middle</MiddleName>"+
                "<LastName>Rossi</LastName>"+
                "<Suffix>II</Suffix>"+
                "<Subject>Giovanni Middle Rossi II</Subject>"+
                "<Initials>C.M.S.</Initials>"+
                "<JobTitle>Job Title</JobTitle>"+
                "<TelexNumber>888888888</TelexNumber>"+
                "<WebPage>http://www.prova.it</WebPage>"+
                "<IMAddress>im@email.com</IMAddress>"+
                "<Email1Address>mail@mail.it</Email1Address>"+
                "<Email1AddressType>SMTP</Email1AddressType>"+
                "<Email2Address/>"+
                "<Email2AddressType/>"+
                "<Email3Address/>"+
                "<Email3AddressType/>"+
                "<Anniversary/>"+
                "<AssistantName/>"+
                "<ManagerName/>"+
                "<AssistantTelephoneNumber/>"+
                "<BillingInformation/>"+
                "<Birthday/>"+
                "<Body/>"+

                "<BusinessAddressCity>B1</BusinessAddressCity>"+
                "<BusinessAddressCountry>B2</BusinessAddressCountry>"+
                "<BusinessAddressPostOfficeBox>B3</BusinessAddressPostOfficeBox>"+
                "<BusinessAddressPostalCode>B4</BusinessAddressPostalCode>"+
                "<BusinessAddressState>B5</BusinessAddressState>"+
                "<BusinessAddressStreet>B6</BusinessAddressStreet>"+
                "<HomeAddressCity>H1</HomeAddressCity>"+
                "<HomeAddressCountry>H2</HomeAddressCountry>"+
                "<HomeAddressPostOfficeBox>H3</HomeAddressPostOfficeBox>"+
                "<HomeAddressPostalCode>H4</HomeAddressPostalCode>"+
                "<HomeAddressState>H5</HomeAddressState>"+
                "<HomeAddressStreet>H6</HomeAddressStreet>"+
                "<OtherAddressCity>O1</OtherAddressCity>"+
                "<OtherAddressCountry>O2</OtherAddressCountry>"+
                "<OtherAddressPostOfficeBox>O3</OtherAddressPostOfficeBox>"+
                "<OtherAddressPostalCode>O4</OtherAddressPostalCode>"+
                "<OtherAddressState>O5</OtherAddressState>"+
                "<OtherAddressStreet>O6</OtherAddressStreet>"+


                "<BusinessFaxNumber/>"+
                "<BusinessTelephoneNumber/>"+
                "<Business2TelephoneNumber/>"+
                "<Categories/>"+
                "<Children/>"+
                "<Companies/>"+
                "<CompanyName/>"+
                "<Department/>"+
                "<Gender>0</Gender>"+
                "<Hobby/>"+
                "<CallbackTelephoneNumber/>"+
                "<CarTelephoneNumber/>"+
                "<CompanyMainTelephoneNumber/>"+
                "<Home2TelephoneNumber/>"+
                "<HomeFaxNumber/>"+
                "<HomeTelephoneNumber/>"+
                "<HomeWebPage/>"+
                "<Importance>1</Importance>"+
                "<Language/>"+
                "<Mileage/>"+
                "<MobileTelephoneNumber/>"+
                "<NickName/>"+
                "<OfficeLocation/>"+
                "<OrganizationalIDNumber/>"+
                "<OtherFaxNumber/>"+
                "<OtherTelephoneNumber/>"+
                "<PagerNumber/>"+
                "<Photo/>"+
                "<PrimaryTelephoneNumber/>"+
                "<Profession/>"+
                "<RadioTelephoneNumber/>"+
                "<Sensitivity>0</Sensitivity>"+
                "<Spouse/>"+
                "<YomiCompanyName/>"+
                "<YomiFirstName/>"+
                "<YomiLastName/>"+
                "</contact>";

        Contact contact = TestUtility.sif2Contact(content);

        JsonItem<Contact> contactItem = new JsonItem<Contact>();
        contactItem.setContentType("type");
        contactItem.setKey("0");
        contactItem.setState("A");
        contactItem.setItem(contact);

        String result1 = converter.toJSON(contactItem);
        JSONObject jsonRoot = JSONObject.fromObject(result1);
        JSONObject jsonData = jsonRoot.getJSONObject("data");
        JSONObject jsonItem = jsonData.getJSONObject("item");

        assertEquals("B3", jsonItem.optString("businessAddressPostOfficeBox"));
        assertEquals("H3", jsonItem.optString("homeAddressPostOfficeBox"));
        assertEquals("O3", jsonItem.optString("otherAddressPostOfficeBox"));

        JsonItem<Contact> contactItem2 = converter.fromJSON(result1);
        String result2 = converter.toJSON(contactItem2);

        String expected = "{\"data\":{\"content-type\":\"application/json-card\",\"item\":{" +
                "\"key\":\"0\",\"state\":\"A\",\"folder\":\"DEFAULT_FOLDER\"," +
                "\"displayName\":\"Rossi, Giovanni Middle\"," +
                "\"title\":\"Mr.\",\"firstName\":\"Giovanni\"," +
                "\"middleName\":\"Middle\",\"lastName\":\"Rossi\"," +
                "\"suffix\":\"II\",\"subject\":\"Giovanni Middle Rossi II\"," +
                "\"categories\":\"\",\"children\":\"\",\"companies\":\"\"," +
                "\"nickName\":\"\",\"company\":\"\",\"jobTitle\":\"Job Title\"," +
                "\"gender\":\"0\",\"hobbies\":\"\"," +
                "\"photo\":\"\"," +
                "\"imAddress\":\"im@email.com\",\"email\":\"mail@mail.it\",\"email2\":\"\"," +
                "\"email3\":\"\",\"body\":\"\",\"url\":\"http://www.prova.it\",\"urlHome\":\"\"," +

                "\"businessAddressStreet\":\"B6\"," +
                "\"businessAddressCity\":\"B1\"," +
                "\"businessAddressCountry\":\"B2\"," +
                "\"businessAddressState\":\"B5\"," +
                "\"businessAddressPostalCode\":\"B4\"," +
                "\"businessAddressPostOfficeBox\":\"B3\"," +

                "\"homeAddressStreet\":\"H6\"," +
                "\"homeAddressCity\":\"H1\"," +
                "\"homeAddressCountry\":\"H2\"," +
                "\"homeAddressState\":\"H5\"," +
                "\"homeAddressPostalCode\":\"H4\"," +
                "\"homeAddressPostOfficeBox\":\"H3\"," +

                "\"otherAddressStreet\":\"O6\"," +
                "\"otherAddressCity\":\"O1\"," +
                "\"otherAddressCountry\":\"O2\"," +
                "\"otherAddressState\":\"O5\"," +
                "\"otherAddressPostalCode\":\"O4\"," +
                "\"otherAddressPostOfficeBox\":\"O3\"," +

                "\"phoneMobile\":\"\",\"phoneBusiness\":\"\",\"phoneBusiness2\":\"\",\"phoneHome\":\"\"," +
                "\"phoneHome2\":\"\",\"phoneHomeFAX\":\"\",\"phoneCompany\":\"\",\"phoneBusinessFAX\":\"\"," +
                "\"phoneCallback\":\"\",\"phoneCar\":\"\",\"phoneAssistant\":\"\"," +
                "\"phoneOther\":\"\",\"phoneOtherFAX\":\"\",\"phonePrimary\":\"\"," +
                "\"phonePager\":\"\",\"phoneRadio\":\"\",\"phoneTelex\":\"888888888\",\"sensitivity\":0," +
                "\"importance\":1,\"language\":\"\",\"mileage\":\"\"," +
                "\"department\":\"\",\"office\":\"\",\"managerName\":\"\",\"assistantName\":\"\",\"birthday\":\"\"," +
                "\"profession\":\"\",\"spouseName\":\"\",\"anniversary\":\"\"}}}";

        JSONObject o1, o2;

        o1 = JSONObject.fromObject(expected).getJSONObject("data").getJSONObject("item");
        o2 = JSONObject.fromObject(result2).getJSONObject("data").getJSONObject("item");
        TestUtility.JSONAssert_assertEquals(o1, o2);
    }

    /**
     * this unit test verifies the temporary patch because
     * the Synclet don't add the EMAIL field in the vcard
     *
     *
     * @throws java.lang.Exception
     */
    public void test_Vcard2Json_ContactWithoutEmail() throws Exception {

        // after synclet
        StringBuilder content = new StringBuilder("BEGIN:VCARD\n")
            .append("VERSION:2.1\n")
            .append("REV:20090121T122455Z\n")
            .append("N:Caffrey (Mistermind);Kelly;;;\n")
            .append("ORG:Mistermind;\n")
            .append("X-CLASS:private\n")
            .append("EMAIL;INTERNET;WORK;ENCODING=QUOTED-PRINTABLE:k.caffrey=40mistermind.com\n")
            .append("PHOTO:\n");

            // the synclet for nokia n95 doesn't set this field
            //"EMAIL;INTERNET:\n"
        content.append("EMAIL;INTERNET;HOME:\n")
               .append("TEL;CELL;HOME:\n")
               .append("TEL;CELL:\n")
               .append("TEL;VOICE;HOME:\n")
               .append("URL:\n")
               .append("TEL;VOICE;WORK:\n")
               .append("TEL;CELL;WORK:\n")
               .append("TEL;VOICE;CAR:\n")
               .append("ROLE:\n")
               .append("NOTE:\n")
               .append("TEL;FAX;WORK:\n")
               .append("ADR;HOME:;;;;;;\n")
               .append("ADR:;;;;;;\n")
               .append("BDAY:\n")
               .append("TITLE:\n")
               .append("TEL:\n")
               .append("TEL;PAGER:\n")
               .append("URL;HOME:\n")
               .append("TEL;VOICE;PREF:\n")
               .append("ADR;WORK:;;;;;;\n")
               .append("NICKNAME:\n")
               .append("TEL;WORK;PREF:\n")
               .append("TEL;FAX:\n")
               .append("TEL;VOICE:\n")
               .append("TEL;FAX;HOME:\n")
               .append("END:VCARD\n");

        Contact contact = TestUtility.vcard2Contact(content.toString());

        JsonItem<Contact> contactItem = new JsonItem<Contact>();
        contactItem.setContentType("type");
        contactItem.setKey("0");
        contactItem.setState("A");
        contactItem.setItem(contact);

        String result = converter.toJSON(contactItem);
        
        StringBuilder expected = new StringBuilder("{\"data\"");
        expected.append(":{\"content-type\":\"application/json-card\",")
                .append("\"item\":{")
                .append("\"key\":\"0\",\"state\":\"A\",")
                .append("\"title\":\"\",")
                .append("\"firstName\":\"Kelly\",\"middleName\":\"\",")
                .append("\"lastName\":\"Caffrey (Mistermind)\",\"suffix\":\"\",")
                .append("\"nickName\":\"\",")
                .append("\"company\":\"Mistermind\",")
                .append("\"jobTitle\":\"\",")
                .append("\"email2\":\"\",")
                .append("\"email3\":\"k.caffrey@mistermind.com\",")
                .append("\"body\":\"\",")
                .append("\"photo\":\"\",")
                .append("\"url\":\"\",\"urlHome\":\"\",")
                .append("\"businessAddressStreet\":\"\",")
                .append("\"businessAddressCity\":\"\",")
                .append("\"businessAddressCountry\":\"\",")
                .append("\"businessAddressState\":\"\",")
                .append("\"businessAddressPostalCode\":\"\",")
                .append("\"businessAddressPostOfficeBox\":\"\",")
                .append("\"businessAddressExtendedAddress\":\"\",")
                .append("\"homeAddressStreet\":\"\",")
                .append("\"homeAddressCity\":\"\",")
                .append("\"homeAddressCountry\":\"\",")
                .append("\"homeAddressState\":\"\",")
                .append("\"homeAddressPostalCode\":\"\",")
                .append("\"homeAddressPostOfficeBox\":\"\",")
                .append("\"homeAddressExtendedAddress\":\"\",")
                .append("\"otherAddressStreet\":\"\",")
                .append("\"otherAddressCity\":\"\",")
                .append("\"otherAddressCountry\":\"\",")
                .append("\"otherAddressState\":\"\",")
                .append("\"otherAddressPostalCode\":\"\",")
                .append("\"otherAddressPostOfficeBox\":\"\",")
                .append("\"otherAddressExtendedAddress\":\"\",")
                .append("\"phoneMobileHome\":\"\",")
                .append("\"phoneMobile\":\"\",")
                .append("\"phoneHome\":\"\",")
                .append("\"phoneCar\":\"\",\"phoneOther\":\"\",")
                .append("\"phonePrimary\":\"\",\"phoneOtherFAX\":\"\",")
                .append("\"phoneHomeFAX\":\"\",\"phoneBusiness\":\"\",")
                .append("\"phoneMobileBusiness\":\"\",")
                .append("\"phoneBusinessFAX\":\"\",\"phonePager\":\"\",")
                .append("\"phoneCompany\":\"\",\"department\":\"\",")
                .append("\"birthday\":\"\",\"profession\":\"\"}}}");

        JSONObject o1, o2;

        o1 = JSONObject.fromObject(expected.toString()).getJSONObject("data").getJSONObject("item");
        o2 = JSONObject.fromObject(result).getJSONObject("data").getJSONObject("item");
        TestUtility.JSONAssert_assertEquals(o1, o2);
    }

    /**
     * this unit test verifies the temporary patch because
     * the Synclet don't add the EMAIL field in the vcard
     *
     *
     * @throws java.lang.Exception
     */
    public void test_Vcard2Json_FN_null() throws Exception {

        // before synclet
        /*
        BEGIN:VCARD
        VERSION:2.1
        REV:20090128T173150Z
        N:null;null;;;
        ADR;HOME:;;;La Granja;;;
        ADR;WORK:;;;Central Middle School;;W1J 8BA;
        ADR:;;;White Cemetery;;;
        BDAY:19960530
        TEL;CELL:07528428424
        EMAIL;INTERNET;ENCODING=QUOTED-PRINTABLE:firstname.lastname=40someplace.com21
        URL:www.keycriteria.com28
        TEL;VOICE;WORK:02030730096
        TEL;FAX;WORK:02030730093
        END:VCARD
        */

        // after synclet
        String content =
                "BEGIN:VCARD\n"+
                "VERSION:2.1\n"+
                "REV:20090128T173150Z\n"+
                "N:null;null;;;\n"+
                "ADR;HOME:;;;La Granja;;;\n"+
                "ADR;WORK:;;;Central Middle School;;W1J 8BA;\n"+
                "ADR:;;;White Cemetery;;;\n"+
                "BDAY:19960530\n"+
                "TEL;CELL:07528428424\n"+
                "EMAIL;INTERNET;ENCODING=QUOTED-PRINTABLE:firstname.lastname=40someplace.com21\n"+
                "URL:www.keycriteria.com28\n"+
                "TEL;VOICE;WORK:02030730096\n"+
                "TEL;FAX;WORK:02030730093\n"+
                "PHOTO:\n"+
                "EMAIL;INTERNET;HOME:\n"+
                "TEL;CELL;HOME:\n"+
                "TEL;VOICE;HOME:\n"+
                "ORG:\n"+
                "TEL;CELL;WORK:\n"+
                "TEL;VOICE;CAR:\n"+
                "ROLE:\n"+
                "NOTE:\n"+
                "TITLE:\n"+
                "TEL;PAGER:\n"+
                "URL;HOME:\n"+
                "TEL;VOICE;PREF:\n"+
                "EMAIL;INTERNET;WORK:\n"+
                "NICKNAME:\n"+
                "TEL;WORK;PREF:\n"+
                "TEL;FAX;HOME:\n"+
                "END:VCARD\n";

        Contact contact = TestUtility.vcard2Contact(content);

        JsonItem<Contact> contactItem = new JsonItem<Contact>();
        contactItem.setContentType("type");
        contactItem.setKey("0");
        contactItem.setState("A");
        contactItem.setItem(contact);

        String result1 = converter.toJSON(contactItem);

        JSONObject jsonRoot = JSONObject.fromObject(result1);
        JSONObject jsonData = jsonRoot.getJSONObject("data");
        JSONObject jsonItem = jsonData.getJSONObject("item");

        String resultFN = jsonItem.optString("firstName");
        String resultLN = jsonItem.optString("lastName");

        assertEquals("", resultFN);
        assertEquals("", resultLN);

    }

   /**
    * Tests the case where a backend sends only some of the fields in the json,
    * in this case the vcard returned should include only the fields that came 
    * in the json.
    * 
    * @throws java.lang.Exception
    */
    public void test_emptyFieldReturnedToClient() throws Exception {
        StringBuilder json = new StringBuilder("{\"data\": {");
        json.append("\"content-type\": \"application/json-card\",")
            .append("\"item\":  {\"key\": \"0\",\"state\": \"A\",")
            .append("\"folder\": \"\", \"title\": \"\",")
            .append("\"email\":\"\", \"firstName\":\"coisa22\"}}}");

        StringBuilder expected = new StringBuilder("BEGIN:VCARD\n");
        expected.append("VERSION:2.1\n")
                .append("N:;coisa22;;;\n")
                .append("EMAIL;INTERNET:\n")
                .append("X-FUNAMBOL-FOLDER:\n")
                .append("END:VCARD\n");

        JsonItem item = converter.fromJSON(json.toString());

        Contact contact = (Contact) item.getItem();
        String result = TestUtility.contact2vcard(contact);

//        System.out.println(expected.toString());
//        System.out.println(result.replaceAll("\\r", ""));
        assertEquals(expected.toString(), result.replaceAll("\\r", ""));
    }
    
    /**
     * Tests if the converter handles correctly a json item without folder.
     *
     * @throws java.lang.Exception
     */
    public void test_noFolderInJson() throws Exception {

        StringBuilder json = new StringBuilder("{\"data\":{\"item\":{");
        json.append("\"firstName\":\"Wulli\",\"lastName\":\"Schloch\",")
            .append("\"email\":\"mehleins@dbdmedia.de\",\"url\":\"http:////\",")
            .append("\"email2\":\"mehlmehr@dbdmedia.de\",\"title\":\"Herr\"},")
            .append("\"content-type\":\"application/json-card\"}}");

        StringBuilder expected = new StringBuilder("BEGIN:VCARD\n");
        expected.append("VERSION:2.1\n")
                .append("N:Schloch;Wulli;;Herr;\n")
                .append("EMAIL;INTERNET:mehleins@dbdmedia.de\n")
                .append("EMAIL;INTERNET;HOME:mehlmehr@dbdmedia.de\n")
                .append("URL:http:////\n")
                .append("END:VCARD\n");

        JsonItem item = converter.fromJSON(json.toString());

        Contact contact = (Contact) item.getItem();
        String result = TestUtility.contact2vcard(contact);
        assertEquals(expected.toString(), result.replaceAll("\\r", ""));
    }

    /**
     * Tests a Contact conversion to JSON.
     *
     * @throws Exception
     */
    public void test_ContactToJSON() throws Exception {

        String jsoncard = "{\"data\":{"
            + "\"content-type\":\"application/json-card\","
            + "\"item\":{"
            + "\"key\":\"0\","
            + "\"state\":\"A\","
            + "\"title\":\"Mr.\","
            + "\"firstName\":\"Mario\","
            + "\"middleName\":\"De\","
            + "\"lastName\":\"Rossi\","
            + "\"displayName\":\"Mr. Mario De Rossi III\","
            + "\"suffix\":\"III\","
            + "\"homeAddressPostOfficeBox\":\"\","
            + "\"homeAddressStreet\":\"c/o Palazzo del Quirinale\","
            + "\"homeAddressCity\":\"Roma\","
            + "\"homeAddressState\":\"\","
            + "\"homeAddressPostalCode\":\"00187\","
            + "\"homeAddressCountry\":\"Italy\","
            + "\"homeAddressLabel\":\"c/o Palazzo del Quirinale\\nVia del Quirinale\\nRoma\\n00187\\nItaly\","
            + "\"homeAddressExtendedAddress\":\"Via del Quirinale\","
            + "\"otherAddressPostOfficeBox\":\"\","
            + "\"otherAddressStreet\":\"Ponte Vecchio\","
            + "\"otherAddressCity\":\"Firenze\","
            + "\"otherAddressState\":\"\","
            + "\"otherAddressPostalCode\":\"50122\","
            + "\"otherAddressCountry\":\"Italy\","
            + "\"otherAddressLabel\":\"Ponte Vecchio\\nLungarno degli Archibusieri\\nFirenze\\n50122\\nItaly\","
            + "\"otherAddressExtendedAddress\":\"Lungarno degli Archibusieri\","
            + "\"photo\":\"R0lGODdhIAAgAHcAACwAAAAAIAAgAIcAAAAAADMAAGYAAJkAAMwAAP8AMwA"
            + "AMzMAM2YAM5kAM8wAM/8AZgAAZjMAZmYAZpkAZswAZv8AmQAAmTMAmWYAmZkAmcwAmf8"
            + "AzAAAzDMAzGYAzJkAzMwAzP8A/wAA/zMA/2YA/5kA/8wA//8zAAAzADMzAGYzAJkzAMw"
            + "zAP8zMwAzMzMzM2YzM5kzM8wzM/8zZgAzZjMzZmYzZpkzZswzZv8zmQAzmTMzmWYzmZk"
            + "zmcwzmf8zzAAzzDMzzGYzzJkzzMwzzP8z/wAz/zMz/2Yz/5kz/8wz//9mAABmADNmAGZ"
            + "mAJlmAMxmAP9mMwBmMzNmM2ZmM5lmM8xmM/9mZgBmZjNmZmZmZplmZsxmZv9mmQBmmTN"
            + "mmWZmmZlmmcxmmf9mzABmzDNmzGZmzJlmzMxmzP9m/wBm/zNm/2Zm/5lm/8xm//+ZAAC"
            + "ZADOZAGaZAJmZAMyZAP+ZMwCZMzOZM2aZM5mZM8yZM/+ZZgCZZjOZZmaZZpmZZsyZZv+"
            + "ZmQCZmTOZmWaZmZmZmcyZmf+ZzACZzDOZzGaZzJmZzMyZzP+Z/wCZ/zOZ/2aZ/5mZ/8y"
            + "Z///MAADMADPMAGbMAJnMAMzMAP/MMwDMMzPMM2bMM5nMM8zMM//MZgDMZjPMZmbMZpn"
            + "MZszMZv/MmQDMmTPMmWbMmZnMmczMmf/MzADMzDPMzGbMzJnMzMzMzP/M/wDM/zPM/2b"
            + "M/5nM/8zM////AAD/ADP/AGb/AJn/AMz/AP//MwD/MzP/M2b/M5n/M8z/M///ZgD/ZjP"
            + "/Zmb/Zpn/Zsz/Zv//mQD/mTP/mWb/mZn/mcz/mf//zAD/zDP/zGb/zJn/zMz/zP///wD"
            + "//zP//2b//5n//8z///8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD//wDqNCz"
            + "qNCwAC9D//wD//wD//wD//wD//wD//wD//wD//wD//wD//wD//wD//wD//wD//wAAAAA"
            + "AAAAAAAAAAAAAAAAAAAAAAAD//wD//wD//wD//wD//wAItACvCRxIUCCAgwgPFlzIcCB"
            + "CaRAjQkTYsOFBiRgxKqzoEEDGjxE3VvQIsuTEkSZTSgPAkKRKkywJXnwJM6ZBkDMl5tT"
            + "oEOfDkD8zxnSpMehKozpvfkyoE2nIa0SL7mTqsyTViU6B1hQatSjNr0fB0ty5UmzZphr"
            + "NRiV7dqxQqELd8oTLdetbuku7sn2q1OfVpTK7qrXZ1yzfgoK/EkZseCVHqIl9PjYYuej"
            + "kwIkpXm6ZUDPHgAA7\","
            + "\"photoType\":\"GIF\","
            + "\"children\":\"Giuseppe,Maria\","
            + "\"gender\":\"Male\","
            + "\"hobbies\":\"Music,Sport\","
            + "\"birthday\":\"1975-01-01\","
            + "\"businessAddressPostOfficeBox\":\"\","
            + "\"businessAddressStreet\":\"Piazza del Duomo\","
            + "\"businessAddressCity\":\"Milano\","
            + "\"businessAddressState\":\"\","
            + "\"businessAddressPostalCode\":\"20122\","
            + "\"businessAddressCountry\":\"Italy\","
            + "\"businessAddressLabel\":\"Piazza del Duomo\\nMilano\\n20122\\nItaly\","
            + "\"businessAddressExtendedAddress\":\"\","
            + "\"profession\":\"Developer\","
            + "\"jobTitle\":\"Senior Developer\","
            + "\"company\":\"Sample Company\","
            + "\"department\":\"Factory\","
            + "\"companies\":\"Demo Company,Foo Bars,ABC Telecom\","
            + "\"phoneHome\":\"+39021234567\","
            + "\"phoneBusiness\":\"+39027654321\","
            + "\"email\":\"mario.derossi@home.it\","
            + "\"urlHome\":\"www.marioderossi.info\","
            + "\"urlWork\":\"www.sample-company.com\","
            + "\"categories\":\"Birthday,Business\","
            + "\"assistantName\":\"Anna Bianchi\","
            + "\"phoneAssistant\":\"+39029999999\","
            + "\"importance\":5"
            + "}}}";

        Contact contact = new Contact();

        Name name = contact.getName();
        name.setSalutation(new Property("Mr."));
        name.setFirstName(new Property("Mario"));
        name.setMiddleName(new Property("De"));
        name.setLastName(new Property("Rossi"));
        name.setSuffix(new Property("III"));
        name.setDisplayName(new Property("Mr. Mario De Rossi III"));

        PersonalDetail pd = contact.getPersonalDetail();

        pd.getAddress().getStreet().setPropertyValue("c/o Palazzo del Quirinale");
        pd.getAddress().getCity().setPropertyValue("Roma");
        pd.getAddress().getCountry().setPropertyValue("Italy");
        pd.getAddress().getState().setPropertyValue("");
        pd.getAddress().getPostalCode().setPropertyValue("00187");
        pd.getAddress().getPostOfficeAddress().setPropertyValue("");
        pd.getAddress().getExtendedAddress().setPropertyValue("Via del Quirinale");
        pd.getAddress().getLabel().setPropertyValue("c/o Palazzo del Quirinale\nVia del Quirinale\nRoma\n00187\nItaly");

        pd.getOtherAddress().getStreet().setPropertyValue("Ponte Vecchio");
        pd.getOtherAddress().getCity().setPropertyValue("Firenze");
        pd.getOtherAddress().getCountry().setPropertyValue("Italy");
        pd.getOtherAddress().getState().setPropertyValue("");
        pd.getOtherAddress().getPostalCode().setPropertyValue("50122");
        pd.getOtherAddress().getPostOfficeAddress().setPropertyValue("");
        pd.getOtherAddress().getExtendedAddress().setPropertyValue("Lungarno degli Archibusieri");
        pd.getOtherAddress().getLabel().setPropertyValue("Ponte Vecchio\nLungarno degli Archibusieri\nFirenze\n50122\nItaly");

        FileInputStream fi = new FileInputStream("src/test/resources/smile.gif");
        byte[] image = new byte[fi.available()];
        fi.read(image);
        fi.close();
        Photo photo = new Photo("GIF", null, image);
        pd.setPhotoObject(photo);

        pd.setChildren("Giuseppe,Maria");
        pd.setBirthday("1975-01-01");
        pd.setGender("Male");
        pd.setHobbies("Music,Sport");

        BusinessDetail bd = contact.getBusinessDetail();

        bd.getAddress().getStreet().setPropertyValue("Piazza del Duomo");
        bd.getAddress().getCity().setPropertyValue("Milano");
        bd.getAddress().getCountry().setPropertyValue("Italy");
        bd.getAddress().getState().setPropertyValue("");
        bd.getAddress().getPostalCode().setPropertyValue("20122");
        bd.getAddress().getPostOfficeAddress().setPropertyValue("");
        bd.getAddress().getExtendedAddress().setPropertyValue("");
        bd.getAddress().getLabel().setPropertyValue("Piazza del Duomo\nMilano\n20122\nItaly");

        bd.getCompany().setPropertyValue("Sample Company");
        bd.getDepartment().setPropertyValue("Factory");
        bd.getRole().setPropertyValue("Developer");

        Title title = new Title();
        title.setPropertyType(SIFC.JOB_TITLE);
        title.setPropertyValue("Senior Developer");
        bd.addTitle(title);

        bd.setCompanies("Demo Company,Foo Bars,ABC Telecom");

        Phone phone1 = new Phone("+39021234567");
        phone1.setPhoneType(SIFC.HOME_TELEPHONE_NUMBER);
        pd.addPhone(phone1);

        Phone phone2 = new Phone("+39027654321");
        phone2.setPhoneType(SIFC.BUSINESS_TELEPHONE_NUMBER);
        bd.addPhone(phone2);

        Email email1 = new Email("mario.derossi@home.it");
        email1.setEmailType(SIFC.EMAIL1_ADDRESS);
        pd.addEmail(email1);

        WebPage url1 = new WebPage("www.marioderossi.info");
        url1.setWebPageType(SIFC.HOME_WEB_PAGE);
        pd.addWebPage(url1);

        WebPage url2 = new WebPage("www.sample-company.com");
        url2.setWebPageType(SIFC.BUSINESS_WEB_PAGE);
        bd.addWebPage(url2);

        contact.setCategories(new Property("Birthday,Business"));

        bd.setAssistant("Anna Bianchi");

        Phone phone3 = new Phone("+39029999999");
        phone3.setPhoneType(SIFC.ASSISTANT_TELEPHONE_NUMBER);
        bd.addPhone(phone3);

        contact.setImportance((short)5);

        // convert to json
        JsonItem<Contact> contactItem = new JsonItem<Contact>();
        contactItem.setContentType("type");
        contactItem.setKey("0");
        contactItem.setState("A");
        contactItem.setItem(contact);
        String actual = converter.toJSON(contactItem);

        TestUtility.JSONAssert_assertEquals(JSONObject.fromObject(jsoncard),
                                            JSONObject.fromObject(actual));
    }

    /**
     * Tests a JSON conversion to Contact.
     *
     * @throws Exception
     */
    public void test_JSONToContact() throws Exception {

        String jsoncard = "{\"data\":{"
            + "\"content-type\":\"application/json-card\","
            + "\"item\":{"
            + "\"key\":\"0\","
            + "\"state\":\"A\","
            + "\"folder\":\"\","
            + "\"title\":\"Mr.\","
            + "\"firstName\":\"Mario\","
            + "\"middleName\":\"De\","
            + "\"lastName\":\"Rossi\","
            + "\"displayName\":\"Mr. Mario De Rossi III\","
            + "\"suffix\":\"III\","
            + "\"homeAddressPostOfficeBox\":\"\","
            + "\"homeAddressStreet\":\"c/o Palazzo del Quirinale\","
            + "\"homeAddressCity\":\"Roma\","
            + "\"homeAddressState\":\"\","
            + "\"homeAddressPostalCode\":\"00187\","
            + "\"homeAddressCountry\":\"Italy\","
            + "\"homeAddressLabel\":\"c/o Palazzo del Quirinale\\nVia del Quirinale\\nRoma\\n00187\\nItaly\","
            + "\"homeAddressExtendedAddress\":\"Via del Quirinale\","
            + "\"otherAddressPostOfficeBox\":\"\","
            + "\"otherAddressStreet\":\"Ponte Vecchio\","
            + "\"otherAddressCity\":\"Firenze\","
            + "\"otherAddressState\":\"\","
            + "\"otherAddressPostalCode\":\"50122\","
            + "\"otherAddressCountry\":\"Italy\","
            + "\"otherAddressLabel\":\"Ponte Vecchio\\nLungarno degli Archibusieri\\nFirenze\\n50122\\nItaly\","
            + "\"otherAddressExtendedAddress\":\"Lungarno degli Archibusieri\","
            + "\"photo\":\"R0lGODdhIAAgAHcAACwAAAAAIAAgAIcAAAAAADMAAGYAAJkAAMwAAP8AMwA"
            + "AMzMAM2YAM5kAM8wAM/8AZgAAZjMAZmYAZpkAZswAZv8AmQAAmTMAmWYAmZkAmcwAmf8"
            + "AzAAAzDMAzGYAzJkAzMwAzP8A/wAA/zMA/2YA/5kA/8wA//8zAAAzADMzAGYzAJkzAMw"
            + "zAP8zMwAzMzMzM2YzM5kzM8wzM/8zZgAzZjMzZmYzZpkzZswzZv8zmQAzmTMzmWYzmZk"
            + "zmcwzmf8zzAAzzDMzzGYzzJkzzMwzzP8z/wAz/zMz/2Yz/5kz/8wz//9mAABmADNmAGZ"
            + "mAJlmAMxmAP9mMwBmMzNmM2ZmM5lmM8xmM/9mZgBmZjNmZmZmZplmZsxmZv9mmQBmmTN"
            + "mmWZmmZlmmcxmmf9mzABmzDNmzGZmzJlmzMxmzP9m/wBm/zNm/2Zm/5lm/8xm//+ZAAC"
            + "ZADOZAGaZAJmZAMyZAP+ZMwCZMzOZM2aZM5mZM8yZM/+ZZgCZZjOZZmaZZpmZZsyZZv+"
            + "ZmQCZmTOZmWaZmZmZmcyZmf+ZzACZzDOZzGaZzJmZzMyZzP+Z/wCZ/zOZ/2aZ/5mZ/8y"
            + "Z///MAADMADPMAGbMAJnMAMzMAP/MMwDMMzPMM2bMM5nMM8zMM//MZgDMZjPMZmbMZpn"
            + "MZszMZv/MmQDMmTPMmWbMmZnMmczMmf/MzADMzDPMzGbMzJnMzMzMzP/M/wDM/zPM/2b"
            + "M/5nM/8zM////AAD/ADP/AGb/AJn/AMz/AP//MwD/MzP/M2b/M5n/M8z/M///ZgD/ZjP"
            + "/Zmb/Zpn/Zsz/Zv//mQD/mTP/mWb/mZn/mcz/mf//zAD/zDP/zGb/zJn/zMz/zP///wD"
            + "//zP//2b//5n//8z///8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD//wDqNCz"
            + "qNCwAC9D//wD//wD//wD//wD//wD//wD//wD//wD//wD//wD//wD//wD//wD//wAAAAA"
            + "AAAAAAAAAAAAAAAAAAAAAAAD//wD//wD//wD//wD//wAItACvCRxIUCCAgwgPFlzIcCB"
            + "CaRAjQkTYsOFBiRgxKqzoEEDGjxE3VvQIsuTEkSZTSgPAkKRKkywJXnwJM6ZBkDMl5tT"
            + "oEOfDkD8zxnSpMehKozpvfkyoE2nIa0SL7mTqsyTViU6B1hQatSjNr0fB0ty5UmzZphr"
            + "NRiV7dqxQqELd8oTLdetbuku7sn2q1OfVpTK7qrXZ1yzfgoK/EkZseCVHqIl9PjYYuej"
            + "kwIkpXm6ZUDPHgAA7\","
            + "\"photoType\":\"GIF\","
            + "\"children\":\"Giuseppe,Maria\","
            + "\"gender\":\"Male\","
            + "\"hobbies\":\"Music,Sport\","
            + "\"birthday\":\"1975-01-01\","
            + "\"businessAddressPostOfficeBox\":\"\","
            + "\"businessAddressStreet\":\"Piazza del Duomo\","
            + "\"businessAddressCity\":\"Milano\","
            + "\"businessAddressState\":\"\","
            + "\"businessAddressPostalCode\":\"20122\","
            + "\"businessAddressCountry\":\"Italy\","
            + "\"businessAddressLabel\":\"Piazza del Duomo\\nMilano\\n20122\\nItaly\","
            + "\"businessAddressExtendedAddress\":\"\","
            + "\"profession\":\"Developer\","
            + "\"jobTitle\":\"Senior Developer\","
            + "\"company\":\"Sample Company\","
            + "\"department\":\"Factory\","
            + "\"companies\":\"Demo Company,Foo Bars,ABC Telecom\","
            + "\"phoneHome\":\"+39021234567\","
            + "\"phoneBusiness\":\"+39027654321\","
            + "\"email\":\"mario.derossi@home.it\","
            + "\"urlHome\":\"www.marioderossi.info\","
            + "\"urlWork\":\"www.sample-company.com\","
            + "\"categories\":\"Birthday,Business\","
            + "\"assistantName\":\"Anna Bianchi\","
            + "\"phoneAssistant\":\"+39029999999\","
            + "\"importance\":5"
            + "}}}";

        // convert from json
        JsonItem<Contact> contactItem = converter.fromJSON(jsoncard);
        
        Contact contact = contactItem.getItem();

        assertEquals(contactItem.getContentType(), "application/json-card");
        assertEquals(contactItem.getKey(), "0");
        assertEquals(contactItem.getState(), "A");

        Name name = contact.getName();
        assertEquals(name.getSalutation().getPropertyValueAsString(), "Mr.");
        assertEquals(name.getFirstName().getPropertyValueAsString(), "Mario");
        assertEquals(name.getMiddleName().getPropertyValueAsString(), "De");
        assertEquals(name.getLastName().getPropertyValueAsString(), "Rossi");
        assertEquals(name.getSuffix().getPropertyValueAsString(), "III");
        assertEquals(name.getDisplayName().getPropertyValueAsString(), "Mr. Mario De Rossi III");

        PersonalDetail pd = contact.getPersonalDetail();

        assertEquals(pd.getAddress().getStreet().getPropertyValueAsString(), "c/o Palazzo del Quirinale");
        assertEquals(pd.getAddress().getCity().getPropertyValueAsString(), "Roma");
        assertEquals(pd.getAddress().getCountry().getPropertyValueAsString(), "Italy");
        assertEquals(pd.getAddress().getState().getPropertyValueAsString(), "");
        assertEquals(pd.getAddress().getPostalCode().getPropertyValueAsString(), "00187");
        assertEquals(pd.getAddress().getPostOfficeAddress().getPropertyValueAsString(), "");
        assertEquals(pd.getAddress().getExtendedAddress().getPropertyValueAsString(), "Via del Quirinale");
        assertEquals(pd.getAddress().getLabel().getPropertyValueAsString(), "c/o Palazzo del Quirinale\nVia del Quirinale\nRoma\n00187\nItaly");

        assertEquals(pd.getOtherAddress().getStreet().getPropertyValueAsString(), "Ponte Vecchio");
        assertEquals(pd.getOtherAddress().getCity().getPropertyValueAsString(), "Firenze");
        assertEquals(pd.getOtherAddress().getCountry().getPropertyValueAsString(), "Italy");
        assertEquals(pd.getOtherAddress().getState().getPropertyValueAsString(), "");
        assertEquals(pd.getOtherAddress().getPostalCode().getPropertyValueAsString(), "50122");
        assertEquals(pd.getOtherAddress().getPostOfficeAddress().getPropertyValueAsString(), "");
        assertEquals(pd.getOtherAddress().getExtendedAddress().getPropertyValueAsString(), "Lungarno degli Archibusieri");
        assertEquals(pd.getOtherAddress().getLabel().getPropertyValueAsString(), "Ponte Vecchio\nLungarno degli Archibusieri\nFirenze\n50122\nItaly");

        FileInputStream fi = new FileInputStream("src/test/resources/smile.gif");
        byte[] image = new byte[fi.available()];
        fi.read(image);
        fi.close();
        Photo photo = pd.getPhotoObject();
        assertEquals(photo.getType(), "GIF");
        assertEquals(photo.getImage().length, image.length);
        for (int i = 0; i < image.length; ++i)
            assertEquals(photo.getImage()[i], image[i]);

        assertEquals(pd.getChildren(), "Giuseppe,Maria");
        assertEquals(pd.getBirthday(), "1975-01-01");
        assertEquals(pd.getGender(), "Male");
        assertEquals(pd.getHobbies(), "Music,Sport");

        BusinessDetail bd = contact.getBusinessDetail();

        assertEquals(bd.getAddress().getStreet().getPropertyValueAsString(), "Piazza del Duomo");
        assertEquals(bd.getAddress().getCity().getPropertyValueAsString(), "Milano");
        assertEquals(bd.getAddress().getCountry().getPropertyValueAsString(), "Italy");
        assertEquals(bd.getAddress().getState().getPropertyValueAsString(), "");
        assertEquals(bd.getAddress().getPostalCode().getPropertyValueAsString(), "20122");
        assertEquals(bd.getAddress().getPostOfficeAddress().getPropertyValueAsString(), "");
        assertEquals(bd.getAddress().getExtendedAddress().getPropertyValueAsString(), "");
        assertEquals(bd.getAddress().getLabel().getPropertyValueAsString(), "Piazza del Duomo\nMilano\n20122\nItaly");

        assertEquals(bd.getCompany().getPropertyValueAsString(), "Sample Company");
        assertEquals(bd.getDepartment().getPropertyValueAsString(), "Factory");
        assertEquals(bd.getRole().getPropertyValueAsString(), "Developer");

        Title title = null;
        for (int i = 0; i < bd.getTitles().size(); ++i) {
            Title item = (Title) bd.getTitles().get(i);
            if (item.getTitleType().equals(SIFC.JOB_TITLE)) {
                title = item;
                break;
            }
        }
        assertNotNull(title);
        assertEquals(title.getPropertyValueAsString(), "Senior Developer");

        assertEquals(bd.getCompanies(), "Demo Company,Foo Bars,ABC Telecom");

        Phone phone1 = null;
        for (int i = 0; i < pd.getPhones().size(); ++i) {
            Phone item = (Phone) pd.getPhones().get(i);
            if (item.getPhoneType().equals(SIFC.HOME_TELEPHONE_NUMBER)) {
                phone1 = item;
                break;
            }
        }
        assertNotNull(phone1);
        assertEquals(phone1.getPropertyValueAsString(), "+39021234567");

        Phone phone2 = null;
        for (int i = 0; i < bd.getPhones().size(); ++i) {
            Phone item = (Phone) bd.getPhones().get(i);
            if (item.getPhoneType().equals(SIFC.BUSINESS_TELEPHONE_NUMBER)) {
                phone2 = item;
                break;
            }
        }
        assertNotNull(phone2);
        assertEquals(phone2.getPropertyValueAsString(), "+39027654321");

        Email email1 = null;
        for (int i = 0; i < pd.getEmails().size(); ++i) {
            Email item = (Email) pd.getEmails().get(i);
            if (item.getEmailType().equals(SIFC.EMAIL1_ADDRESS)) {
                email1 = item;
                break;
            }
        }
        assertNotNull(email1);
        assertEquals(email1.getPropertyValueAsString(), "mario.derossi@home.it");

        WebPage url1 = null;
        for (int i = 0; i < pd.getWebPages().size(); ++i) {
            WebPage item = (WebPage) pd.getWebPages().get(i);
            if (item.getWebPageType().equals(SIFC.HOME_WEB_PAGE)) {
                url1 = item;
                break;
            }
        }
        assertNotNull(url1);
        assertEquals(url1.getPropertyValueAsString(), "www.marioderossi.info");

        WebPage url2 = null;
        for (int i = 0; i < bd.getWebPages().size(); ++i) {
            WebPage item = (WebPage) bd.getWebPages().get(i);
            if (item.getWebPageType().equals(SIFC.BUSINESS_WEB_PAGE)) {
                url2 = item;
                break;
            }
        }
        assertNotNull(url2);
        assertEquals(url2.getPropertyValueAsString(), "www.sample-company.com");

        assertEquals(contact.getCategories().getPropertyValueAsString(), "Birthday,Business");

        assertEquals(bd.getAssistant(), "Anna Bianchi");

        Phone phone3 = null;
        for (int i = 0; i < bd.getPhones().size(); ++i) {
            Phone item = (Phone) bd.getPhones().get(i);
            if (item.getPhoneType().equals(SIFC.ASSISTANT_TELEPHONE_NUMBER)) {
                phone3 = item;
                break;
            }
        }
        assertNotNull(phone3);
        assertEquals(phone3.getPropertyValueAsString(), "+39029999999");

        assertEquals((short)contact.getImportance(), (short)5);
    }
    
     /**
    *
    * This test verifies that a vcard containing all the phone numbers
    * with the + prefix is handled properly (the + prefix isn't lost)
    * when converting it to a json object, extended format
    *
    * @throws Exception
    */

    public void test_Vcard2Json_PhoneWithPlus() throws Exception {

        String phoneBusiness        = "+11 1111 11111";
        String phoneBusiness2       = "+22 (22) 22222";
        String phoneBusinessFax     = "+333 3333 3333";
        String phoneCar             = "+44 4444 4444";
        String phoneCompany         = "+55 5555 5555";
        String phoneHome            = "+66 6666 6666";
        String phoneHome2           = "+77 7777 7777";
        String phoneHomeFax         = "+88 8888 8888";
        String phoneMobile          = "+99 9999 9999";
        String phoneMobileHome      = "+11 1111 11111";
        String phoneMobileBusiness  = "+22 (22) 2222222";
        String phoneOther           = "+333 3333 3333";
        String phoneOtherFax        = "+44 4444 4444";
        String phonePrimary         = "+55 5555 5555";
        String phonePager           = "+66 6666 6666";
        String phoneCallback        = "+77 777 77777";
        String phoneRadio           = "+8888 888 88";
        String phoneTelex           = "+99 9999 99";

        String content =
                "BEGIN:VCARD\n"+
                "VERSION:2.1\n"+
                "REV:20090121T122455Z\n"+
                "N:Caffrey (MOURA);Kelly;;;\n"+
                "ORG:MOURA;\n"+
                "X-CLASS:private\n"+
                "PHOTO:\n"+

                "EMAIL;INTERNET;WORK;ENCODING=QUOTED-PRINTABLE:k.caffrey=40moura.ru\n"+
                "EMAIL;INTERNET:\n"+
                "EMAIL;INTERNET;HOME:\n"+
                "ADR;WORK:px-166;bbbbbbbbb;6544 Battleford Drive;Raleigh;NC;27613-3502;U.S.A.\n"+
                "ADR;HOME:;;;;;;\n"+
                "ADR:;;;;;;\n"+
                "TEL;CELL;HOME:"+phoneMobileHome+"\n"+
                "TEL;CELL:"+phoneMobile+"\n"+
                "TEL;VOICE;HOME:"+phoneHome+"\n"+
                "TEL;VOICE;HOME:"+phoneHome2+"\n"+
                "TEL;VOICE;WORK:"+phoneBusiness+"\n"+
                "TEL;VOICE;WORK:"+phoneBusiness2+"\n"+
                "TEL;CELL;WORK:"+phoneMobileBusiness+"\n"+
                "TEL;VOICE;CAR:"+phoneCar+"\n"+
                "TEL;FAX;WORK:"+phoneBusinessFax+"\n"+
                "TEL;VOICE;PREF:"+phonePrimary+"\n"+
                "TEL;WORK;PREF:"+phoneCompany+"\n"+
                "TEL;FAX:"+phoneOtherFax+"\n"+
                "TEL;VOICE:"+phoneOther+"\n"+
                "TEL;FAX;HOME:"+phoneHomeFax+"\n"+
                "TEL:+qqqqqqq\n"+
                "TEL;PAGER:"+phonePager+"\n"+
                "TEL;X-FUNAMBOL-CALLBACK:"+phoneCallback+"\n"+
                "TEL;X-FUNAMBOL-RADIO:"+phoneRadio+"\n"+
                "TEL;X-FUNAMBOL-TELEX:"+phoneTelex+"\n"+
                "URL:\n"+
                "ROLE:\n"+
                "NOTE:\n"+
                "BDAY:\n"+
                "TITLE:\n"+
                "URL;HOME:\n"+
                "NICKNAME:\n"+
                "END:VCARD\n";

        Contact contact = TestUtility.vcard2Contact(content);

        JsonItem<Contact> contactItem = new JsonItem<Contact>();
        contactItem.setContentType("type");
        contactItem.setKey("0");
        contactItem.setState("A");
        contactItem.setItem(contact);

        String result1 = converter.toJSON(contactItem);
        JSONObject jsonRoot = JSONObject.fromObject(result1);
        JSONObject jsonData = jsonRoot.getJSONObject("data");
        JSONObject jsonItem = jsonData.getJSONObject("item");

        String prop = JsonContactModel.PHONE_BUSINESS.getValue();
        String value = jsonItem.optString(prop);
        assertEquals("Wrong phone business",phoneBusiness, value);

        prop = JsonContactModel.PHONE_BUSINESS_2.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone business2",phoneBusiness2, value);

        prop = JsonContactModel.PHONE_BUSINESS_FAX.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone business fax",phoneBusinessFax, value);

        prop = JsonContactModel.PHONE_CAR.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone business",phoneCar, value);

        prop = JsonContactModel.PHONE_COMPANY.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone company",phoneCompany, value);

        prop = JsonContactModel.PHONE_HOME.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone home",phoneHome, value);

        prop = JsonContactModel.PHONE_HOME_2.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone home 2",phoneHome2, value);

        prop = JsonContactModel.PHONE_HOME_FAX.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone home fax",phoneHomeFax, value);

        prop = JsonContactModel.PHONE_MOBILE.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone mobile",phoneMobile, value);

        prop = JsonContactModel.PHONE_MOBILE_HOME.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone mobile home",phoneMobileHome, value);

        prop = JsonContactModel.PHONE_MOBILE_BUSINESS.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone mobile business",phoneMobileBusiness, value);

        prop = JsonContactModel.PHONE_OTHER.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone other",phoneOther, value);

        prop = JsonContactModel.PHONE_OTHER_FAX.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone other fac",phoneOtherFax, value);

        prop = JsonContactModel.PHONE_PRIMARY.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone primary",phonePrimary, value);

        prop = JsonContactModel.PHONE_PAGER.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone pager",phonePager, value);

        prop = JsonContactModel.PHONE_CALLBACK.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone callback",phoneCallback, value);

        prop = JsonContactModel.PHONE_RADIO.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone radio",phoneRadio, value);

        prop = JsonContactModel.PHONE_TELEX.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone telex",phoneTelex, value);
    }


   /**
    *
    * This test verifies that a sif containing all the phone numbers
    * with the + prefix is handled properly (the + prefix isn't lost)
    * when converting it to a json object, extended format
    *
    * @throws Exception
    */
    public void test_Sif2Json_PhoneWithPlus() throws Exception {

        String phoneBusiness        = "+11 1111 11111";
        String phoneBusiness2       = "+22 (22) 22222";
        String phoneBusinessFax     = "+333 3333 3333";
        String phoneCar             = "+44 4444 4444";
        String phoneCompany         = "+55 5555 5555";
        String phoneHome            = "+66 6666 6666";
        String phoneHome2           = "+77 7777 7777";
        String phoneHomeFax         = "+88 8888 8888";
        String phoneMobile          = "+99 9999 9999";
        String phoneMobileHome      = "+11 1111 11111";
        String phoneMobileBusiness  = "+22 (22) 2222222";
        String phoneOther           = "+333 3333 3333";
        String phoneOtherFax        = "+44 4444 4444";
        String phonePrimary         = "+55 5555 5555";
        String phonePager           = "+66 6666 6666";
        String phoneCallback        = "+77 777 77777";
        String phoneRadio           = "+8888 888 88";
        String phoneTelex           = "+99 9999 99";

                String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                "<contact>"+
                "<SIFVersion>1.1</SIFVersion>"+

                "<Folder>DEFAULT_FOLDER</Folder>"+
                "<FileAs>Rossi, Giovanni Middle</FileAs>"+
                "<Title>Mr.</Title>"+
                "<FirstName>Giovanni</FirstName>"+
                "<MiddleName>Middle</MiddleName>"+
                "<LastName>Rossi</LastName>"+
                "<Suffix>II</Suffix>"+
                "<Subject>Giovanni Middle Rossi II</Subject>"+
                "<Initials>C.M.S.</Initials>"+
                "<JobTitle>Job Title</JobTitle>"+
                "<TelexNumber>"+phoneTelex+"</TelexNumber>"+
                "<WebPage>http://www.prova.it</WebPage>"+
                "<IMAddress>im@email.com</IMAddress>"+
                "<Email1Address>mail@mail.it</Email1Address>"+
                "<Email1AddressType>SMTP</Email1AddressType>"+
                "<Email2Address/>"+
                "<Email2AddressType/>"+
                "<Email3Address/>"+
                "<Email3AddressType/>"+
                "<Anniversary/>"+
                "<AssistantName/>"+
                "<ManagerName/>"+
                "<AssistantTelephoneNumber/>"+
                "<BillingInformation/>"+
                "<Birthday/>"+
                "<Body/>"+
                "<BusinessAddressCity>B1</BusinessAddressCity>"+
                "<BusinessAddressCountry>B2</BusinessAddressCountry>"+
                "<BusinessAddressPostOfficeBox>B3</BusinessAddressPostOfficeBox>"+
                "<BusinessAddressPostalCode>B4</BusinessAddressPostalCode>"+
                "<BusinessAddressState>B5</BusinessAddressState>"+
                "<BusinessAddressStreet>B6</BusinessAddressStreet>"+
                "<HomeAddressCity>H1</HomeAddressCity>"+
                "<HomeAddressCountry>H2</HomeAddressCountry>"+
                "<HomeAddressPostOfficeBox>H3</HomeAddressPostOfficeBox>"+
                "<HomeAddressPostalCode>H4</HomeAddressPostalCode>"+
                "<HomeAddressState>H5</HomeAddressState>"+
                "<HomeAddressStreet>H6</HomeAddressStreet>"+
                "<OtherAddressCity>O1</OtherAddressCity>"+
                "<OtherAddressCountry>O2</OtherAddressCountry>"+
                "<OtherAddressPostOfficeBox>O3</OtherAddressPostOfficeBox>"+
                "<OtherAddressPostalCode>O4</OtherAddressPostalCode>"+
                "<OtherAddressState>O5</OtherAddressState>"+
                "<OtherAddressStreet>O6</OtherAddressStreet>"+


                "<BusinessFaxNumber>"+phoneBusinessFax+"</BusinessFaxNumber>"+
                "<BusinessTelephoneNumber>"+phoneBusiness+"</BusinessTelephoneNumber>"+
                "<Business2TelephoneNumber>"+phoneBusiness2+"</Business2TelephoneNumber>"+
                "<Categories/>"+
                "<Children/>"+
                "<Companies/>"+
                "<CompanyName/>"+
                "<Department/>"+
                "<Gender>0</Gender>"+
                "<Hobby/>"+
                "<CallbackTelephoneNumber>"+phoneCallback+"</CallbackTelephoneNumber>"+
                "<CarTelephoneNumber>"+phoneCar+"</CarTelephoneNumber>"+
                "<CompanyMainTelephoneNumber>"+phoneCompany+"</CompanyMainTelephoneNumber>"+
                "<Home2TelephoneNumber>"+phoneHome2+"</Home2TelephoneNumber>"+
                "<HomeFaxNumber>"+phoneHomeFax+"</HomeFaxNumber>"+
                "<HomeTelephoneNumber>"+phoneHome+"</HomeTelephoneNumber>"+
                "<HomeWebPage/>"+
                "<Importance>1</Importance>"+
                "<Language/>"+
                "<Mileage/>"+
                "<MobileTelephoneNumber>"+phoneMobile+"</MobileTelephoneNumber>"+
                "<NickName/>"+
                "<OfficeLocation/>"+
                "<OrganizationalIDNumber/>"+
                "<OtherFaxNumber>"+phoneOtherFax+"</OtherFaxNumber>"+
                "<OtherTelephoneNumber>"+phoneOther+"</OtherTelephoneNumber>"+
                "<PagerNumber>"+phonePager+"</PagerNumber>"+
                "<Photo/>"+
                "<PrimaryTelephoneNumber>"+phonePrimary+"</PrimaryTelephoneNumber>"+
                "<Profession/>"+
                "<RadioTelephoneNumber>"+phoneRadio+"</RadioTelephoneNumber>"+
                "<Sensitivity>0</Sensitivity>"+
                "<Spouse/>"+
                "<YomiCompanyName/>"+
                "<YomiFirstName/>"+
                "<YomiLastName/>"+
                "</contact>";

        Contact contact = TestUtility.sif2Contact(content);

        JsonItem<Contact> contactItem = new JsonItem<Contact>();
        contactItem.setContentType("type");
        contactItem.setKey("0");
        contactItem.setState("A");
        contactItem.setItem(contact);

        String result1 = converter.toJSON(contactItem);
        JSONObject jsonRoot = JSONObject.fromObject(result1);
        JSONObject jsonData = jsonRoot.getJSONObject("data");
        JSONObject jsonItem = jsonData.getJSONObject("item");

        String prop = JsonContactModel.PHONE_BUSINESS.getValue();
        String value = jsonItem.optString(prop);
        assertEquals("Wrong phone business",phoneBusiness, value);

        prop = JsonContactModel.PHONE_BUSINESS_2.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone business2",phoneBusiness2, value);

        prop = JsonContactModel.PHONE_BUSINESS_FAX.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone business fax",phoneBusinessFax, value);

        prop = JsonContactModel.PHONE_CAR.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone business",phoneCar, value);

        prop = JsonContactModel.PHONE_COMPANY.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone company",phoneCompany, value);

        prop = JsonContactModel.PHONE_HOME.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone home",phoneHome, value);

        prop = JsonContactModel.PHONE_HOME_2.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone home 2",phoneHome2, value);

        prop = JsonContactModel.PHONE_HOME_FAX.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone home fax",phoneHomeFax, value);

        prop = JsonContactModel.PHONE_MOBILE.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone mobile",phoneMobile, value);

        prop = JsonContactModel.PHONE_MOBILE_HOME.getValue();
        value = jsonItem.optString(prop);
        //assertEquals("Wrong phone mobile home",phoneMobileHome, value);

        prop = JsonContactModel.PHONE_MOBILE_BUSINESS.getValue();
        value = jsonItem.optString(prop);
        //assertEquals("Wrong phone mobile business",phoneMobileBusiness, value);

        prop = JsonContactModel.PHONE_OTHER.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone other",phoneOther, value);

        prop = JsonContactModel.PHONE_OTHER_FAX.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone other fac",phoneOtherFax, value);

        prop = JsonContactModel.PHONE_PRIMARY.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone primary",phonePrimary, value);

        prop = JsonContactModel.PHONE_PAGER.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone pager",phonePager, value);

        prop = JsonContactModel.PHONE_CALLBACK.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone callback",phoneCallback, value);

        prop = JsonContactModel.PHONE_RADIO.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone radio",phoneRadio, value);

        prop = JsonContactModel.PHONE_TELEX.getValue();
        value = jsonItem.optString(prop);
        assertEquals("Wrong phone telex",phoneTelex, value);
    }



   /**
    *
    * This test verifies that a sif containing all the phone numbers
    * with the + prefix is handled properly (the + prefix isn't lost)
    * when converting it to a json object containing the VCARD
    *
    * @throws Exception
    */
    public void test_Sif2JsonRFC_PhoneWithPlus() throws Exception {

        String phoneBusiness        = "+11 1111 11111";
        String phoneBusiness2       = "+22 (22) 22222";
        String phoneBusinessFax     = "+333 3333 3333";
        String phoneCar             = "+44 4444 4444";
        String phoneCompany         = "+55 5555 5555";
        String phoneHome            = "+66 6666 6666";
        String phoneHome2           = "+77 7777 7777";
        String phoneHomeFax         = "+88 8888 8888";
        String phoneMobile          = "+99 9999 9999";
        String phoneMobileHome      = "+11 1111 11111";
        String phoneMobileBusiness  = "+22 (22) 2222222";
        String phoneOther           = "+333 3333 3333";
        String phoneOtherFax        = "+44 4444 4444";
        String phonePrimary         = "+55 5555 5555";
        String phonePager           = "+66 6666 6666";
        String phoneCallback        = "+77 777 77777";
        String phoneRadio           = "+8888 888 88";
        String phoneTelex           = "+99 9999 99";



        String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                "<contact>"+
                "<SIFVersion>1.1</SIFVersion>"+

                "<Folder>DEFAULT_FOLDER</Folder>"+
                "<FileAs>Rossi, Giovanni Middle</FileAs>"+
                "<Title>Mr.</Title>"+
                "<FirstName>Giovanni</FirstName>"+
                "<MiddleName>Middle</MiddleName>"+
                "<LastName>Rossi</LastName>"+
                "<Suffix>II</Suffix>"+
                "<Subject>Giovanni Middle Rossi II</Subject>"+
                "<Initials>C.M.S.</Initials>"+
                "<JobTitle>Job Title</JobTitle>"+
                "<TelexNumber>"+phoneTelex+"</TelexNumber>"+
                "<WebPage>http://www.prova.it</WebPage>"+
                "<IMAddress>im@email.com</IMAddress>"+
                "<Email1Address>mail@mail.it</Email1Address>"+
                "<Email1AddressType>SMTP</Email1AddressType>"+
                "<Email2Address/>"+
                "<Email2AddressType/>"+
                "<Email3Address/>"+
                "<Email3AddressType/>"+
                "<Anniversary/>"+
                "<AssistantName/>"+
                "<ManagerName/>"+
                "<AssistantTelephoneNumber/>"+
                "<BillingInformation/>"+
                "<Birthday/>"+
                "<Body/>"+
                "<BusinessAddressCity>B1</BusinessAddressCity>"+
                "<BusinessAddressCountry>B2</BusinessAddressCountry>"+
                "<BusinessAddressPostOfficeBox>B3</BusinessAddressPostOfficeBox>"+
                "<BusinessAddressPostalCode>B4</BusinessAddressPostalCode>"+
                "<BusinessAddressState>B5</BusinessAddressState>"+
                "<BusinessAddressStreet>B6</BusinessAddressStreet>"+
                "<HomeAddressCity>H1</HomeAddressCity>"+
                "<HomeAddressCountry>H2</HomeAddressCountry>"+
                "<HomeAddressPostOfficeBox>H3</HomeAddressPostOfficeBox>"+
                "<HomeAddressPostalCode>H4</HomeAddressPostalCode>"+
                "<HomeAddressState>H5</HomeAddressState>"+
                "<HomeAddressStreet>H6</HomeAddressStreet>"+
                "<OtherAddressCity>O1</OtherAddressCity>"+
                "<OtherAddressCountry>O2</OtherAddressCountry>"+
                "<OtherAddressPostOfficeBox>O3</OtherAddressPostOfficeBox>"+
                "<OtherAddressPostalCode>O4</OtherAddressPostalCode>"+
                "<OtherAddressState>O5</OtherAddressState>"+
                "<OtherAddressStreet>O6</OtherAddressStreet>"+


                "<BusinessFaxNumber>"+phoneBusinessFax+"</BusinessFaxNumber>"+
                "<BusinessTelephoneNumber>"+phoneBusiness+"</BusinessTelephoneNumber>"+
                "<Business2TelephoneNumber>"+phoneBusiness2+"</Business2TelephoneNumber>"+
                "<Categories/>"+
                "<Children/>"+
                "<Companies/>"+
                "<CompanyName/>"+
                "<Department/>"+
                "<Gender>0</Gender>"+
                "<Hobby/>"+
                "<CallbackTelephoneNumber>"+phoneCallback+"</CallbackTelephoneNumber>"+
                "<CarTelephoneNumber>"+phoneCar+"</CarTelephoneNumber>"+
                "<CompanyMainTelephoneNumber>"+phoneCompany+"</CompanyMainTelephoneNumber>"+
                "<Home2TelephoneNumber>"+phoneHome2+"</Home2TelephoneNumber>"+
                "<HomeFaxNumber>"+phoneHomeFax+"</HomeFaxNumber>"+
                "<HomeTelephoneNumber>"+phoneHome+"</HomeTelephoneNumber>"+
                "<HomeWebPage/>"+
                "<Importance>1</Importance>"+
                "<Language/>"+
                "<Mileage/>"+
                "<MobileTelephoneNumber>"+phoneMobile+"</MobileTelephoneNumber>"+
                "<NickName/>"+
                "<OfficeLocation/>"+
                "<OrganizationalIDNumber/>"+
                "<OtherFaxNumber>"+phoneOtherFax+"</OtherFaxNumber>"+
                "<OtherTelephoneNumber>"+phoneOther+"</OtherTelephoneNumber>"+
                "<PagerNumber>"+phonePager+"</PagerNumber>"+
                "<Photo/>"+
                "<PrimaryTelephoneNumber>"+phonePrimary+"</PrimaryTelephoneNumber>"+
                "<Profession/>"+
                "<RadioTelephoneNumber>"+phoneRadio+"</RadioTelephoneNumber>"+
                "<Sensitivity>0</Sensitivity>"+
                "<Spouse/>"+
                "<YomiCompanyName/>"+
                "<YomiFirstName/>"+
                "<YomiLastName/>"+
                "</contact>";

        Contact contact = TestUtility.sif2Contact(content);
        String  converted = TestUtility.contact2vcard(contact);

        String expected = "BEGIN:VCARD\n"+
                          "VERSION:2.1\n"+
                          "N:Rossi;Giovanni;Middle;Mr.;II\n"+
                          "FN:Rossi, Giovanni Middle\n"+
                          "NICKNAME:\n"+
                          "ADR;HOME:H3;;H6;H1;H5;H4;H2\n"+
                          "ADR:O3;;O6;O1;O5;O4;O2\n"+
                          "BDAY:\n"+
                          "TEL;CAR;VOICE:"+phoneCar+"\n"+
                          "TEL;VOICE;HOME:"+phoneHome2+"\n"+
                          "TEL;FAX;HOME:"+phoneHomeFax+"\n"+
                          "TEL;VOICE;HOME:"+phoneHome+"\n"+
                          "TEL;CELL:"+phoneMobile+"\n"+
                          "TEL;FAX:"+phoneOtherFax+"\n"+
                          "TEL;VOICE:"+phoneOther+"\n"+
                          "TEL;X-FUNAMBOL-RADIO:"+phoneRadio+"\n"+
                          "EMAIL;INTERNET;HOME;X-FUNAMBOL-INSTANTMESSENGER:im@email.com\n"+
                          "EMAIL;INTERNET:mail@mail.it\n"+
                          "EMAIL;INTERNET;HOME:\n"+
                          "URL:http://www.prova.it\n"+
                          "URL;HOME:\n"+
                          "X-ANNIVERSARY:\n"+
                          "X-FUNAMBOL-CHILDREN:\n"+
                          "X-SPOUSE:\n"+
                          "ADR;WORK:B3;;B6;B1;B5;B4;B2\n"+
                          "ROLE:\n"+
                          "TITLE:Job Title\n"+
                          "ORG:;;\n"+
                          "TEL;X-FUNAMBOL-TELEX:"+phoneTelex+"\n"+
                          "TEL;FAX;WORK:"+phoneBusinessFax+"\n"+
                          "TEL;VOICE;WORK:"+phoneBusiness+"\n"+
                          "TEL;VOICE;WORK:"+phoneBusiness2+"\n"+
                          "TEL;X-FUNAMBOL-CALLBACK:"+phoneCallback+"\n"+
                          "TEL;WORK;PREF:"+phoneCompany+"\n"+
                          "TEL;PAGER:"+phonePager+"\n"+
                          "TEL;PREF;VOICE:"+phonePrimary+"\n"+
                          "EMAIL;INTERNET;WORK:\n"+
                          "X-FUNAMBOL-COMPANIES:\n"+
                          "X-MANAGER:\n"+
                          "AGENT:BEGIN:VCARD\\n"+
                          "FN:\\n"+
                          "TEL;WORK:\\n" +
                          "END:VCARD\n"+
                          "NOTE:\n"+
                          "CATEGORIES:\n"+
                          "PHOTO:\n"+
                          "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\n"+
                          "X-FUNAMBOL-LANGUAGES:\n"+
                          "X-FUNAMBOL-MILEAGE:\n"+
                          "X-FUNAMBOL-SUBJECT:Giovanni Middle Rossi II\n"+
                          "CLASS:PUBLIC\n"+
                          "END:VCARD\n";





        JsonItem<String> ii = new JsonItem<String>();
        ii.setItem(converted);
        String result1 = converter.toRFC(ii);
        JSONObject jsonRoot = JSONObject.fromObject(result1);
        JSONObject jsonData = jsonRoot.getJSONObject("data");
        JSONObject jsonItem = jsonData.getJSONObject("item");

        String prop = JsonContactModel.VCARD.getValue();
        String value = jsonItem.optString(prop);
        assertEqualsVcard(expected, value);
    }

   /**
    *
    * This test verifies that a vcard containing all the phone numbers
    * with the + prefix is handled properly (the + prefix isn't lost)
    * when converting it to a json object containing the VCARD
    *
    * @throws Exception
    */


    public void test_Vcard2JsonRFC_PhoneWithPlus() throws Exception {

        String phoneBusiness        = "+11 1111 11111";
        String phoneBusiness2       = "+22 (22) 22222";
        String phoneBusinessFax     = "+333 3333 3333";
        String phoneCar             = "+44 4444 4444";
        String phoneCompany         = "+55 5555 5555";
        String phoneHome            = "+66 6666 6666";
        String phoneHome2           = "+77 7777 7777";
        String phoneHomeFax         = "+88 8888 8888";
        String phoneMobile          = "+99 9999 9999";
        String phoneMobileHome      = "+11 1111 11111";
        String phoneMobileBusiness  = "+22 (22) 2222222";
        String phoneOther           = "+333 3333 3333";
        String phoneOtherFax        = "+44 4444 4444";
        String phonePrimary         = "+55 5555 5555";
        String phonePager           = "+66 6666 6666";
    //    String phoneCallback        = "+77 777 77777";
    //    String phoneRadio           = "+8888 888 88";
    //    String phoneTelex           = "+99 9999 99";

        String content =
                "BEGIN:VCARD\n"+
                "VERSION:2.1\n"+
                "REV:20090121T122455Z\n"+
                "N:Caffrey (MOURA);Kelly;;;\n"+
                "ORG:MOURA;\n"+
                "X-CLASS:private\n"+
                "PHOTO:\n"+

                "EMAIL;INTERNET;WORK;ENCODING=QUOTED-PRINTABLE:k.caffrey=40moura.ru\n"+
                "EMAIL;INTERNET:\n"+
                "EMAIL;INTERNET;HOME:\n"+
                "ADR;WORK:px-166;bbbbbbbbb;6544 Battleford Drive;Raleigh;NC;27613-3502;U.S.A.\n"+
                "ADR;HOME:;;;;;;\n"+
                "ADR:;;;;;;\n"+
                "TEL;CELL;HOME:"+phoneMobileHome+"\n"+
                "TEL;CELL:"+phoneMobile+"\n"+
                "TEL;VOICE;HOME:"+phoneHome+"\n"+
                "TEL;VOICE;HOME:"+phoneHome2+"\n"+
                "TEL;VOICE;WORK:"+phoneBusiness+"\n"+
                "TEL;VOICE;WORK:"+phoneBusiness2+"\n"+
                "TEL;CELL;WORK:"+phoneMobileBusiness+"\n"+
                "TEL;VOICE;CAR:"+phoneCar+"\n"+
                "TEL;FAX;WORK:"+phoneBusinessFax+"\n"+
                "TEL;VOICE;PREF:"+phonePrimary+"\n"+
                "TEL;WORK;PREF:"+phoneCompany+"\n"+
                "TEL;FAX:"+phoneOtherFax+"\n"+
                "TEL;VOICE:"+phoneOther+"\n"+
                "TEL;FAX;HOME:"+phoneHomeFax+"\n"+
                "TEL:+qqqqqqq\n"+
                "TEL;PAGER:"+phonePager+"\n"+
                //"TEL;X-FUNAMBOL-CALLBACK:"+phoneCallback+"\n"+
                //"TEL;X-FUNAMBOL-RADIO:"+phoneRadio+"\n"+
                //"TEL;X-FUNAMBOL-TELEX:"+phoneTelex+"\n"+
                "URL:\n"+
                "ROLE:\n"+
                "NOTE:\n"+
                "BDAY:\n"+
                "TITLE:\n"+
                "URL;HOME:\n"+
                "NICKNAME:\n"+
                "END:VCARD\n";

        String expected = "BEGIN:VCARD\n"+
                          "VERSION:2.1\n"+
                          "N:Caffrey (MOURA);Kelly;;;\n"+
                          "NICKNAME:\n"+
                          "ADR;HOME:;;;;;;\n"+
                          "ADR:;;;;;;\n"+
                          "BDAY:\n"+
                          "TEL;CELL;HOME:"+phoneMobileHome+"\n"+
                          "TEL;CELL:"+phoneMobile+"\n"+
                          "TEL;VOICE;HOME:"+phoneHome+"\n"+
                          "TEL;VOICE;HOME:"+phoneHome2+"\n"+
                          "TEL;CAR;VOICE:"+phoneCar+"\n"+
                          "TEL;PREF;VOICE:"+phonePrimary+"\n"+
                          "TEL;FAX:"+phoneOtherFax+"\n"+
                          "TEL;VOICE:"+phoneOther+"\n"+
                          "TEL;FAX;HOME:"+phoneHomeFax+"\n"+
                          "TEL;VOICE:+qqqqqqq\n"+
                          "EMAIL;INTERNET:\n"+
                          "EMAIL;INTERNET;HOME:\n"+
                          "URL:\n"+
                          "URL;HOME:\n"+
                          "ADR;WORK:px-166;bbbbbbbbb;6544 Battleford Drive;Raleigh;NC;27613-3502;U.S.A.\n"+
                          "ROLE:\n"+
                          "TITLE:\n"+
                          "ORG:MOURA;\n"+
                          "TEL;VOICE;WORK:"+phoneBusiness+"\n"+
                          "TEL;VOICE;WORK:"+phoneBusiness2+"\n"+
                          "TEL;CELL;WORK:"+phoneMobileBusiness+"\n"+
                          "TEL;FAX;WORK:"+phoneBusinessFax+"\n"+
                          "TEL;WORK;PREF:"+phoneCompany+"\n"+
                          "TEL;PAGER:"+phonePager+"\n"+
                          "EMAIL;INTERNET;WORK:k.caffrey@moura.ru\n"+
                          "NOTE:\n"+
                          "X-CLASS:private\n"+
                          "REV:20090121T122455Z\n"+
                          "PHOTO:\n"+
                          "END:VCARD\n";


        Contact contact   = TestUtility.vcard2Contact(content);
        String  converted = TestUtility.contact2vcard(contact);

        JsonItem<String> ii = new JsonItem<String>();
        ii.setItem(converted);
        String result1 = converter.toRFC(ii);
        JSONObject jsonRoot = JSONObject.fromObject(result1);
        JSONObject jsonData = jsonRoot.getJSONObject("data");
        JSONObject jsonItem = jsonData.getJSONObject("item");

        String prop = JsonContactModel.VCARD.getValue();
        String value = jsonItem.optString(prop);
        assertEqualsVcard(expected, value);
    }

   /**
    *
    * This test verifies that a json containing a vcard with all the phone numbers
    * with the + prefix is handled properly (the + prefix isn't lost) when returning
    * a vcard
    *
    * @throws Exception
    */

    public void test_Json2Vcard_PhoneWithPlus() throws Exception {

        String phoneBusiness        = "+11 1111 11111";
        String phoneBusiness2       = "+22 (22) 22222";
        String phoneBusinessFax     = "+333 3333 3333";
        String phoneCar             = "+44 4444 4444";
        String phoneCompany         = "+55 5555 5555";
        String phoneHome            = "+66 6666 6666";
        String phoneHome2           = "+77 7777 7777";
        String phoneHomeFax         = "+88 8888 8888";
        String phoneMobile          = "+99 9999 9999";
        String phoneMobileHome      = "+11 1111 11111";
        String phoneMobileBusiness  = "+22 (22) 2222222";
        String phoneOther           = "+333 3333 3333";
        String phoneOtherFax        = "+44 4444 4444";
        String phonePrimary         = "+55 5555 5555";
        String phonePager           = "+66 6666 6666";
        String phoneCallback        = "+77 777 77777";
        String phoneRadio           = "+8888 888 88";
        String phoneTelex           = "+99 9999 99";


        String json = "{\"data\":{\"content-type\":\"application/json-card\",\"item\":"+
                      "{\"key\":\"0\",\"state\":\"A\",\"folder\":\"\",\"title\":\"\",\"firstName\":\"Kelly\","+
                      "\"middleName\":\"\",\"lastName\":\"Caffrey (MOURA)\",\"suffix\":\"\",\"nickName\":\"\","+
                      "\"company\":\"MOURA\",\"jobTitle\":\"\",\"email\":\"\",\"email2\":\"\","+
                      "\"email3\":\"k.caffrey@moura.ru\",\"body\":\"\",\"url\":\"\","+
                      "\"businessAddressStreet\":\"6544 Battleford Drive\","+
                      "\"businessAddressCity\":\"Raleigh\",\"businessAddressCountry\":\"U.S.A.\","+
                      "\"businessAddressState\":\"NC\",\"businessAddressPostalCode\":\"27613-3502\","+
                      "\"businessAddressPostOfficeBox\":\"px-166\","+
                      "\"businessAddressExtendedAddress\":\"bbbbbbbbb\","+
                      "\"homeAddressStreet\":\"\",\"homeAddressCity\":\"\","+
                      "\"homeAddressCountry\":\"\",\"homeAddressState\":\"\","+
                      "\"homeAddressPostalCode\":\"\",\"homeAddressPostOfficeBox\":\"\","+
                      "\"homeAddressExtendedAddress\":\"\",\"otherAddressStreet\":\"\","+
                      "\"otherAddressCity\":\"\",\"otherAddressCountry\":\"\","+
                      "\"otherAddressState\":\"\",\"otherAddressPostalCode\":\"\","+
                      "\"otherAddressPostOfficeBox\":\"\",\"otherAddressExtendedAddress\":\"\","+
                      "\"phoneMobileHome\":\""+phoneMobileHome+"\",\"phoneMobile\":\""+phoneMobile+"\","+
                      "\"phoneHome\":\""+phoneHome+"\",\"phoneHome2\":\""+phoneHome2+"\","+
                      "\"phoneCar\":\""+phoneCar+"\",\"phonePrimary\":\""+phonePrimary+"\","+
                      "\"phoneOtherFAX\":\""+phoneOtherFax+"\",\"phoneOther\":\""+phoneOther+"\","+
                      "\"phoneHomeFAX\":\""+phoneHomeFax+"\",\"phoneRadio\":\""+phoneRadio+"\","+
                      "\"phoneBusiness\":\""+phoneBusiness+"\",\"phoneBusiness2\":\""+phoneBusiness2+"\","+
                      "\"phoneMobileBusiness\":\""+phoneMobileBusiness+"\",\"phoneBusinessFAX\":\""+phoneBusinessFax+"\","+
                      "\"phoneCompany\":\""+phoneCompany+"\",\"phonePager\":\""+phonePager+"\","+
                      "\"phoneCallback\":\""+phoneCallback+"\",\"phoneTelex\":\""+phoneTelex+"\","+
                      "\"department\":\"\",\"birthday\":\"\"}}}";

        JsonItem<Contact> jsonItem = converter.fromJSON(json);
        Contact contact = jsonItem.getItem();
        String result   = TestUtility.contact2vcard(contact);
        String expected = "BEGIN:VCARD\n"+
                          "VERSION:2.1\n"+
                          "N:Caffrey (MOURA);Kelly;;;\n"+
                          "NICKNAME:\n"+
                          "ADR;HOME:;;;;;;\n"+
                          "ADR:;;;;;;\n"+
                          "BDAY:\n"+
                          "TEL;CELL:"+phoneMobile+"\n"+
                          "TEL;CELL;WORK:"+phoneMobileBusiness+"\n"+
                          "TEL;CELL;HOME:"+phoneMobileHome+"\n"+
                          "EMAIL;INTERNET:\n"+
                          "EMAIL;INTERNET;HOME:\n"+
                          "URL:\n"+
                          "ADR;WORK:px-166;bbbbbbbbb;6544 Battleford Drive;Raleigh;NC;27613-3502;U.S.A.\n"+
                          "TITLE:\n"+
                          "ORG:MOURA;\n"+
                          "TEL;VOICE;WORK:"+phoneBusiness+"\n"+
                          "TEL;VOICE;WORK:"+phoneBusiness2+"\n"+
                          "TEL;VOICE;HOME:"+phoneHome+"\n"+
                          "TEL;VOICE;HOME:"+phoneHome2+"\n"+
                          "TEL;FAX;HOME:"+phoneHomeFax+"\n"+
                          "TEL;WORK;PREF:"+phoneCompany+"\n"+
                          "TEL;FAX;WORK:"+phoneBusinessFax+"\n"+
                          "TEL;X-FUNAMBOL-CALLBACK:"+phoneCallback+"\n"+
                          "TEL;CAR;VOICE:"+phoneCar+"\n"+
                          "TEL;VOICE:"+phoneOther+"\n"+
                          "TEL;FAX:"+phoneOtherFax+"\n"+
                          "TEL;PREF;VOICE:"+phonePrimary+"\n"+
                          "TEL;PAGER:"+phonePager+"\n"+
                          "TEL;X-FUNAMBOL-RADIO:"+phoneRadio+"\n"+
                          "TEL;X-FUNAMBOL-TELEX:"+phoneTelex+"\n"+
                          "EMAIL;INTERNET;WORK:k.caffrey@moura.ru\n"+
                          "NOTE:\n"+
                          "X-FUNAMBOL-FOLDER:\n"+
                          "END:VCARD\n";

        assertEqualsVcard(expected,result);
  }

 /**
   *
   * This test verifies that a json with all the phone numbers
   * with the + prefix is handled properly (the + prefix isn't lost)
   * when returning a sif
   *
   * @throws Exception
   */

  public void test_Json2Sif_PhoneWithPlus() throws Exception {

        String phoneBusiness        = "+11 1111 11111";
        String phoneBusiness2       = "+22 (22) 22222";
        String phoneBusinessFax     = "+333 3333 3333";
        String phoneCar             = "+44 4444 4444";
        String phoneCompany         = "+55 5555 5555";
        String phoneHome            = "+66 6666 6666";
        String phoneHome2           = "+77 7777 7777";
        String phoneHomeFax         = "+88 8888 8888";
        String phoneMobile          = "+99 9999 9999";
        String phoneMobileHome      = "+11 1111 11111";
        String phoneMobileBusiness  = "+22 (22) 2222222";
        String phoneOther           = "+333 3333 3333";
        String phoneOtherFax        = "+44 4444 4444";
        String phonePrimary         = "+55 5555 5555";
        String phonePager           = "+66 6666 6666";
        String phoneCallback        = "+77 777 77777";
        String phoneRadio           = "+8888 888 88";
        String phoneTelex           = "+99 9999 99";


        String json = "{\"data\":{\"content-type\":\"application/json-card\",\"item\":"+
                      "{\"key\":\"0\",\"state\":\"A\",\"folder\":\"\",\"title\":\"\",\"firstName\":\"Kelly\","+
                      "\"middleName\":\"\",\"lastName\":\"Caffrey (MOURA)\",\"suffix\":\"\",\"nickName\":\"\","+
                      "\"company\":\"MOURA\",\"jobTitle\":\"\",\"email\":\"\",\"email2\":\"\","+
                      "\"email3\":\"k.caffrey@moura.ru\",\"body\":\"\",\"url\":\"\","+
                      "\"businessAddressStreet\":\"6544 Battleford Drive\","+
                      "\"businessAddressCity\":\"Raleigh\",\"businessAddressCountry\":\"U.S.A.\","+
                      "\"businessAddressState\":\"NC\",\"businessAddressPostalCode\":\"27613-3502\","+
                      "\"businessAddressPostOfficeBox\":\"px-166\","+
                      "\"businessAddressExtendedAddress\":\"bbbbbbbbb\","+
                      "\"homeAddressStreet\":\"\",\"homeAddressCity\":\"\","+
                      "\"homeAddressCountry\":\"\",\"homeAddressState\":\"\","+
                      "\"homeAddressPostalCode\":\"\",\"homeAddressPostOfficeBox\":\"\","+
                      "\"homeAddressExtendedAddress\":\"\",\"otherAddressStreet\":\"\","+
                      "\"otherAddressCity\":\"\",\"otherAddressCountry\":\"\","+
                      "\"otherAddressState\":\"\",\"otherAddressPostalCode\":\"\","+
                      "\"otherAddressPostOfficeBox\":\"\",\"otherAddressExtendedAddress\":\"\","+
                      "\"phoneMobileHome\":\""+phoneMobileHome+"\",\"phoneMobile\":\""+phoneMobile+"\","+
                      "\"phoneHome\":\""+phoneHome+"\",\"phoneHome2\":\""+phoneHome2+"\","+
                      "\"phoneCar\":\""+phoneCar+"\",\"phonePrimary\":\""+phonePrimary+"\","+
                      "\"phoneOtherFAX\":\""+phoneOtherFax+"\",\"phoneOther\":\""+phoneOther+"\","+
                      "\"phoneHomeFAX\":\""+phoneHomeFax+"\",\"phoneRadio\":\""+phoneRadio+"\","+
                      "\"phoneBusiness\":\""+phoneBusiness+"\",\"phoneBusiness2\":\""+phoneBusiness2+"\","+
                      "\"phoneMobileBusiness\":\""+phoneMobileBusiness+"\",\"phoneBusinessFAX\":\""+phoneBusinessFax+"\","+
                      "\"phoneCompany\":\""+phoneCompany+"\",\"phonePager\":\""+phonePager+"\","+
                      "\"phoneCallback\":\""+phoneCallback+"\",\"phoneTelex\":\""+phoneTelex+"\","+
                      "\"department\":\"\",\"birthday\":\"\"}}}";

        JsonItem<Contact> jsonItem = converter.fromJSON(json);
        Contact contact = jsonItem.getItem();
        String result   = TestUtility.contact2sif(contact);
        StringBuilder expected =    new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                                    .append("<contact>\n")
                                    .append("<SIFVersion>1.1</SIFVersion>")
                                    .append("<Body/>")
                                    .append("<Folder/>")
                                    .append("<Title/>")
                                    .append("<FirstName>Kelly</FirstName>")
                                    .append("<MiddleName/>")
                                    .append("<LastName>Caffrey (MOURA)</LastName>")
                                    .append("<Suffix/>")
                                    .append("<NickName/>")
                                    .append("<Birthday/>")
                                    .append("<HomeAddressPostOfficeBox/>")
                                    .append("<HomeAddressExtended/>")
                                    .append("<HomeAddressStreet/>")
                                    .append("<HomeAddressCity/>")
                                    .append("<HomeAddressState/>")
                                    .append("<HomeAddressPostalCode/>")
                                    .append("<HomeAddressCountry/>")
                                    .append("<OtherAddressPostOfficeBox/>")
                                    .append("<OtherAddressStreet/>")
                                    .append("<OtherAddressExtended/>")
                                    .append("<OtherAddressCity/>")
                                    .append("<OtherAddressState/>")
                                    .append("<OtherAddressPostalCode/>")
                                    .append("<OtherAddressCountry/>")
                                    .append("<WebPage/>")
                                    .append("<HomeTelephoneNumber>"+phoneHome+"</HomeTelephoneNumber>")
                                    .append("<HomeFaxNumber>"+phoneHomeFax+"</HomeFaxNumber>")
                                    .append("<MobileTelephoneNumber>"+phoneMobile+"</MobileTelephoneNumber>")
                                    .append("<MobileHomeTelephoneNumber>"+phoneMobileHome+"</MobileHomeTelephoneNumber>")
                                    .append("<CarTelephoneNumber>"+phoneCar+"</CarTelephoneNumber>")
                                    .append("<PrimaryTelephoneNumber>"+phonePrimary+"</PrimaryTelephoneNumber>")
                                    .append("<Home2TelephoneNumber>"+phoneHome2+"</Home2TelephoneNumber>")
                                    .append("<RadioTelephoneNumber>"+phoneRadio+"</RadioTelephoneNumber>")
                                    .append("<OtherFaxNumber>"+phoneOtherFax+"</OtherFaxNumber>")
                                    .append("<OtherTelephoneNumber>"+phoneOther+"</OtherTelephoneNumber>")
                                    .append("<JobTitle/>")
                                    .append("<CompanyName>MOURA</CompanyName>")
                                    .append("<Department/>")
                                    .append("<BusinessAddressPostOfficeBox>px-166</BusinessAddressPostOfficeBox>")
                                    .append("<BusinessAddressStreet>6544 Battleford Drive</BusinessAddressStreet>")
                                    .append("<BusinessAddressExtended>bbbbbbbbb</BusinessAddressExtended>")
                                    .append("<BusinessAddressCity>Raleigh</BusinessAddressCity>")
                                    .append("<BusinessAddressState>NC</BusinessAddressState>")
                                    .append("<BusinessAddressPostalCode>27613-3502</BusinessAddressPostalCode>")
                                    .append("<BusinessAddressCountry>U.S.A.</BusinessAddressCountry>")
                                    .append("<Email1Address/>")
                                    .append("<Email2Address/>")
                                    .append("<Email3Address>k.caffrey@moura.ru</Email3Address>")
                                    .append("<BusinessTelephoneNumber>"+phoneBusiness+"</BusinessTelephoneNumber>")
                                    .append("<BusinessFaxNumber>"+phoneBusinessFax+"</BusinessFaxNumber>")
                                    .append("<MobileBusinessTelephoneNumber>"+phoneMobileBusiness+"</MobileBusinessTelephoneNumber>")
                                    .append("<CompanyMainTelephoneNumber>"+phoneCompany+"</CompanyMainTelephoneNumber>")
                                    .append("<PagerNumber>"+phonePager+"</PagerNumber>")
                                    .append("<CallbackTelephoneNumber>"+phoneCallback+"</CallbackTelephoneNumber>")
                                    .append("<TelexNumber>"+phoneTelex+"</TelexNumber>")
                                    .append("<Business2TelephoneNumber>"+phoneBusiness2+"</Business2TelephoneNumber>")
                                    .append("</contact>");

       
        assertEquals("Wrong sif", expected.toString(),result);
  }


  /**
   *
   * This test verifies that a json containing a vcard with all the phone numbers
   * with the + prefix is handled properly (the + prefix isn't lost)
   *
   * @throws Exception 
   */

  public void test_JsonRFC2Sif_PhoneWithPlus() throws Exception {

        String phoneBusiness        = "+11 1111 11111";
        String phoneBusiness2       = "+22 (22) 22222";
        String phoneBusinessFax     = "+333 3333 3333";
        String phoneCar             = "+44 4444 4444";
        String phoneCompany         = "+55 5555 5555";
        String phoneHome            = "+66 6666 6666";
        String phoneHome2           = "+77 7777 7777";
        String phoneHomeFax         = "+88 8888 8888";
        String phoneMobile          = "+99 9999 9999";
        String phoneMobileHome      = "+11 1111 11111";
        String phoneMobileBusiness  = "+22 (22) 2222222";
        String phoneOther           = "+333 3333 3333";
        String phoneOtherFax        = "+44 4444 4444";
        String phonePrimary         = "+55 5555 5555";
        String phonePager           = "+66 6666 6666";
        String phoneCallback        = "+77 777 77777";
        String phoneRadio           = "+8888 888 88";
        String phoneTelex           = "+99 9999 99";


        String jsonRFC = //"{\"data\":{\"content-type\":\"application/json-vcard\",\"item\":{\"vcard\":\"BEGIN:VCARD\\r\\nVERSION:2.1\\r\\nN:Rossi;Giovanni;Middle;Mr.;II\\r\\nFN:Rossi, Giovanni Middle\r\nNICKNAME:\r\nADR;HOME:H3;;H6;H1;H5;H4;H2\r\nADR:O3;;O6;O1;O5;O4;O2\r\nBDAY:\r\nTEL;CAR;VOICE:+44 4444 4444\r\nTEL;VOICE;HOME:+77 7777 7777\r\nTEL;FAX;HOME:+88 8888 8888\r\nTEL;VOICE;HOME:+66 6666 6666\r\nTEL;CELL:+99 9999 9999\r\nTEL;FAX:+44 4444 4444\r\nTEL;VOICE:+333 3333 3333\r\nTEL;X-FUNAMBOL-RADIO:+8888 888 88\r\nEMAIL;INTERNET;HOME;X-FUNAMBOL-INSTANTMESSENGER:im@email.com\r\nEMAIL;INTERNET:mail@mail.it\r\nEMAIL;INTERNET;HOME:\r\nURL:http://www.prova.it\r\nURL;HOME:\r\nX-ANNIVERSARY:\r\nX-FUNAMBOL-CHILDREN:\r\nX-SPOUSE:\r\nADR;WORK:B3;;B6;B1;B5;B4;B2\r\nROLE:\r\nTITLE:Job Title\r\nORG:;;\r\nTEL;X-FUNAMBOL-TELEX:+99 9999 99\r\nTEL;FAX;WORK:+333 3333 3333\r\nTEL;VOICE;WORK:+11 1111 11111\r\nTEL;VOICE;WORK:+22 (22) 22222\r\nTEL;X-FUNAMBOL-CALLBACK:+77 777 77777\r\nTEL;WORK;PREF:+55 5555 5555\r\nTEL;PAGER:+66 6666 6666\r\nTEL;PREF;VOICE:+55 5555 5555\r\nEMAIL;INTERNET;WORK:\r\nX-FUNAMBOL-COMPANIES:\r\nX-MANAGER:\r\nAGENT:BEGIN:VCARD\\nFN:\\nTEL;WORK:\\nEND:VCARD\r\nNOTE:\r\nCATEGORIES:\r\nPHOTO:\r\nX-FUNAMBOL-FOLDER:DEFAULT_FOLDER\r\nX-FUNAMBOL-LANGUAGES:\r\nX-FUNAMBOL-MILEAGE:\r\nX-FUNAMBOL-SUBJECT:Giovanni Middle Rossi II\r\nCLASS:PUBLIC\r\nEND:VCARD\r\n\"}}}";

                "{\"data\":{\"content-type\":\"application/json-vcard\",\"item\":{\"vcard\":\"BEGIN:VCARD\\r\\n"+
                         "VERSION:2.1\\r\\n"+
                         "N:Rossi;Giovanni;Middle;Mr.;II\\r\\n"+
                         "FN:Rossi, Giovanni Middle\\r\\n"+
                         "NICKNAME:\\r\\n"+
                         "ADR;HOME:H3;;H6;H1;H5;H4;H2\\r\\n"+
                         "ADR:O3;;O6;O1;O5;O4;O2\\r\\n"+
                         "BDAY:\\r\\n"+
                         "TEL;CAR;VOICE:"+phoneCar+"\\r\\n"+
                         "TEL;VOICE;HOME:"+phoneHome+"\\r\\n"+
                         "TEL;FAX;HOME:"+phoneHomeFax+"\\r\\n"+
                         "TEL;VOICE;HOME:"+phoneHome2+"\\r\\n"+
                         "TEL;CELL:"+phoneMobile+"\\r\\n"+
                         "TEL;FAX:"+phoneOtherFax+"\\r\\n"+
                         "TEL;VOICE:"+phoneOther+"\\r\\n"+
                         "TEL;X-FUNAMBOL-RADIO:"+phoneRadio+"\\r\\n"+
                         "EMAIL;INTERNET;HOME;X-FUNAMBOL-INSTANTMESSENGER:im@email.com\\r\\n"+
                         "EMAIL;INTERNET:mail@mail.it\\r\\n"+
                         "EMAIL;INTERNET;HOME:\\r\\n"+
                         "URL:http://www.prova.it\\r\\n"+
                         "URL;HOME:\\r\\n"+
                         "X-ANNIVERSARY:\\r\\n"+
                         "X-FUNAMBOL-CHILDREN:\\r\\n"+
                         "X-SPOUSE:\\r\\n"+
                         "ADR;WORK:B3;;B6;B1;B5;B4;B2\\r\\n"+
                         "ROLE:\\r\\n"+
                         "TITLE:Job Title\\r\\n"+
                         "ORG:;;\\r\\n"+
                         "TEL;X-FUNAMBOL-TELEX:"+phoneTelex+"\\r\\n"+
                         "TEL;FAX;WORK:"+phoneBusinessFax+"\\r\\n"+
                         "TEL;VOICE;WORK:"+phoneBusiness+"\\r\\n"+
                         "TEL;VOICE;WORK:"+phoneBusiness2+"\\r\\n"+
                         "TEL;X-FUNAMBOL-CALLBACK:"+phoneCallback+"\\r\\n"+
                         "TEL;WORK;PREF:"+phoneCompany+"\\r\\n"+
                         "TEL;PAGER:"+phonePager+"\\r\\n"+
                         "TEL;PREF;VOICE:"+phonePrimary+"\\r\\n"+
                         "EMAIL;INTERNET;WORK:\\r\\n"+
                         "X-FUNAMBOL-COMPANIES:\\r\\n"+
                         "X-MANAGER:\\r\\n"+
                         "AGENT:BEGIN:VCARD\\nFN:\\nTEL;WORK:\\nEND:VCARD\\r\\n"+
                         "NOTE:\\r\\n"+
                         "CATEGORIES:\\r\\n"+
                         "PHOTO:\\r\\n"+
                         "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\\r\\n"+
                         "X-FUNAMBOL-LANGUAGES:\\r\\n"+
                         "X-FUNAMBOL-MILEAGE:\\r\\n"+
                         "X-FUNAMBOL-SUBJECT:Giovanni Middle Rossi II\\r\\n"+
                         "CLASS:PUBLIC\\r\\n"+
                         "END:VCARD\\r\\n"+
                         "\"}}}";

         String expectedVcard = "BEGIN:VCARD\r\n"+
                         "VERSION:2.1\r\n"+
                         "N:Rossi;Giovanni;Middle;Mr.;II\r\n"+
                         "FN:Rossi, Giovanni Middle\r\n"+
                         "NICKNAME:\r\n"+
                         "ADR;HOME:H3;;H6;H1;H5;H4;H2\r\n"+
                         "ADR:O3;;O6;O1;O5;O4;O2\r\n"+
                         "BDAY:\r\n"+
                         "TEL;CAR;VOICE:"+phoneCar+"\r\n"+
                         "TEL;VOICE;HOME:"+phoneHome+"\r\n"+
                         "TEL;FAX;HOME:"+phoneHomeFax+"\r\n"+
                         "TEL;VOICE;HOME:"+phoneHome2+"\r\n"+
                         "TEL;CELL:"+phoneMobile+"\r\n"+
                         "TEL;FAX:"+phoneOtherFax+"\r\n"+
                         "TEL;VOICE:"+phoneOther+"\r\n"+
                         "TEL;X-FUNAMBOL-RADIO:"+phoneRadio+"\r\n"+
                         "EMAIL;INTERNET;HOME;X-FUNAMBOL-INSTANTMESSENGER:im@email.com\r\n"+
                         "EMAIL;INTERNET:mail@mail.it\r\n"+
                         "EMAIL;INTERNET;HOME:\r\n"+
                         "URL:http://www.prova.it\r\n"+
                         "URL;HOME:\r\n"+
                         "X-ANNIVERSARY:\r\n"+
                         "X-FUNAMBOL-CHILDREN:\r\n"+
                         "X-SPOUSE:\r\n"+
                         "ADR;WORK:B3;;B6;B1;B5;B4;B2\r\n"+
                         "ROLE:\r\n"+
                         "TITLE:Job Title\r\n"+
                         "ORG:;;\r\n"+
                         "TEL;X-FUNAMBOL-TELEX:"+phoneTelex+"\r\n"+
                         "TEL;FAX;WORK:"+phoneBusinessFax+"\r\n"+
                         "TEL;VOICE;WORK:"+phoneBusiness+"\r\n"+
                         "TEL;VOICE;WORK:"+phoneBusiness2+"\r\n"+
                         "TEL;X-FUNAMBOL-CALLBACK:"+phoneCallback+"\r\n"+
                         "TEL;WORK;PREF:"+phoneCompany+"\r\n"+
                         "TEL;PAGER:"+phonePager+"\r\n"+
                         "TEL;PREF;VOICE:"+phonePrimary+"\r\n"+
                         "EMAIL;INTERNET;WORK:\r\n"+
                         "X-FUNAMBOL-COMPANIES:\r\n"+
                         "X-MANAGER:\r\n"+
                         "AGENT:BEGIN:VCARD\nFN:\nTEL;WORK:\nEND:VCARD\r\n"+
                         "NOTE:\r\n"+
                         "CATEGORIES:\r\n"+
                         "PHOTO:\r\n"+
                         "X-FUNAMBOL-FOLDER:DEFAULT_FOLDER\r\n"+
                         "X-FUNAMBOL-LANGUAGES:\r\n"+
                         "X-FUNAMBOL-MILEAGE:\r\n"+
                         "X-FUNAMBOL-SUBJECT:Giovanni Middle Rossi II\r\n"+
                         "CLASS:PUBLIC\r\n"+
                         "END:VCARD\r\n";

         JsonItem<String> jsonItem = converter.fromRFC(jsonRFC);
         String vcard = jsonItem.getItem();

         assertEqualsVcard(expectedVcard,vcard);

    }

    /**
     *
     * 
     * @throws Exception
     */
    public void test_Vcard2Json_Photo() throws Exception {

        String vcard1 =
            "BEGIN:VCARD\n" +
            "VERSION:2.1\n" +
            "N:Surname;Name;;;\n" +
            "FN:Name Surname\n" +
            "ADR;HOME:P.O. Box;Ext;Street;Town;ST;ZIP;\n" +
            "BDAY:1995-01-01\n" +
            "TEL;VOICE;HOME:+1-555-555-1234\n" +
            "EMAIL;INTERNET:test@mail.com\n" +
            "EMAIL;INTERNET;HOME:tracy@tracy.org\n" +
            "URL:http://abc.com/pub/directory/northam/jpublic.ecd\n" +
            "URL;HOME:http://www.tracy.org\n" +
            "ADR;WORK:;;Street;Town;ST;ZIP;COUNTRY\n" +
            "ROLE:Role\n" +
            "TITLE:Title\n" +
            "ORG:Org;Div;Dep\n" +
            "TEL;VOICE;WORK:1555566667777\n" +
            "TEL;FAX;WORK:+1-800-555-1234\n" +
            "URL;WORK:http://www.abc.com\n" +
            "NOTE:Don't forget to order Girl Scout cookies from Stacey today!\n" +
            "PHOTO;VALUE=URL;TYPE=GIF:http://www.abc.com/dir_photos/my_photo.gif\n" +
            "CLASS:PUBLIC\n" +
            "END:VCARD\n";

        Contact contact1 = TestUtility.vcard2Contact(vcard1);

        JsonItem<Contact> jsonItem1 = new JsonItem<Contact>();
        jsonItem1.setContentType("type");
        jsonItem1.setKey("0");
        jsonItem1.setState("A");
        jsonItem1.setItem(contact1);

        String result1 = converter.toJSON(jsonItem1);
        JSONObject jsonObject1 = JSONObject.fromObject(result1)
                                          .getJSONObject("data")
                                          .getJSONObject("item");

        assertEquals("http://www.abc.com/dir_photos/my_photo.gif",
                     jsonObject1.optString("photoUrl"));
        assertEquals("GIF",
                     jsonObject1.optString("photoType"));

        JsonItem<Contact> jsonItem2 = converter.fromJSON(result1);
        Contact contact2 = jsonItem2.getItem();
        String vcard2 = TestUtility.contact2vcard(contact2);

        assertEqualsVcard(vcard1, vcard2);
    }

    /**
     * This method allows to compare an expected vcard with a result vcard tring to
     * perform a smart matching.
     * If the comparison fails, the test who called this method failed providing
     * information about the comparison failure.
     * This method should be improved.
     *
     * @param expected the expected vcard
     * @param result the resulting vcard
     */
    private void assertEqualsVcard(String expected, String result) {
        if(expected==null) {
            assertNull("Result vcard must be null instead of ["+result+"].", result);
        } else {
            assertNotNull("Result VCARD must be not null",result);
            Map<String,Set<String>> resultMap   = buildStringMap(result);
            Map<String,Set<String>> expectedMap = buildStringMap(expected);
            boolean fail = false;
            StringBuffer reason = new StringBuffer("Test Failed:");
            Iterator<String> expectedKeysIterator = expectedMap.keySet().iterator();
            while(expectedKeysIterator.hasNext()) {
                String expectedKey = expectedKeysIterator.next();
                Set<String> expectedValues = expectedMap.get(expectedKey) ;
                if( resultMap.containsKey(expectedKey)) {
                    Set<String> resultValues   = resultMap.remove(expectedKey);
                    if(expectedValues==null || expectedValues.size()==0) { 
                        if(resultValues!=null && resultValues.size()>0) {
                            fail=true;
                            reason.append("\n");
                            reason.append("Token ["+expectedKey+"] expected with a null/empty values instead of ["+resultValues+"].");

                        }
                    } else {
                        for(String expectedValue:expectedValues) {
                            if(!resultValues.remove(expectedValue)) {
                                fail=true;
                                reason.append("\n");
                                // should be improved to perform at the end of the matching
                                if(resultValues.size()==1) {
                                    reason.append("For token ["+expectedKey+"], expected value ["+expectedValue+"] doesn't match ["+resultValues+"].");
                                    resultValues.clear();
                                } else {
                                    reason.append("For token ["+expectedKey+"], expected value ["+expectedValue+"] not found or mismatch.");
                                }
                            } 
                        }
                        if(!resultValues.isEmpty()) {
                            fail=true;
                            reason.append("\n");
                            reason.append("For token ["+expectedKey+"] result vcard contains further values ["+resultValues+"].");
                        }

                    }
                } else {
                    fail = true;
                    reason.append("\n");
                    reason.append("Token ["+expectedKey+"] with values ["+expectedValues+"] not found in the result vcard.");
                }
            }
            if(! resultMap.isEmpty()) {
                fail = true;
                for(String notFound: resultMap.keySet()) {
                    reason.append("\n");
                    reason.append("Result VCARD contains a not expected token ["+notFound+"].");
                }
            }
            if(fail) {
                fail(reason.toString());
            }
        }
    }
    /**
     * It process a vcard (replacing the \r if any) storing each token as map key
     * and each value found for that token in a corresponding set of string.
     * The set of string is used in order to store multiple values.
     * @param vcard the input vcard
     * @return a map containing all the token recognized in the given vcard as keys
     * and a set of values as map value.
     */
    private Map<String,Set<String>> buildStringMap(String vcard) {
        Map<String,Set<String>> splittedStringMap = new HashMap<String, Set<String>>();
        if(vcard!=null && vcard.length()>0) {
            vcard = vcard.replaceAll("\r", "");
            String[] splittedString = vcard.split("\n");
            for(String temp:splittedString) {
                int index = findSeparatorPosition(temp);
                String key   = temp.substring(0,index);
                String value = temp.substring(index+1);
                if(splittedStringMap.containsKey(key)) {
                    if(value!=null && value.length()>0) {
                        splittedStringMap.get(key).add(value);
                    }
                } else {
                    Set<String> valueSet = new HashSet<String>();
                    if(value!=null && value.length()>0) {
                        valueSet.add(value);
                    }
                    splittedStringMap.put(key, valueSet);
                }
            }
        }
        return splittedStringMap;
    }

    /**
     * find the position of the : delimiter that states where the property name
     * ends and where the property value starts.
     * It should escape the : in the property value.
     *
     * @param temp the line to process
     * 
     * @return the index where the property name ends and the property value starts
     */
    private int findSeparatorPosition(String temp) {
        if(temp!=null && temp.length()>0) {
            int index = temp.length();
            do {
                 index = temp.lastIndexOf(":",index);
            } while(index>0 && temp.charAt(index-1)=='\\');
            return index;
        }
        return 0;
    }


}
