package com.green.campingsmore.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig {

    @PersistenceContext
    private EntityManager em;

    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
