package com.winnguyen1905.promotion.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Permission {
  private String name;

  private String code;

  private String apiPath;

  private String method;

  private String module;
}
