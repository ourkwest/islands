package uk.me.westmacott.islands;

import uk.me.westmacott.islands.Colors.ColorStops;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class HeightMap {


    public static void main(String[] args) throws IOException {
        new HeightMap(0, 500, 500);
    }

    public HeightMap(long seed, int width, int height) throws IOException {

        final Noise noise = new Noise(seed);
        final double scale = 1;

        final double[][] raw = new double[width][height];
        final double[][] normalised = new double[width][height];
        final int[][] imageData = new int[width][height];

        double min = 256.0;
        double max = -256.0;
        for (int x = 0; x < raw.length; x++) {
            for (int y = 0; y < raw[0].length; y++) {
                raw[x][y] = noise.noise(x * scale, y * scale);
                min = Math.min(min, raw[x][y]);
                max = Math.max(max, raw[x][y]);
            }
            if (x % 10 == 0) {
                System.out.print(".");
            }
        }
        System.out.println();

        for (int x = 0; x < raw.length; x++) {
            for (int y = 0; y < raw[0].length; y++) {
                normalised[x][y] = (raw[x][y] - min) / (max - min);
            }
            if (x % 10 == 0) {
                System.out.print(".");
            }
        }
        System.out.println();

        ColorStops sea = ColorStops
                .startAt(Color.BLACK)
                .step(10, Color.BLUE)
                .step(4, new Color(60, 154, 255))
                .step(1, new Color(181, 253, 255));

        ColorStops land = ColorStops
                .startAt(new Color(255, 255, 127))
                .step(10, new Color(1, 216, 0))
                .step(10, new Color(0, 101, 4))
                .step(10, new Color(181, 177, 177));

        for (int x = 0; x < raw.length; x++) {
            for (int y = 0; y < raw[0].length; y++) {

                imageData[x][y] = Color.WHITE.getRGB();

                if (normalised[x][y] < 0.6) {

//                    double index = normalised[x][y] / 0.55 * sea.total();

                    imageData[x][y] = sea.get(0, normalised[x][y], 0.6).getRGB();
                }
                else {

                    imageData[x][y] = land.get(0.6, normalised[x][y], 1.0).getRGB();

                    // Contour lines

                    for (double c = 0.1; c <= 1.0; c += 0.1) {
                        if (normalised[x][y] > c) {
                            for (Point neighbour : neighbours(new Point(x, y), width, height)) {
                                if (normalised[neighbour.x][neighbour.y] < c) {
                                    imageData[x][y] = Color.BLACK.getRGB();
                                }
                            }
                        }
                    }
                }
            }
            if (x % 10 == 0) {
                System.out.print(".");
            }
        }
        System.out.println();


        // waves
        //   seeded at contour line stage
        //   algorithm: done, doing, todo

//
//
//
//        for (int x = 0; x < raw.length; x++) {
//            for (int y = 0; y < raw[0].length; y++) {
//                int l = (int) ((raw[x][y] - min) / (max - min) * 255);
//
//                if (l > 150) {
//                    data[x][y] = new Color(l/3, l, l/2).getRGB();
//                }
//                else {
//                    data[x][y] = new Color(50, 100, 255).getRGB();
//                }
//
//            }
//        }
        Data.spitImage(imageData, "HeightMap");

    }

    public static Point[] neighbours = new Point[]{
            new Point(-1, -1), new Point( 0, -1), new Point( 1, -1),
            new Point(-1,  0),                    new Point( 1,  0),
            new Point(-1,  1), new Point( 0,  1), new Point( 1,  1)};

    public static List<Point> neighbours(Point input, int imageWidth, int imageHeight) {
        List<Point> output = new LinkedList<>();
        for (Point neighbour : neighbours) {
            int x = input.x + neighbour.x;
            int y = input.y + neighbour.y;
            if (0 <= x && x < imageWidth && 0 <= y && y < imageHeight) {
                output.add(new Point(x, y));
            }
        }
        return output;
    }



}
