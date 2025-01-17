/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bubblewrap.mail;

/**
 *
 * @author hdunsford
 */
public class MailBodyException extends NullPointerException {
  /**
   * Creates a new instance of the MailBodyPartNullBodyException Class
   * @param message 
   */
  public MailBodyException(String message) {
    super(message);
  }
  
  public MailBodyException(String message, Exception inner) {
    super(message);
    super.initCause(inner);
  }
}
