package course.examples.spacesaver;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Wrapper class to hold the path to an image file and its bitmap
 */
public class BmpData {
    Bitmap bmap;
    File srcFile;
    public BmpData(Bitmap bitmap, File file) {
        bmap = bitmap;
        srcFile = file;
    }
}
