package org.una.programmingIII.UTEMP_Project.services.file;

import jakarta.validation.Valid;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.FileMetadatum;
import org.una.programmingIII.UTEMP_Project.repositories.FileMetadatumRepository;
import org.una.programmingIII.UTEMP_Project.repositories.SubmissionRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

@Service
@Transactional
public class FileServiceImplementation implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImplementation.class);
    private final FileMetadatumRepository fileMetadatumRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final GenericMapper<FileMetadatum, FileMetadatumDTO> fileMetadatumMapper;

    // Mapa para almacenar los fragmentos en memoria
    private final Map<Long, List<byte[]>> fileChunksMap = new HashMap<>();

    @Autowired
    public FileServiceImplementation(GenericMapperFactory mapperFactory, FileMetadatumRepository fileMetadatumRepository, SubmissionRepository submissionRepository, UserRepository userRepository) {
        this.fileMetadatumMapper = mapperFactory.createMapper(FileMetadatum.class, FileMetadatumDTO.class);
        this.fileMetadatumRepository = fileMetadatumRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    public void receiveFileChunk(FileMetadatumDTO fileChunkDTO) throws FileUploadException {
        Long fileId = fileChunkDTO.getId();
        fileChunksMap.putIfAbsent(fileId, new ArrayList<>());

        List<byte[]> chunks = fileChunksMap.get(fileId);

        // Validar que el chunkIndex es válido
        if (fileChunkDTO.getChunkIndex() < 0 || fileChunkDTO.getChunkIndex() >= fileChunkDTO.getTotalChunks()) {
            throw new IllegalArgumentException("Invalid chunk index: " + fileChunkDTO.getChunkIndex());
        }

        // Agregar el fragmento si es el siguiente en la secuencia
        if (fileChunkDTO.getChunkIndex() == chunks.size()) {
            chunks.add(fileChunkDTO.getFileChunk());
            logger.info("Chunk {} added for file ID: {}", fileChunkDTO.getChunkIndex(), fileId);
        } else {
            logger.warn("Received out-of-order chunk for file ID: {}, expected index: {}", fileId, chunks.size());
        }

        // Verificar si todos los fragmentos han sido recibidos
        if (chunks.size() == fileChunkDTO.getTotalChunks()) {
            finalizeFileUpload(fileId, fileChunkDTO);
        }
    }

    public void finalizeFileUpload(Long fileId, FileMetadatumDTO fileDTO) throws FileUploadException {
        List<byte[]> chunks = fileChunksMap.get(fileId);
        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalArgumentException("No chunks found for file ID: " + fileId);
        }

        String finalFilePath = String.format("path/to/final/%s<%d>.%s",
                fileDTO.getFileName(), fileId, fileDTO.getFileType());

        // Escribir los fragmentos en el archivo final
        try (FileOutputStream fos = new FileOutputStream(finalFilePath)) {
            for (byte[] chunk : chunks) {
                fos.write(chunk);
            }
            logger.info("File upload finalized for file ID: {}", fileId);
        } catch (IOException e) {
            logger.error("Error finalizing file upload: {}", e.getMessage());
            throw new FileUploadException("Error finalizing file upload", e);
        }

        // Crear y guardar la metadata del archivo
        FileMetadatum fileMetadatum = new FileMetadatum();
        fileMetadatum.setId(fileId);
        fileMetadatum.setFileName(String.format("%s<%d>.%s", fileDTO.getFileName(), fileId, fileDTO.getFileType()));
        fileMetadatum.setFileSize(chunks.stream().mapToLong(chunk -> chunk.length).sum());
        fileMetadatum.setStoragePath(finalFilePath);
        fileMetadatum.setFileType(fileDTO.getFileType()); // Ajustar según el tipo real

//         Aquí se asume que los IDs de Submission y User están disponibles en el DTO
//        if (fileDTO.getSubmissionID() != null) {
//            Submission submission = submissionRepository.findById(fileDTO.getSubmissionId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Submission not found for ID: " + fileDTO.getSubmissionId()));
//            fileMetadatum.setSubmission(submission);
//        }
//
//        if (fileDTO.getStudentId() != null) {
//            User student = userRepository.findById(fileDTO.getStudentId())
//                    .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + fileDTO.getStudentId()));
//            fileMetadatum.setStudent(student);
//        }

        fileMetadatumRepository.save(fileMetadatum);

        // Limpiar los fragmentos después de la carga
        fileChunksMap.remove(fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileMetadatumDTO> getAllFileMetadata() {
        return executeWithLogging(() -> fileMetadatumMapper.convertToDTOList(fileMetadatumRepository.findAll()),
                "Error fetching all file metadata");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FileMetadatumDTO> getFileMetadatumById(Long id) {
        return executeWithLogging(() -> {
            FileMetadatum fileMetadatum = getEntityById(id, fileMetadatumRepository, "FileMetadatum");
            return Optional.of(fileMetadatumMapper.convertToDTO(fileMetadatum));
        }, "Error fetching file metadatum by ID");
    }

    @Override
    @Transactional
    public FileMetadatumDTO createFileMetadatum(@Valid FileMetadatumDTO fileMetadatumDTO) {
        FileMetadatum fileMetadatum = fileMetadatumMapper.convertToEntity(fileMetadatumDTO);
        fileMetadatum.setSubmission(getEntityById(fileMetadatumDTO.getSubmission().getId(), submissionRepository, "Submission"));
        fileMetadatum.setStudent(getEntityById(fileMetadatumDTO.getStudent().getId(), userRepository, "User"));
        return executeWithLogging(() -> fileMetadatumMapper.convertToDTO(fileMetadatumRepository.save(fileMetadatum)),
                "Error creating file metadatum");
    }

    @Override
    @Transactional
    public Optional<FileMetadatumDTO> updateFileMetadatum(Long id, @Valid FileMetadatumDTO fileMetadatumDTO) {
        Optional<FileMetadatum> optionalFileMetadatum = fileMetadatumRepository.findById(id);
        FileMetadatum existingFileMetadatum = optionalFileMetadatum.orElseThrow(() -> new ResourceNotFoundException("FileMetadatum", id));

        updateFileMetadatumFields(existingFileMetadatum, fileMetadatumDTO);
        return executeWithLogging(() -> Optional.of(fileMetadatumMapper.convertToDTO(fileMetadatumRepository.save(existingFileMetadatum))),
                "Error updating file metadatum");
    }

    @Override
    @Transactional
    public void deleteFileMetadatum(Long id) {
        FileMetadatum fileMetadatum = getEntityById(id, fileMetadatumRepository, "FileMetadatum");
        executeWithLogging(() -> {
            fileMetadatumRepository.delete(fileMetadatum);
            return null;
        }, "Error deleting file metadatum");
    }

    // --------------- MÉTODOS AUXILIARES -----------------

    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return findEntityById(id, repository)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
    }

    private <T> Optional<T> findEntityById(Long id, JpaRepository<T, Long> repository) {
        return repository.findById(id);
    }

    private void updateFileMetadatumFields(FileMetadatum existingFileMetadatum, FileMetadatumDTO fileMetadatumDTO) {
        existingFileMetadatum.setFileName(fileMetadatumDTO.getFileName());
        existingFileMetadatum.setFileSize(fileMetadatumDTO.getFileSize());
        existingFileMetadatum.setFileType(fileMetadatumDTO.getFileType());
        existingFileMetadatum.setStoragePath(fileMetadatumDTO.getStoragePath());
        existingFileMetadatum.setLastUpdate(LocalDateTime.now());
    }

    private <T> T executeWithLogging(Supplier<T> action, String errorMessage) {
        try {
            return action.get();
        } catch (Exception e) {
            logger.error("{}: {}", errorMessage, e.getMessage());
            throw new ServiceException(errorMessage, e);
        }
    }
}
