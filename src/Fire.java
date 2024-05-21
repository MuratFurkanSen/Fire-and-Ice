import java.util.Random;

public class Fire {

    private static enigma.console.Console cn;
    private static char[][] maze;
    private Coordinates start_cr;
    private Coordinates curr_cr;
    private CircularQueue all_pieces;
    private CircularQueue pieces_for_erasing;
    private CircularQueue erase_times;
    private int timer;
    private Coordinates[] dirs;
    private int dir_index;
    private int dir_rot;
    private boolean isPlaced;
    private int count;
    private int totalSpreadFire;
    Random random = new Random();

    Fire(enigma.console.Console cn, char[][] maze, Coordinates start_cr) throws Exception {//thisleri sildim?
        this.cn = cn;
        this.maze = maze;
        this.start_cr = start_cr;
        all_pieces = new CircularQueue(50);
        pieces_for_erasing = new CircularQueue(50);
        erase_times = new CircularQueue(50);
        all_pieces.enqueue(start_cr);
        pieces_for_erasing.enqueue(start_cr);
        curr_cr = start_cr;
        timer = 1;
        dirs = new Coordinates[]{
                new Coordinates(0, -1),
                new Coordinates(1, 0),
                new Coordinates(0, 1),
                new Coordinates(-1, 0)};

        this.maze[start_cr.getY()][start_cr.getX()] = '-';
        erase_times.enqueue(timer+100);
        totalSpreadFire=1;
        dir_index = random.nextInt(0, 3);
        switch (random.nextInt(1, 2)) {
            case 1:
                dir_rot = 1;
                break;
            case 2:
                dir_rot = -1;
                break;
        }
        //hatırladığım kadarıyla gerek yok
        //dir_index=0;
        // dir_rot=1;
    }


    void increaseTimer() throws Exception {
        timer++;
        if (totalSpreadFire<50 && !all_pieces.isEmpty()){
            spread();
        }
        // Precision Timing for Erasing Operation
        if (!pieces_for_erasing.isEmpty() && !erase_times.isEmpty()&& ((int) erase_times.peek())==timer) {
            erase();
        }
    }

    void spread() throws Exception {
        isPlaced = false;
        Coordinates dir = dirs[dir_index % dirs.length];
        dir_index += dir_rot;
        if (maze[curr_cr.getY() + dir.getY()][curr_cr.getX() + dir.getX()] == ' ') {
            maze[curr_cr.getY() + dir.getY()][curr_cr.getX() + dir.getX()] = '-';
            all_pieces.enqueue(new Coordinates(curr_cr.getX() + dir.getX(), curr_cr.getY() + dir.getY()));
            pieces_for_erasing.enqueue(new Coordinates(curr_cr.getX() + dir.getX(), curr_cr.getY() + dir.getY()));
            isPlaced = true;
            totalSpreadFire++;
            erase_times.enqueue(timer+100);
        }
        count++;
        if (count >= 4) {
            count = 0;
            curr_cr = (Coordinates) all_pieces.peek();
            all_pieces.enqueue(all_pieces.dequeue());
            if (curr_cr.getX() == start_cr.getX()&&curr_cr.getY() == start_cr.getY()) {
                return;
            }
            dir_index += dir_rot * dirs.length * -1;
        }
        if (!isPlaced) {
            spread();
        }
    }

    void erase() {
        all_pieces.dequeue();
        erase_times.dequeue();
        curr_cr = (Coordinates) pieces_for_erasing.dequeue();
        maze[curr_cr.getY()][curr_cr.getX()] = ' ';
    }
    int size(){
        return pieces_for_erasing.size();
    }
}
