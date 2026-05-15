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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.examhero.entity.QuestionAttempt;
import com.example.examhero.service.QuestionAttemptService;
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
 * - GET  /questions              : 問題カード一覧画面
 * - GET  /questions/new          : 問題カード作成画面
 * - POST /questions              : 問題カード保存処理
 * - GET  /questions/{id}         : 問題カード詳細画面
 * - GET  /questions/{id}/solve   : 問題を解く画面
 * - POST /questions/{id}/solve   : 解答を送信して正誤判定する処理
 *
 * 重要:
 *   問題カードはユーザーごとの個人データです。
 *   そのため、問題を取得するときは必ずログイン中ユーザーも条件にします。
 */
@Controller
public class QuestionController {

    /**
     * 問題カードの処理を担当するServiceです。
     *
     * Controller は DB を直接操作せず、
     * Service にアプリケーション処理を依頼します。
     */
    private final QuestionCardService questionCardService;

    /**
     * 試験カテゴリの処理を担当するServiceです。
     *
     * 問題カード作成画面で、
     * ログイン中ユーザーのカテゴリ一覧を表示するために使います。
     */
    private final ExamCategoryService examCategoryService;

    /**
     * 問題を解いた記録を保存するServiceです。
     *
     * 問題を解いたあと、
     * 「誰が・どの問題を・どの答えで・正解だったか」
     * を DB に保存するために使います。
     */
    private final QuestionAttemptService questionAttemptService;

    /**
     * コンストラクタ
     *
     * Spring が QuestionCardService と ExamCategoryService を自動で渡してくれます。
     * これをコンストラクタインジェクションと呼びます。
     */
    public QuestionController(
        QuestionCardService questionCardService,
        ExamCategoryService examCategoryService,
        QuestionAttemptService questionAttemptService
    ) {
        this.questionCardService = questionCardService;
        this.examCategoryService = examCategoryService;
        this.questionAttemptService = questionAttemptService;
    }

    /**
     * 問題カード一覧画面を表示します。
     *
     * GET /questions にアクセスしたときに呼ばれます。
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
         * findAll() は使いません。
         * findAll() を使うと、他のユーザーの問題カードまで表示される危険があります。
         */
        List<QuestionCard> questionCards = questionCardService.findQuestionCardsByUser(loginUser);

