package org.una.programmingIII.UTEMP_Project.transformers.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericMapperFactory {

    @Autowired
    private ModelMapper modelMapper;

    public <E, D> GenericMapper<E, D> createMapper(Class<E> entityClass, Class<D> dtoClass) {
        return new GenericMapperImplementation<>(entityClass, dtoClass, modelMapper);
    }
}
