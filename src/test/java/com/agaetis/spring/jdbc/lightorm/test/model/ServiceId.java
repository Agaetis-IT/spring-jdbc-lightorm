package com.agaetis.spring.jdbc.lightorm.test.model;

import java.io.Serializable;
import java.sql.Date;

import com.agaetis.spring.jdbc.lightorm.annotation.ClassId;
import com.agaetis.spring.jdbc.lightorm.annotation.Column;
import com.agaetis.spring.jdbc.lightorm.annotation.Id;

@ClassId
public class ServiceId implements Serializable {

	/**
	 * Default Serial Id.
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column("idcar")
	private Long car;

	@Id
	@Column("servicedate")
	private Date date;

	public ServiceId(Long car, Date date) {
		this.car = car;
		this.date = date;
	}

	public ServiceId() {
	}

	public Long getCar() {
		return car;
	}

	public void setCar(Long car) {
		this.car = car;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((car == null) ? 0 : car.hashCode());
		result = (prime * result) + ((date == null) ? 0 : date.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ServiceId other = (ServiceId) obj;
		if (car == null) {
			if (other.car != null) {
				return false;
			}
		} else if (!car.equals(other.car)) {
			return false;
		}
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!date.equals(other.date)) {
			return false;
		}
		return true;
	}

}
