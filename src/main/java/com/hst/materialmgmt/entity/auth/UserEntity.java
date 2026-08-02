package com.hst.materialmgmt.entity.auth;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Table(value = "users_tbl", schema = "rm_material_schema")
public class UserEntity {
    @Id @Column("user_id")         private String        userId;
    @Column("company_id")          private String        companyId;
    @Column("username")            private String        username;
    @Column("password_hash")       private String        passwordHash;
    @Column("full_name")           private String        fullName;
    @Column("role")                private String        role;
    @Column("is_active")           private Boolean       isActive;
    @Column("last_login_at")       private LocalDateTime lastLoginAt;
    @Column("created_at")          private LocalDateTime createdAt;
    @Column("updated_at")          private LocalDateTime updatedAt;
}