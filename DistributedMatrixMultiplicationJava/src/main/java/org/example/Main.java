package org.example;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;
import org.example.control.MatrixMultiplierTask;
import org.example.model.BlockKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

public class Main {
    public static final int MATRIX_SIZE = 1024;
    public static final int BLOCK_SIZE = 256;

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        prepareRandomMatrix("matrixA", MATRIX_SIZE, MATRIX_SIZE, BLOCK_SIZE, hz);
        prepareRandomMatrix("matrixB", MATRIX_SIZE, MATRIX_SIZE, BLOCK_SIZE, hz);

        IExecutorService executor = hz.getExecutorService("executor");
        List<Future<Boolean>> futures = new ArrayList<>();

        int numBlocks = MATRIX_SIZE / BLOCK_SIZE;

        System.out.println("Submitting " + (numBlocks * numBlocks) + " multiplication tasks...");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numBlocks; i++) {
            for (int j = 0; j < numBlocks; j++) {
                MatrixMultiplierTask task = new MatrixMultiplierTask(i, j, numBlocks);
                futures.add(executor.submit(task));
            }
        }

        for (Future<Boolean> future : futures) {
            future.get();
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Multiplication completed!");
        System.out.println("Total execution time: " + (endTime - startTime) + " ms");

        hz.shutdown();
    }

    private static void prepareRandomMatrix(String mapName, int rows, int cols, int blockSize, HazelcastInstance hz) {
        IMap<BlockKey, double[][]> map = hz.getMap(mapName);
        Random rand = new Random();
        int numRowBlocks = (int) Math.ceil((double) rows / blockSize);
        int numColBlocks = (int) Math.ceil((double) cols / blockSize);

        for (int i = 0; i < numRowBlocks; i++) {
            for (int j = 0; j < numColBlocks; j++) {
                double[][] block = new double[blockSize][blockSize];
                for (int r = 0; r < blockSize; r++) {
                    for (int c = 0; c < blockSize; c++) {
                        block[r][c] = rand.nextDouble();
                    }
                }
                map.put(new BlockKey(i, j), block);
            }
        }
        System.out.println("Matrix '" + mapName + "' generated and distributed. Dimensions: " + rows + "x" + cols);
    }
}