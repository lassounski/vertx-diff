package com.delaru;

import com.jayway.restassured.RestAssured;
import io.vertx.core.json.JsonObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Base64;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class DiffIT {

  private static final String TEXT = "This is a text";

  @BeforeClass
  public static void configureRestAssured() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = 4004;
    //making sure that the verticles are deployed
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @AfterClass
  public static void destroy() {
    get("/v1/reset").then().assertThat().statusCode(200);

    RestAssured.reset();
  }

  @Test
  public void shouldReturnEqualForEqualDocuments() {
    String encodedText = new String(Base64.getEncoder().encode(TEXT.getBytes()));
    JsonObject json = new JsonObject().put("text", encodedText);
    given()
        .contentType("application/json")
        .body(json.encode())
        .post("/v1/diff/1/left")
        .then()
        .statusCode(200)
        .body("status", equalTo("left side for id 1 accepted"));
    given()
        .contentType("application/json")
        .body(json.encode())
        .post("/v1/diff/1/right")
        .then()
        .statusCode(200)
        .body("status", equalTo("right side for id 1 accepted"));
    get("/v1/diff/1").then().assertThat().statusCode(200).and().body("result", equalTo("equal"));
  }

  @Test
  public void shouldReturn400WhenNoDocumentsAreSaved() {
    get("/v1/diff/2")
        .then()
        .assertThat()
        .statusCode(400)
        .and()
        .body("error", equalTo("No documents found for id 2"));
  }

  @Test
  public void shouldReturnErrorMessageIfOneOfTheDocumentsIsMissing() {
    String encodedText = new String(Base64.getEncoder().encode(TEXT.getBytes()));
    JsonObject json = new JsonObject().put("text", encodedText);
    given()
        .contentType("application/json")
        .body(json.encode())
        .post("/v1/diff/3/right")
        .then()
        .statusCode(200)
        .body("status", equalTo("right side for id 3 accepted"));
    get("/v1/diff/3")
        .then()
        .assertThat()
        .statusCode(400)
        .and()
        .body("error", equalTo("One of the sides for the diff is missing"));
  }

  @Test
  public void shouldReturnErrorIfInvalidB64() {
    JsonObject json = new JsonObject().put("text", TEXT);
    given()
        .contentType("application/json")
        .body(json.encode())
        .post("/v1/diff/4/left")
        .then()
        .statusCode(200)
        .body("status", equalTo("left side for id 4 accepted"));
    given()
        .contentType("application/json")
        .body(json.encode())
        .post("/v1/diff/4/right")
        .then()
        .statusCode(200)
        .body("status", equalTo("right side for id 4 accepted"));
    get("/v1/diff/4")
        .then()
        .assertThat()
        .statusCode(400)
        .and()
        .body("error", equalTo("Invalid B64 document in diff"));
  }
}
