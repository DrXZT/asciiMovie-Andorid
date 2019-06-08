package com.asciimovie.drxzt.asciimovie;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.asciimovie.drxzt.asciimovie.util.ClientUploadUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button changeButton;
    private Button chooseButton;
    private ImageView gifImage;


    private static final int CROP_PHOTO = 102;
    private File tempFile;
    private Uri ImgUrl;
    private ClientUploadUtils clientUploadUtils= new ClientUploadUtils();

    private static final int WRITE_PERMISSION = 0x01;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chooseButton=findViewById(R.id.Choose_button);
        changeButton=(Button)findViewById(R.id.change_button);
        gifImage=(ImageView)findViewById(R.id.GifImage);
        requestWritePermission();
        initData(savedInstanceState);
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChooseDialog();
            }
        });

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ImgUrl==null){
                    toast("没有选择文件");
                    return;
                }

                    new Thread(){
                        public void run() {

                            try {
                                String json = clientUploadUtils.upload("http://192.168.1.102:8080/index/gif/getFile", ImgUrl,getImagePath(ImgUrl));
                            }catch (Exception e){
                                toast("文件上传异常");
                            }
                        }
                    }.start();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("tempFile", tempFile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        switch (requestCode) {
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {

                    if (intent != null) {
                        setPicToView(intent);
                    }
                }
                break;

            default:
                break;
        }
    }
    private void requestWritePermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
        }
    }
    private String getImagePath(Uri uri){
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null){
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    private void showChooseDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setItems(new String[]{"相机", "相册"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                            startActivityForResult(intent, CROP_PHOTO);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(Intent.createChooser(intent, "请选择图片"), CROP_PHOTO);
                        }
                    }
                }).show();
    }

    private void initData(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("tempFile")) {
            tempFile = (File) savedInstanceState.getSerializable("tempFile");
        }else{
            toast(checkDirPath(Environment.getExternalStorageDirectory().getPath()+"/asciiMovie/image/"));
            tempFile = new File(checkDirPath(Environment.getExternalStorageDirectory().getPath()+"/asciiMovie/image/"),
                    System.currentTimeMillis() + ".jpg");
        }
    }
    private static String checkDirPath(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return "";
        }

        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dirPath;
    }

    private void setPicToView(Intent picdata) {
        ImgUrl = picdata.getData();
        if (ImgUrl== null) {
            Log.d("MainActivity","url为空");
            toast("url为空");
            return;
        }
        gifImage.setImageURI(ImgUrl);
        ImgUrl.getPath();

    }

    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
