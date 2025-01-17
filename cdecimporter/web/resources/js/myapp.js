//************************************************************************************
//ScriptName: myapp.js
//Date : 06/10/14               						
//Version : 2.0        									    
//Author Dr J.G. "Koos" Prins		  
//CopyRights: GEI Consultants (2014)
//EMail: kprins@geiconsultants.com 	  	
//************************************************************************************
// Modifications on this code is not recommended
// Suggestions are welcome
//************************************************************************************

var jsfile = "myapp.js";

//<editor-fold defaultstate="collapsed" desc="myapp propotype defintion">
/**
 * The main propotype name space for the MyApp JavaScript Library
 * @type myapp
 */
var myapp = {
  /**
   * The myapp.js versions
   */
  version: "1.0.0",
  /**
   * Placeholder for the Application's Host Url - to be assigned on page startup using
   * the setHostUrl method.
   * @type String
   */
  hostUrl: "",
  /**
   * A Flag set during debugging
   * @type Boolean
   */
  doDebug: false,
  /**
   * Placeholder for the Application's Host Page's pageId
   * @type String
   */
  pageId: "",
  /**
   * Placeholder for the WebService Session ID - initiated if !this.hasPageId to
   * est
   * @type String
   */
  sessionId: "",
  
  /**
   * Set for the Application's Host Url - to be called on page startup to initiate 
   * myapp.hostUrl
   * @param {String} hostUrl the web application hostUrl
   * @param {String} pageId  the Host Page's pageId
   * @returns {undefined}
   */
  setHost: function(hostUrl, pageId) {
    this.hostUrl = myapp.strings.allTrim(hostUrl);
    this.pageId = myapp.strings.allTrim(pageId);
  },
  
  /**
   * Check whether the myapp.hostUrl is assigned.
   */
  hasHostUrl: function() {
    return ((this.hostUrl !== null) && (this.hostUrl.length > 0));
  },
  
  /**
   * Check whether the myapp.pageId is assigned.
   */
  hasPageId: function() {
    return ((this.pageId !== null) && (this.pageId.length > 0));
  },
  
  /**
   * 
   * @param {String} action
   * @param {ParameterMap} params
   * @returns {String}
   */
  buildUrl: function(action, params) {
    /**
     * @type String
     */
    var result = this.hostUrl;
    if (this.hasHostUrl()) {      
      action = myapp.strings.allTrim(action);
      if (action.length === 0) {
        throw new Error("The Url Target cannot be unassigned.");
      }      
      if (result.slice(-1) !== "/") {
        result += "/";
      }
      result += "myajax";
      //      if (action.slice(0,1) != "/") {
      //        result += "/"
      //      }
      result += "?action=" +action;
      
      /** Add the Paramaters to the Url **/
      result = myapp.appendUrlParams(result, params);
    }
    return result;
  },
  
  /**
   * Call to append a set of parameters to the baseUrl. Ignored if param = null|empty
   * @param {String} baseUrl the url to append to
   * @param {ParameterMap} params the set of parameters.
   * @returns {String} the appended Url or the baseUrl is param = null|empty.
   */
  appendUrlParams: function(baseUrl, params) {
    /**
     * @type String
     */
    var result = baseUrl;
    
    /** Add the Paramaters to the Url **/
    if ((params !== null) && (params !==  undefined) && (!params.isEmpty())) {
      var qryStr = params.getAsQueryString();
      if ((qryStr !== null) && (qryStr.length > 0)) {
        if (result.indexOf("?") < 0) {
          result += "?";
        } else if (result.slice(-1) !== "?") {
          result += "&";
        }
        result += qryStr;
      }
    }
    return result;
  },
  
  /**
   * Create an Valid ObjectID by replacing any ":; " with "_" and adding the prefix and/or
  * the suffix to the eElemID
   * @param {String} elemId - an associated element's Id (must be defined)
   * @param {type} namePrefix - an optional prefix to the ObjectId
   * @param {type} nameSuffix - an optional suffix to the ObjectId
   * @returns String
   */
  makeObjectID: function(elemId,namePrefix,nameSuffix) {
    var result = null;
    result = myapp.strings.allTrim(elemId);
    if (result.length === 0) {
      throw new Error("ElementID is undefined");
    }
    
    result = result.replace(/:/g,"_");
    result = result.replace(/;/g,"_");
    result = result.replace(/ /g,"_");
    if (result.length === 0) {
      result = "XXX";
    }

    namePrefix = myapp.strings.allTrim(namePrefix);
    if (namePrefix.length > 0) {
      result = namePrefix + ((result.charAt(0) === "_")? "": "_") + result;
    }

    nameSuffix = myapp.strings.allTrim(nameSuffix);
    if (nameSuffix.length > 0) {
      result += ((result.charAt(result.length-1) === "_")? "" : "_");
      result += nameSuffix;
    }
    return result;
  },
  
  /**
   * Set the doDebug flag. If set debug messages will be displayed, else teh messages
   * will be ignored.
   * @param {Boolean} doDebug
   * @returns {undefined}
   */
  setDoDebug: function(doDebug) {
    this.doDebug = ((doDebug !== null) && (doDebug !== undefined) && (doDebug));
  },
  
  /**
   * Called to show the debug Window with an error message
   * @param {type} message
   * @returns {undefined}
   */
  showDebug: function(message) {
    var dbgComp = document.getElementById("myApp_DebugMsg");
    if ((dbgComp === null) && (this.doDebug)) {
      dbgComp = document.createElement("div");
      dbgComp.setAttribute("id", "myApp_DebugMsg");
      dbgComp.style.border = "#2574B7 1px solid";
      dbgComp.style.font = "12pt/14pt sans-serif";
      var br = document.createElement("div");
      document.getElementsByTagName("body")[0].appendChild(br);
      br.innerHTML = '<br/><br/><br/>';
      document.getElementsByTagName("body")[0].appendChild(dbgComp);
      var tab = 'width: 20px; border-right: #2574B7 1px solid; '
              + 'border-top: #2574B7 1px solid; border-left: #2574B7 1px solid; '
              + 'border-bottom: #2574B7 1px solid; color: #000000; text-align: center;';
      var addActionStr = '<div style="' + tab 
              + '"><a style="text-decoration: none" href="javascript:myapp.closeDebug()'
              + '"><span style="color: red">X</span></a></div>';        
      dbgComp.innerHTML = '<table><tr><td><span style="color: blue">MyApp Debug Window'
              + '</span></td><td>' + addActionStr + '</td></tr></table><br/>';
    }
    if (dbgComp !== null) {
      var s = dbgComp.innerHTML;
      var now = new Date();
      var dateStr = now.getHours() + ':' + now.getMinutes() + ':' + now.getSeconds();
      dbgComp.innerHTML = s + '<span style="color: red">rest debug(' + dateStr 
              + '): </span>' + message + "<br/>";
    }
  },
  
  /**
   * The action to close/hide the Debug Window (clicking on the "X".
   * @returns {undefined}
   */
  closeDebug: function() {
    var dbgComp = document.getElementById("myApp_DebugMsg");
    if (dbgComp !== null) {
      dbgComp.style.display = "none";
      dbgComp.innerHTML = '';
    }
  }
};
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="AjaxRequest Class">
/**
 * A XMLHttpRequest wrapper object that are initiated from myapp.ajax and is added to
 * the myapp.ajax.Queue. 
 * @param {String} method the request Method (GET, POST, PUT, DELETE)
 * @param {String} url the Request Url (with query parameters as  applicable)
 * @param {String} mimeType the request's Mime Type (e.g., "text/plain")
 * @param {Function} onStatusChanged callback function that takes the calling AjaxRequest
 * as a prameters (e.g., myCallBack = function(ajaxReq)).
 * @returns {myAjaxRequest}
 */
