package bubblewrap.io.enums;

import java.util.Arrays;
import bubblewrap.io.DataEntry;

/**
 *
 * @author kprins
 */
public class ContentTypes {
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Constants">
  public static final String TEXT = "text/plain";
  public static final String HTML = "text/html";
  public static final String XML = "text/xml";
  public static final String CSS = "text/css";
  public static final String GIF = "image/gif";
  public static final String JPEG = "image/jpeg";
  public static final String BMP = "image/x-xbitmap";
  public static final String PNG = "image/x-png";
  public static final String TIFF = "image/tiff";
  public static final String PDF = "application/pdf";
  public static final String RTF = "application/rtf";
  public static final String WORD = "application/msword";
  public static final String EXCEL = "application/ms-excel";
  public static final String PPT = "application/ms-powerpoint";
  public static final String TAR = "application/x-tar";
  public static final String ZIP = "application/zip";
  
  public static final String MULTIPART = "multipart";
  //</editor-fold>
  
  /**
   * Return the ContentType if a Matching extension is found. Return null otherwise.
   * @param sExt String
   * @return String
   */
  public static String getByExtension(String sExt) {
    String sResult = null;
    sExt = DataEntry.cleanString(sExt);
    sExt = (sExt == null)? null: sExt.toLowerCase();
    if (sExt != null) {
      if (Arrays.asList("txt","csv","cmd").contains(sExt)) {
        sResult = ContentTypes.TEXT;
      } else if (Arrays.asList("htm","html").contains(sExt)) {
        sResult = ContentTypes.HTML;
      } else if ("xml".equals(sExt)) {
        sResult = ContentTypes.XML;
      } else if ("css".equals(sExt)) {
        sResult = ContentTypes.CSS;
      } else if ("gif".equals(sExt)) {
        sResult = ContentTypes.GIF;
      } else if (Arrays.asList("jpg","jpeg","jpe").contains(sExt)) {
        sResult = ContentTypes.JPEG;
      } else if ("bmp".equals(sExt)) {
        sResult = ContentTypes.BMP;
      } else if ("png".equals(sExt)) {
        sResult = ContentTypes.PNG;
      } else if (Arrays.asList("tif","tiff").contains(sExt)) {
        sResult = ContentTypes.TIFF;
      } else if ("pdf".equals(sExt)) {
        sResult = ContentTypes.PDF;
      } else if ("rtf".equals(sExt)) {
        sResult = ContentTypes.RTF;
      } else if (Arrays.asList("doc","docx","dot","dotx").contains(sExt)) {
        sResult = ContentTypes.WORD;
      } else if (Arrays.asList("xls","xlsx","xlsm").contains(sExt)) {
        sResult = ContentTypes.EXCEL;
      } else if (Arrays.asList("ppt","pptx","pptm","pss","pssx","pssm",
                               "pot","potx","potm").contains(sExt)) {
        sResult = ContentTypes.PPT;
      } else if ("tar".equals(sExt)) {
        sResult = ContentTypes.TAR;
      } else if (Arrays.asList("zip","zp7").contains(sExt)) {
        sResult = ContentTypes.ZIP;
      }
    }
    return sResult;
  }
}
