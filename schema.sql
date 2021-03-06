# Create schemas

# Create tables
CREATE TABLE STUDENT
(
    ID INT NOT NULL,
    NAME VARCHAR(100),
    CLASS VARCHAR(50),
    SECTION VARCHAR(50),
    PRIMARY KEY(ID)
);

CREATE TABLE STUDENT_SPORT
(
    STUDENT_ID INT NOT NULL,
    SPORT_ID INT,
    CATEGORY_ID INT,
    PRIMARY KEY(STUDENT_ID)
);

CREATE TABLE CATEGORY
(
    CATEGORY_ID INT NOT NULL,
    CATEGORY_NAME VARCHAR(50),
    PRIMARY KEY(CATEGORY_ID)
);

CREATE TABLE CATEGORY_SPORTS
(
    SPORT_ID INT NOT NULL,
    CATEGORY_ID INT,
    SPORT_NAME VARCHAR(50),
    PRIMARY KEY(SPORT_ID)
);


# Create FKs
ALTER TABLE STUDENT_SPORT
    ADD    FOREIGN KEY (STUDENT_ID)
    REFERENCES STUDENT(ID)
;
    
ALTER TABLE CATEGORY_SPORTS
    ADD    FOREIGN KEY (CATEGORY_ID)
    REFERENCES CATEGORY(CATEGORY_ID)
;
    
ALTER TABLE STUDENT_SPORT
    ADD    FOREIGN KEY (SPORT_ID)
    REFERENCES CATEGORY_SPORTS(SPORT_ID)
;
    

