
package com.techacademy.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

@Data
@Entity
@Table(name = "employees")
@SQLRestriction("delete_flg = false")
public class Employee {

    /**
     * 権限
     */
    public static enum Role {
        GENERAL("一般"), ADMIN("管理者");

        private String name;

        private Role(String name) {
            this.name = name;
        }

        public String getValue() {
            return this.name;
        }
    }

    /**
     * 従業員コード（主キー）
     */
    @Id
    @NotEmpty
    @Length(max = 10)
    private String code;

    /**
     * 名前
     */
    @NotEmpty
    @Length(max = 20)
    private String name;

    /**
     *  権限
     */
    @Column(length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     *  パスワード
     */
    private String password;

    /**
     *  削除フラグ(論理削除を行うため)
     */
    private boolean deleteFlg;

    /**
     *  登録日時
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAt;

    /**
     *  更新日時
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime updatedAt;

    /**
     * 日報情報
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Report> reports;
}