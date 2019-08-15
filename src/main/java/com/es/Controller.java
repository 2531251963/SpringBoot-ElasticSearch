package com.es;

import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Controller {
    private final TransportClient client;
    @Autowired
    public Controller(TransportClient client) {
        this.client = client;
    }
    @GetMapping(value = "/")
    public String index() {
        return "index";
    }
    @GetMapping(value = "/get/book/novel")
    public String get(@RequestParam(name = "id") String id) {
        if (id.isEmpty()){
            return null;
        }
        GetResponse result=client.prepareGet("book","novel",id).get();
        if (!result.isExists()){
          return null;
        }
        return  JSON.toJSONString(result.getSource(),true) ;
    }
    @PostMapping(value = "/add/book/novel")
    public String add(@RequestBody Novel novel){
        System.out.println(novel.getPublish_date()+novel.getAuthor());
        IndexResponse result=null;
        try {
          XContentBuilder context= XContentFactory.jsonBuilder()
                    .startObject()
                    .field("author",novel.getAuthor())
                    .field("title",novel.getTitle())
                    .field("word_count",novel.getWord_count())
                    .field("publish_date",novel.getPublish_date())
                  .endObject();
          result= client.prepareIndex("book","novel").setSource(context).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
     return result.getId();
    }
    @DeleteMapping(value = "/delete/book/novel")
    public String delete(@RequestParam(name = "id")String id){
        DeleteResponse result=client.prepareDelete("book","novel",id).get();
        return result.getResult().toString();
    }
    @PutMapping(value = "/update/book/novel")
    public String update(@RequestBody HashMap<String,String> map){
        UpdateRequest update=new UpdateRequest("book","novel",map.get("id"));
        UpdateResponse result=null;
        try {
            XContentBuilder builder=XContentFactory.jsonBuilder()
                    .startObject();
            if (map.get("title")!=null){
                builder.field("title",map.get("title"));
            }
            if (map.get("author")!=null){
                builder.field("author",map.get("author"));
            }
            builder.endObject();
            update.doc(builder);
          result=  client.update(update).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.getResult().toString();
    }
    @PostMapping(value = "/query/book/novel")
    public String query(@RequestBody HashMap map){
        BoolQueryBuilder boolQuery= QueryBuilders.boolQuery();
        RangeQueryBuilder rangeQuery=null;

        if (map.get("author")!=null){

            boolQuery.must(QueryBuilders.matchQuery("author",map.get("author")));
        }
        if (map.get("title")!=null){
            boolQuery.must(QueryBuilders.matchQuery("title",map.get("title")));
        }
        if (map.get("gtWordCount")==null){
           rangeQuery=QueryBuilders.rangeQuery("word_count").from(0);
        }else {
            rangeQuery=QueryBuilders.rangeQuery("word_count").from(map.get("gtWordCount"));
        }
        Object ltWordCount=  map.get("ltWordCount");
        if (ltWordCount!=null&&(int)ltWordCount>0){
            rangeQuery.to(ltWordCount);
        }
        boolQuery.filter(rangeQuery);
       SearchRequestBuilder builder= client.prepareSearch("book")
                .setTypes("novel")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(boolQuery)
                .setFrom(0)
                .setSize(10);
        System.out.println(builder);
       SearchResponse response= builder.get();
        List<Map<String,Object>> result=new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            result.add(hit.getSourceAsMap());
        }
        return JSON.toJSONString(result,true);
    }
}
