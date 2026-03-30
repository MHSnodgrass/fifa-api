#!/usr/bin/env python3
"""
FIFA World Cup 2026 — Database Setup & Seed Script
===================================================
Creates the database, tables, and inserts all seed data:
  - 48 teams with full 26-player squads
  - 104 matches (72 group stage + 32 knockout)
  - Dynamic dates: tournament starts tomorrow relative to runtime

Usage:
  pip install mysql-connector-python
  python setup_worldcup.py

Tested on MySQL 8.0+. Compatible with Windows / macOS / Linux.
"""

import mysql.connector as mysql
from datetime import date, timedelta, time
import json
import sys
import getpass

# ── Connection Config ────────────────────────────────────────
DB_NAME = "fifa_world_cup"
MYSQL_HOST = "localhost"
MYSQL_USER = "root"

# ── Date Anchoring ───────────────────────────────────────────
# Tournament starts tomorrow. Group stage runs ~16 days,
# knockout rounds fill the remaining ~14 days.
TOURNAMENT_START = date.today() + timedelta(days=1)

# ── DDL ──────────────────────────────────────────────────────
DDL_STATEMENTS = [
    f"DROP DATABASE IF EXISTS {DB_NAME}",
    f"CREATE DATABASE {DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
    f"USE {DB_NAME}",
    "SET time_zone = '+00:00'",
    """
    CREATE TABLE teams (
        id              INT UNSIGNED    NOT NULL AUTO_INCREMENT,
        country_name    VARCHAR(100)    NOT NULL,
        country_code    CHAR(3)         NOT NULL COMMENT 'FIFA country code',
        flag_url        VARCHAR(512)    DEFAULT NULL,
        logo_url        VARCHAR(512)    DEFAULT NULL,
        fifa_ranking    SMALLINT UNSIGNED DEFAULT NULL,
        group_letter    CHAR(1)         NOT NULL COMMENT 'Tournament group A-L',
        manager_name    VARCHAR(200)    DEFAULT NULL,
        squad           JSON            DEFAULT NULL,
        matches_played  TINYINT UNSIGNED NOT NULL DEFAULT 0,
        wins            TINYINT UNSIGNED NOT NULL DEFAULT 0,
        draws           TINYINT UNSIGNED NOT NULL DEFAULT 0,
        losses          TINYINT UNSIGNED NOT NULL DEFAULT 0,
        goals_for       SMALLINT UNSIGNED NOT NULL DEFAULT 0,
        goals_against   SMALLINT UNSIGNED NOT NULL DEFAULT 0,
        goal_difference SMALLINT         NOT NULL DEFAULT 0,
        group_points    TINYINT UNSIGNED NOT NULL DEFAULT 0,
        yellow_cards    SMALLINT UNSIGNED NOT NULL DEFAULT 0,
        red_cards       SMALLINT UNSIGNED NOT NULL DEFAULT 0,
        eliminated      BOOLEAN         NOT NULL DEFAULT FALSE,
        created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        PRIMARY KEY (id),
        UNIQUE KEY uq_country_code (country_code),
        UNIQUE KEY uq_country_name (country_name),
        INDEX idx_group (group_letter)
    ) ENGINE=InnoDB
    """,
    """
    CREATE TABLE events (
        id                    INT UNSIGNED    NOT NULL AUTO_INCREMENT,
        match_number          SMALLINT UNSIGNED NOT NULL COMMENT 'Official match number 1-104',
        stage                 ENUM('GROUP','ROUND_OF_32','ROUND_OF_16','QUARTERFINAL','SEMIFINAL','THIRD_PLACE','FINAL') NOT NULL,
        group_letter          CHAR(1)         DEFAULT NULL,
        home_team_id          INT UNSIGNED    DEFAULT NULL,
        away_team_id          INT UNSIGNED    DEFAULT NULL,
        home_team_placeholder VARCHAR(50)     DEFAULT NULL,
        away_team_placeholder VARCHAR(50)     DEFAULT NULL,
        match_date            DATE            NOT NULL,
        kickoff_time          TIME            DEFAULT NULL,
        kickoff_utc           DATETIME        DEFAULT NULL,
        arena_name            VARCHAR(200)    NOT NULL,
        city                  VARCHAR(100)    NOT NULL,
        status                ENUM('SCHEDULED','IN_PROGRESS','HALFTIME','FINISHED','POSTPONED','CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
        match_state           JSON            DEFAULT NULL,
        home_score            TINYINT UNSIGNED DEFAULT NULL,
        away_score            TINYINT UNSIGNED DEFAULT NULL,
        winner_team_id        INT UNSIGNED    DEFAULT NULL,
        is_draw               BOOLEAN         DEFAULT NULL,
        has_extra_time        BOOLEAN         NOT NULL DEFAULT FALSE,
        has_penalties         BOOLEAN         NOT NULL DEFAULT FALSE,
        created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        PRIMARY KEY (id),
        UNIQUE KEY uq_match_number (match_number),
        INDEX idx_stage (stage),
        INDEX idx_match_date (match_date),
        INDEX idx_status (status),
        INDEX idx_home_team (home_team_id),
        INDEX idx_away_team (away_team_id),
        CONSTRAINT fk_home_team  FOREIGN KEY (home_team_id)  REFERENCES teams(id) ON DELETE SET NULL ON UPDATE CASCADE,
        CONSTRAINT fk_away_team  FOREIGN KEY (away_team_id)  REFERENCES teams(id) ON DELETE SET NULL ON UPDATE CASCADE,
        CONSTRAINT fk_winner     FOREIGN KEY (winner_team_id) REFERENCES teams(id) ON DELETE SET NULL ON UPDATE CASCADE
    ) ENGINE=InnoDB
    """,
]

# ── Venues (actual 2026 host stadiums) ───────────────────────
VENUES = [
    ("MetLife Stadium", "East Rutherford"),
    ("AT&T Stadium", "Dallas"),
    ("Hard Rock Stadium", "Miami"),
    ("NRG Stadium", "Houston"),
    ("SoFi Stadium", "Los Angeles"),
    ("Lumen Field", "Seattle"),
    ("Gillette Stadium", "Foxborough"),
    ("Lincoln Financial Field", "Philadelphia"),
    ("Mercedes-Benz Stadium", "Atlanta"),
    ("Levi's Stadium", "Santa Clara"),
    ("BC Place", "Vancouver"),
    ("BMO Field", "Toronto"),
    ("Estadio Azteca", "Mexico City"),
    ("Estadio BBVA", "Monterrey"),
    ("Estadio Akron", "Guadalajara"),
    ("Arrowhead Stadium", "Kansas City"),
]

# ── 48 Teams ─────────────────────────────────────────────────
# (country_name, country_code, fifa_ranking, group, manager, squad)
# Squad: list of (name, number, position)
# Positions: GK, DF, MF, FW — standard 26-player roster
#   3 GK, 9 DF, 8 MF, 6 FW

def _squad(players):
    """Convert list of (name, number, position) into JSON-ready dicts."""
    return [
        {"name": p[0], "number": p[1], "position": p[2], "isCaptain": i == 0}
        for i, p in enumerate(players)
    ]

