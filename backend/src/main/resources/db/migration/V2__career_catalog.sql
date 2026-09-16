CREATE TABLE careers (
    id UUID PRIMARY KEY,
    slug VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1500) NOT NULL,
    entry_difficulty VARCHAR(30) NOT NULL,
    degree_relevance VARCHAR(1000) NOT NULL,
    project_expectations VARCHAR(1500) NOT NULL,
    internship_expectations VARCHAR(1500) NOT NULL,
    common_misconceptions VARCHAR(1500) NOT NULL,
    reality_summary VARCHAR(2000) NOT NULL,
    recommended_weekly_hours INTEGER NOT NULL,
    demo_data BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_careers_slug UNIQUE (slug),
    CONSTRAINT uk_careers_name UNIQUE (name),
    CONSTRAINT ck_careers_weekly_hours CHECK (recommended_weekly_hours BETWEEN 1 AND 60)
);

CREATE TABLE career_skills (
    id UUID PRIMARY KEY,
    career_id UUID NOT NULL,
    skill_id UUID NOT NULL,
    importance INTEGER NOT NULL,
    requirement VARCHAR(30) NOT NULL,
    CONSTRAINT fk_career_skills_career FOREIGN KEY (career_id) REFERENCES careers (id) ON DELETE CASCADE,
    CONSTRAINT fk_career_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id),
    CONSTRAINT uk_career_skill_career_skill UNIQUE (career_id, skill_id),
    CONSTRAINT ck_career_skills_importance CHECK (importance BETWEEN 1 AND 5)
);

CREATE INDEX idx_career_skills_career ON career_skills (career_id);
CREATE INDEX idx_career_skills_skill ON career_skills (skill_id);

CREATE TABLE career_responsibilities (
    career_id UUID NOT NULL REFERENCES careers (id) ON DELETE CASCADE,
    responsibility VARCHAR(400) NOT NULL,
    PRIMARY KEY (career_id, responsibility)
);

CREATE TABLE career_job_titles (
    career_id UUID NOT NULL REFERENCES careers (id) ON DELETE CASCADE,
    job_title VARCHAR(150) NOT NULL,
    PRIMARY KEY (career_id, job_title)
);

CREATE TABLE career_risks (
    career_id UUID NOT NULL REFERENCES careers (id) ON DELETE CASCADE,
    risk VARCHAR(500) NOT NULL,
    PRIMARY KEY (career_id, risk)
);

CREATE TABLE career_market_considerations (
    career_id UUID NOT NULL REFERENCES careers (id) ON DELETE CASCADE,
    consideration VARCHAR(500) NOT NULL,
    PRIMARY KEY (career_id, consideration)
);

CREATE TABLE career_interest_signals (
    career_id UUID NOT NULL REFERENCES careers (id) ON DELETE CASCADE,
    signal VARCHAR(100) NOT NULL,
    PRIMARY KEY (career_id, signal)
);

CREATE TABLE career_goal_signals (
    career_id UUID NOT NULL REFERENCES careers (id) ON DELETE CASCADE,
    signal VARCHAR(100) NOT NULL,
    PRIMARY KEY (career_id, signal)
);

