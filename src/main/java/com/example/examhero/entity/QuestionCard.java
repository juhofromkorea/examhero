package com.example.examhero.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * QuestionCard エンティティ
 *
 * このクラスは「問題カード」を表す Entity です。
 * Entity は DB のテーブルに対応するクラスです。
 *
 * ExamHero では、ユーザーが資格試験の問題を1問ずつ登録し、
 * 後から一覧表示・詳細確認・復習に使えるようにします。
 *
 * 例:
 * - 問題文
 * - 選択肢A〜D
 * - 正解
 * - 解説
 *
 * 1週目MVPでは、まず「問題カードの基本構造」を作ります。
 * 実際の登録画面や一覧表示は、この Entity をもとに次のステップで作ります。
 */
@Entity
@Table(name = "question_cards")
public class QuestionCard {

    /**
     * 問題カードID
     *
     * @Id:
     *   このフィールドが主キーであることを表します。
     *
     * @GeneratedValue:
     *   ID の採番を DB に任せる設定です。
     *
     * GenerationType.IDENTITY:
     *   H2 や MySQL などでよく使われる自動採番方式です。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 問題文
     *
     * @Lob:
     *   長い文章を保存したい場合に使います。
     *   問題文は長くなる可能性があるため付けています。
     *
     * nullable = false:
     *   DB上でも必須項目にします。
     *
     * 例:
     *   「Amazon S3 のストレージクラスに関する説明として正しいものはどれか。」
     */
    @Lob
    @Column(nullable = false)
    private String questionText;

    /**
     * 選択肢A
     *
     * 1週目〜2週目MVPでは、まず4択問題だけを扱います。
     */
    @Column(nullable = false, length = 500)
    private String optionA;

    /**
     * 選択肢B
     */
    @Column(nullable = false, length = 500)
    private String optionB;

    /**
     * 選択肢C
     */
    @Column(nullable = false, length = 500)
    private String optionC;

    /**
     * 選択肢D
     */
    @Column(nullable = false, length = 500)
    private String optionD;

    /**
     * 正解
     *
     * 今回はシンプルに "A", "B", "C", "D" の文字列で保存します。
     *
     * 本格的には enum を使う方法もありますが、
     * 初心者が理解しやすいように、まずは String にしています。
     *
     * 入力チェックは、後で QuestionCardForm 側で行います。
     */
    @Column(nullable = false, length = 1)
    private String correctAnswer;

    /**
     * 解説
     *
     * 解説は任意入力です。
     * 長くなる可能性があるため @Lob を付けています。
     *
     * 例:
     *   「S3 Standard-IA はアクセス頻度が低いデータ向けのストレージクラスです。」
     */
    @Lob
    private String explanation;

    /**
     * この問題カードを作成したユーザー
     *
     * @ManyToOne:
     *   多くの問題カードが、1人のユーザーに所属する関係です。
     *
     * 例:
     *   User 1人
     *     ├─ QuestionCard 1
     *     ├─ QuestionCard 2
     *     └─ QuestionCard 3
     *
     * FetchType.LAZY:
     *   QuestionCard を取得した瞬間には User をすぐ読み込まず、
     *   必要になったときに読み込む設定です。
     *
     * @JoinColumn(name = "user_id"):
     *   question_cards テーブルに user_id カラムを作り、
     *   users テーブルとつなげます。
     *
     * 重要:
     *   ExamHero では問題カードはユーザーごとの個人データです。
     *   そのため、必ず User と関連付けます。
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * この問題カードが所属する試験カテゴリ
     *
     * @ManyToOne:
     *   多くの問題カードが、1つの試験カテゴリに所属する関係です。
     *
     * 例:
     *   AWS SAA カテゴリ
     *     ├─ QuestionCard 1
     *     ├─ QuestionCard 2
     *     └─ QuestionCard 3
     *
     * @JoinColumn(name = "exam_category_id"):
     *   question_cards テーブルに exam_category_id カラムを作り、
     *   exam_categories テーブルとつなげます。
     *
     * 重要:
     *   問題カードは、どの試験カテゴリの問題なのか分かる必要があります。
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_category_id", nullable = false)
    private ExamCategory examCategory;

    /**
     * 作成日時
     *
     * 問題カードが初めて保存された日時を入れます。
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     *
     * 問題文や選択肢を編集したときの日時を入れます。
     */
    private LocalDateTime updatedAt;

    /**
     * デフォルトコンストラクタ
     *
     * JPA が DB のデータから Entity オブジェクトを作るために必要です。
     * そのため、引数なしコンストラクタは必ず用意しておきます。
     */
    public QuestionCard() {
    }

    /**
     * 問題カード作成時に使いやすいコンストラクタ
     *
     * Service 層で、フォーム入力値を使って QuestionCard を作るときに利用できます。
     */
    public QuestionCard(
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctAnswer,
            String explanation,
            User user,
            ExamCategory examCategory
    ) {
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.user = user;
        this.examCategory = examCategory;
    }

    /**
     * 新規保存の直前に実行されます。
     *
     * @PrePersist:
     *   Repository の save() で初めて DB に保存される前に自動で呼ばれます。
     */
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 更新の直前に実行されます。
     *
     * @PreUpdate:
     *   既存データが更新される前に自動で呼ばれます。
     */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 問題カードIDを取得します。
     *
     * ID は DB が自動採番するため、基本的には setter を作りません。
     */
    public Long getId() {
        return id;
    }

    /**
     * 問題文を取得します。
     */
    public String getQuestionText() {
        return questionText;
    }

    /**
     * 問題文を設定します。
     */
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    /**
     * 選択肢Aを取得します。
     */
    public String getOptionA() {
        return optionA;
    }

    /**
     * 選択肢Aを設定します。
     */
    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    /**
     * 選択肢Bを取得します。
     */
    public String getOptionB() {
        return optionB;
    }

    /**
     * 選択肢Bを設定します。
     */
    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    /**
     * 選択肢Cを取得します。
     */
    public String getOptionC() {
        return optionC;
    }

    /**
     * 選択肢Cを設定します。
     */
    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    /**
     * 選択肢Dを取得します。
     */
    public String getOptionD() {
        return optionD;
    }

    /**
     * 選択肢Dを設定します。
     */
    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    /**
     * 正解を取得します。
     */
    public String getCorrectAnswer() {
        return correctAnswer;
    }

    /**
     * 正解を設定します。
     */
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    /**
     * 解説を取得します。
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * 解説を設定します。
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /**
     * この問題カードを作成したユーザーを取得します。
     */
    public User getUser() {
        return user;
    }

    /**
     * この問題カードを作成したユーザーを設定します。
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * この問題カードが所属する試験カテゴリを取得します。
     */
    public ExamCategory getExamCategory() {
        return examCategory;
    }

    /**
     * この問題カードが所属する試験カテゴリを設定します。
     */
    public void setExamCategory(ExamCategory examCategory) {
        this.examCategory = examCategory;
    }

    /**
     * 作成日時を取得します。
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 更新日時を取得します。
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}