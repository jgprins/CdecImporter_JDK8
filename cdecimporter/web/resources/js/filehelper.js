/************************************************************************************
* ScriptName: filehelper.js
* Date : 10/1/2013               						
* Version : 1.0        									    
* Author J.G. "Koos" Prins, D.Eng, PE
* CopyRights: GEI Consultants (2013)
* EMail: kprins@geiconsultants.com 	  	
**************************************************************************************
* Modifications on this code is not recommended. Suggestions are welcome
**************************************************************************************
**************************************************************************************
* The objects and methods is dependent of the gloabls.js developed by the same author.
**************************************************************************************/
/*************************************************************************************
 * Class[FileUploadAjax:AppObject] (requires globals.js)
 *************************************************************************************/
FileUploader.baseClass = AppObject;
FileUploader.className = "FileUploader";
function FileUploader() {
  this.inheritsFrom = FileUploader.baseClass;
  this.inheritsFrom();
  this.setClassReferences(FileUploader, FileUploader.baseClass);
  
  /**
   * Default= null
   * @type FileUpload */
  var mpFileElement = null;
  /**
   * Default= null
   * @type Element */
  var mpOutputElement = null;
  
  this.openFile = function(fileName) {
    
  }
  
}

FileUploader.onChange = function(fileElementId, outputElementId) {
  var result = false;
  try {
    if (window.File)
    /**
     * @type FileUpload
     */
    var fileNameElem = null;
    if ((fileElementId !== null) && (allTrim(fileElementId) !== "")) {
      fileElementId = allTrim(fileElementId);
      fileNameElem = window.document.getElementById(fileElementId);
    }
    
    if (fileNameElem === null) {
      throw new Error("Unable to Locate FileName Element[" + fileElementId + "]")
    }
    
    /**
     * @type String
     */
    var fileName = allTrim(fileNameElem.value);
    if ((fileName === null) || (fileName.length === 0)) {
      throw new Error("Please select a file tyo uplaod and try again.")
    }
    
    if (!window.FormData){}
    
  } catch (err) {
    alert("FileUploadAjax.onClick Error:" + err.Message);
  }
  return result;
}
