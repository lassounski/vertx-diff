package com.delaru.worker;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DiffComparatorTest {

  private DiffComparator diffComparator;

  @Before
  public void setUp() {
    diffComparator = new DiffComparator();
  }

  @Test
  public void shouldThrowIllegalArgumentExIfTextMissing() {
    Assertions.assertThatThrownBy(() -> diffComparator.compare())
        .isInstanceOf(IllegalArgumentException.class);

    diffComparator.addText("One text only");
    Assertions.assertThatThrownBy(() -> diffComparator.compare())
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldCompareEqualTexts() {
    diffComparator.addText("Text one");
    diffComparator.addText("Text one");

    assertThat(diffComparator.compare()).isEqualTo(new JsonObject().put("result", "equal"));
  }

  @Test
  public void shouldCompareDifferentTextsInSize() {
    diffComparator.addText("Text one");
    diffComparator.addText("Text three");

    assertThat(diffComparator.compare())
        .isEqualTo(new JsonObject().put("result", "not of equal size"));
  }

  @Test
  public void shouldCompareEqualInSizeDifferentInContent() {
    JsonArray jsonArray = new JsonArray();
    jsonArray.add(new JsonObject().put("position", 5).put("offset", 3));
    jsonArray.add(new JsonObject().put("position", 12).put("offset", 5));
    JsonObject expectedResult = new JsonObject().put("result", jsonArray);

    diffComparator.addText("Text one is great");
    diffComparator.addText("Text two is finer");

    assertThat(diffComparator.compare()).isEqualTo(expectedResult);
  }
}
