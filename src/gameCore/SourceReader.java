/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameCore;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import modifiers.AbstractGameModifier;
import myGames.*;

public class SourceReader extends AbstractGameModifier implements Observer {
    int w, h;
    Integer position;
    String filename;
    String filename1 = "Chapter07/level1.txt";
    String filename2 = "Chapter07/level2.txt";
    String filename0 = "Chapter07/level0.txt";
    BufferedReader reader; 
    int endgameDelay = 100, level = 0;
	/*Constructor sets up arrays of enemies in a LinkedHashMap*/
	public SourceReader(int level){
		super();
                setLevel(level);
		this.filename = filename;
		String line;
		try {
			reader = new BufferedReader(new InputStreamReader(DubsWorld.class.getResource(filename).openStream()));
			line = reader.readLine();
			w = line.length();
			h=0;
			while(line!=null){
				h++;
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	public void read(Object theObject){
	}
        
	public int getLevel()
	{
		return (this.level);
	}        
        
	public void setLevel(int level){
		this.level = level;
		if(this.level==0){
			this.filename = "Chapter07/level1.txt";
		} else if(this.level==1){
			this.filename = "Chapter07/level2.txt";
		} else if(this.level == 3){
                    this.filename = "Chapter07/Help.txt";
                }
                
	}  
        
	
	public void load(){
		DubsWorld world = DubsWorld.getInstance();
		
		try {
			reader = new BufferedReader(new InputStreamReader(DubsWorld.class.getResource(filename).openStream()));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		String line;
		try {
			line = reader.readLine();
			w = line.length();
			h=0;
			while(line!=null){
				for(int i = 0, n = line.length() ; i < n ; i++) { 
				    char c = line.charAt(i);
				    if(c=='1'){
					int[] controls = {KeyEvent.VK_LEFT,KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER};
					world.addPlayer(new Dubs(new Point(i*32, h*32),world.sprites.get("player1"), controls, "1"));
				    }
				    
				    if(c=='2'){
				    	int[] controls = {KeyEvent.VK_LEFT,KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER};
					world.addPlayer(new Dubs(new Point(i*32, h*32),world.sprites.get("player1"), controls, "1"));
				    }
				    
				    if(c=='3'){
				    	int[] controls = {KeyEvent.VK_LEFT,KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER};
					world.addPlayer(new Dubs(new Point(i*32, h*32),world.sprites.get("player1"), controls, "1"));				    	
				    }
                                    
                                    if(c=='t'){
                                        world.addTNT(new TNT(new Point(i*32, h*32), 0, world.sprites.get("tnt")));
                                    }
                                    
                                    if(c=='d'){
                                        world.addDetonator(new Detonator(new Point(i*32, h*32), 0, world.sprites.get("detonator")));
                                    }
                                    
                                    if(c =='e'){
                                        world.addExit(new Exit(new Point(i*32, h*32), 0, world.sprites.get("exit")));
                                    }
                                    
				    if(c=='w'){
				    	IndestructibleWall wall = new IndestructibleWall(i,h);
				    	world.addBackground(wall);
				    }         
                                    
                		    if(c=='5'){
                                        world.addSaw(new Saw(new Point(i*32, h*32), 0, world.sprites.get("saw")));
				    }         
                                            
				}
				h++;
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
        


	@Override
	public void update(Observable o, Object arg) {
		DubsWorld world = DubsWorld.getInstance();
		if(world.isGameOver()){
			if(endgameDelay<=0){
				world.removeClockObserver(this);
				world.finishGame();
			} else endgameDelay--;
		}   
        }
}
