package com.goodow.realtime.server.bootstrap;

import com.goodow.realtime.server.service.SaveHandler;
import com.goodow.realtime.server.service.SnapshotHandler;
import com.goodow.realtime.server.service.WeiXinHandler;

import com.google.inject.Inject;

import org.vertx.java.core.http.RouteMatcher;
import org.vertx.mods.web.WebServer;

public class RealtimeWebServer extends WebServer {
  @Inject private SaveHandler saveHandler;
  @Inject private SnapshotHandler snapshotHandler;
  @Inject private WeiXinHandler weiXinHandler;

  @Override
  protected RouteMatcher routeMatcher() {
    RouteMatcher routeMatcher = super.routeMatcher();

    // routeMatcher.get(Services.SAVE, saveHandler);
    // routeMatcher.get(Services.SNAPSHOT, snapshotHandler);

    routeMatcher.all("/weixin", weiXinHandler);

    return routeMatcher;
  }
}
