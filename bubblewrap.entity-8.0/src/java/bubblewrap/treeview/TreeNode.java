package bubblewrap.treeview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import bubblewrap.core.events.CancelEventHandler;
import bubblewrap.core.events.CancelEventArgs;
import bubblewrap.core.events.EventHandler;
import bubblewrap.core.events.EventArgs;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.treeview.annotation.TreeNodeTypeDef;
import bubblewrap.treeview.enums.NodeExpandType;
import bubblewrap.treeview.enums.TreeNodeType;
import bubblewrap.io.DataEntry;
import java.util.Collections;

/**
 * A TreeNode class represents a node in a {@linkplain TreeView}. It has a TreeNode
 * parent - unless it is a RootNode and can have one or more (optional) child TreeNodes.
 * <p>
 * For managing the children the TreeNode provides the following methods: <ul>
 * <li>{@linkplain #loadChildren() this.loadChildren} - which is automatically 
 * triggered to load the children.</li>
 * <li>{@linkplain #addChild(bubblewrap.treeview.TreeNode) this.addChild} to add a new
 * child to the TreeNode</li>
 * <li>{@linkplain #removeChild(java.lang.String) this.removeChild} (3 overloads) to
 * remove a child from the three node without deleting the node's associated content.
 * Also {@linkplain #removeChildren() this.removeChildren} to remove all children.
 * Removal is only possible to if {@linkplain #canRemoveNode() this.canRemoveNode} 
 * returns true and the node can be {@linkplain #canUnSelectNode() this.unselected}.
 * </li>
 * <li>{@linkplain #deleteChild(java.lang.String) this.deleteChild} (3 overloads) to
 * delete a child from the three node and to deleting the node's associated content.
 * Also {@linkplain #deleteChildren() this.deleteChildren} to remove all children.
 * Removal is only possible to if {@linkplain #canDeleteNode() this.canDeleteNode} 
 * returns true and the node can be {@linkplain #canUnSelectNode() this.unselected}.
 * </li>
 * <li>{@linkplain #clearChildren() this.clearChildren} called to unload the node's
 * children without removing or delete the nodes. This call will set this.isUdpated = 
 * false to force a reload of the TreeNode's content.</li>
 * </ul>
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class TreeNode<TChild extends TreeNode<?, TView>,
                        TView extends TreeView<?, TView>> 
                        implements Serializable, Comparable<TreeNode>, Iterable<TChild> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(TreeNode.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Check if the specified TreeNode class is a designated Root node - a node that has no
   * parent, but can have children.  By default all node's are {@link TreeNodeTypes#Document} nodes.
   * @param nodeClass the specified TreeNode class
   * @return true if the class is defined, has a {@link TreeNodeTypeDef} annotation,
   * and the annotation's nodeType={{@link TreeNodeTypes#TreeRoot}
   */
  public static boolean isTreeRoot(Class<? extends TreeNode> nodeClass) {
    boolean result = false;
    try {
      if (nodeClass != null) {
        TreeNodeTypeDef annot = nodeClass.getAnnotation(TreeNodeTypeDef.class);
        result = ((annot != null) && (TreeNodeType.TreeRoot.equals(annot.nodeType())));
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.isTreeRoot Error:\n {1}",
              new Object[]{TreeNode.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Check if the specified TreeNode class is a designated Container node - a node that
   * can have children. Must have a parent. By default all node's are {@link TreeNodeTypes#Document} nodes.
   * @param nodeClass the specified TreeNode class
   * @return true if the class is defined, has a {@link TreeNodeTypeDef} annotation,
   * and the annotation's nodeType={@linkplain TreeNodeTypes#FOLDER}
   */
  public static boolean isFolder(Class<? extends TreeNode> nodeClass) {
    boolean result = false;
    try {
      if (nodeClass != null) {
        TreeNodeTypeDef annot = nodeClass.getAnnotation(TreeNodeTypeDef.class);
        result = ((annot != null) && 
                  (TreeNodeType.Folder.equals(annot.nodeType())));
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.isTreeRoot Error:\n {1}",
              new Object[]{TreeNode.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * A Unique NodeID - auto-generated
   */
  @XmlElement(name = "nodeId")
  private String nodeId;
  /**
   * The TreeNode's TreeNodeTypeDef
   */
  @XmlElement(name = "nodeType")
  private TreeNodeType nodeType;
  /**
   * The TreeNode's Caption
   */
  private String caption;
  /**
   * The TreeNode's HTML Element Title
   */
  private String title;
  /**
   * The TreeNode's status
   */
  private String nodeStatus;  
  /**
   * The Flag controlling whether the TreeNode's status is displayed (default = false)
   */
  private Boolean showStatus;
  /**
   * The TreeNode'sList display Index
   */
  private Integer displayIdx;
  /**
   * The TreeNode's TreeView reference
   */
  @XmlTransient
  private TView treeView;
  /**
   * The TreeNode's Parent reference
   */
  @XmlTransient
  private TreeNode<?,TView> parent;
  /**
   * The TreeNode's List of Children
   */
  @XmlTransient
  private List<TChild> children;
  /**
   * A placeholder for a custom Node tag
   */
  private String nodeTag;
  /**
   * The TreeNode's visible state (default=null|true)
   */
  private Boolean visible;
  /**
   * The TreeNode's disabled state (default=null|false)
   */
  private Boolean disabled;
  /**
   * When set, the FormMask will be displayed when the node is selected and while its
   * content is loaded.
   */
  private Boolean showMaskOnSelect;
  /**
   * The TreeNode's expanded state (default=null|false)
   */
  @XmlTransient
  private Boolean expanded;
  /**
   * The TreeNode's updated - whether children is loaded or not (default=null|false)
   */
  @XmlTransient
  private Boolean updated;
  /**
   * A transient flag set during processing to prevent the 
   */
  @XmlTransient
  private transient int loadingCount = 0;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="EventHandlers">
  /**
   * The EventHandler that fires the Selection Changing Event.
   */
  @XmlTransient
  public final CancelEventHandler SelectionChanging;
  
  /**
   * Called to fire the Selection Changing Event.
   * @param eventArgs the event info
   */
  protected final void fireSelectionChanging(CancelEventArgs eventArgs) {
    this.SelectionChanging.fireEvent(this, eventArgs);
  }
  
  /**
   * The EventHandler that fires the Selection Changed Event.
   */
  @XmlTransient
  public final EventHandler SelectionChanged;
  
  /**
   * Called to fire the Selection Changed Event.
   * @param eventArgs the event info
   */
  protected final void fireSelectionChanged(EventArgs eventArgs) {
    this.SelectionChanged.fireEvent(this, eventArgs);
  }
  
  /**
   * The EventHandler that fires the Property Changed Event.
   */
  @XmlTransient
  public final EventHandler PropertyChanged;

  /**
   * Called to fire the Property Changed Event. The event is fired when any of the
   * TreeNdoe's property has changed.
   */
  protected final void firePropertyChanged(String fieldName) {
    EventArgs args = new EventArgs();
    this.onPropertyChanged(fieldName, args);
    if (!args.isHandled()) {
      this.PropertyChanged.fireEvent(this, args);
    }
  }
  
  /**
   * Called by {@linkplain #firePropertyChanged() this.firePropertyChanged} before a
   * PropertyChanged is fired. This method can be overridden to handle the custom the 
   * event. To prevent PropertyChanged event to be fired call {@linkplain 
   * EventInfo#setHandled() args.setHandled()} to set its isHandled flag.
   * @param args the EventArgs arguments
   */
  protected void onPropertyChanged(String fieldName, EventArgs args){};  
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  protected TreeNode() {
    super();
    this.SelectionChanging = new CancelEventHandler();
    this.SelectionChanged = new EventHandler();
    this.PropertyChanged = new EventHandler();
    
    this.nodeId = DataEntry.newUniqueId();
    this.parent = null;
    this.treeView = null;
    this.caption = null;
    this.title = null;
    this.nodeTag = null;
    this.nodeType = null;
    this.showMaskOnSelect = false;
    this.displayIdx = 0;
    this.children = new ArrayList<>();
    this.updated = null;
    
    TreeNodeTypeDef annot = this.getClass().getAnnotation(TreeNodeTypeDef.class);
    this.nodeType = ((annot == null) || (annot.nodeType() == null))?
                      TreeNodeType.Document: annot.nodeType();
 
    this.resetState();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call the super method before disposing local resources.</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.SelectionChanged.clear();
    this.SelectionChanging.clear();
    this.PropertyChanged.clear();
    this.treeView = null;
    this.parent = null;
    this.clearChildren();
  }
  // </editor-fold>
    
  // <editor-fold defaultstate="collapsed" desc="Reflection Methods">
  /**
   * FINAL: Get the TreeNode's generically assigned Child Class
   * @return Class&lt;TChild&gt;
   */
  @SuppressWarnings("unchecked")
  public final Class<TChild> getChildClass() {
    Class myClass = this.getClass();
    return ReflectionInfo.getGenericClass(TreeNode.class, myClass, 0);
  }
  
  /**
   * FINAL: Get the TreeNode's generically assigned TreeView Class
   * @return Class&lt;TView&gt;
   */
  @SuppressWarnings("unchecked")
  public final Class<TView> getTreeViewClass() {
    Class myClass = this.getClass();
    return ReflectionInfo.getGenericClass(TreeNode.class, myClass, 1);
  }
  
  /**
   * Called to check if the specified <tt>nodeClass</tt> is a valid Child Node Class
   * (as generically defined by TChild).
   * @param nodeClass the Node class to evaluate
   * @return true if <tt>ndoeClass</tt> != null and assignable to {@linkplain 
   * #getChildClass() this.childClass}.
   */
  public boolean isValidChildClass(Class<? extends TreeNode> nodeClass) {
    boolean result = false;
    try {
      if (nodeClass == null) {
        throw new Exception("The specified Node Class is uanssigned.");
      }
      
      Class<TChild> childClass = this.getChildClass();
      if (childClass == null) {
        throw new Exception("Unable to access the TreeNode's generically assigned "
                + "Child class.");
      }
      
      result = childClass.isAssignableFrom(nodeClass);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.isValidChildClass Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  } 
  
  /**
   * Called to cast treeNode as generically reference TChild class
   * @param treeNode the tree node instance to convert
   * @return the casted child node type
   * @throws IllegalArgumentException if <tt>treeNode</tt> = null or not and instance
   * of this.childClass.
   */
  @SuppressWarnings("unchecked")
  public final TChild toChildNode(TreeNode<?,TView> treeNode) {
    TChild result = null;
    try {
      if (treeNode == null) {
        throw new Exception("The Tree Node is null.");
      }
      
      Class<TChild> childClass = this.getChildClass();
      if (childClass == null) {
        throw new Exception("Unable to access the TreeNode's generically assigned "
                + "Child class.");
      }
      
      if (!childClass.isInstance(treeNode)) {
        throw new Exception("TreeNode Class[" + treeNode.getClass().getSimpleName() 
                            + "] is not an instance of the designated Child Class["
                            + childClass.getSimpleName() + "].");
      }
      
      result = (TChild) treeNode;
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".toChildNode Error:\n " + exp.getMessage());
    }
    return result;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Manage the isLoading state">
  /**
   * Called to start the isLoading state. 
   * Every call increment this.loadingCount 
   */
  public final void beginLoading() {
    this.loadingCount = (this.loadingCount < 0)? 0: this.loadingCount;
    this.loadingCount++;
  }
  
  /**
   * Called to end the isLoading state. 
   * Every call decrement this.loadingCount if &gt; 0. If this.loadingCount = 0, it
   * calls {@linkplain #fireContentChanged() this.fireContentChanged}
   */
  public final void endLoading() {
    if (this.loadingCount > 0) {
      this.loadingCount--;
      if (this.loadingCount == 0) {
        this.fireContentChanged();
      }
    }
  }
  
  /**
   * Called to notify this.parent or this treeView (if this parent=null and 
   * this.treeView != null) of a change in content (children were added or removed) 
   * by calling their fireContentChanged method.
   */
  protected final void fireContentChanged() {
    if (this.isLoading()) {
      return;
    }
    
    if (this.parent != null) {
      this.parent.fireContentChanged();
    } else if (this.treeView != null) {
      this.treeView.fireContentChanged();
    }
  }
  
  /**
   * Get the isLoading state
   * @return true if the this.loadingCount &gt; 0
   */
  public final boolean isLoading() {
    return (this.loadingCount > 0);
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Node Path Methods">
  /**
   * Build a Node selection path based on the Node's NodeId using a "/" delimiter. All
   * Tags are clean and converted to lower case. If this node's NodeId == null, it will
   * be skipped in the path.
   * <p>
   * <b>NOTE:</b> Each node should have a unique Id that will not change when the 
   * TreeView reloads.</p>
   * @return this.parent.path + "/" + this.NodeId or this.NodeId if this.parent=null
   */
  public String getPath() {
    String result = "";
    try {
      TreeNode parentNode = this.getParent();
      if (parentNode != null) {
        result = parentNode.getPath();
      }
      String nodeKey = DataEntry.cleanLoString(this.getNodeId());
      if (nodeKey != null) {
        result += "/" + nodeKey;
      }      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getPath Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Selected this node (or one of its children) based on the defined path. It checks
   * if the path starts with this.nodeId - it this nodeId = null. If true it:<ul>
   *  <li>sets subPath = path.remodeFirst(this.nodeId)</li>
   *  <li>If this.nodeId == null or is in <tt>path</tt> and subPath != null and this
   *    has children call the child.electByPath(subPath) until a match is found</li>
   *  <li>Else if this.nodeId is in Path or this.isRoot, call {@linkplain #selectNode() 
   *    this.selectNode} to set this as the treeView's selected Node</li>
   * </ul> 
   * @param path the Selection Path
   * @return true if this or one of it children has been set as the selected node. 
   */
  public boolean selectByPath(String path) {
    boolean result = false;
    boolean isSelected = false;
    if ((path = this.cleanPath(path)) != null) {
      String tag = DataEntry.cleanLoString(this.getNodeId());
      String subPath = null;
      if (tag != null) {
        if (path.startsWith(tag)) {
          subPath = cleanPath(path.replaceFirst(tag, ""));
          isSelected = true;
        }
      }
      
      if (((tag == null) || (isSelected)) && (subPath != null) && 
              (this.hasChildren())) {
        for (TChild child : this) {
          if (child.selectByPath(subPath)) {
            result = true;
            break;
          }
        }
      } else if ((isSelected) || (this.isRoot())) {
        result = true;
        this.selectNode();
      }
    } else if (this.isRoot()) {
      result = true;
      this.selectNode();
    }
    return result;
  }
  
  /**
   * Called to "clean" the path - stripping and leading '/' delimiters and spaces and
   * convert the path to lowercase.
   * @param path the input path
   * @return the cleaned path
   */
  private String cleanPath(String path) {
    if ((path != null) && (path.startsWith("/"))) {
      path = path.replaceFirst("/", "");
    }
    return DataEntry.cleanLoString(path);
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Managing the Loading of Child Nodes">
  /**
   * Get the Node's isUpdated state
   * @return true if the children has been loaded.
   */
  public boolean isUpdated() {
    return ((this.updated !=null) && (this.updated));
  }
  
  /**
   * This method can be called by a parent, but it is called when attempting to access
   * this node children to trigger a lazy loading of the node's children if its not 
   * already loaded. This call is ignored if
   * this.isUpdated or this.isLoading.
   * <p>
   * This method calls {@linkplain #onLoadChildren() this.onLoadChildren} and if
   * successfully completed (no exception is thrown), it sets this.isUpdated = true.
   * It class begin- and endLoading before and after processing the call.
   * <p>
   * If an exception is thrown, all loaded children will be cleared and it sets
   * this.isUpdated = false. The error will be logged.
   */
  protected final void loadChildren() {
    if ((this.isUpdated()) || (this.isLoading())) {
      return;
    }
    
    try {
      this.beginLoading();
      
      this.onLoadChildren();
      this.updated = true;
    } catch (Exception exp) {
      this.clearChildren();
      logger.log(Level.WARNING, "{0}.loadChildren Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.endLoading();
    }
  }
  
  /**
   * ABSTRACT: Called by {@linkplain #loadChildren() this.loadChildren} to custom
   * load this TreeNode's children. This custom loading of the children should
   * call {@linkplain #addChild(bubblewrap.treeview.TreeNode) this.addChild} to
   * added the children to the node.
   * <p><b>Note:</b> this.isLoading = true during this call.</p>
   * @throws Exception if any error occur.
   */
  protected abstract void onLoadChildren() throws Exception;
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private/Protected Final Methods">
  /**
   * This method attempts to disconnect the child from this parent. It calls
   * {@linkplain #onDisconnectAsChild() this.onDisconnectAsChild()} for
   * custom disconnecting of the parent-child relationship (e.g., removing event 
   * listeners) before it calls {@linkplain #resetParentNode() 
   * this.resetParentNode()} to reset a child's parent reference and cached
   * treeView reference.
   */
  protected final void disconnectAsChild() {
    try {
      this.onDisconnectAsChild();
      this.resetParentNode();
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.diconnectChild Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }   
  
  /**
   * CAN OVERRIDE: Called from {@linkplain #disconnectChild(bubblewrap.core.treeview.TreeNode) 
   * this.disconnectChild(child)} after calling the child's {@linkplain
   * #resetParentNode(bubblewrap.core.treeview.TreeNode) resetParentNode(this)} method.
   * <p>The base method does nothing
   * @param child the node to be disconnected.
   */
  protected void onDisconnectAsChild(){}
  
  /**
   * <p>
   * Called from {@linkplain #assignTo(bubblewrap.core.treeview.TreeNode) this.assignTo} when
   * and Exception if thrown to rollback the action of the assignTo process.This process 
   * include the following two step - each running in its own try-catch block and all 
   * errors are logged and ignored:</p><ul>
   * <li>If target.allowChildren, it calls {@linkplain #deleteChildren() 
   * target.deleteChildren()} to delete all child nodes and there content.
   * </li>
   * <li>Next it calls {@linkplain #onDeleteNode() target.onDeleteNode()} to reverse the
   * assignments made during the {@linkplain #onAssignTo(bubblewrap.core.treeview.TreeNode)
   * this.onAssignTo} process or to delete any persistent content from external sources.
   * </li>
   * </ul> 
   * @param target the target who's assignTo content must be rolled back.
   */
  private void rollBackAssignTo(TreeNode<TChild,TView> target) {
    if ((target != null) && (target.hasChildren())) {
      try {
        target.onDeleteChildren();
      } catch (Exception exp) {
        logger.log(Level.WARNING, "Node{0}.deleteChildren Error:\n {1}",
                new Object[]{target.getCaption(), exp.getMessage()});
      }
    }
    
    try {
      target.beforeDeleteNode();
    } catch (Exception exp) {
      logger.log(Level.WARNING, "Node{0}.onDelete Target Error:\n {1}",
              new Object[]{target.getCaption(), exp.getMessage()});
    }
  }
    
  /**
   * Called internally to set the TreeView of a rootNode. Ignored if 
   * treeView=null.
   * @param treeView reference to the owner TreeView (must be assigned)
   */
  protected final void setTreeView(TView treeView){
    if (treeView == null) {
      return;
    }
    
    if (!(TreeNodeType.TreeRoot.equals(this.nodeType))) {
      throw new IllegalArgumentException("Node[" + this.getCaption() 
              + "]'s TreeView cannot be assigned because it is "
              + "not a designated ROOT nodeType.");
    }
    
    if (this.treeView != null) {
      throw new IllegalArgumentException("RootNode[" + this.getCaption() 
              + "] is alread assigned to a TreeView and cannot be reassigned.");
    }
    this.parent = null;
    this.treeView = treeView;
    if ((this.isUpdated()) && (this.hasChildren())) {
      for (TChild child : this.children) {
        child.initTreeView(treeView, this);
      }
    }
  }
  
  /**
   * <p>
   * Called from {@linkplain #setTreeView(bubblewrap.core.treeview.TreeView)
   * rootNode.setTreeView(treeView)} or parent.initTreeView(treeView, parent.parent) to
   * assign the children's parent and treeView references. Typically, called after
   * deserialization from an XML source.</p>
   * <p>
   * The process is skipped if either <tt>treeView</tt> or <tt>parent</tt> = null or if
   * the TreeNode's parent is already assigned.</p>
   * <p><b>NOTE:</b> Do not call this out of context.</p>
   * @param treeView the owner TreeView
   * @param parent the child's parent
   */
  protected final void initTreeView(TView treeView, TreeNode<?, TView> parent) {
    if ((treeView == null) || (parent == null) || (this.parent != null)) {
      return;
    }
    
    this.treeView = treeView;
    this.parent = parent;
    if ((this.isUpdated()) && (this.hasChildren())) {
      for (TChild child : this.children) {
        child.initTreeView(treeView, this);
      }
    }
  }
  
  /**
   * Called to reset treeView reference of this RootNode (called from {@linkplain 
   * #disconnectChild(bubblewrap.core.treeview.TreeNode) parent.disconnectChild.}). Ignored if
   * parent == null or not this.parent.
   * @param treeView the node's parent
   */
  protected final void resetTreeView(TView treeView) {
    if ((TreeNodeType.TreeRoot.equals(this.nodeType)) && (this.treeView != null) && 
            (this.treeView == treeView)) {
      this.parent = null;
      this.treeView = null;
    }
  }
  
  /**
   * Called internally to set the TreeView and Parent Relationships. Ignored if 
   * parent=null.
   * @param treeView reference to the owner TreeView (must be assigned)
   * @param parent reference to the parent - can only be null if this node's class has
 a designated nodeType=TreeRoot
   */
  protected final void setParentNode(TreeNode<?,TView> parent){
    if (parent == null) {
      return;
    }
    
    if (this.parent != null) {
      throw new IllegalArgumentException("Node[" + this.getCaption() 
              + "] is alread assigned to Parent["
              + this.parent.getCaption() + "] and cannot be reassigned.");
    }
    
    if (TreeNodeType.TreeRoot.equals(this.nodeType)) {
      throw new IllegalArgumentException("The TreeNode's Parent cannot be assigned if "
              + "this is a designated ROOT nodeType.");
    }
    this.parent = parent;
    this.treeView = null;
  }
  
  /**
   * Called to reset parent as this Node's parent (called from {@linkplain 
   * #disconnectChild(bubblewrap.core.treeview.TreeNode) parent.disconnectChild.}).
   * Ignored if parent == null or not this.parent.
   * @param parent the node's parent
   */
  protected final void resetParentNode() {
    if (this.parent != null) {
      this.parent = null;
      this.treeView = null;
    }
  }
  
  /**
   * Called to check if the node can be selected if this is not the TreeView's Selected 
   * node. It if (!this.isSelected) calls {@linkplain #onCanSelectNode() 
   * this.onCanSelectNode} and return the result. Else, return true.
   * @return true is the node can be isSelected
   */
  protected final boolean canSelectNode() {
    boolean result = true;
    try {
      if (!this.isSelected()) {
        result = this.onCanSelectNode();
      }
    } catch (Exception exp) {   
      result = false;
      logger.log(Level.WARNING, "{0}.canSelectNode Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to check if the node can be un-selected if this is the selected node. It 
   * this.isSelected, it calls {@linkplain #onCanUnSelectNode() this.onCanUnSelectNode} 
   * to do a custom validation. If this node can be unselected and its has children,
   * it calls each child's canUnSelectNode to check if any child is selected and if 
   * selected whether is can be unselected.
   * @return true if the node and any of its children is selected and can be unselected.
   */
  protected final boolean canUnSelectNode() {
    boolean result = true;
    try {
      if (((!this.isSelected()) || (result = this.onCanUnSelectNode())) && 
              (this.hasChildren())) {
        for (TChild child : this.children) {
          if (!child.canUnSelectNode()) {
            result = false;
            break;
          }
        }
      }
    } catch (Exception exp) {   
      result = false;
      logger.log(Level.WARNING, "{0}.canUnSelectNode Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called by the TreeView while the TreeView.isSelecting before selecting a new node 
   * while this node is still the selected node. 
   * If is called after calling {@linkplain #canUnSelectNode() this.canUnSelectNode} 
   * and got permission to unselect this node.
   * <p>The method only calls {@link #onBeforeUnSelectNode() this.onBeforeUnSelectNode}
   * to custom handle the request. Inheritor can override this method, to reset 
   * resources, unlock content, etc. when the node become unselected.
   * <p>All errors are trapped and logged.
   */
  protected final void beforeUnSelectNode() {
    try {
      this.onBeforeUnSelectNode();
    } catch (Exception exp) {  
      logger.log(Level.WARNING, "{0}.beforeUnSelectNode Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to check if the node can be removed from its parent and the TreeView. 
   * It check if this node (or any of its children is selected and can be unselected. If
   * true, it calls {@linkplain #onCanRemoveNode() this.onCanRemoveNode} to do a 
   * custom validation, and if true and it has children, it checks if all its children 
   * (and their children) can be removed.
   * @return true is the node and its children can be unselected and removed from its
   * parent and the TreeView.
   */
  protected final boolean canRemoveNode() {
    boolean result = this.canUnSelectNode();
    try {
      if ((result) && (result = this.onCanRemoveNode()) && (this.hasChildren())) {
        for (TChild child : this.children) {
          if (!child.canRemoveNode()) {
            result = false;
            break;
          }
        }
      }
    } catch (Exception exp) {   
      result = false;
      logger.log(Level.WARNING, "{0}.canRemoveNode Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
    
  /**
   * Called to check if the node can be delete (and removed) from its parent and the 
   * TreeView. It check if this node (or any of its children is selected and can be 
   * unselected. If true, it calls {@linkplain #onCanDeleteNode() this.onCanDeleteNode} 
   * to do a custom validation, and if true and it has children, it checks if all its 
   * children (and their children) can be deleted.
   * @return true is the node and its children can be unselected and deleted and removed
   * from its parent and the TreeView.
   */
  protected final boolean canDeleteNode() {
    boolean result = this.canUnSelectNode();
    try {
      if ((result) && (result = this.onCanDeleteNode()) && (this.hasChildren())) {
        for (TChild child : this.children) {
          if (!child.canDeleteNode()) {
            result = false;
            break;
          }
        }
      }
    } catch (Exception exp) {   
      result = false;
      logger.log(Level.WARNING, "{0}.canDeleteNode Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
      
  /**
   * Called to remove a Child Node after the permission to remove the node had been
   * verified.  This method is skipped if (!this.children.contains(child)). This process
   * included the following steps:<ul>
   * <li>Call {@linkplain #unSelectNode() child.unSelectNode} - to unselect the node if 
   * it is the TreeView's selected node.</li>
   * <li>Call {@linkplain #onRemoveChildren() this.onRemoveChildren} - to remove the 
   * child node's children before removing the child node.</li>
   * <li>Call {@link #beforeRemoveNode() child.beforeRemoveNode} - to reset any custom 
   * resources before the child is removed from its parent and the TeeView</li>
   * <li>Call {@linkplain #disconnectChild(bubblewrap.core.treeview.TreeNode) 
   * this.disconnectChild(child)}</li>
   * <li>Finally remove the child Node from this.children.</li>
   * </ul>
   * @param child the child node to remove
   */
  protected final void onRemoveAsChild() {    
    try {
      this.beginLoading();
      this.unSelectNode();
      try {
        this.onRemoveChildren();
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.removeChild.onRemoveChildren Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
      
      try {
        this.beforeRemoveNode();
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.removeChild.beforeRemoveNode Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
      
      try {
        this.disconnectAsChild();
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.removeChild.disconnectChild Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.removeChild Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.endLoading();
    }
  }
  
  /**
   * Called to remove all the Node's children without further verification that the
   * children can be removed. It calls the {@linkplain #onRemoveAsChild() 
   * child.onRemoveAsChild} method for each child before removing the child from
   * this.children list.
   */
  protected final void onRemoveChildren() {
    if ((this.children == null) || (this.children.isEmpty())) {
      return;
    }
    try {
      this.beginLoading();
      for (int iChild = this.children.size()-1; iChild >= 0; iChild--) {
        TChild child = this.children.get(iChild);
        try {
          child.onRemoveAsChild();
        } finally {
          this.children.remove(iChild);
        }
      }
    } finally {
      this.endLoading();
    }
  }
  
  /**
   * Called to delete a Child Node after the permission to delete the node had been
   * obtained.  This method is skipped if (!this.children.contains(child)). This process
   * includes the following steps:<ul>
   * <li>Call {@linkplain #unSelectNode() child.unSelectNode} - to unselect the node if 
   * it is the TreeView's selected node.</li>
   * <li>Call {@linkplain #onDeleteChildren() this.onDeleteChildren} - to delete the 
   * child node's children before deleting the child node</li>
   * <li>Call {@link #beforeDeleteNode() child.beforeDeleteNode} - to delete any custom 
   * resources before the child is deleted from its parent and the TeeView</li>
   * <li>Call {@linkplain #disconnectChild(bubblewrap.core.treeview.TreeNode) 
   * this.disconnectChild(child)}</li>
   * <li>Finally remove the child Node from this.children.</li>
   * </ul>
   * @param child the child node to delete
   */
  protected final void onDeleteAsChild() {
    try {   
      this.beginLoading();
      this.unSelectNode();
      if (this.hasChildren()) {
        try {
          this.onDeleteChildren();
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.onDeleteAsChild.onDeleteChildren Error:\n {1}",
                  new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        }
      }
      try {
        this.beforeDeleteNode();
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.onDeleteAsChild.beforeDeleteNode Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
      try {
        this.disconnectAsChild();
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.onDeleteAsChild.disconnectChild Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.onDeleteAsChild Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {      
      this.endLoading();
    }
  }
  
  /**
   * Called to delete all the Node's children without further verification that the
   * children can be deleted. It calls the {@linkplain #onDeleteChild(
   * bubblewrap.core.treeview.TreeNode) this.onDeleteChild} method for each child.
   */
  protected final void onDeleteChildren() {
    if ((this.children == null) || (this.children.isEmpty())) {
      return;
    }
    try {
      this.beginLoading();
      for (int iChild = this.children.size()-1; iChild >= 0; iChild--) {
        TChild child = this.children.get(iChild);
        try {
          child.onDeleteAsChild();
        } finally {
          this.children.remove(iChild);
        }
      }
    } finally {
      this.endLoading();
    }
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Protected Virtual/Overriddable Methods">
  /**
   * <p>CAN OVERRIDE: Call by {@linkplain #resetState()} to reset any custom state
   * properties. The base method does nothing.</p>
   */
  protected void onResetState(){}

  /**
   * <p>CAN OVERRIDE: This method is called by {@linkplain #canSelectNode()
   * this.canSelectNode} to check to get the custom handling of the request. It is only
   * called if this node is not the selected node. The base method returns true. </p>
   * @return true if the node can be selected
   * @throws Exception - will be caught and logged.
   */
  protected boolean onCanSelectNode() throws Exception {
    return true;
  }
  
  /**
   * <p>CAN OVERRIDE: This method is called by {@linkplain #canUnSelectNode()
   * this.canUnSelectNode} to check get the custom handling of the request.
   * It is only called if this is the selected node. The base method returns true. </p>
   * @return true if the node can be unselected
   * @throws Exception - will be caught and logged.
   */
  protected boolean onCanUnSelectNode() throws Exception {
    return true;    
  }
  
  /**
   * <p>CAN OVERRIDE: This method is called from {@linkplain #beforeUnSelectNode() 
   * this.beforeUnSelectNode}. Inheritor can override this method, to reset 
   * resources, unlock content, etc. when the node become unselected.
   * <p>The base method does nothing.
   * @throws Exception on errors  - will be caught and logged.
   */
  protected void onBeforeUnSelectNode() throws Exception {}
   
  /**
   * <p>CAN OVERRIDE: This method is called by {@linkplain #canUnSelectNode()
   * this.canUnSelectNode} to check get the custom handling of the request.
   * It is only called if this is the selected node. The base method returns true. </p>
   * @return true if the node can be unselected
   * @throws Exception - will be caught and logged.
   */
  protected boolean onCanRemoveNode() throws Exception {
    return true;    
  }
   
  /**
   * <p>CAN OVERRIDE: This method is called by {@linkplain #canUnSelectNode()
   * this.canUnSelectNode} to check get the custom handling of the request.
   * It is only called if this is the selected node. The base method returns true.</p> 
   * @return true if the node can be unselected
   * @throws Exception - will be caught and logged.
   */
  protected boolean onCanDeleteNode() throws Exception {
    return true;    
  }
    
  /**
   * CAN OVERRIDE: 
   * Called by {@linkplain #onRemoveChild(bubblewrap.core.treeview.TreeNode) 
   * this.onRemoveChild} after the child's children has been removed and before the 
   * child is disconnected from this parent and the treeView. 
   * The base method does nothing.
   * <p>
   * <b>NOTE:</b> All exception throw by this process is trapped logged, and ignored.
   */
  protected void beforeRemoveNode(){}
  
  
  /**
   * CAN OVERRIDE: 
   * Called by {@linkplain #onDeleteChild(bubblewrap.core.treeview.TreeNode) 
   * this.onDeleteChild} after the child's children has been deleted and before the 
   * child is disconnected from this parent and the treeView. 
   * The base method does nothing.
   * <p><b>NOTE:</b> All exception throw by this process is trapped logged, and ignored.
   */
  protected void beforeDeleteNode(){}
  
  /**
   * <p>CAN OVERRIDE: This method id called from {@linkplain #assignTo(
   * bubblewrap.core.treeview.TreeNode) this.assignTo(target)} to handle the custom transfer 
   * of field settings and other content (not the node's children or the core TreeNode
   * properties assigned during {@linkplain #clone() cloning}) from this instance to the
   * target</p>
   * <p><b>NOTE:</b> This is called after the target is added to its parent and its 
   * TreeView.</p>
   * @param target the target TreeNode to update.
   * @throws Exception if an error occurred. This will trigger a rollback of the assignTo
   * process.
   */
  protected void onAssignTo(TreeNode<TChild,TView> target) throws Exception {}
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="TreeView Display Properties">
  /**
   * Get the TreeNode's Unique ID - automatically assigned
   * @return the assigned 32-character value
   */
  public String getNodeId() {
    return nodeId;
  }
  
  /**
   * Called from the node's constructor to generate a unique ID - calling {@linkplain 
   * DataEntry#newUniqueId(java.lang.String, java.lang.String) 
   * DataEntry.newUniqueId(baseString, salt)}. This ID will replace the default
   * nodeId - which is generated calling {@linkplain DataEntry#newUniqueID()} without
   * a baseString or salt. The latter is unique ID for this instance of the Node, but not 
   * unique for the node in the treeView (if the node represent specific content).
   * <p>
   * <b>NOTE:</b> The call is ignored if either the <tt>baseString</tt> or <tt>salt</tt>
   * = null|"".</p>
   * @param baseString for using in calculating the unique ID (nor null|"")
   * @param salt for using in calculating the unique ID (nor null|"")
   */
  protected final void initNodeId(String baseString, String salt) {
    if (((baseString = DataEntry.cleanString(baseString)) != null) &&
        ((salt = DataEntry.cleanString(salt)) != null)) {
      this.nodeId = DataEntry.newUniqueId(baseString, salt);      
    }
  }
  
  /**
   * Called from the node's constructor to assign a custom unique ID. This ID will 
   * replace the defaultnodeId - which is generated calling {@linkplain 
   * DataEntry#newUniqueID()} without a baseString or salt. The latter is unique ID 
   * for this instance of the Node, but not unique for the node in the treeView 
   * (if the node represent specific content).
   * <p>
   * <b>NOTE:</b> The call is ignored if <tt>nodeId</tt> == null|"" or its length != 32.
   * @param nodeId the new custom uniueId
   */
  protected final void setNodeId(String nodeId) {
    if (((nodeId = DataEntry.cleanString(nodeId)) != null) &&
            (nodeId.length() == 32) && (!DataEntry.isEq(this.nodeId, nodeId, true))) {
      this.nodeId = nodeId;
      this.firePropertyChanged("nodeId");
    }
  }

  /**
   * Get the TreeNode's {@link TreeNodeTypeDef}@return 
   */
  public TreeNodeType getNodeType() {
    if (this.nodeType == null) {
      TreeNodeTypeDef annot = this.getClass().getAnnotation(TreeNodeTypeDef.class);
      this.nodeType = ((annot == null) || (annot.nodeType() == null))?
                        TreeNodeType.Document: annot.nodeType();
    }
    return this.nodeType;
  }
  
  /**
   * Set the TreeNode's {@link TreeNodeTypes}. This is typically  used to dynamically 
   * change the node's document type or to programmatically assign the nodeType instead
   * of using a {@linkplain TreeNodeType} annotation.
   * @param nodeType the new nodeType (ignored if null).
   */
  protected final void setNodeType(TreeNodeType nodeType) {
    if (nodeType != null) {
      this.nodeType = nodeType;
    }
  }
  
  /**
   * Get whether this node is selectable
   * @return ((this.nodeType.isSelectable) && (!this.getDisabled())) 
   */
  public boolean isSelectable() {
    TreeNodeType type = this.getNodeType();
    return ((type != null) && (type.isSelectable) && (!this.getDisabled()));
  }
  
  /**
   * Get the TreeNode's {@linkplain NodeExpandType}. 
   * @return return this.nodeType.expandType and if ({@linkplain NodeExpandType#Default}
   * and (!{@linkplain #hasChildren() this.hasChildren}) return {@linkplain 
   * NodeExpandType#Collapsed}.
   */
  public NodeExpandType getExpandType() {
    TreeNodeType nodType = this.getNodeType();
    NodeExpandType result = 
            (nodType == null)? NodeExpandType.Default: nodType.expandType;
    if (result == NodeExpandType.Default) {
      if (!this.hasChildren()) {
        result = NodeExpandType.Collapsed;
      }
    }
    return result;
  }

  /**
   * Get a reference to the TreeView to which this TreeNode belong
   * @return the assigned reference - null if not yet added to the TreeView
   */
  public TView getTreeView() {
    if ((this.parent !=null) && (this.treeView == null)) {
      this.treeView = this.parent.getTreeView();
    }
    return this.treeView;
  }

  /**
   * Get a reference to the TreeNode's Parent
   * @return the reference of null if this is the Root Node.
   */
  public TreeNode<?,TView> getParent() {
    return this.parent;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Other TreeNode properties">
  /**
   * Get the TreeNode's Tag 
   * @return the assigned value 
   */
  public String getNodeTag() {
    return nodeTag;
  }

  /**
   * Assign a TreeNode Tag - for referencing purpose
   * @param tag the new value (can be null)
   */
  public void setNodeTag(String tag) {
    this.nodeTag = tag;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="TreeNode display properties">
  /**
   * Get the TreeNode's Caption - for rendering
   * @return the current value
   */
  public String getCaption() {
    return this.caption;
  }
  
  /**
   * Set the TreeNode's Caption - for rendering
   * @param caption the new value
   * @exception NullPointerException if value is null|""
   */
  public void setCaption(String caption) {
    caption = DataEntry.cleanString(caption);
    if (caption == null) {
      throw new NullPointerException("The TreeNode's Caption cannot be unassigned.");
    }
    if (!DataEntry.isEq(this.caption, caption, true)) {
      this.caption = caption;
      this.firePropertyChanged("caption");
    }
  }
  
  /**
   * Get the HTML Elements "title" property - for rendering
   * @return the assigned value or ""
   */
  public String getTitle() {
    return (this.title == null)? "": this.title;
  }
  
  /**
   * the HTML Elements "title" property - for rendering
   * @param title the new title
   */
  public void setTitle(String title) {
    title = DataEntry.cleanString(title);
    if (!DataEntry.isEq(this.title, title, true)) {
      this.title = title;
      this.firePropertyChanged("title");
    }
  }
  
  /**
   * Get the TreeNode's "nodeStatus" property - for rendering
   * @return the assigned value or null
   */
  public String getNodeStatus() {
    return this.nodeStatus;
  }
  
  /**
   * the HTML Elements "title" property - for rendering
   * @param title the new title
   */
  public void setNodeStatus(String nodeStatus) {
    nodeStatus = DataEntry.cleanString(nodeStatus);
    if (!DataEntry.isEq(this.nodeStatus, nodeStatus, true)) {
      this.nodeStatus = nodeStatus;
      this.firePropertyChanged("nodeStatus");
    }
  }
  
  /**
   * Get whether the TreeNode's "nodeStatus" should be displayed in the TreeView
   * @return true to show the Node's Status in the TreeView
   */
  public Boolean doShowStatus() {
    return ((this.showStatus != null) && (this.showStatus));
  }
  
  /**
   * Set whether the TreeNode's "nodeStatus" should be displayed in the TreeView
   * @param showStatus true to show the Node's Status in the TreeView
   */
  public void setShowStatus(Boolean showStatus) {
    this.showStatus = ((showStatus == null) || (!showStatus))? null: showStatus;
  }
  
  /**
   * Get the TreeNode's Display Index
   * @return the assigned value (not null)
   */
  public Integer getDisplayIdx() {
    return (this.displayIdx == null)? 0: this.displayIdx;
  }
  
  /**
   * Set the TreeNode's Display Index
   * @param displayIdx the new index - set to zero if null.
   */
  public void setDisplayIdx(Integer displayIdx) {
    displayIdx = (displayIdx == null)? 0: displayIdx;
    if (!DataEntry.isEq(this.displayIdx, displayIdx)) {
      this.displayIdx = displayIdx;
      this.firePropertyChanged("displayIdx");
    }
  }
  
  /**
   * Get whether to show the formMask on when the node is select and while the node's
   * content is loading.
   * @return the assigned value (default = false)
   */
  public Boolean doShowMaskOnSelect() {
    return ((this.showMaskOnSelect != null) && (this.showMaskOnSelect));
  }
  
  /**
   * Set whether to show the formMask on when the node is select and while the node's
   * content is loading.
   * @param showMaskOnSelect the new setting
   */
  public void setShowMaskOnSelect(Boolean showMaskOnSelect) {
    this.showMaskOnSelect = ((showMaskOnSelect == null) || (!showMaskOnSelect))? null:
             showMaskOnSelect;
  }
  
  /**
   * Get the FormMask ElementId as assigned to {@linkplain 
   * TreeView#getFormMaskElementId() this.treeView.formMaskElementId}
   * @return this.treeView.formMaskElementId or if unassigned or this.treeView = null.
   */
  public String getFormMaskElementId() {
    String result = null;
    TreeView ownerTv = this.getTreeView();
    if (ownerTv != null) {
      result = ownerTv.getFormMaskElementId();
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Handler TreeNode State">
  /**
   * Get whether this is the TreeView's RootNode and it is still attached to the tree.
   * @return true if ndoeType=TreeRoot and (this.treeView != null)
   */
  public boolean isRoot() {
    return ((TreeNodeType.TreeRoot.equals(this.getNodeType())) && 
                                                            (this.treeView != null));
  }
  
  /**
   * Get whether this Node (or one of it parent nodes are disconnected from the TreeView)
   * @return true if this.getTreeView() = null.
   */
  public boolean isDisconnected() {
    return (this.getTreeView() == null);
  }
  
  /**
   * Get whether this TreeNode is in the parent-path of the treeView's isSelected node.
   * @return true if the treeView has a selectNode and this is in its parent-path
   */
  public boolean inPath() {
    TreeNode<?,TView> selected = 
                      (this.treeView == null)? null: this.treeView.getSelectedNode();
    return (selected == null)? null: this.inPath(selected);
  }

  /**
   * Get whether this TreeNode is in the parent-path of the specified node.
   * @param other the TreeNode to access 
   * @return true if this is in the parent-path
   */
  public boolean inPath(TreeNode<?,TView> other) {
    boolean result = (other != null);
    TreeNode<?,TView> upNode = other.parent;
    while (upNode != null) {
      if (this.equals(upNode)) {
        result = true;
        break;
      }
      upNode = upNode.parent;
    }
    return result;
  }
  
  /**
   * Called to reset the TreeNode's state to its default values
   */
  public final void resetState() {
    this.visible = null;
    this.disabled = null;
    this.expanded = null;
    this.loadingCount = 0;
    this.onResetState();
  }  
  /**
   * Get the TreeNode's Visible State (default=null|true)
   * @return the assigned state or true
   */
  public Boolean getVisible() {
    return ((this.visible == null) || (this.visible));
  }

  /**
   * Set the TreeNode's Visible state
   * @param visible true to display TreeNode
   */
  public void setVisible(Boolean visible) {
    this.visible = visible;
  }

  /**
   * Get the TreeNode's Enabled(false)/Disabled(true) state (default=null|false).
   * A Root TreeNode is never disabled
   * @return the assigned state or false
   */
  public Boolean getDisabled() {
    return (TreeNodeType.TreeRoot.equals(this.getNodeType()))? false:
            ((this.disabled != null) && (this.disabled));
  }

  /**
   * Set the TreeNode's Enabled(false)/Disabled(true) state.
   * Note: A Root TreeNode cannot be disabled.
   * @param disabled true to disable the TreeNode
   */
  public void setDisabled(Boolean disabled) {
    if (!TreeNodeType.TreeRoot.equals(this.getNodeType())) {
      disabled = ((disabled == null) || (!disabled))? null: disabled;
      if (!DataEntry.isEq(this.disabled, disabled)) {
        this.disabled = disabled;
        this.firePropertyChanged("disabled");
      }
    }
  }

  /**
   * Get the Items Expanded(true)/Collapsed(false) state (default=null|false)
   * @return the assigned state or false
   */
  public Boolean isExpanded() {
    Boolean result = ((this.expanded != null) && (this.expanded));
    if (NodeExpandType.Expanded.equals(this.nodeType.expandType)) {
      result = true;
    } else if (NodeExpandType.Collapsed.equals(this.nodeType.expandType)) {
      result = false;
    }
    return result;
  }

  /**
   * Set the TreeNode's Expanded(true)/Collapsed(false) state. 
   * Ignored if this.nodeType = Document, or this.hasChidlren=false. if expanded=false
   * and this.allowChildren, call the each child and set its expanded state=false.
   * @param expanded true to expand the TreeNode
   */
  public void setExpanded(Boolean expanded) {
    expanded = ((expanded != null) && (expanded));
    if ((expanded) && (this.parent != null) && (!this.parent.isExpanded())) {
      this.parent.setExpanded(expanded);
    }
    
    if ((!NodeExpandType.Default.equals(this.nodeType.expandType)) || 
            (!this.hasChildren())) {
      return;
    }
    this.expanded = expanded;
    if (!this.expanded) {
      for (TChild child : this) {
        child.setExpanded(expanded);
      }
    }
  }
  
  /**
   * Get the Node Selected state (i.e., whether this is the the treeView's selectNode)
   * @return true if this is the treeView's selectNode.
   */
  @SuppressWarnings("unchecked")
  public Boolean isSelected() {
    TreeView<?, TView> ownerView = null;
    return (((ownerView = this.getTreeView()) != null) && 
            (ownerView.isSelectedNode((TChild) this)));
  }
  
  /**
   * Set the TreeNode's ESelected state. It return unchanged is {@linkplain 
   * #canSelectNode() this.canSelectNode} return false or after firing {@linkplain 
   * #SelectionChanging this.SelectionChanging} event, the CancelEventInfo returned
   * canceled. if successfully changed, it fires {@linkplain #SelectionChanged 
   * this.SelectionChanged} event.
   * @param isSelected true to select the TreeNode
   */
  @SuppressWarnings("unchecked")
  public void selectNode() {
    TreeView<?, TView> ownerView = null;
    if (((ownerView = this.getTreeView()) == null) || (this.isSelected()) || 
            (!this.canSelectNode())) {
      return;
    }
    
    CancelEventArgs args = new CancelEventArgs();
    this.fireSelectionChanging(args);
    if (args.isCanceled()) {
      return;
    }
    ownerView.setAsSelectedNode((TChild) this);
    if (this.isSelected()) {
      this.setExpanded(true);
    }
    
    this.fireSelectionChanged(new EventArgs());
  }
  
  /**
   * Called internally to unselect this node if it is the Owner TreeView's selected node.
   */
  protected final void unSelectNode() {
    TreeView<?, TView> ownerView = null;
    if (((ownerView = this.getTreeView()) != null) &&
            (this.isSelected())) {
      TreeNode<?, TView> node = null;
      ownerView.setSelectedNode(node);
    }
  }
      
//  /**
//   * Called internally to unselect this node if it is the Owner TreeView's selected node.
//   */
//  protected final void unSelectNode() {
//    TreeView<?, TView> ownerView = null;
//    if (((ownerView = this.getTreeView()) != null) &&
//            (this.isSelected())) {
//      ownerView.clearSelectedNode();
//    }
//  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Manage Child TreeNode">
  /**
   * Get the Number of children of this TreeNode
   * @return the size of this.children
   */
  public int getChildCount() {
    this.loadChildren();
    return this.children.size();
  }
  
  /**
   * Get whether this TreeNode has children
   * @return true if this.children is not empty
   */
  public boolean hasChildren() {
    this.loadChildren();
    return (!this.children.isEmpty());
  }
  
  /**
   * Called to check if this TreeNode contains this child - the direct child list. It
   * does not search sub Children.
   * @param other TreeNode
   * @return true if the other child exist as a child.
   */
  public boolean containChild(TChild other) {
    boolean result = this.hasChildren();
    if (result) {
      result = children.contains(other);
    }
    return result;
  }
  
  /**
   * Get a Iterator on this TreeNode's children
   * @return an non-null Iterator
   */
  @Override
  public Iterator<TChild> iterator() {
    this.loadChildren();
    return this.children.iterator();
  }
  
  /**
   * Called to unload all children. if not empty, it calls {@linkplain 
   * #disconnectChild() diconnectChild(child)} for each child
   * before clearing the list. It always set this.isUpdated = false;
   */
  public void clearChildren() {
    if (!this.children.isEmpty()) {
      try {
        this.beginLoading();
        for (TChild child : this.children) {
          child.disconnectAsChild();
        }
        this.children.clear();
      } finally {
        this.updated = null;
        this.endLoading();
      }
    } else {
      this.updated = null;
    }
  }
  
  /**
   * Called to addChild a new Child TreeNode to this TreeNode. The chidlNode's class
   * must be designated a nodetType[Container|Document]. Adding nodetType=TreeRoot 
   * using this method will fail.
   * <p>It calls begin- and endLoading when adding the childNode
   * @param childNode the new item. Ignored if item = null or the item already exist
   * @return true if a new TreeNode was added.
   * @throws IllegalAccessException if this TreeNodeTypeDef=Document - does not support 
 children, or the child is already added to a treeView, or the child.nodeType=TreeRoot.
   */
  @SuppressWarnings("unchecked")
  public boolean addChild(TChild childNode) {
    boolean result = false;
    if (childNode.getParent() != null) {
      throw new IllegalArgumentException("This TreeNode already has an assigned "
              + "Parent Node. It cannot added to another Parent Node or re-added to this "
              + "Parent Node.");
    }
    
    if (DataEntry.cleanString(childNode.getCaption()) == null) {
      throw new IllegalArgumentException("The Child Node's caption is not assigned.");
    }
    
    if ((childNode != null) && (!this.children.contains(childNode))) {
      try {
        this.beginLoading();
        result = this.children.add(childNode);
        if (result) {
          if (this.children.size() > 1) {
            Collections.sort(this.children);
          }
          childNode.setParentNode(this);
        }
      } finally {
        this.endLoading();
      }
    }
    return result;
  }
  
  /**
   * Called to retrieve a child by Index. 
   * @param index the index of the child node to retrieve.
   * @return the child or null if the chidlCount=0 or the index is out of bounds
   */
  public final TChild getChild(int index) {
    TChild result = null;
    if ((this.hasChildren()) && (index >= 0) && (index < this.children.size())) {
      result = this.children.get(index);
    }
    return result;
  }
  
  /**
   * Called to retrieve index of <tt>child</tt> in this.children. 
   * @param child the child node to search for.
   * @return the list index or -1 if not found.
   */
  public int getChildIndex(TChild child) {
    int result = -1;
    if ((child != null) && (this.hasChildren())) {
      result = this.children.indexOf(child);
    }
    return result;
  }
    
  /**
   * <p>Overload 1: Calls to removeChild a child with the specified nodeId. It calls
   * {@linkplain #indexOfChild(java.lang.String) indexOfChild(nodeid)} to locate the
   * child's index and then calls the {@linkplain #removeChild(int) Overload 2} to
   * remove the Child at the specified index.</p>
   * @param nodeId the NodeId of the child to be removed
   * @return true if the child was found and successfully removed.
   */
  public final boolean removeChild(String nodeId) {
    return this.removeChild(this.indexOfChild(nodeId));
  }
  
  /**
   * <p>Overload 2: Called to removeChild a child at a specified index in Child List. 
   * It calls {@linkplain #getChild(int) this.getChild(index)} and if not null, it
   * calls {@linkplain #removeChild(bubblewrap.core.treeview.TreeNode) Overload 3} 
   * to remove the child.</p>
   * @param index the index of the child to removeChild
   * @return true if successful; false if the child could not be located.
   */
  public final boolean removeChild(int index) {
    TChild child = (index < 0)? null: this.getChild(index);
    return this.removeChild(child);
  }
    
  /**
   * <p>Overload 3: Calls to remove the specified child node form this node's children.
   * It checks whether the child is assigned, is contained in this.children and whether
   * the child can be removed by calling its {@linkplain #onCanRemoveNode() 
   * onCanRemoveNode} method. If this check returns true, it calls {@linkplain 
   * #onRemoveChild(bubblewrap.core.treeview.TreeNode) this.onRemoveChild(child)} to 
   * remove the child.</p>
   * @param child the TreeNode child to be removed
   * @return true if the child was found and successfully removed.
   */
  public boolean removeChild(TChild child) {
    boolean result = ((child != null) && (!this.hasChildren()) &&
                      (this.children.contains(child)) &&
                      (!child.canRemoveNode()));
    if (result) {
      try {
        this.beginLoading();
        child.onRemoveAsChild();
        this.children.remove(child);
      } finally {
        this.endLoading();
      }
    }
    return result;
  }
  
  /**
   * Called to remove all the node's children. If this.allowChildren and before
   * removing the children and it first calls {@linkplain #canRemoveNode() 
   * child.canRemoveNode} for each child node to verify that the child and all its 
   * children can be removed. Only of all children can be removes, it will call 
   * {@linkplain #onRemoveChildren() this.onRemoveChildren} to remove the children.
   * @return true if all the children is removed or false if permission to remove the
   * children was denied.
   */
  public final boolean removeChildren() {
    boolean result = true;     
    try {
      this.beginLoading();
      if (this.hasChildren()) {
        for (TChild child : this) {
          if (!child.canRemoveNode()) {
            result = false;
            break;
          }
        }
        if (result) {
          this.onRemoveChildren();
        }
      }
    } finally {
      this.endLoading();
    }
    return result;
  }
    
  /**
   * <p>Overload 1: Calls to delete a child with the specified nodeId. It calls
   * {@linkplain #indexOfChild(java.lang.String) indexOfChild(nodeid)} to locate the
   * child's index and then calls the {@linkplain #deleteChild(int) Overload 2} to
   * delete the Child at the specified index.</p>
   * @param nodeId the NodeId of the child to be deleted
   * @return true if the child was found and successfully deleted.
   */
  public final boolean deleteChild(String nodeId) {
    return this.deleteChild(this.indexOfChild(nodeId));
  }
  
  /**
   * <p>Overload 2: Called to delete a child at a specified index in this.children. 
   * It calls {@linkplain #getChild(int) this.getChild(index)} and if not null, it
   * calls {@linkplain #deleteChild(bubblewrap.core.treeview.TreeNode) Overload 3} 
   * to delete the child.</p>
   * @param index the index of the child to be deleted
   * @return true if successful; false if the child could not be located.
   */
  public boolean deleteChild(int index) {
    TChild child = (index < 0)? null: this.getChild(index);
    return this.deleteChild(child);
  }
    
  /**
   * <p>Overload 3: Calls to delete the specified child node form this node's children.
   * It checks whether the child is assigned, is contained in this.children and whether
   * the child can be removed by calling its {@linkplain #canDeleteNode() 
   * child.canDeleteNode} method. If this check returns true, it calls {@linkplain 
   * #onDeleteChild(bubblewrap.core.treeview.TreeNode) this.onDeleteChild(child)} to 
   * delete the child.</p>
   * @param child the TreeNode child to be deleted
   * @return true if the child was found and successfully deleted.
   */
  public boolean deleteChild(TChild child) {
    boolean result = ((child != null) && (!this.hasChildren()) &&
                      (this.children.contains(child)) &&
                      (!child.canDeleteNode()));
    if (result) {
      try {
        this.beginLoading();
        child.onDeleteAsChild();
      } finally {
        this.endLoading();
      }
    }
    return result;
  }
  
  /**
   * Called to delete all the node's children. If this.allowChildren and before deleting
 the children and it first calls {@linkplain #canDeleteNode() child.canDeleteNode} 
   * for each child node to verify the the child and all its children can be deleted. 
   * Only if all children can be deleted, it calls {@linkplain #onDeleteChildren() 
   * this.onDeleteChildren} to delete the children.
   * @return true if all the children are deleted or false if permission to delete one 
   * of the children was denied.
   */
  public boolean deleteChildren() {
    boolean result = true;  
    try {
      this.beginLoading();
      if (this.hasChildren()) {
        for (TChild child : this) {
          if (!child.canDeleteNode()) {
            result = false;
            break;
          }
        }
        if (result) {
          this.onDeleteChildren();
          this.updated = null;
        }
      }
    } finally {
      this.endLoading();
    }
    return result;
  }  
  
  /**
   * Get the Index of the child TreeNode by Instance or NodeId
   * @param other a TreeNode instance or a NodeId (type[32Char-String])
   * @return the index of other in the child list or -1 if not found.
   */
  public int indexOfChild(TChild other) {
    int result = -1;
    if ((other != null) && (this.hasChildren())) {
      result = this.children.indexOf(other);
    }
    return result;
  }
  
  /**
   * Get the Index of the child TreeNode by Instance or NodeId
   * @param other a TreeNode instance or a NodeId (type[32Char-String])
   * @return the index of other in the child list or -1 if not found.
   */
  public int indexOfChild(String nodeId) {
    int result = -1;
    if ((nodeId != null) && (this.hasChildren())) {
      result = this.children.indexOf(nodeId);
    }
    return result;
  }
  
  /**
   * Called to locate a child by its nodeId
   * @param nodeId
   * @return 
   */
  public TChild findChild(String nodeId) {
    TChild result = null;
    if ((nodeId != null) && (this.hasChildren())) {
      for (TChild child : this.children) {
        if (nodeId.equalsIgnoreCase(child.getNodeId())) {
          result = child;
          break;
        }
      }
    }
    return result;
  }  
  
  /**
   * Called to find a child node in the tree below this node by its nodeId
   * @param nodeId the ID of Node to search for.
   * @return the node if found or else null.
   */
  public TreeNode<?, TView> findNodeById(String nodeId) {
    TreeNode<?, TView> result = null;
    if ((nodeId != null) && (this.hasChildren())) {
      for (TChild child : this.children) {
        if (nodeId.equalsIgnoreCase(child.getNodeId())) {
          result = child;
          break;
        } else if ((result = child.findNodeById(nodeId)) != null) {
          break;
        }
      }
    }
    return result;
  }    
  
  /**
   * Called to find a child node in the tree below this node by its nodeId
   * @param nodeTag the NodeTag of Node to search for.
   * @return the node if found or else null.
   */
  public TreeNode<?, TView> findNodeByTag(String nodeTag) {
    TreeNode<?, TView> result = null;
    if ((nodeTag != null) && (this.hasChildren())) {
      for (TChild child : this.children) {
        if (nodeTag.equalsIgnoreCase(child.getNodeTag())) {
          result = child;
          break;
        } else if ((result = child.findNodeByTag(nodeTag)) != null) {
          break;
        }
      }
    }
    return result;
  }
  
  /**
   * <p>CAN OVERRIDE: This method is called - typically after {@linkplain #clone() 
   * this.cloning} this node and after adding the the clone its parent - to assign the 
   * remainder of this instance settings as well as this.children to the target (clone).
   * </p>
   * <p>Inheritors should first transfer all the additional settings to the target before
   * calling the super method. This base method only perpetuate this cloning process be
   * cloning each child node, adding it to the target and calling the childNode's 
   * assignTo.
   * </p>
   * <p><b>NOTE:</b> This process is skipped if a) the target is unassigned; b) the 
   * target's class does not match this instance's class, and c) if this.children = 
   * null|empty.</p>
   * @param target the target (clone) node to update
   * @throws Exception if the transferring of settings/field value, the cloning of any 
   * children or the assignTo method of the any child failed or throw an exception.
   */
  @SuppressWarnings("unchecked")
  public final void assignTo(TreeNode<TChild,TView> target) throws Exception {
    if ((target == null) || (!this.getClass().equals(target.getClass()))) {
      throw new Exception("The Target Node to update is unassigned or does not "
              + "macth this node's class.");
    }

    if ((target.getParent() == null) || (target.getTreeView() == null)) {
      throw new Exception("The target Node[" + target.caption + "] is disconnected "
              + "from its parent Node and/or the owner TreeView.");
    }
      
    try {
      /* 
       * Call onAssignTo to transfer annd additional field settings and content before
       * adding this.children
       */
      this.onAssignTo(target);
      
      /**
       * If this.hasChildren - clone each child, add the clone to the target and call
       * child.assignTo(clone)
       */
      if ((this.children != null) && (!this.children.isEmpty())) {
        for (TChild child : this.children) {
          TChild clone = (TChild) child.clone();        
          target.addChild(clone);

          TreeNode childTarget = (TreeNode) clone;
          child.assignTo(childTarget);
        }
      }
    } catch (Exception exp) {
      this.rollBackAssignTo(target);
      throw exp;
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="For XML Serialization">
  /**
   * <p>MUST OVERRIDE: For XML Serialization. Inheritors must override this method with
   * a public exposure and with a {@linkplain XmlElement @XmlElement(name="ChildNode",
   * type = ChildClass.class, (nillable=true))} annotation</p>
   * @return a list of this.children
   */
  @XmlElement(name = "TreeNode")
  protected List<TChild> getChildren() {
    return this.children;
  }
  
// Not needed For XML deserialization - content is directly deserialized to this.children.
//  /**
//   * For XML deserialization. It clears the current list of child Nodes and if
//   * <tt>children</tt> != null|empty, call {@linkplain #addChild(
//   * bubblewrap.core.treeview.TreeNode) this.addChild(child)} to add each child to the list.
//   * Errors are trapped and logged.
//   * @param children a list of deserialized Child Nodes.
//   */
//  protected void setChildren(List<TChild> children) {
//    this.children.clear();
//    try {
//      if ((children != null && (!children.isEmpty()))) {
//        for (TChild child : children) {
//          this.addChild(child);
//        }
//      }
//    } catch (Exception exp) {
//      logger.log(Level.WARNING, "{0}.setChildren Error:\n {1}",
//              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
//    }
//  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Override Object">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: Check if obj matches this nodeId. Other can be of type TreeNode or 
   * String (type of nodeId)
   * </p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    boolean result = (obj != null);
    if (result) {
      if (obj instanceof TreeNode) {
        TreeNode other = (TreeNode) obj;
        result = this.nodeId.equals(other.getNodeId());
      } else if (obj instanceof String) {
        String nodId = (String) obj;
        result = this.nodeId.equals(nodId);
      }
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: Return a HashCode on this.nodeId</p>
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + Objects.hashCode(this.nodeId);
    return hash;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: It created a clone for the node's class (required a parameterless
   * constructor and transfer the node's core fields settings, including: caption,
   * displayIdx, nodeTag, title, visible, and disabled.</p>
   * <p>
   * <b>NOTE:</b> Once clones, the clone should be added to its parent and {@linkplain 
   * #assignTo(bubblewrap.core.treeview.TreeNode) this.assignTo(clone)} should be called to
   * transfer additional content and for adding this.children to the clone.</p>
   */
  @Override
  public final TreeNode<TChild,TView> clone() throws CloneNotSupportedException {
    TreeNode<TChild,TView> result = null;
    try {
      try {
        Class<TreeNode<TChild,TView>> thisClass =
                ReflectionInfo.castAsSpecificGenericClass(this.getClass());
        result = thisClass.newInstance();
        if (result == null) {
          throw new Exception("Initiating the new instance using parameterless "
                  + "constructor failed.");
        }
      } catch (Exception exp) {
        if ((exp == null) || (exp.getMessage() == null)) {
          throw new Exception("Initiating the new instance failed. Reason unknown.");
        }
        throw exp;
      }
      
      result.caption = this.caption;
      result.displayIdx = this.displayIdx;
      result.nodeTag = this.nodeTag;
      result.title = this.title;
      result.visible = this.visible;
      result.disabled = this.disabled; 
    } catch (Exception exp) {
      throw new CloneNotSupportedException(exp.getMessage());
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: Return className[this.caption].</p>
   */
  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[" + this.caption + "]";
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implement Comparable">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: Compare this instance to the other TreeNode based in there display
   * indices. Return 1 if (other = null)</p>
   */
  @Override
  public int compareTo(TreeNode other) {
    int result = 1;
    if (other != null) {
      Integer otherIdx = other.getDisplayIdx();
      result = this.displayIdx.compareTo(otherIdx);
    }
    return result;
  }
  //</editor-fold>
}
