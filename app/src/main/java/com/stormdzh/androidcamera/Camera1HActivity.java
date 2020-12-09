package com.stormdzh.androidcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Description: 描述
 * @Author: dzh
 * @CreateDate: 2020-12-09 11:07
 */
public class Camera1HActivity extends Activity implements TextureView.SurfaceTextureListener {

    private TextureView mTextureView;
    private Camera mCamera;
    private ImageView ivTakePhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera1_h);

        mTextureView = findViewById(R.id.mTextureView);
        mTextureView.setSurfaceTextureListener(this);

        ivTakePhoto = findViewById(R.id.ivTakePhoto);
        ivTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });
    }

    private void takePhoto() {

        Camera.Parameters parameters;
        try{
            parameters = mCamera.getParameters();
        }catch(Exception e){
            e.printStackTrace();
            return;
        }
        //获取摄像头支持的各种分辨率,因为摄像头数组不确定是按降序还是升序，这里的逻辑有时不是很好找得到相应的尺寸
        //可先确定是按升还是降序排列，再进对对比吧，我这里拢统地找了个，是个不精确的...
        List<Camera.Size> list = parameters.getSupportedPictureSizes();
        int size = 0;
        for (int i =0 ;i < list.size() - 1;i++){
            if (list.get(i).width >= 480){
                //完美匹配
                size = i;
                break;
            }
            else{
                //找不到就找个最接近的吧
                size = i;
            }
        }
        //设置照片分辨率，注意要在摄像头支持的范围内选择
        parameters.setPictureSize(list.get(size).width,list.get(size).height);
        //设置照相机参数
        mCamera.setParameters(parameters);

        //使用takePicture()方法完成拍照
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            //自动聚焦完成后拍照
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success && camera != null){
                    mCamera.takePicture(new ShutterCallback(), null, new Camera.PictureCallback() {
                        //拍照回调接口
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            savePhoto(data);
                            //停止预览
                            mCamera.stopPreview();
                            //重启预览
                            mCamera.startPreview();
                        }
                    });
                }
            }
        });

    }

    /**
     * 将拍照保存下来
     * @param data
     */
    public void savePhoto(byte[] data){
        FileOutputStream fos = null;
        String timeStamp =new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        //保存路径+图片名字
        String imagePath = setPicSaveFile() + "/" + timeStamp + ".png";
        try{
            fos = new FileOutputStream(imagePath);
            fos.write(data);
            //清空缓冲区数据
            fos.flush();
            //关闭
            fos.close();
            Toast.makeText(this,"拍照成功!",Toast.LENGTH_SHORT).show();
            Log.i("savePhoto","path:"+imagePath);
            Intent intent =new Intent();
            intent.putExtra("path",imagePath);
            setResult(Activity.RESULT_OK,intent);
            finish();
        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }
    }

    /**
     * 设置照片的路径，具体路径可自定义
     * @return
     */
    private String setPicSaveFile(){
        //创建保存的路径
        File storageDir = getOwnCacheDirectory(this,"MyCamera/photos");
        //返回自定义的路径
        return storageDir.getPath();
    }

    private File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        //判断SD卡正常挂载并且拥有根限的时候创建文件
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
                hasExternalStoragePermission(context)){
            appCacheDir = new File(Environment.getExternalStorageDirectory(),cacheDir);
        }
        if (appCacheDir == null || !appCacheDir.exists() && !appCacheDir.mkdirs()){
            appCacheDir = context.getCacheDir();
        }
//        appCacheDir = new File(Environment.getExternalStorageDirectory(),cacheDir);
        if(!appCacheDir.exists()){
            appCacheDir.mkdirs();
        }
        return appCacheDir;
    }

    /**
     * 检查是否有权限
     * @param context
     * @return
     */
    private boolean hasExternalStoragePermission(Context context) {
        int permission = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        //PERMISSION_GRANTED=0
        return permission == 0;
    }

    private class ShutterCallback implements Camera.ShutterCallback {
        @Override
        public void onShutter() {
//            MediaPlayer mPlayer = new MediaPlayer();
//            mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.shutter);
            try{
//                mPlayer.prepare();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
//            mPlayer.start();
        }
    }


    public static int getSdkVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    private static boolean checkCameraFacing(final int facing) {
        if (getSdkVersion() < Build.VERSION_CODES.GINGERBREAD) {
            return false;
        }
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasBackFacingCamera() {
        //Camera.CameraInfo.CAMERA_FACING_BACK
        int CAMERA_FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    public static boolean hasFrontFacingCamera() {
        int CAMERA_FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;
        return checkCameraFacing(CAMERA_FACING_FRONT);
    }


    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        initCamera(surfaceTexture);
    }

    private void initCamera(@NonNull SurfaceTexture surfaceTexture) {

        boolean hasBack = hasBackFacingCamera();
        if (!hasBack) {
            Toast.makeText(this, "摄像头不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        mCamera = Camera.open();
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size previewSize = parameters.getPreviewSize();

            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "portrait");
                mCamera.setDisplayOrientation(90);
                parameters.setRotation(90);
            } else {
                parameters.set("orientation", "landscape");
                mCamera.setDisplayOrientation(0);
                parameters.setRotation(0);
            }

            mCamera.setParameters(parameters);
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

    }
}
