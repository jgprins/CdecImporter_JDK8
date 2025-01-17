package bubblewrap.core.events;

import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The RequestArgs is used internally by a class when calling a custom overrides to
 * request a specific result.  The custom handler can assign null, a valid return value,
 * or an error to indicate that the request has been handled.  Typically, the caller
 * will validate whether the request is handled before taking some default action.
 * if the new RequestArgs(bMultiErrors=true) constructor is used, the RequestArgs will
 * store all the errors that are assigned during the processing, otherwise, it will only
 * store the first error.  If (multiErrors=true), the getAllError will return a list 
 * with all errors and the getErrorMsg will return a ";" delimited list of errors.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class RequestArgs<T> implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private T requestResult = null;
  private List<String> errMsgList = null;
  private Boolean handled = false;
  private Boolean multipleErrors = false;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Constructor">
  /**
   * Public Constructor
   */
  public RequestArgs() {
    this.requestResult = null;
    this.errMsgList = null;
    this.handled = false;
    this.multipleErrors = false;
  }
  
  /**
   * Public Constructor
   */
  public RequestArgs(boolean multipleErrors) {
    this();
    this.multipleErrors = multipleErrors;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Protected methods">
  /**
   * CAN OVERRIDE: This method is called to return a 'default' result when the request
   * is unhandled. The base method returns null.
   * @return T
   */
  protected T getUnhandledResult() {
    return null;
  }

  /**
   * CAN OVERRIDE: This method is called to return a 'default' result when the request
   * has an assigned error message. The base method returns null.
   * @return T
   */
  protected T getOnErrorResult() {
    return null;
  }
  
  /**
   * CAN OVERRIDE: Calling this method set the allowMultipleError=true. (default=false).
   * Inheritors can override this method to get public access to method.
   */
  protected void allowMultipleErrors() {
    this.multipleErrors = true;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public methods">
  /**
   * Get the assigned result, the onErrorResult if hasError or the UnhandledResult if
   * not handled.
   * @return T
   */
  public final T getResult() {
    return (this.handled)? 
            ((this.hasError())? this.getOnErrorResult(): this.requestResult):
            this.getUnhandledResult();
  }

  /**
   * Set the Return value and assign isHandled=true.  Ignored if has been
   * handled previously. pResult can be null.
   * @param result
   */
  protected void setResult(T result) {
    if (!this.handled) {
      this.requestResult = result;
      this.handled = true;
    }
  }

  /**
   * Return if the Error Message is assigned
   * @return boolean
   */
  public final boolean hasError() {
    return ((this.errMsgList != null) && (!this.errMsgList.isEmpty()));
  }

  /**
   * Get the Error Message(s) if assigned. Otherwise, returns "". If MultiErrors are
   * supported it will return a HTML formatted List is multiple errors are recorded.
   * @return String
   */
  public final String getErrorMsg() {
    return this.getHtmlErrorMsg(null);
  }

  /**
   * Get the Error Message(s) if assigned. Otherwise, returns "". If MultiErrors are
   * supported it will return a HTML formatted List is multiple errors are recorded.
   * if (caption!=null), add <p>caption<p> prefix to the Error Message
   * @param caption String (can be null)
   * @return String
   */
  public final String getErrorMsg(String caption) {
    return this.getHtmlErrorMsg(caption);
  }
 
  /**
   * Get the Error Message(s) if assigned. Otherwise, returns "". If MultiErrors are
   * supported and (asHtml=true), it will return a HTML formatted List, else it
   * will return a delimited text sting with the delimiter="; \n\r".
   * @param asHtml boolean
   * @param caption String (can be null)
   * @return String
   */
  public final String getErrorMsg(boolean asHtml, String caption) {
    return (asHtml)? this.getHtmlErrorMsg(caption): 
                            this.getTextErrorMsg(caption);
  }
 
  /**
   * Get the Error Message(s) if assigned. Otherwise, returns "". If MultiErrors are
   * supported it will return a delimited List of errors, with the delimiter="; \n\r".
   * @return String
   */
  private String getTextErrorMsg(String caption) {
    String result = null;
    caption = DataEntry.cleanString(caption);
    if ((this.errMsgList != null) && (!this.errMsgList.isEmpty())) {
      if (this.errMsgList.size() == 1) {
        result = this.errMsgList.get(0);
      } else {
        String prefix = (caption == null)? "": " - ";
        for (String errMsg : this.errMsgList) {
          if (result == null) {
            result = "";
          } else {
            result += "; \n\r";
          }
          result += prefix + errMsg;
        }
        if (caption != null) {
          result = caption + " \n\r" + result;
        }
      }
    }
    return (result == null) ? "" : result;
  }
  
  /**
   * Get the Error Message(s) if assigned. Otherwise, returns "". If MultiErrors are
   * supported it will return a HTML formatted List is multiple errors are recorded.
   * @return String
   */
  private String getHtmlErrorMsg(String caption) {
    String result = null;
    if ((this.errMsgList != null) && (!this.errMsgList.isEmpty())) {
      caption = DataEntry.cleanString(caption);
      result = (caption != null)? "<p>" + caption + "</p><ul>": "<ul>";
      for (String errMsg : this.errMsgList) {
        result += "<li>" + errMsg + "</li>";
      }
      result += "</ul>";
    }
    return (result == null) ? "" : result;
  }
  
  /**
   * Get a reference to the list of errors. Return null if no errors are set.
   * @return List<String>
   */
  public final List<String> getAllErrors() {
    return this.errMsgList;
  }

  /**
   * Set the Error Message if assigned. Also set isHandled=true (event if errMsg = null). 
   * Ignored if has been handled previously.
   * @param errMsg String
   */
  public final void setErrorMsg(String errMsg) {
    errMsg = DataEntry.cleanString(errMsg);
    if (errMsg == null) {
      this.handled = true;
      return;
    }
    
    if ((!this.handled) || (this.multipleErrors))  {
      if (this.errMsgList == null) {
        this.errMsgList = new ArrayList<>();
      }
      this.errMsgList.add(errMsg);
      this.handled = true;
    }
  }

  /**
   * Get the RequestArgs' isHandled state
   * @return boolean
   */
  public final boolean isHandled() {
    return this.handled;
  }

  /**
   * Reset the RequestArgs.
   */
  public final void resetArgs() {
    this.requestResult = null;
    this.errMsgList = null;
    this.handled = false;
  }
  // </editor-fold>
}
