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

import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.contact.Address;
import com.funambol.common.pim.contact.BusinessDetail;
import java.util.List;
import net.sf.json.JSONObject;
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
import com.funambol.framework.tools.Base64;
import com.funambol.json.domain.JsonContactModel;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.util.Utility;

public class ContactConverter implements Converter<JsonItem<Contact>> {

    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#toJSON(java.lang.Object)
     */
    public String toJSON(JsonItem<Contact> item) {

        Contact contact = item.getItem();

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        // the content-type for the extended format is
        putJsonValue(jsonData, JsonContactModel.CONTENT_TYPE.getValue(), Utility.CONTENT_TYPE_CONTACT_EXT);

        putJsonValue(jsonItem, JsonContactModel.KEY.getValue(), item.getKey());
        putJsonValue(jsonItem, JsonContactModel.STATE.getValue(), item.getState());

        // folder
        String folder = Utility.getPropertyValue(contact.getFolder());
        if (!folder.equals("")) {
            folder = Utility.folderConverterC2S(folder, Utility.BACKEND_CONTACT_FOLDER_PREFIX);
            putJsonValue(jsonItem, JsonContactModel.FOLDER.getValue(), folder);
        }

        // name
        if (contact.getName() != null) {
            nameToJson(jsonItem, contact.getName());
        }

        // notes
        if ((contact.getNotes() != null) && (!contact.getNotes().isEmpty())) {
            setJsonFromPropertyValue(jsonItem, JsonContactModel.BODY.getValue(), (Note) contact.getNotes().get(0));
        }

        // personal detail
        if (contact.getPersonalDetail() != null) {
            personalDetailToJson(jsonItem, contact.getPersonalDetail());
        }

        // business detail
        if (contact.getBusinessDetail() != null) {
            businessDetailToJson(contact.getBusinessDetail(), jsonItem);
        }

        // phones
        List<Phone> phones = contact.getPersonalDetail().getPhones();
        phones.addAll(contact.getBusinessDetail().getPhones());
        // personal
        getPhone(phones, SIFC.HOME_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_HOME.getValue());
        getPhone(phones, SIFC.HOME_FAX_NUMBER, jsonItem, JsonContactModel.PHONE_HOME_FAX.getValue());
        getPhone(phones, SIFC.MOBILE_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_MOBILE.getValue());
        getPhone(phones, SIFC.MOBILE_HOME_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_MOBILE_HOME.getValue());
        getPhone(phones, SIFC.CAR_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_CAR.getValue());
        getPhone(phones, SIFC.PRIMARY_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_PRIMARY.getValue());
        getPhone(phones, SIFC.HOME2_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_HOME_2.getValue());
        getPhone(phones, SIFC.RADIO_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_RADIO.getValue());
        getPhone(phones, SIFC.OTHER_FAX_NUMBER, jsonItem, JsonContactModel.PHONE_OTHER_FAX.getValue());
        getPhone(phones, SIFC.OTHER_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_OTHER.getValue());
        // business
        getPhone(phones, SIFC.BUSINESS_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_BUSINESS.getValue());
        getPhone(phones, SIFC.BUSINESS_FAX_NUMBER, jsonItem, JsonContactModel.PHONE_BUSINESS_FAX.getValue());
        getPhone(phones, SIFC.MOBILE_BUSINESS_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_MOBILE_BUSINESS.getValue());
        getPhone(phones, SIFC.COMPANY_MAIN_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_COMPANY.getValue());
        getPhone(phones, SIFC.ASSISTANT_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_ASSISTANT.getValue());
        getPhone(phones, SIFC.PAGER_NUMBER, jsonItem, JsonContactModel.PHONE_PAGER.getValue());
        getPhone(phones, SIFC.CALLBACK_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_CALLBACK.getValue());
        getPhone(phones, SIFC.TELEX_NUMBER, jsonItem, JsonContactModel.PHONE_TELEX.getValue());
        getPhone(phones, SIFC.BUSINESS2_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_BUSINESS_2.getValue());

        // emails
        List<Email> emails = contact.getPersonalDetail().getEmails();
        emails.addAll(contact.getBusinessDetail().getEmails());
        // personal
        getEmail(emails, SIFC.EMAIL1_ADDRESS, jsonItem, JsonContactModel.EMAIL.getValue());
        getEmail(emails, SIFC.EMAIL2_ADDRESS, jsonItem, JsonContactModel.EMAIL2.getValue());
//        getEmail(emails, SIFC."MobileEmailAddress", jsonItem, JsonContactModel.MOBILE_EMAIL.getValue());
        getEmail(emails, SIFC.INSTANT_MESSENGER, jsonItem, JsonContactModel.IM_ADDRESS.getValue());
        // business
        getEmail(emails, SIFC.EMAIL3_ADDRESS, jsonItem, JsonContactModel.EMAIL3.getValue());

        // web pages
        List<WebPage> webPages = contact.getPersonalDetail().getWebPages();
        webPages.addAll(contact.getBusinessDetail().getWebPages());
        // personal
        getWebPage(webPages, SIFC.WEB_PAGE, jsonItem, JsonContactModel.URL_WEB.getValue());
        getWebPage(webPages, SIFC.HOME_WEB_PAGE, jsonItem, JsonContactModel.URL_HOME_WEB.getValue());
        // business
        getWebPage(webPages, SIFC.BUSINESS_WEB_PAGE, jsonItem, JsonContactModel.URL_WORK_WEB.getValue());

        // impp addresses
//        List<IMPPAddress> imppAddresses = contact.getPersonalDetail().getIMPPs();
//        imppAddresses.addAll(contact.getBusinessDetail().getIMPPs());
//        // personal
//        // business

        setJsonFromPropertyValue(jsonItem, JsonContactModel.CATEGORIES.getValue(), contact.getCategories());
        putJsonValue(jsonItem, JsonContactModel.LANGUAGE.getValue(), contact.getLanguages());
        putJsonValue(jsonItem, JsonContactModel.IMPORTANCE.getValue(), contact.getImportance());
        putJsonValue(jsonItem, JsonContactModel.SENSITIVITY.getValue(), contact.getSensitivity());
        putJsonValue(jsonItem, JsonContactModel.SUBJECT.getValue(), contact.getSubject());
        putJsonValue(jsonItem, JsonContactModel.MILEAGE.getValue(), contact.getMileage());

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

    private void putJsonValue(JSONObject jsonItem, String label, Short value) {
        jsonItem.elementOpt(label, value);
    }

    private void putJsonValue(JSONObject jsonItem, String label, String value) {
        jsonItem.elementOpt(label, value);
    }

    private void setJsonFromPropertyValue(JSONObject jsonItem, String label, Property property) {
        if (null != property) {
            String value = property.getPropertyValueAsString();
            if ("null".equals(value)) {
                value = "";
            }
            jsonItem.elementOpt(label, value);
        }
    }

    private void nameToJson(JSONObject jsonItem, Name name) {

        setJsonFromPropertyValue(jsonItem, JsonContactModel.TITLE.getValue(), name.getSalutation());
        setJsonFromPropertyValue(jsonItem, JsonContactModel.FIRSTNAME.getValue(), name.getFirstName());
        setJsonFromPropertyValue(jsonItem, JsonContactModel.MIDDLENAME.getValue(), name.getMiddleName());
        setJsonFromPropertyValue(jsonItem, JsonContactModel.LASTNAME.getValue(), name.getLastName());
        setJsonFromPropertyValue(jsonItem, JsonContactModel.DISPLAYNAME.getValue(), name.getDisplayName());
        setJsonFromPropertyValue(jsonItem, JsonContactModel.NICKNAME.getValue(), name.getNickname());
//        setJsonFromPropertyValue(jsonItem, JsonContactModel.INITIALS.getValue(), name.getInitials());
        setJsonFromPropertyValue(jsonItem, JsonContactModel.SUFFIX.getValue(), name.getSuffix());
    }

    private void personalDetailToJson(JSONObject jsonItem, PersonalDetail personalDetail) {

        Address address;

        // address
        if (null != (address = personalDetail.getAddress())) {
            setJsonFromPropertyValue(jsonItem, JsonContactModel.HOME_ADDR_POBOX.getValue(), address.getPostOfficeAddress());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.HOME_ADDR_STREET.getValue(), address.getStreet());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.HOME_ADDR_CITY.getValue(), address.getCity());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.HOME_ADDR_STATE.getValue(), address.getState());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.HOME_ADDR_POSTALCODE.getValue(), address.getPostalCode());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.HOME_ADDR_COUNTRY.getValue(), address.getCountry());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.HOME_ADDR_LABEL.getValue(), address.getLabel());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.HOME_ADDR_EXTENDED.getValue(), address.getExtendedAddress());
        }

        // other address
        if (null != (address = personalDetail.getOtherAddress())) {
            setJsonFromPropertyValue(jsonItem, JsonContactModel.OTHER_ADDR_POBOX.getValue(), address.getPostOfficeAddress());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.OTHER_ADDR_STREET.getValue(), address.getStreet());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.OTHER_ADDR_CITY.getValue(), address.getCity());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.OTHER_ADDR_STATE.getValue(), address.getState());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.OTHER_ADDR_POSTALCODE.getValue(), address.getPostalCode());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.OTHER_ADDR_COUNTRY.getValue(), address.getCountry());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.OTHER_ADDR_LABEL.getValue(), address.getLabel());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.OTHER_ADDR_EXTENDED.getValue(), address.getExtendedAddress());
        }

        // photo
        Property photoProperty = personalDetail.getPhoto();
        if (photoProperty != null) {
            if (photoProperty.getPropertyValueAsString() != null) {
                Photo photo = personalDetail.getPhotoObject();
                if (photo.getImage() != null && photo.getImage().length > 0) {
                    String encodedImage = new String(Base64.encode(photo.getImage()));
                    putJsonValue(jsonItem, JsonContactModel.PHOTO.getValue(), encodedImage);
                    putJsonValue(jsonItem, JsonContactModel.PHOTO_TYPE.getValue(), photo.getType());
                } else if (photo.getUrl() != null) {
                    putJsonValue(jsonItem, JsonContactModel.PHOTO_URL.getValue(), photo.getUrl());
                    putJsonValue(jsonItem, JsonContactModel.PHOTO_TYPE.getValue(), photo.getType());
                } else {
                    putJsonValue(jsonItem, JsonContactModel.PHOTO.getValue(), "");
                }
            }
        }

