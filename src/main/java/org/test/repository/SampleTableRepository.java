package org.test.repository;

import org.test.bean.SampleTable ;

import org.springframework.data.repository.CrudRepository;



/**
 * Spring JPA Repository for SampleTable
 * 
 *
 */
public interface SampleTableRepository extends CrudRepository<SampleTable, Long> {


}