-- Controlled reference catalog. It is explicitly DEMO DATA and is not live market evidence.
INSERT INTO careers VALUES
('20000000-0000-0000-0000-000000000001', 'backend-developer', 'Backend Developer',
 'Builds server-side applications, APIs, data access layers, authentication, and integrations that power software products.',
 'MEDIUM', 'A BCA or CS degree can support entry, but demonstrable programming, database, API, testing, and project skills matter more than the degree label alone.',
 'A useful portfolio normally includes a tested REST API, authentication and authorization, relational data modeling, documentation, and a deployable service.',
 'Internships help demonstrate teamwork and production exposure, but well-explained projects can provide initial evidence when internships are unavailable.',
 'Knowing one programming language is not the same as being ready to build reliable production services.',
 'Backend work rewards depth in programming, HTTP, data modeling, security, and testing. Framework knowledge without fundamentals is fragile.',
 10, TRUE, TRUE, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000002', 'full-stack-developer', 'Full Stack Developer',
 'Works across browser interfaces, application APIs, databases, and deployment boundaries to deliver complete product features.',
 'MEDIUM', 'A computing degree is relevant, while employers also expect evidence that the student can connect frontend and backend concerns coherently.',
 'Projects should show a usable interface, API, database, authentication, validation, testing, and a clear deployment story.',
 'Internships are valuable because full-stack work involves coordination across several layers; a polished capstone can also show this integration.',
 'Full stack does not mean mastering every framework. Entry-level candidates need a coherent core stack and strong fundamentals.',
 'Breadth is useful only when it rests on enough depth to debug and explain each layer. Avoid collecting technologies without completing products.',
 12, TRUE, TRUE, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000003', 'frontend-developer', 'Frontend Developer',
 'Creates accessible, responsive, maintainable browser experiences and connects them to application APIs.',
 'MEDIUM', 'A degree can help with general computing foundations, but accessible interfaces, JavaScript depth, browser knowledge, and portfolio quality are direct evidence.',
 'A portfolio should demonstrate semantic HTML, responsive layouts, state and data handling, accessibility, testing, and performance awareness.',
 'Internships provide useful product and design collaboration experience; independent projects should document accessibility and technical decisions.',
 'Frontend engineering is not only visual styling. It includes browser behavior, data flow, accessibility, performance, and software design.',
 'Framework skill changes quickly; durable value comes from web fundamentals, accessible interaction design, and the ability to diagnose browser behavior.',
 10, TRUE, TRUE, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000004', 'mobile-developer', 'Mobile Developer',
 'Builds applications for mobile devices using native or cross-platform technologies and platform-specific interaction patterns.',
 'MEDIUM', 'A computing degree is useful, while published or demonstrable mobile applications are strong evidence of platform skills.',
 'Projects should include offline/error states, API integration, device-aware interaction, testing, and a release-quality build.',
 'Internships can expose students to platform release processes. A small, completed application is more useful than many unfinished tutorials.',
 'Cross-platform tools reduce duplication but do not eliminate the need to understand mobile lifecycle, performance, and platform conventions.',
 'Mobile entry paths require a focused platform choice. Learn application fundamentals before adding several native and cross-platform stacks.',
 10, TRUE, TRUE, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000005', 'data-analyst', 'Data Analyst',
 'Transforms data into clear analyses, reports, dashboards, and decision-support insights for stakeholders.',
 'LOW', 'Degrees vary widely in analytics roles. Evidence of SQL, statistics, careful analysis, communication, and relevant projects is important.',
 'Projects should start with a question, clean and validate data, explain methods, show reproducible SQL or analysis, and communicate limitations.',
 'Internships help build domain context. Public datasets can support a portfolio when their source and limitations are clearly documented.',
 'Creating a dashboard is not the same as producing a trustworthy analysis; data quality, reasoning, and communication matter.',
 'Data analysis can be accessible at entry level, but strong candidates explain assumptions and uncertainty instead of only presenting charts.',
 8, TRUE, TRUE, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000006', 'data-engineer', 'Data Engineer',
 'Designs and operates reliable data pipelines, storage models, transformations, and data platforms for analytics and applications.',
 'HIGH', 'A computing degree is relevant, but entry often requires stronger programming, SQL, systems, and data modeling depth than basic analytics roles.',
 'Projects should demonstrate ingestion, validation, transformations, data modeling, idempotency, testing, monitoring, and failure recovery.',
 'Internships are especially useful because production-scale data concerns are difficult to reproduce fully in personal projects.',
 'Learning one orchestration or big-data tool does not replace SQL, programming, modeling, and reliability fundamentals.',
 'Data engineering is a plausible progression from backend or analytics work. Direct entry is possible but normally requires substantial systems evidence.',
 14, TRUE, TRUE, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000007', 'ai-ml-engineer', 'AI/ML Engineer',
 'Builds, evaluates, integrates, and operates machine-learning systems using programming, mathematics, data, and software-engineering practices.',
 'VERY_HIGH', 'Some entry roles favor advanced qualifications, but strong programming, mathematics, experiments, and deployed projects can still provide evidence.',
 'Projects should establish a baseline, document data and metrics, prevent leakage, evaluate limitations, and expose a reproducible inference path.',
 'Research or engineering internships are highly valuable because realistic datasets, evaluation, and operational constraints are hard to simulate.',
 'Market attention around AI does not imply easy entry-level access or remove the need for programming, mathematics, and evaluation foundations.',
 'AI/ML can be a long-term direction for interested students. Beginners should establish Python, statistics, linear algebra, data handling, and software basics before advanced models or MLOps.',
 18, TRUE, TRUE, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000008', 'cloud-devops-engineer', 'Cloud/DevOps Engineer',
 'Improves software delivery and reliability through automation, infrastructure, deployment pipelines, observability, and operational practices.',
 'HIGH', 'A computing degree helps, while Linux, networking, scripting, deployment, and troubleshooting evidence is central.',
 'Projects should deploy an application through a repeatable pipeline with containers, configuration, health checks, logs, monitoring, and rollback thinking.',
 'Operational internships provide valuable real-system exposure. Home labs should remain safe, documented, and focused on reproducible automation.',
 'Cloud certifications alone do not demonstrate the ability to operate, debug, or secure systems.',
 'Direct entry exists but many practitioners transition from development, support, or systems roles. Fundamentals matter before complex orchestration.',
 14, TRUE, TRUE, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000009', 'cybersecurity-analyst', 'Cybersecurity Analyst',
 'Identifies, investigates, communicates, and helps reduce security risks across applications, systems, networks, and organizational processes.',
 'HIGH', 'Degrees and certifications may help screening, but practical networking, operating-system, security, scripting, and analytical evidence remains important.',
 'Safe labs should demonstrate threat analysis, hardening, logging, vulnerability interpretation, and responsible reporting without attacking unauthorized systems.',
 'Internships or supervised labs are valuable because professional security work depends on authorization, process, and careful communication.',
 'Cybersecurity is not one entry-level job and a certification does not guarantee employment. The field contains distinct specialties with different foundations.',
 'Students should build networking, Linux, scripting, and security fundamentals before advanced offensive tools. All practice must remain authorized and ethical.',
 14, TRUE, TRUE, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-00000000000a', 'qa-automation-engineer', 'QA Automation Engineer',
 'Designs tests and automation that expose product risks, improve feedback, and support reliable software delivery.',
 'MEDIUM', 'A computing degree is relevant; strong testing reasoning, programming, API knowledge, and reliable automation provide direct evidence.',
 'Projects should show a test strategy, meaningful cases, API and UI automation, stable fixtures, failure diagnostics, and CI execution.',
 'Internships help students understand product risk and collaboration. Personal automation should avoid brittle tests written only for demonstration.',
 'Automation is not clicking record and replay. Valuable QA work requires risk analysis, test design, debugging, and maintainable code.',
 'QA automation can offer a practical software entry path, but candidates still need programming and testing depth rather than tool-only knowledge.',
 10, TRUE, TRUE, CURRENT_TIMESTAMP);

