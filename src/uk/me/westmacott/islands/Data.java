package uk.me.westmacott.islands;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class Data {

    // Linear intERPolation
    public static Color lerp(Color a, Color b, double proportion) {

        double inverse = 1.0 - proportion;

        int red = (int) (a.getRed() * inverse + b.getRed() * proportion);
        int grn = (int) (a.getGreen() * inverse + b.getGreen() * proportion);
        int blu = (int) (a.getBlue() * inverse + b.getBlue() * proportion);

        return new Color(red, grn, blu);
    }

    // S-curve intERPolation
    public static Color serp(Color a, Color b, double proportion) {

        proportion = (1.0 - Math.cos(Math.PI * proportion)) / 2.0;

        double inverse = 1.0 - proportion;

        int red = (int) (a.getRed() * inverse + b.getRed() * proportion);
        int grn = (int) (a.getGreen() * inverse + b.getGreen() * proportion);
        int blu = (int) (a.getBlue() * inverse + b.getBlue() * proportion);

        return new Color(red, grn, blu);
    }

    // S-curve intERPolation
    public static double serp(double a, double b, double proportion) {
        proportion = (1.0 - Math.cos(Math.PI * proportion)) / 2.0;
        double inverse = 1.0 - proportion;
        return (a * inverse + b * proportion);
    }


    public static void spitImage(int[][] data, Object name) throws IOException {
        spitImage(render(data), name);
    }

    public static void spitImage(BufferedImage image, Object name) throws IOException {
        ImageIO.write(image, "png", new File("./" + name + ".png"));
        System.out.println("Wrote: " + name);
    }

    public static BufferedImage render(int[][] imageData) {
        int width = imageData.length;
        int height = imageData[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
//                if (data[x][y] != -1) {
                    image.setRGB(x, y, imageData[x][y] | 0xFF000000);
//                }
            }
        }
        return image;
    }


    public static double clamp(double min, double value, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int min, int value, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static boolean inBounds(int min, int value, int max) {
        return min <= value && value < max;
    }

    public static boolean close(double xDiff, double yDiff, double distance) {
        return (xDiff * xDiff) + (yDiff * yDiff) < (distance * distance);
    }

    public static double distance(DoublePoint a, DoublePoint b) {
        return distance(a.x, a.y, b.x, b.y);
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        double xDiff = x1 - x2;
        double yDiff = y1 - y2;
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
    }

    private static Path path = Paths.get(".", "data");
    static {
        path.toFile().mkdir();
    }

    public static <T> T readOrWrite(String name, Supplier<T> supplier) {
        File file = path.resolve(Paths.get(name + ".data")).toFile();
        T data;
        try {
            data = read(file);
            System.out.println("Read '" + name + "'.");
        }
        catch (Exception e) {
            data = write(supplier.get(), file);
            System.out.println("Wrote '" + name + "'.");
        }
        return data;
    }

    public static <T> T cache(String name, Supplier<T> supplier, Class... dependencies) {
        long hash = 0;
        for (Class dependency : dependencies) {
            System.out.println(dependency);
            System.out.println(ObjectStreamClass.lookup(dependency));
            long serialVersionUid = ObjectStreamClass.lookup(dependency).getSerialVersionUID();
            hash ^= serialVersionUid;
        }
        String key = name + hash;
        return readOrWrite(key, supplier);
    }

    public static <T> T readAndWrite(String name, Supplier<T> supplier, Function<T,T> mutator) {
        File file = path.resolve(Paths.get(name + ".data")).toFile();
        T data;
        try {
            data = read(file);
            System.out.println("Read '" + name + "'.");
        }
        catch (Exception e) {
            data = supplier.get();
        }
        data = write(mutator.apply(data), file);
        System.out.println("Wrote '" + name + "'.");
        return data;
    }

    private static <T> T write(T data, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    private static <T> T read(File file) throws IOException, ClassNotFoundException {
        T data;FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        data = (T) ois.readObject();
        return data;
    }

    public static int[] newArray(int initialValue, int size) {
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = initialValue;
        }
        return result;
    }

    public static int[][] newArray(int initialValue, int size0, int size1) {
        int[][] result = new int[size0][size1];
        for (int i = 0; i < size0; i++) {
            result[i] = newArray(initialValue, size1);
        }
        return result;
    }

    public static int[][][] newArray(int initialValue, int size0, int size1, int size2) {
        int[][][] result = new int[size0][size1][size2];
        for (int i = 0; i < size0; i++) {
            result[i] = newArray(initialValue, size1, size2);
        }
        return result;
    }


    public static class Pair<T> {
        public final T first;
        public final T second;
        public Pair(T first, T second) {
            this.first = first;
            this.second = second;
        }
    }

    public static class Triple<T> {
        public final T first;
        public final T second;
        public final T third;
        public Triple(T first, T second, T third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public boolean contains(T item) {
            return first == item || second == item || third ==item;
        }
    }

    public static <T> Iterable<Pair<T>> pairwise(Iterable<T> iterable) {
        Iterator<T> it = iterable.iterator();
        return () -> new Iterator<Pair<T>>() {

            T current;

            private void init() {
                if (current == null && it.hasNext()) {
                    current = it.next();
                }
            }

            @Override
            public boolean hasNext() {
                init();
                return it.hasNext();
            }

            @Override
            public Pair<T> next() {
                init();
                T next = it.next();
                Pair<T> result = new Pair<>(current, next);
                current = next;
                return result;
            }
        };
    }

    public static <T> Iterable<Triple<T>> triplewise(Iterable<T> iterable) {
        Iterator<T> it = iterable.iterator();
        return () -> new Iterator<Triple<T>>() {

            T last;
            T current;

            private void init() {
                if (current == null && it.hasNext()) {
                    current = it.next();
                }
                if (last == null && it.hasNext()) {
                    last = current;
                    current = it.next();
                }
            }

            @Override
            public boolean hasNext() {
                init();
                return it.hasNext();
            }

            @Override
            public Triple<T> next() {
                init();
                T next = it.next();
                Triple<T> result = new Triple<>(last, current, next);
                last = current;
                current = next;
                return result;
            }
        };
    }

//    public static void iterray(Object array, Function transformer) {
//
//        if (array.getClass().getCanonicalName().endsWith("[]")) {
//            Array.
//        }
//
//        System.out.println(array.getClass().getCanonicalName());
//        System.out.println(array instanceof Array);
//    }
}
