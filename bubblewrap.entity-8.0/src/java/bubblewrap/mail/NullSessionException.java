/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bubblewrap.mail;

/**
 *
 * @author hdunsford
 */
public class NullSessionException extends Exception{
  
  /**
   * Instantiates a new instance of the NullSessionException class.
   * @param message The string message for this exception.
   */
  public NullSessionException(String message)
  {
    super(message);
  }
  
  /**
   * Initializes a new instance of the NullSessionException with the default text.
   */
  public NullSessionException()
  {
    super("The mail session was null.");
  }
}
