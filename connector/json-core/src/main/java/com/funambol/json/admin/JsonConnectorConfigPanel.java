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

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang.StringUtils;

import com.funambol.admin.AdminException;
import com.funambol.admin.ui.ConnectorManagementPanel;
import javax.swing.JCheckBox;

/**
 * This class implements the connector configuration panel
 * for a Json Connector component
 *
 */
public class JsonConnectorConfigPanel
        extends ConnectorManagementPanel
        implements Serializable {

    public static final String SERVER_NAME_ALLOWED_CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_./:";
    private JTextField serverValue;
    private JButton confirmButton = new JButton();
    private JCheckBox stopSyncOnFatalError = new JCheckBox();

    public JsonConnectorConfigPanel() {
        init();
    }

    /**
     * Create the panel
     */
    private void init() {

        JLabel title, serverLabel;
        JPanel seccPanel;
        JPanel behaviourOnErrorsPanel;

        title = new JLabel();
        seccPanel = new JPanel();
        behaviourOnErrorsPanel = new JPanel();

        serverLabel = new JLabel();
        serverValue = new JTextField();

        setLayout(null);

        title.setFont(titlePanelFont);
        title.setText("Funambol Json Connector");
        title.setBounds(new Rectangle(14, 5, 316, 28));
        title.setAlignmentX(SwingConstants.CENTER);
        title.setBorder(new TitledBorder(""));

        seccPanel.setLayout(null);
        seccPanel.setBorder(new TitledBorder("HTTP Server Configuration"));

        serverLabel.setText("Server:");
        seccPanel.add(serverLabel);
        serverLabel.setBounds(10, 20, 116, 15);
        seccPanel.add(serverValue);
        serverValue.setBounds(150, 20, 220, 19);
        serverValue.setFont(defaultFont);

       
        add(seccPanel);
        seccPanel.setBounds(10, 50, 380, 70);

        //the ssl option panel

        behaviourOnErrorsPanel.setBorder(new TitledBorder("Behaviour on errors"));
        behaviourOnErrorsPanel.setLayout(null);

        stopSyncOnFatalError.setText("Stop sync on fatal errors");
        stopSyncOnFatalError.setBounds(10, 25, 200, 15);

        behaviourOnErrorsPanel.add(stopSyncOnFatalError);

        add(behaviourOnErrorsPanel);
        behaviourOnErrorsPanel.setBounds(10, 170, 380, 60);

        confirmButton.setFont(defaultFont);
        confirmButton.setText("Save");
        add(confirmButton);
        confirmButton.setBounds(160, 250, 70, 25);

        confirmButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                try {
                    validateValues();
                    getValues();
                    JsonConnectorConfigPanel.this.actionPerformed(
                            new ActionEvent(JsonConnectorConfigPanel.this,
                            ACTION_EVENT_UPDATE,
                            event.getActionCommand()));
                } catch (Exception e) {
                    notifyError(new AdminException(e.getMessage()));
                }
            }
        });


        //
        // Setting font...
        //
        Component[] components = getComponents();
        for (int i = 0; (components != null) && (i < components.length); ++i) {
            components[i].setFont(defaultFont);
        }

        //
        // We add it as the last one so that the font won't be changed
        //
        add(title);
    }

    /**
     *
     */
    public void updateForm() {

        JsonConnectorConfig jsonConf = (JsonConnectorConfig) getConfiguration();

        serverValue.setText(jsonConf.getJsonServerUrl());

        stopSyncOnFatalError.setSelected(jsonConf.getStopSyncOnFatalError());
    }

    /**
     * Checks if the values provided by the user are all valid. In case of errors,
     * a IllegalArgumentException is thrown.
     *
     * @throws IllegalArgumentException if:
     *         <ul>
     *         <li>anyvalue is not in the form <host>:<port>
     *         </ul>
     */
    private void validateValues() throws IllegalArgumentException {
        String value;

        // JSON server
        value = serverValue.getText().trim();
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Field 'Server' cannot be empty");
        }


        if (!StringUtils.containsOnly(value, SERVER_NAME_ALLOWED_CHARS.toCharArray())) {
            throw new IllegalArgumentException(
                    "Only the following characters are " +
                    "allowed for field 'SECC Port': \n" +
                    SERVER_NAME_ALLOWED_CHARS);
        }
    }

    /**
     * Set properties with the values provided by the user.
     */
    private void getValues() {

        JsonConnectorConfig jsonConf = (JsonConnectorConfig) getConfiguration();

        jsonConf.setJsonServerUrl((serverValue.getText().trim()));
  
        jsonConf.setStopSyncOnFatalError(stopSyncOnFatalError.isSelected());

    }
}