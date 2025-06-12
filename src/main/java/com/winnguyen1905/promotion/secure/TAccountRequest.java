package com.winnguyen1905.promotion.secure;

import java.util.UUID;

import com.winnguyen1905.promotion.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record TAccountRequest(
    UUID id,
    String username,
    AccountType accountType,
    UUID socketClientId) implements AbstractModel {
  @Builder
  public TAccountRequest(
      UUID id,
      String username,
      AccountType accountType,
      UUID socketClientId) {
    this.id = id;
    this.username = username;
    this.accountType = accountType;
    this.socketClientId = socketClientId;
  }
}
