<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
  "http://www.w3.org/TR/html4/loose.dtd">

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><meta name="apple-mobile-web-app-title" content="CDEC Importer"/>
    <meta name="application-name" content="CDEC Importer"/>
    <meta name="msapplication-TileColor" content="#da532c"/>
    <meta name="msapplication-TileImage" 
          content="/resources/icons/apple-touch-icon-114x114.png"/>

    <link rel="apple-touch-icon-precomposed" sizes="114x114" 
          href="resources/icons/apple-touch-icon-114x114.png" />
    <link rel="icon" type="image/png" sizes="192x192" 
          href="resources/icons/favicon-196x196.png" />
    <link rel="icon" type="image/png" sizes="96x96"
          href="resources/icons/favicon-96x96.png" />
    <link rel="icon" type="image/png" sizes="32x32" id="favicon" 
          href="resources/icons/favicon-32x32.png"/>
    <link rel="icon" type="image/png" sizes="16x16" 
          href="resources/icons/favicon-16x16.png" />
    
    <script src="resources/js/jquery/jquery.min.js"
    type="text/javascript"></script>
    <script src="resources/js/globals.js"
    type="text/javascript"></script>
    <script src="resources/js/myapp.js"
    type="text/javascript"></script>
    <script src="resources/js/cdec.import.js"
    type="text/javascript"></script>

    <!-- <h:meta http-equiv="Content-Type" content="text/html; charset=MacRoman"> -->
    <title>GEI CDEC Data Importer</title>
    
    <style type="text/css">
      .created {color: green}
      .uniqueId {color: red}
      .message {color: black}

      h1 {
        font-family: sans-serif;
        font-size: 16pt;
        font-weight: bold;
        width: auto;
        text-align: center;
        border-bottom: 3px #E3E3E3 ridge;
        width: auto;
      }
      h2 {
        font-family: sans-serif;
        font-size: 14pt;
        font-weight: bold;
        border-bottom: 3px #E3E3E3 ridge;
        width: auto;
      }
      div.pageBody {
        width: 700px;
        min-width: 600px;
      }
      
      td.requestForms {
        width: 50%;
        text-align: left;
        vertical-align: top;
      }
      td.requestForms h3 {
        font-family: sans-serif;
        font-size: 14pt;
        font-weight: bold;
      }
      
      div#requestQueue,  div#historyQueue, div#execRequest {
        border: 1px solid #F3F3F3;
        background-image: linear-gradient(#F6F6F6, #FFF);
        border-radius: 4px;
        color: black; 
        color: black; 
        height: auto; 
        min-height: 50px;
        width: auto;
        vertical-align: top;
      }
      div#execRequest {
        height: 25px; 
        min-height: 25px;
        padding: 0;
      }

      div#requestQueue ul,  div#historyQueue ul {
        margin-top: 0;
        padding: 0 0 0 20px;        
      }
      td.requestForms p,
      div#requestQueue p,  div#historyQueue p, div#execRequest p {
        font-weight: normal;
        margin: 4px 0 0 8px;
        font-family: sans-serif;
        font-size: 10pt;
      }
      div#requestQueue ul li,  div#historyQueue ul li {
        margin: 4px 0 0 0;
        font-family: sans-serif;
        font-size: 10pt;
      }
      div#execProgress {
        position: relative;        
        border: 1px solid #F3F3F3;
        background-image: linear-gradient(#F6F6F6, #FFF);
        border-radius: 4px;
        color: black; 
        height: 20px;
        min-height: 20px;
        max-height: 20px; 
        width: 300px;
        min-width: 300px;
        max-width: 300px;
        margin: 8px 0 0 0;
        background-color: #FFFFFF;
      }

      div#execProgressBar {
        position: relative;
        text-align: center;
        vertical-align: middle;
        left: 0%;
        width: 0%;
        height: 18px;
        border: none;
        padding: 2px 0 0 0;
        margin: 0;
        color: #FFFFFF;
        font-family: sans-serif;
        font-size: 10pt;
        background-color: steelblue;
        background-image: radial-gradient(lightblue, steelblue);
        z-index: 99;
      }
      div#execProgressBar span {
        color: #FFFFFF;
        font-family: sans-serif;
        font-size: 10pt;        
      }
      div.inputRow {
        position: relative;
        display: block;
        text-align: left;
        vertical-align: top;
        width: 100%;
        height: auto;
      }
      div.inputCells {
        display: block;
        text-align: left;
        vertical-align: top;
        float: left;
        margin: 0 10px 0 0;
      }
      div.inputTips {
        display: block;
        text-align: left;
        vertical-align: top;
        width: 100%;
        margin: 4px 0 0 0;
      }
      div.inputTip span {
        font-family: sans-serif;
        font-size: 7pt;
        display: block;
      }
      input[type="text"].test {
        background-color: #F3F3F3;
        background-image: linear-gradient(#F6F6F6, #FFF);
        border-radius: 4px;
        border: 1px solid #E3E3E3;
        height: 25px;
        padding-left: 5px;
        position: relative;
        width: 135px;
      }
      input[type="text"].sensorIds {
        width: 250px;
      }
      label {
        font-family: sans-serif;
        font-size: 9pt;
        display: block;
      }
      div.statusLabel {
        margin-top: 10px;
        width: 100%;
      }
      div.statusLabel {
        font-weight: bold;
      }
      span.errmsg {
        color: red;        
      }
      span.errmsg:before {
        content: "Error: ";
        color: red;  
      }
      span.smallText {
        font-family: sans-serif;
        font-size: 8pt;
      }
      div.errmsg {
        margin: 10px 0 0 0;
        width: 100%;
      }
      input#ddEndDate {
        max-width: 100px;
        width: 100px;
      }
      input#ddNumDays, input#mdNumRecs, input#b120NumYrs, input#b120EndWy {
        max-width: 75px;
        width: 75px;
      }
      input#mdEndYear, input#mdEndMonth{
        max-width: 50px;
        width: 50px;
      }
      tr#ddErrorRow, tr#mdErrorRow, tr#ssErrorRow, tr#porErrorRow,tr#b120ErrorRow {
        display: none;
      }
      div#histResetRequest, div#stopExecRequest {
        width: auto;
        float: right;
        margin: -10px 0 0 0;
      }
    </style>
    
    <script type="text/javascript">
      /* <![CDATA[ */
      var hostUrl = webctx.getImportUrl();
      var updateTimer = null;

      function getStatus() {
        try {
          var url = hostUrl + "/status";

          $.getJSON(url, parseStatus);
        } catch (err) {
          alert(err.message);
        } 
      };

      function startStatusUpdates() {
        if (updateTimer === null) {
          updateTimer = window.setInterval(function() {
            getStatus();
          }, 1000);
        }
      };

      function stopStatusUpdates() {
        if (updateTimer !== null) {
          window.clearInterval(updateTimer);
          updateTimer = null;
        }
      };

      function parseStatus(data) {
        //var content = $( data ).find( "#content" );
        var reqQue = null;
        var reqExec = null;
        var histQue = null;
        if (data !== null) {
          reqQue = data.requests;
          reqExec = data.executing;
          histQue = data.history;
        }
        var hasPending = (updateReqQueue(reqQue));
        var isExecuting = (updateExecuting(reqExec));
        updateHistQueue(histQue);
        if ((hasPending) || (isExecuting)) {
          startStatusUpdates();
        } else {
          stopStatusUpdates();
        }
      };

      function updateReqQueue(reqQue) {
        var result = false;
        if ((reqQue !== null) && (reqQue !== undefined) &&
                (reqQue instanceof Array) && (reqQue.length > 0)) {
          var reqHtml = "<ul>";
          $(reqQue).each(function() {
            reqHtml += "<li>"
                    + this.requestType;
            if ((this.startDate !== null) && (this.startDate !== undefined)) {
              reqHtml += "; startDt = " + this.startDate;
            }
            if ((this.endDate !== null) && (this.endDate !== undefined)) {
              reqHtml += "; endDt = " + this.endDate;
            }
            if ((this.sensorId !== null) && (this.sensorId !== undefined)) {
              reqHtml += "; sensorId = " + this.sensorId;
            }
            reqHtml += "; status = " + this.status + "</li>";
          });
          /*     var req = null;
           var i;
           for (i = 0; i < reqQue.length; i++) {
           req = reqQue[i];
           if ((req !== null) && (req !== undefined)) {
           reqHtml += "<li>" 
           + req.requestType + "; startDt = "
           + req.startDate + "; endDt = "
           + req.endDate + "; status = "
           + req.status + "</li>";
           }
           };*/
          reqHtml += "</ul>";
          $("#requestQueue").empty().html(reqHtml);
          result = true;
        } else {
          $("#requestQueue").empty().html("<p>Empty</p>");
        }
        return result;
      };

      function updateExecuting(reqExec) {
        var result = false;
        var reqHtml = "<p>None</p>";
        var width = "0%";
        if ((reqExec !== null) && (reqExec !== undefined)) {
          reqHtml = "<p>" + reqExec.requestType;                  
          if ((reqExec.startDate !== null) && (reqExec.startDate !== undefined)) {
            reqHtml += "; startDt = " + reqExec.startDate;
          }
          if ((reqExec.endDate !== null) && (reqExec.endDate !== undefined)) {
            reqHtml += "; endDt = " + reqExec.endDate;
          }
          if ((reqExec.sensorId !== null) && (reqExec.sensorId !== undefined)) {
            reqHtml += "; sensorId = " + reqExec.sensorId;
          }
          reqHtml += "; status = " + reqExec.status + "</p>";
          width = reqExec.percCompleted + "%";
          result = true;
        }
        $("#execRequest").empty().html(reqHtml);
        $("#execProgressBar").innerWidth(width);
        if (width !== "0%") {
          $("#execProgressBar").empty().html("<span>" + width + "</span>");
        } else {
          $("#execProgressBar").empty().html("");
        }
        return result;
      };

      function updateHistQueue(histQue) {
        var result = false;
        var histHtml = "<p>Empty</p>";
        if ((histQue !== null) && (histQue !== undefined) &&
                (histQue instanceof Array) && (histQue.length > 0)) {
          histHtml = "<ul>";
          $(histQue).each(function() {
            histHtml += "<li>"
                    + this.requestType;                  
            if ((this.startDate !== null) && (this.startDate !== undefined)) {
              histHtml += "; startDt = " + this.startDate;
            }
            if ((this.endDate !== null) && (this.endDate !== undefined)) {
              histHtml += "; endDt = " + this.endDate;
            }
            if ((this.sensorId !== null) && (this.sensorId !== undefined)) {
              histHtml += "; sensorId = " + this.sensorId;
            }
            histHtml += "; status = " + this.status;
            if ((this.error !== null) && (this.error !== undefined)) {
              histHtml += "<br/><span class='errmsg'>" + this.error + "</span>";
            }
            histHtml += "</li>";
          });
          histHtml += "</ul>";
          result = true;
        }
        $("#historyQueue").empty().html(histHtml);
        return result;
      };
      /* ]]> */
    </script>
  </head>

  <body>    
    <div class="pageBody" >
      <h1>GEI CDEC Data Importer Services</h1>
      <table border="0" cellpadding="0" cellspacing="0"
             style="width:100%;" >
        <tr>
          <td class="requestForms">
            <form id="stationSensorForm" action="/">
              <h3>Import Station-Sensor Data</h3>
              <table border="0" cellpadding="0" cellspacing="0"
                     style="width: auto;">
                <tr>
                  <td style="width: 300px;">
                    <p>Select to import all CDEC Station and Sensor record to update the
                    local tables.</p>
                  </td>
                </tr>
                <tr>
                  <td style="text-align: right; padding: 8px 10px 0 0;">
                    <button type="submit"
                            tabindex="20">Submit</button>            
                  </td>
                </tr>
                <tr id="ssErrorRow">
                  <td>
                    <div class="errmsg">
                      <span id="ssErrMsg" class="errmsg smallText"></span>
                    </div>
                  </td>
                </tr>
              </table>      
            </form>
          </td>
          <td class="requestForms">
            <form id="porForm" action="/">
              <h3>Import Period-of-Record Data</h3>
              <table border="0" cellpadding="0" cellspacing="0"
                     style="width: auto;">
                <tr>
                  <td style="width: auto;">
                    <div class="inputRow">
                      <div class="inputCells">
                        <label for="porSensorIds">Record End Date</label>
                        <input id="porSensorIds"
                               class="test sensorIds" 
                               type="text" 
                               tabindex="0" 
                               placeholder="99999,99999,9999,.."
                               onkeypress="return onKeypressIntList(event);"></input>
                      </div>
                    </div>
                  </td>
                </tr>                
                <tr>
                  <td>
                    <div class="inputTip">
                      <span>Enter one or more comma (",") delimited sensorIds </span>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td style="text-align: right; padding: 8px 10px 0 0;">
                    <button type="submit"
                            tabindex="2">Submit</button>            
                  </td>
                </tr>
                <tr id="porErrorRow">
                  <td>
                    <div class="errmsg">
                      <span id="porErrMsg" class="errmsg smallText"></span>
                    </div>
                  </td>
                </tr>
              </table>      
            </form>
          </td>
        </tr>
        <tr>
          <td class="requestForms">
            <form id="dialyDataForm" action="/">
              <h3>Import Daily Data</h3>
              <table border="0" cellpadding="0" cellspacing="0"
                     style="width: auto;">
                <tr>
                  <td style="width: auto;">
                    <div class="inputRow">
                      <div class="inputCells">
                        <label for="ddEndDate">End Date</label>
                        <input id="ddEndDate"
                               class="test" 
                               type="text" 
                               tabindex="3" 
                               placeholder="YYYY-MM-DD"
                               onkeypress="return onKeypressInteger(event);"
                               maxlength="30"></input>
                      </div>
                      <div class="inputCells">
                        <label for="ddNumDays">Records (days)</label>
                        <input id="ddNumDays"
                               class="test" 
                               type="text" 
                               tabindex="4" 
                               placeholder="Days"
                               onkeypress="return onKeypressInteger(event);"
                               maxlength="3"></input>
                      </div>
                    </div>
                    
                  </td>
                </tr>
                <tr>
                  <td>
                    <div class="inputTip">
                      <span>Default <i>End Date</i> = today; Default <i>Records</i> = 60 days</span>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td style="text-align: right; padding: 8px 10px 0 0;">
                    <button type="submit"
                            tabindex="5">Submit</button>            
                  </td>
                </tr>
                <tr id="ddErrorRow">
                  <td>
                    <div class="errmsg">
                      <span id="ddErrMsg" class="errmsg smallText"></span>
                    </div>
                  </td>
                </tr>
              </table>      
            </form>
          </td>
          <td class="requestForms">
            <form id="monthlyDataForm" action="/">
              <h3>Import Monthly Data</h3>
              <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td>
                    <div class="inputRow">
                      <div class="inputCells">
                        <label for="mdEndYear">Record End Date</label>
                        <input id="mdEndYear"
                               class="test" 
                               type="text" 
                               tabindex="6" 
                               placeholder="YYYY"
                               onkeypress="return onKeypressInteger(event);"
                               maxlength="4"></input>
                        <input id="mdEndMonth"
                               class="test" 
                               type="text" 
                               tabindex="7" 
                               placeholder="MM"
                               onkeypress="return onKeypressInteger(event);"
                               maxlength="2"></input>
                      </div>
                      <div class="inputCells">
                        <label for="mdNumRecs">Records (months)</label>
                        <input id="mdNumRecs"
                               class="test" 
                               type="text" 
                               tabindex="8" 
                               placeholder="Months"
                               onkeypress="return onKeypressInteger(event);"
                               maxlength="3"></input>
                      </div>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td>
                    <div class="inputTip">
                      <span>Default <i>End Date</i> = today; Default <i>Records</i> = 6 months</span>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td style="text-align: right; padding: 8px 10px 0 0;">
                    <button type="submit"
                            tabindex="9">Submit</button>            
                  </td>
                </tr>
                <tr id="mdErrorRow">
                  <td>
                    <div class="errmsg">
                      <span id="mdErrMsg" class="errmsg smallText"></span>
                    </div>
                  </td>
                </tr>
              </table>      
            </form>
          </td>
        </tr>
        
        <tr>
          <td class="requestForms">
            <form id="b120DataForm" action="/">
              <h3>Import B-120 Forecast Data</h3>
              <table border="0" cellpadding="0" cellspacing="0"
                     style="width: auto;">
                <tr>
                  <td style="width: auto;">
                    <div class="inputRow">
                      <div class="inputCells">
                        <label for="b120EndWy">End WY</label>
                        <input id="b120EndWy"
                               class="test" 
                               type="text" 
                               tabindex="10" 
                               placeholder="YYYY"
                               onkeypress="return onKeypressInteger(event);"
                               maxlength="4"></input>
                      </div>
                      <div class="inputCells">
                        <label for="b120NumYrs">Num Yrs</label>
                        <input id="b120NumYrs"
                               class="test" 
                               type="text" 
                               tabindex="11" 
                               placeholder="Years"
                               onkeypress="return onKeypressInteger(event);"
                               maxlength="2"></input>
                      </div>
                    </div>                    
                  </td>
                </tr>
                <tr>
                  <td>
                    <div class="inputTip">
                      <span>Default <i>End WY</i> = Current WY; Default <i>Num Yrs</i> = 1</span>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td style="text-align: right; padding: 8px 10px 0 0;">
                    <button type="submit"
                            tabindex="12">Submit</button>            
                  </td>
                </tr>
                <tr id="b120ErrorRow">
                  <td>
                    <div class="errmsg">
                      <span id="b120ErrMsg" class="errmsg smallText"></span>
                    </div>
                  </td>
                </tr>
              </table>      
            </form>
          </td>
          <td class="requestForms">
            <!-- Placeholder of a Hourly Import Form
            <form id="monthlyDataForm" action="/">
              <h3>Import Monthly Data</h3>
              <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td>
                    <div class="inputRow">
                      <div class="inputCells">
                        <label for="mdEndYear">Record End Date</label>
                        <input id="mdEndYear"
                               class="test" 
                               type="text" 
                               tabindex="6" 
                               placeholder="YYYY"
                               onkeypress="return onKeypressInteger(event);"
                               maxlength="4"></input>
                        <input id="mdEndMonth"
                               class="test" 
                               type="text" 
                               tabindex="7" 
                               placeholder="MM"
                               onkeypress="return onKeypressInteger(event);"
                               maxlength="2"></input>
                      </div>
                      <div class="inputCells">
                        <label for="mdNumRecs">Records (months)</label>
                        <input id="mdNumRecs"
                               class="test" 
                               type="text" 
                               tabindex="8" 
                               placeholder="Months"
                               onkeypress="return onKeypressInteger(event);"
                               maxlength="3"></input>
                      </div>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td>
                    <div class="inputTip">
                      <span>Default <i>End Date</i> = today; Default <i>Records</i> = 6 months</span>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td style="text-align: right; padding: 8px 10px 0 0;">
                    <button type="submit"
                            tabindex="9">Submit</button>            
                  </td>
                </tr>
                <tr id="mdErrorRow">
                  <td>
                    <div class="errmsg">
                      <span id="mdErrMsg" class="errmsg smallText"></span>
                    </div>
                  </td>
                </tr>
              </table>      
            </form>
            -->
          </td>
        </tr>
      </table>
      
      <h2>CDEC Import Request Status</h2>
      <div class="statusLabel"><label>Pending Requests</label></div>
      <div id="requestQueue" >           
      </div>

      <div class="statusLabel">
        <label style="display: inline;">Executing Request</label>
        <div id="stopExecRequest">
          <form id="stopExecForm">
            <button type="submit"
                    tabindex="98">Stop Imports</button>
          </form>
        </div>
      </div>
      <div id="execRequest">
      </div>
      <div id="execProgress" 
           style="border: 1px outset #003399; color: black; min-height: 20px; width: 100%">
        <div id="execProgressBar" style="width: 0%;">&nbsp;</div>
      </div>

      <div class="statusLabel">
        <label style="display: inline;">History Queue</label>
        <div id="histResetRequest">
          <form id="resetHistForm">
            <button type="submit"
                    tabindex="99">Reset History</button>
          </form>
        </div>
      </div>
      <div id="historyQueue" >
      </div>
    </div>
    <script type="text/javascript">
      /* <![CDATA[ */
      $("#b120DataForm").submit(function(event) {
        var url = hostUrl + "/b120";
        // Stop form from submitting normally
        event.preventDefault();
        // Get some values from elements on the page:
        var $form = $(this);
        var endWy = $form.find("input[id='b120EndWy']").val();
        if ((endWy === null) || (endWy === undefined)) {
          endWy = "";
        }
        var numYrs = $form.find("input[id='b120NumYrs']").val();
        if ((numYrs === null) || (numYrs === undefined)) {
          numYrs = "";
        }
        var postStr = "";
        if ((endWy !== "") || (numYrs !== "")) {
          postStr = "{";
          if (endWy !== "") {
            postStr += "\"endwy\":" + endWy;
            if (numYrs !== "") {
              postStr += ",";
            }
          }
          if (numYrs !== "") {
            postStr += "\"numyrs\":" + numYrs;
          }
          postStr += "}";
        }
        // Send the data using post
        $.post(url, postStr, function(data) {
            var obj = null;
            $( "#b120ErrorRow").css("display", "none");
            try {
              obj = jQuery.parseJSON(data);
              if ((obj !== null) && (obj !== undefined) &&
                            (obj.error !== null) && (obj.error !== undefined)) {
                $( "#b120ErrorRow").css("display", "block");
                $( "#b120ErrMsg").empty().html(obj.error);
              }
            } catch(err) {
              alert(err.message);
            }
            getStatus();
          });
      });
      
      $("#stationSensorForm").submit(function(event) {
        var url = hostUrl + "/stationsensor";
        // Stop form from submitting normally
        event.preventDefault();
        // Send the data using post
        $.post(url, "[]", function(data) {
            var obj = null;
            $( "#ssErrorRow").css("display", "none");
            try {
              obj = jQuery.parseJSON(data);
              if ((obj !== null) && (obj !== undefined) &&
                            (obj.error !== null) && (obj.error !== undefined)) {
                $( "#ssErrorRow").css("display", "block");
                $( "#ssErrMsg").empty().html(obj.error);
              }
            } catch(err) {
              alert(err.message);
            }
            getStatus();
          });
      });
      
      
      $("#porForm").submit(function(event) {
        var url = hostUrl + "/por";
        // Stop form from submitting normally
        event.preventDefault();
        // Get some values from elements on the page:
        var $form = $(this);
        var idList = $form.find("input[id='porSensorIds']").val();
        if ((idList === null) || (idList === undefined)) {
          idList = "";
        }
        var postStr = "";
        if (idList !== "") {
          postStr = "[" + idList + "]";
        }

        // Send the data using post
        $.post(url, postStr, function(data) {
            var obj = null;
            $( "#porErrorRow").css("display", "none");
            try {
              obj = jQuery.parseJSON(data);
              if ((obj !== null) && (obj !== undefined) &&
                            (obj.error !== null) && (obj.error !== undefined)) {
                $( "#porErrorRow").css("display", "block");
                $( "#porErrMsg").empty().html(obj.error);
              }
            } catch(err) {
              alert(err.message);
            }
            getStatus();
          });
        // Put the results in a div
        /*posting.done(function( data ) {
         var content = $( data ).find( "#content" );
         $( "#result" ).empty().append( content );
         });*/
      });
      
      $("#dialyDataForm").submit(function(event) {
        var url = hostUrl + "/daily";
        // Stop form from submitting normally
        event.preventDefault();
        // Get some values from elements on the page:
        var $form = $(this);
        var endDt = $form.find("input[id='ddEndDate']").val();
        if ((endDt === null) || (endDt === undefined)) {
          endDt = "";
        }
        var numDays = $form.find("input[id='ddNumDays']").val();
        if ((numDays === null) || (numDays === undefined)) {
          numDays = "";
        }
        var postStr = "";
        if ((endDt !== "") || (numDays !== "")) {
          postStr = "{";
          if (endDt !== "") {
            postStr += "\"enddate\": \"" + endDt + "\"";
            if (numDays !== "") {
              postStr += ",";
            }
          }
          if (numDays !== "") {
            postStr += "\"days\":" + numDays;
          }
          postStr += "}";
        }

        // Send the data using post
        $.post(url, postStr, function(data) {
            var obj = null;
            $( "#ddErrorRow").css("display", "none");
            try {
              obj = jQuery.parseJSON(data);
              if ((obj !== null) && (obj !== undefined) &&
                            (obj.error !== null) && (obj.error !== undefined)) {
                $( "#ddErrorRow").css("display", "block");
                $( "#ddErrMsg").empty().html(obj.error);
              }
            } catch(err) {
              alert(err.message);
            }
            getStatus();
          });
        // Put the results in a div
        /*posting.done(function( data ) {
         var content = $( data ).find( "#content" );
         $( "#result" ).empty().append( content );
         });*/
      });
      
      $("#monthlyDataForm").submit(function(event) {
        var url = hostUrl + "/monthly";
        // Stop form from submitting normally
        event.preventDefault();
        // Get some values from elements on the page:
        var $form = $(this);
        var endDt = "";
        var endYr = $form.find("input[id='mdEndYear']").val();
        var endMon = $form.find("input[id='mdEndMonth']").val();
        if (myapp.strings.rightTrim(endYr) !== "") {
          endDt = endYr;
          if ((endMon !== null) && (endMon !== undefined)) {
            if (endMon.length < 2) {
              endMon = "0" + endMon;
            }
            endDt += "-" + endMon + "-01";
          } else {
            endDt += "-01-01";
          }
        }
        
        var numMonths = $form.find("input[id='mdNumRecs']").val();
        if ((numMonths === null) || (numMonths === undefined)) {
          numMonths = "";
        }
        var postStr = "";
        if ((endDt !== "") || (numMonths !== "")) {
          postStr = "{";
          if (endDt !== "") {
            postStr += "\"enddate\": \"" + endDt + "\"";
            if (numMonths !== "") {
              postStr += ",";
            }
          }
          if (numMonths !== "") {
            postStr += "\"months\":" + numMonths;
          }
          postStr += "}";
        }

        // Send the data using post
        $.post(url, postStr, function(data) {
            var obj = null;
            $( "#mdErrorRow").css("display", "none");
            try {
              obj = jQuery.parseJSON(data);
              if ((obj !== null) && (obj !== undefined) &&
                            (obj.error !== null) && (obj.error !== undefined)) {
                $( "#mdErrorRow").css("display", "block");
                $( "#mdErrMsg").empty().html(obj.error);
              }
            } catch(err) {
              alert(err.message);
            }
            getStatus();
          });
        // Put the results in a div
        /*posting.done(function( data ) {
         var content = $( data ).find( "#content" );
         $( "#result" ).empty().append( content );
         });*/
      });
      
      $("#resetHistForm").submit(function(event) {
        var url = hostUrl + "/resethistory";
        // Stop form from submitting normally
        event.preventDefault();
        // Post Resquest
        $.post(url, "", function (data){
          getStatus();
        });
      });
      
      $("#stopExecForm").submit(function(event) {
        var url = hostUrl + "/stopexecute";
        // Stop form from submitting normally
        event.preventDefault();
        // Post Resquest
        $.post(url, "", function (data){
          getStatus();
        });
      });
      /**
       * Called get Status to get the current status
       */
      getStatus();
      /* ]]> */
    </script>
  </body>
</html>