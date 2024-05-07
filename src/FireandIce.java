import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;

import enigma.console.TextAttributes;
import enigma.core.Enigma;

import static java.awt.Color.*;

public class FireandIce {
    static enigma.console.Console cn = Enigma.getConsole("Hallo", 75, 23, 20, 6);

    public KeyListener klis;

    // ------ Standard variables for keyboard ------
    public int keypr; // key pressed?
    public int rkey; // key (for press/release)
    //---------------------------------------------
    static Random random = new Random();
    static int maxComputerNum = 4;//optional
    static CircularQueue pawnQueue = new CircularQueue(maxComputerNum);

    public static char[][] maze = new char[23][53];

    Player player;

    int time;
    int loopcount;
    Random rnd;
    int px ;
    int py;

    Ice[] ices;
    Coordinates[] iceCoordinates ;
    boolean[] isIceSpreadDone ;
    int[] countForIceSpread ;
    int lastIceIndex;

    Fire[] fires = new Fire[50];
    Coordinates[] fireCoordinates = new Coordinates[50];
    boolean[] isFireSpreadingDone = new boolean[50];
    int[] countForFireSpread = new int[50];
    int lastFireIndex = 0;

    ComputerPawn pawn;
    CircularQueue inputQueue;

    FireandIce() throws Exception {

        Scanner maze_file = null;
        try {
            maze_file = new Scanner(new File("maze.txt"));
        } catch (FileNotFoundException e) {
            FileWriter create = new FileWriter("maze.txt");
            create.close();
        }
        int index = 0;
        while (maze_file.hasNextLine()) {

            String input = maze_file.nextLine();
            if (!input.equals("")) {
                maze[index] = input.toCharArray();
                for (int i = 0; i < maze[index].length; i++) {
                    if(maze[index][i] == 'C'){
                        pawnQueue.enqueue(new ComputerPawn(index,i));
                    }
                }
                index++;
            }
        }
        maze_file.close();
        //maze textinde neden computer arıyoruz


        klis = new KeyListener() {
            public void keyTyped(KeyEvent e) {//silinebilir
            }

            public void keyPressed(KeyEvent e) {
                if (keypr == 0) {
                    keypr = 1;
                    rkey = e.getKeyCode();
                }
            }

            public void keyReleased(KeyEvent e) {//silinebilir
            }
        };
        FireandIce.cn.getTextWindow().addKeyListener(klis);
        // ----------------------------------------------------


        inputQueue = new CircularQueue(10);
        time = 0;
        loopcount = 0;
        rnd = new Random();
        px = rnd.nextInt(52);
        py = rnd.nextInt(22);
        while (maze[py][px] == '#') {
            px = rnd.nextInt(52);
            py = rnd.nextInt(22);
        }
        player = new Player(px, py);
        updateMaze( px, py, 'P', null);

        new Ice(cn);
        ices = new Ice[50];
        iceCoordinates = new Coordinates[50];
        isIceSpreadDone = new boolean[50];
        countForIceSpread = new int[50];
        lastIceIndex = 0;

        fires = new Fire[50];
        fireCoordinates = new Coordinates[50];
        isFireSpreadingDone = new boolean[50];
        countForFireSpread = new int[50];
        lastFireIndex = 0;

        generateInitialInputQueue();


        while (player.getHealth() > 0) {

            checkDamage();

            if (loopcount % 20 == 0) {
                //if input queue has fire adds a new fire object
                if (inputQueue.peek().toString().equals("-")) {
                    int rand_x;
                    int rand_y;
                    while (true) {
                        rand_x = random.nextInt(53);
                        rand_y = random.nextInt(23);
                        if (maze[rand_y][rand_x] == ' ') {
                            break;
                        }
                    }
                    fireCoordinates[lastFireIndex] = new Coordinates(rand_x, rand_y);
                    fires[lastFireIndex] = new Fire(cn, maze, fireCoordinates[lastFireIndex]);
                    lastFireIndex++;
                }
                //if input queue has computer adds a new computer pawn
                else if (inputQueue.peek().toString().equals("C")) {
                    if (pawnQueue.size()<maxComputerNum) {

                        int rand_x;
                        int rand_y;
                        while (true) {
                            rand_x = random.nextInt(53);
                            rand_y = random.nextInt(23);
                            if (maze[rand_y][rand_x] == ' ') {
                                break;
                            }

                        }
                        pawn = new ComputerPawn(rand_y,rand_x);
                        //pawn.setCoordinates(rand_x,rand_y);
                        pawnQueue.enqueue(pawn);
                        maze[rand_y][rand_x] = 'C';
                    }

                }
                //else adds the char to the maze array
                else {
                    int rand_x;
                    int rand_y;
                    while (true) {
                        rand_x = random.nextInt(53);
                        rand_y = random.nextInt(23);
                        if (maze[rand_y][rand_x] == ' ') {
                            break;
                        }
                    }
                    maze[rand_y][rand_x] = inputQueue.peek().toString().charAt(0);
                }
                printMaze();
                addRandomElementToInputQueue();
            }

            // Fire Spreading
            for (int k = 0; k < fires.length; k++) {
                if ((!isFireSpreadingDone[k]) && fires[k] != null) {

                    fires[k].increaseTimer();
                    printMaze();
                    countForFireSpread[k]++;
                    if (countForFireSpread[k] == 150) {
                        isFireSpreadingDone[k] = true;
                    }
                }
            }

            // Ice Spreading
            for (int k = 0; k < ices.length; k++) {
                if ((!isIceSpreadDone[k]) && ices[k] != null) {

                    ices[k].increaseTimer();
                    printMaze();
                    countForIceSpread[k]++;
                    if (countForIceSpread[k] == 125) {
                        isIceSpreadDone[k] = true;
                    }
                }
            }


            // if keyboard button pressed
            if (keypr == 1) {

                if (rkey == KeyEvent.VK_LEFT) {

                    if (maze[py][px - 1] != '#' && maze[py][px - 1] != '+' && maze[py][px - 1] != '-') {
                        updateMaze( px, py, ' ', null);
                        px--;
                        checkTreasure();
                        checkIcePack();
                        updateMaze( px, py, 'P', null);
                    }
                    player.setDirection(-1, 0);//direction left to use packed ice
                    player.setCoordinates(px , py);
                } else if (rkey == KeyEvent.VK_RIGHT) {

                    if (maze[py][px + 1] != '#' && maze[py][px + 1] != '+' && maze[py][px + 1] != '-') {
                        updateMaze( px, py, ' ', null);
                        px++;
                        checkTreasure();
                        checkIcePack();
                        updateMaze( px, py, 'P', null);
                        int x = 1;
                        int y = 0;
                    }
                    player.setDirection(1, 0);
                    player.setCoordinates(px, py);
                } else if (rkey == KeyEvent.VK_UP) {

                    if (maze[py - 1][px] != '#' && maze[py - 1][px] != '+' && maze[py - 1][px] != '-') {
                        updateMaze( px, py, ' ', null);
                        py--;
                        checkTreasure();
                        checkIcePack();
                        updateMaze( px, py, 'P', null);
                    }
                    player.setDirection(0, -1);
                    player.setCoordinates(px, py);
                } else if (rkey == KeyEvent.VK_DOWN) {

                    if (maze[py + 1][px] != '#' && maze[py + 1][px] != '+' && maze[py + 1][px] != '-') {
                        updateMaze( px, py, ' ', null);
                        py++;
                        checkTreasure();
                        checkIcePack();
                        updateMaze( px, py, 'P', null);
                    }
                    player.setDirection(0, 1);
                    player.setCoordinates(px, py );
                } else if (rkey == KeyEvent.VK_SPACE && player.getPackedIceCount() > 0) {
                    ices[lastIceIndex] = new Ice(player.getCoordinates(), player.getDirection(),maze);
                    lastIceIndex++;
                    player.usePackedIce();
                } else if (rkey == KeyEvent.VK_Q) { // Hata kontrollerinde oyunu durdurmak için (silinecek)
                    printMaze();
                    Thread.sleep(50000);
                }
                printMaze();


                keypr = 0; // last action
            }

            Thread.sleep(100);
            movePawns();

            loopcount++;
            if (loopcount % 10 == 0) {
                time++;
            }
        }

    }

