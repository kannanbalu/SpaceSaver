package course.examples.spacesaver;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private SharedPreferences prefs = null;

    private int imgQuality = 80;
    private int spaceThreshold = 90;
    private GridView gridView = null;
    private TextView storageTextView = null;
    private ImageAdapter imageAdapter = null;
    private List<Pair> imageList = null;
    private boolean bDeleteImages = false;

    public static final String LOG_TAG_NAME = "SpaceSaver.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(Constants.USER_PREFERENCE, 0);

        imgQuality = prefs.getInt(Constants.IMAGE_QUALITY, 80);
        spaceThreshold = prefs.getInt(Constants.SPACE_THRESHOLD, 90);
        bDeleteImages = prefs.getBoolean(Constants.DELETE_IMAGES, false);

        final TextView tView = (TextView)findViewById(R.id.qualityText);
        tView.setText("Image Quality: " + imgQuality + " / 100");

        storageTextView = (TextView)findViewById(R.id.storageCapacity);
        storageTextView.setText(Utility.getStorageCapacity());

        CheckBox imgCheckBox = (CheckBox)findViewById(R.id.DeleteImages);
        imgCheckBox.setChecked(bDeleteImages);

        imgCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bDeleteImages = isChecked;
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean(Constants.DELETE_IMAGES, bDeleteImages);
                edit.commit();
            }
        });

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
                edit.putInt(Constants.IMAGE_QUALITY, progressValue);
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
                edit.putInt(Constants.SPACE_THRESHOLD, progressValue);
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
                Log.i(LOG_TAG_NAME, "Fetching images on the device taken by camera");
                List<String> imageFiles = Utility.getCameraImages(MainActivity.this);
                if (imageFiles == null || imageFiles.size() == 0) {
                    Toast.makeText(MainActivity.this, "No images present for compression...", Toast.LENGTH_LONG ).show();
                    return;
                }
                Log.i(LOG_TAG_NAME, "Begin compressing images - # of images found := " + imageFiles.size() + " with imageQuality := " + imgQuality);
                if (imageList != null) {
                    imageList.clear();
                }
                Toast.makeText(MainActivity.this, "Image compression started... Please wait...", Toast.LENGTH_LONG ).show();
                imageList = Utility.compressImages(imageFiles, imgQuality);
                Log.i(LOG_TAG_NAME, "set the compressed image list to the grid view: # of images := " + imageList.size());
                setGridViewAdapter(imageList);
                String msg = Utility.calculateSpaceSaved(imageList);
                Toast.makeText(MainActivity.this, imageList.size() + " images compressed successfully...\n " + msg, Toast.LENGTH_LONG ).show();
                if (false && bDeleteImages) { //remove false later
                    Utility.deleteImages(imageFiles); //Delete all original (uncompressed) images
                }
            }
        });

        gridView = (GridView)findViewById(R.id.imageGrid);

        Button statsBtn = (Button)findViewById(R.id.statsBtn);
        statsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClass(MainActivity.this, course.examples.spacesaver.StatsActivity.class);
                ArrayList<String> list = getImagesList();
                intent.putExtra(Constants.IMAGE_LIST, list);
                MainActivity.this.startActivity(intent);
            }
        });

    }

    public ArrayList<String> getImagesList() {
        ArrayList<String> list = new ArrayList<String>();
        if (imageList == null) return list; //return empty list
        for (Pair pair : imageList) {
            list.add(pair.srcImageFile);
            list.add(pair.compressedImageFile);
        }
        return list;
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
