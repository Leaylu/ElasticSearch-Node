package com.example.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @Author:Luo Yiyong
 * @Date: 2019/7/10 0010 13:08
 * @Version: 1.0.0
 */

public interface TPharmacyService {


	/**
	 * 搜索当前位置附近的药店
	 * @param longitude 经度
	 * @param latitude 纬度
	 * @param radius  搜索范围
	 * @return
	 */
	List<Map<String,Object>> list(String longitude,String latitude,Long radius);

	/**
	 * 执行数据添加
	 */
	void add() throws IOException;

	/**
	 * 创建索引
	 */
	void create() throws IOException;

	/**
	 * 创建映射
	 */
	void createMapping() throws IOException;

	/**
	 * 删除索引
	 */
	void delete() throws IOException;

	/**
	 * 搜索附近的药店
	 */
	void distance() throws Exception;
}
