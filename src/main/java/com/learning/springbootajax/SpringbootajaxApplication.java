package com.learning.springbootajax;

import com.learning.springbootajax.domain.SocialMetaTag;
import com.learning.springbootajax.service.SocialMetaTagService;
import org.directwebremoting.spring.DwrSpringServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
@ImportResource(locations = "classpath:dwr-spring.xml")
@SpringBootApplication
public class SpringbootajaxApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootajaxApplication.class, args);
    }

    @Autowired
    SocialMetaTagService service;
    @Override
    public void run(String... args) throws Exception {
//        SocialMetaTag smt01 = service.getSocialMetaTagByUrl("https://www.udemy.com/spring-boot-mvc-com-thymeleaf");
//        System.out.println(smt01.toString());
//
//        SocialMetaTag smt02 = service.getSocialMetaTagByUrl("https://www.pichau.com.br/fonte-gigabyte-b700h-700w-80plus-bronze-pfc-ativo-gp-b700h");
//        System.out.println(smt02.toString());
    }

    /** mesmo que a configuração via xml */
    @Bean
    public ServletRegistrationBean<DwrSpringServlet> dwrSpringServlet() {
        DwrSpringServlet dwrServlet = new DwrSpringServlet();

        ServletRegistrationBean<DwrSpringServlet> registrationBean =
                new ServletRegistrationBean<>(dwrServlet, "/dwr/*");

        registrationBean.addInitParameter("debug", "true");
        registrationBean.addInitParameter("activeReverseAjaxEnabled", "true");
        return registrationBean;
    }
}
