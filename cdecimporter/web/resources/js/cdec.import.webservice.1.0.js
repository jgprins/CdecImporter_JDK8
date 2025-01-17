//************************************************************************************
//ScriptName: cdec.import.webservice.1.0.js
//Date : 02/13/2015               						
//Version : 1.0        									    
//Author Dr J.G. "Koos" Prins		  
//CopyRights: GEI Consultants (2015)
//EMail: kprins@geiconsultants.com 	  	
//************************************************************************************
// Modifications on this code is not recommended
// Suggestions are welcome
//************************************************************************************
var cdecimporter = cdecimporter || {};

cdecimporter.ws = {
  /**
   * The URL for the Web Service
   */
  importPath: "",
  /**
   * Flag set to true is doing debugging
   * @type Boolean
   */
  doDebug: false,
  
  /**
   * Set the Web Service Server Url
   * @param {String} path 
   * @returns {undefined}
   */
  setImportPath: function(path) {
    this.importPath = path;
  },
  
  /**
   * Get the Full Request URL to retrieve B-120 subUrl's JSON
   * @param {String} subUrl the sub-url for which to query (e.g. "table1")
   * @param {Number} wy the water year
   * @param {Number} mon teh forecast months 2-5
   * @returns {String} the full request URL.
   */
  buildQryPath: function(subUrl, wy, mon) {
    var result = null;
    subUrl = myapp.strings.allTrim(subUrl);
    if ((subUrl === null) || (subUrl.length === 0)) {
      throw new Error("The specified subUrl is invalid or empty.");
    }
    
    wy = myapp.numbers.toInt(wy);
    if ((wy === null) || (isNaN(wy))) {
      throw new Error("The Entered Water Year is invalid. Expected a number");
    }
    
    mon = myapp.numbers.toInt(mon);
    if ((mon === null) || (isNaN(mon))) {
      throw new Error("The Entered Forecast Month is invalid. Expected a number");
    }
    subUrl += "/" + wy;
    subUrl += "/" + mon;
    
    result = this.buildUrl(subUrl,null);
    return result;
  },
  
  /**
   * Check whether the wsfcast.ws.serverUrl is assigned.
   */
  hasImportPath: function() {
    return ((this.importPath !== null) && (this.importPath.length > 0));
  },
  
  /**
   * Check whether the wsfcast.ws.serverUrl is assigned.
   */
  getHostUrl: function() {
    var result = window.location.host;
    return result;
  },
  
  /**
   * Check whether the wsfcast.ws.serverUrl is assigned.
   */
  getImportUrl: function() {
    var result = window.location.host;
    var result = window.location.host;
    if (result.slice(-1) !== "/") {
      if (cdecimporter.importPath.slice(0,1) !== "/") {
        result += "/";
      }
    } else if (cdecimporter.importPath.slice(0,1) === "/") {
      cdecimporter.importPath = cdecimporter.importPath.substring(1);
    }
    result += this.importPath;
    
    return result;
  },
  
  /**
   * Build the full request Url based on a sub-request URL and a set of parameters
   * @param {String} requestUrl the sub-request URL
   * @param {ParameterMap} params the set of parameters (can be null)
   * @returns {String} the full URL
   */
  buildUrl: function(requestUrl, params) {
    /**
     * @type String
     */
    var result = this.getImportUrl();
      
    requestUrl = myapp.strings.allTrim(requestUrl);
    if (requestUrl.length === 0) {
      throw new Error("The Request sub-Url Target cannot be unassigned.");
    }      
    if (result.slice(-1) !== "/") {
      if (requestUrl.slice(0,1) !== "/") {
        result += "/";
      }
    }
    
    if (requestUrl.slice(0,1) === "/") {
      requestUrl = requestUrl.substring(1);
    }
    result += requestUrl;

    /** Add the Paramaters to the Url **/
    result = this.appendUrlParams(result, params);
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
  }
};

/*
 * Support the WebService's Ajax Call - copied from/based upon rjsSupport
 */
