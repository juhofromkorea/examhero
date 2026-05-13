package com.example.examhero.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 *
 * 例:
 * users テーブル
 * - id
 * - username
 * - email
 * - password
 * - role
 * - created_at
 * - updated_at
 */
@Entity
@Table(name = "users") // DB上のテーブル名を users に指定します。user は予約語になる場合があるため避けています。
public class User {

    /**
     * ユーザーID
     *
     * 各ユーザーを一意に区別するための番号です。
     *
     * @Id:
     *   このフィールドが主キーであることを表します。
     *
     * @GeneratedValue:
     *   IDを自動採番する設定です。
     *   ユーザー登録時に、DBが 1, 2, 3... のように自動で番号を付けます。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ユーザー名
     *
     * 画面に表示する名前です。
     * 例: 島陽光、山田智弘、キムジュホ
     *
     * nullable = false:
     *   DBに保存するとき、この値は必須です。
     *
     * length = 50:
     *   最大50文字まで保存できます。
     */
    @Column(nullable = false, length = 50)
    private String username;

    /**
     * メールアドレス
     *
     * ログインIDとして使用します。
     *
     * unique = true:
     *   同じメールアドレスを持つユーザーを複数作れないようにします。
     *
     * 例:
     *   juho@example.com で登録済みの場合、
     *   別のユーザーが同じ juho@example.com で登録することはできません。
     */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * パスワード
     *
     * 注意:
     *   実際のサービスでは、入力されたパスワードをそのまま保存してはいけません。
     *   Spring Security を使って BCrypt などで暗号化した文字列を保存します。
     *
     * 例:
     *   入力値: password123
     *   DB保存値: $2a$10$xxxxxxxxxxxxxxxxxxxxxxxx...
     *
     * 暗号化後の文字列は長くなるため、length は 255 にしています。
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * ユーザー権限
     *
     * 一般ユーザーか管理者かを区別するための値です。
     *
     * 初期値は "USER" です。
     *
     * 例:
     * - USER  : 一般ユーザー
     * - ADMIN : 管理者
     *
     * 今後、管理者ページを作るときに使用します。
     */
    @Column(nullable = false, length = 20)
    private String role = "USER";

    /**
     * 作成日時
     *
     * ユーザーが登録された日時を保存します。
     *
     * @PrePersist の onCreate() メソッドで自動的に値を入れます。
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     *
     * ユーザー情報が更新された日時を保存します。
     *
     * まだ一度も更新されていない場合は null のままです。
     *
     * @PreUpdate の onUpdate() メソッドで自動的に値を入れます。
     */
    private LocalDateTime updatedAt;

    /**
     * デフォルトコンストラクタ
     *
     * JPA がエンティティを作成するときに必要です。
     *
     * 例えば、DBから users テーブルのデータを取得したとき、
     * JPA はこのコンストラクタを使って User オブジェクトを作ります。
     *
     * そのため、Entity クラスには基本的に空のコンストラクタが必要です。
     */
    public User() {
    }

    /**
     * ユーザー登録時に使いやすいコンストラクタ
     *
     * 会員登録処理で User オブジェクトを作るときに使用します。
     *
     * 例:
     * User user = new User("Juho", "juho@example.com", encodedPassword);
     *
     * role は基本的に一般ユーザーとして登録するため、
     * ここでは "USER" を設定しています。
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
     *
     * ここでは、ユーザー登録時の日時を createdAt に設定しています。
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
     *
     * ここでは、ユーザー情報の更新日時を updatedAt に設定しています。
     */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * ユーザーIDを取得します。
     *
     * id は DB が自動で作る値なので、setter は基本的に作りません。
     * アプリ側から勝手に id を変更しないようにするためです。
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
     *   主に Spring Security の認証処理などで使います。
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
     *
     * 例:
     * USER, ADMIN
     */
    public String getRole() {
        return role;
    }

    /**
     * 権限を設定します。
     *
     * 通常の会員登録では USER のままで問題ありません。
     * 管理者ユーザーを作る場合に ADMIN を設定します。
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
}