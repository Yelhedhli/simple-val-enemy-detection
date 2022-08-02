package com.elhedhli.imageanalyzer;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public class EnemyModel extends ArrayList<PixelCoord>{

    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;

    private int height;
    private int width;

    public EnemyModel(ArrayList<PixelCoord> pixelCoords){
        super(pixelCoords);
        this.xMin = pixelCoords.get(0).getX();
        this.xMax = pixelCoords.get(0).getX();
        this.yMin = pixelCoords.get(0).getY();
        this.yMax = pixelCoords.get(0).getY();

        this.height = 0;
        this.width = 0;
    }

    @Override
    public boolean add(PixelCoord pixelCoord){
        int x = pixelCoord.getX();
        int y = pixelCoord.getY();

        if(x < this.xMin){
            this.xMin = x;
            this.width = this.xMax - this.xMin;
        }else if(x > this.xMax){
            this.xMax = x;
            this.width = this.xMax - this.xMin;
        }

        if(y < this.yMin){
            this.yMin = y;
            this.height = this.yMax - this.yMin;
        }else if(y > this.yMax){
            this.yMax = y;
            this.height = this.yMax - this.yMin;
        }

        return super.add(pixelCoord);

    }

}
