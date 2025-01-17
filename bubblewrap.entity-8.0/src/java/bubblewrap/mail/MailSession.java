package bubblewrap.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;

/**
 * <p>This Class is a Wrapper for the JavaMail Session. It initiates the Mail Session 
 * based on a name passed in via the constructor. It initiates a Message instance to
 * be configured and send. It support five sendMessage override and trap the exceptions
 * generated during the formatting and sending of the email message.</p>
 * <p>It hasError property can be checked after every send to ensure that the send went
 * without any error. If an error occurred, the error information can be retrieved via
 * the getExecption or getErrorMsg(bDetailed) methods.</p>
 * <p><b>NOTE:</b> The error is automatically reset with each call to sendMessage.</p>
 * @author kprins
 */
public class MailSession {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(MailSession.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Mail.Session
   */
  private Session mailSession;
  /**
   * Placeholder the email's From address
   */
  private InternetAddress fromAddress;
  /**
   * Placeholder for the SendFailedException is thrown during a email transmittal
   */
  private SendFailedException error;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor with a SessionName - must be register in the web server as a JavaMail 
   * Session)
   * @param sessionName the name if the JavaMail Session
   * @throws NamingException
   */
  public MailSession(String sessionName) throws NamingException {
    this.fromAddress = null;
    this.mailSession = null;
    
    sessionName = DataEntry.cleanString(sessionName);
    if (sessionName == null) {
      throw new NullPointerException("The Mail Session Name cannot be unassigned");
    }
    
    try {
      this.mailSession = (Session) InitialContext.doLookup(sessionName);
      if (this.mailSession == null) {
        throw new Exception("Connection to Mail Session[" + sessionName + "] failed.");
      }
      
      /* Get the Session Default From */
//      String sFrom = DataEntry.cleanString(this.mpSession.getProperty("mail.from"));
//      if (sFrom != null) {
//        try {
//          InternetAddress[] pFromAddresses = InternetAddress.parse(sFrom);
//          this.mpFrom = ((pFromAddresses == null) || (pFromAddresses.length == 0))? 
//                            null: pFromAddresses[0];
//        } catch (Exception pInExp) {}      
//      }
    } catch (Exception exp) {
      String errMsg = DataEntry.cleanString(exp.getMessage());
      errMsg = (errMsg == null) ? "Connection to Mail Session[" + sessionName
              + "] failed." : errMsg;
      throw new NamingException(this.getClass().getSimpleName()
              + ".new Error:\n " + errMsg);
    }
  }

  /**
   * Override: Call super.finalize before releasing the local resources.
   * @throws Throwable
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.mailSession = null;
    this.fromAddress = null;
  }

  /**
   * Set the Default From Message. Thro an exception is sForm is an invalid Internet 
   * Email Address. A valid address is "User Name <username@domain.com>"
   * <p><b>NOTE:</b> The Session Default From is assigned as a property of GlassFish's
   * JavaMail Session.  The only valid email address is the one for which the JavaMail
   * connection is validated (i.e., as setup in GalssFish). Only the Address.personal
   * can be changed.</p>from@param sFrom the new FromAddress
   * @throws AddressException
   */
  public void setFrom(String from) throws AddressException {
    from = DataEntry.cleanString(from);
    InternetAddress[] addressArr = 
                              (from == null)? null: InternetAddress.parse(from,true);
    if ((addressArr != null) && (addressArr.length > 0)) {
      InternetAddress newAddress = addressArr[0];
      this.setFrom(newAddress);
    }
  }
  
