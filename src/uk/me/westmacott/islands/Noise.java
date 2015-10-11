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

public class Noise {

    public static double TAU = Math.PI * 2.0;

    public static void main(String[] args) throws IOException {

        System.out.println("Hi");
        new Noise(0).render();




    }


    List<BiFunction<Double,Double,Double>> waves;

    public Noise(long seed) {

        Random random = new Random(seed);
        waves = new LinkedList<>();
        for (int i = 0; i < 100; i++) {

            double theta = random.nextDouble() * TAU;
            double xf = Math.sin(theta);
            double yf = Math.cos(theta);
            double offset = random.nextDouble() * TAU;
            double scale = i + 1;//random.nextDouble();

            BiFunction<Double, Double, Double> z = (x, y) -> {
                double h = x * xf + y * yf;
                double input = (h + offset) / scale;
                return Math.sin(input) * scale;
            };
            waves.add(z);
        }
    }

    public double noise(double x, double y) {
        double result = 0.0;
        for (BiFunction<Double, Double, Double> waveFn : waves) {
            result += waveFn.apply(x, y);
        }
        return result;
    }

    public void render() throws IOException {

        double scale = 1;

        int size = 500;
        int[][] data = new int[size][size];

        double min = 256.0;
        double max = -256.0;
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
