package org.example;

import com.hazelcast.collection.IQueue;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Random;
import java.util.stream.IntStream;

public class HazelcastMatrixMultiplication {

    private double[][] initializeMatrix(int n){
        Random random = new Random();
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = random.nextDouble();
            }
        }
        return matrix;
    }

    private double multiplyRowColumn(double[] row, double[] col){
        double mult = 0;
        for (int k = 0; k < row.length; k++) {
            mult += row[k] * col[k];
        }
        return mult;
    }

    private void printMatrix(double[][] matrix) {
        int n = matrix.length; // Número de filas (asumiendo que es cuadrada)

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.printf("%.2f ", matrix[i][j]); // Imprimir con dos decimales
            }
            System.out.println(); // Salto de línea después de cada fila
        }
    }
    private void divideAndSendTasks(double[][] a, double[][] b, IQueue<RowMultiplicationTask> queue) throws InterruptedException {
        int n = a.length; // Asumiendo que a y b son matrices cuadradas de tamaño n x n

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // Extraer la fila i de la matriz A
                double[] rowA = a[i];

                // Extraer la columna j de la matriz B
                double[] colB = new double[n];
                for (int k = 0; k < n; k++) {
                    colB[k] = b[k][j];
                }

                // Crear una tarea para multiplicar la fila i por la columna j
                RowMultiplicationTask task = new RowMultiplicationTask(i, j, rowA, colB);

                // Enviar la tarea a la cola
                queue.put(task);
            }
        }
    }
    public void execute(int mode) throws InterruptedException {

        Config config = new Config();
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        IQueue<RowMultiplicationTask> job = hazelcastInstance.getQueue("jobs_queue");
        IQueue<RowMultiplicationTask> result = hazelcastInstance.getQueue("results_queue");

        if (mode==0) {
            System.out.println("Soy Manager");
            int n = 4;
            double[][] a = initializeMatrix(n);
            double[][] b = initializeMatrix(n);
            double[][] c = new double[n][n];


            divideAndSendTasks(a, b, job);

            while(true){
                RowMultiplicationTask task = job.poll();
                if (task!=null){
                    System.out.println("Multiplying [" + task.getRow() + "][" + task.getCol() + "]");
                    c[task.getRow()][task.getCol()] = multiplyRowColumn(task.getRowData(), task.getColData());
                } else{ break; }
            }

            while(true){
                RowMultiplicationTask task = result.poll();
                if (task!=null){
                    c[task.getRow()][task.getCol()] = task.getResult();
                } else{ break; }
            }
            System.out.println("Done");

        } else {
            System.out.println("I'm worker");
            while(true) {
                RowMultiplicationTask task = job.poll();
                if (task != null) {
                    System.out.println("Multiplying [" + task.getRow() + "][" + task.getCol() + "]");
                    result.put(task.setResult(multiplyRowColumn(task.getRowData(), task.getColData())));
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new HazelcastMatrixMultiplication().execute(Integer.parseInt(args[0]));
    }
}