/************************************************************************************
* ScriptName: global.js
* Date : 02/13/12               						
* Version : 2.0        									    
* Author J.G. "Koos" Prins, D.Eng, PE		  
* CopyRights: GEI Consultants (2011)
* EMail: kprins@geiconsultants.com 	  	
**************************************************************************************
*  Modifications on this code is not recommended
* Suggestions are welcome
**************************************************************************************
*
**************************************************************************************
* Globals Static class with function supporting the SessionTimeout, and Java Version 
* validation - Required Requires http://www.java.com/js/deployJava.js loaded.
**************************************************************************************/
function Globals()
{}
Globals.version="2.0";
Globals.minJavaVersion="1.6.0_10";
Globals.SessionTimeOut=0;
Globals.SessionTimer=null;
Globals.TimeOutURL=null;
Globals.TimeRemaining=0;
Globals.TestMode=false;
Globals.TimerStatusElement=null;
/**
 * Call to start a SessionTimer for interval[Globals.SessionTimeOut] that will redirect
 * browser to Globals.TimeoutURL when the time runs out. The Timer is restarted 
 * everytime the fucntion is called. It will not start the timer if the two parameters
 * Globals.SessionTimeOut and Globals.TimeoutURL are not set.
 */
Globals.startSessionTimer = function() {
  if (Globals.SessionTimer !== null) {
    window.clearInterval(Globals.SessionTime);
    Globals.SessionTimer = null;
  };

  var pOnClick = function() {
    if (Globals.SessionTimeOut >= 0) {
      if ((Globals.SessionTimer !== null) && (Globals.TimeRemaining > 0)) {
        Globals.TimeRemaining--;
      }
      if (Globals.TimeRemaining === 0) {
        window.clearInterval(Globals.SessionTimer);
        pOnTimeout();
      } else if ((Globals.TestMode) && (Globals.TimerStatusElement !== null)) {
        Globals.TimerStatusElement.innerHTML = 
                          "Session Time Remaining = "+Globals.TimeRemaining+" sec";
      }
    }
  };

  var pOnTimeout = function() {
    if ((Globals.SessionTimer !== null) 
            && (Globals.TimeOutURL !== null) && (Globals.TimeOutURL !== "")) {
      window.open(Globals.TimeOutURL,"_self","",true);
    }
  }
  
  if ((Globals.SessionTimeOut !== null) && (Globals.SessionTimeOut >= 0) 
      && (Globals.TimeOutURL !== null) && (Globals.TimeOutURL !== "")) {
    Globals.TimeRemaining = Globals.SessionTimeOut;
    Globals.SessionTimer = window.setInterval(function(){pOnClick();}, 1000);
  };
};

/** 
 * Called to set the TimerStatus Element for displaying a Time Message when in TestMode.
 * @param {String} sElemId - the DOM Element ID
 */
Globals.setTimerStatusElement = function(sElemId) {
  Globals.TimerStatusElement = null;
  try {
    if ((sElemId !== null) && (allTrim(sElemId) !== "")) {
      sElemId = allTrim(sElemId);
      Globals.TimerStatusElement = window.document.getElementById(sElemId);
    }
  } catch (pNoErr) {
    alert("Location TimerStatus Element[" + sElemId + "] failed.\n" + pNoErr.message);
    Globals.TimerStatusElement = null;
  };
};

/** 
 * Called to reset the TimeRemaining to the Globals.SessionTimeOut
 */
Globals.resetSessionTimer = function() {
  Globals.TimeRemaining = Globals.SessionTimeOut;
};

/** 
 * Called to reset the TimeRemaining to the Globals.SessionTimeOut
 */
Globals.stopSessionTimer = function() {
  if (Globals.SessionTimer !== null) {
    window.clearInterval(Globals.SessionTime);
    Globals.SessionTimer = null;
  }
};

/**
 * Get the Java Versions - calling the deployJava.getJREs()and generate a ";" delimited
 * string of the supported versions. Requires http://www.java.com/js/deployJava.js
 * @return null or "" or a ";" delimted list of Java versions.
 */
Globals.javaVersions = function () {
  var sResult = null;
  try {
    var pJREs = deployJava.getJREs();
    var sJREVersions = "";
    if ((pJREs !== null) && (pJREs.length > 0)) {
      for (var i = 0; i < pJREs.length; i++) {
        var sVersion = pJREs[i];
        if ((sVersion !== null) && (sVersion !== "")) {
          if (sJREVersions === "") {
            sJREVersions = sVersion;
          } else {
            sJREVersions += "; " + sVersion;
          }
        }
      }                          
    } 
    sResult = sJREVersions;
  } catch (pErr) {
    sResult = null;
  }
  return sResult;
};

/**
 * Get whether any Java adding is available.
 * Requires http://www.java.com/js/deployJava.js
 * @return true if Globals.javaVersions() != null|""
 */
Globals.isJavaEnabled = function () {
  var bResult = false;
  try {
    var sJREs = Globals.javaVersions();
    bResult = ((sJREs !== null) && (sJREs !== ""));
  } catch (pErr) {
    bResult = false;
  }
  return bResult;
};

/**
 * Get whether a Java is running and whether to active Java complies with the minimum 
 * version requirements - calling the deployJava.versionCheck(Globals.minJavaVersion + 
 * "+"). Requires http://www.java.com/js/deployJava.js
 * @return true if a available Java Version is eualt to of higher than 
 * Globals.minJavaVersion 
 */
Globals.hasJavaVersion = function () {
  var bResult = false;
  try {
    var sPattern = Globals.minJavaVersion + "+";
    var bHasVersion = deployJava.versionCheck(sPattern);    
    bResult = bHasVersion;
  } catch (pErr) {
    bResult = false;
  }
  return bResult;
};

/**
 * Called prior to loading the Applet to display a progress box in palce of the applet 
 * prior to loading 
 * @param sElemId the LoadBox's ElementId
 * @param sLoadMsg the message to display (if null a defautl message is displayed
 * @param sStyleClass the custom style class to set the box's width and height. Default
 * properties is set by global.css.appletLoadBox*/
function addAppletLoadMsg(sElemId, sLoadMsg, sStyleClass) {
  try {
    sElemId = allTrim(sElemId);
    if (sElemId === null) {
      throw new Error("The AppletLoadMsg elementId cannot be undefined.");
    }
    
    var pElem = window.document.getElementById(sElemId);
    if (pElem !== null) {
      pElem.style.display="block";
    } else {
      sLoadMsg = allTrim(sLoadMsg);
      if (sLoadMsg === "") {
        sLoadMsg = "Please wait while loading the Applet . . .";
      }

      sStyleClass = allTrim(sStyleClass);
      var sClass = "appletLoadBox";
      if (sStyleClass !== null) {
        sClass += " " + sStyleClass;
      }         

      var sHtml = "<div id='" + sElemId + "' class='" + sClass + "'>\n";
      sHtml += "  <div class='processLoader'></div>\n";
      sHtml += "  <p>" + sLoadMsg + "</p>\n";
      sHtml += "</div>\n";

      window.document.write(sHtml);
    }
  } catch (pErr) {
    alert("Globals.addAppletLoadMsg Error:\n" + pErr.message);
  }
}

/**
 * Method called by the Applet to hide the Load Message Box after the applet has been
 * loaded 
 * @param {String} pArgs the argument passed in by the Applet - the ElementID
 */
function hideAppletLoadMsg(pArgs) {
  try {
    var sElemId = null;
    if ((pArgs !== null) && (pArgs.length > 0)) {
      sElemId = allTrim(pArgs);
    }    
    if (sElemId !== null) {
      var pElem = window.document.getElementById(sElemId);
      if (pElem !== null) {
        pElem.style.display="none";
      }
    }
  } catch (pErr) { 
  }
}

/*************************************************************************************
 * Class[AppObject:Object]
 * Inheritors of this class must override the static baseClass and clasName
 * properties and must set this.inheritsFrom, and call this.inheritsFrom() and
 * this.setClassReferences(pMyClass,pBaseClass). to construct the base class and
 * set the class references in the inheritance tree.
 *************************************************************************************/
AppObject.baseClass = Object;
AppObject.className = "AppObject";
function AppObject() {
  var mpMyClass = AppObject;
  var mpBaseClass = Object;
  this.inheritsFrom = Object;
  this.inheritsFrom();
  this.Error = null;

  /* Set the instance own class and its BaseClass (i.e. the first class in the
	 * chain of class it is inheriting from)
	 * @param pMyClass = the Instance of the
	 * @param pBaseClass - the instance's base class
	 * @type void */
  this.setClassReferences = function(pMyClass,pBaseClass){
    if ((typeof(pMyClass) === "function") && (pMyClass !== null))
      mpMyClass = pMyClass;
    if ((typeof(pBaseClass) === "function") && (pBaseClass !== null))
      mpBaseClass = pBaseClass; 
  };

  /* Get the Instance's Class
	 * @type function */
  this.getClass = function(){
    return (mpMyClass === null)? Object: mpMyClass;
  };

  /* Get the Instance's Base Class (i.e. the first class in the chain of class it
	 * is inheriting from)
	 * @type function */
  this.getBaseClass = function(){
    return (mpBaseClass === null)? Object: mpBaseClass;
  };

  /* Get whether this instance has pClass in its inheritance chain
	 * @param pClass - a Base Class
	 * @type Boolean */
  this.isSubClassOf = function(pClass){
    var bResult = false;
    try {
      if ((typeof(pClass) === "function") && (pClass !== null)) {
        bResult = ((this instanceof pClass) || (mpMyClass === pClass));
        if (!bResult){
          var pBaseClass = mpBaseClass;
          while (!bResult) {
            if ((typeof(pBaseClass) !== "function") || (pBaseClass === null))
              break;

            if ((pBaseClass === pClass) || (pBaseClass.isPrototypeOf(pClass)))
              bResult = true;
            else
              pBaseClass = pBaseClass.baseClass;
          }
        }
      }
    }
    catch (pErr) {
      this.setError(pErr,"isSubClassOf");
    }
    return bResult;
  };
  /* Set an error message
	 * @param sErrMsg - can be a string or an Excepton Error
	 * @param sMethod - Name of fuction where it occur
	 * @type void */
  this.setError=function(sErrMsg,sMethod)	{
    if (sErrMsg === null)
      return;

    if (sErrMsg instanceof Error)	{
      var pEx = sErrMsg;
      this.setError(pEx.filename+"; "+pEx.linenumber+"; "+pEx.message,sMethod);
    } else if (sErrMsg !== "")  {
      if ((sMethod !== null) && (sMethod !== "")) {
        var sClass = (mpMyClass !== null)? mpMyClass.className: "";
        sClass = ((sClass === "") || (sClass === "unassigned") || (sClass === null))?
                  "": sClass+".";
        sErrMsg = sClass+sMethod + " Error:\n" + sErrMsg;
      }

      if (this.Error === null)
        this.Error = sErrMsg;
      else
        this.Error += "<br>\n"+sErrMsg;
    }
  };
  
  this.hasError = function() {
    return (this.Error !== null);
  };
}

/****************************************************************************
 * Class[AppOffsetEnums]
 ******************************************************************************/
AppPoint.baseClass = AppObject;
AppPoint.className = "AppPoint";
function AppPoint() {
  this.inheritsFrom = AppPoint.baseClass;
  this.inheritsFrom();
  this.setClassReferences(AppPoint, AppPoint.baseClass);

  /* @miXCoord @int */
  var miXCoord = null;
  /* @miYCoord @int */
  var miYCoord = null;

  /**
   * Set the Point's Coordinates
   * @param {Integer} iX the x-offset
   * @param {Integer} iY the y-offset
   * @returns {undefined}
   */
  this.setPoint = function(iX,iY) {
    this.setX(iX);
    this.setY(iY);
  };

  /**
   * Set the Point X-Coordinate
   * @param {Integer} iX the x-offset
   */
  this.setX = function(iX) {
    miXCoord = null;
    var iCoord = toInt(iX);
    if (!isNaN(iCoord)) {
      miXCoord = iCoord;
    }
  };

  /**
   * Get the Point's X-Coordinate
   * @returns {Integer}
   */
  this.getX = function() {
    return miXCoord;
  };

  /**
   * Set the Point Y-Coordinate
   * @param {Integer} iY the y-offset
   */
  this.setY = function(iY) {
    miYCoord = null;
    var iCoord = toInt(iY);
    if (!isNaN(iCoord)) {
      miYCoord = iCoord;
    }
  };

  /**
   * Get the Point's Y-Coordinate
   * @returns {Integer}
   */
  this.getY = function() {
    return miYCoord;
  };

  /**
   * Move the Point's Coordinates 
   * @param {Integer} idX the x-offset
   * @param {Integer} idY the y-offset
   */
  this.movePoint = function(idX,idY) {
    this.addToX(idX);
    this.addToY(idY);
  };

  /**
   * Add iDx to the Point's XCoord. Ignore if idX is not a valifd integer. Set miXCoord=
   * idX if the coordinate is undefined.
   * @param {Integer} idX the x-offset
   */
  this.addToX = function(idX) {
    var iAdd = toInt(idX);
    if (!isNaN(iAdd)) {
      miXCoord = (miXCoord === null)? iAdd: miXCoord+iAdd;
    }
  };

  /**
   * Add iDx to the Point's YCoord. Ignore if idY is not a valifd integer. Set miYCoord=
   * idY if the coordinate is undefined.
   * @param {Integer} idY the y-offset
   */
  this.addToY = function(idY) {
    var iAdd = toInt(idY);
    if (!isNaN(iAdd)) {
      miYCoord = (miYCoord === null)? iAdd: miYCoord+iAdd;
    }
  };

  /**
   * Check whether both corodinates are set.
   */
  this.isSet = function() {
    return ((miXCoord !== null) && (miYCoord !== null));
  };

  /**
   *Return the Point as a string
   * @type String */
  this.toString = function() {
    var sResult = "{X="+((miXCoord === null)? "null": miXCoord.toString())+";"+
    "Y="+((miYCoord === null)? "null": miYCoord.toString())+"}";
    return sResult;
  };
}

/**
 * Initiate a new point as the absolute offset of pElement
 * @param {Element} pElement the DOM element to get the top-left location of
 * @return {AppPoint} 
 */
AppPoint.getElementAbsOffset = function(pElement) {
  var pPoint = null;
  if (pElement !== null) {
    var iXOffset = pElement.offsetLeft;
    var iYOffset = pElement.offsetTop;
    //      alert("pElement.Tag="+pElement.tagName+
    //            "\npElement.id="+pElement.id+
    //            "\npElement.position="+pElement.style.position+
    //            "\npElement.offsetLeft="+pElement.offsetLeft+
    //            "\npElement.offsetTop="+pElement.offsetTop);
    var pParent = pElement.offsetParent;
    while (pParent !== null)  {
      //      alert("pParent.Tag="+pParent.tagName+
      //            "\npParent.id="+pParent.style.id+
      //            "\npParent.position="+pParent.style.position+
      //            "\npParent.offsetLeft="+pParent.offsetLeft+
      //            "\npParent.offsetTop="+pParent.offsetTop);
      if (pParent.tagName.toUpperCase() === "DIV") {
        break;
      }
      iXOffset += pParent.offsetLeft;
      iYOffset += pParent.offsetTop;
      
      pParent = pParent.offsetParent;
    }

    pPoint = new AppPoint();
    pPoint.setPoint(iXOffset,iYOffset);
  }

  return pPoint;
};

/**
 * Initiate a new point as the offset of pElement relative to its offsetParent
 * @param {Element} pElement the DOM element to get the top-left location of
 * @return {AppPoint} 
 */
AppPoint.getElementOffset = function(pElement) {
  var pPoint = null;
  if (pElement !== null) {
    var iYOffset = pElement.offsetTop;
    var iXOffset = pElement.offsetTop;
    pPoint = new AppPoint();
    pPoint.setPoint(iXOffset,iYOffset);
  }
  return pPoint;
};

/****************************************************************************
 * Class[AppOffsetEnums]
 ******************************************************************************/
function AppOffsetEnums()
{}
AppOffsetEnums.BOTTOM=0;
AppOffsetEnums.RIGHT=1;
AppOffsetEnums.LEFT=2;
AppOffsetEnums.CUSTOM=3;

/****************************************************************************
 * Public Class[AppElement]
 ******************************************************************************/
