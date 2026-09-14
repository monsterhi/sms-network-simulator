import javax.swing.*;

import Presentation.MainFrame;

/**
 * Entry point of the SMS transmission simulator.
 *
 * The window is built on the event dispatch thread, as Swing requires.
 */
public class App{

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
            ()->{
                new MainFrame();
            }
        );
    }

}
