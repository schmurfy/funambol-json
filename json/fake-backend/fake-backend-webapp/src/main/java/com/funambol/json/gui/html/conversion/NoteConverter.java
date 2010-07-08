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
package com.funambol.json.gui.html.conversion;

import com.funambol.common.pim.note.Note;
import com.funambol.common.pim.sif.SIFNParser;
import com.funambol.json.domain.JsonNoteModel;
import com.funambol.json.gui.html.NameValuePair;
import com.funambol.json.util.Utility;
import com.funambol.json.utility.Definitions;
import java.io.ByteArrayInputStream;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

/**
 * Converter for note objects.
 * 
 * @version $Id$
 */

public class NoteConverter implements Converter {

    private final static Logger log = Logger.getLogger(Definitions.LOG_NAME);
    
    // Input format
    public final static String I_VNOTE_FORMAT                 = "IVNOTE";
    public final static String I_PLAINTEXT_FORMAT             = "IPLAINTEXT";
    
    // Input format label
    public final static String I_VNOTE_LABEL                  = "VNote";
    public final static String I_PLAINTEXT_LABEL              = "Plain Text";
    
    
    public final static NameValuePair[] inputFormat            = NameValuePair.parseFromStrings(I_RAW_LABEL,I_RAW_FORMAT,I_SIF_LABEL,I_SIF_FORMAT,I_VNOTE_LABEL,I_VNOTE_FORMAT,I_PLAINTEXT_LABEL,I_PLAINTEXT_FORMAT);//,SIF_FORMAT_LABEL,NOTE_SIF_FORMAT,NOTE_VNOTE_FORMAT_LABEL,NOTE_VNOTE_FORMAT,NOTE_PLAIN_TEXT_FORMAT_LABEL,NOTE_PLAIN_TEXT_FORMAT));
    
    
    
    String contentType = "application/json-note";
    
    public String applyConversion(String inputFormat, String outputFormat, String inputString) throws ConversionException {
        log.debug("Converting form ["+inputFormat+"] to ["+outputFormat+"].");
        if(inputFormat==null)
            throw new ConversionException("Input format cannot be null");
            
        if(I_RAW_FORMAT.equals(inputFormat))
            return inputString;
        
        // Parsing note object
         Note note = null;
         if(I_SIF_FORMAT.equals(inputFormat)) {
             note = sif2note(inputString);
         } else if (I_VNOTE_FORMAT.equals(inputFormat)) {
             note =vnote2note(inputString);
         } else if(I_PLAINTEXT_FORMAT.equals(inputFormat)) {
            note = plaintext2note(inputString);
         }
        
         
         // Translating note object to json string
         if(note!=null)
             return toJSON(note);
        
        throw new ConversionException("IO formats non recognized ["+inputFormat+", "+outputFormat+"]");
    }

    public NameValuePair[] getAvailableInputFormat() {
        return inputFormat;
    }

    public NameValuePair[] getAvailableOutputFormat() {
        return null;
    }

        /**
     * 
     * @param vnote
     * @return
     * @throws java.lang.Exception
     */
    private Note vnote2note(String vnote) throws ConversionException {
        Note note = new Note();
        /*
        ByteArrayInputStream buffer = null;
        VNoteParser parser = null;
        VNote note = null;
        try {
        note = new Note();
        
        buffer = new ByteArrayInputStream(vnote.getBytes());
        if ((vnote.getBytes()).length > 0) {
        parser = new VNoteParser(buffer);
        note = (VNote) parser.VNote();
        }
        } catch (Exception e) {
        throw new Exception("Error converting VNote to Note. ", e);
        }
        
        if (log.isTraceEnabled()) {
        log.trace("Conversion done.");
        }
         */
        return note;
    }

    /**
     * 
     * @param content
     * @param contentType
     * @return
     */
    private Note plaintext2note(String content) {
        Note note = new Note();
        if (content.contains("\n")) {
            note.getSubject().setPropertyValue(content.substring(0, content.indexOf("\n")));
        }
        note.getTextDescription().setPropertyValue(content);
        return note;
    }

    /**
     * 
     * @param sifn
     * @return
     * @throws java.lang.Exception
     */
    private Note sif2note(String sifn) throws ConversionException {

        ByteArrayInputStream buffer = null;
        SIFNParser parser = null;
        Note note = null;
        try {
            note = new Note();
            buffer = new ByteArrayInputStream(sifn.getBytes());
            if ((sifn.getBytes()).length > 0) {
                parser = new SIFNParser(buffer);
                note = (Note) parser.parse();
            }
        } catch (Exception e) {
            throw new ConversionException("Error converting SIF-N to Note. ", e);
        }

        return note;
    }
    
    /* (non-Javadoc)
     * @see com.funambol.json.converter.Converter#toJSON(java.lang.Object)
     */
    public String toJSON(Note note) {

        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();

        jsonData.elementOpt(JsonNoteModel.CONTENT_TYPE.getValue(), contentType);

        jsonItem.elementOpt(JsonNoteModel.KEY.getValue(), null);
        jsonItem.elementOpt(JsonNoteModel.STATE.getValue(), STATE_NEW);

        // folder         
        String folder = Utility.getPropertyValue(note.getFolder());
        if (!folder.equals("")){
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
}
