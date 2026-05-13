package com.example.examhero.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.examhero.entity.User;

/**
 * UserRepository
 *
 * users テーブルに対して、データの検索・保存・削除などを行うためのクラスです。
 *
 * Repository は「DB操作を担当する層」です。
 *
 * 例えば、以下のような処理を担当します。
 * - ユーザーを保存する
 * - メールアドレスでユーザーを検索する
 * - IDでユーザーを検索する
 * - ユーザーを削除する
 *
 * JpaRepository を継承することで、
 * save(), findById(), findAll(), deleteById() などの基本的なDB操作メソッドを
 * 自分で実装しなくても使えるようになります。
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * メールアドレスでユーザーを検索するメソッド
     *
     * Spring Data JPA では、メソッド名を一定のルールで書くと、
     * 自動的にSQLを作って実行してくれます。
     *
     * findByEmail(String email) と書くと、
     * JPA は以下のような意味として解釈します。
     *
     * 「email カラムの値が、引数 email と一致する User を1件探す」
     *
     * SQLで書くと、イメージとしては以下のような処理です。
     *
     * SELECT * FROM users WHERE email = ?
     *
     * Optional<User> を返す理由:
     *   指定されたメールアドレスのユーザーが存在しない可能性があるためです。
     *
     * 例:
     *   存在する場合   -> Optional の中に User が入る
     *   存在しない場合 -> Optional.empty() になる
     */
    Optional<User> findByEmail(String email);

    /**
     * 指定されたメールアドレスが既に登録されているか確認するメソッド
     *
     * 会員登録のときに、メールアドレスの重複チェックで使います。
     *
     * existsByEmail(String email) と書くと、
     * JPA は以下のような意味として解釈します。
     *
     * 「email カラムの値が、引数 email と一致するデータが存在するか確認する」
     *
     * 戻り値:
     *   true  -> 既に同じメールアドレスのユーザーが存在する
     *   false -> まだ登録されていない
     *
     * SQLで書くと、イメージとしては以下のような処理です。
     *
     * SELECT COUNT(*) FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);
}