//        setJsonFromPropertyValue(jsonItem, JsonContactModel.GEO, personalDetail.getGeo());
        putJsonValue(jsonItem, JsonContactModel.SPOUSE_NAME.getValue(), personalDetail.getSpouse());
        putJsonValue(jsonItem, JsonContactModel.CHILDREN.getValue(), personalDetail.getChildren());
        putJsonValue(jsonItem, JsonContactModel.ANNIVERSARY.getValue(), personalDetail.getAnniversary());
        putJsonValue(jsonItem, JsonContactModel.BIRTHDAY.getValue(), personalDetail.getBirthday());
        putJsonValue(jsonItem, JsonContactModel.GENDER.getValue(), personalDetail.getGender());
        putJsonValue(jsonItem, JsonContactModel.HOBBIES.getValue(), personalDetail.getHobbies());
    }

    private void businessDetailToJson(BusinessDetail businessDetail, JSONObject jsonItem) {

        Address address;

        // address
        if (null != (address = businessDetail.getAddress())) {
            setJsonFromPropertyValue(jsonItem, JsonContactModel.BUSINESS_ADDR_POBOX.getValue(), address.getPostOfficeAddress());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.BUSINESS_ADDR_STREET.getValue(), address.getStreet());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.BUSINESS_ADDR_CITY.getValue(), address.getCity());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.BUSINESS_ADDR_STATE.getValue(), address.getState());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.BUSINESS_ADDR_POSTALCODE.getValue(), address.getPostalCode());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.BUSINESS_ADDR_COUNTRY.getValue(), address.getCountry());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.BUSINESS_ADDR_LABEL.getValue(), address.getLabel());
            setJsonFromPropertyValue(jsonItem, JsonContactModel.BUSINESS_ADDR_EXTENDED.getValue(), address.getExtendedAddress());
        }

        setJsonFromPropertyValue(jsonItem, JsonContactModel.PROFESSION.getValue(), businessDetail.getRole());
        if ((businessDetail.getTitles() != null) && (!businessDetail.getTitles().isEmpty())) {
            setJsonFromPropertyValue(jsonItem, JsonContactModel.JOB_TITLE.getValue(), (Title) businessDetail.getTitles().get(0));
        }
        setJsonFromPropertyValue(jsonItem, JsonContactModel.COMPANY.getValue(), businessDetail.getCompany());
        setJsonFromPropertyValue(jsonItem, JsonContactModel.DEPARTMENT.getValue(), businessDetail.getDepartment());
        putJsonValue(jsonItem, JsonContactModel.MANAGER_NAME.getValue(), businessDetail.getManager());
        putJsonValue(jsonItem, JsonContactModel.ASSISTANT_NAME.getValue(), businessDetail.getAssistant());