AppElement.baseClass = AppObject;
AppElement.className = "AppElement";
AppElement.count = 0;
AppElement.LastElement = null;
AppElement.BodyElement = null;
function AppElement() {
  this.inheritsFrom = AppElement.baseClass;
  this.inheritsFrom();
  this.setClassReferences(AppElement, AppElement.baseClass);
  this.objName="AppElement"+AppElement.count++;
  eval(this.objName+"=this");

  /* Placeholder of a reference to the PinButElement that can pin the popup */
  this.pinElement=null;
  /* Placeholder of a reference to the PinButElement that can unpin the popup */
  this.unpinElement=null;

  /* @msElementID @string */
  var msElementID = null;
  /* @mpParent @AppElement */
  var mpParent = null;
  /* @mpChildren @Array */
  var mpChildren = new Array();
  /* @mbIsPopup @boolean */
  var mbIsPopup = false;
  /* @mbIsPinned @boolean */
  var mbIsPinned = false;
  /* @mbIsPinable @boolean */
  var mbIsPinable = false;
  /* @meOffset @Integer */
  var meOffset = AppOffsetEnums.CUSTOM;
  /* @mpParentOffset @AppPoint */
  var mpParentOffset = null;
  /* @mpStyleTopLeft @AppPoint */
  var mpStyleTopLeft = null;
  //  /* @miXOffset @Integer */
  //  var miXOffset = null;
  //  /* @miYOffset @Integer */
  //  var miYOffset = null;
  //  /* @miStyleTop @Integer */
  //  var miStyleTop = null;
  //  /* @miStyleLeft @Integer */
  //  var miStyleLeft = null;
  /* @mbRelative @Boolean */
  var mbRelative = true;
  /* @mpScrollElement @Element */
  var mpScrollElement = null;

  /* Set the Object's Elemen's ID and initiate the Element reference - Throw
   * exceptions if sElemID is undefined or the element cannot be found.
   * @param sElemID String */
  this.setID = function(sElemID) {
    var pElem = null;
    sElemID = (sElemID == null)? "": allTrim(sElemID.toString());
    if (sElemID == "")
      throw new Error("Element ID is undefined");

    pElem = window.document.getElementById(sElemID);
    if (pElem == null)
      throw new Error("Unable to locate Element["+sElemID+"]");

    msElementID = sElemID;
    this.objName=sElemID+"_obj";
    eval(this.objName+"=this");
  }

  /* Get the Object's Elemen's ID
   * @type String */
  this.getID = function() {
    return msElementID;
  }

  /* Get the Object's Element reference
   * @type Element */
  this.getElement = function() {
    var pElem = window.document.getElementById(msElementID);
    //    if ((pElem == null)) {
    //      alert("Unable to locate Element["+msElementID+"]");
    //    }
    return pElem;
  }

  /* Set a reference this element's Parent Element
	 * @param @pParent @AppElement  */
  this.setParentElement = function(pParent) {
    if ((pParent != null) && (pParent != mpParent)) {
      var pElem = this.getElement();
      if (pElem != null) {
        var iChildZ = null;
        var pVal = (pElem.style == null)? null:pElem.style.zIndex;
        iChildZ = (pVal == null)? NaN(): toInt(pVal);

        var pParentElem = pParent.getElement();
        pVal = ((pParentElem == null) || (pParentElem.style == null))? null:
        pParentElem.style.zIndex;
        var iZorder = (pVal == null)? 0: toInt(pVal);
        if (isNaN(iZorder)) {
          iZorder = 0;
        }

        if (isNaN(iChildZ)) {
          iChildZ = iZorder+50;
        } else if (iChildZ < (iZorder+50)) {
          iChildZ = iZorder+50;
        }
        pElem.style.zIndex = iChildZ;
      }
      pParent.addChildElement(this);
      mpParent = pParent;
    }
  }

  /* Set a reference this element's Parent Element
	 * @param @sParentID @String  */
  this.setParent = function(sParentID) {
    this.setParentElement(getAppElement(sParentID));
  }

  /* Get the Object's Element reference
   * @type @AppElement */
  this.getParent = function() {
    return mpParent;
  }

  /* Add a Child AppElement tho this Element's children list and
   * return the child element reference. Assign itself as the child parent
   * @type @AppElement */
  this.addChildElement = function(pChild) {
    if (pChild != null) {
      try {
        var sChildID = pChild.getID();
        if ((sChildID != null) && (this.getChild(sChildID) == null)) {
          var iLen = mpChildren.push(pChild);
          pChild = mpChildren[iLen-1];
          if (pChild != null) {
            pChild.setParentElement(this);
          }
        }
      } catch (pExp) {
      }
    }
    return pChild;
  }

  /* Get the Child's AppElement object. If existing, add it to it children list and
   * return the child element reference. Assign itself as the child parent
   * @type AppElement */
  this.addChild = function(sChildID) {
    var pChild = getAppElement(sChildID);
    if (pChild != null) {
      pChild = this.addChildElement(pChild);
    }
    return pChild;
  }

  /* Get a child element by ID
   * @param sChildID String
	 * @type AppElement  */
  this.getChild = function(sChildID) {
    var pChild = null;
    try {
      sChildID = (sChildID == null)? "": allTrim(sChildID.toString()).toLowerCase();
      if ((sChildID != "") && (mpChildren.length > 0)) {
        for (var i=0; i < mpChildren.length; i++) {
          var pItem = mpChildren[i];
          if (pItem == null) {
            continue;
          }

          var sItemID = pItem.getID();
          if ((sItemID != null) && (sItemID.toLowerCase() == sChildID)) {
            pChild = pItem;
            break;
          }
        }
      }
    } catch (pExp) {
      alert("AppElement.getChild Error"+pExp.message);
    }

    return pChild;
  }

  /* Get whether this element has child elements
	 * @type Integer  */
  this.hasChildren = function() {
    return (mpChildren.length > 0);
  }

  /* Get the this element's number of child elements
	 * @type Integer  */
  this.getChildCount = function() {
    return mpChildren.length;
  }

  /* Called to perpaetuate the call to children to refresh their state due to a state
   * change in pParent
   * @type void */
  this.refreshChildren = function(pParent) {
    if (pParent == null) {
      return;
    }
    if ((this.hasChildren())) {
      for (var i = 0; i < mpChildren.length; i++) {
        var pChild = mpChildren[i];
        if (pChild != null) {
          try {
            pChild.onRefresh(pParent);
          } catch (pExp) {
          }
        }
      }
    }
  }

  /* Called to Hide this Element's Children
   * @type void */
  this.hideChildren = function() {
    if ((this.hasChildren())) {
      for (var i = 0; i < mpChildren.length; i++) {
        var pChild = mpChildren[i];
        if (pChild != null) {
          try {
            pChild.setVisible(false);
          } catch (pExp) {
          }
        }
      }
    }
  }

  /* Called to Unpin this Element's Popup Children
   * @type void */
  this.unpinChildren = function() {
    if (this.hasChildren() == true) {
      for (var i = 0; i < mpChildren.length; i++) {
        var pChild = mpChildren[i];
        if ((pChild != null) && (pChild.getIsPopup()==true) &&
          (pChild.getIsPinned()==true) && (pChild.unpinElement != null)) {
          try {
            fireUnpinPopup(pChild.getID());
          }catch (pExp) {
            alert("Unpin Error:\n"+pExp.message);
          }
        }
      }
    }
  }

  /* Event called by Parent to refresh the state of the children due to a state change
   * in pParent */
  this.onRefresh = function(pParent) {
    if ((pParent != null) && (pParent.getIsPopup() == true)) {
      if (this.getIsPopup() == true) {
        if ((pParent.getIsPinned() == false) && (this.getIsPinned() == true)) {
          fireUnpinPopup(this.getID());
        }
      } else if (pParent.isVisible() == false) {
        this.setVisible(false);
      }
    }
    this.refreshChildren(pParent);
  }

  /* Set the element IsPopup Flag
   * @param bIsSet boolean */
  this.setIsPopup = function(bIsSet) {
    if ((bIsSet != null) && (bIsSet == true)) {
      mbIsPopup = true;
    }
  }

  /* Get the Element's IsPopup Flag settings
   * @type Boolean */
  this.getIsPopup = function() {
    return mbIsPopup;
  }

  /* Set whether the Element is a Pinable Popup (default=false)
   * @param bIsSet boolean */
  this.setIsPinable = function(bIsSet) {
    mbIsPinable = ((bIsSet != null) && (bIsSet == true));
  }

  /* Get the Element is a Pinable Popup (default=false) 
   * @type Boolean */
  this.getIsPinable = function() {
    return mbIsPinable;
  }

  /* Set the element IsPinned Flag (if (bIsSet), set (isPinable=true)
   * @param bIsSet boolean */
  this.setIsPinned = function(bIsSet) {
    mbIsPinned = ((bIsSet != null) && (bIsSet == true));
    if (mbIsPinned == true) {
      mbIsPinable = true;
    }
    this.refreshChildren(this);
  }

  /* Get the Element's IsPinned Flag settings (always fales if !isPinable)
   * @type Boolean */
  this.getIsPinned = function() {
    return (mbIsPinned);
  }

  /* Get the Element's Position is Relative (or Absolute if false)
   * @type Boolean */
  this.isRelative = function() {
    return mbRelative;
  }

  /**
   * get a reference to the Element's StyleTopLeft point (initiate the point if not
   * previously initiated)
   * @type AppPoint */
  this.getStyleTopLeft = function() {
    if (mpStyleTopLeft == null) {
      mpStyleTopLeft = new AppPoint();
    }
    return mpStyleTopLeft; 
  }

  /* Get the this element's assigned style Top offset (i.e. style.top)
	 * @type Integer  */
  this.getStyleTop = function() {
    var pElement = this.getElement();
    if (pElement == null)
      return 0;

    var sVal = pElement.style.top;
    sVal = (sVal == null)? 0: sVal.replace("px","");
    var iResult = toInt(sVal);
    if (isNaN(iResult)) {
      iResult = (pElement.offsetTop != null)? pElement.offsetTop: 0;
    }
    return iResult;
  }

  /* Get the this element's assigned style Left offset (i.e. style.left)
	 * @type Integer  */
  this.getStyleLeft = function() {
    var pElement = this.getElement();
    if (pElement == null)
      return 0;

    var sVal = pElement.style.left;
    sVal = (sVal == null)? 0: sVal.replace("px","");
    var iResult = toInt(sVal);
    if (isNaN(iResult)) {
      iResult = (pElement.offsetLeft != null)? pElement.offsetLeft: 0;
    }
    return iResult;
  }

  /* Get the this element's assigned style right offset (i.e. left+width)
	 * @type Integer  */
  this.getStyleRight = function() {
    var pElement = this.getElement();
    if (pElement == null){
      return 0;
    }

    var sVal = pElement.style.left;
    sVal = (sVal == null)? "": sVal.replace("px","");
    var iResult = toInt(sVal);
    if (isNaN(iResult)) {
      iResult = (pElement.offsetLeft != null)? pElement.offsetLeft: 0;
    }

    var sVal2 = pElement.style.width;
    sVal2 = (sVal2 == null)? 0: sVal2.replace("px","");
    var iResult2 = toInt(sVal2);
    if (isNaN(iResult2)) {
      iResult2 = (pElement.offsetWidth != null)? pElement.offsetWidth: 0;
    }

    return iResult + iResult2;
  }

  /* Get the this element's assigned style bottom offset (i.e. top+bottom)
	 * @type Integer  */
  this.getStyleBottom = function() {
    var pElement = this.getElement();
    if (pElement == null)
      return 0;

    var sVal = pElement.style.top;
    sVal = (sVal == null)? 0: sVal.replace("px","");
    var iResult = toInt(sVal);
    if (isNaN(iResult)) {
      iResult = (pElement.offsetTop != null)? pElement.offsetTop: 0;
    }
    //alert("StyleBottom["+pElement.id+"]:\n - Style.height="+pElement.style.height+
    //      "\n - offsetHeight="+pElement.offsetHeight+
    //      "\n - clientHeight="+pElement.clientHeight);
    var sVal2 = pElement.style.height;
    sVal2 = (sVal2 == null)? 0: sVal2.replace("px","");
    var iResult2 = toInt(sVal2);
    if (isNaN(iResult2)) {
      iResult2 = (pElement.offsetHeight != null)? pElement.offsetHeight: 0;
    }

    return iResult + iResult2;
  }

  /* Get the this element's  offsetheight
	 * @type Integer  */
  this.getOffsetHeight = function() {
    var pElement = this.getElement();
    if (pElement == null)
      return 0;

    return pElement.offsetHeight;
  }

  /**
   * get the Element Top Scroll offset if mpScrollElement is assigned
   * @type Integer*/
  this.getScrollTop = function() {
    var iScroll = -1
    try {
      var pElem = (mpScrollElement == null)? null: mpScrollElement.getElement();
      if (pElem != null) {
        iScroll = toInt(pElem.scrollTop);
        if (isNaN(iScroll)) {
          iScroll = -1;
        }
      }
    }
    catch (pExp) {
      alert("AppElement.getScrollTop Error:\n"+pExp.message);
    }
    return iScroll;
  }

  /**
   * get the Element Left Scroll offset if mpScrollElement is assigned
   * @type Integer */
  this.getScrollLeft = function() {
    var iScroll = -1
    try {
      var pElem = (mpScrollElement == null)? null: mpScrollElement.getElement();
      if (pElem != null) {
        iScroll = toInt(pElem.scrollLeft);
        if (isNaN(iScroll)) {
          iScroll = -1;
        }
      }
    }
    catch (pExp) {
      alert("AppElement.getScrollLeft Error:\n"+pExp.message);
    }
    return iScroll;
  }

  /* Get the absolute top offset of this element relative to document
	 * @type Integer  */
  this.absTopOffset = function ()	{
    var pElement = this.getElement();
    if (pElement == null) {
      return 0;
    }

    var iOffset = pElement.offsetTop;
    if ((pElement.style.position != 'absolute') &&
      (pElement.style.position != 'relative') &&
      (pElement.style.position != 'fixed')) {
      try {
        var pParent = pElement.offsetParent;
        while (pParent  != null) {
         // alert("Parent["+pParent.tagName+"].offsetTop = "+pParent.offsetTop);
          if ((pParent.style.position == 'absolute') ||
            (pParent.style.position == 'relative') ||
            (pParent.style.position == 'fixed'))
            break;
          iOffset += pParent.offsetTop;
          pParent = pParent.offsetParent;
        }
      } catch (pExp) {}
    }

    //alert("Element["+pElement+"].AbsBottomOffset = "+iOffset);
    return iOffset;
  }

  /* Get the absolute left offset of this element relative to document
	 * @type Integer  */
  this.absLeftOffset = function ()	{
    var pElement = this.getElement();
    if (pElement == null)
      return 0;

    //alert("Parent["+pElement.tagName+"].offsetLeft = "+pElement.offsetLeft);
    var iOffset = pElement.offsetLeft;
    if ((pElement.style.position != 'absolute') &&
      (pElement.style.position != 'relative') &&
      (pElement.style.position != 'fixed')) {
      try {
        var pParent = pElement.offsetParent;
        while (pParent  != null) {
          //alert("Parent["+pParent.tagName+"].offsetLeft = "+pParent.offsetLeft);
          if ((pParent.style.position == 'absolute') ||
            (pParent.style.position == 'relative') ||
            (pParent.style.position == 'fixed'))
            break;
          iOffset += pParent.offsetLeft;
          pParent = pParent.offsetParent;
        }
      } catch (pExp) {}
    }
    //alert("Element["+pElement+"].AbsLeftOffset = "+iOffset);
    return iOffset;
  }

  /* Get the absolute right offset of this element relative to document
	 * @type Integer  */
  this.absRightOffset = function() {
    var pElement = this.getElement();
    if (pElement == null)
      return 0;

    var iOffset = pElement.offsetLeft + pElement.offsetWidth;
    if ((pElement.style.position != 'absolute') &&
      (pElement.style.position != 'relative') &&
      (pElement.style.position != 'fixed')) {
      try {
        var pParent = pElement.offsetParent;
        while (pParent  != null) {
          //alert("Parent["+pParent.tagName+"].offsetTop = "+pParent.offsetLeft);
          if ((pParent.style.position == 'absolute') ||
            (pParent.style.position == 'relative') ||
            (pParent.style.position == 'fixed'))
            break;
          iOffset += pParent.offsetLeft;
          pParent = pParent.offsetParent;
        }
      } catch (pExp) {}
    }
    //alert("Element["+pElement+"].AbsLeftOffset = "+iOffset);
    return iOffset;
  }

  /* Get the absolute bottom offset of this element relative to document
	 * @type Integer  */
  this.absBottomOffset = function() {
    var pElement = this.getElement();
    if (pElement == null)
      return 0;

    var iOffset = pElement.offsetTop + pElement.offsetHeight;
/*    alert("Element.position = "+pElement.style.position+
      "\npElement.tagName="+pElement.tagName+
      "\npElement.Id="+pElement.id+
      "\npElement.class="+pElement.className+
      "\niOffset="+iOffset); */
    if ((pElement.style.position != "absolute") &&
      (pElement.style.position != "relative") &&
      (pElement.style.position != "fixed")) {
      try {
        //        var pParent = pElement.offsetParent;
        var pParent = this.getOffsetParent(pElement);
    /*    alert("Parent["+pParent.tagName+"].Style.cssText = "+pParent.style.position+
          "\nParent.tagName="+pParent.tagName+
          "\nParent.Id="+pParent.id+
          "\nParent.class="+pParent.className+
          "\nParent.top="+pParent.style.top+
          "\niOffset="+iOffset); */
        while (pParent != null) {
          //alert("Parent["+pParent.tagName+"].offsetTop = "+pParent.offsetLeft);
          if ((pParent.style.position == "absolute") ||
            (pParent.style.position == "relative") ||
            (pParent.style.position == "fixed"))
            break;

          iOffset += pParent.offsetTop;
          pParent = this.getOffsetParent(pParent);
          if (pParent != null) {
           /* alert("Parent["+pParent.tagName+"].Style.cssText = "+pParent.style.position+
              "\nParent.tagName="+pParent.tagName+
              "\nParent.Id="+pParent.id+
              "\nParent.class="+pParent.className+
              "\nParent.top="+pParent.style.top+
              "\niOffset="+iOffset); */
          }
        }
        if (pParent != null) {
       /*   alert("Parent["+pParent.tagName+"].Style.cssText = "+pParent.style.position+
            "\nParent.tagName="+pParent.tagName+
            "\nParent.Id="+pParent.id+
            "\nParent.class="+pParent.className+
            "\nParent.top="+pParent.style.top+
            "\niOffset="+iOffset); */
        }
      } catch(pEx) {}
    }
    //alert("Element["+pElement+"].AbsLeftOffset = "+iOffset);
    return iOffset;
  }
 
  /**
  * Get e reference to the ParentOffset point. Initiated the point is not cuurently set.
  */
  this.getParentOffset = function() {
    if (mpParentOffset == null) {
      mpParentOffset = new AppPoint();
    }
    return mpParentOffset;
  }

  /* Set the top-left corner of the Element relative to its ParentElement based on the
   * passed parameters. If eOffset is BOTTOM, RIGHT, or LEFT the Popup will be anchored
   * to the parent's Bottom-Right, Top-Right, or Top-Left corner of the parent element.
   * if iXOffset and/or iYOffset are set the offset will be added to the parent offset
   * to move the popup to accordingly.  If bRelative the style.position is set to
   * 'relative', otherwise it is 'absolute'.  If relative, it will use the Element
   * parent style settings and a calcualte the offset, otehrwise it uses the parent's
   * absolute offsets (only applicable at view stage).
   * If eOffset=custom, the parent offsets and bRelative are ignored and the elements
   * top and left offset is set to iXOffset and iYOffset and the position='relative'.
   * It also assign an onMouseOver event to move the element to full view is necessary.
   * The method always return true.
	 * @type Boolean  */
  this.setOffset = function(eOffset, iXOffset, iYOffset, bRelative) {
    var pElem = this.getElement();
    if (pElem == null)
      return true;

    try {
      bRelative = ((bRelative != null) && (bRelative == true));
      eOffset = toInt(eOffset);
      if (isNaN(eOffset)) {
        eOffset = AppOffsetEnums.CUSTOM;
      }
      meOffset = eOffset;
      mbRelative = bRelative;
      //      alert("meOffset="+meOffset.toString()+
      //            "\niXOffset="+iXOffset+
      //            "\niYOffset="+iYOffset+
      //            "\nmbRelative="+mbRelative);

      var pParent = this.getParent();
      var pParentElem = (pParent == null)? null: pParent.getElement();
      var iLeft = 0;
      var iTop = 0;
      var iOffset = 0;
      var iScroll = 0;
      var pAbsOffset = null;
      var pElemAbsOffset = null;

      var pParentOffset = this.getParentOffset();
      if (pParentOffset.isSet() == false) {
        iYOffset = toInt(iYOffset);
        if (isNaN(iYOffset)) {
          iYOffset = 0;
        }

        iXOffset = toInt(iXOffset);
        if (isNaN(iXOffset)) {
          iXOffset = 0;
        }
        pParentOffset.setPoint(iXOffset,iYOffset);
      } else {
        iYOffset = pParentOffset.getY();
        iXOffset = pParentOffset.getX();
      }
      // alert("ParentOffset="+pParentOffset.toString());

      var pStyleTopLeft = this.getStyleTopLeft();
      if (eOffset == AppOffsetEnums.CUSTOM) {
        if (!pStyleTopLeft.isSet()) {
          if ((pParentElem != null) &&
            (pParentElem.offsetParent != pElem.offsetParent)) {

            pAbsOffset = AppPoint.getElementAbsOffset(pParentElem);
            pElemAbsOffset = AppPoint.getElementOffset(pElem.offsetParent);
            if ((pElemAbsOffset != null) && (pElemAbsOffset.isSet() == true)) {
              var idX = -1*pElemAbsOffset.getX();
              var idY = -1*pElemAbsOffset.getY();
              pAbsOffset.movePoint(idX, idY);
            }
            iLeft = pAbsOffset.getX();
            iTop = pAbsOffset.getY();
          }
          iLeft += iXOffset;
          iTop += iYOffset;
          pStyleTopLeft.setPoint(iLeft,iTop);
        } else {
          iTop = pStyleTopLeft.getY();
          iLeft = pStyleTopLeft.getX();
        }
        // alert("pStyleTopLeft="+pStyleTopLeft.toString());

        iScroll = this.getScrollTop();
        iTop = (iScroll > 0)? iTop-iScroll: iTop;
        iScroll = this.getScrollLeft();
        iLeft = (iScroll > 0)? iLeft-iScroll: iLeft;

        pElem.style.position = "relative";
        pElem.style.top = iTop;
        pElem.style.left = iLeft;
      } else if (pParentElem != null) {
        //     alert("pStyleTopLeft="+pStyleTopLeft.toString());
        if (!pStyleTopLeft.isSet()) {
          if ((mbRelative == false) || (pParentElem.offsetParent != pElem.offsetParent)) {
            pAbsOffset = AppPoint.getElementAbsOffset(pParentElem);
            //      alert("pAbsOffset="+pAbsOffset.toString());
            if ((mbRelative == true)) {
              pElemAbsOffset = AppPoint.getElementOffset(pElem.offsetParent);
              if ((pElemAbsOffset != null) && (pElemAbsOffset.isSet() == true)) {
                var idX1 = -1*pElemAbsOffset.getX();
                var idY1 = -1*pElemAbsOffset.getY();
                pAbsOffset.movePoint(idX1, idY1);
              }
            }
            iLeft = pAbsOffset.getX();
            iTop = pAbsOffset.getY();
          }

          if (eOffset == AppOffsetEnums.BOTTOM) {
            iTop += pParentElem.offsetHeight;
          } else if (eOffset == AppOffsetEnums.RIGHT) {
            iLeft += pParentElem.offsetWidth;
          }
          iLeft += iXOffset;
          iTop += iYOffset;
          pStyleTopLeft.setPoint(iLeft,iTop);
        } else {
          iTop = pStyleTopLeft.getY();
          iLeft = pStyleTopLeft.getX();
        }
        //    alert("pStyleTopLeft="+pStyleTopLeft.toString());


        iScroll = this.getScrollTop();
        iTop = (iScroll > 0)? iTop-iScroll: iTop;

        iScroll = this.getScrollLeft();
        iLeft = (iScroll > 0)? iLeft-iScroll: iLeft;

        pElem.style.position = (bRelative == true)? "relative": "absolute";
        pElem.style.top = iTop+"px";
        pElem.style.left = iLeft+"px";
//                alert("pElem.style.position="+pElem.style.position+
//                        "\npElem.style.top="+pElem.style.top+
//                      "\npElem.style.left="+pElem.style.left);
        if ((bRelative == false) && (window.onresize == null)) {
          window.onresize = new Function("AppElement.movePopups(); return true;");
        }
      }

      if (pElem.onmouseover == null) {
        pElem.onmouseover = new Function(this.objName+".moveElement(); return true;");
      }
    } catch (pExp) {
      alert(pExp.message);
    }
    return true;
  }

  /*
   * Move the element into the visible window */
  this.refreshElement = function() {
    try {
      var pOffset = this.getParentOffset();
      this.setOffset(meOffset, pOffset.getX(), pOffset.getY(), mbRelative);
    } catch (exception) {
    }
  }
  
  /*
   * Move the element into the visible window */
  this.moveElement = function() {
    try {
      var pTopLeft = this.getStyleTopLeft();
      if ((this.isVisible() == false) || (!pTopLeft.isSet())) {
        return;
      }

      var pElem = this.getElement();
      var iMaxYOffset = 0;
      var iMaxXOffset = 0;
      var pBody = window.document.body;
      if (pBody == null) {
        return;
      }

      var iDelTop = 0;
      var iDelLeft = 0;
      var iTop = pTopLeft.getY();
      var iLeft = pTopLeft.getX();
      var iScroll = this.getScrollTop();
      //      alert("Move: Top="+iTop+"; Scroll="+iScroll);
      iTop = (iScroll > 0)? iTop-iScroll: iTop;
      iScroll = this.getScrollLeft();
      iLeft = (iScroll > 0)? iLeft-iScroll: iLeft;

      var iOffsetX = getElementAbsLeft(this.getID());
      var iOffsetY = getElementAbsTop(this.getID());
      var iElemRight = iOffsetX+pElem.offsetHeight+4;
      var iElemBottom = iOffsetY+pElem.offsetWidth+4;
      //      alert("Move: iElemRight="+iElemRight+"; iElemBottom="+iElemBottom+
      //      "\nMaxOffsetX="+iMaxXOffset+"; MaxOffsetY="+iMaxYOffset);
      if (iMaxYOffset > 0) {
        if (iElemBottom > iMaxYOffset) {
          iDelTop = iMaxYOffset-iElemBottom;
        }
      }
      if (iMaxXOffset > 0) {
        if (iElemRight > iMaxXOffset) {
          iDelLeft = iMaxXOffset-iElemRight;
        }
      }
      iTop = iTop+iDelTop;
      iLeft = iLeft+iDelLeft;
      pElem.style.top  = iTop+"px";
      pElem.style.left = iLeft+"px";      
    } catch (pExp) {
      alert(pExp.message);
    }
  }

  /**
   * Called to relocate and move the window when the window is resized */
  this.onWindowResize = function() {
    if ((meOffset !=  AppOffsetEnums.CUSTOM) && (mbRelative == false)) {
      //this.setOffset(meOffset, miXOffset, miYOffset, mbRelative);
      this.moveElement();
    }
    return true;
  }

  /* Call Overload 2 passing eOffset=CUSTOM and iXOffset=iYOffset=0. Calling this
   * overload does not shift the eleemnt from if preset location.
   * @type Boolean  */
  this.showhideElement = function() {
    try {
      var bDoShow = (this.isVisible() != true);
      this.setVisible(bDoShow);
    } catch (pExp) {
      alert(pExp.message);
    }
  }

  /* Get whether the Element is currently visible.
	 * @type Boolean  */
  this.isVisible = function() {
    var pElem = this.getElement();
    return ((pElem != null) && (pElem.style.display != "none"));
  }

  /* Show or hide the Element based on bSet=true|false
   * @type Boolean */
  this.setVisible = function(bSet) {
    var pElem = this.getElement();
    bSet = ((bSet != null) && (bSet == true));
    if (pElem != null) {
      if (bSet == true) {
        if (pElem.style.display == "none") {
          if (this.getIsPopup() == true) {
            AppElement.hidePopups(this);
          }
          pElem.style.display = "block";
          this.setZeroHeight();
        }
        
        //var bIsPinable = this.getIsPinable();
        //if (bIsPinable != true) {
          window.document.onmouseup = 
                          new Function("AppElement.hidePopups(null); return false;");
        //}
      } else {
        if ((this.getIsPopup() == true) && (pElem.style.display != "none")) {
          pElem.style.display = "none";
          this.refreshChildren(this);
        //alert("pElem.top="+pElem.style.top);
        //pElem.style.top="0px";
        //alert("pElem.top(2)="+pElem.style.top);
        } else if (this.getIsPopup()!= true) {
          this.hideChildren(this);
        }
        window.document.onmouseup = null;
      }
    }
  }

  /**
   * Assign the Event[onscroll] of pElement = AppElement.movePopups */
  this.setScrollAnchor = function(pAnchor) {
    try {
      if (pAnchor != null) {
        pAnchor.addAnchorScrollEvent();
        mpScrollElement = pAnchor;
        this.moveElement();
      }
    } catch (pExp) {
      alert("AppElement.setScrollAnchor Error:\n"+pExp.message);
    }
    return true;
  }

  /* Set the Scroll Anchor Elements onscroll event
   * @type @boolean */
  this.addAnchorScrollEvent = function() {
    try {
      var pElem = this.getElement();
      if (pElem != null) {
        if (pElem.onscroll == null) {
          pElem.onscroll = new Function("onAnchorScroll('" + this.getID() 
                                      + "'); return true;");
        }
      }
    } catch (pExp) {
    }
    return true;
  }

  /**
   * Called to relocate and move the window when the window is resized */
  this.onAnchorScroll = function() {
    AppElement.hidePopups(this);
    return true;
  }

  /**
   * Set the Element's Bottom-Margin as the the ClientHeight.
   */
  this.setZeroHeight = function() { 
    try {
      var pElem = this.getElement();
      if ((this.isVisible()) && (pElem != null)) {
        var iCHeight = toInt(pElem.clientHeight);
        var iOHeight = toInt(pElem.offsetHeight);
        //alert("clientHeight="+iCHeight+"\noffsetHeight="+iOHeight);
        var iHeight = (isNaN(iCHeight))? null: iCHeight;
        //alert("pElem.style.marginBottom="+iHeight);
        iHeight = ((iHeight == null) && (!isNaN(iOHeight)))? iOHeight: iHeight;
        if (iHeight != null) {
          //alert("pElem.style.marginBottom="+iHeight);
          iHeight = (-1)*iHeight;
          pElem.style.marginBottom = iHeight+"px";
        }
      //alert("pElem.style.marginBottom="+pElem.style.marginBottom);
      }
    } catch (exception) {
    }
    return true;
  }

  /* Get Whether pObj is this Object
 * @type boolean */
  this.isEqual = function(pObj) {
    var bEqual = false;
    try {
      if ((pObj != null) && (pObj.isSubClassOf(AppElement) == true)) {
        var sObjId = pObj.getID();
        var sMyId = this.getID();
        bEqual = ((sObjId != null) && (sMyId != null) && (sObjId == sMyId));
      }
    } catch (pExp) {
      bEqual = false;
    }
    return bEqual;
  }
}

/* Abstract Method to hide all Element Objects that are set as popups */
AppElement.hidePopups = function(pCaller) {
  for (var iElem = 0; iElem <= (AppElement.count-1); iElem++) {
    var sObjID = "AppElement"+iElem;
    var pObj = null;
    try {
      eval("pObj="+sObjID);
      if ((pObj != null) && (pObj.isVisible() == true) && (pObj.getIsPopup() == true)) {
        if (pObj.isEqual(pCaller) == false) {
          if (pObj.getIsPinned() == false){
            pObj.setVisible(false);
          }
        }
      }
    } catch (ex) {
      pObj = null;
    }
  }
}

/* Abstract Method to hide all Element Objects that are set as popups */
AppElement.movePopups = function() {
  for (var iElem = 0; iElem <= (AppElement.count-1); iElem++) {
    var sObjID = "AppElement"+iElem;
    var pObj = null;
    try {
      eval("pObj="+sObjID);
      if ((pObj != null) && (pObj.isVisible() == true) && (pObj.getIsPopup() == true)) {
        pObj.onWindowResize();
      }
    } catch (ex) {
      pObj = null;
    }
  }
}

/****************************************************************************
* Class[AppOffsetEnums]
******************************************************************************/
function PopupAnchorEnums()
{}
PopupAnchorEnums.LEFTTOP=0;
PopupAnchorEnums.LEFTCENTER=2;
PopupAnchorEnums.LEFTBOTTOM=3;
PopupAnchorEnums.BOTTOMCENTER=4;
PopupAnchorEnums.RIGHTBOTTOM=5;
PopupAnchorEnums.RIGHTCENTER=6;
PopupAnchorEnums.RIGHTTOP=7;
PopupAnchorEnums.TOPCENTER=8;

