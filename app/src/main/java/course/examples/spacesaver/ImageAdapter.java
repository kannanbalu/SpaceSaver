package course.examples.spacesaver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for rendering a set of images passed to it. Images can be of any type (BMP, PNG, GIF, JPG)
 * Created by kannanb on 3/8/2016.
 */
public class ImageAdapter extends BaseAdapter {
    public static final String LOG_TAG_NAME = "SpaceSaver.ImageAdapter";
    List<Pair> imgList = null;
    Context context = null;
    List<BmpData> bmpList = new ArrayList<BmpData>();
    private final static int IMAGE_HEIGHT = 200;
    private final static int IMAGE_WIDTH = 200;

    private Paint paint = new Paint();

    /**
     * Constructor taking a context and a list of images as parameters
     * @param c  Context passed to the imageadapter
     * @param list List of images to be rendered
     */
    public ImageAdapter(Context c, List<Pair> list) {
        context = c;
        imgList = list;
        initialize();
    }

    /**
     * Method to initialize the paint configuration (Color, Style, Font Size)
     */
    private void initialize() {
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(25);
    }

    /**
     * Method to reinitialize the adapter with a new context and list of images
     * @param c Context passed to the imageadapter
     * @param list List of images to be rendered
     */
    public void reInitialize(Context c, List<Pair> list) {
        context = c;
        bmpList.clear();
        imgList.clear();
        imgList = list;
    }

    /**
     * Method to reset a new list of images to the adapter for rendering
     * @param list List of images to be rendered
     */
    public void setImages(List<Pair> list) {
        imgList = list;
    }

    /**
     * Method to get the number of images to be rendered
     * @return number of images
     */
    @Override
    public int getCount() {
        Log.i(LOG_TAG_NAME, "getCount() returned: " + imgList.size() * 2);
        return imgList.size() * 2;
    }

    /**
     * Method to return the image data for a given position in the view
     * @param position Index of the image to be rendered
     * @return a BmpData structure containing the bitmap and path to the image file on the device storage
     */
    @Override
    public BmpData getItem(int position) {
        Log.i(LOG_TAG_NAME, "getItem");
        if (bmpList.size() > position) {
            return bmpList.get(position);
        }
        int index = (position % 2 == 0) ? position/2 : (position-1)/2 ;
        Log.i(LOG_TAG_NAME, "position: " + position + " - index : " + index);
        Pair imagePair = (Pair)imgList.get(index);
        String imgFile =  (position % 2 == 0) ? imagePair.srcImageFile : imagePair.compressedImageFile;
        Log.i(LOG_TAG_NAME, "position: " + position + " - " + imgFile);
        try {
            File origFile = new File(imgFile);
            if (!origFile.exists()) {
                bmpList.add((BmpData)null);
                return null; //If it reaches here, that is because the image has been deleted already
            }
            Log.i(LOG_TAG_NAME, "File: " + origFile + " exists " + origFile.exists());
            Bitmap bmap = decodeSampledBitmapFromResource(origFile, IMAGE_WIDTH, IMAGE_HEIGHT).copy(Bitmap.Config.ARGB_8888, true);
            Log.i(LOG_TAG_NAME, imgFile + " decoded successfully!");
            Log.i(LOG_TAG_NAME, imgFile + " returning bmp:= " + bmap.toString());
            BmpData data = new BmpData(bmap, origFile);
            bmpList.add(data);
            return data;
        } catch (Exception e) {
            Log.i(LOG_TAG_NAME, e.toString());
        }
        return null;
    }

    /**
     * Return the id of the item for a given position
     * @param position index of the item
     * @return
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Method to calculate the sample size from the options provided in BitmapFactory
     * @param options contains the desired options in BitmapFactory
     * @param reqWidth width of the image
     * @param reqHeight height of the image
     * @return sample size from the options provided
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.i(LOG_TAG_NAME, "height := " + height + " width := " + width + " inSampleSize := " + inSampleSize);
        return inSampleSize;
    }

    /**
     * Method to return the bitmap data of a given image file
     * @param imgFile Path to the image file on the device storage
     * @param reqWidth required width of the decoded image
     * @param reqHeight required height of the decoded image
     * @return a Bitmap with dimension as specified in reqWidth and reqHeight parameters
     * @throws Exception
     */
    public  Bitmap decodeSampledBitmapFromResource(File imgFile,int reqWidth, int reqHeight) throws Exception {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //do this to calculate only image dimensions and not allocate memory for constructing bitmap
        FileInputStream instream = new FileInputStream(imgFile);
        Log.i(LOG_TAG_NAME, " About to decode stream...");
        BitmapFactory.decodeStream(instream, null, options);

        Log.i(LOG_TAG_NAME, " calculating sample size... ");
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;  //Now turn it off to produce bitmap with the inSampleSize calculated.
        Log.i(LOG_TAG_NAME, "returning bitmap with the identified sample size...");
        instream.close();
        instream = new FileInputStream(imgFile);
        return BitmapFactory.decodeStream(instream, null, options);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(IMAGE_WIDTH, IMAGE_HEIGHT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
            Log.i(LOG_TAG_NAME, "new imageView created...");
        }
        else
        {
            imageView = (ImageView) convertView;
            Log.i(LOG_TAG_NAME, "imageView casted from convertView ...");
        }
        Log.i(LOG_TAG_NAME, "getView: position: " + position + " - imageView: " + imageView.toString());

        final BmpData data = getItem(position);

        if (data == null || data.bmap == null) {
            //If the original image file is already deleted, there is nothing to be drawn and so no need to create an Intent for this view on user's click/touch event
            return imageView;
        }

        Log.i(LOG_TAG_NAME, " bmpdata srcfile := " + data.srcFile);
        long filesize = data.srcFile.length();

        Log.i(LOG_TAG_NAME, " bmpdata srcfile size := " + data.srcFile.length());
        Log.i(LOG_TAG_NAME, "creating canvas with ..." + data.bmap);
        Canvas c = new Canvas(data.bmap);
        Log.i(LOG_TAG_NAME, "canvas created..." + data.bmap);
        Log.i(LOG_TAG_NAME, "About to draw text");
        c.drawText(Utility.getSizeInString(filesize), 50, 50, paint);
        Log.i(LOG_TAG_NAME, "Text drawn");



        imageView.setImageBitmap((Bitmap) data.bmap);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClass(context, course.examples.spacesaver.ImageViewer.class);
                intent.putExtra(Constants.IMAGE_FILENAME, data.srcFile.getAbsolutePath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                data.bmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                intent.putExtra(Constants.IMAGE_VIEW, byteArray);
                context.startActivity(intent);
            }
        });
        Log.i(LOG_TAG_NAME, "setImageBitmap done..." + filesize);

        return imageView;
    }
}
