package com.techacademy.controller;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
import com.techacademy.service.EmployeeService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * コンストラクタ
     * @param employeeService EmployeeServiceオブジェクト
     */
    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * 従業員一覧画面
     * @param model モデル
     * @return viewの名称
     */
    @GetMapping
    public String list(Model model) {

        model.addAttribute("listSize", employeeService.findAll().size());
        model.addAttribute("employeeList", employeeService.findAll());

        return "employees/list";
    }

    /**
     * 従業員詳細画面
     * @param code 従業員コード
     * @param model モデル
     * @return viewの名称
     */
    @GetMapping(value = "/{code}")
    public String detail(@PathVariable String code, Model model) {

        Employee employee = employeeService.findByCode(code);
        if(Objects.isNull(employee)) {
            //入れ違いで削除されて表示不可なので従業員一覧にリダイレクト
            return "redirect:/employees/";
        }

        model.addAttribute("employee", employee);

        return "employees/detail";
    }

    /**
     * 従業員新規登録画面
     * @param employee 従業員情報
     * @return viewの名称
     */
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Employee employee) {

        return "employees/new";
    }

    /**
     * 従業員新規登録処理
     * @param employee 従業員情報
     * @param res バリデーション結果
     * @param model モデル
     * @return viewの名称
     */
    @PostMapping(value = "/add")
    public String add(@Validated Employee employee, BindingResult res, Model model) {

        // パスワード空白チェック
        /*
         * エンティティ側の入力チェックでも実装は行えるが、更新の方でパスワードが空白でもチェックエラーを出さずに
         * 更新出来る仕様となっているため上記を考慮した場合に別でエラーメッセージを出す方法が簡単だと判断
         */
        if ("".equals(employee.getPassword())) {
            // パスワードが空白だった場合
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));

            return create(employee);

        }

        // 入力チェック
        if (res.hasErrors()) {
            return create(employee);
        }

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
            ErrorKinds result = employeeService.save(employee);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(employee);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(employee);
        }

        return "redirect:/employees";
    }

    /**
     * 従業員削除処理
     * @param code 従業員コード
     * @param userDetail ログイン情報
     * @param model モデル
     * @return viewの名称
     */
    @PostMapping(value = "/{code}/delete")
    public String delete(@PathVariable String code, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        ErrorKinds result = employeeService.delete(code, userDetail);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("employee", employeeService.findByCode(code));
            return detail(code, model);
        }

        return "redirect:/employees";
    }

    /**
     * 従業員更新画面の表示
     * @param code 従業員コード
     * @param model モデル
     * @return viewの名称
     */
    @GetMapping(value = "/{code}/update")
    public String edit(@PathVariable String code, Model model) {

        Employee employee = employeeService.findByCode(code);
        if(Objects.isNull(employee)) {
            //入れ違いで削除されて更新不可なので従業員一覧にリダイレクト
            return "redirect:/employees/";
        }

        model.addAttribute("employee", employee);

        return "employees/edit";
    }

    /**
     * 従業員更新処理
     * @param employee 従業員情報
     * @param res バリデーション結果
     * @param model モデル
     * @return viewの名称
     */
    @PostMapping(value = "/{code}/update")
    public String update(@Validated Employee employee, BindingResult res, Model model) {
        // 入力チェック
        if (res.hasErrors()) {
            return "employees/edit";
        }

        ErrorKinds result = employeeService.update(employee);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return "employees/edit";
        }

        return "redirect:/employees";
    }
}