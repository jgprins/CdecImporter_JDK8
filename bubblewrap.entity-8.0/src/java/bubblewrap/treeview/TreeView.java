package bubblewrap.treeview;

import bubblewrap.admin.context.BwPrincipal;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import bubblewrap.core.events.EventHandler;
import bubblewrap.core.events.EventArgs;
import bubblewrap.io.DataEntry;

/**
 * The TreeView class contains a single rootNode (type {@linkplain TreeNode}) that 
 * contains treeView's TreeNodes. It has a single selected TreeNode - the node that has 
 * the current focus.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class TreeView<TRoot extends TreeNode<?,TView>,
                               TView extends TreeView<TRoot,TView>> 
                               implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">

  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(TreeView.class.getName());
  //</editor-fold>        
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The TreeView' selected node
   */
  @XmlTransient
  private TreeNode<?, TView> selectedNode;
  /**
   * The TreeView's RootNode
   */
  private TRoot rootNode;
  /**
   * The flag indicating whether this TreeView allow multiple selectable nodes
   * (NOTE: currently only support single selection)
   */
  private Boolean multiSelectable;
  /**
   * A Transient counter for managing this.isSelecting state
   */
  private transient int selectingCount = 0;
  /**
   * The name of the FormMask element - assigned at runtime from the {@linkplain 
   * TreeViewPanel}
   */
  private String formMaskElementId;
  /**
   * A flag set after the TreeView's content has change to indicate to the 
   * {@linkplain TreeViewPanel} that it children should be reset
   */
  private Boolean resetPanel;
  //</editor-fold>  
  
  //<editor-fold defaultstate="collapsed" desc="EventHandlers">
  /**
   * The EventHandler that fires the SelectedItem Changed Event.
   */
  @XmlTransient
  public final EventHandler SelectedNodeChanged;
  
  /**
   * Called to fire the SelectedItem Changed Event. The call is ignored if 
   * (this.selectingCount &le; 0) 
   */
  protected void fireSelectedNodeChanged() {
    if (this.selectingCount <= 0) {
      this.SelectedNodeChanged.fireEvent(this, new EventArgs());
    }
  }
  
  /**
   * EventHandler for sending a Content Changed event.
   */
  public final EventHandler ContentChanged;
  
  /**
   * Method called to fie the Content Changed event.
   */
  protected void fireContentChanged() {
    this.ContentChanged.fireEvent(this, new EventArgs());
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public TreeView() {
    super();    
    this.SelectedNodeChanged = new EventHandler();
    this.ContentChanged = new EventHandler();
    this.rootNode = null;
    this.selectedNode = null;
    this.multiSelectable = null;
    this.resetPanel = null;
    this.formMaskElementId = null;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.ContentChanged.clear();
    this.SelectedNodeChanged.clear();
    this.selectedNode = null;
    this.resetRootNode();
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="TreeView Display Properties">
  /**
   * Get whether this TreeView allow multiple selectable nodes
   * (NOTE: currently only support single selection)
   * @return true if multi-selectable (always return false).
   */
  public Boolean isMultiSelectable() {
    return ((this.multiSelectable != null) && (this.multiSelectable));
  }
  
  /**
   * Get the TreeView's assigned FormMask ElementId
   * @return the assigned value (default = null)
   */
  public final String getFormMaskElementId() {
    return this.formMaskElementId;
  }
  
  /**
   * Set the TreeView's associated FormMask ElementId
   * @param formMaskElementId the element Id (can be null).
   */
  public final void setFormMaskElementId(String formMaskElementId) {
    this.formMaskElementId = DataEntry.cleanString(formMaskElementId);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Managing this.isSelecting State">
  /**
   * Check the TreeView's IsSelecting state (i.e., set while the selectedNode is
   * changing to prevent cyclic calls).
   * @return true is this.selectingCount &gt; 0
   */
  public final boolean isSelecting() {
    return (this.selectingCount > 0);
  }
  
  /**
   * Called to BEFORE changing the selected node - increment this.selectingCount
   */
  public final void beginSelecting() {
    this.selectingCount = (this.selectingCount < 0)? 0: this.selectingCount;
    this.selectingCount++;
  }
  
  /**
   * Called to AFTER changing the selected node - decrement this.selectingCount.
   * if (this.selectingCount = 0) it fires {@link #SelectedNodeChanged} event
   */
  public final void endSelecting() {
    if (this.selectingCount > 0) {
      this.selectingCount--;
      if (this.selectingCount == 0) {
        this.fireSelectedNodeChanged();
      }
    }
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Manage the Selected Node">
  /**
   * Check if the TreeView has an selected node
   * @return true if the selected node is assigned.
   */
  public boolean hasSelectedNode() {
    return (this.selectedNode != null);
  }
  
  /**
   * Called by a Node to check if it is the TreeView's currently selected node.
   * @param node the calling node
   * @return true if <tt>node</tt> is the selected node.
   */
  public final boolean isSelectedNode(TreeNode<?,TView> node) {
    return ((this.selectedNode != null) && (this.selectedNode.equals(node)));
  }
  
  /**
   * Get a reference to the currently selected node.
   * @return the assigned node
   */
  @SuppressWarnings("unchecked")
  public final TreeNode<?, TView> getSelectedNode(){
    return this.selectedNode;
  }
  
  /**
   * Called externally to set a new selected node. If <tt>newNode</tt> = null, call
   * {@linkplain #clearSelectedNode() this.clearSelectedNode}, else if this is not
   * the newNode's TreeView, throw an IllegalArgumentException. Else, set the
   * newNode's {@linkplain TreeNode#setIsSelected(java.lang.Boolean) isSelected} = true.
   * @param newNode the new node.
   */
  public final void setSelectedNode(TreeNode<?, TView> newNode) {
    try {
      if (newNode == null) {
        this.clearSelectedNode();
      } else if (!this.equals(newNode.getTreeView())) {
        throw new Exception("The new Node does notbelong to this TreeView.");
      } else {
        newNode.selectNode();
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".setSelectedNode Error:\n " + exp.getMessage());
    }
  }
  
  /**
   * Called to find the Node by its NodeId. It locates the RootNode and call
   * its {@link TreeNode#findNodeById(java.lang.String) findNodeById} to locate the node
   * in the TreeView. 
   * @param nodeId the node to look for. ignored is null or the node is not found
   * @return the Node or null if not found.
   */
  public final TreeNode<?, TView> findNodeById(String nodeId) {
    TreeNode<?, TView> result = null;
    try {
      TRoot root = this.getRootNode();
      if ((root != null) && ((nodeId = DataEntry.cleanString(nodeId)) != null)) {
        result= root.findNodeById(nodeId);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setSelectedNode Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return this.selectedNode;
  }
  
  /**
   * Called to set the selected Node by its NodeId. It locates the RootNode and call
   * its {@link TreeNode#findNodeById(java.lang.String) findNodeById} to locate the node
   * in the TreeView. If found it set the node as the TreeView's Selected Node
   * @param nodeId the node to look for. ignored is null or the node is not found
   * @return the treeView's currently Selected Node.
   */
  public final TreeNode<?, TView> setSelectedNodeById(String nodeId) {
    try {
      TRoot root = this.getRootNode();
      if ((root != null) && ((nodeId = DataEntry.cleanString(nodeId)) != null)) {
        TreeNode<?, TView> node = root.findNodeById(nodeId);
        if ((node != null) && (node.isSelectable() && (!node.isSelected()))) {
          node.selectNode();
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setSelectedNode Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return this.selectedNode;
  }
  
  /**
   * Called to find the Node by its nodeTag. It locates the RootNode and call
   * its {@link TreeNode#findNodeByTag(java.lang.String) findNodeByTag} to locate the 
   * node in the TreeView. 
   * @param nodeTag the nodeTag to look for.
   * @return the Node or null if not found.
   */
  public final TreeNode<?, TView> findNodeByTag(String nodeTag) {
    TreeNode<?, TView> result = null;
    try {
      TRoot root = this.getRootNode();
      if ((root != null) && ((nodeTag = DataEntry.cleanString(nodeTag)) != null)) {
        result= root.findNodeByTag(nodeTag);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setSelectedNode Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return this.selectedNode;
  }
  
  /**
   * Called to set the selected Node by its NodeTag. It locates the RootNode and call
   * its {@link TreeNode#findNodeByTag(java.lang.String) findNodeByTag} to locate the 
   * node in the TreeView. If found it set the node as the TreeView's Selected Node.
   * @param nodeTag the node to look for. ignored is null or the node is not found
   * @return the treeView's currently Selected Node.
   */
  public final TreeNode<?, TView> setSelectedNodeByTag(String nodeTag) {
    try {
      TRoot root = this.getRootNode();
      if ((root != null) && ((nodeTag = DataEntry.cleanString(nodeTag)) != null)) {
        TreeNode<?, TView> node = root.findNodeByTag(nodeTag);
        if ((node != null) && (node.isSelectable() && (!node.isSelected()))) {
          node.selectNode();
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setSelectedNode Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return this.selectedNode;
  }
  
  /**
   * Called externally to clear the TreeView's current selection. If a selectItem is
   * assigned, to check if the node can be unselected, if if not return false without
   * clearing the current selected Item. 
   * <p>Else, it calls {@linkplain #setSelectedNode(bubblewrap.treeview.TreeNode) 
   * this.setSelectedNode(null)} to clear the selected node.. 
   * @return (this.selectedNode == null)
   */
  public final boolean clearSelectedNode() {
    boolean result = true;
    if (this.hasSelectedNode()) {
      if (!this.selectedNode.canUnSelectNode()) {
        result = false;
      } else {
        TreeNode<?, TView> priorNode = this.selectedNode;
        try {
          this.beginSelecting();
          priorNode.beforeUnSelectNode();
          this.selectedNode = null;
          this.onSelectedNodeChanged(priorNode, this.selectedNode);
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.clearSelectedNode Error:\n {1}",
                  new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        } finally {
          this.endSelecting();
        }
      }
    }
    return result;
  }
  
  /**
   * <p>
   * Check if the <tt>newNode</tt> can be come the selected node. Skipped if <tt>newNode
   * </tt> is the currently selected node.</p>
   * <p>If the current selectNode is assigned, call its {@linkplain
   * TreeNode#canUnSelectNode() canUnSelectNode} method to validate that the node can be
   * unselected. It current node can be unselected, call {@linkplain #onCanSelectNode(
   * myapp.core.treeview.TreeNode) this.onCanSelectNode} to check if the new node can
   * be selected.
   * </p>
   * @param newNode new node to select (can be unassigned).
   * @return true if the currently selectNode can be unselected and the new node can
 be selected..
   */
  protected final boolean canSelectNode(TreeNode<?, TView> newNode) {
    boolean result = true;
    if ((!this.hasSelectedNode()) || (!this.selectedNode.equals(newNode))) {
      if (this.selectedNode != null) {
        result = this.selectedNode.canUnSelectNode();
      }
      
      if (result) {
        result = this.onCanSelectNode(newNode);
      }
    }
    return result;
  }
  
  /**
   * CAN OVERRIDE: Override this method to custom-handle whether the specified node can
   * be the selected node.
   * @param newNode the new node to set as the selected node.
   * @return true if the selection is allowed.
   */
  protected boolean onCanSelectNode(TreeNode<?, TView> newNode) {
    return true;
  }
  
  /**
   * Called by the newNode to assign itself as the TreeView's SelectedNode (after
   * checking that is can be the new selected node). If the selection has changed, it
   * calls {@linkplain #beginSelecting() this.beginSelecting} to set this.isSelecting
   * = true, it sets this.selectedNode = newNode before calling {@linkplain 
   * #onSetSelectedNode(bubblewrap.treeview.TreeNode, bubblewrap.treeview.TreeNode) 
   * this.onSetSelectedNode}. Finally, its calls {@linkplain #endSelecting() 
   * this.endSelecting}, which will fire the TreeView's {@link #SelectedNodeChanged 
   * SelectedNodeChanged} event if this.isSelecting turns to to false.
   * @param newNode the new selected node.
   */
  protected final void setAsSelectedNode(TreeNode<?, TView> newNode) {
    boolean hasChanged = (!DataEntry.isEq(newNode, this.selectedNode));
    TreeNode<?, TView> priorNode = null;
    if ((hasChanged) && (((priorNode = this.selectedNode) == null) || 
            (priorNode.canUnSelectNode()))) {
      try {
        this.beginSelecting();
        if (priorNode != null) {
          priorNode.beforeUnSelectNode();
        }
        this.selectedNode = newNode;
        try {
          this.onSetSelectedNode(priorNode, this.selectedNode);
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.setAsSelectedNode Error:\n {1}",
                  new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        }
      } finally {
        this.endSelecting();
      }
    }
  }
  
  /**
   * CAN OVERRIDE: Called from {@linkplain #setAsSelectedNode(
   * bubblewrap.treeview.TreeNode) this.setAsSelectedNode} after set this.selectedNode 
   * and before firing {@link #SelectedNodeChanged this.SelectedNodeChanged} event 
   * to custom handle the change in selected Nodes.
   * <p>It is also called from {@linkplain #clearSelectedNode() this.clearSelectedNode}.
   * <p><b>The base method does nothing.</b>
   * @param priorNode the previous selected node
   * @param newNode the new selected node
   * @throws Exception 
   */
  protected void onSetSelectedNode(TreeNode<?, TView> priorNode, 
                                          TreeNode<?, TView> newNode) throws Exception {    
  }
    
  /**
   * CAN OVERRIDE: Called when the selected Node changed AFTER the new node is assigned,
   * but BEFORE the TreeView's {@linkplain #SelectedItemChanged SelectedItemChanged} event
   * if fired.<p>The base method does nothing. 
   * <p>
    * <b>NOTE:</b> This.isSelecting = true when this call is made.</p>
   * @param priorNode the previously selected Node
   * @param newNode the new selected Node
   * @throws Exception - all errors are trapped and logged.
  */
  protected void onSelectedNodeChanged(TreeNode<?, TView> priorNode, 
                                         TreeNode<?, TView> newNode) throws Exception {} 
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Manage the TreeView's RootNode">
  /**
   * Get the TreeView's RootNode. If this.rootNode = null, it calls {@linkplain 
   * #initRootNode() this.initRootNode} to custom initiate the root node.
   * @return the assigned rootNode or null if initiating the rootNode failed.
   */
  public TRoot getRootNode() {
    if (this.rootNode == null) {
      this.initRootNode();
    }
    return this.rootNode;
  }
  
  /**
   * Set the TreeView's RootNode.
   * @param rootNode the new rootNode.
   * @throws IllegalArgumentException if the node is already assigned.
   */
  @SuppressWarnings("unchecked")
  private void setRootNode(TRoot rootNode) {
    try {
      if (rootNode == null) {
        throw new Exception("The new RootNode is unassigned.");
      }
      if (!TreeNode.isTreeRoot(rootNode.getClass())) {
        throw new Exception("TreeNode Class[" + rootNode.getClass().getSimpleName() 
                + "] is not a designated Root Node");
      }
      
      if (this.rootNode != null) {
        throw new Exception("The TreeView's RootNode is already assigned.");
      }
      
      if (rootNode.getTreeView() != null) {
        throw new Exception("Root TreeNode already has an assigned "
                + "TreeView. It cannot added to another TreeView or re-added to this "
                + "TreeView.");
      }
      
      rootNode.setTreeView((TView) this);
      this.rootNode = rootNode;
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".setRootNode Error:\n " + exp.getMessage());
    }
  }
  
  /**
   * Called by {@linkplain #getRootNode() this.getRootNode()} is this.rootNode is 
   * undefined. It calls {@linkplain #onInitRootNode() this.onInitRootNode} to get a
   * new instance of the treeView's rootNode (newRoot). It the calls {@linkplain 
   * #setRootNode(bubblewrap.treeview.TreeNode) this.setRootNode(newRoot)} to add the
   * new node as the treeView's rootNode. All errors are trapped and logged.
   */
  private void initRootNode() {
    if (this.rootNode != null) {
      return;
    }
    try {
      TRoot newRoot = this.onInitRootNode();
      if (newRoot == null) {
        throw new Exception("The new RootNode is unassigned.");
      }
      this.setRootNode(newRoot);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.initRootNode Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * ABSTRACT: Called by {@linkplain #initRootNode() this.initRootNode}, which is
   * called by {@linkplain #getRootNode() this.getRootNode()} if this.rootNode = null,
   * to initiate the TreeView's root Node. All exceptions are trapped and logged. 
   * @return the new Root Node instance.
   * @throws Exception if the request fails
   */
  protected abstract TRoot onInitRootNode() throws Exception;  
  
  /**
   * Check if the Panel should be reset
   * @return true if a reset is necessary
   */
  public boolean doResetPanel() {
    return ((this.resetPanel == null) || (this.resetPanel));
  }
  
  /**
   * Called internally to set this.doResetPanel=true
   */
  protected void setResetPanel() {
    this.resetPanel = true;
  }
  
  /**
   * Called by the {@linkplain TreeViewPanel} after updating its ChildNode to
   * cancel this.doResetPanel
   */
  public void cancelResetPanel() {
    this.resetPanel = null;
  }
  
  /**
   * Called internally to reset the RootNode (e.g. to re-assign a different node when
   * deserializing the TreeView's content). If the rootNode is assigned, It the calls 
   * {@linkplain #clearSelectedNode() this.clearSelectedNode()} to reset the selected 
   * node and {@linkplain TreeNode#onDeleteChildren() this.rootNode.onDeleteChildren()} 
   * to delete all the rootNode's children before calling {@linkplain 
   * TreeNode#resetTreeView(myapp.core.treeview.TreeView) 
   * this.rootNode.resetTreeView(this)} to reset the rootNode's TreeView reference.
   */
  @SuppressWarnings("unchecked")
  protected final void resetRootNode() {
    try {
      this.clearSelectedNode();
      if (this.rootNode != null) {
        this.rootNode.onDeleteChildren();
        this.rootNode.resetTreeView((TView) this);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.resetRootNode Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.rootNode = null;
    }
  }
  
  /**
   * Called to clear the of this.rootNode (if this.rootNode != null). It first
   * call {@linkplain #clearSelectedNode() this.clearSelectedNode} to clear the
   * current selectedNode. It then calls {@linkplain #onResetingRootContent()
   * this.onResetingRootContent} before calling this.rootNode.clearChildren to
   * unload the rootNode's children.
   * <p>
   * All errors are trapped and logged.
   */
  protected final void resetRootContent() {
    try {      
      if (this.rootNode != null) {
        this.clearSelectedNode();
        try {
          this.onResetingRootContent();
        } finally {
          this.rootNode.clearChildren();
        }
      }
    } catch (Exception ex) {
      logger.log(Level.WARNING, "{0}.resetRootContent Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), ex.getMessage()});
    }
  }
  
  /**
   * Called by {@linkplain #resetRootContent() this.resetRootContent} before clearing
   * the rootNode's Children. Called only if this.rootNode != null. Called after 
   * clearing the current Selected Node.
   */
  protected void onResetingRootContent() { }
  
  /**
   * Called to set the TreeView's selected Path for the specified <tt>path</tt>.
   * @param path the selected node's path or null to selected the rootNode.
   */
  public final void gotoNode(String path) {
    try {
      
      TreeNode root = this.getRootNode();
      if (root != null) {
        root.selectByPath(path);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.gotoNode Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to assign this TreeView's nodes and all its settings to the target TreeView.
   * it call {@linkplain #resetRootNode() this.resetRootNode} to reset this target 
   * TreeView's current rootNode. If this.rootNode != null, clone this.rootNode, add the
   * clone as the target's rootNode and call {@linkplain TreeNode#assignTo(
   * myapp.core.treeview.TreeNode) this.rootNode.assignTo} to transfer all its settings
   * and its sub-treeNodes to the cloned root.
   * @param target the Target TreeView to update.
   * @throws Exception when this process fails. The target's rootNode will be reset if
   * an exception if thrown.
   */
  @SuppressWarnings("unchecked")
  public void assignTo(TreeView<TRoot, TView> target) throws Exception {
    try {
      target.resetRootNode();
      TRoot root = this.getRootNode();
      if (root != null) {
        TRoot clone = (TRoot) root.clone();
        target.setRootNode(clone);
        root.assignTo((TreeNode) clone);
      }
    } catch (Exception exp) {
      target.resetRootNode();
      throw new Exception(this.getClass().getSimpleName()
              + ".assignTo Error:\n " + exp.getMessage());
    }
  }
  //</editor-fold>  
}
