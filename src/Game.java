import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import enigma.console.TextAttributes;
import enigma.core.Enigma;

import javax.swing.text.AttributeSet;

import static java.awt.Color.*;

public class Game {
    static enigma.console.Console cn = Enigma.getConsole("Hallo", 75, 23, 20, 6);

    public KeyListener klis;

	// ------ Standard variables for keyboard ------
	public int keypr; // key pressed?
	public int rkey; // key (for press/release)
	//---------------------------------------------
	static Random random = new Random();
	int time = 0;


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

        int loopcount=0;
        Random rnd=new Random();
        int px=rnd.nextInt(52);
        int py=rnd.nextInt(22);
        while(maze[py][px]=='#') {
        	px=rnd.nextInt(52);
            py=rnd.nextInt(22);
        }
        Player player=new Player(px, py);
        updateMaze(maze, px, py, 'P', null);

		new Ice(cn);
		Ice[] ices = new Ice[50];
		Coordinates[] iceCoordinates = new Coordinates[50];
		boolean[] isIceSpreadDone = new boolean[50];
		int[] countForIceSpread = new int[50];
		int lastIceIndex = 0;

		Fire[] fires = new Fire[50];
		Coordinates[] fireCoordinates = new Coordinates[50];
		boolean [] isFireSpreadingDone = new boolean[50];
		int[] countForFireSpread = new int[50];
		int lastFireIndex = 0;

		generateInitialInputQueue(inputQueue);


        while (player.getHealth()>0) {

        	checkDamage(maze, px, py, player);
			if(loopcount % 20 == 0){

				if(inputQueue.peek().toString().equals("-")){
					int rand_x;
					int rand_y;
					while(true){
						rand_x = random.nextInt(53);
						rand_y = random.nextInt(23);
						if(maze[rand_y][rand_x] == ' '){
							break;
						}
					}
					fireCoordinates[lastFireIndex] = new Coordinates( rand_x,rand_y);
					fires[lastFireIndex] = new Fire(cn,maze,fireCoordinates[lastFireIndex]);
					lastFireIndex++;
				} else {
					int rand_x;
					int rand_y;
					while(true){
						rand_x = random.nextInt(53);
						rand_y = random.nextInt(23);
						if(maze[rand_y][rand_x] == ' '){
							break;
						}
					}
					maze[rand_y][rand_x] = inputQueue.peek().toString().charAt(0);
				}
				printMaze(maze,player);
				addRandomElementToInputQueue(inputQueue);
			}

			// Fire Spreading
			for (int k = 0; k < fires.length; k++) {
				if((!isFireSpreadingDone[k]) && fires[k] != null) {

					fires[k].increaseTimer();
					printMaze(maze,player);
					countForFireSpread[k]++;
					if(countForFireSpread[k] == 150){
						isFireSpreadingDone[k] = true;
					}
				}
			}

			// Ice Spreading
			for (int k = 0; k < ices.length; k++) {
				if((!isIceSpreadDone[k]) && ices[k] != null) {

					ices[k].increaseTimer(maze);
					printMaze(maze,player);
					countForIceSpread[k]++;
					if(countForIceSpread[k] == 125){
						isIceSpreadDone[k] = true;
					}
				}
			}


			if (keypr == 1) { // if keyboard button pressed

					if (rkey == KeyEvent.VK_LEFT) {

						if(maze[py][px-1]!='#'&&maze[py][px-1]!='+'&&maze[py][px-1]!='-') {
							updateMaze(maze, px, py, ' ', null);
							px--;
							checkTreasure(maze, px, py, player);
							checkIcePack(maze, px, py, player);
							updateMaze(maze, px, py, 'P', null);
						}
						player.setDirection(-1,0);//direction left to use packed ice
						player.setCoordinates(px - 1,py);
					}

					else if (rkey == KeyEvent.VK_RIGHT) {

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
						player.setCoordinates(px +1 ,py);
					}
					else if (rkey == KeyEvent.VK_UP) {

						if(maze[py-1][px]!='#'&&maze[py-1][px]!='+'&&maze[py-1][px]!='-') {
						updateMaze(maze, px, py, ' ', null);
						py--;
						checkTreasure(maze, px, py, player);
						checkIcePack(maze, px, py, player);
						updateMaze(maze, px, py, 'P', null);
						}
						player.setDirection(0,-1);
						player.setCoordinates(px,py - 1);
					}
					else if (rkey == KeyEvent.VK_DOWN) {

						if(maze[py+1][px]!='#'&&maze[py+1][px]!='+'&&maze[py+1][px]!='-') {
							updateMaze(maze, px, py, ' ', null);
							py++;
							checkTreasure(maze, px, py, player);
							checkIcePack(maze, px, py, player);
							updateMaze(maze, px, py, 'P', null);
							}
						player.setDirection(0,1);
						player.setCoordinates(px,py + 1
						);
					}
					else if (rkey == KeyEvent.VK_SPACE && player.getPackedIceCount() > 0) {
						ices[lastIceIndex] = new Ice(player.getCoordinates(),player.getDirection());
						lastIceIndex++;
						player.usePackedIce();
					}

					printMaze(maze,player);





				keypr = 0; // last action
			}

        	Thread.sleep(100);

        	loopcount++;
        	if (loopcount % 10==0) {
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
			if(maze[py][px - 1]=='C'){
				p.decreaseHealth(50);
			}
			if(maze[py][px + 1]=='C'){
				p.decreaseHealth(50);
			}
			if(maze[py + 1][px]=='C'){
				p.decreaseHealth(50);
			}
			if(maze[py - 1][px]=='C'){
				p.decreaseHealth(50);
			}



		}
    }


