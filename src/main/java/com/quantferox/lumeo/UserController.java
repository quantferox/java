package com.quantferox.lumeo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/{name}")
  public String index(@PathVariable String name, Model model) {
    model.addAttribute("name", name);
    userService.getBannedUser();
    return "home/index";
  }

  @PostMapping("path")
  public String postMethodName(@RequestBody String entity) {

    return entity;
  }

}
