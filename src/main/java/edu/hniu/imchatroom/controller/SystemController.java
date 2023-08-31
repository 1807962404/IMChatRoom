package edu.hniu.imchatroom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SystemController {

    @GetMapping(value = {"/signin", "/login"})
    public String toSignInPage() {
        return "sign";
    }

    @GetMapping(value = {"/", "/main"})
    public String toMainPage() {
        return "main";
    }

    @GetMapping("/error")
    public String toErrorPage() {
        return "error";
    }
}
