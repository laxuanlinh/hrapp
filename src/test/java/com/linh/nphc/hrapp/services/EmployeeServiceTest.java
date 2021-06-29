package com.linh.nphc.hrapp.services;

import com.linh.nphc.hrapp.exceptions.DuplicateRowException;
import com.linh.nphc.hrapp.exceptions.InvalidFieldException;
import com.linh.nphc.hrapp.exceptions.UnableToReadFileException;
import com.linh.nphc.hrapp.models.Employee;
import com.linh.nphc.hrapp.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;

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

}















