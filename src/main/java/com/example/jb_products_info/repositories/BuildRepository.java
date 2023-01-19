package com.example.jb_products_info.repositories;

import com.example.jb_products_info.entities.Build;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildRepository extends JpaRepository<Build, Long>
{
    List<Build> findAllByProductCode(String code);
    Build findFirstByProductCodeAndBuildNumberStartsWith(String productCode, String buildNumber);
}
