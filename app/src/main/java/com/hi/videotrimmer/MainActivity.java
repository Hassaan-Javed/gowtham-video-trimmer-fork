package com.hi.videotrimmer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.hi.library.utils.FileUtilKt;
import com.hi.library.utils.LogMessage;
import com.hi.library.utils.TrimType;
import com.hi.library.utils.TrimVideo;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private StyledPlayerView playerView;
    private ExoPlayer videoPlayer;
    private EditText edtFixedGap, edtMinGap, edtMinFrom, edtMAxTo;
    private int trimType;
    private Uri captureUri;

    ActivityResultLauncher<Intent> videoTrimResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK &&
                        result.getData() != null) {
                    Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData()));
                    Log.d(TAG, "Trimmed path:: " + uri);

                    videoPlayer.stop();
                    videoPlayer.clearMediaItems();
                    DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
                    MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
                    videoPlayer.addMediaSource(mediaSource);
                    videoPlayer.prepare();
                    videoPlayer.setPlayWhenReady(true);

                    String filepath = String.valueOf(uri);
                    File file = new File(filepath);
                    long length = file.length();
                    Log.d(TAG, "Video size:: " + (length / 1024));
                } else
                    LogMessage.v("videoTrimResultLauncher data is null");
            });



    ActivityResultLauncher<PickVisualMediaRequest> selectVideoResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.PickVisualMedia(),
                    uri -> {
                        if (uri != null) {
                            openTrimActivity(String.valueOf(uri));
                        }
                    });
    ActivityResultLauncher<Uri> takeVideoResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.CaptureVideo(),
                    result->{
                        if (result != null && result) {
                        openTrimActivity(captureUri.toString());
                    } else {
                        Toast.makeText(this, "Video capture cancelled", Toast.LENGTH_SHORT).show();
                    }


            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        playerView = findViewById(R.id.player_view);
        edtFixedGap = findViewById(R.id.edt_fixed_gap);
        edtMinGap = findViewById(R.id.edt_min_gap);
        edtMinFrom = findViewById(R.id.edt_min_from);
        edtMAxTo = findViewById(R.id.edt_max_to);

        findViewById(R.id.btn_default_trim).setOnClickListener(this);
        findViewById(R.id.btn_fixed_gap).setOnClickListener(this);
        findViewById(R.id.btn_min_gap).setOnClickListener(this);
        findViewById(R.id.btn_min_max_gap).setOnClickListener(this);
        initPlayer();
    }

    private void initPlayer() {
        try {
            videoPlayer = new ExoPlayer.Builder(this).build();
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            playerView.setPlayer(videoPlayer);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build();
            videoPlayer.setAudioAttributes(audioAttributes, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.pause();
    }


    private void openTrimActivity(String data) {
        if (trimType == 0) {
            TrimVideo.activity(data)
//                  .setCompressOption(new CompressOption()) //pass empty constructor for default compress option
                    .start(this, videoTrimResultLauncher);
        } else if (trimType == 1) {
            TrimVideo.activity(data)
                    .setTrimType(TrimType.FIXED_DURATION)
                    .setFixedDuration(getEdtValueLong(edtFixedGap))
                    .setLocal("ar")
                    .start(this, videoTrimResultLauncher);
        } else if (trimType == 2) {
            TrimVideo.activity(data)
                    .setTrimType(TrimType.MIN_DURATION)
                    .setLocal("ar")
                    .setMinDuration(getEdtValueLong(edtMinGap))
                    .start(this, videoTrimResultLauncher);
        } else {
            TrimVideo.activity(data)
                    .setTrimType(TrimType.MIN_MAX_DURATION)
                    .setLocal("ar")
                    .setMinToMax(getEdtValueLong(edtMinFrom), getEdtValueLong(edtMAxTo))
                    .start(this, videoTrimResultLauncher);
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId()==R.id.btn_default_trim)
            onDefaultTrimClicked();

        if (v.getId()==R.id.btn_fixed_gap)
            onFixedTrimClicked();

        if (v.getId()==R.id.btn_min_gap)
            onMinGapTrimClicked();

        if (v.getId()==R.id.btn_min_max_gap)
            onMinToMaxTrimClicked();

    }

    private void onDefaultTrimClicked() {
        trimType = 0;

        showVideoOptions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayer.release();
    }

    private void onFixedTrimClicked() {
        trimType = 1;
        if (isEdtTxtEmpty(edtFixedGap)) {

            Toast.makeText(this, "Enter fixed gap duration", Toast.LENGTH_SHORT).show();
            return;
        }

        showVideoOptions();
    }

    private void onMinGapTrimClicked() {
        trimType = 2;
        if (isEdtTxtEmpty(edtMinGap)) {
            Toast.makeText(this, "Enter min gap duration", Toast.LENGTH_SHORT).show();
            return;
        }

        showVideoOptions();
    }


    private void onMinToMaxTrimClicked() {
        trimType = 3;
        if (isEdtTxtEmpty(edtMinFrom)) {
            Toast.makeText(this, "Enter min gap duration", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (isEdtTxtEmpty(edtMAxTo)) {
            Toast.makeText(this, "Enter max gap duration", Toast.LENGTH_SHORT).show();
            return;

        }

        showVideoOptions();
    }

    public void showVideoOptions() {
        try {
            BottomSheet.Builder builder = getBottomSheet();
            builder.sheet(R.menu.menu_video);
            builder.listener(item -> {
                if (R.id.action_take == item.getItemId())
                    captureVideo();
                else
                    openVideo();
                return false;
            });
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BottomSheet.Builder getBottomSheet() {
        return new BottomSheet.Builder(this).title(R.string.txt_option);
    }

    public void captureVideo() {

        try {
            captureUri = FileUtilKt.createVideoUri(this);
            takeVideoResultLauncher.launch(captureUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openVideo() {
        PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                .build();
        selectVideoResultLauncher.launch(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (isPermissionOk(grantResults))
            showVideoOptions();
    }

    private boolean isEdtTxtEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    private long getEdtValueLong(EditText editText) {
        return Long.parseLong(editText.getText().toString().trim());
    }



    private boolean checkPermission(String... permissions) {
        boolean allPermitted = false;
        for (String permission : permissions) {
            allPermitted = (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED);
            if (!allPermitted)
                break;
        }
        if (allPermitted)
            return true;
        ActivityCompat.requestPermissions(this, permissions,
                220);
        return false;
    }

    private boolean isPermissionOk(int... results) {
        boolean isAllGranted = true;
        for (int result : results) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                isAllGranted = false;
                break;
            }
        }
        return isAllGranted;
    }
}