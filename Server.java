package com.example.disaster;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class Server {
	public static void main(String args[]) {
		SpringApplication.run(Server.class, args);
	}
}
