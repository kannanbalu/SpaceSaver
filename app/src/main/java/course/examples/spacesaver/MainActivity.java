package course.examples.spacesaver;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(USER_PREFERENCE, 0);
        imgQuality = prefs.getInt(IMAGE_QUALITY, 80);

        int spaceThreshold = prefs.getInt(SPACE_THRESHOLD, 90);

        final TextView tView = (TextView)findViewById(R.id.qualityText);
        tView.setText("Image Quality: " + imgQuality + " / 100");

        final SeekBar imgBar = (SeekBar)findViewById(R.id.imgBar);
        imgBar.setProgress(imgQuality);
        imgBar.setEnabled(true);
        imgBar.setVisibility(SeekBar.VISIBLE);

        imgBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                tView.setText("Image Quality: " + progressValue + " / " + imgBar.getMax());
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
                List<String> imageFiles = Utility.getCameraImages(MainActivity.this);
                List<Pair> imageList = Utility.compressImages(imageFiles, imgQuality);
                gridView.setAdapter(new ImageAdapter(MainActivity.this, imageList));
                Toast.makeText(MainActivity.this, "Image compression completed...", Toast.LENGTH_LONG ).show();
            }
        });

        gridView = (GridView)findViewById(R.id.imageGrid);
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
