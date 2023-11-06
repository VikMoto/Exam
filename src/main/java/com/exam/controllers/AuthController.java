package com.exam.controllers;

import com.exam.models.Teacher;
import com.exam.repo.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;


    public AuthController(TeacherRepository teacherRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;

        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("teacher", new Teacher());
        return "register";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerTeacher(@RequestBody Teacher teacher) {
        if(teacherRepository.findByUsername(teacher.getUsername()).isPresent()){
            return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
        }
        teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        teacherRepository.save(teacher);
        return new ResponseEntity<>("Teacher registered successfully", HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginTeacher(@RequestBody Teacher teacher) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(teacher.getUsername(), teacher.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return new ResponseEntity<>("Teacher authenticated successfully", HttpStatus.OK);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>("Invalid username/password", HttpStatus.UNAUTHORIZED);
        }
    }
}
