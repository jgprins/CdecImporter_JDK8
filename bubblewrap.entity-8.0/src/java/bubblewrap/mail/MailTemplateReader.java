package bubblewrap.mail;

import bubblewrap.io.DataEntry;
import bubblewrap.io.TemplateReader;

/**
 * This is an extension to the standard template reader that adds specialized accessors
 * for known tags in a mail template.  Even though a standard template reader can work
 * in this case, this class simply makes remembering what values to set easier.
 * @author hdunsford
 */
public class MailTemplateReader extends TemplateReader {
  
  /**
   * Get the email's Title
   * @return String
   */
  public String getTitle() {
    return this.getValue("title");
  }

  /**
   * Set the email's Title
   * @param title String
   */
  public void setTitle(String title) {
    this.setValue("title",  title);
  }

  /**
   * Get the email's Date
   * @return String
   */
  public String getDate() {
    return this.getValue("date");
  }

  /**
   * Set the email's Date
   * @param date String
   */
  public void setDate(String date) {
    this.setValue("date", date);
  }

  /**
   * Get the email's Time
   * @return String
   */
  public String getTime() {
    return this.getValue("time");
  }

  /**
   * Set the email's Time
   * @param time String
   */
  public void setTime(String time) {
    this.setValue("time", time);
  }

  /**
   * Get the email's Subject
   * @return String
   */
  public String getSubject() {
    return this.getValue("subject");
  }

  /**
   * Set the email's Subject
   * @param subject String
   */
  public void setSubject(String subject) {
    this.setValue("subject", subject);
  }

  /**
   * Get the Sender's email address
   * @return the sender
   */
  public String getSender() {
    return this.getValue("sender");
  }

  /**
   * Set the Sender's email address
   * @param sEmail String
   */
  public void setSender(String sEmail) {
    this.setValue("sender", sEmail);
  }

  /**
   * Get the Sender's Name
   * @return the Sender's Name.
   */
  public String getSenderName() {
    return this.getValue("sendername");
  }

  /**
   * Set the Sender's Name
   * @param sName String
   */
  public void setSenderName(String sName) {
    this.setValue("sendername", sName);
  }

  /**
   * Set the email sender's address 
   * @return the recipient
   */
  public String getRecipient() {
    return this.getValue("recipient");
  }

  /**
   * Get the email's recipients' addresses
   * @param recipient String
   */
  public void setRecipient(String recipient) {
    this.setValue("recipient", recipient);
  }

  /**
   * Get the name of the sending organization
   * @return String
   */
  public String getOrganization() {
    return this.getValue("organization");
  }

  /**
   * Set the name of the sending organization
   * @param organization String
   */
  public void setOrganization(String organization) {
    this.setValue("organization", organization);
  }

  /**
   * Get the name of the Catalog
   * @return String
   */
  public String getCatalog() {
    return this.getValue("catalog");
  }

  /**
   * Set the name of the Catalog
   * @param catalog String
   */
  public void setCatalog(String catalog) {
    this.setValue("catalog", catalog);
  }

  /**
   * Get the eMail Content
   * @return String
   */
  public String getContent() {
    return this.getValue("content");
  }

  /**
   * Set the eMail Content
   * @param sContent String
   */
  public void setContent(String sContent) {
    sContent = DataEntry.cleanString(sContent);
    this.setValue("content", sContent);
  }

  /**
   * Get the eMail Content CSS Styles
   * @return the assigned content styles
   */
  public String getContentStyles() {
    return this.getValue("contentStyles");
  }

  /**
   * Set the eMail Content CSS Style Classes. It automatically pre- and append
   * "&lt;style type='text/css'&gt;" and "&lt;/style&gt;" to <tt>styles</tt>
   * @param styles the new styles
   */
  public void setContentStyles(String styles) {
    styles = DataEntry.cleanString(styles);
    if ((styles != null) && (!styles.startsWith("<style"))) {
      styles = "<style type='text/css'>" + styles + "</style>";
    }
    this.setValue("contentStyles", styles);
  }

  /**
   * Get the eMail address of the administrator
   * @return String
   */
  public String getAdmin() {
    return this.getValue("admin");
  }

  /**
   * Set the eMail address of the administrator
   * @param admin String
   */
  public void setAdmin(String admin) {
    this.setValue("admin", admin);
  }

  /**
   * Get the Url Tag
   * @return String
   */
  public String getUrl() {
    return this.getValue("url");
  }

  /**
   * Set the Url Tag
   * @param url String
   */
  public void setUrl(String url) {
    this.setValue("url", url);
  }
}
