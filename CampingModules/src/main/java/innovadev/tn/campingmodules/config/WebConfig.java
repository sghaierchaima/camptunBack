package innovadev.tn.campingmodules.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Méthode pour configurer CORS globalement pour toutes les requêtes
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")  // Remplacez avec l'origine de votre frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // Les méthodes autorisées
                .allowedHeaders("*")  // Autorise tous les headers
                .allowCredentials(true);  // Si vous avez besoin de gérer les cookies ou les credentials
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Assurez-vous que le chemin absolu vers votre répertoire 'uploads' est correct
        registry.addResourceHandler("/camp/campingprograms/uploads/**")
                .addResourceLocations("file:./campingModules/uploads/"); // Remplacez par le chemin réel du répertoire 'uploads'
    }
}