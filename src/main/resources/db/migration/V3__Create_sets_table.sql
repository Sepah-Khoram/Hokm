CREATE TABLE IF NOT EXISTS sets (
  set_id INT AUTO_INCREMENT PRIMARY KEY,
  game_token VARCHAR(255),
    set_number INT,
    ruler_player_id VARCHAR(255),
    rule VARCHAR(255),
    result VARCHAR(255),
    winner_team_id VARCHAR(255),
    FOREIGN KEY (game_token) REFERENCES Game(token)
    );
