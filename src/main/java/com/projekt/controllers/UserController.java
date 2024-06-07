package com.projekt.controllers;

import com.projekt.Validators.UserValidator;
import com.projekt.models.Role;
import com.projekt.models.Search;
import com.projekt.models.User;
import com.projekt.services.RoleService;
import com.projekt.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/register")
    public String showRegisterForm(Model model){
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegisterForm(@Valid @ModelAttribute(name = "user") User user, BindingResult result, Model model){
        if(result.hasErrors()){
            return "register";
        }

        try {
            userService.saveUser(user, true, false,false);
        } catch (Exception exception){
            model.addAttribute("errorEmailOrUsername",true);
            return "register";
        }

        return "login";
    }

    @GetMapping({"/user/add", "/user/edit/{id}"})
    public String showUserForm(@PathVariable(name = "id", required = false) Integer id, Model model){
        model.addAttribute("user", userService.loadById(id));

        if(id == null || userService.exists(id) == false){
            return "user/showAddForm";
        }
        return "user/showEditForm";
    }

    @PostMapping("/user/add")
    public String processUserAddForm(@Valid @ModelAttribute(name = "user") User user, BindingResult result, Model model){
        if(result.hasErrors()){
            return "user/showAddForm";
        }

        try {
            userService.saveUser(user,true,true,true);
        } catch (Exception exception){
            model.addAttribute("errorEmailOrUsername",true);
            return "user/showAddForm";
        }

        model.addAttribute("user", userService.loadAll());
        model.addAttribute("search", new Search());
        return "user/showList";
    }

    @PostMapping("/user/edit/{id}")
    public String processUserForm(@Valid @ModelAttribute(name = "user") User user, BindingResult result,
                                @PathVariable(name = "id", required = false) Integer id, Model model){
        if(result.hasErrors()){
            return "user/showEditForm";
        }

        try {
            userService.editUser(user);
        } catch (Exception exception){
            model.addAttribute("errorEmailOrUsername",true);
            return "user/showEditForm";
        }

        model.addAttribute("user", userService.loadAll());
        model.addAttribute("search", new Search());
        return "user/showList";
    }

    @GetMapping("/user")
    public String showUserList(Model model){
        model.addAttribute("user", userService.loadAll());
        model.addAttribute("search", new Search());
        return "user/showList";
    }

    @GetMapping("/profile")
    public String showProfileInfo(Model model, Principal principal){
        model.addAttribute("user", userService.findUserByUsername(principal.getName()));
        return "user/profile";
    }

    @GetMapping("/profile/edit/{id}")
    public String showEditProfileForm(Model model, @PathVariable(name = "id", required = false) Integer id, Principal principal){
        if(userService.permit(id,principal.getName())){
            model.addAttribute("user", userService.loadById(id));
            return "user/editProfile";
        }

        model.addAttribute("user", userService.findUserByUsername(principal.getName()));
        return "user/profile";
    }

    @PostMapping("/profile/edit/{id}")
    public String processEditProfile(@Valid @ModelAttribute(name = "user") User user, BindingResult result,
                                     @PathVariable(name = "id", required = false) Integer id, Model model, Principal principal){
        if(result.hasErrors()){
            return "user/editProfile";
        }

        try {
            userService.editUser(user);
        } catch (Exception exception){
            model.addAttribute("errorEmailOrUsername",true);
            return "user/editProfile";
        }

        model.addAttribute("user", userService.findUserByUsername(principal.getName()));
        return "user/profile";
    }

    @Transactional
    @GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable(name = "id", required = false) Integer id, Model model){
        userService.delete(id);

        model.addAttribute("user", userService.loadAll());
        model.addAttribute("search", new Search());
        return "user/showList";
    }

    @GetMapping("/activate/{userID}")
    public String activateUser(@PathVariable(name = "userID", required = false) Integer userID, Model model){
        if(userService.exists(userID)){
            if(userService.activate(userID)){
                model.addAttribute("success", true);
            }else{
                model.addAttribute("success", false);
            }
            return "user/activation";
        }else{
            return "index";
        }
    }

    @InitBinder("user")
    public void initBinder(WebDataBinder binder){
        binder.addValidators(new UserValidator());
    }

    @ModelAttribute("rolesList")
    public ArrayList<Role> loadUser(){
        return roleService.loadAll();
    }
}
