package org.una.programmingIII.UTEMP_Project.services.FileMetadatumServices;

import jakarta.validation.Valid;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class FileMetadatumServiceImplementation implements FileMetadatumService {

    private static final Logger logger = LoggerFactory.getLogger(FileMetadatumServiceImplementation.class);

    @Autowired
    private FileMetadatumRepository fileMetadatumRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    private final GenericMapper<FileMetadatum, FileMetadatumDTO> fileMetadatumMapper;

    @Autowired
    public FileMetadatumServiceImplementation(GenericMapperFactory mapperFactory) {
        this.fileMetadatumMapper = mapperFactory.createMapper(FileMetadatum.class, FileMetadatumDTO.class);
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
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
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
