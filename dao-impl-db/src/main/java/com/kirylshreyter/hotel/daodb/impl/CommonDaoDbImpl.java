package com.kirylshreyter.hotel.daodb.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.kirylshreyter.hotel.daodb.util.MapperInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.kirylshreyter.hotel.daoapi.ICommon;

@Repository
public class CommonDaoDbImpl implements ICommon {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommonDaoDbImpl.class);

	Map<Object, Object> map = MapperInitializer.MAPPERS_MAP;

	@Inject
	private JdbcTemplate jdbcTemplate;

	@Override
	public <T> Object get(Object obj, Long id) {

		Object object = map.get(obj.getClass().getName());
		Class<?> modelClass = null;
		try {
			modelClass = Class.forName(object.toString());
		} catch (ClassNotFoundException e) {
			return null;
		}
		Object objectModel = null;
		try {
			objectModel = modelClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
		String SELECT_SQL = "SELECT * FROM %s WHERE id = %s";
		String resultTable = null;
		resultTable = configureAffectedTableName(obj);
		SELECT_SQL = String.format(SELECT_SQL, resultTable, id);
		return jdbcTemplate.queryForObject(SELECT_SQL, (RowMapper<T>) objectModel);
	}

	private String configureAffectedTableName(Object obj) {
		String resultTable;
		String[] pr = obj.getClass().getSimpleName().split("(?=\\p{Lu})");

		for (int i = 0; i < pr.length; i++) {
			pr[i] = pr[i].toLowerCase();
		}
		List<String> list = new ArrayList<>();
		list = Arrays.asList(pr);
		Iterator<String> itr = list.iterator();
		resultTable = itr.next().toString();
		while (itr.hasNext()) {
			resultTable = resultTable + "_" + itr.next().toString();
		}
		return resultTable;
	}

	@Override
	public Boolean delete(Object obj, Long id) {

		String fs = "Trying to delete %s with id = %s.";

		LOGGER.info(String.format(fs, obj.getClass().getSimpleName().toLowerCase(), id));

		Integer deletedRows = null;

		String SELECT_SQL = "DELETE FROM %s WHERE id = %s";

		String resultTable = configureAffectedTableName(obj);

		SELECT_SQL = String.format(SELECT_SQL, resultTable, id);

		try {
			deletedRows = jdbcTemplate.update(SELECT_SQL);

		} catch (DataIntegrityViolationException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
		if (deletedRows == 0) {
			String s = "%s was NOT deleted. User with id = %s does not exist.";
			LOGGER.info(String.format(s, obj.getClass().getSimpleName(), id));
			return false;
		} else {
			String s = "%s with id = %s was deleted.";
			LOGGER.info(String.format(s, obj.getClass().getSimpleName(), id));
			return true;
		}
	}

	@Override
	public <T> List<T> getAll(Object obj) {
		Object object = map.get(obj.getClass().getName());
		Class<?> modelClass = null;
		try {
			modelClass = Class.forName(object.toString());
		} catch (ClassNotFoundException e) {
			return null;
		}
		Object objectModel = null;
		try {
			objectModel = modelClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
		String SELECT_SQL = "SELECT * FROM %s";
		String resultTable = null;

		resultTable = configureAffectedTableName(obj);
		if (resultTable.equals("user")){
			SELECT_SQL = SELECT_SQL.concat("s");
		}
		SELECT_SQL = String.format(SELECT_SQL, resultTable);

		try {
			return jdbcTemplate.query(SELECT_SQL, (RowMapper<T>) objectModel);
		} catch (EmptyResultDataAccessException e) {
			return null;

		}
	}
}
