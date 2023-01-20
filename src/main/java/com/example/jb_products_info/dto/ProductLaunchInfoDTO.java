package com.example.jb_products_info.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductLaunchInfoDTO {
    private String os;
    private String arch;
    private String launcherPath;
    private String javaExecutablePath;
    private String vmOptionsFilePath;
    private String startupWmClass;
    private List<String> bootClassPathJarNames;
    private List<String> additionalJvmArguments;
}
