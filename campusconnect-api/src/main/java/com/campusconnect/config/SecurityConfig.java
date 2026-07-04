package com.campusconnect.config;

import com.campusconnect.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // OPTIONS 请求 - 公开 (CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 错误页面 - 公开 (防止 500 转 403)
                        .requestMatchers("/error").permitAll()
                        // 认证相关 - 公开
                        .requestMatchers("/auth/**").permitAll()
                        // 静态资源 - 公开
                        .requestMatchers("/uploads/**").permitAll()
                        // 文件上传 - 暂时公开
                        .requestMatchers("/file/**").permitAll()
                        .requestMatchers("/upload/**").permitAll()
                        // 其他公开 GET 接口
                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/system/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/study-rooms/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/map/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/lost-found/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/user/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/user/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/user/{id}/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/user/{id}/following").permitAll()
                        .requestMatchers(HttpMethod.GET, "/user/{id}/followers").permitAll()
                        // 帖子分享 - 公开（只增加计数，不需要登录）
                        .requestMatchers(HttpMethod.POST, "/posts/*/share").permitAll()
                        // 帖子 - 其他需要认证
                        .requestMatchers(HttpMethod.POST, "/posts").authenticated()
                        .requestMatchers(HttpMethod.POST, "/posts/*/like").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/posts/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/posts/**").authenticated()
                        .requestMatchers("/ws/**").permitAll()
                        // 消息 - 需要认证
                        .requestMatchers("/messages/**").authenticated()
                        .requestMatchers("/group-buy-live/**").permitAll()
                        // 管理员接口
                        .requestMatchers("/admin/dynamic-config/**").permitAll()
                        .requestMatchers("/admin/dashboard/**").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MODERATOR")
                        //如果你本地想先把拼团所有操作都放开测试，也可以临时写：
                        .requestMatchers("/group-buys/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/activities/**").permitAll()
                        // 活动报名 - 登录用户才能报名
                        .requestMatchers("/activities/*/join").authenticated()
                        //.requestMatchers("/chat/**").permitAll()
                        .requestMatchers("/activities/**").permitAll()
                        .requestMatchers("/activities/**").authenticated()
                        .requestMatchers( "/events/**").permitAll()
                        .requestMatchers( "/events/*/register").authenticated()
                        .requestMatchers("/events/*/register").authenticated()
                        .requestMatchers("/events/*/register").authenticated()
                        .requestMatchers( "/events/*/register").authenticated()
                        // 活动接口
                        .requestMatchers(HttpMethod.GET, "/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/activities/**").permitAll()

// 活动报名、取消报名：登录用户即可
                        .requestMatchers(HttpMethod.POST, "/events/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/events/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/activities/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/activities/**").authenticated()
                        // 其他需要认证
                        .anyRequest().authenticated())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
