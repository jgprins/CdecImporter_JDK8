package gov.water.cdec.importer.controllers;

//import com.google.gson.*;
//import com.google.gson.reflect.TypeToken;
import bubblewrap.http.session.HttpUtils;
import gov.ca.water.cdec.importers.ImportUtils;
import gov.water.cdec.importer.CdecImporter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * REST Web Service
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Controller
@RequestMapping("/import")
public class CdecImportController {

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(CdecImportController.class.getName());
  //</editor-fold>        
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">   
  @Autowired
  ServletContext context; 
  /**
   * The Data Initiation Error Message
   */
  private String errMsg;
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public CdecImportController() {
    super();
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Methods">
  /**
   * Get a Reference to the Singleton CdecImporter
   *
   * @return CdecImporter.getInstance()
   */
  private CdecImporter getImporter() {
    return CdecImporter.getInstance();
  }
  // </editor-fold>

  /**
   * Get the current {@linkplain CdecImporter#getImportStatus() CdecImporter.importStatus}
   * @param request the Servlet Request
   * @param response the Servlet Request
   * @throws IOException if an IO error occur.
   */
  @RequestMapping(
          path = "/status",
          method = RequestMethod.GET,
          produces = "application/json"
  )
  public void getStatus(HttpServletRequest request, 
                        HttpServletResponse response)
                        throws IOException {
    String result = null;
    try {      
      Calendar cal = Calendar.getInstance();
      Long now = cal.getTimeInMillis();
      CdecImporter importer = this.getImporter();
      if (importer != null) {
        result = importer.getImportStatus();
      }
      Long then = cal.getTimeInMillis();
      response.getWriter().write(result);
    } catch (Exception exp) {
      throw new IOException(this.getClass().getSimpleName()
              + ".getStatus Error:\n " + exp.getMessage());
    }
  }

  /**
   * POST method for launching the import of daily data.
   * @param jsonInput the expected format is "{"enddate": "yyyy-MM-dd", "months": ??}".
   * @return an HTTP response with content of the updated or created resource.
   */
  @RequestMapping(
          path = "/daily",
          method = RequestMethod.POST,
          consumes = "application/x-www-form-urlencoded"
  )
  public void importDailyData(@RequestBody String jsonInput,
                              HttpServletRequest request, 
                              HttpServletResponse response)
                              throws IOException {
    JSONObject result = new JSONObject();
    try {
      CdecImporter importer = this.getImporter();
      if (importer == null) {
        throw new Exception("The CDEC Importer is not accessible to handle the "
                + "Request[/import/daily]");
      }
      Date endDt = null;
      Integer numDays = null;
      JSONObject jsonObj = null; 
      jsonInput = HttpUtils.decodeString(jsonInput);
      if (((jsonInput = ImportUtils.cleanString(jsonInput)) != null) && 
              ((jsonObj = new JSONObject(jsonInput)) != null) &&
              (jsonObj.length() > 0)) {
        String strVal = null;
        String dtFormat = "yyyy-MM-dd";     
        if (jsonObj == null) {
          throw new Exception("Unable to parse the JsonInput");
        }

        System.out.println("Request = " + jsonObj.toString());

        if (((strVal = 
                    ImportUtils.cleanString(jsonObj.optString("enddate",""))) != null)){
          try {
            endDt = ImportUtils.dateFromString(strVal, dtFormat, null);
          } catch (Exception dtErr) {
            throw new Exception("Invalid input date '" + strVal 
                    + "'. Expected format '" + dtFormat + "'.");
          }
        }

        if ((numDays = jsonObj.optInt("days",-1)) <= 0){
          numDays = null;
        }
      }
      
      importer.importDailyData(endDt, numDays);
      result.put("status", "success");
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.importDailyData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      result.put("error", exp.getMessage());
    } finally {
      if ((result == null) || (result.length() == 0)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                "Requested process not found");
      } else {
        response.getWriter().write(result.toString());
      }
    }
  }

