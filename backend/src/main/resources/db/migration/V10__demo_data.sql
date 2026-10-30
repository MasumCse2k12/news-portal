-- =============================================================================
--  V10 — Demo seed data (articles, tags, article_tags)
--  Safe to run multiple times: uses DO blocks with existence checks.
-- =============================================================================

-- -------------------------------------------------------------------------
-- Tags
-- -------------------------------------------------------------------------
INSERT INTO tags (name, name_bn, slug) VALUES
    ('Politics',        'রাজনীতি',     'politics'),
    ('Economy',         'অর্থনীতি',    'economy'),
    ('Cricket',         'ক্রিকেট',     'cricket'),
    ('Technology',      'প্রযুক্তি',   'technology'),
    ('Climate',         'জলবায়ু',      'climate'),
    ('Education',       'শিক্ষা',      'education'),
    ('Health',          'স্বাস্থ্য',   'health'),
    ('Election',        'নির্বাচন',    'election'),
    ('AI',              'কৃত্রিম বুদ্ধিমত্তা', 'ai'),
    ('Breaking News',   'ব্রেকিং নিউজ', 'breaking-news')
ON CONFLICT (slug) DO NOTHING;

-- -------------------------------------------------------------------------
-- Demo journalist user (password: Demo@12345)
-- -------------------------------------------------------------------------
INSERT INTO users (username, email, password, full_name, role, is_active)
VALUES (
    'journalist1',
    'journalist1@newsportal.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LetbY/C3sxN0t0V2e',
    'Rahim Uddin',
    'JOURNALIST',
    TRUE
)
ON CONFLICT (username) DO NOTHING;

-- -------------------------------------------------------------------------
-- Articles  (all PUBLISHED, varying view counts so trending works)
-- -------------------------------------------------------------------------
DO $$
DECLARE
    v_author_id   UUID;
    v_cat_bd      INT;
    v_cat_intl    INT;
    v_cat_sports  INT;
    v_cat_biz     INT;
    v_cat_tech    INT;
    v_cat_health  INT;
    v_cat_cricket INT;

    -- article ids
    a1 UUID := gen_random_uuid();
    a2 UUID := gen_random_uuid();
    a3 UUID := gen_random_uuid();
    a4 UUID := gen_random_uuid();
    a5 UUID := gen_random_uuid();
    a6 UUID := gen_random_uuid();
    a7 UUID := gen_random_uuid();
    a8 UUID := gen_random_uuid();
    a9 UUID := gen_random_uuid();
    a10 UUID := gen_random_uuid();
    a11 UUID := gen_random_uuid();
    a12 UUID := gen_random_uuid();
