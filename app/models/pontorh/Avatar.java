package models.pontorh;

import play.db.jpa.Model;
import utils.ImageUtils;

import javax.persistence.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Entity
public class Avatar extends Model {

    @OneToOne(fetch = FetchType.LAZY)
    public User user;

    public String name;

    @Column(name = "content_type")
    public String contentType;

    @Column(name = "data", columnDefinition = "LONGBLOB")
    @Lob
    public byte[] imageBytes;


    public Long size;

    public static Avatar findByUser(User user) {
        return Avatar.find("user", user).first();
    }

    public InputStream toBinary() {
        return new ByteArrayInputStream(imageBytes);
    }

    public static void setUserAvatarFromURL(User user, String url) throws IOException {
        final BufferedImage image = ImageUtils.getImageFromURL(url);
        String contentType = ImageUtils.getType(url);

        Avatar avatar = Avatar.findByUser(user);
        if (avatar == null) {
            avatar = new Avatar();
        }

        avatar.name = "Avatar".concat(user.id.toString()).concat(contentType.replace("image/", "."));
        avatar.contentType = contentType;
        avatar.imageBytes = ImageUtils.getScaledInstance(image, 200, 200, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
        avatar.size = Long.valueOf(avatar.imageBytes.length);
        avatar.user = user;
        avatar.save();
    }

}