    void checkTreasure() {
        if (maze[py][px] == '1') {
            player.addScore(3);
        } else if (maze[py][px] == '2') {
            player.addScore(10);
        } else if (maze[py][px] == '3') {
            player.addScore(30);
        }
    }

    void checkIcePack() {
        if (maze[py][px] == '@') {
            player.addPackedIce();
        }
    }

    void checkDamage() {//bir kere kullandım çok da gerekli değil ama şık
        if (maze[py][px - 1] == '-' || maze[py - 1][px] == '-' || maze[py + 1][px] == '-' || maze[py][px + 1] == '-') {
            player.decreaseHealth(1);
        } else if (maze[py][px - 1] == 'C' || maze[py - 1][px] == 'C' || maze[py + 1][px] == 'C' || maze[py][px + 1] == 'C') {
            if (maze[py][px - 1] == 'C') {
                player.decreaseHealth(50);
            }
            if (maze[py][px + 1] == 'C') {
                player.decreaseHealth(50);
            }
            if (maze[py + 1][px] == 'C') {
                player.decreaseHealth(50);
            }
            if (maze[py - 1][px] == 'C') {
                player.decreaseHealth(50);
            }


        }
    }


    // Maze Update Function//artık gerekli değil??hepsinin yerine fonksiyonun ilk satırını yazabiliriz
    public void updateMaze(int x, int y, char ch, TextAttributes color) {
        maze[y][x] = ch;
        cn.getTextWindow().output(x, y, ch);
    }
    // Usage
    //updateMaze( 5,5,'A', red);

