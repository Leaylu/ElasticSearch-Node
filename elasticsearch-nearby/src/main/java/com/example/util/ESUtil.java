package com.example.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * @Author:Luo Yiyong
 * @Date: 2019/7/10 0010 13:16
 * @Version: 1.0.0
 * @Description:
 * 6.0废弃了indexType
 * 请参考: @link{https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-index-field.html}
 * fild属性:废弃了string全部使用text
 * 新的请求路径:
 * http:ip:9200/{index}/_doc/{id}
 * 在7.0中，_DOC表示端点名称而不是文档类型。该_DOC组件是文件路径的永久组成部分INDEX， GET以及DELETEAPI的前进，而不会在8.0中删除。
 * 官网解释:@link{https://www.elastic.co/guide/en/elasticsearch/reference/current/removal-of-types.html}
 */
@Slf4j
public class ESUtil {
	/**
	 * 连接地址
	 */
	private final static String ES_URL = "192.168.137.186";
	/**
	 * 端口：节点通讯端是9300,http是9200
	 * 官方现在更加支持通过http来操作elasticsearch
	 */
	private final static int  ES_PORT = 9200;

	/**
	 * 客户端对象
	 */
	private  static RestHighLevelClient client = null;


	/**
	 * 索引名称
	 */
	public final static String INDEX_NAME= "pharmacy";
	/**
	 * id名称
	 */
	public final static String ID_NAME = "id";

	/**
	 * 初始化客户端
	 */
	static {
		/*RestClientBuilder builder = RestClient.builder(new HttpHost(ES_URL,ES_PORT,"http")).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
			@Override
			public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
				requestConfigBuilder.setConnectTimeout(5000);
				requestConfigBuilder.setSocketTimeout(60000);
				requestConfigBuilder.setConnectionRequestTimeout(1000);
				return requestConfigBuilder;
			}
		}).setMaxRetryTimeoutMillis(5*60*1000);*/
		//RestClientBuilder build = RestClient.builder(new HttpHost(ES_URL,ES_PORT,"http")).setMaxRetryTimeoutMillis(5*60*1000);
		// 设置请求超时时间
		RestClientBuilder build = RestClient.builder(new Node(new HttpHost(ES_URL,ES_PORT,"http"))).setRequestConfigCallback(
				new RestClientBuilder.RequestConfigCallback() {
					@Override
					public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
						return requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(60000);
					}
				}
		);
		client = new RestHighLevelClient(build);

	}

	/**
	 * 创建索引: 6.0就废弃了indexType
	 * @param indexName 索引名称
	 */
	public static void createIndex(String indexName) throws IOException {

		CreateIndexRequest request = new CreateIndexRequest(indexName);
		// 设置映射

		request.mapping(createMapping());
		// 同步创建
		CreateIndexResponse createIndexResponse = client.indices()
				.create(request,RequestOptions.DEFAULT);

		boolean acknowledged = createIndexResponse.isAcknowledged();

		if(acknowledged){
			log.info("创建成功");
		}else{
			log.info("创建失败");
		}
	}

	public static void createMapping(String indexName) throws IOException {
		AcknowledgedResponse acknowledgedResponse = client.indices().putMapping(createMapping2(indexName),RequestOptions.DEFAULT);

		if(acknowledgedResponse.isAcknowledged()){
			log.info("创建Mapping成功");
		}
	}


	/**
	 * 索引构建
	 * @return
	 * @throws IOException
	 */
	public static PutMappingRequest createMapping2(String indexName) throws IOException {
		PutMappingRequest putMappingRequest = new PutMappingRequest(indexName);
		XContentBuilder builder = createMapping();
		return  putMappingRequest.source(builder);
	}


	/**
	 * 索引模型构建
	 * @return
	 * @throws IOException
	 */
	public static XContentBuilder createMapping() throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		{
				builder.startObject("properties");
				{
					// 编号
					builder.startObject("id");
					{
						builder.field("type", "text");
					}
					builder.endObject();
					// 药店名
					builder.startObject("name");
					{
						builder.field("type", "text");
					}
					builder.endObject();
					// 评价星级
					builder.startObject("star");
					{
						builder.field("type", "long");
					}
					builder.endObject();
					// 药店地址
					builder.startObject("drugstoreAddress");
					{
						builder.field("type", "text");
					}
					builder.endObject();
					// 药店经纬度
					builder.startObject("location");
					{
						builder.field("type", "geo_point");
					}
					builder.endObject();
				}
				builder.endObject();

		}
		builder.endObject();
		return builder;
	}

	/**
	 * 数据添加
	 * @param indexName
	 * @return 响应数量
	 */
	public static Integer addIndex(String indexName, List<Map<String,Object>> list)throws IOException{
		// 批量操作对象
		BulkRequest request = new BulkRequest();

		Random random = new Random();
		list.stream().forEach(map -> {
			try {
				map.put("star",random.nextInt(5)+1);
				IndexRequest indexRequest = new IndexRequest(indexName);
				indexRequest.id(map.get("id").toString());
				indexRequest.source(toJsonBuilder(map));
				request.add(indexRequest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		BulkResponse bulkResponse  = client.bulk(request, RequestOptions.DEFAULT);

		if(bulkResponse.hasFailures()){
			log.info("索引数据添加失败!");
		}else {
			log.info("添加成功");
		}

		return request.numberOfActions();
	}

	/**
	 * 构建药店映射模型
	 * @param map
	 * @return
	 * @throws IOException
	 */
	public static XContentBuilder toJsonBuilder(Map<String,Object> map) throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject().field("id",map.get("id"))
				.field("name",map.get("name"))
				.field("star",map.get("star"))
				.field("drugstoreAddress",map.get("drugstore_address"))
				.startArray("location").value(map.get("longitude")).value(map.get("latitude"))
				.endArray()
				.endObject();
		return builder;
	}

	/**
	 * 已经可以实现了的
	 * @throws IOException
	 */
	public static void searchPharmacy() throws IOException {
		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		List<GeoPoint> points = new ArrayList<>();
		double lat = 30.22728200;
		double lon = 120.19089900;
		String field = "location";
		// 构建当前位置模型
		GeoPoint point = new GeoPoint().reset(lat,lon);
		points.add(point);

		// 设置搜索药店数
		searchSourceBuilder.from(0);
		searchSourceBuilder.size(1000);

		// lat,lon 纬度，经度,查询距离30.22728200,120.19089900,1米到1000米
		QueryBuilder builder = QueryBuilders.geoDistanceQuery(field).point(point)
				.distance("1000",DistanceUnit.METERS)
				// 计算距离模式: plan更快，但在长距离和靠近极点时不准确,ARC默认
				.geoDistance(GeoDistance.ARC);
		searchSourceBuilder.postFilter(builder);
		// 按照远近排序
		GeoDistanceSortBuilder sort = SortBuilders.geoDistanceSort(field,point);

		// 排序
		sort.unit(DistanceUnit.METERS);
		// 升序
		sort.order(SortOrder.ASC);
		// 添加排序等配置
		searchSourceBuilder.sort(sort);

		SortBuilder sortStar = SortBuilders.fieldSort("star");
		sortStar.order(SortOrder.DESC);

		searchSourceBuilder.sort(sortStar);

		searchRequest.source(searchSourceBuilder);
		// 执行查询
		SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
		// 附近的人
		searchResponse(searchResponse);
	}




	public static void searchResponse(SearchResponse searchResponse){
		SearchHits hits = searchResponse.getHits();

		SearchHit[] searchHists = hits.getHits();

		TimeValue useTime = searchResponse.getTook();

		log.info("搜索耗时: {} ms",useTime.getMillis());
		log.info("小丽附近的人: {} 个药店",hits.getTotalHits().value);

		for (SearchHit hit : searchHists){

			String name = hit.getSourceAsMap().get("name").toString();
			int star = (int) hit.getSourceAsMap().get("star");
			List<Double> location = (List<Double>)hit.getSourceAsMap().get("location");

			BigDecimal geoDis = new BigDecimal((Double) hit.getSortValues()[0]);

			Map<String,Object> hitMap = hit.getSourceAsMap();

			hitMap.put("geoDistance",geoDis.setScale(0,BigDecimal.ROUND_HALF_DOWN));

			log.info(name +" 的坐标: "+location+" 距离小丽 "+hit.getSourceAsMap().get("geoDistance")+DistanceUnit.METERS.toString()+"  星级 :"+star);

		}
	}
}
