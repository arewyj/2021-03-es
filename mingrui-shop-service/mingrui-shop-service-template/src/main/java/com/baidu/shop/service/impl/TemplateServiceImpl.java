package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.service.TemplateService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * @ClassName TemplateServiceImpl
 * @Description: TODO
 * @Author wyj
 * @Date 2021/3/9
 * @Version V1.0
 **/
@RestController
public class TemplateServiceImpl extends BaseApiService implements TemplateService {

    private final Integer inHtmlFile = 1;
    private final Integer delHtmlFile = 2;

    @Autowired
    private TemplateEngine TemplateEngine ;

    @Value(value = "${mrshop.static.html.path}")
    private String htmlPath;

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private SpecificationFeign specificationFeign;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private BrandFeign brandFeign;

    @Autowired
    private CategoryFeign categoryFeign;


    @Override  // 通过spuId创建html文件  创建一个
    public Result<JSONObject> createStaticHTMLTemplate(Integer spuId) {

        Map<String, Object> goodsInfo = this.getGoodsInfo(spuId);

        Context context = new Context();
        context.setVariables(goodsInfo);

        File file = new File(htmlPath, spuId + ".html");
        if (!file.exists()){

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, "UTF-8");
            TemplateEngine.process("item",context,writer);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }finally {
            if (ObjectUtil.isNotNull(writer))
                writer.close();
        }

