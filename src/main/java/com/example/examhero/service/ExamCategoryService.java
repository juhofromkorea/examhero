package com.example.examhero.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.examhero.dto.ExamCategoryForm;
import com.example.examhero.entity.ExamCategory;
import com.example.examhero.entity.User;
import com.example.examhero.repository.ExamCategoryRepository;
import com.example.examhero.repository.UserRepository;

/**
 * ExamCategoryService
 *
 * 試験カテゴリに関するアプリケーション処理を担当するServiceクラスです。
 *
 * Service は、Controller と Repository の間に置く層です。
 *
 * Controller:
 *   画面からのリクエストを受け取る
 *
 * Service:
 *   実際の処理、ルール、判断を書く
 *
 * Repository:
 *   DBへの保存・検索を担当する
 *
 * 今回このServiceで担当する処理:
 * - ログイン中ユーザーをメールアドレスから取得する
 * - ログイン中ユーザーのカテゴリ一覧を取得する
 * - 新しいカテゴリを作成する
 * - 同じユーザー内でカテゴリ名が重複しないように確認する
 */
@Service
public class ExamCategoryService {

    /**
     * 試験カテゴリ用Repository
     *
     * exam_categories テーブルへのDB操作に使います。
     */
    private final ExamCategoryRepository examCategoryRepository;

    /**
     * ユーザー用Repository
     *
     * Spring Security から取得できるログインIDはメールアドレスなので、
     * そのメールアドレスから User Entity を取得するために使います。
     */
    private final UserRepository userRepository;

    /**
     * コンストラクタ
     *
     * Spring が ExamCategoryRepository と UserRepository を自動で渡してくれます。
     * これをコンストラクタインジェクションと呼びます。
     */
    public ExamCategoryService(
            ExamCategoryRepository examCategoryRepository,
            UserRepository userRepository
    ) {
        this.examCategoryRepository = examCategoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * メールアドレスからログイン中ユーザーを取得します。
     *
     * Spring Security の UserDetails から取得できる username は、
     * このアプリでは「メールアドレス」として扱っています。
     *
     * そのため、Controller では userDetails.getUsername() を呼び、
     * その値をこのメソッドに渡します。
     *
     * @param email ログイン中ユーザーのメールアドレス
     * @return User Entity
     */
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ログイン中のユーザーが見つかりません"));
    }

    /**
     * ログイン中ユーザーの試験カテゴリ一覧を取得します。
     *
     * 重要:
     *   findAll() は使いません。
     *
     * 理由:
     *   findAll() を使うと、他のユーザーが作ったカテゴリまで取得してしまいます。
     *   ExamHero ではカテゴリはユーザーごとの個人データなので、
     *   必ず User を条件にして検索します。
     *
     * @param user ログイン中ユーザー
     * @return ログイン中ユーザーが作成したカテゴリ一覧
     */
    @Transactional(readOnly = true)
    public List<ExamCategory> findCategoriesByUser(User user) {
        return examCategoryRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 新しい試験カテゴリを作成します。
     *
     * 処理の流れ:
     *   1. カテゴリ名の前後の空白を削除する
     *   2. 同じユーザー内でカテゴリ名が重複していないか確認する
     *   3. Form DTO から Entity を作る
     *   4. Repository を使ってDBに保存する
     *
     * @Transactional:
     *   このメソッド内のDB処理を1つのまとまりとして扱います。
     *   保存途中でエラーが起きた場合、中途半端な保存を防ぎます。
     *
     * @param form 画面から送られてきたカテゴリ入力値
     * @param user ログイン中ユーザー
     * @return 保存された ExamCategory
     */
    @Transactional
    public ExamCategory createCategory(ExamCategoryForm form, User user) {

        /*
         * 画面入力では、ユーザーがうっかり前後にスペースを入れることがあります。
         *
         * 例:
         *   " AWS SAA "
         *
         * そのまま保存すると「AWS SAA」と別名扱いになってしまうため、
         * trim() で前後の空白を削除します。
         */
        String trimmedName = form.getName().trim();

        /*
         * 説明は任意入力です。
         *
         * null の可能性があるため、nullチェックをしてから trim() します。
         */
        String trimmedDescription = null;
        if (form.getDescription() != null) {
            trimmedDescription = form.getDescription().trim();
        }

        /*
         * 同じユーザー内で同じカテゴリ名がすでに存在するか確認します。
         *
         * ここで user も条件に入れることが重要です。
         * 別ユーザーが同じカテゴリ名を使うことは問題ありません。
         */
        if (examCategoryRepository.existsByUserAndName(user, trimmedName)) {
            throw new IllegalArgumentException("同じ名前のカテゴリがすでに存在します");
        }

        /*
         * Form DTO から Entity を作ります。
         *
         * Form:
         *   画面入力を受け取るためのクラス
         *
         * Entity:
         *   DBに保存するためのクラス
         */
        ExamCategory category = new ExamCategory(trimmedName, trimmedDescription, user);

        /*
         * DBに保存します。
         *
         * save() は JpaRepository が用意してくれているメソッドです。
         */
        return examCategoryRepository.save(category);
    }
}