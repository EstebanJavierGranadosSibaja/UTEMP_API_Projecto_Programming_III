package org.una.programmingIII.UTEMP_Project.transformers.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.una.programmingIII.UTEMP_Project.models.UserPermission;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public class UserPermissionConverter implements AttributeConverter<List<UserPermission>, String> {

    public UserPermissionConverter() {}

    public String convertToDatabaseColumn(List<UserPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "";
        }
        return permissions.stream()
                .map(UserPermission::name)
                .collect(Collectors.joining(","));
    }

    public List<UserPermission> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(dbData.split(","))
                .map(UserPermission::valueOf)
                .collect(Collectors.toList());
    }
}
