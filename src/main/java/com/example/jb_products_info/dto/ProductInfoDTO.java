package com.example.jb_products_info.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
@JsonInclude(Include.NON_NULL)
public class ProductInfoDTO {
    private String name;
    private String version;
    private String versionSuffix;
    private String buildNumber;
    private String productCode;
    private String dataDirectoryName;
    private String svgIconPath;
    private String productVendor;
    private List<String> customProperties;
    private List<ProductLaunchInfoDTO> launch;
    private List<String> bundledPlugins;
    private List<String> modules;
    private List<String> fileExtensions;
}
