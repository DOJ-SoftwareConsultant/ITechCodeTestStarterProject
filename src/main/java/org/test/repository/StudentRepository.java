package org.test.repository;

import org.springframework.data.repository.CrudRepository;
import org.test.bean.Student;

public interface StudentRepository extends CrudRepository<Student, Long> {

}
