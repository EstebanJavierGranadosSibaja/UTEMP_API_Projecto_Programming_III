package org.una.programmingIII.UTEMP_Project.services.FileMetadatumServices;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;

import java.util.List;
import java.util.Optional;

public interface FileMetadatumService {
    List<FileMetadatumDTO> getAllFileMetadata();
    Optional<FileMetadatumDTO> getFileMetadatumById(Long id);
    FileMetadatumDTO createFileMetadatum(@Valid FileMetadatumDTO fileMetadatumDTO);
    Optional<FileMetadatumDTO> updateFileMetadatum(Long id, @Valid FileMetadatumDTO fileMetadatumDTO);
    void deleteFileMetadatum(Long id);
}
