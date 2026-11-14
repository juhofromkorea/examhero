package com.example.examhero.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.examhero.entity.QuestionAttempt;
import com.example.examhero.entity.QuestionCard;
import com.example.examhero.entity.User;

/**
 * QuestionAttemptRepository
 *
 * question_attempts テーブルへの DB アクセスを担当する Repository です。
 *
 * Repository:
 *   DB に保存する、DB から検索する、といった処理を担当する層です。
 *
 * JpaRepository<QuestionAttempt, Long> を継承することで、
 * Spring Data JPA が基本的な DB 操作を自動で用意してくれます。
 *
 * 例:
 * - save(questionAttempt)
 * - findById(id)
 * - findAll()
 * - delete(questionAttempt)
 *
 * ただし ExamHero では、ユーザーごとの学習記録を扱うため、
 * User を条件にした検索メソッドを用意します。
 */
public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, Long> {

    /**
     * 指定したユーザーの解答履歴を、新しい順に取得します。
     *
     * findByUser:
     *   user を条件に検索します。
     *
     * OrderByAttemptedAtDesc:
     *   attemptedAt の降順、つまり最近解いた順に並べます。
     *
     * @param user ログイン中ユーザー
     * @return 指定ユーザーの解答履歴一覧
     */
    List<QuestionAttempt> findByUserOrderByAttemptedAtDesc(User user);

    /**
     * 指定したユーザーが、指定した問題を解いた履歴を取得します。
     *
     * 今後、問題詳細画面に
     * 「この問題を過去に何回解いたか」
     * 「最後に解いた結果」
     * などを表示したいときに使えます。
     *
     * @param user ログイン中ユーザー
     * @param questionCard 問題カード
     * @return 指定問題の解答履歴一覧
     */
    List<QuestionAttempt> findByUserAndQuestionCardOrderByAttemptedAtDesc(
            User user,
            QuestionCard questionCard
    );

    /**
     * 指定したユーザーが、指定期間内に何問解いたかを数えます。
     *
     * ダッシュボードで「今日解いた問題数」を表示するときに使えます。
     *
     * attemptedAt >= start
     * attemptedAt < end
     *
     * という条件になります。
     *
     * @param user ログイン中ユーザー
     * @param start 集計開始日時
     * @param end 集計終了日時
     * @return 指定期間内の解答回数
     */
    long countByUserAndAttemptedAtGreaterThanEqualAndAttemptedAtLessThan(
            User user,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * 指定したユーザーの全解答回数を数えます。
     *
     * 今後、総解答数を表示したいときに使えます。
     *
     * @param user ログイン中ユーザー
     * @return 解答回数
     */
    long countByUser(User user);

    /**
     * 指定したユーザーの正解数を数えます。
     *
     * correct が true のデータだけを数えます。
     *
     * 今後、正答率を出すときに使えます。
     *
     * @param user ログイン中ユーザー
     * @param correct true を渡すと正解数、false を渡すと不正解数
     * @return 件数
     */
    long countByUserAndCorrect(User user, boolean correct);
}