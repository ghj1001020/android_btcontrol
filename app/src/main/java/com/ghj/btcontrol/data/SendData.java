package com.ghj.btcontrol.data;

import android.net.Uri;
import android.text.TextUtils;

public class SendData {
    int dataType = 0;   // 0-텍스트 , 1-파일
    String text;    // 텍스트
    Uri uri;    // 파일
    String filename;    // 파일명
    long filesize;  // 파일사이즈

    public SendData(int type, String text) {
        this.dataType = type;
        this.text = text;
    }

    public SendData(int type, Uri uri, String filename, long filesize) {
        this.dataType = type;
        this.uri = uri;
        this.filename = filename;
        this.filesize = filesize;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getFilename() {
        if(TextUtils.isEmpty(filename)) {
            return "UnknownFile";
        }
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }
}
