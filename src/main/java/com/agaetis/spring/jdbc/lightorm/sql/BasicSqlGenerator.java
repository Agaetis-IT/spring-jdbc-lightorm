package com.agaetis.spring.jdbc.lightorm.sql;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.NullHandling;
import org.springframework.data.domain.Sort.Order;

public class BasicSqlGenerator implements SqlGenerator {

	/* (non-Javadoc)
	 * @see com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#select(java.lang.String)
	 */
	@Override
	public String select(String table) {
		return String.format("SELECT * FROM %s", table);
	}

	/* (non-Javadoc)
	 * @see com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#select(java.lang.String, java.util.Collection)
	 */
	@Override
	public String select(String table, Collection<String> conditions) {
		return String.format("SELECT * FROM %s WHERE %s", table, StringUtils.join(conditions, " AND "));
	}

	/* (non-Javadoc)
	 * @see com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#select(java.lang.String, org.springframework.data.domain.Pageable)
	 */
	@Override
	public String select(String table, Pageable pageable) {
		StringBuilder sql = new StringBuilder(select(table, pageable.getSort()));

		sql.append(" LIMIT ").append(pageable.getPageSize()).append(" OFFSET ").append(pageable.getOffset());

		return sql.toString();
	}

	/* (non-Javadoc)
	 * @see com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#select(java.lang.String, org.springframework.data.domain.Sort)
	 */
	@Override
	public String select(String table, Sort sort) {
		StringBuilder sql = new StringBuilder(select(table));

		if (sort != null) {
			sql.append(" ORDER BY");
			for (Order order : sort) {
				sql.append(" ").append(order.getProperty()).append(" ").append(order.getDirection().name());
				NullHandling nulls = order.getNullHandling();
				if (nulls != null) {
					switch (nulls) {
					case NULLS_FIRST:
						sql.append(" NULLS FIRST ");
						break;
					case NULLS_LAST:
						sql.append(" NULLS LAST ");
						break;
					default:
					}
				}
				sql.append(",");
			}
			sql.deleteCharAt(sql.length() - 1);
		}

		return sql.toString();
	}

	/* (non-Javadoc)
	 * @see com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#delete(java.lang.String)
	 */
	@Override
	public String delete(String table) {
		return String.format("DELETE FROM %s", table);
	}

	/* (non-Javadoc)
	 * @see com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#delete(java.lang.String, java.util.Collection)
	 */
	@Override
	public String delete(String table, Collection<String> conditions) {
		return String.format("DELETE FROM %s WHERE %s", table, StringUtils.join(conditions, " AND "));
	}

	/* (non-Javadoc)
	 * @see com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#count(java.lang.String)
	 */
	@Override
	public String count(String table) {
		return String.format("SELECT COUNT(*) FROM %s", table);
	}

	/* (non-Javadoc)
	 * @see com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#insert(java.lang.String, java.util.Collection, java.util.Collection)
	 */
	@Override
	public String insert(String table, Collection<String> columns, Collection<String> values) {
		return String.format("INSERT INTO %s (%s) VALUES (%s)", table, StringUtils.join(columns, ","), StringUtils.join(values, ","));
	}

	/* (non-Javadoc)
	 * @see com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#update(java.lang.String, java.util.Collection, java.util.Collection)
	 */
	@Override
	public String update(String table, Collection<String> columns, Collection<String> conditions) {
		return String.format("UPDATE %s SET %s WHERE %s", table, StringUtils.join(columns, ","), StringUtils.join(conditions, " AND "));
	}
}
