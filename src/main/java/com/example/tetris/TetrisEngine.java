package com.example.tetris;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.view.GestureDetectorCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class TetrisEngine extends SurfaceView implements Runnable, GestureDetector.OnGestureListener, Comparator<Square> {

    //Render
    private SurfaceHolder surfaceHolder;    //FIXME solve the probable deadlocks.
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
    private int deletedRows = 0;
    private int lvl = 1;
    private int pauseBlockX = screenX - 50;
    private int pauseBlockY = 0;
    private boolean canSwapTetro = false;
    private Tetronomino onHoldTetro;
    private boolean isPause = false;
    private boolean clickInPause = false;
    private SQLiteHelper sqlHandler;


    //Time
    private long nextFrameTime;
    private long nextGameClock;

    private Object pauseLock;
    private boolean pause;

    private boolean Running = true;
    private Context context;
    private final double FPS = 60;
    private double timer = 2;
    private final long MILLIS_PER_SECOND = 1000;
    Thread thread;


    //Other
    GestureDetectorCompat mDetector;
    private Rect pauseRect;
    Activity activity;
    public final int REQUEST_PAUSE_CODE = 01;
    public final int REQUEST_SAVE_SCORE_CODE = 02;

    public TetrisEngine(Context context, int w, int h, Activity activity) {
        super(context);
        screenX = w;
        screenY = h;
        this.context = context;
        paint = new Paint();
        surfaceHolder = getHolder();
        blockSize = screenX / NUM_COLUMNS;
        blockHeight = screenY / NUM_ROWS;
        mDetector = new GestureDetectorCompat(context, this);
        thread = new Thread(this);

        sqlHandler = new SQLiteHelper(context, "SCORE_DATABASE", null, 3);
        pauseRect = new Rect(screenX - 75, 0, screenX, 75);
        newGame();
        this.activity = activity;
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
            while (pause) {
                System.out.println("inPause");
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }

    private void gameOver() {

        Running = false;
        Intent intent = new Intent(context, SaveScoreActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("tetrosPlaced", tetrosPlaced);
        intent.putExtra("deletedRows", deletedRows);
        activity.startActivityForResult(intent, REQUEST_SAVE_SCORE_CODE);

    }

    private void newGame() {
        //game
        score = 0;
        tetrosPlaced = 0;
        deletedRows = 0;
        lvl = 0;
        timer = 2;


        pendingTypes = new ArrayList<>();
        Running = true;
        usedSpaces = new boolean[NUM_COLUMNS * NUM_ROWS + NUM_COLUMNS];
        for (int i = 0; i < NUM_COLUMNS; i++) {
            usedSpaces[i + (NUM_ROWS) * NUM_COLUMNS] = true;
        }
        tetronomino = newTetro();
        blocksToRender = new ArrayList<>();

        nextGameClock = System.currentTimeMillis();
        nextFrameTime = System.currentTimeMillis();

        Thread thread = new Thread(this);
        thread.start();
    }

    private void verticalCollision() {
        int[] tetroXs = tetronomino.getBlockXs();
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
                    //check if inside the square
                    if ((pauseRect.left < event.getX() && event.getX() < pauseRect.right) && (pauseRect.top < event.getY() && event.getY() < pauseRect.bottom)) {
                        System.out.println("inside the pause square");
                        clickInPause = true;
                        pauseGame();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (onFling) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:     //TODO: al transformar mueve el tetronimo para que este dentro del espacio.
                    if (clickInPause) {
                        clickInPause = false;
                        return true;
                    }
                    float x = event.getX();
                    //Rotate if tap quick
                    if (event.getY() > 3 * screenY / 4) {
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

        return true;
    }

    private void pauseGame() {
        //Create a small window, blurr the game as a background, show score, show blocks placed, show lvl
        //TODO: start activity for result, return a string if the string is "quit" finish mainActivity, if the
        synchronized (this) {
            pause = true;
            System.out.println("Pause");
            this.notifyAll();
            Intent intent = new Intent(context, PauseActivity.class);
            intent.putExtra("score", score);
            activity.startActivityForResult(intent, REQUEST_PAUSE_CODE);

        }
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
                drawBlock(Bx, By, canvas, Color.BLACK, 0x55555555);//FIXME why did the background turn blue- temp fix directly put the color
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
            drawTetromino(canvas, tetronomino);

            //Draw UI
            paint.setTextSize(40);
            paint.setColor(Color.WHITE);
            canvas.drawText("Score: " + score, 20, 30, paint);
            canvas.drawText("Level: " + lvl, 20, 70, paint);
            //draw nextPiece
            String nextType = pendingTypes.get(0);
            canvas.drawText("Next Piece: " + nextType, screenX / 2, 70, paint);
            if (onHoldTetro != null) {
                canvas.drawText("Holding: " + onHoldTetro.type, screenX / 2, 120, paint);
            }
            //TODO: DRAW BUTTONS
            //canvas.drawBitmap(R.drawable.pause_image, pauseBlockX, 0, paint);
            //draw pause rect?
            paint.setColor(Color.BLUE);

            //lambda?
            //Draw Pause icon
            {//FIXME: would it not make more sense to extract the bitmap of an image.
                int x = 75;
                int y = 75;
                //Draw pause icon
                //draw margin in white
                paint.setColor(Color.WHITE);
                canvas.drawRect(screenX - x, 0, screenX, y, paint);
                //draw black interior
                paint.setColor(Color.BLACK);
                canvas.drawRect(screenX - (x - 5), 0, screenX, (y - 5), paint);


                //draw left rect
                paint.setColor(Color.WHITE);
                //canvas.drawRect(screenX - (x + 2), 3, screenX, (y + 2);
                //draw right rect

            }


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
        synchronized (this) {
            pause = true;
            this.notifyAll();
        }
    }

    public void resume() {
        synchronized (this) {
            pause = false;
            this.notifyAll();
        }
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

    public void drawTetromino(Canvas canvas, Tetronomino tetronomino) {
        int[] tetroXs = tetronomino.getBlockXs();
        int[] tetroYs = tetronomino.getBlockYs();

        for (int i = 0; i < 4; i++) {
            drawBlock(tetroXs[i], tetroYs[i], canvas, Color.BLACK, tetronomino.getColor());
        }
    }

    private Tetronomino newTetro() {
        canSwapTetro = true;
        String tempType = null;
        if (pendingTypes.size() <= 1) {
            if (pendingTypes.size() == 1) {
                //Create temp to add
                tempType = pendingTypes.get(0);
            }
            pendingTypes = CreateNewArrayOfTypes();
            if (tempType != null) {
                pendingTypes.add(tempType);
            }

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
            case 10:
                lvl = 2;
                timer = 2.5;
                break;
            case 20:
                lvl = 3;
                timer = 3;
                break;
            case 30:
                lvl = 3;
                timer = 4;
                break;
            case 40:
                lvl = 4;
                timer = 5;
                break;
            case 50:
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
        testTetro.y = tetronomino.y;
        testTetro.adjustPos(testTetro.type, tetronomino.rotation);
        int[] tetroXs = testTetro.getBlockXs();
        int[] tetroYs = testTetro.getBlockYs();

        int x;
        int y;
        for (int i = 0; i < tetroXs.length; i++) {
            x = tetroXs[i];
            y = tetroYs[i];

            if (x < 0 || x >= NUM_COLUMNS) {
                return false;
            }
            System.out.println("x: " + x + "y: " + y);
            try {
                if (usedSpaces[x + y * NUM_COLUMNS]) {
                    System.out.println("horizontal collision");
                    return false;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
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
    public void onLongPress(MotionEvent e) {//FIXME there is some threading mistake with the hold piece mechanic, also it takes to long with the longpress

        System.out.println("onLongPress");
        if (canSwapTetro) {
            canSwapTetro = false;

            if (onHoldTetro == null) {
                System.out.println("it is null");
                onHoldTetro = tetronomino;
                //create a new one since onHold is empty
                tetronomino = new Tetronomino(pendingTypes.get(0));
                pendingTypes.remove(0);
            } else {
                System.out.println("it is not null");
                //Swap tetros
                Tetronomino temp;
                temp = tetronomino;
                tetronomino = onHoldTetro;
                onHoldTetro = temp;
            }
            tetronomino.x = 4;
            tetronomino.y = 0;
            this.notifyAll();
        }
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
        System.out.println("CheckDeleteRow");
        int usedCounter = 0;
        int numberOfLines = 0;
        for (int y = 0; y < NUM_ROWS; y++) {
            //Check if row is full
            for (int x = 0; x < NUM_COLUMNS; x++) {
                if (usedSpaces[x + y * NUM_COLUMNS]) {
                    usedCounter++;
                    if (usedCounter == NUM_COLUMNS) {
                        //set used spaces to false
                        System.out.println("delete line " + y);
                        numberOfLines++;
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
        System.out.println("n of lines: " + numberOfLines);
        switch (numberOfLines) {
            case 1:
                score += 100;
                break;
            case 2:
                score += 200;
                break;
            case 3:
                score += 400;
                break;
            case 4:
                score += 800;
                break;
        }

        deletedRows += numberOfLines;
    }

    public void blockFreefall(int nLine) {  //FIXME: the blocks should only fall the number of lines that were eliminated or it can produce reacction chains that break the game.
        //activate freefall
        System.out.println("start freefall");
        //first sort the array from the lower ones to the higher ones
        //Lambda expresions are usefull for one use functions.
        Collections.sort(blocksToRender, (o1, o2) -> o2.y - o1.y);

        for (int i = 0; i < blocksToRender.size(); i++) {
            System.out.println("Blocks y cordinate: " + blocksToRender.get(i).y);
            if (blocksToRender.get(i).y < nLine) {//Si el y del cuadrado esta arriba de la linea iniciar algoritmo de freefall
                usedSpaces[blocksToRender.get(i).x + blocksToRender.get(i).y * NUM_COLUMNS] = false;

                System.out.println("nLine: " + nLine + "blockToRender.y: " + blocksToRender.get(i).y);
                blocksToRender.get(i).y++;
                System.out.println("did block hit the ground");
                usedSpaces[blocksToRender.get(i).x + blocksToRender.get(i).y * NUM_COLUMNS] = true;
            }
            //Al comienzo de cada freefall hay que eliminar su "hitbox"

        }
    }


    @Override
    public int compare(Square o1, Square o2) {
        return o1.y - o2.y;
    }


}

//TODO:
// 1. Collisiones laterales con tetros, DONE
// 2. pause to tab out, DONE
// 3. menu pause,   DONE
// 3.5 scores. DONE
// 4. Restablish fullscreen after notification. DONE?
// 5. Change the icon.   DONE
