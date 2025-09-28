package com.bigdata.complexity.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})

public class complexityBench {

    @State(Scope.Thread)
    public static class Data {
        @Param({"1000", "2000", "4000", "8000", "16000", "32000", "64000"})
        public int n;

        int[] sortedArr;
        int[] arrForScan;
        int[] arrToSort;
        int targetExisting;

        @Setup(Level.Trial)
        public void setup() {
            ThreadLocalRandom rnd = ThreadLocalRandom.current();

            sortedArr = new int[n];
            for (int i = 0; i < n; i++) sortedArr[i] = i * 2;
            targetExisting = sortedArr[rnd.nextInt(n)];

            arrForScan = new int[n];
            for (int i = 0; i < n; i++) arrForScan[i] = rnd.nextInt();

            arrToSort = new int[n];
            for (int i = 0; i < n; i++) arrToSort[i] = rnd.nextInt();
        }
    }

    // O(log n): búsqueda binaria en array ordenado
    @Benchmark
    public void binarySearch_logN(Data d, Blackhole bh) {
        int idx = Arrays.binarySearch(d.sortedArr, d.targetExisting);
        bh.consume(idx);
    }

    // O(n): suma lineal de todos los elementos
    @Benchmark
    public void linearSum_n(Data d, Blackhole bh) {
        long sum = 0;
        int[] a = d.arrForScan;
        for (int i = 0; i < a.length; i++) sum += a[i];
        bh.consume(sum);
    }

    // O(n log n): ordenar (dual-pivot quicksort/introsort por debajo)
    @Benchmark
    public void sort_nlogn(Data d, Blackhole bh) {
        int[] copy = Arrays.copyOf(d.arrToSort, d.arrToSort.length);
        Arrays.sort(copy);
        bh.consume(copy[0]);
    }
}