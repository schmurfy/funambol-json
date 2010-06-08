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
package com.funambol.json.converter;

import net.sf.json.JSONObject;
import com.funambol.common.pim.note.Note;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;
import com.funambol.json.domain.JsonItem;
import com.funambol.json.domain.JsonNoteModel;
import com.funambol.json.util.Utility;


/**
 * @version $Id:$
 */
public class NoteConverter implements Converter<JsonItem<Note>> {

    protected static final FunambolLogger log = FunambolLoggerFactory.getLogger(Utility.LOG_NAME);
    
    public final static int TYPE_ERROR = -1;

    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#toJSON(java.lang.Object)
     */
    public String toJSON(JsonItem<Note> item) {

        Note note = item.getItem();

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        // the content-type for the extended format is 
        jsonData.elementOpt(JsonNoteModel.CONTENT_TYPE.getValue(), Utility.CONTENT_TYPE_NOTE_EXT);

        jsonItem.elementOpt(JsonNoteModel.KEY.getValue(), item.getKey());
        jsonItem.elementOpt(JsonNoteModel.STATE.getValue(), item.getState());

        // folder         
        String folder = Utility.getPropertyValue(note.getFolder());
        if (!"".equals(folder)){
            folder = Utility.folderConverterC2S(folder, Utility.BACKEND_NOTES_FOLDER_PREFIX);
            jsonItem.elementOpt(JsonNoteModel.FOLDER.getValue(), folder);
        } else {
            jsonItem.elementOpt(JsonNoteModel.FOLDER.getValue(), Utility.BACKEND_NOTES_FOLDER_PREFIX);
        }
        
        /**
         * if the note came from a device that does not support subject then
         * we must check if it has more th1n 1 line, if so we must split it and send
         * the first line as subject, the rest of the text should be sent as textdescription.
         * if the content od the note is just one line then it should be used as subject
         */
        String textdescription = Utility.getPropertyValue(note.getTextDescription());
        if (textdescription != null) {
            if (textdescription.contains("\n")) {
                String subject = textdescription.substring(0, textdescription.indexOf("\n"));
                textdescription = textdescription.substring(textdescription.indexOf("\n")+1, textdescription.length());
                if (subject != null && textdescription != null) {
                    jsonItem.elementOpt(JsonNoteModel.SUBJECT.getValue(), subject);
                    jsonItem.elementOpt(JsonNoteModel.BODY.getValue(), textdescription);
                } else {
                    jsonItem.elementOpt(JsonNoteModel.SUBJECT.getValue(), subject);
                    jsonItem.elementOpt(JsonNoteModel.BODY.getValue(), "");
                }
            } else {
                jsonItem.elementOpt(JsonNoteModel.SUBJECT.getValue(), textdescription);
                jsonItem.elementOpt(JsonNoteModel.BODY.getValue(), "");
            }
        }

        jsonData.element(JsonNoteModel.ITEM.getValue(), jsonItem);
        jsonRoot.element(JsonNoteModel.DATA.getValue(), jsonData);

        return jsonRoot.toString();
    }

    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#fromJSON(java.lang.String)
     */
    public JsonItem<Note> fromJSON(String jsonContent) {

        JSONObject jsonRoot = JSONObject.fromObject(jsonContent);

        JSONObject jsonData = jsonRoot.getJSONObject(JsonNoteModel.DATA.getValue());
        JSONObject jsonItem = jsonData.getJSONObject(JsonNoteModel.ITEM.getValue());

        JsonItem<Note> item = new JsonItem<Note>();

        item.setContentType(Utility.getJsonValue(jsonData, JsonNoteModel.CONTENT_TYPE.getValue()));

        item.setKey(Utility.getJsonValue(jsonItem, JsonNoteModel.KEY.getValue()));
        
        item.setState(Utility.getJsonValue(jsonItem, JsonNoteModel.STATE.getValue()));

        Note note = new Note();

        // folder
        String folder = Utility.getJsonValue(jsonItem, JsonNoteModel.FOLDER.getValue());
        if (folder!=null){
            folder = Utility.folderConverterS2C(folder, Utility.BACKEND_NOTES_FOLDER_PREFIX);
            note.getFolder().setPropertyValue(folder);
        }
        
        String subject = Utility.getJsonValue(jsonItem, JsonNoteModel.SUBJECT.getValue());
        note.getSubject().setPropertyValue(subject);

        /**
         * If a note is created from the user interface then the returned body will contain the subject\nbody
         * but if the note was created using the API only the body is returned
         * we must keep in mind that some devices don't support subject, so all the note info must go in the
         * text description
         * If backend fixes this issue we should set the received subject "as is" and
         * set the textdescrption as subject\ntextdescription
         */
        String textdescription = jsonItem.optString(JsonNoteModel.BODY.getValue(), null);
        if (textdescription != null) {
            if (subject != null && !textdescription.startsWith(subject)) {
                StringBuilder builder = new StringBuilder();
                builder.append(subject).append("\n").append(textdescription);
                note.getTextDescription().setPropertyValue(builder.toString());
            } else {
                note.getTextDescription().setPropertyValue(textdescription);
            }
        } else {
            if (subject == null) {
                note.getTextDescription().setPropertyValue("");
            } else {
                note.getTextDescription().setPropertyValue(subject);
            }
        }
        item.setItem(note);

        return item;
    }

    /**
     * required by the interface but never used
     * @param item
     * @return
     * @throws java.lang.Exception
     */
    public String toRFC(JsonItem<String> item) {
        return null;
    }

    /**
     * required by the interface but never used
     * @param vcardItem
     * @return
     */
    public JsonItem<String> fromRFC(String jsonRFC) {
        return null;
    }    
    
    
    /**
     * do nothing
     * @param serverTimeZoneID
     */
    public void setServerTimeZoneID(String serverTimeZoneID) {
    }    

}
