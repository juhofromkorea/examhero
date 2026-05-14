package com.example.examhero.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.examhero.entity.ExamCategory;
import com.example.examhero.entity.User;

/**
 * ExamCategoryRepository
 *
 * Repository は DB へのアクセスを担当する層です。
 * Controller や Service が直接 SQL を書かなくても、
 * Repository を通して Entity を保存・検索できます。
 *
 * JpaRepository<ExamCategory, Long> を継承すると、
 * Spring Data JPA が以下のような基本メソッドを自動で用意してくれます。
 *
 * 例:
 * - save(category)       : カテゴリを保存する
 * - findById(id)         : IDでカテゴリを検索する
 * - findAll()            : 全カテゴリを取得する
 * - delete(category)     : カテゴリを削除する
 *
 * ただし、ExamHero では「ログイン中ユーザーのカテゴリだけ」を扱う必要があるため、
 * User を条件にした検索メソッドを追加しています。
 */
public interface ExamCategoryRepository extends JpaRepository<ExamCategory, Long> {

    /**
     * 指定したユーザーが作成したカテゴリ一覧を、新しい順に取得します。
     *
     * Spring Data JPA では、メソッド名をルール通りに書くと、
     * 自動で SQL に相当する処理を作ってくれます。
     *
     * findByUser:
     *   user カラムを条件に検索します。
     *
     * OrderByCreatedAtDesc:
     *   createdAt の降順、つまり新しく作ったカテゴリから順に並べます。
     *
     * このメソッドはカテゴリ一覧画面で使います。
     */
    List<ExamCategory> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 指定したユーザーが、同じカテゴリ名をすでに持っているか確認します。
     *
     * existsBy... は「存在するかどうか」を boolean で返します。
     *
     * 例:
     * - true  : すでに同じ名前のカテゴリがある
     * - false : まだ同じ名前のカテゴリはない
     *
     * このメソッドは、カテゴリ作成時の重複チェックで使います。
     */
    boolean existsByUserAndName(User user, String name);

    /**
     * 指定したユーザーのカテゴリを、IDで1件検索します。
     *
     * findById(id) だけを使うと、他のユーザーのカテゴリIDを指定した場合でも
     * 取得できてしまう危険があります。
     *
     * そのため、今後カテゴリ詳細・編集・削除を作るときは、
     * 「id と user の両方」で検索するこのメソッドを使うと安全です。
     */
    Optional<ExamCategory> findByIdAndUser(Long id, User user);
}