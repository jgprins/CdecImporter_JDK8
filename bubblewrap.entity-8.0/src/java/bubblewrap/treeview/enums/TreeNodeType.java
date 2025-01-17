package bubblewrap.treeview.enums;

/**
 * An Set of Enum value to define TreeView's TreeNode Types
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum TreeNodeType {
  TreeRoot(false, true, false, NodeExpandType.Expanded, "tvNodeHome"),
  Container(true, true, false, NodeExpandType.Expanded, "tvNodeContainer"),
  NodeGroup(true, true, true, NodeExpandType.Default, "tvNodeGroup"),
  Folder(true, true, true, NodeExpandType.Default, "tvNodeFolder"),
  Document(true,false, true, NodeExpandType.Collapsed, "tvNodeDoc");
 
  //<editor-fold defaultstate="collapsed" desc="TreeNodeType definition">
  //<editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * True if the TreeItem supports Parent Items
   */
  public final boolean requireParent;
  /**
   * True if the TreeItem supports Child Items
   */
  public final boolean allowChildren;
  /**
   * True is this node can be selected in the treeView
   */
  public final boolean isSelectable;
  /**
   * The Tree Node's {@linkplain NodeExpandType} option.
   */
  public final NodeExpandType expandType;
  /**
   * The StyleClass for rendering the Node Type 
   */
  public final String styleClass;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">  
  /**
   * Public Constructor
   */
  private TreeNodeType(boolean requireParent, boolean allowChildren, 
                                               boolean isSelectable, 
                                               NodeExpandType expandType,
                                               String styleClass) {
    this.requireParent = requireParent;
    this.allowChildren = allowChildren;
    this.isSelectable = isSelectable;
    this.expandType = expandType;
    this.styleClass = styleClass;
  }
  // </editor-fold>
  //</editor-fold> 
}
