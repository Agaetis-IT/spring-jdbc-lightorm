package com.agaetis.spring.jdbc.lightorm.mapping;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;

import com.agaetis.spring.jdbc.lightorm.annotation.Column;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
public class ColumnMappingDescriptor extends FieldMappingDescriptor {

	private String columnName;

	private String escapedColumnName;

	public ColumnMappingDescriptor(Class<?> clazz, Field field, String escapedCharacter) {
		super(clazz, field, escapedCharacter);
	}

	@Override
	protected void doInitialize(Class<?> clazz, Field field) {
		if (field.isAnnotationPresent(Column.class) && !StringUtils.isEmpty(field.getAnnotation(Column.class).value())) {
			columnName = field.getAnnotation(Column.class).value();
			columnName = columnName.replaceAll(" ", "").toLowerCase();
		} else {
			columnName = field.getName().toLowerCase();
		}

		escapedColumnName = escapedCharacter + columnName + escapedCharacter;
	}

	public String getEscapedColumnName() {
		return escapedColumnName;
	}

	public String getColumnName() {
		return columnName;
	}

}
