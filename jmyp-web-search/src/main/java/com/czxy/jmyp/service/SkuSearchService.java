package com.czxy.jmyp.service;

import com.czxy.jmyp.repository.SkuRepository;
import com.czxy.jmyp.vo.BaseResult;
import com.czxy.jmyp.vo.ReturnSku;
import com.czxy.jmyp.vo.SearchRequest;
import com.czxy.jmyp.vo.SearchSku;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class SkuSearchService {

    @Resource
    private SkuRepository skuRepository;

    public BaseResult search(SearchRequest searchRequest ){

        //1. 数据校验如果三级分类为空
        if(searchRequest.getCat_id()==null){
            return new BaseResult( 1 , "没有数据");
        }

        //0 拼凑条件,使用 BoolQueryBuilder 进行条件的追加（and or）
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //0.1 关键字（暂时不实现）
        //0.2 品牌
        if( searchRequest.getBrand_id() != null ){
            boolQueryBuilder.must(QueryBuilders.termQuery("brandId" , searchRequest.getBrand_id() ) );
        }
        //0.3 规格 : 遍历map依次处理每一个规格，key有要求
        if(searchRequest.getSpec_list() != null ){
            for(String key : searchRequest.getSpec_list().keySet()){
                String value = searchRequest.getSpec_list().get(key);
                //拼凑se查询key
                String searchKey = "specs." + key + ".keyword";
                boolQueryBuilder.must( QueryBuilders.termQuery( searchKey , value ) );
            }
        }

        //0.4 范围
        if(searchRequest.getMin_price() != null){
            boolQueryBuilder.must( QueryBuilders.rangeQuery("price").gte( searchRequest.getMin_price() ) );      //gte 大于等于
        }
        if(searchRequest.getMax_price() != null){
            boolQueryBuilder.must( QueryBuilders.rangeQuery("price").lte( searchRequest.getMax_price() ));       //lte 小于等于
        }



        //1 使用条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //0.5 排序
        //queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC ));
        if("xl".equals( searchRequest.getSort_by() ) && "desc".equals(searchRequest.getSort_way() )){
            queryBuilder.withSort( SortBuilders.fieldSort("sellerCount").order( SortOrder.DESC));
        } else if("jg".equals( searchRequest.getSort_by() ) && "desc".equals(searchRequest.getSort_way() )){
            queryBuilder.withSort( SortBuilders.fieldSort("price").order( SortOrder.DESC));
        } else if("jg".equals( searchRequest.getSort_by() ) && "asc".equals(searchRequest.getSort_way() )){
            queryBuilder.withSort( SortBuilders.fieldSort("price").order( SortOrder.ASC));
        } else if("pl".equals( searchRequest.getSort_by() ) && "desc".equals(searchRequest.getSort_way() )){
            queryBuilder.withSort( SortBuilders.fieldSort("commentCount").order( SortOrder.DESC));
        } else if("sj".equals( searchRequest.getSort_by() ) && "desc".equals(searchRequest.getSort_way() )){
            queryBuilder.withSort( SortBuilders.fieldSort("onSaleTime").order( SortOrder.DESC));
        }

        // 0.end
        queryBuilder.withQuery(boolQueryBuilder);

        //0.6 分页 进行分页操作 ,【注意：从0开始】
        queryBuilder.withPageable(PageRequest.of( searchRequest.getPage() - 1 , searchRequest.getPer_page() ));

        //3 获得数据 -- SearchSku
        Page<SearchSku> page = this.skuRepository.search(queryBuilder.build());

        List<ReturnSku> returnList = new ArrayList<>();
        for (SearchSku searchSku : page.getContent()) {
            ReturnSku returnSku = new ReturnSku();

            returnSku.setId( searchSku.getId().intValue() );
            returnSku.setGoodsName( searchSku.getSkuName() );
            returnSku.setPrice( searchSku.getPrice() );
            returnSku.setMidlogo( searchSku.getLogo());
            returnSku.setCommentCount( searchSku.getCommentCount() );

            System.out.println( returnSku );

            returnList.add( returnSku );
        }
        System.out.println(returnList);

        //4 拼凑页面需要数据 -- BaseResult ( ReturnSku)
        BaseResult baseResult = new BaseResult(0 , "成功");

        baseResult.append("count" , page.getTotalElements() );
        baseResult.append("data" ,returnList );

        return baseResult;

    }
}