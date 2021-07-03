package com.linh.nphc.hrapp.models;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EmployeeDTO {

    private String id;
    private String login;
    private String name;
    private Double salary;
    private String startDate;

}
