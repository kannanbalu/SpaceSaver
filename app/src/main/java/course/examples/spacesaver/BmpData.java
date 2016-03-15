package course.examples.spacesaver;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by kannanb on 3/14/2016.
 */
public class BmpData {
    Bitmap bmap;
    File srcFile;
    public BmpData(Bitmap bitmap, File file) {
        bmap = bitmap;
        srcFile = file;
    }
}
