package com.example.controller;


import com.example.service.TPharmacyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @Author:Luo Yiyong
 * @Date: 2019/7/10 0010 11:39
 * @Version: 1.0.0
 */
@RestController
public class HomeController {

	@Autowired
	TPharmacyService pharmacyService;

	@GetMapping("/list")
	public List<Map<String,Object>> list(){
		return pharmacyService.list(null,null,1000L);
	}

	@GetMapping("/add")
	public void add() throws IOException {
		pharmacyService.add();
	}

	@GetMapping("/create")
	public void create() throws IOException {
		pharmacyService.create();
	}


	@GetMapping("/createmapping")
	public void createMapping() throws IOException {
		pharmacyService.createMapping();
	}


	@GetMapping("/delete")
	public void delete() throws IOException {
		pharmacyService.delete();
	}

	@GetMapping("/distance")
	public void distance() throws Exception {
		pharmacyService.distance();
	}
}
