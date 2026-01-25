package com.banking.system.common.infraestructure.mapper;

import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;

public final class PageMapper {

    private PageMapper() {}

    public static Pageable toPageable(PageRequest pageRequest) {
        return org.springframework.data.domain.PageRequest.of(
                pageRequest.page(),
                pageRequest.size()
        );
    }

    public static <T> PagedResult<T> toPagedResult(Page<T> page) {
        return PagedResult.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    public static <T, R> PagedResult<R> toPagedResult(Page<T> page, Function<T, R> mapper) {
        List<R> mappedItems = page.getContent().stream()
                .map(mapper)
                .toList();

        return PagedResult.of(
                mappedItems,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}
