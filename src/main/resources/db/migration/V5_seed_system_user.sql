INSERT INTO users (login_id, password_hash, nickname, friend_code, status, created_by, updated_by)
VALUES ('system', 'disabled', 'SYSTEM', 'SYSM0000', 'BLOCKED', 'system', 'system')
    ON CONFLICT (login_id) DO NOTHING; -- login_id 기준으로 체크하고 충돌이 날 경우 아무일도 하지않기