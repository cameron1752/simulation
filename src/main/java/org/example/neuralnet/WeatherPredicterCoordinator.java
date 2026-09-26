package org.example.neuralnet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WeatherPredicterCoordinator {

    static List<TrainingExample> testData = new ArrayList<>();
    static List<TrainingExample> trainingData = new ArrayList<>();
    private static final boolean initData = false;
    private static final boolean refreshScaler = false;
    private static final String COORDINATOR = "coordinator";
    // scaler
    private static final Scaler scaler = new Scaler();
    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        // network layer sizes
        int[] sizes = {150, 128, 64, 32, 24};

        // init the weights for the instances
        NetworkTwo initNetwork = new NetworkTwo(sizes);

        // write to database
        initNetwork.write(COORDINATOR, 0);

        // if we need to load data from files
        loadData();

        int round = 1;

        // prime the scaler && write to database
        primeScaler();

        boolean readyForAverage = false;

        // while running
        while (true){
            // if worker nodes have reported back for the current round
            if (readyForAverage){
                System.out.println("Averaging for round: " + round);
                // average the values & re-insert
                DBManager.average(round);
                readyForAverage = false;
                // iterate round
                round++;
            } else {
                System.out.println("Waiting on instances for round: " + round);
                // else wait for a minute
                Thread.sleep(60000);
                // set boolean based on instance count
                readyForAverage = DBManager.readyForAverage(4, round);
            }
        }
    }

    private static void loadData() throws SQLException, IOException {
        if (initData){
            // list of files
            List<String> files = new ArrayList<>();
            files.add("/app/grib/data2.grib");
            files.add("/app/grib/data1.grib");

            for (String file : files){
                DataLoader.loadGrib(file);
            }

            System.out.println(DBManager.cutoverFromStaging(true) + " records cut");
        } else {
            System.out.println("Data already present, skipping init");
        }

    }

    private static void primeScaler() throws SQLException, IOException {
        // if we want to refresh the scaler on training data
        if (refreshScaler){
            System.out.println("Refreshing scaler with test data");
            scaler.reset();
            DataLoader.reset();
            getData();
            while (!trainingData.isEmpty()) {
                scaler.partialFit(trainingData);
                getData();
            }
            scaler.finish();
            // re-save the scaler data
            scaler.write();
            // else just import from the old scaler path
        } else {
            System.out.println("Using historical scaler data");
        }
    }

    private static void getData() throws IOException, SQLException {
        long start = System.currentTimeMillis();
        Object[] result = DataLoader.readFromDB();

        trainingData = (List<TrainingExample>) result[0];
        testData = (List<TrainingExample>) result[1];

        System.out.println(System.currentTimeMillis() - start + "ms");
    }
}
