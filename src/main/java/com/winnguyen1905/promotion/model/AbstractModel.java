package com.winnguyen1905.promotion.model;

import java.io.Serializable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

// @JsonInclude(value = Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface AbstractModel extends Serializable {
  public static final long serialVersionUID = 7213600440729202783L;
}
