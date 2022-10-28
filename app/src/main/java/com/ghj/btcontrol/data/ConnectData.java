package com.ghj.btcontrol.data;

public class ConnectData {

    private int dataType;
    private int seq;
    private String text;
    private String filename;
    private long filesize;
    private long progress = 0;
    String state = "";

    public ConnectData(int type, int seq, String text) {
        this.dataType = type;
        this.state = "Waiting...";
        this.seq = seq;
        this.text = text;
    }

    public ConnectData(int type, int seq, String filename, long filesize) {
        this.dataType = type;
        this.state = "Waiting...";
        this.seq = seq;
        this.filename = filename;
        this.filesize = filesize;
        this.progress = 0;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress += progress;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
