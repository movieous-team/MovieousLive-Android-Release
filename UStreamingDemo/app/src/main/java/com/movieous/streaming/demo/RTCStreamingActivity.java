package com.movieous.streaming.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import com.movieous.streaming.demo.utils.RtcUtils;
import com.movieous.streaming.demo.utils.ToastUtils;
import com.vender.agora.rtc.UserInfo;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.live.LiveTranscoding;
import io.agora.rtc.mediaio.IVideoFrameConsumer;
import io.agora.rtc.mediaio.IVideoSource;
import io.agora.rtc.mediaio.MediaIO;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Base activity enabling sub activities to communicate using
 * remote video calls.
 */
public class RTCStreamingActivity extends StreamingActivity {
    private static final String TAG = "RTCStreamingActivity";

    private IVideoFrameConsumer mIVideoFrameConsumer;
    private boolean mVideoFrameConsumerReady;
    private boolean mIsRtcStreaming = false; // 是否进入连麦, 连麦模式下，本地停止推流，转为由 RTC 推流，退出连麦模式，本地开始推流

    private FrameLayout mSmallViewContainer;
    private SurfaceView mBigView;

    private RtcEngine mRtcEngine;
    private String mChannelName = "movieous"; // TODO 房间号
    private LiveTranscoding mLiveTranscoding;
    private Map<Integer, UserInfo> mUserInfo = new HashMap<>();
    private int mBigUserId = 0;

    public void onCLickStartRtcStreaming(View view) {
        joinChannel();
    }

    public void onClickStopRtcStreaming(View view) {
        mIsRtcStreaming = false;
        removeStreamingUrl(mStreamingUrl);
        mRtcEngine.enableLocalAudio(false);
        leaveChannel();
        startAudioCapture();
        startStreaming();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mIsRtcStreamingEnabled = true;
        super.onCreate(savedInstanceState);
        initView();
        initRtcEngine();
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyEngine();
    }

    @Override
    protected void startStreaming() {
        mBtnStartStreaming.setEnabled(false);
        joinChannel();
        mBtnStopStreaming.setEnabled(true);
    }

    @Override
    protected void stopStreaming() {
        mBtnStopStreaming.setEnabled(false);
        leaveChannel();
        mBtnStartStreaming.setEnabled(true);
    }

    @Override
    protected void switchCamera() {
        setRtcVideoSource();
        super.switchCamera();
    }

    private void initView() {
        mSmallViewContainer = findViewById(R.id.small_video_view_container);
        mSmallViewContainer.setVisibility(View.VISIBLE);
        mBigView = mPreview;
    }

