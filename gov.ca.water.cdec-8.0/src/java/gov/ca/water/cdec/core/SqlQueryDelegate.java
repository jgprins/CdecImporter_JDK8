package gov.ca.water.cdec.core;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.persistence.Query;

/**
 * A Delegate for processing the results from a SQL query using the {@linkplain 
 * EntityFacade#excuteQuery(java.lang.String, bubblewrap.entity.core.SqlQueryDelegate) 
 * EntityFacade.excuteQuery} method. 
 * <p>
 * <b>NOTE:</b> The {@linkplain ResultSet} returned by the query is not typed and it 
 * is up the delegate design to determine the result content.</p>
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class SqlQueryDelegate implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Delegate's Listener that will process the Query results
   */
  private Object listener;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public SqlQueryDelegate(Object listener) {
    super();  
    this.listener = listener;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the dynamically cased reference to the listener
   * @param <TListener> the listener class
   * @return the casted reference
   */
  public <TListener> TListener getListener() {
    return (TListener) this.listener;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Abstact Methods">
  /**
   * Called by the {@linkplain EntityFacade#excuteQuery(java.lang.String, 
   * bubblewrap.entity.core.NamedQueryDelegate) EntityFacade.excuteQuery} method to allow
   * the delegate to assign the parameters required to execute the query.
   * @param query the query to update
   * @throws SQLException 
   */
  public void assignParameters(Query query) throws SQLException{}
  
  /**
   * Called by the {@linkplain EntityFacade#excuteQuery(java.lang.String, 
   * bubblewrap.entity.core.SqlQueryDelegate) EntityFacade.excuteQuery} method for the
   * delegate to process the {@linkplain ResultSet} returned by the SQL query.
   * @param rs the List of record
   * @throws SQLException can be throw if the process fails
   */
  public abstract void loadQuery(List<Object> rs) throws SQLException;
  // </editor-fold>
}
