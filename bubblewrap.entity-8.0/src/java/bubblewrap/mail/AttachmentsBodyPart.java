package bubblewrap.mail;


import bubblewrap.io.DataEntry;
import bubblewrap.io.enums.ContentTypes;
import bubblewrap.io.files.FileManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author kprins
 */
public class AttachmentsBodyPart extends MailBodyPart {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Internal Placeholder for the attachment filenames
   */
  private List<String> fileNameList = null;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor(s)">
  /**
   * Constructor - with ContentsType = MULTIPART
   */
  public AttachmentsBodyPart(){
    super(ContentTypes.MULTIPART);
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Add a new Attachment
   * @param filePath String
   * @throws Exception
   */
  public void addAttachment(String filePath) throws FileNotFoundException {
    filePath = DataEntry.cleanString(filePath);
    File attachFile = new File(filePath);
    if (!attachFile.isFile()) {
      throw new FileNotFoundException("Attachment Path[" + filePath
              + "] is not not for a file.");
    }
    
    if (this.fileNameList == null) {
      this.fileNameList = new ArrayList<String>();
    }
    
    this.fileNameList.add(filePath);
  }
  
  /**
   * Return true is the BodyPart contains no attachments
   * @return boolean
   */
  public boolean isEmpty() {
    return ((this.fileNameList == null) || (this.fileNameList.isEmpty()));
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="MailBodyPart Overrides">
  /**
   * OVERRIDE: Add the Attachments to the BodyPart
   * @throws Exception
   */
  @Override
  protected void onBuildBodyPart() throws MessagingException{
    if (this.isEmpty()) {
      return;
    }
    
    Multipart multiPart = new MimeMultipart();
    for (String filePath: this.fileNameList) {
      File fileObj = new File(filePath);
      if ((fileObj != null) && (fileObj.isFile())) {
        String ext = FileManager.getFileExtension(filePath);
        String baseName = FileManager.getBaseFileName(filePath);
        
        MimeBodyPart attchement = new MimeBodyPart();
        DataSource dataSrc = new FileDataSource(filePath);
        attchement.setDataHandler(new DataHandler(dataSrc));
        attchement.setFileName(fileObj.getName());
        if (baseName != null) {
          attchement.setContentID(baseName);
        }
        multiPart.addBodyPart(attchement);
      }
    }
    if (multiPart.getCount() > 0) {
      this.addContent(multiPart);
    }
  }
  //</editor-fold>
}
