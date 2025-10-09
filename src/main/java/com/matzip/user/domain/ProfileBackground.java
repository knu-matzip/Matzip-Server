package com.matzip.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProfileBackground {
    COLOR_01("BEE1E6"),
    COLOR_02("CDDAFD"),
    COLOR_03("D1E6D3"),
    COLOR_04("DFE7FD"),
    COLOR_05("FDE2E4"),
    COLOR_06("E2ECE9"),
    COLOR_07("EAE4E9"),
    COLOR_08("F0EFEB"),
    COLOR_09("F1DDFF"),
    COLOR_10("F3D9DE"),
    COLOR_11("F4E1D6"),
    COLOR_12("FFF1E6");

    private final String colorHexCode;
}
