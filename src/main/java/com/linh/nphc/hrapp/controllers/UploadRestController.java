package com.linh.nphc.hrapp.controllers;

import com.linh.nphc.hrapp.exceptions.DuplicateRowException;
import com.linh.nphc.hrapp.exceptions.InvalidFieldException;
import com.linh.nphc.hrapp.exceptions.UnableToReadFileException;
import com.linh.nphc.hrapp.exceptions.UnableToSaveEmployeeException;
import com.linh.nphc.hrapp.models.MessageResponse;
import com.linh.nphc.hrapp.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadRestController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    @RequestMapping("/users/upload")
    public ResponseEntity<MessageResponse> upload(@RequestParam("file") MultipartFile file){
        try{
            employeeService.processFile(file);
            return new ResponseEntity<>(new MessageResponse("Data is created"), HttpStatus.CREATED);
        } catch (UnableToSaveEmployeeException ex){
            return new ResponseEntity<>(new MessageResponse("File is uploaded but not processed - "+ex.getMessage()), HttpStatus.OK);
        } catch (UnableToReadFileException | InvalidFieldException | DuplicateRowException ex){
            return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }

    }

}
