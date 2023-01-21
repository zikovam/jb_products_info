package com.example.jb_products_info.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Component
public class SystemStatusDTO {
    private LocalDateTime lastUpdated;
    private ZoneId serverTimezone;
    private Integer countProducts;
    private Set<String> productCodes;
    private Integer countBuilds;
    private Integer downloadedBuilds;
    private Integer buildsDownloading;
    private Integer buildsDownloadQueue;
    private Integer buildsWithoutLinuxVersion;

    @Override
    public String toString() {
        return "SystemStatusDTO{" +
                "lastUpdated=" + lastUpdated +
                ", serverTimezone=" + serverTimezone +
                ", countProducts=" + countProducts +
                ", productCodes=" + productCodes +
                ", countBuilds=" + countBuilds +
                ", downloadedBuilds=" + downloadedBuilds +
                ", buildsDownloading=" + buildsDownloading +
                ", buildsDownloadQueue=" + buildsDownloadQueue +
                ", buildsWithoutLinuxVersion=" + buildsWithoutLinuxVersion +
                '}';
    }
}
