package com.ghj.btcontrol.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Util {

    /**
     * Uri로 부터 파일명 구하기
     */
    public static String getFilenameFromUri(Context context, Uri uri) {
        Cursor cursor = null;
        String filename = "";
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if( cursor != null && cursor.moveToFirst() ) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                filename = cursor.getString( index);
            }
        }
        finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return filename;
    }

    /**
     * Uri -> byte array 변환
     */
    public static byte[] UriToByteArray(Context context, Uri uri) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {

            InputStream is = context.getContentResolver().openInputStream(uri);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024*1024];
            int len = 0;
            while( (len = bis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    /**
     * int -> byte array 변환
     */
    public static byte[] IntToByteArray(int num) {
        byte[] bytes = new byte[] {
                (byte)((num & 0xFF000000) >> 24) ,
                (byte)((num & 0x00FF0000) >> 16) ,
                (byte)((num & 0x0000FF00) >> 8) ,
                (byte)((num & 0x000000FF) >> 0)
        };
        return bytes;
    }

    /**
     * byte array -> int 변환
     */
    public static int ByteArrayToInt(byte[] bytes) {
        if(bytes == null || bytes.length == 0) {
            return 0;
        }
        return ByteBuffer.wrap(bytes).getInt();
    }

    /**
     * filesize 계산
     */
    public static String CalculateFileSize(int bytes) {
        String[] unit = new String[]{"bytes", "KB", "MB", "GB"};
        int unitIdx = 0;
        double size = bytes;
        while( size >= 1000 ) {
            size = size / 1024.0;
            unitIdx++;
        }
        return Math.round(size*100)/100.0 + unit[unitIdx];
    }
}
