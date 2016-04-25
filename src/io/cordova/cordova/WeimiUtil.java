package io.cordova.cordova;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Created by 卫彪 on 2016/4/12.
 * 静态类
 */
public class WeimiUtil {

    public static String uid = "";
    private static boolean isDebug = true;
    public static final int RECEIVE_TEXT = 1001; // 接收文本
    public static final int RECEIVE_PICTURE = 1002; // 接收图片
    public static final int UPLOAD_PIC_PRO = 1003; // 上传进度条
    public static final int DOWNLOAD_PIC_PRO = 1004; // 下载进度条

    /**
     * 根据设备生成一个唯一标识
     *
     * @return
     */
    public static String generateOpenUDID(Context context) {
        String OpenUDID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (OpenUDID == null || OpenUDID.equals("9774d56d682e549c") | OpenUDID.length() < 15) {
            final SecureRandom random = new SecureRandom();
            OpenUDID = new BigInteger(64, random).toString(16);
        }
        return OpenUDID;
    }

    public static final String MSG_ID_PRE = UUID.randomUUID() + "";
    public static int msg_p = 0;

    public static String genLocalMsgId() {
        msg_p++;
        String msgId = MSG_ID_PRE + msg_p;
        return msgId;
    }

    /**
     * log
     * @param msg
     */
    public static void log(String msg){
        if(isDebug)
            System.out.println(msg);
    }

    /**
     * 将图片转化为byte[]
     * @param thumbnailPath 图片路径
     * @return
     */
    public static byte[] getByteByPath(String thumbnailPath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(thumbnailPath);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            byte[] result = output.toByteArray();
            return result;
        }catch (Exception e){
            e.getMessage();
        }
        return null;
    }

    /**
     * 将Bitmpa转化为byte[]
     * @param bitmap
     * @return
     */
    public static byte[] getByteByBitmap(Bitmap bitmap) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            byte[] result = output.toByteArray();
            return result;
        }catch (Exception e){
            e.getMessage();
        }
        return null;
    }

    /**
     * 根据路径获得图片并压缩，返回bitmap用于显示
     * @param filePath
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 150, 150);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 计算图片的缩放值
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * bitmap转为base64
     * @param byteImg
     * @return
     */
    public static String bitmapToBase64(byte[] byteImg) {
        return Base64.encodeToString(byteImg, Base64.DEFAULT).replaceAll("\n", "");
    }

}
