package com.neptune.crb.processing.model;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 15-Apr-26
 */
public class FilterRuleRequest {
 private String name;
 private String category;
 private String field;
 private String operator;
 private String value;
 private Boolean active;

 public FilterRuleRequest()
 {
 }

 public FilterRuleRequest(String name, String category, String field, String operator, String value, Boolean active)
 {
  this.name = name;
  this.category = category;
  this.field = field;
  this.operator = operator;
  this.value = value;
  this.active = active;
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

 public Boolean getActive()
 {
  return active;
 }

 public void setActive(Boolean active)
 {
  this.active = active;
 }

 @Override
 public String toString()
 {
  return "FilterRuleRequest{" +
          "name='" + name + '\'' +
          ", category='" + category + '\'' +
          ", field='" + field + '\'' +
          ", operator='" + operator + '\'' +
          ", value='" + value + '\'' +
          ", active=" + active +
          '}';
 }
}
