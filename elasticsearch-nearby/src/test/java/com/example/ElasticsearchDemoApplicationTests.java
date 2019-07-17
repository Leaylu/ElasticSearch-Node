package com.example;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ElasticsearchDemoApplicationTests {

	private final static Logger logger = LoggerFactory.getLogger(ElasticsearchDemoApplicationTests.class);

	@Test
	public void contextLoads ()throws IOException {

		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		{
			builder.startObject("pharmacy");
			{
				builder.startObject("properties");
				{
					builder.startObject("id").field("type","text").endObject();
					builder.startObject("name").field("type","text").endObject();
					builder.startObject("star").field("type","long").endObject();
					builder.startObject("drugstoreAddress").field("type","text").endObject();
					builder.startObject("location").field("type","geo_point").endObject();
				}
				builder.endObject();
			}
			builder.endObject();
		}
		builder.endObject();

		logger.info("/n {}",builder);
	}

}
