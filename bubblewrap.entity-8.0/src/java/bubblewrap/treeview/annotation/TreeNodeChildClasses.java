package bubblewrap.treeview.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Designation of one or more TreeNodeChildClass defining the ChidlClasses support
 * by this TreeNode
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.TYPE})
public @interface TreeNodeChildClasses {
  /**
   * Get/Set a TreeNode's supported Child classes
   * @return the assigned array of TreeNodeChildClass annotation
   */
  public TreeNodeChildClass[] childClasses();
}
