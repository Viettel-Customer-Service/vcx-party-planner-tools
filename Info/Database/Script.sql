-- =========================
-- 1. DROP TABLE
-- =========================
DROP TABLE IF EXISTS BirthdayLog CASCADE;
DROP TABLE IF EXISTS Employee CASCADE;
DROP TABLE IF EXISTS Department CASCADE;

-- =========================
-- 2. Department
-- =========================
CREATE TABLE Department (
                            DepartmentID SERIAL PRIMARY KEY,
                            DepartmentCode VARCHAR(50) NOT NULL UNIQUE,
                            DepartmentName VARCHAR(255) NOT NULL
);

-- =========================
-- 3. Employee
-- =========================
CREATE TABLE Employee (
                          EmployeeID SERIAL PRIMARY KEY,
                          EmployeeCode VARCHAR(50) NOT NULL UNIQUE,
                          FullName VARCHAR(255) NOT NULL,
                          JobTitle VARCHAR(255),

                          PhoneNumber VARCHAR(20),
                          Email VARCHAR(255) NOT NULL UNIQUE,

                          DateOfBirth DATE NOT NULL,
                          BirthDay INT NOT NULL,
                          BirthMonth INT NOT NULL,

                          DepartmentID INT NOT NULL,

                          IsActive BOOLEAN DEFAULT TRUE,

                          CONSTRAINT fk_employee_department
                              FOREIGN KEY (DepartmentID)
                                  REFERENCES Department(DepartmentID),

    -- 🔥 đảm bảo ngày/tháng hợp lệ
                          CONSTRAINT chk_birth_day CHECK (BirthDay BETWEEN 1 AND 31),
                          CONSTRAINT chk_birth_month CHECK (BirthMonth BETWEEN 1 AND 12),

    -- 🔥 đảm bảo BirthDay/BirthMonth khớp với DateOfBirth
                          CONSTRAINT chk_birth_match
                              CHECK (
                                  BirthDay = EXTRACT(DAY FROM DateOfBirth)::INT
                                  AND BirthMonth = EXTRACT(MONTH FROM DateOfBirth)::INT
)
    );

-- =========================
-- 4. BirthdayLog
-- =========================
CREATE TABLE birthdaylog (
                             logid BIGSERIAL PRIMARY KEY,
                             employeeid INT NOT NULL,
                             send_time TIMESTAMPTZ NOT NULL, -- thời điểm gửi thực tế
                             status VARCHAR(20) NOT NULL, -- SUCCESS / FAILED
                             channel VARCHAR(20) NOT NULL, -- EMAIL / WHATSAPP
                             message TEXT, -- nội dung đã gửi
                             errormessage TEXT,
                             createdat TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT chk_status
                                 CHECK (status IN ('SUCCESS', 'FAILED')),
                             CONSTRAINT chk_channel
                                 CHECK (channel IN ('EMAIL', 'WHATSAPP')),
                             CONSTRAINT fk_log_employee
                                 FOREIGN KEY (employeeid)
                                     REFERENCES public.employee(employeeid)
                                     ON DELETE CASCADE
);

-- =========================
-- 5. INDEX
-- =========================

CREATE INDEX idx_employee_birthday
    ON Employee (BirthMonth, BirthDay);

CREATE INDEX idx_log_employee_date
    ON BirthdayLog (EmployeeID, SentDate);

-- =========================
-- 6. INSERT DATA
-- =========================

-- Department
INSERT INTO Department (DepartmentCode, DepartmentName)
VALUES
    ('P.PTUD', 'Phòng Phát triển ứng dụng'),
    ('P.KT', 'Phòng Kỹ thuật'),
    ('BGD.CTY', 'Ban Giám đốc Công ty'),
    ('BGD.TT', 'Ban Giám đốc trung tâm');

-- Employee (phải truyền đủ 3 field)
INSERT INTO Employee (
    EmployeeCode, FullName, JobTitle, PhoneNumber, Email,
    DateOfBirth, BirthDay, BirthMonth, DepartmentID
)
VALUES
    ('000001', 'Nguyễn Văn A', 'DEV', '0901111111', 'user001@company.com', '2003-10-21', 21, 10, 1);



CREATE TABLE message_template (
                                  id SERIAL PRIMARY KEY,
                                  name VARCHAR(100) NOT NULL,
                                  type VARCHAR(50) NOT NULL,
                                  content TEXT NOT NULL,
                                  is_active BOOLEAN DEFAULT true,
                                  is_default BOOLEAN DEFAULT true,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO message_template (name, type, content)
VALUES (
           'Birthday Template Default',
           'BIRTHDAY',
           'Chúc mừng sinh nhật {jobTitle} {fullName}! 🎉
         Chúc bạn một ngày thật nhiều niềm vui, hạnh phúc và luôn tràn đầy năng lượng tích cực.
         Hy vọng tuổi mới sẽ mang đến cho bạn thật nhiều thành công trong công việc cũng như cuộc sống. 🎂'
       );


CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) DEFAULT 'USER',
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO public.users (username, "password", "role", is_active)
VALUES ('admin', 'ttcnkt@hpbd#2026', 'ADMIN', true);

CREATE EXTENSION IF NOT EXISTS pgcrypto;

UPDATE users
SET password = crypt('ttcnkt@hpbd#2026', gen_salt('bf'))
WHERE username = 'admin';

CREATE TABLE system_config (
                               id SERIAL PRIMARY KEY,
                               config_key VARCHAR(100) UNIQUE NOT NULL,
                               config_value VARCHAR(100) NOT NULL,
                               description TEXT
);

INSERT INTO system_config(config_key, config_value, description) VALUES
                                                                     ('BIRTHDAY_ENABLED', 'true', 'Bật/tắt gửi sinh nhật'),
                                                                     ('BIRTHDAY_HOUR', '8', 'Giờ gửi sinh nhật'),
                                                                     ('BIRTHDAY_MINUTE', '0', 'Phút gửi sinh nhật');