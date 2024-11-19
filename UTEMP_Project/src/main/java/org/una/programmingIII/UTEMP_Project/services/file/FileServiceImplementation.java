package org.una.programmingIII.UTEMP_Project.services.file;

import net.loomchild.segment.util.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.UserNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.FileMetadatum;
import org.una.programmingIII.UTEMP_Project.models.Submission;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.repositories.FileMetadatumRepository;
import org.una.programmingIII.UTEMP_Project.repositories.SubmissionRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileServiceImplementation implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImplementation.class);
    private final FileMetadatumRepository fileMetadatumRepository;
    private final UserRepository userRepository;
    private static final String FILE_BASE_PATH = "users/files"; // Ruta base de archivos
    private final SubmissionRepository submissionRepository;
    private static final Long CHUNK_SIZE = 512L;

    @Autowired
    public FileServiceImplementation(FileMetadatumRepository fileMetadatumRepository,
                                     SubmissionRepository submissionRepository,
                                     UserRepository userRepository) {
        this.fileMetadatumRepository = fileMetadatumRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    @Transactional
    public FileMetadatumDTO createNewFileMetadata(FileMetadatumDTO fileDTO) {
        User user = userRepository.findById(fileDTO.getStudent().getId())
                .orElseThrow(() -> new UserNotFoundException("Usuario con ID " + fileDTO.getStudent().getId() + " no encontrado"));
        Submission submission = submissionRepository.findById(fileDTO.getSubmission().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission con ID " + fileDTO.getSubmission().getId() + " no encontrado"));

        FileMetadatum newFile = generateMetadata(fileDTO, submission, user);
        logger.info("Metadatos del archivo creados para archivo: {}", newFile.getFileName());
        return metadataToDto(fileMetadatumRepository.save(newFile));
    }

    @Transactional
    public FileMetadatumDTO updateMetadata(FileMetadatumDTO fileDTO) {
        // Verificar la existencia del usuario
        User user = userRepository.findById(fileDTO.getStudent().getId())
                .orElseThrow(() -> new UserNotFoundException("Usuario con ID " + fileDTO.getStudent().getId() + " no encontrado"));

        // Verificar la existencia de la submission
        Submission submission = submissionRepository.findById(fileDTO.getSubmission().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission con ID " + fileDTO.getSubmission().getId() + " no encontrado"));

        // Buscar el FileMetadatum existente en la base de datos
        FileMetadatum existingFile = fileMetadatumRepository.findById(fileDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Archivo con ID " + fileDTO.getId() + " no encontrado"));

        // Actualizar los campos del archivo con los nuevos datos
        existingFile.setFileName(fileDTO.getFileName());
        existingFile.setFileType(fileDTO.getFileType());
        existingFile.setFileSize(fileDTO.getFileSize());
        existingFile.setStoragePath(fileDTO.getStoragePath());  // El almacenamiento podría cambiar si el archivo se mueve o actualiza.

        List<FileMetadatum> list = new ArrayList<>();
        list.add(existingFile);
        submission.setFileName(existingFile.getFileName());
        submission.setFileMetadata(list);
        submissionRepository.save(submission);

        // Actualizar la referencia de submission y student
        existingFile.setSubmission(submission);
        existingFile.setStudent(user);

        logger.info("Metadatos del archivo actualizados para archivo: {}", existingFile.getFileName());

        // Guardar y retornar el DTO
        return metadataToDto(fileMetadatumRepository.save(existingFile));
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadatumDTO getFileMetadatumById(Long id) {
        return fileMetadatumRepository.findById(id)
                .map(this::metadataToDto)
                .orElseThrow(() -> new RuntimeException("Archivo con ID " + id + " no encontrado"));
    }

    @Override
    @Transactional
    public boolean deleteFile(Long id) {
        FileMetadatum fileMetadatum = fileMetadatumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo con ID " + id + " no encontrado para eliminar."));
        File file = new File(fileMetadatum.getStoragePath());

        if (file.exists()) {
            if (file.delete()) {
                logger.info("Archivo físico eliminado: {}", file.getAbsolutePath());
                fileMetadatumRepository.delete(fileMetadatum);
                logger.info("Metadatos eliminados.");
                return true;
            } else {
                logger.error("Error al eliminar archivo: {}", file.getAbsolutePath());
                return false;
            }
        } else {
            logger.warn("El archivo no existe: {}", file.getAbsolutePath());
            fileMetadatumRepository.delete(fileMetadatum);
            return true;
        }
    }

    @Override
    @Transactional
    public void receiveFileChunk(FileMetadatumDTO fileDTO) {
        logger.info("Recibiendo fragmento {} de {} para el archivo con ID: {}", fileDTO.getChunkIndex() + 1, fileDTO.getTotalChunks(), fileDTO.getId());

        String uniqueFileName = fileDTO.getId() + "_" + fileDTO.getFileName();
        String filePath = FILE_BASE_PATH + "/" + uniqueFileName;

        if (fileDTO.getChunkIndex() == 0) {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    Files.createFile(file.toPath());
                    logger.info("Archivo creado: {}", filePath);
                }
            } catch (IOException e) {
                logger.error("Error al crear archivo: {}", e.getMessage());
            }
        }

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            raf.seek(fileDTO.getChunkIndex() * CHUNK_SIZE);
            raf.write(fileDTO.getFileChunk());
            logger.info("Fragmento {} guardado", fileDTO.getChunkIndex());
        } catch (IOException e) {
            logger.error("Error al guardar fragmento {}: {}", fileDTO.getChunkIndex(), e.getMessage());
        }

        if (fileDTO.getChunkIndex() + 1 == fileDTO.getTotalChunks()) {
            finalizeUpload(fileDTO);
        }
    }

    @Transactional
    protected void finalizeUpload(FileMetadatumDTO fileDTO) {
        logger.info("Finalizando carga del archivo: {}", fileDTO.getFileName());


        String filePath = FILE_BASE_PATH + "/" + fileDTO.getId() + "_" + fileDTO.getFileName();
        try {
            File file = new File(filePath);
            if (file.exists()) {
                fileDTO.setStoragePath(filePath);
                updateMetadata(fileDTO);
                logger.info("Archivo finalizado y metadatos actualizados.");
            } else {
                logger.error("El archivo no existe al finalizar carga.");
            }
        } catch (Exception e) {
            logger.error("Error al finalizar carga: {}", e.getMessage());
        }
    }



    @PostConstruct
    private void initializeStoragePath() throws IOException {
        Path storagePath = Paths.get(FILE_BASE_PATH);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
            logger.info("Ruta de almacenamiento creada: {}", FILE_BASE_PATH);
        }
    }

    private FileMetadatumDTO metadataToDto(FileMetadatum fileMetadatum) {
        return FileMetadatumDTO.builder()
                .id(fileMetadatum.getId())
                .fileName(fileMetadatum.getFileName())
                .fileType(fileMetadatum.getFileType())
                .fileSize(fileMetadatum.getFileSize())
                .storagePath(fileMetadatum.getStoragePath())
                .build();
    }

    private FileMetadatum generateMetadata(FileMetadatumDTO fileDTO, Submission submission, User user) {
        return FileMetadatum.builder()
                .submission(submission)
                .student(user)
                .fileName(fileDTO.getFileName() != null ? fileDTO.getFileName() : "desconocido")
                .fileType(fileDTO.getFileType() != null ? fileDTO.getFileType() : "desconocido")
                .fileSize(fileDTO.getFileSize() != null ? fileDTO.getFileSize() : 0L)
                .storagePath(FILE_BASE_PATH + "/" + fileDTO.getId() + "_" + fileDTO.getFileName())
                .build();
    }
}


// private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
//        return findEntityById(id, repository)
//                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
//    }
//
//    private <T> Optional<T> findEntityById(Long id, JpaRepository<T, Long> repository) {
//        return repository.findById(id);
//    }