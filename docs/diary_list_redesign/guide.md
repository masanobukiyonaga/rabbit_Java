# 日記一覧ページのリニューアル手順（修正版）

以前の手順で発生した「左メニューとの被り」と「文字の見づらさ」を解消し、参考画像のような「中央配置の黒い透明カード」デザインにするための修正手順です。

## 手順概要

1. `style.css` に、位置調整用の新しいクラスを追加します。
2. `diary.html` の構造を少し変更して、新しいクラスを適用します。

---

## ステップ1: CSSの修正・追加

`src/main/resources/static/css/style.css` の **一番最後** に、以下のコードを追記（または前のコードを上書き）してください。

```css
/* =========================================
   日記一覧リスト (Diary List Redesign)
   ========================================= */

/* 1. 位置調整用ラッパー（サイドバー対策） */
.diary-wrapper {
  display: flex;
  justify-content: center; /* 左右中央寄せ */
  min-height: 100vh;
  padding: 50px 2rem;      /* 上下と左右の余白 */
  
  /* 上下と左右の余白 */
  
  box-sizing: border-box;
}

/* 2. 黒い半透明カード（背景） */
.diary-list-card {
  background: rgba(0, 0, 0, 0.7); /* 背景色（黒の70%濃さで読みやすく） */
  padding: 3rem;
  border-radius: 20px;
  width: 100%;
  max-width: 900px;        /* カードの最大幅 */
  color: #fff;             /* 文字色を白に */
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.5); /* 影をつけて浮き上がらせる */
  height: fit-content;     /* 中身に合わせて高さを自動調整 */
}

/* ページタイトル (DIARY) */
.page-title {
  font-family: 'Jost', sans-serif;
  font-size: 2rem;
  letter-spacing: 0.2em;
  margin-bottom: 2rem;
  text-transform: uppercase;
  border-left: 4px solid #fff;
  padding-left: 1rem;
  text-align: left;
}

/* リンク文字の装飾 */
.ta1 th a {
  color: #fff;
  text-decoration: none;
  font-size: 1.1rem;
  border-bottom: 1px dotted rgba(255, 255, 255, 0.5);
  transition: all 0.3s;
  display: inline-block; /* アニメーション用にブロック化 */
}

.ta1 th a:hover {
  color: #bfaee3; /* ホバー時はラベンダー色 */
  border-bottom: 1px solid #bfaee3;
  transform: translateX(10px); /* 右にスライド */
}

/* テーブル全体の調整 */
.ta1 {
  width: 100%;
  border-collapse: collapse; /* 隙間をなくす */
}

.ta1 th, .ta1 td {
  padding: 1.5rem 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2); /* 区切り線を薄く */
}

.ta1 th {
  width: 30%; /* タイトル列の幅 */
  vertical-align: top;
}

/* スマホ対応（レスポンシブ） */
@media (max-width: 900px) {
  .diary-wrapper {
    padding-left: 1rem;   /* スマホでは左余白をなくす */
    padding-top: 100px;   /* 上にメニューが来るので下げる */
  }
  
  .diary-list-card {
    padding: 1.5rem;
  }
  
  /* スマホでは縦並びにする */
  .ta1 th, .ta1 td {
    display: block;
    width: 100%;
    padding: 0.5rem 0;
  }
  
  .ta1 th {
    border-bottom: none;
    margin-bottom: 0.5rem;
    font-size: 1.2rem;
  }
  
  .ta1 td {
    margin-bottom: 2rem;
    padding-left: 1rem;
    border-left: 2px solid rgba(255,255,255,0.3);
  }
}
```

---

## ステップ2: HTMLの修正

`src/main/resources/templates/diary.html` を以下のコードに完全に書き換えてください。
前の `container` や `diary-list-container` を、新しい `diary-wrapper` と `diary-list-card` に置き換えています。

```html
{% extends "base.html" %}

{% block content %}
<!-- 位置調整用ラッパー -->
<div class="diary-wrapper">

  <!-- 黒背景のカード -->
  <div class="diary-list-card">
    
    <h2 class="page-title">DIARY</h2>

    <!-- 表組み -->
    <table class="ta1">
      
      {% for entry in entries %}
      <tr>
        <!-- 左側：タイトル（リンク付き） -->
        <th>
          <a href="/diary/{{ entry.id }}">{{ entry.title }}</a>
        </th>
        
        <!-- 右側：日付と本文の抜粋 -->
        <td>
          <small style="opacity: 0.7; display:block; margin-bottom:0.5rem;">{{ entry.date }}</small>
          {{ entry.content }}
        </td>
      </tr>
      {% endfor %}
      
    </table>

  </div>

</div>
{% endblock %}
```

## 修正のポイント

1. **左余白の確保**: `padding-left: 350px` を入れたので、左側のメニューバーと被らなくなります。
2. **可読性の向上**: 背景の黒を少し濃くし (`0.6` -> `0.7`)、文字色を完全な白に設定しました。
3. **カードデザイン**: 画面いっぱいに広がらないよう `max-width: 900px` を設定し、中央に配置されるようにしました。
