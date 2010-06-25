
package com.funambol.json.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;

public class ServletProperties {

    static private Properties properties = null;
    static private Logger logger = null;
    static private String path = null;
    private static final String PROPERTIES_FILE_NAME = "backend.properties";

    static public void setPath(String _path) {
        path = _path;
    }

    private ServletProperties() {
    }

    public static Properties getProperties() {
        if (logger == null)
            logger = Logger.getLogger(Definitions.LOG_NAME);

        if (properties == null) {
            loadMapping();
        }

        return properties;
    }

    /**
     * Load properties from mapping file.
     */
    private static void loadMapping() {

        properties = new Properties();
        FileInputStream mappingFile = null;

        try {
            mappingFile = new FileInputStream(new File(path, PROPERTIES_FILE_NAME));
            properties.load(mappingFile);
        } catch (IOException e) {
            logger.error("Mapping file cannot be read.", e);
//            exit(1, e);
        } catch (IllegalArgumentException e) {
            logger.error("Mapping file contains malformed data.", e);
//            exit(1, e);
        } finally {
            if (mappingFile != null) {
                try {
                    mappingFile.close();
                } catch (IOException e) {
                    logger.error("Mapping file cannot be closed.", e);
//                    exit(1, e);
                }
            }
        }
    }

    public static void saveProperties() {

        FileOutputStream mappingfile;

        try {
            mappingfile = new FileOutputStream(new File(path,PROPERTIES_FILE_NAME));

            properties.store(mappingfile, "Back end properties file");
        } catch (Exception e) {
//            log.error("Could not write to properties file", e);
        }
    }
}