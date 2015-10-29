package uk.me.westmacott.islands;

import uk.me.westmacott.islands.Colors.ColorStops;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static uk.me.westmacott.islands.Data.close;
import static uk.me.westmacott.islands.Data.inBounds;
import static uk.me.westmacott.islands.Data.lerp;

public class HeightMap {

    static ColorStops sea = ColorStops
            .startingAt(Color.BLACK)
            .stepOf(10, Color.BLUE)
            .stepOf(4, new Color(60, 154, 255))
            .stepOf(1, new Color(181, 253, 255));

    static ColorStops land = ColorStops
            .startingAt(new Color(255, 255, 127))
            .stepOf(10, new Color(1, 216, 0))
            .stepOf(10, new Color(0, 101, 4))
            .stepOf(10, new Color(181, 177, 177));

    static ColorStops sticks = ColorStops
            .startingAt(new Color(204, 120, 50))
            .stepOf(10, new Color(122, 73, 30))
            .stepOf(10, new Color(226, 185, 45))
            .stepOf(10, new Color(63, 29, 15));

    static double seaDepth = 0.40;
    static double landDepth = 1.0 - seaDepth;

    static ColorStops colorStops = ColorStops
            .chain(sea, seaDepth)
            .andThen(land, landDepth);



    public static void main(String[] args) throws IOException {
        new HeightMap(3, 1500, 1500);
    }

