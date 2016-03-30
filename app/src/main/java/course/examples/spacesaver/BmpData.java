package course.examples.spacesaver;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by kannanb on 3/14/2016.
 */

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