# fmt: off
TEAMS_DATA = [
    # ── GROUP A ──────────────────────────────────────────────
    ("United States", "USA", 11, "A", "Mauricio Pochettino", [
        ("Christian Pulisic", 10, "FW"), ("Weston McKennie", 8, "MF"), ("Tyler Adams", 4, "MF"),
        ("Gio Reyna", 7, "FW"), ("Tim Weah", 11, "FW"), ("Folarin Balogun", 9, "FW"),
        ("Yunus Musah", 6, "MF"), ("Brenden Aaronson", 14, "MF"), ("Antonee Robinson", 5, "DF"),
        ("Sergiño Dest", 2, "DF"), ("Chris Richards", 15, "DF"), ("Miles Robinson", 12, "DF"),
        ("Tim Ream", 13, "DF"), ("Joe Scally", 22, "DF"), ("Mark McKenzie", 3, "DF"),
        ("DeAndre Yedlin", 23, "DF"), ("Cameron Carter-Vickers", 20, "DF"), ("Johnny Cardoso", 16, "MF"),
        ("Malik Tillman", 17, "MF"), ("Luca de la Torre", 18, "MF"), ("Haji Wright", 19, "FW"),
        ("Josh Sargent", 21, "FW"), ("Matt Turner", 1, "GK"), ("Ethan Horvath", 24, "GK"),
        ("Zack Steffen", 25, "GK"), ("Aidan Morris", 26, "MF"),
    ]),
    ("Mexico", "MEX", 15, "A", "Javier Aguirre", [
        ("Hirving Lozano", 10, "FW"), ("Edson Álvarez", 4, "MF"), ("Raúl Jiménez", 9, "FW"),
        ("Diego Lainez", 7, "MF"), ("Jesús Corona", 17, "FW"), ("Alexis Vega", 11, "FW"),
        ("Luis Romo", 8, "MF"), ("Orbelín Pineda", 14, "MF"), ("César Montes", 3, "DF"),
        ("Jorge Sánchez", 2, "DF"), ("Gerardo Arteaga", 5, "DF"), ("Néstor Araújo", 15, "DF"),
        ("Héctor Moreno", 13, "DF"), ("Johan Vásquez", 12, "DF"), ("Jesús Gallardo", 23, "DF"),
        ("Kevin Álvarez", 22, "DF"), ("Luis Chávez", 6, "MF"), ("Carlos Rodríguez", 18, "MF"),
        ("Roberto Alvarado", 16, "MF"), ("Érick Gutiérrez", 20, "MF"), ("Santiago Giménez", 19, "FW"),
        ("Henry Martín", 21, "FW"), ("Guillermo Ochoa", 1, "GK"), ("David Ochoa", 24, "GK"),
        ("Luis Malagón", 25, "GK"), ("Marcelo Flores", 26, "MF"),
    ]),
    ("Colombia", "COL", 12, "A", "Néstor Lorenzo", [
        ("James Rodríguez", 10, "MF"), ("Luis Díaz", 7, "FW"), ("Jhon Arias", 17, "FW"),
        ("Rafael Santos Borré", 9, "FW"), ("Juan Quintero", 11, "MF"), ("Richard Ríos", 6, "MF"),
        ("Matheus Uribe", 8, "MF"), ("Jefferson Lerma", 14, "MF"), ("Davinson Sánchez", 3, "DF"),
        ("Yerry Mina", 13, "DF"), ("Johan Mojica", 5, "DF"), ("Daniel Muñoz", 2, "DF"),
        ("Carlos Cuesta", 15, "DF"), ("Jhon Lucumí", 12, "DF"), ("Deiver Machado", 22, "DF"),
        ("Andrés Llinás", 23, "DF"), ("Santiago Arias", 4, "DF"), ("Jorge Carrascal", 16, "MF"),
        ("Kevin Castaño", 20, "MF"), ("Juan Camilo Portilla", 18, "MF"), ("Miguel Borja", 19, "FW"),
        ("Jhon Córdoba", 21, "FW"), ("David Ospina", 1, "GK"), ("Camilo Vargas", 24, "GK"),
        ("Álvaro Montero", 25, "GK"), ("Yáser Asprilla", 26, "FW"),
    ]),
    ("Guinea", "GUI", 81, "A", "Kaba Diawara", [
        ("Naby Keïta", 8, "MF"), ("Serhou Guirassy", 9, "FW"), ("Mohamed Bayo", 10, "FW"),
        ("Ilaix Moriba", 6, "MF"), ("José Kanté", 7, "FW"), ("Amadou Diawara", 4, "MF"),
        ("Mady Camara", 14, "MF"), ("Aguibou Camara", 11, "MF"), ("Issiaga Sylla", 3, "DF"),
        ("Ibrahima Conté", 5, "DF"), ("Saïdou Sow", 13, "DF"), ("Florentin Pogba", 15, "DF"),
        ("Ousmane Kanté", 2, "DF"), ("Mamadou Kane", 12, "DF"), ("Mohamed Camara", 22, "DF"),
        ("Saidou Alioum", 23, "DF"), ("Abdoulaye Touré", 16, "MF"), ("Moriba Kourouma", 17, "MF"),
        ("Mamadou Kanté", 20, "MF"), ("Fodé Camara", 18, "MF"), ("Morgan Guilavogui", 19, "FW"),
        ("Ousmane Diao", 21, "FW"), ("Aly Keita", 1, "GK"), ("Ibrahim Koné", 24, "GK"),
        ("Moussa Camara", 25, "GK"), ("Simon Falette", 26, "DF"),
    ]),
    # ── GROUP B ──────────────────────────────────────────────
    ("Argentina", "ARG", 1, "B", "Lionel Scaloni", [
        ("Lionel Messi", 10, "FW"), ("Ángel Di María", 11, "FW"), ("Julián Álvarez", 9, "FW"),
        ("Lautaro Martínez", 22, "FW"), ("Paulo Dybala", 21, "FW"), ("Alejandro Garnacho", 17, "FW"),
        ("Rodrigo De Paul", 7, "MF"), ("Enzo Fernández", 24, "MF"), ("Leandro Paredes", 5, "MF"),
        ("Alexis Mac Allister", 20, "MF"), ("Giovani Lo Celso", 18, "MF"), ("Exequiel Palacios", 14, "MF"),
        ("Nicolás Tagliafico", 3, "DF"), ("Cristian Romero", 13, "DF"), ("Nicolás Otamendi", 19, "DF"),
        ("Lisandro Martínez", 25, "DF"), ("Marcos Acuña", 8, "DF"), ("Gonzalo Montiel", 4, "DF"),
        ("Nahuel Molina", 26, "DF"), ("Germán Pezzella", 6, "DF"), ("Juan Foyth", 2, "DF"),
        ("Thiago Almada", 16, "MF"), ("Nicolás González", 15, "FW"), ("Emiliano Martínez", 23, "GK"),
        ("Franco Armani", 1, "GK"), ("Gerónimo Rulli", 12, "GK"),
    ]),
    ("Japan", "JPN", 17, "B", "Hajime Moriyasu", [
        ("Takefusa Kubo", 10, "FW"), ("Kaoru Mitoma", 7, "FW"), ("Daichi Kamada", 8, "MF"),
        ("Wataru Endo", 6, "MF"), ("Ritsu Doan", 11, "FW"), ("Junya Ito", 14, "FW"),
        ("Hidemasa Morita", 4, "MF"), ("Ao Tanaka", 17, "MF"), ("Ko Itakura", 3, "DF"),
        ("Takehiro Tomiyasu", 2, "DF"), ("Maya Yoshida", 22, "DF"), ("Shogo Taniguchi", 5, "DF"),
        ("Hiroki Ito", 15, "DF"), ("Yuto Nagatomo", 13, "DF"), ("Miki Yamane", 23, "DF"),
        ("Hiroki Sakai", 19, "DF"), ("Yukinari Sugawara", 20, "DF"), ("Keito Nakamura", 16, "MF"),
        ("Gaku Shibasaki", 18, "MF"), ("Takuma Asano", 9, "FW"), ("Kyogo Furuhashi", 21, "FW"),
        ("Ayase Ueda", 15, "FW"), ("Shuichi Gonda", 1, "GK"), ("Daniel Schmidt", 24, "GK"),
        ("Eiji Kawashima", 25, "GK"), ("Yuki Soma", 26, "MF"),
    ]),
    ("Peru", "PER", 34, "B", "Jorge Fossati", [
        ("Paolo Guerrero", 9, "FW"), ("André Carrillo", 10, "FW"), ("Christian Cueva", 8, "MF"),
        ("Renato Tapia", 6, "MF"), ("Yoshimar Yotún", 14, "MF"), ("Sergio Peña", 7, "MF"),
        ("Pedro Aquino", 4, "MF"), ("Edison Flores", 11, "FW"), ("Luis Advíncula", 17, "DF"),
        ("Alexander Callens", 3, "DF"), ("Carlos Zambrano", 5, "DF"), ("Miguel Araujo", 15, "DF"),
        ("Aldo Corzo", 2, "DF"), ("Luis Abram", 13, "DF"), ("Anderson Santamaría", 12, "DF"),
        ("Marcos López", 22, "DF"), ("Miguel Trauco", 23, "DF"), ("Wilder Cartagena", 16, "MF"),
        ("Jesús Castillo", 20, "MF"), ("Piero Quispe", 18, "MF"), ("Gianluca Lapadula", 19, "FW"),
        ("Bryan Reyna", 21, "FW"), ("Pedro Gallese", 1, "GK"), ("Carlos Cáceda", 24, "GK"),
        ("Diego Romero", 25, "GK"), ("Joao Grimaldo", 26, "FW"),
    ]),
    ("Senegal", "SEN", 20, "B", "Aliou Cissé", [
        ("Sadio Mané", 10, "FW"), ("Ismaïla Sarr", 7, "FW"), ("Boulaye Dia", 9, "FW"),
        ("Iliman Ndiaye", 17, "FW"), ("Nicolas Jackson", 11, "FW"), ("Krépin Diatta", 18, "FW"),
        ("Idrissa Gueye", 6, "MF"), ("Nampalys Mendy", 14, "MF"), ("Cheikhou Kouyaté", 8, "MF"),
        ("Pape Matar Sarr", 4, "MF"), ("Pathé Ciss", 16, "MF"), ("Mamadou Loum", 20, "MF"),
        ("Kalidou Koulibaly", 3, "DF"), ("Abdou Diallo", 13, "DF"), ("Youssouf Sabaly", 2, "DF"),
        ("Pape Abou Cissé", 5, "DF"), ("Formose Mendy", 22, "DF"), ("Moussa Niakhaté", 15, "DF"),
        ("Ismail Jakobs", 23, "DF"), ("Fode Ballo-Touré", 12, "DF"), ("Abdoulaye Seck", 19, "DF"),
        ("Habib Diarra", 21, "MF"), ("Edouard Mendy", 1, "GK"), ("Alfred Gomis", 24, "GK"),
        ("Seny Dieng", 25, "GK"), ("Lamine Camara", 26, "MF"),
    ]),
    # ── GROUP C ──────────────────────────────────────────────
    ("France", "FRA", 2, "C", "Didier Deschamps", [
        ("Kylian Mbappé", 10, "FW"), ("Antoine Griezmann", 7, "FW"), ("Olivier Giroud", 9, "FW"),
        ("Ousmane Dembélé", 11, "FW"), ("Marcus Thuram", 15, "FW"), ("Randal Kolo Muani", 12, "FW"),
        ("Aurélien Tchouaméni", 8, "MF"), ("Eduardo Camavinga", 6, "MF"), ("Adrien Rabiot", 14, "MF"),
        ("N'Golo Kanté", 13, "MF"), ("Youssouf Fofana", 19, "MF"), ("Warren Zaïre-Emery", 18, "MF"),
        ("Jules Koundé", 5, "DF"), ("Dayot Upamecano", 4, "DF"), ("William Saliba", 17, "DF"),
        ("Ibrahima Konaté", 3, "DF"), ("Theo Hernández", 22, "DF"), ("Ferland Mendy", 23, "DF"),
        ("Benjamin Pavard", 2, "DF"), ("Jonathan Clauss", 24, "DF"), ("Axel Disasi", 20, "DF"),
        ("Mattéo Guendouzi", 16, "MF"), ("Kingsley Coman", 21, "FW"), ("Hugo Lloris", 1, "GK"),
        ("Mike Maignan", 25, "GK"), ("Alphonse Areola", 26, "GK"),
    ]),
    ("Australia", "AUS", 25, "C", "Graham Arnold", [
        ("Mathew Leckie", 10, "FW"), ("Martin Boyle", 7, "FW"), ("Mitchell Duke", 9, "FW"),
        ("Jamie Maclaren", 11, "FW"), ("Craig Goodwin", 17, "FW"), ("Awer Mabil", 22, "FW"),
        ("Aaron Mooy", 8, "MF"), ("Ajdin Hrustić", 6, "MF"), ("Jackson Irvine", 14, "MF"),
        ("Riley McGree", 4, "MF"), ("Keanu Baccus", 18, "MF"), ("Cameron Devlin", 20, "MF"),
        ("Harry Souttar", 3, "DF"), ("Kye Rowles", 5, "DF"), ("Aziz Behich", 13, "DF"),
        ("Miloš Degenek", 2, "DF"), ("Bailey Wright", 15, "DF"), ("Nathaniel Atkinson", 12, "DF"),
        ("Fran Karačić", 19, "DF"), ("Joel King", 23, "DF"), ("Thomas Deng", 16, "DF"),
        ("Connor Metcalfe", 21, "MF"), ("Mat Ryan", 1, "GK"), ("Andrew Redmayne", 24, "GK"),
        ("Danny Vukovic", 25, "GK"), ("Garang Kuol", 26, "FW"),
    ]),
    ("South Korea", "KOR", 22, "C", "Hong Myung-bo", [
        ("Son Heung-min", 7, "FW"), ("Lee Kang-in", 10, "MF"), ("Hwang Hee-chan", 11, "FW"),
        ("Cho Gue-sung", 9, "FW"), ("Hwang Ui-jo", 17, "FW"), ("Song Min-kyu", 22, "FW"),
        ("Jung Woo-young", 6, "MF"), ("Lee Jae-sung", 8, "MF"), ("Paik Seung-ho", 14, "MF"),
        ("Kwon Chang-hoon", 18, "MF"), ("Na Sang-ho", 16, "MF"), ("Jeong Woo-yeong", 20, "MF"),
        ("Kim Min-jae", 3, "DF"), ("Kim Young-gwon", 4, "DF"), ("Kim Jin-su", 5, "DF"),
        ("Cho Yu-min", 15, "DF"), ("Hong Chul", 13, "DF"), ("Yoon Jong-gyu", 2, "DF"),
        ("Kim Tae-hwan", 23, "DF"), ("Park Ji-su", 12, "DF"), ("Lee Ki-je", 19, "DF"),
        ("Son Jun-ho", 21, "MF"), ("Kim Seung-gyu", 1, "GK"), ("Jo Hyeon-woo", 24, "GK"),
        ("Song Bum-keun", 25, "GK"), ("Oh Hyeon-gyu", 26, "FW"),
    ]),
    ("Saudi Arabia", "KSA", 56, "C", "Roberto Mancini", [
        ("Salem Al-Dawsari", 10, "FW"), ("Firas Al-Buraikan", 9, "FW"), ("Saud Abdulhamid", 2, "DF"),
        ("Salman Al-Faraj", 7, "MF"), ("Abdulellah Al-Malki", 6, "MF"), ("Mohamed Kanno", 8, "MF"),
        ("Nasser Al-Dawsari", 14, "MF"), ("Hattan Bahebri", 11, "FW"), ("Saleh Al-Shehri", 17, "FW"),
        ("Abdullah Madu", 3, "DF"), ("Ali Al-Bulayhi", 5, "DF"), ("Abdulelah Al-Amri", 13, "DF"),
        ("Hassan Tambakti", 15, "DF"), ("Yasser Al-Shahrani", 4, "DF"), ("Sultan Al-Ghanam", 22, "DF"),
        ("Ahmed Bamsaud", 23, "DF"), ("Mohammed Al-Burayk", 12, "DF"), ("Ali Al-Asmari", 16, "MF"),
        ("Sami Al-Najei", 18, "MF"), ("Abdulrahman Ghareeb", 20, "MF"), ("Abdullah Al-Hamdan", 19, "FW"),
        ("Ayman Yahya", 21, "FW"), ("Mohammed Al-Owais", 1, "GK"), ("Mohammed Al-Yami", 24, "GK"),
        ("Nawaf Al-Aqidi", 25, "GK"), ("Riyadh Sharahili", 26, "DF"),
    ]),
    # ── GROUP D ──────────────────────────────────────────────
    ("Brazil", "BRA", 3, "D", "Dorival Júnior", [
        ("Neymar Jr", 10, "FW"), ("Vinícius Jr", 7, "FW"), ("Rodrygo", 11, "FW"),
        ("Richarlison", 9, "FW"), ("Endrick", 21, "FW"), ("Raphinha", 19, "FW"),
        ("Casemiro", 5, "MF"), ("Lucas Paquetá", 8, "MF"), ("Bruno Guimarães", 6, "MF"),
        ("Fred", 14, "MF"), ("André", 18, "MF"), ("João Gomes", 16, "MF"),
        ("Marquinhos", 4, "DF"), ("Thiago Silva", 3, "DF"), ("Éder Militão", 13, "DF"),
        ("Gabriel Magalhães", 15, "DF"), ("Danilo", 2, "DF"), ("Alex Sandro", 22, "DF"),
        ("Guilherme Arana", 23, "DF"), ("Bremer", 12, "DF"), ("Wendell", 17, "DF"),
        ("Martinelli", 20, "FW"), ("Alisson", 1, "GK"), ("Ederson", 24, "GK"),
        ("Bento", 25, "GK"), ("Savinho", 26, "FW"),
    ]),
    ("Cameroon", "CMR", 43, "D", "Rigobert Song", [
        ("Vincent Aboubakar", 10, "FW"), ("Eric Maxim Choupo-Moting", 9, "FW"), ("Bryan Mbeumo", 7, "FW"),
        ("Karl Toko Ekambi", 11, "FW"), ("Georges-Kévin N'Koudou", 17, "FW"), ("Léandre Tawamba", 21, "FW"),
        ("André-Frank Zambo Anguissa", 8, "MF"), ("Pierre Kunde", 6, "MF"), ("Martin Hongla", 14, "MF"),
        ("Samuel Gouet", 4, "MF"), ("Olivier Ntcham", 18, "MF"), ("James Léa Siliki", 16, "MF"),
        ("Nicolas N'Koulou", 3, "DF"), ("Michael Ngadeu", 13, "DF"), ("Collins Fai", 2, "DF"),
        ("Jean-Charles Castelletto", 5, "DF"), ("Nouhou Tolo", 15, "DF"), ("Olivier Mbaizo", 22, "DF"),
        ("Enzo Ebosse", 23, "DF"), ("Christopher Wooh", 12, "DF"), ("Duplexe Bangna", 19, "DF"),
        ("Moumi Ngamaleu", 20, "MF"), ("André Onana", 1, "GK"), ("Devis Epassy", 24, "GK"),
        ("Simon Ngapandouetnbu", 25, "GK"), ("Carlos Baleba", 26, "MF"),
    ]),
    ("Ecuador", "ECU", 30, "D", "Sebastián Beccacece", [
        ("Enner Valencia", 10, "FW"), ("Gonzalo Plata", 7, "FW"), ("Michael Estrada", 9, "FW"),
        ("Ángel Mena", 11, "FW"), ("Kevin Rodríguez", 17, "FW"), ("Jeremy Sarmiento", 21, "FW"),
        ("Moisés Caicedo", 8, "MF"), ("Carlos Gruezo", 6, "MF"), ("Alan Franco", 14, "MF"),
        ("Jhegson Méndez", 4, "MF"), ("Romario Ibarra", 18, "MF"), ("Kendry Páez", 16, "MF"),
        ("Piero Hincapié", 3, "DF"), ("Pervis Estupiñán", 5, "DF"), ("Félix Torres", 13, "DF"),
        ("Robert Arboleda", 2, "DF"), ("Jackson Porozo", 15, "DF"), ("Diego Palacios", 22, "DF"),
        ("Angelo Preciado", 23, "DF"), ("Xavier Arreaga", 12, "DF"), ("William Pacho", 19, "DF"),
        ("José Cifuentes", 20, "MF"), ("Hernán Galíndez", 1, "GK"), ("Alexander Domínguez", 24, "GK"),
        ("Moisés Ramírez", 25, "GK"), ("John Yeboah", 26, "FW"),
    ]),
    ("Serbia", "SRB", 33, "D", "Dragan Stojković", [
        ("Dušan Tadić", 10, "MF"), ("Aleksandar Mitrović", 9, "FW"), ("Dušan Vlahović", 7, "FW"),
        ("Filip Kostić", 11, "FW"), ("Luka Jović", 17, "FW"), ("Andrija Živković", 21, "FW"),
        ("Sergej Milinković-Savić", 20, "MF"), ("Nemanja Gudelj", 6, "MF"), ("Saša Lukić", 14, "MF"),
        ("Ivan Ilić", 8, "MF"), ("Nemanja Maksimović", 18, "MF"), ("Marko Grujić", 16, "MF"),
        ("Nikola Milenković", 4, "DF"), ("Strahinja Pavlović", 3, "DF"), ("Miloš Veljković", 5, "DF"),
        ("Filip Mladenović", 13, "DF"), ("Strahinja Eraković", 15, "DF"), ("Nikola Milenkovic", 2, "DF"),
        ("Srđan Babić", 22, "DF"), ("Uroš Spajić", 23, "DF"), ("Erhan Mašović", 12, "DF"),
        ("Srdjan Mijailović", 19, "MF"), ("Predrag Rajković", 1, "GK"), ("Vanja Milinković-Savić", 24, "GK"),
        ("Marko Dmitrović", 25, "GK"), ("Veljko Birmančević", 26, "FW"),
    ]),
    # ── GROUP E ──────────────────────────────────────────────
    ("England", "ENG", 4, "E", "Thomas Tuchel", [
        ("Harry Kane", 9, "FW"), ("Jude Bellingham", 10, "MF"), ("Bukayo Saka", 7, "FW"),
        ("Phil Foden", 11, "FW"), ("Raheem Sterling", 17, "FW"), ("Marcus Rashford", 21, "FW"),
        ("Declan Rice", 4, "MF"), ("Mason Mount", 8, "MF"), ("Jordan Henderson", 14, "MF"),
        ("Kalvin Phillips", 6, "MF"), ("Conor Gallagher", 18, "MF"), ("Kobbie Mainoo", 16, "MF"),
        ("John Stones", 5, "DF"), ("Harry Maguire", 15, "DF"), ("Kyle Walker", 2, "DF"),
        ("Luke Shaw", 3, "DF"), ("Marc Guéhi", 13, "DF"), ("Kieran Trippier", 12, "DF"),
        ("Trent Alexander-Arnold", 22, "DF"), ("Ben White", 23, "DF"), ("Levi Colwill", 19, "DF"),
        ("Cole Palmer", 20, "FW"), ("Jordan Pickford", 1, "GK"), ("Aaron Ramsdale", 24, "GK"),
        ("Dean Henderson", 25, "GK"), ("Eberechi Eze", 26, "MF"),
    ]),
    ("Nigeria", "NGA", 28, "E", "Finidi George", [
        ("Victor Osimhen", 9, "FW"), ("Samuel Chukwueze", 7, "FW"), ("Alex Iwobi", 10, "MF"),
        ("Moses Simon", 11, "FW"), ("Ademola Lookman", 17, "FW"), ("Paul Onuachu", 21, "FW"),
        ("Wilfred Ndidi", 4, "MF"), ("Joe Aribo", 8, "MF"), ("Oghenekaro Etebo", 6, "MF"),
        ("Frank Onyeka", 14, "MF"), ("Raphael Onyedika", 18, "MF"), ("Fisayo Dele-Bashiru", 16, "MF"),
        ("William Ekong", 5, "DF"), ("Calvin Bassey", 3, "DF"), ("Ola Aina", 2, "DF"),
        ("Semi Ajayi", 13, "DF"), ("Bright Osayi-Samuel", 15, "DF"), ("Zaidu Sanusi", 22, "DF"),
        ("Chidozie Awaziem", 23, "DF"), ("Kenneth Omeruo", 12, "DF"), ("Olaoluwa Aina", 19, "DF"),
        ("Kelechi Iheanacho", 20, "FW"), ("Francis Uzoho", 1, "GK"), ("Maduka Okoye", 24, "GK"),
        ("Daniel Akpeyi", 25, "GK"), ("Taiwo Awoniyi", 26, "FW"),
    ]),
    ("Iran", "IRN", 21, "E", "Amir Ghalenoei", [
        ("Mehdi Taremi", 9, "FW"), ("Sardar Azmoun", 10, "FW"), ("Alireza Jahanbakhsh", 7, "FW"),
        ("Karim Ansarifard", 11, "FW"), ("Allahyar Sayyadmanesh", 17, "FW"), ("Shahab Zahedi", 21, "FW"),
        ("Saeid Ezatolahi", 6, "MF"), ("Ahmad Nourollahi", 8, "MF"), ("Saman Ghoddos", 14, "MF"),
        ("Milad Sarlak", 4, "MF"), ("Ali Gholizadeh", 18, "MF"), ("Omid Ebrahimi", 16, "MF"),
        ("Morteza Pouraliganji", 5, "DF"), ("Shojae Khalilzadeh", 3, "DF"), ("Ramin Rezaeian", 2, "DF"),
        ("Majid Hosseini", 13, "DF"), ("Sadegh Moharrami", 15, "DF"), ("Ehsan Hajsafi", 22, "DF"),
        ("Abolfazl Jalali", 23, "DF"), ("Hossein Kanaani", 12, "DF"), ("Milad Mohammadi", 19, "DF"),
        ("Vahid Amiri", 20, "MF"), ("Alireza Beiranvand", 1, "GK"), ("Amir Abedzadeh", 24, "GK"),
        ("Payam Niazmand", 25, "GK"), ("Mehdi Ghayedi", 26, "FW"),
    ]),
    ("Chile", "CHI", 35, "E", "Ricardo Gareca", [
        ("Alexis Sánchez", 7, "FW"), ("Arturo Vidal", 8, "MF"), ("Eduardo Vargas", 9, "FW"),
        ("Charles Aránguiz", 10, "MF"), ("Ben Brereton Díaz", 11, "FW"), ("Darío Osorio", 17, "FW"),
        ("Erick Pulgar", 6, "MF"), ("Diego Valdés", 14, "MF"), ("Marcelino Núñez", 4, "MF"),
        ("Víctor Dávila", 18, "MF"), ("Rodrigo Echeverría", 16, "MF"), ("Williams Alarcón", 20, "MF"),
        ("Gary Medel", 3, "DF"), ("Guillermo Maripán", 5, "DF"), ("Mauricio Isla", 2, "DF"),
        ("Paulo Díaz", 13, "DF"), ("Gabriel Suazo", 15, "DF"), ("Matías Catalán", 22, "DF"),
        ("Thomas Galdames", 23, "DF"), ("Benjamín Kuscevic", 12, "DF"), ("Igor Lichnovsky", 19, "DF"),
        ("Felipe Mora", 21, "FW"), ("Claudio Bravo", 1, "GK"), ("Gabriel Arias", 24, "GK"),
        ("Brayan Cortés", 25, "GK"), ("Alexander Aravena", 26, "FW"),
    ]),
    # ── GROUP F ──────────────────────────────────────────────
    ("Germany", "GER", 5, "F", "Julian Nagelsmann", [
        ("Jamal Musiala", 10, "MF"), ("Florian Wirtz", 17, "MF"), ("Kai Havertz", 7, "FW"),
        ("Leroy Sané", 11, "FW"), ("Niclas Füllkrug", 9, "FW"), ("Serge Gnabry", 13, "FW"),
        ("İlkay Gündoğan", 8, "MF"), ("Toni Kroos", 6, "MF"), ("Joshua Kimmich", 14, "MF"),
        ("Leon Goretzka", 18, "MF"), ("Robert Andrich", 16, "MF"), ("Chris Führich", 20, "MF"),
        ("Antonio Rüdiger", 4, "DF"), ("Jonathan Tah", 5, "DF"), ("Nico Schlotterbeck", 3, "DF"),
        ("David Raum", 22, "DF"), ("Benjamin Henrichs", 2, "DF"), ("Robin Koch", 15, "DF"),
        ("Waldemar Anton", 23, "DF"), ("Maximilian Mittelstädt", 12, "DF"), ("Matthijs de Ligt", 19, "DF"),
        ("Thomas Müller", 21, "FW"), ("Manuel Neuer", 1, "GK"), ("Marc-André ter Stegen", 24, "GK"),
        ("Oliver Baumann", 25, "GK"), ("Aleksandar Pavlović", 26, "MF"),
    ]),
    ("Morocco", "MAR", 13, "F", "Walid Regragui", [
        ("Hakim Ziyech", 7, "FW"), ("Youssef En-Nesyri", 9, "FW"), ("Sofiane Boufal", 10, "MF"),
        ("Achraf Hakimi", 2, "DF"), ("Sofyan Amrabat", 4, "MF"), ("Azzedine Ounahi", 8, "MF"),
        ("Bilal El Khannouss", 14, "MF"), ("Selim Amallah", 18, "MF"), ("Abdelhamid Sabiri", 16, "MF"),
        ("Ibrahim Díaz", 11, "FW"), ("Abde Ezzalzouli", 17, "FW"), ("Ayoub El Kaabi", 21, "FW"),
        ("Nayef Aguerd", 5, "DF"), ("Romain Saïss", 3, "DF"), ("Noussair Mazraoui", 13, "DF"),
        ("Jawad El Yamiq", 15, "DF"), ("Achraf Dari", 22, "DF"), ("Adam Masina", 23, "DF"),
        ("Badr Benoun", 12, "DF"), ("Yahia Attiat-Allah", 19, "DF"), ("Munir Mohamedi", 6, "MF"),
        ("Ilias Akhomach", 20, "FW"), ("Yassine Bounou", 1, "GK"), ("Munir Mohamedi", 24, "GK"),
        ("Anas Zniti", 25, "GK"), ("Eliesse Ben Seghir", 26, "MF"),
    ]),
    ("Uruguay", "URU", 14, "F", "Marcelo Bielsa", [
        ("Luis Suárez", 9, "FW"), ("Darwin Núñez", 11, "FW"), ("Federico Valverde", 8, "MF"),
        ("Facundo Pellistri", 7, "FW"), ("Nicolás de la Cruz", 10, "MF"), ("Agustín Canobbio", 17, "FW"),
        ("Rodrigo Bentancur", 6, "MF"), ("Manuel Ugarte", 14, "MF"), ("Giorgian de Arrascaeta", 4, "MF"),
        ("Matías Vecino", 18, "MF"), ("Nicolás Fonseca", 16, "MF"), ("Facundo Torres", 20, "FW"),
        ("José Giménez", 3, "DF"), ("Diego Godín", 13, "DF"), ("Ronald Araújo", 5, "DF"),
        ("Sebastián Coates", 15, "DF"), ("Mathías Olivera", 22, "DF"), ("Nahitan Nández", 2, "DF"),
        ("Guillermo Varela", 23, "DF"), ("Sebastián Cáceres", 12, "DF"), ("Matías Viña", 19, "DF"),
        ("Maximiliano Gómez", 21, "FW"), ("Fernando Muslera", 1, "GK"), ("Sergio Rochet", 24, "GK"),
        ("Santiago Mele", 25, "GK"), ("Luciano Rodríguez", 26, "FW"),
    ]),
    ("Canada", "CAN", 40, "F", "Jesse Marsch", [
        ("Alphonso Davies", 10, "DF"), ("Jonathan David", 9, "FW"), ("Cyle Larin", 7, "FW"),
        ("Tajon Buchanan", 11, "FW"), ("Jonathan Osorio", 8, "MF"), ("Stephen Eustáquio", 6, "MF"),
        ("Atiba Hutchinson", 14, "MF"), ("Mark-Anthony Kaye", 4, "MF"), ("Ismael Koné", 18, "MF"),
        ("David Wotherspoon", 16, "MF"), ("Liam Fraser", 20, "MF"), ("Ali Ahmed", 26, "MF"),
        ("Kamal Miller", 3, "DF"), ("Steven Vitória", 5, "DF"), ("Alistair Johnston", 2, "DF"),
        ("Derek Cornelius", 13, "DF"), ("Sam Adekugbe", 15, "DF"), ("Richie Laryea", 22, "DF"),
        ("Doneil Henry", 23, "DF"), ("Scott Kennedy", 12, "DF"), ("Lukas MacNaughton", 19, "DF"),
        ("Jacob Shaffelburg", 17, "FW"), ("Ike Ugbo", 21, "FW"), ("Milan Borjan", 1, "GK"),
        ("Maxime Crépeau", 24, "GK"), ("Dayne St. Clair", 25, "GK"),
    ]),
    # ── GROUP G ──────────────────────────────────────────────
    ("Spain", "ESP", 6, "G", "Luis de la Fuente", [
        ("Pedri", 10, "MF"), ("Lamine Yamal", 17, "FW"), ("Álvaro Morata", 7, "FW"),
        ("Dani Olmo", 11, "MF"), ("Ferran Torres", 9, "FW"), ("Nico Williams", 21, "FW"),
        ("Gavi", 6, "MF"), ("Rodri", 8, "MF"), ("Fabián Ruiz", 14, "MF"),
        ("Marcos Llorente", 18, "MF"), ("Mikel Merino", 16, "MF"), ("Alejandro Baena", 20, "MF"),
        ("Aymeric Laporte", 4, "DF"), ("Robin Le Normand", 3, "DF"), ("Dani Carvajal", 2, "DF"),
        ("Marc Cucurella", 5, "DF"), ("Pau Torres", 13, "DF"), ("César Azpilicueta", 15, "DF"),
        ("Alejandro Grimaldo", 22, "DF"), ("Jesús Navas", 23, "DF"), ("Nacho Fernández", 12, "DF"),
        ("Mikel Oyarzabal", 19, "FW"), ("Unai Simón", 1, "GK"), ("David Raya", 24, "GK"),
        ("Robert Sánchez", 25, "GK"), ("Álex Remiro", 26, "GK"),
    ]),
    ("Ghana", "GHA", 60, "G", "Otto Addo", [
        ("Mohammed Kudus", 10, "MF"), ("Jordan Ayew", 9, "FW"), ("André Ayew", 7, "MF"),
        ("Thomas Partey", 5, "MF"), ("Inaki Williams", 11, "FW"), ("Antoine Semenyo", 17, "FW"),
        ("Mohammed Salisu", 3, "DF"), ("Daniel Amartey", 4, "DF"), ("Abdul-Rahman Baba", 6, "DF"),
        ("Tariq Lamptey", 2, "DF"), ("Alexander Djiku", 15, "DF"), ("Gideon Mensah", 13, "DF"),
        ("Denis Odoi", 22, "DF"), ("Joseph Aidoo", 23, "DF"), ("Alidu Seidu", 12, "DF"),
        ("Elisha Owusu", 14, "MF"), ("Salis Abdul Samed", 8, "MF"), ("Ibrahim Sulemana", 16, "MF"),
        ("Osman Bukari", 18, "FW"), ("Ernest Nuamah", 20, "FW"), ("Ransford-Yeboah Königsdörffer", 21, "FW"),
        ("Fatawu Issahaku", 19, "FW"), ("Lawrence Ati-Zigi", 1, "GK"), ("Richard Ofori", 24, "GK"),
        ("Abdul Manaf Nurudeen", 25, "GK"), ("Edmund Addo", 26, "MF"),
    ]),
    ("Paraguay", "PAR", 50, "G", "Alfredo Moreno", [
        ("Miguel Almirón", 10, "FW"), ("Julio Enciso", 7, "FW"), ("Ángel Romero", 9, "FW"),
        ("Antonio Sanabria", 11, "FW"), ("Gabriel Ávalos", 17, "FW"), ("Adam Bareiro", 21, "FW"),
        ("Andrés Cubas", 6, "MF"), ("Mathías Villasanti", 8, "MF"), ("Óscar Romero", 14, "MF"),
        ("Hernesto Caballero", 4, "MF"), ("Damián Bobadilla", 18, "MF"), ("Robert Morales", 16, "MF"),
        ("Gustavo Gómez", 3, "DF"), ("Omar Alderete", 5, "DF"), ("Junior Alonso", 13, "DF"),
        ("Fabián Balbuena", 15, "DF"), ("Alberto Espínola", 2, "DF"), ("Santiago Arzamendia", 22, "DF"),
        ("Juan Escobar", 23, "DF"), ("Matías Rojas", 12, "DF"), ("Blas Riveros", 19, "DF"),
        ("Alejandro Romero Gamarra", 20, "MF"), ("Antony Silva", 1, "GK"), ("Roberto Fernández", 24, "GK"),
        ("Alfredo Aguilar", 25, "GK"), ("Diego Gómez", 26, "MF"),
    ]),
    ("New Zealand", "NZL", 93, "G", "Darren Bazeley", [
        ("Chris Wood", 9, "FW"), ("Sarpreet Singh", 10, "MF"), ("Kosta Barbarouses", 7, "FW"),
        ("Matt Garbett", 8, "MF"), ("Marko Stamenic", 6, "MF"), ("Joe Bell", 14, "MF"),
        ("Clayton Lewis", 4, "MF"), ("Ben Waine", 11, "FW"), ("Elijah Just", 17, "FW"),
        ("Alex Greive", 21, "FW"), ("Logan Rogerson", 18, "FW"), ("Callum McCowatt", 16, "MF"),
        ("Winston Reid", 3, "DF"), ("Michael Boxall", 5, "DF"), ("Tim Payne", 2, "DF"),
        ("Nando Pijnaker", 13, "DF"), ("Bill Tuiloma", 15, "DF"), ("Liberato Cacace", 22, "DF"),
        ("Storm Roux", 23, "DF"), ("Dane Ingham", 12, "DF"), ("Francis de Vries", 19, "DF"),
        ("Tommy Smith", 20, "DF"), ("Stefan Marinovic", 1, "GK"), ("Oliver Sail", 24, "GK"),
        ("Alex Paulsen", 25, "GK"), ("Jesse Randall", 26, "MF"),
    ]),
    # ── GROUP H ──────────────────────────────────────────────
    ("Portugal", "POR", 7, "H", "Roberto Martínez", [
        ("Cristiano Ronaldo", 7, "FW"), ("Bruno Fernandes", 8, "MF"), ("Bernardo Silva", 10, "MF"),
        ("Rafael Leão", 17, "FW"), ("Gonçalo Ramos", 9, "FW"), ("Pedro Neto", 21, "FW"),
        ("Vitinha", 11, "MF"), ("João Palhinha", 6, "MF"), ("Rúben Neves", 14, "MF"),
        ("Otávio", 18, "MF"), ("João Mário", 16, "MF"), ("Matheus Nunes", 20, "MF"),
        ("Rúben Dias", 4, "DF"), ("Pepe", 3, "DF"), ("João Cancelo", 2, "DF"),
        ("Nuno Mendes", 5, "DF"), ("Gonçalo Inácio", 13, "DF"), ("Diogo Dalot", 15, "DF"),
        ("António Silva", 22, "DF"), ("Nélson Semedo", 23, "DF"), ("Danilo Pereira", 12, "DF"),
        ("Francisco Conceição", 19, "FW"), ("Diogo Costa", 1, "GK"), ("Rui Patrício", 24, "GK"),
        ("José Sá", 25, "GK"), ("Diogo Jota", 26, "FW"),
    ]),
    ("Jamaica", "JAM", 64, "H", "Heimir Hallgrímsson", [
        ("Michail Antonio", 9, "FW"), ("Leon Bailey", 7, "FW"), ("Bobby Reid", 10, "MF"),
        ("Shamar Nicholson", 11, "FW"), ("Demarai Gray", 17, "FW"), ("Cory Burke", 21, "FW"),
        ("Ravel Morrison", 8, "MF"), ("Je-Vaughn Watson", 6, "MF"), ("Daniel Johnson", 14, "MF"),
        ("Andre Blake", 4, "MF"), ("Devon Williams", 18, "MF"), ("Kevon Lambert", 16, "MF"),
        ("Damion Lowe", 3, "DF"), ("Liam Moore", 5, "DF"), ("Kemar Lawrence", 13, "DF"),
        ("Adrian Mariappa", 2, "DF"), ("Ethan Pinnock", 15, "DF"), ("Di'Shon Bernard", 22, "DF"),
        ("Amari'i Bell", 23, "DF"), ("Richard King", 12, "DF"), ("Greg Leigh", 19, "DF"),
        ("Kasey Palmer", 20, "MF"), ("Andre Blake", 1, "GK"), ("Dillon Barnes", 24, "GK"),
        ("Jahmali Waite", 25, "GK"), ("Kaheim Dixon", 26, "FW"),
    ]),
    ("Costa Rica", "CRC", 48, "H", "Gustavo Alfaro", [
        ("Keylor Navas", 1, "GK"), ("Bryan Ruiz", 10, "MF"), ("Joel Campbell", 9, "FW"),
        ("Jewison Bennette", 7, "FW"), ("Anthony Contreras", 11, "FW"), ("Johan Venegas", 17, "FW"),
        ("Celso Borges", 8, "MF"), ("Yeltsin Tejeda", 6, "MF"), ("Brandon Aguilera", 14, "MF"),
        ("Gerson Torres", 4, "MF"), ("Álvaro Zamora", 18, "MF"), ("Douglas López", 16, "MF"),
        ("Francisco Calvo", 3, "DF"), ("Óscar Duarte", 5, "DF"), ("Bryan Oviedo", 13, "DF"),
        ("Kendall Waston", 15, "DF"), ("Keysher Fuller", 2, "DF"), ("Juan Pablo Vargas", 22, "DF"),
        ("Carlos Martínez", 23, "DF"), ("Daniel Chacón", 12, "DF"), ("Haxzel Quirós", 19, "DF"),
        ("Manfred Ugalde", 21, "FW"), ("Patrick Sequeira", 24, "GK"), ("Aarón Cruz", 25, "GK"),
        ("Josimar Alcócer", 20, "MF"), ("Alejandro Bran", 26, "MF"),
    ]),
    ("Tunisia", "TUN", 38, "H", "Jalel Kadri", [
        ("Youssef Msakni", 7, "FW"), ("Wahbi Khazri", 10, "FW"), ("Aïssa Laïdouni", 8, "MF"),
        ("Hannibal Mejbri", 6, "MF"), ("Naïm Sliti", 11, "FW"), ("Seifeddine Jaziri", 9, "FW"),
        ("Ellyes Skhiri", 14, "MF"), ("Mohamed Ali Ben Romdhane", 4, "MF"), ("Ferjani Sassi", 18, "MF"),
        ("Ghailene Chaalali", 16, "MF"), ("Saâd Bguir", 20, "MF"), ("Hamza Rafia", 26, "MF"),
        ("Montassar Talbi", 3, "DF"), ("Dylan Bronn", 5, "DF"), ("Mohamed Dräger", 2, "DF"),
        ("Yassine Meriah", 13, "DF"), ("Ali Maâloul", 15, "DF"), ("Nader Ghandri", 22, "DF"),
        ("Wajdi Kechrida", 23, "DF"), ("Ali Abdi", 12, "DF"), ("Bilel Ifa", 19, "DF"),
        ("Issam Jebali", 17, "FW"), ("Aymen Dahmen", 1, "GK"), ("Bechir Ben Saïd", 24, "GK"),
        ("Mouez Hassen", 25, "GK"), ("Taha Yassine Khenissi", 21, "FW"),
    ]),
    # ── GROUP I ──────────────────────────────────────────────
    ("Netherlands", "NED", 8, "I", "Ronald Koeman", [
        ("Memphis Depay", 10, "FW"), ("Cody Gakpo", 11, "FW"), ("Virgil van Dijk", 4, "DF"),
        ("Frenkie de Jong", 8, "MF"), ("Xavi Simons", 7, "MF"), ("Wout Weghorst", 9, "FW"),
        ("Steven Bergwijn", 17, "FW"), ("Donyell Malen", 18, "FW"), ("Teun Koopmeiners", 14, "MF"),
        ("Marten de Roon", 6, "MF"), ("Ryan Gravenberch", 16, "MF"), ("Tijjani Reijnders", 20, "MF"),
        ("Nathan Aké", 5, "DF"), ("Matthijs de Ligt", 3, "DF"), ("Denzel Dumfries", 22, "DF"),
        ("Daley Blind", 13, "DF"), ("Jurriën Timber", 2, "DF"), ("Stefan de Vrij", 15, "DF"),
        ("Micky van de Ven", 23, "DF"), ("Jeremie Frimpong", 12, "DF"), ("Lutsharel Geertruida", 19, "DF"),
        ("Joey Veerman", 21, "MF"), ("Bart Verbruggen", 1, "GK"), ("Justin Bijlow", 24, "GK"),
        ("Mark Flekken", 25, "GK"), ("Joshua Zirkzee", 26, "FW"),
    ]),
    ("Ivory Coast", "CIV", 39, "I", "Emerse Faé", [
        ("Nicolas Pépé", 7, "FW"), ("Sébastien Haller", 9, "FW"), ("Franck Kessié", 8, "MF"),
        ("Ibrahim Sangaré", 6, "MF"), ("Max-Alain Gradel", 10, "FW"), ("Simon Adingra", 17, "FW"),
        ("Jean-Philippe Krasso", 11, "FW"), ("Christian Kouamé", 21, "FW"), ("Oumar Diakité", 18, "MF"),
        ("Hamed Traoré", 14, "MF"), ("Jean Michaël Seri", 4, "MF"), ("Seko Fofana", 16, "MF"),
        ("Odilon Kossounou", 3, "DF"), ("Eric Bailly", 5, "DF"), ("Serge Aurier", 2, "DF"),
        ("Willy Boly", 13, "DF"), ("Simon Deli", 15, "DF"), ("Ghislain Konan", 22, "DF"),
        ("Emmanuel Agbadou", 23, "DF"), ("Wilfried Singo", 12, "DF"), ("Evan N'Dicka", 19, "DF"),
        ("Jeremie Boga", 20, "FW"), ("Badra Ali Sangaré", 1, "GK"), ("Yahia Fofana", 24, "GK"),
        ("Eliezer Ira", 25, "GK"), ("Karim Konaté", 26, "FW"),
    ]),
    ("Switzerland", "SUI", 16, "I", "Murat Yakin", [
        ("Granit Xhaka", 10, "MF"), ("Xherdan Shaqiri", 7, "FW"), ("Breel Embolo", 9, "FW"),
        ("Ruben Vargas", 11, "FW"), ("Dan Ndoye", 17, "FW"), ("Zeki Amdouni", 21, "FW"),
        ("Remo Freuler", 8, "MF"), ("Denis Zakaria", 6, "MF"), ("Djibril Sow", 14, "MF"),
        ("Michel Aebischer", 18, "MF"), ("Ardon Jashari", 16, "MF"), ("Vincent Sierro", 20, "MF"),
        ("Manuel Akanji", 4, "DF"), ("Fabian Schär", 5, "DF"), ("Ricardo Rodríguez", 3, "DF"),
        ("Nico Elvedi", 13, "DF"), ("Silvan Widmer", 2, "DF"), ("Eray Cömert", 15, "DF"),
        ("Leonidas Stergiou", 22, "DF"), ("Ulisses Garcia", 23, "DF"), ("Cédric Zesiger", 12, "DF"),
        ("Fabian Rieder", 19, "MF"), ("Yann Sommer", 1, "GK"), ("Jonas Omlin", 24, "GK"),
        ("Gregor Kobel", 25, "GK"), ("Noah Okafor", 26, "FW"),
    ]),
    ("Uzbekistan", "UZB", 62, "I", "Srecko Katanec", [
        ("Eldor Shomurodov", 9, "FW"), ("Jaloliddin Masharipov", 10, "MF"), ("Dostonbek Khamdamov", 7, "FW"),
        ("Oston Urunov", 11, "FW"), ("Abbosbek Fayzullaev", 17, "FW"), ("Azizbek Turgunboev", 21, "FW"),
        ("Otabek Shukurov", 8, "MF"), ("Jamshid Iskanderov", 6, "MF"), ("Jaloliddin Babaev", 14, "MF"),
        ("Bobir Abdixoliqov", 4, "MF"), ("Ikromjon Alibaev", 18, "MF"), ("Khojimat Erkinov", 16, "MF"),
        ("Husniddin Aliqulov", 3, "DF"), ("Rustamjon Ashurmatov", 5, "DF"), ("Islom Tukhtasinov", 2, "DF"),
        ("Akmal Shorakhmedov", 13, "DF"), ("Sanzhar Tursunov", 15, "DF"), ("Davron Khashimov", 22, "DF"),
        ("Abdukodir Khusanov", 23, "DF"), ("Odiljon Hamrobekov", 12, "DF"), ("Sherzod Nasrullaev", 19, "DF"),
        ("Igor Sergeev", 20, "FW"), ("Eldorbek Suyunov", 1, "GK"), ("Utkir Yusupov", 24, "GK"),
        ("Botir Ergashev", 25, "GK"), ("Khusain Norchaev", 26, "MF"),
    ]),
    # ── GROUP J ──────────────────────────────────────────────
    ("Belgium", "BEL", 9, "J", "Domenico Tedesco", [
        ("Kevin De Bruyne", 7, "MF"), ("Romelu Lukaku", 9, "FW"), ("Eden Hazard", 10, "FW"),
        ("Jérémy Doku", 11, "FW"), ("Dries Mertens", 14, "FW"), ("Loïs Openda", 17, "FW"),
        ("Youri Tielemans", 8, "MF"), ("Amadou Onana", 6, "MF"), ("Axel Witsel", 4, "MF"),
        ("Orel Mangala", 18, "MF"), ("Leander Dendoncker", 16, "MF"), ("Aster Vranckx", 20, "MF"),
        ("Jan Vertonghen", 5, "DF"), ("Toby Alderweireld", 3, "DF"), ("Timothy Castagne", 2, "DF"),
        ("Arthur Theate", 13, "DF"), ("Zeno Debast", 15, "DF"), ("Thomas Meunier", 22, "DF"),
        ("Wout Faes", 23, "DF"), ("Brandon Mechele", 12, "DF"), ("Maxim De Cuyper", 19, "DF"),
        ("Charles De Ketelaere", 21, "FW"), ("Thibaut Courtois", 1, "GK"), ("Simon Mignolet", 24, "GK"),
        ("Koen Casteels", 25, "GK"), ("Johan Bakayoko", 26, "FW"),
    ]),
    ("Egypt", "EGY", 36, "J", "Hossam Hassan", [
        ("Mohamed Salah", 10, "FW"), ("Trézéguet", 7, "FW"), ("Mostafa Mohamed", 9, "FW"),
        ("Ahmed Hegazy", 5, "DF"), ("Mohamed Elneny", 8, "MF"), ("Mahmoud Hassan", 11, "FW"),
        ("Amr El Soleya", 6, "MF"), ("Emam Ashour", 14, "MF"), ("Mohamed Magdy", 17, "FW"),
        ("Omar Marmoush", 18, "FW"), ("Tarek Hamed", 4, "MF"), ("Ibrahim Adel", 16, "MF"),
        ("Ahmed Fatouh", 3, "DF"), ("Ali Gabr", 13, "DF"), ("Omar Gaber", 2, "DF"),
        ("Mohamed Abdel-Moneim", 15, "DF"), ("Akram Tawfik", 22, "DF"), ("Ayman Ashraf", 23, "DF"),
        ("Ahmed Hegazi", 12, "DF"), ("Mahmoud Hamdy", 19, "DF"), ("Ahmed Sayed", 20, "MF"),
        ("Mostafa Fathi", 21, "FW"), ("Mohamed El Shenawy", 1, "GK"), ("Mohamed Abou Gabal", 24, "GK"),
        ("Ahmed El Shennawy", 25, "GK"), ("Karim Fouad", 26, "MF"),
    ]),
    ("Bolivia", "BOL", 83, "J", "Óscar Villegas", [
        ("Marcelo Moreno Martins", 9, "FW"), ("Ramiro Vaca", 10, "MF"), ("Henry Vaca", 7, "FW"),
        ("Víctor Ábrego", 11, "FW"), ("César Menacho", 17, "FW"), ("Rodrigo Ramallo", 21, "FW"),
        ("Leonel Justiniano", 8, "MF"), ("Diego Bejarano", 6, "MF"), ("Boris Céspedes", 14, "MF"),
        ("Fernando Saucedo", 4, "MF"), ("Moisés Villarroel", 18, "MF"), ("Gabriel Villamil", 16, "MF"),
        ("José Sagredo", 3, "DF"), ("Adrián Jusino", 5, "DF"), ("Diego Bejarano", 2, "DF"),
        ("Luis Haquín", 13, "DF"), ("Jairo Quinteros", 15, "DF"), ("Roberto Fernández", 22, "DF"),
        ("Jesús Sagredo", 23, "DF"), ("Marc Enoumba", 12, "DF"), ("José Carrasco", 19, "DF"),
        ("Jeyson Chura", 20, "MF"), ("Carlos Lampe", 1, "GK"), ("Guillermo Viscarra", 24, "GK"),
        ("Rubén Cordano", 25, "GK"), ("Miguel Terceros", 26, "FW"),
    ]),
    ("Qatar", "QAT", 44, "J", "Carlos Queiroz", [
        ("Akram Afif", 10, "FW"), ("Almoez Ali", 9, "FW"), ("Hassan Al-Haydos", 7, "FW"),
        ("Abdulaziz Hatem", 8, "MF"), ("Karim Boudiaf", 6, "MF"), ("Ali Assadalla", 14, "MF"),
        ("Mohammed Waad", 11, "FW"), ("Ahmed Alaaeldin", 17, "FW"), ("Yusuf Abdurisag", 21, "FW"),
        ("Assim Madibo", 4, "MF"), ("Musaab Khidir", 18, "MF"), ("Ismail Mohamad", 16, "MF"),
        ("Abdelkarim Hassan", 3, "DF"), ("Bassam Al-Rawi", 5, "DF"), ("Pedro Miguel", 2, "DF"),
        ("Boualem Khoukhi", 13, "DF"), ("Homam Ahmed", 15, "DF"), ("Tarek Salman", 22, "DF"),
        ("Musab Kheder", 23, "DF"), ("Ro-Ro", 12, "DF"), ("Jassem Gaber", 19, "DF"),
        ("Mohammed Muntari", 20, "FW"), ("Saad Al Sheeb", 1, "GK"), ("Meshaal Barsham", 24, "GK"),
        ("Yousof Hassan", 25, "GK"), ("Sultan Al-Brake", 26, "MF"),
    ]),
    # ── GROUP K ──────────────────────────────────────────────
    ("Croatia", "CRO", 10, "K", "Zlatko Dalić", [
        ("Luka Modrić", 10, "MF"), ("Ivan Perišić", 4, "FW"), ("Mateo Kovačić", 8, "MF"),
        ("Andrej Kramarić", 9, "FW"), ("Marcelo Brozović", 11, "MF"), ("Bruno Petković", 17, "FW"),
        ("Lovro Majer", 7, "MF"), ("Mario Pašalić", 14, "MF"), ("Nikola Vlašić", 6, "MF"),
        ("Luka Sučić", 18, "MF"), ("Martin Erlić", 16, "DF"), ("Kristijan Jakić", 26, "MF"),
        ("Joško Gvardiol", 3, "DF"), ("Dejan Lovren", 5, "DF"), ("Duje Ćaleta-Car", 13, "DF"),
        ("Josip Juranović", 2, "DF"), ("Borna Sosa", 22, "DF"), ("Josip Stanišić", 15, "DF"),
        ("Domagoj Vida", 23, "DF"), ("Josip Šutalo", 12, "DF"), ("Marin Pongračić", 19, "DF"),
        ("Ante Budimir", 21, "FW"), ("Dominik Livaković", 1, "GK"), ("Ivica Ivušić", 24, "GK"),
        ("Nediljko Labrović", 25, "GK"), ("Igor Matanović", 20, "FW"),
    ]),
    ("Algeria", "ALG", 37, "K", "Vladimir Petković", [
        ("Riyad Mahrez", 7, "FW"), ("Ismaël Bennacer", 6, "MF"), ("Islam Slimani", 9, "FW"),
        ("Yacine Brahimi", 10, "FW"), ("Saïd Benrahma", 11, "FW"), ("Baghdad Bounedjah", 17, "FW"),
        ("Sofiane Feghouli", 8, "MF"), ("Ramiz Zerrouki", 14, "MF"), ("Houssem Aouar", 4, "MF"),
        ("Nabil Bentaleb", 18, "MF"), ("Hicham Boudaoui", 16, "MF"), ("Amine Gouiri", 20, "MF"),
        ("Djamel Benlamri", 3, "DF"), ("Aïssa Mandi", 5, "DF"), ("Youcef Atal", 2, "DF"),
        ("Ramy Bensebaini", 13, "DF"), ("Abdelkader Bedrane", 15, "DF"), ("Mehdi Tahrat", 22, "DF"),
        ("Hichem Belmaoui", 23, "DF"), ("Abdelkader Laïfaoui", 12, "DF"), ("Ilyes Chetti", 19, "DF"),
        ("Mohamed Amoura", 21, "FW"), ("Raïs M'Bolhi", 1, "GK"), ("Anthony Mandréa", 24, "GK"),
        ("Moustapha Zeghba", 25, "GK"), ("Farès Chaibi", 26, "MF"),
    ]),
    ("Poland", "POL", 26, "K", "Michał Probierz", [
        ("Robert Lewandowski", 9, "FW"), ("Piotr Zieliński", 10, "MF"), ("Arkadiusz Milik", 7, "FW"),
        ("Karol Świderski", 11, "FW"), ("Krzysztof Piątek", 17, "FW"), ("Adam Buksa", 21, "FW"),
        ("Grzegorz Krychowiak", 8, "MF"), ("Jakub Moder", 6, "MF"), ("Damian Szymański", 14, "MF"),
        ("Sebastian Szymański", 18, "MF"), ("Jakub Piotrowski", 16, "MF"), ("Kacper Urbański", 20, "MF"),
        ("Jan Bednarek", 5, "DF"), ("Kamil Glik", 3, "DF"), ("Matty Cash", 2, "DF"),
        ("Bartosz Bereszyński", 4, "DF"), ("Jakub Kiwior", 13, "DF"), ("Paweł Dawidowicz", 15, "DF"),
        ("Robert Gumny", 22, "DF"), ("Nicola Zalewski", 23, "DF"), ("Bartosz Salamon", 12, "DF"),
        ("Przemysław Frankowski", 19, "FW"), ("Wojciech Szczęsny", 1, "GK"), ("Łukasz Skorupski", 24, "GK"),
        ("Marcin Bułka", 25, "GK"), ("Michał Skóraś", 26, "FW"),
    ]),
    ("Bahrain", "BHR", 81, "K", "Dragan Talajić", [
        ("Abdulla Yusuf Helal", 9, "FW"), ("Komail Al Aswad", 10, "MF"), ("Mahdi Al Humaidan", 7, "FW"),
        ("Ali Madan", 11, "FW"), ("Mohammed Al Romaihi", 17, "FW"), ("Jassim Al Shaikh", 21, "FW"),
        ("Ali Haram", 8, "MF"), ("Mohamed Marhoon", 6, "MF"), ("Kamil Al Aswad", 14, "MF"),
        ("Sayed Dhiya Saeed", 4, "MF"), ("Ahmed Bughammar", 18, "MF"), ("Rashed Al Hooti", 16, "MF"),
        ("Ahmed Nabeel", 3, "DF"), ("Waleed Al Hayam", 5, "DF"), ("Sayed Redha Isa", 2, "DF"),
        ("Hamad Al Shamsan", 13, "DF"), ("Amine Hasan", 15, "DF"), ("Abbas Asghar", 22, "DF"),
        ("Hashim Sayed Isa", 23, "DF"), ("Ali Al Khaibari", 12, "DF"), ("Abdulla Al Khalasi", 19, "DF"),
        ("Mahdi Abduljabbar", 20, "FW"), ("Sayed Shubbar Alawi", 1, "GK"), ("Ebrahim Lutfallah", 24, "GK"),
        ("Abdulla Alyaqoob", 25, "GK"), ("Hasan Al Alawi", 26, "MF"),
    ]),
    # ── GROUP L ──────────────────────────────────────────────
    ("Italy", "ITA", 8, "L", "Luciano Spalletti", [
        ("Lorenzo Insigne", 10, "FW"), ("Federico Chiesa", 7, "FW"), ("Gianluca Scamacca", 9, "FW"),
        ("Giacomo Raspadori", 11, "FW"), ("Mateo Retegui", 17, "FW"), ("Stephan El Shaarawy", 21, "FW"),
        ("Nicolò Barella", 8, "MF"), ("Jorginho", 6, "MF"), ("Marco Verratti", 14, "MF"),
        ("Sandro Tonali", 4, "MF"), ("Davide Frattesi", 18, "MF"), ("Samuele Ricci", 16, "MF"),
        ("Alessandro Bastoni", 5, "DF"), ("Leonardo Bonucci", 3, "DF"), ("Giovanni Di Lorenzo", 2, "DF"),
        ("Federico Dimarco", 13, "DF"), ("Gianluca Mancini", 15, "DF"), ("Matteo Darmian", 22, "DF"),
        ("Andrea Cambiaso", 23, "DF"), ("Rafael Tolói", 12, "DF"), ("Riccardo Calafiori", 19, "DF"),
        ("Bryan Cristante", 20, "MF"), ("Gianluigi Donnarumma", 1, "GK"), ("Alex Meret", 24, "GK"),
        ("Ivan Provedel", 25, "GK"), ("Daniel Maldini", 26, "FW"),
    ]),
    ("Türkiye", "TUR", 29, "L", "Vincenzo Montella", [
        ("Hakan Çalhanoğlu", 10, "MF"), ("Arda Güler", 7, "MF"), ("Cenk Tosun", 9, "FW"),
        ("Kerem Aktürkoğlu", 11, "FW"), ("Barış Alper Yılmaz", 17, "FW"), ("Semih Kılıçsoy", 21, "FW"),
        ("Okay Yokuşlu", 6, "MF"), ("Orkun Kökçü", 8, "MF"), ("İrfan Can Kahveci", 14, "MF"),
        ("Salih Özcan", 18, "MF"), ("Yunus Akgün", 16, "MF"), ("Kaan Ayhan", 4, "MF"),
        ("Merih Demiral", 3, "DF"), ("Çağlar Söyüncü", 5, "DF"), ("Zeki Çelik", 2, "DF"),
        ("Abdülkerim Bardakcı", 13, "DF"), ("Ozan Kabak", 15, "DF"), ("Ferdi Kadıoğlu", 22, "DF"),
        ("Samet Akaydin", 23, "DF"), ("Ahmetcan Kaplan", 12, "DF"), ("Mert Müldür", 19, "DF"),
        ("Yusuf Yazıcı", 20, "FW"), ("Altay Bayındır", 1, "GK"), ("Uğurcan Çakır", 24, "GK"),
        ("Mert Günok", 25, "GK"), ("Kenan Yıldız", 26, "FW"),
    ]),
    ("Denmark", "DEN", 18, "L", "Kasper Hjulmand", [
        ("Christian Eriksen", 10, "MF"), ("Mikkel Damsgaard", 7, "MF"), ("Kasper Dolberg", 9, "FW"),
        ("Martin Braithwaite", 11, "FW"), ("Rasmus Højlund", 17, "FW"), ("Andreas Skov Olsen", 21, "FW"),
        ("Pierre-Emile Højbjerg", 8, "MF"), ("Thomas Delaney", 6, "MF"), ("Morten Hjulmand", 14, "MF"),
        ("Mathias Jensen", 4, "MF"), ("Matt O'Riley", 18, "MF"), ("Christian Nørgaard", 16, "MF"),
        ("Simon Kjær", 3, "DF"), ("Andreas Christensen", 5, "DF"), ("Rasmus Kristensen", 2, "DF"),
        ("Joachim Andersen", 13, "DF"), ("Victor Nelsson", 15, "DF"), ("Joakim Mæhle", 22, "DF"),
        ("Alexander Bah", 23, "DF"), ("Daniel Wass", 12, "DF"), ("Jens Stryger Larsen", 19, "DF"),
        ("Yussuf Poulsen", 20, "FW"), ("Kasper Schmeichel", 1, "GK"), ("Frederik Rønnow", 24, "GK"),
        ("Oliver Christensen", 25, "GK"), ("Gustav Isaksen", 26, "FW"),
    ]),
    ("Indonesia", "IDN", 90, "L", "Shin Tae-yong", [
        ("Egy Maulana Vikri", 10, "MF"), ("Pratama Arhan", 3, "DF"), ("Asnawi Mangkualam", 2, "DF"),
        ("Witan Sulaeman", 7, "FW"), ("Ilham Udin", 11, "FW"), ("Yakob Sayuri", 17, "FW"),
        ("Marc Klok", 8, "MF"), ("Evan Dimas", 6, "MF"), ("Adam Alis", 14, "MF"),
        ("Rizky Ridho", 4, "DF"), ("Ricky Kambuaya", 18, "MF"), ("Syahrian Abimanyu", 16, "MF"),
        ("Jordi Amat", 5, "DF"), ("Elkan Baggott", 13, "DF"), ("Fachruddin Aryanto", 15, "DF"),
        ("Justin Hubner", 22, "DF"), ("Sandy Walsh", 23, "DF"), ("Rachmat Irianto", 12, "DF"),
        ("Dimas Drajad", 19, "DF"), ("Rafael Struick", 9, "FW"), ("Irfan Jaya", 21, "FW"),
        ("Marselino Ferdinan", 20, "FW"), ("Nadeo Argawinata", 1, "GK"), ("Muhammad Ridho", 24, "GK"),
        ("Ernando Ari", 25, "GK"), ("Ivar Jenner", 26, "MF"),
    ]),
]
# fmt: on

