package com.example.jb_products_info.entities.buildInfo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Patches {
    private List<Patch> win;
    private List<Patch> mac;
    private List<Patch> unix;
}
