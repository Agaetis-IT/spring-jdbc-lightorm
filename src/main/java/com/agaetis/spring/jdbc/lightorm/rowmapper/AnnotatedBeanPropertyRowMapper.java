package com.agaetis.spring.jdbc.lightorm.rowmapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.agaetis.spring.jdbc.lightorm.annotation.ClassId;
import com.agaetis.spring.jdbc.lightorm.annotation.Column;
import com.agaetis.spring.jdbc.lightorm.annotation.Id;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
public class AnnotatedBeanPropertyRowMapper<T> implements RowMapper<T> {

	/** Logger available to subclasses */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/** The class we are mapping to */
	private Class<T> mappedClass;

	/** Whether we're strictly validating */
	private boolean checkFullyPopulated = false;

	/** Whether we're defaulting primitives when mapping a null value */
	private boolean primitivesDefaultedForNullValue = false;

	/** Map fields we provide mapping for and columns */
	private Map<String, PropertyPath> fields;

	/**
	 * Create a new BeanPropertyRowMapper, accepting unpopulated properties in
	 * the target bean.
	 * <p>
	 * Consider using the {@link #newInstance} factory method instead, which
	 * allows for specifying the mapped type once only.
	 *
	 * @param mappedClass
	 *            the class that each row should be mapped to
	 */
	public AnnotatedBeanPropertyRowMapper(Class<T> mappedClass) {
		this(mappedClass, false);
	}

	/**
	 * Create a new BeanPropertyRowMapper.
	 *
	 * @param mappedClass
	 *            the class that each row should be mapped to
	 * @param checkFullyPopulated
	 *            whether we're strictly validating that all bean properties
	 *            have been mapped from corresponding database fields
	 */
	public AnnotatedBeanPropertyRowMapper(Class<T> mappedClass, boolean checkFullyPopulated) {
		Assert.notNull(mappedClass, "Mapped class must not be null.");

		initialize(mappedClass);
		this.checkFullyPopulated = checkFullyPopulated;
	}

	/**
	 * Set the class that each row should be mapped to.
	 */
	public void setMappedClass(Class<T> mappedClass) {
		if (this.mappedClass == null) {
			initialize(mappedClass);
		} else {
			if (!this.mappedClass.equals(mappedClass)) {
				throw new InvalidDataAccessApiUsageException("The mapped class can not be reassigned to map to " + mappedClass + " since it is already providing mapping for " + this.mappedClass);
			}
		}
	}

	/**
	 * Initialize the mapping metadata for the given class.
	 *
	 * @param mappedClass
	 *            the mapped class.
	 */
	protected void initialize(Class<T> mappedClass) {
		this.mappedClass = mappedClass;

		this.fields = new TreeMap<String, PropertyPath>(String.CASE_INSENSITIVE_ORDER);

		for (Field field : mappedClass.getDeclaredFields()) {
			Id id = field.getAnnotation(Id.class);
			Column column = field.getAnnotation(Column.class);

			if ((id == null) && (column == null)) {
				continue;
			}

			if (id != null) {
				Class<?> type = field.getType();
				if (type.isAnnotationPresent(ClassId.class)) {
					String prefix = field.getName() + ".";

					Field[] fields = type.getDeclaredFields();
					for (Field f : fields) {
						if (f.isAnnotationPresent(Id.class)) {
							PropertyPath path = PropertyPath.from(prefix + f.getName(), mappedClass);

							registerColumn(path, f);
						}
					}
				} else {
					PropertyPath path = PropertyPath.from(field.getName(), mappedClass);
					registerColumn(path, field);
				}
			} else if (column != null) {
				PropertyPath path = PropertyPath.from(field.getName(), mappedClass);
				registerColumn(path, field);

			}
		}
	}

	/**
	 * Register column with its property path.
	 *
	 * @param path
	 *            path to the field from root class.
	 */
	private void registerColumn(PropertyPath path, Field field) {

		String columnName = field.getName().toLowerCase();
		if (field.isAnnotationPresent(Column.class)) {
			if (!StringUtils.isEmpty(field.getAnnotation(Column.class).value())) {
				columnName = field.getAnnotation(Column.class).value().toLowerCase();
			}
		}

		this.fields.put(columnName, path);
	}

	/**
	 * Get the class that we are mapping to.
	 */
	public final Class<T> getMappedClass() {
		return this.mappedClass;
	}

	/**
	 * Set whether we're strictly validating that all bean properties have been
	 * mapped from corresponding database fields.
	 * <p>
	 * Default is {@code false}, accepting unpopulated properties in the target
	 * bean.
	 */
	public void setCheckFullyPopulated(boolean checkFullyPopulated) {
		this.checkFullyPopulated = checkFullyPopulated;
	}

	/**
	 * Return whether we're strictly validating that all bean properties have
	 * been mapped from corresponding database fields.
	 */
	public boolean isCheckFullyPopulated() {
		return this.checkFullyPopulated;
	}

