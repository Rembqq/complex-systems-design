package org.example.lab1;

import java.io.*;

public class Data {

    static final int N = 200;
    static final int THREADS = 4;
    static final int RUNS = 5;

    static class Bundle {
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
                        ...
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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

        PrintWriter pw = new PrintWriter(new FileWriter(path));
        for(int i = 0; i < M.length; ++i) {
            StringBuilder sb = new StringBuilder();
            for(int j = 0; j < M[0].length; ++j) {
                if(j > 0) sb.append(" ");
                sb.append(M[i][j]);
            }
            pw.println(sb);
        }
    }

    static void writeVector(BufferedReader br, double[] V) throws IOException {

        String line = br.readLine();
        while(line != null && line.trim().isEmpty()) line = br.readLine();
        if(line == null) throw new IOException("Unexpected EOF while reading matrix");
        String[] toks = line.trim().split("\\s+");
        if(toks.length != V.length) throw new IOException("Matrix row length mismatch");
        for (int i = 0; i < V.length; i++) V[i] = Double.parseDouble(toks[i]);
    }

    // Calculating

    // muls
    public static double[] multiplyVecMat(double[] vec, double[][] mat) {

    }

    public static double[][] multiplyMatrices(double[][] mat1, double[][] mat2) {
        return ;
    }

    public static double[][] multiplyScalMat(double scal, double[][] mat) {
        return ;
    }

    // funcs
    public static double[] sortVector(double[] vec) {

    }

    public static double[] sortVector(double[] vec) {

    }

    public static double[] sortMatrix(double[][] vec) {

    }

    public static double minVector(double[] vec) {
        return ;
    }

    // adds subs
    public static double[] addVector(double[] vec) {

    }

    public static double[][] subtractMatrices(double[][] mat1, double[][] mat2) {
        return ;
    }
}
