package com.greenspace.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@SpringBootApplication
@RestController
@RequestMapping("/")
public class ApiApplication {

	@GetMapping("/")
	public String index() {
		return "<strong>Olá mundo!</strong></br>Seja bem vindo, de uma olhada na documentação clicando aqui: <a href=\"/api-docs\" >api-docs</a>";
	}

	@GetMapping("/api-docs")
	public RedirectView apiDocs() {
		return new RedirectView("/swagger-ui.html");
	}

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