//        putJsonValue(jsonItem, JsonContactModel.ASSISTANT_URI.getValue(), businessDetail.isAssistantURI());
        putJsonValue(jsonItem, JsonContactModel.OFFICE.getValue(), businessDetail.getOfficeLocation());
        putJsonValue(jsonItem, JsonContactModel.COMPANIES.getValue(), businessDetail.getCompanies());
    }

    private void getPhone(List<Phone> phones, String type, JSONObject jsonItem, String label) {

        if (phones != null) {
            for (Phone item : phones) {
                String itemType = item.getPropertyType();
                if (itemType != null) {
                    if (itemType.equals(type)) {
                        String value = item.getPropertyValueAsString();
                        putJsonValue(jsonItem, label, value);
                    }
                }
            }
        }
    }

    private void getEmail(List<Email> emails, String type, JSONObject jsonItem, String label) {

        if (emails != null) {
            for (Email item : emails) {
                String itemType = item.getPropertyType();
                if (itemType != null) {
                    if (itemType.equals(type)) {
                        String value = item.getPropertyValueAsString();
                        putJsonValue(jsonItem, label, value);
                    }
                }
            }
        }
    }

    private void getWebPage(List<WebPage> webPages, String type, JSONObject jsonItem, String label) {

        if (webPages != null) {
            for (WebPage item : webPages) {
                String itemType = item.getPropertyType();
                if (itemType != null) {
                    if (itemType.equals(type)) {
                        String value = item.getPropertyValueAsString();
                        putJsonValue(jsonItem, label, value);
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#fromJSON(java.lang.String)
     */
    public JsonItem<Contact> fromJSON(String jsonContent) {

        Contact contact = new Contact();

        JSONObject jsonRoot = JSONObject.fromObject(jsonContent);
        JSONObject jsonData = jsonRoot.getJSONObject(JsonContactModel.DATA.getValue());
        JSONObject jsonItem = jsonData.getJSONObject(JsonContactModel.ITEM.getValue());

        JsonItem<Contact> item = new JsonItem<Contact>();
        item.setContentType(Utility.getJsonValue(jsonData, JsonContactModel.CONTENT_TYPE.getValue()));

        item.setKey(Utility.getJsonValue(jsonItem, JsonContactModel.KEY.getValue()));
        item.setState(Utility.getJsonValue(jsonItem, JsonContactModel.STATE.getValue()));

        // folder
        String folder = Utility.getJsonValue(jsonItem, JsonContactModel.FOLDER.getValue());
        if (folder != null) {
            folder = Utility.folderConverterS2C(folder, Utility.BACKEND_CONTACT_FOLDER_PREFIX);
            contact.setFolder(folder);
        }

        // name
        nameFromJson(jsonItem, contact.getName());

        // notes
        String body = Utility.getJsonValue(jsonItem, JsonContactModel.BODY.getValue());
        if (body != null) {
            Note note = new Note();
            note.setNoteType(SIFC.BODY);
            note.setPropertyValue(body);
            contact.addNote(note);
        }

        // personal detail
        personalDetailFromJson(jsonItem, contact.getPersonalDetail());

        // business detail
        businessDetailFromJson(jsonItem, contact.getBusinessDetail());

        setPropertyValueFromJson(jsonItem, JsonContactModel.CATEGORIES.getValue(), contact.getCategories());
        contact.setLanguages(Utility.getJsonValue(jsonItem, JsonContactModel.LANGUAGE.getValue()));
        contact.setImportance(getJsonValueShort(jsonItem, JsonContactModel.IMPORTANCE.getValue()));
        contact.setSensitivity(getJsonValueShort(jsonItem, JsonContactModel.SENSITIVITY.getValue()));
        contact.setSubject(Utility.getJsonValue(jsonItem, JsonContactModel.SUBJECT.getValue()));
        contact.setMileage(Utility.getJsonValue(jsonItem, JsonContactModel.MILEAGE.getValue()));

        // DIRECTORY_SERVER("directoryServer"),
        // not available in the 7.0

        // EMAIL_ALIAS("emailAlias"),
        // not available in the 7.0

        //  FREEBUSY_ADDRESS("freeBusyAddress"),
        // not available in the 7.0

        item.setItem(contact);

        return item;
    }

    private Short getJsonValueShort(JSONObject jsonItem, String label) {
        try {
            String s = jsonItem.optString(label);
            return Short.parseShort(s);
        } catch (Exception e) {
            // do nothing
        }
        return null;
    }

    private void setPropertyValueFromJson(JSONObject jsonItem, String label, Property property) {
        if (property != null) {
            property.setPropertyValue(Utility.getJsonValue(jsonItem, label));
        }
    }

    private void nameFromJson(JSONObject jsonItem, Name name) {

        setPropertyValueFromJson(jsonItem, JsonContactModel.TITLE.getValue(), name.getSalutation());
        setPropertyValueFromJson(jsonItem, JsonContactModel.FIRSTNAME.getValue(), name.getFirstName());
        setPropertyValueFromJson(jsonItem, JsonContactModel.MIDDLENAME.getValue(), name.getMiddleName());
        setPropertyValueFromJson(jsonItem, JsonContactModel.LASTNAME.getValue(), name.getLastName());
        setPropertyValueFromJson(jsonItem, JsonContactModel.DISPLAYNAME.getValue(), name.getDisplayName());
        setPropertyValueFromJson(jsonItem, JsonContactModel.NICKNAME.getValue(), name.getNickname());
//        setPropertyValueFromJson(jsonItem, JsonContactModel.INITIALS.getValue(), name.getInitials());
        setPropertyValueFromJson(jsonItem, JsonContactModel.SUFFIX.getValue(), name.getSuffix());
    }

    private void personalDetailFromJson(JSONObject jsonItem, PersonalDetail personalDetail) {

        Address address;

        // address
        if (null != (address = personalDetail.getAddress())) {
            setPropertyValueFromJson(jsonItem, JsonContactModel.HOME_ADDR_POBOX.getValue(), address.getPostOfficeAddress());
            setPropertyValueFromJson(jsonItem, JsonContactModel.HOME_ADDR_STREET.getValue(), address.getStreet());
            setPropertyValueFromJson(jsonItem, JsonContactModel.HOME_ADDR_CITY.getValue(), address.getCity());
            setPropertyValueFromJson(jsonItem, JsonContactModel.HOME_ADDR_STATE.getValue(), address.getState());
            setPropertyValueFromJson(jsonItem, JsonContactModel.HOME_ADDR_POSTALCODE.getValue(), address.getPostalCode());
            setPropertyValueFromJson(jsonItem, JsonContactModel.HOME_ADDR_COUNTRY.getValue(), address.getCountry());
            setPropertyValueFromJson(jsonItem, JsonContactModel.HOME_ADDR_LABEL.getValue(), address.getLabel());
            setPropertyValueFromJson(jsonItem, JsonContactModel.HOME_ADDR_EXTENDED.getValue(), address.getExtendedAddress());
        }

        // other address
        if (null != (address = personalDetail.getOtherAddress())) {
            setPropertyValueFromJson(jsonItem, JsonContactModel.OTHER_ADDR_POBOX.getValue(), address.getPostOfficeAddress());
            setPropertyValueFromJson(jsonItem, JsonContactModel.OTHER_ADDR_STREET.getValue(), address.getStreet());
            setPropertyValueFromJson(jsonItem, JsonContactModel.OTHER_ADDR_CITY.getValue(), address.getCity());
            setPropertyValueFromJson(jsonItem, JsonContactModel.OTHER_ADDR_STATE.getValue(), address.getState());
            setPropertyValueFromJson(jsonItem, JsonContactModel.OTHER_ADDR_POSTALCODE.getValue(), address.getPostalCode());
            setPropertyValueFromJson(jsonItem, JsonContactModel.OTHER_ADDR_COUNTRY.getValue(), address.getCountry());
            setPropertyValueFromJson(jsonItem, JsonContactModel.OTHER_ADDR_LABEL.getValue(), address.getLabel());
            setPropertyValueFromJson(jsonItem, JsonContactModel.OTHER_ADDR_EXTENDED.getValue(), address.getExtendedAddress());
        }

        // photo
        String imageUrl = Utility.getJsonValue(jsonItem, JsonContactModel.PHOTO_URL.getValue());
        String imageType = Utility.getJsonValue(jsonItem, JsonContactModel.PHOTO_TYPE.getValue());
        if (imageUrl != null) {
            Photo photo = new Photo(imageUrl, null);
            if (imageType != null) {
                photo.setType(imageType);
            }
            personalDetail.setPhotoObject(photo);
        } else {
            String encodedImage = Utility.getJsonValue(jsonItem, JsonContactModel.PHOTO.getValue());
            if (encodedImage != null) {
                Photo photo = new Photo();
                if (encodedImage.length() > 0) {
                    photo.setImage(Base64.decode(encodedImage));
                    photo.setType(imageType);
                }
                personalDetail.setPhotoObject(photo);
            }
        }
        
//        setPropertyValueFromJson(jsonItem, JsonContactModel.GEO.getValue(), personalDetail.getGeo());
        personalDetail.setSpouse(Utility.getJsonValue(jsonItem, JsonContactModel.SPOUSE_NAME.getValue()));
        personalDetail.setChildren(Utility.getJsonValue(jsonItem, JsonContactModel.CHILDREN.getValue()));
        personalDetail.setAnniversary(Utility.getJsonValue(jsonItem, JsonContactModel.ANNIVERSARY.getValue()));
        personalDetail.setBirthday(Utility.getJsonValue(jsonItem, JsonContactModel.BIRTHDAY.getValue()));
        personalDetail.setGender(Utility.getJsonValue(jsonItem, JsonContactModel.GENDER.getValue()));
        personalDetail.setHobbies(Utility.getJsonValue(jsonItem, JsonContactModel.HOBBIES.getValue()));

        // phones
        List<Phone> phones = personalDetail.getPhones();
        addPhone(phones, SIFC.HOME_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_HOME.getValue());
        addPhone(phones, SIFC.HOME_FAX_NUMBER, jsonItem, JsonContactModel.PHONE_HOME_FAX.getValue());
        addPhone(phones, SIFC.MOBILE_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_MOBILE.getValue());
        addPhone(phones, SIFC.MOBILE_HOME_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_MOBILE_HOME.getValue());
        addPhone(phones, SIFC.CAR_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_CAR.getValue());
        addPhone(phones, SIFC.PRIMARY_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_PRIMARY.getValue());
        addPhone(phones, SIFC.HOME2_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_HOME_2.getValue());
        addPhone(phones, SIFC.RADIO_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_RADIO.getValue());
        addPhone(phones, SIFC.OTHER_FAX_NUMBER, jsonItem, JsonContactModel.PHONE_OTHER_FAX.getValue());
        addPhone(phones, SIFC.OTHER_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_OTHER.getValue());

        // emails
        List<Email> emails = personalDetail.getEmails();
        addEmail(emails, SIFC.EMAIL1_ADDRESS, jsonItem, JsonContactModel.EMAIL.getValue());
        addEmail(emails, SIFC.EMAIL2_ADDRESS, jsonItem, JsonContactModel.EMAIL2.getValue());
        addEmail(emails, SIFC.INSTANT_MESSENGER, jsonItem, JsonContactModel.IM_ADDRESS.getValue());

        // web pages
        List<WebPage> webPages = personalDetail.getWebPages();
        addWebPage(webPages, SIFC.WEB_PAGE, jsonItem, JsonContactModel.URL_WEB.getValue());
        addWebPage(webPages, SIFC.HOME_WEB_PAGE, jsonItem, JsonContactModel.URL_HOME_WEB.getValue());

        // impp addresses
//        List<IMPPAddress> imppAddresses = personalDetail.getIMPPs();
    }

    private void businessDetailFromJson(JSONObject jsonItem, BusinessDetail businessDetail) {

        Address address;

        // address
        if (null != (address = businessDetail.getAddress())) {
            setPropertyValueFromJson(jsonItem, JsonContactModel.BUSINESS_ADDR_POBOX.getValue(), address.getPostOfficeAddress());
            setPropertyValueFromJson(jsonItem, JsonContactModel.BUSINESS_ADDR_STREET.getValue(), address.getStreet());
            setPropertyValueFromJson(jsonItem, JsonContactModel.BUSINESS_ADDR_CITY.getValue(), address.getCity());
            setPropertyValueFromJson(jsonItem, JsonContactModel.BUSINESS_ADDR_STATE.getValue(), address.getState());
            setPropertyValueFromJson(jsonItem, JsonContactModel.BUSINESS_ADDR_POSTALCODE.getValue(), address.getPostalCode());
            setPropertyValueFromJson(jsonItem, JsonContactModel.BUSINESS_ADDR_COUNTRY.getValue(), address.getCountry());
            setPropertyValueFromJson(jsonItem, JsonContactModel.BUSINESS_ADDR_LABEL.getValue(), address.getLabel());
            setPropertyValueFromJson(jsonItem, JsonContactModel.BUSINESS_ADDR_EXTENDED.getValue(), address.getExtendedAddress());
        }

        setPropertyValueFromJson(jsonItem, JsonContactModel.PROFESSION.getValue(), businessDetail.getRole());

        if (jsonItem.has(JsonContactModel.JOB_TITLE.getValue())) {
            Title title = new Title(jsonItem.optString(JsonContactModel.JOB_TITLE.getValue()));
            title.setTitleType(SIFC.JOB_TITLE);
            businessDetail.addTitle(title);
        }

        setPropertyValueFromJson(jsonItem, JsonContactModel.COMPANY.getValue(), businessDetail.getCompany());
        setPropertyValueFromJson(jsonItem, JsonContactModel.DEPARTMENT.getValue(), businessDetail.getDepartment());
        businessDetail.setManager(Utility.getJsonValue(jsonItem, JsonContactModel.MANAGER_NAME.getValue()));
        businessDetail.setAssistant(Utility.getJsonValue(jsonItem, JsonContactModel.ASSISTANT_NAME.getValue()));
//        businessDetail.setAssistantURI(Utility.getJsonValue(jsonItem, JsonContactModel.ASSISTANT_URI.getValue()));
        businessDetail.setOfficeLocation(Utility.getJsonValue(jsonItem, JsonContactModel.OFFICE.getValue()));
        businessDetail.setCompanies(Utility.getJsonValue(jsonItem, JsonContactModel.COMPANIES.getValue()));

        // phones
        List<Phone> phones = businessDetail.getPhones();
        addPhone(phones, SIFC.BUSINESS_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_BUSINESS.getValue());
        addPhone(phones, SIFC.BUSINESS_FAX_NUMBER, jsonItem, JsonContactModel.PHONE_BUSINESS_FAX.getValue());
        addPhone(phones, SIFC.MOBILE_BUSINESS_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_MOBILE_BUSINESS.getValue());
        addPhone(phones, SIFC.COMPANY_MAIN_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_COMPANY.getValue());
        addPhone(phones, SIFC.ASSISTANT_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_ASSISTANT.getValue());
        addPhone(phones, SIFC.PAGER_NUMBER, jsonItem, JsonContactModel.PHONE_PAGER.getValue());
        addPhone(phones, SIFC.CALLBACK_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_CALLBACK.getValue());
        addPhone(phones, SIFC.TELEX_NUMBER, jsonItem, JsonContactModel.PHONE_TELEX.getValue());
        addPhone(phones, SIFC.BUSINESS2_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_BUSINESS_2.getValue());
        // NOTE: The primary phone number is returned into the business detail phones by the SIFC Parser.
        //addPhone(phones, SIFC.PRIMARY_TELEPHONE_NUMBER, jsonItem, JsonContactModel.PHONE_PRIMARY.getValue());

        // emails
        List<Email> emails = businessDetail.getEmails();
        addEmail(emails, SIFC.EMAIL3_ADDRESS, jsonItem, JsonContactModel.EMAIL3.getValue());

        // web pages
        List<WebPage> webPages = businessDetail.getWebPages();
        addWebPage(webPages, SIFC.BUSINESS_WEB_PAGE, jsonItem, JsonContactModel.URL_WORK_WEB.getValue());

        // impp addresses
//        List<IMPPAddress> imppAddresses = businessDetail.getIMPPs();
    }

    private void addPhone(List<Phone> phones, String type, JSONObject jsonItem, String label) {

        String value = Utility.getJsonValue(jsonItem, label);
        if (null != value) {
            Phone instance = new Phone();
            instance.setPropertyType(type);
            instance.setPropertyValue(value);
            phones.add(instance);
        }
    }

    private void addEmail(List<Email> emails, String type, JSONObject jsonItem, String label) {

        String value = Utility.getJsonValue(jsonItem, label);
        if (null != value) {
            Email instance = new Email();
            instance.setPropertyType(type);
            instance.setPropertyValue(value);
            emails.add(instance);
        }
    }

    private void addWebPage(List<WebPage> webPages, String type, JSONObject jsonItem, String label) {

        String value = Utility.getJsonValue(jsonItem, label);
        if (null != value) {
            WebPage instance = new WebPage();
            instance.setPropertyType(type);
            instance.setPropertyValue(value);
            webPages.add(instance);
        }
    }



    /**
     * this method converts from a json contact item object to a json vcard text representation
     * @param item
     * @return
     * @throws java.lang.Exception
     */
    public String toRFC(JsonItem<String> item) {

        String vcard = item.getItem();

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        // the content-type for the internet specification format is
        jsonData.elementOpt(JsonContactModel.CONTENT_TYPE.getValue(), Utility.CONTENT_TYPE_CONTACT_RFC);

        jsonItem.elementOpt(JsonContactModel.KEY.getValue(), item.getKey());
        jsonItem.elementOpt(JsonContactModel.STATE.getValue(), item.getState());

        //jsonItem.elementOpt(JsonContactModel.FOLDER.getValue(), contact.getFolder());

        jsonItem.elementOpt(JsonContactModel.VCARD.getValue(), vcard);

        jsonData.element(JsonContactModel.ITEM.getValue(), jsonItem);
        jsonRoot.element(JsonContactModel.DATA.getValue(), jsonData);

        return jsonRoot.toString();
    }

    /**
     * this method converts an item received in vcard format to an item in Funambol data model (contact)
     * @param vcardItem
     * @return
     */
    public JsonItem<String> fromRFC(String jsonRFC) {

        JSONObject jsonRoot = JSONObject.fromObject(jsonRFC);
        JSONObject jsonData = jsonRoot.getJSONObject(JsonContactModel.DATA.getValue());
        JSONObject jsonItem = jsonData.getJSONObject(JsonContactModel.ITEM.getValue());

        JsonItem<String> item = new JsonItem<String>();

        item.setContentType(jsonData.optString(JsonContactModel.CONTENT_TYPE.getValue()));
        item.setKey(jsonItem.optString(JsonContactModel.KEY.getValue()));
        item.setState(jsonItem.optString(JsonContactModel.STATE.getValue()));

        String vcardItem = jsonItem.optString(JsonContactModel.VCARD.getValue());
        vcardItem = vcardItem.replace("\\", "\\\\");

        item.setItem(vcardItem);

        return item;
    }

    /**
     * do nothing
     * @param serverTimeZoneID
     */
    public void setServerTimeZoneID(String serverTimeZoneID) {
    }

}
