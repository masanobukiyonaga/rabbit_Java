# 日記詳細ページ・手書き風リニューアル計画書

## 目的

1. **全体を見やすく**: スクロール量を減らすため、余白を調整してノートをコンパクトにします。
2. **フッター固定**: 下のバー（Copyrightなど）を常に画面の一番下に表示させます。

## 1. デザインの調整方針

- **ノート**: 余白（パディング）を減らし、画面を広く使えるようにします。
- **フッター**: `position: fixed` を使って画面下部に固定します。

## 2. 作業手順

### 手順 1: `style.css` の修正

既存の「手書きノート風デザイン」の部分を、以下のように書き換えて（上書きして）ください。
※ フッターの固定用スタイルもここに追加します。

```css
/* =========================================
   手書きノート風デザイン (Notebook Style)
   ========================================= */

/* 手書きフォントの適用 */
.notebook-container {
  font-family: "Yomogi", cursive;
  padding: 1rem; /* 2rem -> 1rem に減らしました */
  display: flex;
  justify-content: center;
  /* 画面の高さに合わせるが、内容が多い場合はスクロール可 */
  min-height: 80vh; 
  align-items: center; /* 上下中央寄せ */
}

/* ノートの紙のデザイン */
.notebook-page {
  background-color: #fcf6e8;
  background-image: radial-gradient(#dcdcdc 1px, transparent 1px);
  background-size: 20px 20px;
  width: 100%;
  max-width: 800px;
  padding: 2rem; /* 3rem -> 2rem に減らしました */
  border-radius: 5px;
  box-shadow: 
    1px 1px 0 rgba(0,0,0,0.1),
    5px 5px 15px rgba(0,0,0,0.2);
  position: relative;
  color: #4a3b32;
  margin-bottom: 40px; /* フッターとかぶらないように余白 */
}

/* 日付 */
.notebook-date {
  display: block;
  font-size: 1.1rem;
  text-align: right;
  margin-bottom: 0.2rem;
  color: #666;
}

/* タイトル */
.notebook-title {
  font-size: 1.5rem; /* 少し小さく */
  text-align: center;
  border-bottom: 2px dashed #ccc;
  padding-bottom: 0.5rem;
  margin-bottom: 1.5rem;
}

/* 写真の装飾 */
.notebook-photo-frame {
  background: #fff;
  padding: 8px; /* 10px -> 8px */
  box-shadow: 2px 2px 5px rgba(0,0,0,0.2);
  transform: rotate(-2deg);
  width: 70%; /* 80% -> 70% にして高さを抑える */
  margin: 0 auto 1.5rem auto; /* 2rem -> 1.5rem */
  max-width: 400px;
}

.notebook-photo-frame img {
  width: 100%;
  height: auto;
  display: block;
}

/* 本文 */
.notebook-content {
  font-size: 1.1rem;
  line-height: 1.8; /* 2.0 -> 1.8 に詰める */
  white-space: pre-wrap;
}

/* 戻るボタン */
.button-handwritten {
  display: inline-block;
  margin-top: 1.5rem;
  padding: 0.4rem 1.2rem;
  border: 2px solid #4a3b32;
  border-radius: 20px 5px 20px 5px;
  color: #4a3b32;
  text-decoration: none;
  font-weight: bold;
  transition: all 0.3s;
}

.button-handwritten:hover {
  background: #4a3b32;
  color: #fff;
  transform: scale(1.05); /* ポヨンと拡大 */
}


/* =========================================
   フッター固定設定 (Fixed Footer)
   ========================================= */

/* 著作権表示のバー */
footer {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  background: rgba(0, 0, 0, 0.3); /* 背景を少し暗くして読みやすく */
  z-index: 9999;
  padding: 5px 0;
  text-align: center;
}

/* テンプレートクレジット（右下のリンク） */
.pr {
  position: fixed;
  bottom: 40px; /* フッターの少し上 */
  right: 10px;
  z-index: 9999;
}
```

## 3. 確認

1. `style.css` の末尾（手書きデザイン部分）を上記のコードに書き換える。
2. サーバーを再起動して確認！
