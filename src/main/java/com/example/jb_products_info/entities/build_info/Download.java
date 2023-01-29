package com.example.jb_products_info.entities.build_info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Download {
    private String link;
    private int size;
    private String checksumLink;
}
