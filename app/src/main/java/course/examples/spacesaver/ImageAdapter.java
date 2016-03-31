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

    public ImageAdapter(Context c, List<Pair> list) {
        context = c;
        imgList = list;
        initialize();
    }

    private void initialize() {
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(25);
    }

    public void reInitialize(Context c, List<Pair> list) {
        context = c;
        bmpList.clear();
        imgList.clear();
        imgList = list;
    }

    public void setImages(List<Pair> list) {
        imgList = list;
    }

    @Override
    public int getCount() {
        Log.i(LOG_TAG_NAME, "getCount() returned: " + imgList.size() * 2);
        return imgList.size() * 2;
    }

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

    @Override
    public long getItemId(int position) {
        return 0;
    }

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