        /*
         * Model に入れた値は Thymeleaf のHTML側で使えます。
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
         */
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categories);
            model.addAttribute("loginEmail", loginUser.getEmail());
            return "questions/form";
        }

        try {
            /*
             * Service に問題カード作成処理を依頼します。
             */
            questionCardService.createQuestionCard(form, loginUser);

            /*
             * 保存成功メッセージをリダイレクト先に渡します。
             */
            redirectAttributes.addFlashAttribute("successMessage", "問題カードを作成しました");

            /*
             * 二重送信を防ぐため、保存成功後は redirect を使います。
             */
            return "redirect:/questions";

        } catch (IllegalArgumentException e) {
            /*
             * Service側で発生した業務エラーを画面に表示します。
             *
             * 例:
             * - カテゴリが見つかりません
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
         * findById(id) だけを使うと、
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

    /**
     * 指定カテゴリ内の問題を「番号ボタン」で表示します。
     *
     * GET /questions/categories/{categoryId}/select
     *
     * これは問題カード管理用の一覧ではなく、学習開始用の画面です。
     *
     * 管理用一覧では問題文や正解が見えてしまいます。
     * しかし、学習開始前に問題文や正解が見えると、
     * 問題演習として不自然になります。
     *
     * そのため、この画面では
     * 「第1問」「第2問」のような番号ボタンだけを表示します。
     */
    @GetMapping("/questions/categories/{categoryId}/select")
    public String selectQuestionsByCategory(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        /*
         * ログイン中ユーザーを取得します。
         */
        User loginUser = questionCardService.findUserByEmail(userDetails.getUsername());

        /*
         * カテゴリを取得します。
         *
         * categoryId だけで取得すると他ユーザーのカテゴリにアクセスできる危険があります。
         * そのため、必ず categoryId + loginUser で検索します。
         */
        ExamCategory category = examCategoryService.findCategoryByIdAndUser(categoryId, loginUser);

        /*
         * 指定カテゴリ内の問題カードだけを取得します。
         *
         * QuestionCardService 内では、
         * カテゴリがログイン中ユーザーのものか再確認したうえで検索します。
         */
        List<QuestionCard> questionCards =
                questionCardService.findQuestionCardsByCategory(loginUser, categoryId);

        /*
         * 画面で使うデータを Model に入れます。
         */
        model.addAttribute("loginEmail", loginUser.getEmail());
        model.addAttribute("category", category);
        model.addAttribute("questionCards", questionCards);

        return "questions/select";
    }

    /**
     * 問題を解く画面を表示します。
     *
     * GET /questions/{id}/solve にアクセスしたときに呼ばれます。
     *
     * この時点では、まだユーザーは解答していません。
     * そのため answered は false にして画面へ渡します。
     *
     * @PathVariable:
     *   URLの一部をJavaの引数として受け取るためのアノテーションです。
     *
     * 例:
     *   /questions/5/solve
     *   → id = 5
     *
     * 重要:
     *   問題カードは個人データなので、
     *   IDだけではなく、ログイン中ユーザーも条件にして取得します。
     */
    @GetMapping("/questions/{id}/solve")
    public String solveForm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        /*
         * ログイン中ユーザーを取得します。
         */
        User loginUser = questionCardService.findUserByEmail(userDetails.getUsername());

        /*
         * 問題カードを取得します。
         *
         * 必ず id + loginUser で検索します。
         * これにより、他のユーザーの問題カードを直接URL入力で見られることを防ぎます。
         */
        QuestionCard questionCard = questionCardService.findQuestionCardByIdAndUser(id, loginUser);

        /*
         * solve.html で使うデータを Model に入れます。
         *
         * answered = false:
         *   まだ解答していない状態を表します。
         *
         * selectedAnswer = null:
         *   まだ選択した答えがない状態です。
         *
         * isCorrect = null:
         *   まだ正誤判定をしていない状態です。
         */
        model.addAttribute("questionCard", questionCard);
        model.addAttribute("loginEmail", loginUser.getEmail());
        model.addAttribute("answered", false);
        model.addAttribute("selectedAnswer", null);
        model.addAttribute("isCorrect", null);

        /*
         * templates/questions/solve.html を表示します。
         */
        return "questions/solve";
    }

    /**
     * ユーザーが選んだ答えを受け取り、正誤判定を行い、解答履歴をDBに保存します。
     *
     * POST /questions/{id}/solve に送信されたときに呼ばれます。
     *
     * 今回のステップでは、
     * ただ画面に正解・不正解を表示するだけでなく、
     * question_attempts テーブルに解答履歴を保存します。
     */
    @PostMapping("/questions/{id}/solve")
    public String solveSubmit(
            @PathVariable Long id,
            @RequestParam(name = "selectedAnswer", required = false) String selectedAnswer,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        /*
        * ログイン中ユーザーを取得します。
        */
        User loginUser = questionCardService.findUserByEmail(userDetails.getUsername());

        /*
        * 問題カードを取得します。
        *
        * ここでも必ず id + loginUser で検索します。
        * これにより、他のユーザーの問題カードを直接URL入力で解くことを防ぎます。
        */
        QuestionCard questionCard = questionCardService.findQuestionCardByIdAndUser(id, loginUser);

        /*
        * 何も選ばずに「解答する」ボタンを押した場合の処理です。
        *
        * required = false にしているため、
        * 未選択の場合 selectedAnswer は null になります。
        */
        if (selectedAnswer == null || selectedAnswer.isBlank()) {
            model.addAttribute("questionCard", questionCard);
            model.addAttribute("loginEmail", loginUser.getEmail());
            model.addAttribute("answered", false);
            model.addAttribute("selectedAnswer", null);
            model.addAttribute("isCorrect", null);
            model.addAttribute("errorMessage", "選択肢を1つ選んでください。");

            return "questions/solve";
        }

        try {
            /*
            * 解答履歴を保存します。
            *
            * ここで Service に任せていること:
            * - selectedAnswer を整形する
            * - A〜D のどれか確認する
            * - 正解かどうか判定する
            * - QuestionAttempt Entity を作る
            * - DB に保存する
            */
            QuestionAttempt questionAttempt = questionAttemptService.saveAttempt(
                    loginUser,
                    questionCard,
                    selectedAnswer
            );

            /*
            * 保存された解答履歴から、画面表示に必要な値を取り出します。
            */
            model.addAttribute("questionCard", questionCard);
            model.addAttribute("loginEmail", loginUser.getEmail());
            model.addAttribute("answered", true);
            model.addAttribute("selectedAnswer", questionAttempt.getSelectedAnswer());
            model.addAttribute("isCorrect", questionAttempt.isCorrect());

            /*
            * 正誤判定の結果を表示するため、
            * 同じ solve.html をもう一度表示します。
            */
            return "questions/solve";

        } catch (IllegalArgumentException e) {
            /*
            * 不正な選択肢が送られた場合などは、エラーメッセージを表示します。
            */
            model.addAttribute("questionCard", questionCard);
            model.addAttribute("loginEmail", loginUser.getEmail());
            model.addAttribute("answered", false);
            model.addAttribute("selectedAnswer", null);
            model.addAttribute("isCorrect", null);
            model.addAttribute("errorMessage", e.getMessage());

            return "questions/solve";
        }
    }
}