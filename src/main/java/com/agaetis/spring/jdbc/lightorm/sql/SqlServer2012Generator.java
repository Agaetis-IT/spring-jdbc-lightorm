package com.agaetis.spring.jdbc.lightorm.sql;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

public class SqlServer2012Generator extends BasicSqlGenerator {

	@Override
	public String select(String table, Pageable pageable) {
		StringBuilder sql = new StringBuilder(select(table, pageable.getSort()));

		sql.append(" OFFSET ").append(pageable.getOffset()).append(" ROWS FECH NEXT ").append(pageable.getPageSize()).append(" ROWS ONLY");

		return sql.toString();
	}

	@Override
	public String select(String table, Sort sort) {
		StringBuilder sql = new StringBuilder(select(table));

		if (sort != null) {
			sql.append(" ORDER BY");
			for (Order order : sort) {
				sql.append(" ").append(order.getProperty()).append(" ").append(order.getDirection().name()).append(",");
			}
			sql.deleteCharAt(sql.length() - 1);
		}

		return sql.toString();
	}
}
