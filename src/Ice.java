import enigma.console.TextAttributes;

import java.awt.*;
import java.util.Random;

public class Ice {
    private static enigma.console.Console cn;
    TextAttributes cyan = new TextAttributes(Color.CYAN);
    private Coordinates start_cr;
    private Coordinates curr_cr;
    private Stack all_pieces;
    private Stack temp_stack;

    private Coordinates dirs;
    private int timer;
    boolean isLastPlaced = false;
    Random random = new Random();

    Ice(enigma.console.Console cn){
        this.cn = cn;
    }


    Ice(Coordinates start_cr, Coordinates dirs){
        this.start_cr = start_cr;
        this.curr_cr = start_cr;
        this.dirs = dirs;
        this.all_pieces = new Stack(25);
        this.temp_stack = new Stack(25);
        this.timer = 0;
    }
    void increaseTimer(char[][] maze){
        timer++;
        if (timer<=25){
            spread(maze);

        }
        else if (!temp_stack.isEmpty()&&timer>=100) {
            erase(maze);
        }
        if (all_pieces.size() == 25){
            while (!all_pieces.isEmpty()){
                temp_stack.push(all_pieces.pop());
            }
        }
    }
    void spread(char[][] maze){
        isLastPlaced = false;
        if (maze[curr_cr.getY()+dirs.getY()][curr_cr.getX()+dirs.getX()] == ' '){
            updateMaze(maze,curr_cr.getY()+dirs.getY(), curr_cr.getX()+dirs.getX(),'+', cyan);
            isLastPlaced = true;
            curr_cr =new Coordinates(curr_cr.getX()+dirs.getX(),curr_cr.getY()+dirs.getY());
            temp_stack.push(curr_cr);
        }
        else {
            int[] randDirs = new int[]{1,-1};
            int new_x = 0;
            int new_y = 0;
            if (dirs.getX() == 0){
               new_x = randDirs[random.nextInt(1,2)];
            }
            else {
                new_y = randDirs[random.nextInt(1,2)];
            }

            if (maze[curr_cr.getY()+new_y][curr_cr.getX()+new_x] == ' '){
                maze[curr_cr.getY()+new_y][curr_cr.getX()+new_x] = '+';
                isLastPlaced = true;
                curr_cr = new Coordinates(curr_cr.getX()+new_x,curr_cr.getY()+new_y);
                temp_stack.push(curr_cr);
            }
            else if (maze[curr_cr.getY()+(new_y*-1)][curr_cr.getX()+(new_x*-1)] == ' '){
                maze[curr_cr.getY()+(new_y*-1)][curr_cr.getX()+new_x*-1] = '+';
                isLastPlaced = true;
                curr_cr = new Coordinates(curr_cr.getX()+(new_x*-1),curr_cr.getY()+(new_y*-1));
                temp_stack.push(curr_cr);
            }
            else if (maze[curr_cr.getY()+(dirs.getY()*-1)][curr_cr.getX()+(dirs.getX()*-1)] == ' ') {
                maze[curr_cr.getY() + (dirs.getY()*-1)][curr_cr.getX() + (dirs.getX()*-1)] = '+';
                isLastPlaced = true;
                curr_cr = new Coordinates(curr_cr.getX()+(dirs.getX()*-1),curr_cr.getY()+(dirs.getY()*-1));
                temp_stack.push(curr_cr);
            }

        }
        if (!isLastPlaced){
            curr_cr = (Coordinates) all_pieces.peek();
            temp_stack.push(all_pieces.pop());
            spread(maze);

        }
        else{
            while (!temp_stack.isEmpty()){
                all_pieces.push(temp_stack.pop());
            }
        }
    }

    void erase(char[][] maze){
        curr_cr = (Coordinates) temp_stack.pop();
        maze[curr_cr.getY()][curr_cr.getX()] = ' ';
    }
    public void updateMaze(char[][] maze, int y, int x, char ch, TextAttributes color) {
        maze[y][x] = ch;
        cn.getTextWindow().output(x, y, ch,color);
    }
    // Getters and Setters
    public Coordinates getStart_cr() {
        return start_cr;
    }
    public void setStart_cr(Coordinates start_cr) {
        this.start_cr = start_cr;
    }
    public Coordinates getDirs() {
        return dirs;
    }
    public void setDirs(Coordinates dirs) {
        this.dirs = dirs;
    }
    public Coordinates getCurr_cr() {
        return curr_cr;
    }
    public void setCurr_cr(Coordinates curr_cr) {
        this.curr_cr = curr_cr;
    }
    public Stack getAll_pieces() {
        return all_pieces;
    }
    public void setAll_pieces(Stack all_pieces) {
        this.all_pieces = all_pieces;
    }
}
