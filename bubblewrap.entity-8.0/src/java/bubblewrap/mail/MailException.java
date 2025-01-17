/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bubblewrap.mail;

/**
 *
 * @author hdunsford
 */
public class MailException extends Exception {
  
  /**
   * Creates a new instance of a MailException
   * @param Message 
   */
  public MailException(String message)
  {
    super(message);
  }
  
}
