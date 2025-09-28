package com.bigdata.complexity.bench;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {
        int[] sizes = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 1024000};

        double[] xs = Arrays.stream(sizes).asDoubleStream().toArray();
        double[] yLogN  = new double[sizes.length];
        double[] yN     = new double[sizes.length];
        double[] yNLogN = new double[sizes.length];

        String device = "MacBook Air M3";

        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];
            yLogN[i]  = ComplexityBench.toMillis(ComplexityBench.timeBinarySearch(n));
            yN[i]     = ComplexityBench.toMillis(ComplexityBench.timeLinearSum(n));
            yNLogN[i] = ComplexityBench.toMillis(ComplexityBench.timeSort(n));

            System.out.printf("n=%d | O(log n)=%.3f ms | O(n)=%.3f ms | O(n log n)=%.3f ms%n",
                    n, yLogN[i], yN[i], yNLogN[i]);
        }

        Path graphicsDir = Paths.get("src/com/bigdata/complexity/bench/plots");
        Files.createDirectories(graphicsDir);

        showAndSaveChart("O(log n) — binarySearch", xs, yLogN, device,
                graphicsDir.resolve("logn_mac_m3.png").toString());

        showAndSaveChart("O(n) — linearSum", xs, yN, device,
                graphicsDir.resolve("linear_mac_m3.png").toString());

        showAndSaveChart("O(n log n) — sort", xs, yNLogN, device,
                graphicsDir.resolve("nlogn_mac_m3.png").toString());
    }

    private static void showAndSaveChart(String title, double[] x, double[] y,
                                         String device, String filePath) throws IOException {
        XYChart chart = new XYChartBuilder()
                .width(800).height(600)
                .title(title + " — " + device)
                .xAxisTitle("n")
                .yAxisTitle("Tiempo (ms)")
                .build();

        XYSeries s = chart.addSeries("tiempo", x, y);
        s.setMarker(SeriesMarkers.CIRCLE);

        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setXAxisDecimalPattern("####");
        chart.getStyler().setYAxisDecimalPattern("###.##");

        new SwingWrapper<>(chart).displayChart();
        BitmapEncoder.saveBitmap(chart, filePath, BitmapEncoder.BitmapFormat.PNG);
        System.out.println("Guardado en: " + filePath);
    }
}