package com.hfad.projectflow;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class Thumbnail {
    private int id;
    private byte[] data;


    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}
