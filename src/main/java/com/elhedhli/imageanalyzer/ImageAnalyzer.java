package com.elhedhli.imageanalyzer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;


public class ImageAnalyzer {
    /* Enum to drive logic for determining which enemy search method to use */
    enum searchMethod{
        LOCALGREEDY,
        AREASEARCH,
        CLUSTERING
    }

    public static void main(String[] args) {
        searchMethod searchmethod = searchMethod.CLUSTERING; // set object search method

        // ingest screenshot from valorant
        BufferedImage frame = null;
        try {
            InputStream is = ImageAnalyzer.class.getClassLoader().getResourceAsStream("images/img0.png");
            frame = ImageIO.read(is);
        } catch (IOException e) {
            System.out.println("Exception occurred :" + e.getMessage());
            System.exit(0);
        }

        // to time our algo
        Instant start = Instant.now();

        // extract any pixels that could represent an enemy
        ArrayList<PixelCoord> purplePixels = getPurplePixels(frame);

        // find enemies on screen...
        ArrayList<EnemyModel> enemies = new ArrayList<>(0);

        if(searchmethod == searchMethod.CLUSTERING) { // ...using divisive hierarchical clustering based algo
            enemies = findEnemyClustering(purplePixels);
        }

        if(searchmethod == searchMethod.LOCALGREEDY) { // ...using local greedy search algo
            while (!purplePixels.isEmpty()) {
                EnemyModel enemyModel = findEnemyLocalGreedy(purplePixels);
                if (enemyModel.size() > 10) {
                    enemies.add(enemyModel);
                }
            }
        }

        if(searchmethod == searchMethod.AREASEARCH) { // ...using simple area search algo
            while(!purplePixels.isEmpty()){
                EnemyModel enemyModel = findEnemyAreaSearch(purplePixels);
                if( enemyModel.size() > 10 ){
                    enemies.add(enemyModel);
                }
            }
        }

        // output how long it took us to detect all enemies
        Instant finish  = Instant.now();
        System.out.println(Duration.between(start, finish).toMillis());

        // display and output the enemies we detected
        Graphics2D g = (Graphics2D) frame.getGraphics();
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.RED);

        System.out.println("Enemies:");
        enemies.forEach((enemyModel) -> {
            System.out.printf("xMax: %d, xMin: %d, yMax: %d, yMin: %d, size %d, stdDevX: %f, stdDevY: %f \n", enemyModel.getXMax(),
                                enemyModel.getXMin(), enemyModel.getYMax(), enemyModel.getYMin(), enemyModel.size(), enemyModel.getStdDevX(), enemyModel.getStdDevY());
            g.drawRect(enemyModel.getXMin(), enemyModel.getYMin(), enemyModel.getWidth(), enemyModel.getHeight());
        });

        JLabel picLabel = new JLabel(new ImageIcon(frame));

        JPanel jPanel = new JPanel();
        jPanel.add(picLabel);

