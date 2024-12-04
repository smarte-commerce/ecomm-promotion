package com.winnguyen1905.promotion.config;

import com.winnguyen1905.promotion.core.model.BaseObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Permission extends BaseObject<Permission> {
  private String name;

  private String code;

  private String apiPath;

  private String method;

  private String module;
}
