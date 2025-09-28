package com.bigdata.complexity.bench;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

public class Main {

    private static final int REPEATS = 15;

    private static final String DEVICE = "Asus_Vivobook";

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
                logNTimes[r] = ComplexityBench.timeBinarySearch(n);
                nTimes[r] = ComplexityBench.timeLinearSum(n);
                nLogNTimes[r] = ComplexityBench.timeSort(n);
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

        showAndSaveChart("O(log n)", xs, meanLogN, medianLogN,
                graphicsDir.resolve("logn_plot_" + DEVICE + ".png").toString());

        showAndSaveChart("O(n)", xs, meanN, medianN,
                graphicsDir.resolve("n_plot_" + DEVICE + ".png").toString());

        showAndSaveChart("O(n log n)", xs, meanNLogN, medianNLogN,
                graphicsDir.resolve("nlogn_plot_" + DEVICE + ".png").toString());
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
                .yAxisTitle("Tiempo (ms)")
                .build();

        XYSeries sMean = chart.addSeries("Mean", x, mean);
        sMean.setMarker(SeriesMarkers.CIRCLE);

        XYSeries sMedian = chart.addSeries("Median", x, median);
        sMedian.setMarker(SeriesMarkers.DIAMOND);

        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setXAxisDecimalPattern("####");
        chart.getStyler().setYAxisDecimalPattern("###.##");

        new SwingWrapper<>(chart).displayChart();
        BitmapEncoder.saveBitmap(chart, filePath, BitmapEncoder.BitmapFormat.PNG);
        System.out.println("Saved chart: " + filePath);
    }
}