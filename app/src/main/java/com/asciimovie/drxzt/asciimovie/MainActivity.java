package com.asciimovie.drxzt.asciimovie;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Button changeButton;
    private ImageView gifImage;


    private static final int CROP_PHOTO = 102;
    private File tempFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        changeButton=(Button)findViewById(R.id.change_button);
        gifImage=(ImageView)findViewById(R.id.GifImage);

        initData(savedInstanceState);
        gifImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChooseDialog();
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
        Uri uri = picdata.getData();
        if (uri == null) {
            Log.d("MainActivity","url为空");
            toast("url为空");
            return;
        }
        gifImage.setImageURI(uri);

    }

    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
