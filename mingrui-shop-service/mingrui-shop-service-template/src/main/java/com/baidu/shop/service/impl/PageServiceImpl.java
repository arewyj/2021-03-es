package com.baidu.shop.service.impl;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.service.PageService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName PageServiceImpl
 * @Description: TODO
 * @Author wyj
 * @Date 2021/3/8
 * @Version V1.0
 **/
//@Service
public class PageServiceImpl implements PageService {
   // @Autowired
    private GoodsFeign goodsFeign;

 //   @Autowired
    private SpecificationFeign specificationFeign;

   // @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    //@Autowired
    private BrandFeign brandFeign;

   // @Autowired
    private CategoryFeign categoryFeign;

    @Override
    public Map<String, Object> getGoodsInfo(Integer spuId) {
        Map<String, Object> goodsInfoMap  = new HashMap<>();

        //spu的信息
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        Result<List<SpuDTO>> spuResult = goodsFeign.getSpuInfo(spuDTO);
        SpuDTO spuResultData  = null;
        if(spuResult.isSuccess()){
             spuResultData  = spuResult.getData().get(0);
            goodsInfoMap .put("spuInfo",spuResultData );
        }

        // spuDetail
        Result<SpuDetailEntity> spuDetailResult = goodsFeign.getSpuDetailBySpu(spuId);
        if(spuDetailResult.isSuccess()){
            goodsInfoMap.put("spuDetail",spuDetailResult.getData());
        }

        // 分类信息
        Result<List<CategoryEntity>> categoryResult = categoryFeign.getCategoryByIdList(
                String.join(
                        ","
                        , Arrays.asList(spuResultData.getCid1() + "", spuResultData.getCid2() + ""
                                , spuResultData.getCid3() + "")
                )
        );

        if (categoryResult.isSuccess()){
            goodsInfoMap.put("categoryInfo",categoryResult.getData());
        }

        //品牌信息
        BrandDTO brandDTO = new BrandDTO();
        brandDTO.setId(spuResultData.getBrandId());
        Result<PageInfo<BrandEntity>> brandResult = brandFeign.getBrandInfo(brandDTO);
        if (brandResult.isSuccess()){
            goodsInfoMap.put("brandInfo",brandResult.getData().getList().get(0));
        }

        // sku
        Result<List<SkuDTO>> skusResult = goodsFeign.getSkuBySpuId(spuId);
        if (skusResult.isSuccess()){
            goodsInfoMap.put("skus",skusResult.getData());
        }

        //规格组 通用规格参数
        SpecGroupDTO specGroupDTO = new SpecGroupDTO();
        specGroupDTO.setCid(spuResultData.getCid3());
        Result<List<SpecGroupEntity>> specGroupResult = specificationFeign.getSpecGroupInfo(specGroupDTO);
        if (specGroupResult.isSuccess()){

            List<SpecGroupEntity> specGroupList = specGroupResult.getData();
            List<SpecGroupDTO> specGroupAndParam  = specGroupList.stream().map(specGroup -> {
                SpecGroupDTO sgd = BaiduBeanUtil.copyProperties(specGroup, SpecGroupDTO.class);

                SpecParamDTO specParamDTO = new SpecParamDTO();
                specParamDTO.setGroupId(sgd.getId());
                specParamDTO.setGeneric(true);
                Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParamInfo(specParamDTO);

                if (specParamResult.isSuccess()) {
                    sgd.setSpecList(specParamResult.getData());
                }
                return sgd; // 需要有返回
            }).collect(Collectors.toList());
            goodsInfoMap.put("specGroupAndParam",specGroupAndParam);
        }

        // 特殊规格
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spuResultData.getCid3());
        specParamDTO.setGeneric(false);
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParamInfo(specParamDTO);
        if (specParamResult.isSuccess()){
            List<SpecParamEntity> specParamList  = specParamResult.getData();

            Map<Integer, String> specParamMap  = new HashMap<>();
            specParamList.stream().forEach(specParam ->{
                specParamMap.put(specParam.getId(),specParam.getName());
            });
            goodsInfoMap.put("specParamMap",specParamMap);
        }
        return goodsInfoMap;
    }


}
