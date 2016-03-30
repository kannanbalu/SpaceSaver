package course.examples.spacesaver;

/**
 * Wrapper class containing absolute path to source image and the compressed image
 * Created by kannanb on 3/8/2016.
 */
public class Pair {
    String srcImageFile;
    String compressedImageFile;
    public Pair(String srcImage, String cImage) {
        srcImageFile = srcImage;
        compressedImageFile = cImage;
    }
}
