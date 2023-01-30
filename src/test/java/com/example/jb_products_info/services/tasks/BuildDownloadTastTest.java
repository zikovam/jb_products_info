package com.example.jb_products_info.services.tasks;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BuildDownloadTastTest {
    @InjectMocks
    BuildDownloadTask buildDownloadTask;

    public static WireMockServer wireMockRule = new WireMockServer(options().port(9090));

    @BeforeAll
    public static void beforeAll() {
        wireMockRule.start();
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
    void testDownloadBuildAndExtractFile_allOk() throws Exception{
        String subUrl = "/build/idea-IU-231.5920.14.tar.gz";
        wireMockRule.stubFor(get(urlEqualTo(subUrl))
                .willReturn(aResponse()
                        .withBodyFile("idea-IU-231.5920.14.tar.gz")
                ));

        String buildNumber = "231.5920.14";
        String targetFile = "product-info.json";
        String data = buildDownloadTask
                .downloadBuildAndExtractFile("http://localhost:9090"+subUrl, targetFile, buildNumber);

        // Then
        wireMockRule.verify(
                getRequestedFor(urlEqualTo(subUrl))
        );

        assertEquals(buildNumber, data.substring(0, data.indexOf("{")));

        String fileToCheck = "src/test/resources/__files/product-info.json";
        String productInfo = getString(fileToCheck);

        assertEquals(productInfo, data.substring(data.indexOf("{")));
    }

    private static String getString(String fileToCheck) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileToCheck));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        String ls = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        // delete the last new line separator
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        reader.close();
        return stringBuilder.toString();
    }
}