/****************************************************************************
* Public Class[PinButElement]
******************************************************************************/
PinButElement.baseClass = AppElement;
PinButElement.className = "PinButElement";
PinButElement.count = 0;
function PinButElement() {
  this.inheritsFrom = PinButElement.baseClass;
  this.inheritsFrom();
  this.setClassReferences(PinButElement, PinButElement.baseClass);
  this.objName="PinButElement"+PinButElement.count++;
  eval(this.objName+"=this");

  /* @mbShowOnPin @boolean */
  var mbShowOnPin = false;
  
  /* Set the PinBut ShowOnPin state */
  this.setShowOnPin = function(bSet) {
    mbShowOnPin = ((bSet != null) && (bSet == true));
  }

  /* Always show the element - it is not a popup */
  this.showhideElement = function() {
    var pElem = this.getElement();
    if (pElem == null) {
      return true;
    }

    var pParent = this.getParent;
    var bShow = true;
    if (pParent != null) {
      bShow = (pParent.getIsPinned() == mbShowOnPin);
    }
    this.setVisible(bShow);
    return true;
  }

  /* Event called by Parent to refresh the state of the children due to a state change
 * in pParent */
  this.onRefresh = function(pParent) {
    var pMyParent = this.getParent();
    var bShow = true;
    if (pMyParent != null) {
      bShow = (pMyParent.getIsPinned() == mbShowOnPin);
    }
    this.setVisible(bShow);

    this.refreshChildren(pParent);
  }

  /* Show or hide the Element based on bSet=true|false
 * @type Boolean */
  this.setVisible = function(bSet) {
    var pElem = this.getElement();
    bSet = ((bSet != null) && (bSet == true));
    if (pElem != null) {
      if (bSet == true) {
        pElem.style.display = "block";
      } else {
        pElem.style.display = "none";
      }
    }
  }
}

/****************************************************************************
* Public Class[ForumEditorElement]
******************************************************************************/
ForumEditorElement.baseClass = AppElement;
ForumEditorElement.className = "ForumEditorElement";
ForumEditorElement.count = 0;
function ForumEditorElement() {
  this.inheritsFrom = ForumEditorElement.baseClass;
  this.inheritsFrom();
  this.setClassReferences(ForumEditorElement, ForumEditorElement.baseClass);
  this.objName="ForumEditorElement"+ForumEditorElement.count++;
  eval(this.objName+"=this");

  /* @param msElemPrefix @type String */
  var msElemPrefix = null;
  /* @param mpRequestElem @type Element */
  var mpRequestElem = null;
  /* @param mpCommentElem @type Element */
  var mpCommentElem = null;
  
   /* This function is called to initiate the sub-components of the Forum Editor box 
    * (i.e. the Request Element (hidden) and the Comment Edit (TextArea). It also locate 
    * the and two buttons ("Edit" and "Clear" and assign onclick events
    **/
  this.initComponents = function() {
    try {
      this.Error = null;
      mpRequestElem = null;
      mpCommentElem = null;
      msElemPrefix = null;
      
      var pElem = this.getElement();
      if (pElem == null) {
        throw new Error("The ForumEditor's Element is not accessible.")
      }

      var getId = function(sSuffix) {
        var sResult = (msElemPrefix == null)? "": msElemPrefix +":";
        sResult += sSuffix;
        return sResult;
      }

      if (pElem.hasChildNodes()) {
        var pChildren = pElem.childNodes;
        var pChild = null;
        for (var i = 0; i <= (pChildren.length-1); i++) {
          pChild = pChildren[i];
          var sChildId = ((pChild == null) || (pChild.id == null))? null: pChild.id;
          if (sChildId != null) {
            var iPos = sChildId.indexOf(":submitRequest");
            if (iPos > 0) {
              mpRequestElem = pChild;
              msElemPrefix = sChildId.substring(0,iPos);
              break;
            }
          }
        }
      }
      
      if (mpRequestElem != null) {
        var sEditId = getId("editCommentText");
        mpCommentElem = document.getElementById(sEditId);
        if (mpCommentElem == null) {
          throw new Error("Unable to locate Element["+sEditId+"].")
        }
      }
    } catch (pErr) {
      alert("EditFormElement.Error: \n" + pErr.message);
    }
  }    
  
  /* Button[butClearComment].onclick EventHandler
   * Set the hidden Element[submitRequest].value=1 before the Ajax request is fired 
   */
  this.onClearComment = function() {
    if (mpRequestElem != null) {
      mpRequestElem.value = "1";
    }
  }
  
  /* Button[butEditComment].onclick EventHandler
   * Set the hidden Element[submitRequest].value=2 before the Ajax request is fired 
   */
  this.onEditComment = function() {
    if (mpRequestElem != null) {
      mpRequestElem.value = "2";
    }
  }
}


/**
 * Called by the <ez:forumEditor> component to register the ForumEditorElement
 */
function onRegisterForumEditor(sForumName) {
  try {
    var sElemId = "ForumEditor_" + ((sForumName == null)? "": sForumName);
    var pAppElem = getAppElement(sElemId);
    if (pAppElem == null) {
      pAppElem = new ForumEditorElement();
      pAppElem.setID(sElemId);
      if (pAppElem.Error != null) {
        throw new Error(pAppElem.Error);
      }
      pAppElem.initComponents(sForumName);
      if (pAppElem.Error != null) {
        throw new Error(pAppElem.Error);
      }
    } else {
      pAppElem.initComponents(sForumName);
    }
  } catch (pErr) {
    window.status = "globals.onRegisterForumEditor Error: "+pErr.message;
    alert("globals.onRegisterForumEditor Error:\n"+pErr.message);
  }
}

/****************************************************************************
* Public Class[SeachBoxElement]
******************************************************************************/
SearchBoxElement.baseClass = AppElement;
SearchBoxElement.className = "SeachBoxElement";
SearchBoxElement.count = 0;
function SearchBoxElement() {
  this.inheritsFrom = SearchBoxElement.baseClass;
  this.inheritsFrom();
  this.setClassReferences(SearchBoxElement, SearchBoxElement.baseClass);
  this.objName="SearchBoxElement"+SearchBoxElement.count++;
  eval(this.objName+"=this");

  var msSearchName = null;
  /* @param mpInputElem @type Element */
  var mpInputElem = null;
  /* @param mpRecIdElem @type Element */
  var mpRecIdElem = null;
  /* @param mpRecNameElem @type Element */
  var mpRecNameElem = null;
  /* @param mpDoShowElem @type Element */
  var mpDoShowElem = null;
  /* @param mpSubMenu @type AppElement */
  var mpSubMenu = null;
  /* @param mbSelectFocus @type Boolean */
  var mbSelectFocus = false;
  /* @param mbInputFocus @type Boolean */
  var mbInputFocus = false;
  /* @param mbInputFocus @type Boolean */
  var mbInputText = null;

  /* This function is called to initiate the sub-components of the search box (i.e.
   * the TextInput, Options Submenu, and te two hidden fields ShowOptions and RecordId*/
  this.initComponents = function(sSearchName) {
    try {
      this.Error = null;
      var pElem = this.getElement();
      if (pElem == null) {
        throw new Error("The SearchBox's Element is not accessible.")
      }

      msSearchName = ((sSearchName == null) || (sSearchName == ""))? null:
                        sSearchName;
      var getId = function(sSuffix) {
        var sResult = ((sSearchName == null) || (sSearchName == ""))? "":
                        sSearchName +":";
        sResult += sSuffix;
        return sResult;
      }

      var pSrcId = "SearchOptions";
      if (msSearchName != null) {
        pSrcId += "_" + msSearchName;
      }

      if (pElem.hasChildNodes()) {
        var pChildren = pElem.childNodes;
        var pChild = null;
        for (var i = 0; i <= (pChildren.length-1); i++) {
          pChild = pChildren[i];
          var sChildId = ((pChild == null) || (pChild.id == null))? null: pChild.id;
          if (sChildId != null) {
            if (sChildId.indexOf(getId("SearchValue")) > 0) {
              mpInputElem = pChild;
              try {
                pChild.autocomplete='off';
              } catch (pErr1) {
              }
            } else if (sChildId.indexOf(getId("RecordId")) > 0) {
              mpRecIdElem = pChild;
              pChild.style.display = "none";
            } else if (sChildId.indexOf(getId("RecordName")) > 0) {
              mpRecNameElem = pChild;
            } else if (sChildId.indexOf(getId("ShowOptions")) > 0) {
              mpDoShowElem = pChild;
            } else if (sChildId.indexOf(getId("SearchOptions")) > 0) {
              mpSubMenu = new AppElement();
              mpSubMenu.setID(pChild.id);
              mpSubMenu.setIsPopup(true);
              mpSubMenu.setVisible(false);
              this.addChildElement(mpSubMenu);
            }
          }
        }
      } 

      if (mpInputElem == null) {
        alert("Unable to locate SearchBox Element["+getId("SearchValue")+"]");
      }
      if (mpRecIdElem == null) {
        alert("Unable to locate SearchBox Element["+getId("RecordId")+"]");
      }
      if (mpRecNameElem == null) {
        alert("Unable to locate SearchBox Element["+getId("RecordName")+"]");
      }
      if (mpDoShowElem == null) {
        alert("Unable to locate SearchBox Element["+getId("ShowOptions")+"]");
      }
      if (mpSubMenu == null) {
        alert("Unable to locate SearchBox Element["+getId("SearchOptions")+"]");
      }

    } catch (pErr) {
      this.setError(pErr.message, "initComponents");
    }
  }

  /**
   * Get this instance SearchName
   * @type String */
  this.getSearchName = function() {
    return msSearchName;
  }

  /**
   * Get whether this instance of SearchBoxElement's SearchName matches sSearchName
   * @type Boolean */
  this.getIsSearchBox = function(sSearchName) {
    return ((sSearchName != null) && (msSearchName != null) &&
            (sSearchName == msSearchName));
  }

  /**
   * The onclick event when a SearchBox's selected item is clicked.  It sets the
   * mpRecIdElem.value=pOptionId
   */
  this.onSelectOption = function(pOptionId) {
    try {
      if (mpRecIdElem == null) {
        throw new Error("Hidden Element[RecordId] has not been initiated.");
      }

      mpRecIdElem.value = pOptionId;
      fireClick(mpRecIdElem.id);

      if (mpSubMenu != null) {
        mpSubMenu.setVisible(false);
      }

      if (mpInputElem != null) {
        mpInputElem.focus();
      }
    }catch (pErr) {
      window.status = "globals.onClickSearchOption Error: " +pErr.message;
    }
    return true;
  }

  /* Call the setVisible method of the SubMenu - always return true.
   * @type Boolean*/
  this.setVisible = function(bShow) {
    var bDoShow = ((bShow != null) && (bShow));
    if ((mpDoShowElem = null)) {
      mpDoShowElem.value = (bDoShow)? "true": "false";
    }
    if (mpSubMenu != null) {
      mpSubMenu.setVisible(bDoShow);
      if (mpSubMenu.isVisible()) {
        mpSubMenu.setZeroHeight();
      }
    }
    this.onHideRecId(null);
    return true;
  }

  /* Return SubMenu.isvisible (or fasle if SubMenu is not initiated
   * @type Booelan*/
  this.isVisible = function() {
    var bResult = false;
    if (mpSubMenu != null) {
      bResult = mpSubMenu.isVisible()
    }
    return bResult;
  }

  /**
   * Called when the TextInpu Element recieved focus */
  this.onInputFocus = function() {
    mbSelectFocus = false;
    mbInputFocus = true;
    this.setVisible(true);
    this.onHideRecId(null);
  }

  /**
   * Called when the TextInpu Element lost focus */
  this.onInputBlur = function() {
    mbInputFocus = false;
    if (mbSelectFocus == false) {
      this.setVisible(false);
    }
    
    var sSrchValue = mpRecNameElem.value;
    var sCurValue = mpInputElem.value;
    if (sCurValue != "") {
      if ((sSrchValue != "") && (sSrchValue != sCurValue)) {
        mpInputElem.value = sSrchValue;
      }
    }    
    this.onHideRecId(null);
  }

  /**
   * Called when the TextInpu Element lost focus */
  this.onInputKeyUp = function() {
    mbInputText = mpInputElem.value;
    this.onHideRecId(null);
  }

  /**
   * Called to set whether the SelectionList has Focus
   * @param bSet Boolean */
  this.onSelectFocus = function(bSet) {
    mbSelectFocus = ((bSet != null) && (bSet));
    if ((mbSelectFocus == false) && (mbInputFocus == false)) {
      this.setVisible(false);
    }
    this.onHideRecId(null);
  }

  /**
   * Called after completion of the Ajax call to set the SubMenu's margin (i.e., if it
   * is visible)
   **/
  this.onHideRecId = function(data) {
    if ((mpSubMenu != null) && (mpSubMenu.isVisible())) {
      mpSubMenu.setZeroHeight();
    }
    if (mpRecIdElem != null) {
      mpRecIdElem.style.display = "none";
    }
  }

  /**
   * Called after completion of the Ajax call to set the SubMenu's margin (i.e., if it
   * is visible)
   **/
  this.onAjax = function(data) {
    if ((data != null) && (data.status="success")) {
      if (mpRecIdElem.value != "") {
        var sSrchValue = mpRecNameElem.value;
        if (sSrchValue != null) {
          mpInputElem.value = sSrchValue;
        }
      }
    }
    if ((data != null) && ((data.status == "complete") || (data.status == "success"))) {
      if (mpRecIdElem != null) {
          mpRecIdElem.style.display = "none";
      }
      if ((mpSubMenu != null) && (mpSubMenu.isVisible())) {
       mpSubMenu.setZeroHeight();
      }
    }
  }
}

/**
 * Called by the <ez:seachBox> component to register the SearchBoxElement
 */
function onRegisterSearchBox(sSearchName) {
  try {
    var sElemId = "SearchBox_" + ((sSearchName == null)? "": sSearchName);
    var pAppElem = getAppElement(sElemId);
    if (pAppElem == null) {
      pAppElem = new SearchBoxElement();
      pAppElem.setID(sElemId);
      if (pAppElem.Error != null) {
        throw new Error(pAppElem.Error);
      }
      pAppElem.initComponents(sSearchName);
      if (pAppElem.Error != null) {
        throw new Error(pAppElem.Error);
      }
    } else {
      pAppElem.initComponents(sSearchName);
      pAppElem.setVisible(false);
    }
  } catch (pErr) {
    window.status = "globals.onRegisterSearchBox Error: "+pErr.message;
    alert("globals.onRegisterSearchhBox Error:\n"+pErr.message);
  }
}

/**
 * Called by the <ez:fileUploadControl> component to register the FileUploadElement
 */
function onRegisterFileUploadControl(sFileUploadName, bIsLoaded) {
  try {
    sFileUploadName = ((sFileUploadName == null)? "": sFileUploadName);
    if (sFileUploadName == "") {
      throw new Error("The FileUpload Name is unassigned.")
    }
    
    var pAppElem = getAppElement(sFileUploadName);
    if (pAppElem == null) {
      pAppElem = new FileUploadElement();
      pAppElem.setID(sFileUploadName);
      if (pAppElem.Error != null) {
        throw new Error(pAppElem.Error);
      }
      pAppElem.initComponents(sFileUploadName, bIsLoaded);
      if (pAppElem.Error != null) {
        throw new Error(pAppElem.Error);
      }
    } else {
      pAppElem.initComponents(sFileUploadName, bIsLoaded);
    }
  } catch (pErr) {
    window.status = "globals.onRegisterFileUploadControl Error: "+pErr.message;
    alert("globals.onRegisterFileUploadControl Error:\n"+pErr.message);
  }
}

/****************************************************************************
* Public Class[AppElement]
******************************************************************************/
FileUploadElement.baseClass = AppElement;
FileUploadElement.className = "FileUploadElement";
FileUploadElement.count = 0;
function FileUploadElement() {
  this.inheritsFrom = FileUploadElement.baseClass;
  this.inheritsFrom();
  this.setClassReferences(FileUploadElement, FileUploadElement.baseClass);
  this.objName="FileUploadElement"+FileUploadElement.count++;
  eval(this.objName+"=this");

  /* @param msFileUploadName @type String */
  var msFileUploadName = null;
  /* @param msFileInputId @type String */
  var msFileInputId = null;
  /* @param mpTargetIdElem @type Element (Input.Hidden)*/
  var mpTargetIdElem = null;
  /* @param mpForAjaxElem @type Element (Input.Hidden)*/
  var mpForAjaxElem = null;
  /* @param mpInputRowElem @type Element (TableRow)*/
  var mpInputRowElem = null;
  /* @param mpInputElem @type Element (Input.File)*/
  var mpFileInputElem = null;
  /* @param mpOutputRowElem @type Element (TableRow)*/
  var mpOutputRowElem = null;
  /* @param mpRecNameElem @type Element (Span)*/
  var mpFileNameElem = null;
  /* @param mpDoUploadButton @type Element (Button)*/
  var mpDoUploadButton = null;
  /* @param mpDoCancelButton @type Element (Button)*/
  var mpDoCancelButton = null;
  /* @param mbDoEdit @type Boolean */
  var mbDoUpload = false;
  /* @param msCurUploadPath @type String */
  var msCurUploadPath = null;
  /* @param mbInitiated @type Boolean */
  var mbInitiated = false;
  /* @param mbIsLoaded @type Boolean */
  var mbIsLoaded = false;

  /* This function is called to initiate the sub-components of the FileUpload Control 
   * (i.e. the Table, Hidden Input, File Input, Text Output, Edit Button, and the two
   * Table rows containing the Input/Output element */
  this.initComponents = function(sFileUploadName, bIsLoaded) {
    try {
      mpTargetIdElem = null;
      mpForAjaxElem = null;
      mpDoCancelButton = null;
      mpDoUploadButton = null;
      mpFileInputElem = null;
      mpFileNameElem = null;
      mpInputRowElem = null;
      mpOutputRowElem = null;
      msCurUploadPath = null;
      mbDoUpload = false;
      mbIsLoaded = ((bIsLoaded != null) && (bIsLoaded));
      mbInitiated = true;
      
      this.Error = null;
      var pElem = this.getElement();
      if (pElem == null) {
        throw new Error("The FileUpload Control Element is not accessible.")
      }

      sFileUploadName = ((sFileUploadName == null) || (sFileUploadName == ""))? null:
                        sFileUploadName;
      if (pElem == null) {
        throw new Error("The FileUpload Name is unassigned.");
      }
      msFileUploadName = sFileUploadName;
      
      var getId = function(sSuffix) {
        var sResult = sFileUploadName +"_";
        sResult += sSuffix;
        return sResult;
      }

      var getRow = function(pTable, sRowId) {
        var pResult = null;
        var pRows = pTable.childNodes;
        var pRow = null;
        for (var iRow = 0; iRow <= (pRows.length-1); iRow++) {
          pRow = pRows[iRow];
          var sId = ((pRow == null) || (pRow.id == null))? null: pRow.id;
          var sTag = ((pRow == null) || (pRow.tagName == null))? null: pRow.tagName;
          sTag = ((sTag == null) || (sTag == ""))? null: sTag.toUpperCase();
          if ((sTag != null) && (sTag == "TR")) {
            if ((sId != null) && (sId == sRowId)) {
              pResult = pRow;
              break;
            }
          }else {
            pResult = getRow(pRow,sRowId);
            if (pResult != null) {
              break;
            }
          }
        }
        return pResult;
      }

      var getCellElem = function(pRow, sElemId) {
        var pResult = null;
        var pCells = pRow.childNodes;
        var pCell = null;
        for (var iCell = 0; iCell <= (pCells.length-1); iCell++) {
          pCell = pCells[iCell];
          var pElems = pCell.childNodes;
          for (var iElem = 0; iElem <= (pElems.length-1); iElem++) {
            pElem = pElems[iElem];
            var sId = ((pElem == null) || (pElem.id == null))? null: pElem.id;
//            var sTag = ((pElem == null) || (pElem.tagName == null))? null: pElem.tagName;
//            sTag = ((sTag == null) || (sTag == ""))? null: sTag.toUpperCase();
            if ((sId != null) && (sId.indexOf(sElemId) >= 0)) {
              pResult = pElem;
              break;
            }
          }
        }
        return pResult;
      }

      var sTargetId = getId("targetId");
      var sForAjaxId = getId("forAjax");
      var sInputRowId = getId("InputRow");
      var sOutputRowId = getId("OutputRow");
      msFileInputId = getId("FileInput");
      var sFileNameId = getId("FileName");
      var sDoUpload = getId("DoUpload");
      var sDoCancel = getId("DoCancel");

      if (pElem.hasChildNodes()) {
        var pChildren = pElem.childNodes;
        var pChild = null;
        for (var i = 0; i <= (pChildren.length-1); i++) {
          pChild = pChildren[i];
          var sChildId = ((pChild == null) || (pChild.id == null))? null: pChild.id;
          var sTag = ((pChild == null) || (pChild.tagName == null))? null: pChild.tagName;
          sTag = ((sTag == null) || (sTag == ""))? null: sTag.toUpperCase();
          //alert("Child.id = "+ sChildId +"\n\r"
           //    +"Child.tag = " + sTag);
          if ((sChildId != null) && (sChildId.indexOf(sTargetId) >= 0)) {
            mpTargetIdElem = pChild;              
          } else if ((sChildId != null) && (sChildId.indexOf(sForAjaxId) >= 0)) {
            mpForAjaxElem = pChild;              
          } else if ((sTag !=  null) && (sTag == "TABLE")) {
            mpInputRowElem = getRow(pChild,sInputRowId);
            mpOutputRowElem = getRow(pChild,sOutputRowId);
          }
        }
      } 
      
      msCurUploadPath = null;
      if (mpTargetIdElem == null) {
        alert("Unable to locate FileUpload Element["+sTargetId+"]");
        mbInitiated = false;
      }
  
      if (mpInputRowElem !=  null) {
        mpFileInputElem = getCellElem(mpInputRowElem,msFileInputId);
        if (mpFileInputElem == null) {
          alert("Unable to locate FileUpload Element["+msFileInputId+"]");
          mbInitiated = false;
        }
        
        mpDoCancelButton = getCellElem(mpInputRowElem,sDoCancel);
        if (mpDoCancelButton == null) {
          alert("Unable to locate FileUpload Element["+sDoCancel+"]");
          mbInitiated = false;
        }
      } else {
        alert("Unable to locate FileUpload Element["+sInputRowId+"]");
        mbInitiated = false;
      }

      if (mpOutputRowElem !=  null) {
        mpFileNameElem = getCellElem(mpOutputRowElem,sFileNameId);
        if (mpFileNameElem == null) {
          alert("Unable to locate FileUpload Element["+sFileNameId+"]");
          mbInitiated = false;
        }        
        
        mpDoUploadButton = getCellElem(mpOutputRowElem,sDoUpload);
        if (mpDoUploadButton == null) {
          alert("Unable to locate FileUpload Element["+sDoUpload+"]");
          mbInitiated = false;
        }
      } else {
        alert("Unable to locate FileUpload Element["+sOutputRowId+"]");
        mbInitiated = false;
      }
      
      if (mpForAjaxElem == null) {
        alert("Unable to locate FileUpload Element["+sForAjaxId+"]");
        mbInitiated = false;
      }
      
      var pAppForm = null;
      if (mbInitiated) {
        var pForm = mpTargetIdElem.form; 
        if (pForm) {
          var sFormID = pForm.id;
          pAppForm = initAppForm(sFormID);          
        }
      }
      
//      if (pAppForm) {
//        pAppForm.addFileUpload(this);
//      }
      
      if (mbInitiated) {
        this.onRefreshComponents();
      }
    }catch (pErr) {
      this.setError(pErr.message, "initComponents");
      mbInitiated = false;
    }
  }
  
  /**
  * Private method for refreshing the component settings
  **/
  this.onRefreshComponents = function(){  
    if (!mbInitiated) {
      return;
    }
    
    try {
      if (mpTargetIdElem == null) {
        throw new Error("Unable to access the TargetId Element.");
      }
      msCurUploadPath = mpFileInputElem.innerHTML;      
      msCurUploadPath = (msCurUploadPath == "")? null: msCurUploadPath;

      if (mbIsLoaded) {
        mpInputRowElem.style.display = "none";
        mpFileInputElem.disabled = "disabled";
        mpOutputRowElem.style.display = "block";
      } else {
        mpInputRowElem.style.display = "block";
        mpFileInputElem.disabled = "";
        mpOutputRowElem.style.display = "none";
      }
//      mpFileInputElem.onchange = 
//                       new Function(this.objName+".onSelectFile(); return true;");
      mpDoCancelButton.onclick = new Function(this.objName+".onCancelUpload(); return true;");      
      mpDoUploadButton.onclick = new Function(this.objName+".onDoUpload(); return true;");
      
      this.setVisible(true);
    } catch (pExp) { 
      alert(pExp.message);
    }
  }

  /**
   * Get this instance's FileUploadName
   * @type String */
  this.getFileUploadName = function() {
    return msFileUploadName;
  }

  /**
   * Get whether this instance of FileUpload Element's FileUploadName matches 
   * sFileUploadName
   * @type Boolean */
  this.getIsFileUpload = function(sFileUploadName) {
    return ((sFileUploadName != null) && (msFileUploadName != null) &&
            (sFileUploadName == msFileUploadName));
  }
  
  /**
   * The EventHandler for the Uplaod Button of the FileUpload to allow the user to select
   * a new file to upload. It Enable the FileInput Element. It hides the OutputRow and 
   * shows the InputRow. If (IsLoaded), prompt the user for confirmation to override
   * the existing file.
   **/
  this.onDoUpload = function() {
    try {
      if (mbInitiated) {
        if (mbIsLoaded) {
          var sMsg = "The current file has been uploaded to the server. " +
                "Uploading a new file will override the content " +
                "on the server. \n\r" +
                "Are you sure you want to edit the filename?";
          if ((!window.confirm(sMsg))) {
             return;
          }
          mbIsLoaded = false;
        }
        
        mbDoUpload = true;
        if (mpOutputRowElem.style.display == "block") {
          mpFileInputElem.disabled = "";
          mpOutputRowElem.style.display = "none";
          mpInputRowElem.style.display = "block";
        }
      }
    } catch (pExp) {   
      alert(pExp.message);
    }
  }
  
  /**
   * The EventHandler for the FileUpload control when the user selected to Cancel the
   * edits - it hide the Input Row and Display the Output Row and Disable the FileInput
   * Element
   **/
  this.onCancelUpload = function() {
    try {
      if (mbInitiated) {
        mbDoUpload = false;
        if (mpInputRowElem.style.display == "block") {
          mpFileInputElem.disabled = "disabled";          
          mpInputRowElem.style.display = "none";
          mpOutputRowElem.style.display = "block";
        }        
      }
    } catch (pExp) {     
    }
  }
//   
//  /**
//   * The EventHandler for the FileUpload control after the user selected a new file.
//   * It assign the FileInput Element's value to the internal msUplaodPath, assign this
//   * Path to the hidden ClientPath Element and the FileName element (for display), it
//   * hides the InputRow and show the OutputRow.
//   **/
//  this.onSelectFile = function() {
//    try {
//      if (mbInitiated) {
//        mbDoUpload = false;
//        if (mpInputRowElem.style.display == "block") {
//          msCurUploadPath = mpFileInputElem.value;
//          msCurUploadPath = allTrim(msCurUploadPath);
//          msCurUploadPath = ((msCurUploadPath == null) || (msCurUploadPath == ""))? 
//                                                    null: msCurUploadPath;
//          mpFileNameElem.innerHTML = "-";
//          mpClientPathElem.value = msCurUploadPath;
//          mpClientPathElem.style.display = "none";
//          fireClick(mpClientPathElem.id);
//          
//          mpFileInputElem.disabled = "disabled";          
//          mpInputRowElem.style.display = "none";
//          mpOutputRowElem.style.display = "block";
//        }        
//      }
//    } catch (pExp) {     
//    }
//  }
//  
//  /**
//   * Update the upLoadPath when the process was successfully completed and hide the 
//   * ClientPath element after the process was completed */
//  this.onClientPathAjax = function(data) {
//    if ((data != null) && (data.status == "success")) {
//      if (mpClientPathElem != null) {
//        msCurUploadPath = mpClientPathElem.value;
//      }
//    }
//    if ((data != null) && ((data.status == "complete") || (data.status == "success"))) {
//      if (mpClientPathElem != null) {
//        mpClientPathElem.style.display = "none";
//      }
//    }
//  }
//  
//  /**
//   * EventHandler for when the User Submit the Form with the option to uplaod the files
//   * The AppForm's onSubmit eventhander call this method to set the Form for Uploading
//   * (i.e., Enable the control and assign msUploadPath to its value).
//   **/
//  this.onUploadFile = function() {
//    try {
//      if (mbInitiated) {
//        if (msCurUploadPath != null) {
//          mpFileInputElem.disabled = "";          
//          mpFileInputElem.value = msCurUploadPath;
//          mpControlIdElem.value = msFileInputId;
//        }
//      }
//    } catch (pExp) {     
//    }
//  }
}

