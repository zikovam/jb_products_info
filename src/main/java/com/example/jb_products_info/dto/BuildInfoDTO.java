package com.example.jb_products_info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
public class BuildInfoDTO {
    @JsonProperty("build-number")
    private String buildNumber;
    @JsonProperty("product-info")
    private ProductInfoDTO productInfo;

}
