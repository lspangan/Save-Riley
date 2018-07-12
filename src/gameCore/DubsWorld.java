package gameCore;

import ui.*;
import modifiers.*;
import myGames.*;
import java.awt.*;
import java.awt.image.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Observer;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.util.Observable;

import javax.swing.*;

import modifiers.motions.MotionController;

// extending JPanel to hopefully integrate this into an applet
// but I want to separate out the Applet and Application implementations
public final class DubsWorld extends JPanel implements Runnable, Observer {

    private Thread thread;
    
    // GameWorld is a singleton class!
    private static final DubsWorld game = new DubsWorld();
    public static final GameSounds sound = new GameSounds();
    public static final GameClock clock = new GameClock();
    GameMenu menu;
    public SourceReader reader;
   
    private BufferedImage bimg;
    static Point speed = new Point(0,0), arena;
    int sizeX, sizeY;
    
    /*Some ArrayLists to keep track of game things*/
    private ArrayList<BackgroundObject> background;
    //private ArrayList<Bullet> bullets;
    private ArrayList<PlayerShip> players;
    private ArrayList<InterfaceObject> ui;
    private ArrayList<Ship> tnts;
    private ArrayList<Ship> detonators;
    private ArrayList<Ship> exits;
    private ArrayList<Ship> saws;
    
    
    public static HashMap<String, Image> sprites;
    public static HashMap<String, MotionController> motions = new HashMap<String, MotionController>();

    // is player still playing, did they win, and should we exit
    boolean gameOver, gameWon, gameFinished, levelFinished = false;
    boolean playing = false;
    ImageObserver observer;
        
    // constructors makes sure the game is focusable, then
    // initializes a bunch of ArrayLists
    private DubsWorld(){
        this.setFocusable(true);
        background = new ArrayList<BackgroundObject>();
        players = new ArrayList<PlayerShip>();
        ui = new ArrayList<InterfaceObject>();
        saws = new ArrayList<Ship>();
        exits = new ArrayList<Ship>();
        tnts = new ArrayList<Ship>();
        detonators = new ArrayList<Ship>();
        sprites = new HashMap<String,Image>();
    }
    
    /* This returns a reference to the currently running game*/
    public static DubsWorld getInstance(){
    	return game;
    }

    /*Game Initialization*/
    public void init() {
        setBackground(Color.WHITE);
        sound.playLoop("Resources/warriors.wav");
        loadSprites();
        reader = new SourceReader(0);
        reader.addObserver(this);
        clock.addObserver(reader);
        arena = new Point(reader.w*40,reader.h*40);
        DubsWorld.setSpeed(new Point(0,0));
        gameOver = false;
        observer = this;
        
        addBackground(new Background(arena.x,arena.y,speed, sprites.get("background")));
        menu = new GameMenu();
        //reader.load();
    }
    
    /*Functions for loading image resources*/
    private void loadSprites() {
        sprites.put("background", getSprite("Chapter07/arena.png"));
        sprites.put("wall", getSprite("Chapter07/cone.png"));
        
        sprites.put("start", getSprite("Chapter07/Button_start.png"));
        sprites.put("start2", getSprite("Chapter07/Button_start1.png"));
        sprites.put("quit", getSprite("Chapter07/Button_quit.png"));
        sprites.put("quit2", getSprite("Chapter07/Button_quit1.png"));
        sprites.put("title", getSprite("Chapter07/dubtitle.png"));
        


        sprites.put("explosion2_1", getSprite("Resources/explosion2_1.png"));
        sprites.put("explosion2_2", getSprite("Resources/explosion2_2.png"));
        sprites.put("explosion2_3", getSprite("Resources/explosion2_3.png"));
        sprites.put("explosion2_4", getSprite("Resources/explosion2_4.png"));
        sprites.put("explosion2_5", getSprite("Resources/explosion2_5.png"));
        sprites.put("explosion2_6", getSprite("Resources/explosion2_6.png"));
        sprites.put("explosion2_7", getSprite("Resources/explosion2_7.png"));
        
        sprites.put("exit", getSprite("Chapter07/riley.png"));
        sprites.put("tnt", getSprite("Chapter07/lbj2.png"));
        sprites.put("saw", getSprite("Chapter07/lbj2.png"));
        sprites.put("detonator", getSprite("Chapter07/detonator1.png"));
        sprites.put("detonatorGif", getSprite("Chapter07/detonator.gif"));
        sprites.put("gameover", getSprite("Chapter07/youlose.gif"));
        sprites.put("youWin", getSprite("Chapter07/riley.gif"));
        sprites.put("maroon", getSprite("Chapter07/maroon.png"));
        sprites.put("rescued", getSprite("Chapter07/Rescued.png"));

        sprites.put("player1", getSprite("Chapter07/steph.png"));
        sprites.put("curryMove", getSprite("Chapter07/currydance.gif"));
        sprites.put("curryDead", getSprite("Chapter07/crying.png"));
        
    }
    
