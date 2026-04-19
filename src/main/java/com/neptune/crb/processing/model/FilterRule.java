package com.neptune.crb.processing.model;

import java.time.LocalDateTime;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 15-Apr-26
 */

public class FilterRule
{
    private String id;
    private String name;
    private String category;
    private String field;
    private String operator;
    private String value;
    private String active; // Y/N
    private LocalDateTime createdAt;

    public FilterRule()
    {
    }

    public FilterRule(String id, String name, String category, String field, String operator, String value, String active, LocalDateTime createdAt)
    {
        this.id = id;
        this.name = name;
        this.category = category;
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.active = active;
        this.createdAt = createdAt;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getActive()
    {
        return active;
    }

    public void setActive(String active)
    {
        this.active = active;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    @Override
    public String toString()
    {
        return "FilterRule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", field='" + field + '\'' +
                ", operator='" + operator + '\'' +
                ", value='" + value + '\'' +
                ", active='" + active + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
