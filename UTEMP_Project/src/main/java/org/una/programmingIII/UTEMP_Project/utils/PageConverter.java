package org.una.programmingIII.UTEMP_Project.utils;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageConverter {
    public static <T, U> PageDTO<U> convertPageToDTO(Page<T> page, Function<T, U> converter) {
        List<U> content = page.getContent().stream()
                .map(converter)
                .collect(Collectors.toList());

        return new PageDTO<>(
                content,
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }
}
