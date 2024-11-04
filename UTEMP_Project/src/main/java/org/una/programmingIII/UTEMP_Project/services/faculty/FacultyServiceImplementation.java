package org.una.programmingIII.UTEMP_Project.services.faculty;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Department;
import org.una.programmingIII.UTEMP_Project.models.Faculty;
import org.una.programmingIII.UTEMP_Project.repositories.DepartmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.FacultyRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UniversityRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class FacultyServiceImplementation implements FacultyService {

    private static final Logger logger = LoggerFactory.getLogger(FacultyServiceImplementation.class);

    private final FacultyRepository facultyRepository;
    private final UniversityRepository universityRepository;
    private final DepartmentRepository departmentRepository;

    private final GenericMapper<Faculty, FacultyDTO> facultyMapper;
    private final GenericMapper<Department, DepartmentDTO> departmentMapper;

    @Autowired
    public FacultyServiceImplementation(
            FacultyRepository facultyRepository,
            UniversityRepository universityRepository,
            DepartmentRepository departmentRepository,
            GenericMapperFactory mapperFactory) {

        this.facultyRepository = facultyRepository;
        this.universityRepository = universityRepository;
        this.departmentRepository = departmentRepository;
        this.facultyMapper = mapperFactory.createMapper(Faculty.class, FacultyDTO.class);
        this.departmentMapper = mapperFactory.createMapper(Department.class, DepartmentDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FacultyDTO> getAllFaculties(Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Faculty> facultyPage = facultyRepository.findAll(pageable);
                return facultyPage.map(facultyMapper::convertToDTO);
            }, "Error fetching all faculties");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching faculties: {}", e.getMessage());
            throw new InvalidDataException("Error fetching faculties from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching faculties: {}", e.getMessage());
            throw new InvalidDataException("Error fetching faculties");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FacultyDTO> getFacultyById(Long id) {
        try {
            return executeWithLogging(() -> {
                Faculty faculty = getEntityById(id, facultyRepository, "Faculty");
                return Optional.of(facultyMapper.convertToDTO(faculty));
            }, "Error fetching faculty by ID");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch faculty: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching faculty by ID: {}", e.getMessage());
            throw new ServiceException("Error fetching faculty by ID", e);
        }
    }

    @Override
    @Transactional
    public FacultyDTO createFaculty(FacultyDTO facultyDTO) {
        try {
            Faculty faculty = facultyMapper.convertToEntity(facultyDTO);
            faculty.setUniversity(getEntityById(facultyDTO.getUniversity().getId(), universityRepository, "University"));

            return executeWithLogging(() ->
                            facultyMapper.convertToDTO(facultyRepository.save(faculty)),
                    "Error creating faculty"
            );
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to create faculty: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating faculty: {}", e.getMessage());
            throw new ServiceException("Error creating faculty", e);
        }
    }

    @Override
    @Transactional
    public Optional<FacultyDTO> updateFaculty(Long id, @Valid FacultyDTO facultyDTO) {
        try {
            Optional<Faculty> optionalFaculty = facultyRepository.findById(id);
            Faculty existingFaculty = optionalFaculty.orElseThrow(() -> new ResourceNotFoundException("Faculty", id));

            updateFacultyFields(existingFaculty, facultyDTO);
            return executeWithLogging(() ->
                            Optional.of(facultyMapper.convertToDTO(facultyRepository.save(existingFaculty))),
                    "Error updating faculty"
            );
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to update faculty: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating faculty: {}", e.getMessage());
            throw new ServiceException("Error updating faculty", e);
        }
    }

    @Override
    @Transactional
    public void deleteFaculty(Long id) {
        try {
            Faculty faculty = getEntityById(id, facultyRepository, "Faculty");
            executeWithLogging(() -> {
                facultyRepository.delete(faculty);
                return null;
            }, "Error deleting faculty");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to delete faculty: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting faculty: {}", e.getMessage());
            throw new ServiceException("Error deleting faculty", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FacultyDTO> getFacultiesByUniversityId(Long universityId, Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Faculty> facultyPage = facultyRepository.findByUniversityId(universityId, pageable);
                return facultyPage.map(facultyMapper::convertToDTO);
            }, "Error fetching faculties by university ID");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching faculties by university ID: {}", e.getMessage());
            throw new InvalidDataException("Error fetching faculties from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching faculties by university ID: {}", e.getMessage());
            throw new InvalidDataException("Error fetching faculties by university ID");
        }
    }

    @Override
    @Transactional
    public void addDepartmentToFaculty(Long facultyId, DepartmentDTO departmentDTO) {
        try {
            Faculty faculty = getEntityById(facultyId, facultyRepository, "Faculty");
            Department department = departmentMapper.convertToEntity(departmentDTO);

            department.setFaculty(faculty);
            faculty.getDepartments().add(department);

            executeWithLogging(() -> {
                departmentRepository.save(department);
                return null;
            }, "Error adding department to faculty");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to add department to faculty: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error adding department to faculty: {}", e.getMessage());
            throw new ServiceException("Error adding department to faculty", e);
        }
    }

    @Override
    @Transactional
    public void removeDepartmentFromFaculty(Long facultyId, Long departmentId) {
        try {
            Faculty faculty = getEntityById(facultyId, facultyRepository, "Faculty");
            Department department = getEntityById(departmentId, departmentRepository, "Department");

            if (faculty.getDepartments().contains(department)) {
                faculty.getDepartments().remove(department);

                executeWithLogging(() -> {
                    departmentRepository.delete(department);
                    return null;
                }, "Error removing department from faculty");
            } else {
                throw new ResourceNotFoundException("Department not found in this faculty", departmentId);
            }
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to remove department from faculty: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error removing department from faculty: {}", e.getMessage());
            throw new ServiceException("Error removing department from faculty", e);
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

    private void updateFacultyFields(Faculty existingFaculty, FacultyDTO facultyDTO) {
        existingFaculty.setName(facultyDTO.getName());
        existingFaculty.setUniversity(getEntityById(facultyDTO.getUniversity().getId(), universityRepository, "University"));
        existingFaculty.setLastUpdate(LocalDateTime.now());
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