package bubblewrap.mail;

import bubblewrap.app.context.BwAppContext;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import bubblewrap.io.DataEntry;
import bubblewrap.io.enums.ContentTypes;

/**
 *
 * @author kprins
 */
public abstract class MailBodyPart {

// <editor-fold defaultstate="collapsed" desc="Static Proeprties/Methods">

  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(MailBodyPart.class.getName());
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Private/Protected Fields">
  private String contentType;
  private String contentId;
  private BodyPart bodyPart = null;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor - if sContentType=null/""; set ContentType="text/plain"
   * @param contentType
   * @throws Exception
   */
  protected MailBodyPart(String contentType) {

    contentType = DataEntry.cleanString(contentType);
    if (contentType == null) {
      contentType = ContentTypes.TEXT;
    }
    
    try {
      this.contentId = DataEntry.newUniqueId();
    }
    catch(Exception ex) {
      logger.log(Level.FINE, ex.getMessage());
    }
    BodyPart bodyPart = new MimeBodyPart();
    this.contentType = contentType.toLowerCase();
    this.bodyPart = bodyPart;
    if (this.contentId != null) {
      try {
        this.bodyPart.setHeader("CONTENT-ID", this.contentId);
      }
      catch(MessagingException ex)  {
        logger.log(Level.SEVERE, ex.getMessage());
      }
    }
  }

  /**
   * Override - call super.finalize() before disposing the BodyPart
   * @throws Throwable
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.bodyPart = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public/Protected Methods">
  /**
   * Get the BodyPart's Content ID (internally assigned)
   * @return String
   */
  public String getContentId() {
    return contentId;
  }

  /**
   * Get the BodyPart's ContentType
   * @return String
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Get a reference to the instance Message BodyPart. 
   * @return BodyPart or null if no body part is defined
   * @throws MailBodyPartBodyException
   */
  public BodyPart getBodyPart() throws MailBodyException{
    BodyPart result=null;
    if (this.bodyPart == null) {
      throw new MailBodyException("The Message Body Part is unassigned");
    }
    try {
      this.onBuildBodyPart();      
    }
    catch (MessagingException ex){
      throw new MailBodyException("The onBuildBodyPart method failed.", ex);
    }
    
    try {
      if (this.bodyPart.getContent() != null) {
        result = this.bodyPart;
      }
    } catch (IOException ex) {
      throw new MailBodyException("There was an IO error when getting the body.", ex);
    } catch (MessagingException ex) {
      throw new MailBodyException("There was a messaging exception when getting "
              + "the body.", ex);
    }
    return result;
  }
  
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Protected Final Methods">
  /**
   * Add the pMultiParts to a new MimeMultipart and call addContent(pMultiPart)
   * If no Parts are added to the MultiPart, it is not added to the Owner BodyPart
   * @param multiParts List<MailBodyPart>
   * @param subType String
   * @throws Exception
   */
  protected final void addContent(List<MailBodyPart> multiParts, String subType) 
            throws MessagingException {
    if ((multiParts == null) || (multiParts.isEmpty())){
      return;
    }
    subType = DataEntry.cleanString(subType);
    subType = (subType == null)? "mixed": subType;

    MimeMultipart multiPart = new MimeMultipart(subType);
    for (MailBodyPart mailPart: multiParts) {
      BodyPart part = mailPart.getBodyPart();
      if((part != null))
      {
        Object content = null;
        try {
          content = part.getContent();
        } catch (IOException ex) {
          logger.log(Level.FINE, null, ex);
        }
        if (content != null) {
          multiPart.addBodyPart(part);
        }
      }
    }

    if (multiPart.getCount() > 0) {
      this.addContent(multiPart);
    }
  }

  /**
   * Add pContent to the internal BodyPart. if (pContent is Multipart), call
   * setContent(pContent), else call setContent(pContent,msContentType)
   * @param content Object
   * @throws MessagingException
   */
  protected final void addContent(Object content) throws MessagingException {
    if (content != null) {
      if (content instanceof Multipart) {
        Multipart test = (Multipart)content;
        this.bodyPart.setContent(test);
      } else {
        this.bodyPart.setContent(content, this.contentType);
      }
    }
  }

  /**
   * Set the BodyPart's FileName - User onInitBodyPart to set the DataHandler for
   * the File Type.
   * @param pContent Object
   * @throws MessagingException
   */
  protected final void addFileName(String fileName) throws MessagingException {
    fileName = DataEntry.cleanString(fileName);
    if (fileName != null) {
      this.bodyPart.setFileName(fileName);
    }
  }
  
  /**
   * ABSTRACT: Called by getBodyPart to add the MailBodyPart's content to the internal
   * BodyPart before returning the result. User the addContent overloads and the
   * addFileName methods to build the BodyPart.
   * @throws Exception
   */
  protected abstract void onBuildBodyPart() throws MessagingException;
  // </editor-fold>
}
