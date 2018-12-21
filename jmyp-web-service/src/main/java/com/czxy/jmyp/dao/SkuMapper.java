package com.czxy.jmyp.dao;

import com.czxy.jmyp.pojo.Sku;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@org.apache.ibatis.annotations.Mapper
public interface SkuMapper extends Mapper<Sku>{
    @Select(" select * from tb_sku")
    @Results({
            @Result(id=true,column="id",property="id"),
            @Result(column="stock",property="stock"),
            @Result(column="sku_name",property="skuName"),
            @Result(column="spec_info_id_txt",property="specInfoIdTxt"),
            @Result(column="spec_info_id_list",property="specInfoIdList"),
            @Result(column = "spu_id",property = "spu",one=@One(select = "com.czxy.jmyp.dao.SpuMapper.findSpuById"))
    })
    List<Sku> findAllSkus();
}

