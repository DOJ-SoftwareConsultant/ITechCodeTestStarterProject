package org.test.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.test.bean.StudentSport;

public interface StudentSportRepository extends CrudRepository<StudentSport, Long> {
	
	List<StudentSport> findByStudentId(Long studentId);
}
