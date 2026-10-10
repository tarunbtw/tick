package com.tick.market.dto;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * Generic pagination envelope providing collection items and deterministic paging metadata.
 *
 * @param <T> element type in the response
 */
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean isFirst;
    private boolean isLast;

    public PagedResponse() {
    }

    public PagedResponse(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean isFirst,
        boolean isLast
    ) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.isFirst = isFirst;
        this.isLast = isLast;
    }

    /**
     * Constructs a PagedResponse from a Spring Data Page.
     *
     * @param springPage the Spring Data Page object
     * @param <T>        the element type
     * @return PagedResponse containing content and metadata
     */
    public static <T> PagedResponse<T> from(Page<T> springPage) {
        return new PagedResponse<>(
            springPage.getContent(),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements(),
            springPage.getTotalPages(),
            springPage.isFirst(),
            springPage.isLast()
        );
    }

    /**
     * Constructs a PagedResponse from a Spring Data Page with a mapping transformation function.
     *
     * @param springPage the source Spring Data Page
     * @param mapper     mapping function from source entity E to target DTO T
     * @param <E>        source type
     * @param <T>        target type
     * @return mapped PagedResponse
     */
    public static <E, T> PagedResponse<T> from(Page<E> springPage, Function<E, T> mapper) {
        List<T> mappedContent = springPage.getContent().stream().map(mapper).toList();
        return new PagedResponse<>(
            mappedContent,
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements(),
            springPage.getTotalPages(),
            springPage.isFirst(),
            springPage.isLast()
        );
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }
}
