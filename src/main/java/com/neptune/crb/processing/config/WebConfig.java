package com.neptune.crb.processing.config;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import static com.neptune.crb.processing.util.Logger.logError;
import static com.neptune.crb.processing.util.Logger.logEvent;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // Allow all origins
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new LoggingInterceptor())
                .build();
    }

    static class LoggingInterceptor implements Interceptor
    {
        @NotNull
        @Override
        public Response intercept(Chain chain) throws IOException
        {
            Request request = chain.request();

            long startTime = System.nanoTime();
            logEvent("Sending request: ", request.url()+" on "+ chain.connection());

            Response response = chain.proceed(request);

            long endTime = System.nanoTime();
            logEvent(
                    String.format(
                            "Status: %d, URL: %s, Time: %d ms",
                            response.code(),
                            response.request().url(),
                            TimeUnit.NANOSECONDS.toMillis(endTime - startTime)
                    )
            );
            return response;
        }
    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:5173", "https://mydomain.com") // Specify allowed origins
//                .allowedMethods("GET", "POST", "PUT", "DELETE")
//                .allowedHeaders("*")
//                .allowCredentials(true); // Enable credentials (cookies, auth headers)
//    }
}