/*
* Call to get a previously initiated AppElement - It check if the Element's associate
* AppElement object already exist.
* @type AppElement
*/
function getAppElement(sElemID) {
  var pElem  = null;
  try {
    sElemID = (sElemID == null)? "": allTrim(sElemID.toString());
    if (sElemID == "") {
      throw new Error("Element ID is undefined");
    }
    var sObjId = sElemID+"_obj";
    try {
      eval("pElem="+sObjId);
    } catch(pExp1) {
      pElem = null;
    }
  } catch (pEx) {
    alert("globals.getAppElement Error: \n"+pEx.message);
    pElem = null;
  }
  return pElem;
}

/*
* Call to init a AppElement - It check if the Element's associate AppElement
* obejct already exist and initiate it if not existing.
* @type AppElement
*/
function initAppElement(sElemID) {
  var pElem  = null;
  try {
    pElem = getAppElement(sElemID);
    if (pElem == null) {
      pElem = new AppElement();
      pElem.setID(sElemID);
    }
  } catch (pEx) {
    alert("globals.initAppElement(1) Error: \n"+pEx.message);
  }
  return pElem;
}

///*
//* Call to init a AppElement - It check if the Element's associate AppElement
//* object already exist and initiate it if not existing.
//* @type AppElement
//*/
//function initPopupElement(sElemID,bIsPopup,bIsPinned) {
//  var pElem  = null;
//  try {
//    pElem = getAppElement(sElemID);
//    if (pElem == null) {
//      pElem = new AppElement();
//      pElem.setID(sElemID);
//      pElem.setIsPopup((bIsPopup==true));
//      pElem.setIsPinned(((bIsPopup==true) && (bIsPinned==true)));
//    }
//  } catch (pEx) {
//    alert("globals.initAppElement(2) Error: \n"+pEx.message);
//  }
//  return pElem;
//}

/*
* Call to init a AppElement - It check if the Element's associate AppElement
* obejct already exist and initiate it if not existing.
* @type AppElement
*/
function initPopupElement(sElemID, bIsPopup, bIsPinned, sParentID, eOffset, 
                            iXOffset, iYOffset, bRelative) {
  var pElem  = null;
  try {
    //alert("globals.initAppElement.ElemID(1)="+sElemID);
    pElem= getAppElement(sElemID);
    //alert("globals.initAppElement.ElemID(2)="+sElemID);
    if (pElem == null) {
      pElem = new AppElement();
      pElem.setID(sElemID);
      pElem.setIsPopup((bIsPopup==true));
      pElem.setIsPinned(((bIsPopup==true) && (bIsPinned==true)));
      var pParent = initAppElement(sParentID);
      if (pParent != null) {
        pElem.setParentElement(pParent)
      }
    }
    pElem.setOffset(eOffset, iXOffset, iYOffset, bRelative);
  } catch (pEx) {
    alert("globals.initPopupElement Error: \n"+pEx.message);
  }
  return pElem;
}

/*
* Call to show/hide Element[sElemID] typically assigned to an onClick or
* onMouseOver event of the host element. If Element[sElemID] is visible it will
* be hide or if not visible it will be shown relative to its Host
* Element[sHostID] based on the AppOffsetEnum[eOffset] and the iXOffset.
* It will automatically assign an onMousOver event to the sElemID that will move
* the Element[sElemID] into full visible range.
* @type bool
*/
function onShowHideElement(sElemID,sHostID,eOffset,iXOffset,iYOffset,bDisabled){
  var bSuccess = true;
  try {
    var bShow = ((bDisabled == null) || (bDisabled == false))
    if (bShow) {
      var pElem = initPopupElement(sElemID, true, false, sHostID, eOffset, 
                                                          iXOffset, iYOffset, false);
      if (pElem != null) {
        pElem.showhideElement();
      } else {
        throw Error("Unable to locate Element["+sElemID+"]");
      }
    }
  }
  catch (pEx) {
    bSuccess = false;
    alert("globals.onShowHideElement Error: \n"+pEx.message);
  }
  return bSuccess;
}

/*
* Call to init a AppElement - It check if the Element's associate AppElement
* object already exist and initiate it if not exist.
* @type PinButElement
*/
function initPinButElement(sElemID, bShowOnPin) {
  var pElem = null;
  try {
    pElem = getAppElement(sElemID);
    if (pElem == null) {
      pElem = new PinButElement();
      try {
        pElem.setID(sElemID);
      } catch (pExp1) {
      }
      pElem.setShowOnPin((bShowOnPin == true));
    }
  }catch (pEx) {
    alert("globals.initPinButElement Error: \n"+pEx.message);
  }
  return pElem;
}

/**
* onShowHidePinnedElement is called by an elements mouseover or onclick event to
* display/hide a popup window.  The first time it is called, it wuill initiate the
* Pinned Element as a AppElement (i.e. calling initAppElement) and initiate the
* PinButElements too. Once initiated, it will throttle the Element's visibility
* if not pinned.
*/
function onShowHidePinnedElement(sElemID,sHostID, sPinButID, sUnPinButID, bIsPinned,
  eOffset,iXOffset,iYOffset,bRelative) {
  var bSuccess = true;
  try {
    var pHost = getAppElement(sHostID);
    if ((pHost != null) && (pHost.getIsPopup() == true)) {
      pHost.unpinChildren();
      AppElement.hidePopups(pHost);
    }
    
    //alert("ElemID="+sElemID+"\nHostID="+sHostID);
    var pElem = initPopupElement(sElemID, true, (bIsPinned==true), sHostID, eOffset, 
                                                        iXOffset, iYOffset, bRelative);
    if (pElem != null) {
      pElem.setIsPinable(true);      

      if (sPinButID != null) {
        var pPinBut = pElem.getChild(sPinButID);
        if (pPinBut == null) {
          pPinBut = initPinButElement(sPinButID,false);
          if (pPinBut != null) {
            pElem.addChildElement(pPinBut);
          }
        }
        if (pPinBut != null) {
          pPinBut.onRefresh(pElem);
          pElem.pinElement = pPinBut;
        }
      }

      if (sPinButID != null) {
        var pUnPinBut = pElem.getChild(sUnPinButID);
        if (pUnPinBut == null) {
          pUnPinBut = initPinButElement(sUnPinButID,true);
          if (pUnPinBut != null) {
            pElem.addChildElement(pUnPinBut);
          }
        }
        if (pUnPinBut != null) {
          pUnPinBut.onRefresh(pElem);
          pElem.unpinElement = pUnPinBut;
        }
      }

      if ((pElem.getIsPinned() == false) || (pElem.isVisible() == false)) {
        pElem.showhideElement();
      }
    } else {
      throw Error("Unable to locate Element["+sElemID+"]");
    }
  }
  catch (pEx) {
    bSuccess = false;
    alert("globals.onShowHidePinnedElement Error: \n"+pEx.message);
  }
  return bSuccess;
}

/**
* onShowHidePinnedElement is called by an elements mouseover or onclick event to
* display/hide a popup window.  The first time it is called, it wuill initiate the
* Pinned Element as a AppElement (i.e. calling initAppElement) and initiate the
* PinButElements too. Once initiated, it will throttle the Element's visibility
* if not pinned.
*/
function onUpdatePinnedElement(sElemID,sHostID, sPinButID, sUnPinButID, bIsPinned,
  eOffset,iXOffset,iYOffset,bRelative) {
  var bSuccess = false;
  try {
    var pElem = getAppElement(sElemID);
    if (pElem == null) {
      onShowHidePinnedElement(sElemID, sHostID, sPinButID, sUnPinButID, bIsPinned,
                              eOffset, iXOffset, iYOffset, bRelative);
    } else if (pElem.getIsPopup() == true) {
      pElem.refreshElement();
      AppElement.LastElement = pElem;
      onUpdateMapView();
    }
  } catch (pExp) {
    alert("globals.onUpdatePinnedElement Error:\n"+pExp.message);
  }
  return bSuccess;
}
/*
* Call to Throttle Element[sElemID]'s Pinned state. If changing to Pinned=false, hide
* the element if visible.
* @type bool
*/
function onPinPopup(sElemID,sStateElemId){
  try {
    var pElem = getAppElement(sElemID);
    if ((pElem != null) && (pElem.getIsPopup() == true)) {
      var pStateElem = document.getElementById(sStateElemId);
      var sIsPinned = (pStateElem == null)? null: pStateElem.value;
      //      alert("Cur isPinned="+sIsPinned);
      var bIsPinned = ((sIsPinned == null) || (sIsPinned == ""))? false: (sIsPinned != "true");
      //      alert("New isPinned="+bIsPinned);
      pElem.setIsPinned(bIsPinned);
      if ((pElem.getIsPinned() == false)) {
        pElem.setVisible(false);
        bIsPinned = false;
      } else {
        bIsPinned = true;
        var pParent = pElem.getParent();
        var pParentElem = (pParent == null)? null: pParent.getElement();
        if (pParentElem != null) {
          pParentElem.onmouseover();
        }
      }

      if (pStateElem != null) {
        pStateElem.value = (bIsPinned == true)? 'true': 'false';
      }
    }
  }
  catch (pEx) {
  //  alert("onPinPopup Error: \n"+pEx.message);
  }
  return true;
}

/*
* Call to Hide Element[sElemId] - if the element is a popup and visible and either
* bForce=true or the element is not Pinned.
* @type bool
*/
function onHidePopup(sElemID,sStateElemId,bForce){
  try {
    var pElem = getAppElement(sElemID);
    if ((pElem != null) && (pElem.getIsPopup() == true)) {
      var pParent = pElem.getParent();
      var pStateElem = document.getElementById(sStateElemId);
      var sIsPinned = (pStateElem == null)? null: pStateElem.value;
      var bIsPinned = (((bForce != null) && (bForce == true))) ||
      (((sIsPinned == null) || (sIsPinned == ""))?
        false: (sIsPinned != "true"));

      if ((bIsPinned == true) || (pElem.getIsPinned() == true)) {
        pElem.setIsPinned(false);
      }

      if (pElem.getIsPinned() == false) {
        bIsPinned = false;
        pElem.hideChildren();
        pElem.setVisible(false);
        pElem.refreshChildren(pElem);
        AppElement.LastElement = pElem;
      }

      if (pStateElem != null) {
        pStateElem.value = (bIsPinned == true)? 'true': 'false';
      }
    }
  }
  catch (pEx) {
    alert("globals.onHidePopup Error: \n"+pEx.message);
  }
  return false;
}

///*
//* Call to Hide Element[sElemId] - if the element is a popup and visible and either
//* bForce=true or the element is not Pinned.
//* @type bool
//*/
//function onHidePopup_old(sElemId,sStateElemId,bForce){
//  try {
//    AppElement.hidePopups(null);
//    var pElem = getAppElement(sElemId);
//    if ((pElem != null) && (pElem.getIsPopup() == true)) {
//      var bPinned = false;
//      var bVisible = (pElem.isVisible() == true);
//      if (bVisible == true) {
//        bPinned = (((bForce != null) && (bForce == true)) ||
//          (pElem.getIsPinned() == true));
//
//        if (bPinned == true) {
//          pElem.setIsPinned(false);
//
//          var pStateElem = document.getElementById(sStateElemId);
//          if (pStateElem != null) {
//            pStateElem.value = (pElem.getIsPinned() == false)? 'false': 'true';
//          }
//        }
//        pElem.setVisible(false);
//        pElem.refreshChildren(pElem);
//      }
//    }
//  }
//  catch (pEx) {
//    alert("onHidePopup Error: \n"+pEx.message);
//  }
//  return true;
//}

function onPinButRefresh(pData, sParentId) {
  var sStatus = (pData == null)? "Undefined": pData.status.toLowerCase();
  if (sStatus == "success") {
    var pElem = getAppElement(sParentId);
    if (pElem != null) {
      pElem.refreshChildren(pElem);
    }
  }
}

/*
* If sElemID is a registered AppElement with a pinElement, get the pinElement's
* Element and fire it's click event. */
function firePinPopup(sElemId) {
  try {
    var pElem = getAppElement(sElemId);
    if ((pElem != null) && (pElem.pinElement != null) && (pElem.getIsPopup() == true)
      && (pElem.getIsPinned() == false)) {
      var pBut = pElem.pinElement.getElement();
      if (pBut != null) {
        fireClick(pBut.id);
      }
    }
  } catch (pEx) {
  }
  return true;
}

/*
* If sElemID is a registered AppElement with a unpinElement, get the unpinElement's
* Element and fire it's click event. */
function fireUnpinPopup(sElemId) {
  try {
    var pElem = getAppElement(sElemId);
    if ((pElem != null) && (pElem.unpinElement != null) && (pElem.getIsPopup() == true)
      && (pElem.getIsPinned() == true)) {
      var pBut = pElem.unpinElement.getElement();
      if (pBut != null) {
        fireClick(pBut.id);
      }
    }
  }catch (pEx) {
  }
  return true;
}

/**
 * Fire Element[sElemId]'s Click Event.
 * If the navigator="Gecko" call simulateClick(sElemId)
 */
function fireClick(sElemId) {
  try {
    var pElem = document.getElementById(sElemId);
    if (pElem != null) {
      var pNav = window.navigator;
      if (window.navigator.product == "Gecko") {
        simulateClick(pElem.id);
      } else {
        pElem.click();
      }
    }
  } catch (exception) {
  }
  return true;
}
/**
 * For Gecko product - simulate the Click Event of non-input elements
 **/
function simulateClick(sElemId) {
  try {
    var pElem = document.getElementById(sElemId);
    if (pElem != null) {
      var pEvent = document.createEvent("MouseEvents");
      pEvent.initMouseEvent("click", true, true, window,
        0, 0, 0, 0, 0, false, false, false, false, 0, null);
      pElem.dispatchEvent(pEvent);
    }
  } catch (exception) {
  }
}

/**
* Set the rtelationship between sElemID and its scroll anchor and set
* Element[sAnchorId]'s onscroll event.
*/
function setAnchorScrollEvent(sElemId, sAnchorId) {
  try {
    var pElem = getAppElement(sElemId);
    var pAnchor = initAppElement(sAnchorId);
    if ((pElem != null) && (pAnchor != null)) {
      if (pElem.getIsPopup() == true){
        pElem.setScrollAnchor(pAnchor);
      }
    }
  }catch (pEx) {
  }
}

/**
* if AppEleemnt[sElemId] exists, fire moveElement() method.
*/
function onAnchorScroll(sElemId) {
  //  alert("onScrollPopup.ElemId(1)="+sElemId);
  var pElem = getAppElement(sElemId);
  if (pElem != null) {
    pElem.onAnchorScroll();
  }
}

/**
* Locate AppElement[sElemID]. If exist and isPopup call setHideEvent() to set the
* elements onmouseout.
*/
function setHideElementEvent(sElemId) {
  try {
    var pElem = getAppElement(sElemId);
    if (pElem != null) {
      if (pElem.getIsPopup() == true){
        //alert("setHideElementEvent.ElemId="+sElemId);
        pElem.setHideEvent();
      }
    }
  } catch (pEx) {
  }
}

/**
* Locate AppElement[sElemID]. If exist and isPopup, call setVisibel(false)
*/
function onHideElement(sElemId) {
  try {
    var pElem = getAppElement(sElemId);
    if (pElem != null) {
      if (pElem.getIsPopup() == true){
        pElem.setVisible(false);
      }
    }
  } catch (pEx) {
  }
}

/**
 * Update the Element[sElemId]'s Margin to zero out it height
 */
function onUpdateMargin(sElemId) {
  try {
    var pElem = getAppElement(sElemId);
    if (pElem != null) {
      if (pElem.getIsPopup() == true){
        pElem.setZeroHeight();
      }
    }
  } catch (pEx) {
  }
}

/**
 * Update Map the elements on the map and hide all unpinned popups
 */
function onUpdateMapView() {
  try {
    if (AppElement.BodyElement == null) {
      AppElement.BodyElement = "plPagebody";
    }
    var pElem = AppElement.LastElement;
    if (pElem != null) {
      //alert("LastElemId="+pElem.getID());
      try {
        var pParent = pElem.getParent();
        var pParentElem = (pParent == null)? null: pParent.getElement();
        if (pParentElem != null) {
          var sMenuId = pParentElem.id+"_ActionMenu";
          var pMenuElem = window.document.getElementById(sMenuId);
          if (pMenuElem != null) {
            fireClick(sMenuId);
            if (pElem.pinElement != null) {
              pElem.pinElement.onRefresh(pElem);
            }
            if (pElem.unpinElement != null) {
              pElem.unpinElement.onRefresh(pElem);
            }
          //alert("fired Click["+sMenuId+"]");
          } else if (pParentElem.onmouseover != null) {
            pParentElem.onmouseover();
          //alert("fired MouseOver["+pParentElem.id+"]");
          }
        } else {
          pElem.refreshElement();
        }
      } finally {
        AppElement.LastElement = null;
      }
    }

    AppElement.hidePopups(null);
  } catch (exception) {
  }
  return true;
}
/****************************************************************************
* Class[AppSubmitEnums]
******************************************************************************/
function AppSubmitEnums()
{}
AppSubmitEnums.YES      = 0x0001; //1
AppSubmitEnums.NO       = 0x0002; //2
AppSubmitEnums.CANCEL   = 0x0004; //4
AppSubmitEnums.BACK     = 0x0008; //8

AppSubmitEnums.YESNEW   = 0x0011; //17
AppSubmitEnums.ADDNEW   = 0x0021; //33
AppSubmitEnums.DELETE   = 0x0041; //65
AppSubmitEnums.EDIT     = 0x0081; //129
AppSubmitEnums.FIND     = 0x0101; //257
AppSubmitEnums.TEST     = 0x0201; //513
AppSubmitEnums.CALC     = 0x0401; //1025
AppSubmitEnums.REFRESH  = 0x0801; //2049
AppSubmitEnums.CUSTOM1  = 0x1001; //4097
AppSubmitEnums.CUSTOM2  = 0x2001; //8193
AppSubmitEnums.CUSTOM3  = 0x4001; //16385
AppSubmitEnums.CUSTOM4  = 0x8001; //32769

AppSubmitEnums.SKIP     = 0x0202; //514
AppSubmitEnums.SUSPEND  = 0x0402; //1026

AppSubmitEnums.RESET    = 0x1000; //4096
AppSubmitEnums.CONFIRM  = 0x1000; //8192
AppSubmitEnums.REJECT   = 0x1000; //16384
AppSubmitEnums.RELOAD   = 0x8000; //32768

