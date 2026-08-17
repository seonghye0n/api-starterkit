package com.example.apistarterkit.global.config;

import com.example.apistarterkit.global.security.jwt.JwtAccessDeniedHandler;
import com.example.apistarterkit.global.security.jwt.JwtAuthenticationEntryPoint;
import com.example.apistarterkit.global.security.jwt.JwtAuthenticationFilter;
import com.example.apistarterkit.global.security.jwt.JwtTokenProvider;
import com.example.apistarterkit.global.security.jwt.TokenBlacklistService;
import com.example.apistarterkit.global.security.oauth2.CustomOAuth2UserService;
import com.example.apistarterkit.global.security.oauth2.OAuth2LoginFailureHandler;
import com.example.apistarterkit.global.security.oauth2.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PERMIT_ALL_PATHS = {
            "/api/auth/**",
            "/oauth2/**", "/login/**",
            "/swagger-ui/**", "/v3/api-docs/**",
            "/actuator/health/**"
    };

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults()) // WebConfig의 CorsConfigurationSource 빈을 사용
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler))
                // JWT 필터는 폼로그인 필터 자리(UsernamePasswordAuthenticationFilter) 이전에 두는 것이 관례다.
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, tokenBlacklistService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
