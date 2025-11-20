package com.beautysalon.booking.controller;

import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.repository.IServiceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class HomeController {
    private final IServiceRepository serviceRepository;
    private final IMasterRepository masterRepository;

    public HomeController(IServiceRepository serviceRepository, IMasterRepository masterRepository) {
        this.serviceRepository = serviceRepository;
        this.masterRepository = masterRepository;
    }

    @GetMapping("/")
    @Transactional
    public String showLandingPage(Model model) {
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("masters", masterRepository.findAll());
        return "index";
    }
}
