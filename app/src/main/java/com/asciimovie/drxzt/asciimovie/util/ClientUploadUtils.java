package com.asciimovie.drxzt.asciimovie.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;


public class ClientUploadUtils {
    public String upload(String url, String filePath) throws Exception {
        String fileName=filePath.substring(url.lastIndexOf("/") + 1);
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(3,TimeUnit.MINUTES)
                .readTimeout(3, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .build();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("img", fileName,
                        RequestBody.create(MediaType.parse("multipart/form-data"), new File(filePath)))
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + UUID.randomUUID())
                .url(url)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        return response.body().string();
    }


    public static String getFileName(String url) {
        String filename = "";
        boolean isok = false;
        // 从UrlConnection中获取文件名称
        try {
            URL myURL = new URL(url);

            URLConnection conn = myURL.openConnection();
            if (conn == null) {
                return null;
            }
            Map<String, List<String>> hf = conn.getHeaderFields();
            if (hf == null) {
                return null;
            }
            Set<String> key = hf.keySet();
            if (key == null) {
                return null;
            }

            for (String skey : key) {
                List<String> values = hf.get(skey);
                for (String value : values) {
                    String result;
                    try {
                        result = new String(value.getBytes("ISO-8859-1"), "GBK");
                        int location = result.indexOf("filename");
                        if (location >= 0) {
                            result = result.substring(location
                                    + "filename".length());
                            filename = result
                                    .substring(result.indexOf("=") + 1);
                            isok = true;
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }// ISO-8859-1 UTF-8 gb2312
                }
                if (isok) {
                    break;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 从路径中获取
        if (filename == null || "".equals(filename)) {
            filename = url.substring(url.lastIndexOf("/") + 1);
        }
        return filename;
    }

    public Bitmap getBitmap(String imgUrl) {
        InputStream inputStream=null;
        ByteArrayOutputStream outputStream=null;
        URL url = null;
        try {
            url=new URL(imgUrl);
            HttpURLConnection httpURLConnection=(HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(2000);
            httpURLConnection.connect();
            if(httpURLConnection.getResponseCode()==200) {
                //网络连接成功
                inputStream = httpURLConnection.getInputStream();
                outputStream = new ByteArrayOutputStream();
                byte buffer[] = new byte[1024 * 8];
                int len = -1;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                byte[] bu = outputStream.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bu, 0, bu.length);
                return bitmap;
            }else {
                Log.d(TAG,"网络连接失败----"+httpURLConnection.getResponseCode());
            }
        } catch (Exception e) {
            // TODO: handle exception
        }finally{
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    public void saveImageToGallery(Bitmap bmp) {
        if (bmp==null){
            Log.e(TAG,"bitmap---为空");
            return ;
        }
        String galleryPath= Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                +File.separator+"Camera"+File.separator;
        String fileName = +System.currentTimeMillis() + ".gif";
        File file = new File(galleryPath, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();
            if (isSuccess) {
                Log.e(TAG,"图片保存成功 保存在:" + file.getPath());
            } else {
                Log.e(TAG,"图片保存失败");
            }
        } catch (IOException e) {
            Log.e(TAG,"保存图片找不到文件夹");
            e.printStackTrace();
        }
    }

}