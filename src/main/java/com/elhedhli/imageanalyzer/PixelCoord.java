package com.elhedhli.imageanalyzer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PixelCoord {
    private int x;
    private int y;

    public PixelCoord(int x, int y){
        this.x = x;
        this.y = y;
    }
}
