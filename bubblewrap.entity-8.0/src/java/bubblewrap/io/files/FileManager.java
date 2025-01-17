package bubblewrap.io.files;

//import bubblewrap.http.fileupload.UploadFileContent;
import bubblewrap.http.session.SessionHelper;
import bubblewrap.io.DataEntry;
import bubblewrap.http.session.HttpUtils;
import bubblewrap.io.enums.ContentTypes;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Static Class with File Manager Supports
 * @author kprins
 */
public class FileManager {

  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(FileManager.class.getName());
  /**
   * Placeholder for a reference to the Application's Temp Directory.
   */
  private static File tempDirectory = null;

  //<editor-fold defaultstate="collapsed" desc="Directory Management">
  /**
   * Overload 1: Create a File instance with sPath and call Overload 2. Ignore is sPath
   * if null or and empty string
   * @param sPath String
   * @param bWhenEmpty
   * @return boolean
   */
  public static boolean deleteDir(String sPath, boolean bWhenEmpty) {
    sPath = DataEntry.cleanString(sPath);
    boolean bResult = true;
    if (sPath != null) {
      File pDir = new File(sPath);
      bResult = FileManager.deleteDir(pDir, bWhenEmpty);
    }
    return bResult;
  }

  /**
   * Overload 2: If pDirectory exist, delete the the directory of bWhenEmpty=true and
   * the directory is empty. If the directory exist and its is not empty, it not the
   * delete the directory or its content of bWhenEmpty=ture. However, if (!bWhenEmpty),
   * it will delete all it file and sub directories. Hidden files cannot be deleted and
   * the process will fail if a hidden file is detected.
   * NOTE: No exceptions are thrown and all errors are logged. It will ignore the call
   * if pDirectory=null, or not a directory.
   * Return true is the process was successfully completed or there was nothing to
   * delete is pDirectoyr is not a Directory.
   * @param pDirectory File
   * @param bWhenEmpty boolean
   * @return boolean
   */
  public static boolean deleteDir(File pDirectory, boolean bWhenEmpty) {
    boolean bResult = true;
    try {
      if ((pDirectory != null) && (pDirectory.exists()) && (pDirectory.isDirectory())) {
        if (pDirectory.isHidden()) {
          throw new Exception("Directory[" + pDirectory.getName()
                  + "] is an hidden and  could not be deleted.");
        }

        File[] pContent = pDirectory.listFiles();
        boolean bIsEmpty = ((pContent == null) || (pContent.length == 0));
        if ((!bIsEmpty) && (!bWhenEmpty)) {
          for (File pFile : pContent) {
            if (pFile.isHidden()) {
              throw new Exception("File[" + pFile.getName() + "] is an hidden file and "
                      + "could not be deleted. The process was stopped.");
            } else if (pFile.isDirectory()) {
              if (!FileManager.deleteDir(pFile, bWhenEmpty)) {
                bResult = false;
                break;
              }
            } else {
              if (!pFile.delete()) {
                throw new Exception("Deleting File[" + pFile.getName() + "] failed. "
                        + "The process was stopped.");
              }
            }
          }

          pContent = pDirectory.listFiles();
          bIsEmpty = ((pContent == null) || (pContent.length == 0));
        }

        if (bIsEmpty) {
          if (!pDirectory.delete()) {
            throw new Exception("Deleting Directory[" + pDirectory.getName()
                    + "] failed. The process was stopped.");
          }
        }
      }
    } catch (Exception pExp) {
      bResult = false;
      logger.log(Level.WARNING, "{0}.deleteDir Error:\n {1}",
              new Object[]{FileManager.class, pExp.getMessage()});
    }
    return bResult;
  }

  /**
   * Overload 1: Create a File instance with sPath and call Overload 2. Ignore is sPath
   * if null or and empty string
   * @param sPath String
   * @param bDeleteSubDirs boolean
   * @return boolean
   */
  public static boolean clearDir(String sPath, boolean bDeleteSubDirs) {
    sPath = DataEntry.cleanString(sPath);
    boolean bResult = true;
    if (sPath != null) {
      File pDir = new File(sPath);
      bResult = FileManager.clearDir(pDir, bDeleteSubDirs);
    }
    return bResult;
  }

