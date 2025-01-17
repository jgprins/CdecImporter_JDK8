package bubblewrap.admin.events;

import java.awt.event.ActionEvent;

/**
 * An event argument for passing to the ValidateAccessListener to validate a
 * user's access to a specified task action.  This ActionEvent supports a
 * @author kprins
 */
@Deprecated
public class ValidateAccessEvent extends ActionEvent {

  /**
   * Placeholder for an Error Message to pass abck to the sender
   */
  private String msErrMsg = null;

  /**
   * The default constructoor where soruce is a reference to the sender, eAction
   * referencing the defined Action and sTask the name of a defined task.
   * @param source Object
   * @param eAction ActionEnum
   * @param sTask String
   */
  public ValidateAccessEvent(Object source, int eAction, String sTask) {
    super(source, eAction, sTask);
  }

  /**
   * Get the currently assign Error Message. return a blank string if no error
   * message
   * @return String
   */
  public String getErrMsg() {
    return (this.msErrMsg == null)? "": this.msErrMsg;
  }

  /**
   * Assign an error message. If the event already has a error message, the new
   * message will be appended.
   * @param sErrMsg String
   */
  public void setErrMsg(String sErrMsg) {
    sErrMsg = ((sErrMsg == null) || (sErrMsg.trim().equals("")))? null: sErrMsg.trim();
    if (sErrMsg != null) {
      this.msErrMsg = (this.msErrMsg == null)? sErrMsg:
        this.msErrMsg+";\n "+sErrMsg;
    }
  }

  /**
   * Get whether the event has an assigned error message.
   * @return boolean
   */
  public boolean hasError() {
    return (this.msErrMsg != null);
  }
}
