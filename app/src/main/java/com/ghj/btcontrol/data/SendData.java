package com.ghj.btcontrol.data;

import android.net.Uri;
import android.text.TextUtils;

public class SendData {
    int dataType = 0;   // 0-텍스트 , 1-파일
    int seq = 0;    // 전송순서
    String text;    // 텍스트
    Uri uri;    // 파일
    String filename;    // 파일명
    long filesize;  // 파일사이즈

    public SendData(int type, int seq, String text) {
        this.dataType = type;
        this.seq = seq;
        this.text = text;
    }

    public SendData(int type, int seq, Uri uri, String filename, long filesize) {
        this.dataType = type;
        this.seq = seq;
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
        if(TextUtils.isEmpty(text)) {
            return "";
        }
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

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
