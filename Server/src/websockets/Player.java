package websockets;

import javax.websocket.Session;

public class Player {
	
	private long uuid;
	private String name;
	private int score;
	private boolean hasBomb;
	private boolean isConnected; //if connected or not //TODO i think it's redundant (It's checked if map points to NULL or not)
	private Session session;
	private long lastPong;
	private Game inGame; //null if in no game
	
	
	public Player(String name, long uuid, Session session){
		this.name = name;
		this.uuid = uuid;
		this.session = session;
		setLastPong(System.currentTimeMillis());
	}
	
	public String getName(){
		return name;
	}
	
	public long getUuid(){
		return uuid;
	}
	
	public int getScore(){
		return score;
	}
	
	public boolean hasBomb(){
		return hasBomb;
	}
	
	public void changeScore(int amount){
		score = score +amount;
	}
	
	public void setConnection(boolean status){
		isConnected = status;
	}
	
	public boolean isConnected(){
		return isConnected;
	}
	
	public Session getSession(){
		return session;
	}
	
	public void joinGame(Game game){
		inGame = game;
	}
	
	public Game getJoinedGame(){
		return inGame;
	}

	public synchronized void setLastPong(long time){
		lastPong = time;
	}
	
	public synchronized long getLastPong(){
		return lastPong;
	}

}
