package com.example.disaster;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BuildingController {
	
	@GetMapping("/get-building")
	public String ret(Model model) {
		return "building.html";
	}
	
}
