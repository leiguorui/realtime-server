/*
 * Copyright 2013 Goodow.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.goodow.realtime.server.bootstrap;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;

import io.vertx.java.redis.RedisClient;

// @GuiceVertxBinding(modules = {RealtimeModule.class})
public class RealtimeVerticle extends BusModBase {
  @Override
  public void start(final Future<Void> startedResult) {
    super.start();
    // GuiceVerticleHelper.inject(this, vertx, container);

    JsonObject webServerConfig = config.getObject("webServer");
    container.deployVerticle(RealtimeWebServer.class.getName(), webServerConfig,
        new AsyncResultHandler<String>() {
          @Override
          public void handle(AsyncResult<String> event) {
            if (event.succeeded()) {
              startedResult.setResult(null);
            } else {
              startedResult.setFailure(event.cause());
            }
          }
        });
    JsonObject redisConfig = config.getObject("redis");
    redisConfig = redisConfig == null ? new JsonObject() : redisConfig;
    RedisClient redisClient =
        new RedisClient(eb, redisConfig.getString("address", "io.vertx.mod-redis"));
    redisClient.deployModule(container, redisConfig.getString("host", "localhost"), redisConfig
        .getInteger("port", 6379));
  }
}
