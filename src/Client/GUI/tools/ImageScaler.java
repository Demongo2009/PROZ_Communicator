package Client.GUI.tools;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageScaler {

    static public BufferedImage resizeImage(BufferedImage originalImage, Dimension maxDimension) {
        Dimension imageDimensions = new Dimension(originalImage.getWidth(), originalImage.getHeight());
        Dimension scaledDimensions = getScaledDimension(imageDimensions, maxDimension);

        BufferedImage resizedImage = new BufferedImage(scaledDimensions.width, scaledDimensions.height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, scaledDimensions.width, scaledDimensions.height, Color.WHITE, null);
        g.dispose();

        return resizedImage;
    }

    static private Dimension getScaledDimension(Dimension imgSize, Dimension maxDimension) {
        int originalWidth = imgSize.width;
        int originalHeight = imgSize.height;
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > maxDimension.getWidth()) {
            newWidth = maxDimension.width;
            newHeight = (newWidth * originalHeight) / originalWidth;
        }

        if (newHeight > maxDimension.height) {
            newHeight = maxDimension.height;
            newWidth = (newHeight * originalWidth) / originalHeight;
        }

        return new Dimension(newWidth, newHeight);
    }

}