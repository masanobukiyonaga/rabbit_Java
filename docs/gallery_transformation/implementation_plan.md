# ギャラリー化 実装計画書（完全版）

## 目的

「DIARY」を「GALLERY」に完全に生まれ変らせます。
URLも `/diary` ではなく `/gallery` でアクセスできるようにし、デザインもタイル状にします。

## 1. 編集するファイル

- `templates/base.html`（ナビゲーション）
- `src/main/java/com/example/Main.java`（URLの制御）
- `templates/gallery.html`（**新規作成**：ギャラリーページの中身）

## 2. 作業手順

### 手順 1: ナビゲーションの更新 (`base.html`)

「GALLERY」のリンク先を `/gallery` に変更します。
（※すでにご自身で変更済みですね！完璧です！）

```html
<!-- 変更後 -->
<li><a href="/gallery"><i class="fa-solid fa-camera"></i>GALLERY</a></li>
```

### 手順 2: Javaプログラムの更新 (`Main.java`)

プログラムに「`/gallery` というURLが来たら、ギャラリーを表示する」という命令を追加します。

`src/main/java/com/example/Main.java` を開き、`public String diary()` という部分を探してください（261行目付近）。
その近くに、以下のコードを追加（コピペ）してください。

```java
    // ギャラリーページを表示する命令
    @GetMapping("/gallery")
    @ResponseBody
    public String gallery() {
        // 日記データを全て取得する
        List<Map<String, Object>> entries = jdbcTemplate.queryForList("SELECT * FROM diaries ORDER BY id DESC");
        Map<String, Object> context = new HashMap<>();
        context.put("entries", entries);
        // gallery.html を表示する
        return render("gallery.html", context);
    }
```

### 手順 3: ギャラリーページの作成 (`gallery.html`)

`templates` フォルダの中に、新しく `gallery.html` というファイルを作ってください。
そして、以下の内容を全てコピペして保存します。

（※ `base.html` に直接書くと他のページに影響が出るため、別ファイルにするのが正解です！）

```html
{% extends "base.html" %}

{% block content %}
<main class="masonry-wrapper">
    <div class="masonry-grid">
        <!-- 幅計算用の空要素 -->
        <div class="grid-sizer"></div>

        <!-- 日記データがあるだけ繰り返す -->
        {% for entry in entries %}
        <div class="grid-item">
            
            <!-- 【変更点】ここを「画像拡大」から「日記ページへのリンク」に変えました -->
            <a href="/diary/{{ entry.id }}">
                <img src="{{ entry.image_url }}" alt="{{ entry.title }}">
            </a>

        </div>
        {% endfor %}

    </div>
</main>
{% endblock %}
```

## 3. リンクの変更について（追加）

上記の `gallery.html` コードは修正済みです。

- 以前：`<a href="{{ entry.image_url }}" data-fancybox="gallery" ...>`（画像そのものへリンク、拡大機能付き）
- 今回：`<a href="/diary/{{ entry.id }}">`（日記の詳細ページへリンク）

`data-fancybox` を削除したことで、ポップアップせずに普通のページ遷移として動作します。

## 4. 動作確認

1. Javaのプログラムを変更したので、**再起動**が必要です（停止→再実行）。
2. ブラウザでトップページを開き、メニューの「GALLERY」をクリック。
3. アドレスバーが `/gallery` になり、画像がタイル状に並べば成功です！
