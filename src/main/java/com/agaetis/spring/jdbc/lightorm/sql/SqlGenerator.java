package com.agaetis.spring.jdbc.lightorm.sql;

import java.util.Collection;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface SqlGenerator {

	public abstract String select(String table);

	public abstract String select(String table, Collection<String> conditions);

	public abstract String select(String table, Pageable pageable);

	public abstract String select(String table, Sort sort);

	public abstract String delete(String table);

	public abstract String delete(String table, Collection<String> conditions);

	public abstract String count(String table);

	public abstract String insert(String table, Collection<String> columns, Collection<String> values);

	public abstract String update(String table, Collection<String> columns, Collection<String> conditions);

}