        return this.setResultSuccess();
    }

    @Override  // 初始化html文件
    public Result<JSONObject> initStaticHTMLTemplate() {

      /*  Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(new SpuDTO());
        if (spuInfo.isSuccess()){
            spuInfo.getData().stream().forEach( spuDTO -> {
                this.createStaticHTMLTemplate(spuDTO.getId());
            });
        }*/

        this.operationStaticHTML(inHtmlFile);

        return this.setResultSuccess();
    }

    @Override   // 清空html文件
    public Result<JSONObject> clearStaticHTMLTemplate() {

       /* Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(new SpuDTO());
        if (spuInfo.isSuccess()){
            spuInfo.getData().stream().forEach( spuDTO -> {
                this.deleteStaticHTMLTemplate(spuDTO.getId());
            });
        }*/
        this.operationStaticHTML(delHtmlFile);
        return this.setResultSuccess();
    }

    @Override   // 删除html 文件
    public Result<JSONObject> deleteStaticHTMLTemplate(Integer spuId) {

        File file = new File(htmlPath, spuId + ".html");
        if (file.exists()){
            file.delete();
        }
        return this.setResultSuccess();
    }


    private Boolean operationStaticHTML(Integer operation){

       try {
            Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(new SpuDTO());
            if (spuInfo.isSuccess()){
                spuInfo.getData().stream().forEach( spuDTO -> {
                    if (operation == 1){
                        this.createStaticHTMLTemplate(spuDTO.getId());
                    }else {
                        this.deleteStaticHTMLTemplate(spuDTO.getId());
                    }
                });

            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    // ------------ 以下方法做了拆分
    private Map<String, Object> getGoodsInfo(Integer spuId) {
        Map<String, Object> goodsInfoMap  = new HashMap<>();

        //spu的信息
        SpuDTO spuResultData = this.getSpuInfo(spuId);
        goodsInfoMap .put("spuInfo",spuResultData );
       /* SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        Result<List<SpuDTO>> spuResult = goodsFeign.getSpuInfo(spuDTO);
        SpuDTO spuResultData  = null;
        if(spuResult.isSuccess()){
            spuResultData  = spuResult.getData().get(0);
            goodsInfoMap .put("spuInfo",spuResultData );
        }*/

        // spuDetail
        goodsInfoMap.put("spuDetail",this.getSpuDetail(spuId));
      /*  Result<SpuDetailEntity> spuDetailResult = goodsFeign.getSpuDetailBySpu(spuId);
        if(spuDetailResult.isSuccess()){
            goodsInfoMap.put("spuDetail",spuDetailResult.getData());
        }*/

        // 分类信息
        goodsInfoMap.put("categoryInfo",this.getCategoryInfo(spuResultData.getCid1(),spuResultData.getCid2(),spuResultData.getCid3()));
      /*  Result<List<CategoryEntity>> categoryResult = categoryFeign.getCategoryByIdList(
                String.join(
                        ","
                        , Arrays.asList(spuResultData.getCid1() + "", spuResultData.getCid2() + ""
                                , spuResultData.getCid3() + "")
                )
        );

        if (categoryResult.isSuccess()){
            goodsInfoMap.put("categoryInfo",categoryResult.getData());
        }
*/
        //品牌信息
        goodsInfoMap.put("brandInfo",this.getBrandInfo(spuResultData.getBrandId()));

        /*BrandDTO brandDTO = new BrandDTO();
        brandDTO.setId(spuResultData.getBrandId());
        Result<PageInfo<BrandEntity>> brandResult = brandFeign.getBrandInfo(brandDTO);
        if (brandResult.isSuccess()){
            goodsInfoMap.put("brandInfo",brandResult.getData().getList().get(0));
        }*/

        // 获取sku 的信息
        goodsInfoMap.put("skus",this.getSkus(spuId));
      /*  Result<List<SkuDTO>> skusResult = goodsFeign.getSkuBySpuId(spuId);
        if (skusResult.isSuccess()){
            goodsInfoMap.put("skus",skusResult.getData());
        }*/

        //规格组 通用规格参数
        goodsInfoMap.put("specGroupAndParam",this.getSpecGroupAndParam(spuResultData.getCid3()));

      /*  SpecGroupDTO specGroupDTO = new SpecGroupDTO();
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
        }*/



        // 特殊规格
        goodsInfoMap.put("specParamMap",this.getSpecParamMap(spuResultData.getCid3()));

      /*  SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spuResultData.getCid3());
        specParamDTO.setGeneric(false);
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParamInfo(specParamDTO);
        if (specParamResult.isSuccess()){
            List<SpecParamEntity> specParamList  = specParamResult.getData();

            Map<Integer, String> specParamMap  = new HashMap<>();
            specParamList.stream().forEach(specParam ->{
                specParamMap.put(specParam.getId(),specParam.getName());
            });
            goodsInfoMap.put("specParamMap",this.getSpecParamMap(spuResultData.getCid3()));
        }*/

        return goodsInfoMap;
    }

    private SpuDTO getSpuInfo(Integer spuId){
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        Result<List<SpuDTO>> spuResult = goodsFeign.getSpuInfo(spuDTO);
        SpuDTO spuResultData  = null;
        if(spuResult.isSuccess()){
            spuResultData  = spuResult.getData().get(0);
        }
        return spuResultData;
    }


    private  SpuDetailEntity getSpuDetail(Integer spuId){
        Result<SpuDetailEntity> spuDetailResult = goodsFeign.getSpuDetailBySpu(spuId);
        SpuDetailEntity spuDetailList = null;
        if(spuDetailResult.isSuccess()){
            spuDetailList = spuDetailResult.getData();
        }
        return spuDetailList;
    }


    private  List<CategoryEntity> getCategoryInfo(Integer cid1,Integer cid2,Integer cid3){

        Result<List<CategoryEntity>> categoryResult = categoryFeign.getCategoryByIdList(
                String.join(
                        ","
                        , Arrays.asList(cid1 + "", cid2 + "", cid3 + "")
                )
        );

        List<CategoryEntity> categoryList = null;
        if (categoryResult.isSuccess()){
            categoryList = categoryResult.getData();
        }

        return categoryList;
    }

    private BrandEntity getBrandInfo(Integer brandId){  //品牌信息
        BrandEntity brandEntity = null;
        BrandDTO brandDTO = new BrandDTO();

        brandDTO.setId(brandId);
        Result<PageInfo<BrandEntity>> brandResult = brandFeign.getBrandInfo(brandDTO);
        if (brandResult.isSuccess()){
            brandEntity = brandResult.getData().getList().get(0);
        }
        return brandEntity;
    }

    private List<SkuDTO> getSkus(Integer spuId){
        List<SkuDTO> skuList = null;
        Result<List<SkuDTO>> skusResult = goodsFeign.getSkuBySpuId(spuId);
        if (skusResult.isSuccess()){
          skuList = skusResult.getData();
        }
        return skuList;
    }

    private  List<SpecGroupDTO>  getSpecGroupAndParam(Integer cid3){  //规格组 通用规格参数
        SpecGroupDTO specGroupDTO = new SpecGroupDTO();
        specGroupDTO.setCid(cid3);
        Result<List<SpecGroupEntity>> specGroupResult = specificationFeign.getSpecGroupInfo(specGroupDTO);
        List<SpecGroupDTO> specGroupAndParam  = null;
        if (specGroupResult.isSuccess()){
            List<SpecGroupEntity> specGroupList = specGroupResult.getData();
            specGroupAndParam  = specGroupList.stream().map(specGroup -> {
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
        }
        return specGroupAndParam;
    }



    private  Map<Integer, String> getSpecParamMap(Integer cid3){  // 获取特殊规格参数的方法拆分
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(cid3);
        specParamDTO.setGeneric(false);
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParamInfo(specParamDTO);
        Map<Integer, String> specParamMap  = new HashMap<>();

        if (specParamResult.isSuccess()){
            List<SpecParamEntity> specParamList  = specParamResult.getData();

            specParamList.stream().forEach(specParam ->{
                specParamMap.put(specParam.getId(),specParam.getName());
            });
        }
        return specParamMap;
    }











}
