package sample.model;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import sample.model.rules.Rule;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Giffer {  // TODO (Add more options to giffer)
    // Array to store the images
    private final ArrayList<BufferedImage> images;

    public Giffer() {
        images = new ArrayList<>();
    }

    public void addGrid(Coordinate startCoordinate, Coordinate endCoordinate,
                            Grid pattern, Rule rule) {
        int width = endCoordinate.getX() - startCoordinate.getX();
        int height = endCoordinate.getY() - startCoordinate.getY();

        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();
        for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
            for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
                try {
                    pixelWriter.setColor(x - startCoordinate.getX(), y - startCoordinate.getY(),
                            rule.getColor(pattern.getCell(x, y)));
                }
                catch (IndexOutOfBoundsException ignored) {}  // Ignore cells that are outside of the bounds
            }
        }

        images.add(SwingFXUtils.fromFXImage(image, null));
    }

    public boolean toGIF(File file) {  // Writes to the provided file, returns true if successful
        try {
            // Get the first image
            BufferedImage firstImage = images.get(0);

            // create a new BufferedOutputStream with the last argument
            ImageOutputStream output = new FileImageOutputStream(file);

            // Create a gif sequence with the type of the first image which loop continuously
            GifSequenceWriter writer = new GifSequenceWriter(output, firstImage.getType(),
                    50, true);

            // Write the first image to the sequence
            writer.writeToSequence(firstImage);
            for(int i = 1; i < images.size(); i++) {
                writer.writeToSequence(images.get(i));
            }

            // Close the writer and output and save the gif
            writer.close();
            output.close();
            return true;
        }
        catch (IOException exception) {
            System.out.print("FASFFS");
            return false;
        }
    }
}
