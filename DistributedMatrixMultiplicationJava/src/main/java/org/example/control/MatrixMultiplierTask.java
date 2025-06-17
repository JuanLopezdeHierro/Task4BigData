package org.example.control;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;
import org.example.model.BlockKey;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class MatrixMultiplierTask implements Callable<Boolean>, Serializable, HazelcastInstanceAware {
    private final int blockRowC;
    private final int blockColC;
    private final int numBlocksCommon;
    private transient HazelcastInstance hazelcastInstance;

    public MatrixMultiplierTask(int blockRowC, int blockColC, int numBlocksCommon) {
        this.blockRowC = blockRowC;
        this.blockColC = blockColC;
        this.numBlocksCommon = numBlocksCommon;
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public Boolean call() throws Exception {
        IMap<BlockKey, double[][]> mapA = hazelcastInstance.getMap("matrixA");
        IMap<BlockKey, double[][]> mapB = hazelcastInstance.getMap("matrixB");
        IMap<BlockKey, double[][]> mapC = hazelcastInstance.getMap("matrixC");

        double[][] resultBlock = null;

        for (int k = 0; k < numBlocksCommon; k++) {
            BlockKey keyA = new BlockKey(blockRowC, k);
            BlockKey keyB = new BlockKey(k, blockColC);

            double[][] blockA = mapA.get(keyA);
            double[][] blockB = mapB.get(keyB);

            if (blockA == null || blockB == null) {
                System.err.println("Error: Missing block A(" + blockRowC + "," + k + ") or B(" + k + "," + blockColC + ")");
                return false;
            }

            double[][] product = multiplyBlocks(blockA, blockB);

            if (resultBlock == null) {
                resultBlock = product;
            } else {
                resultBlock = addBlocks(resultBlock, product);
            }
        }

        if (resultBlock != null) {
            mapC.put(new BlockKey(blockRowC, blockColC), resultBlock);
            System.out.println("Computed block C(" + blockRowC + "," + blockColC + ")");
        }
        return true;
    }

    private double[][] multiplyBlocks(double[][] a, double[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int colsB = b[0].length;

        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    private double[][] addBlocks(double[][] a, double[][] b) {
        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] + b[i][j];
            }
        }
        return result;
    }
}