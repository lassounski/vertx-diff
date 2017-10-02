package com.delaru.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Endpoints;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;
import model.DiffRequest;
import model.db.DomainCollection;

import java.io.IOException;

/**
 * Verticle responsible for saving and deleting documents into the DB
 */
public class DocumentVerticle extends AbstractVerticle {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentVerticle.class);

  @Override
  public void start() throws Exception {
    MAPPER.findAndRegisterModules();

    final MongoClient mongoClient = MongoClient.createShared(vertx, config(), "mongo-pool");

    vertx
        .eventBus()
        .consumer(
            Endpoints.RESET_EB,
            event ->
                mongoClient.dropCollection(
                    DomainCollection.DOCS.collection(),
                    result -> {
                      if (result.succeeded()) LOGGER.info("Dropped docs");
                      if (result.failed()) throw new RuntimeException("Failed in dropping docs");
                    }));

    createConsumer(mongoClient, "right", Endpoints.DIFF_RIGHT_EB);
    createConsumer(mongoClient, "left", Endpoints.DIFF_LEFT_EB);
  }

  private void createConsumer(MongoClient mongoClient, String side, String eventBusName) {
    vertx
        .eventBus()
        .consumer(
            eventBusName,
            event -> {
              try {
                LOGGER.info("Received" + side + " DiffRequest");
                DiffRequest diffRequest =
                    MAPPER.readValue(event.body().toString(), DiffRequest.class);
                JsonObject query =
                    new JsonObject()
                        .put("id", diffRequest.getId())
                        .put("side", diffRequest.getSide());
                JsonObject update = new JsonObject().put("$set", diffRequest.getJsonObject());

                UpdateOptions updateOptions = new UpdateOptions().setUpsert(true);

                mongoClient.updateCollectionWithOptions(
                    DomainCollection.DOCS.collection(),
                    query,
                    update,
                    updateOptions,
                    result -> {
                      if (result.succeeded()) {
                        LOGGER.info("Saved " + side + " DiffRequest");
                      } else if (result.failed()) {
                        LOGGER.info("Failed to save " + side + " DiffRequest");
                      }
                    });
                event.reply("OK");
              } catch (IOException e) {
                String errorMessage = "Error on deserialize " + side + " " + "DiffRequest";
                LOGGER.error(errorMessage, e);
                event.fail(500, errorMessage);
              }
            });
  }
}