    private void sendMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "recv msg: " + msg + ", time = " + System.currentTimeMillis());
                ToastUtils.s(RTCStreamingActivity.this, msg);
            }
        });
    }

    // AgoraIO start
    private void initRtcEngine() {
        try {
            mRtcEngine = RtcEngine.create(getApplicationContext(), Constant.AGORA_APPID, mRtcEngineEventHandler);
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
            mRtcEngine.enableVideo();
            mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x480, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15, VideoEncoderConfiguration.STANDARD_BITRATE, VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
            initTranscoding(mVideoEncodeParam.getWidth(), mVideoEncodeParam.getHeight(), mVideoEncodeParam.getBitrate() / 1000, mVideoEncodeParam.getFps());
            setTranscoding();
            setRtcVideoSource();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTranscoding(int width, int height, int bitrate, int fps) {
        if (mLiveTranscoding == null) {
            mLiveTranscoding = new LiveTranscoding();
            mLiveTranscoding.width = width;
            mLiveTranscoding.height = height;
            mLiveTranscoding.videoBitrate = bitrate;
            // if you want high fps, modify videoFramerate
            mLiveTranscoding.videoFramerate = fps;
            mLiveTranscoding.videoCodecProfile = LiveTranscoding.VideoCodecProfileType.BASELINE;
        }
    }

    private void setTranscoding() {
        ArrayList<LiveTranscoding.TranscodingUser> transcodingUsers;
        ArrayList<UserInfo> videoUsers = RtcUtils.getAllVideoUser(mUserInfo);

        transcodingUsers = RtcUtils.cdnLayout(mBigUserId, videoUsers, mLiveTranscoding.width, mLiveTranscoding.height);

        mLiveTranscoding.setUsers(transcodingUsers);
        mLiveTranscoding.userCount = transcodingUsers.size();
        mRtcEngine.setLiveTranscoding(mLiveTranscoding);
    }

    private void setRtcVideoSource() {
        mRtcEngine.setVideoSource(getRtcVideoSource());
    }

    private void joinChannel() {
        setRtcVideoSource();
        Log.i(TAG, "start joinChannel");
        mRtcEngine.joinChannel(null, mChannelName, "", mBigUserId);
    }

    private void leaveChannel() {
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }
    }

    private void addStreamingUrl(String rtmpUrl) {
        Log.i(TAG, "addStreamingUrl: " + rtmpUrl);
        setTranscoding();
        mRtcEngine.addPublishStreamUrl(rtmpUrl, true);
    }

    private void removeStreamingUrl(String rtmpUrl) {
        mRtcEngine.removePublishStreamUrl(rtmpUrl);
    }

    private void destroyEngine() {
        if (mRtcEngine != null) {
            mRtcEngine.removePublishStreamUrl(mStreamingUrl);
            mRtcEngine.leaveChannel();
        }
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    private IVideoSource getRtcVideoSource() {
        IVideoSource rtcVideoSource = new IVideoSource() {
            @Override
            public boolean onInitialize(IVideoFrameConsumer iVideoFrameConsumer) {
                mIVideoFrameConsumer = iVideoFrameConsumer;
                return true;
            }

            @Override
            public boolean onStart() {
                mVideoFrameConsumerReady = true;
                return true;
            }

            @Override
            public void onStop() {
                mVideoFrameConsumerReady = false;
            }

            @Override
            public void onDispose() {
                mVideoFrameConsumerReady = false;
            }

            @Override
            public int getBufferType() {
                // Different PixelFormat should use different BufferType
                // If you choose TEXTURE_2D/TEXTURE_OES, you should use BufferType.TEXTURE
                return MediaIO.BufferType.TEXTURE.intValue();
            }
        };
        return rtcVideoSource;
    }

    IRtcEngineEventHandler mRtcEngineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onError(int errorCode) {
            super.onError(errorCode);
            sendMsg("-->onError<--" + errorCode);
        }

        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
            sendMsg("-->onWarning<--" + warn);
        }

        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            sendMsg("-->onJoinChannelSuccess<--" + channel + "  -->uid<--" + uid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBigUserId = uid;
                    UserInfo mUI = new UserInfo();
                    mUI.view = mBigView;
                    mUI.uid = uid;
                    mUI.view.setZOrderOnTop(true);
                    mUserInfo.put(uid, mUI);

                    stopAudioCapture();
                    mRtcEngine.enableLocalAudio(true);
                    mIsRtcStreaming = true;
                    addStreamingUrl(mStreamingUrl);
                }
            });
        }

        @Override
        public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
            super.onFirstLocalVideoFrame(width, height, elapsed);
            sendMsg("-->onFirstLocalVideoFrame<--");
        }

        @Override
        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onRejoinChannelSuccess(channel, uid, elapsed);
            sendMsg(uid + " -->RejoinChannel<--");
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtStreamingStateView.setText("LEAVE CHANNEL");
                }
            });
            sendMsg("-->leaveChannel<--");
        }

        @Override
        public void onConnectionInterrupted() {
            super.onConnectionInterrupted();
            sendMsg("-->onConnectionInterrupted<--");
        }

        @Override
        public void onConnectionLost() {
            super.onConnectionLost();
            sendMsg("-->onConnectionLost<--");
        }

        @Override
        public void onStreamPublished(String url, final int error) {
            super.onStreamPublished(url, error);
            sendMsg("-->onStreamUrlPublished<--" + url + " -->error code<--" + error);
            if (error == Constants.ERR_OK) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTxtStreamingStateView.setText("STREAMING");
                        // For debug
                        // stopStreaming();
                    }
                });
            }
        }

        @Override
        public void onStreamUnpublished(String url) {
            super.onStreamUnpublished(url);
            sendMsg("-->onStreamUrlUnpublished<--" + url);
        }

        @Override
        public void onTranscodingUpdated() {
            super.onTranscodingUpdated();
        }

        @Override
        public void onUserJoined(final int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            sendMsg("-->onUserJoined<--" + uid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UserInfo mUI = new UserInfo();
                    mUI.view = RtcEngine.CreateRendererView(RTCStreamingActivity.this);
                    mUI.uid = uid;
                    mUI.view.setZOrderOnTop(true);
                    mUserInfo.put(uid, mUI);
                    mSmallViewContainer.addView(mUI.view);
                    mRtcEngine.setupRemoteVideo(new VideoCanvas(mUI.view, Constants.RENDER_MODE_HIDDEN, uid));
                    setTranscoding();
                }
            });
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            super.onUserOffline(uid, reason);
            sendMsg("-->onUserOffline<-- uid = " + uid + ", reason = " + reason);
            mUserInfo.remove(uid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setTranscoding();
                }
            });
        }
    };
    // AgoraIO stop

    // 纹理回调
    @Override
    public int onDrawFrame(int texId, int texWidth, int texHeight, long timestampNs, float[] transformMatrix) {
        // FU 美颜处理
        int outTexId = super.onDrawFrame(texId, texWidth, texHeight, timestampNs, transformMatrix);
        // 处理后的纹理送一份进行连麦
        if (mVideoFrameConsumerReady && mIsRtcStreaming) {
            mIVideoFrameConsumer.consumeTextureFrame(outTexId, MediaIO.PixelFormat.TEXTURE_2D.intValue(), texWidth, texHeight, 0, timestampNs / 1000000, transformMatrix);
        }
        // 返回处理后的纹理进行本地预览和非连麦状态下推流
        return outTexId;
    }
}