cdecimporter.ws.ajax = {
  proxy: "",
  getHttpProxy: function() {
    return this.proxy;
  },
  setHttpProxy: function(proxy_) {
    this.proxy = proxy_;
  },
  isSetHttpProxy: function() {
    return this.getHttpProxy().length > 0;
  },
  getHttpRequest: function() {
    var xmlHttpReq;
    try
    {    // Firefox, Opera 8.0+, Safari, IE7.0+
      xmlHttpReq = new XMLHttpRequest();
    }
    catch (e)
    {    // Internet Explorer 6.0+, 5.0+
      try
      {
        xmlHttpReq = new ActiveXObject("Msxml2.XMLHTTP");
      }
      catch (e)
      {
        try
        {
          xmlHttpReq = new ActiveXObject("Microsoft.XMLHTTP");
        }
        catch (e)
        {
          this.debug("Your browser does not support AJAX!");
        }
      }
    }
    return xmlHttpReq;
  },
  findUrl: function(url, method) {
    var url2 = url;
    if (this.isSetHttpProxy())
      url2 = this.getHttpProxy() + "?method=" + method + "&url=" + url2;
    return url2;
  },
  findMethod: function(method) {
    var method2 = method;
    if ((method !== "GET") && (this.isSetHttpProxy())) {
      method2 = "POST";
    }
    return method2;
  },
  open: function(method2, url2, mimeType, paramLen, async) {
    
    //Change url and method if using http proxy
    var url = this.findUrl(url2, method2);
    var method = this.findMethod(method2);
    
    //add timestamp to make url unique in case of IE7
    var timestamp = new Date().getTime();
    if (url.indexOf("?") !== -1)
      url = url + "&timestamp=" + timestamp;
    else
      url = url + "?timestamp=" + timestamp;
    
    var xmlHttpReq = this.getHttpRequest();
    if (xmlHttpReq === null) {
      this.debug('Error: Cannot create XMLHttpRequest');
      return null;
    }
    try {
      netscape.security.PrivilegeManager.enablePrivilege("UniversalBrowserRead");
    } catch (e) {
      //this.debug("Permission UniversalBrowserRead denied.");
    }
    try {
      xmlHttpReq.open(method, url, async);
    } catch (e) {
      this.debug('Error: XMLHttpRequest.open failed for: ' + url + 
              ' Error name: ' + e.name + ' Error message: ' + e.message);
      return null;
    }
    if (mimeType !== null) {
      if (method === 'GET') {
        //this.debug("setting GET accept: "+mimeType);
        xmlHttpReq.setRequestHeader('Accept', mimeType);
      } else if (method === 'POST' || method === 'PUT') {
        //this.debug("setting content-type: "+mimeType);
        //Send the proper header information along with the request
        xmlHttpReq.setRequestHeader("Content-Type", mimeType);
        xmlHttpReq.setRequestHeader("Content-Length", paramLen);
        xmlHttpReq.setRequestHeader("Connection", "close");
      }
    }
    //For cache control on IE7
    xmlHttpReq.setRequestHeader("Cache-Control", "no-cache");
    xmlHttpReq.setRequestHeader("Pragma", "no-cache");
    xmlHttpReq.setRequestHeader("Expires", "-1");
    
    return xmlHttpReq;
  },
  loadXml: function(xmlStr) {
    var doc2;
    // code for IE
    if (window.ActiveXObject)
    {
      doc2 = new ActiveXObject("Microsoft.XMLDOM");
      doc2.async = "false";
      doc2.loadXML(xmlStr);
    }
    // code for Mozilla, Firefox, Opera, etc.
    else
    {
      var parser = new DOMParser();
      doc2 = parser.parseFromString(xmlStr, getDefaultMime());
    }
    return doc2;
  },
  findIdFromUrl: function(u) {
    var li = u.lastIndexOf('/');
    if (li !== -1) {
      var u2 = u.substring(0, li);      
      var li2 = u2.lastIndexOf('/');
      u2 = u.substring(0, li2);
      return u.substring(li2 + 1, li);
    }
    return -1;
  },
  get: function(url, mime) {
    var xmlHttpReq = this.open('GET', url, mime, 0, false);
    try {
      xmlHttpReq.send(null);
      if (xmlHttpReq.readyState === 4) {
        var rtext = xmlHttpReq.responseText;
        if ((rtext === undefined) || (rtext === '') || 
                                                (rtext.indexOf('HTTP Status') !== -1)) {
          if (rtext !== undefined)
            this.debug('Failed XHR(GET, ' + url + '): Server returned --> ' + rtext);
          return '-1';
        }
        return rtext;           
      }
    } catch (e) {
      this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message + ']');
    }
    return '-1';
  },
  post: function(url, mime, content) {
    var xmlHttpReq = this.open('POST', url, mime, content.length, false);
    try {
      xmlHttpReq.send(content);
      if (xmlHttpReq.readyState === 4) {
        var status = xmlHttpReq.status;
        if (status === 201) {
          return true;
        } else {
          this.debug('Failed XHR(POST, ' + url + '): Server returned --> ' + status);
        }
      }
    } catch (e) {
      this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message + ']');
    }
    return false;
  },
  put: function(url, mime, content) {
    var xmlHttpReq = this.open('PUT', url, mime, content.length, false);
    try {
      xmlHttpReq.send(content);
      if (xmlHttpReq.readyState === 4) {
        var status = xmlHttpReq.status;
        if (status === 204) {
          return true;
        } else {
          this.debug('Failed XHR(PUT, ' + url + '): Server returned --> ' + status);
        }
      }
    } catch (e) {
      this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message + ']');
    }
    return false;
  },
  delete_ : function(url) {
    return this.delete_(url, 'application/xml', null);
  },
  delete_ : function(url, mime, content) {
    var length = 0;
    if (content !== null && content !== undefined) {
      length = content.length;
    }
    var xmlHttpReq = this.open('DELETE', url, mime, length, false);
    try {
      if (length === 0) {
        xmlHttpReq.send(null);
      }
      else {
        xmlHttpReq.send(content);
      }
      if (xmlHttpReq.readyState === 4) {
        var status = xmlHttpReq.status;
        if (status === 204) {
          return true;
        } else {
          this.debug('Failed XHR(DELETE, ' + url + '): Server returned --> ' + status);
        }
      }
    } catch (e) {
      this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message + ']');
    }
    return false;
  },
  
  debug: function(message) {
    var dbgComp = document.getElementById("dbgComp");
    if (dbgComp === null) {
      dbgComp = document.createElement("div");
      dbgComp.setAttribute("id", "dbgComp");
      dbgComp.style.border = "#2574B7 1px solid";
      dbgComp.style.font = "12pt/14pt sans-serif";
      var br = document.createElement("div");
      document.getElementsByTagName("body")[0].appendChild(br);
      br.innerHTML = '<br/><br/><br/>';
      document.getElementsByTagName("body")[0].appendChild(dbgComp);
      if (cdecimporter.ws.doDebug) {
        dbgComp.style.display = "";
      } else {
        dbgComp.style.display = "none";
      }
      var tab = 'width: 20px; border-right: #2574B7 1px solid; border-top: #2574B7 1px solid; border-left: #2574B7 1px solid; border-bottom: #2574B7 1px solid; color: #000000; text-align: center;';
      var addActionStr = '<div style="' + tab + '"><a style="text-decoration: none" href="javascript:rjsSupport.closeDebug()"><span style="color: red">X</span></a></div>';        
      dbgComp.innerHTML = '<table><tr><td><span style="color: blue">Rest Debug Window</span></td><td>' + addActionStr + '</td></tr></table><br/>';
    }
    var s = dbgComp.innerHTML;
    var now = new Date();
    var dateStr = now.getHours() + ':' + now.getMinutes() + ':' + now.getSeconds();
    dbgComp.innerHTML = s + '<span style="color: red">rest debug(' + dateStr + '): </span>' + message + "<br/>";
  },
  closeDebug: function() {
    var dbgComp = document.getElementById("dbgComp");
    if (dbgComp !== null) {
      dbgComp.style.display = "none";
      dbgComp.innerHTML = '';
    }
  }
};

