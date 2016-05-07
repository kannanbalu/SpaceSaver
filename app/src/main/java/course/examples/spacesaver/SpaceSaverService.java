package course.examples.spacesaver;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.List;

/**
 * Service class for constantly monitoring the disk usage and compress images to bring the free space below the threshold defined by the user <br/>
 * Created by kannanb on 4/11/2016.
 */
public class SpaceSaverService extends Service {

    public static final String LOG_TAG_NAME = "SpaceSaver.SpaceSaverService";
    private int imageQuality = 90;
    private boolean bDeleteImages = false;
    private int spaceThreshold = 90;

    @Override
    public void onCreate() {

    }

    /**
     * Method to initialize class parameters and start a background thread for monitoring space usage
     * @param intent Intent passed to service
     * @param flags
     * @param startId
     * @return START_STICKY for the service to be always active
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(LOG_TAG_NAME, "Starting background SpaceSaverService...");
        Toast.makeText(this, "Starting background SpaceSaverService...", Toast.LENGTH_LONG).show();
        imageQuality = intent.getIntExtra(Constants.IMAGE_QUALITY, imageQuality);
        bDeleteImages = intent.getBooleanExtra(Constants.DELETE_IMAGES, bDeleteImages);
        spaceThreshold = intent.getIntExtra(Constants.USED_SPACE_THRESHOLD, spaceThreshold);
        runBackgroundThread();
        return START_STICKY;
    }

    /**
     * Method for performing the following operations at regular intervals
     * 1. Check if available free space is less than the threshold defined by user
     * 2. If free space is less, start compressing images of higher size (few images at a time)
     * 3. Perform compression until the available free space is brought below the desired threshold from the user
     * 4. Continue monitoring at regular intervals on the available free space.
     */
    public void runBackgroundThread() {

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            private boolean bDeleteImages = false;
            private List<String> imageFiles = null;
            @Override
            public void run() {

                long spaceUsed = Utility.getUsedSpacePercentage();
                if (spaceUsed < spaceThreshold)  {
                    Log.i(LOG_TAG_NAME, "No compression required: " + spaceUsed + " threshold: " + spaceThreshold);
                    handler.postDelayed(this, 1000);
                    Toast.makeText(SpaceSaverService.this, "No compression required: " + spaceUsed + " threshold: " + spaceThreshold, Toast.LENGTH_LONG).show();
                    return;
                }

                int imgQuality = SpaceSaverService.this.imageQuality;
                bDeleteImages = SpaceSaverService.this.bDeleteImages;
                Log.i(LOG_TAG_NAME, "Fetching images on the device");
                imageFiles = Utility.getCameraImages(SpaceSaverService.this);
                Log.i(LOG_TAG_NAME, "Fetched " + imageFiles.size() + " images");
                if (imageFiles == null || imageFiles.size() == 0) {
                    handler.postDelayed(this, 10000);
                    return;
                }
                Toast.makeText(SpaceSaverService.this, "Current space used before compression: " + spaceUsed + " threshold: " + spaceThreshold, Toast.LENGTH_LONG).show();
                Log.i(LOG_TAG_NAME, "Begin compressing images - # of images found := " + imageFiles.size() + " with imageQuality := " + imgQuality);

                String imgFolder = Environment.getExternalStorageDirectory().toString() + "/" + Constants.COMPRESSED_IMAGE_FOLDER + "/";
                File folder = new File(imgFolder);
                if (! folder.exists()) {
                    folder.mkdir();
                    Log.i(LOG_TAG_NAME, "Folder: " + folder + " created ");
                } else {
                    Log.i(LOG_TAG_NAME, "Folder: " + folder + " already exists ");
                }
                Log.i(LOG_TAG_NAME, "compressImages, compressing images with image Quality value := " + imgQuality);
                int progressvalue = (int)Math.round((double)(90 / imageFiles.size()));
                int currentprogress = 10;
                for (String image : imageFiles) {
                    String compressedImage = Utility.compressImage(image, imgQuality, imgFolder);
                    currentprogress += progressvalue;
                }

                Log.i(LOG_TAG_NAME, "image compression done");
                if ( bDeleteImages && imageFiles != null) {
                    Utility.deleteImages(imageFiles); //Delete all original (uncompressed) images
                }
                spaceUsed = Utility.getUsedSpacePercentage();
                Log.i(LOG_TAG_NAME, "Current space used after compression: " + spaceUsed + " threshold: " + spaceThreshold);
                Toast.makeText(SpaceSaverService.this, "Current space used after compression: " + spaceUsed + " threshold: " + spaceThreshold, Toast.LENGTH_LONG).show();

                if (spaceUsed < spaceThreshold) {
                    handler.postDelayed(this, 2000); // We still haven't brought the free space below threshold. Compress again soon...
                } else {
                    handler.postDelayed(this, 10000);
                }
            }
        };
        runnable.run();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
