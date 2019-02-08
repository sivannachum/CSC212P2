package edu.smith.cs.csc212.p2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class manages our model of gameplay: missing and found fish, etc.
 * @author jfoley
 *
 */
public class FishGame {
	/**
	 * This is the world in which the fish are missing. (It's mostly a List!).
	 */
	World world;
	/**
	 * The player (a Fish.COLORS[0]-colored fish) goes seeking their friends.
	 */
	Fish player;
	/**
	 * The home location.
	 */
	FishHome home;
	/**
	 * These are the missing fish!
	 */
	List<Fish> missing;
	
	/**
	 * These are fish we've found!
	 */
	List<Fish> found;
	/**
	 * Number of steps!
	 */
	int stepsTaken;
	
	/**
	 * Score!
	 */
	int score;
	/**
	 * A reference to a random object, so we can randomize placement of objects in this world.
	 */
	Random rand = ThreadLocalRandom.current();
	
	/**
	 * The number of rocks generated in each game.
	 */
	static final int NUM_ROCKS = 20;
	/**
	 * Create a FishGame of a particular size.
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	public FishGame(int w, int h) {
		world = new World(w, h);
		
		missing = new ArrayList<Fish>();
		found = new ArrayList<Fish>();
		
		// Add a home!
		home = world.insertFishHome();
		
		for (int i=0; i<NUM_ROCKS; i++) {
			world.insertRockRandomly();
		}
		
		world.insertSnailRandomly();
		
		// Make the player out of the 0th fish color.
		player = new Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);
		
		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < Fish.COLORS.length; ft++) {
			Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);
		}
	}
	
	
	/**
	 * How we tell if the game is over: if missingFishLeft() == 0.
	 * @return the size of the missing list.
	 */
	public int missingFishLeft() {
		return missing.size();
	}
	
	/**
	 * This method is how the PlayFish app tells whether we're done.
	 * @return true if the player has won (or maybe lost?).
	 */
	public boolean gameOver() {
		return missing.isEmpty() && found.isEmpty();
	}

	/**
	 * Update positions of everything (the user has just pressed a button).
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1;
				
		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();
		// The player is there, too, let's skip them.
		overlap.remove(this.player);
		
		// If we find a fish, remove it from missing.
		for (WorldObject wo : overlap) {
			// It is missing if it's in our missing list.
			if (missing.contains(wo)) {
				// Remove this fish from the missing list.
				missing.remove(wo);
				
				// Add the fish to the list of found fish so it follows the player fish around
				Fish fish = (Fish) wo;
				found.add(fish);
				
				// Increase score when you find a fish!
				// Fish that move more often are worth more because they are harder to catch.
				if (fish.fastScared) {
					score += 20;
				}
				else {
					score += 10;
				}
			}
			/**
			 * When the player goes home, all its followers also go home (exit the game).
			 */
			if (wo instanceof FishHome) {
				for (int i = found.size() - 1; i >= 0; i--) {
					Fish delete = found.remove(i);
					world.remove(delete);
				}
			}
			/**
			 * If the player has taken too many steps and its list of found fish is too long, it can lose fish from its followers.
			 * There is an 80% chance of losing a fish from the back of a found list of more than 3.
			 */
			if (stepsTaken > 20) {
				double loseFish = rand.nextDouble();
				if (found.size() > 3 && loseFish <= .8) {
					Fish lostAgain = found.remove(found.size() - 1);
					missing.add(lostAgain);
				}
			}
		}
		
		// Make sure missing fish *do* something.
		wanderMissingFish();
		// When fish get added to "found" they will follow the player around.
		World.objectsFollow(player, found);
		// Step any world-objects that run themselves.
		world.stepAll();
	}
	
	/**
	 * Call moveRandomly() on all of the missing fish to make them seem alive.
	 */
	private void wanderMissingFish() {
		/**
		 * These are fish that have found their own way home that need to be removed from the missing list.
		 */
		List<Fish> toRemove = new ArrayList<Fish>();
		for (Fish lost : missing) {
			double randomDouble = rand.nextDouble();
			// A lost fish moves randomly more often if it is fastScared.
			// 80% of the time, lost fish who are fastScared move randomly.
			if (lost.fastScared && randomDouble < 0.8) {
				lost.moveRandomly();
			}
			// 30% of the time, lost fish who are not fastScared move randomly.
			else if (randomDouble < 0.3) {
				lost.moveRandomly();
			}
			/**
			 * Here we check if there are any other world objects in the same position as a missing fish.
			 * The only possible world object a lost fish can go over is the home, but the player can go over lost fish.
			 * We must remove any fish from the list of WorldObjects on the cell.
			 * If an object in the list is not a fish, it must be the home, so
			 * the fish has made it home and we remove it from the game.
			 */
			List<WorldObject> samePlace = lost.findSameCell();
			boolean home = false;
			for (WorldObject wo : samePlace) {
				if (!(wo instanceof Fish)) {
					home = true;
				}
			}
			if (home) {
				toRemove.add(lost);
				world.remove(lost);
			}
		}
		/**
		 * After the for loop has run, we remove any fish that have made it home.
		 */
		for (int i = toRemove.size() - 1; i >= 0 ; i--) {
			Fish delete = toRemove.remove(i);
			missing.remove(delete);
		}
	}

	/**
	 * This gets a click on the grid. We want it to destroy rocks that ruin the game.
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 */
	public void click(int x, int y) {
		// TODO(P2) use this print to debug your World.canSwim changes!
		System.out.println("Clicked on: "+x+","+y+ " world.canSwim(player,...)="+world.canSwim(player, x, y));
		List<WorldObject> atPoint = world.find(x, y);
		for (WorldObject wo : atPoint)
		{
			if (wo instanceof Rock) {
				world.remove(wo);
			}
		}
	}
	
}
