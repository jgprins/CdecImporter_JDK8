package bubblewrap.admin.events;

import java.util.EventListener;

/**
 *
 * @author kprins
 */
@Deprecated
public interface ValidateAccessListener extends EventListener {
  public boolean onValidateAccess(ValidateAccessEvent e);
}
