package com.movieous.streaming.demo.utils;

import com.vender.agora.rtc.UserInfo;
import io.agora.rtc.live.LiveTranscoding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class RtcUtils {

    public static ArrayList<UserInfo> getAllVideoUser(Map<Integer, UserInfo> userInfo) {
        ArrayList<UserInfo> users = new ArrayList<>();
        Iterator<Map.Entry<Integer, UserInfo>> iterator = userInfo.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, UserInfo> entry = iterator.next();
            UserInfo user = entry.getValue();
            users.add(user);
        }
        return users;
    }

    public static ArrayList<LiveTranscoding.TranscodingUser> cdnLayout(int bigUserId, ArrayList<UserInfo> publishers,
                                                                       int canvasWidth,
                                                                       int canvasHeight) {
        ArrayList<LiveTranscoding.TranscodingUser> users;
        int index = 0;
        float xIndex, yIndex;
        int viewWidth;
        int viewHEdge;

        if (publishers.size() <= 1)
            viewWidth = canvasWidth;
        else
            viewWidth = canvasWidth / 2;

        if (publishers.size() <= 2)
            viewHEdge = canvasHeight;
        else
            viewHEdge = canvasHeight / ((publishers.size() - 1) / 2 + 1);

        users = new ArrayList<>(publishers.size());

        LiveTranscoding.TranscodingUser user0 = new LiveTranscoding.TranscodingUser();
        user0.uid = bigUserId;
        user0.alpha = 1;
        user0.zOrder = 0;
        user0.audioChannel = 0;

        user0.x = 0;
        user0.y = 0;
        user0.width = viewWidth;
        user0.height = viewHEdge;
        users.add(user0);

        index++;
        for (UserInfo entry : publishers) {
            if (entry.uid == bigUserId)
                continue;

            xIndex = index % 2;
            yIndex = index / 2;
            LiveTranscoding.TranscodingUser tmpUser = new LiveTranscoding.TranscodingUser();
            tmpUser.uid = entry.uid;
            tmpUser.x = (int) ((xIndex) * viewWidth);
            tmpUser.y = (int) (viewHEdge * (yIndex));
            tmpUser.width = viewWidth;
            tmpUser.height = viewHEdge;
            tmpUser.zOrder = index + 1;
            tmpUser.audioChannel = 0;
            tmpUser.alpha = 1f;

            users.add(tmpUser);
            index++;
        }

        return users;
    }

}
