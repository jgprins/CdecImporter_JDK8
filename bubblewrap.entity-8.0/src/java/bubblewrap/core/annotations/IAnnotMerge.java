package bubblewrap.core.annotations;

import java.lang.annotation.Annotation;

/**
 * An interface an Annotation implementation to add a merge method to the 
 * implementation class.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public interface IAnnotMerge<TAnnot extends Annotation> {
  
  /**
   * ABSTRACT: Called to merge the annotation of this instance of TAnnot with that of
   * <tt>other</tt>. Each implementation should be design to custom handle this process.
   * @param other the source annotation
   */
  public void merge(TAnnot other);
}
