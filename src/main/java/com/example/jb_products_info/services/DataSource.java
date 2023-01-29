package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Product;
import com.example.jb_products_info.entities.build_info.JbDataServiceResponse;

import javax.xml.stream.XMLEventReader;
import java.io.FileInputStream;

public interface DataSource
{
    void downloadFileByUrl(String url, String fileLocation);

    JbDataServiceResponse getJbDataServiceResponseForProduct(Product product);

    FileInputStream getFileInputStream(String filePath);

    XMLEventReader getXmlEventReaderForFile(String filePath);
}
