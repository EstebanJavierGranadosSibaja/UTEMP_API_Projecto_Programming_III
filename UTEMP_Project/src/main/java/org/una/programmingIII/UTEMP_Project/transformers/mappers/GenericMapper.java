package org.una.programmingIII.UTEMP_Project.transformers.mappers;

import java.util.List;

public interface GenericMapper<E, D> {
    D convertToDTO(E entity);
    E convertToEntity(D dto);
    List<D> convertToDTOList(List<E> entities);
    List<E> convertToEntityList(List<D> dtos);
}
