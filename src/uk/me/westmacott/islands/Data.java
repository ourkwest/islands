package uk.me.westmacott.islands;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Data {

    public static void spitImage(int[][] data, Object name) throws IOException {
        int width = data.length;
        int height = data[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
//                if (data[x][y] != -1) {
                    image.setRGB(x, y, data[x][y] | 0xFF000000);
//                }
            }
        }
        ImageIO.write(image, "png", new File("./" + name + ".png"));
    }

}
