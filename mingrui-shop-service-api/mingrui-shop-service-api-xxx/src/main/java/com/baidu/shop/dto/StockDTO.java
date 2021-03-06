package com.baidu.shop.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName StockDTO
 * @Description: TODO
 * @Author wyj
 * @Date 2021/2/3
 * @Version V1.0
 **/
@ApiModel(value = "库存数据传输DTO")
@Data
public class StockDTO {

    @ApiModelProperty(value = "sku主键",example = "1")
    private Long skuId;

    @ApiModelProperty(value = "可秒杀库存",example = "1")
    private Integer seckillStock;

    @ApiModelProperty(value = "秒杀总数量",example = "1")
    private Integer seckillTotal;

    @ApiModelProperty(value = "库存数量",example = "1")
    private Integer stock;
}