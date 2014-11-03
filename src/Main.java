import javax.swing.JOptionPane;

/**
 * 
 * @author Matthew T. Vaught
 * 
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int more;
		more = JOptionPane.showConfirmDialog(null, "Enter EPOC mapping mode?",
				"Startup", JOptionPane.YES_NO_OPTION);

		MainGUI mainGUI = new MainGUI(more != 1);
		mainGUI.init();
		mainGUI.pack();
		mainGUI.setVisible(true);
	}

}
