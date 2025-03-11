package com.raissac.budget_management.category.repository;

import com.raissac.budget_management.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

}
