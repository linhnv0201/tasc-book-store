package tasc.bookstore.configuration;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cho tất cả các endpoint
                .allowedOrigins(
                        "http://localhost:4200",
                        "127.0.0.1:5500",
                        "https://clothing-dms-aay8aa4l1-vu-linhs-projects-83bba419.vercel.app"
                ) // Chỉ cho phép yêu cầu từ domain này
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*") // Cho phép tất cả các header
                .allowCredentials(true); // Cho phép cookies hoặc thông tin xác thực
    }
}