-- Shared skills are inserted only when a Phase 1 user has not already created the same normalized skill.
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000001', 'Java', 'java', 'Programming language', 'General-purpose language used in backend and enterprise development' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'java');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000002', 'Spring Boot', 'spring boot', 'Backend', 'Java framework for production application and API development' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'spring boot');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000003', 'SQL', 'sql', 'Data', 'Language for querying and managing relational data' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'sql');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000004', 'REST APIs', 'rest apis', 'Backend', 'HTTP-oriented application interface design and implementation' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'rest apis');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000005', 'Git', 'git', 'Engineering practice', 'Distributed version control and collaborative source history' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'git');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000006', 'Docker', 'docker', 'Platform', 'Container image, runtime, and development workflow fundamentals' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'docker');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000007', 'Software Testing', 'software testing', 'Engineering practice', 'Test design, automation boundaries, and feedback practices' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'software testing');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000008', 'JavaScript', 'javascript', 'Programming language', 'Programming language of the web platform' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'javascript');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000009', 'TypeScript', 'typescript', 'Programming language', 'Typed JavaScript for maintainable applications' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'typescript');
INSERT INTO skills SELECT '10000000-0000-0000-0000-00000000000a', 'React', 'react', 'Frontend', 'Component-based browser user-interface library' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'react');
INSERT INTO skills SELECT '10000000-0000-0000-0000-00000000000b', 'HTML', 'html', 'Frontend', 'Semantic structure for web documents and applications' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'html');
INSERT INTO skills SELECT '10000000-0000-0000-0000-00000000000c', 'CSS', 'css', 'Frontend', 'Responsive presentation and layout for web interfaces' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'css');
INSERT INTO skills SELECT '10000000-0000-0000-0000-00000000000d', 'Node.js', 'node.js', 'Backend', 'JavaScript runtime for server-side applications' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'node.js');
INSERT INTO skills SELECT '10000000-0000-0000-0000-00000000000e', 'Python', 'python', 'Programming language', 'General-purpose language used in data, automation, and machine learning' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'python');
INSERT INTO skills SELECT '10000000-0000-0000-0000-00000000000f', 'Statistics', 'statistics', 'Mathematics', 'Reasoning about data, uncertainty, samples, and quantitative evidence' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'statistics');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000010', 'Spreadsheets', 'spreadsheets', 'Analytics', 'Structured analysis using spreadsheet tools' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'spreadsheets');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000011', 'Power BI', 'power bi', 'Analytics', 'Business-intelligence modeling and visualization tool' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'power bi');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000012', 'Data Modeling', 'data modeling', 'Data', 'Designing structured, meaningful, and maintainable data representations' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'data modeling');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000013', 'ETL', 'etl', 'Data', 'Reliable extraction, transformation, and loading of data' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'etl');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000014', 'Apache Spark', 'apache spark', 'Data', 'Distributed processing engine for larger data workloads' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'apache spark');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000015', 'Cloud Fundamentals', 'cloud fundamentals', 'Platform', 'Core compute, networking, storage, identity, and cost concepts' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'cloud fundamentals');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000016', 'Linux', 'linux', 'Platform', 'Command-line, process, file, permission, and service fundamentals' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'linux');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000017', 'CI/CD', 'ci/cd', 'Engineering practice', 'Automated build, test, and delivery workflows' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'ci/cd');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000018', 'Networking', 'networking', 'Platform', 'Core network protocols, addressing, routing, DNS, and troubleshooting' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'networking');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000019', 'Security Fundamentals', 'security fundamentals', 'Security', 'Threat, vulnerability, identity, risk, and defense fundamentals' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'security fundamentals');
INSERT INTO skills SELECT '10000000-0000-0000-0000-00000000001a', 'OWASP', 'owasp', 'Security', 'Application-security risks and defensive verification practices' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'owasp');
INSERT INTO skills SELECT '10000000-0000-0000-0000-00000000001b', 'Test Automation', 'test automation', 'Quality', 'Maintainable programmatic checks for product behavior and risk' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'test automation');
INSERT INTO skills SELECT '10000000-0000-0000-0000-00000000001c', 'Selenium', 'selenium', 'Quality', 'Browser automation tooling for user-interface checks' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'selenium');
INSERT INTO skills SELECT '10000000-0000-0000-0000-00000000001d', 'Mobile Development', 'mobile development', 'Mobile', 'Mobile application lifecycle, interaction, storage, and API fundamentals' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'mobile development');
INSERT INTO skills SELECT '10000000-0000-0000-0000-00000000001e', 'Kotlin', 'kotlin', 'Programming language', 'Language commonly used for Android development' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'kotlin');
INSERT INTO skills SELECT '10000000-0000-0000-0000-00000000001f', 'Swift', 'swift', 'Programming language', 'Language commonly used for Apple-platform development' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'swift');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000020', 'Data Structures and Algorithms', 'data structures and algorithms', 'Computer science', 'Program design, complexity, and foundational problem-solving structures' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'data structures and algorithms');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000021', 'Machine Learning', 'machine learning', 'AI/ML', 'Model training, evaluation, feature, and generalization fundamentals' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'machine learning');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000022', 'Linear Algebra', 'linear algebra', 'Mathematics', 'Vectors, matrices, transformations, and mathematical model foundations' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'linear algebra');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000023', 'Deep Learning', 'deep learning', 'AI/ML', 'Neural-network concepts, training, evaluation, and limitations' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'deep learning');
INSERT INTO skills SELECT '10000000-0000-0000-0000-000000000024', 'System Design', 'system design', 'Software architecture', 'Reasoning about components, trade-offs, scale, and reliability' WHERE NOT EXISTS (SELECT 1 FROM skills WHERE normalized_name = 'system design');

