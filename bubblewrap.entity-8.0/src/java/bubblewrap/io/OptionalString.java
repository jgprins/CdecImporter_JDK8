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
 *
 * @author hdunsford
 */
public class OptionalString extends Optional<String> {

  /**
   * Creates a new instance of an OptionalString.
   *
   * @param item The String item that could be null or empty.
   */
  private OptionalString(String item) {
    super(DataEntry.cleanString(item));
  }

  /**
   * Creates a new optional from the specified item, where the item must not be
   * null. If it is null, this will throw an IllegalArgumentException.   
   * @param item The immutable item for this optional of type String.
   * @return The Optional&lt;T&gt;
   */
  public static OptionalString of(final String item) {
    if (item == null || item.isEmpty()) {
      throw new IllegalArgumentException("Use ofNullable if item can be null or empty.");
    }
    return new OptionalString(item);
  }

  /**
   * Returns an Optional describing the specified value, if non-null, otherwise
   * returns an empty Optional.
   *
   * @param item A String item that can be null or empty.
   * @return An Optional of type T, or an Optional where the item is absent.
   */
  public static OptionalString ofNullable(String item) {
    return new OptionalString(item);
  }  
}
