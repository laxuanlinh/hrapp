package com.linh.nphc.hrapp.models;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
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
    @NotBlank(message = "ID cannot be blank")
    private String id;

    @Column(unique = true)
    @NotNull(message = "Login cannot be null")
    @NotBlank(message = "Login cannot be blank")
    private String login;

    @NotNull(message = "Name cannot be null")
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "Salary cannot be null")
    @DecimalMin(value = "0.0", message = "Invalid salary")
    private Double salary;

    @NotNull(message = "Start date cannot be null")
    private LocalDate startDate;



}
