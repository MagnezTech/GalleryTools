package pl.magneztech.tools.gallery;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mgwozdek on 2016-11-04.
 */
public class ImagePreview implements Icon, Serializable {

    static private Map<String, Image> imageMap;

    private String discription;
    private String path;

    public ImagePreview(Image image, String discription, String path) {
        if (imageMap == null) {
            imageMap = new HashMap<String, Image>();
        }
        this.imageMap.put(discription, image);
        this.discription = discription;
        this.path = path;
    }

    public Image getImage(){
        return imageMap.get(discription);
    }

    public String getDiscription() {
        return discription;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImagePreview that = (ImagePreview) o;

        if (discription != null ? !discription.equals(that.discription) : that.discription != null) return false;
        return path != null ? path.equals(that.path) : that.path == null;

    }

    @Override
    public int hashCode() {
        int result = discription != null ? discription.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(imageMap.get(discription), x, y, 100, 100, null);
    }

    @Override
    public int getIconWidth() {
        return 100;
    }

    @Override
    public int getIconHeight() {
        return 100;
    }

    @Override
    public String toString() {
        return "ImagePreview{" +
                "discription='" + discription + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