	/**
	 * Set whether we're defaulting Java primitives in the case of mapping a
	 * null value from corresponding database fields.
	 * <p>
	 * Default is {@code false}, throwing an exception when nulls are mapped to
	 * Java primitives.
	 */
	public void setPrimitivesDefaultedForNullValue(boolean primitivesDefaultedForNullValue) {
		this.primitivesDefaultedForNullValue = primitivesDefaultedForNullValue;
	}

	/**
	 * Return whether we're defaulting Java primitives in the case of mapping a
	 * null value from corresponding database fields.
	 */
	public boolean isPrimitivesDefaultedForNullValue() {
		return primitivesDefaultedForNullValue;
	}

	/**
	 * Extract the values for all columns in the current row.
	 * <p>
	 * Utilizes public setters and result set metadata.
	 *
	 * @see java.sql.ResultSetMetaData
	 */
	@Override
	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
		Assert.notNull(this.mappedClass, "Mapped class was not specified");

		T result = BeanUtils.instantiate(this.mappedClass);

		DirectFieldAccessor accessor = new DirectFieldAccessor(result);
		initPropertyAccessor(accessor);

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Set<String> populatedProperties = (isCheckFullyPopulated() ? new HashSet<String>() : null);

		for (int index = 1; index <= columnCount; index++) {
			String column = JdbcUtils.lookupColumnName(rsmd, index);
			PropertyPath path = this.fields.get(column);

			if (path == null) {
				continue;
			}

			TypeDescriptor type = accessor.getPropertyTypeDescriptor(path.toDotPath());
			if (type == null) {
				logger.error("Property descriptor doesn't exist for {}.", path);
				throw new InvalidDataAccessApiUsageException("Property descriptor doesn't exist for: " + path);
			}
			try {
				Object value = getColumnValue(rs, index, type);
				if (logger.isDebugEnabled() && (rowNumber == 0)) {
					logger.debug("Mapping column '{}' to property '{}' of type '{}'.", column, path, type.getType());
				}
				try {
					accessor.setPropertyValue(path.toDotPath(), value);
				} catch (TypeMismatchException e) {
					if ((value == null) && primitivesDefaultedForNullValue) {
						logger.debug("Intercepted TypeMismatchException for row {} and column '{}' with value '{}' when setting property '{}' of type '{}' on object: {}.", rowNumber, column, value,
								path, type.getType(), result);
					} else {
						throw e;
					}
				}
				if (populatedProperties != null) {
					populatedProperties.add(path.toDotPath());
				}
			} catch (SQLException ex) {
				throw new DataRetrievalFailureException("Unable to map column " + column + " to property " + path, ex);
			} catch (NotWritablePropertyException ex) {
				throw new DataRetrievalFailureException("Unable to map column " + column + " to property " + path, ex);
			}
		}

		if ((populatedProperties != null) && !populatedProperties.equals(this.fields.keySet())) {
			throw new InvalidDataAccessApiUsageException("Given ResultSet does not contain all fields necessary to populate object of class [" + this.mappedClass + "]: " + this.fields.keySet());
		}

		return result;
	}

	/**
	 * Initialize the given {@link ConfigurablePropertyAccessor} to be used for
	 * row mapping. To be called for each row.
	 * <p>
	 *
	 * @param cpa
	 *            the ConfigurablePropertyAccessor to initialize
	 */
	protected void initPropertyAccessor(ConfigurablePropertyAccessor cpa) {
		cpa.setAutoGrowNestedPaths(true);
	}

	/**
	 * Retrieve a JDBC object value for the specified column.
	 * <p>
	 * The default implementation calls
	 * {@link org.springframework.jdbc.support.JdbcUtils#getResultSetValue(java.sql.ResultSet, int, Class)}
	 * . Subclasses may override this to check specific value types upfront, or
	 * to post-process values return from {@code getResultSetValue}.
	 *
	 * @param rs
	 *            is the ResultSet holding the data
	 * @param index
	 *            is the column index
	 * @param pd
	 *            the bean property that each result object is expected to match
	 *            (or {@code null} if none specified)
	 * @return the Object value
	 * @throws java.sql.SQLException
	 *             in case of extraction failure
	 * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue(java.sql.ResultSet,
	 *      int, Class)
	 */
	protected Object getColumnValue(ResultSet rs, int index, TypeDescriptor type) throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index, type.getType());
	}

	/**
	 * Static factory method to create a new AnnotatedBeanPropertyRowMapper
	 * (with the mapped class specified only once).
	 *
	 * @param mappedClass
	 *            the class that each row should be mapped to
	 */
	public static <T> AnnotatedBeanPropertyRowMapper<T> newInstance(Class<T> mappedClass) {
		return new AnnotatedBeanPropertyRowMapper<T>(mappedClass);
	}

}
