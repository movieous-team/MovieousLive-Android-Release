package com.movieous.streaming.demo.utils;

import com.movieous.codec.UVideoEncodeParam;
import com.movieous.capture.UCameraParam;

public class StreamingParams {

    public static final String[] PREVIEW_SIZE_RATIO_TIPS = {
            "4:3", "16:9"
    };

    public static final String[] PREVIEW_SIZE_LEVEL_TIPS = {
            "240P",
            "360P",
            "480P",
            "720P",
            "960P",
            "1080P",
            "1200P",
    };

    public static final String[] ENCODING_SIZE_LEVEL_TIPS = {
            "240x240",
            "320x240",
            "352x352",
            "640x352",
            "360x360",
            "480x360",
            "640x360",
            "480x480",
            "640x480",
            "848x480",
            "544x544",
            "720x544",
            "720x720",
            "960x720",
            "1280x720",
            "1088x1088",
            "1440x1088"
    };

    public static final String[] ENCODING_BITRATE_LEVEL_TIPS = {
            "500Kbps",
            "800Kbps",
            "1000Kbps",
            "1200Kbps",
            "1600Kbps",
            "2000Kbps",
            "2500Kbps",
            "4000Kbps",
            "8000Kbps"
    };

    public static final UCameraParam.CAMERA_PREVIEW_SIZE_RATIO[] PREVIEW_SIZE_RATIO = {
            UCameraParam.CAMERA_PREVIEW_SIZE_RATIO.RATIO_4_3,
            UCameraParam.CAMERA_PREVIEW_SIZE_RATIO.RATIO_16_9
    };

    public static final UCameraParam.CAMERA_PREVIEW_SIZE_LEVEL[] PREVIEW_SIZE_LEVEL = {
            UCameraParam.CAMERA_PREVIEW_SIZE_LEVEL.SIZE_240P,
            UCameraParam.CAMERA_PREVIEW_SIZE_LEVEL.SIZE_360P,
            UCameraParam.CAMERA_PREVIEW_SIZE_LEVEL.SIZE_480P,
            UCameraParam.CAMERA_PREVIEW_SIZE_LEVEL.SIZE_720P,
            UCameraParam.CAMERA_PREVIEW_SIZE_LEVEL.SIZE_960P,
            UCameraParam.CAMERA_PREVIEW_SIZE_LEVEL.SIZE_1080P,
            UCameraParam.CAMERA_PREVIEW_SIZE_LEVEL.SIZE_1200P,
    };

    public static final UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL[] ENCODING_SIZE_LEVEL = {
            /**
             * 240x240
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_240P_1_1,
            /**
             * 320x240
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_240P_4_3,
            /**
             * 352x352
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_352P_1_1,
            /**
             * 640x352
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_352P_16_9,
            /**
             * 360x360
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_360P_1_1,
            /**
             * 480x360
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_360P_4_3,
            /**
             * 640x360
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_360P_16_9,
            /**
             * 480x480
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_480P_1_1,
            /**
             * 640x480
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_480P_4_3,
            /**
             * 848x480
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_480P_16_9,
            /**
             * 544x544
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_544P_1_1,
            /**
             * 720x544
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_544P_4_3,
            /**
             * 720x720
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_720P_1_1,
            /**
             * 960x720
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_720P_4_3,
            /**
             * 1280x720
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_720P_16_9,
            /**
             * 1088x1088
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_1080P_1_1,
            /**
             * 1440x1088
            */
            UVideoEncodeParam.VIDEO_ENCODE_SIZE_LEVEL.SIZE_1080P_4_3,
    };

    public static final int[] ENCODING_BITRATE_LEVEL = {
            500 * 1000,
            800 * 1000,
            1000 * 1000,
            1200 * 1000,
            1600 * 1000,
            2000 * 1000,
            2500 * 1000,
            4000 * 1000,
            8000 * 1000,
    };

    public static final int[] AUDIO_CHANNEL_NUM = {
            1,
            2,
    };
}
