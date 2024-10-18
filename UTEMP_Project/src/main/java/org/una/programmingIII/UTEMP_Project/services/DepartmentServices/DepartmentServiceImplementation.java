package org.una.programmingIII.UTEMP_Project.services.DepartmentServices;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Department;
import org.una.programmingIII.UTEMP_Project.models.Course;
import org.una.programmingIII.UTEMP_Project.repositories.DepartmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.CourseRepository;
import org.una.programmingIII.UTEMP_Project.repositories.FacultyRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class DepartmentServiceImplementation implements DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImplementation.class);

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private CourseRepository courseRepository;

    private final GenericMapper<Department, DepartmentDTO> departmentMapper;
    private final GenericMapper<Course, CourseDTO> courseMapper;

    @Autowired
    public DepartmentServiceImplementation(GenericMapperFactory mapperFactory) {
        this.departmentMapper = mapperFactory.createMapper(Department.class, DepartmentDTO.class);
        this.courseMapper = mapperFactory.createMapper(Course.class, CourseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartments() {
        return executeWithLogging(() -> departmentMapper.convertToDTOList(departmentRepository.findAll()),
                "Error fetching all departments");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DepartmentDTO> getDepartmentById(Long id) {
        return executeWithLogging(() -> {
            Department department = getEntityById(id, departmentRepository, "Department");
            return Optional.of(departmentMapper.convertToDTO(department));
        }, "Error fetching department by ID");
    }

    @Override
    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) {
        Department department = departmentMapper.convertToEntity(departmentDTO);
        department.setFaculty(getEntityById(departmentDTO.getFaculty().getId(), facultyRepository, "Faculty"));
        return executeWithLogging(() -> departmentMapper.convertToDTO(departmentRepository.save(department)),
                "Error creating department");
    }

    @Override
    @Transactional
    public Optional<DepartmentDTO> updateDepartment(Long id, @Valid DepartmentDTO departmentDTO) {
        Optional<Department> optionalDepartment = departmentRepository.findById(id);
        Department existingDepartment = optionalDepartment.orElseThrow(() -> new ResourceNotFoundException("Department", id));

        updateDepartmentFields(existingDepartment, departmentDTO);
        return executeWithLogging(() -> Optional.of(departmentMapper.convertToDTO(departmentRepository.save(existingDepartment))),
                "Error updating department");
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = getEntityById(id, departmentRepository, "Department");
        executeWithLogging(() -> {
            departmentRepository.delete(department);
            return null;
        }, "Error deleting department");
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByDepartmentId(Long departmentId) {
        Department department = getEntityById(departmentId, departmentRepository, "Department");
        return executeWithLogging(() -> courseMapper.convertToDTOList(department.getCourses()),
                "Error fetching courses by department ID");
    }

    @Override
    @Transactional
    public void addCourseToDepartment(Long departmentId, CourseDTO courseDTO) {
        Department department = getEntityById(departmentId, departmentRepository, "Department");
        Course course = courseMapper.convertToEntity(courseDTO);

        course.setDepartment(department);
        department.getCourses().add(course);

        executeWithLogging(() -> {
            courseRepository.save(course);
            return null;
        }, "Error adding course to department");
    }

    @Override
    @Transactional
    public void removeCourseFromDepartment(Long departmentId, Long courseId) {
        Department department = getEntityById(departmentId, departmentRepository, "Department");
        Course course = getEntityById(courseId, courseRepository, "Course");

        if (department.getCourses().contains(course)) {
            department.getCourses().remove(course);

            executeWithLogging(() -> {
                courseRepository.delete(course);
                return null;
            }, "Error removing course from department");
        }
    }

    // --------------- MÉTODOS AUXILIARES -----------------

    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
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
