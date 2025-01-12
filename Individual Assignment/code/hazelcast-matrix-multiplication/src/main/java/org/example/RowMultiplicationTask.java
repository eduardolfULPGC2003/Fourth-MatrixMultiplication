package org.example;

import java.io.Serializable;

public class RowMultiplicationTask implements Serializable {
    private final int row;
    private final int col;
    private final double[] rowData;
    private final double[] colData;
    private double result;

    public RowMultiplicationTask(int row, int col, double[] rowData, double[] colData) {
        this.row = row;
        this.col = col;
        this.rowData = rowData;
        this.colData = colData;
        this.result = 0;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public double[] getRowData() { return rowData; }
    public double[] getColData() { return colData; }
    public RowMultiplicationTask setResult(double result) {
        this.result = result;
        return this;
    }
    public double getResult() { return result; }
}
