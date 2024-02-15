package com.cheng.linegroup.enums.converter;

import com.cheng.linegroup.enums.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * @author cheng
 * @since 2024/2/14 00:05
 **/
@Converter
public class GenderConverter implements AttributeConverter<Gender, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Gender gender) {
        return gender.ordinal();
    }

    @Override
    public Gender convertToEntityAttribute(Integer integer) {
        return null;
    }
}
