package com.hamdard.CRUDOperations.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hamdard.CRUDOperations.models.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}