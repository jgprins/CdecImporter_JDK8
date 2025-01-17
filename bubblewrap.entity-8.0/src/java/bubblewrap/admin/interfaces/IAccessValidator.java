package bubblewrap.admin.interfaces;

import bubblewrap.admin.context.AdminContext;
import bubblewrap.core.events.AccessValidationEventArgs;

/**
 * An interface (typically used by {@linkplain AdminContext}) to process user access 
 * validation request. It supports only one method {@linkplain #validateAccess(
 * bubblewrap.admin.events.ValidateAccessRequestArgs) validateAccess}
 * @author kprins
 */
public interface IAccessValidator {
  /**
   * Called by EntityWrappers, EntityViews, etc. to validate that the user has access
   * to the targeted content and actions.
   * @param args the {@linkplain AccessValidationEventArgs}
   * @param sender the object that send the request.
   */
  public void validateAccess(Object sender, AccessValidationEventArgs args);
}
