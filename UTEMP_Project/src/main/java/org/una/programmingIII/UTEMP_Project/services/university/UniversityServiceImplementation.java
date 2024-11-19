package org.una.programmingIII.UTEMP_Project.services.university;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.dtos.UniversityDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Faculty;
import org.una.programmingIII.UTEMP_Project.models.University;
import org.una.programmingIII.UTEMP_Project.repositories.FacultyRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UniversityRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class UniversityServiceImplementation implements UniversityService {

    private static final Logger logger = LoggerFactory.getLogger(UniversityServiceImplementation.class);

    private final UniversityRepository universityRepository;
    private final FacultyRepository facultyRepository;
    private final GenericMapper<University, UniversityDTO> universityMapper;
    private final GenericMapper<Faculty, FacultyDTO> facultyMapper;

    @Autowired
    public UniversityServiceImplementation(
            UniversityRepository universityRepository,
            FacultyRepository facultyRepository,
            GenericMapperFactory mapperFactory) {

        this.universityRepository = universityRepository;
        this.facultyRepository = facultyRepository;
        this.universityMapper = mapperFactory.createMapper(University.class, UniversityDTO.class);
        this.facultyMapper = mapperFactory.createMapper(Faculty.class, FacultyDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UniversityDTO> getAllUniversities(Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<University> universityPage = universityRepository.findAll(pageable);
                return universityPage.map(universityMapper::convertToDTO);
            }, "Error fetching all universities");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching all universities: {}", e.getMessage());
            throw new InvalidDataException("Error fetching universities from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching all universities: {}", e.getMessage());
            throw new InvalidDataException("Error fetching all universities");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UniversityDTO> getUniversityById(Long id) {
        try {
            return executeWithLogging(() -> {
                University university = getEntityById(id, universityRepository, "University");
                return Optional.of(universityMapper.convertToDTO(university));
            }, "Error fetching university by ID: " + id);
        } catch (ResourceNotFoundException e) {
            logger.warn("University not found: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error fetching university by ID {}: {}", id, e.getMessage());
            throw new ServiceException("Error fetching university", e);
        }
    }

    @Override
    @Transactional
    public UniversityDTO createUniversity(@Valid UniversityDTO universityDTO) {
        try {
            University university = universityMapper.convertToEntity(universityDTO);
            University savedUniversity = universityRepository.save(university);
            return universityMapper.convertToDTO(savedUniversity);
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while creating university: {}", e.getMessage());
            throw new InvalidDataException("Error: University data is invalid or already exists.");
        } catch (Exception e) {
            logger.error("Error creating university: {}", e.getMessage());
            throw new ServiceException("Error creating university", e);
        }
    }

    @Override
    @Transactional
    public Optional<UniversityDTO> updateUniversity(Long id, @Valid UniversityDTO universityDTO) {
        try {
            Optional<University> optionalUniversity = universityRepository.findById(id);
            University existingUniversity = optionalUniversity.orElseThrow(() ->
                    new ResourceNotFoundException("University", id)
            );
            updateUniversityFields(existingUniversity, universityDTO);
            University savedUniversity = universityRepository.save(existingUniversity);
            return Optional.of(universityMapper.convertToDTO(savedUniversity));
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while updating university: {}", e.getMessage());
            throw new InvalidDataException("Error: University data is invalid or violates constraints.");
        } catch (Exception e) {
            logger.error("Error updating university: {}", e.getMessage());
            throw new ServiceException("Error updating university", e);
        }
    }

    @Override
    @Transactional
    public void deleteUniversity(Long id) {
        try {
            University university = getEntityById(id, universityRepository, "University");
            executeWithLogging(() -> {
                universityRepository.delete(university);
                return null;
            }, "Error deleting university");
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while deleting university: {}", e.getMessage());
            throw new InvalidDataException("Error: Cannot delete university due to existing references.");
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting university: {}", e.getMessage());
            throw new ServiceException("Error deleting university", e);
        }
    }

    @Override
    @Transactional
    public void addFacultyToUniversity(Long universityId, FacultyDTO facultyDTO) {
        try {
            University university = getEntityById(universityId, universityRepository, "University");
            Faculty faculty = facultyMapper.convertToEntity(facultyDTO);
            faculty.setUniversity(university);
            university.getFaculties().add(faculty);
            executeWithLogging(() -> {
                facultyRepository.save(faculty);
                return null;
            }, "Error adding faculty to university");
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while adding faculty to university: {}", e.getMessage());
            throw new InvalidDataException("Error: Cannot add faculty due to existing constraints.");
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error adding faculty to university: {}", e.getMessage());
            throw new ServiceException("Error adding faculty to university", e);
        }
    }

    @Override
    @Transactional
    public void removeFacultyFromUniversity(Long universityId, Long facultyId) {
        University university = getEntityById(universityId, universityRepository, "University");
        Faculty faculty = getEntityById(facultyId, facultyRepository, "Faculty");

        if (university.getFaculties().contains(faculty)) {
            university.getFaculties().remove(faculty);
            executeWithLogging(() -> {
                facultyRepository.delete(faculty);
                return null;
            }, "Error removing faculty from university");
        } else {
            throw new ResourceNotFoundException("Faculty not found in this university", universityId);
        }
    }

    // --------------- MÉTODOS AUXILIARES -----------------

    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return findEntityById(id, repository)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
    }

    private <T> Optional<T> findEntityById(Long id, JpaRepository<T, Long> repository) {
        return repository.findById(id);
    }

    private void updateUniversityFields(University existingUniversity, UniversityDTO universityDTO) {
        existingUniversity.setName(universityDTO.getName());
        existingUniversity.setLocation(universityDTO.getLocation());
        existingUniversity.setLastUpdate(LocalDateTime.now());
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
