package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Product;
import com.example.jb_products_info.entities.build_info.JbDataServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Objects;

import static com.example.jb_products_info.utils.Constants.ERROR_PARSING_UPDATE_XML;

@Service
public class DataSourceImpl implements DataSource {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceImpl.class);

    @Value("${download.build.info.url}")
    private String buildInfoDataDownloadUrl;

    @Override
    public void downloadFileByUrl(String url, String fileLocation) {
        logger.info("downloading update.xml from URL: {}", url);

        try (ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream())) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileLocation)) {
                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                logger.info("download was finished. file location: {}", fileLocation);
            }
        } catch (MalformedURLException e) {
            logger.error("Error with url: {}", url);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error with url", e);
        } catch (IOException e) {
            logger.error("Problem with file downloading and saving");
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Problem with file downloading and saving", e);
        }
    }

    @Override
    public JbDataServiceResponse getJbDataServiceResponseForProduct(Product product) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder.fromUriString(buildInfoDataDownloadUrl)
                        .queryParam("code", product.getCode());

        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");

        String uri = uriBuilder.build().toUriString();

        logger.info("Calling {} for builds info", uri);
        ResponseEntity<List<JbDataServiceResponse>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {
                });

        return Objects.requireNonNull(response.getBody()).get(0);
    }

    @Override
    public FileInputStream getFileInputStream(String filePath){
        try {
            return new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            logger.error("Error finding {}", filePath);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Error finding "+filePath, e);
        }
    }

    @Override
    public XMLEventReader getXmlEventReaderForFile(String filePath){
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        // preventing xxe
        xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        try {
            return xmlInputFactory.createXMLEventReader(getFileInputStream(filePath));
        } catch (XMLStreamException e) {
            logger.error(ERROR_PARSING_UPDATE_XML);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, ERROR_PARSING_UPDATE_XML, e);
        }
    }
}
