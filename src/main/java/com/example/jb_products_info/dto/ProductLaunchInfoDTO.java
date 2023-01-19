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
    String os;
    String arch;
    String launcherPath;
    String javaExecutablePath;
    String vmOptionsFilePath;
    String startupWmClass;
    List<String> bootClassPathJarNames;
    List<String> additionalJvmArguments;
}
