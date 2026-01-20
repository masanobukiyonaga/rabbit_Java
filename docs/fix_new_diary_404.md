# /new_diary 404エラーの修正方法

## 原因

現在、HTML（`base.html`）には `/new_diary` というリンクが書かれていますが、Javaのプログラム（`Main.java`）側では `/diary/new` という住所で待ち受けています。
この「住所の不一致」が原因で、「そんなページはないよ（404 Not Found）」というエラーが出ています。

## 修正手順

`src/main/resources/templates/base.html` のリンク先を修正します。

#### 変更前（誤）

```html
<li><a href="/new_diary"><i class="fa-solid fa-pen"></i>NEW DIARY</a></li>
```

#### 変更後（正）

リンク先を `/diary/new` に書き換えてください。

```html
<li><a href="/diary/new"><i class="fa-solid fa-pen"></i>NEW DIARY</a></li>
```

---

### なぜ `/diary/new` なの？

プログラム内部（`Main.java`）で以下のように設定されているためです。

```java
// Main.java 296行目
@GetMapping("/diary/new") // ← ここで住所を決めている
public String newDiaryForm() { ... }
```

これを修正して保存すれば、エラー画面ではなく正しい作成画面が表示されるようになります！🐰
