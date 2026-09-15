package univh2.fstm.gestionimmobilier.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import univh2.fstm.gestionimmobilier.security.RateLimitingFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitFilter(RateLimitingFilter filter) {
        FilterRegistrationBean<RateLimitingFilter> reg = new FilterRegistrationBean<>(filter);
        reg.addUrlPatterns("/auth/*");
        reg.setOrder(1); // Exécuté avant JwtFilter (Spring Security)
        return reg;
    }
}
