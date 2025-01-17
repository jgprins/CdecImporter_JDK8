package bubblewrap.entity.annotations;

import bubblewrap.core.enums.PrimaryKeyType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to assign to an Entity field to designate the field as the Entity's
 * record Id field and setting the {@linkplain #type()}.
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.FIELD})
public @interface PrimaryKey {
  /**
   * Get/Set the RecordID Field's 's recordId Type (type {@link PrimaryKeyType}}   
   * @return the assigned value 
   */
  PrimaryKeyType type(); 
}
