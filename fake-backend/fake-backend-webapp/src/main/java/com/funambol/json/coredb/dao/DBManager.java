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
package com.funambol.json.coredb.dao;



import com.funambol.json.utility.Definitions;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbcp.BasicDataSource;


import org.apache.log4j.Logger;

/**
 * This class is used to perform query and update on a database, using
 * a connection pool.
 * 
 * @author Filippo
 */

public class DBManager {
    
     private final static String DRIVER_CLASS = "com.mysql.jdbc.Driver";
     private final static String DB_USER      = "funambol";
     private final static String DB_PWD       = "funambol";
     private final static String DB_URL       = "jdbc:mysql://localhost:3306/funambol";
     
    private final static Logger log           = Logger.getLogger(Definitions.LOG_NAME);
    private static DBManager instance         = new DBManager();

    public static DBManager getManager() {
        if(!instance.isInited())
            instance.initDataSource();
        return instance;
    }

    private BasicDataSource ds;
     
     
    public synchronized void initDataSource() {
        this.configureDatasource(DB_USER, DB_PWD, DB_URL, DRIVER_CLASS);
    }
    
    public boolean deleteItem(String table, Map<String, String> item, String...keys) throws Exception {
        if (ds == null) 
            throw new Exception("Data source is null.");
            
        Connection connection       = null;
        Statement stmt              = null;
        ResultSet rsltSet           = null;
        ResultSetMetaData metadata  = null;
          
        try {
            connection = ds.getConnection();
         } catch (SQLException ex) {
            release(connection,stmt,rsltSet);
            throw new Exception("An error occurred retrieving connection.",ex);
         } 
            
         if (connection == null) {
             throw new Exception("Connection is null.");
         }

         try {
              stmt = connection.createStatement();
         } catch (SQLException ex) {
                release(connection,stmt,rsltSet);
                throw new Exception("An error occurred creating statement.",ex);
            }
            
         String query = prepareDeleteQuery(table,item,keys);
       
         try {
             int affectedRows  = stmt.executeUpdate(query);
             if(affectedRows!=1) {
                 connection.rollback();
                 return false;
             } 
             
             connection.commit();
             return true;
         } catch (SQLException ex) {
             release(connection,stmt,rsltSet);
             throw new Exception("An error occurred executing query ["+query+"].",ex);
         } finally {
                    release(connection,stmt,rsltSet);
            }
    }
    
    public String prepareDeleteQuery(String table,Map<String,String> item,String...keys) {
        String result = "DELETE FROM "+table+" WHERE 1=1 ";
        if(keys!=null && keys.length>0) {
            for(String key:keys) {
                if(key!=null) {
                    String value = item.get(key);
                    if(value!=null)
                        result+="and  "+key+"='"+value+"' ";
                }
            }
        }
        return result;
    }
    
    public List<Map<String, String>> executeQuery(String query) throws Exception {
        return executeQuery(query, (DataFilter[]) null);
    }
    