/****************************************************************************
* Public Class[AppElement]
******************************************************************************/
AppForm.baseClass = AppObject;
AppForm.className = "AppForm";
AppForm.count = 0;
AppForm.SubmitBack = null;
AppForm.PageId = null;
AppForm.ActionTarget = null;
AppForm.MenuFormId = null;
AppForm.OnNavigateId = null;
AppForm.NoNavigation = null;
function AppForm() {
  this.inheritsFrom = AppForm.baseClass;
  this.inheritsFrom();
  this.setClassReferences(AppForm, AppForm.baseClass);
  this.objName="AppForm"+AppForm.count++;
  eval(this.objName+"=this");

  /* @msElementID @type String */
  var msElementID = null;
  /* @msDefButtonID @type String */
  var msDefButtonID = null;
  /* @meDefState @type int */
  var meDefState = AppSubmitEnums.RELOAD;
  /* @msStateElemID @type String */
  var msStateElemID = null;
  /* @mbDoEsc @type Boolean*/
  var mbDoSubmitOnEnter = true;
  /* @mbDoEsc @type Boolean*/
  var mbDoEsc = false;
  /* @mbNoBackAction @type Boolean (default= false)*/
  var mbNoBackAction = false;
  /* @mbpFormMaskID @type String*/
  var msFormMaskID = null;
  /* @mpFormMaskSubmits @type String*/
  var msFormMaskSubmits = null;
  /* @mbPendingAjax @type Boolean */
  var mbPendingAjax = false;
  /* @mbPendingSubmit @type Boolean */
  var mbPendingSubmit = false;
  
  /* Set the Object's Elemen's ID and initiate the Element reference - Throw
 * exceptions if sElemID is undefined or the element cannot be found.
 * @param sElemID String */
  this.setID = function(sElemID) {
    var pElem = null;
    sElemID = (sElemID == null)? "": allTrim(sElemID.toString());
    if (sElemID == "") {
      throw new Error("Form ID is undefined");
    }

    pElem = window.document.getElementById(sElemID);
    if (pElem == null) {
      throw new Error("Unable to locate Form["+sElemID+"]");
    }

    if (pElem.tagName.toUpperCase() != "FORM") {
      throw new Error("Invalid Element[" + sElemID +
        "].Tag. Expected a 'FORM', got a '" + pElem.tagName+"'");
    }

    msElementID = pElem.id;

    this.objName= makeObjectID(sElemID, "AppForm_", null);
    eval(this.objName+"=this");

    pElem.onkeypress = new Function("return "+this.objName+".onKeyPress(event);");
    pElem.onsubmit = new Function(" return " + this.objName + ".onSubmitEvent();");  
    //alert("Form.onsubmit=" + pElem.onsubmit);
  }
  
  /* Get the Object's Elemen's ID
   * @type String */
  this.getID = function() {
    return msElementID;
  }

  /* Get the Object's Element reference
   * @type Element */
  this.getElement = function() {
    var pElem = null;
    if (msElementID) {
      pElem = window.document.getElementById(msElementID);
    }
    return pElem;
  }

  /* 
   * Set Form's Defaulf Submit Button
   */
  this.setDefaultButton = function(sButtonID, eDefState) {    
    msDefButtonID = null;
    meDefState = AppSubmitEnums.RELOAD;    
    
    //alert("eState="+eState);
    if ((eDefState==null) || (isNaN(eDefState))) {
      eDefState = AppSubmitEnums.RELOAD;
    } else {
      eDefState = toInt(eDefState);
      if ((eDefState & 0xFFFF) == 0) {
        eDefState = AppSubmitEnums.RELOAD;
      }
    }

    var pElem = getFormInputElement(this.getID(), sButtonID, "SUBMIT");
    if (pElem != null) {
      msDefButtonID = pElem.id;
    }

    meDefState = eDefState;
  }
  
  /* Get the Forms's default ButtonElement ID
   * @type String */
  this.getDefaultButtonID = function() {
    return msDefButtonID;
  }

  /* Get the Forms's default ButtonElement reference
   * @type Element */
  this.getDefaultButton = function() {
    var pElem = null;
    if (msDefButtonID) {
      pElem = window.document.getElementById(msDefButtonID);
    }
    return pElem;
  }
  

  /* Initiate the FormMask Element and the Sting of FormSubmits that will trigger a
   * Form mark on click */
  this.setFormMask = function(sMaskId, sMaskSubmits) {
    msFormMaskID = null;
    msFormMaskSubmits = null;
    try {
      sMaskSubmits = allTrim(sMaskSubmits);
      if ((sMaskSubmits != null) && (sMaskSubmits != "")) {
        sMaskSubmits = sMaskSubmits.replace(/,/g,"},{");
        msFormMaskSubmits = "{" + sMaskSubmits + "}";        
        if ((sMaskId != null) && (sMaskId != "")) {
          if (window.document.getElementById(sMaskId) != null) {
            msFormMaskID = sMaskId;
          }
        }
      }
    } catch(pErr) {
      msFormMaskID = null;
      msFormMaskSubmits = null;
      alert("AppForm.setFormMask Error\n "+pErr.message);
    }
  }

  /* Set the Form's Submit State Element
 */
  this.setStateElement = function(sStateId) {
    msStateElemID = null;    
    var pElem = getFormInputElement(this.getID(), sStateId, "HIDDEN");
    if (pElem != null) {
      msStateElemID = pElem.id;
    }
  }

  /* Get the Forms's default State Element ID
 * @type String */
  this.getStateElementID = function() {    
    return msStateElemID;
  }

  /* Get the Forms's default State Element reference
 * @type Element */
  this.getStateElement = function() {
    var pElem = null;
    if (msStateElemID != null) {
      pElem = window.document.getElementById(msStateElemID);
    }
    return pElem;
  }

  /* Get the Form's Hidden Element[sElemId]}
 */
  this.getHiddenElement = function(sElemId) {
    var pElem = null;    
    try {
      pElem = getFormInputElement(this.getID(), sElemId, "HIDDEN");
    } catch (pErr) {
      alert(pErr.message);
    }
    return pElem;
  }

  /*
 * Set the DoSubmitOnEnter flag. If set the submit button's click will be fired if the
 * user press "Enter" any place on the form
 */
  this.setDoSubmitOnEnter = function(bSet) {
    mbDoSubmitOnEnter = ((bSet == null) || (bSet));
  }

  /*
 * Set the DoEsc flag - turn on the falg that allow user's to cancel by pressing the
 * escape key
 */
  this.setDoEsc = function() {
    mbDoEsc = true;
  }
  
  /* Set or reset the PendAjax Flag (to prevent Submits from being fired */
  this.setPendingAjax = function(bSet) {
    mbPendingAjax = ((bSet == null) || (bSet));
    if ((!mbPendingAjax) && (mbPendingSubmit))  {
      var pBut = this.getDefaultButton();
      if (pBut) {
        // alert("Form["+msElementID+"] Simulated Submit click");
        pBut.disabled = false;
        //pBut.click();
        fireClick(pBut.id);
        //alert("Form["+msElementID+"] simulated Submit Button["+pBut.id+"].click");
        AppForm.SubmitBack = false;
      }
    }
    //alert("Form["+msElementID+"] PendingAjax="+mbPendingAjax);
  }
  
  /* Handles the Form.onsubmit event. It sets the SubmitState=defaultState if currently
   * RELOAD. If the SubmitState != (CANCEL|RELOAD|BACK), and the FormMask is set,
   * display the FormMask with the Processing Message. If (mbPendingAjax) reload the 
   * form instead.
   */
  this.onSubmitEvent = function() {
    var pState = this.getStateElement();
    var eState = AppSubmitEnums.RELOAD;
    if (mbPendingAjax) {
      mbPendingSubmit = true;
      return false;
    } else {
      mbPendingSubmit = false;
    }
    
    //alert("Form["+msElementID+"] pState.value="+pState.value);
    if (pState)  {
      if ((pState.value == AppSubmitEnums.RELOAD) && (!mbPendingAjax)) {
        pState.value = meDefState;
        AppForm.SubmitBack = false;
      }
      eState = pState.value;
      //alert("Form["+msElementID+"] pState.value="+pState.value);
    }
    
    Globals.stopSessionTimer();
    this.onShowFormMask();
    return true;
  }
  
  /* EventHandler for displaying the Mask if the Submit is called */
  this.onShowFormMask = function() {
    try {
      var pState = this.getStateElement();
      var eState = AppSubmitEnums.RELOAD;
      if (pState)  {
        eState = pState.value;
      }

      if ((msFormMaskID == null) || (msFormMaskSubmits == null)) {
        return;
      }

      var pMaskElem = window.document.getElementById(msFormMaskID);
      if (pMaskElem == null) {
        return;
      }

      var sSubmit = "{" + eState.toString() + "}";    
      var bShowMask = (msFormMaskSubmits.indexOf(sSubmit) >= 0);
  //    alert("Submit="+sSubmit+"\n\r"+
  //          "FormMaskSubmits="+msFormMaskSubmits+"\n\r"+
  //          "ShowMask="+bShowMask);
      if (bShowMask) {
        pMaskElem.style.width = "100%";
        pMaskElem.style.height = "100%";
        pMaskElem.style.display="block";
      }
    } catch (pErr) {      
    }
  }
  
  /* The onKeyPress event handler for the form that will trigger the
 * click event for the default button. Works for EI and Firefox.
 */
  this.onKeyPress = function(pEvent) {
    var bResult = true;
    var iKeyCode = 0;
    if (pEvent) {
      if (pEvent.charCode)
        iKeyCode =pEvent.charCode;
      else if (pEvent.keyCode)
        iKeyCode = pEvent.keyCode;
      else
        iKeyCode = pEvent.which;
    } else if (window.event) {
      iKeyCode = window.event.keyCode;
    }

    if ((iKeyCode == 13) && (mbDoSubmitOnEnter)) {
      this.onSubmit(meDefState);
      bResult = false;      
    } else if ((iKeyCode == 27)  && (mbDoEsc)) {
      this.onSubmit(AppSubmitEnums.CANCEL);
      bResult = false;
    }
    return bResult;
  }
  
  /*
 * Set the StateElement.value=eState and for the DefaultButton.click
 */
  this.onSubmit = function(eState) {
    if ((eState == null) || (eState == 0)) {
      eState = AppSubmitEnums.RELOAD;
    }
    
    if (!mbPendingAjax) {
      var pState = this.getStateElement();
      if (pState) {
        pState.value = eState;
      }
       
      if (eState == AppSubmitEnums.RELOAD) {
        var pForm = this.getElement();
        alert("Form["+pForm+"] simulated Form.Submit");
        pForm.submit();
      } else {
        var pBut = this.getDefaultButton();
        if (pBut) {
          // alert("Form["+msElementID+"] Simulated Submit click");
          pBut.disabled = false;
          //pBut.click();
          fireClick(pBut.id);
          //alert("Form["+msElementID+"] simulated Submit Button["+pBut.id+"].click");
          AppForm.SubmitBack = false;
        }
      }
    }    
    return true;
  }
  
  /**
   * Called by the toolbar to assign the goBack event to the body.onunload event 
   */
  this.setNavigationAction = function() {
    var bNoBackAction = 
                     ((AppForm.NoNavigation != null) && (AppForm.NoNavigation == true));
    if (bNoBackAction) {
      var pGoBack = new Function(this.objName+".goBack();"); 
      appendWindowOnUnloadEvent(pGoBack);
      AppForm.SubmitBack = true;
    }    
  }
   
  /**
   * Called by the window.onunload event to fire the Submit[Back]
   */
  this.goBack = function() {
//    if ((AppForm.SubmitBack == true) && (AppForm.NoNavigation)) {
//      alert("Go back to Page["+AppForm.PageId+"].");
//      window.history.forward();
//    } 
    AppForm.SubmitBack = true;
  } 
}

/**
 * Called by the MenuForm on initiation to fire its Button[butOnNavigate]'s click event,
 * which will trigger an Ajax call to the serve.
 */
function onBrowserNavigationEvent() {
  try {
    var pActTrgElem = getFormInputElement(AppForm.MenuFormId, "PageActTrg", "HIDDEN");
    if (pActTrgElem != null) {
      pActTrgElem.value = AppForm.ActionTarget;
    }

    var pJREElem = getFormInputElement(AppForm.MenuFormId, "JavaVersions", "HIDDEN");
    if (pJREElem != null) {
      pJREElem.value = Globals.javaVersions();
    }

    var pJSElem = getFormInputElement(AppForm.MenuFormId, "JSEnabled", "HIDDEN");
    if (pJSElem != null) {
      pJSElem.value = true;
    }

    var sButId = "butOnNavigate";
    var pElem = getFormInputElement(AppForm.MenuFormId, sButId, "BUTTON");
    if (pElem != null) {
      var sElemId = pElem.id;
      if (sElemId != null) {
        //alert("Firing onBrowserNavigationEvent");
        fireClick(sElemId);
      }
    }
  } catch (pErr) {
    alert("globals.onBrowserNavigationEvent.Error: \n" + pErr.message);
  }
}

/**
 * The Ajax response handler for the MenuForm.Button[butOnNavigate].click event. It
 * check the form's Hidden[redirectInc].value and value=0-no action, negative=call
 * history.back(1) else call history.forward(1).
 */
function onBrowserNavigationRedirect(pData) {
  var sStatus = (pData == null)? "Undefined": pData.status.toLowerCase();
  if (sStatus == "success")  {
    try {
      var sRedirectUrl = null;
      var sElemId = "RedirectUrl";
      var pElem = getFormInputElement(AppForm.MenuFormId, sElemId, "HIDDEN");
      if (pElem != null) {
        sRedirectUrl = pElem.value;
        if ((sRedirectUrl != null) && (sRedirectUrl != "")) {
          //alert("redirect to '"+sRedirectUrl+"'.");
          window.open(sRedirectUrl,"_self","toolbar=0",false);
        } else {
          Globals.resetSessionTimer();
        }
      } else {
        throw new Error("Unable to locate Form[" + AppForm.MenuFormId 
                  + "].Element[RedirectInc]");
      }
    } catch (pErr) {
      alert("globals.onBrowserNavigationRedirect.Error: \n" + pErr.message);
    }    
  }
}

/**
 * An f:ajax onEvent method for redirecting the browser to a new location 
 * pData and sRedirectId must be assigned. Default for sTarget="_self")
 * @param pData the Ajax data parameter
 * @param sRedirectId the redirectID
 * @param sTarget the redirect target ("_self", "_new", etc.)
 **/
function onAjaxRedirect(pData, sRedirectId, sTarget) {
  var sStatus = (pData == null)? "Undefined": pData.status.toLowerCase();
  if ((sStatus == "success") && (sRedirectId != null)) {
    try {
      var pSrc = pData.source;
      if (!pSrc) {
        throw new Error ("Unable to locate AjaxData.Source");
      }
      
      var sElemId = pSrc.id;
      var pSrcElem = window.document.getElementById(sElemId);
      if (pSrcElem == null) {
        throw new Error ("Unable to locate AjaxSource.Element[" + sElemId + "].");
      }
      
      var pForm = getElementForm(pSrcElem);
      if (!pForm) {
        throw new Error ("Unable to locate AjaxElement[" + sElemId + "]'s Form");
      }

      var sFormId = pForm.id;
      var pElem = getFormInputElement(sFormId, sRedirectId, "HIDDEN");
      if (pElem != null) {
        var sRedirectUrl = pElem.value;
        sTarget = ((sTarget == null) || (sTarget == ""))? "_self": sTarget;
        if ((sRedirectUrl != null) && (sRedirectUrl != "")) {
          //alert("redirect to '"+sRedirectUrl+"'.");
          window.open(sRedirectUrl,sTarget,"",true);
        }
      } else {
        throw new Error("Unable to locate Redirect Element[" + sRedirectId + "]");
      }
    } catch (pErr) {
      alert("globals.onAjaxRedirect.Error: \n" + pErr.message);
    }    
  }
}

/**
 * Reload the current page on completion of the ajax event.
 * @param pData the Ajax data parameter
 * @param sCurUrl the URl for reloading the current page
 **/
function onAjaxReload(pData, sCurUrl) {
  var sStatus = (pData == null)? "Undefined": pData.status.toLowerCase();
  if (sStatus == "success") {
    alert("reloading[" + sCurUrl +"] . . .");
    if (sCurUrl != null) {
      window.open(sCurUrl,"_self","",true);
    } else {
      window.location.reload();
    }
  }
}

/* 
 * Get the Form[sFormId]'s Hidden Element[sElemId]}
 * @param sFormId String
 * @param sElemId String
 * @param sType String
 * @type Element
 */
function getFormInputElement(sFormId, sElemId, sType) {
  var pElem = null;    
  try {
    sFormId = (sFormId == null)? "": allTrim(sFormId.toString());
    if (sFormId == "null") {
      throw new Error("The Form ID in undefined")
    }    

    sElemId = (sElemId == null)? "": allTrim(sElemId.toString());
    if (sElemId == "") {
      throw new Error("Form Element ID is undefined");
    }

    sType = (sType == null)? "": allTrim(sType.toString());
    if (sType == "") {
      throw new Error("Form Input Element Type is undefined");
    }
    sType = sType.toUpperCase();
    var getChildElement = function(pChild) {
      var pResult = null;
      while (pChild != null) {
        var sTagname = null;
        try {
          //if ((pChild instanceof Element) &&
          //  (pChild.tagName.toUpperCase() == "INPUT") &&
          if ((pChild.tagName.toUpperCase() == "INPUT") &&
            (pChild.type.toUpperCase() == sType)) {
            var sChildId = pChild.id;
            if ((sChildId.indexOf(sFormId, 0) == 0) &&
                (sChildId.indexOf(sElemId, 0) > 0)) {
              pResult = pChild;
              break;
            }
          }
        } catch (pErr1) {
        }
        
        var pSubChild = pChild.firstChild;
        if (pSubChild != null) {
          try {
            pResult = getChildElement(pSubChild);
            if (pResult != null) {
              break;
            }
          } catch(pSubErr) {
            throw new Error("Get SubChild Error:\n" + pSubErr.message);
          }
        }

        pChild = pChild.nextSibling;
      }
      return pResult;
    }

    var sFullElemId = sFormId + ":" + sElemId;      
    pElem = window.document.getElementById(sFullElemId);
    if (pElem != null) {
      if (pElem.tagName.toUpperCase() != "INPUT") {
        throw new Error("Invalid Form Element["+sFullElemId+
          "].Tag. Expected a 'INPUT', got a '"+pElem.tagName+"'");
      }
      if (pElem.type.toUpperCase() != sType) {
        throw new Error("Invalid From Element["+sFullElemId+
          "].Type. Expected a '"+sType+"', got a '"+pElem.type+"'");
      }
    } else {
      var pForm = window.document.getElementById(sFormId);
      if (pForm != null) {
        var pChild = pForm.firstChild;
        pElem = getChildElement(pChild);
      }
    }
//
//    if (pElem == null) {
//      throw new Error("Unable to locate the Element[" + sFullElemId 
//        + "] or Element["+sFormId + ":...:" + sElemId + "].Type[" + sType + "]")
//    }
  } catch (pErr) {
    throw new Error("globals.getFormInputElement[" + sFormId + "," + sElemId 
                      + "].Type[" + sType + "] Error:\n" + pErr.message);
  }
  return pElem;
}

/* Append a new function pFunc to prior assigned functions assigned to the 
 * window.onload Event handler */
function prependWindowOnLoadEvent(pFunc) {
  var pOldFunc = window.onload;
  if (typeof window.onload != 'function') {
    window.onload = pFunc;
  } else {
    window.onload = function() {
      pFunc();
      if (pOldFunc) {
        pOldFunc();
      }
    }
  }
  alert("window.onload="+window.onload);
}

/* Append a new function pFunc to prior assigned functions assigned to the 
 * window.onload Event handler */
function appendWindowOnLoadEvent(pFunc) {
  var pOldFunc = window.onload;
  if (typeof window.onload != 'function') {
    window.onload = pFunc;
  } else {
    window.onload = function() {
      if (pOldFunc) {
        pOldFunc();
      }
      pFunc();
    }
  }
  alert("window.onload="+window.onload);
}

/* Append a new function pFunc to prior assigned functions assigned to the 
 * window.onunload Event handler */
function appendWindowOnUnloadEvent(pFunc) {
  var pOldFunc = window.onunload;
  if (typeof window.onunload != 'function') {
    window.onunload = pFunc;
  } else {
    window.onunload = function() {
      if (pOldFunc) {
        pOldFunc();
      }
      pFunc();
    }
  }
}

/* Create an Valid ObjectID by replcaing any ":; " with "_" and adding the prefix and/or
* the suffix to the eElemID
*/
function makeObjectID(sElemID,sPrefix,sSuffix) {
  if (!sElemID)
    throw new Error("ElementID is undefined");

  sElemID = allTrim(sElemID);
  if (sElemID != "") {
    sElemID = sElemID.replace(/:/g,"_");
    sElemID = sElemID.replace(/;/g,"_");
    sElemID = sElemID.replace(/ /g,"_");
  } else {
    sElemID = "XXX";
  }
  
  sPrefix = allTrim(sPrefix);
  if (sPrefix != "") {
    sElemID = sPrefix + ((sElemID.charAt(0)=="_")? "": "_") + sElemID;
  }
  
  sSuffix = allTrim(sSuffix);
  if (sSuffix != "") {
    sElemID += ((sElemID.charAt(sElemID.length-1) == "_")? "" : "_");
    sElemID += sSuffix;
  }
  return sElemID;
}

/*
* Initiate a new AppForm object if an object for the current form has not been initiated
* and return either the existing or new object
*/
function initAppForm(sFormID) {
  var pForm = null;
  try {
    sFormID = (sFormID == null)? "": allTrim(sFormID.toString());
    if (sFormID == "") {
      throw new Error("The Form's Element ID is undefined");
    }
    // alert("FormId.trim =" + sFormID);
    
    var sObjId = makeObjectID(sFormID, "AppForm_", null);
    try {
      eval("pForm="+sObjId);
    }catch(pExp1) {
      pForm = null;
    }
    
    // alert("sObjId =" + sObjId);
    
    if (pForm == null) {
      try {
        pForm = new AppForm();
      } catch (pExp2) {
        throw new Error("Initiate a New AppForm Error\n " + pExp2.message);
      }
    } else {
      // alert("Form["+sFormID+"] already exists.");
    }
    pForm.setID(sFormID);
  }catch(pExp) {
    alert("initAppForm[" + sFormID + "] Error:\n "+pExp.message);
  }
  return pForm;
}

/**
 * Search and return pElement's Parent Node with tagName=FORM. Return if noll
 */
function getElementForm(pElement) {
  var pResult = null;
  if (pElement != null) {
    var pParent = pElement.parentNode;
    while (pParent != null) {
      if ((pParent.tagName.toUpperCase() == "FORM") ||
          (pParent.nodeName.toUpperCase() == "FORM")) {
        pResult = pParent;
        break;
      }
      pParent = pParent.parentNode;
    }
  }
  return pResult;
}

/**
 *Called to innitiate a new ApPForm and register it the Default Button and the
 * Submit State element.
 * @param sProxyID String
 * @param sTBarID String
 * @param sStateID String
 * @param sSubmitID String
 * @param bDoEsc Boolean
 * @param bDoSubmitOnEnter Boolean
 * @param eDefState integer
 * @type AppFrom
 */ 
function onRegisterAppForm(sProxyID, sTBarID, sStateID, sSubmitID, bDoEsc, 
          bDoSubmitOnEnter, eDefState) {
  var pResult = null;
  try {
    var pProxy = window.document.getElementById(sProxyID);
    if (!pProxy) {
      throw new Error ("Unable to locate ProxyElement["+sProxyID+"]");
    }

    var pForm = getElementForm(pProxy);
    if (!pForm) {
      throw new Error ("Unable to locate ProxyElement["+sProxyID+"]'s Form");
    }
    
    var sFormID = pForm.id;
    if (sTBarID) {
      sStateID = (sStateID)? sTBarID+":"+sStateID: sStateID;
      sSubmitID = (sSubmitID)? sTBarID+":"+sSubmitID: sSubmitID;
    }

    var pAppForm = initAppForm(sFormID);
    if (pAppForm) {
      if (sSubmitID) {
        pAppForm.setDefaultButton(sSubmitID,eDefState);
      }
      if (sStateID) {
        pAppForm.setStateElement(sStateID);
      }
      if ((bDoEsc != null) && (bDoEsc)) {
        pAppForm.setDoEsc();
      }
      pAppForm.setDoSubmitOnEnter(bDoSubmitOnEnter);
    }
    pResult = pAppForm;
    //alert("Registered Form[" + sFormID + "].")
  }catch(pExp) {
     alert("onRegisterAppForm Error:\n "+pExp.message);
  }
  return pResult;
}

/*
* Called by a CommandLink.onclick to cancel the MenuForm's back action 
* (used with MenuLinks). Set AppForm.SubmitBack = false;
*/
function onCancelBack() {
  AppForm.SubmitBack = false;
  var sFormID = AppForm.MenuFormId;
  try {
    var pAppForm = (sFormID != null)? initAppForm(sFormID): null;
    var pStateElem = (pAppForm != null)? pAppForm.getStateElement(): null;
    if (pStateElem) {
      pStateElem.value = AppSubmitEnums.YES;
      //alert("Set Form["+sFormID+"]'s Element[SubmitState].value=YES.");
    } else {
      //alert("Unable to locate Form["+sFormID+"]'s Element[SubmitState].");
    }
  }catch (pErr) {
    alert("Form["+sFormID+"].Element[SubmitState].onCancelBack Error:\n" + pErr.message);
  }
}

/**
 * Called to set the PendingAjax state of an AppForm (as identified by its 
 * ProxyElementID)
 * Submit State element.
 * @param sProxyID String
 * @param bSet Boolean
 */ 
function onSetPendingAjax(sProxyID, bSet) {
  try {
    var pProxy = window.document.getElementById(sProxyID);
    if (!pProxy) {
      throw new Error ("Unable to locate ProxyElement["+sProxyID+"]");
    }

    var pForm = getElementForm(pProxy);
    if (!pForm) {
      throw new Error ("Unable to locate ProxyElement["+sProxyID+"]'s Form");
    }
    
    var sFormID = pForm.id;
    var pAppForm = initAppForm(sFormID);
    if (pAppForm) {
      pAppForm.setPendingAjax(bSet);
    }
  }catch(pExp) {
     alert("globals.onSetPendingAjax Error:\n "+pExp.message);
  }
}

