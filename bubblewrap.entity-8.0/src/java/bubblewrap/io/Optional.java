/*
 * The MIT License
 *
 * Copyright 2015 hdunsford.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package bubblewrap.io;

/**
 * Note, this is temporary. Java 8 introduces Optional as part of its utils. But
 * as long as we are programming in java 7, we can use this.
 *
 * @author hdunsford
 * @param <T> The type of the optional object stored in this optional.
 */
public class Optional<T> {

  /**
   * The actual item stored in the optional.
   */
  private T item;

  /**
   * Creates a new instance of an optional. This is only called from the static
   * methods in this class.
   *
   * @param item The immutable item to use for this object.
   */
  protected Optional(T item) {
    this.setItem(item);
  }

  /**
   * Returns an empty Optional instance.
   *
   * @param <T> The T type of the item in the Optional.
   * @return The empty optional.
   */
  public static <T> Optional<T> empty() {
    return new Optional(null);
  }

  /**
   * Indicates whether some other object is equal to this optional.
   *
   * @param obj The object to test for equality.
   * @return Boolean if the other object is either an optional containing the
   * same value as this optional, or else is an object that equals,
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (this.item == null) {
      return (obj == null);
    }
    if (obj instanceof Optional<?>) {
      Optional<?> test = (Optional<?>) obj;
      if (!test.isPresent()) {
        return false;
      }
      Object testValue = test.get();
      return this.item.equals(testValue);
    }
    return item.equals(obj);
  }


  /**
   * Returns the contained T instance, which must be present; otherwise, throws
   * an IllegalStateException.
   *
   * @return T That is definitely not null.
   */
  public T get() {
    if (!this.isPresent()) {
      throw new IllegalStateException();
    }
    return item;
  }

  /**
   * Returns the hash code value of the present value, if any, or 0 (zero) if no
   * value is present.
   *
   * @return the hash code or 0.
   */
  @Override
  public int hashCode() {
    if (this.isPresent()) {
      return item.hashCode();
    }
    return 0;
  }

  /**
   * Checks if the item is null.
   *
   * @return Boolean true if the item is present, otherwise false.
   */
  public boolean isPresent() {
    return item != null;
  }

  /**
   * Creates a new optional from the specified item, where the item must not be
   * null. If it is null, this will throw an IllegalArgumentException.
   *
   * @param <T> The type of the item to be stored.
   * @param item The immutable item for this optional of type T. T cannot be
   * null.
   * @return The Optional&lt;T&gt;
   */
  public static <T> Optional<T> of(final T item) {
    if (item == null) {
      throw new IllegalArgumentException();
    }
    return new Optional<>(item);
  }

  /**
   * Returns an Optional describing the specified value, if non-null, otherwise
   * returns an empty Optional.
   *
   * @param <T> The type of the item in the nullable optional to be returned.
   * @param item An item of type T that can be null.
   * @return An Optional of type T, or an Optional where the item is absent.
   */
  public static <T> Optional<T> ofNullable(T item) {
    return new Optional<>(item);
  }

  /**
   * Returns the present value in this Optional, or if there is none, returns
   * the specified default.
   *
   * @param alternate The T to use in the case when the item is missing.
   * @return The non-null T item from this optional, or the specified T
   * alternative if the T in this optional is absent.
   */
  public T orElse(T alternate) {
    if (isPresent()) {
      return this.item;
    } else {
      return alternate;
    }
  }

  /**
   * This should only be used in a protected fashion, because it will confuse
   * outside accessors.
   * @return the item
   */
  protected T getItem() {
    return item;
  }
  

  /**
   * This should only be used in a protected fashion, because it will confuse
   * outside accessors.
   * @return the item
   */
  protected void setItem(T item) {
    this.item = item;
  }
}
