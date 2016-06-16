package models.pontorh;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import play.db.jpa.Model;

import javax.persistence.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Entity(name = "user_file")
public class Avatar extends Model {

    @OneToOne(fetch = FetchType.LAZY)
    public User user;

    public String name;

    @Column(name = "content_type")
    public String contentType;

    @Column(name = "data", columnDefinition = "LONGBLOB")
    @Lob
    public byte[] imageBytes;


    public int size;

    public static Avatar findByUser(User user) {
        return Avatar.find("user", user).first();
    }

    public InputStream toBinary() {
        return new ByteArrayInputStream(imageBytes);
    }

    /**
     * @param img           the original image to be scaled
     * @param targetWidth   the desired width of the scaled instance,
     *                      in pixels
     * @param targetHeight  the desired height of the scaled instance,
     *                      in pixels
     * @param hint          one of the rendering hints that corresponds to
     *                      {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *                      {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *                      {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *                      {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *                      scaling technique that provides higher quality than the usual
     *                      one-step technique (only useful in downscaling cases, where
     *                      {@code targetWidth} or {@code targetHeight} is
     *                      smaller than the original dimensions, and generally only when
     *                      the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static byte[] getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality) throws IOException {
        byte[] resizedImageByteArray = null;
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage) img;
        int w, h;
        if (higherQuality && img.getWidth() > targetWidth && img.getHeight() > targetHeight) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ByteArrayOutputStream encoderOutputStream = new ByteArrayOutputStream();
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(encoderOutputStream);
            encoder.encode(tmp);
            resizedImageByteArray = encoderOutputStream.toByteArray();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return resizedImageByteArray;
    }


}
