package com.example.springApp.controller;

import com.example.springApp.domain.Category;
import com.example.springApp.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller

@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping()
    public String categoryList(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "category";
    }

    @GetMapping("/{category}")
    public String userEditForm(@PathVariable Category category, Model model) {
        model.addAttribute("category", category);
        return "categoryEdit";
    }

    @PostMapping("/edit")
    public String editCategory(Map<String, Object> model,
                               @RequestParam String categoryname,
                               @RequestParam("categoryId") Category category) {
        category.setCategoryname(categoryname);
        addCategory(model, category);
        return "redirect:/category";
    }

    @PostMapping("/add")
    public String addCategory(Map<String, Object> model,
                              Category category) {
        try {
            categoryService.save(category);
            model.put("addCategoryResult", "Category successfully added");
        } catch (Exception ex) {
            model.put("addCategoryResult", "Category already exists");
        }
        return "redirect:/category";
    }

    @PostMapping("/delete")
    public String deleteCategory(Map<String, Object> model,
                              Category category) {
        categoryService.delete(categoryService.loadCategoryByCategoryname(category.getCategoryname()));
        return "redirect:/category";
    }
}