  /**
   * POST method for launching the import of monthly data. 
   * @param jsonInput the expected format is "{"enddate": "yyyy-MM-dd", "months": ??}".
   * @return an HTTP response with content of the updated or created resource.
   */
  @RequestMapping(
          path = "/monthly",
          method = RequestMethod.POST,
          produces = "application/json",
          consumes = "application/x-www-form-urlencoded"
  )
  public void importMonthlyData(@RequestBody String jsonInput,
                              HttpServletRequest request, 
                              HttpServletResponse response)
                              throws IOException {
    JSONObject result = new JSONObject();
    try {
      CdecImporter importer = this.getImporter();
      if (importer == null) {
        throw new Exception("The CDEC Importer is not accessible to handle the "
                + "Request[/import/monthly]");
      }
      //String jsonInput = null;
      Date endDt = null;
      Integer numMonths = null;
      jsonInput = HttpUtils.decodeString(jsonInput);
      JSONObject jsonObj = null; 
      if (((jsonInput = ImportUtils.cleanString(jsonInput)) != null) && 
              ((jsonObj = new JSONObject(jsonInput)) != null) &&
              (jsonObj.length() > 0)) {
        String strVal = null;
        String dtFormat = "yyyy-MM-dd";

        System.out.println("Request = " + jsonObj.toString());

        if (((strVal = 
                    ImportUtils.cleanString(jsonObj.optString("enddate",""))) != null)){
          try {
            endDt = ImportUtils.dateFromString(strVal, dtFormat, null);
          } catch (Exception dtErr) {
            throw new Exception("Invalid input date '" + strVal 
                    + "'. Expected format '" + dtFormat + "'.");
          }
        }
        
        if ((numMonths = jsonObj.optInt("months",-1)) <= 0) {
          numMonths = null;
        }
      }
      
      importer.importMonthlyData(endDt, numMonths);

      result.put("status", "success");
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.importMonthlyData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      result.put("error", exp.getMessage());
    } finally {
      if ((result == null) || (result.length() == 0)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                "Requested process not found");
      } else {
        response.getWriter().write(result.toString());
      }
    }
  } 

  /**
   * POST method for launching the import of b120 Forecast data.
   * @param jsonInput the expected format is "{"endwy": //, "numyrs": ??}".
   * @return an HTTP response with content of the updated or created resource.
   */
  @RequestMapping(
          path = "/b120",
          method = RequestMethod.POST,
          consumes = "application/x-www-form-urlencoded"
  )
  public void importB120Data(@RequestBody String jsonInput,
                              HttpServletRequest request, 
                              HttpServletResponse response)
                              throws IOException {
    JSONObject result = new JSONObject();
    try {
      CdecImporter importer = this.getImporter();
      if (importer == null) {
        throw new Exception("The CDEC Importer is not accessible to handle the "
                + "Request[/import/b120]");
      }
      
      Integer endWy = null;
      Integer numYrs = null;
      JSONObject jsonObj = null; 
      jsonInput = HttpUtils.decodeString(jsonInput);
      if (((jsonInput = ImportUtils.cleanString(jsonInput)) != null) && 
              ((jsonObj = new JSONObject(jsonInput)) != null) &&
              (jsonObj.length() > 0)) {
        String strVal = null;
        
        System.out.println("Request = " + jsonObj.toString());
        
        if (((endWy = jsonObj.optInt("endwy", 0)) < 1900)){
          endWy = null;
        }
        
        if (((numYrs = jsonObj.optInt("numyrs", 0)) <= 0)){
          numYrs = null;
        }
      }
      
      importer.importB120Data(endWy, numYrs);

      result.put("status", "success");
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.importB120Data Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      result.put("error", exp.getMessage());
    } finally {
      if ((result == null) || (result.length() == 0)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                "Requested process not found");
      } else {
        response.getWriter().write(result.toString());
      }
    }
  }  

  /**
   * POST method for Importing the Station-Sensor Data - requires and JSON array of
   * integers representing SensorIds.
   * @param jsonInput the array if Sensor for whihc to import the POR data
   * @return an HTTP response with content of the updated or created resource.
   */
  @RequestMapping(
          path = "/por",
          method = RequestMethod.POST,
          consumes = "application/x-www-form-urlencoded"
  )
  public void importPOR(@RequestBody String jsonInput,
                              HttpServletRequest request, 
                              HttpServletResponse response)
                              throws IOException {
    JSONObject result = new JSONObject();
    try {
      CdecImporter importer = this.getImporter();
      if (importer == null) {
        throw new Exception("The CDEC Importer is not accessible to handle the "
                + "Request[/import/por]");
      }
      
      List<Integer> sensorIds = new ArrayList<>(); 
      JSONArray jsonArr = null; 
      jsonInput = HttpUtils.decodeString(jsonInput);
      if (((jsonInput = ImportUtils.cleanString(jsonInput)) != null) && 
              ((jsonArr = new JSONArray(jsonInput)) != null) &&
              (jsonArr.length() > 0)) {
        System.out.println("Request = " + jsonArr.toString());
        Integer sensorId = null;
        for (int i = 0; i < jsonArr.length(); i++) {
          if ((sensorId = jsonArr.optInt(i, -1)) > 0) {
            sensorIds.add(sensorId);
          }
        }
      }
      
      if (sensorIds.isEmpty()) {
        throw new Exception("Empty request.");
      }
      
      importer.importPorData(sensorIds);

      result.put("status", "success");
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.importPOR Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      result.put("error", exp.getMessage());
    } finally {
      if ((result == null) || (result.length() == 0)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                "Requested process not found");
      } else {
        response.getWriter().write(result.toString());
      }
    }
  }  

  /**
   * POST method for Importing the Station-Sensor Data - no post data required.
   * @param jsonInput ignored
   * @return an HTTP response with content of the updated or created resource.
   */
  @RequestMapping(
          path = "/stationsensor",
          method = RequestMethod.POST,
          consumes = "application/x-www-form-urlencoded"
  )
  public void importStationsensor(@RequestBody String jsonInput,
                              HttpServletRequest request, 
                              HttpServletResponse response)
                              throws IOException {
    JSONObject result = new JSONObject();
    try {
      CdecImporter importer = this.getImporter();
      if (importer == null) {
        throw new Exception("The CDEC Importer is not accessible to handle the "
                + "Request[/import/monthly]");
      }
      importer.importStationSensorData();

      result.put("status", "success");
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.importStationSensor Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      result.put("error", exp.getMessage());
    } finally {
      if ((result == null) || (result.length() == 0)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                "Requested process not found");
      } else {
        response.getWriter().write(result.toString());
      }
    }
  }     

  /**
   * POST method for reset the history Queue - no post data required.
   * @param jsonInput ignored
   * @return an HTTP response with content of the updated or created resource.
   */
  @RequestMapping(
          path = "/resethistory",
          method = RequestMethod.POST
  )
  public void resetHistory(HttpServletRequest request, 
                              HttpServletResponse response)
                              throws IOException {
    JSONObject result = new JSONObject();
    try {
      CdecImporter importer = this.getImporter();
      if (importer == null) {
        throw new Exception("The CDEC Importer is not accessible to handle the "
                + "Request[/import/resethistory]");
      }
      importer.resetHistory();

      result.put("status", "success");
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.resetHistory Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      result.put("error", exp.getMessage());
    } finally {
      if ((result == null) || (result.length() == 0)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                "Requested process not found");
      } else {
        response.getWriter().write(result.toString());
      }
    }
  }  

  /**
   * POST method for reset the history Queue - no post data required.
   * @param jsonInput ignored
   * @return an HTTP response with content of the updated or created resource.
   */
  @RequestMapping(
          path = "/stopexecute",
          method = RequestMethod.POST
  )
  public void stopExecute(HttpServletRequest request, 
                              HttpServletResponse response)
                              throws IOException {
    JSONObject result = new JSONObject();
    try {
      CdecImporter importer = this.getImporter();
      if (importer == null) {
        throw new Exception("The CDEC Importer is not accessible to handle the "
                + "Request[/import/stopExecute]");
      }
      importer.stopExecute();

      result.put("status", "success");
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.stopExecute Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      result.put("error", exp.getMessage());
    } finally {
      if ((result == null) || (result.length() == 0)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                "Requested process not found");
      } else {
        response.getWriter().write(result.toString());
      }
    }
  } 
}
