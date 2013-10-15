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
package com.goodow.realtime.server.service;

import com.goodow.realtime.channel.constant.Constants.Params;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class SaveHandler implements Handler<HttpServerRequest> {

  @Override
  public void handle(HttpServerRequest req) {
    String id = req.params().get(Params.ID);
  }
}
