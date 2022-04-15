package com.projekt.controllers;

import com.projekt.models.Category;
import com.projekt.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/category-list")
    public String showCategoryList(Model model){
        model.addAttribute("category", categoryService.loadAll());
        model.addAttribute("use",categoryService.categoriesUse());
        return "category/showList";
    }

    @GetMapping(value = {"/category/edit/{id}","/category/add"})
    public String showFormCategory(@PathVariable(name = "id", required = false) Integer id, Model model){
        model.addAttribute("category", categoryService.loadById(id));

        if(id == null || categoryService.exists(id) == false){
            return "category/showAddForm";
        }
        return "category/showEditForm";
    }

    @PostMapping(value = {"/category/edit/{id}","/category/add"})
    public String processFormCategory(@Valid @ModelAttribute(name = "category") Category category, BindingResult bindingResult,
                                      @PathVariable(name = "id", required = false) Integer id, Model model){
        if(bindingResult.hasErrors()){
            if(id == null){
                return "category/showAddForm";
            }
            return "category/showEditForm";
        }

        categoryService.save(category);

        model.addAttribute("category", categoryService.loadAll());
        model.addAttribute("use",categoryService.categoriesUse());
        return "category/showList";
    }

    @GetMapping("/category/delete/{id}")
    public String deleteCategory(@PathVariable(name = "id", required = false) Integer id, Model model){
        categoryService.delete(id);

        model.addAttribute("category", categoryService.loadAll());
        model.addAttribute("use",categoryService.categoriesUse());
        return "category/showList";
    }
}