    public Image getSprite(String name) {
        URL url = DubsWorld.class.getResource(name);
        Image img = java.awt.Toolkit.getDefaultToolkit().getImage(url);
        try {
            MediaTracker tracker = new MediaTracker(this);
            tracker.addImage(img, 0);
            tracker.waitForID(0);
        } catch (Exception e) {
        }
        return img;
    }
    
    
    
    /********************************
     * 	These functions GET things	*
     * 		from the game world		*
     ********************************/
    
    public int getFrameNumber(){
    	return clock.getFrame();
    }
    
    public int getTime(){
    	return clock.getTime();
    }
    
    public void removeClockObserver(Observer theObject){
    	clock.deleteObserver(theObject);
    }
    
    public ListIterator<BackgroundObject> getBackgroundObjects(){
    	return background.listIterator();
    }
    
    public ListIterator<PlayerShip> getPlayers(){
    	return players.listIterator();
    }
    
    
    public int countPlayers(){
    	return players.size();
    }
    
    public void setDimensions(int w, int h){
    	this.sizeX = w;
    	this.sizeY = h;
    }

    public static Point getSpeed(){
    	return new Point(DubsWorld.speed);
    }  
    
    public static void setSpeed(Point speed){
    	DubsWorld.speed = speed;
    }
    
    /********************************
     * 	These functions ADD things	*
     * 		to the game world		*
     ********************************/

    public void addPlayer(PlayerShip...newObjects){
    	for(PlayerShip player : newObjects){
    		players.add(player);
    		ui.add(new InfoBar(player,Integer.toString(players.size())));
    	}
    }
    
    public ArrayList<PlayerShip> removePlayer(){
        for(int i = 0; i < players.size(); i++){
            players.remove(i);
        }
        return players;
    } 
    
    // add background items (islands)
    public void addBackground(BackgroundObject...newObjects){
    	for(BackgroundObject object : newObjects){
    		background.add(object);
    	}
    }
    
    public ArrayList<BackgroundObject> removeBackground(){
        background.clear();
        return background;
    }     
    
    public void addTNT(Ship tnt){
        tnts.add(tnt);
    }
    
    public ArrayList<Ship> removeTNT(){
        tnts.clear();
        return tnts;
    }     
    
    public void addDetonator(Ship detonator){
        detonators.add(detonator);
    }
    
    public ArrayList<Ship> removeDetonator(){
        detonators.clear();
        return detonators;
    }   
    
    public void addExit(Ship exit){
        exits.add(exit);
    }
    
    public ArrayList<Ship> removeExit(){
        exits.clear();
        return exits;
    }   
    
    public void addSaw(Ship saw){
        saws.add(saw);
    }
    
    public ArrayList<Ship> removeSaw(){
        saws.clear();
        return saws;
    }
    
    public ArrayList<InterfaceObject> removeUI(){
        ui.clear();
        return ui;
    }
    
    
    public void addClockObserver(Observer theObject){
    	clock.addObserver(theObject);
    }
    
