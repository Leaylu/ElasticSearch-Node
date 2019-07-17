package com.example.service.impl;

import com.example.dao.TPharmacyDAO;
import com.example.service.TPharmacyService;
import com.example.util.ESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @Author:Luo Yiyong
 * @Date: 2019/7/10 0010 13:13
 * @Version: 1.0.0
 */
@Service
public class TPharmacyServiceImpl implements TPharmacyService {

	@Autowired
	TPharmacyDAO pharmacyDAO;

	@Override
	public void delete() throws IOException {

	}

	@Override
	public void distance() throws Exception{
		ESUtil.searchPharmacy();
	}

	@Override
	public void create() throws IOException {
		ESUtil.createIndex(ESUtil.INDEX_NAME);
	}

	@Override
	public void createMapping() throws IOException {
		ESUtil.createMapping(ESUtil.INDEX_NAME);
	}

	@Override
	public void add() throws IOException {
		List<Map<String, Object>> list = pharmacyDAO.list();
		ESUtil.addIndex(ESUtil.INDEX_NAME,list);
	}

	@Override
	public List<Map<String, Object>> list(String longitude, String latitude, Long radius) {

		return pharmacyDAO.list();
	}
}
