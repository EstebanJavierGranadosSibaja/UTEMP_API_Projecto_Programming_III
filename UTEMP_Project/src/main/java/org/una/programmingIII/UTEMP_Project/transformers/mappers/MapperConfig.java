package org.una.programmingIII.UTEMP_Project.transformers.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig<E, D> {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
