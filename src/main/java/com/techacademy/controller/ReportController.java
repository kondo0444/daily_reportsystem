package com.techacademy.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;

    /**
     * コンストラクタ
     * @param reportService ReportServiceオブジェクト
     */
    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 日報一覧表示
     * @param user ログイン情報
     * @param model モデル
     * @return viewの名称
     */
    @GetMapping("/")
    public String list(@AuthenticationPrincipal UserDetail user, Model model) {

        Employee employee = user.getEmployee();

        List<Report> reports = null;
        if(employee.getRole().equals(Employee.Role.ADMIN)) {
            //管理者は全ての日報を表示する
            reports = reportService.findAll();
        }else {
            //一般ユーザは自分の日報だけ表示する
            reports = reportService.findByEmployee(user.getEmployee());
        }

        model.addAttribute("listSize", reports.size());
        model.addAttribute("reportList", reports);

        return "reports/list";
    }

    /**
     * 日報新規登録画面
     * @param report 日報（モデル）
     * @param user ログイン情報
     * @return viewの名称
     */
    @GetMapping("/add")
    public String create(@ModelAttribute Report report,
            @AuthenticationPrincipal UserDetail user) {

        //ログイン情報から入れ直しする
        report.setEmployee(user.getEmployee());

        //日付はシステム日付を初期表示させる
        report.setReportDate(LocalDate.now());

        return "reports/new";
    }

    /**
     * 日報登録の実行
     * 注意：@Validatedの次の変数はBindingResultでなければいけない
     * @param report 日報（モデル）
     * @param res バリデーション結果
     * @param user ログイン情報
     * @param model モデル
     * @return viewの名称
     */
    @PostMapping("/add")
    public String add(@Validated Report report, BindingResult res,
            @AuthenticationPrincipal UserDetail user, Model model) {

        // 入力チェック
        if (res.hasErrors()) {
            return create(report, user);
        }

        ErrorKinds result = reportService.save(report);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return create(report, user);
        }

        return "redirect:/reports/";
    }

    /**
     * 日報詳細画面
     * @param id 日報の主キー
     * @param model モデル
     * @return viewの名称
     */
    @GetMapping(value = "/{id}")
    public String detail(@PathVariable Integer id, Model model) {

        Report report = reportService.findById(id);
        if(Objects.isNull(report)) {
            //入れ違いで削除されて表示不可なので一覧にリダイレクト
            return "redirect:/reports/";
        }

        model.addAttribute("report", report);
        return "reports/detail";
    }

    /**
     * 日報削除
     * @param id 日報の主キー
     * @return viewの名称
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {

        reportService.delete(id);

        return "redirect:/reports/";
    }

    /**
     * 日報編集画面
     * @param id 日報の主キー
     * @param model モデル
     * @return viewの名称
     */
    @GetMapping(value = "/{id}/update")
    public String edit(@PathVariable Integer id, Model model) {

        Report report = reportService.findById(id);
        if(Objects.isNull(report)) {
            //入れ違いで削除されて更新不可なので一覧にリダイレクト
            return "redirect:/reports/";
        }

        model.addAttribute("report", report);

        return "reports/edit";
    }

    /**
     * 日報編集実行
     * @param report 日報エンティティ
     * @param res バリデーション結果
     * @param model モデル
     * @return viewの名称
     */
    @PostMapping(value = "/{id}/update")
    public String update(@Validated Report report, BindingResult res, Model model) {

        // 入力チェック
        if (res.hasErrors()) {
            return "/reports/edit";
        }

        ErrorKinds result = reportService.update(report);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return "reports/edit";
        }

        return "redirect:/reports/";
    }
}