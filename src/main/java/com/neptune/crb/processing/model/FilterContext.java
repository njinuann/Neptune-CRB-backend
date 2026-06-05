package com.neptune.crb.processing.model;


import java.util.List;

public class FilterContext {

    private SearchFilter searchFilter;    // the search bar filter, null if none
    private List<FilterRule> ruleFilter;  // active rules that were matched, empty if none
    private long   totalRecords;          // total records before any filter
    private long   filteredRecords;       // records actually submitted
    private boolean filterApplied;        // true if any filter was active

    public SearchFilter getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(SearchFilter searchFilter) {
        this.searchFilter = searchFilter;
    }

    public List<FilterRule> getRuleFilter() {
        return ruleFilter;
    }

    public void setRuleFilter(List<FilterRule> ruleFilter) {
        this.ruleFilter = ruleFilter;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public long getFilteredRecords() {
        return filteredRecords;
    }

    public void setFilteredRecords(long filteredRecords) {
        this.filteredRecords = filteredRecords;
    }

    public boolean isFilterApplied() {
        return filterApplied;
    }

    public void setFilterApplied(boolean filterApplied) {
        this.filterApplied = filterApplied;
    }

    @Override
    public String toString() {
        return "FilterContext{" +
                "searchFilter=" + searchFilter +
                ", ruleFilter=" + ruleFilter +
                ", totalRecords=" + totalRecords +
                ", filteredRecords=" + filteredRecords +
                ", filterApplied=" + filterApplied +
                '}';
    }
}