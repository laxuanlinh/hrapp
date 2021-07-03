package com.linh.nphc.hrapp.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class EmployeeResponse {

    private List<Employee> result;

}
