package org.una.programmingIII.UTEMP_Project.services.file;

import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;

import java.io.IOException;
import java.util.List;

public interface FileService {

    FileMetadatumDTO createNewFileMetadata(FileMetadatumDTO fileDTO);

    FileMetadatumDTO getFileMetadatumById(Long id);

//    void uploadFileChunk(FileMetadatumDTO fileChunkDTO) throws IOException;

//    List<FileMetadatumDTO> downloadFileInChunks(Long fileId) throws IOException;

    boolean deleteFile(Long fileId) throws IOException;

     void receiveFileChunk(FileMetadatumDTO fileDTO);
}