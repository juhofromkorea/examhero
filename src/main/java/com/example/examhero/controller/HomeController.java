package com.example.examhero.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HomeController
 *
 * アプリケーションのトップページを担当するControllerです。
 *
 * トップページとは、ユーザーが最初にアクセスする画面です。
 *
 * 例:
 *   http://localhost:8080/
 *
 * この画面では、ExamHero がどのようなサービスなのかを簡単に紹介し、
 * 会員登録画面やログイン画面へのリンクを表示します。
 *
 * トップページはまだログインしていないユーザーも見る必要があるため、
 * SecurityConfig.java で "/" は permitAll() に設定しています。
 */
@Controller
public class HomeController {

    /**
     * トップページを表示するメソッドです。
     *
     * GET / にアクセスされたときに実行されます。
     *
     * 例:
     *   ブラウザで http://localhost:8080/ にアクセス
     *   → このメソッドが呼ばれる
     *   → templates/index.html が表示される
     *
     * @return 表示するHTMLテンプレート名
     */
    @GetMapping("/")
    public String showHome() {

        /**
         * "index" と書くと、
         * src/main/resources/templates/index.html
         * が表示されます。
         *
         * Spring Boot + Thymeleaf では、
         * templates フォルダの下にあるHTMLファイルを
         * return の文字列で指定します。
         */
        return "index";
    }
}