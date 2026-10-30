-- Top-level categories
INSERT INTO categories (name, name_bn, slug, sort_order) VALUES
    ('Bangladesh',     'বাংলাদেশ',     'bangladesh',     1),
    ('International',  'আন্তর্জাতিক',   'international',  2),
    ('Sports',         'খেলাধুলা',       'sports',         3),
    ('Business',       'ব্যবসা',         'business',       4),
    ('Technology',     'প্রযুক্তি',      'technology',     5),
    ('Entertainment',  'বিনোদন',        'entertainment',  6),
    ('Opinion',        'মতামত',         'opinion',        7),
    ('Health',         'স্বাস্থ্য',       'health',         8);

-- Bangladesh sub-categories
INSERT INTO categories (name, name_bn, slug, parent_id, sort_order)
SELECT 'Dhaka', 'ঢাকা', 'dhaka', id, 1 FROM categories WHERE slug = 'bangladesh';

INSERT INTO categories (name, name_bn, slug, parent_id, sort_order)
SELECT 'Chittagong', 'চট্টগ্রাম', 'chittagong', id, 2 FROM categories WHERE slug = 'bangladesh';

INSERT INTO categories (name, name_bn, slug, parent_id, sort_order)
SELECT 'Sylhet', 'সিলেট', 'sylhet', id, 3 FROM categories WHERE slug = 'bangladesh';

INSERT INTO categories (name, name_bn, slug, parent_id, sort_order)
SELECT 'Rajshahi', 'রাজশাহী', 'rajshahi', id, 4 FROM categories WHERE slug = 'bangladesh';

INSERT INTO categories (name, name_bn, slug, parent_id, sort_order)
SELECT 'Politics', 'রাজনীতি', 'bd-politics', id, 5 FROM categories WHERE slug = 'bangladesh';

-- Sports sub-categories
INSERT INTO categories (name, name_bn, slug, parent_id, sort_order)
SELECT 'Cricket', 'ক্রিকেট', 'cricket', id, 1 FROM categories WHERE slug = 'sports';

INSERT INTO categories (name, name_bn, slug, parent_id, sort_order)
SELECT 'Football', 'ফুটবল', 'football', id, 2 FROM categories WHERE slug = 'sports';

-- Default admin user (password: Admin@12345 — CHANGE IN PRODUCTION)
INSERT INTO users (username, email, password, full_name, role, is_active) VALUES (
    'admin',
    'admin@newsportal.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LetbY/C3sxN0t0V2e',
    'System Administrator',
    'ADMIN',
    TRUE
);
