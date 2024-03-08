package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

@Service
public class ReportService {

    private final ReportRepository repository;

    /**
     * コンストラクタ
     * @param repository 日報ポジトリのインスタンス
     */
    @Autowired
    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    /**
     * 日報の検索
     * 削除フラグオフのレコード全てを検索する
     * @return 検索結果
     */
    public List<Report> findAll(){
        //報告日の降順、従業員コードの昇順で表示する
        return repository.findAllByOrderByReportDateDescEmployeeCode();
    }

    /**
     * 日報の検索
     * 日報のIDで検索、削除フラグオフのレコードを検索する
     * @param id 日報のID
     * @return 検索結果
     */
    public Report findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * 日報の検索
     * 従業員コードで検索、削除フラグオフのレコードを検索する
     * @param employee 従業員情報
     * @return 検索結果
     */
    public List<Report> findByEmployee(Employee employee){
        //JPAで専用メソッド作ると名前が長すぎるのでstreamを使うことにする
        List<Report> reports = this.findAll().stream()
                .filter(x -> x.getEmployee().getCode().equals(employee.getCode()))
                .collect(Collectors.toList());

        return reports;
    }

    /**
     * 日報の追加
     * @param report 日報情報
     * @return 処理結果
     */
    @Transactional
    public ErrorKinds save(Report report) {

        //同じ従業員で同じ日の日報が登録されている場合はエラーとする
        List<Report> reports = repository.findByEmployeeCodeAndReportDate(
                report.getEmployee().getCode(), report.getReportDate());
        if(reports.size() > 0) {
            return ErrorKinds.ALREADY_REGIST_REPORT;
        }

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        report.setDeleteFlg(false);

        repository.save(report);
        return ErrorKinds.SUCCESS;
    }

    /**
     * 日報の削除
     * @param id 日報のID
     */
    @Transactional
    public void delete(Integer id) {
        Report report = repository.findById(id).orElse(null);

        //削除済なら何もしない
        if(Objects.isNull(report)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        repository.save(report);
    }

    /**
     * 日報の更新
     * @param report 日報情報
     * @return 処理結果
     */
    @Transactional
    public ErrorKinds update(Report report) {

        //同じ従業員で同じ日の日報が登録されている場合はエラーとする
        List<Report> reports = repository.findByEmployeeCodeAndReportDateAndIdNot(
                report.getEmployee().getCode(), report.getReportDate(), report.getId());
        if(reports.size() > 0) {
            return ErrorKinds.ALREADY_REGIST_REPORT;
        }

        // 更新前のデータを取得する
        Report current = findById(report.getId());
        if (Objects.isNull(current)) {
            // 削除済
            return ErrorKinds.ALREADY_DELETED;
        }

        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);

        repository.save(report);
        return ErrorKinds.SUCCESS;
    }
}