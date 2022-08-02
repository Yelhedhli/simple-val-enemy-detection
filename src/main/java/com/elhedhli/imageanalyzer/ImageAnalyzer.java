package com.elhedhli.imageanalyzer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;


public class ImageAnalyzer {
    public static void main(String[] args) {
        BufferedImage frame = null;


        try {
            InputStream is = ImageAnalyzer.class.getClassLoader().getResourceAsStream("images/img2.png");
            frame = ImageIO.read(is);
        } catch (IOException e) {
            System.out.println("Exception occured :" + e.getMessage());
            System.exit(0);
        }

        Instant start = Instant.now();

        ArrayList<PixelCoord> purplePixels = getPurplePixels(frame);
        ArrayList<EnemyModel> enemies = new ArrayList<EnemyModel>(0);

//        for(int i = 0; i < purplePixels.size(); i++){
//            System.out.printf("index: %d  x: %d    y: %d \n", i, purplePixels.get(i).getX(), purplePixels.get(i).getY());
//        }

        while(!purplePixels.isEmpty()){
            enemies.add(findEnemy(purplePixels));
            if( enemies.get(enemies.size()-1).size() < 10 ){
                enemies.remove(enemies.size()-1);
            }
        }

        Instant finish  = Instant.now();

        Graphics2D g = (Graphics2D) frame.getGraphics();
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.RED);

        for(int i = 0; i < enemies.size(); i++){
            System.out.printf("Enemy number: %d, xMax: %d, xMin: %d, yMax: %d, yMin: %d, size %d \n", i, enemies.get(i).getXMax(), enemies.get(i).getXMin(), enemies.get(i).getYMax(), enemies.get(i).getYMin(), enemies.get(i).size());
            g.drawRect(enemies.get(i).getXMin(), enemies.get(i).getYMin(), enemies.get(i).getWidth(), enemies.get(i).getHeight());
        }

        JLabel picLabel = new JLabel(new ImageIcon(frame));

        JPanel jPanel = new JPanel();
        jPanel.add(picLabel);

        JFrame f = new JFrame();
        f.setSize(new Dimension(frame.getWidth(), frame.getHeight()));
        f.add(jPanel);
        f.setVisible(true);


        System.out.println(Duration.between(start, finish).toMillis());
    }

    private static ArrayList<PixelCoord> getPurplePixels(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        ArrayList<PixelCoord> result = new ArrayList<PixelCoord>(0);

        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
                int b = ((int) pixels[pixel + 1] & 0xff); // blue
                int g = ((int) pixels[pixel + 2] & 0xff); // green
                int r = ((int) pixels[pixel + 3] & 0xff); // red

                if(isPurple(r, g, b)){
                    result.add(new PixelCoord(col, row));
                }

                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
                int b = ((int) pixels[pixel] & 0xff); // blue
                int g = ((int) pixels[pixel + 1] & 0xff); // green
                int r = ((int) pixels[pixel + 2] & 0xff); // red

                if(isPurple(r, g, b)){
                    result.add(new PixelCoord(col, row));
                }

                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

    private static boolean isPurple(int r, int g, int b) {

        if(( r >= 0xb9 ) && ( g <= 0x99 ) && ( b >= 0xb9 )){
            return true;
        }

        return false;
    }

    private static EnemyModel findEnemy(ArrayList<PixelCoord> purplePixels){
        float ratio = 0.3F;

        EnemyModel enemyModel = new EnemyModel(new ArrayList<PixelCoord>(Arrays.asList(purplePixels.get(0))));

        int indexBottom = findBottom(purplePixels);

        enemyModel.add(purplePixels.get(indexBottom));

        purplePixels.remove(indexBottom);
        if(!purplePixels.isEmpty()){
            purplePixels.remove(0);
        }

        if(!purplePixels.isEmpty()) {
            sweep(enemyModel, purplePixels, Math.round(enemyModel.getHeight() * ratio));
        }

        return enemyModel;
    }

    private static int findBottom(ArrayList<PixelCoord> purplePixels){
        int xTop = purplePixels.get(0).getX();
        int index = 0;
        for(int i = 0; i < purplePixels.size(); i++){
            int xBot = purplePixels.get(i).getX();
            if( (xBot-15 <= xTop) && (xBot+15 >= xTop) ){
                index = i;
            }
        }

        return index;
    }

    private static void sweep(EnemyModel enemyModel, ArrayList<PixelCoord> purplePixels, int range){
        int xTop = purplePixels.get(0).getX();
        for(int i = 0; i < purplePixels.size(); i++){
            int xBot = purplePixels.get(i).getX();
            if( (xBot-range <= xTop) && (xBot+range >= xTop) ){
                enemyModel.add(purplePixels.get(i));
                purplePixels.remove(i);
                i--;
            }
        }
    }

//    private static void scanLeft(EnemyModel enemyModel, ArrayList<PixelCoord> purplePixels){
//        int currX = enemyModel.get(0).getX();
//        int currY = enemyModel.get(0).getY();
//        for(int i = 0; i < purplePixels.size(); i++){
//            int nextX = purplePixels.get(i).getX();
//            int nextY = purplePixels.get(i).getY();
//
//            if(nextY-20 > currY){
//                break;
//            }
//
//            if( (nextX+20 >= currX) && (nextX-20 <= currX) ){
//                enemyModel.add(purplePixels.get(i));
//                purplePixels.remove(i);
//                i--;
//                currX = nextX;
//                currY = nextY;
//            }
//        }
//    }

//    private static void scanRight(EnemyModel enemyModel, ArrayList<PixelCoord> purplePixels){
//        int currX = enemyModel.get(0).getX();
//        int currY = enemyModel.get(0).getY();
//        for(int i = 0; i < purplePixels.size(); i++){
//            int nextX = purplePixels.get(i).getX();
//            int nextY = purplePixels.get(i).getY();
//
//            if(nextY-10 > currY){
//                break;
//            }
//
//            if( (nextX-10 <= currX) && (nextX+10 >= currX) ){
//                enemyModel.add(purplePixels.get(i));
//                purplePixels.remove(i);
//                i--;
//                currX = nextX;
//                currY = nextY;
//            }
//        }
//    }

}