package com.banking.system.transaction.infraestructure.adapter.out.persistence.converter;

import com.banking.system.common.domain.MoneyCurrency;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter to store {@link MoneyCurrency} as a 3-letter ISO-4217 code (VARCHAR).
 */
@Converter(autoApply = false)
public class MoneyCurrencyJpaConverter implements AttributeConverter<MoneyCurrency, String> {

    @Override
    public String convertToDatabaseColumn(MoneyCurrency attribute) {
        return attribute == null ? null : attribute.code();
    }

    @Override
    public MoneyCurrency convertToEntityAttribute(String dbData) {
        return dbData == null ? null : MoneyCurrency.ofCode(dbData);
    }
}

