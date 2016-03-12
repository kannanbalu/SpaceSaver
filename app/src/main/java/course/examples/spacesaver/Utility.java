package course.examples.spacesaver;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kannanb on 3/7/2016.
 */
public class Utility {

    public static final String LOG_TAG_NAME = "SpaceSaver.Utility";

    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    public static List<String> getCameraImages(Context context) {

        final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
        final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);

        final String CAMERA_THUMBNAIL_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/DCIM/.thumbnails";
        final String CAMERA_THUMBNAIL_BUCKET_ID = getBucketId(CAMERA_THUMBNAIL_BUCKET_NAME);

        final String[] projection = { MediaStore.Images.Media.DATA };
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        ArrayList<String> result = new ArrayList<String>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                Log.i(LOG_TAG_NAME, data + " " + " size = " + (new File(data).length()));
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }


    public static ArrayList<Pair> compressImages(List<String> imgList, int imageQuality) {
        ArrayList<Pair> list = new ArrayList<Pair>();
        String imgFolder = Environment.getExternalStorageDirectory().toString() + "/CompressedImages/";
        File folder = new File(imgFolder);
        if (! folder.exists()) {
            folder.mkdir();
            Log.i(LOG_TAG_NAME, "Folder: " + folder + " created ");
        } else {
            Log.i(LOG_TAG_NAME, "Folder: " + folder + " already exists ");
        }
        for (String image : imgList) {
            String compressedImage = compressImage(image, imageQuality, imgFolder);
            list.add(new Pair(image, compressedImage));
        }
        return list;
    }

    public static String compressImage(String imgFile, int imageQuality, String imgFolder) {
        try {
            final String PREFIX_COMPRESSED = "compressed_";
            String srcFileName = imgFile;
            int index = imgFile.lastIndexOf(File.separator);
            if (index != -1) {
                srcFileName = imgFile.substring(index + 1, imgFile.length());
            }
            Log.i(LOG_TAG_NAME, " srcfile: " + srcFileName);
            Bitmap bmap = BitmapFactory.decodeFile(imgFile);
            FileOutputStream ostream = new FileOutputStream(imgFolder + PREFIX_COMPRESSED + srcFileName);
            boolean bcompress = bmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, ostream);
            ostream.flush();
            ostream.close();
            if (!bcompress) {
                Log.i(LOG_TAG_NAME, "compression of image file : " + imgFile + " failed!");
            } else {
                long size = new File(imgFolder + "compressed_" + srcFileName).length();
                Log.i(LOG_TAG_NAME, "compression image : " + imgFolder + PREFIX_COMPRESSED + srcFileName + " size: " + size);
            }
            return imgFolder + PREFIX_COMPRESSED + srcFileName;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(LOG_TAG_NAME, e.toString());
        }
        return null;
    }

    public static void getStorageCapacity() {
        Log.i(LOG_TAG_NAME, Environment.getExternalStorageDirectory().toString());
        Log.i(LOG_TAG_NAME, Environment.getDataDirectory().toString());
        Log.i(LOG_TAG_NAME, Environment.getDownloadCacheDirectory().toString());
        Log.i(LOG_TAG_NAME, Environment.getRootDirectory().toString());
        Log.i(LOG_TAG_NAME, Environment.getExternalStorageState());

        StatFs sfs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        try {
            final long KILOBYTE = 1024;
            StatFs internalStatFs = new StatFs( Environment.getRootDirectory().getAbsolutePath() );
            long internalTotal;
            long internalFree;

            StatFs externalStatFs = new StatFs( Environment.getExternalStorageDirectory().getAbsolutePath() );
            long externalTotal;
            long externalFree;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                internalTotal = ( internalStatFs.getBlockCountLong() * internalStatFs.getBlockSizeLong() ) / ( KILOBYTE * KILOBYTE );
                internalFree = ( internalStatFs.getAvailableBlocksLong() * internalStatFs.getBlockSizeLong() ) / ( KILOBYTE * KILOBYTE );
                externalTotal = ( externalStatFs.getBlockCountLong() * externalStatFs.getBlockSizeLong() ) / ( KILOBYTE * KILOBYTE );
                externalFree = ( externalStatFs.getAvailableBlocksLong() * externalStatFs.getBlockSizeLong() ) / ( KILOBYTE * KILOBYTE );
            }
            else {
                internalTotal = ( (long) internalStatFs.getBlockCount() * (long) internalStatFs.getBlockSize() ) / ( KILOBYTE * KILOBYTE );
                internalFree = ( (long) internalStatFs.getAvailableBlocks() * (long) internalStatFs.getBlockSize() ) / ( KILOBYTE * KILOBYTE );
                externalTotal = ( (long) externalStatFs.getBlockCount() * (long) externalStatFs.getBlockSize() ) / ( KILOBYTE * KILOBYTE );
                externalFree = ( (long) externalStatFs.getAvailableBlocks() * (long) externalStatFs.getBlockSize() ) / ( KILOBYTE * KILOBYTE );
            }

            long total = internalTotal + externalTotal;
            long free = internalFree + externalFree;
            long used = total - free;
            Log.i(LOG_TAG_NAME, "Total bytes = " + total);
            Log.i(LOG_TAG_NAME, "Available bytes = " + free);
            Log.i(LOG_TAG_NAME, "Used bytes = " + used);
            Log.i(LOG_TAG_NAME, " % capacity used " + (used * 100/ total) );
            Log.i(LOG_TAG_NAME, " % capacity unused " + (free * 100/ total)) ;

        } catch (Throwable t) {
            t.printStackTrace();
            Log.i(LOG_TAG_NAME, t.toString());
            return;
        }
    }
}