function AjaxRequest(method, url, mimeType, onStatusChanged) { 
  
  /**
   * @type String - The AjaxRequest unique ID = "AjaxRequest" + (AjaxRequest.Count++)
   */
  this.ajaxId = "AjaxRequest" + AjaxRequest.Count++;;  
  eval(this.ajaxId+"=this");
  
  /**
   * The callback function to notify listner of readyState changes when running a async 
   * mode
   * @type @exp;AjaxRequest@pro;onStatusChanged|Function
   */
  var statusChangedDelegate = ((onStatusChanged !== null) && 
          (onStatusChanged !== undefined) && (typeof onStatusChanged === "function"))?
  onStatusChanged: null;  
  /**
   * @type XMLHttpRequest - the inetrnal reference to the XMLHttpRequest used by the
   * AjaxRequest.
   */
  var xmlHttpReq = null;  
  /**
   * @type Object - the internal reference to the POST|PUT Content
   */
  var reqContent = null;
  /**
   * @type Integer - the internal reference to the POST|PUT Content length 
   * (-1 is unlimited).
   */
  var reqContentLen = null;
  /**
   * @type Boolean - Flag set for the duration the this.sendRequest processing.
   */
  this.isProcessing = false;
  /**
   * @type Boolean -set be the myapp.ajax.queue when when it called this.sendRequest.
   */
  this.isQueued = false;
  
  //<editor-fold defaultstate="collapsed" desc="AjaxRequest.data">
  this.data = {
    context: {}
  };
  this.data.context.sourceId = null;
  this.data.context.pageId = myapp.pageId;
  
  /**
   * @type String the Owner AjaxRequest's unique ID
   */
  this.data.ajaxId = this.ajaxId;
  /**
   * @type String the Request Method ("GET", "POST", etc)
   */
  this.data.method = myapp.strings.allTrim(method);
  /**
   * @type String the Request URL
   */
  this.data.url = myapp.strings.allTrim(url);
  /**
   * @type String the Request Mime Type (e.g. "text/plain")
   */
  this.data.mimeType = myapp.strings.allTrim(mimeType);
  /**
   * @type ParameterMap the request Arguments
   */
  this.data.args = null;
  /**
   * @type Integer the current XMLHtmmRequest's readyState
   */
  this.data.readyState = 0;
  /**
   * @type Integer the HttpResponse Status (default = 102 | Processing). if successful,
   * it will be in the Range[200..299]. 
   * See http://en.wikipedia.org/wiki/List_of_HTTP_status_codes#3xx_Redirection
   */
  this.data.status = 102;
  /**
   * @type String the current XMLHttpRequest's response type (e.g., "text", "document", 
   * etc.). 
   */
  this.data.responseType = "";
  /**
   * @type XMLHttpRequest - only accessible when request has returned successfully. Use
   * this reference t0 the XMLHttpRequest to retrieve the required reponse.
   */
  this.data.request = "";
  /**
   * @type String the current XMLHtmmRequest's responseText when an error occurred.
   */
  this.data.errorMsg = null;
  /**
   * @type Boolean true if thiserrorMsg != null
   */
  this.data.hasError = function() { return (this.errorMsg !== null);};
  /**
   * @type Boolean true if this is a asyc request (i.e., the statusChangedDelegate != 
   * null) - false if a synce request.
   */
  this.data.async = (statusChangedDelegate !== null);
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructor error checking">
  if (this.data.method === null) {
    throw new Error("The Request's Method is undefined.")
  }
  if (this.data.url === null) {
    throw new Error("The Request's URL is undefined.")
  }
  if (this.data.mimeType === null) {
    throw new Error("The Request's Mime Type is undefined.")
  }
  //</editor-fold>
  
  /**
   * Internally Called to get the applicable XMLHttpRequest object
   * @returns {XMLHttpRequest|Object}
   */
  var getHttpRequest = function() {
    var result = null;
    var methods = [
      function() {
        return new XMLHttpRequest();
      },
      function() {
        return new ActiveXObject('Msxml2.XMLHTTP');
      },
      function() {
        return new ActiveXObject('Microsoft.XMLHTTP');
      }
    ];
    for (var i = 0, len = methods.length; i < len; i++) {
      try {
        result = methods[i]();
      } catch(err) {
        continue;
      }
    }
    if (result === null) {
      throw new Error('Could not create an XMLHttp Request object.');
    }    
    return result;
  };
  
  /**
   * Call internally to initiate and open the XMLHttpRequest - depending on the input 
   * parameters. It also set the applicable request headers as follows: Method=POST|PUT: 
   * {Content-Type = mimeType, Content-Length = contentLen, Connection=close}; 
   * Method=GET: {Accept = mimeType}.
   * It calls myapp.appendUrlParams to append the pageid=myapp.pageId, ajaxId=this.Id,
   * and timestamp=date.time to this.data.url.
   * @param {AjaxRequest} ajaxReq = the caller AjaxRequest
   */
  var open = function(ajaxReq) {
    var err;
    var err1;
    try {
      xmlHttpReq = getHttpRequest();
      if (xmlHttpReq === null) {
        throw new Error("")
      }
      //add timestamp to make url unique in case of IE7
      
      var timestamp = new Date().getTime();
      var axjaxId = ajaxReq.ajaxId;
      var params = ajaxReq.data.args; 
      if ((params === null) || (params === undefined)) {
        params = new ParameterMap();
      }
      
      if (myapp.hasPageId()) {
        params.put("pageid", myapp.pageId);
      }
      params.put("ajaxid", axjaxId);
      params.put("timestamp", timestamp.toString());
      
      var reqUrl = ajaxReq.data.url;
      if (ajaxReq.data.method === "GET") {
        reqUrl = myapp.appendUrlParams(ajaxReq.data.url, params);
      } else {
        var qryStr = params.getAsQueryString();
        ajaxReq.setContent(qryStr, qryStr.length);
      }
      
      /* Borrowsed from rjsSupport - no idea why it is needed */
      try {
        netscape.security.PrivilegeManager.enablePrivilege("UniversalBrowserRead");
      } catch (err1) {
        //this.debug("Permission UniversalBrowserRead denied.");
      }
      
      try {
        xmlHttpReq.open(ajaxReq.data.method, reqUrl, ajaxReq.data.async);
      } catch (err1) {
        throw new Error ('XMLHttpRequest.open failed for Url[' + url 
                + ']: Error: ' + err1.name + '; message: ' + err1.message);
      }
      
      /**
       * Common header send for all requests
       */
      xmlHttpReq.setRequestHeader("Faces-Request", myapp.ajax.facesRequest);
      /**
       * Add method specific Request Headers
       */
      if (ajaxReq.data.method === 'GET') {
        //this.debug("setting GET accept: "+mimeType);
        xmlHttpReq.setRequestHeader('Accept', ajaxReq.data.mimeType);
      } else if ((ajaxReq.data.method === 'POST') || (ajaxReq.data.method === 'PUT')) {
        var contentLen = (reqContent === null)? 0: reqContentLen;
        //this.debug("setting content-type: "+mimeType);
        //Send the proper header information along with the request
        xmlHttpReq.setRequestHeader("Content-Type", ajaxReq.data.mimeType);
        xmlHttpReq.setRequestHeader("Content-Length", contentLen);
        xmlHttpReq.setRequestHeader("Connection", "close");
      }
      
      //For cache control on IE7
      xmlHttpReq.setRequestHeader("Cache-Control", "no-cache");
      xmlHttpReq.setRequestHeader("Pragma", "no-cache");
      xmlHttpReq.setRequestHeader("Expires", "-1");
      
      ajaxReq.data.readyState = xmlHttpReq.readyState;
      if (ajaxReq.data.async) {
        xmlHttpReq.onreadystatechange = 
                new Function(ajaxReq.ajaxId + ".onReadyStateChange();");
        
        /** call the callback function with any change in readyState **/
        if (statusChangedDelegate !== null) {
          statusChangedDelegate(ajaxReq.data);
        }
      }
    } catch (err) {
      myapp.showDebug('AjaxRequest.open Error: ' + err.message);
    }
  };
  
  /**
   * Check if the XMLHttpRequest has been initaited and has been opened but is not yet 
   * send.
   * @returns {Boolean} if (xmlHttpReq !== null) && (xmlHttpReq.status == 1)
   */
  var isOpen = function() {
    return ((xmlHttpReq !== null) && (xmlHttpReq.readyState === 1));
  };
  
  /**
   * Check if the request Status is a success status - inrane [200..299]
   * @param {Number} status The success status
   */
  var isSuccessStatus = function(status) {
    var result = false;
    if ((status !== null) && (status !== undefined)) {
      result = ((status >= 200) && (status < 300));
    }
    return result;
  }; 
  
  /**
   * In teh case of Request Method POST, PUT, etc. that requires as a cotent in the 
   * request, this method must be used to assign the content and its associated content 
   * length. 
   * @param {Object} content the content to send
   * @param {long} contentLen (set to -1 to make it unlimited)
   * @return undefined
   */
  this.setContent = function(content, contentLen) {
    reqContent = null;
    reqContentLen = 0;
    if ((content === null) || (content === undefined)) {
      return;
    }
    reqContent = content;
    reqContentLen = ((contentLen === null) || (contentLen === undefined) || 
            (contentLen === 0))? -1: contentLen;
  };
  
  /**
   * Calleed to open and send the XMLHttpRequest based on its set proeprties. During this
   * process, the isProcessing state will be set to true. 
   * <p>If running in async mode, control will be return to the sender after the was 
   * completed - not when results are return. An onStatusChanged eventlistener delegate
   * function should be assigned to listen and response to the request cycles execution.
   * <p>If an error occurred during the open and send process and this AjaxRequest was 
   * added to the 
   * @returns {Boolean} true if the porocess is successfully launched.
   */
  this.sendRequest = function() {
    var result = false;
    var err;
    try {
      this.isProcessing = true;
      open(this);
      if (!isOpen()) {
        throw new Error("The XMLHttpRequest is not initiated or failed to opened.");
      }
      if(this.data.method === "GET") {
        xmlHttpReq.send(null);
      } else if(this.data.method === "POST") {
        xmlHttpReq.send(reqContent);
      } else if(this.data.method === "PUT") {
        xmlHttpReq.send(reqContent);
      } else if(this.data.method === "DELETE") {
        xmlHttpReq.send(reqContent);
      } else {
        throw new Error("Method is not supported.")
      }
      
      if (!this.data.async) {
        this.data.status = xmlHttpReq.status;
        this.data.readyState = xmlHttpReq.readyState;
        this.data.responseType = xmlHttpReq.responseType;
        if (!isSuccessStatus(this.data.status)) {
          var errMsg = null;
          var respText = xmlHttpReq.responseText;
          if ((respText !== null) && (respText !== undefined) && (respText.length > 0)) {
            if (respText.substr(0,9) === "<!DOCTYPE") {
              var pos1 = respText.indexOf("<h1>");
              if (pos1 >= 0) {
                pos1 += 4;
                var pos2 = respText.indexOf("</h1>",pos1);
                if (pos2 >= 0) {
                  errMsg = respText.substring(pos1,pos2);
                }
              }
            } else {
              errMsg = "Response.Status = " + xmlHttpReq.status 
                      + "; Error Msg = " + respText;
            }
          }
          this.data.errorMsg = (errMsg === null)? 
          ("Response.Status = " + xmlHttpReq.status): errMsg;
        } else {
          this.data.request = xmlHttpReq;
        }
      }
      result = (!this.data.hasError());
    } catch (err) {
      this.isProcessing = false;
      if (this.isQueued) {
        myapp.ajax.queue.dequeue();
      }
      this.data.errorMsg = ('AjaxRequest.sendRequest[' + this.data.method 
              + '] Error[' + err.name 
              + ']: Message: [' + err.message + ']');
    }
    return result;
  };
  
  /**
   * The callback function called when the request status change. 
   * @returns {undefined}
   */
  this.onReadyStateChange = function() {
    if (xmlHttpReq === null) {
      return;
    }
    
    var err;
    this.data.readyState = this.xmlHttpReq.readyState;
    if (this.data.readyState === 4) {
      this.data.status = xmlHttpReq.status;
      this.data.responseType = xmlHttpReq.responseType;
      if (!isSuccessStatus(this.data.status)) {
        this.data.errorMsg = xmlHttpReq.responseText;
      } else {
        this.data.request = xmlHttpReq;
      }
    }
    
    /** call the callback function with any change in readyState **/
    if (statusChangedDelegate !== null) {
      statusChangedDelegate(this.data);
    }
    
    /** If Done - dequeue this AajxRequest to start the next one in the queue */
    if ((this.readyState === 4) && (this.isQueued)) {
      myapp.ajax.queue.dequeue();
    }
  };
};
AjaxRequest.Count = 0;
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="ParameterMap Class">
/****************************************************************************
 * Class[ParameterMap]
 ******************************************************************************/
