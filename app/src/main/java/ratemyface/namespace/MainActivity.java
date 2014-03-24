package ratemyface.namespace;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio.Media;
import android.app.Activity;
import android.content.ContentValues;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;

public class MainActivity extends Activity implements OnClickListener, Runnable {

	Camera camera;
	Button b;
	ImageView iv;
    RatingBar rb;
	boolean isCapturing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initialize();
	}
	
	private void initialize() {
        rb = (RatingBar) findViewById(R.id.ratingBar);
        iv = (ImageView) findViewById(R.id.ivFace);
		b = (Button) findViewById(R.id.btToggle);
		b.setOnClickListener(this);
		
		camera = Camera.open();
//		camera.setFaceDetectionListener(faceDetectionListener);
		
		ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
		scheduleTaskExecutor.scheduleAtFixedRate(this, 0, 2, TimeUnit.SECONDS);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btToggle:
			if (isCapturing) {
				isCapturing = false;
				b.setText("Start");
			} else {
				isCapturing = true;
				b.setText("Stop");
			}
			break;
		}
	}

	@Override
	public void run() {
		if (isCapturing) {
			camera.takePicture(myShutterCallback, myPictureCallback_RAW, myPictureCallback_JPG);
		}
	}

	FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			if (faces.length == 1) {
                rb.setNumStars(0);
			}
		}
	};
	
	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean arg0, Camera arg1) {
			b.setEnabled(true);
		}
	};
	
	ShutterCallback myShutterCallback = new ShutterCallback() {	
		@Override
		public void onShutter() {}
	};
	
	PictureCallback myPictureCallback_RAW = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {}
	};
	
	PictureCallback myPictureCallback_JPG = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {
			
			InputStream is = new ByteArrayInputStream(arg0);
			Bitmap bmp = BitmapFactory.decodeStream(is);
			iv.setImageBitmap(bmp);
			
/*			Uri uriTarget = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, new ContentValues());

			OutputStream imageFileOS;
			try {
				imageFileOS = getContentResolver().openOutputStream(uriTarget);
				imageFileOS.write(arg0);
				imageFileOS.flush();
				imageFileOS.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}  */
			
			camera.startPreview();
//			camera.startFaceDetection();
		}
	};

}