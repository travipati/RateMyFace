package util;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by Hephaestus on 3/30/14.
 */
public class SpeechRecognizer {

    private final String TAG = "SpeechRecognizer";
    private Context context;
    private MediaRecorder recorder;
    private File baseDir;

    public SpeechRecognizer(Context ctx){
        context = ctx;
        baseDir = context.getCacheDir();
    }

    public void prepare() throws IOException {
        String fileName = "speechRecording" + getRecordingIndex();

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setOutputFile(baseDir + File.separator + fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.prepare();
    }

    public void start(){


        recorder.start();
    }

    private int getRecordingIndex(){

        for (String file : context.fileList()){
            Log.d(TAG, file);
        }
        return 1;
    }
}
