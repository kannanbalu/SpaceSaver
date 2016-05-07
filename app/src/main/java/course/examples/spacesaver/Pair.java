package course.examples.spacesaver;

/**
 * Wrapper class containing absolute path to source image and the compressed image <br/>
 * Created by kannanb on 3/8/2016.
 */
public class Pair {
    String srcImageFile;
    String compressedImageFile;

    /**
     * Constructor for passing path to source and compressed image
     * @param srcImage Path to the source (original) image
     * @param cImage Path to the compressed image
     */
    public Pair(String srcImage, String cImage) {
        srcImageFile = srcImage;
        compressedImageFile = cImage;
    }
}
