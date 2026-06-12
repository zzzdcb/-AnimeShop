package com.dong.dto;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class PageDTO<T> {

    //总记录数
    private Long total;

    //总页数
    private Long pages;

    //当前页
    private List<T> list;

    /**
     * 自动完成PO(持久层对象) -> VO(视图对象)的属性拷贝，这里需要字段名一致
     *
     * @param page  MyBatis-Plus分页查询结果
     * @param voClass 目标VO的字节码对象
     * @param <PO>  持久层实体类型
     * @param <VO>  视图层实体类型
     * @return 标准分页DTO
     */
    public static <PO,VO> PageDTO<VO> of(IPage<PO> page, Class<VO> voClass){
        PageDTO<VO> pageDTO = new PageDTO<>();

        // 分页信息
        pageDTO.setTotal(page.getTotal());
        pageDTO.setPages(page.getPages());

        // 数据
        List<PO> records = page.getRecords();

        // 4. 空数据判断，避免空指针
        if (CollUtil.isEmpty(records)) {
            pageDTO.setList(Collections.emptyList());
            return pageDTO;
        }

        // 5. PO对象批量转换为VO对象（属性名一致自动映射）
        List<VO> voList = BeanUtil.copyToList(records, voClass);
        pageDTO.setList(voList);

        return pageDTO;
    }

    /**
     * 自定义转换逻辑批量转换PO -> VO
     *
     * @param p      MyBatis-Plus分页查询结果
     * @param convertor 转换方法
     * @param <PO>  持久层实体类型
     * @param <VO>  返回给前端的实体类型
     * @return 标准分页DTO
     */
    public static <PO, VO> PageDTO<VO> of(Page<PO> p, Function<PO, VO> convertor) {
        PageDTO<VO> dto = new PageDTO<>();
        // 1. 总条数
        dto.setTotal(p.getTotal());
        // 2. 总页数
        dto.setPages(p.getPages());
        // 3. 当前页数据
        List<PO> records = p.getRecords();
        if (CollUtil.isEmpty(records)) {
            dto.setList(Collections.emptyList());
            return dto;
        }
        // 4. 拷贝user的VO
        dto.setList(records.stream().map(convertor).collect(Collectors.toList()));
        // 5. 返回
        return dto;
    }
}
