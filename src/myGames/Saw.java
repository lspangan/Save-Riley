/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myGames;

import gameCore.DubsWorld;
import java.awt.Image;
import java.awt.Point;
import java.util.Observable;
import modifiers.AbstractGameModifier;
import modifiers.motions.SimpleMotion;

/**
 *
 * @author leannapangan
 */
public class Saw extends Ship{
        DubsWorld world = DubsWorld.getInstance();
	public Saw(Ship theShip){
		super(theShip.getLocationPoint(), theShip.getSpeed(), 1, DubsWorld.sprites.get("saw"));               
		this.motion = new SimpleMotion();
		this.motion.addObserver(this);
		//this.weapon = theShip.getWeapon();
	}
        
        public Saw(Point location, int health, Image img){
                super(new Point(location), new Point(0,-2), health, img);
                this.motion = new SimpleMotion();
                this.motion.addObserver(this); 
        }        

	@Override
	public void update(Observable o, Object arg) {
		AbstractGameModifier modifier = (AbstractGameModifier) o;
		modifier.read(this);
	}
	
        
	@Override
	public void die(){
    	this.show=false;
        BigExplosion explosion = new BigExplosion(new Point(location.x,location.y));
    	DubsWorld.getInstance().addBackground(explosion);          
    	//weapon.deleteObserver(this);
    	motion.deleteObserver(this);
    	DubsWorld.getInstance().removeClockObserver(motion);
        this.respawn();
	}  
}
