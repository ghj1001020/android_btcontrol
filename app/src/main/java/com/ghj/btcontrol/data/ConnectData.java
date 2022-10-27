package com.ghj.btcontrol.data;

public class ConnectData {

    private int dataType;
    private int seq;
    private String text;
    private String filename;
    private int filesize;
    private int progress;

    public ConnectData(int type, int seq, String text) {
        this.dataType = type;
        this.seq = seq;
        this.text = text;
    }

    public ConnectData(int type, int seq, String filename, int filesize, int progress) {
        this.dataType = type;
        this.seq = seq;
        this.filename = filename;
        this.filesize = filesize;
        this.progress = progress;
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

    public int getFilesize() {
        return filesize;
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
