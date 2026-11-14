package com.example.examhero.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.examhero.dto.ExamCategoryForm;
import com.example.examhero.entity.ExamCategory;
import com.example.examhero.entity.User;
import com.example.examhero.service.ExamCategoryService;

/**
 * ExamCategoryController
 *
 * 試験カテゴリ画面に関するリクエストを受け取るControllerです。
 *
 * Controller の役割:
 * - ブラウザからのリクエストを受け取る
 * - Service を呼び出す
 * - 画面に渡すデータを Model に入れる
 * - 表示するHTMLテンプレート名を返す
 *
 * このControllerで扱うURL:
 * - GET  /categories      : カテゴリ一覧画面
 * - GET  /categories/new  : カテゴリ作成画面
 * - POST /categories      : カテゴリ保存処理
 */
@Controller
public class ExamCategoryController {

    /**
     * 試験カテゴリの処理を担当するServiceです。
     *
     * Controller から直接 Repository を呼ばず、
     * Service を経由することで役割分担を分かりやすくしています。
     */
    private final ExamCategoryService examCategoryService;

    /**
     * コンストラクタ
     *
     * Spring が ExamCategoryService を自動で渡してくれます。
     */
    public ExamCategoryController(ExamCategoryService examCategoryService) {
        this.examCategoryService = examCategoryService;
    }

    /**
     * カテゴリ一覧画面を表示します。
     *
     * @AuthenticationPrincipal:
     *   現在ログインしているユーザー情報を受け取るためのアノテーションです。
     *
     * UserDetails:
     *   Spring Security がログインユーザーを表すために使うオブジェクトです。
     *
     * 注意:
     *   このアプリでは、ログインIDとしてメールアドレスを使っています。
     *   そのため userDetails.getUsername() でメールアドレスを取得できます。
     *
     * @param userDetails ログイン中ユーザー情報
     * @param model 画面に渡すデータを入れる箱
     * @return categories/list.html を表示する
     */
    @GetMapping("/categories")
    public String list(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        /*
         * Spring Security から取得したメールアドレスを使って、
         * DB上の User Entity を取得します。
         */
        User loginUser = examCategoryService.findUserByEmail(userDetails.getUsername());

        /*
         * ログイン中ユーザーが作成したカテゴリだけを取得します。
         */
        List<ExamCategory> categories = examCategoryService.findCategoriesByUser(loginUser);

        /*
         * Model に入れた値は、Thymeleaf のHTML側で使えます。
         *
         * 例:
         *   model.addAttribute("categories", categories);
         *
         * HTML側:
         *   th:each="category : ${categories}"
         */
        model.addAttribute("categories", categories);
        model.addAttribute("loginEmail", loginUser.getEmail());

        /*
         * templates/categories/list.html を表示します。
         */
        return "categories/list";
    }

    /**
     * カテゴリ作成画面を表示します。
     *
     * GET /categories/new にアクセスしたときに呼ばれます。
     *
     * @param model 画面に渡すデータを入れる箱
     * @return categories/form.html を表示する
     */
    @GetMapping("/categories/new")
    public String newForm(Model model) {

        /*
         * th:object="${examCategoryForm}" でフォームと紐づけるため、
         * 空の ExamCategoryForm を Model に入れておきます。
         *
         * これがないと、Thymeleaf の th:field が参照する対象がなくなり、
         * エラーになることがあります。
         */
        model.addAttribute("examCategoryForm", new ExamCategoryForm());

        return "categories/form";
    }

    /**
     * カテゴリ作成フォームから送信された内容を保存します。
     *
     * POST /categories に送信されたときに呼ばれます。
     *
     * @Valid:
     *   ExamCategoryForm に書いた @NotBlank や @Size を使って入力チェックします。
     *
     * BindingResult:
     *   バリデーション結果を受け取ります。
     *
     * 重要:
     *   BindingResult は、@Valid を付けた引数の直後に書く必要があります。
     *   順番が離れると正しくエラーを受け取れない場合があります。
     *
     * RedirectAttributes:
     *   リダイレクト先に一時的なメッセージを渡すために使います。
     *
     * @param form 画面から送られてきた入力値
     * @param bindingResult 入力チェック結果
     * @param userDetails ログイン中ユーザー情報
     * @param redirectAttributes リダイレクト先に渡す一時メッセージ
     * @return 成功時はカテゴリ一覧へ、失敗時は作成画面へ戻る
     */
    @PostMapping("/categories")
    public String create(
            @Valid @ModelAttribute("examCategoryForm") ExamCategoryForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        /*
         * 入力チェックでエラーがある場合は、保存せずに作成画面へ戻します。
         *
         * 例:
         * - カテゴリ名が空
         * - カテゴリ名が50文字を超えている
         * - 説明が500文字を超えている
         */
        if (bindingResult.hasErrors()) {
            return "categories/form";
        }

        try {
            /*
             * ログイン中ユーザーを取得します。
             */
            User loginUser = examCategoryService.findUserByEmail(userDetails.getUsername());

            /*
             * Service にカテゴリ作成処理を依頼します。
             *
             * Controller は細かいDB保存処理を持たず、
             * Service に任せるのが基本です。
             */
            examCategoryService.createCategory(form, loginUser);

            /*
             * 保存成功メッセージをリダイレクト先に渡します。
             *
             * addFlashAttribute は1回だけ表示される一時メッセージです。
             */
            redirectAttributes.addFlashAttribute("successMessage", "カテゴリを作成しました");

            /*
             * 二重送信を防ぐため、保存成功後は redirect を使います。
             *
             * これを PRGパターン と呼びます。
             * Post → Redirect → Get
             */
            return "redirect:/categories";

        } catch (IllegalArgumentException e) {
            /*
             * Service側で発生した業務エラーを画面に表示します。
             *
             * 例:
             * - 同じ名前のカテゴリがすでに存在します
             */
            bindingResult.reject("categoryError", e.getMessage());
            return "categories/form";
        }
    }
}