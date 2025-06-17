package org.example.model;

import java.io.Serializable;
import java.util.Objects;

public class BlockKey implements Serializable {
    private final int row;
    private final int col;

    public BlockKey(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockKey blockKey = (BlockKey) o;
        return row == blockKey.row && col == blockKey.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}