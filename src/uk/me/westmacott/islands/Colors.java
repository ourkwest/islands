package uk.me.westmacott.islands;

import java.awt.*;
import java.util.LinkedList;
import java.util.Random;

import static uk.me.westmacott.islands.Data.lerp;
import static uk.me.westmacott.islands.Data.serp;

public class Colors {
    

    public static void main(String[] args) {

        System.out.println(Math.cos(0.0 * Math.PI));
        System.out.println(Math.cos(0.5 * Math.PI));
        System.out.println(Math.cos(1.0 * Math.PI));
        System.out.println("----------");
        System.out.println(lerp(Color.BLACK, Color.WHITE, 0.0));
        System.out.println(serp(Color.BLACK, Color.WHITE, 0.0));
        System.out.println("----------");
        System.out.println(lerp(Color.BLACK, Color.WHITE, 0.25));
        System.out.println(serp(Color.BLACK, Color.WHITE, 0.25));
        System.out.println("----------");
        System.out.println(lerp(Color.BLACK, Color.WHITE, 0.5));
        System.out.println(serp(Color.BLACK, Color.WHITE, 0.5));
        System.out.println("----------");
        System.out.println(lerp(Color.BLACK, Color.WHITE, 0.75));
        System.out.println(serp(Color.BLACK, Color.WHITE, 0.75));
        System.out.println("----------");
        System.out.println(lerp(Color.BLACK, Color.WHITE, 1.0));
        System.out.println(serp(Color.BLACK, Color.WHITE, 1.0));

    }

    public interface ColorStops {

        default Color get(Random random) {
            return get(random.nextDouble());
        }

        default Color get(double distance) {
            return get(0.0, distance, 1.0);
        }

        Color get(double start, double distance, double end);

        static SimpleColorStops startingAt(Color start) {
            return new SimpleColorStops(start);
        }

        static ColorStopChain chain(ColorStops start, double weighting) {
            return new ColorStopChain(start, weighting);
        }

    }

    public static class SimpleColorStops implements ColorStops {

        private final LinkedList<Node> colors = new LinkedList<>();
        private double total;

        private SimpleColorStops(Color start) {
            stepOf(0.0, start);
        }

        public SimpleColorStops stepOf(double distance, Color color) {
            colors.add(new Node(distance, color));
            total += distance;
            return this;
        }

        public Color get(double start, double distance, double end) {
            double index = total * (distance - start) / (end - start);
            return getAbsolute(index);
        }

        private Color getAbsolute(double distance) {
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

        private static class Node {
            final double distance;
            final Color color;

            private Node(double distance, Color color) {
                this.distance = distance;
                this.color = color;
            }
        }

    }

    public static class ColorStopChain implements ColorStops {

        private final LinkedList<Node> colorStops = new LinkedList<>();
        private double total;

        private ColorStopChain(ColorStops start, double weighting) {
            andThen(start, weighting);
        }

        public ColorStopChain andThen(ColorStops color, double weighting) {
            colorStops.add(new Node(weighting, color));
            total += weighting;
            return this;
        }

        public Color get(double start, double distance, double end) {
            double index = total * (distance - start) / (end - start);
            return getAbsolute(index);
        }

        private Color getAbsolute(double distance) {
            Node last = colorStops.getFirst();
            for (Node node : colorStops) {
                if (distance < node.weight) {
                    return node.colorStops.get(0.0, distance, node.weight);
                }
                distance -= node.weight;
                last = node;
            }
            return last.colorStops.get(0.0, distance, last.weight);
        }

        private static class Node {
            final double weight;
            final ColorStops colorStops;

            private Node(double weight, ColorStops colorStops) {
                this.weight = weight;
                this.colorStops = colorStops;
            }
        }

    }
}
