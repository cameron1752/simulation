package org.example.neuralnet;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;

public class WeatherPredicter {
    static List<TrainingExample> testData = new ArrayList<>();
    static List<TrainingExample> trainingData = new ArrayList<>();
    private static final boolean useStoredData = false;
    private static final String path = "weather.csv";
    private static final String savePath = "weather_weights.csv";
    private static final String scalerPath = "weather_scaler.csv";

//    mvn package
//    docker compose build
//    docker compose up -d db server
//    docker compose --profile training up coordinator worker-0 worker-1 worker-2 worker-3

    public static void main(String[] args) throws IOException, URISyntaxException, SQLException, InterruptedException {

        int[] sizes = {150, 128, 64, 32, 24};
        String instance_id = String.valueOf(UUID.randomUUID());

        NetworkTwo net = new NetworkTwo(sizes);

        // until we're able to read try and read weights
        net.read(0);

        Scaler scaler = new Scaler();

        // until we're able to read try and read
        scaler.read();
        int round = 1;
        // training passes
        for (int epoch = 0; epoch < 30; epoch++) {
            System.out.println("Starting pass: " + epoch);
            // refresh loader
            DataLoader.reset();

            // get first slice of data
            getData();

            // until we're through with the table
            while (!trainingData.isEmpty()) {
                int page = DataLoader.count - 1;
                scaler.transform(trainingData);
                scaler.transform(testData);
                Collections.shuffle(trainingData);
                net.SDG(trainingData, epoch, page, 64, .01f, testData, scaler);
                getData();

                // determine how many times / when to refresh weights
                if (DataLoader.count % 5 == 0){
                    net.write(instance_id, round);
                    net.read(round);
                    round++;
                }
            }

            // save the weights / biases after each pass
            net.save(savePath);
        }

//        DigitCanvas.launch(net);
    }


    private static void getData() throws IOException, SQLException {
        long start = System.currentTimeMillis();
        Object[] result = DataLoader.readFromDB();

        trainingData = (List<TrainingExample>) result[0];
        testData = (List<TrainingExample>) result[1];

        System.out.println(System.currentTimeMillis() - start + "ms");
    }




}
