package bubblewrap.io;

import bubblewrap.entity.core.EntityFacade;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A delegate fro generating a map[key=Tkey,value=TValue based on a list of values.
 * This delegate is used by the {@linkplain EntityFacade#mergeAll(java.util.HashMap,
 * bubblewrap.io.MapperDelegate, bubblewrap.entity.core.EntityMergeDelegate, boolean)
 * EntityFacade.mergeAll}, but could be used in several other cases.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class MapperDelegate<TKey extends Serializable, TValue> {

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(MapperDelegate.class.getName());

  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public MapperDelegate() {
    super();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to convert the list of values to a LinkedHashMap with a set of Map Key
   * representing each record. It calls {@linkplain #onGetKey(java.io.Serializable)
   * this.onGetKey} to generate the MapKey.
   * @param valueList the list of values to convert to a Map
   * @param valueMap the output map of values to assign the Key->Value pair to.
   * @return true is this process was successful.
   * @throws IllegalArgumentException if the if the mapKey is unassigned or it it not
   * unique or this.onGetKey throws an exception.
   */
  public final void toMap(List<TValue> valueList, Map<TKey, TValue> valueMap)
                                                       throws IllegalArgumentException {
    try {
      if (valueMap == null) {
        throw new Exception("The valueMap cannot be unassigned.");
      }

      if ((valueList != null) && (!valueList.isEmpty())) {
        for (TValue value : valueList) {
          if (value == null) {
            continue;
          }
          TKey key = this.onGetKey(value);
          if (key == null) {
            throw new Exception("Generating a MapKey for Value[" + value.toString() +
                    "] failed.");
          } else if (valueMap.containsKey(key)) {
            throw new Exception("Duplicate MapKey[" + key + "] for Value["
                    + value.toString() + "]");
          }

          valueMap.put(key, value);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.toMap Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      throw new IllegalArgumentException(exp);
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Protected Abstract Methods">
  /**
   * Called for each record to be added to the Map to get the unique Map Key from the
   * <tt>value</tt>.
   * @param value a value from the list
   * @return the unique map key (cannot be null)
   * @throws Exception is the process failed.
   */
  public abstract TKey onGetKey(TValue value) throws Exception;
  // </editor-fold>
}
