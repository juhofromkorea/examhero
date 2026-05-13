package com.example.examhero.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * DashboardController
 *
 * ログイン後のメイン画面を担当するControllerです。
 *
 * ダッシュボードとは、ユーザーがログインした後に最初に見る画面です。
 *
 * この画面では、今後以下のような情報を表示していく予定です。
 *
 * - 登録した試験カテゴリ数
 * - 登録した問題カード数
 * - 今日復習する問題数
 * - 学習の進捗状況
 * - 最近解いた問題
 *
 * ただし、現時点ではまだカテゴリや問題カードのEntityを作っていないため、
 * まずは「ログインしたユーザー情報」と「各機能へのリンク」を表示します。
 */
@Controller
public class DashboardController {

    /**
     * ダッシュボード画面を表示するメソッドです。
     *
     * GET /dashboard にアクセスされたときに実行されます。
     *
     * SecurityConfig.java で以下のように設定しているため、
     * ログインに成功すると /dashboard に移動します。
     *
     * .defaultSuccessUrl("/dashboard", true)
     *
     * また、SecurityConfig.java では /dashboard はログイン必須になっています。
     * そのため、ログインしていないユーザーが /dashboard にアクセスすると、
     * 自動的に /login にリダイレクトされます。
     *
     * @param model
     *   Controller から HTML にデータを渡すための入れ物です。
     *
     * @param principal
     *   現在ログインしているユーザー情報を表すオブジェクトです。
     *
     *   Spring Security によってログイン済みの場合、
     *   principal.getName() でログインIDを取得できます。
     *
     *   今回のアプリでは、ログインIDとしてメールアドレスを使っているため、
     *   principal.getName() にはメールアドレスが入ります。
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal) {

        /**
         * ログイン中のユーザーのメールアドレスを取得します。
         *
         * 例:
         *   juho@example.com
         *
         * 現在は CustomUserDetailsService で、
         * Spring Security 用のユーザー名として user.getEmail() を渡しています。
         *
         * そのため、principal.getName() でメールアドレスを取得できます。
         */
        String loginEmail = principal.getName();

        /**
         * HTML側で loginEmail という名前で使えるように、
         * Model にデータを追加します。
         *
         * dashboard.html では以下のように表示できます。
         *
         * th:text="${loginEmail}"
         */
        model.addAttribute("loginEmail", loginEmail);

        /**
         * 今後、カテゴリ数・問題数・復習予定数などを表示する予定です。
         *
         * 現時点ではまだ関連Entityを作っていないため、
         * 仮の値として 0 を渡しています。
         *
         * 後で ExamCategory や QuestionCard を作成したら、
         * Serviceを使って実際の件数を取得する形に変更します。
         */
        model.addAttribute("categoryCount", 0);
        model.addAttribute("questionCount", 0);
        model.addAttribute("todayReviewCount", 0);

        /**
         * 表示するHTMLテンプレートを指定します。
         *
         * "dashboard" と書くと、
         * src/main/resources/templates/dashboard.html
         * が表示されます。
         */
        return "dashboard";
    }
}