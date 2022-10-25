package com.ghj.btcontrol.data;

public class ConnectData {

    int DATATYPE;
    String text;
    String filename;
    int filesize;
    int progress;

    public ConnectData(int type, String text) {
        this.DATATYPE = type;
        this.text = text;
    }

    public ConnectData(int type, String filename, int filesize, int progress) {
        this.DATATYPE = type;
        this.filename = filename;
        this.filesize = filesize;
        this.progress = progress;
    }
}
