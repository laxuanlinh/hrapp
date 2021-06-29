package com.linh.nphc.hrapp.services;

import com.linh.nphc.hrapp.exceptions.DuplicateRowException;
import com.linh.nphc.hrapp.exceptions.InvalidFieldException;
import com.linh.nphc.hrapp.exceptions.UnableToReadFileException;
import com.linh.nphc.hrapp.exceptions.UnableToSaveEmployeeException;
import com.linh.nphc.hrapp.models.Employee;
import com.linh.nphc.hrapp.repositories.EmployeeRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Log4j2
public class EmployeeService {

    private final static int COLUMN_ID = 0;
    private final static int COLUMN_LOGIN = 1;
    private final static int COLUMN_NAME = 2;
    private final static int COLUMN_SALARY = 3;
    private final static int COLUMN_START_DATE = 4;
    private final static String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public void processFile(MultipartFile file){
        List<String[]> contentRows = this.getFileContent(file);
        List<String> idList = new ArrayList<>();
        contentRows.forEach(row -> {
            Employee employee = this.convertToEmployee(row);

            this.validateEmployee(employee);

            if (employee.getId().startsWith("#")){
                return;
            }
            if (idList.contains(employee.getId())){
                throw new DuplicateRowException(String.format("ID %s is duplicated", employee.getId()));
            }
            idList.add(employee.getId());
            this.saveEmployee(employee);
        });

    }

    private List<String[]> getFileContent(MultipartFile file){
        try {
            Reader reader = new InputStreamReader(file.getInputStream());
            CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).withFieldAsNull(CSVReaderNullFieldIndicator.BOTH).build();
            return csvReader.readAll();
        } catch (IOException | CsvException e) {
            throw new UnableToReadFileException(String.format("Unable to read file %s", e.getMessage()));
        }
    }

    private Employee convertToEmployee(String[] row){
        try {
            String id = StringUtils.isBlank(row[COLUMN_ID]) ? null : row[COLUMN_ID];
            String login = StringUtils.isBlank(row[COLUMN_LOGIN]) ? null : row[COLUMN_LOGIN];
            String name = StringUtils.isBlank(row[COLUMN_NAME]) ? null : row[COLUMN_NAME];
            LocalDate startDate = LocalDate.parse(row[COLUMN_START_DATE], DateTimeFormatter.ofPattern(DATE_FORMAT));
            Double salary = StringUtils.isBlank(row[COLUMN_SALARY]) ? null : Double.valueOf(row[COLUMN_SALARY]);

            return new Employee(id, login, name, salary, startDate);
        } catch (DateTimeParseException ex){
            throw new UnableToReadFileException(String.format("Unable to parse date %s", row[COLUMN_START_DATE]));
        } catch (NumberFormatException ex){
            throw new UnableToReadFileException(String.format("Unable to parse number %s", row[COLUMN_SALARY]));
        }
    }

    private void validateEmployee(Employee employee){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        for (ConstraintViolation<Employee> violation : violations) {
            throw new InvalidFieldException(String.format("Unable to process employee %s - %s", employee.toString(), violation.getMessage()));
        }
    }

    private void saveEmployee(Employee employee){
        try{
            employeeRepository.save(employee);
        } catch (DataIntegrityViolationException e){
            throw new UnableToSaveEmployeeException(String.format("Unable to save employee %s due to constraint violation", employee.toString()));
        } catch (Exception e){
            throw new UnableToSaveEmployeeException(String.format("Unable to save employee %s", employee.toString()));
        }
    }

}
