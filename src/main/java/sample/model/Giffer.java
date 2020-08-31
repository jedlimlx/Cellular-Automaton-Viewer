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

public class Giffer {  // TODO (Progress bar or something)
    private double progress;

    // Array to store the images
    private final ArrayList<BufferedImage> images;
    private final int CELL_SIZE, TIME_BETWEEN_FRAMES;

    public Giffer(int CELL_SIZE, int TIME_BETWEEN_FRAMES) {
        images = new ArrayList<>();
        this.CELL_SIZE = CELL_SIZE;
        this.TIME_BETWEEN_FRAMES = TIME_BETWEEN_FRAMES;
    }

    public void addGrid(Coordinate startCoordinate, Coordinate endCoordinate,
                            Grid pattern, Rule rule) {
        int width = endCoordinate.getX() - startCoordinate.getX();
        int height = endCoordinate.getY() - startCoordinate.getY();

        WritableImage image = new WritableImage(width * CELL_SIZE, height * CELL_SIZE);
        PixelWriter pixelWriter = image.getPixelWriter();

        for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
            for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
                try {
                    // Each cell is now CELL_SIZE * CELL_SIZE
                    for (int imgX = 0; imgX < CELL_SIZE; imgX++) {
                        for (int imgY = 0; imgY < CELL_SIZE; imgY++) {
                            pixelWriter.setColor((x - startCoordinate.getX()) * CELL_SIZE + imgX,
                                    (y - startCoordinate.getY()) * CELL_SIZE + imgY,
                                    rule.getColour(pattern.getCell(x, y)));
                        }
                    }
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
                    TIME_BETWEEN_FRAMES, true);

            // Write the first image to the sequence
            writer.writeToSequence(firstImage);
            for(int i = 1; i < images.size(); i++) {
                writer.writeToSequence(images.get(i));
                progress = (double) i / images.size();
            }

            // Close the writer and output and save the gif
            writer.close();
            output.close();
            return true;
        }
        catch (IOException exception) {
            return false;
        }
    }

    public double getProgress() {
        return progress;
    }
}
