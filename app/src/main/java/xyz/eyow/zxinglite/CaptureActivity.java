package xyz.eyow.zxinglite;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.zxing.Result;
import com.google.zxing.lite.camera.CameraManager;
import com.google.zxing.lite.camera.open.OpenCamera;
import com.google.zxing.lite.decoding.CaptureActivityHandler;
import com.google.zxing.lite.utils.InactivityTimer;
import com.google.zxing.lite.view.IViewfinder;
import java.io.IOException;

/**
 * This activity opens the camera and does the actual scanning on a background thread.
 *
 * @author eyow
 */

public class CaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback, IViewfinder {
    private static final String TAG = "CaptureActivity";
    private CaptureActivityHandler handler;
    private InactivityTimer inactivityTimer;
    private OpenCamera camera;
    private boolean hasSurface;


    public Handler getHandler() {
        return handler;
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_capture);
        CameraManager.init(getApplication());
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        handler = null;
        inactivityTimer.onResume();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }


    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        CameraManager.get().closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }


    /**
     *
     * @param view
     */
    public void turnHandler(View view) {
        if (camera == null) {
            camera = CameraManager.get().getCamera();
        }
        camera.getCamera().startPreview();
        Camera.Parameters parameters = camera.getCamera().getParameters();
        //      判断闪光灯当前状态
        if (Camera.Parameters.FLASH_MODE_OFF.equals(parameters.getFlashMode())) {
            turnOn(parameters);
        } else if (Camera.Parameters.FLASH_MODE_TORCH.equals(parameters.getFlashMode())) {
            turnOff(parameters);
        }
    }


    /**
     *
     * @param parameters
     */
    private void turnOn(Camera.Parameters parameters) {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.getCamera().setParameters(parameters);
    }


    /**
     *
     * @param parameters
     */
    private void turnOff(Camera.Parameters parameters) {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.getCamera().setParameters(parameters);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     */

    @Override public void handleDecode(Result rawResult, Bundle bundle) {
        inactivityTimer.onActivity();

        Toast.makeText(getApplicationContext(),
                getResources().getString(R.string.msg_bulk_mode_scanned) + " (" + rawResult.getText() + ')',
                Toast.LENGTH_SHORT).show();
    }


    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (CameraManager.get().isOpen()) {
            return;
        }
        try {
            CameraManager.get().openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, this, null, null, null);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }
}