    public HeightMap(long seed, int width, int height) throws IOException {

        Random random = new Random(seed);

        final Noisy noise = new SerpNoise(random);
        final double scale = 1;

        final int[][] imageData = new int[width][height];

//        final double[][] raw = new double[width][height];
//        final double[][] normalised = new double[width][height];
//        double min = 256.0;
//        double max = -256.0;
//        for (int x = 0; x < raw.length; x++) {
//            for (int y = 0; y < raw[0].length; y++) {
//                raw[x][y] = noise.noise(x * scale, y * scale);
//                min = Math.min(min, raw[x][y]);
//                max = Math.max(max, raw[x][y]);
//            }
//            if (x % 10 == 0) {
//                System.out.print(".");
//            }
//        }
//        System.out.println();
//
//        for (int x = 0; x < raw.length; x++) {
//            for (int y = 0; y < raw[0].length; y++) {
//                normalised[x][y] = (raw[x][y] - min) / (max - min);
//            }
//            if (x % 10 == 0) {
//                System.out.print(".");
//            }
//        }
//        System.out.println();

        final double[][] normalised = noise.normalisedNoise(width, height, 1500);
        final double[][] lighting = new double[width][height];
        final List<Point> huts = new LinkedList<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                imageData[x][y] = colorStops.get(0.0, normalised[x][y], 1.0).getRGB();//Color.WHITE.getRGB();

//                if (normalised[x][y] < 0.6) {
//                    imageData[x][y] = sea.get(0, normalised[x][y], 0.6).getRGB();
//                }
//                else {
//                    imageData[x][y] = land.get(0.6, normalised[x][y], 1.0).getRGB();
//                }

                contourLines(normalised, imageData, x, y);

                lightAngles(normalised, lighting, x, y);

                considerHut(normalised, x, y, random, huts);

//                lighting(normalised, imageData, x, y, landDepth);
            }
            if (x % 10 == 0) {
                System.out.print(".");
            }
        }
        System.out.println();

        applyLighting(imageData, lighting, normalised);

        cullHuts(huts);
        applyHuts(imageData, huts, random);

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
        int n = Data.readAndWrite("HeightMap", () -> 0, x -> x+1);
        Data.spitImage(imageData, "HeightMap_" + n);

    }

    private void cullHuts(List<Point> huts) {
        for (Point hut : huts) {
            for (Point other : huts) {
                if (hut != other) {

//                    if (Math.abs(hut.x - other.x) < 20.0 && Math.abs(hut.y - other.y) < 20.0) {
                        if (close(hut.x - other.x, hut.y - other.y, 28.0)) {
                            huts.remove(hut);
                            cullHuts(huts);
                            return;
                        }
//                    }
                }
            }
        }
    }


    private void applyHuts(int[][] imageData, List<Point> huts, Random random) {
        double lightingAngle = 7.0 * (Constants.TAU / 8.0);
        for (Point hut : huts) {
            for (double theta = 0; theta < Constants.TAU; theta += 0.02) {

                double hMax = 10.0 + random.nextDouble() * 2.0;
                double ct = Math.cos(theta);
                double st = Math.sin(theta);
                double sl = Math.sin(theta + lightingAngle);
                Color color = sticks.get(random);
                color = lerp(Color.BLACK, color, (1.5 + sl)/2.5);

                for (double h = 0.0; h < hMax; h += 0.5) {
                    int x = (int) (hut.x + st * h);
                    int y = (int) (hut.y + ct * h);
                    if (inBounds(0, x, imageData.length) && inBounds(0, y, imageData[0].length)) {
                        imageData[x][y] = color.getRGB();
                    }
                }
            }
        }
    }

    private void considerHut(double[][] normalised, int x, int y, Random random, List<Point> huts) {
        if (normalised[x][y] > (seaDepth * 1.1)
                && normalised[x][y] < (seaDepth * 1.3)
                && random.nextDouble() > 0.9999) {
            huts.add(new Point(x, y));
        }
    }

    private void applyLighting(int[][] imageData, double[][] lighting, double[][] heightData) {
        double min = 0;
        double max = 0;
        for (int x = 0; x < lighting.length; x++) {
            for (int y = 0; y < lighting[0].length; y++) {
                min = Math.min(min, lighting[x][y]);
                max = Math.max(max, lighting[x][y]);
            }
        }
        System.out.println("Min / Max: " + min + " / " + max);

//        float min2 = 1.0f;
//        float max2 = 0.0f;
        for (int x = 0; x < lighting.length; x++) {
            for (int y = 0; y < lighting[0].length; y++) {

                float l = (float) ((lighting[x][y] - min) / (max - min));
//                min2 = Math.min(min2, l);
//                max2 = Math.max(max2, l);

                Color shadow = Color.BLACK;
                Color oldColor = new Color(imageData[x][y]);
                double prop = heightData[x][y] > seaDepth ? l : 1.0 - ((1.0 - l) / 2.0);


//                prop = l;

                imageData[x][y] = lerp(shadow, oldColor, prop).getRGB();
            }
            if (x % 10 == 0) {
                System.out.print("*");
            }
        }
        System.out.println();
//        System.out.println("Min / Max: " + min2 + " / " + max2);
    }

    private void lightAngles(double[][] heightData, double[][] angles, int x, int y) {

        int x1 = Data.clamp(0, x - 5, heightData.length - 1);
        int y1 = Data.clamp(0, y + 5, heightData[0].length - 1);
        int x2 = Data.clamp(0, x + 5, heightData.length - 1);
        int y2 = Data.clamp(0, y - 5, heightData[0].length - 1);

        angles[x][y] = heightData[x1][y1] - heightData[x2][y2];
    }



    private void lighting(double[][] heightData, int[][] imageData, int x, int y, double landDepth) {

        int x1 = Data.clamp(0, x - 5, heightData.length - 1);
        int y1 = Data.clamp(0, y + 5, heightData[0].length - 1);
        int x2 = Data.clamp(0, x + 5, heightData.length - 1);
        int y2 = Data.clamp(0, y - 5, heightData[0].length - 1);

        double scale = 50.0;
        double h1 = scale * heightData[x1][y1];
        double h2 = scale * heightData[x2][y2];
        float l = (float) Data.clamp(0.0, (0.5 + h1 - h2), 1.0);
        Color lighting = new Color(l, l, l);
        Color oldColor = new Color(imageData[x][y]);

        double prop = heightData[x][y] > seaDepth ? 0.75 : 0.95;

        imageData[x][y] = lerp(lighting, oldColor, prop).getRGB();

//        double here = scale * heightData[x][y];
//        double there = scale * heightData[Math.max(0, x - 1)][Math.max(0, y - 1)];
//        float light = (float) Data.clamp(0.0, (0.5 + here - there), 1.0);
//        Color lighting = new Color(light, light, light);
//        Color oldColor = new Color(imageData[x][y]);
//        imageData[x][y] = lerp(lighting, oldColor, 0.75).getRGB();
    }

    private void contourLines(double[][] heightData, int[][] imageData, int x, int y) {

        Color lineColor = Color.WHITE;
        double prop = 0.8;
        for (double c = 0.0; c <= 1.0; c += 0.04) {

            if (c >= seaDepth) {
                lineColor = Color.BLACK;
                prop = 0.5;
            }

            if (heightData[x][y] > c) {
                for (Point neighbour : neighbours(new Point(x, y), heightData.length, heightData[0].length)) {
                    if (heightData[neighbour.x][neighbour.y] < c) {

                        Color oldColor = new Color(imageData[x][y]);
                        imageData[x][y] = lerp(lineColor, oldColor, prop).getRGB();
//                                    imageData[x][y] = Color.BLACK.getRGB();
                        break;
                    }
                }
            }
        }
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
