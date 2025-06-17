package org.example.control;

import com.hazelcast.map.IMap;
import org.example.model.BlockKey;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MatrixMarketReader {

    private int rows;
    private int cols;

    public void readAndDistribute(String filePath, int blockSize, IMap<BlockKey, double[][]> targetMap) throws IOException {
        List<String> dataLines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("%")) {
                    continue;
                }
                String[] dimensions = line.trim().split("\\s+");
                this.rows = Integer.parseInt(dimensions[0]);
                this.cols = Integer.parseInt(dimensions[1]);
                while ((line = br.readLine()) != null) {
                    dataLines.add(line);
                }
                break;
            }
        }

        if (rows == 0 || cols == 0) {
            throw new IOException("Matrix dimensions not found in " + filePath);
        }

        double[][] fullMatrix = new double[rows][cols];
        for (String line : dataLines) {
            String[] entry = line.trim().split("\\s+");
            int r = Integer.parseInt(entry[0]) - 1;
            int c = Integer.parseInt(entry[1]) - 1;
            double val = Double.parseDouble(entry[2]);
            if (r < rows && c < cols) {
                fullMatrix[r][c] = val;
            }
        }

        int numRowBlocks = (int) Math.ceil((double) rows / blockSize);
        int numColBlocks = (int) Math.ceil((double) cols / blockSize);

        for (int i = 0; i < numRowBlocks; i++) {
            for (int j = 0; j < numColBlocks; j++) {
                int startRow = i * blockSize;
                int startCol = j * blockSize;
                int endRow = Math.min(startRow + blockSize, rows);
                int endCol = Math.min(startCol + blockSize, cols);
                int blockRows = endRow - startRow;
                int blockCols = endCol - startCol;

                double[][] block = new double[blockRows][blockCols];
                for (int r = 0; r < blockRows; r++) {
                    System.arraycopy(fullMatrix[startRow + r], startCol, block[r], 0, blockCols);
                }
                targetMap.put(new BlockKey(i, j), block);
            }
        }
        System.out.println("Matrix " + filePath + " loaded. Dimensions: " + rows + "x" + cols + ". Distributed into " + numRowBlocks * numColBlocks + " blocks.");
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}
