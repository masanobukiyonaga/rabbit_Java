# 500エラー修正とCSSキャッシュ対策計画

## 1. 500エラー (S3アップロード) の修正

### 原因

`Main.java` の `uploadToS3` メソッドで、S3へのアップロード中に発生するエラー（認証エラー、通信エラーなど）が適切にキャッチされていません。
現在は `IOException` しかキャッチしていないため、S3特有の例外（`SdkClientException` など）が発生するとサーバー内部エラー（500）になり、画面が真っ白になります。/

### 修正内容

`Main.java` を修正し、`Exception` 全体をキャッチするように変更します。これにより、アップロードに失敗してもアプリ自体はクラッシュせず、エラーメッセージを表示したり、通常通り振る舞ったりできるようになります。

```java
// Main.java (修正予定)
private String uploadToS3(MultipartFile file) {
    try {
        // ... アップロード処理 ...
    } catch (Exception e) { // Exceptionに変更して全てのエラーを拾う
        e.printStackTrace();
        return null; // アップロード失敗時はnullを返す
    }
}
```

## 2. CSSキャッシュ (レイアウト未反映) の修正

### 原因

ブラウザが古いCSSファイルを保持しているため、新しい「中央寄せ」のスタイルが反映されていません。

### 修正内容

`base.html` のCSS読み込みパラメータを `v=13` に更新します。

```html
<!-- base.html (修正予定) -->
<link rel="stylesheet" href="/static/css/style.css?v=13">
```

## 手順

1. `Main.java` の例外処理を修正。
2. `base.html` のバージョン番号を更新。
3. サーバーを再起動（`mvn clean spring-boot:run`）して動作確認。
