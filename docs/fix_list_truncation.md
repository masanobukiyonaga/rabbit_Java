# 日記一覧の本文省略計画 (Truncate List Content)

## 現状の問題

日記一覧ページ (`/diary`) で、本文が全て表示されてしまい、レイアウトが間延びしています（画像のように長文が表示されてしまう）。
ユーザー様のご要望通り、「最初の1行だけを表示（残りは...で省略）」するように変更します。

## 変更内容

### 1. HTML (`diary.html`)

現在、`<td>` の中に直接 `{{ entry.content }}` が書かれていますが、これを `<div class="diary-text-preview">` で囲みます。

```html
<!-- 変更前 -->
<td>
  <small>...</small>
  {{ entry.content }}
</td>

<!-- 変更後 -->
<td>
  <small>...</small>
  <div class="diary-text-preview">
    {{ entry.content }}
  </div>
</td>
```

### 2. CSS (`style.css`)

新しく作った `.diary-text-preview` クラスに、省略用のスタイルを適用します。

```css
.diary-text-preview {
  display: block;
  white-space: nowrap;      /* 改行させない（1行にする） */
  overflow: hidden;         /* はみ出た部分を隠す */
  text-overflow: ellipsis;  /* はみ出た部分を「...」にする */
  max-width: 500px;         /* 幅制限（画面サイズに応じて調整） */
  color: #ccc;              /* 少し薄い色にして「プレビュー感」を出す */
}

/* スマホ対応 */
@media (max-width: 600px) {
  .diary-text-preview {
    max-width: 200px;       /* スマホならもっと短く */
  }
}
```

## 手順

1. `diary.html` を修正して `div` で囲む。
2. `style.css` にスタイルを追加。
3. `base.html` のバージョンを `v=16` に更新。