    // this is the main function where game stuff happens!
    // each frame is also drawn here
    public void drawFrame(int w, int h, Graphics2D g2) {
        ListIterator<?> iterator = getBackgroundObjects();
        //playing = true;
        // iterate through all blocks
                while (iterator.hasNext()) {
                    BackgroundObject obj = (BackgroundObject) iterator.next();
                    obj.update(w, h);
                    obj.draw(g2, this);

                    if (obj instanceof BigExplosion || obj instanceof SmallExplosion) {
                        if (!obj.show) {
                            iterator.remove();
                        }
                        continue;
                    }

                    // player-to-wall and player-to-rock collision detections
                    ListIterator<PlayerShip> players = getPlayers();
                    while (players.hasNext() && obj.show) {
                        Dubs player = (Dubs) players.next();
                        Rectangle location = obj.getLocation();
                        Rectangle playerLocation = player.getLocation();
                        if (obj.collision(player)) {
                            if (obj.img.equals(sprites.get("wall"))) {
                                if (playerLocation.y < location.y) {
                                    player.move(0, -2);
                                }
                                if (playerLocation.y > location.y) {
                                    player.move(0, 2);
                                }
                                if (playerLocation.x < location.x) {
                                    player.move(-2, 0);
                                }
                                if (playerLocation.x > location.x) {
                                    player.move(2, 0);
                                }
                            }

                        }
                    }

                    //saw-to-wall collision detection
                    ListIterator<Ship> saw = saws.listIterator();
                    while (saw.hasNext() && obj.show) {
                        Ship s = (Ship) saw.next();
                        Rectangle location = obj.getLocation();
                        Rectangle loc = s.getLocation();
                        if (obj.collision(s)) {
                            if (obj.img.equals(sprites.get("wall"))) {
                                if (location.y < loc.y) {
                                    s.speed = new Point(0, 1);

                                }
                                if (location.y > loc.y) {
                                    s.speed = new Point(0, -1);
                                }
                            }
                        }
                    }

                    //tnt-to-wall collision detection
                    ListIterator<Ship> tnt = tnts.listIterator();
                    while (tnt.hasNext() && obj.show) {
                        Ship s = (Ship) tnt.next();
                        Rectangle location = obj.getLocation();
                        Rectangle loc = s.getLocation();
                        if (obj.collision(s)) {
                            if (obj.img.equals(sprites.get("wall"))) {
                                if (location.y < loc.y) {
                                    s.speed = new Point(0, 1);

                                }
                                if (location.y > loc.y) {
                                    s.speed = new Point(0, -1);
                                }
                            }
                        }
                    }
                    
                }
                if (menu.isWaiting()) {
                    menu.draw(g2, w, h);
                } else if (!gameFinished) {

                    PlayerShip p1 = players.get(0);
                    PlayerShip p2 = players.get(1);
                    PlayerShip p3 = players.get(2);


                    if ((p1.lives < 0 || p2.lives < 0 || p3.lives < 0)) {
                        gameOver = true;  
                    }

                    //loads level 2
                    if ((p1.saves == 3) && reader.getLevel() == 0) {
                        removeSaw();
                        removeTNT();
                        removeDetonator();
                        removeExit();
                        removeBackground();
                        removePlayer();
                        addBackground(new Background(arena.x, arena.y, speed, sprites.get("background")));
                        endLevel();
                        p1.setLocation(p1.resetPoint);
                        p2.setLocation(p2.resetPoint);
                        p3.setLocation(p3.resetPoint);
                        p1.saves = 0;
                    }
                    //finishes game
                    if ((p1.saves == 3) && (reader.getLevel() == 1)) {
                        game.finishGame();
                        isGameFinished();
                        gameWon = true;

                    }

                    //player-to-tnt and player-to-detonator collsion detection
                    ListIterator<?> it2 = detonators.listIterator();
                    iterator = tnts.listIterator();
                    while (iterator.hasNext() && it2.hasNext()) {
                        Ship powerup = (Ship) iterator.next();
                        Ship det = (Ship) it2.next();
                        ListIterator<PlayerShip> players = getPlayers();
                        while (players.hasNext() && powerup.show) {
                            PlayerShip player = players.next();
                            if (powerup.collision(player)) {
                                //player.die();
                                player.lives = -1;
                                p1.setLocation(p1.resetPoint);
                                p2.setLocation(p2.resetPoint);
                                p3.setLocation(p3.resetPoint);
                                p1.saves = 0;
                                iterator.remove();
                                player.die();
                            }
                            if (det.collision(player)) {
                                det.detonate();
                                powerup.die();
                                iterator.remove();
                                it2.remove();
                            }
                        }
                        powerup.draw(g2, this);
                        det.draw(g2, this);

                    }

                    //player-to-exit collsion detection
                    iterator = exits.listIterator();
                    while (iterator.hasNext()) {
                        Ship exit = (Ship) iterator.next();

                        if (exit.collision(p1)) {
                            p1.save();
                            p1.saves += 1;
                            p1.show = false;
                            p1.hide();
                            p1.setLocation(new Point(-10, -10));
                        }
                        if (exit.collision(p2)) {
                            p2.save();
                            p1.saves += 1;
                            p2.show = false;
                            p2.hide();
                            p2.setLocation(new Point(-10, -10));
                        }
                        if (exit.collision(p3)) {
                            p3.save();
                            p1.saves += 1;
                            p3.show = false;
                            p3.hide();
                            p3.setLocation(new Point(-10, -10));
                        }
                        exit.draw(g2, this);
                    }

                    //player-to-saw collision detection
                    iterator = saws.listIterator();
                    while (iterator.hasNext()) {
                        Ship saw = (Ship) iterator.next();
                        ListIterator<PlayerShip> players = getPlayers();
                        while (players.hasNext() && saw.show) {
                            PlayerShip player = players.next();
                            if (saw.collision(player)) {
                                sound.play("Chapter07/Saw.wav");
                                //player.die();
                                player.lives = -1;
                                p1.setLocation(p1.resetPoint);
                                p2.setLocation(p2.resetPoint);
                                p3.setLocation(p3.resetPoint);
                                p1.saves = 0;
                        //powerup.
                                //iterator.remove();
                            }

                        }
                        saw.draw(g2, this);

                    }

                    // player-to-player collisions
                    p1.update(w, h);

                    if (p1.collision(p2) || p1.collision(p3)) {
                        Rectangle pLoc1 = p1.getLocation();
                        Rectangle pLoc2 = p2.getLocation();
                        if ((pLoc1.y < pLoc2.y)) {
                            p1.move(0, -2);
                            //p2.move(0, 2);
                        }
                        if ((pLoc1.y > pLoc2.y)) {
                            p1.move(0, 2);
                            //p2.move(0, -2);
                        }
                        if (pLoc1.x < pLoc2.x) {
                            p1.move(-2, 0);
                            //p2.move(2, 0);
                        }
                        if ((pLoc1.x > pLoc2.x)) {
                            p1.move(2, 0);
                            //p2.move(-2, 0);
                        }
                        p1.update(w, h);
                        //p2.update(w, h);
                    }
                    if (p1.collision(p3)) {
                        Rectangle pLoc1 = p1.getLocation();
                        Rectangle pLoc3 = p3.getLocation();
                        if (pLoc1.y < pLoc3.y) {
                            p1.move(0, -2);
                            //p3.move(0, 2);
                        }
                        if (pLoc1.y > pLoc3.y) {
                            p1.move(0, 2);
                            //p3.move(0, -2);
                        }
                        if (pLoc1.x < pLoc3.x) {
                            p1.move(-2, 0);
                            //p3.move(2, 0);
                        }
                        if ((pLoc1.x > pLoc3.x)) {
                            p1.move(2, 0);
                            //p3.move(-2, 0);
                        }
                        p1.update(w, h);
                        //p3.update(w, h);
                    }
                    p2.update(w, h);
                    if (p2.collision(p1)) {
                        Rectangle pLoc1 = p1.getLocation();
                        Rectangle pLoc2 = p2.getLocation();
                        if ((pLoc2.y < pLoc1.y)) {
                            //p1.move(0, 2);
                            p2.move(0, -2);
                        }
                        if ((pLoc2.y > pLoc1.y)) {
                            //p1.move(0, -2);
                            p2.move(0, 2);
                        }
                        if ((pLoc2.x < pLoc1.x)) {
                            //p1.move(2, 0);
                            p2.move(-2, 0);
                        }
                        if ((pLoc2.x > pLoc1.x)) {
                            //p1.move(-2, 0);
                            p2.move(2, 0);
                        }
                        //p1.update(w, h);
                        p2.update(w, h);
                    }
                    if (p2.collision(p3)) {
                        Rectangle pLoc2 = p2.getLocation();
                        Rectangle pLoc3 = p3.getLocation();
                        if ((pLoc2.y < pLoc3.y)) {
                            p2.move(0, -2);
                            //p3.move(0, 2);
                        }
                        if ((pLoc2.y > pLoc3.y)) {
                            p2.move(0, 2);
                            //p3.move(0, -2);
                        }
                        if ((pLoc2.x < pLoc3.x)) {
                            p2.move(-2, 0);
                            //p3.move(2, 0);
                        }
                        if ((pLoc2.x > pLoc3.x)) {
                            p2.move(2, 0);
                            //p3.move(-2, 0);
                        }
                        p2.update(w, h);
                        //p3.update(w, h);
                    }
                    p3.update(w, h);
                    if (p3.collision(p1)) {
                        Rectangle pLoc1 = p1.getLocation();
                        Rectangle pLoc3 = p3.getLocation();
                        if ((pLoc3.y < pLoc1.y)) {
                            //p1.move(0, 2);
                            p3.move(0, -2);
                        }
                        if ((pLoc3.y > pLoc1.y)) {
                            //p1.move(0, -2);
                            p3.move(0, 2);
                        }
                        if ((pLoc3.x < pLoc1.x)) {
                            //p1.move(2, 0);
                            p3.move(-2, 0);
                        }
                        if ((pLoc3.x > pLoc1.x)) {
                            //p1.move(-2, 0);
                            p3.move(2, 0);
                        }
                        //p1.update(w, h);
                        p3.update(w, h);
                    }
                    if (p3.collision(p2)) {
                        Rectangle pLoc2 = p2.getLocation();
                        Rectangle pLoc3 = p3.getLocation();
                        if ((pLoc3.y < pLoc2.y)) {
                            //p2.move(0, 2);
                            p3.move(0, -2);
                        }
                        if ((pLoc3.y > pLoc2.y)) {
                            //p2.move(0, -2);
                            p3.move(0, 2);
                        }
                        if ((pLoc3.x < pLoc2.x)) {
                            //p2.move(2, 0);
                            p3.move(-2, 0);
                        }
                        if ((pLoc3.x > pLoc2.x)) {
                            //p2.move(-2, 0);
                            p3.move(2, 0);
                        }
                        //p2.update(w, h);
                        p3.update(w, h);
                    }
                    p1.draw(g2, this);
                    p2.draw(g2, this);
                    p3.draw(g2, this);
                } // end game stuff
                  else  if (gameOver == true) {
                        removeBackground();
                        removeUI();
                        addBackground(new Background(arena.x,arena.y,speed, sprites.get("maroon")));
                        g2.drawImage(sprites.get("maroon"), 0, 0, 700, 390, null);
                        g2.drawImage(sprites.get("gameover"), 0, 0, 700, 390, null);  
                }
                  else if (isGameFinished()) {
                        removeBackground();
                        removeUI();
                //addBackground(new Background(arena.x,arena.y,speed, sprites.get("background")));
                        //g2.drawImage(sprites.get("background"), 0 , 0, 640, 480, null);
                        //g2.clearRect(700, 390, w, h);
                        g2.setBackground(Color.WHITE);
                        g2.drawImage(sprites.get("youWin"), 0, 0, 700, 390, null);
                    }
        
    }
        
    
    public void loadLevel(int level){
 		reader.setLevel(level);
		reader.load();       
    }
        
