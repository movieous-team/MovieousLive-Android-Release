package com.movieous.streaming.demo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kiwi.ui.KwControlView;
import com.movieous.capture.UAudioFrameListener;
import com.movieous.capture.UCameraParam;
import com.movieous.capture.UCameraFocusListener;
import com.movieous.capture.UMicrophoneParam;
import com.movieous.codec.UAudioEncodeParam;
import com.movieous.codec.UVideoEncodeParam;
import com.movieous.filter.UVideoFrameListener;
import com.movieous.filter.UWatermarkParam;
import com.movieous.streaming.UStatisticsInfoListener;
import com.movieous.streaming.UStreamingManager;
import com.movieous.streaming.UStreamingState;
import com.movieous.streaming.UStreamingStateListener;
import com.movieous.streaming.demo.kiwi.KwTrackerWrapper;
import com.movieous.streaming.demo.utils.StreamingParams;
import com.movieous.streaming.demo.view.FocusIndicator;

public class StreamingActivity extends AppCompatActivity implements UVideoFrameListener, UAudioFrameListener,
        UStreamingStateListener, UStatisticsInfoListener, UCameraFocusListener {
    private static final String TAG = "StreamingActivity";

    public static final String PREVIEW_SIZE_RATIO = "PreviewSizeRatio";
    public static final String PREVIEW_SIZE_LEVEL = "PreviewSizeLevel";
    public static final String ENCODING_SIZE_LEVEL = "EncodingSizeLevel";
    public static final String ENCODING_BITRATE_LEVEL = "EncodingBitrateLevel";
    public static final String STREAMING_URL = "StreamingUrl";

    private UCameraParam mCameraParam;
    private UMicrophoneParam mMicrophoneParam;
    private UVideoEncodeParam mVideoEncodeParam;
    private UAudioEncodeParam mAudioEncodeParam;
    private UWatermarkParam mWatermarkParam;
    private FocusIndicator mFocusIndicator;
    private GestureDetector mGestureDetector;
    private int mFocusIndicatorX;
    private int mFocusIndicatorY;

    private Button mBtnStartStreaming;
    private Button mBtnSwitchCamera;
    private Button mBtnStopStreaming;
    private Button mBtnSwitchPictureStreaming;
    private TextView mTxtStreamingStateView;
    private TextView mTxtStaticsView;
    private GLSurfaceView mPreview;

    private Handler mHandler = new Handler();
    private UStreamingManager mStreamingManager;
    private String mStreamingUrl;
    private boolean mIsRestreamingEnabled = false;

    protected KwTrackerWrapper mKwTrackWrapper;
    protected KwControlView mKwControlView;
    protected int mSurfaceWidth;
    protected int mSurfaceHeight;
    private boolean mIsKwOnSurfaceCreatedInvoked;
    private boolean mIsKwOnSurfaceChangedInvoked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_streaming);

        // response screen rotation event
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        mStreamingUrl = getIntent().getStringExtra(STREAMING_URL);

        mBtnStartStreaming = findViewById(R.id.start_streaming);
        mBtnSwitchCamera = findViewById(R.id.switch_camera);
        mBtnStopStreaming = findViewById(R.id.stop_streaming);
        mBtnSwitchPictureStreaming = findViewById(R.id.switch_picture_streaming);
        mTxtStreamingStateView = findViewById(R.id.streaming_state);
        mTxtStaticsView = findViewById(R.id.streaming_statics);
        mFocusIndicator = findViewById(R.id.focus_indicator);

        mBtnStartStreaming.setEnabled(false);
        mBtnStartStreaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStreaming();
            }
        });

        mBtnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStreamingManager.switchCamera();
                mFocusIndicator.focusCancel();
            }
        });

        mBtnStopStreaming.setEnabled(false);
        mBtnStopStreaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopStreaming();
            }
        });

        mBtnSwitchPictureStreaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStreamingManager.switchPictureStreaming();
            }
        });

        mPreview = findViewById(R.id.preview);

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mStreamingManager == null) {
                    return false;
                }
                mFocusIndicatorX = (int) e.getX() - mFocusIndicator.getWidth() / 2;
                mFocusIndicatorY = (int) e.getY() - mFocusIndicator.getHeight() / 2;
                mStreamingManager.manualFocus(mFocusIndicator.getWidth(), mFocusIndicator.getHeight(), (int) e.getX(), (int) e.getY());
                return false;
            }
        });

        mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        // kiwi
        findViewById(R.id.builtin_beauty).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() != null) {
                    mKwControlView.setVisibility(View.INVISIBLE);
                    v.setTag(null);
                } else {
                    mKwControlView.setVisibility(View.VISIBLE);
                    v.setTag(1);
                }

            }
        });
        mKwTrackWrapper = new KwTrackerWrapper(this);
        mKwTrackWrapper.onCreate(this);
        mKwControlView = findViewById(R.id.kiwi_control_layout);
        mKwControlView.setOnEventListener(mKwTrackWrapper.initUIEventListener());

        initStreamingManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mKwTrackWrapper.onResume(this);
        mStreamingManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mKwTrackWrapper.onPause(this);
        mStreamingManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mKwTrackWrapper.onDestroy(this);
        mHandler.removeCallbacksAndMessages(null);
        stopStreaming();
        mStreamingManager.destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private UCameraParam.CAMERA_FACING_ID chooseCameraFacingId() {
        if (UCameraParam.hasCameraFacing(UCameraParam.CAMERA_FACING_ID.THIRD)) {
            return UCameraParam.CAMERA_FACING_ID.THIRD;
        } else if (UCameraParam.hasCameraFacing(UCameraParam.CAMERA_FACING_ID.FRONT)) {
            return UCameraParam.CAMERA_FACING_ID.FRONT;
        } else {
            return UCameraParam.CAMERA_FACING_ID.BACK;
        }
    }

    private void startStreaming() {
        mBtnStartStreaming.setEnabled(false);
        mStreamingManager.startPublish(mStreamingUrl);
        mBtnStopStreaming.setEnabled(true);
    }

    private void stopStreaming() {
        mBtnStopStreaming.setEnabled(false);
        mHandler.removeCallbacksAndMessages(null);
        mStreamingManager.stopPublish();
        mBtnStartStreaming.setEnabled(true);
    }

    private void initStreamingManager() {
        int previewSizeRatioPos = getIntent().getIntExtra(PREVIEW_SIZE_RATIO, 1);
        int previewSizeLevelPos = getIntent().getIntExtra(PREVIEW_SIZE_LEVEL, 2);
        int encodingSizeLevelPos = getIntent().getIntExtra(ENCODING_SIZE_LEVEL, 9);
        int encodingBitrateLevelPos = getIntent().getIntExtra(ENCODING_BITRATE_LEVEL, 3);
        int audioChannelNumPos = 0;
        // 摄像头采集参数设置
        UCameraParam.CAMERA_FACING_ID facingId = chooseCameraFacingId();
        mCameraParam = new UCameraParam();
        mCameraParam.setCameraId(facingId);
        mCameraParam.setCameraPreviewSizeRatio(StreamingParams.PREVIEW_SIZE_RATIO[previewSizeRatioPos]);
        mCameraParam.setCameraPreviewSizeLevel(StreamingParams.PREVIEW_SIZE_LEVEL[previewSizeLevelPos]);
        // 麦克风参数设置
        mMicrophoneParam = new UMicrophoneParam();
        mMicrophoneParam.setChannelConfig(AudioFormat.CHANNEL_IN_MONO);
        // 视频编码参数设置
        mVideoEncodeParam = new UVideoEncodeParam(this);
        mVideoEncodeParam.setEncodeSizeLevel(StreamingParams.ENCODING_SIZE_LEVEL[encodingSizeLevelPos]);
        mVideoEncodeParam.setEncodeBitrate(StreamingParams.ENCODING_BITRATE_LEVEL[encodingBitrateLevelPos]);
        // 音频编码参数设置
        mAudioEncodeParam = new UAudioEncodeParam();
        mAudioEncodeParam.setChannels(StreamingParams.AUDIO_CHANNEL_NUM[audioChannelNumPos]);
        // 推流核心类
        mStreamingManager = new UStreamingManager();
        mStreamingManager.setStreamingStateListener(StreamingActivity.this);
        mStreamingManager.setVideoFrameListener(StreamingActivity.this);
        mStreamingManager.setAudioFrameListener(StreamingActivity.this);
        mStreamingManager.setStatisticsInfoListener(StreamingActivity.this);
        mStreamingManager.setFocusListener(StreamingActivity.this);
        mStreamingManager.prepare(mPreview, mCameraParam, mMicrophoneParam, mVideoEncodeParam, mAudioEncodeParam, null);
        // 图片推流
        mStreamingManager.setPictureStreamingResourceId(R.drawable.pause_publish);
        // 水印演示
        mWatermarkParam = new UWatermarkParam();
        mWatermarkParam.setPosition(0.8f, 0.1f);
        mWatermarkParam.setAlpha(128);
        mWatermarkParam.setResourceId(R.drawable.movieous);
        mStreamingManager.setWatermark(mWatermarkParam);
    }

    private void reStreaming(long delayMills) {
        mIsRestreamingEnabled = false;
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mStreamingManager.startPublish(mStreamingUrl);
            }
        }, delayMills);
    }

    /**
     * Called when the surface is created or recreated.
     * To be called in {@link GLSurfaceView.Renderer#onSurfaceCreated}
     * invoked.
     **/
    @Override
    public void onSurfaceCreated() {
        mKwTrackWrapper.onSurfaceCreated(this);
    }

    /**
     * To be called in {@link GLSurfaceView.Renderer#onSurfaceChanged}.
     *
     * @param width  the width of the Surface
     * @param height the height of the Surface
     **/
    @Override
    public void onSurfaceChanged(int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
    }

    /**
     * Called after surface destroyed
     **/
    @Override
    public void onSurfaceDestroy() {
        mKwTrackWrapper.onSurfaceDestroyed();
    }

    /**
     * To be called in {@link GLSurfaceView.Renderer}.
     *
     * @param texId           the texture ID of Camera SurfaceTexture object to be rendered.
     * @param texWidth        width of the drawing surface in pixels.
     * @param texHeight       height of the drawing surface in pixels.
     * @param timestampNs     timestamp of this frame in Ns.
     * @param transformMatrix when NOT specify the callback texture to be OES, it will be Identity matrix.
     * @return the texture ID of the newly generated texture to
     * be assigned to the SurfaceTexture object.
     **/
    @Override
    public int onDrawFrame(int texId, int texWidth, int texHeight, long timestampNs, float[] transformMatrix) {
        if (mKwTrackWrapper == null) return texId;
        if (!mIsKwOnSurfaceChangedInvoked) {
            mIsKwOnSurfaceChangedInvoked = true;
            mKwTrackWrapper.onSurfaceChanged(mSurfaceWidth, mSurfaceHeight, texWidth, texHeight);
        }
        return mKwTrackWrapper.onDrawFrame(texId, texWidth, texHeight);
    }

    /**
     * Record audio frame failed
     *
     * @param errorCode the error code
     */
    @Override
    public void onAudioRecordFailed(int errorCode) {

    }

    /**
     * The audio frame available
     *
     * @param data        the pcm data
     * @param timestampNs the timestampNs
     */
    @Override
    public void onAudioFrameAvailable(byte[] data, long timestampNs) {

    }

    /**
     * Invoked if the {@link UStreamingState} changed
     *
     * @param state the specified {@link UStreamingState}
     * @param extra the extra information
     */
    @Override
    public void onStateChanged(UStreamingState state, Object extra) {
        Log.i(TAG, "onStateChanged state:" + state);
        switch (state) {
            case READY:
                mTxtStreamingStateView.setText("READY");
                startStreaming();
                break;
            case CONNECTING:
                mTxtStreamingStateView.setText("CONNECTING");
                break;
            case CONNECTED:
                mTxtStreamingStateView.setText("CONNECTED");
                break;
            case STREAMING:
                mTxtStreamingStateView.setText("STREAMING");
                break;
            case SHUTDOWN:
                mTxtStreamingStateView.setText("SHUTDOWN");
                break;
            case DISCONNECTED:
                mTxtStreamingStateView.setText(mIsRestreamingEnabled ? "RESTREAMING" : "DISCONNECTED");
                if (mIsRestreamingEnabled) {
                    reStreaming(2000);
                }
                break;
            case UNAUTHORIZED_STREAMING_URL:
                mTxtStreamingStateView.setText("UNAUTHORIZED");
                break;
            case INVALID_STREAMING_URL:
                mTxtStreamingStateView.setText("INVALID_STREAMING_URL");
                break;
            case ERROR:
                mTxtStreamingStateView.setText("ERROR");
                mIsRestreamingEnabled = true;
                stopStreaming();
                break;
        }
    }

    /**
     * Callback fired once video statistics info arrived.
     *
     * @param fps
     * @param bitrate
     */
    @Override
    public void onVideoInfoChanged(double fps, double bitrate) {
        mTxtStaticsView.setText("fps: " + (int) fps + "\n" + "bitrate: " + (int) bitrate / 1000);
    }

    /**
     * Callback fired once audio bitrate arrived.
     *
     * @param bitrate
     */
    @Override
    public void onAudioBitrateChanged(double bitrate) {
        Log.d(TAG, "audio bitrate: " + (int) bitrate / 1000 + " kbps");
    }

    /**
     * result of if manual focus start success.
     *
     * @param result
     */
    @Override
    public void onManualFocusStart(boolean result) {
        if (result) {
            Log.i(TAG, "manual focus begin success");
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFocusIndicator.getLayoutParams();
            lp.leftMargin = mFocusIndicatorX;
            lp.topMargin = mFocusIndicatorY;
            mFocusIndicator.setLayoutParams(lp);
            mFocusIndicator.focus();
        } else {
            mFocusIndicator.focusCancel();
            Log.i(TAG, "manual focus not supported");
        }
    }

    /**
     * result of if manual focus success.
     *
     * @param result
     */
    @Override
    public void onManualFocusStop(boolean result) {
        Log.i(TAG, "manual focus end result: " + result);
        if (result) {
            mFocusIndicator.focusSuccess();
        } else {
            mFocusIndicator.focusFail();
        }
    }

    /**
     * ongoing manual focus canceled, because another manual focus triggered.
     */
    @Override
    public void onManualFocusCancel() {
        Log.i(TAG, "manual focus canceled");
        mFocusIndicator.focusCancel();
    }

    /**
     * continuous auto focus start
     * only trigger when selected focus mode as FOCUS_MODE_CONTINUOUS_VIDEO or FOCUS_MODE_CONTINUOUS_PICTURE
     */
    @Override
    public void onAutoFocusStart() {
        Log.i(TAG, "auto focus start");
    }

    /**
     * continuous auto focus stop
     * only trigger when selected focus mode as FOCUS_MODE_CONTINUOUS_VIDEO or FOCUS_MODE_CONTINUOUS_PICTURE
     */
    @Override
    public void onAutoFocusStop() {
        Log.i(TAG, "auto focus stop");
    }
}
