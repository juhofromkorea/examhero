package com.example.examhero.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.examhero.dto.QuestionCardForm;
import com.example.examhero.entity.ExamCategory;
import com.example.examhero.entity.QuestionCard;
import com.example.examhero.entity.User;
import com.example.examhero.repository.ExamCategoryRepository;
import com.example.examhero.repository.QuestionCardRepository;
import com.example.examhero.repository.UserRepository;

/**
 * QuestionCardService
 *
 * 問題カードに関するアプリケーション処理を担当するServiceクラスです。
 *
 * Service は、Controller と Repository の間に置く層です。
 *
 * Controller:
 *   ブラウザからのリクエストを受け取る
 *
 * Service:
 *   実際の処理、ルール、判断を書く
 *
 * Repository:
 *   DBへの保存・検索を担当する
 *
 * 今回このServiceで担当する処理:
 * - ログイン中ユーザーをメールアドレスから取得する
 * - ログイン中ユーザーの問題カード一覧を取得する
 * - カテゴリ別の問題カード一覧を取得する
 * - 問題カードを1件取得する
 * - 新しい問題カードを作成する
 *
 * 重要:
 *   ExamHeroでは、問題カードはユーザーごとの個人データです。
 *   そのため、問題カードを取得するときは必ず User 条件を使います。
 */
@Service
public class QuestionCardService {

    /**
     * 問題カード用Repository
     *
     * question_cards テーブルへのDB操作に使います。
     */
    private final QuestionCardRepository questionCardRepository;

