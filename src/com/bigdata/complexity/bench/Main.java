package com.bigdata.complexity.bench;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;


public class Main {

    private static final int REPEATS = 15;
    private static final String DEVICE = "LG Gram 16Z90R_Windows_11";

    public static void main(String[] args) throws IOException {
        int[] sizes = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 1024000, 2048000, 4096000};
        double[] xs = Arrays.stream(sizes).asDoubleStream().toArray();

        double[] meanLogN = new double[sizes.length];
        double[] medianLogN = new double[sizes.length];

        double[] meanN = new double[sizes.length];
        double[] medianN = new double[sizes.length];

        double[] meanNLogN = new double[sizes.length];
        double[] medianNLogN = new double[sizes.length];

        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];

            long[] logNTimes = new long[REPEATS];
            long[] nTimes = new long[REPEATS];
            long[] nLogNTimes = new long[REPEATS];

            for (int r = 0; r < REPEATS; r++) {
                nTimes[r] = ComplexityBench.timeLinearSum(n); //O(n)
                logNTimes[r] = ComplexityBench.timeBinarySearch(n); //O(Log n)
                nLogNTimes[r] = ComplexityBench.timeSort(n); //O(nLog n)
            }

            meanLogN[i] = Arrays.stream(logNTimes).mapToDouble(ComplexityBench::toMillis).average().orElse(0);
            meanN[i] = Arrays.stream(nTimes).mapToDouble(ComplexityBench::toMillis).average().orElse(0);
            meanNLogN[i] = Arrays.stream(nLogNTimes).mapToDouble(ComplexityBench::toMillis).average().orElse(0);

            medianLogN[i] = median(logNTimes);
            medianN[i] = median(nTimes);
            medianNLogN[i] = median(nLogNTimes);

            System.out.printf("n=%d | O(log n) mean=%.3f ms median=%.3f ms | O(n) mean=%.3f ms median=%.3f ms | O(n log n) mean=%.3f ms median=%.3f ms%n",
                    n, meanLogN[i], medianLogN[i], meanN[i], medianN[i], meanNLogN[i], medianNLogN[i]);
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

        // Combined chart of means
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

    private static void showAndSaveChart(String title, double[] x, double[] mean, double[] median,
                                         String filePath) throws IOException {

        XYChart chart = new XYChartBuilder()
                .width(800).height(600)
                .title(title + " — " + DEVICE)
                .xAxisTitle("n")
                .yAxisTitle("Time (µs)") // Changed to microseconds
                .build();

        // Convert ms to µs for better visibility
        double[] meanMicro = Arrays.stream(mean).map(v -> v * 1000).toArray();
        double[] medianMicro = Arrays.stream(median).map(v -> v * 1000).toArray();

        XYSeries sMean = chart.addSeries("Mean", x, meanMicro);
        sMean.setMarker(SeriesMarkers.CIRCLE);

        XYSeries sMedian = chart.addSeries("Median", x, medianMicro);
        sMedian.setMarker(SeriesMarkers.DIAMOND);

        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setXAxisDecimalPattern("####");
        chart.getStyler().setYAxisDecimalPattern("###.####"); // More precision for small values

        chart.getStyler().setXAxisMax(Double.valueOf(4_000_000.0)); // Limit X axis
        chart.getStyler().setYAxisMin(Double.valueOf(1.0));

        new SwingWrapper<>(chart).displayChart();
        BitmapEncoder.saveBitmap(chart, filePath, BitmapEncoder.BitmapFormat.PNG);
        System.out.println("Saved chart: " + filePath);
    }


    // New function: displays the three means together in a single chart
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

        // Limit X axis to 1,000,000
        chart.getStyler().setXAxisMax(Double.valueOf(1_000_000.0));
        chart.getStyler().setYAxisMax(Double.valueOf(100.0));

        new SwingWrapper<>(chart).displayChart();
        BitmapEncoder.saveBitmap(chart, filePath, BitmapEncoder.BitmapFormat.PNG);
        System.out.println("Saved combined chart: " + filePath);
    }

}
