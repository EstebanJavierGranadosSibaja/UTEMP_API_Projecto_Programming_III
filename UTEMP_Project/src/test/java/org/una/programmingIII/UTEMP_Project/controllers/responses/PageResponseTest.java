package org.una.programmingIII.UTEMP_Project.controllers.responses;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.una.programmingIII.UTEMP_Project.utils.PageDTO;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PageResponseTest {

    @Test
    void testDefaultConstructor() {
        PageResponse<Object> pageResponse = new PageResponse<>();

        assertNull(pageResponse.getData(), "El campo 'data' debe ser nulo por defecto");
        assertEquals(0, pageResponse.getTotalElements(), "El campo 'totalElements' debe ser 0 por defecto");
        assertEquals(0, pageResponse.getTotalPages(), "El campo 'totalPages' debe ser 0 por defecto");
        assertEquals(0, pageResponse.getPageNumber(), "El campo 'pageNumber' debe ser 0 por defecto");
        assertEquals(0, pageResponse.getPageSize(), "El campo 'pageSize' debe ser 0 por defecto");
    }

    @Test
    void testConstructorWithPageDTO() {
        PageDTO<String> pageDTO = mock(PageDTO.class);
        when(pageDTO.getContent()).thenReturn(Arrays.asList("Item1", "Item2"));
        when(pageDTO.getTotalElements()).thenReturn(5L);
        when(pageDTO.getTotalPages()).thenReturn(2);
        when(pageDTO.getNumber()).thenReturn(1);
        when(pageDTO.getSize()).thenReturn(2);

        PageResponse<String> pageResponse = new PageResponse<>(pageDTO);

        assertEquals(Arrays.asList("Item1", "Item2"), pageResponse.getData(), "El campo 'data' debe ser la lista contenida en el PageDTO");
        assertEquals(5L, pageResponse.getTotalElements(), "El campo 'totalElements' debe coincidir con el valor del PageDTO");
        assertEquals(2, pageResponse.getTotalPages(), "El campo 'totalPages' debe coincidir con el valor del PageDTO");
        assertEquals(1, pageResponse.getPageNumber(), "El campo 'pageNumber' debe coincidir con el valor del PageDTO");
        assertEquals(2, pageResponse.getPageSize(), "El campo 'pageSize' debe coincidir con el valor del PageDTO");
    }

    @Test
    void testSettersAndGetters() {
        PageResponse<String> pageResponse = new PageResponse<>();

        pageResponse.setData(Arrays.asList("Test"));
        pageResponse.setTotalElements(100L);
        pageResponse.setTotalPages(10);
        pageResponse.setPageNumber(1);
        pageResponse.setPageSize(10);

        assertEquals(Arrays.asList("Test"), pageResponse.getData(), "El campo 'data' debe ser igual a la lista proporcionada");
        assertEquals(100L, pageResponse.getTotalElements(), "El campo 'totalElements' debe ser igual al valor proporcionado");
        assertEquals(10, pageResponse.getTotalPages(), "El campo 'totalPages' debe ser igual al valor proporcionado");
        assertEquals(1, pageResponse.getPageNumber(), "El campo 'pageNumber' debe ser igual al valor proporcionado");
        assertEquals(10, pageResponse.getPageSize(), "El campo 'pageSize' debe ser igual al valor proporcionado");
    }

    @Test
    void testToString() {
        PageResponse<String> pageResponse = new PageResponse<>(Arrays.asList("Item1", "Item2"), 5L, 2, 1, 2);

        String expectedString = "PageResponse(data=[Item1, Item2], totalElements=5, totalPages=2, pageNumber=1, pageSize=2)";
        assertEquals(expectedString, pageResponse.toString(), "El método toString debe devolver la representación correcta del objeto");
    }
}
