import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;

import enigma.console.TextAttributes;
import enigma.core.Enigma;

import static java.awt.Color.*;

public class FireandIce {
    static enigma.console.Console cn = Enigma.getConsole("Fire and Ice", 75, 23, 20, 5);
    public KeyListener klis;

    // ------ Standard variables for keyboard ------
    public int keypr; // key pressed?
    public int rkey; // key (for press/release)
    //---------------------------------------------
    static Random random = new Random();
    static int maxComputerNum = 100;//optional
    static CircularQueue pawnQueue = new CircularQueue(maxComputerNum);

    public static char[][] maze = new char[23][53];

    static Player player;

    static int time;
    int loopcount;
    Random rnd;
    int px ;
    int py;
    boolean hardMode = false;
    static int computerScore = 0;

    Ice[] ices;
    Coordinates[] iceCoordinates ;
    boolean[] isIceSpreadDone ;
    int[] countForIceSpread ;
    int lastIceIndex;

    Fire[] fires;
    Coordinates[] fireCoordinates;
    boolean[] isFireSpreadingDone;
    int[] countForFireSpread;
    int lastFireIndex;

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
                index++;
            }
        }
        maze_file.close();

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
        ices = new Ice[100];
        iceCoordinates = new Coordinates[100];
        isIceSpreadDone = new boolean[100];
        countForIceSpread = new int[100];
        lastIceIndex = 0;

        fires = new Fire[100];
        fireCoordinates = new Coordinates[100];
        isFireSpreadingDone = new boolean[100];
        countForFireSpread = new int[100];
        lastFireIndex = 0;

        generateInitialInputQueue(maze);


        while (player.getHealth() > 0) {
            printMaze();
            checkDamage();

            if (loopcount % 20 == 0) {
                //if input queue has fire adds a new fire object
                if (inputQueue.peek().toString().equals("-")) {
                    int rand_x;
                    int rand_y;
                    while (true) {
                        rand_x = random.nextInt(52) + 1;
                        rand_y = random.nextInt(22) + 1;
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
                            rand_x = random.nextInt(52) + 1;
                            rand_y = random.nextInt(22) + 1;
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

                    if (maze[py][px - 1] != '#' && maze[py][px - 1] != '+' && maze[py][px - 1] != '-' && maze[py][px - 1] != 'C') {
                        updateMaze( px, py, ' ', null);
                        px--;
                        checkTreasure();
                        checkIcePack();
                        updateMaze( px, py, 'P', null);
                    }
                    player.setDirection(-1, 0);//direction left to use packed ice
                    player.setCoordinates(px , py);
                } else if (rkey == KeyEvent.VK_RIGHT) {

                    if (maze[py][px + 1] != '#' && maze[py][px + 1] != '+' && maze[py][px + 1] != '-' && maze[py][px + 1] != 'C') {
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

                    if (maze[py - 1][px] != '#' && maze[py - 1][px] != '+' && maze[py - 1][px] != '-' && maze[py - 1][px] != 'C') {
                        updateMaze( px, py, ' ', null);
                        py--;
                        checkTreasure();
                        checkIcePack();
                        updateMaze( px, py, 'P', null);
                    }
                    player.setDirection(0, -1);
                    player.setCoordinates(px, py);
                } else if (rkey == KeyEvent.VK_DOWN) {

                    if (maze[py + 1][px] != '#' && maze[py + 1][px] != '+' && maze[py + 1][px] != '-' && maze[py + 1][px] != 'C') {
                        updateMaze( px, py, ' ', null);
                        py++;
                        checkTreasure();
                        checkIcePack();
                        updateMaze( px, py, 'P', null);

                    }
                    player.setDirection(0, 1);
                    player.setCoordinates(px, py );
                } else if (rkey == KeyEvent.VK_SPACE && player.getPackedIceCount() > 0) {
                    if(maze[py + 1][px] == ' ' || maze[py][px + 1] == ' ' || maze[py - 1][px] == ' ' || maze[py][px - 1] == ' '){
                        ices[lastIceIndex] = new Ice(player.getCoordinates(), player.getDirection(),maze);
                        lastIceIndex++;
                        player.usePackedIce();
                    }
                } else if (rkey == KeyEvent.VK_Q) { // Hata kontrollerinde oyunu durdurmak için (silinecek)
                    printMaze();
                    Thread.sleep(5000);
                }
                printMaze();


                keypr = 0; // last action
            }
            if(loopcount %4 == 0){
                recalculatePawnPaths();
                movePawnsHardMode();
                printMaze();
            }
            Thread.sleep(100);
            loopcount++;
            if (loopcount % 10 == 0) {
                time++;
            }
        }
        player.setHealth(0);
        printMaze();
        cn.getTextWindow().setCursorPosition(55,20);
        System.out.println("GAME OVER");

    }
    public static int checkComputerTreasure(int x_coordinate,int y_coordinate){
        if(maze[x_coordinate][y_coordinate] == '1'){
            return 9;
        }
        else if(maze[x_coordinate][y_coordinate] == '2'){
            return 30;
        }else if(maze[x_coordinate][y_coordinate] == '3'){
            return 90;
        }
        return 0;
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
        //recalculatePawnPaths();
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
        TextAttributes wallColor = new TextAttributes(black, blue);
        TextAttributes iceColor = new TextAttributes(cyan);
        TextAttributes fireColor = new TextAttributes(red);
        TextAttributes computerColor = new TextAttributes(green);
        TextAttributes playerColor = new TextAttributes(black,orange);
        TextAttributes packedIce = new TextAttributes(cyan);
        TextAttributes lowHealthComputer = new TextAttributes(yellow );

        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (maze[i][j] == '#') {
                    cn.getTextWindow().setCursorPosition(j, i);
                    cn.getTextWindow().output('■', wallColor);
                } else if (maze[i][j] == '+') {
                    cn.getTextWindow().setCursorPosition(j, i);
                    cn.getTextWindow().output(maze[i][j], iceColor);
                } else if (maze[i][j] == '-') {
                    cn.getTextWindow().setCursorPosition(j, i);
                    cn.getTextWindow().output(maze[i][j], fireColor);
                } else if (maze[i][j] == 'C') {
                    cn.getTextWindow().setCursorPosition(j, i);
                    cn.getTextWindow().output(maze[i][j], computerColor);
                } else if (maze[i][j] == 'P') {
                    cn.getTextWindow().setCursorPosition(j, i);
                    cn.getTextWindow().output(maze[i][j], playerColor);
                }else if (maze[i][j] == '@') {
                    cn.getTextWindow().setCursorPosition(j, i);
                    cn.getTextWindow().output(maze[i][j], packedIce);
                }
                else {
                    cn.getTextWindow().setCursorPosition(j, i);
                    cn.getTextWindow().output(maze[i][j]);
                }
            }
        }
        int computerNumber = pawnQueue.size();
        for (int i = 0; i < computerNumber; i++) {
            ComputerPawn p = (ComputerPawn) pawnQueue.peek();
            if(p.getHealth() < 500){
                cn.getTextWindow().output(p.getCoordinates().getY(),p.getCoordinates().getX(),'C',lowHealthComputer);
            }
            pawnQueue.enqueue(pawnQueue.dequeue());
        }

        //time
        cn.getTextWindow().setCursorPosition(55, 8);
        System.out.println("Time     : " + time);
        //for player
        cn.getTextWindow().setCursorPosition(55, 11);
        System.out.println("P.Health : " + player.getHealth() + "  ");
        cn.getTextWindow().setCursorPosition(55, 12);
        System.out.println("P.Score  : " + player.getScore());
        cn.getTextWindow().setCursorPosition(55, 13);
        System.out.println("P.Ice    : " + player.getPackedIceCount()+"    ");
        //for computer
        // Değişecek!!
        cn.getTextWindow().setCursorPosition(55, 17);
        System.out.println("C.Score  : " + computerScore);
        cn.getTextWindow().setCursorPosition(55, 18);
        System.out.println("C.Robots  : " + pawnQueue.size() + " ");
        printInputQueue();

    }

    public void generateInitialInputQueue(char[][]maze) throws Exception {

        for (int i = 0; i < 10; i++) {
            Object inputQueueElement = generateInputQueueElement();
            if (inputQueueElement.toString().equals("-")) {
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
            else if (inputQueueElement.toString().equals("C")) {
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
            else{
                while(true){
                    int rand_x = random.nextInt(53);
                    int rand_y = random.nextInt(23);
                    if(maze[rand_y][rand_x] == ' '){
                        maze[rand_y][rand_x] = inputQueueElement.toString().charAt(0);
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < 10; i++) {
            inputQueue.enqueue(generateInputQueueElement());
        }
        printMaze();
    }

    public void addRandomElementToInputQueue() {
        inputQueue.dequeue();
        inputQueue.enqueue(generateInputQueueElement());
        printMaze();
    }

    public void printInputQueue() {
        TextAttributes blackWhite = new TextAttributes(Color.black, Color.white);
        cn.getTextWindow().setCursorPosition(55, 2);
        cn.getTextWindow().output("<<<<<<<<<<", blackWhite);
        cn.getTextWindow().setCursorPosition(55, 4);
        cn.getTextWindow().output("<<<<<<<<<<", blackWhite);
        cn.getTextWindow().setCursorPosition(55, 3);
        for (int i = 0; i < 10; i++) {
            System.out.print(inputQueue.peek());
            inputQueue.enqueue(inputQueue.dequeue());
        }
    }

    public Object generateInputQueueElement() {
        Object returnData;
        int randomNumber = random.nextInt(30);
        if (randomNumber < 5) {
            returnData = "1";
        } else if (randomNumber < 10) {
            returnData = "2";
        } else if (randomNumber < 11) {
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



	/*public static void generateComputerPawn(){
		if(pawnQueue.size()<maxComputerNum){
			pawnQueue.enqueue(new ComputerPawn());
		}
	}*/

    public static boolean isValidDestination(int x, int y) {

        int n = maze.length;
        int m = maze[0].length;
        return x >= 1 && x < n && y >= 1 && y < m && maze[x][y] != '#' && maze[x][y] !='-' && maze[x][y] !='+' && maze[x][y] !='C';
    }

    public static Coordinates getClosestTreasureToPawn(Coordinates pawn){
        int pawnX = pawn.getX();
        int pawnY = pawn.getY();
        double minDistance = 1000.0;

        CircularQueue treasureQueue = new CircularQueue(50);
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                if(maze[i][j] == '1'||maze[i][j] == '2'||maze[i][j] == '3'){
                    double distance = Math.sqrt(Math.pow(pawnX-i,2) + Math.pow(pawnY-j,2));
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

    public static void movePawnsHardMode(){
        boolean computerDied = false;
        int size = pawnQueue.size();
        for (int i = 0; i < size; i++) {
            ComputerPawn p = (ComputerPawn) pawnQueue.peek();
            if(p.getPath() == null || p.getPath().isEmpty()){
                //check fire or ice
                char pos = maze[p.getCoordinates().getX()][p.getCoordinates().getY()];
                char north = maze[p.getCoordinates().getX()-1][p.getCoordinates().getY()];
                char south = maze[p.getCoordinates().getX()+1][p.getCoordinates().getY()];
                char west = maze[p.getCoordinates().getX()][p.getCoordinates().getY()-1];
                char east = maze[p.getCoordinates().getX()][p.getCoordinates().getY()+1];
                if(pos == '+' ||  north == '+' || south == '+'|| west == '+'|| east == '+'){
                    p.decreaseHealth(200);
                }
                if(p.getHealth()<=0){
                    computerDied = true;
                    player.addScore(100);
                    maze[p.getCoordinates().getX()][p.getCoordinates().getY()] = ' ';
                    pawnQueue.dequeue();
                }
            }
            else{
                int srcX = p.getCoordinates().getX();
                int srcY = p.getCoordinates().getY();
                int dstX = ((Coordinates)p.getPath().peek()).getX();
                int dstY = ((Coordinates)p.getPath().peek()).getY();
                while(srcX == dstX && srcY == dstY){
                    p.getPath().dequeue();
                    dstX = ((Coordinates)p.getPath().peek()).getX();
                    dstY = ((Coordinates)p.getPath().peek()).getY();

                }


                if(maze[dstX][dstY] != 'P'){

                    //check fire or ice
                    char pos = maze[srcX][srcY];
                    char north = maze[srcX-1][srcY];
                    char south = maze[srcX+1][srcY];
                    char west = maze[srcX][srcY-1];
                    char east = maze[srcX][srcY+1];
                    if(pos == '+' ||  north == '+' || south == '+'|| west == '+'|| east == '+'){
                        p.decreaseHealth(200);
                    }
                    if(p.getHealth()<=0){
                        computerDied = true;
                        player.addScore(100);
                        maze[srcX][srcY] = ' ';
                        pawnQueue.dequeue();
                    }
                    else{
                        if(maze[dstX][dstY] != 'C'){
                            computerScore += checkComputerTreasure(dstX,dstY);
                            maze[srcX][srcY] = ' ';
                            maze[dstX][dstY] = 'C';
                            p.setCoordinates(dstX,dstY);
                            p.cycleNextDestination();
                        }
                    }
                }
            }
            if(!computerDied){
                pawnQueue.enqueue(pawnQueue.dequeue());
            }

        }
    }

    public static Queue findPath(Coordinates start, Coordinates destination) {
        int[] dx = {0, 0, 1, -1}; // Possible movements in x direction
        int[] dy = {1, -1, 0, 0};
        int n = maze.length;
        int m = maze[0].length;
        boolean[][] visited = new boolean[n][m];
        Coordinates[][] parent = new Coordinates[n][m];
        Queue queue = new Queue(50);

        queue.enqueue(start);
        visited[start.getX()][start.getY()] = true;

        while (!queue.isEmpty() && (destination != null)) {
            Coordinates current = (Coordinates) queue.dequeue();
            if ( current.getX() == destination.getX() && current.getY() == destination.getY()) {
                // Destination reached, construct path
                Queue path = new Queue(50);
                while (current != null) {
                    path.enqueue(current);
                    current = parent[current.getX()][current.getY()];
                }
                path.reverse();
                return path;
            }

            for (int i = 0; i < 4; i++) {
                int newX = current.getX() + dx[i];
                int newY = current.getY() + dy[i];
                if (isValidDestination(newX, newY) && !visited[newX][newY]) {
                    Coordinates next = new Coordinates(newX, newY);
                    queue.enqueue(next);
                    visited[newX][newY] = true;
                    parent[newX][newY] = current;
                }
            }
        }

        // No path found
        return null;
    }

    static void recalculatePawnPaths(){

        int pawn_amount = pawnQueue.size();

        for (int i = 0; i < pawn_amount; i++) {
            ComputerPawn p =(ComputerPawn) pawnQueue.peek();
            p.setPath(findPath(p.getCoordinates(),getClosestTreasureToPawn(p.getCoordinates())));
            pawnQueue.enqueue(pawnQueue.dequeue());

        }

    }



}