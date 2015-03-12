package com.agaetis.spring.jdbc.lightorm.repository;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.agaetis.spring.jdbc.lightorm.annotation.Column;
import com.agaetis.spring.jdbc.lightorm.annotation.Id;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a href="http://www.agaetis.fr">Agaetis</a>  on 12/03/2015.
 */
public class FieldMappingDescriptor {
	private Field field;
	private String columnName = null;
	private String escapedColumnName = null;
	private boolean id;
	private boolean idAutoIncrement;
	private Method writeMethod;
	private Method readMethod;
	private String escapedCharacter = "";
	private Class<?> tableClass;

	public FieldMappingDescriptor(Class<?> clazz, Field field, String escapedCharacter) {
		this.escapedCharacter = escapedCharacter;
		initialize(clazz, field);
	}

	public Object readFieldValue(Object obj) {
		try {
			return readMethod.invoke(obj);
		} catch (IllegalAccessException e) {
			throw new InvalidDataAccessResourceUsageException("Impossible de lire le champ [" + getField().getName() + "] de l'objet [" + tableClass.getCanonicalName() + "]", e);
		} catch (InvocationTargetException e) {
			throw new InvalidDataAccessResourceUsageException("Impossible de lire le champ [" + getField().getName() + "] de l'objet [" + tableClass.getCanonicalName() + "]", e);
		}
	}

	public void writeFieldValue(Object obj, Object value) {
		try {
			writeMethod.invoke(obj, value);
		} catch (IllegalAccessException e) {
			throw new InvalidDataAccessResourceUsageException("Impossible d'ecrire dans le champ [" + getField().getName() + "] de l'objet [" + tableClass.getCanonicalName() + "]", e);
		} catch (InvocationTargetException e) {
			throw new InvalidDataAccessResourceUsageException("Impossible d'ecrire dans le champ [" + getField().getName() + "] de l'objet [" + tableClass.getCanonicalName() + "]", e);
		}
	}

	private void initialize(Class<?> clazz, Field field) {
		tableClass = clazz;
		this.field = field;
		PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(clazz, field.getName());
		writeMethod = pd.getWriteMethod();
		readMethod = pd.getReadMethod();

		if (field.isAnnotationPresent(Id.class)) {
			id = true;
			idAutoIncrement = field.getAnnotation(Id.class).autoIncrement();
		}

		if (field.isAnnotationPresent(Column.class) && !StringUtils.isEmpty(field.getAnnotation(Column.class).value())) {
			columnName = field.getAnnotation(Column.class).value();
			columnName = columnName.replaceAll(" ", "").toLowerCase();
		} else {
			columnName = field.getName().toLowerCase();
		}

		escapedColumnName = escapedCharacter + columnName + escapedCharacter;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getEscapedColumnName() {
		return escapedColumnName;
	}

	public Method getWriteMethod() {
		return writeMethod;
	}

	public boolean isId() {
		return id;
	}

	public boolean isIdAutoIncrement() {
		return idAutoIncrement;
	}

	public Method getReadMethod() {
		return readMethod;
	}

	public Field getField() {
		return field;
	}
}
