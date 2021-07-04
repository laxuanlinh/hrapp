package com.linh.nphc.hrapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linh.nphc.hrapp.models.Employee;
import com.linh.nphc.hrapp.models.EmployeeResponse;
import com.linh.nphc.hrapp.repositories.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @AfterEach
    public void after(){
        employeeRepository.deleteAll();
    }

    @Test
    public void shouldProcessFile() throws Exception {
        MockMultipartFile file = getFile("employees.csv");
        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/upload")
                .file("file", file.getBytes())).andExpect(status().isCreated());
        List<Employee> employees = employeeRepository.findAll();
        assertEquals(10, employees.size());
    }

    private MockMultipartFile getFile(String filePath) throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource(filePath).toURI());
        byte[] content = Files.readAllBytes(path);
        return new MockMultipartFile("employees.csv", "employees.csv", "text/plain", content);
    }

    @Test
    public void shouldGetEmployeesWithSalaryRange() throws Exception {
        this.shouldProcessFile();
        MvcResult result =mockMvc.perform(get("/users?maxSalary=4000&minSalary=1000"))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeResponse employees = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertEquals(2, employees.getResult().size());
    }

    @Test
    public void shouldGetEmployeesWithOffsetAndLimit() throws Exception {
        this.shouldProcessFile();
        MvcResult result =mockMvc.perform(get("/users?offset=2&limit=10&maxSalary=50000"))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeResponse employees = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertEquals(8, employees.getResult().size());
    }

    @Test
    public void shouldGetEmployeesWithName() throws Exception {
        this.shouldProcessFile();
        MvcResult result =mockMvc.perform(get("/users?name=Harry"))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeResponse employees = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertEquals(1, employees.getResult().size());
        Employee employee = employees.getResult().get(0);
        assertEquals("Harry Potter", employee.getName());
        assertEquals("e0001", employee.getId());
    }

    @Test
    public void shouldGetEmployeesWithID() throws Exception {
        this.shouldProcessFile();
        MvcResult result =mockMvc.perform(get("/users?id=e0001"))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeResponse employees = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertEquals(1, employees.getResult().size());
        Employee employee = employees.getResult().get(0);
        assertEquals("Harry Potter", employee.getName());
        assertEquals("e0001", employee.getId());
    }

    @Test
    public void shouldNotProcessFileWhenDuplicate() throws Exception {
        MockMultipartFile file = getFile("employees_duplicates.csv");
        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/upload")
                .file("file", file.getBytes())).andExpect(status().isBadRequest());
        List<Employee> employees = employeeRepository.findAll();
        assertEquals(0, employees.size());
    }

    @Test
    public void shouldUploadButNotProcessFileWhenConstraintViolation() throws Exception {
        MockMultipartFile file = getFile("employees_login_duplicates.csv");
        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/upload")
                .file("file", file.getBytes())).andExpect(status().isOk());
        List<Employee> employees = employeeRepository.findAll();
        assertEquals(0, employees.size());
    }

    @Test
    public void shouldNotProcessFileWhenSalaryLessThan0() throws Exception {
        MockMultipartFile file = getFile("employees_lesszero.csv");
        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/upload")
                .file("file", file.getBytes())).andExpect(status().isBadRequest());
        List<Employee> employees = employeeRepository.findAll();
        assertEquals(0, employees.size());
    }

    @Test
    public void shouldNotProcessFileWhenLoginNull() throws Exception {
        MockMultipartFile file = getFile("employees_null.csv");
        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/upload")
                .file("file", file.getBytes())).andExpect(status().isBadRequest());
        List<Employee> employees = employeeRepository.findAll();
        assertEquals(0, employees.size());
    }

    @Test
    public void shouldNotProcessFileWhenWrongDateFormat() throws Exception {
        MockMultipartFile file = getFile("employees_wrongdate.csv");
        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/upload")
                .file("file", file.getBytes())).andExpect(status().isBadRequest());
        List<Employee> employees = employeeRepository.findAll();
        assertEquals(0, employees.size());
    }

    @Test
    public void shouldNotProcessFileWhenWrongSalaryFormat() throws Exception {
        MockMultipartFile file = getFile("employees_wrongsalary.csv");
        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/upload")
                .file("file", file.getBytes())).andExpect(status().isBadRequest());
        List<Employee> employees = employeeRepository.findAll();
        assertEquals(0, employees.size());
    }

    @Test
    public void shouldGetEmployee() throws Exception {
        Employee employeeDTO = new Employee("e0002",
                "ronwl",
                "Ron Weasley",
                19234.50,
                LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        employeeRepository.save(employeeDTO);

        mockMvc.perform(get("/users/e0002"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":\"e0002\",\"login\":\"ronwl\",\"name\":\"Ron Weasley\",\"salary\":19234.5,\"startDate\":\"2001-11-16\"}"));
    }

    @Test
    public void shouldCreateEmployee() throws Exception {
        mockMvc.perform(post("/users").content("{\n" +
                "    \"id\": \"emp0001\",\n" +
                "    \"name\": \"Harry Potter\",\n" +
                "    \"login\": \"hpotter\",\n" +
                "    \"salary\": 1234.00,\n" +
                "    \"startDate\": \"2001-11-16\"\n" +
                "}").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        List<Employee> employees = employeeRepository.findAll();
        assertEquals(1, employees.size());
        Employee employee = employees.get(0);
        assertEquals("emp0001", employee.getId());
        assertEquals("Harry Potter", employee.getName());
        assertEquals("hpotter", employee.getLogin());
        assertEquals(1234.00, employee.getSalary());
        assertEquals("2001-11-16", employee.getStartDate().toString());
    }

    @Test
    public void shouldNotCreateEmployeeWhenIDExist() throws Exception {
        Employee employee = new Employee("emp0001",
                "ronwl",
                "Ron Weasley",
                19234.50,
                LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        employeeRepository.save(employee);
        mockMvc.perform(post("/users").content("{\n" +
                "    \"id\": \"emp0001\",\n" +
                "    \"name\": \"Harry Potter\",\n" +
                "    \"login\": \"hpotter\",\n" +
                "    \"salary\": 1234.00,\n" +
                "    \"startDate\": \"2001-11-16\"\n" +
                "}").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldUpdateEmployee() throws Exception {
        Employee employee = new Employee("emp0001",
                "ronwl",
                "Ron Weasley",
                19234.50,
                LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        employeeRepository.save(employee);
        mockMvc.perform(put("/users").content("{\n" +
                "    \"id\": \"emp0001\",\n" +
                "    \"name\": \"Harry Potter\",\n" +
                "    \"login\": \"hpotter\",\n" +
                "    \"salary\": 1234.00,\n" +
                "    \"startDate\": \"2001-11-16\"\n" +
                "}").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    public void shouldNotUpdateEmployeeWhenLoginNotUnique() throws Exception {
        Employee employee1 = new Employee("emp0001",
                "ronwl",
                "Ron Weasley",
                19234.50,
                LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        employeeRepository.save(employee1);
        Employee employee2 = new Employee("emp0002",
                "hpotter",
                "Harry Potter",
                19234.50,
                LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        employeeRepository.save(employee2);

        mockMvc.perform(put("/users").content("{\n" +
                "    \"id\": \"emp0002\",\n" +
                "    \"name\": \"Harry Potter\",\n" +
                "    \"login\": \"ronwl\",\n" +
                "    \"salary\": 1234.00,\n" +
                "    \"startDate\": \"2001-11-16\"\n" +
                "}").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldDeleteEmployee() throws Exception {
        Employee employee1 = new Employee("emp0001",
                "ronwl",
                "Ron Weasley",
                19234.50,
                LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        employeeRepository.save(employee1);

        mockMvc.perform(delete("/users/emp0001")).andExpect(status().isOk());
        assertEquals(0, employeeRepository.findAll().size());
    }

    @Test
    public void shouldNotDeleteEmployeeIfNotExist() throws Exception {
        Employee employee1 = new Employee("emp0001",
                "ronwl",
                "Ron Weasley",
                19234.50,
                LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        employeeRepository.save(employee1);

        mockMvc.perform(delete("/users/emp0002")).andExpect(status().isBadRequest());
    }



}
