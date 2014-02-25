package com.goodow.realtime.server.bootstrap;

import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.vertx.VertxBusServer;

import com.alienos.guice.VertxModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import java.util.logging.Logger;

public class RealtimeModule extends AbstractModule implements VertxModule {
  private static final Logger log = Logger.getLogger(RealtimeModule.class.getName());

  private Vertx vertx;
  private Container container;
  //as

  @Override
  public void setContainer(Container container) {
    this.container = container;
  }

  @Override
  public void setVertx(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  protected void configure() {
  }

  @Provides
  @Singleton
  Bus provideBus() {
    return new VertxBusServer(vertx.eventBus());
  }

  @Provides
  @Singleton
  Client provideElasticSearchClient() {
    JsonObject config =
        container.config().getObject("elasticsearch").getObject("client").getObject("transport");
    TransportClient client =
        new TransportClient().addTransportAddress(new InetSocketTransportAddress(config.getString(
            "host", "localhost"), config.getInteger("port", 9300)));
    return client;
  }
}
