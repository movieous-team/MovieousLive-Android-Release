package com.movieous.streaming.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.movieous.streaming.demo.utils.PermissionChecker;
import com.movieous.streaming.demo.utils.StreamingParams;
import com.movieous.streaming.demo.utils.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final String[] publishUrls = {
            "rtmp://livepush.ucloud.com.cn/test/%s"
    };

    private final String[] playUrls = {
            "rtmp://livertmp.ucloud.com.cn/test/%s"
    };

    private String mStreamingUrl;
    private SharedPreferences mSharedPreferences;

    private Spinner mPreviewSizeRatioSpinner;
    private Spinner mPreviewSizeLevelSpinner;
    private Spinner mEncodingSizeLevelSpinner;
    private Spinner mEncodingBitrateLevelSpinner;
    private EditText mUrlEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String streamId = (int) Math.floor((new Random().nextDouble() * 10000.0 + 10000.0)) + "";
        mStreamingUrl = String.format(publishUrls[0], streamId);
        // restore data.
        mSharedPreferences = getSharedPreferences("MovieousStreaming", MODE_PRIVATE);
        mStreamingUrl = mSharedPreferences.getString("streamingUrl", mStreamingUrl);

        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("streamingUrl", mUrlEditText.getText().toString());
        editor.apply();
    }

    private void initView() {
        TextView versionInfoTextView = findViewById(R.id.VersionInfoTextView);
        String info = "版本号：" + getVersionDescription() + "，编译时间：" + getBuildTimeDescription();
        versionInfoTextView.setText(info);

        mUrlEditText = findViewById(R.id.url);
        mUrlEditText.setText(mStreamingUrl);

        mPreviewSizeRatioSpinner = findViewById(R.id.PreviewSizeRatioSpinner);
        mPreviewSizeLevelSpinner = findViewById(R.id.PreviewSizeLevelSpinner);
        mEncodingSizeLevelSpinner = findViewById(R.id.EncodingSizeLevelSpinner);
        mEncodingBitrateLevelSpinner = findViewById(R.id.EncodingBitrateLevelSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, StreamingParams.PREVIEW_SIZE_RATIO_TIPS);
        mPreviewSizeRatioSpinner.setAdapter(adapter);
        mPreviewSizeRatioSpinner.setSelection(1);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, StreamingParams.PREVIEW_SIZE_LEVEL_TIPS);
        mPreviewSizeLevelSpinner.setAdapter(adapter);
        mPreviewSizeLevelSpinner.setSelection(2);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, StreamingParams.ENCODING_SIZE_LEVEL_TIPS);
        mEncodingSizeLevelSpinner.setAdapter(adapter);
        mEncodingSizeLevelSpinner.setSelection(9);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, StreamingParams.ENCODING_BITRATE_LEVEL_TIPS);
        mEncodingBitrateLevelSpinner.setAdapter(adapter);
        mEncodingBitrateLevelSpinner.setSelection(2);
    }

    public void onClickStreaming(View v) {
        if (isPermissionOK()) {
            jumpToStreamingActivity();
        }
    }

    private void jumpToActivity(Class<?> cls) {
        Intent intent = new Intent(MainActivity.this, cls);
        startActivity(intent);
    }

    private boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.s(this, "Some permissions is not approved !!!");
        }
        return isPermissionOK;
    }

    private String getVersionDescription() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "未知";
    }

    protected String getBuildTimeDescription() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(BuildConfig.BUILD_TIMESTAMP);
    }

    public void jumpToStreamingActivity() {
        mStreamingUrl = mUrlEditText.getText().toString();
        Intent intent = new Intent(MainActivity.this, StreamingActivity.class);
        intent.putExtra(StreamingActivity.STREAMING_URL, mStreamingUrl);
        intent.putExtra(StreamingActivity.PREVIEW_SIZE_RATIO, mPreviewSizeRatioSpinner.getSelectedItemPosition());
        intent.putExtra(StreamingActivity.PREVIEW_SIZE_LEVEL, mPreviewSizeLevelSpinner.getSelectedItemPosition());
        intent.putExtra(StreamingActivity.ENCODING_SIZE_LEVEL, mEncodingSizeLevelSpinner.getSelectedItemPosition());
        intent.putExtra(StreamingActivity.ENCODING_BITRATE_LEVEL, mEncodingBitrateLevelSpinner.getSelectedItemPosition());
        startActivity(intent);
    }

}
