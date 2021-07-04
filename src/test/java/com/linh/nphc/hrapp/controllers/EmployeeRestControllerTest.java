package com.linh.nphc.hrapp.controllers;

import com.linh.nphc.hrapp.exceptions.InvalidFieldException;
import com.linh.nphc.hrapp.models.Employee;
import com.linh.nphc.hrapp.models.EmployeeDTO;
import com.linh.nphc.hrapp.models.EmployeeResponse;
import com.linh.nphc.hrapp.models.MessageResponse;
import com.linh.nphc.hrapp.services.EmployeeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeRestControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeRestController employeeRestController;

    @Test
    public void shouldReturnSuccess(){
        when(employeeService.getEmployees(anyDouble(), anyDouble(), anyString(), anyString(), anyString(), any(Pageable.class))).thenReturn(new ArrayList<>());
        ResponseEntity<Object> responseEntity = employeeRestController.getEmployees(100.0,
                5000.0,
                "e00001",
                "login",
                "name",
                0,
                10,
                "name",
                "ASC");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void shouldReturnBadRequestWhenSalaryLessThan0(){
        ResponseEntity<Object> responseEntity = employeeRestController.getEmployees(-100.0,
                5000.0,
                "e00001",
                "login",
                "name",
                0,
                10,
                "name",
                "ASC");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Min salary must be greater than 0", ((MessageResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
        responseEntity = employeeRestController.getEmployees(100.0,
                -5000.0,
                "e00001",
                "login",
                "name",
                0,
                10,
                "name",
                "ASC");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Max salary must be greater than 0", ((MessageResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }

    @Test
    public void shouldReturnBadRequestWhenMaxLessThanMinSalary(){
        ResponseEntity<Object> responseEntity = employeeRestController.getEmployees(5000.0,
                4000.0,
                "e00001",
                "login",
                "name",
                0,
                10,
                "name",
                "ASC");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Max salary cannot be less than min salary", ((MessageResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }

    @Test
    public void shouldReturnBadRequestWhenOffsetLessThan0(){
        ResponseEntity<Object> responseEntity = employeeRestController.getEmployees(100.0,
                5000.0,
                "e00001",
                "login",
                "name",
                -1,
                10,
                "name",
                "ASC");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Offset must be greater than 0", ((MessageResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }

    @Test
    public void shouldReturnBadRequestWhenLimitLessThan0(){
        ResponseEntity<Object> responseEntity = employeeRestController.getEmployees(100.0,
                5000.0,
                "e00001",
                "login",
                "name",
                0,
                -10,
                "name",
                "ASC");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Limit must be greater than 0", ((MessageResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }

    @Test
    public void shouldReturnBadRequestWhenSortNotValid(){
        ResponseEntity<Object> responseEntity = employeeRestController.getEmployees(100.0,
                5000.0,
                "e00001",
                "login",
                "name",
                0,
                1,
                "some random field",
                "ASC");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Invalid sorting", ((MessageResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }

    @Test
    public void shouldReturnBadRequestWhenOrderNotValid(){
        ResponseEntity<Object> responseEntity = employeeRestController.getEmployees(100.0,
                5000.0,
                "e00001",
                "login",
                "name",
                0,
                10,
                "name",
                "some weird order");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Invalid order", ((MessageResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }

    @Test
    public void shouldReturnInternalErrorWhenUnableToQueryEmployees(){
        when(employeeService.getEmployees(anyDouble(), anyDouble(), anyString(), anyString(), anyString(), any(Pageable.class))).thenThrow(new RuntimeException("Unable to query employees"));
        ResponseEntity<Object> responseEntity = employeeRestController.getEmployees(100.0,
                5000.0,
                "e00001",
                "login",
                "name",
                0,
                10,
                "name",
                "ASC");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Unable to query employees", ((MessageResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }

    @Test
    public void shouldReturnEmployee(){
        Employee employee = new Employee("e0002","ronwl","Ron Weasley",19234.50, LocalDate.parse("2001-11-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        when(employeeService.getEmployee("e0002")).thenReturn(employee);
        ResponseEntity<Object> responseEntity = employeeRestController.getEmployee("e0002");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("e0002", ((Employee) Objects.requireNonNull(responseEntity.getBody())).getId());
        assertEquals("ronwl", ((Employee) Objects.requireNonNull(responseEntity.getBody())).getLogin());
        assertEquals("Ron Weasley", ((Employee) Objects.requireNonNull(responseEntity.getBody())).getName());
        assertEquals(19234.50, ((Employee) Objects.requireNonNull(responseEntity.getBody())).getSalary());
        assertEquals("2001-11-16", ((Employee) Objects.requireNonNull(responseEntity.getBody())).getStartDate().toString());
    }

    @Test
    public void shouldReturnEmptyEmployeeWhenIdNotMatch(){
        when(employeeService.getEmployee("e0001")).thenReturn(null);
        ResponseEntity<Object> responseEntity = employeeRestController.getEmployee("e0001");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
    }

    @Test
    public void shouldReturnBadRequestIfIDNull(){
        ResponseEntity<Object> responseEntity = employeeRestController.getEmployee(null);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("ID cannot be null", ((MessageResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }

    @Test
    public void shouldReturnInternalErrorWhenUnableToQuery(){
        when(employeeService.getEmployee("e0001")).thenThrow(new RuntimeException("Unable to query employee"));
        ResponseEntity<Object> responseEntity = employeeRestController.getEmployee("e0001");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Unable to query employee", ((MessageResponse) Objects.requireNonNull(responseEntity.getBody())).getMessage());
    }

    @Test
    public void shouldCreateEmployee(){
        doNothing().when(employeeService).createEmployee(any(EmployeeDTO.class));
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId("id");
        employeeDTO.setName("name");
        employeeDTO.setLogin("login");
        employeeDTO.setSalary(4000.0);
        employeeDTO.setStartDate("2011-01-01");
        ResponseEntity<MessageResponse> responseEntity = employeeRestController.createEmployee(employeeDTO);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals("Successfully created", Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    public void shouldReturnBadRequestIfEmployeeNull_CreateEmployee(){
        ResponseEntity<MessageResponse> responseEntity = employeeRestController.createEmployee(null);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Employee cannot be null", Objects.requireNonNull(responseEntity.getBody()).getMessage());

    }

    @Test
    public void shouldReturnBadRequestWhenFieldInvalid_CreateEmployee(){
        doThrow(new InvalidFieldException("Field is not valid")).when(employeeService).createEmployee(any(EmployeeDTO.class));
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId("id");
        employeeDTO.setName("name");
        employeeDTO.setLogin("login");
        employeeDTO.setSalary(4000.0);
        employeeDTO.setStartDate("2011-01-01");
        ResponseEntity<MessageResponse> responseEntity = employeeRestController.createEmployee(employeeDTO);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Field is not valid", Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    public void shouldReturnInternalErrorWhenUnexpectedError_CreateEmployee(){
        doThrow(new RuntimeException("Unable to create employee")).when(employeeService).createEmployee(any(EmployeeDTO.class));
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId("id");
        employeeDTO.setName("name");
        employeeDTO.setLogin("login");
        employeeDTO.setSalary(4000.0);
        employeeDTO.setStartDate("2011-01-01");
        ResponseEntity<MessageResponse> responseEntity = employeeRestController.createEmployee(employeeDTO);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Unable to create employee", Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    public void shouldUpdateEmployee(){
        doNothing().when(employeeService).updateEmployee(any(EmployeeDTO.class));
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId("id");
        employeeDTO.setName("name");
        employeeDTO.setLogin("login");
        employeeDTO.setSalary(4000.0);
        employeeDTO.setStartDate("2011-01-01");
        ResponseEntity<MessageResponse> responseEntity = employeeRestController.updateEmployee(employeeDTO);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Successfully updated", Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    public void shouldReturnBadRequestIfEmployeeNull_UpdateEmployee(){
        ResponseEntity<MessageResponse> responseEntity = employeeRestController.updateEmployee(null);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Employee cannot be null", Objects.requireNonNull(responseEntity.getBody()).getMessage());

    }

    @Test
    public void shouldReturnBadRequestWhenFieldInvalid_UpdateEmployee(){
        doThrow(new InvalidFieldException("Employee ID already exists")).when(employeeService).updateEmployee(any(EmployeeDTO.class));
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId("id");
        employeeDTO.setName("name");
        employeeDTO.setLogin("login");
        employeeDTO.setSalary(4000.0);
        employeeDTO.setStartDate("2011-01-01");
        ResponseEntity<MessageResponse> responseEntity = employeeRestController.updateEmployee(employeeDTO);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Employee ID already exists", Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    public void shouldReturnInternalErrorWhenUnexpectedError_UpdateEmployee(){
        doThrow(new RuntimeException("Unable to update employee")).when(employeeService).updateEmployee(any(EmployeeDTO.class));
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId("id");
        employeeDTO.setName("name");
        employeeDTO.setLogin("login");
        employeeDTO.setSalary(4000.0);
        employeeDTO.setStartDate("2011-01-01");
        ResponseEntity<MessageResponse> responseEntity = employeeRestController.updateEmployee(employeeDTO);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Unable to update employee", Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    public void shouldDeleteEmployee(){
        doNothing().when(employeeService).deleteEmployee(anyString());
        ResponseEntity<MessageResponse> responseEntity = employeeRestController.deleteEmployee("e0002");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Successfully deleted", Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    public void shouldNotDeleteEmployeeWhenIDNull(){
        ResponseEntity<MessageResponse> responseEntity = employeeRestController.deleteEmployee(null);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("ID cannot be null", Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    public void shouldNotDeleteEmployeeWhenIDNotMatch(){
        doThrow(new InvalidFieldException("No such employee")).when(employeeService).deleteEmployee(anyString());
        ResponseEntity<MessageResponse> responseEntity = employeeRestController.deleteEmployee("e0002");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("No such employee", Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    public void shouldReturnInternalErrorWhenUnexpectedError(){
        doThrow(new RuntimeException("Some error")).when(employeeService).deleteEmployee(anyString());
        ResponseEntity<MessageResponse> responseEntity = employeeRestController.deleteEmployee("e0002");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Some error", Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

}