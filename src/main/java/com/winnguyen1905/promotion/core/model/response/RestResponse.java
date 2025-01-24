package com.winnguyen1905.promotion.core.model.response;

import com.winnguyen1905.promotion.core.model.AbstractModel;

public record RestResponse<T>(Integer statusCode, String error, Object message, T data) implements AbstractModel {}
