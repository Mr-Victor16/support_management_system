package com.projekt.services;

import com.projekt.models.Category;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public interface CategoryService {
    ArrayList<Category> loadAll();

    Category loadById(Integer id);

    boolean exists(Integer id);

    void save(Category category);

    void delete(Integer id);

    ArrayList<Integer> categoriesUse();
}
