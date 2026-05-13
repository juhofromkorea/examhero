package com.example.examhero.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig
 *
 * Spring Security の設定をまとめるクラスです。
 *
 * Spring Security は、ログイン・ログアウト・アクセス制御などを担当する仕組みです。
 *
 * この設定クラスでは、主に以下の内容を決めています。
 *
 * 1. どのURLはログインなしで見られるか
 * 2. どのURLはログインが必要か
 * 3. ログイン画面はどのURLを使うか
 * 4. ログアウト後にどこへ移動するか
 * 5. パスワードをどの方式で暗号化するか
 */
@Configuration
public class SecurityConfig {

    /**
     * Spring Security の細かい動作を設定するメソッドです。
     *
     * SecurityFilterChain は、ブラウザからのリクエストに対して、
     * 「このURLはアクセスしてもよいか」
     * 「ログインが必要か」
     * 「ログアウト処理をどうするか」
     * などを判断するための設定です。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            /**
             * URLごとのアクセス権限を設定します。
             *
             * permitAll():
             *   ログインしていなくてもアクセスできるURLです。
             *
             * authenticated():
             *   ログインしているユーザーだけがアクセスできるURLです。
             */
            .authorizeHttpRequests(auth -> auth

                /**
                 * トップページ、会員登録画面、ログイン画面は
                 * ログインしていない人でも見る必要があるため、許可します。
                 */
                .requestMatchers("/", "/signup", "/login").permitAll()

                /**
                 * CSS、JavaScript、画像などの静的ファイルは
                 * ログインしていなくても読み込めるようにします。
                 *
                 * これを許可しないと、ログイン画面やトップページのCSSが
                 * 正しく反映されないことがあります。
                 */
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                /**
                 * H2 Console にアクセスできるようにします。
                 *
                 * H2 Console は、開発中にDBの中身をブラウザで確認するための画面です。
                 */
                .requestMatchers(PathRequest.toH2Console()).permitAll()

                /**
                 * 上記以外のURLは、ログイン済みユーザーだけアクセスできるようにします。
                 *
                 * 例:
                 * - /dashboard
                 * - /categories
                 * - /questions
                 */
                .anyRequest().authenticated()
            )

            /**
             * ログイン設定です。
             *
             * loginPage("/login"):
             *   自作のログイン画面として /login を使う、という意味です。
             *
             * usernameParameter("email"):
             *   ログインフォームの「ログインID」として使う input の name を指定します。
             *
             *   Spring Security は、何も設定しない場合、
             *   ログインIDの入力欄 name="username" を探します。
             *
             *   しかし、このアプリではメールアドレスでログインしたいため、
             *   login.html 側では name="email" を使う予定です。
             *
             *   そのため、ここで
             *   「ログインIDは email という名前の入力欄から受け取ってください」
             *   と Spring Security に教えています。
             *
             * passwordParameter("password"):
             *   パスワード入力欄の name を指定します。
             *
             *   Spring Security のデフォルトも name="password" なので、
             *   これは省略しても動きます。
             *   ただし、チームメンバーが見たときに分かりやすいように明示しています。
             *
             * defaultSuccessUrl("/dashboard", true):
             *   ログイン成功後、/dashboard に移動します。
             *
             * permitAll():
             *   ログイン画面自体は、ログインしていない人でも見られるようにします。
             */
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )

            /**
             * ログアウト設定です。
             *
             * logoutUrl("/logout"):
             *   /logout にアクセスするとログアウト処理を行います。
             *
             * logoutSuccessUrl("/"):
             *   ログアウト成功後、トップページへ移動します。
             *
             * invalidateHttpSession(true):
             *   ログアウト時にセッション情報を削除します。
             *
             * deleteCookies("JSESSIONID"):
             *   ブラウザに保存されているセッションIDのCookieを削除します。
             */
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            /**
             * H2 Console を使うためのCSRF設定です。
             *
             * CSRFは、悪意のある外部サイトから勝手にリクエストを送られることを防ぐ仕組みです。
             *
             * 本来は重要なセキュリティ機能ですが、
             * H2 Console は開発用画面なので、ここでは例外的にCSRFチェックの対象外にします。
             */
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(PathRequest.toH2Console())
            )

            /**
             * H2 Console をブラウザで表示するための設定です。
             *
             * H2 Console は内部的に frame を使うため、
             * この設定をしないと画面が正しく表示されないことがあります。
             */
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    /**
     * パスワードを暗号化するためのBeanです。
     *
     * PasswordEncoder は、ユーザーが入力したパスワードを
     * そのままDBに保存しないために使います。
     *
     * ここでは BCryptPasswordEncoder を使います。
     *
     * 例:
     * 入力されたパスワード:
     *   password123
     *
     * DBに保存される値:
     *   $2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     *
     * BCrypt は、同じパスワードでも毎回違う暗号化結果になるため、
     * セキュリティ面でよく使われる方式です。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}