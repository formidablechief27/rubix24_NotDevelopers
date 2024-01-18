package com.example.disaster;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExploreController {

	@GetMapping("/explore")
	public String explore(Model model) {
		return "explore.html";
	}
	
}
