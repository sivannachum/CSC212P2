package edu.smith.cs.csc212.p2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class FishFood extends WorldObject {
	
	/**
	 * Construct FishFood in our world.
	 * @param world - the grid world.
	 */
	public FishFood(World world) {
		super(world);
	}
	
	@Override
	/**
	 * Fish food is yellow and circular.
	 */
	public void draw(Graphics2D g) {
		g.setColor(Color.YELLOW);
		Ellipse2D.Double food = new Ellipse2D.Double(-.4, -.2, 1, 1);
		g.fill(food);
	}
	
	@Override
	/**
	 * FishFood does not move.
	 */
	public void step() {
		
	}

}
