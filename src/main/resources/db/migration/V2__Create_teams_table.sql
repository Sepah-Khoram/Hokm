CREATE TABLE IF NOT EXISTS teams (
    team_id VARCHAR(255) PRIMARY KEY,
    game_token VARCHAR(255),
    team_number INT,
    FOREIGN KEY (game_token) REFERENCES Game(token)
    );
