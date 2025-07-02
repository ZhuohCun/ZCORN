package net.zcscloud.zhuohcun.zcorn;

import org.modelmapper.ModelMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@MapperScan("net.zcscloud.zhuohcun.zcorn.dao")
public class ZcornApplication {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    public static void main(String[] args) {
            try{
                SpringApplication.run(ZcornApplication.class, args);
            }catch(Exception e){

            }
    }

}
