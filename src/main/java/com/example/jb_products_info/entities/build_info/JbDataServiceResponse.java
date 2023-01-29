package com.example.jb_products_info.entities.build_info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JbDataServiceResponse {
    private String code;
    private String intellijProductCode;
    private List<String> alternativeCodes;
    private String salesCode;
    private String name;
    private String productFamilyName;
    private String link;
    private String description;
    private List<Tag> tags;
    private List<Type> types;
    private List<String> categories;
    private List<Release> releases;
    private Distributions distributions;
    private boolean forSale;
}