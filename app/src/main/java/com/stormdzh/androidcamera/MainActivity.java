package com.stormdzh.androidcamera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * @description
 * @author: dzh https://github.com/Liangzhuhua/MyCamera
 * @CreateDate: 2020-12-09 13:25
 */
public class MainActivity extends AppCompatActivity {

    private ImageView ivPic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},200);
        }

        ivPic=findViewById(R.id.ivPic);

        findViewById(R.id.tvCameraH).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivityForResult(new Intent(MainActivity.this,Camera1HActivity.class),100);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK) return;
        String path = data.getStringExtra("path");
        Glide.with(this).load(path).into(ivPic);

    }
}
