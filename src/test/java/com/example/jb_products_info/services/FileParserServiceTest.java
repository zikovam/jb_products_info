package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Product;
import com.example.jb_products_info.entities.build_info.Download;
import com.example.jb_products_info.entities.build_info.Downloads;
import com.example.jb_products_info.entities.build_info.JbDataServiceResponse;
import com.example.jb_products_info.entities.build_info.Release;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileParserServiceTest {
    @InjectMocks
    FileParserService fileParserService;

    @Mock
    private DataSource dataSource;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(fileParserService, "codesToAvoid", List.of("OC"));
    }

    @Test
    void testCollectProducts_fileNotFound() {
        doThrow(ResponseStatusException.class).when(dataSource).getXmlEventReaderForFile(anyString());

        Assertions.assertThrows(ResponseStatusException.class,
                () -> fileParserService.collectProducts(),
                "ResponseStatusException was expected");

        verify(dataSource, times(1)).getXmlEventReaderForFile(anyString());
    }

    @Test
    void testCollectProducts_collectTwoProductsOfThreePresented() throws Exception {
        String path = "src/test/resources/update.xml";
        File file = new File(path);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader reader = factory.createXMLEventReader(new FileReader(file));

        Download linuxDownload = Download.builder()
                .link("testLink")
                .build();
        Downloads downloads = Downloads.builder()
                .linux(linuxDownload)
                .build();
        Release release = Release.builder()
                .date(LocalDate.of(2023, 1, 11))
                .version("testVersion")
                .build("testBuildNumber")
                .downloads(downloads)
                .build();
        JbDataServiceResponse response = JbDataServiceResponse.builder()
                .releases(List.of(release))
                .build();

        when(dataSource.getXmlEventReaderForFile(anyString())).thenReturn(reader);
        when(dataSource.getJbDataServiceResponseForProduct(any())).thenReturn(response);

        List<Product> products = fileParserService.collectProducts();

        assertEquals(2, products.size());

        assertEquals(1, products.get(0).getBuilds().size());
        assertEquals("testBuildNumber", products.get(0).getBuilds().get(0).getBuildNumber());
        assertEquals("testVersion", products.get(0).getBuilds().get(0).getVersion());
        assertEquals("testLink", products.get(0).getBuilds().get(0).getDownloadUrl());
        assertEquals(products.get(0).getCode(), products.get(0).getBuilds().get(0).getProduct().getCode());

        assertEquals(1, products.get(1).getBuilds().size());
        assertEquals("testBuildNumber", products.get(1).getBuilds().get(0).getBuildNumber());
        assertEquals("testVersion", products.get(1).getBuilds().get(0).getVersion());
        assertEquals("testLink", products.get(1).getBuilds().get(0).getDownloadUrl());
        assertEquals(products.get(1).getCode(), products.get(1).getBuilds().get(0).getProduct().getCode());

        verify(dataSource, times(1)).getXmlEventReaderForFile(anyString());
        verify(dataSource, times(2)).getJbDataServiceResponseForProduct(any());
    }

    @Test
    void testCollectProductByCode_allOk() throws Exception {
        String path = "src/test/resources/update.xml";
        File file = new File(path);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader reader = factory.createXMLEventReader(new FileReader(file));

        Download linuxDownload = Download.builder()
                .link("testLink")
                .build();
        Downloads downloads = Downloads.builder()
                .linux(linuxDownload)
                .build();
        Release release = Release.builder()
                .date(LocalDate.of(2023, 1, 11))
                .version("testVersion")
                .build("testBuildNumber")
                .downloads(downloads)
                .build();
        JbDataServiceResponse response = JbDataServiceResponse.builder()
                .releases(List.of(release))
                .build();

        when(dataSource.getXmlEventReaderForFile(anyString())).thenReturn(reader);
        when(dataSource.getJbDataServiceResponseForProduct(any())).thenReturn(response);

        Product product = fileParserService.collectProduct("CL");

        assertEquals("CL", product.getCode());

        assertEquals(1, product.getBuilds().size());
        assertEquals("testBuildNumber", product.getBuilds().get(0).getBuildNumber());
        assertEquals("testVersion", product.getBuilds().get(0).getVersion());
        assertEquals("testLink", product.getBuilds().get(0).getDownloadUrl());
        assertEquals(product.getCode(), product.getBuilds().get(0).getProduct().getCode());

        verify(dataSource, times(1)).getXmlEventReaderForFile(anyString());
        verify(dataSource, times(2)).getJbDataServiceResponseForProduct(any());
    }
}
