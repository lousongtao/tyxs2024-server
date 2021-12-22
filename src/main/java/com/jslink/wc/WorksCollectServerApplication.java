package com.jslink.wc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorksCollectServerApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(WorksCollectServerApplication.class, args);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
