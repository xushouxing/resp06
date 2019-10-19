package cn.itcast;
import cn.itcast.poji.Item;
import cn.itcast.repository.ItemRepository;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ElasticsearchTest {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private ItemRepository itemRepository;
    @Test
    public void createIndex(){
        elasticsearchTemplate.createIndex(Item.class);
        elasticsearchTemplate.putMapping(Item.class);
    }
    @Test
    public void indexList() {
        List<Item> list = new ArrayList<>();
        list.add(new Item(1L, "小米手机7", "手机", "小米", 3299.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(2L, "坚果手机R1", "手机", "锤子", 3699.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(3L, "华为META10", "手机", "华为", 4499.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(4L, "小米Mix2S", "手机", "小米", 4299.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(5L, "荣耀V10", "手机", "华为", 2799.00, "http://image.leyou.com/13123.jpg"));
        // 接收对象集合，实现批量新增
        itemRepository.saveAll(list);
    }
    @Test
    public void query(){
        Iterable<Item> items = itemRepository.findAll();
        items.forEach(item -> {
            System.out.println(item);
        });
    }
    @Test
    public void query1(){
        List<Item> items = itemRepository.findByTitleAndPriceBefore("手机", 1000, 4000);
        items.forEach(item -> {
            System.out.println(item);
        });
    }
    @Test
    public void testQuery0(){
        // 词条查询
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title", "小米");
        // 执行查询
        Iterable<Item> items = this.itemRepository.search(queryBuilder);
        items.forEach(System.out::println);
    }
    @Test
    public void query2(){
        Iterable<Item> items = itemRepository.findByPriceBetween(1000l, 4000l);
        items.forEach(item -> {
            System.out.println(item);
        });
    }
    @Test
    public void testQuery(){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本的分词查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("title", "小米"));
        // 执行搜索，获取结果
        Page<Item> items = this.itemRepository.search(queryBuilder.build());
        // 打印总条数
        System.out.println(items.getTotalElements());
        // 打印总页数
        System.out.println(items.getTotalPages());
        items.forEach(System.out::println);
    }
        @Test
        public void testNativeQuery(){
            // 构建查询条件
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            // 添加基本的分词查询
            queryBuilder.withQuery(QueryBuilders.termQuery("category", "手机"));

            // 初始化分页参数
            int page = 0;
            int size = 3;
            // 设置分页参数
            queryBuilder.withPageable(PageRequest.of(page, size));

            // 执行搜索，获取结果
            Page<Item> items = this.itemRepository.search(queryBuilder.build());
            // 打印总条数
            System.out.println(items.getTotalElements());
            // 打印总页数
            System.out.println(items.getTotalPages());
            // 每页大小
            System.out.println(items.getSize());
            // 当前页
            System.out.println(items.getNumber());
            items.forEach(System.out::println);
        }
    @Test
    public void testSort() {
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本的分词查询
        queryBuilder.withQuery(QueryBuilders.termQuery("category", "手机"));

        // 排序
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));

        // 执行搜索，获取结果
        Page<Item> items = this.itemRepository.search(queryBuilder.build());
        // 打印总条数
        System.out.println(items.getTotalElements());
        items.forEach(System.out::println);
    }
    //聚合查询 brand分组
    @Test
    public void query5(){
        NativeSearchQueryBuilder queryBuilder=new NativeSearchQueryBuilder();
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        queryBuilder.addAggregation(AggregationBuilders.terms("brands").field("brand"));
        AggregatedPage aggPage = (AggregatedPage)itemRepository.search(queryBuilder.build());
        StringTerms brands =(StringTerms)aggPage.getAggregation("brands");
        List<StringTerms.Bucket> buckets = brands.getBuckets();
        buckets.forEach(bucket -> {
            System.out.println(bucket.getKeyAsString()+"-"+bucket.getDocCount());
        });
    }
    @Test
    public void query6(){
        NativeSearchQueryBuilder queryBuilder=new NativeSearchQueryBuilder();
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        queryBuilder.addAggregation(AggregationBuilders.terms("brands").field("brand").subAggregation(AggregationBuilders.avg("price_avg")).field("price"));
        AggregatedPage aggPage = (AggregatedPage)itemRepository.search(queryBuilder.build());
        StringTerms brands =(StringTerms)aggPage.getAggregation("brands");
        List<StringTerms.Bucket> buckets = brands.getBuckets();
        buckets.forEach(bucket -> {
            System.out.println(bucket.getKeyAsString()+"-"+bucket.getDocCount());
            InternalAvg price_avg = (InternalAvg)bucket.getAggregations().asMap().get("price_avg");
            System.out.println(price_avg.getValue());
        });
    }
}