-- Career-to-skill relations use the shared normalized skill graph.
INSERT INTO career_skills SELECT '30000000-0000-0000-0001-000000000001', '20000000-0000-0000-0000-000000000001', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'java';
INSERT INTO career_skills SELECT '30000000-0000-0000-0001-000000000002', '20000000-0000-0000-0000-000000000001', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'spring boot';
INSERT INTO career_skills SELECT '30000000-0000-0000-0001-000000000003', '20000000-0000-0000-0000-000000000001', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'sql';
INSERT INTO career_skills SELECT '30000000-0000-0000-0001-000000000004', '20000000-0000-0000-0000-000000000001', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'rest apis';
INSERT INTO career_skills SELECT '30000000-0000-0000-0001-000000000005', '20000000-0000-0000-0000-000000000001', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'git';
INSERT INTO career_skills SELECT '30000000-0000-0000-0001-000000000006', '20000000-0000-0000-0000-000000000001', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'software testing';
INSERT INTO career_skills SELECT '30000000-0000-0000-0001-000000000007', '20000000-0000-0000-0000-000000000001', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'docker';
INSERT INTO career_skills SELECT '30000000-0000-0000-0001-000000000008', '20000000-0000-0000-0000-000000000001', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'system design';

INSERT INTO career_skills SELECT '30000000-0000-0000-0002-000000000001', '20000000-0000-0000-0000-000000000002', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'javascript';
INSERT INTO career_skills SELECT '30000000-0000-0000-0002-000000000002', '20000000-0000-0000-0000-000000000002', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'typescript';
INSERT INTO career_skills SELECT '30000000-0000-0000-0002-000000000003', '20000000-0000-0000-0000-000000000002', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'react';
INSERT INTO career_skills SELECT '30000000-0000-0000-0002-000000000004', '20000000-0000-0000-0000-000000000002', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'rest apis';
INSERT INTO career_skills SELECT '30000000-0000-0000-0002-000000000005', '20000000-0000-0000-0000-000000000002', id, 3, 'REQUIRED' FROM skills WHERE normalized_name = 'sql';
INSERT INTO career_skills SELECT '30000000-0000-0000-0002-000000000006', '20000000-0000-0000-0000-000000000002', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'git';
INSERT INTO career_skills SELECT '30000000-0000-0000-0002-000000000007', '20000000-0000-0000-0000-000000000002', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'node.js';
INSERT INTO career_skills SELECT '30000000-0000-0000-0002-000000000008', '20000000-0000-0000-0000-000000000002', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'docker';

INSERT INTO career_skills SELECT '30000000-0000-0000-0003-000000000001', '20000000-0000-0000-0000-000000000003', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'javascript';
INSERT INTO career_skills SELECT '30000000-0000-0000-0003-000000000002', '20000000-0000-0000-0000-000000000003', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'typescript';
INSERT INTO career_skills SELECT '30000000-0000-0000-0003-000000000003', '20000000-0000-0000-0000-000000000003', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'react';
INSERT INTO career_skills SELECT '30000000-0000-0000-0003-000000000004', '20000000-0000-0000-0000-000000000003', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'html';
INSERT INTO career_skills SELECT '30000000-0000-0000-0003-000000000005', '20000000-0000-0000-0000-000000000003', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'css';
INSERT INTO career_skills SELECT '30000000-0000-0000-0003-000000000006', '20000000-0000-0000-0000-000000000003', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'software testing';
INSERT INTO career_skills SELECT '30000000-0000-0000-0003-000000000007', '20000000-0000-0000-0000-000000000003', id, 3, 'REQUIRED' FROM skills WHERE normalized_name = 'rest apis';

