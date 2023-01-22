package com.example.jb_products_info.entities.build_info;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Downloads {
    private Download linuxARM64;
    private Download linux;
    private Download windows;
    private Download windowsARM64;
    private Download mac;
    private Download macM1;
    private Download thirdPartyLibrariesJson;
}
