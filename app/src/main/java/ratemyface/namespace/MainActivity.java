package ratemyface.namespace;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.FaceDetector;
import android.os.Bundle;
import android.app.Activity;
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

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null){
            camera.release();
            camera = null;
        }
    }
	
	private void initialize() {
		b = (Button) findViewById(R.id.btToggle);
		b.setOnClickListener(this);
        iv = (ImageView) findViewById(R.id.ivFace);
        rb = (RatingBar) findViewById(R.id.ratingBar);

        camera = getCameraInstance();

		ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
		scheduleTaskExecutor.scheduleAtFixedRate(this, 0, 2, TimeUnit.SECONDS);
	}

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return c;
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
			camera.takePicture(null, null, myPictureCallback);
		}
	}

	PictureCallback myPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {

            InputStream is = new ByteArrayInputStream(arg0);
            Bitmap bmp = BitmapFactory.decodeStream(is);

            if (bmp.getWidth() % 2 == 1)
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth()-1, bmp.getHeight());

            FaceDetector.Face[] faces;
            FaceDetector face_detector = new FaceDetector(bmp.getWidth(), bmp.getHeight(), 5);
            faces = new FaceDetector.Face[5];
            int face_count = face_detector.findFaces(bmp, faces);

            System.out.println(face_count);
            for (int i = 0; i < face_count; i++) {
                bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(bmp);
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                canvas.drawBitmap(bmp, new Matrix(), null);
                PointF midpoint = new PointF();
                faces[i].getMidPoint(midpoint);
                canvas.drawRect(midpoint.x - faces[i].eyesDistance(), midpoint.y - faces[i].eyesDistance(),
                        midpoint.x + faces[i].eyesDistance(), midpoint.y + faces[i].eyesDistance(), paint);
            }

            iv.setImageBitmap(bmp);
		}
	};

}