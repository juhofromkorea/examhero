package com.example.examhero.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.examhero.dto.QuestionCardForm;
import com.example.examhero.entity.ExamCategory;
import com.example.examhero.entity.QuestionCard;
import com.example.examhero.entity.User;
import com.example.examhero.service.ExamCategoryService;
import com.example.examhero.service.QuestionCardService;

/**
 * QuestionController
 *
 * 問題カード画面に関するリクエストを受け取るControllerです。
 *
 * Controller の役割:
 * - ブラウザからのリクエストを受け取る
 * - Service を呼び出す
 * - 画面に渡すデータを Model に入れる
 * - 表示するHTMLテンプレート名を返す
 *
 * このControllerで扱うURL:
 * - GET  /questions          : 問題カード一覧画面
 * - GET  /questions/new      : 問題カード作成画面
 * - POST /questions          : 問題カード保存処理
 * - GET  /questions/{id}     : 問題カード詳細画面
 *
 * 重要:
 *   問題カードはユーザーごとの個人データです。
 *   そのため、一覧・詳細・作成処理では必ずログイン中ユーザーを確認します。
 */
@Controller
public class QuestionController {

    /**
     * 問題カードの処理を担当するServiceです。
     *
     * Controller は細かいDB操作を直接行わず、
     * Service に処理を依頼します。
     */
    private final QuestionCardService questionCardService;

    /**
     * 試験カテゴリの処理を担当するServiceです。
     *
     * 問題カード作成画面では、カテゴリ選択欄を表示する必要があります。
     * そのため、ログイン中ユーザーのカテゴリ一覧を取得するために使います。
     */
    private final ExamCategoryService examCategoryService;

    /**
     * コンストラクタ
     *
     * Spring が QuestionCardService と ExamCategoryService を自動で渡してくれます。
     * これをコンストラクタインジェクションと呼びます。
     */
    public QuestionController(
            QuestionCardService questionCardService,
            ExamCategoryService examCategoryService
    ) {
        this.questionCardService = questionCardService;
        this.examCategoryService = examCategoryService;
    }

    /**
     * 問題カード一覧画面を表示します。
     *
     * GET /questions にアクセスしたときに呼ばれます。
     *
     * @AuthenticationPrincipal:
     *   現在ログインしているユーザー情報を受け取るためのアノテーションです。
     *
     * UserDetails:
     *   Spring Security がログインユーザーを表すために使うオブジェクトです。
     *
     * このアプリではメールアドレスをログインIDとして使っているため、
     * userDetails.getUsername() でログイン中ユーザーのメールアドレスを取得できます。
     *
     * @param userDetails ログイン中ユーザー情報
     * @param model 画面に渡すデータを入れる箱
     * @return questions/list.html を表示する
     */
    @GetMapping("/questions")
    public String list(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        /*
         * Spring Security から取得したメールアドレスを使って、
         * DB上の User Entity を取得します。
         */
        User loginUser = questionCardService.findUserByEmail(userDetails.getUsername());

        /*
         * ログイン中ユーザーが作成した問題カードだけを取得します。
         *
         * ここで findAll() は使いません。
         * findAll() を使うと、他のユーザーの問題カードまで表示される危険があります。
         */
        List<QuestionCard> questionCards = questionCardService.findQuestionCardsByUser(loginUser);

        /*
         * Model に入れた値は、Thymeleaf のHTML側で使えます。
         *
         * HTML側の例:
         *   th:each="questionCard : ${questionCards}"
         */
        model.addAttribute("questionCards", questionCards);
        model.addAttribute("loginEmail", loginUser.getEmail());

        /*
         * templates/questions/list.html を表示します。
         */
        return "questions/list";
    }

    /**
     * 問題カード作成画面を表示します。
     *
     * GET /questions/new にアクセスしたときに呼ばれます。
     *
     * 作成画面では、問題文や選択肢だけでなく、
     * 「どの試験カテゴリに登録するか」を選ぶ必要があります。
     *
     * そのため、ログイン中ユーザーのカテゴリ一覧も画面に渡します。
     *
     * @param userDetails ログイン中ユーザー情報
     * @param model 画面に渡すデータを入れる箱
     * @return questions/form.html を表示する
     */
    @GetMapping("/questions/new")
    public String newForm(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        /*
         * ログイン中ユーザーを取得します。
         */
        User loginUser = questionCardService.findUserByEmail(userDetails.getUsername());

        /*
         * 問題カードはカテゴリに所属する必要があるため、
         * ログイン中ユーザーのカテゴリ一覧を取得します。
         */
        List<ExamCategory> categories = examCategoryService.findCategoriesByUser(loginUser);

        /*
         * th:object="${questionCardForm}" でフォームと紐づけるため、
         * 空の QuestionCardForm を Model に入れておきます。
         *
         * これがないと、Thymeleaf の th:field が参照する対象がなくなり、
         * エラーになることがあります。
         */
        model.addAttribute("questionCardForm", new QuestionCardForm());

        /*
         * カテゴリ選択欄に表示するための一覧です。
         */
        model.addAttribute("categories", categories);
        model.addAttribute("loginEmail", loginUser.getEmail());

        /*
         * templates/questions/form.html を表示します。
         */
        return "questions/form";
    }

