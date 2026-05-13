package com.example.examhero.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.examhero.dto.SignupForm;
import com.example.examhero.entity.User;
import com.example.examhero.repository.UserRepository;

/**
 * UserService
 *
 * ユーザーに関する処理を担当するServiceクラスです。
 *
 * Service は「アプリケーションの実際の処理」を書く場所です。
 *
 * 例えば、会員登録では以下のような処理が必要です。
 *
 * 1. 入力されたメールアドレスが既に使われていないか確認する
 * 2. パスワードを暗号化する
 * 3. Userオブジェクトを作成する
 * 4. UserRepositoryを使ってDBに保存する
 *
 * Controllerにすべての処理を書くこともできますが、
 * Controllerは「画面からのリクエストを受け取る役割」に集中させ、
 * 実際の処理はServiceに分けることで、コードが整理されます。
 */
@Service
public class UserService {

    /**
     * UserRepository
     *
     * users テーブルにアクセスするためのRepositoryです。
     *
     * このServiceでは、ユーザーを保存したり、
     * メールアドレスの重複確認をしたりするために使います。
     */
    private final UserRepository userRepository;

    /**
     * PasswordEncoder
     *
     * パスワードを暗号化するための部品です。
     *
     * SecurityConfig.java で BCryptPasswordEncoder をBean登録しているため、
     * Springがここに自動で注入してくれます。
     *
     * ユーザーが入力したパスワードをそのままDBに保存するのは危険なので、
     * 必ず passwordEncoder.encode() で暗号化してから保存します。
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * コンストラクタ
     *
     * Springでは、このようにコンストラクタを使って必要な部品を受け取る書き方がよく使われます。
     *
     * これを「コンストラクタインジェクション」と呼びます。
     *
     * Springがアプリ起動時に UserRepository と PasswordEncoder を用意し、
     * UserService を作るときに自動で渡してくれます。
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 会員登録を行うメソッド
     *
     * SignupFormには、会員登録画面で入力された値が入っています。
     *
     * このメソッドでは、その入力値を使ってUserを作成し、DBに保存します。
     *
     * @Transactional:
     *   このメソッド内のDB処理を1つのまとまりとして扱うための設定です。
     *
     *   例えば、途中でエラーが発生した場合、
     *   中途半端な状態でDBに保存されないようにします。
     *
     * 処理の流れ:
     *   1. メールアドレスの重複確認
     *   2. パスワードの暗号化
     *   3. Userオブジェクトの作成
     *   4. DBへ保存
     */
    @Transactional
    public User register(SignupForm form) {

        /**
         * 1. メールアドレスの重複確認
         *
         * 既に同じメールアドレスで登録されたユーザーがいる場合、
         * 新しく登録できないようにします。
         */
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("このメールアドレスは既に登録されています");
        }

        /**
         * 2. パスワードの暗号化
         *
         * form.getPassword() には、ユーザーが画面で入力した生のパスワードが入っています。
         *
         * 例:
         *   password123
         *
         * これをそのままDBに保存するのは危険なので、
         * BCryptを使って暗号化します。
         *
         * 暗号化後の例:
         *   $2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
         */
        String encodedPassword = passwordEncoder.encode(form.getPassword());

        /**
         * 3. Userオブジェクトの作成
         *
         * SignupFormは画面入力用のDTOです。
         * DBに保存するためには、EntityであるUserに変換する必要があります。
         *
         * username:
         *   画面で入力されたユーザー名
         *
         * email:
         *   画面で入力されたメールアドレス
         *
         * password:
         *   暗号化済みパスワード
         */
        User user = new User(
            form.getUsername(),
            form.getEmail(),
            encodedPassword
        );

        /**
         * 4. DBへ保存
         *
         * userRepository.save(user) を呼ぶことで、
         * users テーブルにユーザー情報が保存されます。
         *
         * save() は JpaRepository が最初から用意してくれているメソッドです。
         */
        return userRepository.save(user);
    }

    /**
     * メールアドレスが既に登録されているか確認するメソッド
     *
     * Controller側で、登録前に簡単な確認をしたい場合などに使えます。
     *
     * 戻り値:
     *   true  -> 既に登録されている
     *   false -> まだ登録されていない
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}