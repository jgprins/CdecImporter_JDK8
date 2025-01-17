package bubblewrap.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be used in addition to OneToMany, ManyToOne, OneToOne, or 
 * ManyToMany annotations to provide additional association information.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.FIELD, ElementType.METHOD})
public @interface Association {
  /**
   * An optional flag to indicate that this target entity in this association is the
   * child's parent. This can be used in lieu of using the ManyToMany or OneToOne
   * annotations' cascade setting to indicate the parent/child relationship.
   * Ignored is the association is a OneToMany or ManyToOne.
   * Default = false.
   * @return the assigned setting
   */
  boolean parent() default false;
  /**
   * An optional flag indicating the EntityWrapper(s) in the association should be
   * cached by the parent or child. Default = false. Set flag on the child field to
   * cache the parent and vice versa.
   * @return the assigned setting
   */
  boolean cache() default false;
  /**
   * An optional flag to indicate that a parent in a ManyToOne, OneToOne, or 
   * ManyToMany association is the OwnerParent of the Child (i.e., the parent that is
   * responsible for loading the child entities and that should maintain the "master"
   * collection of child entities. Default = false.
   * <p><b>Note:</b> Ignored if assigned to a parent association</p>
   * <p>Ignored if assigned to a One-ToMany relationship
   * @return the assigned value
   */
  boolean ownerParent() default false;
}
