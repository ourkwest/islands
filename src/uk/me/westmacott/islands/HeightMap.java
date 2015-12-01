package uk.me.westmacott.islands;

import uk.me.westmacott.islands.Colors.ColorStops;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static uk.me.westmacott.islands.Data.*;

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

        final double[][] heightData = noise.normalisedNoise(width, height, 1500);
        final double[][] lighting = new double[width][height];
        final List<Point> huts = new LinkedList<>();

        Normals normals = new Normals(heightData);
        lightAmounts(normals, lighting);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                imageData[x][y] = colorStops.get(0.0, heightData[x][y], 1.0).getRGB();//Color.WHITE.getRGB();

//                if (normalised[x][y] < 0.6) {
//                    imageData[x][y] = sea.get(0, normalised[x][y], 0.6).getRGB();
//                }
//                else {
//                    imageData[x][y] = land.get(0.6, normalised[x][y], 1.0).getRGB();
//                }

                contourLines(heightData, imageData, x, y);

//                lightAngles(heightData, lighting, x, y);

                considerHut(heightData, x, y, random, huts);

//                lighting(normalised, imageData, x, y, landDepth);
            }
            if (x % 10 == 0) {
                System.out.print(".");
            }
        }
        System.out.println();

        renderLighting(imageData, lighting, heightData);

        cullHuts(huts);

        BufferedImage image = Data.render(imageData);

        renderPaths(image, huts, random, heightData, normals);

        renderHuts(image, huts, random);

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
        Data.spitImage(image, "HeightMap_" + n);

    }

    private void renderPaths(BufferedImage image, List<Point> huts, Random random,
                             double[][] heightData, Normals normals) {

        Graphics2D graphics = image.createGraphics();
        int defaultStepSize = 10;
        int pathLimit = Math.max(image.getHeight(), image.getWidth()) / 3;

        LinkedList<LinkedList<DoublePoint>> paths = new LinkedList<>();

        for (int i = 0; i < huts.size(); i++) {
            for (int j = 0; j < i; j++) {

                Point hutA = huts.get(i);
                Point hutB = huts.get(j);

                double distance = distance(hutA.x, hutA.y, hutB.x, hutB.y);
                if (distance > pathLimit) {
                    continue;
                }
                int steps = clamp(2, (int) distance / defaultStepSize, 100);
                double xStep = (double)(hutB.x - hutA.x) / (double)steps;
                double yStep = (double)(hutB.y - hutA.y) / (double)steps;

                LinkedList<DoublePoint> path = new LinkedList<>();
                double x = hutA.x;
                double y = hutA.y;
                for (int k = 0; k <= steps; k++) {
                    path.add(new DoublePoint((int)x, (int)y));
                    x += xStep;
                    y += yStep;
                }
                paths.add(path);
            }
        }


        // TODO: iterate!
        // push all path points towards nearby path points
        // push all path points towards their neighbours
        // push all path points towards the height of their neighbours
        // push all path points above sea level
        // TODO: more of this: prevent path nodes being too close to their own neighbours? explicitly push away!

        List<Triple<DoublePoint>> pathNodes = new ArrayList<>();
        for (LinkedList<DoublePoint> path : paths) {
            for (Triple<DoublePoint> triple : triplewise(path)) {
                pathNodes.add(triple);
            }
        }

        int pathIterations = 300;
        for (int i = 0; i < pathIterations; i++) {

            Collections.shuffle(pathNodes, random); // minimise ordering-based artifacts?

            // Attract everyone
//            for (Triple<DoublePoint> nodeA : pathNodes) {
//                for (Triple<DoublePoint> nodeB : pathNodes) {
//                    if (!nodeA.contains(nodeB.second)) {
//                        double d = distance(nodeA.second, nodeB.second);
//                        double factor = 10.0 * Math.max(1.0, Math.pow(d / 10.0, 4.0));
//                        double xStep = (nodeA.second.x - nodeB.second.x) / factor;
//                        double yStep = (nodeA.second.y - nodeB.second.y) / factor;
//                        nodeA.second.x -= xStep;
//                        nodeA.second.y -= yStep;
//                        nodeB.second.x += xStep;
//                        nodeB.second.y += yStep;
//                    }
//                }
//            }

            // Attract neighbours
            for (Triple<DoublePoint> node : pathNodes) {
                if (distance(node.third, node.second) > defaultStepSize) {
                    node.second.x += ((node.third.x - node.second.x) / 10);
                    node.second.y += ((node.third.y - node.second.y) / 10);
                }
                if (distance(node.first, node.second) > defaultStepSize) {
                    node.second.x += ((node.first.x - node.second.x) / 10);
                    node.second.y += ((node.first.y - node.second.y) / 10);
                }
            }

            // Move towards height of neighbours
            for (Triple<DoublePoint> node : pathNodes) {
                double height1 = heightData[((int) node.first.x)][((int) node.first.y)];
                double height2 = heightData[((int) node.second.x)][((int) node.second.y)];
                double height3 = heightData[((int) node.third.x)][((int) node.third.y)];
                double targetHeight = height1 + height3 / 2;
                double amount = (targetHeight - height2) * 100.0;

                node.second.x -= amount * normals.dx[((int) node.second.x)][((int) node.second.y)];
                node.second.y -= amount * normals.dy[((int) node.second.x)][((int) node.second.y)];

                node.second.x = Data.clamp(0, node.second.x, heightData.length - 1);
                node.second.y = Data.clamp(0, node.second.y, heightData.length - 1);

            }

            for (List<DoublePoint> path : paths) {
                for (DoublePoint point : path) {
                    graphics.setColor(Color.PINK);
                    graphics.drawLine(
                            (int) point.x, (int) point.y,
                            (int) point.x, (int) point.y);
                }
            }

            System.out.print("\r " + i);
        }
        System.out.println();


        for (List<DoublePoint> path : paths) {
            for (Pair<DoublePoint> pair : pairwise(path)) {

                graphics.setColor(Color.BLUE);
                graphics.drawLine(
                        (int) pair.first.x, (int) pair.first.y,
                        (int) pair.second.x, (int) pair.second.y);

                graphics.setColor(Color.RED);
                graphics.drawLine(
                        (int) pair.first.x, (int) pair.first.y,
                        (int) pair.first.x, (int) pair.first.y);

            }
        }

    }

    // TODO: rather than culling them, we should spread them
    // culling like this biases against random clustering
    // we should also make some huts a bit bigger depending on proximity of other huts?
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


    private void renderHuts(BufferedImage image, List<Point> huts, Random random) {
        Graphics2D graphics = image.createGraphics();
        double lightingAngle = 7.0 * (Constants.TAU / 8.0);
        for (Point hut : huts) {

            double size = 6.0 + random.nextDouble() * 4.0;
            for (double theta = 0; theta < Constants.TAU; theta += 0.02) {

                double sizeBit = size + random.nextDouble() * 2.0;
                double ct = Math.cos(theta);
                double st = Math.sin(theta);
                double sl = Math.sin(theta + lightingAngle);
                Color color = sticks.get(random);
                color = lerp(Color.BLACK, color, (1.5 + sl)/2.5);

                graphics.setColor(color);
                graphics.drawLine(hut.x, hut.y, (int)(hut.x + st * sizeBit), (int)(hut.y + ct * sizeBit));

//                for (double h = 0.0; h < hMax; h += 0.5) {
//                    int x = (int) (hut.x + st * h);
//                    int y = (int) (hut.y + ct * h);
//
//                    if (inBounds(0, x, image.length) && inBounds(0, y, imageData[0].length)) {
//                        imageData[x][y] = color.getRGB();
//                    }
//                }
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

    private void renderLighting(int[][] imageData, double[][] lighting, double[][] heightData) {
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

    private static class Normals {

        double[][] dx;
        double[][] dy;

        public Normals(double[][] heightData) {
            dx = new double[heightData.length][heightData[0].length];
            dy = new double[heightData.length][heightData[0].length];
            for (int x1 = 0; x1 < heightData.length; x1++) {
                for (int y1 = 0; y1 < heightData[0].length; y1++) {
                    int x0 = Data.clamp(0, x1 - 1, heightData.length - 1);
                    int x2 = Data.clamp(0, x1 + 1, heightData.length - 1);
                    int y0 = Data.clamp(0, y1 - 1, heightData[0].length - 1);
                    int y2 = Data.clamp(0, y1 + 1, heightData[0].length - 1);
                    dx[x1][y1] = heightData[x0][y1] - heightData[x2][y1];
                    dy[x1][y1] = heightData[x1][y0] - heightData[x1][y2];
                }
            }
        }
    }

    private void lightAmounts(Normals normals, double[][] angles) {
        for (int x = 0; x < angles.length; x++) {
            for (int y = 0; y < angles[0].length; y++) {
                angles[x][y] = normals.dx[x][y] - normals.dy[x][y];
            }
        }
    }

    private void lightAngles(double[][] heightData, double[][] angles, int x, int y) {

        int lightEvaluationDistance = 1;
        int x1 = Data.clamp(0, x - lightEvaluationDistance, heightData.length - 1);
        int y1 = Data.clamp(0, y + lightEvaluationDistance, heightData[0].length - 1);
        int x2 = Data.clamp(0, x + lightEvaluationDistance, heightData.length - 1);
        int y2 = Data.clamp(0, y - lightEvaluationDistance, heightData[0].length - 1);

        angles[x][y] = heightData[x1][y1] - heightData[x2][y2];
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
