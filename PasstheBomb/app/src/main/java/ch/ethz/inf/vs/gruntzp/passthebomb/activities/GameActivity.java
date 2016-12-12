package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.MessageListener;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Game;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Player;

//TODO make the bomb and the layout for the players and the score
public class GameActivity extends AppCompatActivity implements MessageListener {

    private int currentApiVersion;
    private Game game;
    private Player thisPlayer;
    private RelativeLayout gameView;
    private ImageView bomb;


    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // initialize global variables
        bomb = (ImageView) findViewById(R.id.bomb);
        Bundle extras = getIntent().getExtras();
        game = (Game) extras.get("game");
        thisPlayer = (Player) extras.get("thisPlayer");
        //TODO get information on who has the bomb and set that in the variable 'game'
        //These things were already done in LobbyActivity


        //for testing only
        /*
        game = new Game("herp derp", "theBest", false, "");
        game.addPlayer(new Player("Senpai"));
        game.addPlayer(new Player("herp"));
        game.addPlayer(new Player("derp"));
        game.addPlayer(new Player("somebody"));
        game.getPlayers().get(1).setScore(9000);
        thisPlayer = game.getPlayers().get(0);
        thisPlayer.setHasBomb(true);
        thisPlayer.setScore(2000);
        //endGame();
        */


