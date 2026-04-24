package com.company.birthday.dto.response;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public record PaginationView(
        int currentPage,
        int size,
        int totalPages,
        long totalElements,
        boolean hasPrevious,
        boolean hasNext,
        Integer previousPage,
        Integer nextPage,
        List<PaginationItemView> items
) {

    public static PaginationView from(Page<?> pageData) {
        int currentPage = pageData.getNumber();
        int totalPages = pageData.getTotalPages();
        int lastPage = totalPages - 1;

        List<PaginationItemView> pageItems = buildItems(currentPage, lastPage);
        boolean hasPrevious = totalPages > 0 && currentPage > 0;
        boolean hasNext = totalPages > 0 && currentPage < lastPage;

        return new PaginationView(
                currentPage,
                pageData.getSize(),
                totalPages,
                pageData.getTotalElements(),
                hasPrevious,
                hasNext,
                hasPrevious ? currentPage - 1 : null,
                hasNext ? currentPage + 1 : null,
                pageItems
        );
    }

    private static List<PaginationItemView> buildItems(int currentPage, int lastPage) {
        List<PaginationItemView> result = new ArrayList<>();

        if (lastPage < 0) {
            return result;
        }

        TreeSet<Integer> pages = new TreeSet<>();
        pages.add(0);
        pages.add(lastPage);

        for (int i = currentPage - 1; i <= currentPage + 1; i++) {
            if (i >= 0 && i <= lastPage) {
                pages.add(i);
            }
        }

        if (currentPage <= 2) {
            pages.add(1);
            pages.add(2);
        }

        if (currentPage >= lastPage - 2) {
            pages.add(lastPage - 1);
            pages.add(lastPage - 2);
        }

        pages.removeIf(page -> page < 0 || page > lastPage);

        Integer previous = null;
        for (Integer page : pages) {
            if (previous != null && page - previous > 1) {
                result.add(PaginationItemView.separator());
            }
            result.add(PaginationItemView.page(page, page == currentPage));
            previous = page;
        }

        return result;
    }
}



