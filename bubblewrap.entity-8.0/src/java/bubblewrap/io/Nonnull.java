/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bubblewrap.io;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface Nonnull {  
}
