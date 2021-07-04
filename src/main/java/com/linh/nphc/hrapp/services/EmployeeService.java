package com.linh.nphc.hrapp.services;

import com.linh.nphc.hrapp.exceptions.DuplicateRowException;
import com.linh.nphc.hrapp.exceptions.InvalidFieldException;
import com.linh.nphc.hrapp.exceptions.UnableToReadFileException;
import com.linh.nphc.hrapp.exceptions.UnableToSaveEmployeeException;
import com.linh.nphc.hrapp.models.Employee;
import com.linh.nphc.hrapp.models.EmployeeDTO;
import com.linh.nphc.hrapp.repositories.EmployeeRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
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
import java.util.*;

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
            LocalDate startDate = this.parseDate(row[COLUMN_START_DATE]);
            Double salary = StringUtils.isBlank(row[COLUMN_SALARY]) ? null : Double.valueOf(row[COLUMN_SALARY]);

            return new Employee(id, login, name, salary, startDate);
        } catch (NumberFormatException ex){
            throw new UnableToReadFileException(String.format("Unable to parse number %s", row[COLUMN_SALARY]));
        }
    }

    private LocalDate parseDate(String dateStr){
        String[] dateFormats = {"yyyy-MM-dd", "dd-MMM-yy"};
        for (String format : dateFormats){
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException ignored) {}
        }

        throw new UnableToReadFileException(String.format("Invalid date %s", dateStr));
    }

    private void validateEmployee(Employee employee){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        for (ConstraintViolation<Employee> violation : violations) {
            throw new InvalidFieldException(violation.getMessage());
        }
    }

    private void saveEmployee(Employee employee){
        try{
            employeeRepository.save(employee);
        } catch (Exception e){
            throw new UnableToSaveEmployeeException(String.format("Unable to save employee %s", employee.toString()));
        }
    }

    @Transactional
    public List<Employee> getEmployees(Double minSalary, Double maxSalary, String id, String login, String name, Pageable pageable){
        return this.employeeRepository.findEmployeesBySalaryRangeAndNameAndLoginAndID(minSalary, maxSalary, id, login, name, pageable);
    }

    @Transactional
    public Employee getEmployee(String id){
        return this.employeeRepository.findById(id).orElse(null);
    }

    @Transactional
    public void createEmployee(EmployeeDTO employeeDTO){
        Employee employee = new Employee(employeeDTO.getId(), employeeDTO.getLogin(), employeeDTO.getName(), employeeDTO.getSalary(), this.parseDate(employeeDTO.getStartDate()));
        this.validateEmployee(employee);
        if (this.employeeRepository.findById(employee.getId()).isPresent()){
            throw new InvalidFieldException("Employee ID already exists");
        }
        if (this.employeeRepository.findByLogin(employee.getLogin()).isPresent()){
            throw new InvalidFieldException("Employee login not unique");
        }
        this.saveEmployee(employee);
    }

    @Transactional
    public void updateEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee(employeeDTO.getId(), employeeDTO.getLogin(), employeeDTO.getName(), employeeDTO.getSalary(), this.parseDate(employeeDTO.getStartDate()));
        this.validateEmployee(employee);
        Employee existingEmployee = this.employeeRepository.findById(employee.getId()).orElseThrow(() -> new InvalidFieldException("No such employee"));
        Optional<Employee> existingEmployeeByLogin = this.employeeRepository.findByLogin(employee.getLogin());
        if (this.loginAlreadyExists(existingEmployee, existingEmployeeByLogin)){
            throw new InvalidFieldException("Employee login not unique");
        }
        this.saveEmployee(employee);
    }

    private boolean loginAlreadyExists(Employee existingEmployee, Optional<Employee> existingEmployeeByLogin){
        return existingEmployeeByLogin.isPresent() && !existingEmployee.getId().equals(existingEmployeeByLogin.get().getId());
    }

    public void deleteEmployee(String id) {
        Employee employee = this.employeeRepository.findById(id).orElseThrow(()->new InvalidFieldException("No such employee"));
        employeeRepository.delete(employee);
    }
}












