package com.example.jb_products_info.repositories;

import com.example.jb_products_info.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>
{
    Product findByCode(String code);
}
