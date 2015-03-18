package com.agaetis.spring.jdbc.lightorm.test.model;

import com.agaetis.spring.jdbc.lightorm.annotation.Column;
import com.agaetis.spring.jdbc.lightorm.annotation.Id;
import com.agaetis.spring.jdbc.lightorm.annotation.Table;

/**
 * Created by User on 11/03/2015.
 */
@Table("tblcar")
public class Car {

	@Id(autoIncrement = true)
	@Column("carid")
	private Long id;

	@Column("brandref")
	private Integer bandId;

	@Column
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getBandId() {
		return bandId;
	}

	public void setBandId(Integer bandId) {
		this.bandId = bandId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Car car = (Car) o;

		if (id != null ? !id.equals(car.id) : car.id != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
