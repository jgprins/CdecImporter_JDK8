package bubblewrap.core.selectors;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.events.EventHandler;
import bubblewrap.io.DataEntry;
import bubblewrap.io.IntegerRange;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A TablePageManager manages the display content of a table by page given a fix number 
 * of items per page. It is designed as an interface between a {@linkplain TableSelector}
 * and a TablePager JSF Component. The TablePager will use the TablePageManager's 
 * {@linkplain #gotoFirst() gotoFirst}, {@linkplain #nextPage() nextPage},
 * , {@linkplain #priorPage() priorPage}, , {@linkplain #gotoLast() gotoLast}, and
 * {@linkplain #setPageIndex(java.lang.Integer)setPageIndex} method to navigate the pages.
 * With every page change the TablePageManager fires its {@linkplain #PageChanged 
 * PageChanged} event. The TableSelector in turn listens to the event and return 
 * a subset of its {@linkplain TableSelector#getSelectOptions() SelectOptions} for the
 * list index range [{@linkplain #getStartIndex() startIndex}..{@linkplain #getEndIndex()
 * endIndex}] based on the selected page.
 * <p>To prevent unwanted (accessive) event firing, use the {@linkplain #beginUpdate() 
 * beginUpdate} and {@linkplain #endUpdate() endUpdate} in a try-finally block. The 
 * {@linkplain #PageChanged PageChanged} event is not fired while {@linkplain
 * #isUpdating() this.isUpdating} = true.
 * <p>
 * <b>NOTE:</b> The Default ItemsPerPage = 0, which implies there is only one page. </p>
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class TablePageManager {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(TablePageManager.class.getName());
  //</editor-fold>        

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Item Index Range 
   */
  private IntegerRange indexRange;
  /**
   * The number of items per Page 
   */
  private Integer itemsPerPage;
  /**
   * The current PageIndex [1..numPages]
   */
  private Integer pageIndex; 
  /**
   * The current PageIndex 
   */
  private Integer pageCount;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Events">
  /**
   * The EventHandler that fires the Page Changed Event.
   */
  public final EventHandler PageChanged;

  /**
   * Called to fire the Page Changed Event. This event will not be fired if 
   * (this.isUpdating)
   */
  protected final void firePageChanged() {
    if (!this.isUpdating()) {
      this.PageChanged.fireEvent(this, new EventArgs());
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public TablePageManager() {
    super();
    this.PageChanged = new EventHandler();
    this.indexRange = new IntegerRange();
    this.itemsPerPage = null;
    this.pageIndex = null;
    this.pageCount = null;
    this.initRangeListener();
  }
  
  /**
   * Public Constructor
   * @param indexRange the range of record/item indices (must be assigned and not empty)
   * @param itemsPerPage the number of items per page (> 1)
   */
  public TablePageManager(Integer itemsPerPage) {
    this();
    this.itemsPerPage = itemsPerPage;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  /**
   * Called by the constructor to initiate a event delegate for listening to 
   * this.indexRange.RangeChanged event. When fired it calls {@linkplain 
   * #onUpdateSettings() this.onUpdateSettings}
   */
  private void initRangeListener() {
    this.indexRange.RangeChanged.add(new EventDelegate(this) {
      
      @Override
      public void onEvent(Object sender, EventArgs args) {
        TablePageManager listener = this.getListener();
        if ((listener != null) && (listener.indexRange == sender)) {
          try {
            listener.onUpdateSettings();
          } catch (Exception exp) {
            logger.log(Level.WARNING, "{0}.onEvent Error:\n {1}",
                    new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
          }
        }
      }
    });
  }
  
  /**
   * Called to (re-)initiate/sync the TablePageManager's settings. The settings are 
   * updates as follows: <ul>
   * <li>If (this.isEmpty): this.pageCount = null; this.pageIndex = null</li>
   * <li>Else if (this.itemsPerPage = null): this.ppaegCount = 1, this.paegIndex = 1</li>
   * <li>Else, calc this.pageCount based on the index range and this.itemsPerPage and
   * set this.pageIndex = 1 if currently unassigned, or this.pageCount if this.pageIndex
   * &gt; this.pageCount, else leave it unchanged</li>
   * </ul>
   * Call {@linkplain #firePageChanged() this.firePageChanged} on completion of the process
   */
  private void onUpdateSettings() {
    if (this.isEmpty()) {
      this.pageCount = null;
      this.pageIndex = null;
    } else {
      if (this.itemsPerPage == null) {
        this.pageCount = 1;
        this.pageIndex = 1;
      } else {
        Integer numItems = (indexRange.getMax() - indexRange.getMin() + 1);
        this.pageCount = numItems/this.itemsPerPage;
        while ((this.pageCount * this.itemsPerPage) < numItems) {
          this.pageCount++;
        }

        if (this.pageIndex == null) {
          pageIndex = 1;
        } else if (pageIndex > this.pageCount) {
          pageIndex = this.pageCount;
        }
      }
    }
    this.firePageChanged();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public isUpdating State Management">
  /**
   * Transient counter for managing the isUpdating state
   */
  private transient int countUpdate = 0;

  /**
   * Get the current isUpdating state
   * @return true if (this.countUpdate > 0)
   */
  public final boolean isUpdating() {
    return (this.countUpdate > 0);
  }

  /**
   * Called to start the Update process. It increment this.countUpdate.
   * <p>
   * <b>NOTE:</b> Every call to beginUpdate must be followed by a call to {@linkplain
   * #endUpdate() this.endUpdate}.</p>
   */
  public final void beginUpdate() {
    this.countUpdate = (this.countUpdate < 0) ? 0 : this.countUpdate;
    this.countUpdate++;
  }

  /**
   * Called - after calling {@linkplain #beginUpdate() this.beginUpdate} - to decrement
   * this.countUpdate and to call {@linkplain #onUpdateEnds() this.onUpdateEnds} to
   * process the event.
   */
  public final void endUpdate() {
    if (this.countUpdate > 0) {
      this.countUpdate--;
      if (this.countUpdate == 0) {
        this.onUpdateEnds();
      }
    }
  }

  /**
   * Calls to execute any post-process after the TablePageManager's settings have changed
   * (e.g. fire an event).
   */
  protected void onUpdateEnds() {
    this.firePageChanged();
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Call to get a reference to the TablePageManager's IndexRange. 
   * <p>The IndexRange can be updated by setting by calling its {@linkplain 
   * IntegerRange#reset() reset} method followed by a call to its {@linkplain 
   * IntegerRange#grow(java.lang.Comparable...) grow} method to set a new range.
   * <p>Internally the TablePageManager listens to the Range's {@linkplain 
   * IntegerRange#RangeChanged RangeChanged} event and will update the page count, and
   * focus page accordingly.
   * @return 
   */
  public final IntegerRange getIndexRange() {
    return this.indexRange;
  }
  
  /**
   * Get the number of Items to display per page (default = 0 - all items are displayed 
   * on one page).
   * @return the assigned value or 0 if unassigned.
   */
  public Integer getItemsPerPage() {
    return (this.itemsPerPage == null)? 0: this.itemsPerPage;
  }
  
  /**
   * Called to set the PageMange's number of items-per-page. If it has changed,
   * it calls {@linkplain #onUpdateSettings() this.onUpdateSettings} to update the
   * PageManagers settings, which will fire the {@linkplain #PageChanged PageChanged}
   * event.
   * @param itemsPerPage new number of pages.
   */
  public void setItemsPerPage(Integer itemsPerPage) {
    itemsPerPage = ((itemsPerPage == null) || (itemsPerPage <= 0))? null: itemsPerPage;
    if (!DataEntry.isEq(this.itemsPerPage, itemsPerPage)) {
      this.itemsPerPage = itemsPerPage;
      this.onUpdateSettings();
    }
  }
  
  /**
   * Check if the TablePage settings are defined.
   * @return true if this.indexRange.isEmpty - the range of page indices is no set.
   */
  public boolean isEmpty() {
    return (this.indexRange.isEmpty());
  }
  
  /**
   * Get the Number of Pages 
   * @return this.pageCount or 0 if this.isEmpty
   */
  public Integer getPageCount() {
    return (this.isEmpty())? 0: this.pageCount;
  }
  
  /**
   * Get the current Page's Index Range[1..this.pageCount]
   * @return this.pageIndex or 0 if this.isEmpty()
   */
  public Integer getPageIndex() {
    if ((!this.isEmpty()) && (this.pageIndex == null)) {
      this.pageIndex = 1;
    }
    return (this.isEmpty())? 0: this.pageIndex;
  }
  
  /**
   * Get the index of the first record on the current Page. These indices is within the
   * specified IndexRange.
   * @return the start index or 0 if this.isEmpty.
   */
  public Integer getStartIndex() {
    Integer result = 0;
    Integer pgIdx = this.getPageIndex();
    if ((!this.isEmpty()) && (pgIdx > 0)) {
      result = this.indexRange.getMin() + (this.itemsPerPage * (pgIdx - 1));
    }
    return result;
  }
  
  /**
   * Get the index of the last record on the current Page. These indices is within the
   * specified IndexRange.
   * @return the end index (always &le; this.indexRange.max) or 0 if this.isEmpty.
   */
  public Integer getEndIndex() {
    Integer result = 0;
    Integer pgIdx = this.getPageIndex();
    if ((!this.isEmpty()) && (pgIdx > 0)) {
      result = Math.min(this.indexRange.getMax()+1,
                        (this.indexRange.getMin() + (this.itemsPerPage * pgIdx)));
    }
    return result;
  }
  
  /**
   * Check if the current page is the First Page
   * @return true if this.isEmpty or this.pageIndex &le; 1)
   */
  public boolean isFirstPage() {
    return ((this.isEmpty()) || (this.getPageIndex() <= 1));
  }
  
  /**
   * Check if the current page is the Last Page
   * @return true if this.isEmpty or this.pageIndex &ge; this.pageCount)
   */
  public boolean isLastPage() {
    return ((this.isEmpty()) || (this.getPageIndex() >= this.pageCount));
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Paging Actions">
  /**
   * Set to a specific pageIndex in the range[1..this.pageCount]. Ignored if this.isEmpty.
   * It fires the PageChanged event if the pageIndex has Changed
   * @param newIndex the new paegIndex
   */
  public void setPageIndex(Integer newIndex) {
    if ((newIndex == null) || (this.isEmpty())) {
      return;
    }
    newIndex = Math.min(Math.max(1, newIndex),this.pageCount);
    if (!DataEntry.isEq(newIndex, this.pageIndex)) {
      this.pageIndex  = newIndex;
      this.firePageChanged();
    }
  }
  
  /**
   * Set this.pageIndex = 1. Ignored if this.isEmpty. It fires the 
   * PageChanged event if the pageIndex has Changed
   */
  public void gotoFirst() {
    if (!this.isEmpty()) {
      this.pageIndex = 1;
      this.firePageChanged();
    }
  }
  
  /**
   * Set this.pageIndex = this.pageCount. Ignored if this.isEmpty. It fires the 
   * PageChanged event if the pageIndex has Changed
   */
  public void gotoLast() {
    if (!this.isEmpty()) {
      this.pageIndex = this.pageCount;
      this.firePageChanged();
    }
  }
  
  /**
   * Goto the next Page (i.e., Min(this.pageCount,this.pageIndex+1) It fires the 
   * PageChanged event if the pageIndex has Changed
   */
  public void nextPage() {
    if ((!this.isEmpty()) && (this.pageIndex < this.pageCount)) {
      this.pageIndex++;
      this.firePageChanged();
    }
  }
  
  
  /**
   * Goto the prior Page (i.e., Max(1,this.pageIndex-1). It fires the PageChanged event if
   * the pageIndex has Changed
   */
  public void priorPage() {
    if ((!this.isEmpty()) && (this.pageIndex > 1)) {
      this.pageIndex--;
      this.firePageChanged();
    }
  }
  
  /**
   * Called to move the pageIndex to the page that contains the item with the specified
   * <tt>itemIndex</tt>
   * @param itemIndex the selected item index in this.indexRange
   */
  public void gotoItem(int itemIndex) {
    if ((this.indexRange == null) || (!this.indexRange.inRange(itemIndex)) ||
            (this.itemsPerPage == 0)) {
      return;
    }
    
    int newPage = 1;
    while (newPage <= this.pageCount) {
      if ((itemIndex < ((newPage * this.itemsPerPage)) + this.indexRange.getMin())){
        this.setPageIndex(newPage);
        break;
      }                                        
      newPage++;
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: "TablePageManager[ Range[?..?]; perPage=?; pageCount=?; pageIndex=? ]</p>
   */
  @Override
  public String toString() {
    String result = "TablePageManager[";
    if (isEmpty()) {
      result += "empty]";
    } else {
      result += " " + this.indexRange.toString();
      result += "; perPage=" + this.itemsPerPage;
      result += "; pageCount=" + this.pageCount;
      result += "; pageIndex=" + this.pageIndex;
      result += " ]";
    }
    return result;
  }
  // </editor-fold>
}
