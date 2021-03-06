package com.macro.mall.search.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.macro.mall.search.dao.EsProductDao;
import com.macro.mall.search.domain.EsProduct;
import com.macro.mall.search.domain.EsProductRelatedInfo;
import com.macro.mall.search.repository.EsProductRepository;
import com.macro.mall.search.service.EsProductService;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hu
 * @create 2022/2/13
 */
@Service
public class EsProductServiceImpl implements EsProductService {
    @Autowired
    EsProductDao esProductDao;
    @Autowired
    EsProductRepository esProductRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public int importAll() {
        List<EsProduct> allEsProductList = esProductDao.getAllEsProductList(null);
        Iterable<EsProduct> esProducts = esProductRepository.saveAll(allEsProductList);
        Iterator<EsProduct> iterator = esProducts.iterator();
        int result = 0;
        while (iterator.hasNext()) {
            result++;
            iterator.next();
        }
        return result;
    }

    @Override
    public void delete(Long id) {
        esProductRepository.deleteById(id);
    }

    @Override
    public EsProduct create(Long id) {
        List<EsProduct> allEsProductList = esProductDao.getAllEsProductList(id);
        EsProduct save = null;
        if (!CollUtil.isEmpty(allEsProductList)) {
            save = esProductRepository.save(allEsProductList.get(0));
        }
        return save;
    }