    // Maze Update Function
    public void updateMaze(char[][] maze, int x, int y, char ch, enigma.console.TextAttributes color){
        maze[y][x] = ch;
        cn.getTextWindow().output(x,y, ch);
    }
    // Usage
    //updateMaze(maze, 5,5,'A', red);

	public void cleanScreen(char[][]maze){
		for (int i = 0; i <  maze.length; i++) {
			for (int j = 0; j < maze[i].length; j++) {
				cn.getTextWindow().setCursorPosition(j,i);
				cn.getTextWindow().output(' ');
			}
		}
	}

	public  void printMaze(char[][] maze,Player player){
		TextAttributes wallColor = new TextAttributes(blue, blue);
		TextAttributes iceColor = new TextAttributes(cyan);
		TextAttributes fireColor = new TextAttributes(red);

		for (int i = 0; i <  maze.length; i++) {
			for (int j = 0; j < maze[i].length; j++) {
				if(maze[i][j] == '#'){
					cn.getTextWindow().setCursorPosition(j,i);
					cn.getTextWindow().output(maze[i][j],wallColor);
				}
				else if (maze[i][j] == '+'){
					cn.getTextWindow().setCursorPosition(j,i);
					cn.getTextWindow().output(maze[i][j],iceColor);
				}
				else if (maze[i][j] == '-'){
					cn.getTextWindow().setCursorPosition(j,i);
					cn.getTextWindow().output(maze[i][j],fireColor);
				}
				else{
					cn.getTextWindow().setCursorPosition(j,i);
					cn.getTextWindow().output(maze[i][j]);
				}

			}
		}
		cn.getTextWindow().setCursorPosition(55, 10);
		System.out.println("P.Health : "+player.getHealth() + " ");
		cn.getTextWindow().setCursorPosition(70, 10);
		System.out.println("   ");
		cn.getTextWindow().setCursorPosition(55, 15);
		System.out.println("P.Score  : "+player.getScore());
		cn.getTextWindow().setCursorPosition(55, 20);
		System.out.println("P.Ice    : "+player.getPackedIceCount());
		cn.getTextWindow().setCursorPosition(55, 1);
		System.out.println(time);
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
		TextAttributes blackWhite = new TextAttributes(Color.black,Color.white);
		cn.getTextWindow().setCursorPosition(55,5);
		cn.getTextWindow().output("<<<<<<<<<<",blackWhite);
		cn.getTextWindow().setCursorPosition(55,7);
		cn.getTextWindow().output("<<<<<<<<<<",blackWhite);
		cn.getTextWindow().setCursorPosition(55,6);
		for (int i = 0; i < 10; i++) {
			System.out.print(inputQueue.peek());
			inputQueue.enqueue(inputQueue.dequeue());
		}
	}

	public static Object generateInputQueueElement(){
		Object returnData;
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