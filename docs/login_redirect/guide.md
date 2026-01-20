# ログイン後のページをHOMEにする方法

ログイン成功後に表示されるページを「日記一覧ページ (`/diary`)」から「ホーム画面 (`/`)」に変更する手順です。

## 対象ファイル

`src/main/java/com/example/Main.java`

## 変更箇所

`loginSubmit` メソッド内のリダイレクト先URLを変更します。

### 変更前（現在のコード）

```java
if (appUsername.equals(username) && appPassword.equals(password)) {
    session.setAttribute("logged_in", true);
    response.sendRedirect("/diary"); // ← ここ
}
```

### 変更後

```java
if (appUsername.equals(username) && appPassword.equals(password)) {
    session.setAttribute("logged_in", true);
    response.sendRedirect("/");      // ← "/diary" を "/" に変更
}
```

## 手順

1. `src/main/java/com/example/Main.java` を開きます。
2. 410行目付近にある `response.sendRedirect("/diary");` を探します。
3. `/diary` の部分を削除し、 `/` だけにします。
4. ファイルを保存します。
5. サーバーを再起動（ターミナルで `mvn spring-boot:run` を実行しなおす）して反映完了です。
