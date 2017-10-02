package com.delaru.worker;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Compares two documents by deciding if they are equal, not equal or different
 */
public class DiffComparator {

  private String left, right;

  /**
   * Adds a text to the comparator. Needs to be called at least twice.
   *
   * @param text to be compared
   */
  public void addText(String text) {
    if (left == null) this.left = text;
    else right = text;
  }

  /**
   * Compares the texts that has been set with {@code addText()}. Throws an {@link
   * IllegalArgumentException} if one of the texts is {@code null}.
   *
   * @return a {@link JsonObject} with the result
   */
  public JsonObject compare() {
    JsonObject jsonObject = new JsonObject();
    if (left == null || right == null) {
      throw new IllegalArgumentException("One of the texts is null");
    }
    if (left.equals(right)) {
      return jsonObject.put("result", "equal");
    } else if (left.length() != right.length()) {
      return jsonObject.put("result", "not of equal size");
    } else {
      JsonObject diff = new JsonObject();
      JsonArray jsonArray = new JsonArray();
      char[] leftChars = left.toCharArray();
      char[] rightChars = right.toCharArray();
      boolean differentSection = false;
      int offset = 0;

      for (int index = 0; index < leftChars.length; index++) {
        if (leftChars[index] != rightChars[index]) {
          offset += 1;
          if (!differentSection) {
            differentSection = true;
            diff = new JsonObject().put("position", index);
          }
        }
        if ((leftChars[index] == rightChars[index] && differentSection)
            || (index == leftChars.length - 1 && differentSection)) {
          diff.put("offset", offset);
          jsonArray.add(diff);
          differentSection = false;
          offset = 0;
        }
      }
      return jsonObject.put("result", jsonArray);
    }
  }
}
