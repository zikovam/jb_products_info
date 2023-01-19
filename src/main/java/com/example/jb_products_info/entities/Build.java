package com.example.jb_products_info.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "tb_build")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Build {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String buildNumber;
    private String version;
    private LocalDate releaseDate;
    private String downloadUrl;
    @Lob
    private String productInfoJsonData;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Build build)) return false;
        return Objects.equals(getBuildNumber(), build.getBuildNumber()) &&
                Objects.equals(getVersion(), build.getVersion()) &&
                Objects.equals(getReleaseDate(), build.getReleaseDate()) &&
                Objects.equals(getDownloadUrl(), build.getDownloadUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBuildNumber(), getVersion(), getReleaseDate(), getDownloadUrl());
    }

    @Override
    public String toString() {
        return "Build{" +
                "id=" + id +
                ", buildNumber='" + buildNumber + '\'' +
                ", version='" + version + '\'' +
                ", releaseDate=" + releaseDate +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", productInfoJsonData='" + productInfoJsonData + '\'' +
                '}';
    }
}
