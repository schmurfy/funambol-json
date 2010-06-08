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
package com.funambol.json.manager;

import com.funambol.common.pim.contact.*;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;
import com.funambol.framework.tools.merge.MergeResult;
import com.funambol.json.converter.Converter;
import com.funambol.json.dao.JsonDAO;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.engine.source.UtilitySyncSource;
import com.funambol.json.exception.DaoException;
import com.funambol.json.exception.HttpException;
import com.funambol.json.exception.MalformedJsonContentException;
import com.funambol.json.util.Utility;
import java.util.TimeZone;
import java.util.List;

public class JsonContactManager extends JsonManagerImpl<Contact> {

    protected static final FunambolLogger log = FunambolLoggerFactory.getLogger(Utility.LOG_NAME);
    protected static final String FIELD_EMAIL_1_ADDRESS = "Email1Address";
    protected static final String FIELD_EMAIL_2_ADDRESS = "Email2Address";
    protected static final String FIELD_EMAIL_3_ADDRESS = "Email3Address";

    public JsonContactManager(JsonDAO dao, Converter<JsonItem<Contact>> converter) {
        super(dao, converter);
    }

    /*
    @Override
    protected Map<String, String> extractInformation(JsonItem<Contact> item) {

    Map<String, String> parameters = new HashMap<String, String>();

    // get first name
    String firstName = item.getItem().getName().getFirstName().getPropertyValueAsString();
    if (firstName != null) {
    parameters.put(JsonContactModel.FIRSTNAME.getValue(), firstName);
    }else {
    parameters.put(JsonContactModel.FIRSTNAME.getValue(), "");
    }

    // get last name
    String lastName  = item.getItem().getName().getLastName().getPropertyValueAsString();
    if (lastName != null) {
    parameters.put(JsonContactModel.LASTNAME.getValue(), lastName);
    } else {
    parameters.put(JsonContactModel.LASTNAME.getValue(), "");
    }

    // get the first email
    List emails = item.getItem().getPersonalDetail().getEmails();
    if (emails.size()>0){
    String email = ((Email) emails.get(0)).getPropertyValueAsString();
    if (email != null) {
    parameters.put(JsonContactModel.EMAIL.getValue(), email);
    } else {
    parameters.put(JsonContactModel.EMAIL.getValue(), "");
    }
    }

    // get company
    String company   = item.getItem().getBusinessDetail().getCompany().getPropertyValueAsString();
    if (company != null) {
    parameters.put(JsonContactModel.COMPANY.getValue(), company);
    } else {
    parameters.put(JsonContactModel.COMPANY.getValue(), "");
    }

    return parameters;

    }
     */
    /**
     * merges 2 items, and updates (if needed) the item in the backend
     * @param sessionID
     * @param serverItem
     * @param clientItem
     * @param since
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public boolean mergeExtendedItem(String sessionID, JsonItem<Contact> serverItem, JsonItem<Contact> clientItem, long since) throws DaoException, MalformedJsonContentException {
        try {

            // client item
            Contact serverContact = (Contact) serverItem.getItem();
            Contact clientContact = (Contact) clientItem.getItem();

            MergeResult mergeResult = clientContact.merge(serverContact);

            if (log.isTraceEnabled()) {
                log.trace("Merge procedure end. MergeResult: " + mergeResult);
            }

            if (mergeResult.isSetBRequired()) {
                updateExtendedItem(sessionID, serverItem, since);
            }

            return mergeResult.isSetARequired();

        } catch (HttpException re) {
            log.error("Failed the connection to the Json backend", re);
            throw new DaoException(re.getMessage(), re);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        }
    }

    /**
     * merges 2 items, and updates (if needed) the item in the backend
     * @param sessionID
     * @param serverItem
     * @param clientItem
     * @param since
     * @return
     * @throws com.funambol.json.exception.DaoException
     * @throws com.funambol.json.exception.MalformedJsonContentException
     */
    public boolean mergeRFCItem(String sessionID,
            JsonItem<Contact> serverItem,
            JsonItem<Contact> clientItem,
            long since,
            boolean vcardIcalBackend,
            boolean vcalFormat,
            String rfcType,
            TimeZone timezone,
            String charset) throws DaoException, MalformedJsonContentException {
        try {

            // client item
            Contact serverContact = (Contact) serverItem.getItem();
            Contact clientContact = (Contact) clientItem.getItem();

            MergeResult mergeResult = clientContact.merge(serverContact);

            if (log.isTraceEnabled()) {
                log.trace("Merge procedure end. MergeResult: " + mergeResult);
            }

            if (mergeResult.isSetBRequired()) {

                String objRFC = UtilitySyncSource.contact2vcard(serverContact, timezone, charset);

                JsonItem<String> eventItem = new JsonItem<String>();

                String id = serverItem.getKey();
                eventItem.setKey(id);
                eventItem.setItem(objRFC);
                eventItem.setContentType(rfcType);
                eventItem.setState(String.valueOf(serverItem.getState()));
                updateRFCItem(sessionID, eventItem, since);

            }

            return mergeResult.isSetARequired();

        } catch (HttpException re) {
            log.error("Failed the connection to the Json backend", re);
            throw new DaoException(re.getMessage(), re);
        } catch (RuntimeException re) {
            log.error(re.getMessage(), re);
            throw new MalformedJsonContentException("The Json content is malformed!", re);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new MalformedJsonContentException("The Json content is malformed!", ex);
        }
    }

