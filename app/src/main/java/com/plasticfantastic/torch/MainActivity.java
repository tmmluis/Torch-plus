package com.plasticfantastic.torch;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivityDebug";

    private long mIntermittency = 600;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mBuilder;
    private CameraCaptureSession mSession;
    private SurfaceTexture mSurfaceTexture;
    private Thread mThread;
    private NumberPicker mFlasherPicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFlasherPicker = (NumberPicker) findViewById(R.id.numberPicker);
        mFlasherPicker.setVisibility(View.INVISIBLE);
        mFlasherPicker.setMinValue(1);
        mFlasherPicker.setMaxValue(6);

        mFlasherPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                switch (newVal) {
                    case 1:
                        mIntermittency = 600;
                        break;
                    case 2:
                        mIntermittency = 500;
                        break;
                    case 3:
                        mIntermittency = 400;
                        break;
                    case 4:
                        mIntermittency = 300;
                        break;
                    case 5:
                        mIntermittency = 200;
                        break;
                    case 6:
                        mIntermittency = 100;
                        break;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startCamera();

        // Resetting the radiogroup to "off" mode
        RadioButton b = (RadioButton) findViewById(R.id.button_off);
        b.setChecked(true);
        // Setting number picker invisible
        mFlasherPicker.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mThread != null) {
            Thread moribund = mThread;
            mThread = null;
            moribund.interrupt();
        }
        close();
    }

    public void onRadioButtonClicked(View view) {
        // First we check if there is a running thread using the flash. If that is the case
        // we set it to null an interrupt it.
        if (mThread != null) {
            Thread moribund = mThread;
            mThread = null;
            moribund.interrupt();
        }

        mFlasherPicker.setVisibility(View.INVISIBLE);

        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        try {
            switch (view.getId()) {
                case R.id.button_off:
                    if (checked) {
                        mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                        mSession.capture(mBuilder.build(), null, null);
                    }
                    break;
                case R.id.button_torch:
                    if (checked) {
                        mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                        mSession.capture(mBuilder.build(), null, null);
                    }
                    break;
                case R.id.button_flasher:
                    if (checked) {
                        mFlasherPicker.setVisibility(View.VISIBLE);
                        mThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (mThread == Thread.currentThread()) {
                                        mBuilder.set(CaptureRequest.FLASH_MODE,
                                                CameraMetadata.FLASH_MODE_TORCH);
                                        mSession.capture(mBuilder.build(), null, null);
                                        Thread.sleep(mIntermittency);
                                        mBuilder.set(CaptureRequest.FLASH_MODE,
                                                CameraMetadata.FLASH_MODE_OFF);
                                        mSession.capture(mBuilder.build(), null, null);
                                        Thread.sleep(mIntermittency);
                                    }
                                } catch (InterruptedException | CameraAccessException e) {
                                    Log.d(TAG, "Flasher interrupted");
                                }
                            }
                        });
                        mThread.start();
                    }
                    break;
                case R.id.button_sos:
                    if (checked) {
                        mThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (mThread == Thread.currentThread()) {

                                        playS();
                                        playO();
                                        playS();
                                        Thread.sleep(1050);
                                    }
                                } catch (InterruptedException | CameraAccessException e) {
                                    Log.d(TAG, "SOS interrupted");
                                }
                            }
                        });
                        mThread.start();
                    }
                    break;
                case R.id.button_strobe:
                    if (checked) {
                        mThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (mThread == Thread.currentThread()) {
                                        mBuilder.set(CaptureRequest.FLASH_MODE,
                                                CameraMetadata.FLASH_MODE_TORCH);
                                        mSession.capture(mBuilder.build(), null, null);
                                        Thread.sleep(65);
                                        mBuilder.set(CaptureRequest.FLASH_MODE,
                                                CameraMetadata.FLASH_MODE_OFF);
                                        mSession.capture(mBuilder.build(), null, null);
                                        Thread.sleep(65);

                                    }
                                } catch (InterruptedException | CameraAccessException e) {
                                    Log.d(TAG, "Strobe interrupted");
                                }
                            }
                        });
                        mThread.start();
                    }
                    break;
            }
        } catch (CameraAccessException e) {
            RadioButton b = (RadioButton) findViewById(R.id.button_off);
            b.setChecked(true);
            e.printStackTrace();
        }
    }

    /** utility method for playing the "S" character in morse code */
    private void playS() throws CameraAccessException, InterruptedException {
        for (int i = 0; i < 3; i++) {
            mBuilder.set(CaptureRequest.FLASH_MODE,
                    CameraMetadata.FLASH_MODE_TORCH);
            mSession.capture(mBuilder.build(), null, null);
            Thread.sleep(150);
            mBuilder.set(CaptureRequest.FLASH_MODE,
                    CameraMetadata.FLASH_MODE_OFF);
            mSession.capture(mBuilder.build(), null, null);
            Thread.sleep(150);
        }
    }

    /** utility method for playing the "O" character in morse code */
    private void playO() throws CameraAccessException, InterruptedException {
        for (int i = 0; i < 3; i++) {
            mBuilder.set(CaptureRequest.FLASH_MODE,
                    CameraMetadata.FLASH_MODE_TORCH);
            mSession.capture(mBuilder.build(), null, null);
            Thread.sleep(450);
            mBuilder.set(CaptureRequest.FLASH_MODE,
                    CameraMetadata.FLASH_MODE_OFF);
            mSession.capture(mBuilder.build(), null, null);
            Thread.sleep(150);
        }
    }

    /** Attempts to open an existing system camera */
    private void startCamera() {
        mCameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        String cameraId = getCameraId();

        if (cameraId != null && !cameraId.isEmpty()) {
            Log.d(TAG, "Found one camera with flash! Id = " + cameraId);
            try {
                mCameraManager.openCamera(cameraId, new MyCameraDeviceStateCallback(), null);
            } catch (CameraAccessException e) {
                Log.d(TAG, "CameraAccessException: Failed to open the camera");
            }
        } else {
            Log.d(TAG, "No camera found!");
        }
    }

    /** Searches for the system for a backfacing camera with a flash and returns its id */
    private String getCameraId() {
        String cameraId = "";
        try {
            for (String id : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);
                // We don't want a front facing camera.
                if (characteristics.get(CameraCharacteristics.LENS_FACING)
                        == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                    cameraId = id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return cameraId;
    }

    class MyCameraDeviceStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG, "onOpened: camera device opened");
            mCameraDevice = camera;
            try {
                mBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                Log.d(TAG, "onOpened: Capture request created");
                mBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                Log.d(TAG, "onOpened: Capture request set to AE_MODE");

                mSurfaceTexture = new SurfaceTexture(1);
                Size size = getSmallestSize(mCameraDevice.getId());
                mSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
                Surface surface = new Surface(mSurfaceTexture);

                List<Surface> surfaceList = new ArrayList<>();
                surfaceList.add(surface);
                mBuilder.addTarget(surface);

                mCameraDevice.createCaptureSession(surfaceList, new MyCameraCaptureSessionStateCallback(), null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d(TAG, "onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.d(TAG, "onError");
        }
    }

    class MyCameraCaptureSessionStateCallback extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            Log.d(TAG, "onConfigured: Capture session created");
            mSession = session;
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Log.d(TAG, "onConfigureFailed");
        }
    }

    private Size getSmallestSize(String cameraId) throws CameraAccessException {
        Size[] outputSizes = mCameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                .getOutputSizes(SurfaceTexture.class);
        if (outputSizes == null || outputSizes.length == 0) {
            throw new IllegalStateException(
                    "Camera " + cameraId + "doesn't support any outputSize.");
        }
        Size chosen = outputSizes[0];
        for (Size s : outputSizes) {
            if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
                chosen = s;
            }
        }
        return chosen;
    }


    private void close() {
        if (mCameraDevice == null || mSession == null) {
            return;
        }
        try {
            mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            mSession.capture(mBuilder.build(), null, null);
            Log.d(TAG, "close(): turning flash off");
        } catch (CameraAccessException e) {
            Log.d(TAG, "close(): Failed to turn flash off");
        }
        mSession.close();
        mSurfaceTexture.release();
        mCameraDevice.close();
        mCameraDevice = null;
        mSession = null;
    }
}