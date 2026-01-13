# 🐰 うづきちゃんの日記 (Rabbit Diary App)

AWS上に構築した、うさぎの成長記録・日記アプリケーションです。
画像をクラウド（S3）に保存し、CDN（CloudFront）経由で高速配信する構成を実装しました。

## 📷 アプリの画面
<img width="2495" height="1401" alt="image" src="https://github.com/user-attachments/assets/be7ae3fd-2a06-4ca3-91cf-8383b8825d8f" />
<img width="2495" height="1403" alt="スクリーンショット 2025-11-20 014406" src="https://github.com/user-attachments/assets/239805f4-56ad-4d10-9f2b-0847e1e9ae8e" />
<img width="2482" height="1413" alt="スクリーンショット 2025-11-20 014701" src="https://github.com/user-attachments/assets/825dc704-4821-454a-afd7-9f50f0ff890d" />

## 🛠 使用技術 (Tech Stack)
### バックエンド
- **Python 3.9** (Flask framework)
- **MySQL** (データ永続化)

### インフラ・クラウド (AWS)
- **EC2** (Amazon Linux 2023 / Webサーバー)
- **RDS** (MySQL / データベースサーバー)
- **S3** (画像ストレージ)
- **CloudFront** (CDN / 画像配信の高速化・負荷分散)

### その他・ツール
- **Git / GitHub** (バージョン管理)
- **python-dotenv** (環境変数によるセキュリティ対策)

## 🔥 こだわったポイント
1. **セキュリティ**
   - DBパスワードなどの機密情報はコードに直接書かず、環境変数(`.env`)を使用して管理し、GitHub公開時の漏洩を防いでいます。
   
2. **パフォーマンス**
   - ユーザーがアップロードした画像はWebサーバー（EC2）ではなくS3に保存し、CloudFrontを経由させることで表示速度の向上とサーバー負荷の軽減を行いました。

3. **CRUD機能の実装**
   - 日記の「投稿・閲覧・編集・削除」というWebアプリの基本機能をすべてSQLとPythonで実装しました。

4. **投げ銭機能（Stripe連携）**
   - 外部決済サービス（Stripe）へのリンクボタンを設置し、うづきちゃんへのおやつ代（寄付）を受け取れるようにしました。

## 🚀 今後の展望
- ユーザーログイン機能の強化
- 日記の検索機能の追加