    /**
     * 試験カテゴリ用Repository
     *
     * 問題カード作成時に、
     * フォームから送られてきた examCategoryId が
     * 本当にログイン中ユーザーのカテゴリか確認するために使います。
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
     * Spring が Repository を自動で渡してくれます。
     * これをコンストラクタインジェクションと呼びます。
     */
    public QuestionCardService(
            QuestionCardRepository questionCardRepository,
            ExamCategoryRepository examCategoryRepository,
            UserRepository userRepository
    ) {
        this.questionCardRepository = questionCardRepository;
        this.examCategoryRepository = examCategoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * メールアドレスからログイン中ユーザーを取得します。
     *
     * Spring Security の UserDetails から取得できる username は、
     * このアプリでは「メールアドレス」として扱っています。
     *
     * Controllerでは、
     *   userDetails.getUsername()
     * を呼び、その値をこのメソッドに渡します。
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
     * ログイン中ユーザーの問題カード一覧を取得します。
     *
     * 重要:
     *   findAll() は使いません。
     *
     * 理由:
     *   findAll() を使うと、他のユーザーが作成した問題カードまで
     *   取得してしまう可能性があります。
     *
     * ExamHeroでは問題カードは個人データなので、
     * 必ず User を条件にして検索します。
     *
     * @param user ログイン中ユーザー
     * @return ログイン中ユーザーが作成した問題カード一覧
     */
    @Transactional(readOnly = true)
    public List<QuestionCard> findQuestionCardsByUser(User user) {
        return questionCardRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * ログイン中ユーザーの、指定カテゴリ内の問題カード一覧を取得します。
     *
     * 例:
     * - AWS SAAカテゴリの問題だけ表示する
     * - Java Silverカテゴリの問題だけ表示する
     *
     * @param user ログイン中ユーザー
     * @param examCategoryId 試験カテゴリID
     * @return 指定カテゴリ内の問題カード一覧
     */
    @Transactional(readOnly = true)
    public List<QuestionCard> findQuestionCardsByCategory(User user, Long examCategoryId) {

        /*
         * カテゴリIDだけで検索すると、他ユーザーのカテゴリを指定される危険があります。
         *
         * そのため、ExamCategoryRepository の findByIdAndUser(id, user) を使い、
         * 「そのカテゴリがログイン中ユーザーのものか」を確認します。
         */
        ExamCategory examCategory = findCategoryByIdAndUser(examCategoryId, user);

        /*
         * ログイン中ユーザー + 指定カテゴリ の条件で問題カードを取得します。
         */
        return questionCardRepository.findByUserAndExamCategoryOrderByCreatedAtDesc(user, examCategory);
    }

    /**
     * ログイン中ユーザーの問題カードをIDで1件取得します。
     *
     * 問題詳細画面で使う予定です。
     *
     * findById(id) だけを使うと、他のユーザーの問題カードIDを指定された場合でも
     * 取得できてしまう危険があります。
     *
     * そのため、必ず findByIdAndUser(id, user) を使います。
     *
     * @param id 問題カードID
     * @param user ログイン中ユーザー
     * @return 問題カード
     */
    @Transactional(readOnly = true)
    public QuestionCard findQuestionCardByIdAndUser(Long id, User user) {
        return questionCardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("問題カードが見つかりません"));
    }

    /**
     * 新しい問題カードを作成します。
     *
     * 処理の流れ:
     *   1. フォームから送られたカテゴリIDを確認する
     *   2. そのカテゴリがログイン中ユーザーのものか確認する
     *   3. 入力値の前後の空白を削除する
     *   4. QuestionCard Entity を作成する
     *   5. Repository を使ってDBに保存する
     *
     * @Transactional:
     *   このメソッド内のDB処理を1つのまとまりとして扱います。
     *   保存途中でエラーが起きた場合、中途半端な保存を防ぎます。
     *
     * @param form 画面から送られてきた問題カード入力値
     * @param user ログイン中ユーザー
     * @return 保存された QuestionCard
     */
    @Transactional
    public QuestionCard createQuestionCard(QuestionCardForm form, User user) {

        /*
         * まず、フォームから送られてきたカテゴリIDを使ってカテゴリを取得します。
         *
         * ただし、単純に findById() は使いません。
         * 他ユーザーのカテゴリIDを指定される可能性があるからです。
         *
         * findByIdAndUser() を使うことで、
         * 「ログイン中ユーザーが所有しているカテゴリだけ」を取得できます。
         */
        ExamCategory examCategory = findCategoryByIdAndUser(form.getExamCategoryId(), user);

        /*
         * フォーム入力値の前後の空白を削除します。
         *
         * 例:
         *   " AWS S3について "
         *   → "AWS S3について"
         *
         * 必須項目は Bean Validation の @NotBlank でチェック済みですが、
         * 保存前に trim() しておくとデータがきれいになります。
         */
        String questionText = form.getQuestionText().trim();
        String optionA = form.getOptionA().trim();
        String optionB = form.getOptionB().trim();
        String optionC = form.getOptionC().trim();
        String optionD = form.getOptionD().trim();
        String correctAnswer = form.getCorrectAnswer().trim();

        /*
         * 解説は任意入力です。
         *
         * null の可能性があるため、nullチェックをしてから trim() します。
         * 空文字の場合は null にしておくと、DB上でも「未入力」と分かりやすくなります。
         */
        String explanation = null;

        if (form.getExplanation() != null && !form.getExplanation().trim().isEmpty()) {
            explanation = form.getExplanation().trim();
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
        QuestionCard questionCard = new QuestionCard(
                questionText,
                optionA,
                optionB,
                optionC,
                optionD,
                correctAnswer,
                explanation,
                user,
                examCategory
        );

        /*
         * DBに保存します。
         *
         * save() は JpaRepository が用意してくれているメソッドです。
         */
        return questionCardRepository.save(questionCard);
    }

    /**
     * 指定したカテゴリIDが、ログイン中ユーザーのカテゴリか確認して取得します。
     *
     * このメソッドはService内部で使う補助メソッドです。
     *
     * なぜ必要か:
     *   問題カード作成時、フォームから examCategoryId が送られてきます。
     *   しかし、そのIDが本当にログイン中ユーザーのカテゴリとは限りません。
     *
     * 例:
     *   user A が category id = 1 を持っている
     *   user B がURLやHTMLを書き換えて category id = 1 を送る
     *
     * もし findById(1) だけで取得すると、
     * user B が user A のカテゴリに問題を作れてしまう危険があります。
     *
     * そのため、id と user の両方で検索します。
     *
     * @param examCategoryId 試験カテゴリID
     * @param user ログイン中ユーザー
     * @return ログイン中ユーザーが所有する試験カテゴリ
     */
    private ExamCategory findCategoryByIdAndUser(Long examCategoryId, User user) {
        return examCategoryRepository.findByIdAndUser(examCategoryId, user)
                .orElseThrow(() -> new IllegalArgumentException("カテゴリが見つかりません"));
    }

    /**
     * ログイン中ユーザーが作成した問題カードの総数を取得します。
     *
     * ダッシュボードで「登録済み問題数」を表示したいときに使えます。
     *
     * @param user ログイン中ユーザー
     * @return 問題カード総数
     */
    @Transactional(readOnly = true)
    public long countQuestionCardsByUser(User user) {
        return questionCardRepository.countByUser(user);
    }
}