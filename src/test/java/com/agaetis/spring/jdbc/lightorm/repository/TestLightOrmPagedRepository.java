package com.agaetis.spring.jdbc.lightorm.repository;

import java.util.Iterator;

import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.agaetis.spring.jdbc.lightorm.AbstractTest;
import com.agaetis.spring.jdbc.lightorm.test.dao.CarDao;
import com.agaetis.spring.jdbc.lightorm.test.model.Car;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
public class TestLightOrmPagedRepository extends AbstractTest {

	@Autowired
	private CarDao carDao;

	@Test
	public void findAll01() {
		Page<Car> cars = carDao.findAll(new PageRequest(1, 2, Direction.ASC, "name"));
		Assert.assertThat("Invalid size.", cars.getContent(), IsCollectionWithSize.hasSize(2));
		Assert.assertThat("Invalid count.", cars.getTotalElements(), Is.is(9l));

		Assert.assertThat("Invalid first element name.", cars.getContent().get(0).getName(), Is.is("A5"));
		Assert.assertThat("Invalid second element name.", cars.getContent().get(1).getName(), Is.is("A7"));
	}

	@Test
	public void findAll02() {
		Page<Car> cars = carDao.findAll(new PageRequest(1, 2, Direction.DESC, "name"));
		Assert.assertThat("Invalid size.", cars.getContent(), IsCollectionWithSize.hasSize(2));
		Assert.assertThat("Invalid count.", cars.getTotalElements(), Is.is(9l));

		Assert.assertThat("Invalid second element name.", cars.getContent().get(0).getName(), Is.is("Clio"));
		Assert.assertThat("Invalid first element name.", cars.getContent().get(1).getName(), Is.is("Captur"));
	}

	@Test
	public void findAll03() {
		Page<Car> cars = carDao.findAll(new PageRequest(0, 4));
		Assert.assertThat("Invalid size.", cars.getContent(), IsCollectionWithSize.hasSize(4));
		Assert.assertThat("Invalid count.", cars.getTotalElements(), Is.is(9l));

		Assert.assertThat("Invalid first element name.", cars.getContent().get(0).getName(), Is.is("A1"));
		Assert.assertThat("Invalid second element name.", cars.getContent().get(1).getName(), Is.is("A3"));
		Assert.assertThat("Invalid third element name.", cars.getContent().get(2).getName(), Is.is("A5"));
		Assert.assertThat("Invalid fourth element name.", cars.getContent().get(3).getName(), Is.is("A7"));
	}

	@Test
	public void findAll04() {
		Iterable<Car> cars = carDao.findAll(new Sort(new Order(Direction.DESC, "brand"), new Order(Direction.ASC, "name")));

		Iterator<Car> iterator = cars.iterator();

		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid first car name.", iterator.next().getName(), Is.is("C5"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid second car name.", iterator.next().getName(), Is.is("Picasso"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid third car name.", iterator.next().getName(), Is.is("Captur"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid fourth car name.", iterator.next().getName(), Is.is("Clio"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid fifth car name.", iterator.next().getName(), Is.is("Mégane"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid sixth car name.", iterator.next().getName(), Is.is("A1"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid seventh car name.", iterator.next().getName(), Is.is("A3"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid eighth car name.", iterator.next().getName(), Is.is("A5"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(true));
		Assert.assertThat("Invalid ninth car name.", iterator.next().getName(), Is.is("A7"));
		Assert.assertThat("Invalid size.", iterator.hasNext(), Is.is(false));
	}
}