    /* End the game, and signal either a win or loss */
    public void endGame(boolean win){
    	this.gameOver = true;
    	this.gameWon = win;
    }
    
    public boolean isGameOver(){
    	return gameOver;
    }
    
    public boolean isLevelFinished(){
        return levelFinished;
    }
    
    public void endLevel(){
        this.levelFinished = true;
        game.loadLevel(1);
    }
    
    // signal that we can stop entering the game loop
    public void finishGame(){
    	gameFinished = true;
    }
    
    public boolean isGameFinished(){
        return gameFinished;
    }

    public Graphics2D createGraphics2D(int w, int h) {
        Graphics2D g2 = null;
        if (bimg == null || bimg.getWidth() != w || bimg.getHeight() != h) {
            bimg = (BufferedImage) createImage(w, h);
        }
        g2 = bimg.createGraphics();
        g2.setBackground(getBackground());
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.clearRect(0, 0, w, h);
        return g2;
    }

    /* paint each frame */
    public void paint(Graphics g) {
        if(players.size()!=0)
        	clock.tick();
    	Dimension windowSize = getSize();
        Graphics2D g2 = createGraphics2D(windowSize.width,windowSize.height);
        drawFrame(windowSize.width,windowSize.height, g2);
        g2.dispose();
        g.drawImage(bimg, 0, 0, this);
        
        // interface stuff
        ListIterator<InterfaceObject> objects = ui.listIterator();
        int offset = 0;
        while(objects.hasNext()){
        	InterfaceObject object = objects.next();
        	object.draw(g, offset, windowSize.height);
        	offset += 500;
        }        
    }
    

    /* start the game thread*/
    public void start() {
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    /* run the game */
    public void run() {
    	
        Thread me = Thread.currentThread();
        while (thread == me) {
        	this.requestFocusInWindow();
            repaint();
          
          try {
                thread.sleep(23); // pause a little to slow things down
            } catch (InterruptedException e) {
                break;
            }
            
        }
    }
    
    

    /*I use the 'read' function to have observables act on their observers.
     */
	@Override
	public void update(Observable o, Object arg) {
		AbstractGameModifier modifier = (AbstractGameModifier) o;
		modifier.read(this);
	}
        
	public static void main(String argv[]) {
	    final DubsWorld game = DubsWorld.getInstance();
	    JFrame f = new JFrame("Save Riley");
	    f.addWindowListener(new WindowAdapter() {
		    public void windowGainedFocus(WindowEvent e) {
		        game.requestFocusInWindow();
		    }
	    });
	    f.getContentPane().add("Center", game);
	    f.pack();
	    f.setSize(new Dimension(700, 390));
	    game.setDimensions(700, 500);
	    game.init();
	    f.setVisible(true);
	    f.setResizable(false);
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    game.start();
	}        
}