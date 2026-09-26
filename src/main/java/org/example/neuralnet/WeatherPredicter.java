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

        String instance_id = String.valueOf(UUID.randomUUID());
        int start = Integer.parseInt(System.getenv("START_INDEX"));;
        int offset = 4;

        System.out.println("Instance with UUID: [" + instance_id + "] has start index of " + start);

        System.out.println("Sleeping for 2.5 minutes");
        Thread.sleep(150000);

        int[] sizes = {150, 128, 64, 32, 24};

        NetworkTwo net = new NetworkTwo(sizes);

        // until we're able to read try and read weights
        net.read(0);

        Scaler scaler = new Scaler();

        // until we're able to read try and read
        scaler.read();

        int round = 1;
        int syncEvery = 5 * offset; // every 5 of MY pages = every 20 global pages

        for (int epoch = 0; epoch < 30; epoch++) {
            System.out.println("Starting pass: " + epoch);
            DataLoader.reset();
            getData();

            while (!trainingData.isEmpty()) {
                int page = DataLoader.count - 1;

                if (page % offset == start) {
                    System.out.println("Instance [" + instance_id + "] start " + start + " processing page: " + page);
                    scaler.transform(trainingData);
                    scaler.transform(testData);
                    Collections.shuffle(trainingData);
                    net.SDG(trainingData, epoch, page, 64, .01f, testData, scaler);
                }

                if ((page + 1) % syncEvery == 0) {
                    net.write(instance_id, round);
                    net.read(round);
                    round++;
                }

                getData();
            }
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