        //GUI stuff
        hideNavigationBar();
        gameView = (RelativeLayout) findViewById(R.id.game);
        setUpBomb();
        setUpPlayers();


    }

    /* When a player gets disconnected call this method.
     * This method greys out the given player's field.
     */
    public void showPlayerAsDisconnected(){
        /** TODO add player ID in the parameter
         ** -> iterate over the list of players and check which player has the same ID
         *  then if game.getPlayers.get(i) has it call
         *
         *  Button playerField = (Button) gameView.getChildAt(i);
         *
         *  if i is smaller than where thisPlayer is in the list of players
         *  else use i-1
         *
         *  and then call
         *
         *  playerField.setBackground(getDrawable(R.drawable.greyed_out_field)));
         *
         *  if i != 3, else use R.drawable.greyed_out_field_upsidedown instead
         **/


        /* Testing that the positions of the fields don't get messed up
         *
          for(int i = 0; i<game.getPlayers().size()-1; i++){
            Button playerField = (Button) gameView.getChildAt(i);
            if(i!=3) {
                playerField.setBackground(getDrawable(R.drawable.greyed_out_field));
            }else{
                playerField.setBackground(getDrawable(R.drawable.greyed_out_field_upsidedown));
            }
         }
         *
         */

    }

    private void setUpPlayers(){
        int j = 0; //index for player field
        for(int i=0; i<game.getPlayers().size(); i++){
            if (thisPlayer != game.getPlayers().get(i)) {
                Button player_field = (Button) gameView.getChildAt(j);
                player_field.setVisibility(View.VISIBLE);
                player_field.setText(game.getPlayers().get(i).getName() + "\n" + game.getPlayers().get(i).getScore());
                j++;
            }
        }
        TextView own_score = (TextView) findViewById(R.id.Score_number);
        own_score.setText(thisPlayer.getScore()+"");
    }

    public void updateScore(){
        int j = 0; //index for player field
        for(int i=0; i<game.getPlayers().size(); i++){
            if (thisPlayer != game.getPlayers().get(i)) {
                Button player_field = (Button) gameView.getChildAt(j);
               //we include score in the name-string
                player_field.setText(game.getPlayers().get(i).getName() + "\n" + game.getPlayers().get(i).getScore());
                j++;
            }
        }
        TextView own_score = (TextView) findViewById(R.id.Score_number);
        own_score.setText(thisPlayer.getScore()+"");
    }

    private void setUpBomb(){
        enableOnTouchAndDragging();
        setBombVisibility();
        setBombInCenter();

    }

    private void setBombVisibility(){
        if(!thisPlayer.isHasBomb()){
            bomb.setVisibility(View.INVISIBLE);
        } else {
            bomb.setVisibility(View.VISIBLE);
        }
    }

    private void setBombInCenter(){
        FrameLayout.LayoutParams par=(FrameLayout.LayoutParams)bomb.getLayoutParams();
        par.gravity = Gravity.CENTER;
        bomb.setLayoutParams(par);
    }

    private void enableOnTouchAndDragging(){
        bomb.setOnTouchListener(new View.OnTouchListener() {
            private Boolean[] touch = {false, false, false, false};
            int prevX,prevY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final FrameLayout.LayoutParams par=(FrameLayout.LayoutParams)v.getLayoutParams();

                for(int i=0; i<game.getPlayers().size()-1; i++) {
                    Button playerfield = (Button) gameView.getChildAt(i);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {

                            //start dragging
                            prevX=(int)event.getRawX();
                            prevY=(int)event.getRawY();
                            par.bottomMargin=-2*v.getHeight();
                            par.rightMargin=-2*v.getWidth();

                            // makes sure that the bomb image doesn't jump around
                            if(par.gravity == Gravity.CENTER) {
                                int[] location = new int[2];
                                bomb.getLocationOnScreen(location);
                                int x = location[0];
                                int y = location[1];
                                par.gravity = Gravity.NO_GRAVITY;
                                par.topMargin = y;
                                par.leftMargin = x;
                            }

                            v.setLayoutParams(par);
                            scaleIn(bomb, 5);

                            //check intersection
                            if(checkInterSection(playerfield, i,  event.getRawX(), event.getRawY())
                                    && playerfield.getVisibility() == View.VISIBLE) {
                                scaleIn(playerfield, i);
                                touch[i] = true;
                                Log.i("down", "yes!");
                            }
                            break;
                        }
                        case MotionEvent.ACTION_MOVE: {

                            //drag
                            par.topMargin+=(int)event.getRawY()-prevY;
                            prevY=(int)event.getRawY();
                            par.leftMargin+=(int)event.getRawX()-prevX;
                            prevX=(int)event.getRawX();
                            v.setLayoutParams(par);

                            //check touch
                            if(checkInterSection(playerfield, i, event.getRawX(), event.getRawY()) &&
                                    !touch[i] && playerfield.getVisibility() == View.VISIBLE) {
                                scaleIn(playerfield, i);
                                touch[i] = true;

                                Log.i("move", "yes!");
                            } else if(!checkInterSection(playerfield, i, event.getRawX(), event.getRawY()) && touch[i]) {
                                // run scale animation and make it smaller
                                scaleOut(playerfield, i);
                                touch[i] = false;

                                Log.i("move", "no!");
                            }
                            break;
                        }
                        case MotionEvent.ACTION_UP: {

                            //stop dragging
                            par.topMargin+=(int)event.getRawY()-prevY;
                            par.leftMargin+=(int)event.getRawX()-prevX;
                            v.setLayoutParams(par);
                            scaleOut(bomb, 5);

                            //check if touching
                            if (touch[i] && playerfield.getVisibility() == View.VISIBLE) {
                                // run scale animation and make it smaller
                                scaleOut(playerfield, i);
                                touch[i] = false;
                                //TODO and if it was touching, then send server information to pass the bomb on
                                //TODO make bomb invisible; remember to set ishasbomb for thisplayer to false
                                Log.i("up", "no!");
                            }
                            break;
                        }

                    }
                }
                return true;
            }
        });
    }

    private void scaleIn(View v, int childID){
        Animation anim;
        switch (childID){
            case 2:
            {
                anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_in_green);
                break;
            }
            case 1:
            {
                anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_in_yellow);
                break;
            }
            default:
            {
                anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_in_normally);
                break;
            }
        }

        v.startAnimation(anim);
        anim.setFillAfter(true);
    }

    private void scaleOut(View v, int childID){
        Animation anim;
        switch (childID){
            case 2:
            {
                anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_out_green);
                break;
            }
            case 1:
            {
                anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_out_yellow);
                break;
            }
            default:
            {
                anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_out_normally);
                break;
            }
        }

        v.startAnimation(anim);
        anim.setFillAfter(true);
    }

    private boolean checkInterSection(View view, int childID, float rawX, float rawY) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        int width = view.getWidth();
        int height = view.getHeight();
        switch (childID){
            case 1: //yellow field
            {
                y -= width ;
                width = view.getHeight();
                height = view.getWidth();
                break;
            }
            case 2: //green field
            {
                x -= height;
                width = view.getHeight();
                height = view.getWidth();
                break;
            }
            default: //case 0: red field or blue field
            {
                break;
            }
        }
        //Check the intersection of point with rectangle achieved
        return rawX > x && rawX < (x+width) && rawY>y && rawY <(y+height);
    }

    //TODO call this when the server sends information that the game has ended
    /* finishes game; displays button to go to ScoreboardActivity
     * call this when the server sends information that the game has ended
     */
    public void endGame(){
        TextView gameOver = (TextView) findViewById(R.id.game_over);
        Button toScoreboard = (Button) findViewById(R.id.to_scoreboard);

        gameOver.setVisibility(View.VISIBLE);
        toScoreboard.setVisibility(View.VISIBLE);
    }


    // makes sure navigation bar doesn't appear again
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // Suppress navigation bar
    private void hideNavigationBar(){
        currentApiVersion = android.os.Build.VERSION.SDK_INT;

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                    {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility)
                        {
                            if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                            {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }
    }

    /** TODO? if user somehow manages to bring back the navigation bar,
    ** should it not do anything, or
    ** should it bring them back to the main menu or something
    ** and kick him out of the game?
    **/
    @Override
    public void onBackPressed(){
        super.onBackPressed(); //to change?
    }

    public void onClickContinue(View view) {
        Intent myIntent = new Intent(this, ScoreboardActivity.class);

        //give extra information to the next activity
        myIntent.putExtra("game", game);
        myIntent.putExtra("thisPlayer", thisPlayer);

        //start next activity
        this.startActivity(myIntent);

        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        controller.bind(this);
    }

    @Override
    public void onMessage(int type, JSONObject body) {
        //TODO
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.unbind(this);
    }
}