    /**
     *
     * @param item the contact we want to check.
     *
     * @return true if at least one field used for the twin search in the given
     * contact contains meaningful data, false otherwise
     */
    public boolean isTwinSearchAppliableOn(JsonItem<Contact> item) {
        Email email = null;
        String emailAddress = null;
        String emailAddressHome = null;
        String emailAddressWork = null;
        Contact c = (Contact) item.getItem();

        List emailsPersonal = c.getPersonalDetail().getEmails();
        if (emailsPersonal != null) {
            String emailType = null;
            for (int i = 0, l = emailsPersonal.size(); i < l; i++) {
                email = (Email) emailsPersonal.get(i);
                emailType = email.getEmailType();
                if ((FIELD_EMAIL_1_ADDRESS).equals(emailType)) {
                    emailAddress = email.getPropertyValueAsString();
                } else if ((FIELD_EMAIL_2_ADDRESS).equals(emailType)) {
                    emailAddressHome = email.getPropertyValueAsString();
                }
            }
        }

        List emailsBusiness = c.getBusinessDetail().getEmails();
        if (emailsBusiness != null) {
            for (int i = 0, l = emailsBusiness.size(); i < l; i++) {
                email = (Email) emailsBusiness.get(i);
                if ((FIELD_EMAIL_3_ADDRESS).equals(email.getEmailType())) {
                    emailAddressWork = email.getPropertyValueAsString();
                }
            }
        }

        String firstName =
                c.getName().getFirstName().getPropertyValueAsString();
        String lastName =
                c.getName().getLastName().getPropertyValueAsString();
        String displayName =
                c.getName().getDisplayName().getPropertyValueAsString();
        String companyName = null;
        if (c.getBusinessDetail().getCompany() != null) {
            companyName = c.getBusinessDetail().getCompany().getPropertyValueAsString();
        }

        firstName = normalizeField(firstName);
        lastName = normalizeField(lastName);
        displayName = normalizeField(displayName);
        emailAddress = normalizeField(emailAddress);
        emailAddressHome = normalizeField(emailAddressHome);
        emailAddressWork = normalizeField(emailAddressWork);
        companyName = normalizeField(companyName);


        return (firstName.length() > 0
                || lastName.length() > 0
                || emailAddress.length() > 0
                || emailAddressHome.length() > 0
                || emailAddressWork.length() > 0
                || companyName.length() > 0
                || displayName.length() > 0);

    }

    /**
     *
     *
     * @param fieldValue the value of the field to normalize
     *
     * @return the normalized field, the field itself if it's not null
     */
    private String normalizeField(String fieldValue) {
        if (fieldValue == null || ("null".equals(fieldValue))) {
            return "";
        }
        return fieldValue;
    }
}
