package org.una.programmingIII.UTEMP_Project.transformers.mappers;

import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;

public class GenericMapperImplementation<E, D> implements GenericMapper<E, D> {

    private final Class<E> entityClass;
    private final Class<D> dtoClass;
    private final ModelMapper modelMapper;

    public GenericMapperImplementation(Class<E> entityClass, Class<D> dtoClass, ModelMapper modelMapper) {
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
        this.modelMapper = modelMapper;
    }

    @Override
    public D convertToDTO(E entity) {
        return modelMapper.map(entity, dtoClass);
    }

    @Override
    public E convertToEntity(D dto) {
        return modelMapper.map(dto, entityClass);
    }

    @Override
    public List<D> convertToDTOList(List<E> entities) {
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<E> convertToEntityList(List<D> dtos) {
        return dtos.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
}
