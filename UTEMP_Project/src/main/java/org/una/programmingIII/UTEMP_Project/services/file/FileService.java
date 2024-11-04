package org.una.programmingIII.UTEMP_Project.services.file;

import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface FileService {

    Optional<FileMetadatumDTO> getFileMetadatumById(Long id);

    FileMetadatumDTO updateFileMetadatum(Long id, FileMetadatumDTO fileChunkDTO) throws IOException;

    void receiveFileChunk(FileMetadatumDTO fileChunkDTO) throws IOException;

    void deleteFileMetadatum(Long id);

    List<FileMetadatumDTO> downloadFileInChunks(Long fileId) throws IOException;
}