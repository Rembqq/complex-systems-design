package org.example.lab1;


import java.util.Arrays;

public class SecondInstance {
    final Data.Bundle data;
    final int threads;

    final double[][] MM_shared;
    final double[][] ME_shared;
    final double[][] MT_shared;
    final double[][] MZ_shared;
    final double[] D_shared;
    final double[] E_shared;

    // Shared results
    final double[] O_shared;
    final double[][] MG_shared;

    // Locks
    final Object oWriteLock = new Object();  // для запису у O_shared
    final Object mgWriteLock = new Object(); // для запису у MG_shared
    final Object minUpdateLock = new Object();

    public SecondInstance(Data.Bundle data, int threads) {
        this.data = data;
        this.threads = threads;
        this.MM_shared = data.MM;
        this.ME_shared = data.ME;
        this.MT_shared = data.MT;
        this.MZ_shared = data.MZ;
        this.D_shared = data.D;
        this.E_shared = data.E;
        int n = data.n;
        this.O_shared = new double[n];
        this.MG_shared = new double[n][n];
    }

    void runOnce() throws Exception {
        int n = Data.N;
        int P = Data.THREADS;
        int base = n / P;

        int rem = n % P;

        Thread[] threadsArr = new Thread[P];

        // Shared scalar that threads will frequently update (to create coherence traffic)
        final double[] sharedMinHolder = new double[] { Double.POSITIVE_INFINITY };

        for(int t = 0, rowStart = 0; t < P; t++) {
            final int start = rowStart;
            final int size = base + (t < rem ? 1 : 0);
            rowStart += size;

            Thread worker = new Thread(() -> {
                // Work directly on shared arrays (reads and writes)
                int nLocal = n;

                // 1) Compute T_rows for assigned rows (using shared MM and ME)
                // but write results directly into a shared temporary Tlocal for rows (synchronized when write)

                double[][] localTrows = new double[size][nLocal];

                for(int i = 0; i < size; ++i) {
                    int globalRow = start + i;
                    for(int j = 0; j < nLocal; ++j) {
                        double sum = 0.0;
                        double c = 0.0;
                        for (int k = 0; k < nLocal; ++k) {
                            double y = MM_shared[globalRow][k] * ME_shared[k][j] - c;
                            double tval = sum + y;
                            c = (tval - sum) - y;
                            sum = tval;
                        }
                        localTrows[i][j] = sum;
                    }
                    synchronized (mgWriteLock) {
                        System.arraycopy(localTrows[i], 0, MG_shared[globalRow], 0, nLocal);
                    }
                }

                // sort rows mg
                for(int i = 0; i < size; ++i) {
                    int globalRow = start + i;
                    synchronized (mgWriteLock) {
                        Arrays.sort(MG_shared[globalRow]);
                    }
                }

                // sort D
                double[] sortedDglobal = new double[nLocal];
                synchronized (minUpdateLock) {
                    System.arraycopy(D_shared, 0, sortedDglobal, 0, nLocal);
                    Arrays.sort(sortedDglobal);
                }

                //
                for(int i = 0; i < size; ++i) {
                    int globalRow = start + i;
                    double value = Data.dotKahan(MG_shared[globalRow], sortedDglobal);
                }
                synchronized (oWriteLock) {
                    O_shared[globalRow] = value;
                }
            });
        }
    }
}
