package com.linh.nphc.hrapp.controllers;

import com.linh.nphc.hrapp.exceptions.InvalidFieldException;
import com.linh.nphc.hrapp.models.*;
import com.linh.nphc.hrapp.services.EmployeeService;
import org.apache.coyote.Response;
import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class EmployeeRestController {

    private static final String DEFAULT_MIN_SALARY = "0";
    private static final String DEFAULT_MAX_SALARY = "4000.00";
    private static final String DEFAULT_OFFSET = "0";
    private static final String DEFAULT_SORT = "id";
    private static final String DEFAULT_ORDER = "ASC";

    @Autowired
    private EmployeeService employeeService;

    @GetMapping(value = "/users")
    public ResponseEntity<Object> getEmployees( @RequestParam(name = "minSalary", required = false, defaultValue = DEFAULT_MIN_SALARY) Double minSalary,
                                                @RequestParam(name = "maxSalary", required = false, defaultValue = DEFAULT_MAX_SALARY) Double maxSalary,
                                                @RequestParam(name = "id", required = false) String id,
                                                @RequestParam(name = "login", required = false) String login,
                                                @RequestParam(name = "name", required = false) String name,
                                                @RequestParam(name = "offset", required = false, defaultValue = DEFAULT_OFFSET) Integer offset,
                                                @RequestParam(name = "limit", required = false) Integer limit,
                                                @RequestParam(name = "sort", required = false, defaultValue = DEFAULT_SORT) String sort,
                                                @RequestParam(name = "order", required = false, defaultValue = DEFAULT_ORDER) String order){
        try{
            this.validateRequest(minSalary, maxSalary, offset, limit, sort, order);
            Pageable pageable = this.getPagination(offset, limit, sort, order);

            return new ResponseEntity<>(new EmployeeResponse(this.employeeService.getEmployees(minSalary, maxSalary, id, login, name, pageable)), HttpStatus.OK);
        } catch (InvalidFieldException e){
            return new ResponseEntity<>(new MessageResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            return new ResponseEntity<>(new MessageResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateRequest(Double minSalary, Double maxSalary, Integer offset, Integer limit, String sort, String orderStr){
        if (minSalary != null && minSalary < 0){
            throw new InvalidFieldException("Min salary must be greater than 0");
        }
        if (maxSalary != null && maxSalary < 0){
            throw new InvalidFieldException("Max salary must be greater than 0");
        }
        if (minSalary != null && maxSalary != null && maxSalary < minSalary){
            throw new InvalidFieldException("Max salary cannot be less than min salary");
        }

        if (offset != null && offset < 0){
            throw new InvalidFieldException("Offset must be greater than 0");
        }
        if (limit != null && limit < 0){
            throw new InvalidFieldException("Limit must be greater than 0");
        }
        if (sort != null && !Stream.of("id", "name", "login", "salary").collect(Collectors.toList()).contains(sort)){
            throw new InvalidFieldException("Invalid sorting");
        };
        if (!this.containsEnum(orderStr)){
            throw new InvalidFieldException("Invalid order");
        };
    }

    private boolean containsEnum(String orderStr){
        for(Sort.Direction direction : Sort.Direction.values()){
            if(direction.name().equalsIgnoreCase(orderStr)){
                return true;
            }
        }
        return false;
    }

    private Pageable getPagination(Integer offset,
                                  Integer limit,
                                  String sort,
                                  String orderStr){
        limit = Optional.ofNullable(limit).orElse(Integer.MAX_VALUE);
        Sort.Direction order = orderStr == null ? Sort.Direction.ASC : Sort.Direction.valueOf(orderStr);
        return new OffsetBasedPageRequest(offset, limit, Sort.by(order, sort));
    }

    @GetMapping(value = "/users/{id}")
    public ResponseEntity<Object> getEmployee(@PathVariable("id") String id){
        try{
            if (id == null){
                return new ResponseEntity<>(new MessageResponse("ID cannot be null"), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(employeeService.getEmployee(id), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(new MessageResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/users")
    public ResponseEntity<MessageResponse> createEmployee(@RequestBody EmployeeDTO employee){
        try{
            if (employee == null){
                return new ResponseEntity<>(new MessageResponse("Employee cannot be null"), HttpStatus.BAD_REQUEST);
            }
            employeeService.createEmployee(employee);
            return new ResponseEntity<>(new MessageResponse("Successfully created"), HttpStatus.CREATED);
        } catch (InvalidFieldException e){
            return new ResponseEntity<>(new MessageResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            return new ResponseEntity<>(new MessageResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/users")
    public ResponseEntity<MessageResponse> updateEmployee(@RequestBody EmployeeDTO employee){
        try{
            if (employee == null){
                return new ResponseEntity<>(new MessageResponse("Employee cannot be null"), HttpStatus.BAD_REQUEST);
            }
            employeeService.updateEmployee(employee);
            return new ResponseEntity<>(new MessageResponse("Successfully updated"), HttpStatus.CREATED);
        } catch (InvalidFieldException e){
            return new ResponseEntity<>(new MessageResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            return new ResponseEntity<>(new MessageResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
