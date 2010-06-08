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

import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.converter.NoteToSIFN;
import com.funambol.common.pim.note.Note;
import com.funambol.common.pim.sif.SIFNParser;
import com.funambol.json.domain.JsonItem;
import java.io.ByteArrayInputStream;
import junit.framework.TestCase;
import net.sf.json.JSONObject;

/**
 *
 * @author sergio
 */
public class NoteConverterTest extends TestCase {

    public NoteConverterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    /**
     * 
     * @throws com.funambol.common.pim.converter.ConverterException
     * @throws java.lang.Exception
     */
    public void testSifNote() throws ConverterException, Exception {
        
        // step 1
        
        String sifn = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<note>\n<SIFVersion>1.1</SIFVersion>" +
                "<Subject>subject</Subject>" +
                "<Body>subject\nbody</Body>" +
                "<Folder/>" +
                "</note>";

        Note note = sif2Note(sifn);

        note.setFolder(new Property(""));
        
        JsonItem<Note> item = new JsonItem<Note>();
        item.setItem(note);        
        NoteConverter instance = new NoteConverter();
        String result = instance.toJSON(item);

        String expectedResult = 
                "{\"data\":{\"content-type\":\"application/json-note\",\"item\":{\"folder\":\"Root\\\\Notes\",\"subject\":\"subject\",\"body\":\"body\"}}}";

        
        //System.out.println("......." + result);
        //System.out.println("......." + expectedResult);
        
        assertEquals(result,expectedResult);

        
        // step 2

        result = "{\"data\":{\"item\":{\"folder\":\"\",\"subject\":\"subject\",\"body\":\"subject\\nbody\"}}}";
        
        item = instance.fromJSON(result);
        
        System.setProperty("file.encoding", "UTF-8");
        NoteToSIFN n2xml = new NoteToSIFN(null,null);
        
        String xml = n2xml.convert((Note) item.getItem());

        String expectedsifn = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<note>\n<SIFVersion>1.1</SIFVersion>" +
                "<Subject>subject</Subject>" +
                "<Body>subject\nbody</Body><Folder/>" +
                "</note>";
        assertEquals(expectedsifn, xml);
    }

    /**
     * 
     */
    public void testMultilineNotes() {
        
        Note note = new Note();
        note.getSubject().setPropertyValue("Subject");
        note.getTextDescription().setPropertyValue("Subject\nline1\nline2");
        note.getFolder().setPropertyValue("12");

        JsonItem<Note> item = new JsonItem<Note>();
        item.setItem(note);
        NoteConverter instance = new NoteConverter();
        
        String result = instance.toJSON(item);
        
        String expectedResult = 
                "{\"data\":" +
                "{\"content-type\":\"application/json-note\",\"item\":" +
                "{" +
                "\"folder\":\"12\"," +
                "\"subject\":\"Subject\"," +
                "\"body\":\"line1\\nline2\"}}}";
        
        assertEquals(result, expectedResult);
    }
    
    
    /**
     * 
     */
    public void testOneLineNotes() {
        
        Note note = new Note();
        note.getSubject().setPropertyValue("Subject");
        note.getTextDescription().setPropertyValue("Subject");
        note.setFolder(new Property("12"));
        
        JsonItem<Note> item = new JsonItem<Note>();
        item.setItem(note);
        NoteConverter instance = new NoteConverter();
        
        String result = instance.toJSON(item);
                
        String expectedResult = 
                "{\"data\":" +
                "{\"content-type\":\"application/json-note\",\"item\":" +
                "{" +
                "\"folder\":\"12\"," +
                "\"subject\":\"Subject\"," +
                "\"body\":\"\"}}}";
        
        assertEquals(result, expectedResult);
    }
    
    
    /**
     * From OUTLOOK to BACKEND
     * 
     * i.e.
     * 
     * ..
     * <Folder>DEFAULT_FOLDER\Funambol</Folder>
     * ..
     * 
     */
    public void test_FolderParser_C2S_00() {
        
        JsonItem<Note> noteItem = new JsonItem<Note>();
        
        noteItem.setContentType("type");
        noteItem.setKey("0");
        noteItem.setState("A");
        
        Note note = new Note();
        
        // the outlook path is
        // \\Personal Folders\Contacts\Funambol
        // the 
        // DEFAULT_FOLDER <==> \\Personal Folders\Contacts        
        note.setFolder(new Property("DEFAULT_FOLDER\\Funambol"));
        
        note.setSubject(new Property("prima nota"));
        
        noteItem.setItem(note);
        
        NoteConverter converter = new NoteConverter();
        String jsonResult = converter.toJSON(noteItem);

        JSONObject jo = JSONObject.fromObject(jsonResult);
        
        JSONObject jodata = jo.getJSONObject("data");
        JSONObject joitem = jodata.getJSONObject("item");
        String folder = joitem.getString("folder");

        //System.out.println("........... from OUTLOOK to BACKEND: '" + folder + "'");
        assertEquals(folder, "Root\\Notes\\Funambol");
        
    }

    /**
     * From WM to BACKEND
     * 
     * the WM doesn't send the property <Folder>
     * 
     */
    public void test_FolderParser_C2S_01() {
        
        JsonItem<Note> noteItem = new JsonItem<Note>();
        
        noteItem.setContentType("type");
        noteItem.setKey("0");
        noteItem.setState("A");
        
        Note note = new Note();
        
        note.setSubject(new Property("prima nota"));
        
        noteItem.setItem(note);
        
        NoteConverter converter = new NoteConverter();
        String jsonResult = converter.toJSON(noteItem);

        JSONObject jo = JSONObject.fromObject(jsonResult);
        
        JSONObject jodata = jo.getJSONObject("data");
        JSONObject joitem = jodata.getJSONObject("item");
        String folder = joitem.optString("folder");

        //System.out.println("........... from WM to BACKEND: '" + folder + "'");
        assertEquals(folder, "Root\\Notes");
    }

    
    /**
     * From BACKEND to clients
     * 
     * the WM doesn't send the property <Folder>
     * i.e.
     * ..
     * <Email1Address>1@funambol.com</Email1Address>
     * <FirstName>Gamma Zuse</FirstName>
     * 
     * ..
     * 
     */
    public void test_FolderParser_S2C_00() {
                        
                
        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonData = new JSONObject();
        JSONObject jsonItem = new JSONObject();
        
        jsonData.elementOpt("content-type", "application/json-note");        
        
        jsonItem.elementOpt("key", "0");
        jsonItem.elementOpt("state", "A");
        jsonItem.elementOpt("folder", "Root\\Notes\\json");        
        jsonItem.elementOpt("subject", "prima nota");
        
        jsonData.element("item", jsonItem);
        jsonRoot.element("data", jsonData);
        
        NoteConverter converter = new NoteConverter();
        JsonItem<Note> noteItem = converter.fromJSON(jsonRoot.toString());

        Note item = noteItem.getItem(); 
        
        String folder = item.getFolder().getPropertyValueAsString();

        //System.out.println("........... from BACKEND to client: '" + folder + "'");
        assertEquals(folder, "DEFAULT_FOLDER\\json");
    }
    
    
    //---------------------------------------------------------- Private Methods
    
    private Note sif2Note (String sifn) throws Exception {
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
            throw new Exception("Error converting SIF-N to Note. ", e);
        }
        return note;
    }
}
