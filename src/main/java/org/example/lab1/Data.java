package org.example.lab1;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class Data {

    static int DEFAULT_GENERATION_N = 200;
    //static int THREADS = 4;

    public static class Bundle {
        int n;
        double[][] MM, ME, MT, MZ;
        double[] D, E;
    }

    // Read
    static Bundle readFile(String path) {
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            Bundle b = new Bundle();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if(!line.isEmpty()) {
                    b.n = Integer.parseInt(line);
                    break;
                }
            }

            if(line == null) throw new IOException("Missing n");
            int n = b.n;
            b.MM = new double[n][n];
            b.ME = new double[n][n];
            b.MT = new double[n][n];
            b.MZ = new double[n][n];
            b.D = new double[n];
            b.E = new double[n];

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if(line.isEmpty()) continue;
                if(line.equalsIgnoreCase("MM")) readMatrix(br, b.MM);
                if(line.equalsIgnoreCase("ME")) readMatrix(br, b.ME);
                if(line.equalsIgnoreCase("MT")) readMatrix(br, b.MT);
                if(line.equalsIgnoreCase("MZ")) readMatrix(br, b.MZ);
                if(line.equalsIgnoreCase("D")) readVector(br, b.D);
                if(line.equalsIgnoreCase("E")) readVector(br, b.E);
            }
            return b;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void readMatrix(BufferedReader br, double[][] M) throws IOException {
        int n = M.length;

        for(int i = 0; i < n; ++i) {
            String row = br.readLine();
            while(row != null && row.trim().isEmpty()) row = br.readLine();
            if(row == null) throw new IOException("Unexpected EOF while reading matrix");
            String[] toks = row.trim().split("\\s+");
            if(toks.length != n) throw new IOException("Matrix row length mismatch");
            for (int j = 0; j < n; j++) M[i][j] = Double.parseDouble(toks[j]);
        }
    }

    static void readVector(BufferedReader br, double[] V) throws IOException {

        String line = br.readLine();
        while(line != null && line.trim().isEmpty()) line = br.readLine();
        if(line == null) throw new IOException("Unexpected EOF while reading matrix");
        String[] toks = line.trim().split("\\s+");
        if(toks.length != V.length) throw new IOException("Matrix row length mismatch");
        for (int i = 0; i < V.length; i++) V[i] = Double.parseDouble(toks[i]);
    }

    // Write

    static void writeMatrix(String path, double[][] M) throws IOException {
        try(PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            for (int i = 0; i < M.length; ++i) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < M[0].length; ++j) {
                    if (j > 0) sb.append(" ");
                    sb.append(M[i][j]);
                }
                pw.println(sb);
            }
        }
    }

    static void writeVector(String path, double[] V) throws IOException {
        try(PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < V.length; ++i) {
                if (i > 0) sb.append(" ");
                sb.append(V[i]);
            }
            pw.println(sb);
        }
    }

    // Calculating

    // muls
    public static double[] multiplyVecMatKahan(double[] vec, double[][] mat) {
        int n = mat.length;
        double[] y = new double[n];
        for(int i = 0; i < n; ++i) {
            y[i] = dotKahan(mat[i], vec);
        }
        return y;
    }

    public static double[][] multiplyMatricesKahan(double[][] mat1, double[][] mat2) {
        double[][] resultM = new double[mat1.length][mat2[0].length];
        double sum;
        double c;
        for(int i = 0; i < mat1.length; ++i) {
            for(int j = 0; j < mat1[0].length; ++j) {
                sum = 0.0;
                c = 0.0;
                for (int k = 0; k < mat1[0].length; ++k) {
                    double y = mat1[i][k] * mat2[k][j] - c;
                    double t = sum + y;
                    c = (t - sum) - y;
                    sum = t;
                }
                resultM[i][j] = sum;
            }
        }
        return resultM;
    }

    static double dotKahan(double[] a, double[] b) {
        double sum = 0.0;
        double c = 0.0;
        for (int i = 0; i < a.length; i++) {
            double y = a[i] * b[i] - c;
            double t = sum + y;
            c = (t - sum) - y;
            sum = t;
        }
        return sum;
    }

    public static void multiplyScalMat(double scal, double[][] mat) {
        for(int i = 0; i < mat.length; ++i) {
            for(int j = 0; j < mat[0].length; ++j) {
                mat[i][j] *= scal;
            }
        }
    }

    // funcs
    public static double[] sortVector(double[] vec) {
        double[] vec1 = vec.clone();
        Arrays.sort(vec1);
        return vec1;
    }

    // sort the whole matrix, not just each distinct row
//    public static void sortMatrix(double[][] M) {
//        int n = M.length;
//        int m = M[0].length;
//
//        double[] all = new double[n * m];
//        int idx = 0;
//        for (double[] row : M) {
//            for (double val : row) {
//                all[idx++] = val;
//            }
//        }
//
//        Arrays.sort(all);
//
//        idx = 0;
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < m; j++) {
//                M[i][j] = all[idx++];
//            }
//        }
//    }

    static void sortRowsInPlace(double[][] M) {
        for (double[] row : M) Arrays.sort(row);
    }


    public static double minVector(double[] v) {
        double min = v[0];
        for (int i = 1; i < v.length; i++) if (v[i] < min) min = v[i];
        return min;
    }

    // adds subs
    public static double[] addVector(double[] vec1, double[] vec2) {
        double[] rVec = new double[vec1.length];
        for(int i = 0; i < vec1.length; ++i) {
            rVec[i] = vec1[i] + vec2[i];
        }
        return rVec;
    }

    public static double[][] subtractMatrices(double[][] mat1, double[][] mat2) {
        double[][] rMat = new double[mat1.length][mat1[0].length];
        for(int i = 0; i < mat1.length; ++i) {
            for(int j = 0; j < mat1[0].length; ++j) {
                rMat[i][j] = mat1[i][j] - mat2[i][j];
            }
        }
        return rMat;
    }

    // copy

    static double[][] deepCopyMatrix(double[][] A) {
        int n = A.length, m = A[0].length;
        double[][] B = new double[n][m];
        for (int i = 0; i < n; i++) System.arraycopy(A[i], 0, B[i], 0, m);
        return B;
    }

    static double[] deepCopyVector(double[] v) {
        return Arrays.copyOf(v, v.length);
    }

    // --- CSV logging ---
//    static void appendCSV() {
//
//    }


    // --- Input generator (varied orders of magnitude) ---
    static void generateInputFile(String filename, int n) throws IOException {

        Random r = new Random(12345);

        try(PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println(n);
            String[] matLabels = {"MM", "ME", "MT", "MZ"};
            String[] vecLabels = {"D", "E"};
            for(String label : matLabels) {
                pw.println(label);
                for(int i = 0; i < n; ++i) {
                    StringBuilder sb = new StringBuilder();
                    for(int j = 0; j < n; ++j) {
                        double elem = generateVaried(r);
                        if (j > 0) sb.append(' ');
                        sb.append(elem);
                    }
                    pw.println(sb);
                }
                pw.println();
            }

            for(String label : vecLabels) {
                pw.println(label);
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < n; ++i) {
                    if (i > 0) sb.append(' ');
                    sb.append(generateVaried(r));
                }
                pw.println(sb);
                pw.println();
            }
        }
    }

    static double generateVaried(Random r) {
        double m = 1 + r.nextDouble() * 9;
        int exp = r.nextInt(21) - 10;
        return m * Math.pow(10.0, exp);
    }

}
