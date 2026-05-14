package com.example.examhero.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ExamCategoryForm
 *
 * このクラスは、試験カテゴリ作成画面の入力値を受け取るためのDTOです。
 *
 * DTO は「画面から送られてくるデータを一時的に受け取るためのクラス」です。
 *
 * Entity である ExamCategory を直接フォーム入力に使うこともできますが、
 * 初心者向けの開発では、以下の理由から Form DTO を分ける方が安全です。
 *
 * 1. 画面で入力させたい項目だけを明確にできる
 * 2. DBの構造と画面入力を分けて考えられる
 * 3. バリデーションをフォーム専用に書ける
 *
 * 今回の流れ:
 *   categories/form.html
 *     ↓ 入力値をPOST送信
 *   ExamCategoryController
 *     ↓ ExamCategoryForm として受け取る
 *   ExamCategoryService
 *     ↓ ExamCategory Entity に変換する
 *   ExamCategoryRepository
 *     ↓ DBに保存する
 */
public class ExamCategoryForm {

    /**
     * 試験カテゴリ名
     *
     * @NotBlank:
     *   null、空文字、スペースだけの入力を許可しません。
     *
     * @Size(max = 50):
     *   最大50文字までに制限します。
     *
     * 例:
     * - AWS SAA
     * - 基本情報技術者試験
     * - Java Silver
     */
    @NotBlank(message = "カテゴリ名を入力してください")
    @Size(max = 50, message = "カテゴリ名は50文字以内で入力してください")
    private String name;

    /**
     * 試験カテゴリの説明
     *
     * 説明は任意入力なので @NotBlank は付けません。
     *
     * @Size(max = 500):
     *   入力する場合は最大500文字までに制限します。
     */
    @Size(max = 500, message = "説明は500文字以内で入力してください")
    private String description;

    /**
     * デフォルトコンストラクタ
     *
     * Spring MVC がフォーム入力値をこのクラスに入れるために必要です。
     */
    public ExamCategoryForm() {
    }

    /**
     * カテゴリ名を取得します。
     */
    public String getName() {
        return name;
    }

    /**
     * カテゴリ名を設定します。
     *
     * Spring MVC はフォーム送信時に、
     * name という入力項目の値をこの setter によって入れます。
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 説明を取得します。
     */
    public String getDescription() {
        return description;
    }

    /**
     * 説明を設定します。
     */
    public void setDescription(String description) {
        this.description = description;
    }
}