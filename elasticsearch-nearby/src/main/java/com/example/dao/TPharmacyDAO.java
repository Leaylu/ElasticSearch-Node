package com.example.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Author:Luo Yiyong
 * @Date: 2019/7/10 0010 11:37
 * @Version: 1.0.0
 */
@Mapper
public interface TPharmacyDAO {

	@Select("select * from t_pharmacy")
	List<Map<String,Object>> list();
}
