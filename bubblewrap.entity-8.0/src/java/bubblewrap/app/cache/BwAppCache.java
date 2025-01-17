package bubblewrap.app.cache;

import bubblewrap.core.annotations.LookupMethod;
import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.schedules.TimeSpan;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Startup
@Singleton(name = "BwAppCache")
public class BwAppCache implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(BwAppCache.class.getName());
  //</editor-fold>      
    
  //<editor-fold defaultstate="collapsed" desc="Static Singleton Methods">
  /**
   * Placeholder for the application's Static BwAppCache instance.
   */
  private static BwAppCache _singleton = null;
  /**
   * <p>
   * Lookup the Application Singleton Enterprise 'BwAppCache' Java Bean (EJB)</p>
   * <p>
   * @return a reference to the BwAppCache instance
   */
  @LookupMethod
  private synchronized static BwAppCache doLookup() {
    BwAppCache result = null;
    try {
      result = BwAppCache._singleton;
      if (result == null) {
        throw new Exception("Unable to access the shared BubbleWrap Application Cache");
      }
    } catch (Exception exp) {
      logger.log(Level.SEVERE, "BwAppCache.doLookup Error: \n{0}",
              exp.getMessage());
    }
    return result;
  }
  
  /**
   * <p>
   * Initiate a BwAppCache for testing purposes.</p>
   * <p>
   * <b>Note:</b> This method should not be called other than from a test unit.</p>
   * @return a reference to the BwAppCache instance
   */
  protected static BwAppCache initTestInstance() {
    if (BwAppCache._singleton == null) {
      try {
        BwAppCache._singleton = new BwAppCache();
        if (BwAppCache._singleton == null) {
          throw new Exception("Initiating a new BwAppCache instance failed");
        }
        BwAppCache._singleton.initAppCache();
      } catch (Exception exp) {
        logger.log(Level.SEVERE, "BwAppCache.initInstance Error: \n{0}",
                exp.getMessage());
      }
    }
    return BwAppCache._singleton;
  }
  
  /**
   * Call be the UnitTest to Dispose BwAppCache._singleton
   */
  protected static void disposeTestInstance() {
    if (BwAppCache._singleton != null) {
      try {
        BwAppCache._singleton.destroyAppCache();
      } catch (Exception exp) {
        logger.log(Level.SEVERE, "BwAppCache.initInstance Error: \n{0}",
                exp.getMessage());
      } finally {
        BwAppCache._singleton = null;
      }
    } 
  }
  //</editor-fold>
  
  private static TimeUnit _gcTimeUnit = TimeUnit.DAYS;
  private static long _gcDelay = 1l;
  
  /**
   * Called to set the BwAppCache's Garbage Collection (GC) configuration settings (i.e.,
   * the GC Delay Time Unit (Default = DAYS), the GC Delay Time (default = 1). If the 
   * settings have changed, and the BwAppCache's singleton has been initiated, it will
   * restart (reschedule) the BwAppCache's garbage collection process.
   * @param gcTimeUnit the new TimeUnit 
   * @param gcDelay the delay in 
   */
  public static void configGC(TimeUnit gcTimeUnit, long gcDelay) {
    if (gcTimeUnit == null) {
      throw new NullPointerException("The BwAppCache's Garbage Collection delay timeunit "
              + "is unassigned.");
    }
    if (gcDelay <= 0) {
      throw new NullPointerException("The BwAppCache's Garbage Colelction delay must "
              + "be greater than zero.");
    }
    
    
    if ((!DataEntry.isEq(BwAppCache._gcTimeUnit, gcTimeUnit)) ||
            (BwAppCache._gcDelay != gcDelay)) {
      BwAppCache._gcTimeUnit = gcTimeUnit;
      BwAppCache._gcDelay = gcDelay;

      if (BwAppCache._singleton != null) {
        BwAppCache._singleton.restartGC();
      }
    }
  }
  
  //<editor-fold defaultstate="collapsed" desc="Private ExecuteGCProcess class">
  /**
   * A Runnable to execute the Garbage Collection (GC) process after pausing the
   * runnable's thread by calling {@linkplain #_gcTimeUnit}.sleep({@linkplain
   * #_gcDelay})
   */
  private class ExecuteGCProcess implements Runnable {
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public ExecuteGCProcess() {
      super();
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Override Runnable">
    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: It pause execution be calling {@linkplain #_gcTimeUnit}.sleep({@linkplain
     * #_gcDelay}), and call {@linkplain #_singleton}.{@linkplain #executeGC() executeGC}
     * when waking up.</p>
     */
    @Override
    public void run() {
      try {
        logger.log(Level.INFO, "{0}.run Initiated On {1}",
                new Object[]{"BwAppCache.ExecuteGC", DateTime.getNow(null).toString()});
        try {
          if ((BwAppCache._gcTimeUnit != null) && (BwAppCache._gcDelay > 0l)) {
            BwAppCache._gcTimeUnit.sleep(BwAppCache._gcDelay);
          }
        } catch (Exception exp) {
          throw new Exception("TimeUnit.sleep Error:\n " + exp.getMessage());
        }
        
        logger.log(Level.INFO, "{0}.run Started On {1}",
                new Object[]{"BwAppCache.ExecuteGC", DateTime.getNow(null).toString()});
        if (BwAppCache._singleton != null) {
          BwAppCache._singleton.executeGC();
          if (BwAppCache._singleton.gcThread != null) {
            Thread restart = new Thread(new RestartQCProcess(), "AppCache.GC.restart");
            restart.start();
          }
        }
        logger.log(Level.INFO, "{0}.run Completed On {1}",
                new Object[]{"BwAppCache.ExecuteGC", DateTime.getNow(null).toString()});
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.run Error on {1}:\n {2}",
                new Object[]{"BwAppCache.ExecuteGC", DateTime.getNow(null).toString(),
                  exp.getMessage()});
      }
    }
    // </editor-fold>
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private RestartQCProcess class">
  /**
   * A runnable to restart the QC Process after the ExecuteQC process has been completed
   * it calls {@linkplain BwAppCache#startGC()} on run.
   */
  private class RestartQCProcess implements Runnable {
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public RestartQCProcess() {
      super();
    }
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Implements Runnable">
    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: It calls {@linkplain BwAppCache#startGC()} and logs the completion time.
     * </p>
     */
    @Override
    public void run() {
      try {
        Thread curThread = null;
        if ((BwAppCache._singleton != null) && 
            ((curThread = BwAppCache._singleton.gcThread) != null)) {
          if (!Thread.State.TERMINATED.equals(curThread.getState())) {
            curThread.join(100l);
          }
          if (BwAppCache._singleton != null) {
            BwAppCache._singleton.gcThread = null;
            BwAppCache._singleton.startGC();
            logger.log(Level.INFO, "{0}.run Completed On {1}",
                new Object[]{"BwAppCache.RestartQC", DateTime.getNow(null).toString()});
          }
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.run Error on {1}:\n {2}",
                new Object[]{"BwAppCache.RestartQC", DateTime.getNow(null).toString(),
                  exp.getMessage()});
      }
    }
    //</editor-fold>
  }
//</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">    
  private HashMap<String, BwCacheContentMap> cacheMap;
  
  private ExecuteGCProcess execProcess;
  /**
   * The 
   */
  private Thread gcThread;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public BwAppCache() {
    super();  
  }

  /**
   * <p>
   * A private PostConstruct method to initiate the BwAppContext's core settings:</p>
   * <ul>
   * <li>It assign this instance as static this.mpAppContext</li>
   * <li>It adds BwCoreExtension to the AppExtension list.</li>
   * </ul>
   */
  @PostConstruct
  protected void initAppCache() {
    String sClass = this.getClass().getSimpleName();
    logger.log(Level.INFO, "{0}.initAppCache Start", sClass);
    try {
      this.cacheMap = new HashMap<>();
      this.execProcess = new ExecuteGCProcess();
      this.gcThread = null;
      BwAppCache._singleton = this;
      logger.log(Level.INFO, "{0}.initAppCache Done", sClass);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.initAppCache Failed:\n {1}",
              new Object[]{sClass, exp.getMessage()});
      throw exp;
    }
  }

  /**
   * <p>
   * A private PreDestroy method to reset this.mpAppContext and to clear the
   * Application Context's content. Calling {@linkplain #clearAppContext()
   * clearAppContext}.</p>
   */
  @PreDestroy
  protected void destroyAppCache() {
    String sClass = this.getClass().getSimpleName();
    logger.log(Level.INFO, "{0}.destroyAppCache Start", sClass);
    this.cacheMap.clear();
    if (this.gcThread != null) {
      this.gcThread.interrupt();
    }
    BwAppCache._singleton = null;
    logger.log(Level.INFO, "{0}.destroyAppCache Done", sClass);
  }

  /**
   * OVERRIDE: Dispose Local resources before calling the super method
   * @throws Throwable
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  /**
   * Called to start the GC process, it not already running. If initiates this.gcThread
   * with this.execProcess as runnable and start the thread. If this.gcThread.isAlive
   * - it execute successfully, it start a second thread with {@linkplain 
   * RestartQCProcess} as a runnable, which will wait for this.gcThread to stop before
   * it recall this.startGC.
   * <p>
   * <b>NOTE:</b> The GC process is not started if this.cacheMap.isEmpty</p>
   */
  private synchronized void startGC() {
    if ((this.gcThread == null) && (!this.cacheMap.isEmpty())) {
      try {
        this.gcThread = new Thread(this.execProcess, "AppCache.GC");
        this.gcThread.start();
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.method Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
  }
  
  /**
   * Called to interrupt the currently executing this.gcThread. If the current
   */
  private synchronized void restartGC() {
    Thread curThread = this.gcThread;
    if (curThread != null) {
      curThread.interrupt();
    }
  }
  
  /**
   * Called by the {@linkplain ExecuteGCProcess} on run to remove cached content that has 
   * expired. If this.cacheMap isEmpty on completion of this process, the GC Process will
   * not be reschedule, but will automatically be restart when new content is added.
   */
  private synchronized void executeGC() {
    try {
      logger.log(Level.INFO, "{0}.executeGC Started On {1}",
                new Object[]{this.getClass().getSimpleName(), 
                              DateTime.getNow(null).toString()});
      if (!this.cacheMap.isEmpty()) {
        List<String> keyList = new ArrayList<>(this.cacheMap.keySet()); 
        BwCacheContentMap classMap = null;
        for (String key : keyList) {
          if (((classMap = this.cacheMap.get(key)) == null) ||
                  (classMap.isEmpty()) || (classMap.executeGC())) {
            this.cacheMap.remove(key);
          }
        }
      }
      logger.log(Level.INFO, "{0}.executeGC Completed On {1}",
                new Object[]{this.getClass().getSimpleName(), 
                              DateTime.getNow(null).toString()});
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.executeGC Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="private Methods">
  /**
   * Called by the public put Overloads to add the cached content to this.cacheMap
   * @param contentClass the content's class
   * @param cacheContent the wrapped BwCacheContent
   */
  private void put(Class<? extends Serializable> contentClass, 
                                                           BwCacheContent cacheContent) {
    String mapKey = contentClass.getName().toLowerCase();
    BwCacheContentMap contentMap = null;
    if ((!this.cacheMap.containsKey(mapKey)) || 
            ((contentMap = this.cacheMap.get(mapKey)) == null)) {
      contentMap = new BwCacheContentMap();
      this.cacheMap.put(mapKey, contentMap);
    }
    
    contentMap.put(cacheContent.cacheKey, cacheContent);
    this.startGC();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * OVERLOAD 1: Cache the specified <tt>content</tt> without a expiration schedule or
   * date. The record will remain in the AppCache until manually removed or the 
   * application shuts down.
   * <p>The <tt>recordId</tt> and <tt>content</tt> are wrapped in {@linkplain 
   * BwCacheKey} and {@linkplain BwCacheContent} object, respectively before it is added
   * to this.cacheMap (type {@linkplain BwCacheContentMap} with the content's className
   * as the key to the this.cacheMap.   
   * <p>The process is skipped if <tt>recordId</tt> or <tt>content</tt> is null. If the
   * cache already contains cached content for <tt>recordId</tt> the previously cached 
   * record will be replaced.
   * @param recordId the content's unique record ID
   * @param content the content to be cached.
   */
  public static synchronized final void put(Serializable recordId, Serializable content) {
    try {
      BwAppCache appCache = BwAppCache.doLookup();
      if (appCache == null) {
        throw new Exception("The Application's BwAppCache is not accessible.");
      }
      
      if ((recordId == null) || (content == null)) {
        throw new Exception("The Requests recordId or contentClass is unassigned.");
      }
      
      BwCacheContent cacheContent = new BwCacheContent(recordId, content);
      if (cacheContent == null) {
        throw new Exception("Wrapping the cached content in a BwCacheContent failed.");
      }
      
      appCache.put(content.getClass(), cacheContent);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.put Error:\n {1}",
              new Object[]{BwAppCache.class.getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * OVERLOAD 2: Cache the specified <tt>content</tt> with a <tt>cacheFor</tt> TimeSpan,
   * which is used to to calculate the cached content's expiration date relative to today.
   * The content's expiration date is reset every time the content is retrieved.
   * The record will removed by the AppCache's Garbage Collection when it expired.
   * <p>See {@linkplain #put(java.io.Serializable, java.io.Serializable) OVERLOAD 1}
   * for more information on the cache management.
   * @param recordId the content's unique record ID
   * @param content the content to be cached.
   * @param cacheFor the TimeSpan for setting the cached content's expiration date.
   */
  public static synchronized final void put(Serializable recordId, Serializable content,
                                                                      TimeSpan cacheFor) {
    try {
      BwAppCache appCache = BwAppCache.doLookup();
      if (appCache == null) {
        throw new Exception("The Application's BwAppCache is not accessible.");
      }
      
      if ((recordId == null) || (content == null)) {
        throw new Exception("The Requests recordId or contentClass is unassigned.");
      }
      
      BwCacheContent cacheContent = new BwCacheContent(recordId, content, cacheFor);
      if (cacheContent == null) {
        throw new Exception("Wrapping the cached content in a BwCacheContent failed.");
      }
      
      appCache.put(content.getClass(), cacheContent);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.put Error:\n {1}",
              new Object[]{BwAppCache.class.getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * OVERLOAD 3: Cache the specified <tt>content</tt> with a <tt>expireDt</tt>,
   * which is assigned as the cached content's fixed expiration date.
   * The content's expiration date WILL NOT be reset when the content is retrieved.
   * The record will removed by the AppCache's Garbage Collection when it expired.
   * <p>See {@linkplain #put(java.io.Serializable, java.io.Serializable) OVERLOAD 1}
   * for more information on the cache management.
   * @param recordId the content's unique record ID
   * @param content the content to be cached.
   * @param expireDt the cached content's fixed expiration date.
   */
  public static synchronized final void put(Serializable recordId, Serializable content, 
                                                                    DateTime expireDt) {
    try {
      BwAppCache appCache = BwAppCache.doLookup();
      if (appCache == null) {
        throw new Exception("The Application's BwAppCache is not accessible.");
      }
      
      if ((recordId == null) || (content == null)) {
        throw new Exception("The Requests recordId or contentClass is unassigned.");
      }
      
      BwCacheContent cacheContent = new BwCacheContent(recordId, content, expireDt);
      if (cacheContent == null) {
        throw new Exception("Wrapping the cached content in a BwCacheContent failed.");
      }
      
      appCache.put(content.getClass(), cacheContent);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.put Error:\n {1}",
              new Object[]{BwAppCache.class.getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Get the previously cached content for the specified <tt>recordId</tt> and
   * <tt>contentClass</tt>. It locates the {@linkplain BwCacheContentMap contentMap} 
   * for the <tt>contentClass</tt> and if found it gets the cached content for 
   * for <tt>recordId</tt> from <tt>contentMap</tt>. It then validates that the
   * cached content's class matches <tt>contentClass</tt>.
   * @param <TContent> extends Serializable
   * @param recordId the content's unique record ID
   * @param contentClass the content's class
   * @return the matching content if found or null if not found.
   */
  @SuppressWarnings("unchecked")
  public static synchronized final <TContent extends Serializable> TContent get( 
                                  Serializable recordId, Class<TContent> contentClass) {
    TContent result = null;
    try {
      BwAppCache appCache = BwAppCache.doLookup();
      if (appCache == null) {
        throw new Exception("The Application's BwAppCache is not accessible.");
      }
      
      if ((recordId == null) || (contentClass == null)) {
        throw new Exception("The Requests recordId or contentClass is unassigned.");
      }
      
      String mapKey = contentClass.getName().toLowerCase();
      BwCacheContentMap contentMap = null;
      BwCacheContent cache = null;
      Serializable content = null;
      if ((appCache.cacheMap != null) && (appCache.cacheMap.containsKey(mapKey)) &&
              ((contentMap = appCache.cacheMap.get(mapKey)) != null) &&
              ((cache = contentMap.get(recordId)) != null) &&
              ((content = cache.getContent()) != null)) {
        if (!contentClass.isInstance(content)) {
          // Should never reach here - just a double check
          throw new Exception("The Cached content class[" 
                  + content.getClass().getSimpleName()
                  + "] does not match the specified Content Class[" 
                  + contentClass.getSimpleName() + "].");
        }
        result = (TContent) content;
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.get Error:\n {1}",
              new Object[]{BwAppCache.class.getSimpleName(), exp.getMessage()});
    }
    
    return result;
  }
  
  /**
   * Check if the Cache contains cached content for the specified <tt>recordId</tt> and
   * <tt>contentClass</tt>. It locates the {@linkplain BwCacheContentMap contentMap} 
   * for the <tt>contentClass</tt> and if found it checks if the <tt>contentMap</tt>
   * contains cached content for <tt>recordId</tt> and if the cached content's class
   * matches <tt>contentClass</tt>.
   * @param <TContent> extends Serializable
   * @param recordId the content's unique record ID
   * @param contentClass the content's class
   * @return true if matching content was found.
   */
  public static synchronized final  <TContent extends Serializable> boolean contains(
                                    Serializable recordId, Class<TContent> contentClass) {
    boolean result = false;
    String mapKey = null;
    BwAppCache appCache = null;
    BwCacheContentMap contentMap = null;
    BwCacheContent cache = null;
    Serializable content = null;      
    if ((recordId != null) && (contentClass != null) &&
        ((appCache = BwAppCache.doLookup()) != null) &&
        ((mapKey = contentClass.getName().toLowerCase()) != null) &&
        (appCache.cacheMap != null) && (appCache.cacheMap.containsKey(mapKey)) &&
        ((contentMap = appCache.cacheMap.get(mapKey)) != null) &&
        ((cache = contentMap.get(recordId)) != null) &&
        ((content = cache.getContent()) != null)) {
      result = contentClass.isInstance(content);
    }
    return result;
  }
  
  /**
   * Called to manually remove a previously cached record form the AppCache. It locates
   * the {@linkplain BwCacheContentMap contentMap} for the <tt>contentClass</tt> and
   * if found it calls {@linkplain BwCacheContentMap#remove(java.lang.Object) 
   * contentMap.remove(recordId)} to remove the cached content (if it exists). If the
   * contentMap is empty after removing the content, the contentMap will be removed from
   * this.cacheMap.
   * @param <TContent> extends Serializable
   * @param recordId the content's unique record ID
   * @param contentClass the content's class
   */
  public static synchronized final <TContent extends Serializable> void remove(
                                    Serializable recordId, Class<TContent> contentClass) {
    String mapKey = null;
    BwCacheContentMap contentMap = null;
    BwAppCache appCache = null;
    if ((recordId != null) && (contentClass != null) &&
        ((appCache = BwAppCache.doLookup()) != null) &&
        ((mapKey = contentClass.getName().toLowerCase()) != null) &&
        (appCache.cacheMap != null) && (appCache.cacheMap.containsKey(mapKey)) &&
        ((contentMap = appCache.cacheMap.get(mapKey)) != null)) {
      contentMap.remove(recordId);
      if (contentMap.isEmpty()) {
        appCache.cacheMap.remove(mapKey);
      }
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: "BwAppCache[ size=" + this.cacheMap.size() + "]"</p>
   */
  @Override
  public String toString() {
    return "BwAppCache[ size=" + this.cacheMap.size() + "]";
  }
  // </editor-fold>
}
