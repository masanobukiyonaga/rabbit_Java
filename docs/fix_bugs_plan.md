# バグ修正とキャッシュ対策計画 (Bug Fix & Cache Plan)

## 1. 500エラー (Internal Server Error) の調査と修正

編集ページ (`/diary/{id}/edit`) にアクセスするとエラーが発生しています。

### 考えられる原因

1. **テンプレートの読み込み失敗**: `edit_diary.html` が正しくデプロイされていない、またはファイルがロックされている。
2. **テンプレートの記述ミス**: Jinjavaが解釈できない構文が含まれている。
3. **データ取得エラー**: `jdbcTemplate` のクエリで予期せぬエラー（IDが存在しないなど）が起きているが、`catch` ブロック外で例外が出ている。

### 対応策

1. **`edit_diary.html` の再確認**: 正しい内容で保存されているか確認します。特に `{% extends %}` や `{% block %}` の閉じ忘れがないかチェックします。
2. **ログの確認**: ユーザーにターミナルのエラーログを確認してもらい、具体的な原因（NullPointerExceptionなど）を特定します。

## 2. CSSキャッシュ問題の修正

CSSを編集しても反映されないのは、ブラウザが古いCSSを記憶（キャッシュ）しているためです。

### 対応策

`src/main/resources/templates/base.html` の読み込み記述を変更し、バージョン番号を更新します。

```html
<!-- 変更前 -->
<link rel="stylesheet" href="/static/css/style.css?v=11">

<!-- 変更後（バージョンを上げる） -->
<link rel="stylesheet" href="/static/css/style.css?v=12">
```

## 手順

1. `base.html` のCSSバージョンを更新。
2. `edit_diary.html` のコードを再確認・修正（必要であれば）。
3. Webサーバーを再起動して確認。
