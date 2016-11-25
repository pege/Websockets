package ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by Michelle on 20.11.2016.
 * Class that describes a player.
 */

// implements Parcelable so that it can be put in putExtra()
public class Player implements Parcelable, Comparable<Player>{

    private String name;
    private int score;
    private boolean hasBomb;

    public Player(String name){
        this.name = name;
        this.score = 0;
        this.hasBomb = false;
    }

    public Player(Parcel in){
        this.name = in.readString();
        this.score = in.readInt();
        this.hasBomb = in.readByte() != 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isHasBomb() {
        return hasBomb;
    }

    public void setHasBomb(boolean hasBomb) {
        this.hasBomb = hasBomb;
    }

    // leave this empty please
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(score);
        dest.writeByte((byte) (hasBomb ? 1 : 0));
    }

    public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>()
    {
        public Player createFromParcel(Parcel in)
        {
            return new Player(in);
        }
        public Player[] newArray(int size)
        {
            return new Player[size];
        }
    };

    @Override
    public int compareTo(Player otherPlayer) {
        int compareScore = otherPlayer.getScore();

        //descending order
        return compareScore - this.score;
    }
}