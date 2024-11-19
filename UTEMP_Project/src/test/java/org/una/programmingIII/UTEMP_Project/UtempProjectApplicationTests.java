package org.una.programmingIII.UTEMP_Project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UtempProjectApplicationTests {

    @Test
    void contextLoads() {
        // Simplemente asegura que el contexto se carga sin errores
    }

    @Test
    void testApplicationStarts() {
        UtempProjectApplication.main(new String[]{});
        // Puedes agregar más validaciones aquí si es necesario
    }

    // Añadir más tests para verificar componentes específicos si es necesario
}
