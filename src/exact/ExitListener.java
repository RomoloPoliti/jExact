package exact;



import java.awt.event.*;

/** A listener that you attach to the top-level JFrame of
 *  your application, so that quitting the frame exits the 
 *  application.
 *  <p>
 *  From <a href="http://courses.coreservlets.com/Course-Materials/">the
 *  coreservlets.com tutorials on servlets, JSP, Struts, JSF, Ajax, GWT, and Java</a>.
 */ 
 
public class ExitListener extends WindowAdapter {
  @Override
  public void windowClosing(WindowEvent event) {
    System.exit(0);
  }
}