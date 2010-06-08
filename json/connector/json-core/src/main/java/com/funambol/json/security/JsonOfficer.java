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
package com.funambol.json.security;

import java.security.Principal;

import com.funambol.framework.core.Authentication;
import com.funambol.framework.core.Cred;
import com.funambol.framework.filter.Clause;
import com.funambol.framework.filter.LogicalClause;
import com.funambol.framework.filter.WhereClause;
import com.funambol.framework.security.AbstractOfficer;
import com.funambol.framework.security.Officer;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.server.Sync4jUser;
import com.funambol.framework.server.inventory.DeviceInventory;
import com.funambol.framework.server.inventory.DeviceInventoryException;
import com.funambol.framework.server.store.NotFoundException;
import com.funambol.framework.server.store.PersistentStore;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.tools.MD5;
import com.funambol.framework.tools.encryption.EncryptionTool;
import com.funambol.json.domain.JsonAuthRequest;
import com.funambol.json.domain.JsonAuthResponse;
import com.funambol.json.exception.BadRequestException;
import com.funambol.json.exception.DaoException;
import com.funambol.json.exception.InternalServerErrorException;
import com.funambol.json.exception.MalformedJsonContentException;
import com.funambol.json.exception.UnauthorizedException;
import com.funambol.json.manager.JsonAuthenticatorManager;
import com.funambol.json.manager.JsonAuthenticatorManagerImpl;
import com.funambol.server.admin.AdminException;
import com.funambol.server.admin.UserManager;
import com.funambol.server.config.Configuration;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class JsonOfficer extends AbstractOfficer
        implements Officer, Serializable {

    //---------------------------------------------------------------- Constants

    private static final long serialVersionUID = 1299049232320758212L;

    private static final String ROLE_USER  = "sync_user";       
    private static final String ROLE_ADMIN = "sync_administrator";       
    
    
    //--------------------------------------------------------------- Properties
    
    private UserManager userManager;
    
    private PersistentStore persistentStore;
    
    private JsonAuthenticatorManager authenticatorManager;

    
    /**
     * This map contains a pair of [device id Pattern]-[builder to use]
     */
    protected Map deviceBuilders = null;

    /**
     * @return Returns the deviceBuilders.
     */
    public Map getDeviceBuilders() {
        return deviceBuilders;
    }

    /**
     * @param deviceBuilders The deviceBuilders to set.
     */
    public void setDeviceBuilders(Map deviceBuilders) {
        this.deviceBuilders = deviceBuilders;
    }
    /**
     * The default builder
     */
    protected String defaultBuilder = null;

    /**
     * @return Returns the defaultBuilder.
     */
    public String getDefaultBuilder() {
        return defaultBuilder;
    }

    /**
     * @param defaultBuilder The defaultBuilder to set.
     */
    public void setDefaultBuilder(String defaultBuilder) {
        this.defaultBuilder = defaultBuilder;
    }
    /**
     * The default sender
     */
    protected String defaultSender = null;

    /**
     * @return Returns the defaultSender.
     */
    public String getDefaultSender() {
        return defaultSender;
    }

    /**
     * @param defaultSender The defaultSender to set.
     */
    public void setDefaultSender(String defaultSender) {
        this.defaultSender = defaultSender;
    }
    /**
     * This map contains a pair of [device id Pattern]-[sender to use]
     */
    protected Map deviceSenders = null;

    /**
     * @return Returns the deviceSenders.
     */
    public Map getDeviceSenders() {
        return deviceSenders;
    }

    /**
     * @param deviceSenders The deviceSenders to set.
     */
    public void setDeviceSenders(Map deviceSenders) {
        this.deviceSenders = deviceSenders;
    }
    
    /**
     * the officer executes the user provisioning for the DS Schema ONLY
     */
    private boolean autoProvisioning = false;

    /**
     * @return the vcardIcalBackend
     */
    public boolean getAutoProvisioning() {
        return autoProvisioning;
    }

    /**
     * @param vcardIcalBackend the vcardIcalBackend to set
     */
    public void setAutoProvisioning(boolean autoProvisioning) {
        this.autoProvisioning = autoProvisioning;
    }    
    
    /**
     * this property will be used when the customer needs to
     * use the unique ID instead of the username that comes from the client;
     * this happens when the username from the client is a sort of alias
     */
    private boolean customerUserIdEnable = false;

    /**
     *
     */
    public boolean getCustomerUserIdEnable() {
        return customerUserIdEnable;
    }

    /**
     *
     */
    public void setCustomerUserIdEnable(boolean _customerUserIdEnable) {
        this.customerUserIdEnable = _customerUserIdEnable;
    }

    /**
     *
     */
    private String customerUserIdLabel = "";

    /**
     *
     */
    public String getCustomerUserIdLabel() {
        return customerUserIdLabel;
    }

    /**
     *
     */
    public void setCustomerUserIdLabel(String _customerUserIdLabel) {
        this.customerUserIdLabel = _customerUserIdLabel;
    }
    
    //----------------------------------------------------------- Public Methods
    
    /**
     * 
     */
    public JsonOfficer() {
        super();        
    }
    

    @Override
    public Sync4jUser authenticateUser(Cred credential) {

        Configuration config = Configuration.getConfiguration();

        this.userManager          = config.getUserManager();
        this.persistentStore      = config.getStore();
        this.authenticatorManager = new JsonAuthenticatorManagerImpl();
        this.authenticatorManager.setCustomerUserIdLabel(customerUserIdLabel);

        JsonUser user = null;

        String type = credential.getType();

        if ((Cred.AUTH_TYPE_BASIC).equals(type)) {
            user = authenticateBasicCredential(credential);
        } else if ((Cred.AUTH_TYPE_MD5).equals(type)) {
            user = authenticateMD5Credential(credential);
        }

        return user;
    }

    @Override
    public void unAuthenticate(Sync4jUser user) {
        super.unAuthenticate(user);
        logout(user);	//logout from json server
    }

    
    //---------------------------------------------------------- Private Methods
    
    /** 
     * @param user the JsonUser to logiut
     */
    private void logout(Sync4jUser user) {
        try {
            JsonUser jsonUser = (JsonUser) user;
            authenticatorManager.logout(jsonUser);
        } catch (ClassCastException e) {
            log.error("The user is not instance of JsonUser, logout aborted! ", e);
        } catch (BadRequestException e) {
            log.error("Bad Request, logout aborted! ", e);
        } catch (UnauthorizedException e) {
            log.error("Unauthorized Exception, logout aborted! ", e);
        } catch (InternalServerErrorException e) {
            log.error("Internal Server Error Exception, logout aborted! ", e);
        } catch (MalformedJsonContentException e) {
            log.error("MalformedJson Content Exception, logout aborted! ", e);
        } catch (Exception e) {
            log.error("Generic Exception, logout aborted! ", e);
        }
    }

    /**
     * @param credential
     * @return
     */
    private JsonUser authenticateBasicCredential(Cred credential) {

        String username, password;

        String credentialType = credential.getType();
        Authentication auth = credential.getAuthentication();
        String deviceId = auth.getDeviceId();
        String userpwd = new String(Base64.decode(auth.getData()));

        int p = userpwd.indexOf(':');

        if (p == -1) {
            username = userpwd;
            password = "";
        } else {
            username = (p > 0) ? userpwd.substring(0, p) : "";
            password = (p == (userpwd.length() - 1)) ? "" : userpwd.substring(p + 1);
        }

        if (log.isTraceEnabled()) {
            log.trace("Username: " + username);
        }


        JsonUser user = null;

        
        // check if he's an admin
        try {
            Sync4jUser userTmp = getUser(username); 
            if (userTmp != null) {
                // it could be a normal user or an admin user
                String[] roles = userTmp.getRoles();
                boolean isAdmin = false;
                for (int i=0; i<roles.length; i++){
                    if (ROLE_ADMIN.equals(roles[i])){
                        isAdmin = true;
                        break;
                    } 
                }
                if (isAdmin){
                    // check the user in the ds-server schema ONLY
                    user = authenticateUserAdmin(username, password, credentialType);
                } else {
                    // check the user in the back-end system and in the ds-server schema
                    user = authenticateUserNormal(username, password, credentialType, deviceId, credential);
                }
            } else {
                // it could be a normal user that must be provisioned (if required)
                // check the user in the back-end system and in the in the ds-server schema
                user = authenticateUserNormal(username, password, credentialType, deviceId, credential);
            }
        } catch (Exception e){
            log.error("Error in the Basic authentication.", e);
        }
        

        return user;

    }

    /**
     * Checks the given MD5 credentials using the appropriate algorithm in
     * according to the given parameter SyncML VerProto
     *
     * @param credentials the cred to check
     *
     * @return the Sync4jUser if the credentials are autenticated, null otherwise
     */
    private JsonUser authenticateMD5Credential(Cred credentials) {

        JsonUser user = null;
        boolean isAuthenticate = true;

        String username = null;
        try {

            Authentication auth = credentials.getAuthentication();
            username = auth.getUsername();

            Sync4jUser s4jUser = getUser(username);

            if (s4jUser == null) {
                if (log.isTraceEnabled()) {
                    log.trace("User '" + username + "' was not retrieved from the DB.");
                }
                return null;
            }

            user = new JsonUser(username);
            user.setPassword(s4jUser.getPassword());

            user.setFirstname(s4jUser.getFirstname());
            user.setLastname(s4jUser.getLastname());
            user.setEmail(s4jUser.getEmail());
            user.setRoles(s4jUser.getRoles());

            // verify in the DS schema
            isAuthenticate = isCredAuthenticate(user, credentials);

            if (!isAuthenticate) {
                return null;
            }

            JsonAuthRequest authRequest = new JsonAuthRequest();
            authRequest.setUser(username);
            authRequest.setPass(s4jUser.getPassword());

            // authenticate the user in the backend server
            JsonAuthResponse authResponse = authenticate(authRequest);
            if (authResponse == null){
                return null;
            }
            String token = authResponse.getSessionID();

            if (token == null || token.length() == 0) {
                if (log.isTraceEnabled()) {
                    log.trace("User not authenticated.");
                }
                return null;
            } else {
                user.setSessionID(token);
                if (log.isTraceEnabled()) {
                    log.trace("User with '" + username + "' is a VALID user; " + " start loading the folder ids");
                }
            }

        } catch (Exception e) {
            log.error("Generic Error in authenticateMD5Credential() methods for the user: " + username, e);
            return null;
        }

        return user;
    }

    /**
     * Checks if the credentials are authenticate
     *
     * @param user the Sync4jUser to check
     * @param cred the credentials to check
     *
     * @return true if the user is a sync_user and if the credentials are
     *         authenticate, false otherwise
     */
    private boolean isCredAuthenticate(Sync4jUser user, Cred cred) {

        if (!isASyncUser(user)) {
            return false;
        }

        Authentication auth = cred.getAuthentication();

        String login = user.getUsername();
        String password = user.getPassword();

        //
        // digest sent by client in format b64
        //
        String msgDigestNonceB64 = auth.getData();
        byte[] clientNonce = auth.getNextNonce().getValue();

        boolean digestOk = false;

        if (isProtocolSyncML10(cred)) {
            digestOk = checkMD5Credential10(msgDigestNonceB64,
                    clientNonce,
                    login,
                    password);
        } else {
            digestOk = checkMD5Credential11(msgDigestNonceB64,
                    clientNonce,
                    login,
                    password);
        }
        if (digestOk) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the digest computed with the given clientNonce, userName,
     * password and the syncml 11 algorithm is equal to the given clientDigest.
     * @param clientDigest the client digest
     * @param clientNonce the nonce to use in the digest computation
     * @param userName the username
     * @param password the password
     * @return true if the clientDigest is right, false otherwise
     */
    private boolean checkMD5Credential11(String clientDigest,
            byte[] clientNonce,
            String userName,
            String password) {
        //
        // Calculate digest
        //
        byte[] userDigest = MD5.digest((new String(userName + ':' + password)).getBytes());
        byte[] userDigestB64 = Base64.encode(userDigest);

        if (log.isTraceEnabled()) {
            log.trace("username: " + userName);
            log.trace("userDigestB64: " + new String(userDigestB64));
            log.trace("clientNonce: " + new String(Base64.encode(clientNonce)));
        }

        //
        // computation of the MD5 digest
        //
        // Creates a unique buffer containing the bytes to digest
        //
        byte[] buf = new byte[userDigestB64.length + 1 + clientNonce.length];

        System.arraycopy(userDigestB64, 0, buf, 0, userDigestB64.length);
        buf[userDigestB64.length] = (byte) ':';
        System.arraycopy(clientNonce, 0, buf, userDigestB64.length + 1,
                clientNonce.length);

        byte[] digest = MD5.digest(buf);

        //
        // encoding digest in Base64 for comparation with digest sent by client
        //
        String serverDigestNonceB64 = new String(Base64.encode(digest));

        if (log.isTraceEnabled()) {
            log.trace("serverDigestNonceB64: " + serverDigestNonceB64);
            log.trace("clientDigest: " + clientDigest);
        }

        return clientDigest.equals(serverDigestNonceB64);
    }

    /**
     * Checks if the digest computed with the given clientNonce, userName,
     * password and the syncml 10 algorithm is equal to the given clientDigest.
     * @param clientDigest the client digest
     * @param clientNonce the nonce to use in the digest computation
     * @param userName the username
     * @param password the password
     * @return true if the clientDigest is right, false otherwise
     */
    private boolean checkMD5Credential10(String clientDigest,
            byte[] clientNonce,
            String userName,
            String password) {
        //
        // Calculate digest
        //
        String usernamepwd = userName + ':' + password;

        byte[] usernamepwdBytes = usernamepwd.getBytes();

        if (log.isTraceEnabled()) {
            log.trace("username: " + userName);
            log.trace("password: " + password);
            log.trace("clientNonce: " + new String(Base64.encode(clientNonce)));
        }

        //
        // computation of the MD5 digest
        //
        // Creates a unique buffer containing the bytes to digest
        //
        byte[] buf = new byte[usernamepwdBytes.length + 1 + clientNonce.length];

        System.arraycopy(usernamepwdBytes, 0, buf, 0, usernamepwdBytes.length);
        buf[usernamepwdBytes.length] = (byte) ':';
        System.arraycopy(clientNonce, 0, buf, usernamepwdBytes.length + 1,
                clientNonce.length);

        byte[] digest = MD5.digest(buf);

        //
        // encoding digest in Base64 for comparation with digest sent by client
        //
        String serverDigestNonceB64 = new String(Base64.encode(digest));

        if (log.isTraceEnabled()) {
            log.trace("serverDigestNonceB64: " + serverDigestNonceB64);
            log.trace("clientDigest: " + clientDigest);
        }

        return clientDigest.equals(serverDigestNonceB64);
    }

    /**
     *
     */
    private boolean isProtocolSyncML10(Cred credential) {
        String syncMLVerProto =
                credential.getAuthentication().getSyncMLVerProto();

        if (log.isTraceEnabled()) {
            log.trace("Check MD5 credential with protocol " + syncMLVerProto);
        }

        //
        // The modality in order to calculate the digest is different to second
        // of the used version of SyncML
        //
        if (syncMLVerProto.indexOf("1.0") != -1) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param authRequest
     * @return
     */
    private JsonAuthResponse authenticate(JsonAuthRequest authRequest) {
        JsonAuthResponse authResponse = null;
        try {
            authResponse = authenticatorManager.login(authRequest);
        } catch (BadRequestException e) {
            log.error("Error authenticating user: " + authRequest.getUser() + " ", e);
        } catch (UnauthorizedException e) {
            log.error("Error authenticating user: " + authRequest.getUser() + " ", e);
        } catch (InternalServerErrorException e) {
            log.error("Error authenticating user: " + authRequest.getUser() + " ", e);
        } catch (MalformedJsonContentException e) {
            log.error("Error authenticating user: " + authRequest.getUser() + " ", e);
        } catch (DaoException e) {
            log.error("Error authenticating user: " + authRequest.getUser() + " ", e);
        }
        return authResponse;
    }

    /**
     * get info from ds schema for the given user
     *
     * @param userName the username
     * @return true if the user exists, false otherwise
     */
    private Sync4jUser getUser(String username) throws Exception {

        Sync4jUser user = null;

        try {
            Sync4jUser[] users;
            WhereClause wc;
            String value[] = new String[]{username};
            wc = new WhereClause("username", value, WhereClause.OPT_EQ, true);

            users = userManager.getUsers(wc);

            if (users.length == 1) {
                user = users[0];
                userManager.getUserRoles(user);
            } else {
                return null;
            }

        } catch (Exception e) {
            throw new Exception("Error getting user in ds-schema ", e);
        }

        return user;
    }

    /**
     * Checks if the given user is a sync_user
     * @param user the user to check
     * @return true if the given user is a sync_user, false otherwise
     */
    private boolean isASyncUser(Sync4jUser user) {
        //
        // Check the roles
        //
        String[] roles = user.getRoles();
        if (roles == null || roles.length == 0) {
            return false;
        }
        for (int i = 0; i < roles.length; i++) {
            if (ROLE_USER.equals(roles[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * check the user in the back-end system and in the AFTER in
     *  the funambolds-server schema
     *
     *
     * @param username
     * @param password
     * @param credentialType
     * @param deviceId
     * @param credential
     * @return
     */
    private JsonUser authenticateUserNormal(String username,
                                            String password,
                                            String credentialType,
                                            String deviceId,
                                            Cred credential) {

        JsonUser user = null;

        try {

            JsonAuthRequest authRequest = new JsonAuthRequest();
            authRequest.setUser(username);
            authRequest.setPass(password);
            
            // authenticate the user in the backend server
            JsonAuthResponse authResponse = authenticate(authRequest);

            if (authResponse == null){
                return null;
            }

            String token = authResponse.getSessionID();

            // this property will be used when the customer needs to
            // use the unique ID instead of the username that comes from the client;
            // this happens when the username from the client is a sort of alias
            if (customerUserIdEnable){
                if (log.isTraceEnabled()) {
                    log.trace("customerUserIdEnable property is true; " +
                            "The backend gives the unique ID that must be used." +
                            "The alias was: " + username);
                }
                username = authResponse.getCustomerUserID();
                if (log.isTraceEnabled()) {
                    log.trace("The new username is: " + username );
                }
            }

            if (token == null || token.length() == 0) {
                if (log.isTraceEnabled()) {
                    log.trace("User not authenticated.");
                }
                return null;

            } else {

                user = new JsonUser(username, password, credentialType);

                user.setSessionID(token);

                if (log.isTraceEnabled()) {
                    log.trace("User with '" + username + "' is a VALID user;");
                }

                // Inserts the user in funambol table if it doesn't exist
                if (autoProvisioning){
                    //
                    // Inserts the user in funambol table if it doesn't exist
                    // the User Provisioning is performed by this Officer and 
                    if (!existsUser(username)) {                       
                        // add user in the DS SCHEMA
                        if (log.isTraceEnabled()) {
                            log.trace("User '" + username + "' not found. A new user will be created");
                        }
                        userManager.insertUser(user);
                        if (log.isTraceEnabled()) {
                            log.trace("User '" + username + "' created");
                        }
                    } else {
                        // update
                        if (log.isTraceEnabled()) {
                            log.trace("User '" + username + "' found. The user will be updated");
                        }
                        userManager.setUser(user);
                        if (log.isTraceEnabled()) {
                            log.trace("User '" + username + "' updated");
                        }                        
                    }                    
                } else {
                    // the User Provisioning is performed by the PAPI
                    if (!existsUser(username)) {
                        if (log.isTraceEnabled()) {
                            log.trace("User '" + username + "' must be provided manually");
                        }
                        return null;
                    }
                }

                if ("portal-ui".equals(deviceId)){
                    // For Portal UI authentication we do not need a principal
                    return user;
                } else {
                    if (existsDevice(deviceId)) {

                        if (log.isTraceEnabled()) {
                            log.trace("Device with '" + deviceId + "' exists.");
                        }

                        // Inserts the principal if it doesn't exist
                        if (!existsPrincipal(username, deviceId)) {
                            long pri = insertPrincipal(username, deviceId);
                            credential.getAuthentication().setPrincipalId(pri);
                        }

                        // update the notification method for the given device
                        Sync4jDevice device = getDevice(deviceId);
                        String notificationSender = getNotificationSender(device);
                        String notificationBuilder = getNotificationBuilder(device);

                        device.setNotificationSender(notificationSender);
                        device.setNotificationBuilder(notificationBuilder);

                        if (log.isInfoEnabled()) {
                            log.info("Notification Sender: " + notificationSender + " and " +
                                    "Notification Builder: " + notificationBuilder +
                                    "for device: " + deviceId);
                        }

                        setDevice(device);

                    } else {
                        if (log.isTraceEnabled()) {
                            log.trace("Device with '" + deviceId + "' doesn't exists " + 
                                    "cannot insert the principal; " + " make the " +
                                    "user " + username + " INVALID.");
                        }
                        return null;
                    }
                }
                
            }

        } catch (PersistentStoreException e) {
            log.error("Error inserting a new user: " + username, e);
            return null;
        } catch (Exception e) {
            log.error("Error checking the user: " + username, e);
            return null;
        }


        return user;

    }

    /**
     * check the user in the back-end system and in the AFTER in
     *  the funambolds-server schema
     *
     *
     * @param username
     * @param password
     * @param credentialType
     * @param deviceId
     * @param credential
     * @return
     */
    private JsonUser authenticateUserAdmin(String username,
            String password,
            String credentialType) {

        JsonUser user = new JsonUser(username, password, credentialType);

        try {

            String[] valueUsername = new String[]{username};
            WhereClause wcUsername = new WhereClause("username", valueUsername, WhereClause.OPT_EQ, true);

            String[] valuePassword = new String[]{EncryptionTool.encrypt(password)};
            WhereClause wcPassword = new WhereClause("password", valuePassword, WhereClause.OPT_EQ, true);

            Clause wc = new LogicalClause(LogicalClause.OPT_AND, new Clause[]{wcUsername, wcPassword});

            Sync4jUser[] users = userManager.getUsers(wc);

            if (users.length != 1) {
                return null;
            }

        } catch (PersistentStoreException e) {
            log.error("Error inserting a new user: " + username, e);
            return null;
        } catch (Exception e) {
            log.error("Error checking the user: " + username, e);
            return null;
        }

        return user;

    }

    /**
     *
     * @param deviceId
     * @return
     * @throws com.funambol.framework.server.inventory.DeviceInventoryException
     */
    private Sync4jDevice getDevice(String deviceId) throws DeviceInventoryException {
        DeviceInventory devInventory = Configuration.getConfiguration().getDeviceInventory();
        Sync4jDevice device = new Sync4jDevice(deviceId);
        devInventory.getDevice(device);
        return device;
    }

    /**
     *
     * @param deviceId
     * @return
     * @throws com.funambol.framework.server.inventory.DeviceInventoryException
     */
    private void setDevice(Sync4jDevice device) throws DeviceInventoryException {
        DeviceInventory devInventory = Configuration.getConfiguration().getDeviceInventory();
        devInventory.setDevice(device);
    }

    /**
     *
     */
    private String getNotificationBuilder(Sync4jDevice device) {

        String deviceId = device.getDeviceId();

        if (deviceBuilders == null) {
            return defaultBuilder;
        }

        Iterator itBuilders = deviceBuilders.keySet().iterator();
        while (itBuilders.hasNext()) {
            String pattern = (String) itBuilders.next();
            if (Pattern.matches(pattern, deviceId)) {
                return (String) deviceBuilders.get(pattern);
            }
        }

        return defaultBuilder;

    }

    /**
     *
     */
    private String getNotificationSender(Sync4jDevice device) {

        String deviceId = device.getDeviceId();

        if (deviceSenders == null) {
            return defaultSender;
        }

        Iterator itSenders = deviceSenders.keySet().iterator();
        while (itSenders.hasNext()) {
            String pattern = (String) itSenders.next();
            if (Pattern.matches(pattern, deviceId)) {
                return (String) deviceSenders.get(pattern);
            }
        }

        return defaultSender;
    }

    private boolean existsDevice(String deviceId) throws DeviceInventoryException {

        boolean exists = false;

        DeviceInventory devInventory = Configuration.getConfiguration().getDeviceInventory();

        exists = devInventory.getDevice(new Sync4jDevice(deviceId), false);

        return exists;
    }

    /**
     * Verify if there is a principal with the given userName and deviceId in
     * store
     * 
     * @param userName
     *                the username
     * @param deviceId
     *                the device's id
     * @throws PersistentStoreException
     *                 if an error occurs
     */
    private boolean existsPrincipal(String userName, String deviceId) throws PersistentStoreException {
        Principal principal;
        try {
            principal = Sync4jPrincipal.createPrincipal(userName, deviceId);
            persistentStore.read(principal);
            return true;
        } catch (NotFoundException e) {
            if (log.isTraceEnabled()) {
                log.trace("Principal for " + userName + ":" + deviceId + " not found");
            }
            return false;
        }
    }

    /**
     * Insert a principal
     * 
     * @param userName
     *                the username
     * @param deviceId
     *                the device's id
     * @return the id of the principal
     * @throws PersistentStoreException
     *                 if an error occurs
     */
    private long insertPrincipal(String userName, String deviceId) throws PersistentStoreException {
        Sync4jPrincipal principal = Sync4jPrincipal.createPrincipal(userName, deviceId);
        try {
            persistentStore.store(principal);
        } catch (PersistentStoreException e) {
            log.error("Error creating new principal: ", e);
            if (log.isTraceEnabled()) {
                log.trace("authenticateBasicCredential", e);
            }
            throw e;
        }
        return principal.getId();
    }

    /**
     * Checks if there is an user with the given username
     *
     * @param userName the username
     * @return true if the user exists, false otherwise
     */
    private boolean existsUser(String userName)
            throws PersistentStoreException, AdminException {

        Sync4jUser[] users;
        WhereClause wc;

        String value[] = new String[]{userName};
        wc = new WhereClause("username", value, WhereClause.OPT_EQ, true);
        users = userManager.getUsers(wc);

        if (users.length > 0) {
            return true;
        }

        return false;

    }
    
}
