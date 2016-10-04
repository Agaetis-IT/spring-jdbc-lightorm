package com.agaetis.spring.jdbc.lightorm.repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.jdbc.JdbcTestUtils;

import com.agaetis.spring.jdbc.lightorm.AbstractTest;
import com.agaetis.spring.jdbc.lightorm.test.dao.CarDao;
import com.agaetis.spring.jdbc.lightorm.test.dao.ServiceDao;
import com.agaetis.spring.jdbc.lightorm.test.model.Car;
import com.agaetis.spring.jdbc.lightorm.test.model.Service;
import com.agaetis.spring.jdbc.lightorm.test.model.ServiceId;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class TestLightOrmCrudRepository extends AbstractTest {

	@Autowired
	private CarDao carDao;

	@Autowired
	private ServiceDao serviceDao;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void testFindAll01() {
		List<Car> cars = carDao.findAll();
		Assert.assertThat("Invalid size.", cars, IsCollectionWithSize.hasSize(9));
	}

	@Test
	public void testFindAll02() {
		Iterable<Car> cars = carDao.findAll(Arrays.asList(1l, 2l, 3l));

		Iterator<Car> iterator = cars.iterator();

		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid first car name.", iterator.next().getName(), Is.is("A1"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid second car name.", iterator.next().getName(), Is.is("A3"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid third car name.", iterator.next().getName(), Is.is("A5"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(false));
	}

	@Test
	public void testFindAll03() {
		Iterable<Car> cars = carDao.findAll((Iterable<Long>) null);
		Iterator<Car> iterator = cars.iterator();
		Assert.assertThat("No results.", iterator.hasNext(), Is.is(false));
	}

	@Test
	public void testFindAll04() {
		Iterable<Car> cars = carDao.findAll(new ArrayList<Long>(0));
		Iterator<Car> iterator = cars.iterator();
		Assert.assertThat("No results.", iterator.hasNext(), Is.is(false));
	}

	@Test
	public void testFindOne01() {
		Car car = carDao.findOne(1l);
		Assert.assertThat("Car not found.", car, IsNull.notNullValue());
		Assert.assertThat("Car id error.", car.getId(), Is.is(1l));
		Assert.assertThat("Car brand error.", car.getBrand(), Is.is(1));
		Assert.assertThat("Car name error.", car.getName(), Is.is("A1"));
	}

	@Test
	public void testFindOne02() {
		Car car = carDao.findOne(100l);
		Assert.assertThat("Car exists.", car, IsNull.nullValue());
	}

	@Test
	public void testFindOne03() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, 0, 1);
		calendar = DateUtils.truncate(calendar, Calendar.DATE);

		Service service = serviceDao.findOne(new ServiceId(1l, new Date(calendar.getTimeInMillis())));

		Assert.assertThat("Service not found.", service, IsNull.notNullValue());
		ServiceId id = service.getId();
		Assert.assertThat("Service id not set.", id, IsNull.notNullValue());
		Assert.assertThat("Service id error.", id.getCar(), Is.is(1l));
		Calendar instance = Calendar.getInstance();
		instance.setTime(id.getDate());
		Assert.assertThat("Service id error.", instance.get(Calendar.YEAR), Is.is(2014));
		Assert.assertThat("Service id error.", instance.get(Calendar.MONTH), Is.is(0));
		Assert.assertThat("Service id error.", instance.get(Calendar.DATE), Is.is(1));
	}

	@Test
	public void testExists01() {
		Assert.assertTrue("Car doesn't exist.", carDao.exists(1l));
	}

	@Test
	public void testExists02() {
		Assert.assertFalse("Car exists.", carDao.exists(100l));
	}

	@Test
	public void testExists03() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, 0, 1);
		calendar = DateUtils.truncate(calendar, Calendar.DATE);

		Assert.assertTrue("Car exists.", serviceDao.exists(new ServiceId(1l, new Date(calendar.getTimeInMillis()))));
	}

	@Test
	public void testSave01() {
		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(9));

		Car car = new Car(1, "A6");
		car = carDao.save(car);

		Assert.assertThat("Car not saved.", car.getId(), IsNull.notNullValue());
		Assert.assertThat("Car brand error.", car.getBrand(), Is.is(1));
		Assert.assertThat("Car name error.", car.getName(), Is.is("A6"));

		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(10));
	}

	@Test
	public void testSave02() {
		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(9));

		Car car = new Car(1l, 1, "A6");
		car = carDao.save(car);

		Assert.assertThat("Car not saved.", car.getId(), IsNull.notNullValue());
		Assert.assertThat("Car id error.", car.getId(), Is.is(1l));
		Assert.assertThat("Car brand error.", car.getBrand(), Is.is(1));
		Assert.assertThat("Car name error.", car.getName(), Is.is("A6"));

		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(9));
	}

	@Test
	public void testSave03() {
		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(9));

		Car car = new Car(10l, 1, "A6");
		car = carDao.save(car);

		Assert.assertThat("Car not saved.", car.getId(), IsNull.notNullValue());
		Assert.assertThat("Car id error.", car.getId(), Is.is(10l));
		Assert.assertThat("Car brand error.", car.getBrand(), Is.is(1));
		Assert.assertThat("Car name error.", car.getName(), Is.is("A6"));

		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(10));
	}

	@Test
	public void testSave04() {
		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(9));

		Car car1 = new Car(10l, 1, "A6");
		Car car2 = new Car(1l, 1, "New A1");
		Car car3 = new Car(11l, 1, "X5");

		Iterable<Car> cars = carDao.save(Arrays.asList(car1, car2, car3));

		Iterator<Car> iterator = cars.iterator();

		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid first car name.", iterator.next().getName(), Is.is("A6"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid second car name.", iterator.next().getName(), Is.is("New A1"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid third car name.", iterator.next().getName(), Is.is("X5"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(false));

		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(11));
	}

	@Test
	public void testSave05() {
		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(9));

		Iterable<Car> cars = carDao.save((Iterable<Car>) null);

		Iterator<Car> iterator = cars.iterator();

		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(false));

		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(9));
	}

	@Test
	public void testSave06() {
		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(9));

		Car car = new Car(0l, 1, "A6");
		car = carDao.save(car);

		Assert.assertThat("Car not saved.", car.getId(), IsNull.notNullValue());
		Assert.assertThat("Car id error.", car.getId(), Is.is(10l));
		Assert.assertThat("Car brand error.", car.getBrand(), Is.is(1));
		Assert.assertThat("Car name error.", car.getName(), Is.is("A6"));

		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(10));
	}

	@Test
	public void testSave07() {
		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "service"), Is.is(3));

		Service service = serviceDao.save(new Service(new ServiceId(2l, new Date(System.currentTimeMillis()))));

		Assert.assertThat("Data not saved.", service, IsNull.notNullValue());

		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "service"), Is.is(4));
	}

	@Test
	public void testDelete01() {
		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(9));

		carDao.delete(1l);

		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(8));
	}

	@Test
	public void testDelete02() {
		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(9));

		carDao.delete(new Car(1l, 1, ""));

		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(8));
	}

	@Test
	public void testDelete03() {
		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(9));

		Car car1 = new Car(1l, 1, "");
		Car car2 = new Car(2l, 1, "");
		Car car3 = new Car(10l, 1, "");

		carDao.delete(Arrays.asList(car1, car2, car3));

		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(7));
	}

	@Test
	public void testDelete04() {
		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(9));

		carDao.deleteAll();

		Assert.assertThat("Invalid count.", JdbcTestUtils.countRowsInTable(jdbcTemplate, "car"), Is.is(0));
	}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void testDelete05() {
		carDao.delete((Long) null);
	}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void testDelete06() {
		carDao.delete(new Car(null, 1, ""));
	}
}
