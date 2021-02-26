package com.example.tetris;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.view.GestureDetectorCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class TetrisEngine extends SurfaceView implements Runnable, GestureDetector.OnGestureListener, Comparator<Square> { //TODO: lock to Portrait

    //Render
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Paint paint;
    private int screenX;
    private int screenY;
    private int borderWidth;
    private ArrayList<Square> blocksToRender = new ArrayList<>();
    private boolean updatingPos = true;
    //Move
    private float oldX;
    private float newX;
    float dx = 0;
    boolean onFling;

    //Game
    private final int NUM_ROWS = 20;
    private final int NUM_COLUMNS = 10;
    private int blockSize;
    private int blockHeight;
    private Tetronomino tetronomino;
    //it creates 3 of each type and orders them randomly.
    private ArrayList<String> pendingTypes = new ArrayList<>();
    private boolean quickTap = true;
    private boolean[] usedSpaces = new boolean[NUM_COLUMNS * NUM_ROWS + NUM_COLUMNS];
    private int score = 0;
    private int tetrosPlaced = 0;
    private int lvl = 1;

    //Time
    private long nextFrameTime;
    private long nextGameClock;

    private boolean Running = true;
    private Context context;
    private final double FPS = 60;
    private double timer = 2;
    private final long MILLIS_PER_SECOND = 1000;

    //Other
    GestureDetectorCompat mDetector;

    public TetrisEngine(Context context, int w, int h) {
        super(context);
        screenX = w;
        screenY = h;
        this.context = context;
        paint = new Paint();
        surfaceHolder = getHolder();
        blockSize = screenX / NUM_COLUMNS;
        blockHeight = screenY / NUM_ROWS;
        mDetector = new GestureDetectorCompat(context, this);

        newGame();

    }

    @Override
    public void run() {
        System.out.println("run");
        while (Running) {
            update();
            // an if render boolean may be nesesary to save time.+
            if (newFrameTime()) {
                draw();
            }
        }

    }

    private void gameOver() {
        Running = false;
    }

    private void newGame() {
        //game
        score = 0;
        tetrosPlaced = 0;
        lvl = 0;
        timer = 2;


        pendingTypes = new ArrayList<>();
        Running = true;
        usedSpaces = new boolean[NUM_COLUMNS * NUM_ROWS + NUM_COLUMNS];
        for (int i = 0; i < NUM_COLUMNS; i++) {
            usedSpaces[i + (NUM_ROWS) * NUM_COLUMNS] = true;
        }
        tetronomino = newTetro();
        blocksToRender = new ArrayList<Square>();

        nextGameClock = System.currentTimeMillis();
        nextFrameTime = System.currentTimeMillis();

        Thread thread = new Thread(this);
        thread.start();
    }

    private void verticalCollision() {
        int[] tetroXs = tetronomino.getBlockXs();   //TODO: make a general collision funcion
        int[] tetroYs = tetronomino.getBlockYs();


        if (isUnderOccupied(tetroXs, tetroYs)) {
            for (int i = 0; i < tetroXs.length; i++) {
                if (tetroYs[i] < 0) {
                    gameOver();
                    return;
                }
            }
            //Save used spaces
            for (int ii = 0; ii < tetroXs.length; ii++) {
                //System.out.println("usedSpaces are: x " + tetroXs[ii] + " y " + tetroYs[ii]);
                usedSpaces[tetroXs[ii] + (tetroYs[ii]) * NUM_COLUMNS] = true;
            }
            newTetro();
            checkToDeleteRow();
        }


    }

    private boolean isUnderOccupied(int[] tetroXs, int[] tetroYs) {
        for (int i = 0; i < tetroXs.length; i++) {
            try {
                if (usedSpaces[tetroXs[i] + (tetroYs[i] + 1) * NUM_COLUMNS]) {
                    return true;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                //if it returns out of bounds(for example because y is negative when spawning) assume there is nothing bellow
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        if (updatingPos) {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    quickTap = true;
                    oldX = event.getX() / blockSize;
                    //save touch location to translate

                    break;

                case MotionEvent.ACTION_MOVE:       //FIXME just feels unresponsive
                    quickTap = false;
                    if (onFling) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:     //TODO: al transformar mueve el tetronimo para que este dentro del espacio.
                    float x = event.getX();
                    //Rotate if tap quick
                    if (quickTap) {
                        if (event.getY() > 7 * screenY / 8) {
                            if (screenX / 2 < x && isAbleToMove(tetronomino, tetronomino.x + 1)) {
                                System.out.println("move right");

                                tetronomino.x++;
                            }
                            if (screenX / 2 > x && isAbleToMove(tetronomino, tetronomino.x - 1)) {
                                System.out.println("moveLeft");
                                tetronomino.x--;
                            }
                            tetronomino.adjustPos(tetronomino.type, tetronomino.rotation);
                        } else {

                            if (screenX / 2 > x) {
                                //tap left
                                System.out.println("rotate CCW");
                                tetronomino.adjustPos(tetronomino.type, tetronomino.rotation - 90);
                            } else {
                                //tap right
                                System.out.println("rotate CW");
                                tetronomino.adjustPos(tetronomino.type, tetronomino.rotation + 90);
                            }
                            System.out.println("rotation " + tetronomino.rotation);
                        }

                        if (!Running) {
                            System.out.println("newGame");
                            newGame();
                        }
                    }
            }

        }
        return true;
    }

    public void update() {  //FIXME some transformation can let you go out of bounds
        if (isGoingDown()) {
            verticalCollision();
            tetronomino.y++;
            tetronomino.adjustPos(tetronomino.type, tetronomino.rotation);
        }
        // }
    }


    public void draw() {

        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            //clear
            canvas.drawColor(Color.BLACK);

            //draw Background
            int Bx = 0;
            int By = 0;
            for (int i = 0; i < (NUM_COLUMNS * NUM_ROWS); i++) {
                drawBlock(Bx, By, canvas, Color.BLACK, 0X55555555);
                Bx += 1;
                if (Bx == NUM_COLUMNS) {
                    Bx = 0;
                    By += 1;
                }
            }


            //drawTetrominos
            paint.setColor(Color.argb(255, 255, 255, 255));
            //draw all previus tetrominos
            for (int i = blocksToRender.size() - 1; i >= 0; i--) {
                drawBlock(blocksToRender.get(i).x, blocksToRender.get(i).y, canvas, Color.BLACK, blocksToRender.get(i).color);
            }

            {
                //Test to see used Spaces
                int x, y;
                for (int i = 0; usedSpaces.length > i; i++) {
                    if (usedSpaces[i]) {

                        y = i / NUM_COLUMNS;
                        x = i % NUM_COLUMNS;
                        drawBlock(x, NUM_ROWS, canvas, 0XFFAAAAAA, 0xFF777777);
                    }
                }
            }
            drawTetromino(5, 0, canvas, tetronomino);

            //Draw UI
            paint.setTextSize(40);
            paint.setColor(Color.WHITE);
            canvas.drawText("Score: " + score, 20, 30, paint);
            canvas.drawText("Level: " + lvl, 20, 70, paint);
            //TODO: DRAW BUTTONS

            if (!Running) {
                //draw Game over
                paint.setColor(Color.RED);
                paint.setTextSize(120);
                canvas.drawText("GAME OVER", 50, 500, paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }

    }

    public void pause() {
        //Thread.join()
    }

    public void resume() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public boolean newFrameTime() {
        if (nextFrameTime <= System.currentTimeMillis()) {
            nextFrameTime = (long) (System.currentTimeMillis() + MILLIS_PER_SECOND / FPS);
            return true;
        }
        return false;
    }

    public void drawBlock(int x, int y, Canvas canvas, int border, int color) {
        int blockW = blockSize;
        int blockH = blockHeight;
        paint.setColor(border);
        canvas.drawRect(x * blockW, y * blockH,
                (x * blockW) + blockW, (y * blockH) + blockH, paint);
        paint.setColor(color);
        canvas.drawRect((x * blockW) + 3, (y * blockH) + 3,
                (x * blockW) + blockW - 3, (y * blockH) + blockH - 3, paint);
    }

    public void drawTetromino(int x, int y, Canvas canvas, Tetronomino tetronomino) {
        int[] tetroXs = tetronomino.getBlockXs();
        int[] tetroYs = tetronomino.getBlockYs();

        for (int i = 0; i < 4; i++) {
            drawBlock(tetroXs[i], tetroYs[i], canvas, Color.BLACK, tetronomino.getColor());
        }
    }

    private Tetronomino newTetro() {
        if (pendingTypes.size() == 0) {
            pendingTypes = CreateNewArrayOfTypes();
        }
        if (tetronomino != null) {
            int[] tetroXs = tetronomino.getBlockXs();
            int[] tetroYs = tetronomino.getBlockYs();
            for (int i = 0; i < tetronomino.getBlockXs().length; i++) {
                Square square = new Square(tetroXs[i], tetroYs[i], tetronomino.getColor());

                blocksToRender.add(square);
            }
        }
        //Random Type generator, we need to create, 0-7


        tetronomino = new Tetronomino(pendingTypes.get(0));
        pendingTypes.remove(0);

        //Points and lvl related code
        score += 10;
        tetrosPlaced++;
        switch (tetrosPlaced) {
            case 0:
                lvl = 1;
                timer = 2;
                break;
            case 5:
                lvl = 2;
                timer = 2.5;
                break;
            case 10:
                lvl = 3;
                timer = 3;
                break;
            case 15:
                lvl = 3;
                timer = 4;
                break;
            case 20:
                lvl = 4;
                timer = 5;
                break;
            case 25:
                lvl = 5;
                timer = 6;
                break;

        }

        return tetronomino;

    }


    private ArrayList<String> CreateNewArrayOfTypes() {
        //Create String[]
        ArrayList<String> typeArrayList = new ArrayList<>();
        //Repeited 3 times to increase randomness
        typeArrayList.add("O");
        typeArrayList.add("L");
        typeArrayList.add("J");
        typeArrayList.add("T");
        typeArrayList.add("I");
        typeArrayList.add("S");
        typeArrayList.add("Z");
        typeArrayList.add("O");
        typeArrayList.add("L");
        typeArrayList.add("J");
        typeArrayList.add("T");
        typeArrayList.add("I");
        typeArrayList.add("S");
        typeArrayList.add("Z");
        typeArrayList.add("O");
        typeArrayList.add("L");
        typeArrayList.add("J");
        typeArrayList.add("T");
        typeArrayList.add("I");
        typeArrayList.add("S");
        typeArrayList.add("Z");


        Random r = new Random();
        for (int i = 0; i < typeArrayList.size(); i++) {
            String tempList = typeArrayList.get(i);
            int id = r.nextInt(typeArrayList.size() - 1) + 1;
            typeArrayList.set(i, typeArrayList.get(id));
            typeArrayList.set(id, tempList);
        }
        //Read the array
        for (int i = 0; i < typeArrayList.size(); i++) {
            System.out.println(typeArrayList.get(i));
        }
        return typeArrayList;
    }

    public boolean isAbleToMove(Tetronomino tetronomino, int newX) {
        Tetronomino testTetro = new Tetronomino(tetronomino.type);
        testTetro.x = newX;
        testTetro.adjustPos(testTetro.type, tetronomino.rotation);
        int[] tetroXs = testTetro.getBlockXs();

        int x;
        for (int i = 0; i < tetroXs.length; i++) {
            x = tetroXs[i];

            if (x < 0 || x >= NUM_COLUMNS) {
                return false;
            }

        }
        return true;

    }

    private boolean isGoingDown() {
        if (nextGameClock <= System.currentTimeMillis()) {
            nextGameClock = System.currentTimeMillis() + MILLIS_PER_SECOND / (long) timer;
            return true;
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (velocityY > 100 || velocityY < -100) {
            onFling = true;
            boolean returnVal = freefall();
            onFling = false;
            return returnVal;
        }
        return true;
    }

    private boolean freefall() {
        //It returns a boolean so it can return in the onFling method
        while (true) {
            if (!isUnderOccupied(tetronomino.getBlockXs(), tetronomino.getBlockYs())) {
                tetronomino.y++;
                tetronomino.adjustPos(tetronomino.type, tetronomino.rotation);
            } else {
                verticalCollision();

                return true;
            }
        }
    }

    public void checkToDeleteRow() {
        int usedCounter = 0;
        for (int y = 0; y < NUM_ROWS; y++) {
            //Check if row is full
            for (int x = 0; x < NUM_COLUMNS; x++) {
                if (usedSpaces[x + y * NUM_COLUMNS]) {
                    usedCounter++;
                    if (usedCounter == NUM_COLUMNS) {
                        //set used spaces to false
                        System.out.println("delete line " + y);
                        for (int i = 0; i < NUM_COLUMNS; i++) {
                            usedSpaces[i + y * NUM_COLUMNS] = false;
                        }
                        //delete squares in render
                        for (int i = 0; i < blocksToRender.size(); i++) {
                            if (blocksToRender.get(i).y == y) {
                                blocksToRender.remove(i);
                                //adjust i because the list is shifted when an element is removed
                                i--;

                            }
                        }
                        blockFreefall(y);
                    }
                }
            }
            usedCounter = 0;

        }

    }

    public void blockFreefall(int nLine) {
        //activate freefall
        System.out.println("start freefall");
        //first sort the array
        Collections.sort(blocksToRender, new Comparator<Square>() {//FIXME LAMBDA WHAT?
            @Override
            public int compare(Square o1, Square o2) {
                return o2.y - o1.y;
            }
        });

        for (int i = 0; i < blocksToRender.size(); i++) {
            System.out.println("Blocks y cordinate: " + blocksToRender.get(i).y);
            if (blocksToRender.get(i).y < nLine) {//Si el y del cuadrado esta arriba de la linea iniciar algoritmo de freefall
                usedSpaces[blocksToRender.get(i).x + blocksToRender.get(i).y * NUM_COLUMNS] = false;
                while (true) {
                    System.out.println("x: " + blocksToRender.get(i).x + " y " + blocksToRender.get(i).y + " NC " + NUM_COLUMNS);
                    if (usedSpaces[blocksToRender.get(i).x + (blocksToRender.get(i).y + 1) * NUM_COLUMNS]) {
                        System.out.println("did block hit the ground");
                        usedSpaces[blocksToRender.get(i).x + blocksToRender.get(i).y * NUM_COLUMNS] = true;
                        break;
                    } else {        //Creo que el problema es que puede revisar un bloque sobre otro que se va a mover, nesesito revisarlos en un orden de abajo hacia arriba, lo que significa
                        //                  ordenar el blocksToRender por su valor y de mayor a menor.
                        blocksToRender.get(i).y++;
                    }
                    //Al comienzo de cada freefall hay que eliminar su "hitbox"
                }
            }
        }
    }

    @Override
    public int compare(Square o1, Square o2) {
        return o1.y - o2.y;
    }
}
//TODO: Delete Lines: ya se puede detectar si una linea esta llena y cual de las lineas esta llena lo que falta es; frefall, change the array that holds the tetros to render to instead hold squares to render, then you only need to eliminate the squares.