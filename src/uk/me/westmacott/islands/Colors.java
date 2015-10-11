package uk.me.westmacott.islands;

import java.awt.*;
import java.util.LinkedList;

public class Colors {
    

    public static Color lerp(Color a, Color b, double proportion) {
        
        double inverse = 1.0 - proportion;
        
        int red = (int) (a.getRed() * inverse + b.getRed() * proportion);
        int grn = (int) (a.getGreen() * inverse + b.getGreen() * proportion);
        int blu = (int) (a.getBlue() * inverse + b.getBlue() * proportion);
        
        return new Color(red, grn, blu);
    }

    public static void main(String[] args) {

        System.out.println(ColorStops
                .startAt(Color.BLACK)
                .step(10, Color.WHITE)
                .step(10, Color.RED)
                .get(0.6, 0.8, 1.0));

        System.out.println(ColorStops
                .startAt(Color.BLACK)
                .step(10, Color.WHITE)
                .step(11, Color.RED)
                .get(0.6, 0.8, 1.0));

    }

    public static class ColorStops {

        private final LinkedList<Node> colors = new LinkedList<>();
        private double total;

        public static ColorStops startAt(Color start) {
            return new ColorStops(start);
        }

        private ColorStops(Color start) {
            step(0.0, start);
        }

        public ColorStops step(double distance, Color color) {
            colors.add(new Node(distance, color));
            total += distance;
            return this;
        }

        public Color get(double start, double distance, double end) {
            double index = total * (distance - start) / (end - start);
            return get(index);
        }

        public Color get(double distance) {
            Node last = colors.getFirst();
            for (Node node : colors) {
                if (distance < node.distance) {
                    return lerp(last.color, node.color, distance / node.distance);
                }
                distance -= node.distance;
                last = node;
            }
            return colors.getLast().color;
        }

        public double total() {
            return total;
        }

        private static class Node {
            final double distance;
            final Color color;

            private Node(double distance, Color color) {
                this.distance = distance;
                this.color = color;
            }
        }

    }
    
}
