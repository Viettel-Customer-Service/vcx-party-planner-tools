package com.company.birthday.dto.response;

public record PaginationItemView(
        Integer pageNumber,
        String label,
        boolean current,
        boolean ellipsis
) {

    public static PaginationItemView page(int pageNumber, boolean current) {
        return new PaginationItemView(pageNumber, String.valueOf(pageNumber + 1), current, false);
    }

    public static PaginationItemView separator() {
        return new PaginationItemView(null, "...", false, true);
    }
}



