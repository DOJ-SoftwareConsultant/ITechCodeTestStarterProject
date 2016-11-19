/**
 * 
 */
package org.test.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Dinesh.Rajput
 *
 */
@Entity
@Table(name="CATEGORY_SPORTS")
public class CategorySports implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="SPORT_ID", nullable=false)
	Long sportId;
	
	@Column(name="SPORT_NAME", nullable=false)
	String sportName;
	
	@Column(name="CATEGORY_ID", nullable=false)
	//@ManyToOne
	Long categoryId;
	public Long getSportId() {
		return sportId;
	}
	public void setSportId(Long sportId) {
		this.sportId = sportId;
	}
	public String getSportName() {
		return sportName;
	}
	public void setSportName(String sportName) {
		this.sportName = sportName;
	}
	public Long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((categoryId == null) ? 0 : categoryId.hashCode());
		result = prime * result + ((sportId == null) ? 0 : sportId.hashCode());
		result = prime * result
				+ ((sportName == null) ? 0 : sportName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CategorySports other = (CategorySports) obj;
		if (categoryId == null) {
			if (other.categoryId != null)
				return false;
		} else if (!categoryId.equals(other.categoryId))
			return false;
		if (sportId == null) {
			if (other.sportId != null)
				return false;
		} else if (!sportId.equals(other.sportId))
			return false;
		if (sportName == null) {
			if (other.sportName != null)
				return false;
		} else if (!sportName.equals(other.sportName))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "CategorySports [sportId=" + sportId + ", sportName="
				+ sportName + ", categoryId=" + categoryId + "]";
	}
	public CategorySports(Long sportId, String sportName, Long categoryId) {
		super();
		this.sportId = sportId;
		this.sportName = sportName;
		this.categoryId = categoryId;
	}
	public CategorySports() {
		super();
	}
	
}
