package org.una.programmingIII.UTEMP_Project.controllers.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> data;
    private long totalElements;
    private int totalPages;
    private int pageNumber; // número de la página actual
    private int pageSize;   // tamaño de la página

    public PageResponse(Page<T> page) {
        this.data = page.getContent(); // Lista de elementos
        this.totalElements = page.getTotalElements(); // Total de elementos
        this.totalPages = page.getTotalPages(); // Total de páginas
        this.pageNumber = page.getNumber(); // Número de la página actual
        this.pageSize = page.getSize(); // Tamaño de la página
    }
}
