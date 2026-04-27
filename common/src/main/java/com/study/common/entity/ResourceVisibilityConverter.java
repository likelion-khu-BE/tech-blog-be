package com.study.common.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ResourceVisibilityConverter
    implements AttributeConverter<ResourceVisibility, String> {

  @Override
  public String convertToDatabaseColumn(ResourceVisibility attribute) {
    return attribute == null ? null : attribute.toDbValue();
  }

  @Override
  public ResourceVisibility convertToEntityAttribute(String dbData) {
    return dbData == null ? null : ResourceVisibility.fromDbValue(dbData);
  }
}
