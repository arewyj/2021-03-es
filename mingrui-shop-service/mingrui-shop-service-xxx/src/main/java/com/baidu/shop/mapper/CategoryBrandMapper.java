package com.baidu.shop.mapper;

import com.baidu.shop.entity.CategoryBrandEntity;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

/**
 * @ClassName CategoryBrandMapper
 * @Description: TODO
 * @Author wyj
 * @Date 2021/1/23
 * @Version V1.0
 **/
public interface CategoryBrandMapper extends Mapper<CategoryBrandEntity> , InsertListMapper<CategoryBrandEntity> {
}
