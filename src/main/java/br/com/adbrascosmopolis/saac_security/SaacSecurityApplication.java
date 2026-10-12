package br.com.adbrascosmopolis.saac_security;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class SaacSecurityApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(SaacSecurityApplication.class, args);
//	}
//}



//import io.github.cdimascio.dotenv.Dotenv;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class SaacSecurityApplication {
//
//	public static void main(String[] args) {
//		Dotenv dotenv = Dotenv.configure()
//				.ignoreIfMissing()
//				.load();
//
//		dotenv.entries().forEach(entry ->
//				System.setProperty(entry.getKey(), entry.getValue())
//		);
//
//		SpringApplication.run(SaacSecurityApplication.class, args);
//	}
//}



import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaacSecurityApplication {

	static {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> {
			System.out.println("[dotenv] Setting " + entry.getKey() + "=" + entry.getValue());
			System.setProperty(entry.getKey(), entry.getValue());
		});

		System.out.println("[dotenv] DB_SERVER = " + System.getProperty("DB_SERVER"));
	}

	public static void main(String[] args) {
		SpringApplication.run(SaacSecurityApplication.class, args);
	}
}

