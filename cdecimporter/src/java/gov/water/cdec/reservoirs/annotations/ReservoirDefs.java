package gov.water.cdec.reservoirs.annotations;

import java.lang.annotation.*;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ReservoirDefs {
  ReservoirDef[] value();
}
