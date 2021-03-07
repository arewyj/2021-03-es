package com.baidu.shop.service.impl;
import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.service.CategoryService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ObjectUtil;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.beans.Transient;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author wyj
 * @Date 2021/1/19
 * @Version V1.0
 **/
@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService{

    @Resource
    private CategoryMapper categoryMapper;

    @Override//查询
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setParentId(pid);
        List<CategoryEntity>list = categoryMapper.select(categoryEntity);
        return this.setResultSuccess(list);
    }

    @Transactional
    @Override//新增
    public Result<JsonObject> saveCategory(CategoryEntity categoryEntity) {

        CategoryEntity cate = new CategoryEntity();
        cate.setId(categoryEntity.getParentId());
        cate.setIsParent(1);
        categoryMapper.updateByPrimaryKeySelective(cate);

        categoryMapper.insertSelective(categoryEntity);
        return this.setResultSuccess();
    }

    @Transactional
    @Override//修改
    public Result<JSONObject> editCategory(CategoryEntity categoryEntity) {
        categoryMapper.updateByPrimaryKeySelective(categoryEntity);
        return this.setResultSuccess();
    }

    @Transactional
    @Override//删除
    public Result<JsonObject> deleteById(Integer id) {
        if (null == id || id <= 0) return this.setResultError("id不合法");

        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);
        if (null == categoryEntity) return this.setResultError("数据不存在");

        if (categoryEntity.getIsParent() == 1) return this.setResultError("当前节点为父节点");

        Example example = new Example(categoryEntity.getClass());
        example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());

        List<CategoryEntity> categoryEntities = categoryMapper.selectByExample(example);
        if (categoryEntities.size() <= 1){
            CategoryEntity categoryEntity1 = new CategoryEntity();
            categoryEntity1.setIsParent(0);
            categoryEntity1.setId(categoryEntity.getParentId());
            categoryMapper.updateByPrimaryKeySelective(categoryEntity1);
        }

        categoryMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }

    @Override
    public Result<List<CategoryEntity>> getCategoryByIdList(String ids) {
        List<Integer> idList = Arrays.asList(ids.split(",")).stream().map(idStr -> Integer.valueOf(idStr)).collect(Collectors.toList());
        List<CategoryEntity> categoryEntities  = categoryMapper.selectByIdList(idList);

        return this.setResultSuccess(categoryEntities);
    }
}
