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

/**
 * A filter can be used to perform conversions loading data from database.
 * A filter must be bound to one or more column names or to a column class type and it
 * will be applied on the column value.
 * 
 * @author Filippo
 */

public abstract class DataFilter<T> {
    
    Class<T>  clazz         = null;
    String[]  columnNames   = null;
    
    /**
     * Builds a new DataFilter object bound to the specified class.
     * 
     * @param clazz is the class managed by this filter, null value is not allowed.
     */

    public DataFilter(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Builds a new DataFilter object bound to the specified column names.
     * 
     * @param columnNames are column names managed by this filter, ther must be at least one not null column name.
     */
     
    
    public DataFilter(Class<T> clazz,String...columnNames) {
        this(clazz);
        this.columnNames = columnNames;
    }

    
    public String applyFilter(Object obj) throws FilterException {
        if(obj==null)
            return "NULL";
        
        if(clazz.isAssignableFrom(obj.getClass())) {
            T item = clazz.cast(obj);
            return object2Representation(item);
        } else
            throw new FilterException("Filter type ["+getManagedClass()+"] is not suitable for object class ["+obj.getClass()+"].");
    }
    
    public abstract String object2Representation(T item) throws FilterException;
    
    //public abstract boolean isFilter object2Representation(T item) throws FilterException;

    
    /**
     * @return the name of the class type managed by this filter.
     */
    public String getManagedClass() {
        return clazz.getName();
    }
    
    public String[] getManagedColumnNames() {
        return columnNames;
    }   
    
    /**
     * This method allows to know wether the filter is defined on column names.
     * 
     * @return true if the filter is bound to column names.
     */
    
    public boolean isDefinedForColumnName() {
        return columnNames!=null && columnNames.length>0;
    }
    

}
