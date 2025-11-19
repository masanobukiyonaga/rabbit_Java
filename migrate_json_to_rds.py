#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
from datetime import datetime
from pytz import timezone
import mysql.connector
import os

# タイムゾーン設定
JST = timezone('Asia/Tokyo')

# ------------------------
# RDS 接続情報
# ------------------------
DB_HOST = "april-diary-db.c9ouqcm6qmdp.ap-northeast-1.rds.amazonaws.com"
DB_PORT = 3306
DB_USER = "admin"
DB_PASSWORD = "8108za10"
DB_NAME = "april_diary"

# JSON ファイル
JSON_FILE = os.path.join(os.path.dirname(__file__), 'diary_data.json')

# ------------------------
# RDS 接続関数
# ------------------------
def get_db_connection():
    return mysql.connector.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        charset='utf8mb4'
    )

# ------------------------
# テーブル確認・カラム追加
# ------------------------
def ensure_table_and_columns():
    conn = get_db_connection()
    cursor = conn.cursor()
    # テーブル作成
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS diaries (
            id INT AUTO_INCREMENT PRIMARY KEY,
            title VARCHAR(255) NOT NULL,
            content TEXT NOT NULL,
            image_url VARCHAR(500),
            date DATETIME NOT NULL
        )
    """)
    conn.commit()

    # カラム確認
    cursor.execute("SHOW COLUMNS FROM diaries;")
    columns = [col[0] for col in cursor.fetchall()]

    # もし image_url や date がなければ追加
    if 'image_url' not in columns:
        cursor.execute("ALTER TABLE diaries ADD COLUMN image_url VARCHAR(500) NULL;")
    if 'date' not in columns:
        cursor.execute("ALTER TABLE diaries ADD COLUMN date DATETIME NOT NULL DEFAULT NOW();")

    conn.commit()
    cursor.close()
    conn.close()
    print("✅ テーブルとカラム確認・作成完了")

# ------------------------
# JSON → RDS 移行
# ------------------------
def migrate_json_to_rds():
    with open(JSON_FILE, 'r', encoding='utf-8') as f:
        entries = json.load(f)

    conn = get_db_connection()
    cursor = conn.cursor()

    migrated_count = 0

    for entry in entries:
        title = entry.get('title', '')
        content = entry.get('content', '')
        image_url = entry.get('image', None)

        date_str = entry.get('date', None)
        try:
            date_obj = datetime.strptime(date_str, '%Y-%m-%d %H:%M') if date_str else datetime.now(JST)
        except Exception:
            date_obj = datetime.now(JST)

        # デバッグ用
        print("DEBUG:", title, image_url, date_obj)

        try:
            cursor.execute("""
                INSERT INTO diaries (title, content, image_url, date)
                VALUES (%s, %s, %s, %s)
            """, (title, content, image_url, date_obj))
            migrated_count += 1
        except Exception as e:
            print(f"❌ INSERT エラー: {title}, {e}")
            continue

    conn.commit()
    cursor.close()
    conn.close()
    print(f"✅ JSON データ {migrated_count} 件を RDS に移行完了！")

# ------------------------
# 実行
# ------------------------
if __name__ == "__main__":
    ensure_table_and_columns()
    migrate_json_to_rds()