INSERT INTO career_skills SELECT '30000000-0000-0000-0004-000000000001', '20000000-0000-0000-0000-000000000004', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'mobile development';
INSERT INTO career_skills SELECT '30000000-0000-0000-0004-000000000002', '20000000-0000-0000-0000-000000000004', id, 4, 'PREFERRED' FROM skills WHERE normalized_name = 'kotlin';
INSERT INTO career_skills SELECT '30000000-0000-0000-0004-000000000003', '20000000-0000-0000-0000-000000000004', id, 4, 'PREFERRED' FROM skills WHERE normalized_name = 'swift';
INSERT INTO career_skills SELECT '30000000-0000-0000-0004-000000000004', '20000000-0000-0000-0000-000000000004', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'rest apis';
INSERT INTO career_skills SELECT '30000000-0000-0000-0004-000000000005', '20000000-0000-0000-0000-000000000004', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'software testing';
INSERT INTO career_skills SELECT '30000000-0000-0000-0004-000000000006', '20000000-0000-0000-0000-000000000004', id, 3, 'REQUIRED' FROM skills WHERE normalized_name = 'git';
INSERT INTO career_skills SELECT '30000000-0000-0000-0004-000000000007', '20000000-0000-0000-0000-000000000004', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'javascript';

INSERT INTO career_skills SELECT '30000000-0000-0000-0005-000000000001', '20000000-0000-0000-0000-000000000005', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'sql';
INSERT INTO career_skills SELECT '30000000-0000-0000-0005-000000000002', '20000000-0000-0000-0000-000000000005', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'statistics';
INSERT INTO career_skills SELECT '30000000-0000-0000-0005-000000000003', '20000000-0000-0000-0000-000000000005', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'spreadsheets';
INSERT INTO career_skills SELECT '30000000-0000-0000-0005-000000000004', '20000000-0000-0000-0000-000000000005', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'data modeling';
INSERT INTO career_skills SELECT '30000000-0000-0000-0005-000000000005', '20000000-0000-0000-0000-000000000005', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'power bi';
INSERT INTO career_skills SELECT '30000000-0000-0000-0005-000000000006', '20000000-0000-0000-0000-000000000005', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'python';

INSERT INTO career_skills SELECT '30000000-0000-0000-0006-000000000001', '20000000-0000-0000-0000-000000000006', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'sql';
INSERT INTO career_skills SELECT '30000000-0000-0000-0006-000000000002', '20000000-0000-0000-0000-000000000006', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'python';
INSERT INTO career_skills SELECT '30000000-0000-0000-0006-000000000003', '20000000-0000-0000-0000-000000000006', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'data modeling';
INSERT INTO career_skills SELECT '30000000-0000-0000-0006-000000000004', '20000000-0000-0000-0000-000000000006', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'etl';
INSERT INTO career_skills SELECT '30000000-0000-0000-0006-000000000005', '20000000-0000-0000-0000-000000000006', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'apache spark';
INSERT INTO career_skills SELECT '30000000-0000-0000-0006-000000000006', '20000000-0000-0000-0000-000000000006', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'docker';
INSERT INTO career_skills SELECT '30000000-0000-0000-0006-000000000007', '20000000-0000-0000-0000-000000000006', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'cloud fundamentals';

INSERT INTO career_skills SELECT '30000000-0000-0000-0007-000000000001', '20000000-0000-0000-0000-000000000007', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'python';
INSERT INTO career_skills SELECT '30000000-0000-0000-0007-000000000002', '20000000-0000-0000-0000-000000000007', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'statistics';
INSERT INTO career_skills SELECT '30000000-0000-0000-0007-000000000003', '20000000-0000-0000-0000-000000000007', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'linear algebra';
INSERT INTO career_skills SELECT '30000000-0000-0000-0007-000000000004', '20000000-0000-0000-0000-000000000007', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'machine learning';
INSERT INTO career_skills SELECT '30000000-0000-0000-0007-000000000005', '20000000-0000-0000-0000-000000000007', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'data structures and algorithms';
INSERT INTO career_skills SELECT '30000000-0000-0000-0007-000000000006', '20000000-0000-0000-0000-000000000007', id, 3, 'REQUIRED' FROM skills WHERE normalized_name = 'sql';
INSERT INTO career_skills SELECT '30000000-0000-0000-0007-000000000007', '20000000-0000-0000-0000-000000000007', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'deep learning';

INSERT INTO career_skills SELECT '30000000-0000-0000-0008-000000000001', '20000000-0000-0000-0000-000000000008', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'linux';
INSERT INTO career_skills SELECT '30000000-0000-0000-0008-000000000002', '20000000-0000-0000-0000-000000000008', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'docker';
INSERT INTO career_skills SELECT '30000000-0000-0000-0008-000000000003', '20000000-0000-0000-0000-000000000008', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'ci/cd';
INSERT INTO career_skills SELECT '30000000-0000-0000-0008-000000000004', '20000000-0000-0000-0000-000000000008', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'networking';
INSERT INTO career_skills SELECT '30000000-0000-0000-0008-000000000005', '20000000-0000-0000-0000-000000000008', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'cloud fundamentals';
INSERT INTO career_skills SELECT '30000000-0000-0000-0008-000000000006', '20000000-0000-0000-0000-000000000008', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'git';
INSERT INTO career_skills SELECT '30000000-0000-0000-0008-000000000007', '20000000-0000-0000-0000-000000000008', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'security fundamentals';