function ParameterMap() { 
  /**
   * An Internal ParameterKey 'Class' for storing ParamaterMap Entries
   * @returns {ParameterMap.ParameterEntry}
   */
  function ParameterEntry() {
    /**
     * The internal Entry storing the key-value set
     * @type ParameterEntry.entry
     */
    var entry = {
      /**
       * Entry Key
       * @type String
       */
      key: null,
      /**
       * Entry Value
       * @type Object
       */
      value: null
    };
    
    /**
     * Set the ParameterMap Entry's Key-value Pair. Throw exception is key=null|""
     * or is already assigned.
     * @param {String} key the Entry's Key
     * @param {String} value the Entry's value (cannot be null)
     * @returns {undefined}
     */
    this.set = function(key, value) {
      key = myapp.strings.allTrim(key);
      if (key.length === 0) {
        throw new Error("The entry key cannot be unassigned.");
      }
      value = myapp.strings.allTrim(value);
      if (value.length === 0) {
        throw new Error("The entry value cannot be unassigned.");
      }
      if (entry.key !== null) {
        throw new Error("The entry key cannot be re-assigned.");
      }
      entry.key = key;
      entry.value = value;
    };
    
    /**
     * Get the Parameter Entry's Key
     * @returns {String} the assigend value
     */
    this.getKey = function() {
      return entry.key;
    };
    
    /**
     * Get the Parameter Entry's Value
     * @returns {String} the assigend value
     */
    this.getValue = function() {
      return entry.value;
    };
    
    /**
     * Set the Dictionary Entry's Value. If value = null|undefined|"", throw an Error.
     * If (typeof value != string), if (typeof value = boolean), set value = '1|0' for
     * value = true|false. Else set value = value.toString(). 
     * @param {String} value the new value (can be null)
     * @returns {undefined} 
     */
    this.setValue = function(value) {
      if ((value !== null) && (value !== undefined) && (typeof value !== 'string')) {
        if (typeof value === 'boolean') {
          value = (value)? '1': '0';
        } else {
          value = value.toString();
        }
      }
      value = myapp.strings.allTrim(value);
      if (value.length === 0) {
        throw new Error("The entry value cannot be unassigned.");
      }
      entry.value = value;
    };
    
    /**
     * Get whether the Entry is undefined 
     * @returns {Boolean} true if this.entry.key=null
     */
    this.isEmpty = function() {
      return (entry.key === null);
    };
    
    /**
     * Check if the specified key matches the Entry's Key
     * @param {String} key the key to match
     * @returns {Boolean} true if it is a match - macth is not case sensitive
     */
    this.isEntry = function(key) {
      var result = false;
      key = myapp.strings.allTrim(key);
      if ((key.length > 0) && (!this.isEmpty())) {
        result = (entry.key.toLowerCase() === key.toLowerCase());
      }
      return result;
    };    
  };
  
  /**
   * Array of Dictionary Entries of type ParamaterEntry
   * @type Array
   */
  var parEntries = null;
  
  
  /**
   * Get the index of the Parameter Dictionary Entry by key
   * @param {String} key - Parameter key to search for
   * @returns {int} the index or -1 if not found
   */
  function getIndexOf(key) {
    var result = -1;
    var idx;
    key = myapp.strings.allTrim(key);
    if ((key.length > 0) && (parEntries !== null) && (parEntries.length > 0)) {
      for (idx = 0; idx < parEntries.length; idx++) {
        /**
         * @type ParameterEntry
         */
        var entry = parEntries[idx];
        if ((entry !== null) && (entry.isEntry(key))) {
          result = idx;
          break;
        }
      }
    }
    return result;
  };
  
  /**
   * Get the  Parameter Entry by key
   * @param {String} key - Parameter key to search for
   * @returns {ParameterEntry} or null if not found
   */
  function getEntry(key) {
    var result = null;
    var idx;
    key = myapp.strings.allTrim(key);
    if ((key.length > 0) && (parEntries !== null) && (parEntries.length > 0)) {
      for (idx = 0; idx < parEntries.length; idx++) {
        /**
         * @type ParameterEntry
         */
        var entry = parEntries[idx];
        if ((entry !== null) && (entry.isEntry(key))) {
          result = entry;
          break;
        }
      }
    }
    return result;
  };
  
  /**
   * Set the Dictionary Key-Value Pair. If the Key already exists, the 
   * value will be overridden
   * @param {String} key the Entry's Key
   * @param {Object} value the Entry's Value (can be null)
   * @returns {void}
   */
  this.put = function(key,value) {
    key = myapp.strings.allTrim(key);
    if (key === "") {
      throw new Error("The entry key cannot be unassigned.");
    }
    
    /**
     * @type ParameterEntry
     */
    var entry = getEntry(key);
    if (entry !== null) {
      entry.setValue(value);
    } else {
      entry = new ParameterEntry();
      entry.set(key,value);
      if (parEntries === null) {
        parEntries = new Array();
      }
      parEntries.push(entry);
    }
  };
  
  /**
   * Get the Dictioanry Value by its Key
   * @param {String} key - dictionary key to search for
   * @returns {String} the parameter value or "" if not found.
   */
  this.get = function(key) {
    /**
     * @type ParameterEntry
     */
    var entry = getEntry(key);
    return (entry === null)? "": entry.getValue();
  };
  
  /**
   * Call to remove an entry with matching key
   * @param {String} key - dictionary key to search for
   * @returns {undefined}
   */
  this.remove = function(key){
    var idx = getIndexOf(key);
    if (idx >= 0) {
      parEntries.splice(idx,1);
    }
  };
  
  /**
   * Check if the Dictionary is empty - nor entries.
   * @returns {Boolean} true if undefine dor empty.
   */
  this.isEmpty = function() {
    return ((parEntries === null) || (parEntries.length === 0));
  };
  
  /**
   * Get the number of entries in the ParameterMap
   * @returns {Number} the numbe rof entries or 0 if empty.
   */
  this.size = function() {
    return (this.isEmpty())? 0: parEntries.length;
  };
  
  /**
   * Get the ParameterMap's Keys
   * @returns {Array} an array of ParameterEntry.key or an empty array if isEmpty.
   */
  this.getKeys = function() {
    var result = new Array();
    if (!this.isEmpty()) {
      /**
       * @type ParameterEntry
       */
      var entry = null;
      for (var iEntry = 0; iEntry < parEntries.length; iEntry++) {
        entry = parEntries[iEntry];
        result.push(entry.getKey());
      }
    }
    return result;
  };
  
  /**
   * Get the ParameterMap entries as a URL query String - encoded as necessary.
   * @returns {String} the appended Url or the baseUrl is param = null|empty.
   */
  this.getAsQueryString = function() {
    /**
     * @type String
     */
    var result = "";
    
    /** Add the Paramaters to the Url **/
    if (!this.isEmpty()) {
      /**
       * an Array of ParameterMap keys
       * @type Array
       */        
      var parKeys = this.getKeys();
      if ((parKeys !== null) && (parKeys.length > 0)) {
        var key = "";
        var value = "";
        for (var iKey = 0; iKey < parKeys.length; iKey++) {
          key = parKeys[iKey];
          value = this.get(key);
          if ((value !== null) && (value.length > 0)) {
            if (result.length > 0) {
              result += "&";
            }
            result += encodeURIComponent(key) + "=" + encodeURIComponent(value);
          }
        }
      }
    }
    return result;
  };
  
  this.getAsJSON = function() {
    /**
     * @type String
     */
    var result = "";
    /** Add the Paramaters to the Url **/
    if (!this.isEmpty()) {
      /**
       * an Array of ParameterMap keys
       * @type Array
       */        
      var parKeys = this.getKeys();
      if ((parKeys !== null) && (parKeys.length > 0)) {
        var key = "";
        var value = "";
        var cnt = 0;
        for (var iKey = 0; iKey < parKeys.length; iKey++) {
          key = parKeys[iKey];
          value = this.get(key);
          if ((value !== null) && (value.length > 0)) {
            if (cnt > 0) {
              result += ",";
            }
            result += "{\"" + key + "\":";
            if (isNaN(value)) {
              result += "\"" + value + "\""
            }
            cnt++;
          }
        }
      }
    }
    return result;
  };
};
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="myapp.ajax Static Class">
/**
 * The myApp Ajax Prototype for handling any custom Ajax Request
 * @type myapp.ajax
 */
