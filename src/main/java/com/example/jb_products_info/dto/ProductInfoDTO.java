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
    String name;
    String version;
    String versionSuffix;
    String buildNumber;
    String productCode;
    String dataDirectoryName;
    String svgIconPath;
    String productVendor;
    List<String> customProperties;
    List<ProductLaunchInfoDTO> launch;
    List<String> bundledPlugins;
    List<String> modules;
    List<String> fileExtensions;
}
