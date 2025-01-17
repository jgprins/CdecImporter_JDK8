package bubblewrap.entity.validators;

/**
 * <p>The FieldContraintTypes enums are used by the {@linkplain FieldValidator} to manage
 * the Entity field's assigned constraints. Enum values include:</p><ul>
 * <li>{@linkplain #NotNull} - the field value cannot be unassigned</li>
 * <li>{@linkplain #SizeRange} - the String field value has size limitation</li>
 * <li>{@linkplain #Unique} - the field value must be unique in recordset</li>
 * <li>{@linkplain #UniqueInParent} - the field value must be unique for each 
 *      foreignKey-parent</li>
 * <li>{@linkplain #InputMask} - the field value must comply to special input constraint 
 *      (e.g., email address, web site, etc.)</li>
 * <li>{@linkplain #FieldValidation} - the field value must comply to input constraint 
 *      defined by a custom input validator (.e., to validate a range of values or 
 *      values in a selection list)</li>
 * </ul>
 * @author kprins
 */
public enum FieldContraintTypes {
  /**
   * Field Value cannot be null.
   */
  NotNull,
  /**
   * The value size cannot exceed a specified size (typically Strings or byte arrays).
   */
  SizeRange,
  /**
   * Field value must be unique in recordset.
   */
  Unique,
  /**
   * Field value must be unique for each foreignKey-parent.
   */
  UniqueInParent,
  /**
   * Field with a special input constraint (e.g., email address, web site, etc.).
   */
  InputMask,
  /**
   * Field with a custom input validator (.e., to validate a range of values or values
   * in a selection list).
   */
  FieldValidation;  
}