myapp.ajax = {
    /**
   * Placeholder for the Application's Faces-Request parameter which will be added as a 
   * XMLHttpRequest header[Faces-Request] (default = "partial/ajax").
   * @type String
   */
  facesRequest: "partial/ajax",

  /**
   * The Ajax Queue used for async request.
   * @type Queue
   */
  queue: function Queue() {
    // Create the internal queue array
    var queue = [];

    // the amount of space at the front of the queue, initialised to zero
    var queueSpace = 0;

    /**
     * Returns the size of this Queue. The size of a Queue is equal to the number
     * of elements that have been enqueued minus the number of elements that have
     * been dequeued.
     * @returns {Number} the number of AjaxRequests in the Queue
     */
    this.getSize = function getSize() {
      return queue.length - queueSpace;
    };

    /**
     * Check if this Queue is empty. A Queue is empty if the number of elements that have 
     * been enqueued equals the number of
     * elements that have been dequeued.
     * @returns {Boolean} true and false otherwise
     */
    this.isEmpty = function isEmpty() {
      return (this.getSize() === 0);
    };

    /**
     * Enqueues the specified element in this Queue.
     * @param {AjaxRequest} element
     * @returns {undefined}
     */
    this.enqueue = function enqueue(element) {
      // Queue the request
      queue.push(element);
      element.queue = this;
      var nextElement = this.getOldestElement();
      if ((nextElement !== null) && (nextElement !== undefined) &&
              (!nextElement.isProcessing)) {
        nextElement.sendRequest();
      }
    };

    /** 
     * Dequeues an element from this Queue. The oldest element in this Queue is
     * removed and returned. If this Queue is empty then undefined is returned.
     * It also retrieve the new oldest element and is not isProcessing call its
     * sendRquest method.
     * @returns Object The element that was removed from the queue.
     */
    this.dequeue = function dequeue() {
      /*
       * initialise the element to return to be undefined
       * @type AjaxReAjaxRequest
       */
      var element = undefined;

      // check whether the queue is empty
      if (queue.length) {
        // fetch the oldest element in the queue
        element = queue[queueSpace];
        if (element !== null) {
          element.queue = null;
        }

        // update the amount of space and check whether a shift should occur
        if (++queueSpace * 2 >= queue.length) {
          // set the queue equal to the non-empty portion of the queue
          queue = queue.slice(queueSpace);
          // reset the amount of space at the front of the queue
          queueSpace = 0;
        }
      }

      /* Get the next Oldest Element and if available, assign this as its queue and
       * call its sendRequest to fire the request.
       * @type AjaxReAjaxRequest
       */
      var nextElement = this.getOldestElement();
      if ((nextElement !== null) && (nextElement !== undefined)) {
//        nextElement.
        nextElement.sendRequest();
      }

      // return the removed element
      try {
        return element;
      } finally {
        element = null; // IE 6 leak prevention
      }
    };

    /** Returns the oldest element in this Queue. If this Queue is empty then
     * undefined is returned. This function returns the same value as the dequeue
     * function, but does not remove the returned element from this Queue.
     * @ignore
     */
    this.getOldestElement = function getOldestElement() {
      // initialise the element to return to be undefined
      var element = undefined;

      // if the queue is not element then fetch the oldest element in the queue
      if (queue.length) {
        element = queue[queueSpace];
      }
      // return the oldest element
      try {
        return element;
      } finally {
        element = null; //IE 6 leak prevention
      }
    };
  }(),
    
  /**
   * Get the myapp.ajax's Request URL
   * @returns {String} - myapp.hostUrl/myajax or "" if (!myapp.hasHostUrl)
   */
  ajaxUrl: function() {
    var getSessionId = function(ajaxUrl) {
      var result = myapp.strings.allTrim(myapp.sessionId);
      if (result === "") {
        var args = new ParameterMap();
        args.put("action", "getsessionid");
        
        var mime = "text/plain";
        var ajaxRequest = new AjaxRequest('GET', ajaxUrl, mime, 0, null);
        ajaxRequest.data.context.sourceId = "myapp.ajax";
        ajaxRequest.data.args = args;
        ajaxRequest.sendRequest();
        if (ajaxRequest.data.hasError()) {
          result = null;
        } else if (ajaxRequest.data.request !== null) {     
          result = ajaxRequest.data.request.responseText;
          myapp.sessionId = result;
        }
      }
      return result;
    };
    
    /**
     * @type String
     */
    var result = "";
    if (myapp.hasHostUrl()) {      
      var result = myapp.hostUrl;
      if (result.slice(-1) !== "/") {
        result += "/";
      }
      result += "myajax";
      
      if (!myapp.hasPageId()) {
        var sessionId = getSessionId(result);
        if (sessionId !== null) {
          result += "?jsessionid=" + sessionId;
        }
      }
    }
    return result;
  },
  
  /**
   * Called to get a request text response based on a set of Query Paramaters
   * @param {String} sourceId the ID of the element/object that send the request
   * @param {ParameterMap} args - a parameters of arguments to send to the server.
   * @returns {String|ajaxRquest.xmlHttpReq.responseText}
   */
  getText: function(sourceId, args) {
    var result = null;
    try {
      var mime = "text/plain";
      var url = this.ajaxUrl();
      var ajaxRequest = new AjaxRequest('GET', url, mime, 0, null);
      ajaxRequest.data.context.sourceId = sourceId;
      ajaxRequest.data.args = args;
      ajaxRequest.sendRequest();
      if (ajaxRequest.data.hasError()) {
        result = ("The Ajax Request Failed: \n" + ajaxRequest.data.errorMsg);
      } else if (ajaxRequest.data.request !== null) {     
        result = ajaxRequest.data.request.responseText;
      }
    } catch (err) {
      result = ('myapp.ajax.getText Exception[' + err.name 
              + ']; message: [' + err.message + ']');
    }
    return result;
  }
//  post: function(url, mime, content) {
//    var xmlHttpReq = this.openRequest('POST', url, mime, content.length, false);
//    try {
//      xmlHttpReq.send(content);
//      if (xmlHttpReq.readyState == 4) {
//        var status = xmlHttpReq.status;
//        if (status == 201) {
//          return true;
//        } else {
//          this.debug('Failed XHR(POST, ' + url + '): Server returned --> ' + status);
//        }
//      }
//    } catch (e) {
//      this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message + ']');
//    }
//    return false;
//  },
//  put: function(url, mime, content) {
//    var xmlHttpReq = this.openRequest('PUT', url, mime, content.length, false);
//    try {
//      xmlHttpReq.send(content);
//      if (xmlHttpReq.readyState == 4) {
//        var status = xmlHttpReq.status;
//        if (status == 204) {
//          return true;
//        } else {
//          this.debug('Failed XHR(PUT, ' + url + '): Server returned --> ' + status);
//        }
//      }
//    } catch (e) {
//      this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message + ']');
//    }
//    return false;
//  },
//  delete_: function(url) {
//    return this.delete_(url, 'application/xml', null);
//  },
//  delete_ : function(url, mime, content) {
//    var length = 0;
//    if (content != null && content != undefined) {
//      length = content.length;
//    }
//    var xmlHttpReq = this.open('DELETE', url, mime, length, false);
//    try {
//      if (length == 0) {
//        xmlHttpReq.send(null);
//      }
//      else {
//        xmlHttpReq.send(content);
//      }
//      if (xmlHttpReq.readyState == 4) {
//        var status = xmlHttpReq.status;
//        if (status == 204) {
//          return true;
//        } else {
//          this.debug('Failed XHR(DELETE, ' + url + '): Server returned --> ' + status);
//        }
//      }
//    } catch (e) {
//      this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message + ']');
//    }
//    return false;
//  },
};
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="myapp.strings static methods">
/**
 * The myApp Ajax Prototype for handling custom String operation
 * @type myapp.strings
 */
