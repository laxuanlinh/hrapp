package com.linh.nphc.hrapp.controllers;

import com.linh.nphc.hrapp.exceptions.UnableToReadFileException;
import com.linh.nphc.hrapp.exceptions.UnableToSaveEmployeeException;
import com.linh.nphc.hrapp.services.EmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadRestControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private UploadRestController uploadRestController;

    @Test
    public void shouldReturn201WhenDataCreated(){
        doNothing().when(employeeService).processFile(any(MultipartFile.class));
        assertEquals(HttpStatus.CREATED, uploadRestController.upload(new MockMultipartFile("EmployeeFile.csv", new byte[]{})).getStatusCode());
    }

    @Test
    public void shouldReturn200WhenUploadedButDataNotCreated(){
        doThrow(new UnableToSaveEmployeeException("Unable to save employee due to constraint violation")).when(employeeService).processFile(any(MultipartFile.class));
        assertEquals(HttpStatus.OK, uploadRestController.upload(new MockMultipartFile("EmployeeFile.csv", new byte[]{})).getStatusCode());
    }

    @Test
    public void shouldReturn400WhenFileInvalid(){
        doThrow(new UnableToReadFileException("Unable to read file EmployeeFile.csv")).when(employeeService).processFile(any(MultipartFile.class));
        assertEquals(HttpStatus.BAD_REQUEST, uploadRestController.upload(new MockMultipartFile("EmployeeFile.csv", new byte[]{})).getStatusCode());
    }

}