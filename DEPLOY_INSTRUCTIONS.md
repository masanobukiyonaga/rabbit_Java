# AWS EC2へのデプロイ（更新）手順

今回作成した「投げ銭機能」を本番環境（EC2）に反映させる手順です。
Elastic Beanstalkを使わず、EC2で直接動かしている場合の一般的な更新手順になります。

## 手順の概要
1. **ローカル**: 変更をGitに保存（コミット）して、GitHubにアップロード（プッシュ）する
2. **サーバー (EC2)**: サーバーに接続して、変更をダウンロード（プル）する
3. **サーバー (EC2)**: アプリを再起動して変更を反映させる

---

## 詳細手順

### 1. ローカルでの作業（変更の保存）
VS Codeのターミナルで以下のコマンドを順番に実行してください。

```powershell
# 1. 変更したファイルをすべてステージング（登録）
git add .

# 2. 変更を保存（コミット）
git commit -m "投げ銭機能を追加"

# 3. GitHubにアップロード（プッシュ）
git push origin main
```
※ `main` の部分は、もし `master` ブランチを使っている場合は `master` に変えてください。

### 2. EC2サーバーでの作業（変更の取り込み）

まず、PowerShellなどでEC2にSSH接続します。
（いつもの接続コマンドを使ってください。例: `ssh -i key.pem ec2-user@...`）

接続できたら、以下のコマンドを実行します。

```bash
# 1. アプリのディレクトリに移動（パスはご自身の環境に合わせてください）
cd /path/to/rabbit_site
# 例: cd ~/rabbit_site や cd /var/www/rabbit_site など

# 2. GitHubから最新の変更を取得
git pull origin main

# 3. 必要なライブラリのインストール（もしrequirements.txtが変わっていた場合）
# 今回は変更していませんが、念のため
pip install -r requirements.txt
```

### 3. アプリの再起動
変更を反映させるために、Webサーバーを再起動する必要があります。
どのようにアプリを動かしているかによってコマンドが異なります。

**パターンA: systemd (サービス) で動かしている場合**
（`sudo systemctl start ...` などで起動した場合）
```bash
sudo systemctl restart rabbit-site
# ※ "rabbit-site" の部分はご自身で設定したサービス名に置き換えてください
# サービス名がわからない場合は `systemctl list-units --type=service` で探せます
```

**パターンB: Gunicornなどを直接実行している場合**
一度プロセスを終了して、再度起動コマンドを打ちます。
```bash
# プロセスIDを調べる
ps aux | grep gunicorn

# プロセスを終了（kill）して、いつもの起動コマンドを実行
```

**パターンC: python application.py で動かしている場合（開発用など）**
`Ctrl + C` で停止して、再度 `python application.py` を実行します。

---

これで本番環境にも「投げ銭ボタン」が表示されるはずです！
ブラウザでアクセスして確認してみてください。
