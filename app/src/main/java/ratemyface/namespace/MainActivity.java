package ratemyface.namespace;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RatingBar;

import util.CameraView;

public class MainActivity extends Activity implements OnClickListener {

	Camera camera;
	Button b;
    RatingBar rb;
	boolean isCapturing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initialize();
	}
	
	private void initialize() {
		b = (Button) findViewById(R.id.btToggle);
		b.setOnClickListener(this);
        rb = (RatingBar) findViewById(R.id.ratingBar);

        camera = getFrontCamera();
        CameraView cv = new CameraView(this, camera);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.ivFace);
        frameLayout.addView(cv);
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

    private Camera getFrontCamera(){
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for(int i = 0; i < Camera.getNumberOfCameras(); i++){
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                return Camera.open(i);
        }

        System.out.println("Front camera not found, opening default camera instead.");
        return Camera.open();
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
}