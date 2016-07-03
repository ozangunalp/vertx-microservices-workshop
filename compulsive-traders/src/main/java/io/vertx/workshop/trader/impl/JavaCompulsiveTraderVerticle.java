package io.vertx.workshop.trader.impl;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.MessageSource;
import io.vertx.workshop.common.MicroServiceVerticle;
import io.vertx.workshop.portfolio.PortfolioService;

/**
 * A compulsive trader...
 */
public class JavaCompulsiveTraderVerticle extends MicroServiceVerticle {

  @Override
  public void start(Future<Void> future) {
    super.start();

    //TODO
    //----
    String company = TraderUtils.pickACompany();
    int numberOfShares = TraderUtils.pickANumber();
    System.out.println("Java compulsive trader configured for company " + company + " and shares: " + numberOfShares);
    Future<MessageConsumer<JsonObject>> messageConsumerFuture = Future.future();
    Future<PortfolioService> portfolioServiceFuture = Future.future();

    MessageSource.getConsumer(discovery, new JsonObject().put("name", "market-data"), messageConsumerFuture.completer());
    EventBusService.getProxy(discovery, PortfolioService.class, portfolioServiceFuture.completer());
    CompositeFuture.all(messageConsumerFuture, portfolioServiceFuture).setHandler(ar -> {
       if (ar.failed()) {
         future.fail(ar.cause());
       } else {
         MessageConsumer<JsonObject> consumer = messageConsumerFuture.result();
         PortfolioService portfolioService = portfolioServiceFuture.result();
         consumer.handler(message -> {
           JsonObject body = message.body();
           TraderUtils.dumbTradingLogic(company,numberOfShares,portfolioService,body);
         });
         future.complete();
       }
    });
    // ----
  }


}
