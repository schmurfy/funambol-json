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

import com.funambol.common.pim.note.Note;
import com.funambol.framework.tools.merge.MergeResult;
import com.funambol.json.converter.Converter;
import com.funambol.json.dao.JsonDAO;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.exception.DaoException;
import com.funambol.json.exception.HttpException;
import com.funambol.json.exception.MalformedJsonContentException;
import java.util.TimeZone;

/**
 * @version $Id:$
 */
public class JsonNoteManager extends JsonManagerImpl<Note> {
    

    public JsonNoteManager(JsonDAO dao, Converter<JsonItem<Note>> converter) {
        super(dao, converter);
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
    public boolean mergeExtendedItem(String sessionID, JsonItem<Note> serverItem, JsonItem<Note> clientItem, long since) throws DaoException, MalformedJsonContentException {
        try {


            // client item
            Note serverNote = (Note)serverItem.getItem();
            Note clientNote = (Note)clientItem.getItem();

            MergeResult mergeResult = clientNote.merge(serverNote);

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

    public boolean mergeRFCItem(String sessionID, 
                                JsonItem<Note> serverItem,
                                JsonItem<Note> clientItem,
                                long since,
                                boolean vcardIcalBackend,
                                boolean vcalFormat,
                                String rfcType,
                                TimeZone timezone,
                                String charset) throws DaoException, MalformedJsonContentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
/**
     *
     * @param item the note we want to check.
     *
     * @return true if at least one field used for the twin search in the given
     * note contains meaningful data, false otherwise
     */
    public boolean isTwinSearchAppliableOn(JsonItem<Note> item) {
        Note note = (Note)item.getItem();
         return note!=null &&
                  (note.getTextDescription()!=null &&
                   note.getTextDescription().getPropertyValueAsString()!=null &&
                   note.getTextDescription().getPropertyValueAsString().length()>0);
    }
}