# ── Match Schedule Generator ────────────────────────────────
# Group stage: 12 groups × 6 matches = 72 matches
# Knockout: Round of 32 (16) + R16 (8) + QF (4) + SF (2) + 3rd (1) + Final (1) = 32
# Total: 104 matches

KICKOFF_TIMES = [
    time(13, 0), time(16, 0), time(19, 0), time(22, 0),
]

def generate_group_matches(start_date, venues):
    """Generate 72 group stage matches across ~16 days."""
    groups = {}
    for t in TEAMS_DATA:
        g = t[3]
        groups.setdefault(g, []).append(t[1])  # country_code

    matches = []
    match_num = 1
    group_letters = sorted(groups.keys())

    # Each group has 6 matches (round-robin of 4 teams)
    # Schedule: matchday 1 (games 1-2), matchday 2 (games 3-4), matchday 3 (games 5-6)
    for gi, gl in enumerate(group_letters):
        codes = groups[gl]
        # Round robin pairings for 4 teams: (0v1, 2v3), (0v2, 1v3), (0v3, 1v2)
        pairings = [
            (codes[0], codes[1], codes[2], codes[3]),  # matchday 1
            (codes[0], codes[2], codes[1], codes[3]),  # matchday 2
            (codes[0], codes[3], codes[1], codes[2]),  # matchday 3
        ]
        for md_idx, (h1, a1, h2, a2) in enumerate(pairings):
            # Spread across days: 4 matches per day, groups staggered
            day_offset = md_idx * 5 + (gi // 4)
            match_day = start_date + timedelta(days=day_offset)
            slot = (gi % 4)

            venue = venues[(match_num - 1) % len(venues)]

            for home, away in [(h1, a1), (h2, a2)]:
                kt = KICKOFF_TIMES[slot % len(KICKOFF_TIMES)]
                matches.append({
                    "match_number": match_num,
                    "stage": "GROUP",
                    "group_letter": gl,
                    "home_code": home,
                    "away_code": away,
                    "match_date": match_day,
                    "kickoff_time": kt,
                    "arena_name": venue[0],
                    "city": venue[1],
                })
                match_num += 1
                slot += 1
                venue = venues[(match_num - 1) % len(venues)]

    return matches, match_num


def generate_knockout_matches(start_date, next_match_num, venues):
    """Generate 32 knockout matches with TBD placeholders."""
    matches = []
    mn = next_match_num

    # ── Round of 32 (16 matches) — days 18-21 ───────────────
    r32_day = start_date + timedelta(days=17)
    r32_placeholders = [
        ("1st Group A", "2nd Group B"),
        ("1st Group B", "2nd Group A"),
        ("1st Group C", "2nd Group D"),
        ("1st Group D", "2nd Group C"),
        ("1st Group E", "2nd Group F"),
        ("1st Group F", "2nd Group E"),
        ("1st Group G", "2nd Group H"),
        ("1st Group H", "2nd Group G"),
        ("1st Group I", "2nd Group J"),
        ("1st Group J", "2nd Group I"),
        ("1st Group K", "2nd Group L"),
        ("1st Group L", "2nd Group K"),
        ("3rd Group A/B/C", "3rd Group D/E/F"),
        ("3rd Group G/H/I", "3rd Group J/K/L"),
        ("Best 3rd #5", "Best 3rd #6"),
        ("Best 3rd #7", "Best 3rd #8"),
    ]
    for i, (hp, ap) in enumerate(r32_placeholders):
        day_off = i // 4
        venue = venues[(mn - 1) % len(venues)]
        kt = KICKOFF_TIMES[i % len(KICKOFF_TIMES)]
        matches.append({
            "match_number": mn,
            "stage": "ROUND_OF_32",
            "group_letter": None,
            "home_code": None,
            "away_code": None,
            "home_placeholder": hp,
            "away_placeholder": ap,
            "match_date": r32_day + timedelta(days=day_off),
            "kickoff_time": kt,
            "arena_name": venue[0],
            "city": venue[1],
        })
        mn += 1

    # ── Round of 16 (8 matches) — days 23-24 ────────────────
    r16_day = start_date + timedelta(days=22)
    for i in range(8):
        day_off = i // 4
        venue = venues[(mn - 1) % len(venues)]
        kt = KICKOFF_TIMES[i % len(KICKOFF_TIMES)]
        matches.append({
            "match_number": mn,
            "stage": "ROUND_OF_16",
            "group_letter": None,
            "home_code": None,
            "away_code": None,
            "home_placeholder": f"Winner R32-{73 + i * 2}",
            "away_placeholder": f"Winner R32-{74 + i * 2}",
            "match_date": r16_day + timedelta(days=day_off),
            "kickoff_time": kt,
            "arena_name": venue[0],
            "city": venue[1],
        })
        mn += 1

    # ── Quarterfinals (4 matches) — days 26-27 ──────────────
    qf_day = start_date + timedelta(days=25)
    for i in range(4):
        day_off = i // 2
        venue = venues[(mn - 1) % len(venues)]
        kt = KICKOFF_TIMES[i % len(KICKOFF_TIMES)]
        matches.append({
            "match_number": mn,
            "stage": "QUARTERFINAL",
            "group_letter": None,
            "home_code": None,
            "away_code": None,
            "home_placeholder": f"Winner R16-{89 + i * 2}",
            "away_placeholder": f"Winner R16-{90 + i * 2}",
            "match_date": qf_day + timedelta(days=day_off),
            "kickoff_time": kt,
            "arena_name": venue[0],
            "city": venue[1],
        })
        mn += 1

    # ── Semifinals (2 matches) — day 29 ─────────────────────
    sf_day = start_date + timedelta(days=28)
    for i in range(2):
        venue = venues[(mn - 1) % len(venues)]
        kt = KICKOFF_TIMES[i % len(KICKOFF_TIMES)]
        matches.append({
            "match_number": mn,
            "stage": "SEMIFINAL",
            "group_letter": None,
            "home_code": None,
            "away_code": None,
            "home_placeholder": f"Winner QF-{97 + i * 2}",
            "away_placeholder": f"Winner QF-{98 + i * 2}",
            "match_date": sf_day,
            "kickoff_time": kt,
            "arena_name": venue[0],
            "city": venue[1],
        })
        mn += 1

    # ── Third Place — day 31 ────────────────────────────────
    venue = venues[(mn - 1) % len(venues)]
    matches.append({
        "match_number": mn,
        "stage": "THIRD_PLACE",
        "group_letter": None,
        "home_code": None,
        "away_code": None,
        "home_placeholder": "Loser SF-101",
        "away_placeholder": "Loser SF-102",
        "match_date": start_date + timedelta(days=30),
        "kickoff_time": time(19, 0),
        "arena_name": "MetLife Stadium",
        "city": "East Rutherford",
    })
    mn += 1

    # ── Final — day 32 ──────────────────────────────────────
    matches.append({
        "match_number": mn,
        "stage": "FINAL",
        "group_letter": None,
        "home_code": None,
        "away_code": None,
        "home_placeholder": "Winner SF-101",
        "away_placeholder": "Winner SF-102",
        "match_date": start_date + timedelta(days=31),
        "kickoff_time": time(20, 0),
        "arena_name": "MetLife Stadium",
        "city": "East Rutherford",
    })
    mn += 1

    return matches


# ── Main ─────────────────────────────────────────────────────
def main():
    print("=" * 60)
    print("  FIFA World Cup 2026 — Database Setup")
    print("=" * 60)
    print(f"  Tournament start date: {TOURNAMENT_START}")
    print(f"  MySQL host: {MYSQL_HOST}")
    print(f"  MySQL user: {MYSQL_USER}")
    print()

    password = getpass.getpass(f"Enter MySQL password for '{MYSQL_USER}': ")

    try:
        conn = mysql.connect(
            host=MYSQL_HOST,
            user=MYSQL_USER,
            password=password,
        )
    except mysql.Error as e:
        print(f"  [ERROR] Could not connect to MySQL: {e}")
        sys.exit(1)

    cursor = conn.cursor()
    print("\n  [1/4] Creating database and tables...")

    for stmt in DDL_STATEMENTS:
        cursor.execute(stmt)
    conn.commit()

    # Switch to the new database
    cursor.execute(f"USE {DB_NAME}")
    cursor.execute("SET time_zone = '+00:00'")

    # ── Insert teams ─────────────────────────────────────────
    print("  [2/4] Inserting 48 teams with squads...")

    team_insert = """
        INSERT INTO teams
            (country_name, country_code, flag_url, logo_url, fifa_ranking,
             group_letter, manager_name, squad)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
    """

    code_to_id = {}
    for t_name, t_code, t_rank, t_group, t_manager, t_squad in TEAMS_DATA:
        flag = f"/assets/flags/{t_code.lower()}.png"
        logo = f"/assets/logos/{t_code.lower()}.png"
        squad_json = json.dumps(_squad(t_squad))

        cursor.execute(team_insert, (
            t_name, t_code, flag, logo, t_rank,
            t_group, t_manager, squad_json,
        ))
        code_to_id[t_code] = cursor.lastrowid

    conn.commit()
    print(f"         → {len(TEAMS_DATA)} teams inserted.")

    # ── Generate & insert matches ────────────────────────────
    print("  [3/4] Generating match schedule...")

    group_matches, next_mn = generate_group_matches(TOURNAMENT_START, VENUES)
    knockout_matches = generate_knockout_matches(TOURNAMENT_START, next_mn, VENUES)

    all_matches = group_matches + knockout_matches
    print(f"         → {len(group_matches)} group + {len(knockout_matches)} knockout = {len(all_matches)} total")

    print("  [4/4] Inserting matches...")

    event_insert = """
        INSERT INTO events
            (match_number, stage, group_letter,
             home_team_id, away_team_id,
             home_team_placeholder, away_team_placeholder,
             match_date, kickoff_time, kickoff_utc,
             arena_name, city, status)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """

    for m in all_matches:
        home_id = code_to_id.get(m.get("home_code"))
        away_id = code_to_id.get(m.get("away_code"))

        if home_id and away_id:
            # Group match — look up team names for placeholders too
            h_name = next(t[0] for t in TEAMS_DATA if t[1] == m["home_code"])
            a_name = next(t[0] for t in TEAMS_DATA if t[1] == m["away_code"])
            h_placeholder = h_name
            a_placeholder = a_name
        else:
            h_placeholder = m.get("home_placeholder", "TBD")
            a_placeholder = m.get("away_placeholder", "TBD")

        kt = m["kickoff_time"]
        kickoff_utc = f"{m['match_date']} {kt.strftime('%H:%M:%S')}" if kt else None

        cursor.execute(event_insert, (
            m["match_number"],
            m["stage"],
            m.get("group_letter"),
            home_id,
            away_id,
            h_placeholder,
            a_placeholder,
            m["match_date"],
            kt.strftime("%H:%M:%S") if kt else None,
            kickoff_utc,
            m["arena_name"],
            m["city"],
            "SCHEDULED",
        ))

    conn.commit()
    print(f"         → {len(all_matches)} matches inserted.")

    cursor.close()
    conn.close()

    print()
    print("=" * 60)
    print("  Setup complete!")
    print(f"  Database '{DB_NAME}' is ready.")
    print(f"  First match: {TOURNAMENT_START}")
    print(f"  Final:       {TOURNAMENT_START + timedelta(days=31)}")
    print("=" * 60)


if __name__ == "__main__":
    main()