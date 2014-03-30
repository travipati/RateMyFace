package util;

/**
 * Created by Hephaestus on 3/24/14.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    final static String TAG = "CameraView";

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
                    Log.d(TAG, "Camera " + camera.toString() + " got a picture");
                    if (camera.getParameters().getPreviewFormat() == ImageFormat.NV21) {}
                    else
                        return;

                    Camera.Size size = camera.getParameters().getPreviewSize();

                    YuvImage yuvimage = new YuvImage(bytes, ImageFormat.NV21, size.width, size.height, null);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    yuvimage.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, baos);
                    byte[] jdata = baos.toByteArray();
                    BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
                    bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFatoryOptions);

                    if (bmp == null) {
                        Log.d(TAG, "Null Bitmap created");
                        return;
                    }

                    FaceDetector.Face[] faces = new FaceDetector.Face[1];
                    FaceDetector faceDetector = new FaceDetector(bmp.getWidth(), bmp.getHeight(), 1);
                    faceDetector.findFaces(bmp, faces);
                    int face_count = faceDetector.findFaces(bmp, faces);
                    Log.d("FaceDetector", face_count + " faces found in the image");
                }
            });

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e){
            Log.e(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}