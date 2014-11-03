import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * 
 * @author Matthew T. Vaught
 * 
 */
public class MainGUI extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3480754019861279737L;
	/**
	 * 
	 */

	// Both
	private final int PANEL_X = 640;
	private final int PANEL_Y = 640;
	private final Cube CUBE;
	private final String LOGO_LOCATION = "Logo.png";
	private final int[] tick = { 0, 0, 0, 0 };// UP,RIGHT,DOWN,LEFT
	private final boolean training;
	private final int DURATION;
	private MyJPanel theDisplay;
	private boolean run;

	// Training Mode
	private boolean readyTrain = true;
	private final int DEFAULT_TRAINING_DURATION = 20;
	private int start = 0;

	// Testing mode
	private final int COIN_SIZE = 25;
	private final String OUT_FILE_NAME;
	private final int DEFAULT_TESTING_DURATION = 20;
	private long startTime;
	private int coins = 0;
	private int[][] coinLocations;

	/**
	 * Constructor for the MainGUI. Does not create/start the window. Use
	 * .init() to start the window.
	 * 
	 * @param training
	 *            Define if program is going to enter the training mode or the
	 *            testing mode. True if training.
	 */
	public MainGUI(boolean training) {
		// define final variables.
		this.training = training;
		// Create cube object
		CUBE = new Cube(LOGO_LOCATION);

		// Define training/testing specific variables.
		if (training) {// Training
			// Set the Training Duration
			int n = -1;
			// loop until a valid integer is given
			do {
				String s = JOptionPane
						.showInputDialog("Enter the duration of training");
				if (s != null)
					n = Integer.parseInt(s);
				else
					// use the default if "cancel" is pressed in the dialog
					n = DEFAULT_TRAINING_DURATION;
			} while (n <= 0);
			// Set the duration
			DURATION = n;
			// Define "don't-care" value for the output file.
			OUT_FILE_NAME = null;
		} else {// Testing
			// Set the coin locations
			setCoinLocations();
			// Get the testing duration
			int n = -1;
			do {
				String s = JOptionPane
						.showInputDialog("Enter the max duration of testing");
				if (s != null)
					n = Integer.parseInt(s);
				else
					// use default if "cancel" is sent
					n = DEFAULT_TESTING_DURATION;
			} while (n <= 0);
			// Set the duration
			DURATION = n;
			// Get the output file name
			String s;
			do {
				s = JOptionPane
						.showInputDialog("Enter the name of the test.\n--Will be used to label the output file--");
			} while (s == null);
			// Set the output file name
			OUT_FILE_NAME = s;
		}
	}

	/**
	 * Method called by constructor to read in coin location from file
	 * "coins.loc". Will exit the program if file is not found, there is a
	 * file-read error, or if there aren't any locations.
	 */
	private void setCoinLocations() {
		// try-catch will close program if error detected
		try {
			// Create a scanner to the file
			Scanner scan = new Scanner(new File("coins.loc"));
			// get the number of coins, and define array
			coinLocations = new int[scan.nextInt()][2];
			// Read in x-y pairs
			for (int i = 0; i < coinLocations.length; i++) {
				coinLocations[i][0] = scan.nextInt();
				coinLocations[i][1] = scan.nextInt();
			}
			// Close the file
			scan.close();
		} catch (IOException e) {
			System.err
					.println("Could not connect to the coin locations in coins.loc");
			System.exit(1);
		}
		if (coinLocations.length <= 0) {
			System.exit(1);
		}
	}

	/**
	 * Initializes the GUI, creating the window and starting the test/train
	 * programs.
	 */
	public void init() {

		// setup top level of user interface
		Container pane = getContentPane();
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		this.setTitle("Emotiv EPOC 3D Cube");

		// Create display for drawing game state
		theDisplay = new MyJPanel();
		theDisplay.resetCube();
		theDisplay.addKeyListener(theDisplay);
		theDisplay.setBackground(Color.black);
		theDisplay.setPreferredSize(new Dimension(PANEL_X, PANEL_Y));
		c.gridy = 1;
		pane.add(theDisplay, c);
		theDisplay.setFocusable(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create timer, and set the run = true
		run = true;
		Timer timer = createTimer();

		// Mode-specific toggles.
		if (training) {
			System.out.println("PRESS SPACEBAR TO START UP TRAINING");
		} else {
			if (1 == JOptionPane.showConfirmDialog(null,
					"Start? \"No\" will close the program", "Startup",
					JOptionPane.YES_NO_OPTION)) {
				System.exit(0);
			}
		}
		// start the timer
		timer.start();
		// record the start time. [only used by test()]
		startTime = System.nanoTime();
	}

	/**
	 * Method called by init(), returns a Timer object containing the
	 * instructions to execute every cycle.
	 * 
	 * @return the Timer object
	 */
	private Timer createTimer() {
		// Create the instructions that will execute every cycle
		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				// checks to see if program is supposed to be running
				if (run) {
					// Divides into specific modes
					if (training) {
						train();
					} else {
						test();
					}
					// Redraw the window
					repaint();
				}
			}
		};
		// Return the timer which ticks ~.005 s
		return new Timer(5, actionListener);
	}

	/**
	 * The logic that executes every cycle of the "clock" timer for the testing
	 * mode.
	 */
	private void test() {
		// Encapsulate the cube inside the window, preventing it from leaving
		// the screen
		int[] direction = CUBE.getDirection();

		if (CUBE.getX() + CUBE.getTotalLength() > PANEL_X) {
			direction[1] = 0;
		}
		if (CUBE.getX() < 0) {
			direction[3] = 0;
		}
		if (CUBE.getY() + CUBE.getTotalLength() > PANEL_Y) {
			direction[2] = 0;
		}
		if (CUBE.getY() < 0) {
			direction[0] = 0;
		}

		// Iterates through the array that controls the directional movement of
		// the cube.
		for (int i = 0; i < 4; i++) {
			// increase the directional tick.
			tick[i]++;
			// assuming direction value is not zero, activate motion
			// proportionally to the value of direction
			if (direction[i] != 0 && tick[i] % (10 - direction[i]) == 0) {
				// Define specific commands for each direction
				switch (i) {
				case 0: // UP
					CUBE.changeY(-2);
					break;
				case 1: // RIGHT
					CUBE.changeX(2);
					break;
				case 2: // DOWN
					CUBE.changeY(2);
					break;
				case 3: // LEFT
					CUBE.changeX(-2);
					break;
				}
				// Reset the tick
				tick[i] = 0;
			} else if (direction[i] == 0) {
				// Keep the tick at 0 if direction is 0. (Prevents extremely
				// large data values and unexpected results when direction is
				// not zero)
				tick[i] = 0;
			}
		}
		// define a temporary stopTime (ensures better accuracy between the
		// check and the print of the value
		long stopTime = System.nanoTime();
		// Execute if the cube has hit the "coin"
		if (theDisplay.checkCollision()) {
			// increase the number of coins found, automatically displays and
			// calculates for the next coin in the list.
			coins++;
			// Checks to see if there are no more coins, if so:
			if (coins >= coinLocations.length) {
				// stop the test, call close()
				close(stopTime);

			}
			// Execute if the test has exceed the desired length (usually will
			// be larger than the target value by a factor of .01 or less)
		} else if ((stopTime - startTime > (long) DURATION * (long) 1000000000)) {
			// System.out.println(stopTime - startTime);
			close(stopTime);
		}
	}

	/**
	 * The logic that executes every cycle of the "clock" timer for the training
	 * mode.
	 */
	private void train() {
		// end program after four directions are tested
		if (start > 4) {
			System.exit(0);
		}
		// checks to see if the program is waiting for user input to continue
		// the training. If it is, it doesn't execute the code
		if (!readyTrain) {
			// A basic count-down for the training session
			if (tick[0] % (1000 / 5) == 0) {
				System.out.print(" " + (DURATION - (tick[0] / (1000 / 5))));
			}

			// Will enter if the training has run for the set duration
			if (tick[0] > DURATION * (1000 / 5)) {
				// reset tick
				tick[0] = 0;
				// disable cycle, enable checking for signal
				readyTrain = true;
				// Output information to console to direct input
				switch (start) {
				case 1:
					System.out
							.println("\nPRESS SPACEBAR TO START DOWN TRAINING");
					break;
				case 2:
					System.out
							.println("\nPRESS SPACEBAR TO START LEFT TRAINING");
					break;
				case 3:
					System.out
							.println("\nPRESS SPACEBAR TO START RIGHT TRAINING");
					break;
				case 4:
					System.out.println("\nPRESS SPACEBAR TO END PROGRAM");
				}
				// Reset the location of the cube
				theDisplay.resetCube();
				// exit
				return;
			}
			// Code only executes if Duration has not been met
			// Specify action depending on which direction is being trained
			switch (start) {
			case 1:// UP
				CUBE.changeY(-.5);
				break;
			case 2:// DOWN
				CUBE.changeY(.5);
				break;
			case 3:// LEFT
				CUBE.changeX(-.5);

				break;
			case 4:// RIGHT
				CUBE.changeX(.5);

				break;
			}
			// increment tick
			tick[0]++;

			// if cube is going to leave the screen, send it back to the center
			if (CUBE.getX() + CUBE.getTotalLength() > PANEL_X
					|| CUBE.getX() < 0
					|| CUBE.getY() + CUBE.getTotalLength() > PANEL_Y
					|| CUBE.getY() < 0) {
				theDisplay.resetCube();
			}
		}
	}

	/**
	 * Method called by testing to execute when the test duration has ended.
	 * Outputs data to file.
	 */
	private void close(long stopTime) {
		// disable the cycles
		run = false;
		// Tries to print to file, prints to console if it is unable to.
		try {
			// Print file name, duration, number of coins
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(OUT_FILE_NAME + ".txt")));
			out.println("Name Time NumCoins");
			out.println(OUT_FILE_NAME + " "
					+ ((stopTime - startTime) / 1000000000.) + " " + coins);
			out.close();
		} catch (IOException e) {
			System.err.println("Could not write data to file, printing in cmd");
			System.out.println("Name Time NumCoins");
			System.out.println(OUT_FILE_NAME + " "
					+ ((stopTime - startTime) / 1000000000.) + " " + coins);
		}
		// Close the program
		System.exit(0);
	}

	private class MyJPanel extends JPanel implements KeyListener {

		/**
		 *  
		 */

		/**
		 * 
		 */
		private static final long serialVersionUID = 1025120368596590759L;

		/**
		 * paintComponent method is part of the JPanel class and is called to
		 * draw things to the JPanel
		 * 
		 * @param g
		 *            graphics object use for drawing to the JPanel
		 */
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			// draws coin if testing
			if (!training) {
				drawCoin(g, coinLocations[coins][0], coinLocations[coins][1]);
			}
			// draw cube
			CUBE.drawCube(g);

		}

		/**
		 * Checks to see if the coin and the cube have intersected in the
		 * window.
		 * 
		 * @return True if they have intersected
		 */
		public boolean checkCollision() {
			// Basic rectangular collision detection, treats both objects as
			// rectangles.
			int coinX = coinLocations[coins][0];
			int coinY = coinLocations[coins][1];
			if (coinX < CUBE.getX() + CUBE.getTotalLength()
					&& coinX + COIN_SIZE > CUBE.getX()) {
				// System.out.println("X");
				if (coinY < CUBE.getY() + CUBE.getTotalLength()
						&& coinY + COIN_SIZE > CUBE.getY()) {

					return true;
				}
			}
			return false;
		}

		/**
		 * Reset the position of the cube to the ~center of the window
		 */
		public void resetCube() {
			int halfCubeTotalLength = CUBE.getTotalLength() / 2;
			CUBE.setX(PANEL_X / 2 - halfCubeTotalLength);
			CUBE.setY(PANEL_Y / 2 - halfCubeTotalLength);
		}

		/**
		 * Draws a yellow cube that is the coin
		 * 
		 * @param g
		 *            Graphics component
		 * @param x
		 *            coordinate for x-axis
		 * @param y
		 *            coordinate for y-axis
		 */
		private void drawCoin(Graphics g, int x, int y) {
			g.setColor(Color.yellow);
			g.fillRect(x, y, COIN_SIZE, COIN_SIZE);
			/*
			 * Un-comment to draw a square around the cube that represents the
			 * used in collision detection
			 */
			// int cubeLength = getLength(CUBE_LENGTH) + CUBE_LENGTH;
			// g.drawRect((int) cubeX, (int) cubeY, cubeLength, cubeLength);
		}

		/**
		 * Action event for the key being pressed. See java documentation for
		 * description on keyPressed in keyListener
		 */
		@Override
		public void keyPressed(KeyEvent arg0) {
			// training code
			if (training) {
				// check for space press
				if (arg0.getKeyCode() == 32) {
					// if program is accepting input
					if (readyTrain) {
						// disable program accepting input
						readyTrain = false;
						// Increment testing #
						start++;
					}
				}
				// exit to prevent entering testing code
				return;
			}
			// Testing code
			// ----------------------
			// get the char code. (in hindsight, should have started with
			// charID... but hindsight)
			int[] direction = CUBE.getDirection();
			char test = arg0.getKeyChar();
			// check for first row ('1'-'0')
			if ('0' <= test && test <= '9') {
				direction[0] = test - '1';
				if (direction[0] == -1) {
					direction[0] = 9;
				}
			} else {
				// check for other rows
				switch (test) {
				// DIR 1 (q-p)
				case 'q':
					direction[1] = 0;
					break;
				case 'w':
					direction[1] = 1;
					break;
				case 'e':
					direction[1] = 2;
					break;
				case 'r':
					direction[1] = 3;
					break;
				case 't':
					direction[1] = 4;
					break;
				case 'y':
					direction[1] = 5;
					break;
				case 'u':
					direction[1] = 6;
					break;
				case 'i':
					direction[1] = 7;
					break;
				case 'o':
					direction[1] = 8;
					break;
				case 'p':
					direction[1] = 9;
					break;
				// DIR 2 (a-;)
				case 'a':
					direction[2] = 0;
					break;
				case 's':
					direction[2] = 1;
					break;
				case 'd':
					direction[2] = 2;
					break;
				case 'f':
					direction[2] = 3;
					break;
				case 'g':
					direction[2] = 4;
					break;
				case 'h':
					direction[2] = 5;
					break;
				case 'j':
					direction[2] = 6;
					break;
				case 'k':
					direction[2] = 7;
					break;
				case 'l':
					direction[2] = 8;
					break;
				case ';':
					direction[2] = 9;
					break;
				// DIR 3 (z-/)
				case 'z':
					direction[3] = 0;
					break;
				case 'x':
					direction[3] = 1;
					break;
				case 'c':
					direction[3] = 2;
					break;
				case 'v':
					direction[3] = 3;
					break;
				case 'b':
					direction[3] = 4;
					break;
				case 'n':
					direction[3] = 5;
					break;
				case 'm':
					direction[3] = 6;
					break;
				case ',':
					direction[3] = 7;
					break;
				case '.':
					direction[3] = 8;
					break;
				case '/':
					direction[3] = 9;
					break;
				default:
					// System.out.println(arg0.getKeyCode());
					break;
				}
			}
			// Override directions for axis if related arrow key is pressed.
			int code = arg0.getKeyCode();
			if (code >= 37 && code <= 40) {
				for (int i = 0; i < 4; i++) {
					if (code % 2 == 0) {
						if (i % 2 == 0) {
							direction[i] = 0;
						}
					} else {
						if (i % 2 == 1) {
							direction[i] = 0;
						}
					}
				}
			}
			// set direction for each arrow key
			switch (code - 37) {
			case 0:
				direction[3] = 7;
				break;
			case 1:
				direction[0] = 7;
				break;
			case 2:
				direction[1] = 7;
				break;
			case 3:
				direction[2] = 7;
				break;
			case 32 - 37:
				// spacebar: remove all direction settings
				Arrays.fill(direction, 0);
			}
			// ESC: reset cube and set directions to 0
			if (code == 27) {
				Arrays.fill(direction, 0);
				resetCube();
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// Not used

		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// Not used

		}

	}
}
