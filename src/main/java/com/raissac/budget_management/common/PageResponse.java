package com.raissac.budget_management.common;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private Integer page;
    private Integer totalElements;
    private Integer totalPages;
    private boolean first;
    private boolean last;
}