        JFrame f = new JFrame();
        f.setSize(new Dimension(frame.getWidth(), frame.getHeight()));
        f.add(jPanel);
        f.setVisible(true);
    }

    /* Get all pixels that could potentially be part of an enemy character model */
    private static ArrayList<PixelCoord> getPurplePixels(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData(); //get raw pixel buffer
        final int width = image.getWidth();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        ArrayList<PixelCoord> result = new ArrayList<>(0);

        // if a pixel matches the colour of the player outline, this is a potential enemy, add it to our list
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

    /* Determine if these rgb values are purple TODO: more sophisticated/accurate approach */
    private static boolean isPurple(int r, int g, int b) {
        return (r >= 0xCC) && (g <= 0x99) && (b >= 0xCC);
    }

    /* Group these pixels into objects using a divisive hierarchical clustering algo*/
    private static ArrayList<EnemyModel> findEnemyClustering(ArrayList<PixelCoord> purplePixels){
        ArrayList<EnemyModel> enemies = new ArrayList<>(0);
//        EnemyModel enemyModel = new EnemyModel(); // initial cluster encompassing all points

        EnemyModel enemyModel = new EnemyModel(new ArrayList<>(Arrays.asList(purplePixels.get(0))));

        purplePixels.remove(0);

        for(PixelCoord pixelCoord : purplePixels){ // EnemyModel constructor not fully implemented, so we have to do this
            enemyModel.add(pixelCoord);
        }

        enemies.add(enemyModel);

        split(enemyModel, enemies); // split cluster if needed

        return enemies;
    }

    /* Recursively split the cluster that is passed in, if needed TODO: Make this not recursive */
    private static void split(EnemyModel enemyModel, ArrayList<EnemyModel> enemies){
        EnemyModel newEnemyModel; // in case we need to split the cluster, new cluster will be store here

        // based on the standard deviation of the x and y values of the points, determine whether to split or not
        if( checkSplit(enemyModel) ){
            newEnemyModel = new EnemyModel();

            // split the cluster around the mean of the x-vals
            double mean = enemyModel.getMeanX();
            for (int i = 0; i < enemyModel.size(); i++) {
                if(enemyModel.get(i).getX() > mean){
                    newEnemyModel.add(enemyModel.get(i));
                    enemyModel.remove(enemyModel.get(i));
                    i--;
                }
            }

            // recursively call split again on the new clusters
            if(enemyModel.size() > 20){
                split(enemyModel, enemies);
            }else{
                enemies.remove(enemyModel);
            }
            if(newEnemyModel.size() > 20){
                enemies.add(newEnemyModel);
                split(newEnemyModel, enemies);
            }
        }
    }

    private static boolean checkSplit(EnemyModel enemyModel){
        double predicted = enemyModel.size();
        predicted = 2.35 + 0.0616*predicted - 0.0000298*Math.pow(predicted, 2);

        return !((Math.abs(predicted - enemyModel.getStdDevX()) / predicted) < 0.33);
    }

    /*
    Group these pixels into objects using a local greedy search algo that picks up any pixels near the object
    and assigns them to itself
    */
    private static EnemyModel findEnemyLocalGreedy(ArrayList<PixelCoord> purplePixels){
        // prime object with one pixel
        EnemyModel enemyModel = new EnemyModel(new ArrayList<>(Arrays.asList(purplePixels.get(0))));

        purplePixels.remove(0);

        scan(enemyModel, purplePixels); // local greedy search for any nearby pixels

        return enemyModel;
    }

    /* Scan all around for pixels in any direction */
    private static void scan(EnemyModel enemyModel, ArrayList<PixelCoord> purplePixels){
        // scan for any pixels within 10 pixels of this object and add them
        for(int j = 0; j < enemyModel.size(); j++) {
            int currX = enemyModel.get(j).getX();
            int currY = enemyModel.get(j).getY();
            for (int i = 0; i < purplePixels.size(); i++) {
                int nextX = purplePixels.get(i).getX();
                int nextY = purplePixels.get(i).getY();

                if ((nextX + 10 >= currX) && (nextX - 10 <= currX) && (nextY + 10 >= currY) && (nextY - 10 <= currY)) {
                    enemyModel.add(purplePixels.get(i));
                    purplePixels.remove(i);
                    i--;
                }
            }
        }
    }

    /*
    Group these pixels into objects using a simple algo that searches the area near any offending pixels and determines
    whether it is an enemy or not based on the number of offending pixels nearby
    */
    private static EnemyModel findEnemyAreaSearch(ArrayList<PixelCoord> purplePixels){
        float ratio = 0.3F; // model width to model height ratio valorant

        EnemyModel enemyModel = new EnemyModel(new ArrayList<>(Arrays.asList(purplePixels.get(0))));

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

    /* Find the pixel the farthest directly below */
    private static int findBottom(ArrayList<PixelCoord> purplePixels){
        int xTop = purplePixels.get(0).getX();
        int index = 0;
        int i = 0;
        for(PixelCoord pixelCoord : purplePixels){
            int xBot = pixelCoord.getX();
            if( (xBot-15 <= xTop) && (xBot+15 >= xTop) ){
                index = i;
            }
            i++;
        }

        return index;
    }

    /* Sweep directly downwards for pixels below and to the left or right */
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

}