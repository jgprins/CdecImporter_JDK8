package bubblewrap.core.enums;

/**
 * The Navigation RequestEnums are used by the RequestContext to classify the HTTP
 * Request Type for more specific handling of request.
 * @author kprins
 */
public class RequestEnums {
  /**
   * Unclassified Request Type
   */
  public static final int NONE = 0x0000;
  /**
   * The HttpRequest.method = GET (Address or Redirect Request)
   */
  public static final int ONGET = 0x0001;
  /**
   * The HttpRequest.method = POST (Typically from a form submit)
   */
  public static final int ONPOST = 0x0002;
  /**
   * The HttpRequest.method = PUT (seldom used) 
   */
  public static final int ONPUT = 0x0004;
  /**
   * A Partial Context has been submitted via an Ajax Request.
   */
  public static final int AJAX = 0x0008;
  /**
   * The Page's Form is submits via Submit[YES] - not reset, cancel, back, etc.
   * Set if 
   */
  public static final int FORMSUBMIT = 0x0010;
  /**
   * The Page's Form is submits via Submit[RELOAD] - not reset, cancel, back, etc.
   */
  public static final int FORMRELOAD = 0x0020;
  /**
   * This an Applet Request
   */
  public static final int FORAPPLET = 0x0040;
  /**
   * This an Application Action Request (Get request with an ActionId parameter)
   */
  public static final int APPACTION = 0x0080;
  /**
   * The Ajax request a NavSender callback.
   */
  public static final int BWAJAX = 0x0100;
  /**
   * The NavSender callback triggered a redirect to a prior page.
   */
  public static final int NAVSENDERREDIRECT= 0x0200;
  /**
   * The Ajax Request is fired to execute an Submit Action. This will fire the 
   * NavigationContext.onAjaxSubmit event, which will be propagated to all listening 
   * ContentViews.
   */
  public static final int AJAXSUBMIT= 0x0400;
  /**
   * The Ajax Request triggered and assigned a redirect
   */
  public static final int AJAXREDIRECT= 0x0600;
  /**
   * The Ajax request a NavSender callback.
   */
  public static final int BWAJAXUPLOAD = 0x0800;

  /**
   * Get whether the request method=POST
   * @param eStatus int
   * @return boolean
   */
  public static boolean isPost(int eStatus) {
    return ((eStatus & RequestEnums.ONPOST) == RequestEnums.ONPOST);
  }
    
  /**
   * Get whether the Current Request is triggered by an Request[ONPOST|FORMRELOAD]
   * @param eStatus int
   * @return boolean
   */
  public static boolean isFormReload(int eStatus) {
    return ((eStatus & (RequestEnums.ONPOST | RequestEnums.FORMRELOAD)) == 
            (RequestEnums.ONPOST | RequestEnums.FORMRELOAD));
  }
 
  /**
   * Get whether the Current Request is triggered by an Request[ONPOST|FORMSUBMIT]
   * @param eStatus int
   * @return boolean
   */
  public static boolean isFormSubmit(int eStatus) {
    return ((eStatus & (RequestEnums.ONPOST | RequestEnums.FORMSUBMIT)) == 
            (RequestEnums.ONPOST | RequestEnums.FORMSUBMIT));
  }
 
  /**
   * Get whether the Current Request is triggered by an Request[ONPOST|FORAPPLET]
   * @param eStatus the RequestEnums value to evaluate
   * @return true if (ONPOST |FORAPPLET)
   */
  public static boolean isAppletPost(int eStatus) {
    return ((eStatus & (RequestEnums.ONPOST | RequestEnums.FORAPPLET)) == 
            (RequestEnums.ONPOST | RequestEnums.FORAPPLET));
  }
 
  /**
   * Get whether the Current Request is triggered by an Request[ONGET|APPACTION]
   * @param eStatus the RequestEnums value to evaluate
   * @return true if (ONGET |APPACTION)
   */
  public static boolean isAppAction(int eStatus) {
    return ((eStatus & (RequestEnums.ONGET | RequestEnums.APPACTION)) == 
            (RequestEnums.ONGET | RequestEnums.APPACTION));
  }
  
  /**
   * Get whether the Current Request is triggered by an Request[AJAX]
   * @param eStatus int
   * @return boolean
   */
  public static boolean isAjax(int eStatus) {
    return ((eStatus & RequestEnums.AJAX) == RequestEnums.AJAX);
  }
    
  /**
   * Get whether the Current Request is triggered by an Request[AJAX|NAVSENDERAJAX]
   * @param eStatus int
   * @return boolean
   */
  public static boolean isBwAjax(int eStatus) {
    return ((eStatus & (RequestEnums.AJAX | RequestEnums.BWAJAX)) == 
            (RequestEnums.AJAX | RequestEnums.BWAJAX));
  }
    
  /**
   * Get whether the Current Request is triggered by an Request[AJAX|BWAJAXUPLOAD]
   * @param eStatus the requestType to test
   * @return true is a BwFileUploadRequest
   */
  public static boolean isBwFileUploadAjax(int eStatus) {
    return ((eStatus & (RequestEnums.AJAX | RequestEnums.BWAJAXUPLOAD)) == 
            (RequestEnums.AJAX | RequestEnums.BWAJAXUPLOAD));
  }
  
  /**
   * Get whether the Current Request is triggered by an Request[AJAX|NAVSENDERREDIRECT]
   * @param eStatus int
   * @return boolean
   */
  public static boolean isNavSenderRedirect(int eStatus) {
    return ((eStatus & (RequestEnums.AJAX | RequestEnums.NAVSENDERREDIRECT)) == 
            (RequestEnums.AJAX | RequestEnums.NAVSENDERREDIRECT));
  }
 
  /**
   * Get whether the Current Request is triggered by an Request[AJAX|AJAXSUBMIT].
   * @param eStatus int
   * @return boolean
   */
  public static boolean isAjaxSubmit(int eStatus) {
    return ((eStatus & (RequestEnums.AJAX | RequestEnums.AJAXSUBMIT)) == 
            (RequestEnums.AJAX | RequestEnums.AJAXSUBMIT));
  }
 
  /**
   * Get whether the Current Request is triggered by an Request[AJAX|AJAXSUBMIT].
   * @param eStatus int
   * @return boolean
   */
  public static boolean isAjaxRedirect(int eStatus) {
    return ((eStatus & (RequestEnums.AJAX | RequestEnums.AJAXREDIRECT)) == 
            (RequestEnums.AJAX | RequestEnums.AJAXREDIRECT));
  }

  /**
   * Get whether the Request eStatus and is triggered by Method[eMethod]. Return true
   * if eMothod=[ONGET|ONPOST|ONPUT|AJAX] and eStatus include eMethod.
   * @param eStatus int
   * @param eMethod int
   * @return boolean
   */
  public static boolean isMethod(int eStatus, int eMethod) {
    boolean bResult = ((eMethod & 0x00F) != 0x0000);
    if (bResult) {
      bResult = ((eStatus & eMethod) == eMethod);
    }
    return bResult;
  }
}

