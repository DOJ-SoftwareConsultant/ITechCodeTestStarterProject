/**
 * 
 */
package org.test.webservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.test.bean.Category;
import org.test.bean.CategorySports;
import org.test.bean.Student;
import org.test.bean.StudentSport;
import org.test.repository.CategoryRepository;
import org.test.repository.CategorySportsRepositiry;
import org.test.repository.StudentRepository;
import org.test.repository.StudentSportRepository;




/**
 * @author Dinesh.Rajput
 *
 */
@Controller
public class RestServiceController {
	
	@Autowired
	StudentSportRepository studentSportRepository;
	@Autowired
	CategoryRepository categoryRepository; 
	@Autowired
	CategorySportsRepositiry categorySportsRepositiry;
	@Autowired
	StudentRepository studentRepository;
	
	@RequestMapping(value="/students/{studentId}")
	public @ResponseBody Map<String, Object> fetchSportsForStudent(@PathVariable Long studentId ){
		Map<String, Object> model = new HashMap<String, Object>();
		List<StudentSport> studentSports = studentSportRepository.findByStudentId(studentId);
		Student student = studentRepository.findOne(studentId);
		model.put("student", student);
		if(studentSports != null && !studentSports.isEmpty()){
			Category category = categoryRepository.findOne(studentSports.get(0).getCategoryId());
			model.put("category", category);
			List<CategorySports> listOfSports = new ArrayList<CategorySports>();
			for(StudentSport studentSport : studentSports){
				listOfSports.add(categorySportsRepositiry.findOne(studentSport.getSportId()));
			}
			model.put("sports", listOfSports);
		}
		return model;
	}
	
	//using only data mocking 
	@RequestMapping(value="/data")
	public @ResponseBody Map<String, Object> test(@PathVariable Long id ){
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("message", "Success");
		model.put("status", "True");
		Student student = new Student(111l, "Rahul", "V", "B");
		studentRepository.save(student);
		Category category = new Category(11l, "Atheletics");
		categoryRepository.save(category);
		CategorySports sports = new CategorySports(1l, "Running", 11l);
		categorySportsRepositiry.save(sports);
		sports = new CategorySports(2l, "Walking", 11l);
		categorySportsRepositiry.save(sports);
		StudentSport studentSport = new StudentSport(1l, 11l, 111l);
		studentSportRepository.save(studentSport);
		studentSport = new StudentSport(2l, 11l, 111l);
		studentSportRepository.save(studentSport);
		return model;
	}
}
