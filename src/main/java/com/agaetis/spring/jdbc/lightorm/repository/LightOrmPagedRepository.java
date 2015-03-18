package com.agaetis.spring.jdbc.lightorm.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
public abstract class LightOrmPagedRepository<T> extends LightOrmCrudRepository<T> {

	public List<T> findAll(Pageable pageable, String orderColumnName) {
		String sql = "select * from " + getBeanMappingDescriptor().getEscapedTableName() + " order by " + orderColumnName + " offset " + pageable.getPageNumber() + " rows fetch next "
				+ pageable.getPageSize() + " rows only";
		return getJdbcTemplate().query(sql, getRowMapper());
	}
}
