package bubblewrap.app.cache;

import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.schedules.TimeSpan;
import bubblewrap.io.schedules.enums.Interval;
import java.io.Serializable;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class BwCacheContent {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The cached content
   */
  public final BwCacheKey cacheKey;
  /**
   * The cached content
   */
  private Serializable content;
  /**
   * The Date-Time when the Cached Content expires and and can be removed from the
   * AppCache.
   */
  private DateTime expiredDt;
  /**
   * The TimeSpan for scheduling  the Cached Content Expiration Date.
   */
  private TimeSpan timeSpan;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   * @param content the content to cache - cannot be null.
   */
  protected BwCacheContent(Serializable recordId, Serializable content) {
    super();    
    if ((this.cacheKey = new BwCacheKey(recordId)) == null) {
      throw new NullPointerException("Initiating the Cached Content's Key failed.");
    } 
    if ((this.content = content) == null) {
      throw new NullPointerException("The Cached Content cannot be null.");
    }
    this.expiredDt = null;
    this.timeSpan = null;
  }
  
  /**
   * Public Constructor with the cached content and a <tt>cacheFor</tt> TimeSpan for 
   * which the content should be kept in the AppCache.
   * @param content the content to cache - cannot be null.
   * @param cacheFor the cache TimeSpan.
   */
  protected BwCacheContent(Serializable recordId, Serializable content, 
                                                                     TimeSpan cacheFor) {
    this(recordId, content); 
    if ((this.timeSpan = cacheFor) != null) {
      this.timeSpan = cacheFor;
      this.resetExpired();
    }
  }
  
  /**
   * Public Constructor with the cached content and a fixed expiration 
   * Date-time when the content should be removed from the AppCache.
   * @param content the content to cache - cannot be null.
   * @param expiredDt the fixed date at which this content expires.
   */
  protected BwCacheContent(Serializable recordId, Serializable content, 
                                                                    DateTime expiredDt) {
    this(recordId, content);     
    this.expiredDt = expiredDt;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Cache's assigned content's cacheKey
   * @return this.cacheKey.getRecordId()
   */
  public Serializable getRecordId() {
    return this.cacheKey.getRecordId();
  }
  
  /**
   * Get the Cache's assigned content
   * @return this.content
   */
  public Serializable getContent() {
    return this.content;
  }  
  
  /**
   * Called by the constructor to initiate the expiration date of the content and also
   * called every time the content is requested.
   */
  public final void resetExpired() {
    Interval interval = null;
    long ticks = 0;
    if ((this.timeSpan != null) && ((interval = this.timeSpan.getInterval()) != null) &&
            ((ticks = this.timeSpan.getTickCount()) > 0)) {
      DateTime curDt = DateTime.getNow(null);
      this.expiredDt = curDt.addTime(ticks, interval.getTimeUnit());
    }
  }
    
  /**
   * Get whether the content has expired and should be removed from the cache 
   * (typically called by the {@linkplain BwAppCache}'s Garbage Collector.
   * @return true if (this.expiredDt != null) && earlier than the current date
   */
  public boolean isExpired() {
    DateTime curDt = null;
    return ((this.expiredDt != null) && ((curDt = DateTime.getNow(null)) != null) &&
            (curDt.isAfter(this.expiredDt)));
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: "BwCacheContent[ expiresOn: " + this.expiredDt.toString() + "]"</p>
   */
  @Override
  public String toString() {
    String result = "BwCacheContent";
    if (this.expiredDt != null) {
      result += "[ expiresOn: " + this.expiredDt.toString() + "]";
    }
    return result;
  }
  // </editor-fold>
}
