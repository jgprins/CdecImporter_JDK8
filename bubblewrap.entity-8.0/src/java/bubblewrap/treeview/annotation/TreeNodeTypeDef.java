package bubblewrap.treeview.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import bubblewrap.treeview.enums.TreeNodeType;
import java.lang.annotation.Inherited;

/**
 * A Designation of the TreeNode class' TreeNodeTypeDef
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.TYPE})
@Inherited
public @interface TreeNodeTypeDef {
  /**
   * Get/Set a TreeNode's TreeNodeTypeDef (default={@linkplain TreeNodeType#Document})
   * @return the assigned value or the default value if unassigned.
   */
  TreeNodeType nodeType() default TreeNodeType.Document;
}
