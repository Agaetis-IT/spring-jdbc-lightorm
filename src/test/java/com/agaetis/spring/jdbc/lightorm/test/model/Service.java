package com.agaetis.spring.jdbc.lightorm.test.model;

import com.agaetis.spring.jdbc.lightorm.annotation.Id;
import com.agaetis.spring.jdbc.lightorm.annotation.Table;

@Table
public class Service {
	@Id
	private ServiceId id;

	public Service(ServiceId id) {
		this.id = id;
	}

	public Service() {
	}

	public ServiceId getId() {
		return id;
	}

	public void setId(ServiceId id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
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
		Service other = (Service) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
