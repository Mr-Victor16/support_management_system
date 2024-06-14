package com.projekt.services;

import com.projekt.models.Category;
import com.projekt.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("categoryDetailsService")
public class CategoryServiceImpl implements CategoryService{
    private final CategoryRepository categoryRepository;
    private final TicketService ticketService;

    public CategoryServiceImpl(CategoryRepository categoryRepository, TicketService ticketService) {
        this.categoryRepository = categoryRepository;
        this.ticketService = ticketService;
    }

    @Override
    public ArrayList<Category> loadAll() {
        return (ArrayList<Category>) categoryRepository.findAll();
    }

    @Override
    public Category loadById(Integer id) {
        if(id == null || !categoryRepository.existsById(id)){
            return new Category();
        }

        return categoryRepository.getById(id);
    }

    @Override
    public boolean exists(Integer id) {
        return categoryRepository.existsById(id);
    }

    @Override
    public void save(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public void delete(Integer id) {
        if(ticketService.countUseCategory(id) == 0 && categoryRepository.existsById(id)){
            categoryRepository.deleteById(id);
        }
    }

    @Override
    public ArrayList<Integer> categoriesUse() {
        ArrayList<Integer> list = new ArrayList<>();
        List<Category> categories = categoryRepository.findAll();

        for (int i=0; i<categories.size(); i++){
            list.add(ticketService.countUseCategory(categories.get(i).getCategoryID()));
        }

        return list;
    }


}
