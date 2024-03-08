package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReportService reportService;

    /**
     * コンストラクタ
     * @param employeeRepository 従業員リポジトリのインスタンス
     * @param passwordEncoder パスワードエンコーダーのインスタンス
     * @param reportService 日報サービスのインスタンス
     */
    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository
            , PasswordEncoder passwordEncoder
            , ReportService reportService) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.reportService = reportService;
    }

    /**
     * 従業員保存
     * @param employee 従業員情報
     * @return 処理結果
     */
    @Transactional
    public ErrorKinds save(Employee employee) {

        // パスワードチェック
        ErrorKinds result = employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 従業員番号重複チェック
        if (findByCode(employee.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        employee.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

    /**
     * 従業員保存
     * @param employee 従業員情報
     * @return 処理結果
     */
    @Transactional
    public ErrorKinds update(Employee employee) {

        // 更新前のデータを取得する
        Employee current = findByCode(employee.getCode());
        if (Objects.isNull(current)) {
            // 削除済
            return ErrorKinds.ALREADY_DELETED;
        }

        // パスワード
        if(! employee.getPassword().isEmpty()) {
            // パスワードチェック
            ErrorKinds result = employeePasswordCheck(employee);
            if (ErrorKinds.CHECK_OK != result) {
                return result;
            }
        } else {
            employee.setPassword(current.getPassword());
        }
        // 作成時間
        employee.setCreatedAt(current.getCreatedAt());

        // 更新時間を更新する
        LocalDateTime now = LocalDateTime.now();
        employee.setUpdatedAt(now);

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

    /**
     * 従業員削除
     * @param code 従業員コード
     * @param userDetail ログイン情報
     * @return 処理結果
     */
    @Transactional
    public ErrorKinds delete(String code, UserDetail userDetail) {

        // 自分を削除しようとした場合はエラーメッセージを表示
        if (code.equals(userDetail.getEmployee().getCode())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }

        Employee employee = findByCode(code);

        // 入れ違いで削除済なら何もしない
        if(Objects.nonNull(employee)) {
            // 日報があれば削除する
            List<Report> reports = this.reportService.findByEmployee(employee);
            for(Report report : reports) {
                this.reportService.delete(report.getId());
            }


            LocalDateTime now = LocalDateTime.now();
            employee.setUpdatedAt(now);
            employee.setDeleteFlg(true);
        }

        return ErrorKinds.SUCCESS;
    }

    /**
     * 従業員の検索
     * 削除フラグオフのレコードを検索する
     * @return　検索結果
     */
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    /**
     * 従業員の検索
     * 従業員コードで検索、削除フラグオフのレコードを検索する
     * @param code 従業員コード
     * @return 検索結果
     */
    public Employee findByCode(String code) {
        // findByIdで検索
        Optional<Employee> option = employeeRepository.findById(code);
        // 取得できなかった場合はnullを返す
        Employee employee = option.orElse(null);
        return employee;
    }

    /**
     * 従業員パスワードチェック
     * @param employee 従業員情報
     * @return 処理結果
     */
    private ErrorKinds employeePasswordCheck(Employee employee) {

        // 従業員パスワードの半角英数字チェック処理
        if (isHalfSizeCheckError(employee)) {

            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 従業員パスワードの8文字～16文字チェック処理
        if (isOutOfRangePassword(employee)) {

            return ErrorKinds.RANGECHECK_ERROR;
        }

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        return ErrorKinds.CHECK_OK;
    }

    /**
     * 従業員パスワードの半角英数字チェック処理
     * @param employee 従業員情報
     * @return 処理結果
     */
    private boolean isHalfSizeCheckError(Employee employee) {

        // 半角英数字チェック
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());
        return !matcher.matches();
    }

    /**
     * 従業員パスワードの8文字～16文字チェック処理
     * @param employee 従業員情報
     * @return 処理結果
     */
    public boolean isOutOfRangePassword(Employee employee) {

        // 桁数チェック
        int passwordLength = employee.getPassword().length();
        return passwordLength < 8 || 16 < passwordLength;
    }

}