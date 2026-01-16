# ログイン画面 リニューアル計画書

## 目的

真っ暗な背景に文字が沈んでしまっているログイン画面 (`login.html`) を、背景の「月とうさぎ」に似合う、幻想的でオシャレなデザインに変更します。
**修正**: 左側のメニューバーと重ならないように、ログインフォームの位置を調整します。

## 1. デザインのコンセプト

- **ガラスモーフィズム (Glassmorphism)**:
  半透明の磨りガラスのような白いカードを画面中央に配置します。
- **レイアウト調整**:
  左側のメニューバー（ヘッダー）の分だけ余白を空けて、コンテンツが被らないようにします。

## 2. 作業手順

### 手順 1: `style.css` の修正

`static/css/style.css` の最後に追加した `.login-wrapper` のスタイルを、以下のように修正します。
`padding-left` を追加して、左側のメニューバー（約300pxと想定）の分だけ中身を右にずらします。

```css
/* =========================================
   ログイン画面デザイン (Login Page)
   ========================================= */

/* 画面全体：中央寄せ */
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh; 
  padding: 1rem;
  
  /* 【重要】左メニューバー対策 */
  /* メニューバーの幅(通常250-300px) + 余白 を確保 */
  padding-left: 320px; 
}

/* スマホ表示（メニューが上に来る場合）の調整 */
@media (max-width: 900px) {
  .login-wrapper {
    padding-left: 1rem; /* スマホなら左余白はリセット */
    align-items: center;
    padding-top: 100px; /* 上にメニューが来るので少し下げる */
  }
}

/* ガラス風カード */
.login-card {
  background: rgba(255, 255, 255, 0.15); /* 白の半透明 */
  backdrop-filter: blur(10px); 
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  padding: 3rem;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5);
  text-align: center;
  color: #fff;
}

/* ... (以下の .login-title, .form-group などは変更なし) ... */
.login-title {
  font-family: 'Jost', sans-serif;
  font-size: 2rem;
  letter-spacing: 0.2em;
  margin-bottom: 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  padding-bottom: 1rem;
}

.error-message {
  color: #ffcccc;
  background: rgba(255, 0, 0, 0.2);
  padding: 0.5rem;
  border-radius: 5px;
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
}

.form-group {
  margin-bottom: 1.5rem;
  text-align: left;
}

.form-group label {
  display: block;
  font-size: 0.8rem;
  margin-bottom: 0.5rem;
  opacity: 0.8;
  letter-spacing: 0.1em;
}

.input-modern {
  width: 100%;
  padding: 0.8rem 1rem;
  border: none;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.8);
  font-size: 1rem;
  transition: all 0.3s;
  outline: none;
}

.input-modern:focus {
  background: #fff;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.button-modern {
  width: 100%;
  padding: 0.8rem;
  margin-top: 1rem;
  border: none;
  border-radius: 30px;
  background: #fff;
  color: #333;
  font-size: 1.1rem;
  font-weight: bold;
  letter-spacing: 0.1em;
  cursor: pointer;
  transition: all 0.3s;
}

.button-modern:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

.back-link {
  display: inline-block;
  margin-top: 2rem;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.3s;
}

.back-link:hover {
  color: #fff;
}
```

## 3. 確認

1. `style.css` の `.login-wrapper` 部分を書き換える。
2. サーバーを再起動して、メニューと被らないことを確認。
