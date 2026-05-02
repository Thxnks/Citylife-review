package com.citylife;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.citylife.mapper")
@SpringBootApplication
public class CityLifeReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(CityLifeReviewApplication.class, args);
    }

}
