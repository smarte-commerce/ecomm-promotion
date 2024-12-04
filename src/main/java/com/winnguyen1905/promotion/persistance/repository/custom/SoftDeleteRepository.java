package com.winnguyen1905.promotion.persistance.repository.custom;

import java.util.List;

import com.winnguyen1905.promotion.persistance.entity.EBase;

public interface SoftDeleteRepository<T extends EBase, UUID> {
  void softDeleteOne(T entity);

  void softDeleteMany(List<T> entities);
}
