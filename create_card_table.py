import pymysql

try:
    connection = pymysql.connect(
        host='localhost',
        user='root',
        password='123456',
        database='iseatspace',
        charset='utf8mb4'
    )
    
    cursor = connection.cursor()
    
    create_table_sql = """
    CREATE TABLE IF NOT EXISTS card (
        uuid VARCHAR(36) PRIMARY KEY,
        user_id VARCHAR(36) NOT NULL,
        card_id VARCHAR(8) NOT NULL,
        create_time DATETIME NOT NULL,
        study_duration INT NOT NULL,
        rarity VARCHAR(10) NOT NULL,
        border_theme VARCHAR(50) NOT NULL,
        card_theme VARCHAR(50) NOT NULL,
        theme_category VARCHAR(50) NOT NULL,
        markdown TEXT NOT NULL,
        image_url VARCHAR(500) NOT NULL,
        INDEX idx_user_id (user_id),
        INDEX idx_create_time (create_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    """
    
    cursor.execute(create_table_sql)
    connection.commit()
    print("Table 'card' created successfully!")
    
except Exception as e:
    print(f"Error creating table: {e}")
finally:
    if connection:
        connection.close()
