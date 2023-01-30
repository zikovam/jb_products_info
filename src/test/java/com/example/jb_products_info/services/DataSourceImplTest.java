package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Product;
import com.example.jb_products_info.entities.build_info.JbDataServiceResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DataSourceImplTest {
    @InjectMocks
    DataSourceImpl dataSource;
    public static WireMockServer wireMockRule = new WireMockServer(options().port(9090));

    @BeforeAll
    public static void beforeAll() {
        wireMockRule.start();
    }

    @BeforeEach
    void beforeEach(){
        ReflectionTestUtils.setField(dataSource, "buildInfoDataDownloadUrl","http://localhost:9090/products");
    }

    @AfterAll
    public static void afterAll() {
        wireMockRule.stop();
    }
    @AfterEach
    public void afterEach() {
        wireMockRule.resetAll();
    }

    @Test
    void testGetJbDataServiceResponseForProduct_allOk() {
        // Given
        String code = "IU";
        wireMockRule.stubFor(get(urlEqualTo("/products?code=" + code))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("IU_buildInfo.json")
                        ));

        Product product = Product.builder()
                .code(code)
                .build();
        // When
        JbDataServiceResponse response = dataSource.getJbDataServiceResponseForProduct(product);

        // Then
        wireMockRule.verify(
                getRequestedFor(urlEqualTo("/products?code=" + code))
        );
        assertNotNull(response);
        assertEquals(code, response.getIntellijProductCode());
        assertEquals("IntelliJ IDEA Ultimate", response.getName());
        assertEquals(424, response.getReleases().size());
        assertEquals(424, response.getReleases().size());
        assertEquals("231.5920.14", response.getReleases().get(0).getBuild());
        assertEquals(LocalDate.of(2023, 1, 27), response.getReleases().get(0).getDate());
        assertEquals("https://download.jetbrains.com/idea/ideaIU-231.5920.14.tar.gz",
                response.getReleases().get(0).getDownloads().getLinux().getLink());
    }

    @Test
    void testDownloadFileByUrl_downloadSuccessfully(){
        // Given
        String url = "http://localhost:9090/updates/updates.xml";
        wireMockRule.stubFor(get(urlEqualTo("/updates/updates.xml"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                        .withBodyFile("update.xml")
                ));

        // When
        String downloadedFileLocation = "src/test/resources/__files/downloaded_update.xml";
        dataSource.downloadFileByUrl(url, downloadedFileLocation);

        // Then
        wireMockRule.verify(
                getRequestedFor(urlEqualTo("/updates/updates.xml"))
        );
        File downloadedFile = new File(downloadedFileLocation);
        assertTrue(downloadedFile.exists());
        assertTrue(downloadedFile.delete());
    }

    @Test
    void testGetXmlEventReaderForFile_allOk() throws Exception{
        String fileLocation = "src/test/resources/__files/update.xml";

        XMLEventReader eventReaderForFile = dataSource.getXmlEventReaderForFile(fileLocation);

        int eventType = XMLEvent.START_ELEMENT;
        while (eventReaderForFile.hasNext()){
            eventType = eventReaderForFile.nextEvent().getEventType();
        }
        //if we're here - this is xml and everything is ok
        assertEquals(XMLEvent.END_DOCUMENT, eventType);
    }

    @Test
    void testGetXmlEventReaderForFile_wrongFile() throws Exception{
        String fileLocation = "src/test/resources/__files/notXml.txt";

        XMLEventReader eventReaderForFile = dataSource.getXmlEventReaderForFile(fileLocation);

        if (eventReaderForFile.hasNext()){
            eventReaderForFile.nextEvent(); //passing startDocument
            assertThrows(XMLStreamException.class, eventReaderForFile::nextEvent);
        }
    }

    @Test
    void testGetXmlEventReaderForFile_noFile(){
        String fileLocation = "src/test/resources/__files/nonExistingFile.txt";

        assertThrows(ResponseStatusException.class, () -> dataSource.getXmlEventReaderForFile(fileLocation));
    }

    @Test
    void testGetFileInputStream_noFile(){
        String fileLocation = "src/test/resources/__files/nonExistingFile.txt";

        assertThrows(ResponseStatusException.class, () -> dataSource.getFileInputStream(fileLocation));
    }

    @Test
    void testGetFileInputStream_allOk() throws Exception{
        String fileLocation = "src/test/resources/__files/update.xml";

        FileInputStream inputStream = dataSource.getFileInputStream(fileLocation);

        //Checking size to make sure we got the file
        assertEquals(69265, inputStream.available());
    }

}
