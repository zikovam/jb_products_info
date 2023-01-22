package com.example.jb_products_info.utils;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    private Constants() {
        //preventing instantiate utility class
    }

    public static final String UPDATE_FILE_PATH = System.getProperty("user.dir") + "/updates.xml";

}
