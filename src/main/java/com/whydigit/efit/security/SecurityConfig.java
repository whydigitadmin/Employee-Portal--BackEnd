package com.whydigit.efit.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SecurityConfig {

	@Bean
	TokenAuthenticationFilter tokenAuthenticationFilter() {
		return new TokenAuthenticationFilter();
	}

	@Bean
	RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
		return new RestAuthenticationEntryPoint();
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.cors().and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().csrf()
				.disable().formLogin().disable().httpBasic().disable().exceptionHandling()
				.authenticationEntryPoint(restAuthenticationEntryPoint()).and().authorizeRequests()
				.antMatchers("/", "/error", "/favicon.ico", "/**/*.png", "/**/*.gif", "/**/*.svg", "/**/*.jpg",
						"/**/*.html", "/**/*.css", "/**/*.js")
				.permitAll()
				.antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/security",
						"/swagger-ui.html", "/swagger-ui/*", "/api/user/signup", "/api/user/login", "/api/user/logout",
						"/api/user/getRefreshToken", "/api/basicSetting/companyInfo","/api/basicMaster/employee",
						"/api/basicMaster/permissionRequest","/api/basicMaster/permissionRequestapp/{id}","/api/basicMaster/leaverequest","/api/basicMaster/leaverequestapp/{id}","/api/basicMaster/checkin",
						"/api/basicMaster/checkout","/api/basicMaster/holiday","/api/basicMaster/employee1","/api/basicMaster/chkStatus/{empcode}","/api/basicMaster/permissionRequests/{empcode}","/api/basicMaster/leaverequests/{empcode}","/api/admin/**",
						"/api/auth/login","/api/basicMaster/leavetype","/api/basicMaster/leavetype/{id}","/api/basicMaster/employee/daily/status","/api/basicMaster/attendance/{empcode}","/api/employee/getEmployeeAttendanceActivity","/api/employee/employeeInOutAction","/api/employee/getEmployeeStatus")
				.permitAll().antMatchers("/api/**").hasAnyRole("USER", "GUEST_USER").anyRequest().authenticated();
		http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

}
