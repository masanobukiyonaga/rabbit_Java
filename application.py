from flask import Flask, render_template, request, redirect, url_for, session
from markupsafe import Markup
from werkzeug.utils import secure_filename
from functools import wraps
from datetime import datetime
from pytz import timezone
import boto3
import uuid
import mysql.connector
import os
# 【追加】環境変数を読み込むためのライブラリ
from dotenv import load_dotenv

# 【追加】 .envファイルを読み込む
load_dotenv()

# タイムゾーン設定
JST = timezone('Asia/Tokyo')

# Flask アプリ初期化
application = Flask(__name__)
# 【修正】環境変数から読み込む
application.secret_key = os.environ.get("FLASK_SECRET_KEY")

# ------------------------
# AWS S3 設定
# ------------------------
S3_BUCKET = 'april-static'
CLOUDFRONT_URL = 'https://d2sf57sw5vr2iz.cloudfront.net'
s3 = boto3.client('s3')

# ------------------------
# 簡易ユーザー認証
# ------------------------
# 【修正】環境変数から読み込む
USERNAME = os.environ.get("APP_USERNAME")
PASSWORD = os.environ.get("APP_PASSWORD")

# ------------------------
# RDS 接続情報
# ------------------------
DB_HOST = "april-diary-db.c9ouqcm6qmdp.ap-northeast-1.rds.amazonaws.com"
DB_PORT = 3306
DB_USER = "admin"
DB_PASSWORD = "8108za10"
DB_NAME = "april_diary"

def get_db_connection():
    return mysql.connector.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
	# 【修正】環境変数から読み込む（これで安全！）
        password=os.environ.get("DB_PASSWORD"),
        charset='utf8mb4'
    )

# ------------------------
# S3 アップロード関数
# ------------------------
def upload_to_s3(file):
    try:
        filename = secure_filename(file.filename)
        ext = filename.rsplit('.', 1)[1].lower()
        unique_filename = f"images/{uuid.uuid4()}.{ext}"
        s3.upload_fileobj(
            file,
            S3_BUCKET,
            unique_filename,
            ExtraArgs={'ContentType': file.content_type}
        )
        return f"{CLOUDFRONT_URL}/{unique_filename}"
    except Exception as e:
        application.logger.error(f"S3 upload failed: {e}")
        return None

# ------------------------
# ログイン必須デコレーター
# ------------------------
def login_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not session.get("logged_in"):
            return redirect(url_for("login"))
        return f(*args, **kwargs)
    return decorated_function

# ------------------------
# トップページ
# ------------------------
@application.route('/')
def index():
    rabbit = {
        "name": "うづきちゃん",
        "age": "4歳",
        "gender": "女の子",
        "favorite_food": "乾燥りんご",
        "hobby": "チモシーとうんこを散らかす",
        "image": "0513.jpg"
    }
    return render_template('index.html', rabbit=rabbit)

# ------------------------
# 日記一覧
# ------------------------
@application.route('/diary')
def diary():
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM diaries ORDER BY id DESC")
    entries = cursor.fetchall()
    cursor.close()
    conn.close()
    return render_template('diary.html', entries=entries)

# ------------------------
# 個別日記
# ------------------------
@application.route('/diary/<int:entry_id>')
def diary_entry(entry_id):
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM diaries WHERE id=%s", (entry_id,))
    entry = cursor.fetchone()
    cursor.close()
    conn.close()
    if entry:
        return render_template('diary_entry.html', entry=entry)
    return "日記が見つかりません", 404

# ------------------------
# 新規日記投稿
# ------------------------
@application.route('/diary/new', methods=['GET', 'POST'])
@login_required
def new_diary():
    if request.method == 'POST':
        title = request.form.get('title', '')
        content = request.form.get('content', '').replace('\r\n', '\n').replace('\r', '\n')

        image_url = None
        file = request.files.get('image')
        if file and file.filename:
            image_url = upload_to_s3(file)

        date_obj = datetime.now(JST)

        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("""
            INSERT INTO diaries (title, content, image_url, `date`)
            VALUES (%s, %s, %s, %s)
        """, (title, content, image_url, date_obj))
        conn.commit()
        cursor.close()
        conn.close()

        return redirect('/diary')

    return render_template('new_diary.html')

# ------------------------
# 編集
# ------------------------
@application.route("/diary/<int:entry_id>/edit", methods=["GET", "POST"])
@login_required
def edit_diary(entry_id):
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM diaries WHERE id=%s", (entry_id,))
    entry = cursor.fetchone()

    if not entry:
        cursor.close()
        conn.close()
        return "日記が見つかりません", 404

    if request.method == "POST":
        title = request.form["title"]
        content = request.form["content"].replace('\r\n', '\n').replace('\r', '\n')

        file = request.files.get('image')
        image_url = entry["image_url"]
        if file and file.filename:
            image_url = upload_to_s3(file)

        cursor.execute("""
            UPDATE diaries
            SET title=%s, content=%s, image_url=%s
            WHERE id=%s
        """, (title, content, image_url, entry_id))
        conn.commit()
        cursor.close()
        conn.close()
        return redirect(url_for("diary"))

    cursor.close()
    conn.close()
    return render_template("edit_diary.html", entry=entry)

# ------------------------
# 削除
# ------------------------
@application.route("/diary/<int:entry_id>/delete", methods=["POST"])
@login_required
def delete_diary(entry_id):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM diaries WHERE id=%s", (entry_id,))
    conn.commit()
    cursor.close()
    conn.close()
    return redirect(url_for("diary"))

# ------------------------
# 改行変換フィルター
# ------------------------
@application.template_filter('nl2br')
def nl2br_filter(s):
    if s is None:
        return ''
    escaped = Markup.escape(s)
    return Markup(escaped.replace('\r\n', '\n').replace('\r', '\n').replace('\n', '<br>\n'))

# ------------------------
# S3 テスト
# ------------------------
@application.route('/test-s3')
def test_s3():
    try:
        result = s3.list_objects_v2(Bucket=S3_BUCKET)
        return f"S3にアクセス成功！オブジェクト数: {result.get('KeyCount', 0)}"
    except Exception as e:
        return f"接続エラー: {e}"

# ------------------------
# 投げ銭完了ページ
# ------------------------
@application.route('/thanks')
def thanks():
    return render_template('thanks.html')

# ------------------------
# ログイン・ログアウト
# ------------------------
@application.route("/login", methods=["GET", "POST"])
def login():
    error = None
    if request.method == "POST":
        username = request.form.get("username")
        password = request.form.get("password")
        if username == USERNAME and password == PASSWORD:
            session["logged_in"] = True
            return redirect(url_for("diary"))
        else:
            error = "ユーザー名またはパスワードが間違っています"
    return render_template("login.html", error=error)

@application.route("/logout")
def logout():
    session.pop("logged_in", None)
    return redirect(url_for("login"))

# ------------------------
# 実行
# ------------------------
if __name__ == '__main__':
    application.run(host='0.0.0.0', port=5000, debug=True)
