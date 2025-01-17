package gov.ca.water.cdec.core;

import java.io.Serializable;

/**
 * A Delegate to assist in merging two recordset (a Source dataset - of any type) and a 
 * target dataset (typically, the Entity Bean type). It has two abstract method that
 * requires implementation:<ul>
 * <li>{@linkplain #updateMerge(java.lang.Object, java.io.Serializable) this.updateMerge}
 * - to compare and update matching records</li>
 * <li>{@linkplain #newMerge(java.lang.Object) this.newMerge} to initiate a new instance
 * and populate it with values from the source record</li>
 * </ul>
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class EntityMergeDelegate<TKey extends Serializable, 
                                                  TSrc, TTrg extends Serializable> {

  // <editor-fold defaultstate="collapsed" desc="Public Abstract Methods">
  /**
   * Check if the srcBean contains updates to the trgBean, update the trgBean if true, and
   * return true. Else return false.
   * @param srcObject the Source Map's value can be a trgBean type or any other 
   * object type (e.g. a JSON Object)
   * @param trgBean the target Bean to update
   * @return true if the record was updated and must be submitted to the database.
   * @throws Exception to break out and rollback the entity merges.
   */
  public abstract boolean updateMerge(TSrc srcObject, TTrg trgBean) throws Exception;
  
  /**
   * Map srcBean properties to a new trgBean properties accordingly for insert 
   * transaction.    
   * @param srcKey the Source Map's Key
   * @param srcValue the Source Map's value can be a trgBean type or 
   * any other object type  (e.g. a JSON Object)
   * @return TTrg the new Target Bean instance
   * @throws Exception to break out and rollback the entity merges.
   */
  public abstract TTrg newMerge(TKey srcKey, TSrc srcValue) throws Exception;
  // </editor-fold>
}
