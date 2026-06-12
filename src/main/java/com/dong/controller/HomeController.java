package com.dong.controller;

import com.dong.common.Result;
import com.dong.service.IHomeService;
import com.dong.vo.BannerVO;
import com.dong.vo.HotProductVO;
import com.dong.vo.NewProductVO;
import com.dong.vo.SeckillVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final IHomeService homeService;

    @GetMapping("/banner")
    public Result<List<BannerVO>> banner() {
        return homeService.getBanner();
    }

    @GetMapping("/hotProduct")
    public Result<List<HotProductVO>> hotProduct() {
        return homeService.getHotProductList();
    }

    @GetMapping("/newProduct")
    public Result<List<NewProductVO>> newProduct() {
        return homeService.getNewProductList();
    }

    @GetMapping("/seckillProduct")
    public Result<List<SeckillVO>> seckillProduct() {
        return homeService.getSeckillVO();
    }
}
