package cn.itcast.repository;

import cn.itcast.poji.Item;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ItemRepository extends ElasticsearchRepository<Item,Long> {
    List<Item> findByTitleAndPriceBefore(String s, double d1, double d2);
    List<Item> findByPriceBetween(double price1, double price2);
}
