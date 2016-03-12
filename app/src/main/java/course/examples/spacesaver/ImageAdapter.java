package course.examples.spacesaver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kannanb on 3/8/2016.
 */
public class ImageAdapter extends BaseAdapter {
    public static final String LOG_TAG_NAME = "SpaceSaver.ImageAdapter";
    List<Pair> imgList = null;
    Context context = null;
    List<Bitmap> bmpList = new ArrayList<Bitmap>();
    public ImageAdapter(Context c, List<Pair> list) {
        context = c;
        imgList = list;
    }

    public void setImages(List<Pair> list) {
        imgList = list;
    }

    @Override
    public int getCount() {
        return imgList.size() * 2;
    }

    @Override
    public Object getItem(int position) {
        Log.i(LOG_TAG_NAME, "getItem");
        if (bmpList.size() > position) {
            return bmpList.get(position);
        }
        int index = (position % 2 == 0) ? position/2 : (position-1)/2 ;
        Log.i(LOG_TAG_NAME, "position: " + position + " - index : " + index);
        Pair imagePair = (Pair)imgList.get(index);
        String imgFile =  (position % 2 == 0) ? imagePair.compressedImageFile : imagePair.srcImageFile;
        Log.i(LOG_TAG_NAME, "position: " + position + " - " + imgFile);
        Bitmap bmap = BitmapFactory.decodeFile(imgFile);
        bmpList.add(bmap);
        return bmap;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        }
        else
        {
            imageView = (ImageView) convertView;
        }
        Log.i(LOG_TAG_NAME, "getView: position: " + position);
        imageView.setImageBitmap((Bitmap)getItem(position));
        return imageView;
    }
}
