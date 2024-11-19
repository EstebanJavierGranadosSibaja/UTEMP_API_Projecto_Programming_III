package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.una.programmingIII.UTEMP_Project.models.FileMetadatum;

import java.util.List;

public interface FileMetadatumRepository extends JpaRepository<FileMetadatum, Long> {

    /**
     * Busca metadatos de archivos que contengan una parte del nombre y coincidan con un tipo de archivo específico.
     *
     * @param fileName parte del nombre del archivo a buscar
     * @param fileType el tipo de archivo a buscar
     * @return una lista de metadatos de archivos que coinciden con los criterios de búsqueda
     */
    List<FileMetadatum> findByFileNameContainingAndFileType(String fileName, String fileType);
}

