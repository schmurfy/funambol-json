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
package com.funambol.json.domain;

/**
 * This is the model for a JSON contact.
 *
 * @version $Id$
 */
public enum JsonContactModel {
    
    DATA("data"),
    CONTENT_TYPE("content-type"),
    STATE("state"),
    ITEM("item"),
    VCARD("vcard"),

    // name
    TITLE("title"),
    FIRSTNAME("firstName"),
    MIDDLENAME("middleName"),
    LASTNAME("lastName"),
    DISPLAYNAME("displayName"),
    NICKNAME("nickName"),
//    INITIALS("???"),
    SUFFIX("suffix"),

    // notes
    BODY("body"),

    // personal detail address
    HOME_ADDR_POBOX("homeAddressPostOfficeBox"),
    HOME_ADDR_STREET("homeAddressStreet"),
    HOME_ADDR_CITY("homeAddressCity"),
    HOME_ADDR_STATE("homeAddressState"),
    HOME_ADDR_POSTALCODE("homeAddressPostalCode"),
    HOME_ADDR_COUNTRY("homeAddressCountry"),
    HOME_ADDR_LABEL("homeAddressLabel"),
    HOME_ADDR_EXTENDED("homeAddressExtendedAddress"),

    // personal detail other address
    OTHER_ADDR_POBOX("otherAddressPostOfficeBox"),
    OTHER_ADDR_STREET("otherAddressStreet"),
    OTHER_ADDR_CITY("otherAddressCity"),
    OTHER_ADDR_STATE("otherAddressState"),
    OTHER_ADDR_POSTALCODE("otherAddressPostalCode"),
    OTHER_ADDR_COUNTRY("otherAddressCountry"),
    OTHER_ADDR_LABEL("otherAddressLabel"),
    OTHER_ADDR_EXTENDED("otherAddressExtendedAddress"),

    // personal detail
    PHOTO("photo"),
    PHOTO_TYPE("photoType"),
    PHOTO_URL("photoUrl"),
//    GEO("???"),
    SPOUSE_NAME("spouseName"),
    CHILDREN("children"),
    ANNIVERSARY("anniversary"),
    BIRTHDAY("birthday"),
    GENDER("gender"),
    HOBBIES("hobbies"),

    // personal detail phones
    PHONE_HOME("phoneHome"),
    PHONE_HOME_FAX("phoneHomeFAX"),
    PHONE_MOBILE("phoneMobile"),
    PHONE_MOBILE_HOME("phoneMobileHome"),
    PHONE_CAR("phoneCar"),
    PHONE_PRIMARY("phonePrimary"),
    PHONE_HOME_2("phoneHome2"),
    PHONE_RADIO("phoneRadio"),
    PHONE_OTHER_FAX("phoneOtherFAX"),
    PHONE_OTHER("phoneOther"),

    // personal detail emails
    EMAIL("email"),
    EMAIL2("email2"),
//    MOBILE_EMAIL("???"),
    IM_ADDRESS("imAddress"),

    // personal detail web pages
    URL_WEB("url"),
    URL_HOME_WEB("urlHome"),

    // personal detail impp addresses

    // business detail address
    BUSINESS_ADDR_POBOX("businessAddressPostOfficeBox"),
    BUSINESS_ADDR_STREET("businessAddressStreet"),
    BUSINESS_ADDR_CITY("businessAddressCity"),
    BUSINESS_ADDR_STATE("businessAddressState"),
    BUSINESS_ADDR_POSTALCODE("businessAddressPostalCode"),
    BUSINESS_ADDR_COUNTRY("businessAddressCountry"),
    BUSINESS_ADDR_LABEL("businessAddressLabel"),
    BUSINESS_ADDR_EXTENDED("businessAddressExtendedAddress"),

    // business detail
    PROFESSION("profession"),
    JOB_TITLE("jobTitle"),
    COMPANY("company"),
    DEPARTMENT("department"),
    MANAGER_NAME("managerName"),
    ASSISTANT_NAME("assistantName"),
//    ASSISTANT_URI("???"),
    OFFICE("office"),
    COMPANIES("companies"),

    // business detail phones
    PHONE_BUSINESS("phoneBusiness"),
    PHONE_BUSINESS_FAX("phoneBusinessFAX"),
    PHONE_MOBILE_BUSINESS("phoneMobileBusiness"),
    PHONE_COMPANY("phoneCompany"),
    PHONE_ASSISTANT("phoneAssistant"),
    PHONE_CALLBACK("phoneCallback"),
    PHONE_PAGER("phonePager"),
    PHONE_TELEX("phoneTelex"),
    PHONE_BUSINESS_2("phoneBusiness2"),

    // business detail emails
    EMAIL3("email3"),

    // business detail web pages
    URL_WORK_WEB("urlWork"),

    // business detail impp addresses

    // details
    CATEGORIES("categories"),
    LANGUAGE("language"),
    IMPORTANCE("importance"),
    SENSITIVITY("sensitivity"),
    SUBJECT("subject"),
    MILEAGE("mileage"),
    FOLDER("folder"),
    FREEBUSY_ADDRESS("freeBusyAddress"),
    KEY("key")
//    DIRECTORY_SERVER("directoryServer"),
//    EMAIL_ALIAS("emailAlias"),
           ;
    
    private final String value;
    
    private JsonContactModel(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