    /**
     * 問題カード作成フォームから送信された内容を保存します。
     *
     * POST /questions に送信されたときに呼ばれます。
     *
     * @Valid:
     *   QuestionCardForm に書いた @NotBlank、@Size、@Pattern などを使って入力チェックします。
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
     * @param model 画面に再表示するデータを入れる箱
     * @param redirectAttributes リダイレクト先に渡す一時メッセージ
     * @return 成功時は問題一覧へ、失敗時は作成画面へ戻る
     */
    @PostMapping("/questions")
    public String create(
            @Valid @ModelAttribute("questionCardForm") QuestionCardForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        /*
         * ログイン中ユーザーを取得します。
         *
         * バリデーションエラーで作成画面に戻る場合も、
         * カテゴリ一覧を再表示する必要があるため、
         * 先に loginUser と categories を取得しておきます。
         */
        User loginUser = questionCardService.findUserByEmail(userDetails.getUsername());
        List<ExamCategory> categories = examCategoryService.findCategoriesByUser(loginUser);

        /*
         * 入力チェックでエラーがある場合は、保存せずに作成画面へ戻します。
         *
         * 注意:
         *   作成画面ではカテゴリ選択欄が必要なので、
         *   categories を Model に入れ直す必要があります。
         *
         * これを忘れると、エラー時に画面を再表示したとき
         * カテゴリの select box が作れず、テンプレートエラーになることがあります。
         */
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categories);
            model.addAttribute("loginEmail", loginUser.getEmail());
            return "questions/form";
        }

        try {
            /*
             * Service に問題カード作成処理を依頼します。
             *
             * Controller は「保存する」という指示だけを出し、
             * 実際の Entity 作成や DB 保存は Service に任せます。
             */
            questionCardService.createQuestionCard(form, loginUser);

            /*
             * 保存成功メッセージをリダイレクト先に渡します。
             *
             * addFlashAttribute は1回だけ表示される一時メッセージです。
             */
            redirectAttributes.addFlashAttribute("successMessage", "問題カードを作成しました");

            /*
             * 二重送信を防ぐため、保存成功後は redirect を使います。
             *
             * これを PRGパターン と呼びます。
             * Post → Redirect → Get
             */
            return "redirect:/questions";

        } catch (IllegalArgumentException e) {
            /*
             * Service側で発生した業務エラーを画面に表示します。
             *
             * 例:
             * - カテゴリが見つかりません
             *
             * カテゴリIDを書き換えられた場合や、
             * すでに削除されたカテゴリを指定した場合に起こる可能性があります。
             */
            bindingResult.reject("questionCardError", e.getMessage());

            /*
             * エラーで作成画面に戻る場合も、カテゴリ一覧をもう一度渡します。
             */
            model.addAttribute("categories", categories);
            model.addAttribute("loginEmail", loginUser.getEmail());

            return "questions/form";
        }
    }

    /**
     * 問題カード詳細画面を表示します。
     *
     * GET /questions/{id} にアクセスしたときに呼ばれます。
     *
     * @PathVariable:
     *   URLの一部をJavaの引数として受け取るためのアノテーションです。
     *
     * 例:
     *   /questions/5
     *   → id = 5
     *
     * 重要:
     *   問題カードは個人データなので、
     *   IDだけではなく、ログイン中ユーザーも条件にして取得します。
     *
     * @param id URLに含まれる問題カードID
     * @param userDetails ログイン中ユーザー情報
     * @param model 画面に渡すデータを入れる箱
     * @return questions/detail.html を表示する
     */
    @GetMapping("/questions/{id}")
    public String detail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        /*
         * ログイン中ユーザーを取得します。
         */
        User loginUser = questionCardService.findUserByEmail(userDetails.getUsername());

        /*
         * 問題カードIDとログイン中ユーザーの両方で検索します。
         *
         * questionCardRepository.findById(id) だけを使うと、
         * 他のユーザーの問題カードを見られてしまう危険があります。
         */
        QuestionCard questionCard = questionCardService.findQuestionCardByIdAndUser(id, loginUser);

        /*
         * 詳細画面で表示するデータを Model に入れます。
         */
        model.addAttribute("questionCard", questionCard);
        model.addAttribute("loginEmail", loginUser.getEmail());

        /*
         * templates/questions/detail.html を表示します。
         */
        return "questions/detail";
    }
}