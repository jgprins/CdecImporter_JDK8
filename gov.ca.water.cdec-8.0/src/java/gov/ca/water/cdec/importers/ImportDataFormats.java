package gov.ca.water.cdec.importers;

/**
 * <p>Enums with the following Data Format Types:</p><ul>
 *  <li><b>{@linkplain #NONE}:</b> - (0) Undefined Format</li>
 *  <li><b>{@linkplain #CDEC_SHEF_A}:</b> - (1) CDEC SHEF.A Format</li>
 *  <li><b>{@linkplain #CDEC_SHEF_B}:</b> - (2) CDEC SHEF.B Format</li>
 *  <li><b>{@linkplain #CDEC_SHEF_E}:</b> - (3) CDEC SHEF.E Format</li>
 * </ul>
  * @author kprins
 */
public enum ImportDataFormats {
  
  // <editor-fold defaultstate="collapsed" desc="Static Enums">
  /**
   * Undefined Format [0, ""]
   */
  NONE(0,"","Undefined Format"),
  /**
   * CDEC SHEF.A Format [1, ".A]
   */
  CDEC_SHEF_A(1,".A","CDEC SHEF.A Format"),
  /**
   * CDEC SHEF.B Format [2, ".B"]
   */
  CDEC_SHEF_B(2,".B","CDEC SHEF.B Format"),
  /**
   * CDEC SHEF.E Format [3, ".E"]
   */
  CDEC_SHEF_E(3, ".E", "CDEC SHEF.E Format");
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  public final int intValue;
  public final String formatId;
  public final String label;
  /**
   * Public Constructor  
   */
  private ImportDataFormats(int intValue, String formatId, String label) {
    this.intValue = intValue;
    this.formatId = formatId;
    this.label = label;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Static Method">
//  /**
//   * Get the FormatID (e.g., ".A") for the specified SHEF Format. Throw an exception
//   * if eFormat is not a valid format.
//   * @param formatId int (ImportDataFormats)
//   * @return String
//   * @throws Exception
//   */
//  public static String getFormatId(int formatId) throws Exception {
//    String sID = null;
//    if (formatId == ImportDataFormats.CDEC_SHEF_A) {
//      sID =".A";
//    } else if (formatId == ImportDataFormats.CDEC_SHEF_B) {
//      sID =".B";
//    } else if (formatId == ImportDataFormats.CDEC_SHEF_E) {
//      sID =".E";
//    } else {
//      throw new Exception("ImportDataFormats[" + Integer.toString(formatId)
//              + "].FormatId is not supported ");
//    }
//    return sID;
//  }
//  
//  /**
//   * Get whether the DataFormat supports a DataPareser
//   * @param eFormat the DataFormat if interest
//   * @return true if a TimeSeriesDataParser is supported.
//   */
//  public static boolean hasDataParser(int eFormat) {
//    boolean bResult = false;
//    switch (eFormat) {
//      case ImportDataFormats.CDEC_SHEF_A:
//      case ImportDataFormats.NWS_10DAY_FNF_CAST:
//      case ImportDataFormats.NWS_20DAY_FNF_CAST:
//      case ImportDataFormats.NWS_AJ_FNF_CAST:
//        bResult = true;
//        break;
//      default:
//        throw new AssertionError();
//    }
//    return bResult;
//  }
//
//  /**
//   * Get the DataParser for the specified DataFormat
//   * @param eFormat the DataFormat if interest
//   * @return an instance of the DataPaser or null if not supported.
//   */
//  @SuppressWarnings("unchecked")
//  public static <TParser extends DataParser> TParser getDataParser(int eFormat) {
//    TParser pResult = null;
//    switch (eFormat) {
//      case ImportDataFormats.CDEC_SHEF_A:
//        pResult = (TParser) new SHEFDataParser(eFormat);
//        break;
//      case ImportDataFormats.NWS_10DAY_FNF_CAST:
//        pResult = (TParser) new NWS10DayFcastParser();
//        break;
//      case ImportDataFormats.NWS_20DAY_FNF_CAST:
//        pResult = (TParser) new NWS20DayFcastParser();
//        break;
//      case ImportDataFormats.NWS_AJ_FNF_CAST:
//        pResult = (TParser) new NWSAJFcastParser();
//        break;
//      default:
//        throw new AssertionError();
//    }
//    return pResult;
//  }
// 
//  /**
//   * Get the resource file path using the ImportDataFormats' assigned {@linkplain 
//   * ResourceFile} annotation settings and the specified sensorId to create a resource
//   * file name and path.  It calls the {@linkplain FileManager#getRealResourcePath(
//   * java.lang.String) FileManager.getRealResourcePath} method to initiate the returned
//   * File.
//   * @param eFormat the ImportDataFormats of the file.
//   * @param sSensorId the unique ID for which the file path is generated.
//   * @return the resource file "real" path.
//   */
//  public static String getRelativeResPath(int eFormat, String sSensorId) {
//    String sResult = null;
//    try {
//      String sEnum = EnumInfo.getEnumName(ImportDataFormats.class, eFormat);
//      ResourceFile pAnnot = EnumInfo.getFieldAnnotation(ImportDataFormats.class, eFormat, 
//                                  ResourceFile.class);
//      if (pAnnot != null) {
//        sSensorId = DataEntry.cleanString(sSensorId);
//        if ((sSensorId == null) || (sSensorId.equals("."))) {
//          throw new Exception("The Resource's SensorId reference cannot be undefined.");
//        }
//        
//        String sExt = DataEntry.cleanString(pAnnot.fileExt());
//        if ((sExt == null) || (sExt.equals("."))) {
//          throw new Exception("DataFormat[" + sEnum + "]'s Resource File Extension is "
//                  + "undefined.");
//        }
//        
//        String sRelPath = DataEntry.cleanString(pAnnot.relativePath());
//        if (sRelPath == null) {
//          throw new Exception("DataFormat[" + sEnum + "]'s Resource File Relative "
//                  + " resoruce Path is undefined.");
//        }
//        sRelPath = FileManager.toUnixFilename(sRelPath);
//        if (!sRelPath.endsWith("/")) {
//          sRelPath += "/";
//        }
//        if (!sRelPath.startsWith("/")) {
//          sRelPath = "/" + sRelPath;
//        }
//        
//        String sFileName = FileManager.toUnixFilename(sSensorId);
//        if (sSensorId.endsWith(".")) {
//          if (sExt.startsWith(".")) {
//            sExt = sExt.substring(1);
//          }
//        } else if (!sExt.startsWith(".")) {
//          sExt = "." + sExt;
//        }
//        sFileName += sExt;
//        
//        sResult = sRelPath + sFileName;
//      }
//    } catch (Exception pExp) {
//      throw new AssertionError("ImportDataFormats.getRelativeResPath Error:\n " 
//              + pExp.getMessage());
//    }
//    return sResult;
//  }
//  
//  /**
//   * Get the resource file path using the ImportDataFormats' assigned {@linkplain 
//   * ResourceFile} annotation settings and the specified sensorId to create a resource
//   * file name and path. It first call {@linkplain #getRelativeResPath(int, 
//   * java.lang.String) getRelativeResPath} to retrieve the ImportDataFormats' Resource
//   * File's relative path. It then calls {@linkplain FileManager#getRealResourcePath(
//   * java.lang.String) FileManager.getRealResourcePath} to initiate the Resource 
//   * File's real file system path.
//   * @param eFormat the ImportDataFormats of the file.
//   * @param sSensorId the unique ID for which the file path is generated.
//   * @return the resource file "real" path.
//   */
//  public static File getResourcePath(int eFormat, String sSensorId) {
//    File pResult = null;
//    try {
//      String sRelPath = ImportDataFormats.getRelativeResPath(eFormat, sSensorId);
//      if (sRelPath != null) {
//        File pResPath = FileManager.getRealResourcePath(sRelPath);
//        if (pResPath == null) {
//          File pTmpPath = FileManager.getTempDirectory();
//          pResPath = new File(pTmpPath,sRelPath);
//        }        
//        pResult = pResPath;
//      }
//    } catch (Exception pExp) {
//      throw new AssertionError("ImportDataFormats.getResourcePath Error:\n " 
//              + pExp.getMessage());
//    }
//    return pResult;
//  }
//  
//  /**
//   * Get the resource file path using the ImportDataFormats' assigned {@linkplain 
//   * ResourceFile} annotation settings and the specified sensorId to create a resource
//   * file name and path. It first call {@linkplain #getRelativeResPath(int, 
//   * java.lang.String) getRelativeResPath} to retrieve the ImportDataFormats' Resource
//   * File's relative path. It then calls {@linkplain HttpUtils#buildRequestUrl(
//   * java.lang.String) HttpUtils.buildRequestUrl} to initiate the Resource 
//   * resource URL.
//   * @param eFormat the ImportDataFormats of the file.
//   * @param sSensorId the unique ID for which the file path is generated.
//   * @return the resource file's relative URL.
//   */
//  public static String getResourceUrl(int eFormat, String sSensorId) {
//    return ImportDataFormats.getRelativeResPath(eFormat, sSensorId);
//  }
//  
//  /**
//   * Get the DataFormat's ResourceFile annotation Content-Type assignment.
//   * @param eFormat the ImportDataFormats of the file.
//   * @return the assigned value or null if the ImportDataFormats does not have an 
//   * ResourceFile annotation or the Content-Type is not assigned.
//   */
//  public static String getResourceContentType(int eFormat) {
//    String sResult = null;
//    try {
//      ResourceFile pAnnot = EnumInfo.getFieldAnnotation(ImportDataFormats.class, eFormat, 
//                                  ResourceFile.class);
//      if (pAnnot != null) {
//        sResult = DataEntry.cleanString(pAnnot.contentType());
//      }
//    } catch (Exception pExp) {
//      throw new AssertionError("ImportDataFormats.getResourceContentType Error:\n " 
//              + pExp.getMessage());
//    }
//    return sResult;
//  }
  //</editor-fold>  
}
