package com.example.examhero.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * QuestionAttempt エンティティ
 *
 * このクラスは「ユーザーが問題を解いた記録」を表す Entity です。
 *
 * Entity:
 *   DB のテーブルに対応するクラスです。
 *
 * この Entity は DB 上では question_attempts テーブルになります。
 *
 * 例:
 * - どのユーザーが解いたか
 * - どの問題を解いたか
 * - どの選択肢を選んだか
 * - 正解だったか
 * - いつ解いたか
 *
 * 今後、正答率・今日解いた問題数・復習履歴などを作るときに使います。
 */
@Entity
@Table(name = "question_attempts")
public class QuestionAttempt {

    /**
     * 풀이 기록 ID
     *
     * @Id:
     *   主キーであることを表します。
     *
     * @GeneratedValue:
     *   ID の採番を DB に任せます。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * この解答記録を作ったユーザーです。
     *
     * @ManyToOne:
     *   1人のユーザーは複数回問題を解くことができます。
     *
     * 例:
     *   User 1人
     *     ├─ Attempt 1
     *     ├─ Attempt 2
     *     └─ Attempt 3
     *
     * FetchType.LAZY:
     *   必要になるまで User 情報を読み込まない設定です。
     *
     * @JoinColumn(name = "user_id"):
     *   question_attempts テーブルに user_id カラムを作り、
     *   users テーブルと関連付けます。
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 解いた問題カードです。
     *
     * @ManyToOne:
     *   1つの問題カードは、何度も解かれる可能性があります。
     *
     * 例:
     *   QuestionCard 1問
     *     ├─ Attempt 1回目
     *     ├─ Attempt 2回目
     *     └─ Attempt 3回目
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_card_id", nullable = false)
    private QuestionCard questionCard;

    /**
     * ユーザーが選んだ答えです。
     *
     * 今回は4択問題だけを扱うため、
     * "A", "B", "C", "D" のいずれかを保存します。
     */
    @Column(nullable = false, length = 1)
    private String selectedAnswer;

    /**
     * 正解だったかどうかです。
     *
     * true:
     *   正解
     *
     * false:
     *   不正解
     *
     * カラム名は is_correct にしています。
     */
    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    /**
     * 問題を解いた日時です。
     *
     * Repository の save() で初めて保存される直前に、
     * @PrePersist のメソッドで自動設定します。
     */
    @Column(nullable = false)
    private LocalDateTime attemptedAt;

    /**
     * デフォルトコンストラクタ
     *
     * JPA が Entity を作るために必要です。
     */
    public QuestionAttempt() {
    }

    /**
     * 풀이 기록을 만들 때 사용하는 생성자입니다.
     *
     * Service에서 QuestionAttempt를 만들 때 사용합니다.
     */
    public QuestionAttempt(
            User user,
            QuestionCard questionCard,
            String selectedAnswer,
            boolean correct
    ) {
        this.user = user;
        this.questionCard = questionCard;
        this.selectedAnswer = selectedAnswer;
        this.correct = correct;
    }

    /**
     * DB に初めて保存される直前に自動実行されます。
     *
     * @PrePersist:
     *   Entity が新規保存される前に呼ばれる JPA の機能です。
     *
     * ここで attemptedAt を設定することで、
     * Controller や Service 側で毎回 LocalDateTime.now() を書かなくて済みます。
     */
    @PrePersist
    public void onCreate() {
        this.attemptedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public QuestionCard getQuestionCard() {
        return questionCard;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }
}