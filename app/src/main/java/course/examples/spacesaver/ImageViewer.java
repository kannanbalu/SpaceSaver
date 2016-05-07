package course.examples.spacesaver;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Activity class to render a single image in an Image View alongwith its path on the local storage
 * Created by kannanb on 3/14/2016.
 */
public class ImageViewer extends Activity {

    public static final String LOG_TAG_NAME = "SpaceSaver.ImageViewer";
    private static final String LOG_TAG_TOUCH = "SpaceSaver.ImageViewer.Touch";

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;


    /**
     * Method to initialize UI components of the class and prepare for user interactions .
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_main);
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        TextView textView = (TextView) findViewById(R.id.fileTextView);

        Intent intent = getIntent();
        byte [] imageBytes = intent.getByteArrayExtra(Constants.IMAGE_VIEW);
        Log.i(LOG_TAG_NAME, " image bytes is := " + imageBytes);
        Log.i(LOG_TAG_NAME, " image bytes length is := " + imageBytes.length);
        String imageFile = intent.getStringExtra(Constants.IMAGE_FILENAME);
        Log.i(LOG_TAG_NAME, " image file is := " + imageFile);
        textView.setText(imageFile);
        Bitmap bmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length).copy(Bitmap.Config.ARGB_8888, true);
        imageView.setScaleType(ImageView.ScaleType.MATRIX); //This is a must or else imageview draws image in center by default.
        imageView.setImageBitmap(bmap);

        Drawable d = imageView.getDrawable();
        Rect bounds = d.getBounds();
        int top = (int)imageView.getY() + bounds.top;
        int left = (int)imageView.getX() + bounds.left;
        Log.i(LOG_TAG_NAME, "x: " + imageView.getX() + "y: " + imageView.getY() + " bounds-top: " + bounds.top + " bounds-left: " + bounds.left);

        matrix.setTranslate(top, left);
    }

    /**
     * Method to handle touch events on the image
     * @param event type of touch event triggered (for moving the image, zoom in / zoom out the image)
     * @return if the event is consumed or not
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        ImageView view = (ImageView)findViewById(R.id.imageView);
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.i(LOG_TAG_TOUCH, "e.getX: " + event.getX() + " e.getY: " + event.getY());
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                mode = NONE;
                Log.d(LOG_TAG_TOUCH, "pointer up");
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                oldDist = spacing(event);
                Log.d(LOG_TAG_TOUCH, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(LOG_TAG_TOUCH, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG)
                {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                }
                else if (mode == ZOOM)
                {
                    float newDist = spacing(event);
                    Log.d(LOG_TAG_TOUCH, "newDist=" + newDist);
                    if (newDist > 5f)
                    {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling up or down depending on 2-finger distance expanding or shrinking
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
        view.setImageMatrix(matrix); // refresh imageview with the transformed matrix .
        return true; // event is consumed
    }

    /**
     * Method to check the spacing between the two fingers on touch
     * @param event containing position of the two fingers
     * @return spacing between the two fingers
     */
    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /**
     * Method to calculate the midpoint between the two fingers
     * @param point Point structure to be filled in with the mid point
     * @param event Event containing the current position of two fingers
     */
    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}
