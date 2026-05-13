package com.example.examhero.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * SignupForm
 *
 * 会員登録画面で入力された値を受け取るためのDTOクラスです。
 *
 * DTOとは Data Transfer Object の略で、
 * 画面とControllerの間でデータを受け渡しするための入れ物です。
 *
 * 例えば、会員登録画面では以下のような入力欄があります。
 *
 * - ユーザー名
 * - メールアドレス
 * - パスワード
 *
 * ユーザーが画面に入力して送信すると、
 * その値がこの SignupForm オブジェクトに入って Controller に渡されます。
 *
 * Entity の User クラスを直接フォーム入力に使うこともできますが、
 * 画面入力用のクラスとDB保存用のクラスを分けたほうが安全で管理しやすいです。
 */
public class SignupForm {

    /**
     * ユーザー名
     *
     * 会員登録画面で入力されたユーザー名を受け取ります。
     *
     * @NotBlank:
     *   空文字やスペースだけの入力を許可しない設定です。
     *
     * 例:
     *   OK  -> "Juho"
     *   OK  -> "田中太郎"
     *   NG  -> ""
     *   NG  -> "   "
     *
     * message:
     *   入力エラーがあったときに画面へ表示するメッセージです。
     */
    @NotBlank(message = "ユーザー名を入力してください")
    @Size(max = 50, message = "ユーザー名は50文字以内で入力してください")
    private String username;

    /**
     * メールアドレス
     *
     * ログインIDとして使うメールアドレスを受け取ります。
     *
     * @NotBlank:
     *   未入力を防ぎます。
     *
     * @Email:
     *   メールアドレスの形式になっているか確認します。
     *
     * 例:
     *   OK -> juho@example.com
     *   NG -> juho
     *   NG -> test@
     */
    @NotBlank(message = "メールアドレスを入力してください")
    @Email(message = "メールアドレスの形式で入力してください")
    @Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    private String email;

    /**
     * パスワード
     *
     * 会員登録時に入力されたパスワードを受け取ります。
     *
     * @NotBlank:
     *   未入力を防ぎます。
     *
     * @Size:
     *   パスワードの長さを制限します。
     *
     * ここでは最低8文字、最大100文字にしています。
     *
     * 注意:
     *   この SignupForm に入っている password は、ユーザーが入力した生のパスワードです。
     *   DBに保存するときは、この値をそのまま保存せず、
     *   UserService 側で BCrypt などを使って暗号化してから保存します。
     */
    @NotBlank(message = "パスワードを入力してください")
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください")
    private String password;

    /**
     * デフォルトコンストラクタ
     *
     * Spring MVC がフォームの値を SignupForm に入れるときに必要です。
     *
     * 画面から送られてきた値をもとに、
     * Spring が内部的に SignupForm オブジェクトを作ります。
     */
    public SignupForm() {
    }

    /**
     * ユーザー名を取得します。
     */
    public String getUsername() {
        return username;
    }

    /**
     * ユーザー名を設定します。
     *
     * Spring MVC が画面から送られてきた username の値を
     * このメソッドを通じてセットします。
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
     *
     * Spring MVC が画面から送られてきた email の値を
     * このメソッドを通じてセットします。
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * パスワードを取得します。
     */
    public String getPassword() {
        return password;
    }

    /**
     * パスワードを設定します。
     *
     * Spring MVC が画面から送られてきた password の値を
     * このメソッドを通じてセットします。
     */
    public void setPassword(String password) {
        this.password = password;
    }
}