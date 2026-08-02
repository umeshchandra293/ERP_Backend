package com.hst.materialmgmt.repository.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import com.hst.materialmgmt.entity.auth.UserEntity;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Repository
public class UserRepository {

    @Autowired private DatabaseClient db;

    public Mono<UserEntity> findByUsername(String username) {
        return db.sql("SELECT * FROM rm_material_schema.users_tbl WHERE username = :u AND is_active = TRUE")
            .bind("u", username)
            .map((row, meta) -> UserEntity.builder()
                .userId(row.get("user_id",       String.class))
                .companyId(row.get("company_id", String.class))
                .username(row.get("username",     String.class))
                .passwordHash(row.get("password_hash", String.class))
                .fullName(row.get("full_name",    String.class))
                .role(row.get("role",             String.class))
                .isActive(row.get("is_active",    Boolean.class))
                .lastLoginAt(row.get("last_login_at", LocalDateTime.class))
                .build())
            .one();
    }

    public Mono<Void> updateLastLogin(String username) {
        return db.sql("UPDATE rm_material_schema.users_tbl SET last_login_at = NOW() WHERE username = :u")
            .bind("u", username).fetch().rowsUpdated().then();
    }

    public Mono<Void> changePassword(String username, String newHash) {
        return db.sql("UPDATE rm_material_schema.users_tbl SET password_hash = :h, updated_at = NOW() WHERE username = :u")
            .bind("h", newHash).bind("u", username).fetch().rowsUpdated().then();
    }
}