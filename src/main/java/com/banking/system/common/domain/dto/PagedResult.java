package com.banking.system.common.domain.dto;

import java.util.List;
import java.util.function.Function;

public record PagedResult<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T> PagedResult<T> of(List<T> items, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;
        return new PagedResult<>(items, page, size, totalElements, totalPages, hasNext);
    }

    public static <T, R> PagedResult<R> mapContent(PagedResult<T> other, Function<T, R> mapper) {
        List<R> mappedItems = other.items.stream()
                .map(mapper)
                .toList();

        return new PagedResult<>(
                mappedItems,
                other.page(),
                other.size(),
                other.totalElements(),
                other.totalPages(),
                other.hasNext()
        );
    }
}
