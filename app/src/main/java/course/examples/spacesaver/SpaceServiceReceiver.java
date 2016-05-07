package course.examples.spacesaver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Class for automatically starting the SpaceSaverService on device boot <br/>
 * Created by kannanb on 4/11/2016.
 */
public class SpaceServiceReceiver extends BroadcastReceiver {

    public static final String LOG_TAG_NAME = "SpaceSaver.SpaceServiceReceiver";

    /**
     * Method to start the SpaceSaverService on device boot completion
     * @param context Context passed to start the service
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG_NAME, "Android device booted. Starting SpaceService service...");
        Toast.makeText(context, "Android device booted. Starting SpaceService service...", Toast.LENGTH_LONG).show();
        Intent serviceIntent = new Intent(context, SpaceSaverService.class);
        //serviceIntent.putExtra(Constants.IMAGE_QUALITY, imgQuality);
        //serviceIntent.putExtra(Constants.DELETE_IMAGES, bDeleteImages);
        //serviceIntent.putExtra(Constants.USED_SPACE_THRESHOLD, spaceThreshold);
        context.startService(serviceIntent);
    }
}