  /**
   * Assign the default From Email Address - can be null
   * @param address InternetAddress
   */
  public void setFrom(InternetAddress address) throws AddressException {
    if (address == null) {
      return;
    }
    if ((this.fromAddress != null) && 
            (!DataEntry.isEq(address.getAddress(),this.fromAddress.getAddress()))) {
        throw new AddressException("The Authentication email address cannot be "
                + "changed, only the email's Personal assignment");
    }
    this.fromAddress = address;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public/Private Methods">
  /**
   * Initiate a new MimeMessage for the MailSession
   * @return Message
   * @throws Exception
   */
  public Message newMessage() throws Exception {
    if (this.mailSession == null) {
      throw new Exception("The Mail Session connection is not initiated");
    }
    return new MimeMessage(this.mailSession);
  }
  
  /**
   * Check after a sendMessage call to see if there were any send errors.
   * @return true if an error was recorded.
   */
  public boolean hasSendError() {
    return (this.error != null);
  }
  
  /**
   * Called to reset the Send Error
   */
  private void resetSendError() {
    this.error = null;
  }
  
  /**
   * Get a reference to the send Errors
   * @return the assigned SendFailedException (or null if there were no errors).
   */
  public SendFailedException getSendExecption() {
    return this.error;
  }
  
  /**
   * Get the Exception's Error message. If (!bDetailed), the message will be the 
   * SendFailedException's message and if assigned, its causes error message. Otherwise,
   * it will be an HTML formatted message with the base message plus a list of all
   * invalid emails address and all valid but not send email addresses. If both these
   * lists are empty, the base error message will be returned.
   * @param detailed true to get a list of the invalid and valid but not send email
   * addresses.
   * @return the current error message or null if the Send Exception is unassigned.
   */
  public String getErrorMsg(boolean detailed) {
    String result = null;
    if (this.error != null) {
      String errMsg = this.error.getMessage();
      Throwable pCause = this.error.getCause();
      if ((pCause != null) && (pCause.getMessage() != null)) {
        errMsg += "; Cause=" + pCause.getMessage();
      }
      
      if (detailed) {
        Address[] addressArr = this.error.getInvalidAddresses();
        String inValid = "";
        if ((addressArr != null) && (addressArr.length > 0)) {
          for (Address address : addressArr) {
            inValid += "<li>" + address.toString() + "</li>";
          }
        }
        
        addressArr = this.error.getValidUnsentAddresses();
        String notSend = "";
        if ((addressArr != null) && (addressArr.length > 0)) {
          for (Address addrerss : addressArr) {
            notSend += "<li>" + addrerss.toString() + "</li>";
          }
        }
        
        if ((!inValid.equals("")) || (!notSend.equals(""))) {
          errMsg = "<p>" + errMsg + "</p>";
          if (!inValid.equals("")) {
            errMsg = "<p><b>Invalid Email Addresses:</b></p><ul>" +
                      inValid +"</ul>";
          }
          if (!notSend.equals("")) {
            errMsg = "<p><b>Valid but not Send Email Addresses:</b></p><ul>" +
                      notSend +"</ul>";
          }
        }
      }
      result = errMsg;
    }
    return result;
  }
  
  /**
   * Overload 1: Initiate and send a message with sSubject and sBody to sAddresses
   * (a delimited list of addresses). Using the Session's From address. If sContentType
   * is undefined it will set it to "text/plain". Calls overload 3.
   * @param sSubject String
   * @param subject String
   * @param contentType String
   * @param addresses String
   * @param toType RecipientType
   * @return true if the email was successfully send. 
   */
  public void sendMessage(String subject, String bodyText, String contentType,
          String addresses, RecipientType toType) {
    try {
      this.resetSendError();
      
      addresses = DataEntry.cleanString(addresses);
      if (addresses == null) {
        throw new Exception("The E-mail Recipient's address is undefined");
      }
      
      Address[] addressArr = InternetAddress.parse(addresses,true);
      if ((addressArr == null) || (addressArr.length == 0)) {
        throw new Exception("Unable to parse E-mail Address(es)["+addresses+"]");
      }
      
      contentType = DataEntry.cleanString(contentType);
      if (contentType == null) {
        contentType  = "text/plain";
      }
      
      List<BodyPart> parts = new ArrayList<>();
      BodyPart body = new MimeBodyPart();
      body.setContent(bodyText, contentType);
      parts.add(body);
      
      this.sendMessage(subject, parts, addressArr, toType);
    } catch (Exception exp) {
      this.error = new SendFailedException(exp.getMessage());
      logger.log(Level.WARNING, "{0}.sendMessage Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Overload 2: Initiate and send a message with sSubject and pParts to sAddresses
   * (a delimited list of addresses). If sContentType is undefined it will set it
   * to "text/plain". 
   * @param sSubject subject
   * @param bodyParts List<BodyPart>
   * @param addresses String
   * @param toType RecipientType
   */
  public void sendMessage(String subject, List<BodyPart> bodyParts,
                String addresses, RecipientType toType)  {
    Address[] addressArr = null;
    try {
      this.resetSendError();
      
      addresses = DataEntry.cleanString(addresses);
      if (addresses == null) {
        throw new Exception("The E-mail Recipient's address is udnefined");
      }
      
      addressArr = InternetAddress.parse(addresses,true);
      if ((addressArr == null) || (addressArr.length == 0)) {
        throw new Exception("Unable to parse E-mail Address(es)["+addresses+"]");
      }
            
      this.sendMessage(subject, bodyParts, addressArr, toType);
    } catch (Exception exp) {
      this.error = new SendFailedException(exp.getMessage());
      logger.log(Level.WARNING, "{0}.sendMessage Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Overload 3: Initiate and send a message with sSubject and pParts to pAddresses
   * (an array of Address).
   * @param subject String
   * @param bodyParts List<BodyPart>
   * @param addressArr Address[]
   * @param toType RecipientType
   */
  private void sendMessage(String subject, List<BodyPart> bodyParts, Address[] addressArr,
          RecipientType toType) {
    try {
      this.resetSendError();

      subject = DataEntry.cleanString(subject);
      subject = (subject == null)? "Empty Subject": subject;

      if ((bodyParts == null) || (bodyParts.isEmpty())) {
        throw new Exception("The E-mail's Body Text is undefined");
      }

      Message emailMsg = this.newMessage();
      emailMsg.setSubject(subject);
      MimeMultipart multiPart = new MimeMultipart("mixed");
      for (BodyPart pPart : bodyParts) {
        multiPart.addBodyPart(pPart);
      }
      emailMsg.setContent(multiPart);
      emailMsg.setRecipients(toType, addressArr);
      if (this.fromAddress != null) {
        emailMsg.setFrom(this.fromAddress);
      }

      this.onSendMessage(emailMsg);      
    } catch (Exception exp) {
      this.error = new SendFailedException(exp.getMessage());
      logger.log(Level.WARNING, "{0}.sendMessage Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Overload 4: Initiate and send an HTML email message with sSubject and pContent to
   * pAddresses (an array of content).
   * @param pContent EmailContent
   * @param pAddresses Address[]
   * @param toType RecipientType
   */
  public void sendMessage(EmailContent content, String toAddresses,
                                          RecipientType toType)  {
    Address fromAddress = null;
    Address[] addressArr = null;
    try {      
      this.resetSendError();

      toAddresses = DataEntry.cleanString(toAddresses);
      if (toAddresses == null) {
        throw new Exception("The E-mail Recipient's address is udnefined");
      }

      addressArr = InternetAddress.parse(toAddresses, false);
      if ((addressArr == null) || (addressArr.length == 0)) {
        throw new Exception("Unable to parse any E-mail Addresses from["
                + toAddresses + "]");
      }
            
      this.sendMessage(content, addressArr, toType);
    } catch (Exception exp) {
      this.error = new SendFailedException(exp.getMessage());
      logger.log(Level.WARNING, "{0}.sendMessage Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }  
  
  /**
   * Overload 5: Initiate and send an HTML email message with sSubject and pContent to
   * pAddresses (an array of Address). 
   * content pContent EmailContent
   * @param pAddresses Address[]
   * @param toType RecipientType
   */
  public void sendMessage(EmailContent content, Address[] addressArr,
                                                  RecipientType toType) {

    try {
      this.resetSendError();

      if (content == null) {
        throw new Exception("The E-mail's Body Text is undefined");
      }

      if ((addressArr == null) || (addressArr.length == 0)) {
        throw new Exception("Unable to parse any E-mail Addresses from["
                + addressArr + "]");
      }
      
      String subject = (content == null) ? null : content.getSubject();
      subject = (subject == null) ? "Empty Subject" : subject;

      Message mailMsg = this.newMessage();
      mailMsg.setSubject(subject);
      BodyPart bodyPart = content.getBodyPart();
      Object partContent = bodyPart.getContent();
      if (partContent instanceof Multipart) {
        Multipart test = (Multipart) partContent;
        mailMsg.setContent(test);
      } else {
        mailMsg.setContent(bodyPart, bodyPart.getContentType());
      }
      mailMsg.setRecipients(toType, addressArr);
      mailMsg.setFrom();
      Address[] fromArr = mailMsg.getFrom();
//      if (this.mpFrom != null) {
//        pMsg.setFrom(this.mpFrom);
//      }

      this.onSendMessage(mailMsg);
    } catch (Exception exp) {
      this.error = new SendFailedException(exp.getMessage());
      logger.log(Level.WARNING, "{0}.sendMessage Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * <p>Send the Message - ignored if mailMsg=null. Throws the NullSessionException if
   * the Mail Session has not been initiated. The MessagingException is thrown by the
   * Transport.send method if the JavaMail can not authenticate or there is something
   * wrong with the message or the addresses of the recipients.</p>
   * <p><b>NOTE:</b> This method uses the static Transport.send method which implies
   * that the JavaMail setting on the server must be setup to Authenticate the 
   * outgoing mail account. In Glassfish you have to set the <b>Default User</b> and
   * you have to add two additional properties:</p><ul>
   *  <li><b>mail.smtp.auth</b>=true</li>
   *  <li><b>mail.smtp.password</b>={the user's password}</li>
   * </ul>
   * @see Transport#send(javax.mail.Message) 
   * @param mailMsg the Email message to be send
   * @throws NullSessionException
   * @throws MessagingException 
   */
  private void onSendMessage(Message mailMsg) {
    if (mailMsg != null) {
      try {
        if (this.mailSession == null) {
          throw new Exception("The Mail Session connection is not initiated");
        } 
        
        mailMsg.setSentDate(DateTime.getNowAsDate());
        mailMsg.saveChanges();
        Transport.send(mailMsg);
      } catch (SendFailedException sendExp) {
        this.error = sendExp;
        logger.log(Level.WARNING, "{0}.onSendMessage Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), sendExp.getMessage()});
      } catch (Exception exp) {
        this.error = new SendFailedException(exp.getMessage());
        logger.log(Level.WARNING, "{0}.onSendMessage Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
  }
  //</editor-fold>
}
