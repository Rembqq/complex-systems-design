package org.example.lab1;


import java.util.Arrays;

public class SecondInstance {
    final Data.Bundle data;
    final int threads;
    final int n;

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
        this.n = data.n;
        this.O_shared = new double[n];
        this.MG_shared = new double[n][n];
    }

    void runOnce() throws Exception {
        //int n = data.n;
        int base = n / threads;
        int rem = n % threads;

        Thread[] threadsArr = new Thread[threads];

        // Shared scalar that threads will frequently update (to create coherence traffic)
        final double[] sharedMinHolder = new double[] { Double.POSITIVE_INFINITY };

        for(int t = 0, rowStart = 0; t < threads; t++) {
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

                // compute O for assigned rows, writing into O_shared in synchronized fashion
                for(int i = 0; i < size; ++i) {
                    int globalRow = start + i;
                    double value = Data.dotKahan(MG_shared[globalRow], sortedDglobal);
                    synchronized (oWriteLock) {
                        O_shared[globalRow] = value;
                    }
                }

                // min(D+E)
                for (int i = 0; i < nLocal; i++) {
                    double sumDE = D_shared[i] + E_shared[i];
                    // update sharedMinHolder under lock frequently (intentionally)
                    synchronized (minUpdateLock) {
                        if (sumDE < sharedMinHolder[0]) sharedMinHolder[0] = sumDE;
                    }
                }

                // min * MM * MT
                for (int i = 0; i < size; i++) {
                    int globalRow = start + i;
                    for (int j = 0; j < nLocal; j++) {
                        double sum = 0.0, c = 0.0;
                        for (int k = 0; k < nLocal; k++) {
                            double prod = MM_shared[globalRow][k] * MT_shared[k][j];
                            double y = prod - c;
                            double tval = sum + y;
                            c = (tval - sum) - y;
                            sum = tval;
                        }
                        double val = sum * sharedMinHolder[0];
                        // write into MG_shared (subtract later)
                        synchronized (mgWriteLock) {
                            MG_shared[globalRow][j] = val;
                        }
                    }
                }

                // MZ * ME
                for (int i = 0; i < size; i++) {
                    int globalRow = start + i;
                    for (int j = 0; j < nLocal; j++) {
                        double sum = 0.0, c = 0.0;
                        for (int k = 0; k < nLocal; k++) {
                            double prod = MZ_shared[globalRow][k] * ME_shared[k][j];
                            double y = prod - c;
                            double tval = sum + y;
                            c = (tval - sum) - y;
                            sum = tval;
                        }
                        synchronized (mgWriteLock) {
                            MG_shared[globalRow][j] = MG_shared[globalRow][j] - sum;
                        }
                    }
                }

            });
            worker.setName("SharedWorker-" + t);
            threadsArr[t] = worker;
        }
        // start
        for (Thread th : threadsArr) th.start();
        // join
        for (Thread th : threadsArr) th.join();
    }
}
