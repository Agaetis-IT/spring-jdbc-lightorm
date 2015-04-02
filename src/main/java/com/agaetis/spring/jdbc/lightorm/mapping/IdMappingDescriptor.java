package com.agaetis.spring.jdbc.lightorm.mapping;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import com.agaetis.spring.jdbc.lightorm.annotation.ClassId;
import com.agaetis.spring.jdbc.lightorm.annotation.Id;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
public class IdMappingDescriptor<ID extends Serializable> extends FieldMappingDescriptor implements Iterable<ColumnMappingDescriptor> {

	private boolean autoIncremented;

	private boolean composite;

	private List<ColumnMappingDescriptor> columns;

	private Class<ID> idClass;

	public IdMappingDescriptor(Class<?> clazz, Field field, String escapedCharacter) {
		super(clazz, field, escapedCharacter);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doInitialize(Class<?> clazz, Field field) {
		idClass = (Class<ID>) field.getType();

		composite = idClass.isAnnotationPresent(ClassId.class);

		autoIncremented = field.getAnnotation(Id.class).autoIncrement();

		if (composite && autoIncremented) {
			throw new InvalidDataAccessResourceUsageException("Invalid declared composite id [" + getField().getName() + "] cannot be autoincremented");
		}

		columns = new LinkedList<ColumnMappingDescriptor>();
		if (composite) {
			Field[] fields = FieldUtils.getAllFields(idClass);
			for (Field f : fields) {
				if (f.isAnnotationPresent(Id.class)) {
					columns.add(new ColumnMappingDescriptor(idClass, f, escapedCharacter));
				}
			}
		} else {
			columns.add(new ColumnMappingDescriptor(clazz, field, escapedCharacter));
		}
	}

	public boolean isAutoIncremented() {
		return autoIncremented;
	}

	public boolean isComposite() {
		return composite;
	}

	public Class<ID> getIdClass() {
		return idClass;
	}

	public List<ColumnMappingDescriptor> getColumns() {
		return columns;
	}

	@Override
	public Iterator<ColumnMappingDescriptor> iterator() {
		return columns.iterator();
	}

	public int size() {
		return columns.size();
	}

}
