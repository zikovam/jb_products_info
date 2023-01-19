package com.example.jb_products_info;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JbProductsInfoApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run( JbProductsInfoApplication.class, args );
    }
}
