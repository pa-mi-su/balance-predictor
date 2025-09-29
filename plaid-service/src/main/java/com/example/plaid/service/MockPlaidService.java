package com.example.plaid.service;

import com.example.plaid.model.AccountBalance;
import org.springframework.stereotype.Service;

@Service
public class MockPlaidService {
  private volatile double mockBalance = 2500.00;
  public AccountBalance getCurrentBalance(Long userId) { return new AccountBalance(mockBalance); }
  public void setMockBalance(double b) { this.mockBalance = b; }
}
