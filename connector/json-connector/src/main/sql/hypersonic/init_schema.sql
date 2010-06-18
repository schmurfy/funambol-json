--
-- Funambol is a mobile platform developed by Funambol, Inc.
-- Copyright (C) 2010 Funambol, Inc.
--
-- This program is free software; you can redistribute it and/or modify it under
-- the terms of the GNU Affero General Public License version 3 as published by
-- the Free Software Foundation with the addition of the following permission
-- added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
-- WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
-- WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
--
-- This program is distributed in the hope that it will be useful, but WITHOUT
-- ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
-- details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program; if not, see http://www.gnu.org/licenses or write to
-- the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
-- MA 02110-1301 USA.
--
-- You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
-- 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
--
-- The interactive user interfaces in modified source and object code versions
-- of this program must display Appropriate Legal Notices, as required under
-- Section 5 of the GNU Affero General Public License version 3.
--
-- In accordance with Section 7(b) of the GNU Affero General Public License
-- version 3, these Appropriate Legal Notices must retain the display of the
-- "Powered by Funambol" logo. If the display of the logo is not reasonably
-- feasible for technical reasons, the Appropriate Legal Notices must display
-- the words "Powered by Funambol".
--

--
-- Initialization data for the JSON Connector module
--

-- -----------------------------------------------------------------------------
-- Module structure registration
--
-- Change the code below with the information that best describe your project.
-- However, pay particular attention to the changes you apply or the server will
-- not be able to reconstruct the module-connector-syncsourcetype hierarchy
--
-- -----------------------------------------------------------------------------
delete from fnbl_module where id='jsonconnector';
insert into fnbl_module (id, name, description)
values('jsonconnector','jsonconnector','json Module');

delete from fnbl_connector where id='jsonconnector';
insert into fnbl_connector(id, name, description, admin_class)
values('jsonconnector','jsonconnector','jsonconnector Connector','com.funambol.json.admin.JsonConnectorConfigPanel');

--
-- SyncSource Types
--

delete from fnbl_sync_source_type where id='contact-json';
insert into fnbl_sync_source_type(id, description, class, admin_class)
values('contact-json','Contact SyncSource','com.funambol.json.engine.source.ContactSyncSource','com.funambol.json.admin.ContactSyncSourceAdminPanel');

delete from fnbl_sync_source_type where id='calendar-json';
insert into fnbl_sync_source_type(id, description, class, admin_class)
values('calendar-json','Appointment and Task SyncSource','com.funambol.json.engine.source.CalendarSyncSource','com.funambol.json.admin.CalendarSyncSourceAdminPanel');

delete from fnbl_sync_source_type where id='note-json';
insert into fnbl_sync_source_type(id, description, class, admin_class)
values('note-json','Note SyncSource','com.funambol.json.engine.source.NoteSyncSource','com.funambol.json.admin.NoteSyncSourceAdminPanel');

--
-- Connector source types
--
delete from fnbl_connector_source_type where connector='jsonconnector' and sourcetype='contact-json';
insert into fnbl_connector_source_type(connector, sourcetype)
values('jsonconnector','contact-json');

delete from fnbl_connector_source_type where connector='jsonconnector' and sourcetype='calendar-json';
insert into fnbl_connector_source_type(connector, sourcetype)
values('jsonconnector','calendar-json');

delete from fnbl_connector_source_type where connector='jsonconnector' and sourcetype='note-json';
insert into fnbl_connector_source_type(connector, sourcetype)
values('jsonconnector','note-json');

--
-- Module - Connector
--
delete from fnbl_module_connector where module='jsonconnector' and connector='jsonconnector';
insert into fnbl_module_connector(module, connector)
values('jsonconnector','jsonconnector');


