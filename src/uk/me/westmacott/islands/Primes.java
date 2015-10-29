package uk.me.westmacott.islands;

import java.util.Arrays;
import java.util.Random;

public class Primes {

    private static int[] somePrimes = new int[]{
            11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59,
            61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109};

    static int getRandomPrime(Random random) {
        int index = random.nextInt(somePrimes.length);
        return somePrimes[index];
    }

    static int[] getThreeRandomPrimes(Random random) {
        int a = getRandomInt(random, somePrimes.length);
        int b = getRandomInt(random, somePrimes.length, a);
        int c = getRandomInt(random, somePrimes.length, a, b);
        return new int[]{somePrimes[a], somePrimes[b], somePrimes[c]};
    }

    static int getRandomInt(Random random, int bound, int...exclude) {
        trying:
        while (true) {
            int index = random.nextInt(bound);
            for (int i : exclude) {
                if (i == index) {
                    continue trying;
                }
            }
            return index;
        }
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(getThreeRandomPrimes(new Random())));
    }

}
