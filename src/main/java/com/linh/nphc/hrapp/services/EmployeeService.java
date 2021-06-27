package com.linh.nphc.hrapp.services;

import com.linh.nphc.hrapp.exceptions.InvalidFileException;
import com.linh.nphc.hrapp.exceptions.MissingFieldException;
import com.linh.nphc.hrapp.exceptions.UnableToReadFileException;
import com.linh.nphc.hrapp.exceptions.UnableToSaveEmployee;
import com.linh.nphc.hrapp.models.Employee;
import com.linh.nphc.hrapp.repositories.EmployeeRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class EmployeeService {

    private final static int COLUMN_ID = 0;
    private final static int COLUMN_LOGIN = 1;
    private final static int COLUMN_NAME = 2;
    private final static int COLUMN_SALARY = 3;
    private final static int COLUMN_START_DATE = 4;

    @Autowired
    private EmployeeRepository employeeRepository;

    public void processFile(MultipartFile file){
        List<String[]> contentRows = this.getFileContent(file);
        List<String> idList = new ArrayList<>();
        contentRows.forEach(row -> {
            Employee employee = this.getValueModel(row);
            if (idList.contains(employee.getId())){
                throw new InvalidFileException(String.format("ID %s is duplicated", employee.getId()));
            }
            idList.add(employee.getId());
            employeeRepository.save(employee);
        });
        employeeRepository.findAll().forEach(System.out::println);  

    }

    private List<String[]> getFileContent(MultipartFile file){
        try {
            Reader reader = new InputStreamReader(file.getInputStream());
            CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
            return csvReader.readAll();
        } catch (IOException | CsvException e) {
            throw new UnableToReadFileException("Unable to read file "+e.getMessage());
        }
    }

    private Employee getValueModel(String[] row){
        try {
            String id = Optional.ofNullable(row[COLUMN_ID]).orElseThrow(() -> new MissingFieldException("ID cannot be null"));
            String login = Optional.ofNullable(row[COLUMN_LOGIN]).orElseThrow(() -> new MissingFieldException("Login cannot be null"));
            String name = Optional.ofNullable(row[COLUMN_NAME]).orElseThrow(() -> new MissingFieldException("Name cannot be null"));
            String salary = Optional.ofNullable(row[COLUMN_SALARY]).orElseThrow(() -> new MissingFieldException("Salary cannot be null"));
            String startDateStr = Optional.ofNullable(row[COLUMN_START_DATE]).orElseThrow(() -> new MissingFieldException("Start date cannot be null"));

            LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            return new Employee(id, login, name, Double.valueOf(salary), startDate);
        } catch (DateTimeParseException ex){
            throw new UnableToReadFileException(String.format("Unable to parse date %s", row[COLUMN_START_DATE]));
        }
    }

}