/*
* Called by any Form Element to locate the AppForm and fire its onSubmit(eSubmit) event.
*/
function onAppFormClick(pElement,eSubmit) {
  try {
    var pAppForm = null;
    var sFormID = null;
    if (pElement) {
      var pForm = pElement.form;
      if (pForm) {
        sFormID = pForm.id;
        pAppForm = initAppForm(sFormID);
      } 
    }
    if (pAppForm) {
      pAppForm.onSubmit(eSubmit);
    } else {
      alert("Cannot find Form["+sFormID+"].");
    }
  }catch (pEx) {
    alert(pEx.message);
  }
}

/*
* Called by any Form Element to simulate the YES|SUBMIT Button submit. It locate the 
* AppForm and fire its onSubmit(YES) event.
*/
function onAppFormYes(pElement) {
  onAppFormClick(pElement, AppSubmitEnums.YES)  
}

/*
* Called by any Form Element to simulate the NO Button submit. It locate the 
* AppForm and fire its onSubmit(NO) event.
*/
function onAppFormNo(pElement) {
  onAppFormClick(pElement, AppSubmitEnums.NO)  
}

/*
 * Called by any Form Element to simulate the CANCEL Button submit. It locate the 
 * AppForm and fire its onSubmit(CANCEL) event.
 */
function onAppFormCancel(pElement) {
  onAppFormClick(pElement, AppSubmitEnums.CANCEL)  
}

/*
* Called by any Form Element to simulate the BACK Button submit. It locate the 
* AppForm and fire its onSubmit(BACK) event.
*/
function onAppFormBack(pElement) {
  onAppFormClick(pElement, AppSubmitEnums.BACK)  
}

/*
* Called by any Form Element to simulate the RESET Button submit. It locate the 
* AppForm and fire its onSubmit(RESET) event.
*/
function onAppFormReset(pElement) {
  onAppFormClick(pElement, AppSubmitEnums.RESET)  
}

/*
* Called by any Form Element to simulate the YESNEW Button submit. It locate the 
* AppForm and fire its onSubmit(YESNEW) event.
*/
function onAppFormSubmitNew(pElement) {
  onAppFormClick(pElement, AppSubmitEnums.YESNEW)  
}

/*
* Called by any Form Element to simulate the SKIP Button submit. It locate the 
* AppForm and fire its onSubmit(SKIP) event.
*/
function onAppFormSkip(pElement) {
  onAppFormClick(pElement, AppSubmitEnums.SKIP)  
}

/*
* Called by any Form Element to simulate the SUSPEND Button submit. It locate the 
* AppForm and fire its onSubmit(SUSPEND) event.
*/
function onAppFormSuspend(pElement) {
  onAppFormClick(pElement, AppSubmitEnums.SUSPEND)  
}

/*
* Called by any Form Element to simulate the ADDNEW Button submit. It locate the 
* AppForm and fire its onSubmit(ADDNEW) event.
*/
function onAppFormAddNew(pElement) {
  onAppFormClick(pElement, AppSubmitEnums.ADDNEW)
}

/*
* Called by any Form Element to simulate the DELETE Button submit. It locate the 
* AppForm and fire its onSubmit(DELETE) event.
*/
function onAppFormDelete(pElement) {
  onAppFormClick(pElement, AppSubmitEnums.DELETE)
}

/*
* Called by any Form Element to simulate the RELOAD Button submit. It locate the 
* AppForm and fire its onSubmit(RELOAD) event.
*/
function onAppFormReload(pElement) {
  onAppFormClick(pElement, AppSubmitEnums.RELOAD)
}

/*
* Called by the SubmitNew Button to locate the AppForm and fire its onSubmit(eSubmit)
* event.
*/
function onAppFormSubmitNext(pButton, eSubmit) {
  onAppFormClick(pElement, eSubmit)
}

function strStartsWith(sTarget, sSearch) {
  var bResult = false;
  try {
    if ((sTarget != null) && (sSearch != null) || (sSearch.length <= sTarget.length)) {
      sTarget = sTarget.toUpperCase();
      sSearch = sSearch.toUpperCase();
      bResult = (sTarget.indexOf(sSearch) == 0);
    }
  } catch (pErr) { 
    alert("globals.strStartsWidth Error:\n"+pErr);
  }

  return bResult;
}

function strEndsWith(sTarget, sSearch) {
  var bResult = false;
  try {
    if ((sTarget != null) && (sSearch != null) || (sSearch.length <= sTarget.length)) {
      sTarget = sTarget.toUpperCase();
      sSearch = sSearch.toUpperCase();
      bResult = (sTarget.substr(sTarget.length-sSearch.length) == sSearch);
    }
  } catch (pErr) { 
    alert("globals.strEndsWidth Error:\n"+pErr);
  }
  return bResult;
}
/*
* Called to set the focus on a Form Input Element based on the reference of a proxy
* element with the focus element base ID set a its title property
*/
function onAppFormFocus(sProxyID) {
  try {
    var pProxy = window.document.getElementById(sProxyID);
    if (!pProxy)
      throw new Error ("Unable to locate ProxyElement["+sProxyID+"]");

    var pForm = this.getElementForm(pProxy);
    if (!pForm) {
      throw new Error ("Unable to locate ProxyElement["+sProxyID+"]'s Form");
    }
    
    var sFocusID = pProxy.title;
    var sFormID = pForm.id;
   // alert("Focus Id=" + sFocusID + "\nFormId="+sFormID);
    var getFocusElem = function(pChildren) {
      var pChild = null;
      
      if ((pChildren != null) && (pChildren.length > 0)) {
        for (var i=0; i < pChildren.length; i++) {
          var pItem = pChildren[i];
          if (pItem == null) {
            continue;
          }
          
          var sItemID = pItem.id;
          if ((sItemID != null) && (sItemID != "")) {
            if (strEndsWith(sItemID,sFocusID)) {
              if (strStartsWith(sItemID,sFormID)) {
   //alert("FocusElement=" + sItemID);
                pChild = pItem;
                break;
              }
            }
          }
          
          if (pChild == null) {
            pChild = getFocusElem(pItem.childNodes);
            if (pChild != null) {
              break;
            }
          }
        }
      } 
      return pChild;
    }
    
    if ((pForm != null) && (sFocusID)) {
      try {
        var pFocus = getFocusElem(pForm.childNodes);
        if (pFocus != null) {
          //alert("Focus Element=" + pFocus.id);
          var sTag = pFocus.tagName.toUpperCase();
          if (sTag == "SELECT") {
            if ((pFocus.selectedIndex<0) || (pFocus.selectedIndex==null)) {
              pFocus.selectedIndex=0;
            }
            pFocus.focus();            
          } else {
            pFocus.focus();
            if (sTag != 'TEXTAREA') {
              pFocus.select();
            }
          }
        }
      } catch(pEx1) {}
    }
  } catch(pExp) {
    //alert("onAppFormFocus Error:\n "+pExp.message);
  }
}

/*
* Called to register the FileUpload EZComponent's elements
*/
function onAppFormAddFileUpload(sElemID) {
  try {
    var sTableId = sElemID+"_Control";
    var pCtrlTable = window.document.getElementById(sTableId);
    if (!pCtrlTable) {
      throw new Error ("Unable to locate Control["+sTableId+"]");
    }

    var pForm = getElementForm(pCtrlTable);
    if (!pForm) {
      throw new Error ("Unable to locate ProxyElement["+pCtrlTable+"]'s Form");
    }

    var sFormID = pForm.id;
    var pAppForm = initAppForm(pForm.id);
    if (pAppForm == null) {
      throw new Error ("Unable to initiate AppFoorm["+sFormID+"].");
    }
  } catch(pExp) {
    alert("onAppFormFocus Error:\n "+pExp.message);
  }
}

/***************************************************************************************
 * Called by the BitmapEditor's CheckBox.onclick to flip the Bitmap Filter switch 
 * associated with the Checkbox. It assumes that pElem's ID containts a 3-digit suffix
 * that specifies the BitMapFilter's iSwitch and iBit index (e.g. "4_1" - iSwidth=4, 
 * iBit=1).
 * @param pElem the Checkbox Element
 * @param sFilterId the ElemementId suffix of the hidden BitmapValue element.
 * @param sOutputId the ElemementId of the BitmapValueTtext (span) element.
 * @param iNumSwitches the number of switches [1..4] in the Filter.
 * @param bReadOnly the Bitmap Filter's isReadOnly state.
 * @return false (cancel change in value) if (bReadOnly) or the reference element could
 * not be located. Return true if teh hidden value was successfully updated.
 **************************************************************************************/
function onAppFormFlipBitmap(pElem, sFilterId, sOutputId, iNumSwitches, bReadOnly) {
  var bResult = false;
  sFilterId = allTrim(sFilterId);
  sOutputId = allTrim(sOutputId);
  if ((pElem != null) && (!bReadOnly) && (sFilterId != "") && (sOutputId != "")) {
    /** Return a new value with the bit turned ON | OFF **/
    var flipBitmap = function (eValue, iSwitch, iBit) {
      var eFilter = getBit(iSwitch,iBit);
      eValue = eValue >>> 0;
      eFilter = eFilter >>> 0;
//      alert("iSwitch="+iSwitch+"\niBit="+iBit
//            +"\neValue="+eValue+"; HexValue="+eValue.toString(16)
//            +"\neFilter="+eFilter+"; HexFilter="+eFilter.toString(16));
      var eResult = eValue;   
      var eMap = ((eValue & eFilter) >>> 0);
      if (eMap != eFilter) {
        eResult = (eValue | eFilter);
      } else {
        eFilter = ((0xFFFFFFFF ^ eFilter) >>> 0);        
        eResult = (eValue & eFilter);
      }
      eResult = eResult >>> 0;
//      alert("iSwitch="+iSwitch+"\niBit="+iBit
//            +"\neFilter="+eFilter+"; HexFilter="+eFilter.toString(16)
//            +"\neResult="+eResult+"; HexResult="+eResult.toString(16));
      return eResult;
    }
    
    /** Get the formatted value **/
    var formatOutput = function(eNewValue) {
      eNewValue = (isNaN(eNewValue))? 0: eNewValue;
      iNumSwitches = toInt(iNumSwitches);
      iNumSwitches = (isNaN(iNumSwitches))? 8: iNumSwitches;
      var iDigits = (iNumSwitches*2)
      var sHexVal = eNewValue.toString(16);
      if (sHexVal.length > iDigits) {
        sHexVal.substring((sHexVal.length - iDigits));
      } else {
        sHexVal = padLeft(sHexVal, "0", iDigits);
      }
      sHexVal = sHexVal.toUpperCase();
      var sResult = null;
      if (sHexVal != null) {
        for (var i = 0; i < iNumSwitches; i++) {
          var iStart = i*2;
          if (sResult == null) {
            sResult = sHexVal.substring(iStart, iStart+2);
          } else {
            sResult += " | " + sHexVal.substring(iStart, iStart+2);
          }
        }
      } 
      return sResult;
    }
    
    /** Funtion to get bit Filter **/
    var getBit = function(iSwitch, iBit) {
      var eResult = 0;
      if ((iSwitch >= 1) && (iSwitch <= 4) && (iBit >= 1) && (iBit <= 8)) {
        eResult = ((1 << (((iSwitch-1)* 8) + (iBit-1))) >>> 0);
      }
      return eResult;
    }
    
    /** Check id the bit is set **/
    var isSet = function(eValue, iSwitch, iBit) {
      var eFilter = getBit(iSwitch,iBit);
      var eMap = ((eValue & eFilter) >>> 0);
      return (eMap == eFilter);
    }
    
    /** Referesh all values in the BitMapFilter **/
    var refreshFilter = function(sPrefix, eValue) {
      for (var iRow = 1; iRow <= iNumSwitches; iRow++) {
        for (var iCol = 1; iCol <= 8; iCol++) {
          var sValueId = sPrefix + iRow + "_" + iCol
          var pValElem = document.getElementById(sValueId);
          if (pValElem != null) {
            pValElem.checked = isSet(eValue,iRow,iCol);
          }
        }
      }
    }
    
    /** Main Body of EventHandler **/
    try {
      var bChecked = pElem.checked;
      var sElemId = pElem.id;
      var sPrefix = sElemId.substring(0,sElemId.length-3);
      var iSwitch = toInt(sElemId.substr(sElemId.length-3,1));
      var iBit = toInt(sElemId.substr(sElemId.length-1,1));
     // alert("ElementId="+sElemId+"\niSwidth="+iSwitch+"\niBit="+iBit
      //      +"\nChecked="+bChecked);

      var pForm = pElem.form;
      var pFilterElem = getFormInputElement(pForm.id,sFilterId,"HIDDEN");
      if (pFilterElem == null) {
        throw new Error("Unable to locate Input.Hidden[" + sFilterId + "].");
      }
      
      var pOutputElem = document.getElementById(sOutputId);
      if (pOutputElem == null) {
        throw new Error("Unable to locate Output Element[" + sOutputId + "].");
      }
      
      var eValue = toInt(pFilterElem.value);
      eValue = (isNaN(eValue))? 0: eValue;
      var eNewValue =  flipBitmap(eValue, iSwitch, iBit);
      var sFilterText = formatOutput(eNewValue);
      
      pFilterElem.value=eNewValue;
      pOutputElem.innerHTML=sFilterText;
      refreshFilter(sPrefix, eNewValue);
      bResult = true;
    } catch(pExp) {
      alert("onAppFormFlipBitmap Error:\n "+pExp.message);
    }
  }
  return bResult;
}



/**
 * Set the value of Sibling element of Host assuming the the element names differ by a
 * suffix. ID the Sibling element be its ElementId which is determined by replacing the
 * pHost.id's sHostSuffix with sTrgSuffix.
 */
function setSiblingValue(pHost,sHostSuffix,sTrgSuffix,sValue) {
  try {
    if (pHost == null) {
      throw new Error("The Host Element is unassigned.");
    }

    var sHostId = pHost.id;
    if ((sHostId == null) || (sHostId == "")) {
      throw new Error("The Host's ElementId is not assign.");
    }

    if (sHostId.indexOf(sHostSuffix) < 0) {
      throw new Error("HostId["+sHostSuffix +
                                    "] does not contain Suffix["+sHostSuffix +"].");
    }

    var sTrgId = sHostId.replace(sHostSuffix, sTrgSuffix);

    var pTrg = document.getElementById(sTrgId);
    if (pTrg == null) {
      throw new Error("Unable to locate Subling["+sTrgId + "].")
    }

    pTrg.value = (sValue == null)? "": sValue.toString();
  } catch (pExp) {
    alert("setSiblingValue Error:\n "+pExp.message);
  }
  return false;
}

/**
 * A Dummy - do nothing ajax callback funtion - use to substitute a meaningfiul
 * call back funtion (DO NOT REMOVE)
 */
function noAjaxDoNothing(data) {}

/*
* Call be an onClick event to display a HRef in the Parent window
* @type Boolean
*/
function onGoto(sHref, sTarget) {
  var bResult = true;
  try
  {
    AppElement.hidePopups(null);
    sTarget = (sTarget == null)? "": allTrim(sTarget).toLowerCase();
    if (sTarget == "_self") {
      bResult = onGotoSelf(sHref);
    } else if (sTarget == "_parent") {
      bResult = onGotoParent(sHref);
    } else if (sTarget == "_new") {
      bResult = onGotoNew(sHref,null);
    } else {
      bResult = onGotoSelf(sHref);
    }
  }
  catch(ex)	{}
  return bResult;
}
/*
* Call be an onClick event to display a HRef in the Parent window
* @type Boolean
*/
function onGotoMain(href)
{
  try
  {
    if ((href != null) && (href != "")) {
      var pWindow = window;
      while ((pWindow.frameElement != null) && (pWindow.frameElement.ownerDocument != null))
      {
        pWindow = pWindow.frameElement.ownerDocument.parentWindow;
      }

      pWindow.location = href;
    }
  }
  catch(ex)	{}
  return true;
}
//************************************************************************************
// Call be an onClick event to display a HRef in the Parent window
//************************************************************************************
function onGotoParent(href) {
  try {
    if ((href != null) && (href != "")){
      var pWindow = window;
      if ((pWindow.frameElement != null) &&
        (pWindow.frameElement.ownerDocument != null))
        pWindow = pWindow.frameElement.ownerDocument.parentWindow;

      pWindow.location = href;
    }
  }
  catch(ex) {}
		
  return true;
}

//************************************************************************************
// Call be an onClick event to display a HRef in the same window
//************************************************************************************
function onGotoSelf(href) {
  try {
    if ((href != null) && (href != "")) {
      window.location = href;
    }
  }
  catch(ex) {}
		
  return true;
}

//************************************************************************************
// Open a print Window with sUrl 
//************************************************************************************
this.onGotoNew = function(sUrl,sFeatures) {
  try  {
    if (sFeatures == null)
      sFeatures = "dependent=no";
				
    window.open(sUrl,"Child_Window",sFeatures);
  }
  catch(ex)  {
    alert(ex.message);
  }
  return true;
}
//************************************************************************************
// Open a print Window with sUrl 
//************************************************************************************
this.onPrintPage = function(sUrl){
  try {
    var sFeatures = "width=700,menubar,toolbar,dependent=yes";
    var pWindow = window.open(sUrl,"Print_Window",sFeatures);
    if (pWindow != null) {
      //pWindow.toolbar.vsisble=true;
      pWindow.print();
    }
  }
  catch(ex)
  {}
  return true;
}

//************************************************************************************
// Call be a MouseOver event to display the obj.title in the status bar
//************************************************************************************
function onHoover(obj,sCursor){
  try  {
    if (obj != null)    {
      if (obj.title != null)
        window.defaultstatus = obj.title;
			
      var pStyle = obj.style;
      if (pStyle != null)      {
        if ((sCursor == null) || (sCursor == "")) {
          pStyle.cursor = "url('handpoint.cur') ,hand, auto;";
        } else {
          pStyle.cursor = sCursor;
        }
      }
    }
  }
  catch(ex)
  {}
  return true;
}

//************************************************************************************
// Call be a Mouseout event to clear the status bar
//************************************************************************************
function onExit(obj){
  try  {
    window.defaultstatus = "";
    var pStyle = obj.style;
    if (pStyle != null) {
      pStyle.cursor = "default;";
    }
  }
  catch(ex)
  {}
		
  return true;
}


//************************************************************************************
// Opens a new window showing sUrl with spciaifed features
//************************************************************************************
function OpenBrowserWindow(theURL,winName,features)  {
  if ((features == null) || (features == ""))
    features = "";
		
  var sName = new String(winName);
  sName = sName.replace(/\s/g,"_");
		
  window.open(theURL,sName,features);
  return true;
}

//************************************************************************************
// Opens a Topic Page and display sSrc in an IFrame
//************************************************************************************
function ShowTopic(sParentFolder,sCaption,sSrc,bDoPrint,bDoExpand)
{
  try
  {
    if ((sSrc == null) || (sSrc == ""))
      throw new Error("Topic Link is undefined");
				
    var sUrl = GetRelativePath("_borders", sParentFolder);
    if (sUrl == null)
      throw new Error("Unable to extract the relative path to Page[topicpage]");
    sUrl += "/topicpage.htm";
		
    var sPars = "";
    if ((sCaption == null) || (sCaption == ""))
      sCaption = "Page["+sSrc+"]";
			
    var pUrl = new Url();
    sPars = "title="+pUrl.encode(sCaption);
				
    if ((sSrc != null) && (sSrc != ""))
      sPars = (sPars=="")? "src="+sSrc: sPars +"&src="+sSrc;
		
    if ((bDoPrint != null) && (bDoPrint == true))
      sPars = (sPars=="")? "print=true": sPars +"&print=true";
		
    if ((bDoExpand != null) && (bDoExpand == true))
      sPars = (sPars=="")? "expand=true": sPars +"&expand=true";
		
    if (sPars != "")
      sUrl += "?"+sPars;
			
    var sFeatures = "'menubar=no,toolbar=no,status=no,directories=no,"
                     + "location=0,resizeable=no,height=580,width=580'";
    OpenBrowserWindow(sUrl,"Topic_Window",sFeatures);
  }
  catch(ex)
  {
    alert("Show Topic Error: "+ex.message);
  }
}

//************************************************************************************
// Convert items in aArray to a path
//************************************************************************************
function ArrayToPath(aArray)
{
  var sPath = "";
  if ((aArray == null) || (aArray.length == 0))
    return sPath;
  
  for(var i=0;i<aArray.length;i++)
  {
    var sFolder = aArray[i];
    if ((sFolder != null) && (sFolder != ""))
    {
      if (sPath == "")
        sPath = sFolder;
      else
        sPath = sPath+"/"+sFolder;
    }
  }
  return sPath;
}

//************************************************************************************
// Convert a path to an array for folder names
//************************************************************************************
function PathToArray(sPath)
{
  var aArray = new Array();

  if ((sPath != null) && (sPath.length != 0))
    aArray = sPath.split("/");

  return aArray;
}
//************************************************************************************
// Repalce Back Slashed with Forward Slashes
//************************************************************************************
function PathToForwardSlash(sPath)
{
  if ((sPath != null) && (sPath != "")) {
    var pRegExp = new RegExp("\\", "g");
    sPath = sPath.replace(pRegExp,"/");
  }
  return sPath
}
//************************************************************************************
// Strip the slashed from the front and backend of the path
// Call PathToForwardSlash to convert to forward slashes.
//************************************************************************************
function PathStripSlashes(sPath)
{
  if ((sPath != null) && (sPath != ""))
  {
    sPath = PathToForwardSlash(sPath);
    while ((sPath.length > 1) && (sPath.charAt(0) == "/"))
    {
      if (sPath.length == 1)
        sPath = "";
      else
        sPath = sPath.substring(1);
    }

    while ((sPath.length > 1) && (sPath.charAt(sPath.length-1) == "/"))
    {
      if (sPath.length == 1)
        sPath = "";
      else
        sPath = sPath.substring(0,sPath.length-1);
    }
  }
  return sPath;
}
//************************************************************************************
//  Build a relative path using sPath and sBasePath
//************************************************************************************
function GetRelativePath(sPath, sBasePath)
{
  var sRelPath = null;
  if ((sBasePath == null) || (sBasePath.length==0))
    return sPath;

  var aArrPath = PathToArray(sPath);
  var aArrBase = PathToArray(sBasePath);
  var aArrRel = new Array();
  var iLevel = 0;
  var i = 0;
  if (aArrBase.Length == 0)
    sRelPath = sPath;
  else if (aArrBase.length > aArrPath.length)
  {
    for (i=0;i<aArrBase.length;i++)
    {
      if (i >= aArrPath.length)
      {
        if (iLevel < 2)
        {
          aArrRel.unshift("..");
          iLevel++;
        }
        else
          break;
      }
      else if (iLevel > 0)
      {
        if (iLevel > 2)
          break;
				
        aArrRel.unshift("..");
        iLevel++;
      }
      else if (aArrBase[i] != aArrPath[i])
      {
        iLevel++;
        aArrRel.push("..");
        if (i < (aArrPath.length))
        {
          for(var j=i;j<aArrPath.length;j++)
          {
            aArrRel.push(aArrPath[j]);
          }
        }
      }
    }
		
    if (aArrRel.length > 0)
      sRelPath  =  ArrayToPath(aArrRel);
  }
  else
  {
    for (i=0;i<aArrPath.length;i++)
    {
      if ((iLevel >0) || (i >= aArrBase.length))
      {
        aArrRel.push(aArrPath[i]);
      }
      else if (aArrPath[i] != aArrBase[i])
      {
        while ((iLevel < 2) && ((i+iLevel) < aArrBase.length))
        {
          aArrRel.unshift("..");
          iLevel++;
        }
        aArrRel.push(aArrPath[i]);
      }
    }
		
    if (aArrRel.length > 0)
    {
      sRelPath  =  ArrayToPath(aArrRel);
    }
  }
  return sRelPath;
}

//************************************************************************************
// Extract the value of parameter sPar from the page's URL.
// Return null if not assigned.
//************************************************************************************
function GetUrlParameter(sPar)
{			
  var sHRef = window.location.search;
  var sVal = null;
  var iIndex = -1;
  if (sHRef != null)
    iIndex = sHRef.indexOf(sPar);
		
  if (iIndex >= 0)
  {
    var iEnd = sHRef.indexOf("&",iIndex);
    var sSubStr = sHRef;
    if (iEnd > iIndex)
      sSubStr = sHRef.substring(iIndex,iEnd);
		
    var sValStr = "";
    if (sSubStr != "")
    {
      var iEqual = sSubStr.indexOf("=");
      if ((iEqual >= 0) && ((iEqual+1) < (sSubStr.length-1)))
        sValStr = sSubStr.substring(iEqual+1);
    }
			
    if (sValStr.length > 0)
    {
      var pUrl = new Url();
      sVal = pUrl.decode(sValStr);
    }
  }
  return sVal;
}

