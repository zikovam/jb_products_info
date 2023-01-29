package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.entities.Product;
import com.example.jb_products_info.entities.build_info.JbDataServiceResponse;
import com.example.jb_products_info.entities.build_info.Release;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.jb_products_info.utils.Constants.ERROR_PARSING_UPDATE_XML;
import static com.example.jb_products_info.utils.Constants.UPDATE_XML_FILE_PATH;

@Service
public class FileParserService {
    final Logger logger = LoggerFactory.getLogger(FileParserService.class);

    @Value("${codes.duplicated}")
    private List<String> codesToAvoid;

    private final DataSource dataSource;

    public FileParserService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Product collectProduct(String productCode) {
        for (Product product : collectProducts()) {
            if (product.getCode().equals(productCode)) {
                return product;
            }
        }
        return null;
    }

    public List<Product> collectProducts() {
        logger.info("parsing update.xml from file {}", UPDATE_XML_FILE_PATH);
        List<Product> products;
        try {
            products = parseUpdateXmlProducts(UPDATE_XML_FILE_PATH);
        } catch (XMLStreamException e) {
            logger.error(ERROR_PARSING_UPDATE_XML);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, ERROR_PARSING_UPDATE_XML, e);
        }
        logger.info("found products in file: {}", products.size());
        logger.info("adding information about builds for products");
        addBuildInfos(products);

        return products;
    }

    private List<Product> parseUpdateXmlProducts(String filePath) throws XMLStreamException {
        List<Product> products = new ArrayList<>();

        XMLEventReader reader = dataSource.getXmlEventReaderForFile(filePath);

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
                if (endElement.getName().getLocalPart().equals("product")
                        && product.getCode() != null) {
                    products.add(product);
                }
            }
        }
        return products;
    }

    private void addBuildInfos(List<Product> products) {
        for (Product product : products) {
            JbDataServiceResponse productInfo = dataSource.getJbDataServiceResponseForProduct(product);

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
}