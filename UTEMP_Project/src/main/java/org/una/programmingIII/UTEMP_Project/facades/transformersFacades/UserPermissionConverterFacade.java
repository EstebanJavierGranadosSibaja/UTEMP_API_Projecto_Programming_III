package org.una.programmingIII.UTEMP_Project.facades.transformersFacades;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
import org.una.programmingIII.UTEMP_Project.models.UserPermission;
import org.una.programmingIII.UTEMP_Project.transformers.converters.UserPermissionConverter;

import java.util.List;

@Component
@Converter(autoApply = false)
public class UserPermissionConverterFacade implements AttributeConverter<List<UserPermission>, String> {

    private final UserPermissionConverter userPermissionConverter = new UserPermissionConverter();

    @Override
    public String convertToDatabaseColumn(List<UserPermission> permissions) {
        return userPermissionConverter.convertToDatabaseColumn(permissions);
    }

    @Override
    public List<UserPermission> convertToEntityAttribute(String dbData) {
        return userPermissionConverter.convertToEntityAttribute(dbData);
    }
}
