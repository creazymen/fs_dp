package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_LIST_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        String key = CACHE_SHOP_TYPE_LIST_KEY;
        //1、从redis中查询是否有店铺列表缓存
        List<String> shopTypeJsonList = stringRedisTemplate.opsForList().range(key,0,-1);
        // 判断redis中是否存在数据
        if (shopTypeJsonList != null && !shopTypeJsonList.isEmpty()) {
            // 存在 则返回
            ArrayList<ShopType> typeList = new ArrayList<>();
            for (String str : shopTypeJsonList){
                typeList.add(JSONUtil.toBean(str,ShopType.class));
            }
            return Result.ok(typeList);
        }
        //3、不存在则去数据库中查询
        List<ShopType> typeList = query().orderByAsc("sort").list();
        // 如果查到的列表为空 则报错 分类不存在
        if (typeList == null || typeList.isEmpty()) {
            return Result.fail("分类不存在!");
        }
        //数据库中存在 把查询到数据保存到redis中
        for (ShopType shopType : typeList){
            stringRedisTemplate.opsForList().rightPushAll(key,JSONUtil.toJsonStr(shopType));
        }
        return Result.ok(typeList);
    }
}