INSERT INTO career_skills SELECT '30000000-0000-0000-0009-000000000001', '20000000-0000-0000-0000-000000000009', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'networking';
INSERT INTO career_skills SELECT '30000000-0000-0000-0009-000000000002', '20000000-0000-0000-0000-000000000009', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'linux';
INSERT INTO career_skills SELECT '30000000-0000-0000-0009-000000000003', '20000000-0000-0000-0000-000000000009', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'security fundamentals';
INSERT INTO career_skills SELECT '30000000-0000-0000-0009-000000000004', '20000000-0000-0000-0000-000000000009', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'owasp';
INSERT INTO career_skills SELECT '30000000-0000-0000-0009-000000000005', '20000000-0000-0000-0000-000000000009', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'python';
INSERT INTO career_skills SELECT '30000000-0000-0000-0009-000000000006', '20000000-0000-0000-0000-000000000009', id, 2, 'PREFERRED' FROM skills WHERE normalized_name = 'git';

INSERT INTO career_skills SELECT '30000000-0000-0000-000a-000000000001', '20000000-0000-0000-0000-00000000000a', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'software testing';
INSERT INTO career_skills SELECT '30000000-0000-0000-000a-000000000002', '20000000-0000-0000-0000-00000000000a', id, 5, 'REQUIRED' FROM skills WHERE normalized_name = 'test automation';
INSERT INTO career_skills SELECT '30000000-0000-0000-000a-000000000003', '20000000-0000-0000-0000-00000000000a', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'selenium';
INSERT INTO career_skills SELECT '30000000-0000-0000-000a-000000000004', '20000000-0000-0000-0000-00000000000a', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'rest apis';
INSERT INTO career_skills SELECT '30000000-0000-0000-000a-000000000005', '20000000-0000-0000-0000-00000000000a', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'java';
INSERT INTO career_skills SELECT '30000000-0000-0000-000a-000000000006', '20000000-0000-0000-0000-00000000000a', id, 3, 'PREFERRED' FROM skills WHERE normalized_name = 'ci/cd';
INSERT INTO career_skills SELECT '30000000-0000-0000-000a-000000000007', '20000000-0000-0000-0000-00000000000a', id, 4, 'REQUIRED' FROM skills WHERE normalized_name = 'git';

-- The catalog is controlled editorial data. Market notes deliberately describe
-- validation questions rather than presenting time-sensitive demand claims.
INSERT INTO career_responsibilities (career_id, responsibility) VALUES
('20000000-0000-0000-0000-000000000001', 'Design and maintain APIs and server-side business logic'),
('20000000-0000-0000-0000-000000000001', 'Model data and improve reliability, performance, and security'),
('20000000-0000-0000-0000-000000000002', 'Build product features across browser, API, and database layers'),
('20000000-0000-0000-0000-000000000002', 'Test, deploy, and monitor complete web experiences'),
('20000000-0000-0000-0000-000000000003', 'Turn product designs into accessible responsive interfaces'),
('20000000-0000-0000-0000-000000000003', 'Manage client state, API integration, testing, and performance'),
('20000000-0000-0000-0000-000000000004', 'Build and test applications for Android or iOS devices'),
('20000000-0000-0000-0000-000000000004', 'Integrate device features, APIs, storage, and release workflows'),
('20000000-0000-0000-0000-000000000005', 'Clean, query, and interpret data for business questions'),
('20000000-0000-0000-0000-000000000005', 'Create dashboards and communicate evidence with stakeholders'),
('20000000-0000-0000-0000-000000000006', 'Build dependable ingestion and transformation pipelines'),
('20000000-0000-0000-0000-000000000006', 'Design data models and operate data platforms'),
('20000000-0000-0000-0000-000000000007', 'Prepare data and train, evaluate, and deploy models'),
('20000000-0000-0000-0000-000000000007', 'Measure model quality, limitations, drift, and operational impact'),
('20000000-0000-0000-0000-000000000008', 'Automate builds, deployments, infrastructure, and observability'),
('20000000-0000-0000-0000-000000000008', 'Improve system availability, recovery, security, and cost control'),
('20000000-0000-0000-0000-000000000009', 'Assess systems for vulnerabilities and suspicious activity'),
('20000000-0000-0000-0000-000000000009', 'Strengthen controls and support incident response'),
('20000000-0000-0000-0000-00000000000a', 'Design risk-based test plans and automated checks'),
('20000000-0000-0000-0000-00000000000a', 'Investigate failures and improve release confidence');

