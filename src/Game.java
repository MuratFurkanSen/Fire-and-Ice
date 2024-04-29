import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;

import enigma.core.Enigma;

public class Game {
    static enigma.console.Console cn = Enigma.getConsole("Hallo", 80, 23, 25, 6);
    public KeyListener klis;

    // ------ Standard variables for keyboard ------
    public int keypr; // key pressed?
    public int rkey; // key (for press/release)
    private Random random = new Random();


    Game() throws Exception {
        CircularQueue inputQueue = new CircularQueue(10);
        char[][] maze = new char[23][53];
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
        // Game Maze
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                cn.getTextWindow().setCursorPosition(j, i);
                cn.getTextWindow().output(maze[i][j]);
            }
        }
        // Filling up Input Queue at the Beginning
        for (int i = 0; i < 10; i++) {
            inputQueue.enqueue(newRandomInput());
        }
        // Place Inputs at Beginning
        while (!inputQueue.isEmpty()){
            int rand_x = random.nextInt(0,52);
            int rand_y = random.nextInt(0,23);
            if (maze[rand_y][rand_x] == ' '){
                maze[rand_y][rand_x] = (char) inputQueue.dequeue();
            }
        }
        // Filling up Input Queue after First Fill
        for (int i = 0; i < 10; i++) {
            inputQueue.enqueue(newRandomInput());
        }
        for (int i = 0; i < 10; i++) {
            cn.getTextWindow().setCursorPosition(55+i,5);
            cn.getTextWindow().output((char) inputQueue.peek());
            inputQueue.enqueue(inputQueue.dequeue());
        }
        cn.getTextWindow().setCursorPosition(55,3);
        cn.getTextWindow().output("Input");
        cn.getTextWindow().setCursorPosition(55,4);
        cn.getTextWindow().output("<<<<<<<<<<");
        cn.getTextWindow().setCursorPosition(55,6);
        cn.getTextWindow().output("<<<<<<<<<<");

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
        Game.cn.getTextWindow().addKeyListener(klis);
        // ----------------------------------------------------


        int time = 0;
        int loopcount = 0;
        Random rnd = new Random();
        int px = rnd.nextInt(52);
        int py = rnd.nextInt(22);
        while (maze[py][px] == '#') {
            px = rnd.nextInt(52);
            py = rnd.nextInt(22);
        }
        Player player = new Player(px, py);
        updateMaze(maze, px, py, 'P', null);
        Ice[] ices = new Ice[50];
        int lastIceIndex = 0;


        while (player.getHealth() > 0) {

            checkDamage(maze, px, py, player);

            if (keypr == 1) { // if keyboard button pressed
                if (rkey == KeyEvent.VK_LEFT || rkey == KeyEvent.VK_RIGHT || rkey == KeyEvent.VK_UP
                        || rkey == KeyEvent.VK_DOWN) {
                    if (rkey == KeyEvent.VK_LEFT) {

                        if (maze[py][px - 1] != '#' && maze[py][px - 1] != '+' && maze[py][px - 1] != '-') {
                            updateMaze(maze, px, py, ' ', null);
                            px--;
                            checkTreasure(maze, px, py, player);
                            checkIcePack(maze, px, py, player);
                            updateMaze(maze, px, py, 'P', null);
                        }
                        player.setDirection(-1, 0);//direction left to use packed ice
                    }

                    if (rkey == KeyEvent.VK_RIGHT) {
                        if (maze[py][px + 1] != '#' && maze[py][px + 1] != '+' && maze[py][px + 1] != '-') {
                            updateMaze(maze, px, py, ' ', null);
                            px++;
                            checkTreasure(maze, px, py, player);
                            checkIcePack(maze, px, py, player);
                            updateMaze(maze, px, py, 'P', null);
                            int x = 1;
                            int y = 0;
                        }
                        player.setDirection(1, 0);
                    }
                    if (rkey == KeyEvent.VK_UP) {
                        if (maze[py - 1][px] != '#' && maze[py - 1][px] != '+' && maze[py - 1][px] != '-') {
                            updateMaze(maze, px, py, ' ', null);
                            py--;
                            checkTreasure(maze, px, py, player);
                            checkIcePack(maze, px, py, player);
                            updateMaze(maze, px, py, 'P', null);
                        }
                        player.setDirection(0, -1);
                    }
                    if (rkey == KeyEvent.VK_DOWN) {
                        if (maze[py + 1][px] != '#' && maze[py + 1][px] != '+' && maze[py + 1][px] != '-') {
                            updateMaze(maze, px, py, ' ', null);
                            py++;
                            checkTreasure(maze, px, py, player);
                            checkIcePack(maze, px, py, player);
                            updateMaze(maze, px, py, 'P', null);
                        }
                        player.setDirection(0, 1);
                    }

                }
				/*
				else if (rkey == KeyEvent.VK_SPACE){
					ices[lastIceIndex] = new Ice(player.,player.getDirection())
				}
				*/

                keypr = 0; // last action
            }
            cn.getTextWindow().setCursorPosition(55, 10);
            System.out.println("P.Health : " + player.getHealth() + "   ");
            cn.getTextWindow().setCursorPosition(70, 10);
            System.out.println("   ");
            cn.getTextWindow().setCursorPosition(55, 15);
            System.out.println("P.Score  : " + player.getScore());
            cn.getTextWindow().setCursorPosition(55, 20);
            System.out.println("P.Ice    : " + player.getPackedIceCount());
            Thread.sleep(100);
            for (int i = 0; i < ices.length; i++) {
                if (ices[i] != null) {
                    ices[i].increaseTimer(maze);
                }

            }
            loopcount++;
            if (loopcount % 10 == 0) {
                time++;

            }
            if (time %2 == 0){
                while (!inputQueue.isFull()){
                    int rand_x = random.nextInt(0,52);
                    int rand_y = random.nextInt(0,23);
                    if (maze[rand_y][rand_x] == ' '){
                        maze[rand_y][rand_x] = (char) inputQueue.dequeue();
                    }
                }
                inputQueue.enqueue(newRandomInput());
                for (int i = 0; i < 10; i++) {
                    cn.getTextWindow().setCursorPosition(55+i,5);
                    cn.getTextWindow().output(' ');
                    inputQueue.enqueue(inputQueue.dequeue());
                }
                for (int i = 0; i < 10; i++) {
                    cn.getTextWindow().setCursorPosition(55+i,5);
                    cn.getTextWindow().output((char) inputQueue.peek());
                    inputQueue.enqueue(inputQueue.dequeue());
                }
            }
        }


    }

    void checkTreasure(char[][] maze, int px, int py, Player p) {
        if (maze[py][px] == '1') {
            p.addScore(3);
        } else if (maze[py][px] == '2') {
            p.addScore(10);
        } else if (maze[py][px] == '3') {
            p.addScore(30);
        }
    }

    void checkIcePack(char[][] maze, int px, int py, Player p) {
        if (maze[py][px] == '@') {
            p.addPackedIce();
        }
    }

    void checkDamage(char[][] maze, int px, int py, Player p) {//bir kere kullandım çok da gerekli değil ama şık
        if (maze[py][px - 1] == '-' || maze[py - 1][px] == '-' || maze[py + 1][px] == '-' || maze[py][px + 1] == '-') {
            p.decreaseHealth(1);
        } else if (maze[py][px - 1] == 'C' || maze[py - 1][px] == 'C' || maze[py + 1][px] == 'C' || maze[py][px + 1] == 'C') {
            p.decreaseHealth(50);
        }
    }


    // Maze Update Function
    public void updateMaze(char[][] maze, int x, int y, char ch, enigma.console.TextAttributes color) {
        maze[y][x] = ch;
        cn.getTextWindow().output(x, y, ch);
    }
    // Usage
    //updateMaze(maze, 5,5,'A', red);

    // Input Queue
    public char newRandomInput() {
            char returnData;
            int randomNumber = random.nextInt(30);
            if(randomNumber < 5){
                returnData = '1';
            }else if(randomNumber < 10){
                returnData = '2';
            }else if(randomNumber < 15){
                returnData = '3';
            }else if(randomNumber < 21){
                returnData = '-';
            }else if(randomNumber < 27){
                returnData = '@';
            }
            else returnData = 'C';

            return returnData;

    }

    }