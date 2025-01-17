package bubblewrap.io;

import bubblewrap.io.params.ParameterMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class was developed to read in a file that acts as the mail template.
 * Rather than using a fixed set of hard coded HTML, this uses a file for the
 * template structure and then substitutes special tags with the specified content.  
 * The tags for replacement must use self closing tags as follows: <TAG/> 
 * @author hdunsford
 */
public class TemplateReader {  
  
  //<editor-fold defaultstate="collapsed" desc="Static Field">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger =
          Logger.getLogger(TemplateReader.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Field">
  /**
   * The keys are the tag names without the markup so <Tag/> becomes just Tag.
   */
  protected ParameterMap replacementTags;
  /**
   * Path to the Template File
   */
  private String templateFile;
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Initializes a new instance of the TemplateReader class.
   */
  public TemplateReader() {
    this.replacementTags = null;
    this.templateFile = null;
  }
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Private Methods">    
  /**
   * Opens the specified file, ensuring that the string text is filled.
   * @param templatePath String
   * @throws FileNotFoundException
   */
  private String readFile(File file) throws IOException  {
    String result = this.readRawText(file);
    result = this.replaceTags(result);
    return result;
  }
  
  /**
   * This code searches through the original text in the template, searches for
   * specialized tags for replacement and then updates them using the content found
   * in this particular file.
   * @param originalText String
   * @return String
   */
  private String replaceTags(String originalText) throws IOException {
    String result = originalText;
    try {
      if ((this.replacementTags != null) && (!this.replacementTags.isEmpty())) {
        result = this.replacementTags.replaceParameterTags(originalText, null);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.replaceTags Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * This code reads each line, appends it to a single output expression and then
   * creates a final string from the result.
   * @param filePath String
   * @return String
   * @throws IOException
   */
  private String readRawText(File file) throws IOException {
    StringBuilder builder = null;
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      builder = new StringBuilder();
      String line = null;
      while((line = reader.readLine()) != null) {
        builder.append(line).append("\n");
      }
    }
    return builder.toString();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Protected Methods">    
  /**
   * If the replacement tag exists, this will return the current value for the specified
   * tag.  If it does not exist, this returns null and will not throw an exception.
   * @param sKey String
   * @return String (can be null)
   */
  protected String getValue(String sKey)  {
    String result = null;
    if(this.replacementTags.containsKey(sKey)) {
      result = this.replacementTags.get(sKey, null);
    }
    return result;
  }
  
  /**
   * Sets the value for the specified key.  If the key exists, it is replaced with the
   * new value and no exception will be thrown.
   * @param sKey The string tag to set.
   * @param sValue The value to set in the tag.
   */
  protected void setValue(String sKey, String sValue)  {
    try {
      if (this.replacementTags == null) {
        this.replacementTags = new ParameterMap();
      }
      this.replacementTags.put(sKey, sValue);
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.setValue Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Gets the text from the template file and substitutes the mapped properties.
   * @return the template file content substitutions for replacement tags.
   */
  public String getText()  {
    String result = null;
    try {
      if(this.templateFile == null) {
        throw new Exception("The Path to the Template File is not assigned.");
      }
      
      File file = new File(this.templateFile);
      String pPath = file.getAbsolutePath();
      if(!file.exists()) {
        throw new Exception("Template File["
                + this.templateFile + "] does not exist.");
      }
      
      try {
        result = this.readFile(file);
      } catch(IOException pIOErr) {
        throw new Exception("The following read error occurred:\n\t"
                + pIOErr.getMessage());
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getText Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      result = "<html><head></head><body>"
              + "<h3>Error Message</h3>"
              + "<p>Reading the template file failed because:<p><p><i>"
              + pExp.getMessage()
              + "</i></p></body></html>";
    }
    return result;
  }
  
  /**
   * Get the Reader's the assigned templateFile path
   * @return String
   */
  public String getTemplateFile() {
    return this.templateFile;
  }
  
  /**
   * Set TemplateReader's templateFile path
   * @param templateFile the full path to the template file.
   */
  public void setTemplateFile(String templateFile) {
    this.templateFile = DataEntry.cleanString(templateFile);
  }
  //</editor-fold>
}
