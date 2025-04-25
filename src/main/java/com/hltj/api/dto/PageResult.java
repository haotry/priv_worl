package com.hltj.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private Long total;

    private List<T> list;

    public static <T> PageResult<T> of(Long total, List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setList(list);
        return result;
    }
}