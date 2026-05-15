package com.example.examhero.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.examhero.entity.QuestionAttempt;
import com.example.examhero.entity.QuestionCard;
import com.example.examhero.entity.User;
import com.example.examhero.repository.QuestionAttemptRepository;

/**
 * QuestionAttemptService
 *
 * 問題を解いた記録に関するアプリケーション処理を担当する Service です。
 *
 * Service:
 *   Controller と Repository の間に置く層です。
 *
 * Controller:
 *   画面から送られたリクエストを受け取る
 *
 * Service:
 *   正誤判定、保存するデータの作成、入力値の簡単な確認などを行う
 *
 * Repository:
 *   DB への保存・検索を担当する
 *
 * 今回この Service で行うこと:
 * - ユーザーが選んだ答えを整形する
 * - 正解かどうかを判定する
 * - QuestionAttempt Entity を作る
 * - Repository を使って DB に保存する
 */
@Service
public class QuestionAttemptService {

    /**
     * 解答履歴用 Repository です。
     *
     * question_attempts テーブルへの保存・検索に使います。
     */
    private final QuestionAttemptRepository questionAttemptRepository;

    /**
     * コンストラクタ
     *
     * Spring が QuestionAttemptRepository を自動で渡してくれます。
     * これをコンストラクタインジェクションと呼びます。
     */
    public QuestionAttemptService(QuestionAttemptRepository questionAttemptRepository) {
        this.questionAttemptRepository = questionAttemptRepository;
    }

    /**
     * 問題の解答履歴を保存します。
     *
     * 処理の流れ:
     *   1. 選択肢を整形する
     *   2. A〜D のどれかか確認する
     *   3. QuestionCard の correctAnswer と比較する
     *   4. QuestionAttempt Entity を作る
     *   5. DB に保存する
     *
     * @Transactional:
     *   このメソッド内の DB 処理を1つのまとまりとして扱います。
     *   途中でエラーが起きた場合、中途半端な保存を防ぎます。
     *
     * @param user ログイン中ユーザー
     * @param questionCard 解いた問題カード
     * @param selectedAnswer ユーザーが選んだ答え
     * @return 保存された解答履歴
     */
    @Transactional
    public QuestionAttempt saveAttempt(
            User user,
            QuestionCard questionCard,
            String selectedAnswer
    ) {
        /*
         * null や空文字は Controller 側でもチェックしますが、
         * Service 側でも念のためチェックします。
         *
         * Controller だけに頼ると、
         * 将来別の Controller からこの Service を呼んだときに
         * 不正な値が入り込む可能性があります。
         */
        if (selectedAnswer == null || selectedAnswer.isBlank()) {
            throw new IllegalArgumentException("選択肢を1つ選んでください。");
        }

        /*
         * 前後の空白を削除し、大文字に統一します。
         *
         * 例:
         *   " a " → "A"
         */
        String normalizedSelectedAnswer = selectedAnswer.trim().toUpperCase();

        /*
         * A, B, C, D 以外の値が送られてきた場合はエラーにします。
         *
         * 通常の画面操作では起きませんが、
         * HTML を改変して不正な値を送信される可能性もあるためです。
         */
        if (!isValidAnswer(normalizedSelectedAnswer)) {
            throw new IllegalArgumentException("選択肢は A, B, C, D のいずれかを選んでください。");
        }

        /*
         * 正解かどうかを判定します。
         *
         * 例:
         *   selectedAnswer = "C"
         *   correctAnswer = "C"
         *   → true
         */
        boolean correct = normalizedSelectedAnswer.equals(questionCard.getCorrectAnswer());

        /*
         * 解答履歴 Entity を作ります。
         *
         * Entity:
         *   DB に保存するデータの形です。
         */
        QuestionAttempt questionAttempt = new QuestionAttempt(
                user,
                questionCard,
                normalizedSelectedAnswer,
                correct
        );

        /*
         * DB に保存します。
         *
         * save() は JpaRepository が用意してくれているメソッドです。
         */
        return questionAttemptRepository.save(questionAttempt);
    }

    /**
     * 今日解いた問題数を取得します。
     *
     * ダッシュボード表示で使えるように、先に用意しておきます。
     *
     * @param user ログイン中ユーザー
     * @return 今日の解答回数
     */
    @Transactional(readOnly = true)
    public long countTodayAttempts(User user) {
        /*
         * 今日の 00:00 を作ります。
         *
         * 例:
         *   2026-05-15 00:00
         */
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        /*
         * 明日の 00:00 を作ります。
         *
         * attemptedAt < startOfTomorrow という条件にすることで、
         * 今日中の解答だけを数えます。
         */
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        return questionAttemptRepository
                .countByUserAndAttemptedAtGreaterThanEqualAndAttemptedAtLessThan(
                        user,
                        startOfToday,
                        startOfTomorrow
                );
    }

    /**
     * 指定ユーザーの全解答回数を取得します。
     *
     * @param user ログイン中ユーザー
     * @return 全解答回数
     */
    @Transactional(readOnly = true)
    public long countAllAttempts(User user) {
        return questionAttemptRepository.countByUser(user);
    }

    /**
     * 指定ユーザーの正解数を取得します。
     *
     * @param user ログイン中ユーザー
     * @return 正解数
     */
    @Transactional(readOnly = true)
    public long countCorrectAttempts(User user) {
        return questionAttemptRepository.countByUserAndCorrect(user, true);
    }

    /**
     * 選択肢が A, B, C, D のどれかか確認します。
     *
     * private:
     *   この Service の中だけで使う補助メソッドです。
     *
     * @param answer 選択肢
     * @return A〜D なら true、それ以外なら false
     */
    private boolean isValidAnswer(String answer) {
        return "A".equals(answer)
                || "B".equals(answer)
                || "C".equals(answer)
                || "D".equals(answer);
    }
}