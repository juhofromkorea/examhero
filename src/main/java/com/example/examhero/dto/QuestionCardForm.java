package com.example.examhero.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * QuestionCardForm
 *
 * このクラスは、問題カード登録画面の入力値を受け取るためのDTOです。
 *
 * DTO は「画面から送られてきたデータを一時的に受け取るためのクラス」です。
 * DBテーブルそのものを表す Entity とは役割が違います。
 *
 * Entity:
 *   DBに保存するデータ構造を表すクラス
 *
 * DTO / Form:
 *   画面入力を受け取るためのクラス
 *
 * 今回の流れ:
 *
 * questions/form.html
 *   ↓ ユーザーが問題文・選択肢・正解などを入力する
 *
 * QuestionController
 *   ↓ QuestionCardForm として入力値を受け取る
 *
 * QuestionCardService
 *   ↓ QuestionCardForm を QuestionCard Entity に変換する
 *
 * QuestionCardRepository
 *   ↓ DBに保存する
 *
 * 初期MVPでは、まず4択問題だけを扱います。
 * そのため、選択肢は A〜D の4つ、正解は A/B/C/D のどれか1つにします。
 */
public class QuestionCardForm {

    /**
     * 試験カテゴリID
     *
     * 問題カードは、必ずどれか1つの試験カテゴリに所属します。
     *
     * 例:
     * - AWS SAA
     * - Java Silver
     * - 基本情報技術者試験
     *
     * 画面上ではカテゴリ名を選ばせますが、
     * サーバーにはカテゴリのIDを送ります。
     *
     * @NotNull:
     *   カテゴリが選択されていない状態を許可しません。
     */
    @NotNull(message = "カテゴリを選択してください")
    private Long examCategoryId;

    /**
     * 問題文
     *
     * @NotBlank:
     *   null、空文字、スペースだけの入力を許可しません。
     *
     * @Size(max = 3000):
     *   問題文が長くなりすぎないように、最大文字数を制限します。
     *
     * Entity 側では @Lob を使っていますが、
     * Form 側ではユーザー入力の上限を決めておくと安全です。
     */
    @NotBlank(message = "問題文を入力してください")
    @Size(max = 3000, message = "問題文は3000文字以内で入力してください")
    private String questionText;

    /**
     * 選択肢A
     *
     * 初期MVPでは4択問題のみを扱うため、A〜Dはすべて必須にします。
     */
    @NotBlank(message = "選択肢Aを入力してください")
    @Size(max = 500, message = "選択肢Aは500文字以内で入力してください")
    private String optionA;

    /**
     * 選択肢B
     */
    @NotBlank(message = "選択肢Bを入力してください")
    @Size(max = 500, message = "選択肢Bは500文字以内で入力してください")
    private String optionB;

    /**
     * 選択肢C
     */
    @NotBlank(message = "選択肢Cを入力してください")
    @Size(max = 500, message = "選択肢Cは500文字以内で入力してください")
    private String optionC;

    /**
     * 選択肢D
     */
    @NotBlank(message = "選択肢Dを入力してください")
    @Size(max = 500, message = "選択肢Dは500文字以内で入力してください")
    private String optionD;

    /**
     * 正解
     *
     * 初期MVPでは、正解は A / B / C / D のどれか1つだけにします。
     *
     * @NotBlank:
     *   正解が未選択の状態を許可しません。
     *
     * @Pattern(regexp = "A|B|C|D"):
     *   A、B、C、D 以外の値を許可しません。
     *
     * 画面側ではラジオボタンやセレクトボックスで選ばせる予定ですが、
     * 画面だけに頼るのは危険です。
     * サーバー側でも必ずチェックします。
     */
    @NotBlank(message = "正解を選択してください")
    @Pattern(regexp = "A|B|C|D", message = "正解はA、B、C、Dのいずれかを選択してください")
    private String correctAnswer;

    /**
     * 解説
     *
     * 解説は任意入力です。
     * そのため @NotBlank は付けません。
     *
     * ただし、長くなりすぎないように最大文字数だけ制限します。
     */
    @Size(max = 3000, message = "解説は3000文字以内で入力してください")
    private String explanation;

    /**
     * デフォルトコンストラクタ
     *
     * Spring MVC がフォーム入力値をこのクラスに入れるために必要です。
     */
    public QuestionCardForm() {
    }

    /**
     * 試験カテゴリIDを取得します。
     */
    public Long getExamCategoryId() {
        return examCategoryId;
    }

    /**
     * 試験カテゴリIDを設定します。
     *
     * 画面のカテゴリ選択欄から送られてきた値がここに入ります。
     */
    public void setExamCategoryId(Long examCategoryId) {
        this.examCategoryId = examCategoryId;
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
     *
     * 値は A / B / C / D のいずれかです。
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
}