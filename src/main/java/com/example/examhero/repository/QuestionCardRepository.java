package com.example.examhero.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.examhero.entity.ExamCategory;
import com.example.examhero.entity.QuestionCard;
import com.example.examhero.entity.User;

/**
 * QuestionCardRepository
 *
 * 問題カードに関するDBアクセスを担当するRepositoryです。
 *
 * Repository は、Controller や Service が直接SQLを書かなくても、
 * Entity を保存・検索・削除できるようにするための層です。
 *
 * JpaRepository<QuestionCard, Long> を継承すると、
 * Spring Data JPA が基本的なDB操作メソッドを自動で用意してくれます。
 *
 * 例:
 * - save(questionCard)      : 問題カードを保存する
 * - findById(id)            : IDで問題カードを1件検索する
 * - findAll()               : すべての問題カードを取得する
 * - delete(questionCard)    : 問題カードを削除する
 *
 * ただし、ExamHeroでは「ログイン中ユーザーの問題カードだけ」を扱う必要があります。
 *
 * そのため、findAll() をそのまま使うのではなく、
 * User を条件にした検索メソッドを用意します。
 */
public interface QuestionCardRepository extends JpaRepository<QuestionCard, Long> {

    /**
     * 指定したユーザーが作成した問題カード一覧を、新しい順に取得します。
     *
     * findByUser:
     *   user を条件に検索します。
     *
     * OrderByCreatedAtDesc:
     *   createdAt の降順、つまり新しく作成した問題カードから順に並べます。
     *
     * このメソッドは、問題カード一覧画面で使います。
     *
     * 重要:
     *   findAll() を使うと、他のユーザーが作成した問題カードまで取得してしまいます。
     *   個人学習サービスでは、必ずログイン中ユーザーで絞り込む必要があります。
     *
     * @param user ログイン中ユーザー
     * @return ログイン中ユーザーが作成した問題カード一覧
     */
    List<QuestionCard> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 指定したユーザーが、指定したカテゴリに登録した問題カード一覧を取得します。
     *
     * findByUserAndExamCategory:
     *   user と examCategory の両方を条件に検索します。
     *
     * OrderByCreatedAtDesc:
     *   新しく作成した問題カードから順に並べます。
     *
     * このメソッドは、今後「カテゴリ別の問題一覧」を作るときに使います。
     *
     * 例:
     * - AWS SAAカテゴリの問題だけ表示する
     * - Java Silverカテゴリの問題だけ表示する
     *
     * @param user ログイン中ユーザー
     * @param examCategory 表示したい試験カテゴリ
     * @return 指定ユーザー・指定カテゴリに属する問題カード一覧
     */
    List<QuestionCard> findByUserAndExamCategoryOrderByCreatedAtDesc(User user, ExamCategory examCategory);

    /**
     * 指定したユーザーの問題カードを、IDで1件取得します。
     *
     * findById(id) だけを使うと、他のユーザーの問題カードIDを指定された場合でも
     * 取得できてしまう危険があります。
     *
     * そのため、問題カード詳細画面・編集画面・削除処理では、
     * 「id と user の両方」で検索するこのメソッドを使うと安全です。
     *
     * Optional<QuestionCard>:
     *   検索結果が存在する場合も、存在しない場合もあることを表します。
     *
     * 例:
     * - 見つかった場合: Optional の中に QuestionCard が入る
     * - 見つからない場合: Optional.empty()
     *
     * @param id 問題カードID
     * @param user ログイン中ユーザー
     * @return 指定ユーザーが所有する問題カード
     */
    Optional<QuestionCard> findByIdAndUser(Long id, User user);

    /**
     * 指定したユーザーが、指定したカテゴリに何件の問題カードを登録しているか数えます。
     *
     * countBy...:
     *   条件に一致するデータ件数を long 型で返します。
     *
     * このメソッドは、今後カテゴリ一覧画面で
     * 「このカテゴリには問題が何問あるか」を表示したいときに使えます。
     *
     * 例:
     * - AWS SAA: 30問
     * - Java Silver: 12問
     *
     * 1週目MVPでは必須ではありませんが、
     * 2週目以降の問題カード機能で使いやすいので先に用意しておきます。
     *
     * @param user ログイン中ユーザー
     * @param examCategory 試験カテゴリ
     * @return 指定カテゴリ内の問題カード数
     */
    long countByUserAndExamCategory(User user, ExamCategory examCategory);

    /**
     * 指定したユーザーが作成した問題カードの総数を数えます。
     *
     * このメソッドは、ダッシュボードで
     * 「登録済み問題カード数」を表示するときに使えます。
     *
     * @param user ログイン中ユーザー
     * @return ログイン中ユーザーが作成した問題カード総数
     */
    long countByUser(User user);
}