myapp.strings = {
  /**
   * Trim all leading and trailing white spaces
   * @param {String} inStr
   * @returns {String|myapp.strings.allTrim.result}
   */
  allTrim: function(inStr) {
    var result = leftTrim(inStr);
    result= rightTrim(result);
    return result;
  },
  
  /**
   * Trim all white spaces to the left of the String
   * @param {String} inStr
   * @returns {String}
   */
  leftTrim: function(inStr) {
    var result = (inStr === undefined)? null: inStr;  
    if (result !== null) {
      try {
        var regExp = new RegExp("^([ \t\n\r\f\v]*)");
        var match = regExp.exec(inStr);
        if (match !== null) {
          result = result.replace(regExp,'');
        }
      } catch (err) {
        result = null;
      }
    }
    return (result === null)? "": result;
  },
  
  /**
   * Trim all white spaces to the right of the String
   * @param {String} inStr
   * @returns {String}
   */
  rightTrim: function(inStr) {
    var result = (inStr === undefined)? null: inStr;
    if (result !== null) {
      try {
        var regExp = new RegExp("([ \t\n\r\f\v\b]*)$");
        var match = regExp.exec(inStr);
        if (match !== null) {
          result = result.replace(regExp,'');
        }
      } catch (err) {
        result = null;
      }
    }
    return (result === null)? "": result;
  },
  
  /**
   * Check is the target string starts with the search string - search is case 
   * insensitive.
   * @param {String} targetStr the string to search
   * @param {String} searchStr the sring to search for
   * @returns {Boolean} true if targetStr starts with searchStr
   */
  startsWith: function(targetStr, searchStr) {
    var result = false;
    var err;
    try {
      if ((targetStr !== null) && (targetStr !== undefined) && (searchStr !== null) || 
              (searchStr.length <= targetStr.length)) {
        targetStr = targetStr.toUpperCase();
        searchStr = searchStr.toUpperCase();
        result = (targetStr.indexOf(searchStr) === 0);
      }
    } catch (err) { 
      alert("myapp.strings.startsWidth Error:\n" + err.message);
    }
    return result;
  },
  
  /**
   * Check is the target string ends with the search string - search is case insensitive.
   * @param {String} targetStr
   * @param {String} searchStr
   * @returns {Boolean} true if targetStr ends with searchStr
   */
  endsWith: function(targetStr, searchStr) {
    var result = false;
    var err;
    try {
      if ((targetStr !== null) && (targetStr !== undefined) && (searchStr !== null) || 
              (searchStr.length <= targetStr.length)) {
        targetStr = targetStr.toUpperCase();
        searchStr = searchStr.toUpperCase();
        result = (targetStr.substr(targetStr.length - searchStr.length) === searchStr);
      }
    } catch (err) { 
      alert("myapp.strings.endsWidth Error:\n" + err.message);
    }
    return result;
  },
  
  /**
   * Check is the target string contains the search string, but it neither starts or ends
   * with the searchStr - search is case insensitive.
   * @param {String} targetStr to search
   * @param {String} searchStr to search for
   * @returns {Boolean} true if targetStr contains searchStr
   */
  containedWithIn: function(targetStr, searchStr) {
    var result = false;
    var err;
    try {
      if (((targetStr !== null) && (targetStr !== undefined) && 
              (searchStr !== null) && (searchStr !== undefined) && (searchStr.length > 0)) || 
              (searchStr.length < targetStr.length)) {
        targetStr = targetStr.toUpperCase();
        searchStr = searchStr.toUpperCase();
        var idx = targetStr.indexOf(searchStr);
        result = ((idx >= 0) && (idx < (searchStr.length-1)));
      }
    } catch (err) { 
      alert("myapp.strings.containedWithIn Error:\n" + err.message);
    }
    return result;
  },
  
  /**
   * Called to replace all occurences of searchStr in targetStr with repalceStr
   * @param {type} targetStr the target String to update
   * @param {type} searchStr the string to search and replace (ignored is null|"")
   * @param {type} replaceStr the string to replace searchStr with (default="")
   * @returns {String} the update String
   */
  replace: function(targetStr, searchStr, replaceStr) {
    var result = targetStr;
    if ((targetStr !== null) && (targetStr !== undefined) && (targetStr.length > 0) &&
        (searchStr !== null) && (searchStr !== undefined) && (searchStr.length > 0) &&
        (targetStr.indexOf(searchStr) >= 0)) {
      replaceStr = ((replaceStr !== null) && (replaceStr !== undefined))? "": replaceStr;
      
      result = "";
      /**
       * type {String}
       */
      var remStr = targetStr; 
      var iPos = remStr.indexOf(searchStr);
      while (iPos >= 0) {
        result += (iPos > 0)? remStr.substring(0, iPos-1): "";
        result += replaceStr;
        iPos += searchStr.length;
        remStr = (iPos >= remStr.length)? "": remStr.substring(iPos);
        iPos = remStr.indexOf(searchStr);
      }
      
      if (remStr.length > 0) {
        result += remStr;
      }
    }
    return result;
  },
  
  /**
   * Check if the two String match. It calles this.allTrim() to remove all leading or 
   * trailing white spaces, before comapring the two strings. If (ignoreCase=true), the
   * two strings are converted to uppercase before compared.
   * @param {type} str1 the first string (can be null)
   * @param {type} str2 the second string (can be null)
   * @param {type} ignoreCase true to ignore case
   * @returns {Boolean} true is the two strings is a match.
   */
  isEq: function(str1, str2, ignoreCase) {
    ignoreCase = ((ignoreCase !== null) && (ignoreCase !== undefined) && (ignoreCase));
    str1 = this.allTrim(str1);
    str2 = this.allTrim(str2);
    if (ignoreCase) {
      str1 = str1.toUpperCase();
      str2 = str2.toUpperCase();
    }
    return (str1 === str2);
  },
  
  /**
   * Convert the input str (inStr) to a Numeric string without currency ($) symbol.
   * @param {type} inStr
   * @returns {String}
   */
  removeCurrency: function(inStr) {
    var result = "";
    
    var regExp = new RegExp("[\(]");
    var minusStr = '';

    //check if negative
    var matches = regExp.exec(inStr);
    if(matches !== null) {
      minusStr = '-';
    }

    regExp = new RegExp("[\)]|[\(]|[,]","g");
    matches = regExp.exec(inStr);
    if (matches !== null) {
      inStr = inStr.replace(regExp,'');
    }

    var iPos = inStr.indexOf('$');
    if(iPos >=  0) {
      inStr = inStr.substring(iPos+1, inStr.length-iPos);
    }
    
    inStr = allTrim(inStr);
    if (inStr.length > 0) {
      result = minusStr + inStr;
    }

    return result;
  },

  /**
   * Convert input numeric string to a currency ($) string with 2 Decimals. It first 
   * converts the string to an flaot number, convert it back the string value with two 
   * decimal digits, add thousand seperators, and then add the "$" current prefix.
   * @param {String} inStr
   * @returns {String} the formatted string
   */
  addCurrency: function(inStr) {
    var result = "$0.00";
    var floatVal = myapp.numbers.toFloat(inStr);
    if (!isNaN(floatVal)) {    	  
      floatVal = Math.round(floatVal*100)/100;
      var valStr = floatVal.toFixed(2);
      valStr = this.addCommas(valStr);

      var regExp = new RegExp("^-");
      var matches = regExp.exec(valStr);
      if (matches !== null) {
        valStr = "($"+valStr.replace(regExp,'')+")";
      } else {
        valStr = "$"+valStr;
      }
      result = valStr;
    }
    return result;
  },

  /**
   * Format a numeric String value by adding throusand seperators
   * @param {type} inStr the numeric input string
   * @returns {String}
   */
  addCommas: function(inStr) {
    var result = inStr; 
    var decStr = "";
    var intStr = "";
    var regExp  = new RegExp("(-?[0-9]*)\.([0-9]*)");
    var matches = regExp.exec(inStr);
    if (matches !== null) {
      intStr = matches[1];
      decStr = matches[2];
    } else {
      intStr = inStr;
    }

    regExp  = new RegExp("(-?[0-9]+)([0-9]{3})");
    var newStr = "";
    var remStr = "";
    matches = regExp.exec(intStr);
    while (matches !== null) {
      remStr = matches[1];
      var subStr = matches[2];
      if (subStr.length > 0) {
        if (newStr.length > 0) {
          subStr += ",";
        }
        newStr = subStr + newStr;
      }
      matches = regExp.exec(remStr);
    }

    if (remStr === "-") {
      intStr = remStr + newStr;
    } else if (remStr.length > 0) {
      if (newStr.length > 0) {
        remStr += ",";
      }
      intStr = remStr + newStr;
    }

    result = (decStr.length > 0)? (intStr + "." + decStr): intStr; 
    return result;
  },

  /**
   * Strips a numeric string for any commas that was added as thousand seperators
   * @param {String} inStr
   * @returns {String}
   */
  removeCommas: function (inStr) {
    var regExp  = new RegExp(",","g");
    var result = inStr;
    //check for match to search criteria
    if (regExp.exec(inStr) !== null) {
      result = inStr.replace(regExp, '');
    }
    return result;
  }
};
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="myapp.numbers static methods">
myapp.numbers = {
  /**
   * Convert the input string (inStr) to an integer value. It trim all white spaces form
   * inStr and remove all currency prefixes and commas before attempting to parse the 
   * string to an integer value
   * @param {type} inStr
   * @returns {Number|Number.NaN} the inetrger value or NaN is the aprsing failed or the
   * inStr = ""|null.
   */
  toInt: function(inStr) {
    var result = Number.NaN;
    if ((inStr !== undefined) && (inStr !== null)) {
      inStr = inStr.toString();
      inStr = myapp.strings.allTrim(inStr);
      inStr = myapp.strings.removeCurrency(inStr);
      if (!isNaN(inStr)) {
        result = parseInt(inStr);
      }
    }
    return result;
  },
  /**
   * Convert the input string (inStr) to an float value. It trim all white spaces form
   * inStr and remove all currency prefixes and commas before attempting to parse the 
   * string to an float value
   * @param {type} inStr
   * @returns {Number|Number.NaN} the inetrger value or NaN is the aprsing failed or the
   * inStr = ""|null.
   */
  toFloat: function(inStr) {
    var result = Number.NaN;
    if ((inStr !== undefined) && (inStr !== null)) {
      inStr = inStr.toString();
      inStr = myapp.strings.allTrim(inStr);
      inStr = myapp.strings.removeCurrency(inStr);
      if (!isNaN(inStr)){
        result = parseFloat(inStr);
      }	
    }
    return result;
  }
};
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="myapp.element Static Methods">
myapp.element = {
  /**
   * Get the Form Element for the specified child element. 
   * @param {Element} formElement any element of the form
   * @returns {Node|Element}
   */
  findForm: function(formElement) {
    /**
     * @type Element
     */
    var result = null;
    if (formElement !== null) {
      var parent = formElement.parentNode;
      while (parent !== null) {
        if ((parent.tagName.toUpperCase() === "FORM") ||
            (parent.nodeName.toUpperCase() === "FORM")) {
          result = parent;
          break;
        }
        parent = parent.parentNode;
      }
    }
    return result;
  },
  
  /**
   * 
   * @param {Element} proxyElem another element of the form
   * @param {String} partialId the partial suffix of the element's ID
   * @returns {Node|Element}
   */
  findFormElement: function(proxyElem, partialId) {
    /**
     * @type Element
     */
    var result = null;
    var formId = null;
    // alert("Focus Id=" + sFocusID + "\nFormId="+sFormID);
    var onGetFormElem = function(childList) {
      var childElem = null;

      if ((childList !== null) && (childList !== undefined) && (childList.length > 0)) {
        for (var i=0; i < childList.length; i++) {
          var childItem = childList[i];
          if ((childItem === null) || (childItem === undefined)) {
            continue;
          }

          var itemId = childItem.id;
          if ((itemId !== null) && (itemId !== undefined) && (itemId.length > 0)) {
            if (myapp.strings.isEq(itemId, partialId, true)) {
              childElem = childItem;
                break;
            } else if (myapp.strings.endsWith()(itemId, partialId)) {
              if (myapp.strings.startsWith(itemId, formId)) {
                childElem = childItem;
                break;
              }
            }
          }

          if (childElem === null) {
            childElem = onGetFormElem(childItem.childNodes);
            if (childElem !== null) {
              break;
            }
          }
        }
      } 
      return childElem;
    };

    var elemForm = myapp.element.findForm(proxyElem);
    if (elemForm !== null) {
      formId = elemForm.id;
      result = onGetFormElem(elemForm.childNodes);
    }
    return result;
  },
  
  /**
   * Get a Target Element for a specified Suffix in the Sibling list.
   * @param {Array} siblings - this list of child element to search
   * @param {String} targetSuffix - the Suffix to uniquely identify the sibling
   * @returns {Element} the taregt element or null if not found.
   */
  findSiblingElement: function(siblings, targetSuffix) {
    /**
     * @type element
     */
    var result = null;      
    if ((siblings !== null) && (siblings !== undefined) && (siblings.length > 0)) {
      for (var i=0; i < siblings.length; i++) {
        var childElem = siblings[i];
        if ((childElem === null) || (childElem === undefined)) {
          continue;
        }

        /**
         * @type String
         */
        var itemId = childElem.id;
        if ((itemId !== null) && (itemId !== undefined) && (itemId.length > 0)) {
          if(myapp.strings.endsWith(itemId,targetSuffix)) {
            result = childElem;
            break;
          }
        }
      }
    } 
    return result;
  },
  
  /**
   * Get a Siblling Element of a specified Type that is identified by a specified Style 
   * Class.
   * @param {Element} elem - this list of child element to search
   * @param {String} siblingType - the Tag Type
   * @param {String} siblingClass - the Style Class assigned to the Sibling
   * @returns {Element} the sibling element or null if not found.
   */
  findSiblingByTypeClass: function(elem, siblingType, siblingClass) {
    /**
     * @type Element
     */
    var result = null;      
    /**
     * @type Element
     */
    var parent = ((elem === null) || (elem === undefined))? null: elem.parentNode;
    /**
     * @type Array
     */
    var siblings = null;
    if ((parent !== null) && (parent !== undefined)) {
      siblings = parent.childNodes;
    }
    
    if ((siblings !== null) && (siblings !== undefined) && (siblings.length > 0)) {
      for (var i=0; i < siblings.length; i++) {   
        /**
         * @type Element
         */
        var childElem = siblings[i];
        if ((childElem === null) || (childElem === undefined) || (childElem === elem)) {
          continue;
        }

        /**
         * @type String
         */
        var childType = childElem.tagName;
        if (!myapp.strings.isEq(childType, siblingType, true)) {
          continue;
        }
        /**
         * @type String
         */
        var childClass = childElem.className;
        if ((childClass !== null) && (childClass !== undefined) && (childClass.length > 0)) {
          if (myapp.strings.containedWithIn(childClass,siblingClass)) {
            result = childElem;
            break;
          }
        }
      }
    } 
    return result;
  },

  /**
   * Set the value of Target Element of a Proxy-Element assuming that Elements has the same
   * Parent and the Target Element's name ends with the defined targetSuffix.
   * @param {String} proxyElemId the Proxy Element's ID
   * @param {String} targetSuffix the targetElement's suffix
   * @param {Object} newValue the new value to assign
   * @returns {Boolean} always return false
   */
  setProxySiblingValue: function(proxyElemId, targetSuffix, newValue) {
    try {
      if ((proxyElemId === null) || (proxyElemId === undefined) || 
              (allTrim(proxyElemId) === "")) {
        throw new Error("The Proxy Element name is unassigned.");
      }

      targetSuffix = ((targetSuffix === null) || (targetSuffix === undefined))? null:
              allTrim(targetSuffix);
      if (targetSuffix === "") {
        throw new Error("The Target Element's suffix is unassigned.");
      }

      /**
       * @type @exp;window@pro;document@call;getElementById
       */
      var proxyElem = window.document.getElementById(proxyElemId);
      if ((proxyElem === null) || (proxyElem === undefined)) {
        throw new Error("Unable to locate ProxyElement[" + proxyElemId + "].");
      }

      /**
       * @type @exp;proxyElem@pro;parentNode
       */
      var parentElem = proxyElem.parentNode;
      if ((parentElem === null) || (parentElem === undefined)) {
        throw new Error("ProxyElement[" + proxyElemId 
                + "]'s parentNode is not accessible.");
      }

      var targetElem = 
                    myapp.element.findSiblingElement(parentElem.childNodes, targetSuffix);
      if ((targetElem === null) || (targetElem === undefined)) {
        throw new Error("Unable to locate Proxy Sibling[*" + targetSuffix + "].");
      }

      targetElem.value = (newValue === null)? "": newValue.toString();
    } catch (err) {
      alert("setProxySiblingValue Error:\n " + err.message);
    }
    return false;
  },

  /**
   * Called to show the FormMask element as identified by its elementId
   * @param {String} formMaskId the FormMask element's elementId
   * @returns {Boolean} always true.
   */
  showFormMask: function(formMaskId) {
    try {
      var maskElem = window.document.getElementById(formMaskId);
      if ((maskElem !== null) && (maskElem !== undefined)) {
        maskElem.style.width = "100%";
        maskElem.style.height = "100%";
        maskElem.style.display = "block";
      }
    } catch (err) {
    }
    return true;
  }
};
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="myapp.forms Static Class">
myapp.forms = {  
  /**
   * The Default Form Update Interval (set as 60s).
   * @type Number default interval in seconds
   */
  DEF_UPDATE_INTERVAL: 60,
  
//  /**
//   * A Method called to handle the AJAX callback function. It locates the glabals.AppForm
//   * for the form identified by <tt>formId</tt> and if data.status="begin", it sets the
//   * appForm.pendingAjax = true, and if data.status="complete" | "error", it set the
//   * appForm.pendingAjax = false.
//   * @param {type} data the AJAX callback data object
//   * @param {type} formId the AppForm formId
//   * @returns {undefined}
//   */
//  setAjaxCallback: function(data, formId) {
//    if ((data !== null) && (data !== undefined)) {
//      /**
//       * @type AppForm
//       */
//      var appForm = null;
//      var status = data.status;
//      if ((status === null) || (status === undefined)) {
//        return;
//      } else if (myapp.strings.isEq(data.status, "begin", true)) {
//        appForm = findAppForm(formId);
//        if (appForm) {
//          appForm.setPendingAjax(true);
//        }
//      } else if ((myapp.strings.isEq(data.status, "complete", true)) ||
//                 (myapp.strings.isEq(data.status, "error", true))) {
//        appForm = findAppForm(formId);
//        if (appForm) {
//          appForm.setPendingAjax(false);
//        }
//      }
//    }
//  },
  
  /**
   * Called when focusForm came into focus (e.g., after a Ajax event). It check if the
   * AppForm.FocusElemDict caontains a regsitered focus ElementId and if found, it try
   * to set the focus back on the element.
   * @param {Form} focusForm the Form the cam into focus 
   * @returns {Boolean} always true
   */
  onSetFormFocus: function(focusForm) {
    try {
      if ((focusForm === null)|| (focusForm === undefined)) {
        throw new Error ("The Focus Form is undefined.");
      }

      var formId = focusForm.id;
      var focusElemdId = AppForm.FocusElemDict.get(formId);
      if (focusElemdId === null) {
        return ;
      }
      
      var focusElem = window.document.getElementById(focusElemdId);
      var elemForm = (focusElem === null)? null: myapp.element.findForm(focusElem);
      if ((focusElem === null) || (elemForm === null) || 
                                      (!myapp.strings.isEq(elemForm.id, formId))) {
        return ;
      }

      var elemTag = focusElem.tagName.toUpperCase();
      if (elemTag === "SELECT") {
        if ((focusElem.selectedIndex < 0) || (focusElem.selectedIndex === null)) {
          focusElem.selectedIndex = 0;
        }
        focusElem.focus();            
      } else {
        focusElem.focus();
        if ((elemTag !== 'TEXTAREA') && (elemTag !== "CHECKBOX")) {
          focusElem.select();
        }
      }
    } catch(exp) {
      alert("onSetFormFocus Error:\n "+exp.message);
    }
  },
  
  /**
   * A Method called to handle the AJAX callback function. It locates the glabals.AppForm
   * for the form identified by <tt>formId</tt> and if data.status="begin", it sets the
   * appForm.pendingAjax = true, and if data.status="complete" | "error", it set the
   * appForm.pendingAjax = false.
   * @param {Object} data the AJAX callback data object
   * @returns {undefined}
   */
  setAjaxCallback: function(data) {
    if ((data !== null) && (data !== undefined) && (data.source !== null)) {      
      /**
       * @type Form
       */
      var webForm = myapp.element.findForm(data.source);
      if (webForm === null) {
        return;
      }
      /**
       * @type AppForm
       */
      var appForm = null;
      var status = data.status;
      if ((status === null) || (status === undefined)) {
        return;
      } else if (myapp.strings.isEq(data.status, "begin", true)) {
        appForm = findAppForm(webForm.id);
        if (appForm) {
          appForm.setPendingAjax(true);
        }
      } else if ((myapp.strings.isEq(data.status, "complete", true)) ||
                 (myapp.strings.isEq(data.status, "error", true))) {
        appForm = findAppForm(webForm.id);
        if (appForm) {
          appForm.setPendingAjax(false);
        }
        myapp.forms.onSetFormFocus(webForm);
      }
    }
  },
  
  /**
   * Called to initiate the FormUpdater control
   * @param {String} formId the Form's ElementId
   * @param {String} controlId the FormUpdater's Elements Id (not its client ID)
   * @param {Number} interval the update intervals in seconds
   * @returns {undefined}
   */
  initFormUpdater: function(formId, controlId, interval) {
    try {
      controlId = myapp.strings.allTrim(controlId);
      if (controlId.length === 0) {
        throw Error("The FormUpdate ControlId is undefined");
      }
      
      interval = myapp.numbers.toInt(interval);
      if ((interval === null) || (isNaN(interval)) || (interval <= 0)) {
        interval = myapp.forms.DEF_UPDATE_INTERVAL;
      }
      
      /**
       * @type AppForm
       */
     var appForm = initAppForm(formId);
     if (appForm === null) {
       throw Error("Unable to locate or initiate AppForm[" + formId + "].");
     }
     
     var buttonId = controlId+ "_FormUpdateButton";
     var spinnerId = controlId+ "_FormUpdateSpinner";
     
     appForm.setPendingAjax(false);
     appForm.setAjaxSpinnerId(spinnerId);
     appForm.startAutoUpdate(buttonId, interval);
     var webForm = appForm.getElement();
     if (webForm !== null) {
       myapp.forms.onSetFormFocus(webForm);
     }
    } catch (err) {
      alert("myapp.forms.initFormUpdater Error:\n" + err.message);
    }
  },
  
  /**
   * Called to pause the AutoUpdate process. Typically assign to a element.onfucus event.
   * <p><b>Important:</b> call the 
   * @param {type} senderElem teh sender element (must be long to the autoUpdate form.
   * @returns {Boolean} always true.
   */
  pauseAutoUpdates: function(senderElem) {
    var result = true;
    var err;
    try {
      if (senderElem !== null) {
        /** type {AppForm} */
        var webForm = myapp.element.findForm(senderElem);
        if (!webForm) {
          throw new Error ("Unable to locate Element[" + senderElem + "]'s Form");
        }

        var formId = myapp.strings.allTrim(webForm.id);
        if ((formId !== null) && (formId.length > 0)) {        
          /** @type {AppForm} */
          var appForm = findAppForm(formId);
          if (appForm !== null) {
            appForm.pauseAutoUpdateTimer();
          }
        }
      }
    } catch(err) {
       alert("myapp.forms.pauseAutoUpdates Error:\n "+err.message);
    }
    return result;
  },

  /**
   * Called to resume the AutoUpdate process. Typically assign to a element.onblur event.
   * See pauseAutoUpdates for more details. 
   * @param {Element} senderElem the sender element (must belong to the autoUpdate form).
   * @returns {Boolean} always true.
   */
  resumeAutoUpdates: function(senderElem) {
    var result = true;
    var err;
    try {
      if (senderElem !== null) {
        /** type {AppForm} */
        var webForm = myapp.element.findForm(senderElem);
        if (!webForm) {
          throw new Error ("Unable to locate Element[" + senderElem + "]'s Form");
        }

        var formId = myapp.strings.allTrim(webForm.id);
        if ((formId !== null) && (formId.length > 0)) {         
          /** @type {AppForm} */
          var appForm = findAppForm(formId);
          if (appForm !== null) {          
            appForm.resumeAutoUpdateTimer();
          }
        }
      }
    } catch(err) {
       alert("myapp.forms.resumeAutoUpdates Error:\n " + err.message);
    }
    return result;
  },
  
  /**
   * Called to reset all the Form-related timers (i.e., the Globals.sessionTimer and the
   * AppForm's autoUpdateTime.  It locates the applicable for by the senderElem - a child
   * element of the form.
   * @param {Element} senderElem
   * @returns {Boolean} always return true.
   */
  resetTimers: function(senderElem) {
    var result = true;
    var err;
    try {
      Globals.resetSessionTimer();
      
      if (senderElem !== null) {
        /** type {AppForm} */
        var webForm = myapp.element.findForm(senderElem);
        if (!webForm) {
          return result;
        }

        var formId = myapp.strings.allTrim(webForm.id);
        if ((formId !== null) && (formId.length > 0)) {         
          /** @type {AppForm} */
          var appForm = findAppForm(formId);
          if (appForm !== null) {          
            appForm.resetAutoUpdateTimer();
          }
        }
      }
    } catch(err) {
       alert("myapp.forms.resetTimers Error:\n " + err.message);
    }
    return result;
  },
  
  /**
   * Called to check if senderElem's Form is busy processing AJAX request. It locates
   * the senderElem's form and its related AppForm and returns AppForm.hasPendingAjax.
   * @param {Element} senderElem
   * @returns {Boolean} AppForm.hasPendingAjax or false if the form or AppForm cannot be
   * located..
   */
  isFormBusy: function(senderElem) {
    var result = false;
    var err;
    try {
      if (senderElem !== null) {
        /** type {AppForm} */
        var webForm = myapp.element.findForm(senderElem);
        if (!webForm) {
          return result;
        }

        var formId = myapp.strings.allTrim(webForm.id);
        if ((formId !== null) && (formId.length > 0)) {         
          /** @type {AppForm} */
          var appForm = findAppForm(formId);
          if (appForm !== null) {          
            result = appForm.hasPendingAjax();
          }
        }
      }
    } catch(err) {
       alert("myapp.forms.isFormBusy Error:\n " + err.message);
    }
    return result;
  }
};
//</editor-fold>