    public void cleanScreen() {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                cn.getTextWindow().setCursorPosition(j, i);
                cn.getTextWindow().output(' ');
            }
        }
    }

    public void printMaze() {
        TextAttributes wallColor = new TextAttributes(blue, blue);
        TextAttributes iceColor = new TextAttributes(cyan);
        TextAttributes fireColor = new TextAttributes(red);
        TextAttributes computerColor = new TextAttributes(green);

        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (maze[i][j] == '#') {
                    cn.getTextWindow().setCursorPosition(j, i);
                    cn.getTextWindow().output(maze[i][j], wallColor);
                } else if (maze[i][j] == '+') {
                    cn.getTextWindow().setCursorPosition(j, i);
                    cn.getTextWindow().output(maze[i][j], iceColor);
                } else if (maze[i][j] == '-') {
                    cn.getTextWindow().setCursorPosition(j, i);
                    cn.getTextWindow().output(maze[i][j], fireColor);
                } else if (maze[i][j] == 'C') {
                    cn.getTextWindow().setCursorPosition(j, i);
                    cn.getTextWindow().output(maze[i][j], computerColor);
                } else {
                    cn.getTextWindow().setCursorPosition(j, i);
                    cn.getTextWindow().output(maze[i][j]);
                }

            }
        }

        //time
        cn.getTextWindow().setCursorPosition(55, 2);
        System.out.println("Time     : " + time);
        //for player
        cn.getTextWindow().setCursorPosition(55, 10);
        System.out.println("P.Health : " + player.getHealth() + " ");
        cn.getTextWindow().setCursorPosition(55, 12);
        System.out.println("P.Score  : " + player.getScore());
        cn.getTextWindow().setCursorPosition(55, 14);
        System.out.println("P.Ice    : " + player.getPackedIceCount()+"    ");
        //for computer
        // Değişecek!!
        cn.getTextWindow().setCursorPosition(55, 17);
        System.out.println("C.Health : ");
        cn.getTextWindow().setCursorPosition(55, 19);
        System.out.println("C.Score  : " );
    }

    public void generateInitialInputQueue() {
        for (int i = 0; i < 10; i++) {
            inputQueue.enqueue(generateInputQueueElement());
        }
        printInputQueue();
    }

    public void addRandomElementToInputQueue() {
        inputQueue.dequeue();
        inputQueue.enqueue(generateInputQueueElement());
        printInputQueue();
    }

    public void printInputQueue() {
        TextAttributes blackWhite = new TextAttributes(Color.black, Color.white);
        cn.getTextWindow().setCursorPosition(55, 5);
        cn.getTextWindow().output("<<<<<<<<<<", blackWhite);
        cn.getTextWindow().setCursorPosition(55, 7);
        cn.getTextWindow().output("<<<<<<<<<<", blackWhite);
        cn.getTextWindow().setCursorPosition(55, 6);
        for (int i = 0; i < 10; i++) {
            System.out.print(inputQueue.peek());
            inputQueue.enqueue(inputQueue.dequeue());
        }
    }

    public static Object generateInputQueueElement() {
        Object returnData;
        int randomNumber = random.nextInt(30);
        if (randomNumber < 5) {
            returnData = "1";
        } else if (randomNumber < 10) {
            returnData = "2";
        } else if (randomNumber < 15) {
            returnData = "3";
        } else if (randomNumber < 21) {
            returnData = "-";
        } else if (randomNumber < 27) {
            returnData = "@";
        } else {
            returnData = "C";

        }
        return returnData;
    }



    public static boolean isAtPawnLimit() {

        return pawnQueue.size() == maxComputerNum;

    }

	/*public static void generateComputerPawn(){
		if(pawnQueue.size()<maxComputerNum){
			pawnQueue.enqueue(new ComputerPawn());
		}
	}*/

    public char[][] getMaze() {
        return maze;
    }

    public boolean isValidDestination(int x, int y) {

        boolean isNotWall = maze[y][x] != '#';
        boolean isNotFire = maze[y][x] != '-';
        boolean isNotIce = maze[y][x] != '+';

        return isNotIce && isNotFire && isNotWall;
    }

    public static void movePawns(){
        int count = pawnQueue.size();
        for (int i = 0; i < count; i++) {
            ComputerPawn pawn = (ComputerPawn) pawnQueue.peek();
            Coordinates pawnLocation = pawn.getCoordinates();
            Coordinates closestTreasure = getClosestTreasureToPawn(pawnLocation);
            int quadrant = 0;
            int pawnX = pawnLocation.getX();
            int pawnY = pawnLocation.getY();
            int treasureX = closestTreasure.getX();
            int treasureY = closestTreasure.getY();;
            CircularQueue destQueue = new CircularQueue(4);
            if(treasureX>=pawnX && treasureY >= pawnY){
                quadrant = 4;
                //pawnx, pawny+1
                //pawnx +1, pawny
                destQueue.enqueue(new Coordinates(0,1));
                destQueue.enqueue(new Coordinates(1,0));
                destQueue.enqueue(new Coordinates(0,-1));
                destQueue.enqueue(new Coordinates(-1,0));
            }
            else if(treasureX>=pawnX && treasureY<=pawnY){
                quadrant = 2;
                //pawnx, pawny+1
                //pawnx-1,pawny
                destQueue.enqueue(new Coordinates(0,1));
                destQueue.enqueue(new Coordinates(-1,0));
                destQueue.enqueue(new Coordinates(0,-1));
                destQueue.enqueue(new Coordinates(1,0));
            }
            else if(treasureX<=pawnX && treasureY>=pawnY){
                quadrant = 3;
                //pawnx, pawny-1
                //pawnx+1, pawny
                destQueue.enqueue(new Coordinates(0,-1));
                destQueue.enqueue(new Coordinates(1,0));
                destQueue.enqueue(new Coordinates(0,1));
                destQueue.enqueue(new Coordinates(1,0));
            }
            else {
                quadrant =1;
                //pawnx, pawny-1
                //pawnx-1,pawny

                destQueue.enqueue(new Coordinates(0,-1));
                destQueue.enqueue(new Coordinates(-1,0));
                destQueue.enqueue(new Coordinates(0,1));
                destQueue.enqueue(new Coordinates(1,0));
            }
            for (int j = 0; j < 4; j++) {
                Coordinates c = (Coordinates)destQueue.peek();
                int destX = pawnX+c.getX();
                int destY = pawnY+c.getY();
                char dest = maze[destX][destY];
                if (dest == ' '||dest == '1' || dest == '2' || dest == '3') {
                    maze[destX][destY] = 'C';
                    maze[pawnX][pawnY] = ' ';
                    pawn.setCoordinates(destX,destY);
                    break;
                }
                else{
                    destQueue.enqueue(destQueue.dequeue());
                }
            }
            pawnQueue.enqueue(pawnQueue.dequeue());


        }
    }

    public static Coordinates getClosestTreasureToPawn(Coordinates pawn){
        int pawnX = pawn.getX();
        int pawnY = pawn.getY();
        double minDistance = 1000.0;

        CircularQueue treasureQueue = new CircularQueue(50);
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                if(maze[i][j] == '1'||maze[i][j] == '2'||maze[i][j] == '3'){
                    double distance = Math.sqrt(Math.pow(pawnX-pawnY,2) + Math.pow(j-i,2));
                    if(distance<minDistance){
                        treasureQueue.dequeue();
                        treasureQueue.enqueue(new Coordinates(i,j));
                        minDistance = distance;
                    }


                }

            }

        }
        return (Coordinates) treasureQueue.peek();



    }


}