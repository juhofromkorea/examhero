package com.example.examhero.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.examhero.entity.User;
import com.example.examhero.repository.UserRepository;

/**
 * CustomUserDetailsService
 *
 * Spring Security がログイン処理を行うときに、
 * DBからユーザー情報を取得するためのServiceクラスです。
 *
 * Spring Security は、ログイン時に以下の流れで処理を行います。
 *
 * 1. ログイン画面で入力されたIDを受け取る
 * 2. UserDetailsService の loadUserByUsername() を呼び出す
 * 3. DBからユーザー情報を取得する
 * 4. 入力されたパスワードとDB上の暗号化済みパスワードを比較する
 * 5. 一致すればログイン成功
 *
 * 今回のアプリでは、ログインIDとしてメールアドレスを使います。
 * そのため、このクラスではメールアドレスを使って users テーブルからユーザーを検索します。
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * UserRepository
     *
     * users テーブルからユーザー情報を検索するために使います。
     *
     * ログイン時には、入力されたメールアドレスを使って
     * UserRepository の findByEmail() を呼び出します。
     */
    private final UserRepository userRepository;

    /**
     * コンストラクタ
     *
     * Spring が CustomUserDetailsService を作成するときに、
     * UserRepository を自動で渡してくれます。
     *
     * これをコンストラクタインジェクションと呼びます。
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Spring Security がログイン時に自動で呼び出すメソッドです。
     *
     * メソッド名は loadUserByUsername ですが、
     * 今回のアプリでは username ではなく email をログインIDとして使います。
     *
     * そのため、引数 username には実際にはメールアドレスが入ってくる想定です。
     *
     * 例:
     *   username = "juho@example.com"
     *
     * @param username ログイン画面で入力されたログインID
     *                 今回はメールアドレスとして扱います。
     *
     * @return Spring Security が扱える UserDetails オブジェクト
     *
     * @throws UsernameNotFoundException
     *         入力されたメールアドレスのユーザーがDBに存在しない場合に発生させます。
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        /**
         * 1. メールアドレスでユーザーを検索します。
         *
         * UserRepository の findByEmail() は Optional<User> を返します。
         *
         * ユーザーが存在する場合:
         *   Optional の中に User が入っています。
         *
         * ユーザーが存在しない場合:
         *   Optional.empty() になります。
         */
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));

        /**
         * 2. ユーザーの権限を作成します。
         *
         * Spring Security では、権限名に "ROLE_" を付けるのが一般的です。
         *
         * DB上の role が "USER" の場合:
         *   Spring Security上では "ROLE_USER" として扱います。
         *
         * DB上の role が "ADMIN" の場合:
         *   Spring Security上では "ROLE_ADMIN" として扱います。
         */
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole());

        /**
         * 3. Spring Security 用の UserDetails オブジェクトを作成して返します。
         *
         * 注意:
         *   ここで返している org.springframework.security.core.userdetails.User は、
         *   私たちが作った com.example.examhero.entity.User とは別のクラスです。
         *
         * Spring Security は、この UserDetails を使って以下を判断します。
         *
         * - ログインID
         * - 暗号化済みパスワード
         * - 権限
         *
         * 第一引数:
         *   ログインIDです。
         *   今回はメールアドレスを使います。
         *
         * 第二引数:
         *   DBに保存されている暗号化済みパスワードです。
         *
         * 第三引数:
         *   ユーザーの権限リストです。
         *
         * Collections.singletonList(authority):
         *   権限が1つだけのリストを作ります。
         */
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}