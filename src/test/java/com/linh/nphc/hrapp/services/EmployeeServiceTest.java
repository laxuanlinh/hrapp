package com.linh.nphc.hrapp.services;

import com.linh.nphc.hrapp.exceptions.DuplicateRowException;
import com.linh.nphc.hrapp.exceptions.InvalidFieldException;
import com.linh.nphc.hrapp.exceptions.UnableToReadFileException;
import com.linh.nphc.hrapp.models.Employee;
import com.linh.nphc.hrapp.models.EmployeeDTO;
import com.linh.nphc.hrapp.models.OffsetBasedPageRequest;
import com.linh.nphc.hrapp.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    public void setUp(){
        lenient().when(employeeRepository.save(any(Employee.class))).thenReturn(new Employee());
    }

    @Test
    public void shouldProcessFile() throws IOException, URISyntaxException {
        MultipartFile file = this.getFile("employees.csv");
        employeeService.processFile(file);
    }

    private MultipartFile getFile(String filePath) throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource(filePath).toURI());
        byte[] content = Files.readAllBytes(path);
        return new MockMultipartFile("employees.csv", "employees.csv", "text/plain", content);
    }

    @Test
    public void shouldThrowExceptionWhenDuplicates() throws IOException, URISyntaxException {
        MultipartFile file = this.getFile("employees_duplicates.csv");
        assertThrows(DuplicateRowException.class, () -> employeeService.processFile(file));
    }

    @Test
    public void shouldThrowExceptionWhenFieldInvalid() throws IOException, URISyntaxException {
        MultipartFile fileNull = this.getFile("employees_null.csv");
        assertThrows(InvalidFieldException.class, () -> employeeService.processFile(fileNull));
        MultipartFile fileLessZero = this.getFile("employees_lesszero.csv");
        assertThrows(InvalidFieldException.class, () -> employeeService.processFile(fileLessZero));
    }

    @Test
    public void shouldThrowExceptionWhenWrongDateFormat() throws URISyntaxException, IOException {
        MultipartFile file = this.getFile("employees_wrongdate.csv");
        assertThrows(UnableToReadFileException.class, () -> employeeService.processFile(file));
    }

    @Test
    public void shouldThrowExceptionWhenWrongSalary() throws URISyntaxException, IOException {
        MultipartFile file = this.getFile("employees_wrongsalary.csv");
        assertThrows(UnableToReadFileException.class, () -> employeeService.processFile(file));
    }

    @Test
    public void shouldReturnListOfEmployees(){
        when(employeeRepository.findEmployeesBySalaryRangeAndNameAndLoginAndID(anyDouble(), anyDouble(), anyString(), anyString(), anyString(), any(Pageable.class))).thenReturn(Arrays.asList(new Employee()));
        assertEquals(1, employeeService.getEmployees(0.0,
                4000.0,
                "id",
                "login",
                "name",
                new OffsetBasedPageRequest(0, 10, Sort.by(Sort.Direction.ASC, "id"))).size());
    }

    @Test
    public void shouldThrowExceptionIfNotAbleToQueryEmployees(){
        when(employeeRepository.findEmployeesBySalaryRangeAndNameAndLoginAndID(anyDouble(), anyDouble(), anyString(), anyString(), anyString(), any(Pageable.class))).thenThrow(new RuntimeException("Unable to query employees"));
        assertThrows(RuntimeException.class, ()->employeeService.getEmployees(0.0,
                4000.0,
                "id",
                "login",
                "name",
                new OffsetBasedPageRequest(0, 10, Sort.by(Sort.Direction.ASC, "id"))));
    }

    @Test
    public void shouldReturnEmployee(){
        when(employeeRepository.findById(anyString())).thenReturn(Optional.of(new Employee("e0002","ronwl","Ron Weasley",19234.50, LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")))));
        Employee employee = employeeService.getEmployee("e0002");
        assertEquals("e0002", employee.getId());
        assertEquals("ronwl", employee.getLogin());
        assertEquals("Ron Weasley", employee.getName());
        assertEquals(19234.50, employee.getSalary());
        assertEquals("2001-11-16", employee.getStartDate().toString());
    }

    @Test
    public void shouldNotReturnNullIfIDNotMatch(){
        when(employeeRepository.findById(anyString())).thenReturn(Optional.empty());
        Employee employee = employeeService.getEmployee("e0002");
        assertNull(employee);
    }

    @Test
    public void shouldThrowExceptionIfUnableToQuery(){
        when(employeeRepository.findById(anyString())).thenThrow(new RuntimeException("Not able to query"));
        assertThrows(RuntimeException.class, () -> employeeService.getEmployee("e0002"));
    }

    @Test
    public void shouldCreateEmployee(){
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId("id");
        employeeDTO.setName("name");
        employeeDTO.setLogin("login");
        employeeDTO.setSalary(4000.0);
        employeeDTO.setStartDate("2011-01-01");
        employeeService.createEmployee(employeeDTO);

    }

    @Test
    public void shouldThrowExceptionWhenUserAlreadyExist_CreateEmployee(){
        when(employeeRepository.findById(anyString())).thenReturn(Optional.of(new Employee("e0002","ronwl","Ron Weasley",19234.50, LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")))));
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId("id");
        employeeDTO.setName("name");
        employeeDTO.setLogin("login");
        employeeDTO.setSalary(4000.0);
        employeeDTO.setStartDate("2011-01-01");
        assertThrows(InvalidFieldException.class, ()->employeeService.createEmployee(employeeDTO));
    }

    @Test
    public void shouldThrowExceptionWhenLoginAlreadyExist_CreateEmployee(){
        when(employeeRepository.findByLogin(anyString())).thenReturn(Optional.of(new Employee("e0002","ronwl","Ron Weasley",19234.50, LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")))));
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId("id");
        employeeDTO.setName("name");
        employeeDTO.setLogin("ronwl");
        employeeDTO.setSalary(4000.0);
        employeeDTO.setStartDate("2011-01-01");
        assertThrows(InvalidFieldException.class, ()->employeeService.createEmployee(employeeDTO));
    }

    @Test
    public void shouldUpdateEmployee(){
        when(employeeRepository.findById(anyString())).thenReturn(Optional.of(new Employee("e0002","ronwl","Ron Weasley",19234.50, LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")))));
        when(employeeRepository.findByLogin(anyString())).thenReturn(Optional.of(new Employee("e0002","ronwl","Ron Weasley",19234.50, LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")))));
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId("e0002");
        employeeDTO.setName("Ron Weasley");
        employeeDTO.setLogin("ronwl");
        employeeDTO.setSalary(4000.0);
        employeeDTO.setStartDate("2011-01-01");
        employeeService.updateEmployee(employeeDTO);
    }

    @Test
    public void shouldThrowExceptionWhenLoginAlreadyExist_UpdateEmployee(){
        when(employeeRepository.findById(anyString())).thenReturn(Optional.of(new Employee("e0002","ronwl","Ron Weasley",19234.50, LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")))));
        when(employeeRepository.findByLogin(anyString())).thenReturn(Optional.of(new Employee("e0003","ronwl","some random name",19234.50, LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")))));
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId("e0002");
        employeeDTO.setName("name");
        employeeDTO.setLogin("ronwl");
        employeeDTO.setSalary(4000.0);
        employeeDTO.setStartDate("2011-01-01");
        assertThrows(InvalidFieldException.class, ()->employeeService.updateEmployee(employeeDTO));
    }

    @Test
    public void shouldThrowExceptionWhenEmployeeNotExist_UpdateEmployee(){
        when(employeeRepository.findById(anyString())).thenReturn(Optional.empty());
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId("e0002");
        employeeDTO.setName("name");
        employeeDTO.setLogin("ronwl");
        employeeDTO.setSalary(4000.0);
        employeeDTO.setStartDate("2011-01-01");
        assertThrows(InvalidFieldException.class, ()->employeeService.updateEmployee(employeeDTO));
    }

}















