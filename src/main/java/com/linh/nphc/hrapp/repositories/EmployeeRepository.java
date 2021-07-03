package com.linh.nphc.hrapp.repositories;

import com.linh.nphc.hrapp.models.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    @Query("select e from Employee e where  e.salary >= :minSalary " +
            "and e.salary < :maxSalary " +
            "and (:id is null or e.id = :id) " +
            "and (:login is null or e.login = :login) " +
            "and (:name is null or e.name like %:name%)")
    List<Employee> findEmployeesBySalaryRangeAndNameAndLoginAndID(@Param("minSalary")Double minSalary,
                                                                  @Param("maxSalary")Double maxSalary,
                                                                  @Param("id")String id,
                                                                  @Param("login")String login,
                                                                  @Param("name")String name,
                                                                  Pageable pageable);

    Optional<Employee> findByLogin(String login);
}
