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
package com.funambol.json.abstractServlet;

import com.funambol.framework.tools.DBTools;
import com.funambol.framework.tools.DataSourceTools;
import com.funambol.server.db.DataSourceContextHelper;
import com.funambol.server.db.RoutingDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.servlet.Servlet;

import junit.framework.TestCase;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public abstract class AbstractHttpTransportTest extends TestCase {

    protected Server server;

    static {
        try {
            System.setProperty("java.naming.factory.initial", "org.apache.naming.java.javaURLContextFactory");
            DataSourceContextHelper.configureAndBindDataSources();
        } catch (Exception e){
            System.out.println("ERROR ....  " + e.getMessage());
        }
    }


    protected static final String JNDI_CORE_DB = "jdbc/fnblcore";
    protected static final String JNDI_USER_DB = "jdbc/fnbluser";
    protected static final String USER_PART1 = "userdb";

	protected AbstractHttpTransportTest(Servlet servlet) {

        server = new Server();

        //SslSocketConnector sslConnector = new SslSocketConnector();
        //String pathKeystore = System.getProperty("funambol.home");
        //pathKeystore = pathKeystore + "/keystore";
        //sslConnector.setKeystore(pathKeystore);
        //sslConnector.setKeyPassword("changeit"); //test key password
        //sslConnector.setPort(8442);
                
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(com.funambol.json.util.Utility.PORT_DEFAULT);		
        server.setConnectors(new Connector[] { connector });
		Context context = new Context(server, "/", Context.SESSIONS);
		context.addServlet(new ServletHolder(servlet), "/*");
	}

    /**
     *
     * @throws java.lang.Exception
     */
    @Override
	protected void setUp() throws Exception {
        System.setProperty("file.encoding", "UTF-8");
		super.setUp();
		server.start();
        setUpFakeCoreDatabase();
        setUpFakeUserDatabase();
	}

    /**
     *
     * @throws java.lang.Exception
     */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		server.stop();
        tearDownFakeCoreDatabase();
        tearDownFakeUserDatabase();

	}

    //---------------------------------------------------------- Private Methods

    private Connection getConnectionToDB() throws NamingException, SQLException, ClassNotFoundException,
           IllegalAccessException, InstantiationException {
        return DataSourceTools.lookupDataSource(JNDI_CORE_DB).getConnection();
    }

    /**
     * create a fake table for a fake DB
     *
     * @throws java.lang.Exception
     */
    private void setUpFakeCoreDatabase() throws Exception {

        String CREATE_FAKE_FNBL_USER =
                "create table fnbl_user ("+
                "  username   varchar(255) not null,"+
                "  password   varchar(255) not null,"+
                "  email      varchar(255),"+
                "  first_name varchar(255),"+
                "  last_name  varchar(255),"+
                "  constraint pk_user primary key (username)"+
                ");";

        String CREATE_FAKE_FNBL_ID =
                "create table fnbl_id (" +
                "  idspace      varchar(30) not null," +
                "  counter      bigint      not null," +
                "  increment_by int         default 100," +
                "  constraint pk_id primary key (idspace)" +
                ");";

        String CREATE_FAKE_FNBL_ROLE =
                "create table fnbl_role (" +
                "  role        varchar(128) not null," +
                "  description varchar(200) not null," +
                "  constraint pk_role primary key (role)" +
                ");" ;


        String CREATE_FAKE_FNBL_USER_ROLE =
                "create table fnbl_user_role (" +
                "  username varchar(255) not null," +
                "  role     varchar(128) not null, " +
                "  constraint pk_user_role primary key (username,role)," +
                "  constraint fk_userrole foreign key (username)" +
                "  references fnbl_user (username) on delete cascade on update cascade" +
                ");" ;


        String INSERT_USER_1 = "insert into fnbl_user (username, password, email) values ('guest', '65GUmi03K6o=', 'guest@funambol.com');";

        String INSERT_USER_2 = "insert into fnbl_role values('sync_user','User');";

        String INSERT_USER_3 = "insert into fnbl_role values('sync_administrator','Administrator');";


        String INSERT_USER_5 = "insert into fnbl_user_role values('guest','sync_user');";


        Connection conn = null;
        PreparedStatement stmt = null;

        try {

            conn = getConnectionToDB();

            stmt = conn.prepareStatement(CREATE_FAKE_FNBL_USER);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(CREATE_FAKE_FNBL_USER_ROLE);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(CREATE_FAKE_FNBL_ROLE);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(CREATE_FAKE_FNBL_ID);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(INSERT_USER_1);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(INSERT_USER_2);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(INSERT_USER_3);
            stmt.execute();
            DBTools.close(null, stmt, null);


            stmt = conn.prepareStatement(INSERT_USER_5);
            stmt.execute();
            DBTools.close(null, stmt, null);


        } catch (Exception e) {
            System.out.println("error " + e.getMessage());
        } finally {
            DBTools.close(conn, stmt, null);
        }

    }

    /**
     *
     * @throws java.lang.Exception
     */
     private void tearDownFakeCoreDatabase() throws Exception {


        String DROP_FAKE_FNBL_ID = "drop table fnbl_id;";

        String DROP_FAKE_FNBL_USER_ROLE = "drop table fnbl_user_role;";

        String DROP_FAKE_FNBL_ROLE = "drop table fnbl_role;";

        String DROP_FAKE_FNBL_USER = "drop table fnbl_user;";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {

            conn = getConnectionToDB();

            stmt = conn.prepareStatement(DROP_FAKE_FNBL_ID);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(DROP_FAKE_FNBL_USER_ROLE);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(DROP_FAKE_FNBL_ROLE);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(DROP_FAKE_FNBL_USER);
            stmt.execute();
            DBTools.close(null, stmt, null);

        } catch (Exception e) {
            System.out.println("error " + e.getMessage());
        } finally {
            DBTools.close(conn, stmt, null);
        }
     }


    /**
     * create a fake table for a fake DB
     *
     * @throws java.lang.Exception
     */
    private void setUpFakeUserDatabase() throws Exception {


        String CREATE_FAKE_FNBL_PIM_CONTACT =
                "create table fnbl_pim_contact ("+
                "    id              bigint PRIMARY KEY,"+
                "    userid          varchar(255),"+
                "    last_update     bigint,"+
                "    status          char,"+
                "    photo_type      smallint,"+
                "    importance      smallint,"+
                "    sensitivity     smallint,"+
                "    subject         varchar(255),"+
                "    folder          varchar(255),"+
                "    anniversary     varchar(16),"+
                "    first_name      varchar(64),"+
                "    middle_name     varchar(64),"+
                "    last_name       varchar(64),"+
                "    display_name    varchar(128),"+
                "    birthday        varchar(16),"+
                "    body            varchar(255),"+
                "    categories      varchar(255),"+
                "    children        varchar(255),"+
                "    hobbies         varchar(255),"+
                "    initials        varchar(16),"+
                "    languages       varchar(255),"+
                "    nickname        varchar(64),"+
                "    spouse          varchar(128),"+
                "    suffix          varchar(32),"+
                "    title           varchar(32),"+
                "    gender          char(1),"+
                "    assistant       varchar(128),"+
                "    company         varchar(255),"+
                "    department      varchar(255),"+
                "    job_title       varchar(128),"+
                "    manager         varchar(128),"+
                "    mileage         varchar(16),"+
                "    office_location varchar(64),"+
                "    profession      varchar(64),"+
                "    companies       varchar(255)"+
                ");";

        String CREATE_FAKE_FNBL_PIM_CONTACT_ITEM =
                "create table fnbl_pim_contact_item ("+
                "    contact      bigint,"+
                "    type         smallint,"+
                "    value        varchar(255),"+
                "    PRIMARY KEY (contact, type),"+
                "    FOREIGN KEY (contact) REFERENCES fnbl_pim_contact ON DELETE CASCADE"+
                ");";

        String CREATE_FAKE_FNBL_PIM_CONTACT_ADDRESS =
                "create table fnbl_pim_address ("+
                "    contact          bigint, "+
                "    type             smallint,"+
                "    street           varchar(128),"+
                "    city             varchar(64),"+
                "    state            varchar(64),"+
                "    postal_code      varchar(16),"+
                "    country          varchar(32),"+
                "    po_box           varchar(16),"+
                "    extended_address varchar(255),"+
                "    PRIMARY KEY (contact, type),"+
                "    FOREIGN KEY (contact) REFERENCES fnbl_pim_contact ON DELETE CASCADE"+
                ");";

        String CREATE_FAKE_FNBL_PIM_CONTACT_PHOTO =
                "create table fnbl_pim_contact_photo ("+
                "    contact      bigint,"+
                "    type         varchar(64),"+
                "    photo        binary,"+
                "    url          varchar(255),"+
                "    PRIMARY KEY (contact),"+
                "    FOREIGN KEY (contact) REFERENCES fnbl_pim_contact ON DELETE CASCADE"+
                ");";


        RoutingDataSource userDS = (RoutingDataSource)DataSourceTools.lookupDataSource(JNDI_USER_DB);
        Connection conn = null;
        PreparedStatement stmt = null;

        try {

            conn = userDS.getRoutedConnection(USER_PART1);

            stmt = conn.prepareStatement(CREATE_FAKE_FNBL_PIM_CONTACT);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(CREATE_FAKE_FNBL_PIM_CONTACT_ITEM);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(CREATE_FAKE_FNBL_PIM_CONTACT_ADDRESS);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(CREATE_FAKE_FNBL_PIM_CONTACT_PHOTO);
            stmt.execute();
            DBTools.close(null, stmt, null);

        } catch (Exception e) {
            System.out.println("error " + e.getMessage());
        } finally {
            DBTools.close(conn, stmt, null);
        }

    }

    /**
     *
     * @throws java.lang.Exception
     */
     private void tearDownFakeUserDatabase() throws Exception {

        // contacts
        String DROP_FAKE_FNBL_PIM_CONTACT =
                "drop table fnbl_pim_contact;";

        String DROP_FAKE_FNBL_PIM_CONTACT_ITEM =
                "drop table fnbl_pim_contact_item;";

        String DROP_FAKE_FNBL_PIM_CONTACT_ADDRESS =
                "drop table fnbl_pim_address;";

        String DROP_FAKE_FNBL_PIM_CONTACT_PHOTO =
                "drop table fnbl_pim_contact_photo;";

        RoutingDataSource userDS = (RoutingDataSource)DataSourceTools.lookupDataSource(JNDI_USER_DB);
        Connection conn = null;
        PreparedStatement stmt = null;


        try {

            conn = userDS.getRoutedConnection(USER_PART1);

            stmt = conn.prepareStatement(DROP_FAKE_FNBL_PIM_CONTACT_ITEM);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(DROP_FAKE_FNBL_PIM_CONTACT_ADDRESS);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(DROP_FAKE_FNBL_PIM_CONTACT_PHOTO);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(DROP_FAKE_FNBL_PIM_CONTACT);
            stmt.execute();
            DBTools.close(null, stmt, null);

        } catch (Exception e) {
            System.out.println("error " + e.getMessage());
        } finally {
            DBTools.close(conn, stmt, null);
        }
     }


    /**
     * create a fake table for a fake DB
     *
     * @throws java.lang.Exception
     */
    public void setUpTest_AuthenticateUser_2(String user) throws Exception {

        String DELETE_FAKE_FNBL_USER =
                "delete from fnbl_user where username = 'guest';";

        String INSERT_USER_1 = "insert into fnbl_user (username, password, email) " +
                "values ('"+user+"', '65GUmi03K6o=', 'guest@funambol.com');";

        String INSERT_USER_5 = "insert into fnbl_user_role values('"+user+"','sync_user');";


        Connection conn = null;
        PreparedStatement stmt = null;

        try {


            conn = getConnectionToDB();

            stmt = conn.prepareStatement(DELETE_FAKE_FNBL_USER);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(INSERT_USER_1);
            stmt.execute();
            DBTools.close(null, stmt, null);

            stmt = conn.prepareStatement(INSERT_USER_5);
            stmt.execute();
            DBTools.close(null, stmt, null);


        } catch (Exception e) {
            System.out.println("error " + e.getMessage());
        } finally {
            DBTools.close(conn, stmt, null);
        }

    }


}
