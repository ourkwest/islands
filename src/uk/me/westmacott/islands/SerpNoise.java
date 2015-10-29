package uk.me.westmacott.islands;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import static uk.me.westmacott.islands.Constants.TAU;
import static uk.me.westmacott.islands.Data.serp;

public class SerpNoise implements Noisy, Serializable {

    public static void main(String[] args) throws IOException {

        int n = Data.readAndWrite("SerpTest", () -> 0, x -> x + 1);

        new SerpNoise().render("SerpTest_" + n);
    }

    List<BiFunction<Double,Double,Double>> fns = new LinkedList<>();

    public SerpNoise(long seed) {
        this(new Random(seed));
    }

    public SerpNoise() {
        this(new Random());
    }

    public SerpNoise(Random random) {

        int noiseCount = 100;
        double[][] noises = new double[noiseCount][noiseCount];
        for (int x = 0; x < noises.length; x++) {
            for (int y = 0; y < noises[0].length; y++) {
                noises[x][y] = random.nextDouble();
            }
        }

        int layers = 100;
        double scale = 1.0;
        for (int i = 0; i < layers; i++) {
            scale = scale * 0.95;

            double theta = random.nextDouble() * TAU;
            double xf = Math.sin(theta);
            double yf = Math.cos(theta);
            double xOffset = random.nextDouble() * noiseCount;
            double yOffset = random.nextDouble() * noiseCount;
            double heightScale = scale / 1000;// * scale;
            double widthScale = scale * 1000;// * scale;

//            double xf = 1.0;
//            double yf = 0.0;

            
//            System.out.println("Scale: " + heightScale + " x " + widthScale);


            BiFunction<Double, Double, Double> z = (x, y) -> {

                x += xOffset;
                y += yOffset;
                x /= widthScale;
                y /= widthScale;

                double u = x * xf + y * yf;
                double v = x * yf - y * xf;

                u %= noiseCount;
                u += noiseCount;
                u %= noiseCount;
                v %= noiseCount;
                v += noiseCount;
                v %= noiseCount;

//                int u1 = ((((int)u) % noiseCount) + noiseCount) % noiseCount;
//                int u2 = (((u1 + 1) % noiseCount) + noiseCount) % noiseCount;
//                int v1 = ((((int)v) % noiseCount) + noiseCount) % noiseCount;
//                int v2 = (((v1 + 1) % noiseCount) + noiseCount) % noiseCount;

                int u1 = (int)u;
                int u2 = (u1 + 1) % noiseCount;
                int v1 = (int)v;
                int v2 = (v1 + 1) % noiseCount;

                double v1s = serp(noises[u1][v1], noises[u2][v1], u - u1);
                double v2s = serp(noises[u1][v2], noises[u2][v2], u - u1);
                return serp(v1s, v2s, v - v1) * heightScale;
            };
            fns.add(z);
        }

    }

    @Override
    public double[][] normalisedNoise(int width, int height, double scale) {
        String name = "SerpNoise_" + width + "_x_" + height + "_" + scale;
        return Data.cache(name, () -> normalisedNoiseUncached(width, height, scale), this.getClass());
    }

    public double[][] normalisedNoiseUncached(int width, int height, double scale) {

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
        for (BiFunction<Double, Double, Double> waveFn : fns) {
            result += waveFn.apply(x, y);
        }
        return result;
    }

    public void render(String name) throws IOException {

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
        Data.spitImage(data, name);
    }
}
