package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;

@Value.Immutable
@JsonSerialize(as = ImmutableDiffRequest.class)
@JsonDeserialize(as = ImmutableDiffRequest.class)
public abstract class DiffRequest {

  public abstract String getId();

  public abstract String getText();

  public abstract String getSide();

  @Value.Derived
  @JsonIgnore
  public JsonObject getJsonObject() {
    JsonObject jsonObject = new JsonObject();

    jsonObject.put("id", getId());
    jsonObject.put("text", getText());
    jsonObject.put("side", getSide());

    return jsonObject;
  }

  public static ImmutableDiffRequest.Builder builder() {
    return ImmutableDiffRequest.builder();
  }
}
