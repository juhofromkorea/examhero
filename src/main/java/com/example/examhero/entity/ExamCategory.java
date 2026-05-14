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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * ExamCategory エンティティ
 *
 * このクラスは「試験カテゴリ」を表す Entity です。
 * Entity は DB のテーブルに対応するクラスです。
 *
 * 例:
 * - AWS SAA
 * - 基本情報技術者試験
 * - Java Silver
 *
 * ExamHero では、カテゴリはユーザーごとに管理します。
 * そのため、このカテゴリが「どのユーザーに所属しているか」を
 * User エンティティとの関連で保存します。
 */
@Entity
@Table(
        name = "exam_categories",
        uniqueConstraints = {
                /*
                 * 同じユーザーが、同じ名前のカテゴリを重複して作れないようにします。
                 *
                 * 例:
                 * - user_id = 1 のユーザーが「AWS SAA」を2つ作ることはできません。
                 * - ただし、別のユーザーが「AWS SAA」を作ることはできます。
                 *
                 * これにより「カテゴリ名はユーザーごとに一意」というルールになります。
                 */
                @UniqueConstraint(columnNames = {"user_id", "name"})
        }
)
public class ExamCategory {

    /**
     * カテゴリID
     *
     * @Id は主キーを表します。
     * @GeneratedValue は DB に ID の自動採番を任せる設定です。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * カテゴリ名
     *
     * nullable = false により、DB上でも必須項目になります。
     * length = 50 により、最大50文字まで保存できます。
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * カテゴリ説明
     *
     * 説明は任意入力にしたいため nullable = false は付けていません。
     * 例: 「AWS SAA試験の頻出問題をまとめるカテゴリ」
     */
    @Column(length = 500)
    private String description;

    /**
     * このカテゴリを作成したユーザー
     *
     * @ManyToOne:
     *   多くのカテゴリが、1人のユーザーに所属する関係を表します。
     *
     * FetchType.LAZY:
     *   カテゴリを取得した瞬間には User をすぐ読み込まず、
     *   必要になったタイミングで読み込む設定です。
     *   JPA では関連データの読み込みすぎを防ぐためによく使います。
     *
     * @JoinColumn(name = "user_id"):
     *   exam_categories テーブルに user_id カラムを作り、
     *   users テーブルの id とつなげます。
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 作成日時
     *
     * カテゴリが初めて保存された日時を入れます。
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     *
     * カテゴリ名や説明を変更したときの日時を入れます。
     */
    private LocalDateTime updatedAt;

    /**
     * デフォルトコンストラクタ
     *
     * JPA が DB のデータから Entity オブジェクトを作るために必要です。
     */
    public ExamCategory() {
    }

    /**
     * カテゴリ作成時に使いやすいコンストラクタ
     *
     * Service 層で、フォーム入力値とログイン中ユーザーを使って
     * ExamCategory を作るときに利用できます。
     */
    public ExamCategory(String name, String description, User user) {
        this.name = name;
        this.description = description;
        this.user = user;
    }

    /**
     * 新規保存の直前に実行されます。
     *
     * @PrePersist は、Repository の save() で初めて DB に保存される前に
     * 自動で呼ばれる JPA の仕組みです。
     */
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 更新の直前に実行されます。
     *
     * @PreUpdate は、既存データが更新される前に自動で呼ばれます。
     */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * カテゴリIDを取得します。
     *
     * ID は DB が自動採番するため、基本的には setter を作りません。
     */
    public Long getId() {
        return id;
    }

    /**
     * カテゴリ名を取得します。
     */
    public String getName() {
        return name;
    }

    /**
     * カテゴリ名を設定します。
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * カテゴリ説明を取得します。
     */
    public String getDescription() {
        return description;
    }

    /**
     * カテゴリ説明を設定します。
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * このカテゴリを所有するユーザーを取得します。
     */
    public User getUser() {
        return user;
    }

    /**
     * このカテゴリを所有するユーザーを設定します。
     */
    public void setUser(User user) {
        this.user = user;
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