BEGIN
    SELECT id INTO v_author_id FROM users WHERE username = 'admin' LIMIT 1;

    SELECT id INTO v_cat_bd      FROM categories WHERE slug = 'bangladesh'    LIMIT 1;
    SELECT id INTO v_cat_intl    FROM categories WHERE slug = 'international' LIMIT 1;
    SELECT id INTO v_cat_sports  FROM categories WHERE slug = 'sports'        LIMIT 1;
    SELECT id INTO v_cat_biz     FROM categories WHERE slug = 'business'      LIMIT 1;
    SELECT id INTO v_cat_tech    FROM categories WHERE slug = 'technology'    LIMIT 1;
    SELECT id INTO v_cat_health  FROM categories WHERE slug = 'health'        LIMIT 1;
    SELECT id INTO v_cat_cricket FROM categories WHERE slug = 'cricket'       LIMIT 1;

    -- ------------------------------------------------------------------
    -- 1. Breaking: Bangladesh flood relief
    -- ------------------------------------------------------------------
    INSERT INTO articles (id, title, title_bn, slug, summary, summary_bn, content,
                          status, author_id, category_id, is_breaking, is_featured,
                          view_count, published_at, created_at, updated_at)
    VALUES (a1,
        'Bangladesh Launches Major Flood Relief Operation Across Seven Districts',
        'সাত জেলায় বড় বন্যা ত্রাণ অভিযান শুরু করল বাংলাদেশ',
        'bangladesh-flood-relief-operation-2026',
        'The government has mobilised army, navy and disaster management teams to deliver emergency aid to over two million flood-affected residents.',
        'সরকার সেনা, নৌবাহিনী ও দুর্যোগ ব্যবস্থাপনা দলকে দুই কোটিরও বেশি বন্যাকবলিত বাসিন্দাদের জরুরি সহায়তা দিতে মোতায়েন করেছে।',
        '<p>Bangladesh has launched a large-scale flood relief operation spanning seven districts in the northern and central regions after record monsoon rains caused rivers to burst their banks. The National Disaster Management Council activated emergency protocols early this morning, dispatching more than 3,000 army personnel alongside navy rescue boats and helicopters.</p>
<p>Prime Minister addressed the nation, promising that no flood victim would be left without food, clean water, or shelter. Relief camps have been established at 142 upazila-level centres, each stocked with a week''s supply of dry food, oral rehydration salts, and blankets.</p>
<p>The UN Office for the Coordination of Humanitarian Affairs said it was closely monitoring the situation and stood ready to supplement national efforts if required. International NGOs including BRAC and Save the Children have already activated their own emergency response plans.</p>',
        'PUBLISHED', v_author_id, v_cat_bd, TRUE, TRUE,
        84200, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '2 hours')
    ON CONFLICT (slug) DO NOTHING;

    -- ------------------------------------------------------------------
    -- 2. Bangladesh politics
    -- ------------------------------------------------------------------
    INSERT INTO articles (id, title, title_bn, slug, summary, summary_bn, content,
                          status, author_id, category_id, is_breaking, is_featured,
                          view_count, published_at, created_at, updated_at)
    VALUES (a2,
        'National Election Commission Announces Voter Registration Drive for 2027 Polls',
        'জাতীয় নির্বাচন কমিশন ২০২৭ নির্বাচনের জন্য ভোটার নিবন্ধন অভিযান ঘোষণা করেছে',
        'election-commission-voter-registration-2027',
        'The Election Commission will open new registration centres in every union parishad from next month, targeting an additional five million first-time voters.',
        'নির্বাচন কমিশন আগামী মাস থেকে প্রতিটি ইউনিয়ন পরিষদে নতুন নিবন্ধন কেন্দ্র খুলবে এবং অতিরিক্ত পঞ্চাশ লাখ প্রথমবার ভোটারকে লক্ষ্য করবে।',
        '<p>The Bangladesh Election Commission on Thursday unveiled a three-month voter registration campaign ahead of the 2027 general election. Chief Election Commissioner said the drive aimed to enroll five million citizens who turned 18 since the last voter roll update.</p>
<p>Mobile registration units will travel to remote upazilas while a new smartphone app will allow urban residents to submit their biometric data from home. The Commission has also partnered with universities to set up on-campus booths targeting first-time student voters.</p>
<p>Opposition parties broadly welcomed the initiative but called for simultaneous audits of the existing voter list to remove deceased and duplicate registrations.</p>',
        'PUBLISHED', v_author_id, v_cat_bd, FALSE, TRUE,
        31500, NOW() - INTERVAL '5 hours', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '5 hours')
    ON CONFLICT (slug) DO NOTHING;

    -- ------------------------------------------------------------------
    -- 3. International — UN Climate Summit
    -- ------------------------------------------------------------------
    INSERT INTO articles (id, title, title_bn, slug, summary, summary_bn, content,
                          status, author_id, category_id, is_breaking, is_featured,
                          view_count, published_at, created_at, updated_at)
    VALUES (a3,
        'UN Climate Summit Reaches Landmark Deal on Loss and Damage Fund',
        'জাতিসংঘ জলবায়ু সম্মেলনে ক্ষয়ক্ষতি তহবিল নিয়ে ঐতিহাসিক চুক্তি',
        'un-climate-summit-loss-damage-fund-deal',
        'More than 190 nations agreed late Friday to a $500 billion loss and damage finance framework, the most ambitious climate pact in a decade.',
        'শুক্রবার রাতে ১৯০টিরও বেশি দেশ ৫০০ বিলিয়ন ডলারের একটি ক্ষতি ও ক্ষয় অর্থায়ন কাঠামোতে সম্মত হয়েছে।',
        '<p>Negotiators at the UN Climate Conference reached a breakthrough agreement early Saturday morning, establishing a $500 billion loss and damage fund that will channel money from wealthy nations to those most affected by climate change.</p>
<p>The deal, brokered after 36 hours of marathon negotiations, commits developed countries to front-loading contributions over the next five years. A dedicated secretariat will be housed at the World Bank but governed independently to ensure equitable access for small island states and least-developed countries.</p>
<p>Bangladesh, a leading voice in the vulnerable nations bloc, hailed the outcome. "This is not charity — it is climate justice," said the country''s chief negotiator. Environmental groups called it a historic step while cautioning that implementation would be the true test.</p>',
        'PUBLISHED', v_author_id, v_cat_intl, FALSE, TRUE,
        62800, NOW() - INTERVAL '8 hours', NOW() - INTERVAL '9 hours', NOW() - INTERVAL '8 hours')
    ON CONFLICT (slug) DO NOTHING;

    -- ------------------------------------------------------------------
    -- 4. Sports — Bangladesh cricket
    -- ------------------------------------------------------------------
    INSERT INTO articles (id, title, title_bn, slug, summary, summary_bn, content,
                          status, author_id, category_id, is_breaking, is_featured,
                          view_count, published_at, created_at, updated_at)
    VALUES (a4,
        'Bangladesh Stun Australia in Historic T20 World Cup Semifinal',
        'ঐতিহাসিক টি-টোয়েন্টি বিশ্বকাপ সেমিফাইনালে বাংলাদেশ অস্ট্রেলিয়াকে হারিয়েছে',
        'bangladesh-beat-australia-t20-world-cup-semifinal',
        'A blistering half-century from Towhid Hridoy and four wickets from Taskin Ahmed powered Bangladesh to a stunning six-wicket victory.',
        'তৌহিদ হৃদয়ের দুর্দান্ত অর্ধশতক এবং তাসকিন আহমেদের চার উইকেটে বাংলাদেশ ছয় উইকেটে অসাধারণ জয় পেয়েছে।',
        '<p>Bangladesh scripted one of their greatest victories in cricket history on Thursday, defeating Australia by six wickets in the T20 World Cup semifinal at the Sydney Cricket Ground to book a place in the final for the first time.</p>
<p>Chasing 168, Bangladesh were wobbling at 48 for 3 before Towhid Hridoy blazed 74 off 42 balls, putting on a match-winning stand of 98 with Mahmudullah. Taskin Ahmed had earlier dismantled the Australian top order, finishing with 4 for 22 from his four overs.</p>
<p>Thousands of jubilant fans flooded the streets of Dhaka, Chittagong, and Sylhet in scenes of unprecedented celebration. The BCB president called it "the greatest night in Bangladesh cricket."</p>',
        'PUBLISHED', v_author_id, COALESCE(v_cat_cricket, v_cat_sports), TRUE, TRUE,
        120000, NOW() - INTERVAL '12 hours', NOW() - INTERVAL '13 hours', NOW() - INTERVAL '12 hours')
    ON CONFLICT (slug) DO NOTHING;

    -- ------------------------------------------------------------------
    -- 5. Technology — AI
    -- ------------------------------------------------------------------
    INSERT INTO articles (id, title, title_bn, slug, summary, summary_bn, content,
                          status, author_id, category_id, is_breaking, is_featured,
                          view_count, published_at, created_at, updated_at)
    VALUES (a5,
        'Bangladesh Tech Startups Raise Record $1.2 Billion in 2026',
        '২০২৬ সালে বাংলাদেশ টেক স্টার্টআপ রেকর্ড ১.২ বিলিয়ন ডলার তুলেছে',
        'bangladesh-tech-startups-raise-record-2026',
        'Venture capital inflows into Bangladeshi technology companies hit an all-time high, driven by fintech, e-commerce logistics, and AI-powered agri-tech.',
        'ফিনটেক, ই-কমার্স লজিস্টিক্স এবং এআই-চালিত কৃষিপ্রযুক্তির কারণে বাংলাদেশি প্রযুক্তি কোম্পানিতে ভেঞ্চার ক্যাপিটাল প্রবাহ সর্বকালের সর্বোচ্চে পৌঁছেছে।',
        '<p>Bangladesh''s startup ecosystem reached a major milestone in 2026, attracting $1.2 billion in venture capital and private equity funding — a 65 percent jump from the previous year and the highest figure on record.</p>
<p>Fintech companies led the charge, accounting for $480 million of total investment, followed by logistics-tech at $310 million and agri-tech at $220 million. Notably, three startups crossed the $100 million valuation threshold this year, adding to the country''s growing roster of unicorn candidates.</p>
<p>Analysts credit a maturing regulatory environment, a young and digitally savvy population of 170 million, and improving broadband infrastructure for the surge. The government''s Digital Bangladesh 2.0 initiative, which provides tax holidays and co-investment facilities for qualifying startups, has also played a significant role.</p>',
        'PUBLISHED', v_author_id, v_cat_tech, FALSE, FALSE,
        47300, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day 1 hour', NOW() - INTERVAL '1 day')
    ON CONFLICT (slug) DO NOTHING;

    -- ------------------------------------------------------------------
    -- 6. Business
    -- ------------------------------------------------------------------
    INSERT INTO articles (id, title, title_bn, slug, summary, summary_bn, content,
                          status, author_id, category_id, is_breaking, is_featured,
                          view_count, published_at, created_at, updated_at)
    VALUES (a6,
        'Remittance Inflows Surge 22 Percent to $28 Billion in FY2026',
        'রেমিট্যান্স প্রবাহ ২২ শতাংশ বেড়ে ২০২৫-২৬ অর্থবছরে ২৮ বিলিয়ন ডলারে পৌঁছেছে',
        'remittance-inflows-surge-fy2026',
        'Bangladesh earned $28.4 billion in remittances in FY2026, driven by higher migration to Gulf countries and expanded mobile financial services.',
        'মধ্যপ্রাচ্যে অভিবাসন বৃদ্ধি এবং মোবাইল ফিনান্সিয়াল সার্ভিস সম্প্রসারণের কারণে বাংলাদেশ ২০২৫-২৬ অর্থবছরে ২৮.৪ বিলিয়ন ডলার রেমিট্যান্স আয় করেছে।',
        '<p>Bangladesh received $28.4 billion in remittances in the fiscal year ending June 2026, the Bangladesh Bank reported on Monday, representing a 22 percent increase over the previous year. The figure cements remittances as the country''s single largest source of foreign exchange, surpassing garment exports for the second consecutive year.</p>
<p>Saudi Arabia, the UAE, and Qatar remained the top sending countries, collectively accounting for 61 percent of inflows. The central bank attributed the jump in part to a crackdown on informal hundi channels and the rapid adoption of mobile financial services such as bKash and Nagad by overseas workers.</p>
<p>Economists said the record inflow would help rebuild foreign exchange reserves, which had come under pressure following a global commodity price spike. The taka appreciated slightly against the dollar on the news.</p>',
        'PUBLISHED', v_author_id, v_cat_biz, FALSE, FALSE,
        28900, NOW() - INTERVAL '1 day 4 hours', NOW() - INTERVAL '1 day 5 hours', NOW() - INTERVAL '1 day 4 hours')
    ON CONFLICT (slug) DO NOTHING;

    -- ------------------------------------------------------------------
    -- 7. International — Middle East
    -- ------------------------------------------------------------------
    INSERT INTO articles (id, title, title_bn, slug, summary, summary_bn, content,
                          status, author_id, category_id, is_breaking, is_featured,
                          view_count, published_at, created_at, updated_at)
    VALUES (a7,
        'G20 Leaders Agree on Global Minimum Tax Framework for Tech Giants',
        'জি-২০ নেতারা প্রযুক্তি জায়ান্টদের জন্য বৈশ্বিক ন্যূনতম কর কাঠামোতে সম্মত',
        'g20-global-minimum-tax-tech-giants',
        'G20 heads of state have endorsed a 15 percent global minimum corporate tax that will primarily target large multinational technology companies.',
        'জি-২০ রাষ্ট্রপ্রধানরা ১৫ শতাংশ বৈশ্বিক ন্যূনতম কর্পোরেট করের অনুমোদন দিয়েছেন যা প্রাথমিকভাবে বড় বহুজাতিক প্রযুক্তি কোম্পানিগুলোকে লক্ষ্য করবে।',
        '<p>G20 leaders formally endorsed a global minimum corporate tax of 15 percent at their annual summit, a deal expected to generate an additional $220 billion in annual tax revenue worldwide, much of it from large technology multinationals that have historically sheltered profits in low-tax jurisdictions.</p>
<p>The agreement, years in the making under the OECD''s Pillar Two framework, will require participating countries — which account for more than 85 percent of global GDP — to enact implementing legislation by end of 2027.</p>
<p>Tech industry lobby groups criticised the deal, warning of reduced investment in research and development. Developing nations, including Bangladesh, expressed cautious support but called for a larger share of the new revenues to flow to lower-income countries rather than wealthy governments.</p>',
        'PUBLISHED', v_author_id, v_cat_intl, FALSE, FALSE,
        19400, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days 1 hour', NOW() - INTERVAL '2 days')
    ON CONFLICT (slug) DO NOTHING;

    -- ------------------------------------------------------------------
    -- 8. Health
    -- ------------------------------------------------------------------
    INSERT INTO articles (id, title, title_bn, slug, summary, summary_bn, content,
                          status, author_id, category_id, is_breaking, is_featured,
                          view_count, published_at, created_at, updated_at)
    VALUES (a8,
        'WHO Approves New Dengue Vaccine Effective Against All Four Serotypes',
        'বিশ্ব স্বাস্থ্য সংস্থা চার ধরনের ডেঙ্গুর বিরুদ্ধে কার্যকর নতুন ভ্যাকসিন অনুমোদন করেছে',
        'who-approves-dengue-vaccine-all-serotypes',
        'The World Health Organization has approved a new dengue vaccine showing 87 percent efficacy across all four dengue serotypes in phase-three trials.',
        'বিশ্ব স্বাস্থ্য সংস্থা একটি নতুন ডেঙ্গু ভ্যাকসিন অনুমোদন করেছে যা তৃতীয় পর্যায়ের পরীক্ষায় চার ধরনের ডেঙ্গুর বিরুদ্ধে ৮৭ শতাংশ কার্যকারিতা দেখিয়েছে।',
        '<p>The World Health Organization on Wednesday approved a new dengue vaccine developed by a Japanese-Thai consortium that demonstrated 87 percent efficacy against all four dengue virus serotypes in large-scale phase-three trials across 12 countries, including Bangladesh.</p>
<p>The vaccine, administered in a two-dose schedule six months apart, showed a particularly strong 91 percent efficacy against severe dengue, the form responsible for hospitalisation and death. Unlike an earlier vaccine, it can be safely given to individuals regardless of prior dengue infection status.</p>
<p>Bangladesh, which reported over 300,000 dengue cases in 2025, has already placed an order for 10 million doses and plans to begin a phased rollout starting with Dhaka and Chittagong in the first quarter of 2027. The Directorate General of Health Services said the vaccine would initially be targeted at school-age children aged 6–16.</p>',
        'PUBLISHED', v_author_id, v_cat_health, FALSE, FALSE,
        55100, NOW() - INTERVAL '2 days 6 hours', NOW() - INTERVAL '2 days 7 hours', NOW() - INTERVAL '2 days 6 hours')
    ON CONFLICT (slug) DO NOTHING;

    -- ------------------------------------------------------------------
    -- 9. Technology — AI in Bangladesh
    -- ------------------------------------------------------------------
    INSERT INTO articles (id, title, title_bn, slug, summary, summary_bn, content,
                          status, author_id, category_id, is_breaking, is_featured,
                          view_count, published_at, created_at, updated_at)
    VALUES (a9,
        'Bangladesh Launches AI-Powered National Language Model for Bengali',
        'বাংলাদেশ বাংলার জন্য এআই-চালিত জাতীয় ভাষা মডেল চালু করেছে',
        'bangladesh-ai-national-language-model-bengali',
        'The ICT Division unveiled BanglaBERT-2, a large language model trained on 100 billion tokens of Bengali text, for use in public services and education.',
        'আইসিটি বিভাগ বাংলাBERT-2 উন্মোচন করেছে, একটি বড় ভাষা মডেল যা ১০০ বিলিয়ন টোকেন বাংলা পাঠ্যে প্রশিক্ষিত এবং সরকারি সেবা ও শিক্ষায় ব্যবহারের জন্য।',
        '<p>Bangladesh''s ICT Division on Tuesday launched BanglaBERT-2, the country''s first homegrown large language model, trained on 100 billion tokens of Bengali text sourced from news archives, books, government documents, and social media. The model is designed to power chatbots, document summarisation, and translation tools across public services.</p>
<p>The AI system will initially be integrated into the Bangladesh National Digital Portal, allowing citizens to query government services, check application status, and receive guidance in natural Bengali. A dedicated API will be made available free of charge to domestic developers, startups, and educational institutions.</p>
<p>Minister for ICT said the initiative would help bridge the digital divide for the 170 million Bengalis who are not comfortable with English-centric AI tools. Independent researchers praised the effort but cautioned that the model would require ongoing fine-tuning to reduce hallucinations in specialised domains such as legal and medical information.</p>',
        'PUBLISHED', v_author_id, v_cat_tech, FALSE, FALSE,
        39700, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days 1 hour', NOW() - INTERVAL '3 days')
    ON CONFLICT (slug) DO NOTHING;

    -- ------------------------------------------------------------------
    -- 10. Bangladesh — Economy
    -- ------------------------------------------------------------------
    INSERT INTO articles (id, title, title_bn, slug, summary, summary_bn, content,
                          status, author_id, category_id, is_breaking, is_featured,
                          view_count, published_at, created_at, updated_at)
    VALUES (a10,
        'Bangladesh GDP Growth Hits 7.8 Percent in FY2026, Fastest in Five Years',
        '২০২৫-২৬ অর্থবছরে বাংলাদেশের জিডিপি প্রবৃদ্ধি পাঁচ বছরে সর্বোচ্চ ৭.৮ শতাংশে পৌঁছেছে',
        'bangladesh-gdp-growth-7-8-percent-fy2026',
        'The Bangladesh Bureau of Statistics confirmed 7.8 percent GDP growth in FY2026, the strongest performance since 2019, driven by exports and domestic consumption.',
        'বাংলাদেশ পরিসংখ্যান ব্যুরো ২০২৫-২৬ অর্থবছরে ৭.৮ শতাংশ জিডিপি প্রবৃদ্ধি নিশ্চিত করেছে যা ২০১৯ সালের পর সবচেয়ে শক্তিশালী প্রদর্শনী।',
        '<p>Bangladesh''s economy expanded by 7.8 percent in the fiscal year ending June 2026, the Bangladesh Bureau of Statistics announced on Wednesday, the fastest growth rate since FY2019 and well above the government''s original 7.0 percent target.</p>
<p>The strong performance was underpinned by a 12 percent surge in garment and textile exports, record remittance receipts, and resilient domestic consumption fuelled by rising real wages. The agriculture sector posted 3.4 percent growth despite mid-season flooding, reflecting improved crop varieties and flood-resistant farming techniques.</p>
<p>The World Bank and ADB both revised their growth forecasts upward, noting that Bangladesh had successfully navigated post-pandemic inflationary pressures better than many peer economies. However, economists cautioned that sustaining the trajectory would require continued investment in infrastructure, power, and human capital.</p>',
        'PUBLISHED', v_author_id, v_cat_biz, FALSE, FALSE,
        22600, NOW() - INTERVAL '3 days 8 hours', NOW() - INTERVAL '3 days 9 hours', NOW() - INTERVAL '3 days 8 hours')
    ON CONFLICT (slug) DO NOTHING;

    -- ------------------------------------------------------------------
    -- 11. International — Space
    -- ------------------------------------------------------------------
    INSERT INTO articles (id, title, title_bn, slug, summary, summary_bn, content,
                          status, author_id, category_id, is_breaking, is_featured,
                          view_count, published_at, created_at, updated_at)
    VALUES (a11,
        'NASA and ESA Confirm Discovery of Earth-Like Exoplanet with Liquid Water',
        'নাসা ও ইএসএ তরল পানিসহ পৃথিবীর মতো বহির্গ্রহ আবিষ্কারের নিশ্চিত করেছে',
        'nasa-esa-earth-like-exoplanet-liquid-water',
        'Astronomers using the James Webb Space Telescope have confirmed the presence of liquid water oceans on Kepler-442c, 1,200 light-years from Earth.',
        'জেমস ওয়েব স্পেস টেলিস্কোপ ব্যবহার করে জ্যোতির্বিজ্ঞানীরা পৃথিবী থেকে ১,২০০ আলোকবর্ষ দূরে কেপলার-৪৪২সি-তে তরল পানির মহাসাগরের উপস্থিতি নিশ্চিত করেছেন।',
        '<p>In what scientists are calling the most significant astronomical discovery of the century, NASA and ESA jointly announced on Friday that the James Webb Space Telescope has confirmed the presence of liquid water oceans on Kepler-442c, a rocky exoplanet roughly 1.3 times the size of Earth located 1,200 light-years away in the constellation Lyra.</p>
<p>Spectroscopic analysis of the planet''s atmosphere revealed signatures of water vapour, molecular oxygen, and methane in ratios that strongly suggest active biological or geochemical processes. The planet orbits within the habitable zone of its star and has surface temperatures estimated between 5 and 22 degrees Celsius.</p>
<p>While scientists cautioned that the findings do not constitute direct proof of extraterrestrial life, they described the discovery as "a paradigm shift" in the search for habitable worlds beyond our solar system. A dedicated follow-up observation campaign using the Extremely Large Telescope in Chile is planned for 2028.</p>',
        'PUBLISHED', v_author_id, v_cat_intl, FALSE, FALSE,
        91200, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days 1 hour', NOW() - INTERVAL '4 days')
    ON CONFLICT (slug) DO NOTHING;

    -- ------------------------------------------------------------------
    -- 12. Sports — Football
    -- ------------------------------------------------------------------
    INSERT INTO articles (id, title, title_bn, slug, summary, summary_bn, content,
                          status, author_id, category_id, is_breaking, is_featured,
                          view_count, published_at, created_at, updated_at)
    VALUES (a12,
        'FIFA World Cup 2026 Final: Brazil vs France in a Classic for the Ages',
        'ফিফা বিশ্বকাপ ২০২৬ ফাইনাল: ব্রাজিল বনাম ফ্রান্স — যুগের সেরা ক্লাসিক',
        'fifa-world-cup-2026-final-brazil-france',
        'Brazil edged France 3-2 in extra time in a pulsating World Cup final watched by a record 1.8 billion viewers worldwide.',
        'রোমাঞ্চকর বিশ্বকাপ ফাইনালে ব্রাজিল অতিরিক্ত সময়ে ফ্রান্সকে ৩-২ গোলে হারিয়েছে, বিশ্বজুড়ে রেকর্ড ১৮০ কোটি দর্শক ম্যাচটি উপভোগ করেছেন।',
        '<p>Brazil claimed their sixth FIFA World Cup title on Sunday with a breathtaking 3-2 victory over France in extra time at the MetLife Stadium in New Jersey, in what many pundits are already calling the greatest final in the tournament''s 96-year history.</p>
<p>Vinicius Jr. opened the scoring in the 12th minute before Mbappe equalised with a stunning volley. Rodrygo put Brazil ahead just before half-time, only for Camavinga to draw France level with a towering header in the 78th minute. The tie looked destined for penalties until Endrick, 19, ghosted in at the far post to head home a corner in the 108th minute to seal a legendary victory.</p>
<p>An estimated 1.8 billion viewers worldwide watched the match, a new record for a single sporting event. The streets of Rio de Janeiro erupted in scenes of carnival-like celebration lasting through the night. For France, consolation came in the form of Mbappe''s Golden Boot, having scored 9 goals across the tournament.</p>',
        'PUBLISHED', v_author_id, v_cat_sports, FALSE, FALSE,
        145000, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days 1 hour', NOW() - INTERVAL '5 days')
    ON CONFLICT (slug) DO NOTHING;

    -- ------------------------------------------------------------------
    -- Article–Tag associations
    -- ------------------------------------------------------------------
    INSERT INTO article_tags (article_id, tag_id)
    SELECT a1, id FROM tags WHERE slug IN ('breaking-news', 'climate') ON CONFLICT DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a2, id FROM tags WHERE slug IN ('politics', 'election') ON CONFLICT DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a3, id FROM tags WHERE slug IN ('climate', 'breaking-news') ON CONFLICT DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a4, id FROM tags WHERE slug IN ('cricket', 'breaking-news') ON CONFLICT DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a5, id FROM tags WHERE slug IN ('technology', 'economy') ON CONFLICT DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a6, id FROM tags WHERE slug IN ('economy', 'politics') ON CONFLICT DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a7, id FROM tags WHERE slug IN ('economy', 'politics') ON CONFLICT DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a8, id FROM tags WHERE slug IN ('health') ON CONFLICT DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a9, id FROM tags WHERE slug IN ('technology', 'ai') ON CONFLICT DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a10, id FROM tags WHERE slug IN ('economy') ON CONFLICT DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a11, id FROM tags WHERE slug IN ('technology') ON CONFLICT DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a12, id FROM tags WHERE slug IN ('cricket') ON CONFLICT DO NOTHING;

END $$;
