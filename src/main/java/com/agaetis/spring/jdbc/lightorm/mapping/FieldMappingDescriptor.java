package com.agaetis.spring.jdbc.lightorm.mapping;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
public abstract class FieldMappingDescriptor {
	protected Field field;

	protected Method writeMethod;

	protected Method readMethod;

	protected String escapedCharacter;

	public FieldMappingDescriptor(Class<?> clazz, Field field, String escapedCharacter) {
		this.escapedCharacter = escapedCharacter;
		initialize(clazz, field);
	}

	public Object readFieldValue(Object obj) {
		try {
			return readMethod.invoke(obj);
		} catch (Exception e) {
			throw new InvalidDataAccessResourceUsageException("Unable to read field [" + getField().getName() + "] on [" + readMethod.getClass().getCanonicalName() + "]", e);
		}
	}

	public void writeFieldValue(Object obj, Object value) {
		try {
			writeMethod.invoke(obj, value);
		} catch (Exception e) {
			throw new InvalidDataAccessResourceUsageException("Unable to write field [" + getField().getName() + "] on [" + writeMethod.getClass().getCanonicalName() + "]", e);
		}
	}

	private void initialize(Class<?> clazz, Field field) {
		this.field = field;
		PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(clazz, field.getName());

		writeMethod = pd.getWriteMethod();
		readMethod = pd.getReadMethod();

		doInitialize(clazz, field);
	}

	protected abstract void doInitialize(Class<?> clazz, Field field);

	public Method getWriteMethod() {
		return writeMethod;
	}

	public Method getReadMethod() {
		return readMethod;
	}

	public Field getField() {
		return field;
	}
}
