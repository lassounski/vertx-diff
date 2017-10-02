package com.delaru.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Endpoints;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import model.DiffRequest;

public class ApiVerticle extends AbstractVerticle {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiVerticle.class);

  @Override
  public void start() throws Exception {
    startWebApp();
  }

  private void startWebApp() {
    // allows to declare API endpoints
    final Router router = Router.router(this.vertx);
    // handles the request body and sets it on the RoutingContext
    router.route().handler(BodyHandler.create());

    createPostDocumentEndpoint(router, "left", Endpoints.DIFF_LEFT_EB);
    createPostDocumentEndpoint(router, "right", Endpoints.DIFF_RIGHT_EB);
    createDiffEndpoint(router);
    createResetEndpoint(router);

    // creates the server
    this.vertx.createHttpServer().requestHandler(router::accept).listen(4004);
  }

  private void createResetEndpoint(Router router) {
    router
        .get("/v1/reset")
        .handler(
            ctx -> {
              vertx.eventBus().send(Endpoints.RESET_EB, "reset");
              ctx.response().end();
            });
  }

  private void createDiffEndpoint(Router router) {
    router
        .get("/v1/diff/:id")
        .handler(
            ctx -> {
              String id = ctx.request().getParam("id");
              vertx
                  .eventBus()
                  .send(
                      Endpoints.DIFF_EB,
                      id,
                      replyEvent -> {
                        HttpServerResponse response = ctx.response();

                        response.putHeader("Content-Type", "application/json");
                        if (replyEvent.succeeded()) {
                          response.end(Json.encodePrettily(replyEvent.result().body()));
                        }
                        if (replyEvent.failed()) {
                          response
                              .setStatusCode(400)
                              .end(
                                  Json.encodePrettily(
                                      new JsonObject()
                                          .put("error", replyEvent.cause().getMessage())));
                        }
                      });
              LOGGER.info("Sent diffRequest for comparing id:" + id);
            });
  }

  private void createPostDocumentEndpoint(
      Router router, String diffSide, String diffSideEventBusName) {
    router
        .post("/v1/diff/:id/" + diffSide)
        .handler(
            ctx -> {
              String id = ctx.request().getParam("id");
              JsonObject text = ctx.getBodyAsJson();
              DiffRequest diffRequest =
                  DiffRequest.builder().id(id).text(text.getString("text")).side(diffSide).build();

              try {
                vertx
                    .eventBus()
                    .send(
                        diffSideEventBusName,
                        MAPPER.writeValueAsString(diffRequest),
                        event -> {
                          LOGGER.info("Sent diffRequest for saving");
                          HttpServerResponse response = ctx.response();
                          response.putHeader("Content-Type", "application/json");
                          response.end(
                              new JsonObject()
                                  .put(
                                      "status",
                                      String.format("%s side for id %s accepted", diffSide, id))
                                  .encodePrettily());
                        });
              } catch (JsonProcessingException e) {
                String errorMessage = String.format("Error on serializing [%s]", diffRequest);
                LOGGER.error(errorMessage);
                ctx.response().setStatusCode(500).end(errorMessage);
              }
            });
  }
}
