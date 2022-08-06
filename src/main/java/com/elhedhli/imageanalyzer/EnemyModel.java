package com.elhedhli.imageanalyzer;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.*;

import static java.util.Comparator.comparingInt;

@Getter
public class EnemyModel extends ArrayList<PixelCoord>{

    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;

    private double stdDevX;
    private double stdDevY;
    private double meanX;
    private double meanY;

    private int height;
    private int width;

    @Getter(AccessLevel.NONE)
    private double M2X;
    @Getter(AccessLevel.NONE)
    private double M2Y;

    public EnemyModel(ArrayList<PixelCoord> pixelCoords){
        super(pixelCoords);

        this.xMin = pixelCoords.get(0).getX();
        this.xMax = pixelCoords.get(0).getX();
        this.yMin = pixelCoords.get(0).getY();
        this.yMax = pixelCoords.get(0).getY();

        this.height = 0;
        this.width = 0;

        this.stdDevX = 0;
        this.stdDevY = 0;
        this.meanX = pixelCoords.get(0).getX();
        this.meanY = pixelCoords.get(0).getY();

        this.M2X = 0;
        this.M2Y = 0;
    }

    public EnemyModel(){
        super();

        this.xMin = 0;
        this.xMax = 0;
        this.yMin = 0;
        this.yMax = 0;

        this.height = 0;
        this.width = 0;

        this.stdDevX = 0;
        this.stdDevY = 0;
        this.meanX = 0;
        this.meanY = 0;

        this.M2X = 0;
        this.M2Y = 0;
    }

    @Override
    public boolean add(PixelCoord pixelCoord){
        int x = pixelCoord.getX();
        int y = pixelCoord.getY();

        if(this.isEmpty()){
            this.xMin = x;
            this.xMax = x;
            this.yMin = y;
            this.yMax = y;

            this.meanX = pixelCoord.getX();
            this.meanY = pixelCoord.getY();

            return super.add(pixelCoord);
        }

        calcStdDevX(x);
        calcStdDevY(y);

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

    @Override
    public boolean remove(Object o){
        PixelCoord pixelCoord = (PixelCoord) o;

        boolean result = super.remove(o);

        if(result) {
            if (pixelCoord.getX() == this.xMax) {
                this.xMax = findXMax();
                this.width = this.xMax - this.xMin;
            } else if (pixelCoord.getX() == this.xMin) {
                this.xMax = findXMin();
                this.width = this.xMax - this.xMin;
            }

            if (pixelCoord.getY() == this.yMax) {
                this.yMax = findYMax();
                this.height = this.yMax - this.yMin;
            } else if (pixelCoord.getY() == this.yMin) {
                this.yMax = findYMin();
                this.height = this.yMax - this.yMin;
            }

            backStdDevX(pixelCoord.getX());
            backStdDevY(pixelCoord.getY());
        }

        return result;
    }

    private void calcStdDevX(double newX){
        double count = this.size() + 1;
        double d1 = newX - this.meanX;
        this.meanX += d1/count;

        double d2 = newX - this.meanX;
        this.M2X += d1*d2;
        this.stdDevX =  Math.sqrt(this.M2X/count);
    }

    private void calcStdDevY(double newY){
        double count = this.size() + 1;
        double d1 = newY - this.meanY;
        this.meanY += d1/count;

        double d2 = newY - this.meanY;
        this.M2Y += d1*d2;
        this.stdDevY = Math.sqrt(this.M2Y/count);
    }

    private void backStdDevX(double oldX){
        double count = this.size();
        double d1 = oldX - this.meanX;
        this.meanX -= d1/count;

        double d2 = oldX - this.meanX;
        this.M2X -= d1*d2;
        this.stdDevX =  Math.sqrt(this.M2X/count);
    }

    private void backStdDevY(double oldY){
        double count = this.size();
        double d1 = oldY - this.meanY;
        this.meanY -= d1/count;

        double d2 = oldY - this.meanY;
        this.M2Y -= d1*d2;
        this.stdDevY =  Math.sqrt(this.M2Y/count);
    }

    private Integer findXMax(){
        int max = 0;
        for(PixelCoord pixelCoord : this){
            if(pixelCoord.getX() > max){
                max = pixelCoord.getX();
            }
        }
        return max;
    }

    private Integer findXMin(){
        int min = 1920;
        for(PixelCoord pixelCoord : this){
            if(pixelCoord.getX() < min){
                min = pixelCoord.getX();
            }
        }
        return min;
    }

    private Integer findYMax(){
        int max = 0;
        for(PixelCoord pixelCoord : this){
            if(pixelCoord.getY() > max){
                max = pixelCoord.getY();
            }
        }
        return max;
    }

    private Integer findYMin(){
        int min = 1080;
        for(PixelCoord pixelCoord : this){
            if(pixelCoord.getY() < min){
                min = pixelCoord.getY();
            }
        }
        return min;
    }
}