INSERT INTO career_job_titles (career_id, job_title) VALUES
('20000000-0000-0000-0000-000000000001', 'Backend Developer'), ('20000000-0000-0000-0000-000000000001', 'Java Developer'),
('20000000-0000-0000-0000-000000000002', 'Full-Stack Developer'), ('20000000-0000-0000-0000-000000000002', 'Software Engineer'),
('20000000-0000-0000-0000-000000000003', 'Frontend Developer'), ('20000000-0000-0000-0000-000000000003', 'UI Engineer'),
('20000000-0000-0000-0000-000000000004', 'Mobile App Developer'), ('20000000-0000-0000-0000-000000000004', 'Android or iOS Developer'),
('20000000-0000-0000-0000-000000000005', 'Data Analyst'), ('20000000-0000-0000-0000-000000000005', 'Business Intelligence Analyst'),
('20000000-0000-0000-0000-000000000006', 'Data Engineer'), ('20000000-0000-0000-0000-000000000006', 'Analytics Engineer'),
('20000000-0000-0000-0000-000000000007', 'Machine Learning Engineer'), ('20000000-0000-0000-0000-000000000007', 'Applied AI Engineer'),
('20000000-0000-0000-0000-000000000008', 'Cloud Engineer'), ('20000000-0000-0000-0000-000000000008', 'DevOps Engineer'),
('20000000-0000-0000-0000-000000000009', 'Security Analyst'), ('20000000-0000-0000-0000-000000000009', 'Security Engineer'),
('20000000-0000-0000-0000-00000000000a', 'QA Automation Engineer'), ('20000000-0000-0000-0000-00000000000a', 'Software Development Engineer in Test');

INSERT INTO career_risks (career_id, risk) VALUES
('20000000-0000-0000-0000-000000000001', 'Framework knowledge without debugging and database fundamentals is fragile'),
('20000000-0000-0000-0000-000000000001', 'Production systems require security and operational ownership'),
('20000000-0000-0000-0000-000000000002', 'Breadth can delay depth in any one layer'),
('20000000-0000-0000-0000-000000000002', 'End-to-end ownership increases context switching'),
('20000000-0000-0000-0000-000000000003', 'Tooling changes quickly while browser fundamentals remain essential'),
('20000000-0000-0000-0000-000000000003', 'Visual polish alone does not demonstrate engineering depth'),
('20000000-0000-0000-0000-000000000004', 'Platform-specific release and device behavior add complexity'),
('20000000-0000-0000-0000-000000000004', 'A tutorial clone is weaker evidence than a tested original app'),
('20000000-0000-0000-0000-000000000005', 'Dashboards without sound definitions can mislead decisions'),
('20000000-0000-0000-0000-000000000005', 'Communication and business context matter as much as tools'),
('20000000-0000-0000-0000-000000000006', 'Poor data quality and observability can silently corrupt outputs'),
('20000000-0000-0000-0000-000000000006', 'Distributed systems add substantial operational complexity'),
('20000000-0000-0000-0000-000000000007', 'The entry path requires durable mathematics and software foundations'),
('20000000-0000-0000-0000-000000000007', 'Model demos are not production systems without evaluation and monitoring'),
('20000000-0000-0000-0000-000000000008', 'Automation without systems knowledge can amplify mistakes'),
('20000000-0000-0000-0000-000000000008', 'On-call and reliability expectations vary significantly by employer'),
('20000000-0000-0000-0000-000000000009', 'Certifications alone do not prove practical investigation skills'),
('20000000-0000-0000-0000-000000000009', 'Safe, authorized practice environments are essential'),
('20000000-0000-0000-0000-00000000000a', 'Automation cannot replace product understanding and exploratory testing'),
('20000000-0000-0000-0000-00000000000a', 'Brittle tests can slow delivery rather than improve confidence');

INSERT INTO career_market_considerations (career_id, consideration) VALUES
('20000000-0000-0000-0000-000000000001', 'Validate the dominant backend stack and junior expectations in your target region.'),
('20000000-0000-0000-0000-000000000002', 'Compare full-stack breadth expectations across startups, agencies, and larger teams.'),
('20000000-0000-0000-0000-000000000003', 'Check whether local roles emphasize product UI, framework depth, or design systems.'),
('20000000-0000-0000-0000-000000000004', 'Compare native and cross-platform hiring patterns for your target employers.'),
('20000000-0000-0000-0000-000000000005', 'Inspect postings for domain knowledge and the reporting tools used locally.'),
('20000000-0000-0000-0000-000000000006', 'Check whether entry roles expect cloud platforms, orchestration, or analytics tooling.'),
('20000000-0000-0000-0000-000000000007', 'Distinguish research-heavy roles from applied engineering and data science roles.'),
('20000000-0000-0000-0000-000000000008', 'Review local expectations for cloud providers, on-call work, and prior operations experience.'),
('20000000-0000-0000-0000-000000000009', 'Compare defensive security, compliance, and application-security entry paths.'),
('20000000-0000-0000-0000-00000000000a', 'Check how employers divide manual testing, automation, and developer-owned quality.');