//************************************************************************************
// Return true if the Pages Url include the string sPar
//************************************************************************************
function HasUrlParameter(sPar)
{			
  var sHRef = window.location.search;
  return ((sHRef != null)  && (sHRef.indexOf(sPar) >= 0))
}

//************************************************************************************
// URL Class
//************************************************************************************
function Url()
{
  // public method for url encoding
  this.encode = function(string)
  {
    return escape(this._utf8_encode(string));
  }

  // public method for url decoding
  this.decode = function(string)
  {
    return this._utf8_decode(unescape(string));
  }

  // private method for UTF-8 encoding
  this._utf8_encode = function (string)
  {
    string = string.replace(/\r\n/g,"\n");
    var utftext = "";

    for (var n = 0; n < string.length; n++)
    {
      var c = string.charCodeAt(n);

      if (c < 128)
      {
        utftext += String.fromCharCode(c);
      }
      else if((c > 127) && (c < 2048))
      {
        utftext += String.fromCharCode((c >> 6) | 192);
        utftext += String.fromCharCode((c & 63) | 128);
      }
      else
      {
        utftext += String.fromCharCode((c >> 12) | 224);
        utftext += String.fromCharCode(((c >> 6) & 63) | 128);
        utftext += String.fromCharCode((c & 63) | 128);
      }
    }

    return utftext;
  }

  //************************************************************************************
  // private method for UTF-8 decoding
  //************************************************************************************
  this._utf8_decode = function (utftext)
  {
    var string = "";
    var i = 0;
    var c = 0;
    var c1 = 0;
    var c2 = 0;

    while ( i < utftext.length )
    {
      c = utftext.charCodeAt(i);

      if (c < 128)
      {
        string += String.fromCharCode(c);
        i++;
      }
      else if((c > 191) && (c < 224))
      {
        c2 = utftext.charCodeAt(i+1);
        string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
        i += 2;
      }
      else
      {
        c2 = utftext.charCodeAt(i+1);
        var c3 = utftext.charCodeAt(i+2);
        string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
        i += 3;
      }
    }
    return string;
  }
}

//************************************************************************************
// Open a print Window with sUrl 
//************************************************************************************
function onSubmitForm(sElementID,sUrl) {
  try {
    var pForm = getElementForm(sElementID);
    if (pForm != null) {
      pForm.action = sUrl;
      pForm.submit();
    } else {
      throw new Error("Cannot find locate Element["+sElementID+"]'s parent Form.");
    }
  }
  catch(ex) {
    alert("onSubmitForm Error: \n"+ex.message);
  }
  return true;
}

//************************************************************************************
// Called to submit the form with a reference to Element - 
// onSubmitForm(sElementID,sUrl+&ElemntID=sElementID)
//************************************************************************************
function onSendElementID(sElementID,sUrl)	 {
  try  {
    if ((sElementID != null) && (sElementID != "") &&
      (sUrl != null) && (sUrl != "")) {
      sUrl += "&ElementID="+sElementID;
      onSubmitForm(sElementID,sUrl);
    }
  }
  catch(ex) {
    alert("onSendElementID Error: \n"+ex.message);
  }
		
  return true;
}

//************************************************************************************
// Called to on Exit a Element - isset(sUrl):call onGoToSelf; 
// if isset(sClass) - set obj.className; call onExit.
//************************************************************************************
function onExitAction(obj,sUrl,sClass) {
  try  {
    if (obj != null) {
      if (sClass != null)
        obj.className = sClass;
		
      if (sUrl != null)
        onGoToSelf(sUrl);

      onExit(obj);
    }
  }
  catch(ex)
  {}
		
  return true;
}

//************************************************************************************
// if (window.event.keyCode) or (pEvent.which) = 13 (Enter) - find Form[sFormID] and
// Fire its submit event. This function raise no errors - just do nothing
//************************************************************************************
function onEnterToSubmit(sFormID, pEvent) {
  var key;
  var bResult = true;
  try  {
    if (window.event) {
      key = window.event.keyCode;	//IE
    } else {
      key = pEvent.which;				//firefox
    }
    
    if (key == 13) {
      bResult = true;
    }
  }
  catch(ex) {}
		
  return bResult;
}

/**
 * The keypress event handler is designed to intercept and cancel a "Enter"-key press. 
 * Return false if handled and in the case of IE, set the keyCode=0 to cancel the event. 
 * Assign as follows: onkeypress="return onKeypressClickonEnter(event);"
 **/
function onKeypressCancelEnter(pEvent) {
  var bResult = true;
  try {
    var iKeyCode = 0;
    pEvent = (pEvent)? pEvent: window.event;
    if (pEvent) {
      if (pEvent.charCode)
        iKeyCode =pEvent.charCode;
      else if (pEvent.keyCode)
        iKeyCode = pEvent.keyCode;
      else
        iKeyCode = pEvent.which;
    } else if (window.event) {
      iKeyCode = window.event.keyCode;
    }

    if (iKeyCode == 13) {
      bResult = false;
      if (pEvent.keyCode) {
        pEvent.keyCode = 0;
      }
    } 
  } catch (pErr) { 
    alert("globals.onKeypressClickonEnter Error:\n" + pErr.message);
  }

  return bResult;
}

/**
 * Activate a Control value Update on the Server by sending an request, via the src
 * parameter of a 'hidden' or 'error frame'. sBaseUrl is the base url of the request
 * end will be ammended with "&ElementID=sElementID&Value=sValue".  It will display
 * an error if the frame is not found or sElementID is undefined. 
 */
function onUpdateControlValue(sElementID,sBaseUrl) {
  try  {
    var pElement = (sElementID == null)? null:
    window.document.getElementById(sElementID);
    if (pElement == null)
      throw new Error("Unable to locate HTML Element["+sElementID+"]");
		
    if ((sBaseUrl == null) || (sBaseUrl == ""))
      throw new Error("The BaseUrl is undefined");
			
    var sValue = null;
    var sTag = pElement.tagName.toLowerCase();
    if (sTag == "input")
      sTag = pElement.type.toLowerCase();
    var iItem = 0;
    switch (sTag) {
      case "text":
      case "textarea":
      case "password":
        sValue = pElement.value;
        break;
      case "select":
        sValue = pElement.value;
        break;
      case "checkbox":
        var pCheckBoxes = window.document.getElementsByName(pElement.name);
        if (pCheckBoxes == null) {
          sValue = (pElement.checked)? pElement.value: 0;
         } else {
          for (iItem = 0; iItem < pCheckBoxes.length; iItem++) {
            if (pCheckBoxes[iItem].checked) {
              if (sValue == null) {
                sValue = pCheckBoxes[iItem].value;
              } else {
                sValue += ","+pCheckBoxes[iItem].value;
              }
            }
          }
        }
        break;
      case "radio":
        var pRadios = window.document.getElementsByName(pElement.name);
        if (pRadios == null) {
          sValue = (pElement.checked)? pElement.value: 0;
        } else {
          for (iItem = 0; iItem < pRadios.length; iItem++) {
            if (pCheckBoxes[iItem].Checked) {
              if (sValue == null) {
                sValue = pCheckBoxes[iItem].value;
              } else {
                sValue += ","+pCheckBoxes[iItem].value;
              }
            }
          }
        }
        break;
    }

    sValue = (sValue == null)? "": sValue;
    var sUrl = sBaseUrl+"&ElementID="+sElementID+"&Value="+sValue;

    //alert("Tag="+sTag+"\nElementID["+pElement.name+"]\nValue="+sValue);
		
    var pImg = new Image();
    pImg.src = sUrl;
  }
  catch(ex) {
    alert(ex.message);
  }
		
  return true;
}

/**
 * Find Form[sFormID] and fire its submit event. 
 * This function raise no errors - just do nothing
 */
function onValidateElement(sElementID,sType,sMsg,sPar) {
  var pElement;
  var bValid = true;
  try {
    pElement = (sElementID == null)? null: window.document.getElementById(sElementID)
    if (pElement == null)
      throw new Error("Unable to locate HTML Element["+sElementID+"]");
		
    bValid = ValidateElementValue(pElement,sElementID,sType,sMsg,sPar);
  } catch (ex) {
    alert(ex.message);
    bValid = false;
  }
		
  return bValid;
}

/**
 * Find Form[sFormID] and fire its submit event. 
 * This function raise no errors - just do nothing
 */
function onValidateEntries(sFormID,pValidates)
{
  var pForm;
  var pElement;
  var bValid = true;
  try
  {
    pForm = (sFormID == null)? null: window.document.getElementById(sFormID)
    if (pForm == null) {
      throw new Error("Unable to locate Form["+sFormID+"]");
    }
    
    if ((pValidates != null) && (pValidates.length > 0)) {
      for (var i = 0; i < pValidates.length; i++) {
        var pValidation = pValidates[i];
        if ((pValidation == null) || (pValidation.length < 3))
          throw new Error("Validation["+i.toString()+"] is invalid");
				
        pElement = null;
        var sName = pValidation[0];
        var sType = pValidation[1];
        var sMsg  = pValidation[2];
        var pPar = null;
        if (pValidation.length >= 4)
          pPar = pValidation[3];
				
        pElement = ((sName != null) && (sName != ""))? pForm.elements[sName]: null;
        if (pElement == null)
          throw new Error("Unable to locate Input["+sName+"]");
				
        bValid = ValidateElementValue(pElement,sName,sType,sMsg,pPar);
        if (!bValid)
          break;
      }
    }
  }
  catch (ex)
  {
    alert(ex.message);
    bValid = false;
  }
		
  return bValid;
}
//************************************************************************************
// Find Form[sFormID] and fire its submit event. 
// This function raise no errors - just do nothing
//************************************************************************************
function ValidateElementValue(pElement,sName,sType,sMsg,sPar) {
  var bValid = true;
  if (pElement == null)
    return bValid;
		
  try {
    var sValue = pElement.value.toString();
    sType  = sType.toLowerCase();
    sPar = (sPar == null)? "": allTrim(sPar.toString());
    var sErr = "";
    switch (sType) {
      case "notblank":
        if ((sValue == null) || (allTrim(sValue) == "")) {
          sMsg = ((sMsg == null) || (sMsg == ""))? 
            "You did not enter a value in Field["+sName+"].": sMsg;
          throw new Error(sMsg);
        }
        break;
      case "maxlength":
        var iPar = toInt(sPar);
        if ((!isNaN(iPar)) && (iPar > 0) && (sValue != null) && (sValue.length > iPar)) {
          sMsg = ((sMsg == null) || (sMsg == ""))? "Field["+sName+
          "]'s length exceed MaxLength["+iPar+"].": sMsg;
          throw new Error(sMsg);
        }
        break;
      case "email":
        if ((sValue != null) && (allTrim(sValue) != "") && (!ValidateEmail(sValue))) {
          sMsg = ((sMsg == null) || (sMsg == ""))?
          "The e-mail address you entered in not in the right format. Try again": sMsg;
          throw new Error(sMsg);
        }
        break;
      case "epwname":
      case "objectname":
        if ((sValue != null) && (sValue != "") && (!ValidateePWName(sValue)))	{
          sMsg = ((sMsg == null) || (sMsg == ""))?
          "The ePrayerWorld User/House Name you entered in not in the right format. "
        + "Try again": sMsg;
          throw new Error(sMsg);
        }
        break;
      case "epwaddress":
      case "objectaddress":
        if ((sValue != null) && (sValue != "") && (!ValidateePWAddress(sValue))) {
          sMsg = ((sMsg == null) || (sMsg == ""))?
          "The ePrayerWorld Address you entered in not in the right format. "
          + "Try again": sMsg;
          throw new Error(sMsg);
        }
        break;
      case "username":
        if ((sValue != null) && (sValue != "") && (!ValidateUserName(sValue))) {
          sMsg = ((sMsg == null) || (sMsg == ""))?
          "The username["+sValue+"] you entered in not in the right format. "+
          "It must be between 6-16 characters, starting with a letter, and  "+
          "comprises letters and numbers only. Please try again": sMsg;
          throw new Error(sMsg);
        }
        break;
      case "password":
        if ((sValue != null) && (sValue != "") && (!ValidatePassword(sValue))) {
          sMsg = ((sMsg == null) || (sMsg == ""))?
          "The password["+sValue+"] you entered in not in the right format. "+
          "It must be between 6-16 characters, starting with a letter, and  "+
          "comprises letters and numbers only. Please try again": sMsg;
          throw new Error(sMsg);
        }
        break;
      case "dropdown":
        if (pElement.value == sPar) {
          sMsg = ((sMsg == null) || (sMsg == ""))?
          "Please make a valid dropdown selection and try again": sMsg;
          throw new Error(sMsg);
        }
        break;
      case "confirm":
        if ((sValue != null) && (sValue != "") && (sPar != null)) {
          var pElement2 = ((sPar != null) && (sPar != ""))? 
                                  window.document.getElementById(sPar): null;
          var sValue2 = (pElement2 == null)? "": pElement2.value.toString();
          if ((sValue2 != null) && (sValue2 != "") && (sValue2 != sValue))  {
            sMsg = ((sMsg == null) || (sMsg == ""))?
            "Your confirmation entry does not match your orginal. Check you " 
            + "entries and try again": sMsg;
            throw new Error(sMsg);
          }
        }
        break;
      case "zipcode":
        if ((sValue != null) && (sValue != "")) {
          sPar = (sPar == null)? "": sPar.toLowerCase();
          var bLong = ((sPar != "") && (sPar == "long"));
          if ((bLong)) {
            if (!ValidateZipCodeLong(sValue)) {
              sMsg = ((sMsg == null) || (sMsg == ""))?
              "The ZipCode you entered is invalid. Check you entries and try again": 
              sMsg;
              throw new Error(sMsg);
            }
          }
          else if (!ValidateZipCodeShort(sValue)) {
            sMsg = ((sMsg == null) || (sMsg == ""))?
            "The ZipCode you entered is invalid. Check you entries and try again": sMsg;
            throw new Error(sMsg);
          }
        }
        break;
      case "phoneno":
        if ((sValue != null) && (sValue != "") && (!ValidatePhoneNo(sValue))) {
          sMsg = ((sMsg == null) || (sMsg == ""))?
          "The phone number you entered is invalid. Check you entries and try again": 
          sMsg;
          throw new Error(sMsg);
        }
        break;
      case "integer":
        if ((sValue != null) && (sValue != "") && (!ValidateInteger(sValue))) {
          sMsg = ((sMsg == null) || (sMsg == ""))?
          "The integer number you entered is invalid. Check you entries and try again": 
          sMsg;
          throw new Error(sMsg);
        } else if ((sPar != null) && (sPar != "")) {
          var iVal = toInt(sValue);
          sPar = sPar.toLowerCase();
          if (!isNaN(iVal)) {
            if ((sPar.indexOf("unsigned") >= 0) && (iVal < 0)) {
              sErr = "The entered value must be a positive integer";
            }
            if ((sPar.indexOf("notzero") >= 0) && (iVal == 0)){
              sErr = "The entered value cannot be zero";
            }
            if (sErr != "") {
              sMsg = ((sMsg == null) || (sMsg == ""))? sErr: sMsg;
              throw new Error(sMsg);
            }
          }
        }
        break;
      case "numeric":
        if ((sValue != null) && (sValue != "") && (!ValidateNumeric(sValue))){
          sMsg = ((sMsg == null) || (sMsg == ""))?
          "The number you entered is invalid. Check you entry and try again": sMsg;
          throw new Error(sMsg);
        } else if ((sPar != null) && (sPar != "")) {
          var fVal = toFloat(sValue);
          sPar = sPar.toLowerCase();
          if (fVal != Number.NaN) {
            if ((sPar.indexOf("unsigned") >= 0) && (fVal < 0.0)) {
              sErr = "The entered value must be a positive number";
            }
            if ((sPar.indexOf("notzero") >= 0) && (fVal == 0.0)){
              sErr = "The entered value cannot be zero";
            }

            if (sErr != "") {
              sMsg = ((sMsg == null) || (sMsg == ""))? sErr: sMsg;
              throw new Error(sMsg);
            }
          }
        }
        break;
      default:
        throw new Error("Invalid validation Type["+sType+"]");
        break;
    }
  } catch (pErr) {
    var sElemId = pElement.id;
    alert("Input Validation Error:\n" + pErr.message);
    bValid = false;    
    gotoInputElement(sElemId);
  }
  return bValid;
}

/**
 * Called to set the focus on an input element and select its content 
 */
function gotoInputElement(sElemId) {
  try {
    var pElement = (sElemId == null)? null: window.document.getElementById(sElemId)
    if (pElement != null) {
      var sTag = pElement.tagName.toUpperCase();
      if (sTag == "SELECT") {
        if ((pElement.selectedIndex<0) || (pFocus.selectedIndex==null)) {
          pElement.selectedIndex=0;
        }
        pElement.focus();            
      } else {
        pElement.focus();
        if (sTag != 'TEXTAREA') {
          pElement.select();
        }
      }
    } else {
      alert("Could not find Element[" + sElemId + "]");
    }
  } catch (ex) {
  }
}
//*************************************************************
// Validates sInput as a Valid E-mail Address
//*************************************************************
function ValidateEmail(sInput) {
  if ((sInput == null) || (sInput.length == 0))
    return false;
		
  var pRegEx = new RegExp("(^[a-z]([a-z0-9_\.\-]*)@([a-z]([a-z0-9_\.\-]*))([.][a-z]{3})$)|"+
    "(^[a-z]([a-z0-9_\.\-]*)@([a-z]([a-z0-9_\.\-]*))(\.[a-z]{3})(\.[a-z]{2})*$)","i");
  var pResult = pRegEx.exec(sInput);
  return (pResult != null);
}

//*************************************************************
// Validates sInput as a Valid E-mail Address
//*************************************************************
function ValidateePWName(sInput)
{
  if ((sInput == null) || (sInput.length == 0))
    return false;
		
  var pRegEx = new RegExp("(^[a-z]([a-z0-9_]{5,11}))");
  var pResult = pRegEx.exec(sInput);
  return (pResult != null);
}

//*************************************************************
// Validates sInput as a Valid E-mail Address
//*************************************************************
function ValidateePWAddress(sInput)
{
  if ((sInput == null) || (sInput.length == 0))
    return false;
		
  var pRegEx = new RegExp("(^[a-z]([a-z0-9_]{4,11})\.[a-z]([a-z0-9_]{4,11}))");
  var pResult = pRegEx.exec(sInput);
  return (pResult != null);
}

//*************************************************************
// Validates sInput as a Valid Username
//*************************************************************
function ValidateUserName(sInput)
{
  if ((sInput == null) || (sInput.length < 6) || (sInput.length > 16))
    return false;
		
  var pRegEx = new RegExp("^[a-zA-Z][a-zA-Z0-9]*[a-zA-Z0-9]$","i");
  var pResult = pRegEx.exec(sInput);
  return (pResult != null);
}

//*************************************************************
// Validates sInput as a Valid password
//*************************************************************
function ValidatePassword(sInput)
{
  if ((sInput == null) || (sInput.length<6) || (sInput.length>16))
    return false;
		
  var pRegEx = new RegExp("^[a-zA-Z][a-zA-Z0-9]*[a-zA-Z0-9]$","i");
  var pResult = pRegEx.exec(sInput);
  return (pResult != null);
}

//*************************************************************
// Validates sInput as a USA 5-gigit ZipCode
//*************************************************************
function ValidateZipCodeShort(sInput)
{
  if ((sInput == null) || (sInput.length != 5))
    return false;
		
  var pRegEx = new RegExp("^[0-9]{5}$");
  var pResult = pRegEx.exec(sInput);
  return (pResult != null);
}

//*************************************************************
// Validates sInput as a USA 9-gigit ZipCode
//*************************************************************
function ValidateZipCodeLong(sInput)
{
  if ((sInput == null) || (sInput.length<5) || (sInput.length>10))
    return false;
		
  var pRegEx = new RegExp("(^[0-9]{5}$)|(^[0-9]{5}-[0-9]{4}$)");
  var pResult = pRegEx.exec(sInput);
  return (pResult != null);
}

//*************************************************************
// Validates sInput as a USA Phone Number
//*************************************************************
function ValidatePhoneNo(sInput) {
  if ((sInput == null) || (sInput.length == 0))
    return false;
  

  //remove Unwanted characters
  var pRegExp  = new RegExp("[\(]?[\)]?[ ]?[-]?[,]?[.]?","g");
  var bCheck = pRegExp.exec(sInput);
  var sPhoneNo = sInput;
  if (bCheck != null) {
    sPhoneNo = sInput.replace(pRegExp, "");
  }
  //alert("Stripped PhoneNo=" + sPhoneNo);
  //Return true if number is 10 digits
  var pRegExp2  = new RegExp("[a..z]?[A..Z]?","g");
  bCheck = pRegExp2.test(sPhoneNo);
  //alert("Check 2=" + bCheck);
  return (((bCheck != null) && (bCheck)) && (sPhoneNo.length == 10));
}

//*************************************************************
// Validates sInput as a numeric value
//*************************************************************
function ValidateNumeric(sInput)
{
  if ((sInput == null) || (sInput.length == 0))
    return false;
	
  var dVal = toFloat(sInput);
  return (!isNaN(dVal));
/*	var pRegEx = new RegExp("(^-?[0-9][0-9]*\.[0-9]*$)|"+
													"(^-?[0-9][0-9]*$)|"+
													"(^-?\.[0-9][0-9]*$)");
	var pResult = pRegEx.exec(sInput);
	return (pResult != null);
 */
}

//*************************************************************
// Validates sInput as an integer
//*************************************************************
function ValidateInteger(sInput) {
  if ((sInput == null) || (sInput.length == 0))
    return false;
	
  var dVal = toInt(sInput);
  return (!isNaN(dVal));
		
/*	var pRegEx = new RegExp("(^-?[0-9][0-9]*$)/");
	var pResult = pRegEx.exec(sInput);
	return (pResult != null);
 */
}

/**
 * Validates and sInput againts a sMatchStr
 * @param {String} sInput
 * @param {String} sMatchStr - a regular expression
 * @returns {Boolean} ture if the input string matches the regular expression's pattern
 */
function ValidateValue(sInput,sMatchStr)
{
  if ((sInput == null) || (sInput.length == 0))
    return false;
		
  var pRegEx = new RegExp(sMatchStr);
  var pResult = pRegEx.exec(sInput);
  return (pResult != null);
}

/**
 * Return the Character for the entered key taht was pressed
 * @param {event} pEvent the contols keydown event to get the input from
 * @return the Char for the keyCode
 */
function getEventChar(pEvent) { 
  var sResult = null;
  if (pEvent.which == null) { 
    sResult = String.fromCharCode(pEvent.keyCode) // IE  
  } else if ((pEvent.which!=0) && (pEvent.charCode!=0)) { 
    sResult = String.fromCharCode(pEvent.which)   // the rest  
  } else {  
    // special key  
  } 
  return sResult;
} 

/**
 * For unsigned integer values (numbers => 0). Thus, allow 0..9 and special charaters 
 * (e.g. Backspace, Shift, etc.)
 * @param {event} pEvent the contols keydown event to get the input from
 * @return {Boolean} true if in range and false for any other numbers
 */
function onKeypressUInteger(pEvent) {
  var bResult = true;
  var sChar = getEventChar(pEvent);
 // alert("Event.Char="+sChar);
  bResult = ((sChar == null) || (!isNaN(sChar)));
  if ((!bResult) && (pEvent.which == null)) {
    pEvent.keyCode = 0;
  }
  return bResult;
}

/**
 * For positive and negative integer numbers. Thus, allow 0..9,-, and special characters
 * (e.g.,Backspace, Shift, etc.)
 * @param {event} pEvent the contols keydown event to get the input from
 * @return {Boolean} true if in range and false for any other numbers
 */
function onKeypressInteger(pEvent) {
  var bResult = true;
  var sChar = getEventChar(pEvent);
 // alert("Event.Char="+sChar);
  bResult = ((sChar == null) || (sChar == "-") || (!isNaN(sChar)));
  if ((!bResult) && (pEvent.which == null)) {
    pEvent.keyCode = 0;
  }
  return bResult;
}
/**
 * For positive and negative integer numbers. Thus, allow 0..9,',',-, and special characters
 * (e.g.,Backspace, Shift, etc.)
 * @param {event} pEvent the contols keydown event to get the input from
 * @return {Boolean} true if in range and false for any other numbers
 */
