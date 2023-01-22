package com.example.jb_products_info.entities.build_info;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Distributions{
    private Distribution linuxARM64;
    private Distribution linux;
    private Distribution windowsAnaconda;
    private Distribution macAnaconda;
    private Distribution linuxAnaconda;
    private Distribution thirdPartyLibrariesJson;
    private Distribution windows;
    private Distribution windowsARM64;
    private Distribution mac;
    private Distribution macM1;
    private Distribution macM1Anaconda;
}
