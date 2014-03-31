package util;

/**
 * Created by Hephaestus on 3/24/14.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    final static String TAG = "CameraView";
    final static String FaceDetectorTAG = "FaceDetector";

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraView(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {/* empty. Take care of releasing the Camera preview in your activity.*/}

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.

        if (mHolder.getSurface() == null)   // preview surface does not exist
            return;

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera camera) {
                    // got a frame
                    if (camera.getParameters().getPreviewFormat() == ImageFormat.NV21) {}
                    else
                        return;

                    Bitmap bmp = getBitmapFromBytes(bytes, camera);
                    if (bmp == null) {
                        Log.d(TAG, "Null Bitmap created");
                        return;
                    }

                    FaceDetector faceDetector = new FaceDetector(bmp.getWidth(), bmp.getHeight(), 1);
                    FaceDetector.Face[] faces = new FaceDetector.Face[1];
                    if(faceDetector.findFaces(bmp, faces) == 0){
                        Log.d(FaceDetectorTAG, "No faces found");
                        return;
                    }

                    for (FaceDetector.Face face : faces) {
                        //filter bad ones
                        if (face.confidence() < FaceDetector.Face.CONFIDENCE_THRESHOLD){
                            Log.d(FaceDetectorTAG, "Face confidence: " + face.confidence() + " filtered out.");
                            return;
                        }

                        // so this is the info we've got to work with out of the api
                        float eyesDistance = face.eyesDistance();
                        PointF killPoint = new PointF();    // this sets the given point to the point on the face
                        face.getMidPoint(killPoint);        // between the eyes, obviously they misnamed the fcn

                        float xRotation, yRotation, zRotation;
                        xRotation = face.pose(FaceDetector.Face.EULER_X);
                        yRotation = face.pose(FaceDetector.Face.EULER_Y);
                        zRotation = face.pose(FaceDetector.Face.EULER_Z);
                    }

                    int face_count = faceDetector.findFaces(bmp, faces);
                    Log.d("FaceDetector", face_count + " faces found in the image");
//                    analyzeImage(bmp);
                }
            });

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e){
            Log.e(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private Bitmap getBitmapFromBytes(byte[] bytes, Camera camera){
        Camera.Size size = camera.getParameters().getPreviewSize();

        YuvImage yuvImage = new YuvImage(bytes, ImageFormat.NV21, size.width, size.height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, baos))
            return null;

        byte[] jdata = baos.toByteArray();
        BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
        bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFatoryOptions);
    }

    private void analyzeImage(Bitmap bmp) {

    }

    private void drawRect() {
//        System.out.println(face_count);
//        for (int i = 0; i < face_count; i++) {
//            bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
//            Canvas canvas = new Canvas(bmp);
//            Paint paint = new Paint();
//            paint.setColor(Color.RED);
//            canvas.drawBitmap(bmp, new Matrix(), null);
//            PointF midpoint = new PointF();
//            faces[i].getMidPoint(midpoint);
//            canvas.drawRect(midpoint.x - faces[i].eyesDistance(), midpoint.y - faces[i].eyesDistance(),
//                    midpoint.x + faces[i].eyesDistance(), midpoint.y + faces[i].eyesDistance(), paint);
//        }
//
//        iv.setImageBitmap(bmp);
    }
}