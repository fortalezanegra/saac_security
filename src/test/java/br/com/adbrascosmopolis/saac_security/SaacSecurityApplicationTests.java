package br.com.adbrascosmopolis.saac_security;

//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//class SaacSecurityApplicationTests {
//
//	@Test
//	void contextLoads() {
//	}
//
//}


import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SaacSecurityApplicationTests {

	static {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue()));
	}

	@Test
	void contextLoads() {
	}
}

