
public class Player {
	private int health;
	private Coordinates direction;
	private Coordinates playerCoordinates;
	private int score;
	private int packedicecount;

	Player(int x,int y){
		health=1000;
		direction=new Coordinates(1,0);
		playerCoordinates=new Coordinates(x, y);
		score=0;
		packedicecount=0;
	}
	void decreaseHealth(int h) {
		health-=h;
	}
	void setDirection(int x,int y) {
		direction.setXY(x, y);
	}
	void setCoordinates(int xc,int yc) {
		playerCoordinates.setXY(xc,yc);
	}
	void addScore(int sc) {
		score+=sc;
	}
	void addPackedIce() {
		packedicecount++;
	}
	void usePackedIce() {
		packedicecount--;
	}
	int getHealth() {return health;}
	void setHealth(int h) {health=h;}
	Coordinates getDirection() {return direction;}
	Coordinates getCoordinates() {
		return playerCoordinates;
	}
	int getScore() {return score;}
	int getPackedIceCount() {
		return packedicecount;
	}

}
