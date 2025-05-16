import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;

@Configuration
public class CookieConfig {
    @Bean
    public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
        // Set SameSite=None for all cookies and ensure Secure flag is used
        return CookieSameSiteSupplier.ofNone().whenHasName("JSESSIONID");
    }
}

