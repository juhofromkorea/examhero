package com.example.examhero.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.examhero.entity.ExamCategory;
import com.example.examhero.entity.User;
import com.example.examhero.service.ExamCategoryService;
import com.example.examhero.service.QuestionAttemptService;
import com.example.examhero.service.QuestionCardService;

/**
 * DashboardController
 *
 * ログイン後のメイン画面を担当するControllerです。
 *
 * 今回の変更点:
 * - ダッシュボードにカテゴリ一覧を表示する
 * - カテゴリをクリックすると、そのカテゴリの問題番号一覧へ進む
 * - カテゴリ数、問題カード数、今日解いた問題数を実データで表示する
 */
@Controller
public class DashboardController {

    /**
     * 試験カテゴリに関する処理を担当するServiceです。
     */
    private final ExamCategoryService examCategoryService;

    /**
     * 問題カードに関する処理を担当するServiceです。
     */
    private final QuestionCardService questionCardService;

    /**
     * 問題を解いた記録に関する処理を担当するServiceです。
     */
    private final QuestionAttemptService questionAttemptService;

    /**
     * コンストラクタ
     *
     * Spring が必要なServiceを自動で渡してくれます。
     */
    public DashboardController(
            ExamCategoryService examCategoryService,
            QuestionCardService questionCardService,
            QuestionAttemptService questionAttemptService
    ) {
        this.examCategoryService = examCategoryService;
        this.questionCardService = questionCardService;
        this.questionAttemptService = questionAttemptService;
    }

    /**
     * ダッシュボード画面を表示します。
     *
     * GET /dashboard にアクセスされたときに実行されます。
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal) {

        /*
         * Spring Security からログイン中ユーザーのメールアドレスを取得します。
         */
        String loginEmail = principal.getName();

        /*
         * メールアドレスから、DB上の User Entity を取得します。
         */
        User loginUser = examCategoryService.findUserByEmail(loginEmail);

        /*
         * ダッシュボードに表示するカテゴリ一覧です。
         *
         * これにより、ログイン直後に
         * 「AWS SAA」「Java Silver」などのカテゴリカードを表示できます。
         */
        List<ExamCategory> categories = examCategoryService.findCategoriesByUser(loginUser);

        /*
         * ダッシュボード上部の数値カードに表示する実データです。
         */
        long categoryCount = examCategoryService.countCategoriesByUser(loginUser);
        long questionCount = questionCardService.countQuestionCardsByUser(loginUser);
        long todayAttemptCount = questionAttemptService.countTodayAttempts(loginUser);

        /*
         * HTML側で使う値を Model に入れます。
         */
        model.addAttribute("loginEmail", loginEmail);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryCount", categoryCount);
        model.addAttribute("questionCount", questionCount);
        model.addAttribute("todayReviewCount", todayAttemptCount);

        return "dashboard";
    }
}