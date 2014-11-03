import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

/**
 * Class type for a cube object in the EPOC cube project. Does not move itself,
 * but requires methods in Cube to be called to change the x,y values.
 * 
 * @author Matthew T. Vaught
 * 
 */
public class Cube {
	private static final int CUBE_LENGTH = 75;
	private final int CUBE_3D_COMPONENT;
	private final int CUBE_TOTAL_LENGTH;
	private final BufferedImage LOGO;
	private double x = 0;
	private double y = 0;
	private final int[] direction = { 0, 0, 0, 0 };

	/**
	 * Constructor for the Cube, takes a string that is the location of the logo
	 * to be placed on the square part of the cube. Additionally, sets all
	 * constants of the cube that are used to speed up calculations.
	 * 
	 * @param logoFileLocation
	 *            the location of the logo
	 */
	public Cube(String logoFileLocation) {
		LOGO = getLogo(logoFileLocation);
		CUBE_3D_COMPONENT = (int) (CUBE_LENGTH / 2 / Math.sqrt(2));
		CUBE_TOTAL_LENGTH = CUBE_3D_COMPONENT + CUBE_LENGTH;
	}

	/**
	 * Set the x location of the cube to a completely new value.
	 * 
	 * @param x
	 *            the new x location
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Set the y location of the cube to a completely new value
	 * 
	 * @param y
	 *            the new y location
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Returns a constant value that represents the total length across the
	 * screen.
	 * 
	 * @return the total length of 2D shape
	 */
	public int getTotalLength() {
		return CUBE_TOTAL_LENGTH;
	}

	/**
	 * Draws a white "cube" that is the moving object
	 * 
	 * @param g
	 *            Graphics component
	 */
	public void drawCube(Graphics g) {
		// define width, height, and adjust y axis to make x,y the top left
		// corner of the square part of the cube
		int tempY = (int) y + CUBE_3D_COMPONENT;
		int width = CUBE_LENGTH, height = CUBE_LENGTH;
		// Set fill color
		g.setColor(Color.white);

		// define significant points along the edge of the "cube" shaped
		// polygon
		int[] xCord = new int[6];
		xCord[0] = (int) x; // top left on square
		xCord[1] = (int) x; // bottom left
		xCord[2] = (int) (x + width); // bottom right on square
		xCord[3] = (int) (x + width + CUBE_3D_COMPONENT);
		// bottom right on 3D component
		xCord[4] = xCord[3]; // top right
		xCord[5] = (int) (x + CUBE_3D_COMPONENT); // top left (3D component)
		int[] yCord = new int[6];
		// y coordinates match x.
		yCord[0] = tempY;
		yCord[1] = tempY + height;
		yCord[2] = tempY + height;
		yCord[3] = tempY + height - CUBE_3D_COMPONENT;
		yCord[4] = yCord[3] - height;
		yCord[5] = tempY - CUBE_3D_COMPONENT;
		// draw the polygon
		g.fillPolygon(xCord, yCord, 6);

		// set depth line color
		g.setColor(Color.black);
		// draw the depth lines
		g.drawRect((int) x, tempY, width, height);
		g.drawLine((int) x + width, tempY, (int) x + width + CUBE_3D_COMPONENT,
				tempY - CUBE_3D_COMPONENT);

		// if there is a logo available, center and draw it on the square
		// portion of the cube
		if (LOGO != null) {
			g.drawImage(LOGO, (int) x + (CUBE_LENGTH / 2)
					- (LOGO.getWidth() / 2),
					tempY + (CUBE_LENGTH / 2) - (LOGO.getWidth() / 2), null);
		}

	}

	/**
	 * Checks to see if the coin and the cube have intersected in the window.
	 * 
	 * @param squareX
	 * @param squareY
	 * @param squareLength
	 * 
	 * @return True if they have intersected
	 */
	public boolean checkCollision(int squareX, int squareY, int squareLength) {
		// Basic rectangular collision detection, treats both objects as
		// rectangles.
		if (squareX < x + CUBE_TOTAL_LENGTH && squareX + squareLength > x) {
			// System.out.println("X");
			if (squareY < y + CUBE_TOTAL_LENGTH && squareY + squareLength > y) {

				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a 4-element int array that has the representative directional
	 * motions.
	 * 
	 * @return direction array
	 */
	public int[] getDirection() {
		return direction;
	}

	/**
	 * Sets all values in the direction array to zero.
	 */
	public void resetDirection() {
		Arrays.fill(direction, 0);
	}

	/**
	 * Adds the passed value to the current value of x.
	 * 
	 * @param i
	 *            the value being added
	 */
	public void changeX(double i) {
		x += i;
	}

	/**
	 * Adds the passed value to the current value of y.
	 * 
	 * @param i
	 *            the value being added
	 */
	public void changeY(double i) {
		y += i;
	}

	/**
	 * Returns the current value of x type cast as an int
	 * 
	 * @return x position as int
	 */
	public int getX() {
		return (int) x;
	}

	/**
	 * Returns the current value of y, type cast as an int
	 * 
	 * @return y position as int
	 */
	public int getY() {
		return (int) y;
	}

	/**
	 * Method called by the constructor to get the logo found in the file
	 * fileLocation
	 * 
	 * @param fileLocation
	 *            a String object that represents the path to the logo.
	 * 
	 * @return BufferedImage object of the logo found
	 */
	private BufferedImage getLogo(String fileLocation) {
		BufferedImage logo;
		// Attempt to read in the file
		try {
			logo = ImageIO.read(new File(fileLocation));
		} catch (IOException e) {
			logo = null;
			System.err.println("Unable to get Logo from file");
		}
		// return the logo or null
		return logo;
	}
}
