package com.dong.service;

import com.dong.common.Result;
import com.dong.vo.BannerVO;
import com.dong.vo.HotProductVO;
import com.dong.vo.NewProductVO;
import com.dong.vo.SeckillVO;

import java.util.List;

public interface IHomeService {
    Result<List<BannerVO>> getBanner();

    Result<List<HotProductVO>> getHotProductList();

    Result<List<NewProductVO>> getNewProductList();

    Result<List<SeckillVO>> getSeckillVO();
}