  /**
   * <p>Overload 2: Delete all non-hidden files from the Directory and all sub-directories
   * if bDeleteSubDirs=true. Return false if deleting of a file or sub-directory failed.
   * Ignored if pDirectory=null, does not exists, or is not a directory</p>
   * <p><b>NOTE:</b> The Directory is not deleted</p>
   * @param pDirectory the Directory to clear
   * @param bDeleteSubDirs true=delete sub-directories to.
   * @return true if deleting all files (and subdirectories if bDeleteSubDirs=true) was 
   * successful.
   */
  public static boolean clearDir(File pDirectory, boolean bDeleteSubDirs) {
    boolean bResult = true;
    try {
      if ((pDirectory != null) && (pDirectory.exists()) && (pDirectory.isDirectory())) {
        if (pDirectory.isHidden()) {
          throw new Exception("Directory[" + pDirectory.getName()
                  + "] is an hidden and  could not be deleted.");
        }

        File[] pContent = pDirectory.listFiles();
        boolean bIsEmpty = ((pContent == null) || (pContent.length == 0));
        if (!bIsEmpty) {
          for (File pFile : pContent) {
            if (pFile.isHidden()) {
              continue;
            } else if (pFile.isDirectory()) {
              if (bDeleteSubDirs) {
                if (!FileManager.deleteDir(pFile, false)) {
                  bResult = false;
                  break;
                }
              }
            } else {
              if (!pFile.delete()) {
                throw new Exception("Deleting File[" + pFile.getName() + "] failed. "
                        + "The process was stopped.");
              }
            }
          }

          pContent = pDirectory.listFiles();
          bIsEmpty = ((pContent == null) || (pContent.length == 0));
        }
      }
    } catch (Exception pExp) {
      bResult = false;
      logger.log(Level.WARNING, "{0}.clearDir Error:\n {1}",
              new Object[]{FileManager.class, pExp.getMessage()});
    }
    return bResult;
  }