    public List<Map<String, String>> executeQuery(String query, DataFilter...filters) throws Exception {
            log.info("Executing query [" + query + "].");
            if (ds == null) 
                throw new Exception("Data source is null.");
            
            Connection connection       = null;
            Statement stmt              = null;
            ResultSet rsltSet           = null;
            ResultSetMetaData metadata  = null;

            
            try {
                connection = ds.getConnection();
            } catch (SQLException ex) {
                release(connection,stmt,rsltSet);
                throw new Exception("An error occurred retrieving connection.",ex);
            } 
            
            if (connection == null) {
                throw new Exception("Connection is null.");
            }

            
            try {
                 stmt = connection.createStatement();
            } catch (SQLException ex) {
                release(connection,stmt,rsltSet);
                throw new Exception("An error occurred creating statement.",ex);
            }
            
            
            try {
                 rsltSet  = stmt.executeQuery(query);
                 metadata = rsltSet.getMetaData();
            } catch (SQLException ex) {
                release(connection,stmt,rsltSet);
                throw new Exception("An error occurred executing query ["+query+"].",ex);
            }

            try {
                if(rsltSet!=null) {
                    
                           Map<String,DataFilter> columnNamesFilters = new HashMap<String, DataFilter>();
                           Map<String,DataFilter> columnClassFilters = new HashMap<String, DataFilter>();
                           populateFilterMap(columnNamesFilters,columnClassFilters,filters);
                    
                           List<Map<String, String>> result = new ArrayList<Map<String, String>>();
                           int numberOfColumns = metadata.getColumnCount();
                           while(rsltSet.next()) {
                               Map<String, String> newItem = new HashMap<String, String>();
                               for(int i=1;i<=numberOfColumns;i++) {
                                   String columnName  = metadata.getColumnName(i);
                                   String columnValue = null;
                                   String columnClass = metadata.getColumnClassName(i);
                                   // Retrieving filter bound to column class or column name
                                   DataFilter filter  = null; 
                                   if(!columnNamesFilters.isEmpty()&& columnNamesFilters.containsKey(columnName)) {
                                        filter = columnNamesFilters.get(columnName);
                                   } else if(!columnClassFilters.isEmpty()&& columnClassFilters.containsKey(columnClass))  {
                                       filter = columnClassFilters.get(columnClass);
                                   }
                                   
                                   if(filter!=null) {
                                       Object obj        = rsltSet.getObject(i);
                                       columnValue       = filter.applyFilter(obj);
                                   } else
                                       columnValue      = rsltSet.getString(i);

                                   newItem.put(columnName, columnValue);
                               }
                               result.add(newItem);
                           }
                           return result;
                
              } else
                  throw new Exception("ResultSet is null.");
            } catch (SQLException ex) {
                   throw new Exception("An error occurred creating result list for query ["+query+"].",ex);
            } finally {
                    release(connection,stmt,rsltSet);
            }
            
    }
    
    protected void release(Connection connection, Statement stmt, ResultSet rsltSet) {
        if(rsltSet!=null) 
            try {
                 rsltSet.close();
            } catch (SQLException ex) {
                log.warn("An error occurred closing ResultSet object.",ex);
            }
        if(stmt!=null)
            try {
                stmt.close();
                // @todo chiudere la connessione??
            } catch (SQLException ex) {
                log.warn("An error occurred closing ResultSet object.",ex);
            }
        // @todo chiudere la connessione??
    }

    private boolean isInited() {
        return ds!=null;
    }


    public String getDriver() {
        if(ds!=null)
            return ds.getDriverClassName();
        return null;
    }
    
    public String getUser() {
        if(ds!=null)
            return ds.getUsername();
        return null;
    }
    
    public String getPassword() {
        if(ds!=null)
            return ds.getPassword();
        return null;
    }
    
    public String getUrl() {
        if(ds!=null)
            return ds.getUrl();
        return null;
    }
    
    public void configureDatasource(String user, String password, String url, String driver) {
        ds = new BasicDataSource();
        ds.setDriverClassName(driver==null?"":driver);
        ds.setUsername(user==null?"":user);
        ds.setPassword(password==null?"":password);
        ds.setUrl(url==null?"":url);
        ds.setDefaultAutoCommit(false);
    
    }
    
    
    private void populateFilterMap(Map<String, DataFilter> columnNamesFilters, Map<String, DataFilter> columnClassFilters, DataFilter[] filters) {
        if(filters!=null && filters.length>0) {
            for(DataFilter filter:filters) {
                if(filter!=null) {
                    if(filter.isDefinedForColumnName()) {
                            String[] columnNames = filter.getManagedColumnNames();
                            for(String column:columnNames) {
                                columnNamesFilters.put(column, filter);
                            }
                        }
                    } else {
                        columnClassFilters.put(filter.getManagedClass(), filter);
                    }
            }
        }

    }

}
