public class ComputerPawn {

    private int health;
    private Coordinates computerCoordinates;
    private int score;

    ComputerPawn(int x, int y){
        health=1000;
        computerCoordinates = new Coordinates(x,y);
        score=0;

    }
    void increasescore(int add){score+=add;}
    int getscore(){
        return score;
    }
    void decreaseHealth(int h) {
        health-=h;
    }
    void setCoordinates(int x,int y) {
        this.computerCoordinates.setXY(x,y);
    }
    int getHealth() {return health;}
    Coordinates getCoordinates() {
        return computerCoordinates;
    }


}
