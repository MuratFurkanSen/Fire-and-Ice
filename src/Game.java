import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import enigma.core.Enigma;

public class Game {
    static enigma.console.Console cn = Enigma.getConsole("Hallo", 100, 23, 25, 6);

    public KeyListener klis;

	// ------ Standard variables for keyboard ------
	public int keypr; // key pressed?
	public int rkey; // key (for press/release)
	//---------------------------------------------
	static Random random = new Random();


    Game() throws Exception {
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
        for (int i = 0; i <  maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                cn.getTextWindow().setCursorPosition(j,i);
                cn.getTextWindow().output(maze[i][j]);
            }
        }
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



		CircularQueue inputQueue = new CircularQueue(10);
		String lastPlayerDirection = "";
        int time=0;
        int loopcount=0;
        Random rnd=new Random();
        int px=rnd.nextInt(52);
        int py=rnd.nextInt(22);
		boolean isFireSpreadingDone = true;
		int countForFireSpread = 0;
        while(maze[py][px]=='#') {
        	px=rnd.nextInt(52);
            py=rnd.nextInt(22);
        }
        Player player=new Player(px, py);
        updateMaze(maze, px, py, 'P', null);
		Ice[] ices = new Ice[50];
		int lastIceIndex = 0;

		Fire[] fires = new Fire[50];
		Coordinates[] fireCoordinates = new Coordinates[50];
		int lastFireIndex = 0;

		generateInitialInputQueue(inputQueue);

