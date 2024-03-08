package com.techacademy.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    /**
     * 日報検索
     * @param employeeCode 従業員コード
     * @param date 報告日
     * @return 日報リスト
     */
    public List<Report> findByEmployeeCodeAndReportDate(String employeeCode, LocalDate date);

    /**
     * 日報検索
     * @param employeeCode 従業員コード
     * @param date 報告日
     * @param id ID
     * @return 日報リスト
     */
    public List<Report> findByEmployeeCodeAndReportDateAndIdNot(String employeeCode, LocalDate date, Integer id);

    /**
     * 日報検索
     * 全検索、報告日と従業員コードでソート
     * @return
     */
    public List<Report> findAllByOrderByReportDateDescEmployeeCode();
}