package com.bigdata.complexity.bench;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

public class Main {

    private static final int REPEATS = 100; // 15 results will be kept, but we run 16 and discard the first
    private static final String DEVICE = "LG Gram 16Z90R_Windows_11";

    public static void main(String[] args) throws IOException {
        int[] sizes = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 1024000, 2048000, 4096000};
        double[] xs = Arrays.stream(sizes).asDoubleStream().toArray();

        double[] meanLogN = new double[sizes.length];
        double[] medianLogN = new double[sizes.length];
        double[] stdLogN = new double[sizes.length];   // <-- standard deviation

        double[] meanN = new double[sizes.length];
        double[] medianN = new double[sizes.length];
        double[] stdN = new double[sizes.length];      // <-- standard deviation

        double[] meanNLogN = new double[sizes.length];
        double[] medianNLogN = new double[sizes.length];
        double[] stdNLogN = new double[sizes.length];  // <-- standard deviation

        // Warm-up with multiple sizes
        int[] warmupSizes = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 1024000, 2048000, 4096000};
        for (int s : warmupSizes) {
            for (int w = 0; w < 5; w++) {
                ComplexityBench.timeLinearSum(s);
                ComplexityBench.timeBinarySearch(s);
                ComplexityBench.timeSort(s);
            }
        }

        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];

            long[] logNTimes = new long[REPEATS];
            long[] nTimes = new long[REPEATS];
            long[] nLogNTimes = new long[REPEATS];

            System.gc();
            // Run REPEATS+3 and discard the first three
            for (int r = 0; r < REPEATS + 3; r++) {
                long tN = ComplexityBench.timeLinearSum(n);       // O(n)
                long tLogN = ComplexityBench.timeBinarySearch(n); // O(log n)
                long tNLogN = ComplexityBench.timeSort(n);        // O(n log n)

                if (r >= 3) { // discard the first 3
                    nTimes[r - 3] = tN;
                    logNTimes[r - 3] = tLogN;
                    nLogNTimes[r - 3] = tNLogN;
                }
            }

            // Statistics
            meanLogN[i] = Arrays.stream(logNTimes).mapToDouble(ComplexityBench::toMillis).average().orElse(0);
            meanN[i] = Arrays.stream(nTimes).mapToDouble(ComplexityBench::toMillis).average().orElse(0);
            meanNLogN[i] = Arrays.stream(nLogNTimes).mapToDouble(ComplexityBench::toMillis).average().orElse(0);

            medianLogN[i] = median(logNTimes);
            medianN[i] = median(nTimes);
            medianNLogN[i] = median(nLogNTimes);

            stdLogN[i] = stdDev(logNTimes);
            stdN[i] = stdDev(nTimes);
            stdNLogN[i] = stdDev(nLogNTimes);

            System.out.printf(
                    "n=%d | O(log n) mean=%.3f ms median=%.3f ms σ=%.3f | " +
                            "O(n) mean=%.3f ms median=%.3f ms σ=%.3f | " +
                            "O(n log n) mean=%.3f ms median=%.3f ms σ=%.3f%n",
                    n,
                    meanLogN[i], medianLogN[i], stdLogN[i],
                    meanN[i], medianN[i], stdN[i],
                    meanNLogN[i], medianNLogN[i], stdNLogN[i]
            );
        }

        Path graphicsDir = Paths.get("src/com/bigdata/complexity/bench/plots");
        Files.createDirectories(graphicsDir);

        // Individual charts
        showAndSaveChart("O(log n)", xs, meanLogN, medianLogN,
                graphicsDir.resolve("logn_plot_" + DEVICE + ".png").toString());
        showAndSaveChart("O(n)", xs, meanN, medianN,
                graphicsDir.resolve("n_plot_" + DEVICE + ".png").toString());
        showAndSaveChart("O(n log n)", xs, meanNLogN, medianNLogN,
                graphicsDir.resolve("nlogn_plot_" + DEVICE + ".png").toString());

        // Combined chart
        showAndSaveCombinedChart(xs, meanLogN, meanN, meanNLogN,
                graphicsDir.resolve("combined_mean_plot_" + DEVICE + ".png").toString());
    }

    private static double median(long[] arr) {
        Arrays.sort(arr);
        int mid = arr.length / 2;
        if (arr.length % 2 == 0) {
            return ComplexityBench.toMillis(arr[mid - 1] + arr[mid]) / 2.0;
        } else {
            return ComplexityBench.toMillis(arr[mid]);
        }
    }

    // New function for standard deviation
    private static double stdDev(long[] arr) {
        double mean = Arrays.stream(arr).mapToDouble(ComplexityBench::toMillis).average().orElse(0);
        double variance = Arrays.stream(arr)
                .mapToDouble(ComplexityBench::toMillis)
                .map(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }

    private static void showAndSaveChart(String title, double[] x, double[] mean, double[] median,
                                         String filePath) throws IOException {

        XYChart chart = new XYChartBuilder()
                .width(800).height(600)
                .title(title + " — " + DEVICE)
                .xAxisTitle("n")
                .yAxisTitle("Time (µs)")
                .build();

        double[] meanMicro = Arrays.stream(mean).map(v -> v * 1000).toArray();
        double[] medianMicro = Arrays.stream(median).map(v -> v * 1000).toArray();

        XYSeries sMean = chart.addSeries("Mean", x, meanMicro);
        sMean.setMarker(SeriesMarkers.CIRCLE);

        XYSeries sMedian = chart.addSeries("Median", x, medianMicro);
        sMedian.setMarker(SeriesMarkers.DIAMOND);

        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setXAxisDecimalPattern("####");
        chart.getStyler().setYAxisDecimalPattern("###.####");

        chart.getStyler().setXAxisMax(Double.valueOf(4_000_000.0));
        chart.getStyler().setYAxisMin(Double.valueOf(0.0));

        new SwingWrapper<>(chart).displayChart();
        BitmapEncoder.saveBitmap(chart, filePath, BitmapEncoder.BitmapFormat.PNG);
        System.out.println("Saved chart: " + filePath);
    }

    private static void showAndSaveCombinedChart(double[] x, double[] meanLogN, double[] meanN, double[] meanNLogN,
                                                 String filePath) throws IOException {

        XYChart chart = new XYChartBuilder()
                .width(800).height(600)
                .title("Mean Comparison — " + DEVICE)
                .xAxisTitle("n")
                .yAxisTitle("Time (ms)")
                .build();

        XYSeries sLogN = chart.addSeries("O(log n)", x, meanLogN);
        sLogN.setMarker(SeriesMarkers.CIRCLE);

        XYSeries sN = chart.addSeries("O(n)", x, meanN);
        sN.setMarker(SeriesMarkers.DIAMOND);

        XYSeries sNLogN = chart.addSeries("O(n log n)", x, meanNLogN);
        sNLogN.setMarker(SeriesMarkers.SQUARE);

        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setXAxisDecimalPattern("####");
        chart.getStyler().setYAxisDecimalPattern("###.##");

        chart.getStyler().setXAxisMax(Double.valueOf(1_000_000.0));
        chart.getStyler().setYAxisMax(Double.valueOf(100.0));

        new SwingWrapper<>(chart).displayChart();
        BitmapEncoder.saveBitmap(chart, filePath, BitmapEncoder.BitmapFormat.PNG);
        System.out.println("Saved combined chart: " + filePath);
    }

}
