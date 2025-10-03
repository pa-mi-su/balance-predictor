package com.example.balance.service;

import com.example.balance.model.AccountBalance;
import com.example.balance.model.PendingEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class BalanceCalcService {

  private final WebClient ledgerClient;
  private final WebClient plaidClient;

  public BalanceCalcService(WebClient.Builder lb) {
    // use Eureka logical service names (SPRING_APPLICATION_NAME)
    this.ledgerClient = lb.baseUrl("http://LEDGER-SERVICE").build();
    this.plaidClient  = lb.baseUrl("http://PLAID-SERVICE").build();
  }

  public Mono<Double> projectedBalance(Long userId) {
    Mono<AccountBalance> base = plaidClient.get()
            .uri(uri -> uri.path("/api/plaid/balance")
                    .queryParam("userId", userId)
                    .build())
            .retrieve()
            .bodyToMono(AccountBalance.class);

    Mono<List<PendingEvent>> events = ledgerClient.get()
            .uri(uri -> uri.path("/api/ledger/events")
                    .queryParam("userId", userId)
                    .build())
            .retrieve()
            .bodyToFlux(PendingEvent.class)
            .collectList();

    return Mono.zip(base, events).map(tuple -> {
      double current = tuple.getT1().currentBalance();
      double pendingSum = tuple.getT2()
              .stream()
              .mapToDouble(PendingEvent::amount)
              .sum();
      return current + pendingSum;
    });
  }
}
