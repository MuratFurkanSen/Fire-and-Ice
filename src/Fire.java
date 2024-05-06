import java.util.Random;

public class Fire {

    private static enigma.console.Console cn;
    private static char[][] maze;
    private Coordinates curr_cr;
    private CircularQueue all_pieces;
    private CircularQueue pieces_for_erasing;
    private int timer;
    private Coordinates[] dirs;
    private int dir_index;
    private int dir_rot;
    private boolean isPlaced;
    private int count;
    Random random = new Random();

    Fire(enigma.console.Console cn, char[][] maze,Coordinates start_cr) {
        this.cn = cn;
        this.maze = maze;
        this.all_pieces = new CircularQueue(50);
        this.pieces_for_erasing = new CircularQueue(50);
        all_pieces.enqueue(start_cr);
        pieces_for_erasing.enqueue(start_cr);
        this.curr_cr = start_cr;
        this.timer = 1;
        this.dirs = new Coordinates[]{
                new Coordinates(0, -1),
                new Coordinates(1, 0),
                new Coordinates(0, 1),
                new Coordinates(-1, 0)};

        this.maze[start_cr.getY()][start_cr.getX()] = '-';
        dir_index = random.nextInt(0, 3);
        switch (random.nextInt(1, 2)) {
            case 1:
                dir_rot = 1;
                break;
            case 2:
                dir_rot = -1;
                break;
        }
        dir_index=0;
        dir_rot=1;
    }


    void increaseTimer() {
        timer++;
        if (timer <= 50) {
            spread(maze);
        } else if (!all_pieces.isEmpty() && timer >= 100) {
            erase(maze);
        }
    }

    void spread(char[][]maze) {
        isPlaced = false;
        Coordinates dir = dirs[dir_index % dirs.length];
        dir_index += dir_rot;
        if (maze[curr_cr.getY() + dir.getY()][curr_cr.getX() + dir.getX()] == ' ') {
            maze[curr_cr.getY() + dir.getY()][curr_cr.getX() + dir.getX()] = '-';
            all_pieces.enqueue(new Coordinates(curr_cr.getX() + dir.getX(), curr_cr.getY() + dir.getY()));
            pieces_for_erasing.enqueue(new Coordinates(curr_cr.getX() + dir.getX(), curr_cr.getY() + dir.getY()));
            isPlaced = true;
        }
        count++;
        if (count >= 4){
            count = 0;
            curr_cr = (Coordinates) all_pieces.peek();
            all_pieces.enqueue(all_pieces.dequeue());
            dir_index += dir_rot*dirs.length*-1;
        }
        if (!isPlaced){
            spread(maze);
        }
    }
    void erase(char[][] maze) {
        all_pieces.dequeue();
        curr_cr = (Coordinates) pieces_for_erasing.dequeue();
        maze[curr_cr.getY()][curr_cr.getX()] = ' ';
    }
}
