/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myGames;

import gameCore.DubsWorld;
import static gameCore.DubsWorld.sound;
import java.awt.Image;
import java.awt.Point;
import java.util.Observable;
import modifiers.AbstractGameModifier;
import modifiers.motions.SimpleMotion;


public class Exit extends Ship{
        DubsWorld world = DubsWorld.getInstance();
	public Exit(Ship theShip){
		super(theShip.getLocationPoint(), theShip.getSpeed(), 0, DubsWorld.sprites.get("exit"));  
		this.motion = new SimpleMotion();
		this.motion.addObserver(this);
		//this.weapon = theShip.getWeapon();
	}
        
        public Exit(Point location, int health, Image img){
                super(new Point(location), new Point(0,0), health, img);
        }        

	@Override
	public void update(Observable o, Object arg) {
		AbstractGameModifier modifier = (AbstractGameModifier) o;
		modifier.read(this);
	}
	
        @Override
	public void die(){
        this.show = false;
    	motion.deleteObserver(this);
    	DubsWorld.getInstance().removeClockObserver(motion);
	}
            
}
