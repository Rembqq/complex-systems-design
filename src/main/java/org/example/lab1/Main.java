package org.example.lab1;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        String inputFilename = "input_generated.txt";
        Path inputPath = Paths.get(inputFilename);
        int threads = 4;
        String outPrefix = "output";

        //Data.generateInputFile("input_generated.txt", 200);
        if (!Files.exists(inputPath)) {
            System.out.println("No input file provided — generating '" + inputFilename + "'");
            Data.generateInputFile(inputFilename, Data.DEFAULT_GENERATION_N);
        }

        System.out.println("Reading input from: " + inputFilename);
        Data.Bundle data = Data.readFile(inputFilename);
        int n = data.n;
        System.out.println("n=" + n + ", threads=" + threads);

        FirstInstance fi = new FirstInstance(data, threads);
        long t0fi = System.nanoTime();
        List<FirstInstance.SegmentResult> segs = fi.runOnce();
        long t1fi = System.nanoTime();
        double elapsed1 = (t1fi - t0fi) / 1_000_000.0;
        System.out.printf("FirstVariant: %.3f ms%n", elapsed1);

        // aggregate and write outputs
        double[] O_full = new double[n];
        double[][] MG_full = new double[n][n];
        for (FirstInstance.SegmentResult s : segs) {
            System.arraycopy(s.Osegment, 0, O_full, s.startRow, s.Osegment.length);
            for (int i = 0; i < s.MGsegment.length; i++) {
                System.arraycopy(s.MGsegment[i], 0, MG_full[s.startRow + i], 0, n);
            }
        }
        Data.writeVector(outPrefix + "_first_O.txt", O_full);
        Data.writeMatrix(outPrefix + "_first_MG.txt", MG_full);

        SecondInstance si = new SecondInstance(data, threads);
        long t0si = System.nanoTime();
        si.runOnce();
        long t1si = System.nanoTime();
        double elapsed2 = (t1si - t0si) / 1_000_000.0;
        System.out.printf("SecondVariant: %.3f ms%n", elapsed2);

        Data.writeVector(outPrefix + "_second_O.txt", si.O_shared);
        Data.writeMatrix(outPrefix + "_second_MG.txt", si.MG_shared);
    }
}