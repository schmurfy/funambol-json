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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import org.apache.commons.lang.StringUtils;
import com.funambol.admin.AdminException;
import com.funambol.admin.mo.SyncSourceManagementObject;
import com.funambol.admin.ui.SourceManagementPanel;
import com.funambol.common.pim.calendar.CalendarContent;
import com.funambol.common.pim.calendar.Event;
import com.funambol.common.pim.calendar.Task;
import com.funambol.framework.engine.source.ContentType;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.engine.source.SyncSourceInfo;
import com.funambol.json.engine.source.CalendarSyncSource;

/**
 * This class implements the configuration panel for CalendarSyncSource
 * 
 * @version $Id: CalendarSyncSourceAdminPanel.java 51920 2010-06-04 05:10:49Z gazzaniga $
 */
public class CalendarSyncSourceAdminPanel extends SourceManagementPanel implements Serializable {

    /**
     * Allowed characters for name and uri
     */
    public static final String NAME_ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_.";
    protected static final String TYPE_LABEL_SIFE = "SIF-E";
    protected static final String TYPE_LABEL_SIFT = "SIF-T";
    protected static final String TYPE_LABEL_VCAL = "VCal";
    protected static final String TYPE_LABEL_ICAL = "ICal";
    protected static final String TYPE_LABEL_JSON_EXT = "Json Extended";
    protected static final String TYPE_SIFE = "text/x-s4j-sife";
    protected static final String TYPE_SIFT = "text/x-s4j-sift";
    protected static final String TYPE_VCAL = "text/x-vcalendar";
    protected static final String TYPE_ICAL = "text/calendar";
    protected static final String TYPE_JSON_EXT = "text/jsonextended";
    protected static final String VERSION_SIFE = "1.0";
    protected static final String VERSION_SIFT = "1.0";
    protected static final String VERSION_VCAL = "1.0";
    protected static final String VERSION_ICAL = "2.0";
    protected static final String VERSION_JSONEXT = "";
    /** label for the panel's name */
    private JLabel panelName = new JLabel();
    private final String PANEL_NAME = "Edit Appointment and Task SyncSource";
    /** border to evidence the title of the panel */
    private TitledBorder titledBorder;
    private JLabel nameLabel = new JLabel();
    private JTextField nameValue = new JTextField();
    private JLabel typeLabel = new JLabel();
    private JLabel datastoretypeLabel = new JLabel();
    private JLabel sourceUriLabel = new JLabel();
    private JTextField sourceUriValue = new JTextField();
    private JButton confirmButton = new JButton();
    private JCheckBox eventValue;
    private JCheckBox taskValue;
    private JComboBox typeCombo = new JComboBox();
    private JComboBox datastoretypeCombo = new JComboBox();

    /**
     * Creates a new ContactSyncSourceAdminPanel instance
     */
    public CalendarSyncSourceAdminPanel() {
        init();
    }

