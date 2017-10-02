package com.delaru.worker;

import commons.Endpoints;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import model.db.DomainCollection;

import java.util.Base64;
import java.util.List;

/**
 * Worker verticle responsible from consuming Diff events from the EventBus and communicating with the DB
 */
public class DiffVerticle extends AbstractVerticle {

  private final DiffComparator diffComparator = new DiffComparator();
  private static final Logger LOGGER = LoggerFactory.getLogger(DiffVerticle.class);

  @Override
  public void start() throws Exception {
    final MongoClient mongoClient = MongoClient.createShared(vertx, config(), "mongo-pool");
    vertx
        .eventBus()
        .consumer(
            Endpoints.DIFF_EB,
            event -> {
              LOGGER.info("Making diff");
              String id = event.body().toString();
              JsonObject byIdQuery = new JsonObject().put("id", id);
              mongoClient.find(
                  DomainCollection.DOCS.collection(),
                  byIdQuery,
                  response -> {
                    if (response.succeeded()) {
                      if (response.result().size() < 2) {
                        handleMissingDocuments(event, id, response.result().size());
                      } else {
                        decodeAndDiff(event, response.result());
                      }
                    } else {
                      event.fail(500, "Error fetching documents for id:" + id);
                    }
                  });
            });
  }

  private void decodeAndDiff(Message<Object> event, List<JsonObject> documents) {
    try {
      documents
          .stream()
          .map(entry -> entry.getString("text"))
          .map(encodedText -> new String(Base64.getDecoder().decode(encodedText)))
          .forEach(diffComparator::addText);
    } catch (IllegalArgumentException ex) {
      event.fail(400, "Invalid B64 document in diff");
    }
    event.reply(diffComparator.compare());
  }

  private void handleMissingDocuments(Message<Object> event, String id, int size) {
    switch (size) {
      case 0:
        event.fail(400, "No documents found for id " + id);
      case 1:
        event.fail(400, "One of the sides for the diff is missing");
    }
  }
}
