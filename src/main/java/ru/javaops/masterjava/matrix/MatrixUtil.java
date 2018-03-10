package ru.javaops.masterjava.matrix;

import java.util.Random;
import java.util.concurrent.*;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply2(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        final CompletionService<int[]> completionService = new ExecutorCompletionService<int[]>(executor);

        int[][] matrixBT = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrixBT[i][j] = matrixB[j][i];
            }
        }

        CountDownLatch doneSignal = new CountDownLatch(matrixSize);

        for (int i = 0; i < matrixSize; i++) {
            Future<int[]> line = executor.submit(new ComputeTheLine2(doneSignal, matrixA[i], matrixBT, matrixSize));
            matrixC[i] = line.get();
        }
        doneSignal.await(60, TimeUnit.SECONDS);

        return matrixC;
    }

    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int[][] matrixBT = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrixBT[i][j] = matrixB[j][i];
            }
        }

        CountDownLatch doneSignal = new CountDownLatch(matrixSize);
        for (int i = 0; i < matrixSize; i++) {
            executor.submit(new ComputeTheLine(doneSignal, i, matrixA, matrixBT, matrixC, matrixSize));
        }
        doneSignal.await(60, TimeUnit.SECONDS);

        return matrixC;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int[][] matrixBT = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrixBT[i][j] = matrixB[j][i];
            }
        }
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * matrixBT[j][k];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }


    private static class ComputeTheLine implements Runnable {

        private final CountDownLatch doneSignal;
        private final int i;
        private final int[][] matrixA;
        private final int[][] matrixBT;
        private final int[][] matrixC;
        private final int matrixSize;

        public ComputeTheLine(CountDownLatch doneSignal, int i, int[][] matrixA, int[][] matrixBT, int[][] matrixC, int matrixSize) {
            this.doneSignal = doneSignal;
            this.i = i;
            this.matrixA = matrixA;
            this.matrixBT = matrixBT;
            this.matrixC = matrixC;
            this.matrixSize = matrixSize;
        }

        @Override
        public void run() {

            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * matrixBT[j][k];
                }
                matrixC[i][j] = sum;
            }
            doneSignal.countDown();
        }
    }

    private static class ComputeTheLine2 implements Callable<int[]> {

        private final CountDownLatch doneSignal;
        private final int[] lineA;
        private final int[][] matrixBT;
        private final int matrixSize;

        public ComputeTheLine2(CountDownLatch doneSignal, int[] lineA, int[][] matrixBT, int matrixSize) {
            this.doneSignal = doneSignal;
            this.lineA = lineA;
            this.matrixBT = matrixBT;
            this.matrixSize = matrixSize;
        }

        @Override
        public int[] call() throws Exception {
            int[] lineC = new int[matrixSize];
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += lineA[k] * matrixBT[j][k];
                }
                lineC[j] = sum;
            }
            doneSignal.countDown();
            return lineC;
        }
    }
}
