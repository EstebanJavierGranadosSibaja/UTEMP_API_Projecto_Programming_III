package org.una.programmingIII.UTEMP_Project.services.department;

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
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Course;
import org.una.programmingIII.UTEMP_Project.models.Department;
import org.una.programmingIII.UTEMP_Project.repositories.CourseRepository;
import org.una.programmingIII.UTEMP_Project.repositories.DepartmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.FacultyRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class DepartmentServiceImplementation implements DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImplementation.class);

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;

    private final GenericMapper<Department, DepartmentDTO> departmentMapper;
    private final GenericMapper<Course, CourseDTO> courseMapper;

    @Autowired
    public DepartmentServiceImplementation(
            DepartmentRepository departmentRepository,
            FacultyRepository facultyRepository,
            CourseRepository courseRepository,
            GenericMapperFactory mapperFactory) {

        this.departmentRepository = departmentRepository;
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
        this.departmentMapper = mapperFactory.createMapper(Department.class, DepartmentDTO.class);
        this.courseMapper = mapperFactory.createMapper(Course.class, CourseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentDTO> getAllDepartments(Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Department> departmentPage = departmentRepository.findAll(pageable);
                return departmentPage.map(departmentMapper::convertToDTO);
            }, "Error fetching all departments");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching all departments: {}", e.getMessage());
            throw new InvalidDataException("Error fetching departments from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching all departments: {}", e.getMessage());
            throw new InvalidDataException("Error fetching departments");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DepartmentDTO> getDepartmentById(Long id) {
        try {
            return executeWithLogging(() -> {
                Department department = getEntityById(id, departmentRepository, "Department");
                return Optional.of(departmentMapper.convertToDTO(department));
            }, "Error fetching department by ID");
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found while fetching department ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while fetching department ID {}: {}", id, e.getMessage());
            throw new RuntimeException("An unexpected error occurred while fetching the department.", e);
        }
    }

    @Override
    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) {
        try {
            Department department = departmentMapper.convertToEntity(departmentDTO);
            department.setFaculty(getEntityById(departmentDTO.getFaculty().getId(), facultyRepository, "Faculty"));
            return executeWithLogging(() -> departmentMapper.convertToDTO(departmentRepository.save(department)),
                    "Error creating department");
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found while creating department: {}", e.getMessage());
            throw e;
        } catch (InvalidDataException e) {
            logger.error("Invalid data provided for creating department: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while creating department: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while creating the department.", e);
        }
    }

    @Override
    @Transactional
    public Optional<DepartmentDTO> updateDepartment(Long id, @Valid DepartmentDTO departmentDTO) {
        try {
            Optional<Department> optionalDepartment = departmentRepository.findById(id);
            Department existingDepartment = optionalDepartment.orElseThrow(() -> new ResourceNotFoundException("Department", id));

            updateDepartmentFields(existingDepartment, departmentDTO);

            return executeWithLogging(() -> Optional.of(departmentMapper.convertToDTO(departmentRepository.save(existingDepartment))),
                    "Error updating department");
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found while updating department: {}", e.getMessage());
            throw e;
        } catch (InvalidDataException e) {
            logger.error("Invalid data provided for updating department: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while updating department: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while updating the department.", e);
        }
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        try {
            Department department = getEntityById(id, departmentRepository, "Department");
            executeWithLogging(() -> {
                departmentRepository.delete(department);
                return null;
            }, "Error deleting department");
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found while deleting department: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while deleting department: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while deleting the department.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentDTO> getDepartmentsByFacultyId(Long facultyId, Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Department> departmentPage = departmentRepository.findByFacultyId(facultyId, pageable);
                return departmentPage.map(departmentMapper::convertToDTO);
            }, "Error fetching departments by faculty ID");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching departments for faculty ID {}: {}", facultyId, e.getMessage());
            throw new InvalidDataException("Error fetching departments from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching departments for faculty ID {}: {}", facultyId, e.getMessage());
            throw new InvalidDataException("Error fetching departments");
        }
    }

    @Override
    @Transactional
    public void addCourseToDepartment(Long departmentId, CourseDTO courseDTO) {
        try {
            Department department = getEntityById(departmentId, departmentRepository, "Department");
            Course course = courseMapper.convertToEntity(courseDTO);

            course.setDepartment(department);
            department.getCourses().add(course);

            executeWithLogging(() -> {
                courseRepository.save(course);
                return null;
            }, "Error adding course to department");
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found while adding course to department: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while adding course to department: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while adding the course to the department.", e);
        }
    }

    @Override
    @Transactional
    public void removeCourseFromDepartment(Long departmentId, Long courseId) {
        try {
            Department department = getEntityById(departmentId, departmentRepository, "Department");
            Course course = getEntityById(courseId, courseRepository, "Course");

            if (department.getCourses().contains(course)) {
                department.getCourses().remove(course);

                executeWithLogging(() -> {
                    courseRepository.delete(course);
                    return null;
                }, "Error removing course from department");
            } else {
                throw new ResourceNotFoundException("Course not found in this department", courseId);
            }
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found while removing course from department: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while removing course from department: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while removing the course from the department.", e);
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

    private void updateDepartmentFields(Department existingDepartment, DepartmentDTO departmentDTO) {
        existingDepartment.setName(departmentDTO.getName());
        existingDepartment.setFaculty(getEntityById(departmentDTO.getFaculty().getId(), facultyRepository, "Faculty"));
        existingDepartment.setLastUpdate(LocalDateTime.now());
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