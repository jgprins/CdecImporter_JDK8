package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;
import bubblewrap.io.DoubleRange;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONObject;
/**
 * A IntegerRangeValidator extends the IntegerValidator by a) allowing null values and
 * b) support a Range[minimumValue..maximumValue] with a defaultValue if the set value
 * is null. The default range is not set (i.e., the range checking is ignored) . 
 * For supported option see {@linkplain #onSetOptions(org.json.JSONObject) onSetOptions}.
 * @author kprins
 */
public class DoubleRangeValidator extends DoubleValidator {

  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  private DoubleRange range;
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public DoubleRangeValidator() {
    super();
    this.initValidator(true);
    this.range = new DoubleRange();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Overload 1: This method is used to specify the allowable Integer Range. 
   * Call Overload 2 with allowNull=true
   * @param minValue the minimum Value (inclusive)
   * @param maxValue the maximum Value (inclusive)
   * @throws Exception
   */
  public void setInputRange(double minValue, double maxValue) throws Exception {
    this.setInputRange(minValue, maxValue, true);
  }
  
  /**
   * This method is used to specify the allowable Integer Range and whether a null 
   * value is allowed. 
   * @param minValue the minimum Value (inclusive)
   * @param maxValue the maximum Value (inclusive)
   * @param allowNull true if a null value is allowed.
   * @throws Exception
   */
  public void setInputRange(double minValue, double maxValue, boolean allowNull)
          throws Exception {
    try {
      this.range.reset();
      this.range.grow(minValue, maxValue);
      this.initValidator(allowNull);
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.setInputRange Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw pExp;
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Input Validator Overrides">
  /**
   * Validate that pInpu is within the set Bound[minimumValue..maximumValue].
   * Return true is inputValue=null. Set this.errorMsg is invalid.
   * @param inputValue Integer
   * @return boolean
   */
  @Override
  public boolean onIsValidInput(Double inputValue) {
    if (super.onIsValidInput(inputValue)) {
      if ((!this.range.isEmpty()) && (!this.range.inRange(inputValue))) {
        this.setErrorMsg("Input[" + inputValue.toString() + "] is out of Bounds[" 
                + this.range.getMin().toString() + ".." 
                + this.range.getMax().toString() + "].");
      }
    }
    return (!this.hasError());
  }
  
//  /**
//   * Implemented by OptionsValidator to parse sOptions in Selection Options.
//   * The base method does nothing.
//   * @param optionStr the string containing the options ( format: "{minvalue},
//   * {maxValue}, {allowNull=0|1}")
//   * @throws Exception
//   */
//  @Override
//  public void setOptions(String optionStr) throws Exception {
//    optionStr = DataEntry.cleanString(optionStr);
//    if (optionStr != null) {
//      List<String> valList = DataEntry.splitString(optionStr, ",");
//      
//      this.range.reset();
//      if ((valList != null) && (valList.size() >= 1)) {
//        this.range = Double.parseDouble(valList.get(0));
//      } else {
//        this.range = 0.0d;
//      }
//      
//      this.maxValue = this.range+1;
//      if ((valList != null) && (valList.size() >= 2)) {
//        this.maxValue = Double.parseDouble(valList.get(1));
//      }
//      
//      if (valList.size() > 2) {
//        super.setOptions(valList.get(2));
//      }
//    }
//  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Parse the cleanString string assuming options in the format: 
   * "min=#.00,max=#.00,allownull=0|1|true|false". Default allownull=null and the range
   * is not set/ignored.</p>
   */
  @Override
  protected void onSetOptions(JSONObject options) {
    this.range.reset();
    if ((options == null) || (options.length() == 0)) {
      return;
    }
    JSONArray optionArr = options.optJSONArray("keyvalues");
    String value = null;
    Boolean isSet = null;
    Double minValue = null;
    Double maxValue = null;
    if (optionArr != null) {
      if (optionArr.length() > 0) {
        minValue = optionArr.getDouble(0);
      }
      if (optionArr.length() > 1) {
        maxValue = optionArr.getDouble(1);
      }
      if (optionArr.length() > 2) {
        super.onSetOptions(options);
      }
    } else {
      minValue = options.optDouble("min", Double.NaN);
      maxValue = options.optDouble("max", Double.NaN);
      super.onSetOptions(options);
    }
    
    this.range.grow(minValue,maxValue);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: if (this.range.isEmpty), return super.getOptions() else return 
   * DataEntry.concatKeyValuePairs("min=" + this.range.getMin(),
   * "max=" + this.range.getMax(), super.getOptions())</p>
   */
  @Override
  public String getOptions() {
    String result = null;
    if (this.range.isEmpty()) {
      result = DataEntry.concatKeyValuePairs("min=" + this.range.getMin(),
                                             "max=" + this.range.getMax(),
                                             super.getOptions());
    }  else {
      result = super.getOptions();
    }
    return result;
  }
  //</editor-fold>
}
