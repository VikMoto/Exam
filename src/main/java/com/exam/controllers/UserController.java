package com.exam.controllers;

import com.exam.models.User;
import com.exam.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;


    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listUsers(Model model) {
        Iterable<User> users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "lastName"));
        model.addAttribute("users", users);
        return "users/list";
    }


    // For RESTful practices, it's good to use the HTTP DELETE method for deletions.
    // However, HTML forms only support GET and POST methods. We would need to use JavaScript
    // or the _method hack to support DELETE, which is not ideal. We'll stick with POST here.
    @PostMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/users";
    }


}