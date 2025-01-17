
var webctx = {
  /**
   * The URL for the Web Service
   */
  contextPath: "/cdecimporter",
  /**
   * The URL for the Web Service
   */
  importPath: "/import",
  
  /**
   * Check whether the wsfcast.ws.serverUrl is assigned.
   */
  hasImportPath: function() {
    return ((webctx.importPath !== null) && (webctx.importPath.length > 0));
  },
  
  /**
   * Check whether the wsfcast.ws.serverUrl is assigned.
   */
  getHostUrl: function() {
    var protocol = window.location.protocol;
    if (!protocol.endsWith("//")) {
      protocol += "//";
    }
    var result = protocol +  window.location.host;
    var ctxUrl = result;
    if (((webctx.contextPath !== null) && (webctx.contextPath.length > 0))) {
      if (ctxUrl.slice(-1) !== "/") {
        if (webctx.contextPath.slice(0,1) !== "/") {
          ctxUrl += "/";
        }
      } else if (webctx.contextPath.slice(0,1) === "/") {
        webctx.contextPath = webctx.contextPath.substring(1);
      }
      ctxUrl += webctx.contextPath;
      
      var href = window.location.href;
      if ((href !== null) && (href !== undefined) &&
          (href.startsWith(ctxUrl))) {
        result = ctxUrl;
      }
    }
    return result;
  },
  
  /**
   * Check whether the wsfcast.ws.serverUrl is assigned.
   */
  getImportUrl: function() {
    var result = webctx.getHostUrl();
    if (webctx.hasImportPath()) {
      if (result.slice(-1) !== "/") {
        if (webctx.importPath.slice(0,1) !== "/") {
          result += "/";
        }
      } else if (webctx.importPath.slice(0,1) === "/") {
        webctx.importPath = webctx.importPath.substring(1);
      }
      result += webctx.importPath;
    }    
    return result;
  }  
};
/*
 * Support js for Ptsearch
 */
function CdecImporter(uri_) {
  this.uri = uri_;
}

CdecImporter.prototype = {
  getUri: function() {
    return this.uri;
  },
  toString: function() {
    var result =
            '{' +
            '"@uri":"' + this.uri + '"' +
            '}';
    return result;
  },
  asString: function(param) {
    if (param === undefined) {
      return '';
    }
    var result = '';
    if (!(param instanceof Array)) {
      if ('object' === typeof(param)) {
        result = '{';
        var count = 0;
        for (var prop in param) {
          count++;
        }
        var i = 0;
        for (var prop in param) {
          result = result + '"' + prop + '":' + this.asString(param[prop]);
          if (i < count - 1) {
            result = result + ',';
          }
          i++;
        }
        result = result + '}';
      }
      else {
        result = '"' + param + '"';
      }
      return result;
    }
    else {
      result = '[';
      var j = 0;
      for (j = 0; j < param.length; j++) {
        result = result + this.asString(param[j]);
        if (j !== param.length - 1) {
          result = result + ',';
        }
      }
      result = result + ']';
      return result;
    }
  }
};

function CdecImporterRemote(uri_) {
  this.uri = uri_;
}

CdecImporterRemote.prototype = {
  /* Default getJson_() method . Do not remove. */
  getJson_: function(uri_) {
    return rjsSupport.get(this.uri + uri_, 'application/json');
  },
  /* Default putJson_() method . Do not remove. */
  putJson_: function(uri_, content) {
    return rjsSupport.put(this.uri + uri_, 'application/json', content);
  },
  /* Default postJson_() method . Do not remove. */
  postJson_: function(uri_, content) {
    return rjsSupport.post(this.uri + uri_, 'application/json', content);
  },
  /* Default deleteJson_() method . Do not remove. */
  deleteJson_: function(uri_, content) {
    return rjsSupport.delete_(this.uri + uri_, 'application/json', content);
  },
  get: function(uri_) {
    return rjsSupport.get(this.uri + uri_, 'text/plain');
  },
  postText: function(uri_, content) {
    return rjsSupport.post(this.uri + uri_, 'text/plain', content);
  },
  postXml: function(uri_, content) {
    return rjsSupport.post(this.uri + uri_, 'application/xml', content);
  }
}
