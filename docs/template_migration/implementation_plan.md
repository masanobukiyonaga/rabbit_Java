# デザインテンプレート移行計画

## 概要
Template Partyの無料テンプレート `tp_biz52_re_cafe_slide` をベースに、Webサイトを再構築する。
「カフェ風スライドショー」の特徴を活かし、うづきちゃんの写真を魅力的に見せる。

## 手順

### 1. 静的リソースの配置
ダウンロードフォルダ (`D:\Dwonlords\tp_biz52_re_cafe_slide`) から以下のフォルダを `src/main/resources/static` にコピーする。既存ファイルはバックアップまたは上書きする。
- `css/` -> `style.css`, `slide.css`
- `js/` -> `main.js` (jQueryなどはCDN利用のまま)
- `images/` -> ロゴや背景画像 (うづきちゃんの写真はAWS S3/既存URLを使用)

### 2. テンプレートの統合 (`base.html`)
テンプレートの `index.html` をベースに、Thymeleaf/Jinjava のレイアウトファイル `base.html` を再作成する。
- **共通部分**: `<head>`, `<header>` (Nav), `<aside id="mainimg">` (スライドショー), `<footer>`。
- **コンテンツ部分**: `<main>` タグ内を `{% block content %}` で置き換える。

### 3. 各ページの適用
- **トップページ (`index.html`)**: `new-top` お知らせエリアなどを活用し、Rabbit Info を表示。
- **日記一覧**: テンプレートの `portfolio.html`（ギャラリー）のデザインを流用し、日記カードを表示する。

## 注意点
- **著作権リンク**: Template Party の利用規約に基づき、フッターの著作権リンク (`《Web Design:Template-Party》`) は**削除しない**。
- **CSS競合**: 既存の `style.css` とテンプレートのものが混ざらないよう、テンプレート側を優先する。

## ユーザー確認
- テンプレート内のサンプル画像 (`images/`) も一旦コピーしてよいか確認する（後で差し替える前提）。
