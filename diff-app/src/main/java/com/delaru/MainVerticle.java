package com.delaru;


import com.delaru.api.ApiVerticle;
import com.delaru.worker.DiffVerticle;
import com.delaru.worker.DocumentVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * The entry point of the application where the verticles are created.
 * Each verticle can be considered as a microservice, and could be deployed separately.
 * The DiffVerticle is a worker, meaning that its work will be performed in a separate thread by
 * Vert.x leaving the event loop unblocked.
 */
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        ClusterManager mgr = new HazelcastClusterManager();
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.clusteredVertx(options, res -> {
            final Vertx vertx = res.result();
            final DeploymentOptions worker = new DeploymentOptions();
            worker.setWorker(true);

            vertx.deployVerticle(ApiVerticle.class.getName());
            vertx.deployVerticle(DocumentVerticle.class.getName());
            vertx.deployVerticle(DiffVerticle.class.getName(), worker);
        });
    }
}
