package uk.me.westmacott.islands;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public class SinNoise implements Noisy {

    public static double TAU = Math.PI * 2.0;


    public static void main(String[] args) throws IOException {

        System.out.println("Hi");
        SinNoise noise = new SinNoise(0);
        noise.render(0);
        noise.render(50);
        noise.render(99);


    }


    List<BiFunction<Double,Double,Double>> waves;

    public SinNoise(long seed) {
        this(new Random(seed));
    }

    public SinNoise() {
        this(new Random());
    }

    public SinNoise(Random random) {
        waves = new LinkedList<>();

        int layers = 100;
        double scale = 200.0;
        for (int i = 0; i < layers; i++) {
            scale = scale * 0.95;

            double theta = random.nextDouble() * TAU;
            double xf = Math.sin(theta);
            double yf = Math.cos(theta);
            double offset = random.nextDouble() * TAU;
            double heightScale = scale * scale;
            double widthScale = scale;// * scale;
            System.out.println("Scale: " + heightScale + " x " + widthScale);

//            double scale = (i + 0.1) * 10.0;//random.nextDouble();
//            int[] primes = Primes.getThreeRandomPrimes(random);
//            int pA = primes[0];
//            int pB = primes[1];
//            int pC = primes[2];
//            primes = Primes.getThreeRandomPrimes(random);
//            int pA2 = primes[0];
//            int pB2 = primes[1];
//            int pC2 = primes[2];
//            double oA = (random.nextDouble() - 0.5);
//            double oB = (random.nextDouble() - 0.5);
//            double oC = (random.nextDouble() - 0.5);

            BiFunction<Double, Double, Double> z = (x, y) -> {

                double h = x * xf + y * yf;
                double input = (h + offset) / widthScale;
                return Math.sin(input) * heightScale;

//                double u = x * xf + y * yf;
//                double v = x * yf - y * xf;
//
////                double a = (u + oA) / pA;
////                double b = (u + oB) / pB;
////                double c = (u + oC) / pC;
////                double d = (v + oA) / pA2;
////                double e = (v + oB) / pB2;
////                double f = (v + oC) / pC2;
////                return ((Math.sin(a / scale) + Math.sin(b / scale) + Math.sin(c / scale)) +
////                        (Math.sin(d / scale) + Math.sin(e / scale) + Math.sin(f / scale))) * scale;
////
////
//                double input1 = (u + offset) / scale;
//                double input2 = (v + offset) / scale;
//                return Math.sin(input1) * Math.sin(input2) * scale;
            };
            waves.add(z);
        }
    }

    @Override
    public double[][] normalisedNoise(int width, int height, double scale) {
        final double[][] raw = new double[width][height];
        final double[][] normalised = new double[width][height];
        double min = 256.0;
        double max = -256.0;
        int modPrint = width / 100;
        for (int x = 0; x < raw.length; x++) {
            for (int y = 0; y < raw[0].length; y++) {
                raw[x][y] = noise(x * scale / width, y * scale / height);
                min = Math.min(min, raw[x][y]);
                max = Math.max(max, raw[x][y]);
            }
            if (x % modPrint == 0) {
                System.out.print("r");
            }
        }
        System.out.println();

        for (int x = 0; x < raw.length; x++) {
            for (int y = 0; y < raw[0].length; y++) {
                normalised[x][y] = (raw[x][y] - min) / (max - min);
            }
            if (x % modPrint == 0) {
                System.out.print("n");
            }
        }
        System.out.println();

        return normalised;
    }

    public double noise(double x, double y) {
        double result = 0.0;
        for (BiFunction<Double, Double, Double> waveFn : waves) {
            result += waveFn.apply(x, y);
        }
        return result;
    }

    public double noise(double x, double y, int layer) {
        return waves.get(layer).apply(x, y);
    }

    public void render() throws IOException {

        double scale = 1;

        int size = 1500;
        int[][] data = new int[size][size];

        double min = 0;
        double max = 0;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                double n = noise(x * scale, y * scale);
                min = Math.min(min, n);
                max = Math.max(max, n);
            }
            if (x % 10 == 0) {
                System.out.print(".");
            }
        }

        System.out.println(min);
        System.out.println(max);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int l = (int)( (noise(x * scale, y * scale) - min) / (max - min) * 255);
                try {
                    data[x][y] = new Color(l, l, l).getRGB();
                }
                catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    System.out.println(l);
                }
            }
            if (x % 10 == 0) {
                System.out.print(".");
            }
        }
        spitImage(data, "1");
    }

    public void render(int layer) throws IOException {

        double scale = 1;

        int size = 1500;
        int[][] data = new int[size][size];

        double min = 0;
        double max = 0;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                double n = noise(x * scale, y * scale, layer);
                min = Math.min(min, n);
                max = Math.max(max, n);
            }
            if (x % 10 == 0) {
                System.out.print(".");
            }
        }

        System.out.println(min);
        System.out.println(max);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int l = (int)( (noise(x * scale, y * scale, layer) - min) / (max - min) * 255);
                try {
                    data[x][y] = new Color(l, l, l).getRGB();
                }
                catch (IllegalArgumentException e) {
//                    e.printStackTrace();
                    System.out.println(l);
                }
            }
            if (x % 10 == 0) {
                System.out.print(".");
            }
        }
        spitImage(data, "layer-" + layer);
    }

    private static void spitImage(int[][] data, Object suffix) throws IOException {
        int width = data.length;
        int height = data[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (data[x][y] != -1) {
                    image.setRGB(x, y, data[x][y] | 0xFF000000);
                }
            }
        }
        String filename = String.format("Test-%s", suffix);
        ImageIO.write(image, "png", new File("./" + filename + ".png"));
    }


}
