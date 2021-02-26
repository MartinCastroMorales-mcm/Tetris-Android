package com.example.tetris;

import android.graphics.Color;

public class Tetronomino {
    public int x, y;
    private int[] blockXs = new int[4];
    private int[] blockYs = new int[4];
    public int rotation = 0;
    private int color = 0xFFFFFFFF;
    String type;


    public Tetronomino(String type) {
        //FIXME temporary starting cordinates
        this.type = type;
        x = 4;
        y = 0;
        //TODO: do a basic one with just one square and later add the forms
        adjustPos(type, rotation);
    }

    public void adjustPos(String type, int rotation) {
        this.rotation = rotation;
        //aparently this method is not nesesary at least until the transformations
        //You could try to just use rotations but that would require you to find the origin of each so that everything remains perfectly align.
        switch (type) {
            case "O":
                //yellow
                color = Color.argb(255, 255, 255, 0);
                //no need for rotation not now at least
                switch (rotation) {
                    case 360:
                        this.rotation = 0;
                    case 0:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x + 1;
                        blockYs[1] = y;
                        blockXs[2] = x;
                        blockYs[2] = y + 1;
                        blockXs[3] = x + 1;
                        blockYs[3] = y + 1;
                        break;
                    case 90:
                        //will alone cannot stop the sun from setting, also this a square why rotate.
                    case 180:

                    case -90:
                        this.rotation = 270;
                    case 270:
                }
                break;
            case "L":
                //blue
                color = Color.argb(255, 0, 0, 255);
                switch (rotation) {
                    case 360:
                        this.rotation = 0;
                    case 0:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x;
                        blockYs[1] = y - 2;
                        blockXs[2] = x;
                        blockYs[2] = y - 1;
                        blockXs[3] = x + 1;
                        blockYs[3] = y;
                        break;
                    case 90:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x + 2;
                        blockYs[1] = y;
                        blockXs[2] = x + 1;
                        blockYs[2] = y;
                        blockXs[3] = x;
                        blockYs[3] = y + 1;
                        break;
                    case 180:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x;
                        blockYs[1] = y + 2;
                        blockXs[2] = x;
                        blockYs[2] = y + 1;
                        blockXs[3] = x - 1;
                        blockYs[3] = y;
                        break;
                    case -90:
                        this.rotation = 270;
                    case 270:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x - 2;
                        blockYs[1] = y;
                        blockXs[2] = x - 1;
                        blockYs[2] = y;
                        blockXs[3] = x;
                        blockYs[3] = y - 1;
                        break;
                }
                break;
            case "J":
                //orange
                color = 0XFFFFA500;
                switch (rotation) {
                    case 360:
                        this.rotation = 0;
                    case 0:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x;
                        blockYs[1] = y - 2;
                        blockXs[2] = x;
                        blockYs[2] = y - 1;
                        blockXs[3] = x - 1;
                        blockYs[3] = y;
                        break;
                    case 90:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x + 2;
                        blockYs[1] = y;
                        blockXs[2] = x + 1;
                        blockYs[2] = y;
                        blockXs[3] = x;
                        blockYs[3] = y - 1;
                        break;
                    case 180:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x;
                        blockYs[1] = y + 2;
                        blockXs[2] = x;
                        blockYs[2] = y + 1;
                        blockXs[3] = x + 1;
                        blockYs[3] = y;
                        break;
                    case -90:
                        this.rotation = 270;
                    case 270:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x - 2;
                        blockYs[1] = y;
                        blockXs[2] = x - 1;
                        blockYs[2] = y;
                        blockXs[3] = x;
                        blockYs[3] = y + 1;
                        break;

                }
                break;
            case "T":

                //purble
                color = 0xFF880088;
                switch (rotation) {
                    case 360:
                        this.rotation = 0;
                    case 0:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x;
                        blockYs[1] = y - 1;
                        blockXs[2] = x + 1;
                        blockYs[2] = y;
                        blockXs[3] = x - 1;
                        blockYs[3] = y;
                        break;
                    case 90:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x + 1;
                        blockYs[1] = y;
                        blockXs[2] = x;
                        blockYs[2] = y + 1;
                        blockXs[3] = x;
                        blockYs[3] = y - 1;
                        break;
                    case 180:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x;
                        blockYs[1] = y + 1;
                        blockXs[2] = x - 1;
                        blockYs[2] = y;
                        blockXs[3] = x + 1;
                        blockYs[3] = y;
                        break;
                    case -90:
                        this.rotation = 270;
                    case 270:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x - 1;
                        blockYs[1] = y;
                        blockXs[2] = x;
                        blockYs[2] = y - 1;
                        blockXs[3] = x;
                        blockYs[3] = y + 1;
                        break;
                }
                break;
            case "I":
                //light blue
                color = 0xFF89cfF0;
                switch (rotation) {//TODO
                    case 360:
                        this.rotation = 0;
                    case 0:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x;
                        blockYs[1] = y + 1;
                        blockXs[2] = x;
                        blockYs[2] = y - 1;
                        blockXs[3] = x;
                        blockYs[3] = y - 2;
                        break;
                    case 90:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x + 1;
                        blockYs[1] = y;
                        blockXs[2] = x - 1;
                        blockYs[2] = y;
                        blockXs[3] = x - 2;
                        blockYs[3] = y;
                        break;
                    case 180:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x;
                        blockYs[1] = y + 1;
                        blockXs[2] = x;
                        blockYs[2] = y - 1;
                        blockXs[3] = x;
                        blockYs[3] = y - 2;
                        break;
                    case -90:
                        this.rotation = 270;
                    case 270:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x - 1;
                        blockYs[1] = y;
                        blockXs[2] = x + 1;
                        blockYs[2] = y;
                        blockXs[3] = x + 2;
                        blockYs[3] = y;
                        break;

                }
                break;
            case "S":
                //lime
                color = 0xff49FF38;
                switch (rotation) {
                    case 360:
                        this.rotation = 0;
                    case 0:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x;
                        blockYs[1] = y - 1;
                        blockXs[2] = x + 1;
                        blockYs[2] = y - 1;
                        blockXs[3] = x - 1;
                        blockYs[3] = y;
                        break;
                    case 90:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x + 1;
                        blockYs[1] = y;
                        blockXs[2] = x + 1;
                        blockYs[2] = y + 1;
                        blockXs[3] = x;
                        blockYs[3] = y - 1;
                        break;
                    case 180:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x;
                        blockYs[1] = y - 1;
                        blockXs[2] = x + 1;
                        blockYs[2] = y - 1;
                        blockXs[3] = x - 1;
                        blockYs[3] = y;
                        break;
                    case -90:
                        this.rotation = 270;
                    case 270:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x + 1;
                        blockYs[1] = y;
                        blockXs[2] = x + 1;
                        blockYs[2] = y + 1;
                        blockXs[3] = x;
                        blockYs[3] = y - 1;
                        break;

                }
                break;
            case "Z":
                //red
                color = 0xffff0000;
                switch (rotation) {
                    case 360:
                        this.rotation = 0;
                    case 0:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x;
                        blockYs[1] = y - 1;
                        blockXs[2] = x - 1;
                        blockYs[2] = y - 1;
                        blockXs[3] = x + 1;
                        blockYs[3] = y;
                        break;
                    case 90:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x + 1;
                        blockYs[1] = y;
                        blockXs[2] = x + 1;
                        blockYs[2] = y - 1;
                        blockXs[3] = x;
                        blockYs[3] = y + 1;
                        break;
                    case 180:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x;
                        blockYs[1] = y - 1;
                        blockXs[2] = x - 1;
                        blockYs[2] = y - 1;
                        blockXs[3] = x + 1;
                        blockYs[3] = y;
                        break;
                    case -90:
                        this.rotation = 270;
                    case 270:
                        blockXs[0] = x;
                        blockYs[0] = y;
                        blockXs[1] = x + 1;
                        blockYs[1] = y;
                        blockXs[2] = x + 1;
                        blockYs[2] = y - 1;
                        blockXs[3] = x;
                        blockYs[3] = y + 1;
                        break;
                }
                break;
            // I made a great mistake, if rotate CW with tap, how CCW double tap.
            //We are gonna have to learn some gestures
        }
        /*for(int i = 0; i < blockYs.length; i++) {
            System.out.println("after adjust: " + blockXs[i] + " y " + blockYs[i]);
        }*/
    }
    public int[] getBlockXs() {
        return blockXs;
    }

    public int[] getBlockYs() {
        return blockYs;
    }

    public int getColor() {
        return color;
    }
}
