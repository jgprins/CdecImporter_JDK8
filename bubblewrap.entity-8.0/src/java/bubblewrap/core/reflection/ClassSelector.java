package bubblewrap.core.reflection;

import bubblewrap.core.annotations.ClassInfo;
import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventHandler;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;

/**
 *
 * @author kprins
 */
public class ClassSelector<TBase> implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger =
          Logger.getLogger(ClassSelector.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Selector's Base Class
   */
  private Class<TBase> mpBaseClass;
  /**
   * Placeholder for the names of the Packages to explore
   */
  private List<PackageLoader> mpPackages;
  /**
   * The list of SubClasses of the BaseClass
   */
  private List<Class<? extends TBase>> mpSubClasses;
  /**
   * The Label for the Null Value
   */
  private String msNullLabel;
  /**
   * The Label to display if the SubClass List is empty
   */
  private String msEmptyLabel;
  /**
   * Get the index of the selected class.
   */
  private Integer miSelection;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Event Sender">
  /**
   * EventHandler for sending a Selection Changed event.
   */
  public final EventHandler SelectionChanged;
  /**
   * Method called to fie the Selection Changed event.
   */
  protected void fireSelectionChanged() {
    this.SelectionChanged.fireEvent(this, new EventArgs());
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor with a baseClass reference in only one Package
   */
  public ClassSelector(Class<TBase> pBaseClass, PackageLoader pPackage) {
    this(pBaseClass, (pPackage==null)? null: new PackageLoader[]{pPackage});
  }
  
  /**
   * Public Constructor with a baseClass reference and multiple Packages
   */
  public ClassSelector(Class<TBase> pBaseClass, PackageLoader[] pPackages) {
    this.SelectionChanged = new EventHandler();
    this.mpBaseClass = null;
    this.mpPackages = null;
    this.msNullLabel = null;
    this.msEmptyLabel = null;    
    this.mpSubClasses = null;
    
    if (pBaseClass == null) {
      throw new NullPointerException("The ClassSelector's BaseClass cannot be "
              + "unassigned");
    }
    
    if ((pPackages == null) || (pPackages.length == 0)) {
      throw new NullPointerException("The ClassSelector's Package Loader(s) "
              + "cannot by unassigned.");
    }
    
    this.mpBaseClass = pBaseClass;
    this.mpPackages = new ArrayList<>();
    for (PackageLoader pPackage : pPackages) {
      if (pPackage != null) {
        this.mpPackages.add(pPackage);
      }
    }
    
    if (this.mpPackages.isEmpty()) {
      throw new NullPointerException("The ClassSelector's Package Loader(s) "
              + "cannot by unassigned.");
    }
    
    this.msNullLabel = null;
    this.msEmptyLabel = null;    
    this.mpSubClasses = null;
  }  
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Call the super method before disposing the local 
   * resources</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.SelectionChanged.clear();
    this.mpPackages = null;
    this.mpSubClasses = null;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Protected Methods">
  /**
   * Called by {@linkplain #getSubClasses() getSubClasses} to get the sub-classes of 
   * the base class located in the specified Packages. If the Selector has one or more
   * packages assigned, it will call {@linkplain #onGetClassesInPackage(
   * java.lang.String, java.util.List) onGetClassesInPackage} to retrieve a list of 
   * classes that inherits from the.baseClass.
   * @return a list with SubClasses or an empty list if non is found.
   * @throws Exception 
   */
  @SuppressWarnings("unchecked")
  private List<Class<? extends TBase>> onGetSubClasses() throws Exception {
    List<Class<? extends TBase>> pResult = new ArrayList<>();
    if ((this.mpBaseClass != null) && 
                        (this.mpPackages != null) && (!this.mpPackages.isEmpty())) {
      for (PackageLoader pPackage : this.mpPackages) {
        List<Class> pClasses = pPackage.getClasses();
        if ((pClasses == null) || (pClasses.isEmpty())) {
          continue;
        }
        
        for (Class pClass : pClasses) {
         if (this.mpBaseClass.isAssignableFrom(pClass)) {
            Class<? extends TBase> pSubClass = (Class<? extends TBase>) pClass;
            pResult.add(pSubClass);
         }
        }
      }
    }
    return pResult;
  }  
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Selector Base Class
   * @return the assigned Base Class of type TBase
   */
  public Class<TBase> getBaseClass() {
    return mpBaseClass;
  }
  
  /**
   * Assign the Selectors Null and Empty Labels. The Null Label is displayed as the
   * first item in the selection list. The Empty label is displayed as the null value
   * when the selection list is empty. The defaults are "-- Select a " + this.baseClass
   * + " Sub-class --" and "-- " + this.baseClass + " has no SubClasses"
   * @param sNullLabel the new Null Label (or null to accept the default label)
   * @param sEmptyLabel the new Empty Label (or null to accept the default label)
   */
  public void setNullLabels(String sNullLabel, String sEmptyLabel) {
    this.msNullLabel = DataEntry.cleanString(sNullLabel);
    this.msEmptyLabel = DataEntry.cleanString(sEmptyLabel);
  }
  
  /**
   * Get the Selector's Null value Label
   * @return the assigned NullLabel (default = "-- Select a " + this.baseClass
   * + " Sub-class --"
   */
  public String getNullLabel() {
    return (this.msNullLabel == null)? "-- Select a " + this.mpBaseClass.getSimpleName()
            + " Sub-class --": this.msNullLabel;
  }
  
  /**
   * Get the Selector's Empty List Label
   * @return the assigned EmptyLabel (default = "-- " + this.baseClass +
   * " has no SubClasses"
   */
  public String getEmptyLabel() {
    return (this.msEmptyLabel == null)? "-- " + this.mpBaseClass.getSimpleName()
            + " has no Sub-classes --": this.msEmptyLabel;
  }
  
  /**
   * <p><b>NOTE:</b> Get the List of SubClasses sorted by their simpleNames. If the list
   * has not yet been initiated it calls protected {@linkplain #onGetSubClasses() 
   * onGetSubClasses} method to initiate and return the list. The returned list is 
   * sorted before a reference to the list is returned.</p>
   *  <p><b>NOTE:</b> Exceptions are logged.</p>
   * @return A list of sub classes. The list is empty if no subclasses exists or an
   * error occurred.
   */
  public final List<Class<? extends TBase>> getSubClasses() {
    if (this.mpSubClasses == null) {
      try {
        this.mpSubClasses = this.onGetSubClasses();
        if ((this.mpSubClasses != null) && (this.mpSubClasses.size() > 1)) {
          Collections.sort(this.mpSubClasses, new ClassByNameComparator());
        }
      } catch (Exception pExp) {
        this.mpSubClasses = null;
        logger.log(Level.WARNING, "{0}.getSubClasses Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      } finally {
        if (this.mpSubClasses == null) {
          this.mpSubClasses = new ArrayList<>();
        }
      }
    }
    return this.mpSubClasses;
  }
  
  /**
   * Get the Current Selection Index
   * @return the current index (default = null)
   */
  public Integer getSelectId() {
    return this.miSelection;
  }
  
  /**
   * Set the Selection Index. Fire Event[CHANGED] is the selection has changed.
   * @param iSelection the new Index.
   */
  public void setSelectId(Integer iSelection) {
    if (!DataEntry.isEq(this.miSelection, iSelection)) {
      this.miSelection = iSelection;
      this.fireSelectionChanged();
    }
  }
  
  /**
   * Get the currently selected SubClass
   * @return the selected SubClass of BaseClass<TBase>
   */
  public Class<? extends TBase> getSelection() {
    Class<? extends TBase> pResult = null;
    try {
      if ((this.miSelection != null) && (this.miSelection >= 0)) {
        List<Class<? extends TBase>> pSubClasses = this.getSubClasses();
        if ((pSubClasses != null) && (this.miSelection < pSubClasses.size())) {
          pResult = pSubClasses.get(this.miSelection);
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSelectedClass Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  
  
  /**
   * Set the new selected SubClass. The selection is cleared if pSubClass=null or the
   * SubClass cannot be found..
   * @param pSubClass the new subclass 
   */
  public void setSelection(Class<? extends TBase> pSubClass) {
    Integer iSelection = null;
    try {
      if (pSubClass != null) {
        List<Class<? extends TBase>> pSubClasses = this.getSubClasses();
        if ((pSubClasses != null) && (pSubClasses.contains(pSubClass))) {
          iSelection = pSubClasses.indexOf(pSubClass);
          if (iSelection < 0) {
            iSelection = null;
          }
        }
      } 
    } catch (Exception pExp) {
      iSelection = null;
      logger.log(Level.WARNING, "{0}.setSelectedClass Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.setSelectId(iSelection);
    }
  }
  
  /**
   * Get the SubClasses as a selection list. Return a list with a single item - with the 
   * Empty Label - if the list is empty. Otherwise, the first item in the List has the 
   * Null value Label and the value of null.
   * @return a list of selection options.
   */
  public final List<SelectItem> getSelectOptions() {
    List<SelectItem> pResult = new ArrayList<>();
    List<Class<? extends TBase>> pSubClasses = this.getSubClasses();
    if ((pSubClasses != null) && (!pSubClasses.isEmpty())) {
      pResult.add(new SelectItem(null, this.getNullLabel()));
      
      for (int iItem = 0; iItem < pSubClasses.size(); iItem++) {
        Class<? extends TBase> pSubClass = pSubClasses.get(iItem);
        if (pSubClass != null) {
          pResult.add(new SelectItem(iItem, this.getClassLabel(pSubClass)));
        }
      }
    } else {
      pResult.add(new SelectItem(null, this.getEmptyLabel()));
    }
    return pResult;
  }
  
  /**
   * Get the Class SelectItem Label for the class' ClassInfo Annotation 
   * @param pSubClass the class of interest
   * @return ClassInfo Annotation's classLabel or this class' simpleName if the 
   * Annotation is not assigned.
   */
  private String getClassLabel(Class<? extends TBase> pSubClass) {
    String sResult = null;
    if (pSubClass != null) {
      ClassInfo pInfo = pSubClass.getAnnotation(ClassInfo.class);
      String sLabel = (pInfo == null)? null: DataEntry.cleanString(pInfo.classLabel());
      sResult = (sLabel == null)? pSubClass.getSimpleName(): sLabel;
    }
    return sResult;
  }
  //</editor-fold>  
}
