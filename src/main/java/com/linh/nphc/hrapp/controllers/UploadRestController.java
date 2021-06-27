package com.linh.nphc.hrapp.controllers;

import com.linh.nphc.hrapp.exceptions.UnableToReadFileException;
import com.linh.nphc.hrapp.models.UploadResponse;
import com.linh.nphc.hrapp.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class UploadRestController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    @RequestMapping("/users/upload")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file){
        try{
            employeeService.processFile(file);
            return new ResponseEntity<>(new UploadResponse("Data is created"), HttpStatus.CREATED);
        } catch (UnableToReadFileException ex){
            return new ResponseEntity<>(new UploadResponse("File is uploaded but not processed - "+ex.getMessage()), HttpStatus.OK);
        }

    }

}
