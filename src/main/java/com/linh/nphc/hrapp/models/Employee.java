package com.linh.nphc.hrapp.models;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Employee {

    @Id
//    @GenericGenerator(name = "client_id", strategy = "com.eframe.model.generator.ClientIdGenerator")
//    @GeneratedValue(generator = "client_id")
    private String id;

    @Column(unique = true)
    private String login;
    private String name;
    private Double salary;
    private LocalDate startDate;



}
