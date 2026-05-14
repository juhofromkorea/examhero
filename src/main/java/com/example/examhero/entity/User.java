package com.example.examhero.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * User エンティティ
 *
 * このクラスは「ユーザー情報」を表すクラスです。
 *
 * Spring Data JPA では、@Entity を付けたクラスは
 * データベースのテーブルと対応します。
 *
 * この User クラスは、データベース上では users テーブルとして作成されます。
 */
@Entity
@Table(name = "users") // DB上のテーブル名を users に指定します。user は予約語になる場合があるため避けています。
public class User {

    /**
     * ユーザーID
     *
     * @Id:
     *   このフィールドが主キーであることを表します。
     *
     * @GeneratedValue:
     *   IDを自動採番する設定です。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ユーザー名
     *
     * 画面に表示する名前です。
     */
    @Column(nullable = false, length = 50)
    private String username;

    /**
     * メールアドレス
     *
     * ログインIDとして使用します。
     * unique = true により、同じメールアドレスで複数登録できないようにします。
     */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * パスワード
     *
     * 注意:
     *   実際のサービスでは、入力されたパスワードをそのまま保存してはいけません。
     *   UserService 側で BCrypt によって暗号化した文字列を保存します。
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * ユーザー権限
     *
     * 例:
     * - USER  : 一般ユーザー
     * - ADMIN : 管理者
     */
    @Column(nullable = false, length = 20)
    private String role = "USER";

    /**
     * 作成日時
     *
     * ユーザーが登録された日時を保存します。
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     *
     * ユーザー情報が更新された日時を保存します。
     */
    private LocalDateTime updatedAt;

    /**
     * ユーザーが作成した試験カテゴリ一覧
     *
     * @OneToMany:
     *   1人のユーザーが、複数の試験カテゴリを持つ関係を表します。
     *
     * mappedBy = "user":
     *   ExamCategory クラス側にある user フィールドが、
     *   この関連の主役であることを表します。
     *
     * JPA の関連では、外部キーを持っている側が関係の主役になります。
     * 今回は exam_categories テーブルが user_id を持つため、
     * ExamCategory 側が主役です。
     *
     * ここでは cascade や orphanRemoval はまだ付けていません。
     * 初期MVPでは「ユーザー削除時にカテゴリも自動削除する」処理を
     * まだ作らないため、まずは安全でシンプルな関連だけにしています。
     */
    @OneToMany(mappedBy = "user")
    private List<ExamCategory> examCategories = new ArrayList<>();

    /**
     * デフォルトコンストラクタ
     *
     * JPA がエンティティを作成するときに必要です。
     */
    public User() {
    }

    /**
     * ユーザー登録時に使いやすいコンストラクタ
     *
     * 会員登録処理で User オブジェクトを作るときに使用します。
     */
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = "USER";
    }

    /**
     * DBに初めて保存される直前に実行されるメソッド
     *
     * @PrePersist:
     *   Entity が新規保存される前に自動実行されます。
     */
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * DBのデータが更新される直前に実行されるメソッド
     *
     * @PreUpdate:
     *   Entity が更新される前に自動実行されます。
     */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * ユーザーIDを取得します。
     *
     * id は DB が自動で作る値なので、setter は基本的に作りません。
     */
    public Long getId() {
        return id;
    }

    /**
     * ユーザー名を取得します。
     */
    public String getUsername() {
        return username;
    }

    /**
     * ユーザー名を設定します。
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * メールアドレスを取得します。
     */
    public String getEmail() {
        return email;
    }

    /**
     * メールアドレスを設定します。
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * パスワードを取得します。
     *
     * 注意:
     *   実際の画面でパスワードをそのまま表示することは基本的にありません。
     */
    public String getPassword() {
        return password;
    }

    /**
     * パスワードを設定します。
     *
     * 注意:
     *   ここに渡す値は、できれば暗号化済みのパスワードにします。
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 権限を取得します。
     */
    public String getRole() {
        return role;
    }

    /**
     * 権限を設定します。
     */
    public void setRole(String role) {
        this.role = role;
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

    /**
     * このユーザーが作成した試験カテゴリ一覧を取得します。
     *
     * 一覧画面では基本的に ExamCategoryRepository の
     * findByUserOrderByCreatedAtDesc(user) を使う方が分かりやすいです。
     */
    public List<ExamCategory> getExamCategories() {
        return examCategories;
    }

    /**
     * 試験カテゴリ一覧を設定します。
     *
     * JPA が DB から User と関連カテゴリを復元するときに使う可能性があるため、
     * setter も用意しています。
     */
    public void setExamCategories(List<ExamCategory> examCategories) {
        this.examCategories = examCategories;
    }
}