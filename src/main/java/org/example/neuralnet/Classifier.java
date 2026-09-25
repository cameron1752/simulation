package org.example.neuralnet;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class Classifier {
    static List<TrainingExample> testData;
    static List<TrainingExample> trainingData;
    private static final boolean useStoredData = true;
    private static final String path = "training_save.txt";

    public static void main(String[] args) throws IOException, URISyntaxException {

        int[] sizes = {784, 30, 10};

        Network net = new Network(sizes);

        if (!useStoredData){
            getData();
            net.SDG(trainingData, 30, 10, 3.0f, testData);
            net.save(path);
        } else {
            net.load(path);
        }

//        DigitCanvas.launch(net);
    }

    private static void getData() throws IOException {
        String base = "C:/Users/Cammy/IdeaProjects/simulation/src/main/resources/";

        testData = DataLoader.load(
                base + "emnist-digits-test-images-idx3-ubyte/emnist-digits-test-images-idx3-ubyte",
                base + "emnist-digits-test-labels-idx1-ubyte/emnist-digits-test-labels-idx1-ubyte"
        );

        trainingData = DataLoader.load(
                base + "emnist-digits-train-images-idx3-ubyte/emnist-digits-train-images-idx3-ubyte",
                base + "emnist-digits-train-labels-idx1-ubyte/emnist-digits-train-labels-idx1-ubyte"
        );
    }

}
