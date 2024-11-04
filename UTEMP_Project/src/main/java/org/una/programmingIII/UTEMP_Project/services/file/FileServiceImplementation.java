package org.una.programmingIII.UTEMP_Project.services.file;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.FileNotFoundDataBaseException;
import org.una.programmingIII.UTEMP_Project.models.FileMetadatum;
import org.una.programmingIII.UTEMP_Project.models.Submission;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.repositories.FileMetadatumRepository;
import org.una.programmingIII.UTEMP_Project.repositories.SubmissionRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@Transactional
public class FileServiceImplementation implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImplementation.class);
    private final FileMetadatumRepository fileMetadatumRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final String fileBasePath = "path/to/final";

    private final Map<Long, List<byte[]>> fileChunksMap = new HashMap<>();

    @Autowired
    public FileServiceImplementation(FileMetadatumRepository fileMetadatumRepository,
                                     SubmissionRepository submissionRepository,
                                     UserRepository userRepository) {
        this.fileMetadatumRepository = fileMetadatumRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    // Métodos de Consulta

    @Transactional(readOnly = true)
    public Optional<FileMetadatumDTO> getFileMetadatumById(Long id) {
        FileMetadatum file = fileMetadatumRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundDataBaseException(id));

        return Optional.of(FileMetadatumDTO.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .storagePath(file.getStoragePath())
                .lastUpdate(file.getLastUpdate())
                .createdAt(file.getCreatedAt())
                .build());
    }

    // Métodos de Recepción y Finalización

    public void receiveFileChunk(FileMetadatumDTO fileChunkDTO) throws IOException {
        Long fileId = fileChunkDTO.getId();
        fileChunksMap.putIfAbsent(fileId, new ArrayList<>());

        List<byte[]> chunks = fileChunksMap.get(fileId);
        validateChunkIndex(fileChunkDTO, chunks.size());

        if (fileChunkDTO.getChunkIndex() == chunks.size()) {
            chunks.add(fileChunkDTO.getFileChunk());
            logger.info("Chunk {} added for file ID: {}", fileChunkDTO.getChunkIndex(), fileId);
        } else {
            logger.warn("Received out-of-order chunk for file ID: {}, expected index: {}", fileId, chunks.size());
        }

        if (chunks.size() == fileChunkDTO.getTotalChunks()) {
            finalizeFileUpload(fileId, fileChunkDTO);
        }
    }

    public void finalizeFileUpload(Long fileId, FileMetadatumDTO fileDTO) throws IOException {
        List<byte[]> chunks = fileChunksMap.get(fileId);
        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalArgumentException("No chunks found for file ID: " + fileId);
        }

        validateStoragePath(fileBasePath);
        String finalFilePath = String.format("%s/%s<%d>.%s", fileBasePath, fileDTO.getFileName(), fileId, fileDTO.getFileType());

        try (FileOutputStream fos = new FileOutputStream(finalFilePath)) {
            for (byte[] chunk : chunks) {
                fos.write(chunk);
            }
            logger.info("File upload finalized for file ID: {}", fileId);
        } catch (IOException e) {
            logger.error("Error finalizing file upload: {}", e.getMessage());
            throw new FileUploadException("Error finalizing file upload", e);
        }

        saveFileMetadata(fileDTO, finalFilePath);
        fileChunksMap.remove(fileId);
    }

    // Métodos de Actualización

    @Override
    @Transactional
    public FileMetadatumDTO updateFileMetadatum(Long id, FileMetadatumDTO fileChunkDTO) throws IOException {
        if (!fileMetadatumRepository.existsById(id)) {
            throw new FileNotFoundDataBaseException(id);
        }

        deleteFileMetadatum(id);
        receiveFileChunk(fileChunkDTO);
        return getFileMetadatumById(id).orElse(null);
    }

    // Métodos de Eliminación

    @Transactional
    public void deleteFileMetadatum(Long id) {
        FileMetadatum fileMetadatum = fileMetadatumRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundDataBaseException(id));

        File file = new File(fileMetadatum.getStoragePath());
        if (file.exists() && !file.delete()) {
            logger.warn("Failed to delete file: {}", file.getAbsolutePath());
        } else {
            logger.info("Deleted file: {}", file.getAbsolutePath());
        }
        fileMetadatumRepository.deleteById(id);
    }

    // Método para descargar archivos en fragmentos

    public List<FileMetadatumDTO> downloadFileInChunks(Long fileId) throws IOException {
        FileMetadatum fileMetadatum = fileMetadatumRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundDataBaseException(fileId));

        File file = new File(fileMetadatum.getStoragePath());
        List<FileMetadatumDTO> chunks = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[512 * 1024]; // 512KB
            int bytesRead;
            int chunkIndex = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] fileChunk = Arrays.copyOf(buffer, bytesRead);
                FileMetadatumDTO chunkDTO = FileMetadatumDTO.builder()
                        .id(fileId)
                        .fileName(fileMetadatum.getFileName())
                        .fileType(fileMetadatum.getFileType())
                        .fileChunk(fileChunk)
                        .chunkIndex(chunkIndex++)
                        .totalChunks((int) Math.ceil((double) fileMetadatum.getFileSize() / (512 * 1024)))
                        .build();
                chunks.add(chunkDTO);
            }
        }

        return chunks;
    }

    // Métodos de Validación

    private void validateChunkIndex(FileMetadatumDTO fileChunkDTO, int currentSize) {
        if (fileChunkDTO.getChunkIndex() < 0 || fileChunkDTO.getChunkIndex() >= fileChunkDTO.getTotalChunks()) {
            throw new IllegalArgumentException("Invalid chunk index: " + fileChunkDTO.getChunkIndex());
        }
    }

    private void saveFileMetadata(FileMetadatumDTO fileDTO, String finalFilePath) {
        List<byte[]> chunks = fileChunksMap.get(fileDTO.getId());
        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalArgumentException("No chunks found for file ID: " + fileDTO.getId());
        }

        FileMetadatum fileMetadatum = new FileMetadatum();
        fileMetadatum.setFileName(String.format("%s<%d>.%s", fileDTO.getFileName(), fileDTO.getId(), fileDTO.getFileType()));

        long totalSize = chunks.stream().mapToLong(chunk -> chunk.length).sum();
        fileMetadatum.setFileSize(totalSize);

        fileMetadatum.setStoragePath(finalFilePath);
        fileMetadatum.setFileType(fileDTO.getFileType());

        Optional<User> userOpt = userRepository.findById(fileDTO.getStudent().getId());
        Optional<Submission> submissionOpt = submissionRepository.findById(fileDTO.getSubmission().getId());

        if (userOpt.isEmpty() || submissionOpt.isEmpty()) {
            throw new IllegalArgumentException("User or Submission not found");
        }

        fileMetadatum.setStudent(userOpt.get());
        fileMetadatum.setSubmission(submissionOpt.get());
        fileMetadatumRepository.save(fileMetadatum);
    }


    private void validateStoragePath(String path) throws IOException {
        File directory = new File(path);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create storage directory: " + path);
        }
    }
}