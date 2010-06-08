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

package com.funambol.json.admin;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang.StringUtils;

import com.funambol.admin.AdminException;
import com.funambol.admin.mo.SyncSourceManagementObject;
import com.funambol.admin.ui.SourceManagementPanel;
import com.funambol.framework.engine.source.ContentType;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.engine.source.SyncSourceInfo;
import com.funambol.json.engine.source.ContactSyncSource;


/**
 * This class implements the configuration panel for ContactSyncSource
 *
 */
public class ContactSyncSourceAdminPanel extends SourceManagementPanel
implements Serializable {

    // --------------------------------------------------------------- Constants

    /**
     * Allowed characters for name and uri
     */
    public static final String NAME_ALLOWED_CHARS
    = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_.";

    private static final String TYPE_LABEL_SIFC  = "SIF-C";
    private static final String TYPE_LABEL_VCARD = "VCard";

    private static final String TYPE_SIFC  = "text/x-s4j-sifc";
    private static final String TYPE_VCARD = "text/x-vcard";

    private static final String VERSION_SIFC  = "1.0";
    private static final String VERSION_VCARD = "2.1";
    
    private static final String PANEL_NAME = "Edit Contact SyncSource";

    /** label for the panel's name */
    private JLabel panelName = new JLabel();

    private JLabel           nameLabel          = new JLabel()     ;
    private JTextField       nameValue          = new JTextField() ;
    private JLabel           typeLabel          = new JLabel()     ;
    private JLabel           sourceUriLabel     = new JLabel()     ;
    private JTextField       sourceUriValue     = new JTextField() ;
    private JButton          confirmButton      = new JButton()    ;
    
    private TitledBorder titledBorder;
    private JComboBox typeCombo = new JComboBox();

    /**
     * Creates a new ContactSyncSourceAdminPanel instance
     */
    public ContactSyncSourceAdminPanel() {
        init();
    }


    /**
     * Create the panel
     * @throws Exception if error occures during creation of the panel
     */
    private void init(){
    	// set layout
        this.setLayout(null);
        // set properties of label, position and border
        //  referred to the title of the panel
        titledBorder = new TitledBorder("");
        panelName.setFont(titlePanelFont);
        panelName.setText(getPanelName());
        panelName.setBounds(new Rectangle(14, 5, 316, 28));
        panelName.setAlignmentX(SwingConstants.CENTER);
        panelName.setBorder(titledBorder);

        final int LABEL_X = 14;
        final int VALUE_X = 170;
        int y = 60;
        final int GAP_X = 150;
        final int GAP_Y = 30;

        sourceUriLabel.setText("Source URI: ");
        sourceUriLabel.setFont(defaultFont);
        sourceUriLabel.setBounds(new Rectangle(LABEL_X, y, 150, 18));
        sourceUriValue.setFont(defaultFont);
        sourceUriValue.setBounds(new Rectangle(VALUE_X, y, 350, 18));

        y += GAP_Y; // New line

        nameLabel.setText("Name: ");
        nameLabel.setFont(defaultFont);
        nameLabel.setBounds(new Rectangle(LABEL_X, y, 150, 18));
        nameValue.setFont(defaultFont);
        nameValue.setBounds(new Rectangle(VALUE_X, y, 350, 18));
        y += GAP_Y; // New line

        typeLabel.setText("Type: ");
        typeLabel.setFont(defaultFont);
        typeLabel.setBounds(new Rectangle(LABEL_X, y, 150, 18));
        typeCombo.setFont(defaultFont);
        typeCombo.setBounds(new Rectangle(VALUE_X, y, 350, 18));

        y += GAP_Y; // New line
        int x = LABEL_X;

        confirmButton.setFont(defaultFont);
        confirmButton.setText("Add");
        confirmButton.setBounds(VALUE_X, y, 70, 25);

        // What happens when the confirmButton is pressed?
        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event ) {
                try {
                    validateValues();
                    getValues();
                    if (getState() == STATE_INSERT) {
                        ContactSyncSourceAdminPanel.this.actionPerformed(
                                new ActionEvent(ContactSyncSourceAdminPanel.this,
                                ACTION_EVENT_INSERT, event.getActionCommand()));
                    } else {
                        ContactSyncSourceAdminPanel.this.actionPerformed(
                                new ActionEvent(ContactSyncSourceAdminPanel.this,
                                ACTION_EVENT_UPDATE, event.getActionCommand()));
                    }
                } catch (Exception e) {
                    notifyError(new AdminException(e.getMessage(), e));
                }
            }
        });

        // Adds all components to the panel
        this.add(panelName        , null);
        this.add(nameLabel        , null);
        this.add(sourceUriLabel   , null);
        this.add(sourceUriValue   , null);
        this.add(nameValue        , null);
        this.add(typeLabel        , null);
        this.add(typeCombo        , null);
        this.add(confirmButton    , null);

    }

    /**
     * Updates the panel form with values from the linked SyncSource.
     */
    public void updateForm() {

        if (!(getSyncSource() instanceof ContactSyncSource)) {
            notifyError(
                    new AdminException("This is not a ContactSyncSource! "
                    + "Unable to process SyncSource values."
                    )
                    );
            return;
        }

        if (getState() == STATE_INSERT) {
            confirmButton.setText("Add");
        } else if (getState() == STATE_UPDATE) {
            confirmButton.setText("Save");
        }

        ContactSyncSource syncSource = (ContactSyncSource)getSyncSource();

        sourceUriValue.setText(syncSource.getSourceURI());
        nameValue.setText(syncSource.getName());

        if (syncSource.getSourceURI() != null) {
            sourceUriValue.setEditable(false);
        }

        // Preparing to populate the combo box...
        typeCombo.removeAllItems();
        List types = getTypes();
        if (types == null) {
            types = new ArrayList();
        }
        for (int i=0; i< types.size(); i++) {
            typeCombo.addItem(types.get(i));
        }
        String typeToSelect = getTypeToSelect(syncSource);
        if (typeToSelect != null)  {
            typeCombo.setSelectedItem(typeToSelect);
        } else {
            typeCombo.setSelectedIndex(0);
        }

        SyncSourceManagementObject mo = (SyncSourceManagementObject)getManagementObject();
        
        String transformationsRequired = mo.getTransformationsRequired();

    }

    /**
     * Checks if the values provided by the user are all valid. If they are, the
     * method ends regularly.
     *
     * @throws IllegalArgumentException if name, uri, type or directory are empty
     *                                  (null or zero-length)
     */
    protected void validateValues() throws IllegalArgumentException {
        String value = null;

        value = nameValue.getText();
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException(
                    "Field 'Name' cannot be empty. "
                    + "Please provide a SyncSource name.");
        }

        if (!StringUtils.containsOnly(value, NAME_ALLOWED_CHARS.toCharArray())) {
            throw new IllegalArgumentException(
                    "Only the following characters are allowed for field 'Name':"
                    + "\n" + NAME_ALLOWED_CHARS);
        }

        value = (String)typeCombo.getSelectedItem();
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Field 'Type' cannot be empty. "
                    + "Please provide a SyncSource type.");
        }

        value = sourceUriValue.getText();
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException(
                    "Field 'Source URI' cannot be empty. "
                    + "Please provide a SyncSource URI.");
        }
    }

    /**
     * Sets the SyncSource's properties with the values provided by the user.
     */
    private void getValues() {
        
        ContactSyncSource syncSource = (ContactSyncSource)getSyncSource();

        syncSource.setSourceURI(sourceUriValue.getText().trim());
        syncSource.setName(nameValue.getText().trim());

        String transformationsRequired = null;
        SyncSourceManagementObject mo =
                (SyncSourceManagementObject) getManagementObject();
        mo.setTransformationsRequired(transformationsRequired);

        setSyncSourceInfo(syncSource, (String)typeCombo.getSelectedItem());
    }
    
    /**
     * Returns the panel name
     * @return the panel name
     */
    private String getPanelName() {
        return PANEL_NAME;
    }
    
    /**
     * Returns the available types
     * @return the available types;
     */
    private List getTypes() {
        List supportedTypes = new ArrayList();
        supportedTypes.add(TYPE_LABEL_SIFC);
        supportedTypes.add(TYPE_LABEL_VCARD);
        return supportedTypes;
    }

    /**
     * Returns the type to select based on the given syncsource
     * @return the type to select based on the given syncsource
     */
    private String getTypeToSelect(SyncSource syncSource) {
        String preferredType = null;
        if (syncSource.getInfo() != null &&
            syncSource.getInfo().getPreferredType() != null) {

            preferredType = syncSource.getInfo().getPreferredType().getType();
            if (TYPE_VCARD.equals(preferredType)) {
                return TYPE_LABEL_VCARD;
            }
            if (TYPE_SIFC.equals(preferredType)) {
                return TYPE_LABEL_SIFC;
            }
        }
        return null;
    }
    
    /**
     * Sets the source info of the given syncsource based on the given selectedType
     */
    public void setSyncSourceInfo(SyncSource syncSource, String selectedType) {
        ContactSyncSource pimSource = (ContactSyncSource)syncSource;
        ContentType[] contentTypes = null;
        if (TYPE_LABEL_SIFC.equals(selectedType)) {
            contentTypes = new ContentType[1];
            contentTypes[0] = new ContentType(TYPE_SIFC, VERSION_SIFC);
        } else if (TYPE_LABEL_VCARD.equals(selectedType)) {
            contentTypes = new ContentType[1];
            contentTypes[0] = new ContentType(TYPE_VCARD, VERSION_VCARD);
        }

        pimSource.setInfo(new SyncSourceInfo(contentTypes, 0));
    }
}