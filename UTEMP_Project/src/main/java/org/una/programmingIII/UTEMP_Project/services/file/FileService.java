package org.una.programmingIII.UTEMP_Project.services.file;

import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface FileService {

    // Métodos de Consulta
    Optional<FileMetadatumDTO> getFileMetadatumById(Long id);

    // Métodos de Actualización
    FileMetadatumDTO updateFileMetadatum(Long id, FileMetadatumDTO fileChunkDTO) throws IOException;

    // Métodos de Recepción y Finalización
    void receiveFileChunk(FileMetadatumDTO fileChunkDTO) throws IOException;

    // Métodos de Eliminación
    void deleteFileMetadatum(Long id);

    public List<FileMetadatumDTO> downloadFileInChunks(Long fileId) throws IOException;

}
//    void finalizeFileUpload(Long fileId, FileMetadatumDTO fileDTO) throws IOException;

// Métodos de Validación
//    void validateStoragePath(String path) throws IOException;