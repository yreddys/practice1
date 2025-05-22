package com.price.entity;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPrice {
 private String date;
 private double open;
 private double high;
 private double low;
 private double close;
 private long volume;
}