  /**
   * Get the Application's Temporary Directory - use the file system to create a 
   * temporary file and  return its parent file. This path is cached for later use.
   * @return Path to this applications Temporary Directory.
   */
  public static File getTempDirectory() {
    if (FileManager.tempDirectory == null) {
      File tempFile = null;
      try {
        tempFile = File.createTempFile("testfile", "tmp");
        FileManager.tempDirectory = tempFile.getParentFile();
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}.getTempDirectory Error:\n {1}",
                new Object[]{"FileManager", pExp.getMessage()});
      } finally {
        if (tempFile != null) {
          tempFile.delete();
        }
      }
    }
    return FileManager.tempDirectory;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="File View/Download Methods">
  /**
   * Download Buffer Size
   */
  public static final int BUFSIZE = 2048;

  /**
   * Forces an immediate file download of the specified file. The Content Type of the
   * File is specified, it will be used. Otherwise it will call the ExternalContext's
   * getMimeType(), and if that return no value, it returns "application/octet-stream".
   * @param inputFile The Download File's full path
   * @param mimeType The Content's MIME type (can be null to accept default)
   */
  public static void downloadFile(File inputFile, String mimeType) throws Exception {
    ServletOutputStream outStream = null;
    try {
      // Use the FacesContext to get the HttpServletResponse to force a download action.
      FacesContext fcaseCtx = FacesContext.getCurrentInstance();
      HttpServletResponse response =
              (HttpServletResponse) fcaseCtx.getExternalContext().getResponse();

      // Get a File object for the download file.
      if (inputFile == null) {
        throw new Exception("The Downlaod FIle is undefined");
      } else if (!inputFile.exists()) {
        throw new Exception("Unable to locate File[" + inputFile.getPath() + "].");
      }
      mimeType = DataEntry.cleanString(mimeType);

      int length = 0;

      // If we can get a MimeType from the file itself, do so.
      mimeType = (mimeType != null) ? mimeType
              : fcaseCtx.getExternalContext().getMimeType(inputFile.getPath());
      mimeType = (mimeType != null) ? mimeType : "application/octet-stream";
      mimeType = "application/octet-stream";
      // If the file extension isn't associated with a file type, then use an octet-stream
      response.setContentType(mimeType);

      // Assign the content length to match the length of the file
      response.setContentLength((int) inputFile.length());

      // Set the header on the response
      response.setHeader("Content-Disposition",
              "attachment; filename=\"" + inputFile.getName() + "\"");
      byte[] pBuffer = new byte[BUFSIZE];

      // Get the output stream for writing the content to.
      outStream = response.getOutputStream();

      // Write the content into the output stream
      try (DataInputStream inStream = 
                                new DataInputStream(new FileInputStream(inputFile))) {
        while ((inStream != null) && ((length = inStream.read(pBuffer)) != -1)) {
          outStream.write(pBuffer, 0, length);
        }
        outStream.flush();
      } finally {
        if (outStream != null) {
          outStream.close();
          outStream = null;
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.downloadFile Error:\n {1}",
              new Object[]{"FileManager", pExp.getMessage()});
    }
  }

  /**
   * Forces an View of the specified file in the browser. The Content Type of the
   * File is specified, it will be used. Otherwise it will call the ExternalContext's
   * getMimeType(), and if that return no value, it returns "application/octet-stream".
   * @param inputFile File
   * @param mimeType String
   */
  public static void viewFile(File inputFile, String mimeType) throws IOException {
    HttpServletResponse response = null;
    try {
      // Use the FacesContext to get the HttpServletResponse to force a download action.
      FacesContext fcaseCtx = FacesContext.getCurrentInstance();
      response = (HttpServletResponse) fcaseCtx.getExternalContext().getResponse();

      // Get a File object for the download file.
      if (inputFile == null) {
        throw new Exception("The Download File is undefined");
      } else if (!inputFile.exists()) {
        throw new Exception("Unable to locate File[" + inputFile.getPath() + "].");
      }
      
      String fileName = inputFile.getName();

      // If we can get a MimeType from the file itself, do so.
      mimeType = (mimeType != null) ? mimeType
              : fcaseCtx.getExternalContext().getMimeType(inputFile.getPath());
      mimeType = (mimeType != null) ? mimeType : "application/octet-stream";

      /* If the file extension isn't associated with a file type, then use an 
       * octet-stream */
      response.setHeader("Content-Disposition", "attachment; filename='" + fileName + "'");
      response.setContentType(mimeType);

      // Assign the content length to match the length of the file
      response.setContentLength((int) inputFile.length());
      // Set the header on the response
      response.setHeader("Content-Location", inputFile.getPath());
      
      byte[] readBuffer = new byte[BUFSIZE];
      // Get the output stream for writing the content to.
      ServletOutputStream outStream = response.getOutputStream();

      // Write the content into the output stream
      int length = 0;
      try (DataInputStream inStream = 
                                  new DataInputStream(new FileInputStream(inputFile))){
        while ((inStream != null) && ((length = inStream.read(readBuffer)) != -1)) {
          outStream.write(readBuffer, 0, length);
        }
        outStream.flush();
      } finally {
        if (outStream != null) {
          outStream.close();
          outStream = null;
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.viewFile Error:\n {1}",
              new Object[]{"FileManager", pExp.getMessage()});
      if (response != null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, pExp.getMessage());
      }
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Resource Paths">
  /**
   * Obtains the file path for the templates folder in the resources folder.
   * @return String
   */
  public static String getTemplatesPath(){
    FacesContext pCtx = FacesContext.getCurrentInstance();
    ExternalContext pExtCtx = pCtx.getExternalContext();
    return pExtCtx.getRealPath("/resources/templates");
  }
  
  /**
   * Obtains the file path for the 'downloads' folder in the resources folder.
   * @return String
   */
  public static String getDownloadsPath(){
    FacesContext pCtx = FacesContext.getCurrentInstance();
    ExternalContext pExtCtx = pCtx.getExternalContext();
    return pExtCtx.getRealPath("/resources/downloads");
  }
  
  /**
   * Expands the relative Path assuming that the Resource[sRelativePath] is located in
   * the ServletContext's resource path. if (sRelativePath=null|""), return the
   * ServletContext's base path. Return the result as a File object.
   * <p><b>NOTE:</b> This method does not verify the resource exist.</p>
   * @param sRelativePath String
   * @return File
   */
  public static File getRealResourcePath(String sRelativePath) {
    return FileManager.getRealResourcePath(sRelativePath, null);
  }
  
  /**
   * Expands the relative Path assuming that the Resource[sRelativePath] is located in
   * the ServletContext's resource path. if (sRelativePath=null|""), return the
   * ServletContext's base path. Return the result as a File object. If pContext=null,
   * set pContext=FacesContext.getCurrentInstance()
   * <p><b>NOTE:</b> This method does not verify the resource exist.</p>
   * @param sRelativePath String
   * @param pContext ServletContext (can be null).
   * @return File
   */
  private static File getRealResourcePath(String sRelativePath, 
                                                              ServletContext pContext) {
    File pResult = null;
    try {
      if (pContext == null) {
        FacesContext pFacesCtx = FacesContext.getCurrentInstance();
        ExternalContext pExtCtx =
                (pFacesCtx == null) ? null : pFacesCtx.getExternalContext();
        pContext = (ServletContext) pExtCtx.getContext();
      }
      
      if (pContext == null) {
        throw new Exception("Unable to access the Session's ServletContext");
      }
      
      sRelativePath = DataEntry.cleanString(sRelativePath);
      sRelativePath = (sRelativePath == null) ? "/" : sRelativePath;
      String sPath = pContext.getRealPath(sRelativePath);
      pResult = new File(sPath);
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getRealResourcePath Error:\n {1}",
              new Object[]{"FileManager", pExp.getMessage()});
    }
    return pResult;
  }
  
  /**
   * Get the Source Path for <tt>realPath</tt> relative to ServletContext.realPath.
   * @param realPath the reap path to the source folder or file
   * @return the relative path or "/" if pRealPath or realPath.path if the path is not
   * relative to the ServletContext.realPath.
   */
  public static String getRelativeResourcePath(File realPath) {
    String result = null;
    try {
      if (realPath == null) {
        result = File.separator;
      } else {
        File basePath = FileManager.getRealResourcePath(null);
        String realAbsPath = realPath.getAbsolutePath();
        String baseAbsPath = basePath.getAbsolutePath();
        if ((realAbsPath != null) && (realAbsPath.startsWith(baseAbsPath))) {
          result = realAbsPath.substring(baseAbsPath.length());
          if (!result.startsWith(File.separator)) {
            result = File.separator + result;
          }
        } else {
          result = (DataEntry.cleanString(realAbsPath) == null)? 
                                                          File.separator : realAbsPath;
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getRelativeSourcePath Error:\n {1}",
              new Object[]{"FileManager", exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Call getRealResourcePath to expand sRelativePath and if it returns a File Path,
   * test whether the Path exist.
   * @param sRelativePath String
   * @return boolean
   */
  public static boolean hasResource(String sRelativePath) {
    boolean bResult = false;
    try {
      File pPath = FileManager.getRealResourcePath(sRelativePath);
      if (pPath != null) {
        bResult = pPath.exists();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.hasResource Error:\n {1}",
              new Object[]{"FileManager", pExp.getMessage()});
    }
    return bResult;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Session Cache Management">
  /**
   * Locate or Create a Session Cache Folder and if it does not exist creates it.
   * Return the File (path) for this folder/directory. Throw an exception if this
   * process fails. The relative path is Path[/cache/<sessionId>]
   * @return File
   * @throws Exception
   */
  public static File getSessionCache() throws Exception {
    File pResult = null;
    try {
      String sSessionId = DataEntry.cleanString(SessionHelper.getSessionId());
      if (sSessionId == null) {
        throw new Exception("Unable to access the SessionId.");
      }
      
      String sCachePath = "/cache/" + sSessionId + "/";
      File pPath = FileManager.getRealResourcePath(sCachePath);
      if (pPath == null) {
        throw new Exception("Initiating the ResourcePath[" + sCachePath + "] failed.");
      } else if (!pPath.exists()) {
        pPath.mkdirs();
      }
      pResult = pPath;
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSessionCache Error:\n {1}",
              new Object[]{"FileManager", pExp.getMessage()});
      throw pExp;
    }
    return pResult;
  }
  
  /**
   * Check if the Session Cache's relative path is Path[/cache/<sessionId>] exists.
   * @return boolean
   */
  public static boolean hasSessionCache() {
    boolean bResult = false;
    try {
      String sSessionId = DataEntry.cleanString(SessionHelper.getSessionId());
      if (sSessionId != null) {
        String sCachePath = "/cache/" + sSessionId + "/";
        File pPath = FileManager.getRealResourcePath(sCachePath);
        if (pPath != null) {
          bResult = pPath.exists();
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSessionCache Error:\n {1}",
              new Object[]{"FileManager", pExp.getMessage()});
    }
    return bResult;
  }
  
  /**
   * If the Session Cache's relative path is Path[/cache/<sessionId>] exists,
   * delete the content and the path
   */
  public static void deleteSessionCache() {
    FileManager.deleteSessionCache(SessionHelper.getSessionId(), null);
  }
  
  /**
   * If the Session Cache's relative path is Path[/cache/<sessionId>] exists,
   * delete the content and the path
   * @param sSessionId String
   * @param pContext FacesContext
   */
  public static void deleteSessionCache(String sSessionId, ServletContext pContext) {
    try {
      sSessionId = DataEntry.cleanString(sSessionId);
      if (sSessionId != null) {
        String sCachePath = "/cache/" + sSessionId + "/";
        File pPath = FileManager.getRealResourcePath(sCachePath, pContext);
        if ((pPath != null) && (pPath.exists())) {
          FileManager.deleteDir(pPath, false);
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.deleteSessionCache Error:\n {1}",
              new Object[]{"FileManager", pExp.getMessage()});
    }
  }
  
  /**
   * Overload 1: Get the full/expanded URL for accessing File[sCacheFile] in the session
   * cache. Call overload 2 with bRelativePath=false.
   * @param sCacheFile the name of the file in the cache
   * @return the resolve path
   * @throws Exception
   */
  public static String getSessionCacheUrl(String sCacheFile) throws Exception {
    return FileManager.getSessionCacheUrl(sCacheFile,false);
  }
  
  /**
   * Overload 2: Get the full/expanded URL for accessing File[sCacheFile] in the session
   * cache. If bRelativePath=true, do not call HttpUtils.buildRequestUrl to add the
   * "contextPath/serveletPath" to the SessionCache's relative path.
   * @param sCacheFile the name of the file in the cache
   * @param bRelativePath If bRelativePath=true, do not call HttpUtils.buildRequestUrl
   * to add the "contextPath/serveletPath" to the SessionCache's relative path.
   * @return the resolve path
   * @throws Exception
   */
  public static String getSessionCacheUrl(String sCacheFile, boolean bRelativePath)
          throws Exception {
    String sResult = null;
    try {
      sCacheFile = DataEntry.cleanString(sCacheFile);
      if (sCacheFile == null) {
        throw new Exception("The Session Cache Filename is unassigned.");
      }
      
      String sSessionId = DataEntry.cleanString(SessionHelper.getSessionId());
      if (sSessionId == null) {
        throw new Exception("Unable to access the SessionId.");
      }
      
      String sCachePath = "/cache/" + sSessionId + "/" + sCacheFile;
      if (bRelativePath) {
        sResult = sCachePath;
      } else {
        sResult = HttpUtils.buildRequestUrl(sCachePath);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSessionCacheUrl Error:\n {1}",
              new Object[]{"FileManager", pExp.getMessage()});
    }
    return sResult;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="File Upload Methods">
//  /**
//   * Called to Transfer the uploaded file content to its target location in pOutPath.
//   * If the fie is uploading in chunks, an "chunks" directory is added to the pOutPath
//   * and an ".XXX" extension is added to the filename (e.g., ".005" for chunkIndex=5).
//   * @param pFileContent the uploaded file content wrapper
//   * @param pOutPath to target location of the uploaded file
//   * @throws Exception
//   */
//  public static void uploadFile(UploadFileContent pFileContent, File pOutPath)
//          throws Exception {
//    try {
//      if ((pFileContent == null) || (pFileContent.getFileSize() == 0)) {
//        return;
//      }
//      
//      if (pOutPath == null) {
//        throw new Exception("The Output Path is not specified.");
//      }
//      
//      String sFileName = pFileContent.getFileName();
//      if (sFileName == null) {
//        throw new Exception("The Upload File's Filename is unknown.");
//      }
//      
//      File pTargetPath = null;
//      if (pFileContent.isInChunks()) {
//        Integer iIdx = pFileContent.getChunkIndex();
//        String sExt = DataEntry.leftPadString(iIdx.toString(), "0", 3);
//        sFileName += "." + sExt;
//        
//        pTargetPath = new File(pOutPath.getAbsoluteFile(),"chunks");
//      } else {
//        pTargetPath = pOutPath.getAbsoluteFile();
//      }
//      
//      if (!pTargetPath.exists()) {
//        pTargetPath.mkdirs();
//      }
//      
//      File sFilePath = new File(pTargetPath,sFileName);
//      
//      /* Stream the UplaodFile data to the output file */
//      FileOutputStream pFileStream = null;
//      try {
//        pFileStream = new FileOutputStream(sFilePath);
//        if (pFileContent.writeTo(pFileStream) >= 0) {
//          pFileStream.flush();
//        }
//      } finally {
//        if (pFileStream != null) {
//          pFileStream.close();
//        }
//      }
//    } catch (Exception pExp) {
//      logger.log(Level.WARNING, "{0}.uploadFile Error:\n {1}",
//              new Object[]{"FileManager", pExp.getMessage()});
//      throw pExp;
//    }
//  }
//  
//  /**
//   * <p>Called to merge the file chunks into the final upload file during a chunked file
//   * upload process. This call will be ignored if (!pFileContent.isInChunks) or if
//   * (pFileContent.action!=EndChunks).</p>
//   * <p>It look for chunks in Path[pOutPath/chunks] and create the merged file in
//   * pOutPath. It will throw and exception and abort the process if chunk in the
//   * range[1..pFileContent.chunkIndex] is missing.</p>
//   * <b>NOTES:</b> <ol>The chunks are deleted after successfully merging all chunks.
//   * </ol>
//   * @param pFileContent the uploaded file content wrapper
//   * @param pOutPath to target location of the uploaded file
//   * @throws Exception
//   */
//  public static void mergeFileChunks(UploadFileContent pFileContent, File pOutPath)
//          throws Exception {
//    try {
//      if (pFileContent == null)  {
//        throw new Exception("The Upload File Content if undefined.");
//      }
//      
//      /* Skip the process if !isChunks or (action!=EndChunks) */
//      if ((!pFileContent.isInChunks())
//              || (pFileContent.getAction() != FileUploadActions.EndChunks)) {
//        return;
//      }
//      
//      int iNumChunks = pFileContent.getChunkIndex();
//      if (iNumChunks <= 0) {
//        throw new Exception("The Number of Chunks less or equal to zero.");
//      }
//      
//      if (pOutPath == null) {
//        throw new Exception("The Output Path is not specified.");
//      }
//      
//      String sFileName = pFileContent.getFileName();
//      if (sFileName == null) {
//        throw new Exception("The Upload File's Filename is unknown.");
//      }
//      
//      if (!pOutPath.exists()) {
//        pOutPath.mkdirs();
//      }
//      
//      File pOutFilePath = new File(pOutPath.getAbsoluteFile(),sFileName);
//      
//      File pChunkPath = new File(pOutPath.getAbsoluteFile(), "chunks");
//      if (!pChunkPath.exists()) {
//        throw new Exception("The Chunks Path[" + pChunkPath.getPath()
//                + "] is no longer accessible.");
//      }
//      
//      
//      /* Stream the UplaodFile data to the output file */
//      long lFileSize = 0;
//      FileOutputStream pOutStream = null;
//      try {
//        pOutStream = new FileOutputStream(pOutFilePath.getAbsoluteFile());
//        
//        for (int iIdx = 1; iIdx <= iNumChunks; iIdx++) {
//          String sExt = DataEntry.leftPadString(Integer.toString(iIdx), "0", 3);
//          String sChunkName = sFileName + "." + sExt;
//          
//          File pChunkFilePath = new File(pChunkPath,sChunkName);
//          if (!pChunkFilePath.exists()) {
//            throw new Exception("Unable to locate ChunkFile[" + sChunkName
//                    + "] in path[" + pChunkPath.getPath() + "].");
//          }
//          
//          FileInputStream pInStream = null;
//          try {
//            pInStream = new FileInputStream(pChunkFilePath);
//            if (pInStream == null) {
//              throw new Exception("Unable to create the Soruce File's input stream.");
//            }
//            
//            long pOutSize = 0;
//            byte[] pBuffer = new byte[FileManager.BUFSIZE];
//            int pBytesRead = pInStream.read(pBuffer);
//            while (pBytesRead != -1) {
//              pOutStream.write(pBuffer, 0, pBytesRead);
//              pOutSize += pBytesRead;
//              pBytesRead = pInStream.read(pBuffer);
//            }
//            
//            lFileSize += pOutSize;
//          } finally {
//            if (pInStream != null) {
//              pInStream.close();
//            }
//          }
//        }
//      } finally {
//        if (pOutStream != null) {
//          pOutStream.close();
//        }
//      }
//      
//      pFileContent.setFileSize(lFileSize);
//    } catch (Exception pExp) {
//      logger.log(Level.WARNING, "{0}.mergeFileChunks Error:\n {1}",
//              new Object[]{"FileManager", pExp.getMessage()});
//      throw pExp;
//    }
//    
//    /**
//     * When the successful completion of the merge - delete all chunks and the chunks
//     * sub-directory
//     */
//    FileManager.deleteFileChunks(pOutPath);
//  }
//  
//  /**
//   * Check if pOutPath/chunks exists and if true, delete the Directory and all its
//   * contents.
//   * @param pOutPath to File Upload target output path
//   * @throws Exception
//   */
//  public static void deleteFileChunks(File pOutPath) throws Exception {
//    try {
//      if (pOutPath == null) {
//        return;
//      }
//      
//      File pTargetPath = new File(pOutPath.getAbsoluteFile(),"chunks");
//      if (pTargetPath.exists()) {
//        FileManager.deleteDir(pTargetPath, false);
//      }
//    } catch (Exception pExp) {
//      logger.log(Level.WARNING, "{0}.deleteFileChunks Error:\n {1}",
//              new Object[]{"FileManager", pExp.getMessage()});
//      throw pExp;
//    }
//  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="File Names Methods">
  /**
   * Call to convert a file name (not a path - only the filename and extension) to a
   * UniX Compliant file name in lower case. It replace all white spaces, special
   * characters, or brackets (i.e., is not 0-9,a-Z,"." or "-"), with "_"
   * @param sFileName String
   * @return String
   */
  public static String toUnixFilename(String sFileName) {
    String sResult = null;
    try {
      sFileName = DataEntry.cleanString(sFileName);
      if (sFileName != null) {
        sResult = sFileName.replaceAll("[^a-zA-Z0-9\\.-_:]+", "_");
        sResult = sResult.toLowerCase();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.toUnixFilename Error:\n {1}",
              new Object[]{"toUnixFilename.toUnixFilename", pExp.getMessage()});
    }
    return sResult;
  }

  /**
   * Get the Extension to sFileName. If sFilename does not contain a "." or begins or
   * ends with a ".", return null. Otherwise, return the string following the last "."
   * in sFileName
   * @param fileName the filename to evaluate
   * @return the extension without the leading ".", or null if fileName = null|"" or
   * has no assigned extension.
   */
  public static String getFileExtension(String fileName) {
    String result = null;
    fileName = DataEntry.cleanString(fileName);
    int iPos = (fileName == null) ? -1 : fileName.lastIndexOf(".");
    if ((iPos > 0) && (iPos < fileName.length() - 1)) {
      result = fileName.substring(iPos + 1);
    }

    return result;
  }

  /**
   * Get the File contentType based in sFileName's extension. It the FaceContext's
   * ExternalContext is accessible, it calls ExternalContext.getMimeType and return the
   * result. Otherwise, its calls getFileExtension to extract the extension from 
   * sFilename and is the extension is found, callContentTypes.getByExtension to get 
   * the ContentType. 
   * Return null if the extension cannot be extracted or Content Type match is found for
   * the file's extension.
   * @param sFilename String
   * @return String
   */
  public static String getContentType(String sFilename) {
    String sResult = null;
    try {
      sFilename = DataEntry.cleanString(sFilename);
      if (sFilename != null) {
        FacesContext pCtx = FacesContext.getCurrentInstance();
        ExternalContext pExtCtx = (pCtx == null) ? null : pCtx.getExternalContext();
        if (pExtCtx != null) {
          sResult = (pExtCtx.getMimeType(sFilename));
        } else {
          String sExt = FileManager.getFileExtension(sFilename);
          if (sExt != null) {
            sResult = ContentTypes.getByExtension(sExt);
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "FileManager.getContentType Error:\n {0}",
              pExp.getMessage());
    }
    return sResult;
  }

  /**
   * Get the Extension to sFileName. If sFilename does not contain a "." or begins or
   * ends with a ".", return null. Otherwise, return the string following the last "."
   * in sFileName
   * @param sFilename String
   * @return String
   */
  public static String changeFileExtension(String sFilename, String sNewExt) {
    sFilename = DataEntry.cleanString(sFilename);
    sNewExt = DataEntry.cleanString(sNewExt);

    if (sFilename != null) {
      String sExt = FileManager.getFileExtension(sFilename);
      if (sExt != null) {
        int iPos = sFilename.lastIndexOf(sExt);
        sFilename = sFilename.substring(0, iPos);
      }

      if (sNewExt != null) {
        if (sNewExt.startsWith(".")) {
          sNewExt = sNewExt.replaceFirst(".", "");
        }

        sFilename += sNewExt;
      }
    }
    return sFilename;
  }

  /**
   * Get the File's BaseName (i.e. without the path and extension). Return null if
   * sFilePath is undefined or not the path to a File (e.g. a Folder)
   * @param sFilePath String
   * @return String
   */
  public static String getBaseFileName(String sFilePath) {
    String sResult = null;

    sFilePath = DataEntry.cleanString(sFilePath);
    File pFile = (sFilePath == null) ? null : new File(sFilePath);
    if ((pFile != null) && (!pFile.isDirectory())) {
      String sFileName = pFile.getName();
      String sExt = FileManager.getFileExtension(sFileName);
      if (sExt == null) {
        sResult = sFileName;
      } else {
        sExt = "." + sExt;
        int iPos = sFileName.lastIndexOf(sExt);
        if (iPos > 0) {
          sResult = sFileName.substring(0, iPos);
        } else {
          sResult = sFileName;
        }
      }
    }

    return sResult;
  }

  /**
   * Called to clean the path and set the Path Separator as sSeparator. If the latter
   * is null or an empty string, the File.pathSeparator is used instead. 
   * @param inPath String
   * @param newSeperator String
   * @return String
   */
  public static String cleanPath(String inPath, String newSeperator) {
    String result = null;
    try {
      inPath = DataEntry.cleanString(inPath);
      File file = (inPath == null) ? null : new File(inPath);
      inPath = (file == null) ? null : file.getPath();

      if (inPath != null) {
        newSeperator = DataEntry.cleanString(newSeperator);
        if ((newSeperator != null) && (!newSeperator.equals(File.separator))) {
          result = inPath.replace(File.separator, newSeperator);
        } else {
          result = inPath;
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "FileManager.cleanPath Error:\n {0}",
              pExp.getMessage());
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="File Info">
  /**
   * Converts a File Size lSzie to a string
   * @param lSize the file size in bytes
   * @return "-" if lSize =null|negative, X bytes if smaller the 1024 or X.XX KB, 
   * X.XX MB, X.XX GB, or X.XX TB, depending on the file size
   */
  public static String getFileSize(Long lSize) {
    String sResult = "-";
    if ((lSize != null) && (lSize > 0)) {
      if (lSize < 1024) {
        sResult = lSize.toString() + " bytes";
      } else {
        Double bSize = lSize.doubleValue();
        bSize = bSize/1024;
        if (bSize < 1000) {
          sResult = String.format("%.2f KB",bSize);
        } else {
          bSize = bSize/1024;
          if (bSize < 1000) {
            sResult = String.format("%.2f MB",bSize);
          } else {
            bSize = bSize/1024;
            if (bSize < 1000) {
              sResult = String.format("%.2f GB",bSize);
            } else {
              bSize = bSize/1024;
              sResult = String.format("%.2f TB",bSize);
            }
          }
        }
      }
    }
    return sResult;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="File Management Methods">
  /**
   * Copy pSrcFile to pTrgPath where sSrcFile represents a file. If pTrgPath isDir,
   * it will append pSrcFile's Filename to pTrgPath. If successful, it will return
   * a non-negative number. Otherwise it will return -1. It throws an exception if an
   * error occur. If the pTrgPath does not exist, it will create the Path.
   * @param pSrcFile File
   * @param pTrgPath File
   * @return long
   * @throws Exception
   */
  public static long copyFile(File pSrcFile, File pTrgPath) throws Exception {
    long lResult = -1;
    try {
      if ((pSrcFile == null) || (!pSrcFile.isFile())) {
        return 0;
      }
      
      /* Get the Target's output path for the file and create the absolute file
       * path */
      String sFileName = pSrcFile.getName();
      if (sFileName == null) {
        throw new Exception("The Source File's fileName is not accessible "
                + "or defined.");
      }
      
      String sOutPath = null;
      if (pTrgPath.isDirectory()) {
        if (!pTrgPath.exists()) {
          pTrgPath.mkdirs();
        }
        File pOutFile = new File(pTrgPath, sFileName);
        sOutPath = pOutFile.getAbsolutePath();
      } else {
        File pDirPath = pTrgPath.getParentFile();
        if ((pDirPath != null) && (!pDirPath.exists())) {
          pDirPath.mkdirs();
        }
        sOutPath = pTrgPath.getAbsolutePath();
      }
      
      if (sOutPath == null) {
        throw new Exception("Unable to initiate the Target Output Path.");
      }
      
      FileInputStream pInStream = null;
      try {
        pInStream = new FileInputStream(pSrcFile);
        if (pInStream == null) {
          throw new Exception("Unable to create the Soruce File's input stream.");
        }
        
        /* Stream the UplaodFile data to the output file */
        FileOutputStream pOutStream = null;
        try {
          pOutStream = new FileOutputStream(sOutPath);
          
          long pOutSize = 0;
          byte[] pBuffer = new byte[FileManager.BUFSIZE];
          int pBytesRead = pInStream.read(pBuffer);
          while (pBytesRead != -1) {
            pOutStream.write(pBuffer, 0, pBytesRead);
            pOutSize += pBytesRead;
            pBytesRead = pInStream.read(pBuffer);
          }
          lResult = pOutSize;
        } finally {
          if (pOutStream != null) {
            pOutStream.close();
          }
        }
      } finally {
        if (pInStream != null) {
          pInStream.close();
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.copyFile Error:\n {1}",
              new Object[]{"FileManager", pExp.getMessage()});
      throw pExp;
    }
    return lResult;
  }
  
  /**
   * <p>Called to stream to content an InputStream to a target file. Ignored if either
   * the InputStream or the Target File is undefined. If the pTrgFile's Parent Path
   * does not exist, it will create a sub-directories in the path.</p>
   * <p><b>NOTE:</b> This method does not delete any existing files before this process
   * starts. Thus, if pInStream=null or empty, it the existing file will remain intact.
   * </p>
   * @param inStream the InputStream containing the file's content
   * @param trgFile the target file
   * @return the number of bytes streamed to the file. It return -1 if the process 
   * failed.
   * @throws IOException if an error occur. 
   */
  public static long writeToFile(InputStream inStream, File trgFile) 
                                                                  throws IOException {
    long result = -1;
    try {
      if (inStream == null) {
        throw new Exception("The Input Stream is undefined.");
      }
      
      if (trgFile == null) {
        throw new IOException("The target output file is not specified.");
      } else if (trgFile.isDirectory()) {
        throw new IOException("The specified Target File is a Directory.");
      }
        
      File dirPath = trgFile.getParentFile();
      if ((dirPath != null) && (!dirPath.exists())) {
        dirPath.mkdirs();
      }

      try (FileOutputStream outStream = new FileOutputStream(trgFile)) {          
        long outSize = 0;
        byte[] buffer = new byte[FileManager.BUFSIZE];
        int bytesRead = inStream.read(buffer);
        while (bytesRead != -1) {
          outStream.write(buffer, 0, bytesRead);
          outSize += bytesRead;
          bytesRead = inStream.read(buffer);
        }
        result = outSize;
      }      
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.writeToFile Error:\n {1}",
              new Object[]{"FileManager", pExp.getMessage()});
      throw new IOException("FileManager.writeToFile Error:\n\t" + pExp.getMessage());
    }
    return result;
  }
  //</editor-fold>
}