    @Override
    public void delete(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            List<EsProduct> esProductList = new ArrayList<>();
            for (Long id : ids) {
                EsProduct esProduct = new EsProduct();
                esProduct.setId(id);
                esProductList.add(esProduct);
            }
            esProductRepository.deleteAll(esProductList);
        }
    }

    @Override
    public Page<EsProduct> search(String keyword, Integer pageNum, Integer pageSize) {
        Pageable of = PageRequest.of(pageNum, pageSize);
        return esProductRepository.findByNameOrSubTitleOrKeywords(keyword, keyword, keyword, of);
    }

    @Override
    public Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize, Integer sort) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        Pageable of = PageRequest.of(pageNum, pageSize);
        //??????
        nativeSearchQueryBuilder.withPageable(of);
        if (brandId != null || productCategoryId != null) {
            //??????
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            if (brandId != null) {
                boolQueryBuilder.must(QueryBuilders.termQuery("brandId", brandId));
            }
            if (productCategoryId != null) {
                boolQueryBuilder.must(QueryBuilders.termQuery("productCategoryId", productCategoryId));
            }
            nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        }
        //???????????????
        if (StringUtils.isEmpty(keyword)) {
            nativeSearchQueryBuilder.withQuery(QueryBuilders.matchAllQuery());
        } else {
            List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("name", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(10)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("subTitle", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(5)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("keywords", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(2)));
            FunctionScoreQueryBuilder.FilterFunctionBuilder[] filters = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
            filterFunctionBuilders.toArray(filters);
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(filters);
            nativeSearchQueryBuilder.withQuery(functionScoreQueryBuilder);
        }
        //??????
        if (sort == 1) {
            //?????????????????????
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC));
        } else if (sort == 2) {
            //?????????????????????
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("sale").order(SortOrder.DESC));
        } else if (sort == 3) {
            //?????????????????????
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
        } else if (sort == 4) {
            //?????????????????????
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        } else {
            //????????????
            nativeSearchQueryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
        }
        nativeSearchQueryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
        SearchHits<EsProduct> search = elasticsearchRestTemplate.search(searchQuery, EsProduct.class);
        if(search.getTotalHits()<=0){
            return new PageImpl<>(null,of,0);
        }
        List<EsProduct> collect = search.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return new PageImpl<>(collect,of,search.getTotalHits());
    }

    @Override
    public Page<EsProduct> recommend(Long id, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        List<EsProduct> productList = esProductDao.getAllEsProductList(id);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        if(!productList.isEmpty()){
            EsProduct product=productList.get(0);
            String keyword = product.getName();
            Long productCategoryId = product.getProductCategoryId();
            Long brandId = product.getBrandId();
            String subTitle = product.getSubTitle();
            //??????????????????????????????????????????
            List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders=new ArrayList<>();
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("name",keyword),
                    ScoreFunctionBuilders.weightFactorFunction(8)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("subTitle", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(2)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("keywords", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(2)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("brandId", brandId),
                    ScoreFunctionBuilders.weightFactorFunction(5)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("productCategoryId", productCategoryId),
                    ScoreFunctionBuilders.weightFactorFunction(3)));
            FunctionScoreQueryBuilder.FilterFunctionBuilder[] filters=new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
            filterFunctionBuilders.toArray(filters);
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(filters)
                    .setMinScore(2)
                    .scoreMode(FunctionScoreQuery.ScoreMode.SUM);
            NativeSearchQueryBuilder searchQueryBuilder = nativeSearchQueryBuilder.withQuery(functionScoreQueryBuilder);
            SearchHits<EsProduct> search = elasticsearchRestTemplate.search(searchQueryBuilder.build(), EsProduct.class);
            if(search.getTotalHits()<=0){
                return new PageImpl<>(null,pageable,0);
            }
            List<EsProduct> collect = search.stream().map(SearchHit::getContent).collect(Collectors.toList());
            return new PageImpl<>(collect,pageable,search.getTotalHits());
        }
        return new PageImpl<>(null);
    }

    @Override
    public EsProductRelatedInfo searchRelatedInfo(String keyword) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //???????????????
        if(StringUtils.isEmpty(keyword)){
            nativeSearchQueryBuilder.withQuery(QueryBuilders.matchAllQuery());
        }else{
            nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(keyword,"name","subTitle","keywords"));
        }
        //????????????
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("brandNames").field("brandName"));
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("productCategoryNames").field("productCategoryName"));
        //????????????productAttr
        NestedAggregationBuilder nestedAggregationBuilder = AggregationBuilders.nested("allAttrValues", "attrValueList")
                .subAggregation(AggregationBuilders.filter("productAttrs", QueryBuilders.termQuery("attrValueList.type", 1))
                        .subAggregation(AggregationBuilders.terms("attrIds").field("attrValueList.productAttributeId"))
                        .subAggregation(AggregationBuilders.terms("attrNames").field("attrValueList.name"))
                );
        nativeSearchQueryBuilder.addAggregation(nestedAggregationBuilder);
        NativeSearchQuery build = nativeSearchQueryBuilder.build();
        SearchHits<EsProduct> search = elasticsearchRestTemplate.search(build, EsProduct.class);
        return convertProductRelatedInfo(search);
    }

    /**
     * ???????????????esproduct????????????????????????????????????????????????esproductRelationInfo???
     * @param response
     * @return
     */
    private EsProductRelatedInfo convertProductRelatedInfo(SearchHits<EsProduct> response){
        EsProductRelatedInfo esProductRelatedInfo = new EsProductRelatedInfo();
        //???????????????????????????name
        Map<String, Aggregation> aggregationMap = response.getAggregations().getAsMap();
        Aggregation brandNames = aggregationMap.get("brandNames");
        ArrayList<String> brand = new ArrayList<>();
        //??????????????????
        for (int i = 0; i < ((Terms)brandNames).getBuckets().size(); i++) {
            brand.add(((Terms)brandNames).getBuckets().get(i).getKeyAsString());
        }
        esProductRelatedInfo.setBrandNames(brand);
        //??????productCategoryNames??????
        ArrayList<String> category = new ArrayList<>();
        Aggregation productCategoryNames = aggregationMap.get("productCategoryNames");
        for (int i = 0; i < ((Terms)productCategoryNames).getBuckets().size(); i++) {
            brand.add(((Terms)productCategoryNames).getBuckets().get(i).getKeyAsString());
        }
        esProductRelatedInfo.setProductCategoryNames(category);
        //????????????????????????
        Aggregation allAttrValues = aggregationMap.get("allAttrValues");
        List<? extends Terms.Bucket> attrIds = ((Terms) ((ParsedFilter) ((ParsedNested) allAttrValues).getAggregations().get("productAttrs")).getAggregations().get("attrIds")).getBuckets();
        List<EsProductRelatedInfo.ProductAttr> attrList = new ArrayList<>();
        for (Terms.Bucket attrId : attrIds) {
            EsProductRelatedInfo.ProductAttr attr = new EsProductRelatedInfo.ProductAttr();
            attr.setAttrId((Long) attrId.getKey());
            List<String> attrValueList = new ArrayList<>();
            List<? extends Terms.Bucket> attrValues = ((ParsedStringTerms) attrId.getAggregations().get("attrValues")).getBuckets();
            List<? extends Terms.Bucket> attrNames = ((ParsedStringTerms) attrId.getAggregations().get("attrNames")).getBuckets();
            for (Terms.Bucket attrValue : attrValues) {
                attrValueList.add(attrValue.getKeyAsString());
            }
            attr.setAttrValues(attrValueList);
            if(!CollectionUtils.isEmpty(attrNames)){
                String attrName = attrNames.get(0).getKeyAsString();
                attr.setAttrName(attrName);
            }
            attrList.add(attr);
        }
        esProductRelatedInfo.setProductAttrs(attrList);
        return esProductRelatedInfo;
    }
}