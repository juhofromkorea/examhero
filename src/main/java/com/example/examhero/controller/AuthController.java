package com.example.examhero.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.example.examhero.dto.SignupForm;
import com.example.examhero.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * AuthController
 *
 * 認証関連の画面を担当するControllerです。
 *
 * 「認証」とは、簡単に言うと
 * ユーザーが誰なのかを確認する仕組みです。
 *
 * このControllerでは、主に以下の画面・処理を担当します。
 *
 * 1. 会員登録画面を表示する
 * 2. 会員登録処理を行う
 * 3. ログイン画面を表示する
 *
 * 実際のログイン処理そのものは Spring Security が担当します。
 * そのため、このControllerではログイン画面を表示するだけです。
 */
@Controller
public class AuthController {

    /**
     * UserService
     *
     * ユーザー登録など、ユーザーに関する実際の処理を担当するServiceです。
     *
     * Controllerは画面からのリクエストを受け取りますが、
     * メールアドレスの重複確認やパスワード暗号化などの詳しい処理は
     * UserService に任せます。
     */
    private final UserService userService;

    /**
     * コンストラクタ
     *
     * Springが AuthController を作成するときに、
     * UserService を自動で渡してくれます。
     *
     * これをコンストラクタインジェクションと呼びます。
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 会員登録画面を表示するメソッド
     *
     * GET /signup にアクセスされたときに実行されます。
     *
     * 例:
     *   ブラウザで http://localhost:8080/signup にアクセス
     *   → このメソッドが呼ばれる
     *   → templates/auth/signup.html が表示される
     *
     * Model:
     *   ControllerからHTMLへデータを渡すための入れ物です。
     *
     * ここでは、signupForm という名前で空の SignupForm を渡しています。
     * Thymeleafのフォーム画面では、この signupForm を使って入力欄を作ります。
     */
    @GetMapping("/signup")
    public String showSignupForm(Model model) {

        /**
         * HTML側で th:object="${signupForm}" として使うために、
         * 空の SignupForm オブジェクトを Model に入れます。
         */
        model.addAttribute("signupForm", new SignupForm());

        /**
         * 表示するHTMLテンプレートの場所を返します。
         *
         * "auth/signup" と書くと、
         * src/main/resources/templates/auth/signup.html
         * を表示します。
         */
        return "auth/signup";
    }

    /**
     * 会員登録処理を行うメソッド
     *
     * POST /signup にフォームが送信されたときに実行されます。
     *
     * 会員登録画面でユーザーが入力した値は、
     * SignupForm オブジェクトに入って渡されます。
     *
     * @Valid:
     *   SignupForm に書いたバリデーションルールを実行します。
     *
     *   例:
     *   - ユーザー名が空ではないか
     *   - メールアドレス形式になっているか
     *   - パスワードが8文字以上か
     *
     * BindingResult:
     *   バリデーションエラーの結果を受け取るためのオブジェクトです。
     *
     * 注意:
     *   @Valid を使う場合、BindingResult は必ず直後の引数に書きます。
     *   順番を間違えるとエラー処理がうまく動かないことがあります。
     */
    @PostMapping("/signup")
    public String signup(
            @Valid SignupForm signupForm,
            BindingResult bindingResult,
            Model model) {

        /**
         * 1. 入力チェックでエラーがあるか確認します。
         *
         * 例えば、メールアドレスが空だったり、
         * パスワードが短すぎたりした場合、
         * bindingResult.hasErrors() は true になります。
         */
        if (bindingResult.hasErrors()) {

            /**
             * 入力エラーがある場合は、会員登録画面をもう一度表示します。
             *
             * このとき、エラーメッセージは BindingResult に入っているため、
             * HTML側で表示できます。
             */
            return "auth/signup";
        }

        try {
            /**
             * 2. UserService に会員登録処理を依頼します。
             *
             * Controller自身は、パスワード暗号化やDB保存を直接行いません。
             * その処理は UserService に任せます。
             */
            userService.register(signupForm);

        } catch (IllegalArgumentException e) {

            /**
             * 3. メールアドレス重複などの業務エラーを処理します。
             *
             * UserService では、既に登録済みのメールアドレスの場合に
             * IllegalArgumentException を投げるようにしています。
             *
             * そのメッセージを errorMessage という名前で HTML に渡します。
             */
            model.addAttribute("errorMessage", e.getMessage());

            /**
             * 入力内容を保持したまま、会員登録画面を再表示します。
             */
            return "auth/signup";
        }

        /**
         * 4. 会員登録に成功した場合
         *
         * ログイン画面へリダイレクトします。
         *
         * redirect:/login:
         *   /login に再アクセスさせるという意味です。
         *
         * registered:
         *   登録完了メッセージをログイン画面で表示するためのパラメータです。
         *
         * 実際のURLは以下のようになります。
         *   /login?registered
         */
        return "redirect:/login?registered";
    }

    /**
     * ログイン画面を表示するメソッド
     *
     * GET /login にアクセスされたときに実行されます。
     *
     * 注意:
     *   ログイン処理自体は、このメソッドでは行いません。
     *   Spring Security が自動で処理します。
     *
     * このメソッドは、ログイン画面のHTMLを表示するだけです。
     */
    @GetMapping("/login")
    public String showLoginForm() {

        /**
         * src/main/resources/templates/auth/login.html
         * を表示します。
         */
        return "auth/login";
    }
}