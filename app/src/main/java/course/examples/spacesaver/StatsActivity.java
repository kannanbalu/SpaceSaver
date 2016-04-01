package course.examples.spacesaver;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.GraphicalView;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/*
 * Class for displaying Line Chart representing statistical comparisions between original and the respective compressed images
 */
public class StatsActivity extends Activity {

    public static final String LOG_TAG_NAME = "SpaceSaver.StatsActivity";

    private List<Double> srcFileList = new ArrayList<Double>();
    private List<Double> compressedFileList = new ArrayList<Double>();
    private double maxsize = 0;
    private String sizestr = "Mbytes";
    private String spaceSavedMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        LinearLayout linearLayout =  (LinearLayout)findViewById(R.id.layoutinfo);

        Intent intent = getIntent();
        spaceSavedMessage = intent.getStringExtra(Constants.SPACE_SAVED_INFO);

        //TextView textView = (TextView)findViewById(R.id.spaceSavedText);
        //textView.setText(spaceSavedMessage);

        long [] srcFileSizes = intent.getLongArrayExtra(Constants.SRC_FILE_SIZES);
        long [] compressedFileSizes = intent.getLongArrayExtra(Constants.COMPRESSED_FILE_SIZES);

        Log.i(LOG_TAG_NAME, "# of source files length: " + srcFileSizes.length + " compressed files length: " + compressedFileSizes.length);

        maxsize = 0;
        for (int i=0; i<srcFileSizes.length; i++) {
            double srcsize = Utility.getSizeInMbytes(srcFileSizes[i]);
            if (srcsize > maxsize) {
                maxsize = srcsize;
            }
            BigDecimal bd = new BigDecimal(srcsize).setScale(1, RoundingMode.HALF_EVEN);
            srcsize = bd.doubleValue();
            srcFileList.add(srcsize);

            double compressedsize = Utility.getSizeInMbytes(compressedFileSizes[i]);
            bd = new BigDecimal(compressedsize).setScale(1, RoundingMode.HALF_EVEN);
            compressedsize = bd.doubleValue();
            compressedFileList.add(compressedsize);
        }

        if (maxsize < 1) {  //Less than 1MB image
            maxsize = maxsize * Utility.KILOBYTE;  //kilobytes
            sizestr = "Kbytes";
        } else {
            maxsize += 2; //A little additional buffering on Y-axis
        }

        XYSeries series1 = new XYSeries("Original image size");
        int index = 0;
        for (int i=0; i<srcFileList.size(); i++) {
            series1.add(index++, srcFileList.get(i));
        }

        Log.i(LOG_TAG_NAME, "first series added: " + srcFileList.size());
        XYSeries series2 = new XYSeries("Compressed image size");
        index = 0;
        for (int i=0; i<compressedFileList.size(); i++) {
            series2.add(index++, compressedFileList.get(i));
        }

        Log.i(LOG_TAG_NAME, "second series added: " + compressedFileList.size());
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series1);
        dataset.addSeries(series2);

        Log.i(LOG_TAG_NAME, "both series added to series dataset");

        // Now we create the renderer
        XYSeriesRenderer renderer1 = new XYSeriesRenderer();
        renderer1.setLineWidth(2);
        renderer1.setColor(Color.RED);
        renderer1.setDisplayBoundingPoints(true);
        renderer1.setDisplayChartValues(true);
        renderer1.setPointStyle(PointStyle.CIRCLE);
        renderer1.setPointStrokeWidth(3);

        XYSeriesRenderer renderer2 = new XYSeriesRenderer();
        renderer2.setLineWidth(2);
        renderer2.setColor(Color.GREEN);
        renderer2.setDisplayBoundingPoints(true);
        renderer2.setDisplayChartValues(true);
        renderer2.setPointStyle(PointStyle.CIRCLE);
        renderer2.setPointStrokeWidth(3);


        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.BLACK);

        mRenderer.addSeriesRenderer(renderer1);
        mRenderer.addSeriesRenderer(renderer2);
        mRenderer.setYTitle("Image size in " + sizestr);
        mRenderer.setXTitle("Images");

        mRenderer.setLabelsTextSize(20);
        mRenderer.setXLabelsColor(Color.BLUE);

        mRenderer.setAxesColor(Color.MAGENTA);
        mRenderer.setXLabelsColor(Color.BLUE);

        // We want to avoid black border
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
// Disable Pan on two axis
        mRenderer.setPanEnabled(false, false);
        mRenderer.setYAxisMax(maxsize);
        mRenderer.setYAxisMin(0);
        mRenderer.setShowGrid(true);

        mRenderer.setLabelsColor(Color.GREEN);
        mRenderer.setChartTitle("Original image vs Compressed image comparison \n\n" + spaceSavedMessage);
        mRenderer.setChartTitleTextSize(15);

        mRenderer.setInScroll(true);

        Log.i(LOG_TAG_NAME, "fetching line chart view...  dataset: " + dataset + " renderer: " + mRenderer);

        GraphicalView chartView = ChartFactory.getLineChartView(this, dataset, mRenderer);
        linearLayout.addView(chartView);
        Log.i(LOG_TAG_NAME, "chartView added to Linear layout...");
    }
}