function onKeypressIntList(pEvent) {
  var bResult = true;
  var sChar = getEventChar(pEvent);
 // alert("Event.Char="+sChar);
  bResult = ((sChar === null) || (sChar == "-")  || (sChar == ",") || (!isNaN(sChar)));
  if ((!bResult) && (pEvent.which === null)) {
    pEvent.keyCode = 0;
  }
  return bResult;
}

/**
 * For positive or negative decimal numbers. Thus, allow "0..9","-","." and special 
 * characters (e.g.,Backspace, Shift, etc.)
 * @param {event} pEvent the contols keydown event to get the input from
 * @return {Boolean} true if in range and false for any other numbers
 */
function onKeypressDecimal(pEvent) {
  var bResult = true;
  var sChar = getEventChar(pEvent);
 // alert("Event.Char="+sChar);
  bResult = ((sChar == null) || (sChar == "-") || (sChar == ".") || (!isNaN(sChar)));
  if ((!bResult) && (pEvent.which == null)) {
    pEvent.keyCode = 0;
  }
  return bResult;
}

/**
 * For Alpha input only. Thus, allow "a..zA..Z" and special 
 * characters (e.g.,Backspace, Shift, etc.)
 * @param {event} pEvent the contols keydown event to get the input from
 * @return {Boolean} true if in range and false for any other numbers
 */
function onKeypressAlphaOnly(pEvent) {
  var bResult = true;
  var sChar = getEventChar(pEvent);
  var pRegEx = new RegExp("^[a-zA-Z]$","i");
  bResult = ((sChar == null) || (pRegEx.exec(sChar) != null));
  if ((!bResult) && (pEvent.which == null)) {
    pEvent.keyCode = 0;
  }
  return bResult;
}

/**
 * For Alpha input only. Thus, allow "a..zA..Z0..9" and special 
 * characters (e.g.,Backspace, Shift, etc.)
 * @param {event} pEvent the contols keydown event to get the input from
 * @return {Boolean} true if in range and false for any other numbers
 */
function onKeypressAlphaNumericOnly(pEvent) {
  var result = true;
  var sChar = getEventChar(pEvent);
  var pRegEx = new RegExp("^[a-zA-Z0..9]$","i");
  result = ((sChar == null) || (pRegEx.exec(sChar) != null));
  if ((!result) && (pEvent.which == null)) {
    pEvent.keyCode = 0;
  }
  return result;
}

/***************************************************************************************
 * Trim Whitespace of the rightside of sValue
 * @param {String} sValue input string
 * @returns {String} the input string stripped of all trailing spaces and white space
 * characters (\t\n\r\f\v\b)
 **************************************************************************************/
function rightTrim(sValue) {
  if (sValue != null) {
    var pRegExp =new RegExp("([ \t\n\r\f\v\b]*)$");
    var pResult = pRegExp.exec(sValue);
    if (pResult != null) {
      sValue = sValue.replace(pRegExp,'');
    }
  }
  return (sValue == null)? "": sValue;
}

/***************************************************************************************
 * Trim Whitespace of the rightside of sValue
 * @param {String} sValue input string
 * @returns {String} the input string stripped of all leading spaces and white space
 * characters (\t\n\r\f\v)
 **************************************************************************************/
function leftTrim(sValue) {
  if (sValue != null) {
    var pRegExp =new RegExp("^([ \t\n\r\f\v]*)");
    var pResult = pRegExp.exec(sValue);
    if (pResult != null) {
      sValue = sValue.replace(pRegExp,'');
    }
  }
  return (sValue == null)? "": sValue;
}

/***************************************************************************************
 * Trim Whitespace of the both side of sValue
 * @param {String} sValue input string
 * @returns {String} the input string stripped of all leading and trailing spaces and
 * white space characters (\t\n\r\f\v)
**************************************************************************************/
function allTrim(sValue) {
  sValue = leftTrim(sValue);
  return rightTrim(sValue);
}

/***************************************************************************************
 * Trim sValue, remove all currency prefixes and commas and
 * parse to a float value. Return NaN if sValue is an invalid 
 * numeric value.
 * @param {variant} inVal the input value to onvert/parse to a double
 * @returns {Double} the converted value or NaN if unassigned
 **************************************************************************************/
function toFloat(inVal) {
  var result = Number.NaN;
  if ((inVal === null) || (inVal === undefined)) {
    return result;
  }
		
  var strVal = inVal.toString();
  strVal = allTrim(strVal);
  strVal = removeCurrency(strVal);
  if (isNaN(strVal)){
    return result;
  }		
  result = parseFloat(strVal);
  return result;
}

/***************************************************************************************
 * Trim sValue, remove all currency prefixes and commas and
 * parse to a integer value. Return NaN if sValue is an invalid 
 * numeric value.
 * @param {variant} inValue description
 * @returns {Integer} The parsed integer or NaN is unassigned
 **************************************************************************************/
function toInt(inValue) {
  var result = Number.NaN;
  if ((inValue === null) || (inValue === undefined)) {
    return result;
  }
		
  var strVal = inValue.toString();
  strVal = allTrim(strVal);
  strVal = removeCurrency(strVal);
  if (isNaN(strVal)) {
    return result;
  }
		
  result = parseInt(strVal);
  return result;
}

/***************************************************************************************
 * Convert inVal to a boolean value. Default value=false. Convert numbers, string, and
 * boolean values. If Number, result = (inVal <> 0). If String = inVal[0]="t|T", else
 * result = inVal.
 * @param {variant} inVal description
 * @returns {Booealn} The parsed boolean or false is unassigned
 **************************************************************************************/
function toBool(inVal) {
  var result = false;
  if ((inVal !== null) && (inVal !== undefined)) {
    var intVal = toInt(inVal);
    if (typeof(inVal) === 'boolean') {
      result = inVal;
    } else if (!isNaN(intVal)) {
      result = (intVal !== 0);
    } else if (typeof(inVal) === 'string') {
      inVal = allTrim(inVal).toLowerCase();
      result = (inVal.indexOf("t") === 0);
    }
  }
  return result;
}

/***************************************************************************************
 * Call to Pad a String with a character or a blank string to a sepcified length by 
 * prepending the character to the string
 * @param {Strng} sValue = the value to be padded
 * @param {Strng} sPadChar the value to page sValue with
 * @param {Integer} iLength the length of teh return string
 * @return {String} sValue padded to the left with sPadChar to length iLength or 
 * sValue if sValue.length >= iLength or sPadChar.length=0.
 **************************************************************************************/
function padLeft(sValue, sPadChar, iLength) {
  var result = sValue;
  if ((result.length < iLength) && (sPadChar.length > 0)) {
    while (result.length < iLength) {
      result = sPadChar + result;
    }
  }  
  return result;
}
/***************************************************************************************
 * Call to Pad a String with a character or a blank string to a sepcified length by 
 * appending the character to the string
 * @param {String} sValue = the value to be padded
 * @param {String} sPadChar the value to page sValue with
 * @param {Integer} iLength the length of the return string
 * @return {String} svalue padded to the right with sPadChar to length iLength. 
 * Return sValue if sValue.length >= iLength or sPadChar.length=0.
 **************************************************************************************/
function padRight(sValue, sPadChar, iLength) {
  var sResult = sValue;
  if ((sResult.length < iLength) && (sPadChar.length > 0)) {
    while (sResult.length < iLength) {
      sResult += sPadChar;
    }
  }  
  return sResult;
}

//*************************************************************
// Convert sValue to a Numeric string without currency ($)
//*************************************************************
function removeCurrency(sValue) 
{
  var pRegExp = new RegExp("[\(]");
  var sMinus = '';

  //check if negative
  var pResult = pRegExp.exec(sValue);
  if(pResult != null) {
    sMinus = '-';
  }
  
  pRegExp = new RegExp("[\)]|[\(]|[,]","g");
  pResult = pRegExp.exec(sValue);
  if (pResult != null) {
    sValue = sValue.replace(pRegExp,'');
  }
  
  var iPos = sValue.indexOf('$');
  if(iPos >=  0)
    sValue = sValue.substring(iPos+1, sValue.length-iPos);
  sValue = allTrim(sValue);

  return sMinus + sValue;
}

/***************************************************************************************
 * Convert sValue to a currency ($) string with 2 Decimals
 **************************************************************************************/
function addCurrency(sValue) 
{
  var sCurStr = "$0.00";
  var dVal = toFloat(sValue);
  if (!isNaN(dVal)) {    	  
    dVal = Math.round(dVal*100)/100;
    var sValStr = dVal.toFixed(2);
    sValStr = addCommas(sValStr);

    var pRegExp = new RegExp("^-");
    var pResult = pRegExp.exec(sValStr);
    if (pResult != null) {
      sValStr = "($"+sValStr.replace(pRegExp,'')+")";
    } else {
      sValStr = "$"+sValStr;
    }
    sCurStr = sValStr;
  }
  return sCurStr;
}

/***************************************************************************************
 *  Convert sValue to a currency ($) string with 2 Decimals
 **************************************************************************************/
function addCommas(sValue)
{
  var sDecStr = "";
  var sValStr = "";
  var pRegExp  = new RegExp("(-?[0-9]*)\.([0-9]*)");
  var pResult = pRegExp.exec(sValue);
  if (pResult != null)
  {
    sValStr = pResult[1];
    sDecStr = pResult[2];
  }
  else
    sValStr = sValue;
  
  pRegExp  = new RegExp("(-?[0-9]+)([0-9]{3})");
  pResult = pRegExp.exec(sValStr);
  if (pResult != null)
  {
    var sNewStr = "";
    var sRemStr = "";
    while (pResult != null)
    {
      sRemStr = pResult[1];
      var sSubStr = pResult[2];
      if (sSubStr != "")
      {
        if (sNewStr != "")
          sSubStr += ",";
        sNewStr = sSubStr+sNewStr;
      }
      pResult = pRegExp.exec(sRemStr);
    }
		
    if (sRemStr == "-")
      sValStr = sRemStr + sNewStr;
    else if (sRemStr != "")
      sValStr = sRemStr + "," + sNewStr;
  }
  
  if (sDecStr == "")
    return sValStr;
  else
    return sValStr+"."+sDecStr;
}

//*************************************************************
// Convert sValue to a currency ($) string with 2 Decimals
//*************************************************************
function removeCommas(sValue)
{
  var pRegExp  = new RegExp(",","g");

  //check for match to search criteria
  var pResult = pRegExp.exec(sValue);
  if (pResult != null)
    sValue = sValue.replace(pRegExp, '');
  return sValue;
}

//*************************************************************
// Calculate the sum of sVal1 and sVal2 and return a total.
// Set the input to 0.00 if invalid or return 0.00 if an error occur.
//*************************************************************
function calcSum(sVal1,sVal2)
{
  var dOutput = 0.0;
  try
  {
    var dVal1 = toFloat(sVal1);
    if (isNaN(dVal1))
      dVal1 = 0.0;
			
    var dVal2 = toFloat(sVal2);
    if (isNaN(dVal2))
      dVal2 = 0.0;
			
    dOutput = dVal1 + dVal2;
  }
  catch (ex)
  {
    alert(ex.message);
    dOutput = 0.0;
  }
  return dOutput;
}

//*************************************************************
// Calculate the differance between sVal1 and sVal2 and return a result.
// Set the input to 0.00 if invalid or return 0.00 if an error occur.
//*************************************************************
function calcDiff(sVal1,sVal2)
{
  var dOutput = 0.0;
  try
  {
    var dVal1 = toFloat(sVal1);
    if (isNaN(dVal1))
      dVal1 = 0.0;
			
    var dVal2 = toFloat(sVal2);
    if (isNaN(dVal2))
      dVal2 = 0.0;
			
    dOutput = dVal1 - dVal2;
  }
  catch (ex)
  {
    alert(ex.message);
    dOutput = 0.0;
  }
  return dOutput;
}

//*************************************************************
// Calculate sVal1/sVal2 and return a output.
// Return 0.00 if sVal1 is invalid and NaN is sVal2=0 is invalid.
//*************************************************************
function calcDivide(sVal1,sVal2)
{
  var dOutput = 0.0;
  try
  {
    var dVal1 = toFloat(sVal1);
    if (isNaN(dVal1))
      dVal1 = 0.0;
			
    var dVal2 = toFloat(sVal2);
    if (isNaN(dVal2))
      dVal2 = 0.0;
			
    if (dVal2 == 0.0)
      dOutput = Number.NaN;
    else
      dOutput = dVal1/dVal2;
  }
  catch (ex)
  {
    alert(ex.message);
    dOutput = 0.0;
  }
  return dOutput;
}

//*************************************************************
// Calculate the product between sVal1 and sVal2 and return a
// a Currency string of the product. Return $0.00 if an error
// occur or any of the values are invalid.
//*************************************************************
function calcProduct(sVal1, sVal2)
{
  var dOutput = 0.0;
  try
  {
    var dVal1 = toFloat(sVal1);
    if (isNaN(dVal1))
      dVal1 = 0.0;
			
    var dVal2 = toFloat(sVal2);
    if (isNaN(dVal2))
      dVal2 = 0.0;
			
    dOutput = dVal1 * dVal2;
  }
  catch (ex)
  {
    alert(ex.message);
    dOutput = 0.0;
  }
  return dOutput;
}

//*************************************************************
// Assume aInput is [[sVal1,sVal2]].  It will calculate the product
// between each [sVal1,sVal2] set and return a total of all the 
// products.  Empty pairs will be skipped.
// Return $0.00 is an error occur or is aInput is not defined or empty.
//*************************************************************
function calcSumProduct(aInput)
{
  var dOutput = 0.0;
  try
  {
    if ((aInput != null) && (aInput.length > 0))
    {
      for (var iPair=0; iPair < aInput.length; iPair++)
      {
        var pPair = aInput[iPair];
        if ((pPair == null) || (pPair.length < 2))
          continue;
					
        var dProduct = calcProduct(pPair[0],pPair[1]);
        if (!isNaN(dProduct))
          dOutput += dProduct;
      }
    }
  }
  catch (ex)
  {
    alert(ex.message);
    dOutput = 0.0;
  }
  return dOutput;
}

//*************************************************************
// Calculate the product between sQty and sRate and return a
// a Currency string of the product. Return $0.00 is an error
// occur or any of the values are invalid.
//*************************************************************
function calcAmount(sQty, sRate)
{
  var sAmount = "$0.00";
  try
  {
    var dAmount = calcProduct(sQty,sRate);
    if (!isNaN(dAmount))
      sAmount = addCurrency(dAmount);
  }
  catch (ex)
  {
    alert(ex.message);
    sAmount = "$0.00";
  }
  return sAmount;
}

//*************************************************************
// Get the abs offset of Element[sElementid]
//*************************************************************
this.getElementAbsTop = function (sElementID) {
  var pItemElement = document.getElementById(sElementID);
  if (pItemElement == null)
    return 0;
		
  var iOffset = pItemElement.offsetTop;
  var pParent = pItemElement.offsetParent;
  while (pParent != null)  {
    iOffset += pParent.offsetTop;
    if (pParent.style.position != "fixed") {
      break;
    }else {
      pParent = pParent.offsetParent;
    }
  }
	
  //alert("Element["+pElement+"].AbsBottomOffset = "+iOffset);
  return iOffset;
}

//*************************************************************
// Get the abs Left of Element[sElementid]
//*************************************************************
this.getElementAbsLeft = function (sElementID) {
  var pItemElement = document.getElementById(sElementID);
  if (pItemElement == null)
    return 0;
		
  var iOffset = pItemElement.offsetLeft;
  var pParent = pItemElement.offsetParent;
  while (pParent  != null) {
    //alert("Parent["+pParent.tagName+"].offsetTop = "+pParent.offsetTop);
    iOffset += pParent.offsetLeft;
    if (pParent.style.position != "fixed") {
      break;
    } else {
      pParent = pParent.offsetParent;
    }
  }
  //alert("Element["+pElement+"].AbsBottomOffset = "+iOffset);
  return iOffset;
}

//*************************************************************
// Get the the LeftOffset by adding iOffset to pHostID.offsetleft
//*************************************************************
function AbsLeftOffset(pHostID, aOffset)
{
  var pHost;
  var iLeft = 0;
  var iOffset = 0;
  try
  {
    pHost = (pHostID == null)? null: window.document.getElementById(pHostID)
    if (pHost != null)
      iLeft = pHost.offsetLeft;
    else
      iLeft = pBody.offsetLeft;
		
    iOffset = toInt(aOffset);
    if ((!isNaN(iOffset))	&& (iOffset > 0))
      iLeft += iOffset;
  }
  catch (ex)	{
    alert(ex.message);
  }
		
  return iLeft;
}

//*************************************************************
// Get the the absolute LeftOffset by adding iOffset to Body.offsetleft
//*************************************************************
function AbsLeftOffset(aOffset) {
  var pBody;
  var iLeft = 0;
  var iOffset = 0;
  try {
    //alert("Check Form["+sFormID+"]: Elements["+pValidates.toString()+"]");
		
    pBody = window.document.body;
    if (pBody != null)
      iLeft = pBody.offsetLeft;

    iOffset = toInt(aOffset);
    if ((!isNaN(iOffset))	&& (iOffset > 0))
      iLeft += iOffset;
  }
  catch (ex) {
    alert(ex.message);
  }
		
  return iLeft.toFixed(0);
}

//*************************************************************
// Get the the TopOffset by adding iOffset to Body.offsetleft
//*************************************************************
function AbsTopOffset(aOffset) {
  var pBody;
  var iTop = 0;
  var iOffset = 0;
  try {
    //alert("Check Form["+sFormID+"]: Elements["+pValidates.toString()+"]");
		
    pBody = window.document.body;
    if (pBody != null)
      iTop = pBody.offsetTop;

    iOffset = toInt(aOffset);
    if ((!isNaN(iOffset))	&& (iOffset > 0))
      iTop += iOffset;
  }
  catch (ex) {
    alert(ex.message);
  }
		
  return iTop.toFixed(0);
}

/*
 * CSS Browser Selector v0.4.0 (Nov 02, 2010)
 * Rafael Lima (http://rafael.adm.br)
 * http://rafael.adm.br/css_browser_selector
 * License: http://creativecommons.org/licenses/by/2.5/
 * Contributors: http://rafael.adm.br/css_browser_selector#contributors
 * Modified: JG (Koos) Prins, GEI Consultants.
 * MUST CALL css_browser_selector(navigator.userAgent); in browse
 * @type String
 */
//function css_browser_selector(u){
//  var ua=u.toLowerCase();
//  var is=function(t){
//    return ua.indexOf(t)>-1;
//  };
//  var g='gecko';
//  var w='webkit';
//  var s='safari';
//  var o='opera';
//  var m='mobile';
//  var h=document.documentElement;
//  var b= '';
//  if (!(/opera|webtv/i.test(ua))&&/msie\s(\d)/.test(ua)) {
//    b= ('ie ie'+RegExp.$1);
//  } else if (is('firefox/2')) {
//    b = g + ' ff2';
//  } else if (is('firefox/3.5')) {
//    b = g + ' ff3 ff3_5';
//  } else if (is('firefox/3.6')) {
//    b = g + ' ff3 ff3_6';
//  } else if (is('firefox/3')) {
//    b = g + ' ff3';
//  } else if (is('gecko/')) {
//    b = g;
//  } else if (is('opera')) {
//    b = o;
//    if (/version\/(\d+)/.test(ua)) {
//      b += ' '+o+RegExp.$1;
//    } else if (/opera(\s|\/)(\d+)/.test(ua)) {
//      b += ' '+o+RegExp.$2
//    }
//  } else if (is('konqueror')) {
//    b = 'konqueror';
//  } else if (is('blackberry')) {
//    b = m + ' blackberry';
//  } else if (is('android')) {
//    b = m + ' android';
//  } else if (is('chrome')) {
//    b = w + ' chrome';
//  } else if (is('iron')) {
//    b = w + ' iron';
//  } else if (is('applewebkit/')) {
//    b = w + ' ' + s;
//    if (/version\/(\d+)/.test(ua)) {
//      b += ' '+s+RegExp.$1;
//    }
//  } else if (is('mozilla/')) {
//    b = g;
//  }
//
//  var os= '';
//  if (is('j2me')) {
//    os = m + ' j2me';
//  } else if (is('iphone')) {
//    os = m + ' iphone';
//  } else if (is('ipod')) {
//    os = m + ' ipod';
//  } else if (is('ipad')) {
//    os = m + ' ipad';
//  } else if (is('mac')) {
//    os = 'mac';
//  } else if (is('darwin')) {
//    os = 'mac';
//  } else if (is('webtv')) {
//    os = 'webtv';
//  } else if (is('win')) {
//    os = 'win';
//    if (is('windows nt 6.0')) {
//      os += ' vista';
//    }
//  } else if (is('freebsd')) {
//    os = 'freebsd';
//  }else if ((is('x11')) || (is('linux'))) {
//    os = 'linux';
//  }
//
//  b += (b == '')? os: ' ' + os;
//  b += (b == '')? 'js': ' js';
//  h.className += ' '+b;
//  return b;
//}
//
///**
// * Check whether the current browser is BrowserType[id] (e.g. 'ie','gecko') - return
// * true or false
// * @type boolean;
// */
//function isBrowser(id) {
//  var bResult = false;
//  if ((id != null) && (id != "")) {
//    id = id.toLowerCase();
//    var sTagClass = document.documentElement.className;
//    if ((sTagClass != null) && (sTagClass != "")) {
//      sTagClass = sTagClass.toLowerCase();
//      bResult = (sTagClass.indexOf(id) > -1);
//    }
//  }
//  return bResult;
//}

///**
// * Clip the content of an container to the size of the container and add an ellipsis.
// * It identify the elements if its class definition contains the sEllipsisClass class
// * name. It will regard it as an single line element if the sMultilineClass is set and
// * the element's class name include sMultilineClass.
// */
///*function setEllipsis(sEllipsisClass, sMultilineClass) {
//  if (((sEllipsisClass != null) && (sEllipsisClass != ""))) {
//    return false;
//  }
//  sEllipsisClass = allTrim(sEllipsisClass.toLowerCase());
//
//  var bDoMiltiLine = ((sMultilineClass != null) && (sMultilineClass != ""));
//  if (bDoMiltiLine) {
//    sMultilineClass = allTrim(sMultilineClass.toLowerCase());
//  }
//
//  var bIsGecko = isBrowser("gecko");
//  var bIsOpera = isBrowser("opera");
//
//  var getType = function(pElem) {
//    var eType = 0;
//    var sClassName = (pElem == null)? "": pElem.className.toLowerCase();
//    if (sClassName != "") {
//      var pNames = sClassName.split(/[ ]/);
//      if (pNames.length > 0) {
//        for (var i = 0; i < (pNames.length-1); i++) {
//          var sName = pNames[i];
//          if (sName == sEllipsisClass) {
//            eType += 1;
//          } else if ((bDoMiltiLine) && (sName == sMultilineClass)) {
//            eType += 2;
//          }
//        }
//      }
//    }
//    return eType;
//  }
//
//  var setChildren = function(pElem) {
//    if (pElem == null) {
//      return;
//    }
//    var pChildren = pElem.childNodes;
//    if ((pChildren != null) && (pChildren.length > 0)) {
//      for (var iChild = 0; iChild < (pChildren.length-1); iChild++) {
//        var pChild = pChildren[iChild];
//        setChildren(pChild);
//      }
//    } else {
//      var eType = getType(pElem);
//      var pInnerText = pElem.innerHTML
//      if (eType == 1) {
//        if ((pInnerText != null) && (pInnerText.get < pElem.) {
//
//      }
//      } else if (eType == 3) {
//
//      }
//    }
//  }
//
//
//  return true;
//}
//
//(function($) {
//  $.fn.ellipsis = function() {
//    return this.each(function() {var el = $(this);
//      if(el.css("overflow") == "hidden")
//      {
//        var text = el.html();
//        var multiline = el.hasClass('multiline');
//        var t = $(this.cloneNode(true))
//        .hide()
//        .css('position', 'absolute')
//        .css('overflow', 'visible')
//        .width(multiline ? el.width() : 'auto')
//        .height(multiline ? 'auto' : el.height());
//        el.after(t);
//        function height() { return t.height() > el.height(); };
//        function width() { return t.width() > el.width(); };
//        var func = multiline ? height : width;
//        while (text.length > 0 && func()) {
//          text = text.substr(0, text.length - 1);
//          t.html(text + "...");
//        }
//        el.html(t.html());
//        t.remove();
//      }
//    });
//  };
//})(jQuery); */
 
