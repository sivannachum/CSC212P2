package edu.smith.cs.csc212.p2;

public class FallingRock extends Rock {
	
	/**
	 * Construct a Falling Rock in our world.
	 * @param world - the grid world.
	 */
	public FallingRock(World world) {
		super(world);
	}
	
	@Override
	/**
	 * Falling rocks move down whenever they can.
	 */
	public void step() {
		moveDown();
	}

}
