package com.heaven7.test;

public class ArrayToArray3 {

    public static void main(String[] args) {
        int m = 3, n = 4, l = 5;
        //int a[m][n][l];
        //int b[m * n * l];
        int [][][] a = new int[m][n][l];
        int [] b = new int[m * n * l];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < l; k++) {
                    a[i][j][k] = i + j + k;
                    b[i * (l * n) + j * l + k % l] = a[i][j][k];
                }
            }
        }
        //再转回三维数组
        //int c[m][n][l];
        int [][][] c = new int[m][n][l];
        for (int i = 0; i < m * n * l; i++) {
            c[i / (n * l)][i % (n * l) / l][i % l] = b[i];
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < l; k++) {
                    System.out.println(String.format("c[%d][%d][%d]=%d", i, j, k, c[i][j][k]));
                }
            }
        }
    }
}

