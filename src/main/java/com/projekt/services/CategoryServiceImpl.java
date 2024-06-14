package com.projekt.services;

import com.projekt.models.Category;
import com.projekt.repositories.CategoryRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("categoryDetailsService")
public class CategoryServiceImpl implements CategoryService{
    private final CategoryRepository categoryRepository;
    private final TicketRepository ticketRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, TicketRepository ticketRepository) {
        this.categoryRepository = categoryRepository;
        this.ticketRepository = ticketRepository;
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

        return categoryRepository.getReferenceById(id);
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
        if(ticketRepository.countByCategoriesId(id) == 0 && categoryRepository.existsById(id)){
            categoryRepository.deleteById(id);
        }
    }

    @Override
    public ArrayList<Integer> categoriesUse() {
        ArrayList<Integer> list = new ArrayList<>();
        List<Category> categories = categoryRepository.findAll();

        for (int i=0; i<categories.size(); i++){
            list.add(ticketRepository.countByCategoriesId(categories.get(i).getId()));
        }

        return list;
    }

}
