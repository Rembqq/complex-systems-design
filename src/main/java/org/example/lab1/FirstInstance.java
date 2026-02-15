package org.example.lab1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.example.lab1.Data.*;

public class FirstInstance {

    final Data.Bundle data;
    final int threads;

    public FirstInstance(Data.Bundle data, int threads) {
        this.data = data;
        this.threads = threads;
    }

    static class SegmentResult {
        final int startRow;
        final double[] Osegment;
        final double[][] MGsegment;

        public SegmentResult(int startRow, double[] Osegment, double[][] MGsegment) {
            this.startRow = startRow;
            this.Osegment = Osegment;
            this.MGsegment = MGsegment;
        }
    }

    List<SegmentResult> runOnce() throws Exception {
        int n = Data.N;
        int P = Data.THREADS;
        int base = n / P;

        int rem = n % P;

        ExecutorService pool = Executors.newFixedThreadPool(P);
        List<Future<SegmentResult>> futures = new ArrayList<>();

        for(int t = 0, rowStart = 0; t < P; t++) {
            final int start = rowStart;
            final int size = base + (t < rem ? 1 : 0);
            rowStart += size;

            Callable<SegmentResult> task = () -> {
                double[][] MM_loc = deepCopyMatrix(data.MM);
                double[][] ME_loc = deepCopyMatrix(data.ME);
                double[][] MT_loc = deepCopyMatrix(data.MT);
                double[][] MZ_loc = deepCopyMatrix(data.MZ);
                double[] D_loc = deepCopyVector(data.D);
                double[] E_loc = deepCopyVector(data.E);

                double[][] T = multiplyMatricesKahan(MM_loc, ME_loc);

                sortRowsInPlace(T);

                //sortVector(D_loc);

                double[] sortedD = Arrays.copyOf(D_loc, D_loc.length);
                Arrays.sort(sortedD);

                double[] Oseg = new double[size];
                for (int i = 0; i < size; i++) {
                    Oseg[i] = dotKahan(T[start + i], sortedD);
                }

                double[] DplusE = addVector(D_loc, E_loc);
                double minLocal = minVector(DplusE);

                double[][] M1 = multiplyMatricesKahan(MM_loc, MT_loc);
                multiplyScalMat(minLocal, M1);

                double[][] M2 = multiplyMatricesKahan(MZ_loc, ME_loc);
                double[][] MG = subtractMatrices(M1, M2);

                double[][] MGseg = new double[size][n];
                for (int i = 0; i < size; i++) System.arraycopy(MG[start + i], 0, MGseg[i], 0, n);

                return new SegmentResult(start, Oseg, MGseg);
            };
            futures.add(pool.submit(task));
        }

        List<SegmentResult> results = new ArrayList<>();
        for(Future<SegmentResult> f : futures) results.add(f.get());
        pool.shutdown();
        return results;
    }
}
