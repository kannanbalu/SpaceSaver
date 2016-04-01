package course.examples.spacesaver;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
 * Utility class containing reusable and independent methods
 * Created by kannanb on 3/7/2016.
 */
public class Utility {

    public static final String LOG_TAG_NAME = "SpaceSaver.Utility";
    public static final long KILOBYTE = 1024;
    public static final int MAX_IMAGES_TO_COMPRESS = 15;
    public static final int MAX_FILE_SIZE = 8; //8 MB file size

    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    /*
     * Retrieves list of images on the device from the location /DCIM/Camera
     * @param context Context used for querying content provider in retrieving images
     * @return returns a list containing absolute path to images on the device
     */
    public static List<String> getCameraImages(Context context) {

        final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
        final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);

        final String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null, //selection,
                null, //selectionArgs,
                MediaStore.MediaColumns.SIZE + " DESC");
        int maxCount = MAX_IMAGES_TO_COMPRESS;  //compress only MAX_IMAGES_TO_COMPRESS images at a time lest we run out of memory.
        int count = 0;
        int size = cursor.getCount() < maxCount ? cursor.getCount() : maxCount;
        Log.i(LOG_TAG_NAME, "Total # of images fetched from the device : " + cursor.getCount());
        ArrayList<String> result = new ArrayList<String>(size);
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                long filesize = new File(data).length();
                Log.i(LOG_TAG_NAME, data + " " + " size = " + filesize);
                if (filesize == 0 || Utility.getSizeInMbytes(filesize) > MAX_FILE_SIZE ) {
                    continue;   //It's too memory and compute intensive to compress and load images with higher size on smaller devices. Just skip them.
                }
                result.add(data);
                count++;
            } while (count < maxCount && cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    /*
     * Deletes the first maxCount images from the provided list. The images are deleted from the physical device
     * @param imgList  list containing path to images that needs to be deleted
     * @param maxCount delete maxCount images from the imgList
     */
    public static void deleteImages(List<String> imgList, int maxCount) {
        int count = 0;
        for (String image : imgList) {
            File imageFile = new File(image);
            if (imageFile.exists()) {
                imageFile.delete();
            }
            count++;
            if (count >= maxCount) break;
        }
    }

    /*
     * Deletes all the images from the provided list. The images are deleted from the physical device
     * @param imgList  list containing path to images that needs to be deleted
     */
    public static void deleteImages(List<String> imgList) {
        deleteImages(imgList, imgList.size());
    }

    /*
     * Deletes imageFile from the physical device
     * @param imgFile file to be deleted
     */
    public static void deleteImage(File imgFile) {
        if (imgFile.exists()) {
            imgFile.delete();
        }
    }

    /*
     * Method to evaluate the space saved as a result of compressed images
     * @param pairs A list of Pair objects (containing source file and compressed image)
     * @return A string containing information on space occupied by original images, compressed images and total savings
     */
    public static String calculateSpaceSaved(List<Pair> pairs) {
        int totalSourceFilesLength = 0;
        int totalCompressedFilesLength = 0;
        for (Pair pair : pairs) {
            File srcFile = new File(pair.srcImageFile);
            File compressedFile = new File(pair.compressedImageFile);
            totalSourceFilesLength += srcFile.length();
            totalCompressedFilesLength += compressedFile.length();
        }
        int totalSaved = totalSourceFilesLength - totalCompressedFilesLength;
        int percentSaved = (totalSaved * 100)/ totalSourceFilesLength;
        String savedString = " Original files capacity : " + getSizeInString(totalSourceFilesLength) +
                            " Compressed files capacity : " + getSizeInString(totalCompressedFilesLength) +
                            " Total Savings : [ " + percentSaved + " %]";
        return savedString;
    }

    /*
     * Method to compress all the images in a given list of images of desired image quality
     * @param imgList  A list of images that needs to be compressed
     * @param imageQuality Images to be compressed of the desired quality
     * @return Returns a list of Pair objects containing path to source and compressed image
     */
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
        Log.i(LOG_TAG_NAME, "compressImages, compressing images with image Quality value := " + imageQuality);
        for (String image : imgList) {
            String compressedImage = compressImage(image, imageQuality, imgFolder);
            list.add(new Pair(image, compressedImage));
        }
        return list;
    }

    /*
     * Method to compress an image of the desired image quality and place the generated compressed image in the imgFolder
     * @param imgFile Absolute path to image file that needs to be compressed
     * @param imageQuality The quality level of the compressed image
     * @param imgFolder  Path to the location where the generated compressed image needs to be placed
     * @return Absolute path to the newly generated image
     */
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

    /*
     * Method to print the storage capacity details (total, used, free)
     * @return String containing details on the capacity details
     */
    public static String getStorageCapacity() {
        Log.i(LOG_TAG_NAME, Environment.getExternalStorageDirectory().toString());
        Log.i(LOG_TAG_NAME, Environment.getDataDirectory().toString());
        Log.i(LOG_TAG_NAME, Environment.getDownloadCacheDirectory().toString());
        Log.i(LOG_TAG_NAME, Environment.getRootDirectory().toString());
        Log.i(LOG_TAG_NAME, Environment.getExternalStorageState());

        StatFs sfs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        try {
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
            Log.i(LOG_TAG_NAME, " % capacity used " + (used * 100 / total));
            Log.i(LOG_TAG_NAME, " % capacity unused " + (free * 100 / total)) ;

            String capacityStr = "Total : " + getSizeInString(total * KILOBYTE * KILOBYTE) + " Available : " + getSizeInString(free * KILOBYTE * KILOBYTE)
                                        + " Used : " + getSizeInString(used * KILOBYTE * KILOBYTE) + "  [ " + (used * 100 / total) + "%used ]";
            return capacityStr;
        } catch (Throwable t) {
            t.printStackTrace();
            Log.i(LOG_TAG_NAME, t.toString());
            return "";
        }
    }

    /*
     * Method to retrieve the percentage of space used on the device
     * @return percentage of space used on the device
     */
    public long getUsedSpacePercentage() {
        StatFs sfs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long usedPercentage = 0;
        try {
            StatFs internalStatFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
            long internalTotal;
            long internalFree;

            StatFs externalStatFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            long externalTotal;
            long externalFree;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                internalTotal = (internalStatFs.getBlockCountLong() * internalStatFs.getBlockSizeLong()) / (KILOBYTE * KILOBYTE);
                internalFree = (internalStatFs.getAvailableBlocksLong() * internalStatFs.getBlockSizeLong()) / (KILOBYTE * KILOBYTE);
                externalTotal = (externalStatFs.getBlockCountLong() * externalStatFs.getBlockSizeLong()) / (KILOBYTE * KILOBYTE);
                externalFree = (externalStatFs.getAvailableBlocksLong() * externalStatFs.getBlockSizeLong()) / (KILOBYTE * KILOBYTE);
            } else {
                internalTotal = ((long) internalStatFs.getBlockCount() * (long) internalStatFs.getBlockSize()) / (KILOBYTE * KILOBYTE);
                internalFree = ((long) internalStatFs.getAvailableBlocks() * (long) internalStatFs.getBlockSize()) / (KILOBYTE * KILOBYTE);
                externalTotal = ((long) externalStatFs.getBlockCount() * (long) externalStatFs.getBlockSize()) / (KILOBYTE * KILOBYTE);
                externalFree = ((long) externalStatFs.getAvailableBlocks() * (long) externalStatFs.getBlockSize()) / (KILOBYTE * KILOBYTE);
            }

            long total = internalTotal + externalTotal;
            long free = internalFree + externalFree;
            long used = total - free;
            usedPercentage = used * 100 / total;
        } catch (Exception e) {

        }
        return usedPercentage;
    }

    /*
     * Method to retrieve a string containing size of a file in bytes, Kbytes, Mbytes, Gbytes based on the size provided in bytes
     * @param filsize  Size of a file in bytes
     * @return Return a string containing the size converted to K, M, G bytes
     */
    public static String getSizeInString(long filesize) {
        double size = filesize;
        String sizeStr = String.valueOf(filesize) + " bytes";
        if (size >= KILOBYTE) {
            size = size / KILOBYTE;
            sizeStr = String.format("%.2f", size) + "K bytes";
        }
        if (size >= KILOBYTE) {
            size = size / KILOBYTE;
            sizeStr = String.format("%.2f", size) + "M bytes";
        }
        if (size >= KILOBYTE) {
            size = size / KILOBYTE;
            sizeStr = String.format("%.2f", size) + "G bytes";
        }
        return sizeStr;
    }

    public static double getSizeInMbytes(long filesize) {
        double size = filesize;
        size = size / (KILOBYTE * KILOBYTE);
        return size;
    }

    public static class ImageCompressTask extends AsyncTask<Object, Integer, List<Pair>> {

        private ProgressDialog dialog = null;
        private MainActivity activity = null;
        private boolean bDeleteImages = false;
        private List<String> imageFiles = null;
        private String dialogMessage = "";

        public ImageCompressTask(MainActivity context) {
            activity = context;
            dialog = new ProgressDialog(context);
            dialogMessage = "Fetching images from the device...";
            dialog.setMessage(dialogMessage);
            dialog.setTitle("Please Wait");
            dialog.setMax(100);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(LOG_TAG_NAME, "progress dialog visible...");
            dialog.show();
        }

        @Override
        protected List<Pair> doInBackground(Object... params) {
            int imgQuality = (int)params[0];
            List<Pair> imageList = (List<Pair>)params[1];
            bDeleteImages = (boolean)params[2];
            Log.i(LOG_TAG_NAME, "Fetching images on the device");
            publishProgress(10);
            imageFiles = Utility.getCameraImages(activity);
            Log.i(LOG_TAG_NAME, "Fetched " + imageFiles.size() + " images");
            if (imageFiles == null || imageFiles.size() == 0) {
                return imageList;
            }
            Log.i(LOG_TAG_NAME, "Begin compressing images - # of images found := " + imageFiles.size() + " with imageQuality := " + imgQuality);

            String imgFolder = Environment.getExternalStorageDirectory().toString() + "/CompressedImages/";
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
            dialogMessage = "Compressing images ...";
            for (String image : imageFiles) {
                String compressedImage = compressImage(image, imgQuality, imgFolder);
                imageList.add(new Pair(image, compressedImage));
                currentprogress += progressvalue;
                publishProgress(currentprogress);
            }

            Log.i(LOG_TAG_NAME, "image compression done");
            return imageList;
        }

        @Override
        protected void onProgressUpdate(Integer... value) {
            super.onProgressUpdate(value);
            dialog.setMessage(dialogMessage);
            Log.i(LOG_TAG_NAME, "progess update: " + value[0]);
            dialog.setProgress(value[0]);
        }

        @Override
        protected void onPostExecute(List<Pair> result) {
            super.onPostExecute(result);
            Log.i(LOG_TAG_NAME, "progress dialog dismissed!");
            activity.updateGridView();
            if ( bDeleteImages && imageFiles != null) {
                deleteImages(imageFiles); //Delete all original (uncompressed) images
            }
            dialog.dismiss();
        }
    }
}
