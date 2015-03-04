package unimelb.cis.spatialanalytics.fuelpriceshare.cropimage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

public class CropImageView extends ImageViewTouchBase {

    ArrayList<HighlightView> highlightViews = new ArrayList<HighlightView>();
    HighlightView motionHighlightView;
    HighlightView highlightViewTemp;

    Context context;

    private float lastX;
    private float lastY;
    private int motionEdge;

    Paint paint = new Paint();


    boolean drawRectangle = true;
    Point beginCoordinate = new Point();
    Point endCoordinate = new Point();

    private boolean isRightFirst = false;


    public void ini() {
        // paint.setARGB(125, 50, 50, 50);
        paint.setColor(0xFF33B5E5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(dpToPx(2f));

    }

    private float dpToPx(float dp) {
        return dp * this.getResources().getDisplayMetrics().density;
    }


    @SuppressWarnings("UnusedDeclaration")
    public CropImageView(Context context) {
        super(context);

        ini();

    }

    @SuppressWarnings("UnusedDeclaration")
    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ini();

    }

    @SuppressWarnings("UnusedDeclaration")
    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ini();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (bitmapDisplayed.getBitmap() != null) {
            for (HighlightView hv : highlightViews) {

                hv.matrix.set(getUnrotatedMatrix());
                hv.invalidate();
                if (hv.hasFocus()) {
                    centerBasedOnHighlightView(hv);
                }
            }
        }
    }

    @Override
    protected void zoomTo(float scale, float centerX, float centerY) {
        super.zoomTo(scale, centerX, centerY);
        for (HighlightView hv : highlightViews) {
            hv.matrix.set(getUnrotatedMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void zoomIn() {
        super.zoomIn();
        for (HighlightView hv : highlightViews) {
            hv.matrix.set(getUnrotatedMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void zoomOut() {
        super.zoomOut();
        for (HighlightView hv : highlightViews) {
            hv.matrix.set(getUnrotatedMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        for (HighlightView hv : highlightViews) {
            hv.matrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
    }

    public boolean isInsideRectangle(float positionX, float positionY, Rect rect) {
        if (positionX >= rect.left && positionX <= rect.right && positionY >= rect.top && positionY <= rect.bottom)
            return true;
        else
            return false;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        CropImageActivity cropImageActivity = (CropImageActivity) context;
        if (cropImageActivity.isSaving()) {
            return false;
        }


        //////added by Han; used to draw a rectangle by the user to set the initial size of the rectangle.

        if (drawRectangle) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ///the first down touch has to be valid which means it has to fall into the rectangle of the image
                    for (HighlightView hv : highlightViews) {
                        highlightViewTemp = hv;
                        // hv.handleMotion(HighlightView.MOVE, endCoordinate.x-beginCoordinate.x, endCoordinate.y-beginCoordinate.y);
                        Rect r = highlightViewTemp.getImageScreenRect();
                        if (isInsideRectangle(event.getX(), event.getY(), r)) {
                            isRightFirst = true;
                            beginCoordinate.x = event.getX();
                            beginCoordinate.y = event.getY();
                            endCoordinate.x = event.getX();
                            endCoordinate.y = event.getY();
                            invalidate(); // Tell View that the canvas needs to be redrawn
                        }
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isRightFirst) {
                        //if the first down touch is valid, then draw the rectangle when moving
                        Rect r = highlightViewTemp.getImageScreenRect();
                        if (isInsideRectangle(event.getX(), event.getY(), r)) {
                            endCoordinate.x = event.getX();
                            endCoordinate.y = event.getY();
                            invalidate(); // Tell View that the canvas needs to be redrawn
                        }
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    if (isRightFirst) {
                        // Do something with the beginCoordinate and endCoordinate, like creating the 'final' object
                        drawRectangle = false; // Stop drawing the rectangle
                        invalidate(); // Tell View that the canvas needs to be redrawn
                        //reset the default setting for the rectangle size
                        highlightViewTemp.setDrawRect(beginCoordinate.x, beginCoordinate.y, endCoordinate.x, endCoordinate.y);

                        isRightFirst = false;
                    }
                    break;
            }
            return true;
        }


        /////the flowing are original code

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (HighlightView hv : highlightViews) {

                    int edge = hv.getHit(event.getX(), event.getY());
                    if (edge != HighlightView.GROW_NONE) {
                        motionEdge = edge;
                        motionHighlightView = hv;
                        lastX = event.getX();
                        lastY = event.getY();
                        motionHighlightView.setMode((edge == HighlightView.MOVE)
                                ? HighlightView.ModifyMode.Move
                                : HighlightView.ModifyMode.Grow);
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (motionHighlightView != null) {
                    centerBasedOnHighlightView(motionHighlightView);
                    motionHighlightView.setMode(HighlightView.ModifyMode.None);
                }
                motionHighlightView = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (motionHighlightView != null) {
                    motionHighlightView.handleMotion(motionEdge, event.getX()
                            - lastX, event.getY() - lastY);
                    lastX = event.getX();
                    lastY = event.getY();
                    ensureVisible(motionHighlightView);
                }
                break;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                center(true, true);
                break;
            case MotionEvent.ACTION_MOVE:
                // if we're not zoomed then there's no point in even allowing
                // the user to move the image around. This call to center puts
                // it back to the normalized location (with false meaning don't
                // animate).
                if (getScale() == 1F) {
                    center(true, true);
                }
                break;
        }

        return true;
    }

    // Pan the displayed image to make sure the cropping rectangle is visible.
    private void ensureVisible(HighlightView hv) {
        Rect r = hv.drawRect;

        int panDeltaX1 = Math.max(0, getLeft() - r.left);
        int panDeltaX2 = Math.min(0, getRight() - r.right);

        int panDeltaY1 = Math.max(0, getTop() - r.top);
        int panDeltaY2 = Math.min(0, getBottom() - r.bottom);

        int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
        int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy(panDeltaX, panDeltaY);
        }
    }

    // If the cropping rectangle's size changed significantly, change the
    // view's center and scale according to the cropping rectangle.
    private void centerBasedOnHighlightView(HighlightView hv) {
        Rect drawRect = hv.drawRect;

        float width = drawRect.width();
        float height = drawRect.height();

        float thisWidth = getWidth();
        float thisHeight = getHeight();

        float z1 = thisWidth / width * .6F;
        float z2 = thisHeight / height * .6F;

        float zoom = Math.min(z1, z2);
        zoom = zoom * this.getScale();
        zoom = Math.max(1F, zoom);

        if ((Math.abs(zoom - getScale()) / zoom) > .1) {
            float[] coordinates = new float[]{hv.cropRect.centerX(), hv.cropRect.centerY()};
            getUnrotatedMatrix().mapPoints(coordinates);
            zoomTo(zoom, coordinates[0], coordinates[1], 300F);
        }

        ensureVisible(hv);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if (drawRectangle) {
            // Note: I assume you have the paint object defined in your class
            canvas.drawRect(beginCoordinate.x, beginCoordinate.y, endCoordinate.x, endCoordinate.y, paint);
            return;
        }


        for (HighlightView mHighlightView : highlightViews) {
            mHighlightView.draw(canvas);
        }
    }

    public void add(HighlightView hv) {

        highlightViews.add(hv);
        invalidate();
    }


}

class Point {
    float x, y;
    float dx, dy;

    @Override
    public String toString() {
        return x + ", " + y;
    }


}
