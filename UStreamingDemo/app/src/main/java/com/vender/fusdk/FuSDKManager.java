package com.vender.fusdk;

import android.content.Context;
import com.faceunity.FURenderer;

public class FuSDKManager {
    private FURenderer mPreviewFilterEngine;
    private FURenderer mSaveFilterEngine;
    private Context mContext;

    public FuSDKManager(Context context) {
        mContext = context;
    }

    public static void init(Context context) {
        FURenderer.initFURenderer(context.getApplicationContext());
    }

    /**
     * 获取预览 filter engine
     */
    public FURenderer getPreviewFilterEngine() {
        if (mPreviewFilterEngine == null) {
            mPreviewFilterEngine = createFilterEngine();
        }
        return mPreviewFilterEngine;
    }

    /**
     * 获取保存 filter engine
     */
    public FURenderer getSaveFilterEngine() {
        if (mSaveFilterEngine == null) {
            mSaveFilterEngine = createFilterEngine();
        }
        return mSaveFilterEngine;
    }

    /**
     * 销毁预览 filter engine
     */
    public void destroyPreviewFilterEngine() {
        if (mPreviewFilterEngine != null) {
            mPreviewFilterEngine.onSurfaceDestroyed();
            mPreviewFilterEngine = null;
        }
    }

    /**
     * 销毁保存 filter engine
     */
    public void destroySaveFilterEngine() {
        if (mSaveFilterEngine != null) {
            mSaveFilterEngine.onSurfaceDestroyed();
            mSaveFilterEngine = null;
        }
    }

    private FURenderer createFilterEngine() {
        FURenderer filterEngine = new FURenderer.Builder(mContext).build();
        return filterEngine;
    }

}
