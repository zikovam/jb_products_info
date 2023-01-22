package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.entities.Product;
import com.example.jb_products_info.entities.build_info.JbDataServiceResponse;
import com.example.jb_products_info.entities.build_info.Release;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.jb_products_info.utils.Constants.UPDATE_FILE_PATH;

@Service
public class FileParserService {
    final Logger logger = LoggerFactory.getLogger(FileParserService.class);
    @Value("${download.build.info.url}")
    private String buildInfoDataDownloadUrl;
    @Value("${codes.duplicated}")
    private List<String> codesToAvoid;

    public Product collectProduct(String productCode) throws XMLStreamException, FileNotFoundException {
        for (Product product : collectProducts()) {
            if (product.getCode().equals(productCode)) {
                return product;
            }
        }
        return null;
    }

    public List<Product> collectProducts() throws FileNotFoundException, XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        // preventing xxe
        xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        logger.info("parsing update.xml from file {}", UPDATE_FILE_PATH);
        List<Product> products = parseUpdateXmlProducts(xmlInputFactory);

        logger.info("found products: {}: {}", products.size(),
                products.stream().map(Product::getCode).sorted().toList());

        logger.info("adding information about builds for products");
        addBuildInfos(products);

        return products;
    }

    private List<Product> parseUpdateXmlProducts(XMLInputFactory xmlInputFactory) throws XMLStreamException, FileNotFoundException {
        List<Product> products = new ArrayList<>();

        XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(UPDATE_FILE_PATH));

        Product product = null;
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                StartElement element = event.asStartElement();

                String localPart = element.getName().getLocalPart();
                if (localPart.equals("product")) {
                    product = new Product();
                    Attribute name = element.getAttributeByName(new QName("name"));
                    if (name != null) {
                        product.setName(name.getValue());
                    }
                } else if (localPart.equals("code")) {
                    event = reader.nextEvent();
                    String code = event.asCharacters().getData();
                    if (!codesToAvoid.contains(code)) {
                        if (Objects.requireNonNull(product).getCode() == null) {
                            product.setCode(code);
                        } else {
                            String name = product.getName();
                            products.add(product);
                            product = new Product();
                            product.setName(name);
                            product.setCode(code);
                        }
                    }
                }
            }
            if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                if (endElement.getName().getLocalPart().equals("product")) {
                    products.add(product);
                }
            }
        }
        return products;
    }

    private void addBuildInfos(List<Product> products) {
        for (Product product : products) {
            JbDataServiceResponse productInfo = getJbDataServiceResponse(product);

            List<Build> builds = new ArrayList<>();
            List<Release> releases = productInfo.getReleases();

            for (Release release : releases) {
                if (release.getDate() != null && release.getDate().isAfter(LocalDate.now().minusYears(1))) {
                    Build build = Build.builder()
                            .releaseDate(release.getDate())
                            .version(release.getVersion())
                            .buildNumber(release.getBuild())
                            .downloadUrl((release.getDownloads() != null && release.getDownloads().getLinux() != null) ?
                                    release.getDownloads().getLinux().getLink() :
                                    null)
                            .product(product)
                            .build();
                    builds.add(build);
                }
            }
            product.setBuilds(builds);
        }
    }

    private JbDataServiceResponse getJbDataServiceResponse(Product product) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder.fromUriString(buildInfoDataDownloadUrl)
                        .queryParam("code", product.getCode());

        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");

        ResponseEntity<List<JbDataServiceResponse>> response = restTemplate.exchange(
                uriBuilder.build().toUriString(),
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {
                });

        return Objects.requireNonNull(response.getBody()).get(0);
    }
}