    /**
     * Create the panel
     * @throws Exception if error occures during creation of the panel
     */
    private void init() {

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

        typeLabel.setText("Client Type: ");
        typeLabel.setFont(defaultFont);
        typeLabel.setBounds(new Rectangle(LABEL_X, y, 150, 18));
        typeCombo.setFont(defaultFont);
        typeCombo.setBounds(new Rectangle(VALUE_X, y, 350, 18));
        typeCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                updateEntityTypeCheckBoxes();
            }
        });

        y += GAP_Y; // New line

        datastoretypeLabel.setText("Datastore Type: ");
        datastoretypeLabel.setFont(defaultFont);
        datastoretypeLabel.setBounds(new Rectangle(LABEL_X, y, 150, 18));
        datastoretypeCombo.setFont(defaultFont);
        datastoretypeCombo.setBounds(new Rectangle(VALUE_X, y, 350, 18));

        y += GAP_Y; // New line
        int x = LABEL_X;

        y = addExtraComponents(x, y, GAP_X, GAP_Y); // Add other components, if needed

        y += GAP_Y; // New line

        confirmButton.setFont(defaultFont);
        confirmButton.setText("Add");
        confirmButton.setBounds(VALUE_X, y, 70, 25);

        // What happens when the confirmButton is pressed?
        confirmButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                try {
                    validateValues();
                    getValues();
                    if (getState() == STATE_INSERT) {
                        CalendarSyncSourceAdminPanel.this.actionPerformed(
                                new ActionEvent(CalendarSyncSourceAdminPanel.this,
                                ACTION_EVENT_INSERT, event.getActionCommand()));
                    } else {
                        CalendarSyncSourceAdminPanel.this.actionPerformed(
                                new ActionEvent(CalendarSyncSourceAdminPanel.this,
                                ACTION_EVENT_UPDATE, event.getActionCommand()));
                    }
                } catch (Exception e) {
                    notifyError(new AdminException(e.getMessage(), e));
                }
            }
        });

        // Adds all components to the panel
        this.add(panelName, null);
        this.add(nameLabel, null);
        this.add(sourceUriLabel, null);
        this.add(sourceUriValue, null);
        this.add(nameValue, null);
        this.add(typeLabel, null);
        this.add(typeCombo, null);
        this.add(confirmButton, null);
        this.add(eventValue, null);
        this.add(taskValue, null);
        this.add(datastoretypeLabel, null);
        this.add(datastoretypeCombo, null);


    }

    /**
     * Loads the given syncSource showing the name, uri and type in the panel's
     * fields.
     *
     * @param syncSource the SyncSource instance
     */
    public void updateForm() {
        if (!(getSyncSource() instanceof CalendarSyncSource)) {
            notifyError(
                    new AdminException(
                    "This is not an ContactSyncSource! Unable to process SyncSource values."));
            return;
        }
        if (getState() == STATE_INSERT) {
            confirmButton.setText("Add");
        } else if (getState() == STATE_UPDATE) {
            confirmButton.setText("Save");
        }

        CalendarSyncSource syncSource = (CalendarSyncSource) getSyncSource();

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
        for (int i = 0; i < types.size(); i++) {
            typeCombo.addItem(types.get(i));
        }
        String typeToSelect = getTypeToSelect(syncSource);
        if (typeToSelect != null) {
            typeCombo.setSelectedItem(typeToSelect);
        } else {
            typeCombo.setSelectedIndex(0);
        }

        // Preparing to populate the backend combo box...
        datastoretypeCombo.removeAllItems();
        types = getBackendTypes();
        if (types == null) {
            types = new ArrayList();
        }
        for (int i = 0; i < types.size(); i++) {
            datastoretypeCombo.addItem(types.get(i));
        }
        typeToSelect = getBackendTypeToSelect(syncSource);
        if (typeToSelect != null) {
            datastoretypeCombo.setSelectedItem(typeToSelect);
        } else {
            datastoretypeCombo.setSelectedIndex(0);
        }

        updateEntityTypeCheckBoxes();
    }

    /**
     * Checks if the values provided by the user are all valid. In caso of errors,
     * a IllegalArgumentException is thrown.
     *
     * @throws IllegalArgumentException if:
     *         <ul>
     *         <li>name, uri, type or directory are empty (null or zero-length)
     *         <li>the types list length does not match the versions list length
     *         </ul>
     */
    private void validateValues() throws IllegalArgumentException {
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

        value = (String) typeCombo.getSelectedItem();
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

        if (!eventValue.isSelected() && !taskValue.isSelected()) {
            throw new IllegalArgumentException(
                    "Please check at least one between 'Events' and 'Tasks'.");
        }


    }

    /**
     * Set syncSource properties with the values provided by the user.
     */
    private void getValues() {

        CalendarSyncSource syncSource = (CalendarSyncSource) getSyncSource();

        syncSource.setSourceURI(sourceUriValue.getText().trim());
        syncSource.setName(nameValue.getText().trim());

        String transformationsRequired = null;

        SyncSourceManagementObject mo = (SyncSourceManagementObject) getManagementObject();

        mo.setTransformationsRequired(transformationsRequired);

        setSyncSourceInfo(syncSource, (String) typeCombo.getSelectedItem());

        setBackendSyncSourceInfo(syncSource, (String) datastoretypeCombo.getSelectedItem());

        if ((eventValue == null) || (taskValue == null)) {
            return;
        }

        Class entityType;
        if (eventValue.isSelected()) {
            if (taskValue.isSelected()) {
                entityType = CalendarContent.class;
            } else {
                entityType = Event.class;
            }
        } else {
            entityType = Task.class;
        }
        syncSource.setEntityType(entityType);
    }

    /**
     * Updates entities checkboxes according to the selected type
     */
    private void updateEntityTypeCheckBoxes() {
        if (isSIFSelected()) {
            eventValue.setSelected(areEventsAllowed());
            taskValue.setSelected(areTasksAllowed());

            eventValue.setEnabled(false);
            taskValue.setEnabled(false);
        } else {
            CalendarSyncSource syncSource = (CalendarSyncSource) getSyncSource();
            boolean events = false;
            boolean tasks = false;
            if (syncSource.getEntityType() == null
                    || syncSource.getEntityType().isAssignableFrom(Event.class)) {
                events = true;
            }
            if (syncSource.getEntityType() == null
                    || syncSource.getEntityType().isAssignableFrom(Task.class)) {
                tasks = true;
            }
            eventValue.setSelected(events);
            taskValue.setSelected(tasks);

            eventValue.setEnabled(true);
            taskValue.setEnabled(true);
        }
    }

    /**
     * Checks whether a SIF content type is selected.
     *
     * @return true if in the combo box the selected string starts with "SIF"
     */
    protected boolean isSIFSelected() {

        if (typeCombo.getItemCount() == 0) {
            return false;
        }
        return ((String) typeCombo.getSelectedItem()).startsWith("SIF");
    }

    private boolean areEventsAllowed() {
        if (typeCombo.getSelectedItem() == null) {
            return false;
        }

        if (TYPE_LABEL_SIFT.equals((String) typeCombo.getSelectedItem())) {
            return false;
        }
        return true;
    }

    private boolean areTasksAllowed() {
        if (typeCombo.getSelectedItem() == null) {
            return false;
        }

        if (TYPE_LABEL_SIFE.equals((String) typeCombo.getSelectedItem())) {
            return false;
        }
        return true;
    }

    /**
     * Returns the available types
     * @return the available types;
     */
    protected List getTypes() {
        List supportedTypes = new ArrayList();
        supportedTypes.add(TYPE_LABEL_SIFE);
        supportedTypes.add(TYPE_LABEL_SIFT);
        supportedTypes.add(TYPE_LABEL_VCAL);
        supportedTypes.add(TYPE_LABEL_ICAL);
        return supportedTypes;
    }

    /**
     * Returns the available types
     * @return the available types;
     */
    protected List getBackendTypes() {
        List supportedTypes = new ArrayList();
        supportedTypes.add(TYPE_LABEL_JSON_EXT);
        supportedTypes.add(TYPE_LABEL_VCAL);
        supportedTypes.add(TYPE_LABEL_ICAL);
        return supportedTypes;
    }

    /**
     * Returns the type to select based on the given syncsource
     * @return the type to select based on the given syncsource
     */
    protected String getTypeToSelect(SyncSource syncSource) {
        String preferredType = null;
        if (syncSource.getInfo() != null
                && syncSource.getInfo().getPreferredType() != null) {

            preferredType = syncSource.getInfo().getPreferredType().getType();
            if (TYPE_ICAL.equals(preferredType)) {
                return TYPE_LABEL_ICAL;
            }
            if (TYPE_VCAL.equals(preferredType)) {
                return TYPE_LABEL_VCAL;
            }
            if (TYPE_SIFE.equals(preferredType)) {
                return TYPE_LABEL_SIFE;
            }
            if (TYPE_SIFT.equals(preferredType)) {
                return TYPE_LABEL_SIFT;
            }
        }
        return null;
    }

    /**
     * Returns the type to select based on the given syncsource
     * @return the type to select based on the given syncsource
     */
    private String getBackendTypeToSelect(SyncSource syncSource) {
        String preferredType = null;
        CalendarSyncSource pimSource = (CalendarSyncSource) syncSource;
        if (pimSource.getBackendType() != null
                && pimSource.getBackendType().getPreferredType() != null) {

            preferredType = pimSource.getBackendType().getPreferredType().getType();
            if (TYPE_ICAL.equals(preferredType)) {
                return TYPE_LABEL_ICAL;
            }
            if (TYPE_VCAL.equals(preferredType)) {
                return TYPE_LABEL_VCAL;
            }
            if (TYPE_SIFE.equals(preferredType)) {
                return TYPE_LABEL_SIFE;
            }
            if (TYPE_SIFT.equals(preferredType)) {
                return TYPE_LABEL_SIFT;
            }
            if (TYPE_JSON_EXT.equals(preferredType)) {
                return TYPE_LABEL_JSON_EXT;
            }
        }
        return null;
    }

    /**
     * Adds extra components just above the confirm button.
     *
     * @param x horizontal position
     * @param y vertical position
     * @param xGap standard horizontal gap
     * @param yGap standard vertical gap
     * @return the new vertical position
     */
    private int addExtraComponents(int x, int y, int xGap, int yGap) {

        eventValue = new JCheckBox("Events");
        taskValue = new JCheckBox("Tasks");

        eventValue.setFont(defaultFont);
        eventValue.setSelected(true);
        eventValue.setBounds(170, y, 100, 25);
        eventValue.setEnabled(true);

        x += xGap; // Shift a bit to the right

        taskValue.setFont(defaultFont);
        taskValue.setSelected(true);
        taskValue.setBounds(170 + xGap, y, 100, 25);
        taskValue.setEnabled(true);

        return y + yGap;
    }

    /**
     * Returns the panel name
     * @return the panel name
     */
    private String getPanelName() {
        return PANEL_NAME;
    }

    /**
     * Sets the source info of the given syncsource based on the given selectedType
     * @param syncSource the source
     * @param selectedType the selected type
     */
    public void setSyncSourceInfo(SyncSource syncSource, String selectedType) {
        CalendarSyncSource pimSource = (CalendarSyncSource) syncSource;
        ContentType[] contentTypes = null;
        if (TYPE_LABEL_ICAL.equals(selectedType)) {
            contentTypes = new ContentType[2];
            contentTypes[0] = new ContentType(TYPE_ICAL, VERSION_ICAL);
            contentTypes[1] = new ContentType(TYPE_VCAL, VERSION_VCAL);
        } else if (TYPE_LABEL_VCAL.equals(selectedType)) {
            contentTypes = new ContentType[2];
            contentTypes[0] = new ContentType(TYPE_VCAL, VERSION_VCAL);
            contentTypes[1] = new ContentType(TYPE_ICAL, VERSION_ICAL);
        } else if (TYPE_LABEL_SIFE.equals(selectedType)) {
            contentTypes = new ContentType[1];
            contentTypes[0] = new ContentType(TYPE_SIFE, VERSION_SIFE);
        } else if (TYPE_LABEL_SIFT.equals(selectedType)) {
            contentTypes = new ContentType[1];
            contentTypes[0] = new ContentType(TYPE_SIFT, VERSION_SIFT);
        }

        pimSource.setInfo(new SyncSourceInfo(contentTypes, 0));
    }

    /**
     * Sets the backend source info of the given syncsource based on the given selectedType
     */
    public void setBackendSyncSourceInfo(SyncSource syncSource, String selectedType) {
        CalendarSyncSource pimSource = (CalendarSyncSource) syncSource;
        ContentType[] contentTypes = null;
        if (TYPE_LABEL_ICAL.equals(selectedType)) {
            contentTypes = new ContentType[2];
            contentTypes[0] = new ContentType(TYPE_ICAL, VERSION_ICAL);
            contentTypes[1] = new ContentType(TYPE_VCAL, VERSION_VCAL);
        } else if (TYPE_LABEL_VCAL.equals(selectedType)) {
            contentTypes = new ContentType[2];
            contentTypes[0] = new ContentType(TYPE_VCAL, VERSION_VCAL);
            contentTypes[1] = new ContentType(TYPE_ICAL, VERSION_ICAL);
        } else if (TYPE_LABEL_SIFE.equals(selectedType)) {
            contentTypes = new ContentType[1];
            contentTypes[0] = new ContentType(TYPE_SIFE, VERSION_SIFE);
        } else if (TYPE_LABEL_SIFT.equals(selectedType)) {
            contentTypes = new ContentType[1];
            contentTypes[0] = new ContentType(TYPE_SIFT, VERSION_SIFT);
        } else if (TYPE_LABEL_JSON_EXT.equals(selectedType)) {
            contentTypes = new ContentType[1];
            contentTypes[0] = new ContentType(TYPE_JSON_EXT, VERSION_JSONEXT);
        }

        pimSource.setBackendType(new SyncSourceInfo(contentTypes, 0));
    }
}
