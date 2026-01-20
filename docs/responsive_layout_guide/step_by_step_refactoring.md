# レスポンシブ修正ガイド（お知らせバー＆メイン余白）

ご指定いただいた2つの箇所を、画面サイズが変わっても崩れない「レスポンシブな書き方」に修正する手順です。

---

## 修正1: トップページのお知らせバー (`.new-top`)

「左から何px、幅は何px」という指定をやめ、「画面幅の90%を使って中央配置」という形に変更します。

### 対象ファイル: `src/main/resources/static/css/style.css`

**318行目付近** の `.new-top` と、**333行目付近** の `@media` 内の設定を書き換えます。

#### 変更前（Before）

```css
/* PC画面で「左から100px、幅は全体から200px引いた分」という複雑な計算をしている */
.new-top {
  position: absolute;
  left: 0px;
  /* ...中略... */
}

@media (min-width:700px) {
  .new-top {
    left: 100px;
    width: calc(100% - 200px);
    /* ...中略... */
  }
}
```

#### 変更後（After / これに書き換え！）

「左も右も自動(`0`)、マージンも自動(`auto`)」にすると、絶対配置(`absolute`)の要素でも真ん中に来ます。

```css
.new-top {
  position: absolute;
  /* 左右を0にして margin: auto で中央寄せするテクニック */
  left: 0;
  right: 0;
  margin: 0 auto;
  
  bottom: 40px;
  z-index: 1;
  
  /* 幅はスマホ基準で94%くらい */
  width: 94%;
  max-width: 1000px; /* PCでも1000px以上は広がらない */
  
  display: flex;
  background: rgba(0, 0, 0, 0.3);
  padding: 0 0.8rem 0 1rem;
  box-sizing: border-box; /* パディングを含めた幅計算にする */
}

@media (min-width:700px) {
  .new-top {
    bottom: 100px;
    /* PCなら少し丸くする等の装飾だけ残す（位置調整は不要！） */
    border-radius: 100px;
    padding: 0 1.2rem 0 2rem;
  }
}
```

---

## 修正2: メインコンテンツの余白 (`body:not(.home) main`)

「左に60px空ける」という指定をやめ、Flexboxを使って「サイドバーの横に並べる」構成にします。
これにより、数値計算なしで自動的にサイドバーの隣に配置されます。

### 手順A: 全体の囲みをFlexboxにする

`style.css` の **215行目付近** にある `#container` を修正します。

```css
/* 変更前 */
#container {
  display: grid;
  grid-template-rows: auto 1fr;
  /* ... */
}

/* 変更後：サイドバー(header)とメイン(main)を横に並べたいので flex-direction: row に */
#container {
  display: flex;
  flex-direction: row; /* 横並び */
  min-height: 100vh;
}
```

### 手順B: メインコンテンツの余白を削除

`style.css` の **410行目〜435行目付近** にあるマージン設定をシンプルにします。

```css
/* 変更後（これだけ！） */
body:not(.home) main {
  flex: 1; /* 残りのスペースを全部使う */
  margin: 0 auto; /* 中身の要素は中央寄せ */
  padding: 2rem;  /* 最低限の隙間 */
  width: 100%;
  max-width: 1200px; /* 読みやすい最大幅 */
}

/* 以前のような @media での margin 調整はすべて 【削除】 してOKです！ */
/* header(サイドバー)は position: fixed なので、そこだけ注意が必要ですが、
   今のデザインなら main に padding-left: (サイドバーの幅) を入れるのが一番簡単かつ安全です */

/* 修正版：サイドバー固定(fixed)のままレスポンシブにするならこちら */
body:not(.home) main {
  /* サイドバーの幅(header-size)分だけ左を空ける */
  margin-left: var(--header-size); 
  
  padding: 3rem;
  width: 100%;
  box-sizing: border-box;
}
```

**補足:**
現在のコードは「サイドバーが固定(`position: fixed`)」で作られているため、Flexboxで単純に横並びにするよりも、**「マージンをサイドバーの幅(`var(--header-size)`)と同じにする」** という書き方（上記の修正版）が最もコード変更が少なく、かつ崩れにくい方法です。
これにより、「60px」や「100px」といった謎の数字がなくなり、「サイドバーの幅分だけ空ける」という明確なルールになります。
