package org.una.programmingIII.UTEMP_Project.services.file;

import jakarta.validation.Valid;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;

import java.util.List;
import java.util.Optional;

public interface FileService {
    Page<FileMetadatumDTO> getAllFileMetadata(int page, int size);

    Optional<FileMetadatumDTO> getFileMetadatumById(Long id);

    FileMetadatumDTO createFileMetadatum(@Valid FileMetadatumDTO fileMetadatumDTO);

    Optional<FileMetadatumDTO> updateFileMetadatum(Long id, @Valid FileMetadatumDTO fileMetadatumDTO);

    void deleteFileMetadatum(Long id);

    void finalizeFileUpload(Long fileId, FileMetadatumDTO fileDTO) throws FileUploadException;

    void receiveFileChunk(FileMetadatumDTO fileChunkDTO) throws FileUploadException;
}