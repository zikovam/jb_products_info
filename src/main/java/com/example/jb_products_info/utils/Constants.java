package com.example.jb_products_info.utils;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    private Constants() {
        //preventing instantiate utility class
    }

    public static final String UPDATE_XML_FILE_PATH = System.getProperty("user.dir") + "/updates.xml";

    public static final String ERROR_FIND_UPDATE_XML = "Can't find update.xml file";
    public static final String ERROR_SAVE_UPDATE_XML = "Error saving update.xml file.";
    public static final String ERROR_PARSING_UPDATE_XML = "Error parsing update.xml file.";

}
