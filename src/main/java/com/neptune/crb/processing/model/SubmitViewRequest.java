package com.neptune.crb.processing.model;

import java.util.List;
import java.util.Map;


public class SubmitViewRequest {

    private List<Map<String, Object>> viewData;      // the actual records
    private String                    viewName;       // e.g. "Individual Customers"
    private FilterContext             filterContext;  // what filters were applied
    private List<FilterRule>          filterRules;    // active rules at time of submit

    public List<Map<String, Object>> getViewData() {
        return viewData;
    }

    public void setViewData(List<Map<String, Object>> viewData) {
        this.viewData = viewData;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public FilterContext getFilterContext() {
        return filterContext;
    }

    public void setFilterContext(FilterContext filterContext) {
        this.filterContext = filterContext;
    }

    public List<FilterRule> getFilterRules() {
        return filterRules;
    }

    public void setFilterRules(List<FilterRule> filterRules) {
        this.filterRules = filterRules;
    }

    @Override
    public String toString() {
        return "SubmitViewRequest{" +
                "viewData=" + viewData +
                ", viewName='" + viewName + '\'' +
                ", filterContext=" + filterContext +
                ", filterRules=" + filterRules +
                '}';
    }
}
