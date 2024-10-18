package org.una.programmingIII.UTEMP_Project.services.UniversityServices;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.UniversityDTO;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.University;
import org.una.programmingIII.UTEMP_Project.models.Faculty;
import org.una.programmingIII.UTEMP_Project.repositories.UniversityRepository;
import org.una.programmingIII.UTEMP_Project.repositories.FacultyRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class UniversityServiceImplementation implements UniversityService {

    private static final Logger logger = LoggerFactory.getLogger(UniversityServiceImplementation.class);

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private final GenericMapper<University, UniversityDTO> universityMapper;
    private final GenericMapper<Faculty, FacultyDTO> facultyMapper;

    @Autowired
    public UniversityServiceImplementation(GenericMapperFactory mapperFactory) {
        this.universityMapper = mapperFactory.createMapper(University.class, UniversityDTO.class);
        this.facultyMapper = mapperFactory.createMapper(Faculty.class, FacultyDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UniversityDTO> getAllUniversities() {
        return executeWithLogging(() -> universityMapper.convertToDTOList(universityRepository.findAll()),
                "Error fetching all universities");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UniversityDTO> getUniversityById(Long id) {
        return executeWithLogging(() -> {
            University university = getEntityById(id, universityRepository, "University");
            return Optional.of(universityMapper.convertToDTO(university));
        }, "Error fetching university by ID");
    }

    @Override
    @Transactional
    public UniversityDTO createUniversity(UniversityDTO universityDTO) {
        University university = universityMapper.convertToEntity(universityDTO);
        return executeWithLogging(() -> universityMapper.convertToDTO(universityRepository.save(university)),
                "Error creating university");
    }

    @Override
    @Transactional
    public Optional<UniversityDTO> updateUniversity(Long id, @Valid UniversityDTO universityDTO) {
        Optional<University> optionalUniversity = universityRepository.findById(id);
        University existingUniversity = optionalUniversity.orElseThrow(() -> new ResourceNotFoundException("University", id));

        updateUniversityFields(existingUniversity, universityDTO);
        return executeWithLogging(() -> Optional.of(universityMapper.convertToDTO(universityRepository.save(existingUniversity))),
                "Error updating university");
    }

    @Override
    @Transactional
    public void deleteUniversity(Long id) {
        University university = getEntityById(id, universityRepository, "University");
        executeWithLogging(() -> {
            universityRepository.delete(university);
            return null;
        }, "Error deleting university");
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyDTO> getFacultiesByUniversityId(Long universityId) {
        University university = getEntityById(universityId, universityRepository, "University");
        return executeWithLogging(() -> facultyMapper.convertToDTOList(university.getFaculties()),
                "Error fetching faculties by university ID");
    }

    @Override
    @Transactional
    public void addFacultyToUniversity(Long universityId, FacultyDTO facultyDTO) {
        University university = getEntityById(universityId, universityRepository, "University");
        Faculty faculty = facultyMapper.convertToEntity(facultyDTO);

        faculty.setUniversity(university);
        university.getFaculties().add(faculty);

        executeWithLogging(() -> {
            facultyRepository.save(faculty);
            return null;
        }, "Error adding faculty to university");
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
        }
    }

    // --------------- MÉTODOS AUXILIARES -----------------

    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
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
