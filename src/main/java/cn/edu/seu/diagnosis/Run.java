package cn.edu.seu.diagnosis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

@Configuration
@Controller
@SpringBootApplication
public class Run extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Run.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
        return applicationBuilder.sources(Run.class);
    }

    @Bean
    TaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("scheduler-task-thread-");
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    @Bean
    RestTemplate getRestTemplate() {return new RestTemplate();}

    @RequestMapping({"/", "/index.html"})
    public ModelAndView index() {
        return new ModelAndView("index");
    }

}
