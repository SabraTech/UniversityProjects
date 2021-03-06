package eg.edu.alexu.csd.oop.draw;

import eg.edu.alexu.csd.oop.draw.gui.Gui;

import java.awt.EventQueue;

import javax.swing.UIManager;

public class Main {

  /**
   * Launch the application.
   *
   * @param args
   *          the arguments
   */

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
          Gui window = new Gui();
          window.getFrame().setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

}