INSERT INTO career_interest_signals (career_id, signal) VALUES
('20000000-0000-0000-0000-000000000001', 'backend'), ('20000000-0000-0000-0000-000000000001', 'java'), ('20000000-0000-0000-0000-000000000001', 'apis'), ('20000000-0000-0000-0000-000000000001', 'databases'),
('20000000-0000-0000-0000-000000000002', 'full stack'), ('20000000-0000-0000-0000-000000000002', 'web applications'), ('20000000-0000-0000-0000-000000000002', 'frontend and backend'), ('20000000-0000-0000-0000-000000000002', 'javascript'),
('20000000-0000-0000-0000-000000000003', 'frontend'), ('20000000-0000-0000-0000-000000000003', 'user interfaces'), ('20000000-0000-0000-0000-000000000003', 'web design'), ('20000000-0000-0000-0000-000000000003', 'accessibility'),
('20000000-0000-0000-0000-000000000004', 'mobile'), ('20000000-0000-0000-0000-000000000004', 'android'), ('20000000-0000-0000-0000-000000000004', 'ios'), ('20000000-0000-0000-0000-000000000004', 'apps'),
('20000000-0000-0000-0000-000000000005', 'data analysis'), ('20000000-0000-0000-0000-000000000005', 'dashboards'), ('20000000-0000-0000-0000-000000000005', 'business insights'), ('20000000-0000-0000-0000-000000000005', 'statistics'),
('20000000-0000-0000-0000-000000000006', 'data engineering'), ('20000000-0000-0000-0000-000000000006', 'pipelines'), ('20000000-0000-0000-0000-000000000006', 'databases'), ('20000000-0000-0000-0000-000000000006', 'distributed systems'),
('20000000-0000-0000-0000-000000000007', 'artificial intelligence'), ('20000000-0000-0000-0000-000000000007', 'machine learning'), ('20000000-0000-0000-0000-000000000007', 'mathematics'), ('20000000-0000-0000-0000-000000000007', 'data science'),
('20000000-0000-0000-0000-000000000008', 'cloud'), ('20000000-0000-0000-0000-000000000008', 'devops'), ('20000000-0000-0000-0000-000000000008', 'infrastructure'), ('20000000-0000-0000-0000-000000000008', 'automation'),
('20000000-0000-0000-0000-000000000009', 'cybersecurity'), ('20000000-0000-0000-0000-000000000009', 'security'), ('20000000-0000-0000-0000-000000000009', 'networks'), ('20000000-0000-0000-0000-000000000009', 'ethical hacking'),
('20000000-0000-0000-0000-00000000000a', 'testing'), ('20000000-0000-0000-0000-00000000000a', 'quality assurance'), ('20000000-0000-0000-0000-00000000000a', 'automation'), ('20000000-0000-0000-0000-00000000000a', 'debugging');

INSERT INTO career_goal_signals (career_id, signal) VALUES
('20000000-0000-0000-0000-000000000001', 'software developer'), ('20000000-0000-0000-0000-000000000001', 'backend developer'), ('20000000-0000-0000-0000-000000000001', 'build apis'), ('20000000-0000-0000-0000-000000000001', 'server-side'),
('20000000-0000-0000-0000-000000000002', 'software developer'), ('20000000-0000-0000-0000-000000000002', 'full stack'), ('20000000-0000-0000-0000-000000000002', 'build products'), ('20000000-0000-0000-0000-000000000002', 'web developer'),
('20000000-0000-0000-0000-000000000003', 'frontend developer'), ('20000000-0000-0000-0000-000000000003', 'web developer'), ('20000000-0000-0000-0000-000000000003', 'user experience'), ('20000000-0000-0000-0000-000000000003', 'build interfaces'),
('20000000-0000-0000-0000-000000000004', 'mobile developer'), ('20000000-0000-0000-0000-000000000004', 'android developer'), ('20000000-0000-0000-0000-000000000004', 'ios developer'), ('20000000-0000-0000-0000-000000000004', 'publish apps'),
('20000000-0000-0000-0000-000000000005', 'data analyst'), ('20000000-0000-0000-0000-000000000005', 'analytics'), ('20000000-0000-0000-0000-000000000005', 'business intelligence'), ('20000000-0000-0000-0000-000000000005', 'insights'),
('20000000-0000-0000-0000-000000000006', 'data engineer'), ('20000000-0000-0000-0000-000000000006', 'data platform'), ('20000000-0000-0000-0000-000000000006', 'pipelines'), ('20000000-0000-0000-0000-000000000006', 'analytics engineering'),
('20000000-0000-0000-0000-000000000007', 'ai engineer'), ('20000000-0000-0000-0000-000000000007', 'ml engineer'), ('20000000-0000-0000-0000-000000000007', 'data scientist'), ('20000000-0000-0000-0000-000000000007', 'intelligent systems'),
('20000000-0000-0000-0000-000000000008', 'devops engineer'), ('20000000-0000-0000-0000-000000000008', 'cloud engineer'), ('20000000-0000-0000-0000-000000000008', 'site reliability'), ('20000000-0000-0000-0000-000000000008', 'automate deployment'),
('20000000-0000-0000-0000-000000000009', 'security analyst'), ('20000000-0000-0000-0000-000000000009', 'cybersecurity'), ('20000000-0000-0000-0000-000000000009', 'protect systems'), ('20000000-0000-0000-0000-000000000009', 'security engineer'),
('20000000-0000-0000-0000-00000000000a', 'qa engineer'), ('20000000-0000-0000-0000-00000000000a', 'test automation'), ('20000000-0000-0000-0000-00000000000a', 'software quality'), ('20000000-0000-0000-0000-00000000000a', 'automation engineer');
