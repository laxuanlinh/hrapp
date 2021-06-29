package com.linh.nphc.hrapp.models;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Employee {

    @Id
    @NotNull(message = "ID cannot be null")
    private String id;

    @Column(unique = true)
    @NotNull(message = "Login cannot be null")
    private String login;

    @NotNull(message = "Name cannot be null")
    private String name;

    @NotNull(message = "Salary cannot be null")
    @DecimalMin(value = "0.0", message = "Salary must be greater than 0")
    private Double salary;

    @NotNull(message = "Start date cannot be null")
    private LocalDate startDate;



}
