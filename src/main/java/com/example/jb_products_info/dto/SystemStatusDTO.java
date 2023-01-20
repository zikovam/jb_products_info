package com.example.jb_products_info.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Component
public class SystemStatusDTO { //TODO: think about what information we should present in Status
    private Timestamp lastUpdated;
    private Integer countProducts;
    private Integer countBuilds;
    private Integer downloadedBuilds;
}
