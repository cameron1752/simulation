package org.example.neuralnet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WeatherPredicterCoordinator {

    static List<TrainingExample> testData = new ArrayList<>();
    static List<TrainingExample> trainingData = new ArrayList<>();
    private static final boolean initData = true;
    private static final boolean refreshScaler = true;
    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        int[] sizes = {150, 128, 64, 32, 24};

        // init the weights for the instances
        NetworkTwo initNetwork = new NetworkTwo(sizes);
        initNetwork.write("coordinator", 0);

        // if we need to load data from files
        if (initData){
            loadData();
        }

        // scaler
        Scaler scaler = new Scaler();

        int round = 1;

        // prime the scaler && write to database
        primeScaler(scaler);

        boolean readyForAverage = false;

        while (true){
            if (readyForAverage){
                DBManager.average(round);
                round++;
            } else {
                Thread.sleep(60000);
                readyForAverage = DBManager.readyForAverage(5, round);
            }
        }
    }

    private static void loadData() throws SQLException, IOException {
//        String dataPath1 = "/app/grib/data1.grib";
        String dataPath2 = "/app/grib/data2.grib";

//        DataLoader.loadGrib(dataPath1);
        DataLoader.loadGrib(dataPath2);

        System.out.println(DBManager.cutoverFromStaging(true) + " records cut");
    }

    private static void primeScaler(Scaler scaler) throws SQLException, IOException {
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
