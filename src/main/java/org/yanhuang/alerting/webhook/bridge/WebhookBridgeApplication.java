package org.yanhuang.alerting.webhook.bridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
public class WebhookBridgeApplication {

	public static void main(String[] args) {
		final SpringApplication application = new SpringApplication(WebhookBridgeApplication.class);
		application.addListeners(new ApplicationPidFileWriter());
		application.run(args);
	}

}