cdecimporter.ws.ajax.json = {  
  /* Default getJson_() method . Do not remove. */
  get: function(fullUrl) {
    return cdecimporter.ws.ajax.get(fullUrl, 'application/json');
  },
  /* Default putJson_() method . Do not remove. */
  put: function(fullUrl, content) {
    return cdecimporter.ws.ajax.put(fullUrl, 'application/json', content);
  },
  /* Default postJson_() method . Do not remove. */
  post: function(fullUrl, content) {
    return cdecimporter.ws.ajax.post(fullUrl, 'application/json', content);
  },
  /* Default deleteJson_() method . Do not remove. */
  delete: function(fullUrl, content) {
    return cdecimporter.ws.ajax.delete_(fullUrl, 'application/json', content);
  }
};

cdecimporter.ws.ajax.html = {  
  /* Default getJson_() method . Do not remove. */
  get: function(fullUrl) {
    return cdecimporter.ws.ajax.get(fullUrl, 'application/html');
  },
  /* Default putJson_() method . Do not remove. */
  put: function(fullUrl, content) {
    return cdecimporter.ws.ajax.put(fullUrl, 'application/html', content);
  },
  /* Default postJson_() method . Do not remove. */
  post: function(fullUrl, content) {
    return cdecimporter.ws.ajax.post(fullUrl, 'application/html', content);
  },
  /* Default deleteJson_() method . Do not remove. */
  delete: function(fullUrl, content) {
    return cdecimporter.ws.ajax.delete_(fullUrl, 'application/html', content);
  }
};

cdecimporter.ws.ajax.text = {
  get: function(fullUrl) {
    return cdecimporter.ws.ajax.get(fullUrl, 'text/plain');
  },
  post: function(fullUrl, content) {
    return cdecimporter.ws.ajax.post(fullUrl, 'text/plain', content);
  }
};

cdecimporter.ws.ajax.xml = {
  get: function(fullUrl) {
    return cdecimporter.ws.ajax.get(fullUrl, 'application/xml');
  },
  post: function(fullUrl, content) {
    return cdecimporter.ws.ajax.post(fullUrl, 'application/xml', content);
  }
};

