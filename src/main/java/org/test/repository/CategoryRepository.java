package org.test.repository;

import org.springframework.data.repository.CrudRepository;
import org.test.bean.Category;

public interface CategoryRepository extends CrudRepository<Category, Long> {

}
