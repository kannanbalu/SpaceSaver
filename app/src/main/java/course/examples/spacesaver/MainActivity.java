package course.examples.spacesaver;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    private SharedPreferences prefs = null;
    public static final String USER_PREFERENCE = "UserPrefs";
    public static final String IMAGE_QUALITY = "ImageQuality";
    public static final String SPACE_THRESHOLD = "SpaceThreshold";
    private int imgQuality = 80;
    private GridView gridView = null;
    private TextView storageTextView = null;
    private ImageAdapter imageAdapter = null;

    public static final String LOG_TAG_NAME = "SpaceSaver.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(USER_PREFERENCE, 0);
        imgQuality = prefs.getInt(IMAGE_QUALITY, 80);

        int spaceThreshold = prefs.getInt(SPACE_THRESHOLD, 90);

        final TextView tView = (TextView)findViewById(R.id.qualityText);
        tView.setText("Image Quality: " + imgQuality + " / 100");

        storageTextView = (TextView)findViewById(R.id.storageCapacity);
        storageTextView.setText(Utility.getStorageCapacity());

        final SeekBar imgBar = (SeekBar)findViewById(R.id.imgBar);
        imgBar.setProgress(imgQuality);
        imgBar.setEnabled(true);
        imgBar.setVisibility(SeekBar.VISIBLE);

        imgBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                tView.setText("Image Quality: " + progressValue + " / " + imgBar.getMax());
                MainActivity.this.imgQuality = progressValue;
                SharedPreferences.Editor edit = prefs.edit();
                edit.putInt(IMAGE_QUALITY, progressValue);
                edit.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final TextView thresholdView = (TextView)findViewById(R.id.spaceThreshold);
        thresholdView.setText("Space Threshold: " + spaceThreshold + " / 100");

        final SeekBar thresholdBar = (SeekBar)findViewById(R.id.thresholdBar);
        thresholdBar.setProgress(spaceThreshold);
        thresholdBar.setEnabled(true);
        thresholdBar.setVisibility(SeekBar.VISIBLE);

        thresholdBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                thresholdView.setText("Space Threshold: " + progressValue + " / " + thresholdBar.getMax());
                SharedPreferences.Editor edit = prefs.edit();
                edit.putInt(SPACE_THRESHOLD, progressValue);
                edit.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button btn = (Button)findViewById(R.id.compressButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Image compression started...", Toast.LENGTH_LONG ).show();
                Log.i(LOG_TAG_NAME, "Fetching images on the device taken by camera");
                List<String> imageFiles = Utility.getCameraImages(MainActivity.this);
                Log.i(LOG_TAG_NAME, "Begin compressing images - # of images found := " + imageFiles.size() + " with imageQuality := " + imgQuality);
                List<Pair> imageList = Utility.compressImages(imageFiles, imgQuality);
                Log.i(LOG_TAG_NAME, "set the compressed image list to the grid view: # of images := " + imageList.size());
                setGridViewAdapter(imageList);
                //Utility.deleteImages(imageFiles, Utility.MAX_IMAGES_TO_COMPRESS);
                String msg = Utility.calculateSpaceSaved(imageList);
                Toast.makeText(MainActivity.this, imageList.size() + " images compressed successfully...\n " + msg, Toast.LENGTH_LONG ).show();
            }
        });

        gridView = (GridView)findViewById(R.id.imageGrid);
    }

    public void setGridViewAdapter(List<Pair> list) {
            if (imageAdapter == null) {
                imageAdapter = new ImageAdapter(this, list);
            } else {
                imageAdapter.reInitialize(this, list);
            }
            gridView.setAdapter(imageAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
