package com.example.jb_products_info.entities.build_info;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Patch {
    private String fromBuild;
    private String link;
    private int size;
    private String checksumLink;
}
