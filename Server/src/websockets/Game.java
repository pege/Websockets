package websockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.websocket.Session;

import org.json.JSONArray;
import org.json.JSONObject;

import org.passthebomb.library.MessageFactory;
import org.passthebomb.library.Constants;


public final class Game {

	private static final int maxBombLifeTime = 100; // max Lifetime of a bomb
	private static final int minBombLifetime = 20;

	private static final int scoreIncrease = 20;
	private static final int scoreDecrease = -20;

	private Player owner;
	private ArrayList<Player> players = new ArrayList<>();

	private final String gameName;
	private final String password;

	private boolean started;

	private Player bombOwner;
	private int bomb = -1;
	private int initialbomb = -1;

	public Game(Player owner, String gamename, String password) {
		this.gameName = gamename;
		this.password = password;
		this.owner = owner;
		players.add(owner);
	}

	public Player getBombOwner() {
		return bombOwner;
	}

	public void setBombOwner(Player owner) {
		bombOwner = owner;
	}

	public boolean hasStarted() {
		return started;
	}

	public int getBomb() {
		return bomb;
	}

	public void setBomb(int value) {
		bomb = value;
	}

	public String getGamename() {
		return gameName;
	}

	public boolean checkPassword(String password) {
		return this.password.equals(password);
	}

	public boolean hasPassword() {
		return !password.equals("");
	}

	public void addPlayer(Player player) {
		players.add(player);
	}

	public void removePlayer(Player player) {
		if (started) {
			players.set(players.indexOf(player), null);
			if (numberOfPlayers() > 0 && bombOwner == player)
				setBombOwner(pickRandom());
			if (numberOfPlayers() > 0 && player == owner)
				owner = pickRandom();
		} else {// still in Lobby
			players.remove(player);
			if (numberOfPlayers() > 0 && player == owner)
				owner = players.get(0);
		}
	}

	public String getPlayersName() {
		String s = "";
		for (Player p : players) {
			if (p != null)
				s = s + p.getName() + ", ";
		}
		return s;
	}

	public Player getOwner() {
		return owner;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public int numberOfPlayers() {
		int size = 0;
		for (Player player : players) {
			if (player != null)
				size++;
		}
		return size;
	}

	public void startGame() {
		started = true;
		bombOwner = pickRandom();
		bomb = createBomb();
		initialbomb = bomb;
		System.out.println(getGamename() + " has started");
	}

	public void bomb_exploded(Player p) {
		p.changeScore((-1)*p.getScore() / 2);
		// p.changeScore(scoreDecrease);

		for (Player player : players) {
			if (player != null && p != player) {
				player.changeScore(scoreIncrease);
			}
		}
	}

	private static Random random = new Random();

	private static int createBomb() {
		// TODO: Gauss
		return random.nextInt(maxBombLifeTime - minBombLifetime) + minBombLifetime;
	}

	public Player pickRandom() {
		int size = players.size();
		int choice = random.nextInt(size);
		while (players.get(choice) == null) {
			choice = random.nextInt(size);
		}
		return players.get(choice);
	}

	public int indexOfPlayer(Player player) {
		return players.indexOf(player);
		
	}

	public void broadcast_detailed_state(int type) {
		switch (type) {
		case MessageFactory.SC_UPDATE_SCORE:
			broadcast(MessageFactory.SC_UpdateScore(this.toJSON(1)));
			break;
		case MessageFactory.SC_PLAYER_MAYBEDC:
			broadcast(MessageFactory.SC_PlayerMaybeDC(this.toJSON(1)));
			break;
		case MessageFactory.SC_GAME_STARTED:
			broadcast(MessageFactory.SC_GameStarted(this.toJSON(1)));
			break;
		case MessageFactory.SC_BOMB_PASSED:
			broadcast(MessageFactory.SC_BombPassed(this.toJSON(1)));
			break;
		case MessageFactory.SC_BOMB_EXPLODED:
			broadcast(MessageFactory.SC_BombExploded(this.toJSON(1)));
			break;
		default:
			break;
		}

	}

	public void broadcast(String message) {
		System.out.println(message);
		for (Player player : players) {
			if (player != null) {
				Session s = player.getSession();

				try {
					if (s.isOpen())
						s.getBasicRemote().sendText(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isFinished() {
		for (Player p : players) {
			if (p != null && p.getScore() >= Constants.FINAL_SCORE)
				return true;
		}
		return false;
	}

	public void destroy() {
		for (Player p : players) {
			if (p != null) {
				p.leaveGame();
			}
		}
	}

	public synchronized JSONObject toJSON(int level) {
		// JSONObject header = new JSONObject();
		JSONObject body = new JSONObject();

		// header.put("type", Message.SC_GAME_UPDATE);

		body.put("name", gameName);
		body.put("hasPassword", hasPassword());
		body.put("noP", numberOfPlayers());
		body.put("started", started);

		if (level > 0) {
			body.put("owner", owner.getUuid());
			body.put("bombOwner", bombOwner == null ? -1 : bombOwner.getUuid());
			body.put("bomb", bomb);
			body.put("initial_bomb", initialbomb);
			JSONArray players = new JSONArray();
			for (Player player : this.getPlayers())
				if (player == null) {
					players.put("left");
				} else {
					players.put(player.toJSON());
				}

			body.put("players", players);
		}
		return body;
	}
}
