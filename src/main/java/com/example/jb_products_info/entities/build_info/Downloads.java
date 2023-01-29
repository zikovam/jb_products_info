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
public class Downloads {
    private Download linuxARM64;
    private Download linux;
    private Download windows;
    private Download windowsARM64;
    private Download mac;
    private Download macM1;
    private Download thirdPartyLibrariesJson;
}
