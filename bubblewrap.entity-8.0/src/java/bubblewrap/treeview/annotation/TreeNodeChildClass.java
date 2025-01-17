package bubblewrap.treeview.annotation;

import bubblewrap.treeview.TreeNode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Designation of a Child TreeNode class supported by the TreeNode
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.TYPE})
public @interface TreeNodeChildClass {
  /**
   * Get/Set a TreeNode's supported Child class
   * @return the assigned value.
   */
  Class<? extends TreeNode> childClass();
}
