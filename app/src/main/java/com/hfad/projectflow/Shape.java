package com.hfad.projectflow;

public class Shape {
    public ShapeType type;
    public float startX, startY, endX, endY;

    public Shape(ShapeType type, float startX, float startY, float endX, float endY) {
        this.type = type;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }
}
