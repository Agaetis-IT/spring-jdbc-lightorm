package com.agaetis.spring.jdbc.lightorm.mapping;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.data.mapping.model.IllegalMappingException;

import com.agaetis.spring.jdbc.lightorm.annotation.Column;
import com.agaetis.spring.jdbc.lightorm.annotation.Id;
import com.agaetis.spring.jdbc.lightorm.annotation.Table;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
public class BeanMappingDescriptor<T, ID extends Serializable> {
	private Class<T> tableClass;

	private String tableName = null;

	private String escapedTableName = null;

	private List<ColumnMappingDescriptor> columnsMappingDescriptors = new LinkedList<ColumnMappingDescriptor>();

	private IdMappingDescriptor<ID> idMappingDescriptor;

	private String escapedCharacter;

	public BeanMappingDescriptor(Class<T> tableClass, String escapedCharacter) {
		this.escapedCharacter = escapedCharacter;
		initialize(tableClass);
	}

	private void initialize(Class<T> tableClass) {
		this.tableClass = tableClass;
		retrieveTableName();
		retrieveFields();
	}

	private void retrieveTableName() {
		String schema = null;
		if (!tableClass.isAnnotationPresent(Table.class)) {
			tableName = tableClass.getSimpleName().toLowerCase();
		} else {
			Table annotation = tableClass.getAnnotation(Table.class);
			if (annotation.value().isEmpty()) {
				tableName = tableClass.getSimpleName().toLowerCase();
			} else {
				tableName = annotation.value();
				if (!annotation.schema().isEmpty()) {
					schema = annotation.schema();
				}
			}
		}
		if (schema == null) {
			escapedTableName = escapedCharacter + tableName + escapedCharacter;
		} else {
			escapedTableName = escapedCharacter + schema + escapedCharacter + "." + escapedCharacter + tableName + escapedCharacter;
		}
	}

	private void retrieveFields() {
		for (Field field : tableClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Column.class)) {
				PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(tableClass, field.getName());

				if (pd.getReadMethod() == null) {
					throw new IllegalMappingException("No  getter for field " + field.getName() + " on class " + tableClass.getCanonicalName());
				}
				if (pd.getWriteMethod() == null) {
					throw new IllegalMappingException("No  setter for field " + field.getName() + " on class " + tableClass.getCanonicalName());
				}

				if (field.isAnnotationPresent(Id.class)) {
					if (idMappingDescriptor != null) {
						throw new IllegalMappingException("Id field already found on class " + tableClass.getCanonicalName());
					}
					idMappingDescriptor = new IdMappingDescriptor<ID>(tableClass, field, escapedCharacter);

				} else {
					columnsMappingDescriptors.add(new ColumnMappingDescriptor(tableClass, field, escapedCharacter));
				}
			}
		}

		validateMappings();
	}

	private boolean validateMappings() {
		// Il faut au moins un Id
		if (idMappingDescriptor == null) {
			throw new IllegalMappingException("No Id Field found on class " + tableClass.getCanonicalName());
		}

		return true;
	}

	public String getTableName() {
		return tableName;
	}

	public List<ColumnMappingDescriptor> getColumnsMappingDescriptors() {
		return columnsMappingDescriptors;
	}

	public IdMappingDescriptor<ID> getIdMappingDescriptor() {
		return idMappingDescriptor;
	}

	public String getEscapedTableName() {
		return escapedTableName;
	}

}
