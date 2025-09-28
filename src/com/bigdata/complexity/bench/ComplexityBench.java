package com.bigdata.complexity.bench;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class ComplexityBench {

    // O(log n): búsqueda binaria
    public static long timeBinarySearch(int n) {
        int[] sorted = new int[n];
        for (int i = 0; i < n; i++) sorted[i] = i;
        int target = n / 2;

        long t0 = System.nanoTime();
        Arrays.binarySearch(sorted, target);
        long t1 = System.nanoTime();
        return t1 - t0;
    }

    // O(n): suma lineal
    public static long timeLinearSum(int n) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rnd.nextInt();

        long t0 = System.nanoTime();
        long sum = 0;
        for (int v : arr) sum += v;
        long t1 = System.nanoTime();

        if (sum == Long.MIN_VALUE) System.out.print("");
        return t1 - t0;
    }


    // O(n log n): ordenar
    public static long timeSort(int n) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rnd.nextInt();

        long t0 = System.nanoTime();
        Arrays.sort(arr);
        long t1 = System.nanoTime();
        return t1 - t0;
    }

    // Conversión ns -> ms
    public static double toMillis(long nanos) {
        return nanos / 1_000_000.0;
    }

    public static double standardDeviation(long[] arr) {
        double mean = Arrays.stream(arr).mapToDouble(ComplexityBench::toMillis).average().orElse(0);
        double sumSq = 0;
        for (long v : arr) {
            double ms = ComplexityBench.toMillis(v);
            sumSq += (ms - mean) * (ms - mean);
        }
        return Math.sqrt(sumSq / arr.length);
    }

}