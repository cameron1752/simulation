package org.example.neuralnet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class DigitCanvas extends JPanel {
    private BufferedImage canvas;
    private Network network;

    public DigitCanvas(Network network) {
        this.network = network;
        canvas = new BufferedImage(280, 280, BufferedImage.TYPE_BYTE_GRAY);
        clearCanvas();

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Graphics2D g = canvas.createGraphics();
                g.setColor(Color.WHITE);
                g.fillOval(e.getX() - 10, e.getY() - 10, 20, 20);
                g.dispose();
                repaint();
                classify();
            }
        });
    }

    private void clearCanvas() {
        Graphics2D g = canvas.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 280, 280);
        g.dispose();
    }

    private void classify() {
        // scale 280x280 canvas down to 28x28 and flatten to float[784]
        BufferedImage scaled = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(canvas, 0, 0, 28, 28, null);
        g.dispose();

        float[] pixels = new float[784];
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int rgb = scaled.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // red channel of grayscale image
                pixels[y * 28 + x] = gray / 255.0f;
            }
        }

        float[] output = network.feedForward(pixels);
        int prediction = network.argMax(output);
        System.out.println("Prediction: " + (prediction) + " (confidence: " + String.format("%.1f%%", output[prediction] * 100) + ")");
        System.out.println(Arrays.toString(output));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvas, 0, 0, null);
    }

    public static void launch(Network network) {
        JFrame frame = new JFrame("Digit Classifier");
        DigitCanvas panel = new DigitCanvas(network);
        panel.setPreferredSize(new Dimension(280, 280));

        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> { panel.clearCanvas(); panel.repaint(); });

        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.add(clear, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
