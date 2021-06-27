package com.linh.nphc.hrapp.repositories;

import com.linh.nphc.hrapp.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;

@RepositoryRestController
public interface EmployeeRepository extends JpaRepository<Employee, String> {
}
