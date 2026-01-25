package com.banking.system.common.domain;

public record PageRequest(int page, int size) {
    public PageRequest {
        if (page < 0 ) throw new IllegalArgumentException("Page must be >= 0");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Size must be between 1 and 100");
    }

    public int offset() {
        return page * size;
    }

    public static PageRequest of(int page, int size) {
        return  new PageRequest(page, size);
    }
}
