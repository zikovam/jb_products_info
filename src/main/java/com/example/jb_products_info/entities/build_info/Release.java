package com.example.jb_products_info.entities.build_info;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Release {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String type;
    private Downloads downloads;
    private Patches patches;
    private String notesLink;
    private boolean licenseRequired;
    private String version;
    private String majorVersion;
    private String build;
    private String whatsnew;
    private UninstallFeedbackLinks uninstallFeedbackLinks;
    private String printableReleaseType;
}