        while (player.getHealth()>0) {

        	checkDamage(maze, px, py, player);
			if(loopcount % 20 == 0){
				addRandomElementToInputQueue(inputQueue);
			}
			if((inputQueue.peek().toString().equals("-") && loopcount % 2 == 0) || !isFireSpreadingDone){
				if(isFireSpreadingDone){
					int rand_x;
					int rand_y;
					while(true){
						rand_x = random.nextInt(53);
						rand_y = random.nextInt(23);
						if(maze[rand_y][rand_x] == ' '){
							break;
						}
					}
					fireCoordinates[lastFireIndex] = new Coordinates(rand_x,rand_y);
					fires[lastFireIndex] = new Fire(cn,maze,fireCoordinates[lastFireIndex]);
					isFireSpreadingDone = false;
				}


				for (int i = 0; i <  maze.length; i++) {
					for (int j = 0; j < maze[i].length; j++) {
						cn.getTextWindow().setCursorPosition(j,i);
						cn.getTextWindow().output(maze[i][j]);
					}
				}

				countForFireSpread++;
				fires[lastFireIndex].increaseTimer();

				if(countForFireSpread == 50){
					lastFireIndex++ ;
					isFireSpreadingDone = true;
				}

			}

			if (keypr == 1) { // if keyboard button pressed
				if (rkey == KeyEvent.VK_LEFT || rkey == KeyEvent.VK_RIGHT || rkey == KeyEvent.VK_UP
						|| rkey == KeyEvent.VK_DOWN) {
					if (rkey == KeyEvent.VK_LEFT) {
						lastPlayerDirection = "left";

						if(maze[py][px-1]!='#'&&maze[py][px-1]!='+'&&maze[py][px-1]!='-') {
							updateMaze(maze, px, py, ' ', null);
							px--;
							checkTreasure(maze, px, py, player);
							checkIcePack(maze, px, py, player);
							updateMaze(maze, px, py, 'P', null);
						}
						player.setDirection(-1,0);//direction left to use packed ice
					}

					if (rkey == KeyEvent.VK_RIGHT) {
						lastPlayerDirection = "right";

						if(maze[py][px+1]!='#'&&maze[py][px+1]!='+'&&maze[py][px+1]!='-') {
						updateMaze(maze, px, py, ' ', null);
						px++;
						checkTreasure(maze, px, py, player);
						checkIcePack(maze, px, py, player);
						updateMaze(maze, px, py, 'P', null);
						int x = 1;
						int y = 0;
						}
						player.setDirection(1,0);
					}
					if (rkey == KeyEvent.VK_UP) {
						lastPlayerDirection = "up";

						if(maze[py-1][px]!='#'&&maze[py-1][px]!='+'&&maze[py-1][px]!='-') {
						updateMaze(maze, px, py, ' ', null);
						py--;
						checkTreasure(maze, px, py, player);
						checkIcePack(maze, px, py, player);
						updateMaze(maze, px, py, 'P', null);
						}
						player.setDirection(0,-1);
					}
					if (rkey == KeyEvent.VK_DOWN) {
						lastPlayerDirection = "down";

						if(maze[py+1][px]!='#'&&maze[py+1][px]!='+'&&maze[py+1][px]!='-') {
							updateMaze(maze, px, py, ' ', null);
							py++;
							checkTreasure(maze, px, py, player);
							checkIcePack(maze, px, py, player);
							updateMaze(maze, px, py, 'P', null);
							}
						player.setDirection(0,1);
					}

//					if(rkey == KeyEvent.VK_SPACE){
//						switch (lastPlayerDirection){
//							case "left":
//								fireCoordinates[lastFireIndex] = new Coordinates(px - 1,py);
//								for (int k = 0; k < 50; k++) {
//									fires[lastFireIndex].increaseTimer();
//									clearScreen(maze);
//									for (int i = 0; i <  maze.length; i++) {
//										for (int j = 0; j < maze[i].length; j++) {
//											cn.getTextWindow().setCursorPosition(j,i);
//											cn.getTextWindow().output(maze[i][j]);
//										}
//									}
//									Thread.sleep(800);
//								}
//								lastFireIndex++ ;
//								break;
//							case "right":
//								fireCoordinates[lastFireIndex] = new Coordinates(px + 1,py);
//								for (int k = 0; k < 50; k++) {
//									fires[lastFireIndex].increaseTimer();
//									clearScreen(maze);
//									for (int i = 0; i <  maze.length; i++) {
//										for (int j = 0; j < maze[i].length; j++) {
//											cn.getTextWindow().setCursorPosition(j,i);
//											cn.getTextWindow().output(maze[i][j]);
//										}
//									}
//									Thread.sleep(800);
//								}
//								lastFireIndex++ ;
//								break;
//							case "up":
//								fireCoordinates[lastFireIndex] = new Coordinates(px,py - 1);
//								for (int k = 0; k < 50; k++) {
//									fires[lastFireIndex].increaseTimer();
//									clearScreen(maze);
//									for (int i = 0; i <  maze.length; i++) {
//										for (int j = 0; j < maze[i].length; j++) {
//											cn.getTextWindow().setCursorPosition(j,i);
//											cn.getTextWindow().output(maze[i][j]);
//										}
//									}
//									Thread.sleep(800);
//								}
//								lastFireIndex++ ;
//								break;
//							case "down":
//								fireCoordinates[lastFireIndex] = new Coordinates(px,py + 1);
//								for (int k = 0; k < 50; k++) {
//									fires[lastFireIndex].increaseTimer();
//									clearScreen(maze);
//									for (int i = 0; i <  maze.length; i++) {
//										for (int j = 0; j < maze[i].length; j++) {
//											cn.getTextWindow().setCursorPosition(j,i);
//											cn.getTextWindow().output(maze[i][j]);
//										}
//									}
//									Thread.sleep(800);
//								}
//								lastFireIndex++ ;
//								break;
//						}
//					}

				}
				/*
				else if (rkey == KeyEvent.VK_SPACE){
					ices[lastIceIndex] = new Ice(player.,player.getDirection())
				}
				*/

				keypr = 0; // last action
			}
			cn.getTextWindow().setCursorPosition(55, 10);
        	System.out.println("P.Health : "+player.getHealth() + " ");
			cn.getTextWindow().setCursorPosition(70, 10);
        	System.out.println("   ");
			cn.getTextWindow().setCursorPosition(55, 15);
        	System.out.println("P.Score  : "+player.getScore());
			cn.getTextWindow().setCursorPosition(55, 20);
        	System.out.println("P.Ice    : "+player.getPackedIceCount());
        	Thread.sleep(100);
			for (int i = 0; i < ices.length; i++) {
				if (ices[i] != null){
					ices[i].increaseTimer(maze);
				}
			}

        	loopcount++;
        	if (loopcount%10==0) {
				time++;
			}
		}

    }

    void checkTreasure(char[][] maze,int px,int py,Player p) {
    	if(maze[py][px]=='1') {
    		p.addScore(3);
    	}
    	else if(maze[py][px]=='2') {
    		p.addScore(10);
    	}
    	else if(maze[py][px]=='3') {
    		p.addScore(30);
    	}
    }
    void checkIcePack(char[][] maze,int px,int py,Player p) {
    	if(maze[py][px]=='@') {
    		p.addPackedIce();
    	}
    }
    void checkDamage(char[][] maze,int px,int py,Player p) {//bir kere kullandım çok da gerekli değil ama şık
    	if(maze[py][px-1]=='-'||maze[py-1][px]=='-'||maze[py+1][px]=='-'||maze[py][px+1]=='-') {
			p.decreaseHealth(1);
		}
		else if(maze[py][px-1]=='C'||maze[py-1][px]=='C'||maze[py+1][px]=='C'||maze[py][px+1]=='C') {
			p.decreaseHealth(50);
		}
    }


    // Maze Update Function
    public void updateMaze(char[][] maze, int x, int y, char ch, enigma.console.TextAttributes color){
        maze[y][x] = ch;
        cn.getTextWindow().output(x,y, ch);
    }
    // Usage
    //updateMaze(maze, 5,5,'A', red);

	public void clearScreen(char[][]maze){
		for (int i = 0; i <  maze.length; i++) {
			for (int j = 0; j < maze[i].length; j++) {
				cn.getTextWindow().setCursorPosition(j,i);
				cn.getTextWindow().output(' ');
			}
		}
	}

	public static void generateInitialInputQueue(CircularQueue inputQueue){
		for (int i = 0; i < 10; i++) {
			inputQueue.enqueue(generateInputQueueElement());
		}
		printInputQueue(inputQueue);
	}

	public static void addRandomElementToInputQueue(CircularQueue inputQueue){
		inputQueue.dequeue();
		inputQueue.enqueue(generateInputQueueElement());
		printInputQueue(inputQueue);
	}

	public static void printInputQueue(CircularQueue inputQueue){
		cn.getTextWindow().setCursorPosition(55,5);
		System.out.print("<<<<<<<<<<");
		cn.getTextWindow().setCursorPosition(55,7);
		System.out.print("<<<<<<<<<<");
		cn.getTextWindow().setCursorPosition(55,6);
		for (int i = 0; i < inputQueue.size(); i++) {
			System.out.print(inputQueue.peek());
			inputQueue.enqueue(inputQueue.dequeue());
		}
	}

	public static Object generateInputQueueElement(){
		Object returnData = null;
		int randomNumber = random.nextInt(30);
		if(randomNumber < 5){
			returnData = "1";
		}else if(randomNumber < 10){
			returnData = "2";
		}else if(randomNumber < 15){
			returnData = "3";
		}else if(randomNumber < 21){
			returnData = "-";
		}else if(randomNumber < 27){
			returnData = "@";
		}
		else returnData = "C";
		return returnData;

	}


    }