package com.example.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requiere PostgreSQL en ejecución; la lógica se cubre con tests unitarios")
class PruebaTecnicaApplicationTests {

	@Test
	void contextLoads() {
	}

}
