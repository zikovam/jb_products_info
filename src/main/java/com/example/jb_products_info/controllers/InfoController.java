package com.example.jb_products_info.controllers;

import com.example.jb_products_info.dto.BuildInfoDTO;
import com.example.jb_products_info.dto.SystemStatusDTO;
import com.example.jb_products_info.exceptions.BuildNotFoundException;
import com.example.jb_products_info.services.InfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/")
public class InfoController {
    Logger logger = LoggerFactory.getLogger(InfoController.class);

    private final InfoService infoService;

    @Autowired
    public InfoController(InfoService infoService) {
        this.infoService = infoService;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String statusForHuman() {
        logger.info("Providing general status of the system");
        SystemStatusDTO status = infoService.getStatus();
        String html = """
                <html>
                <header>
                    <title>
                        Status
                    </title>
                </header>
                <body>
                <h1>Status:</h1>
                <table>
                    <tbody>
                    <tr>
                        <td>Database last updated</td>
                        <td>%s</td>
                    </tr>
                    <tr>
                        <td>Server timezone</td>
                        <td>%s</td>
                    </tr>
                    <tr>
                        <td>Total products stored</td>
                        <td>%s</td>
                    </tr>
                    <tr>
                        <td>Total builds stored</td>
                        <td>%s</td>
                    </tr>
                    <tr>
                        <td>Total builds with product-info.json data</td>
                        <td>%s</td>
                    </tr>
                    <tr>
                        <td>Builds downloading now</td>
                        <td>%s</td>
                    </tr>
                    <tr>
                        <td>Builds waiting for download</td>
                        <td>%s</td>
                    </tr>
                    <tr>
                        <td>Builds without Linux version</td>
                        <td>%s</td>
                    </tr>
                    </tbody>
                </table>
                <h2>Products:</h2>
                <p>%s</p>
                </body>
                </html>
                """;
        return String.format(html,
                status.getDatabaseLastUpdated(),
                status.getServerTimezone(),
                status.getCountProducts(),
                status.getCountBuilds(),
                status.getBuildsDownloaded(),
                status.getBuildsDownloading(),
                status.getBuildsDownloadQueue(),
                status.getBuildsWithoutLinuxVersion(),
                status.getProductCodes()); //view
    }

    @GetMapping("status")
    public SystemStatusDTO status() {
        logger.info("Providing general status of the system");
        return infoService.getStatus();
    }

    @GetMapping("{productCode}")
    public List<BuildInfoDTO> buildInfosForCode(@PathVariable String productCode) {
        logger.info("Providing info for product code: {}", productCode);
        List<BuildInfoDTO> buildInfoList = infoService.getBuildInfosByCode(productCode);
        if (buildInfoList.isEmpty()) {
            throw new BuildNotFoundException();
        }
        return buildInfoList;
    }

    @GetMapping("{productCode}/{buildNumber}")
    public BuildInfoDTO buildInfo(@PathVariable String productCode,
                                  @PathVariable String buildNumber) {
        logger.info("Providing info for product code: {} and build: {}", productCode, buildNumber);
        BuildInfoDTO buildInfo = infoService.getBuildInfo(productCode, buildNumber);
        if (buildInfo == null) {
            throw new BuildNotFoundException();
        }
        return buildInfo;
    }
}
