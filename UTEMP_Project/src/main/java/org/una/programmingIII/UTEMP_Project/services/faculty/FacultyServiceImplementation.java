package org.una.programmingIII.UTEMP_Project.services.faculty;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Faculty;
import org.una.programmingIII.UTEMP_Project.models.Department;
import org.una.programmingIII.UTEMP_Project.repositories.FacultyRepository;
import org.una.programmingIII.UTEMP_Project.repositories.DepartmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UniversityRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class FacultyServiceImplementation implements FacultyService {

    private static final Logger logger = LoggerFactory.getLogger(FacultyServiceImplementation.class);

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private final GenericMapper<Faculty, FacultyDTO> facultyMapper;
    private final GenericMapper<Department, DepartmentDTO> departmentMapper;

    @Autowired
    public FacultyServiceImplementation(GenericMapperFactory mapperFactory) {
        this.facultyMapper = mapperFactory.createMapper(Faculty.class, FacultyDTO.class);
        this.departmentMapper = mapperFactory.createMapper(Department.class, DepartmentDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyDTO> getAllFaculties() {
        return executeWithLogging(() -> facultyMapper.convertToDTOList(facultyRepository.findAll()),
                "Error fetching all faculties");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FacultyDTO> getFacultyById(Long id) {
        return executeWithLogging(() -> {
            Faculty faculty = getEntityById(id, facultyRepository, "Faculty");
            return Optional.of(facultyMapper.convertToDTO(faculty));
        }, "Error fetching faculty by ID");
    }

    @Override
    @Transactional
    public FacultyDTO createFaculty(FacultyDTO facultyDTO) {
        Faculty faculty = facultyMapper.convertToEntity(facultyDTO);
        faculty.setUniversity(getEntityById(facultyDTO.getUniversity().getId(), universityRepository, "University"));
        return executeWithLogging(() -> facultyMapper.convertToDTO(facultyRepository.save(faculty)),
                "Error creating faculty");
    }

    @Override
    @Transactional
    public Optional<FacultyDTO> updateFaculty(Long id, @Valid FacultyDTO facultyDTO) {
        Optional<Faculty> optionalFaculty = facultyRepository.findById(id);
        Faculty existingFaculty = optionalFaculty.orElseThrow(() -> new ResourceNotFoundException("Faculty", id));

        updateFacultyFields(existingFaculty, facultyDTO);
        return executeWithLogging(() -> Optional.of(facultyMapper.convertToDTO(facultyRepository.save(existingFaculty))),
                "Error updating faculty");
    }

    @Override
    @Transactional
    public void deleteFaculty(Long id) {
        Faculty faculty = getEntityById(id, facultyRepository, "Faculty");
        executeWithLogging(() -> {
            facultyRepository.delete(faculty);
            return null;
        }, "Error deleting faculty");
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getDepartmentsByFacultyId(Long facultyId) {
        Faculty faculty = getEntityById(facultyId, facultyRepository, "Faculty");
        return executeWithLogging(() -> departmentMapper.convertToDTOList(faculty.getDepartments()),
                "Error fetching departments by faculty ID");
    }

    @Override
    @Transactional
    public void addDepartmentToFaculty(Long facultyId, DepartmentDTO departmentDTO) {
        Faculty faculty = getEntityById(facultyId, facultyRepository, "Faculty");
        Department department = departmentMapper.convertToEntity(departmentDTO);

        department.setFaculty(faculty);
        faculty.getDepartments().add(department);

        executeWithLogging(() -> {
            departmentRepository.save(department);
            return null;
        }, "Error adding department to faculty");
    }

    @Override
    @Transactional
    public void removeDepartmentFromFaculty(Long facultyId, Long departmentId) {
        Faculty faculty = getEntityById(facultyId, facultyRepository, "Faculty");
        Department department = getEntityById(departmentId, departmentRepository, "Department");

        if (faculty.getDepartments().contains(department)) {
            faculty.getDepartments().remove(department);

            executeWithLogging(() -> {
                departmentRepository.delete(department);
                return null;
            }, "Error removing department from faculty");
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

    private void updateFacultyFields (Faculty existingFaculty, FacultyDTO facultyDTO){
        existingFaculty.setName(facultyDTO.getName());
        existingFaculty.setUniversity(getEntityById(facultyDTO.getUniversity().getId(), universityRepository, "University"));
        existingFaculty.setLastUpdate(LocalDateTime.now());
    }

    private <T > T executeWithLogging(Supplier< T > action, String errorMessage) {
        try {
            return action.get();
        } catch (Exception e) {
            logger.error("{}: {}", errorMessage, e.getMessage());
            throw new ServiceException(errorMessage, e);
        }
    }
}
