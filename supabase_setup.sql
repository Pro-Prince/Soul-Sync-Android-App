-- ============================================================================
-- SOULSYNC UNIFIED SUPABASE INITIALIZATION DATABASE SCRIPT (UUID IDENTITIES)
-- ============================================================================
-- Run this script in the Supabase SQL Editor to configure all tables, indexes,
-- RLS policies, automatic triggers, and storage buckets in one single execution.

-- ----------------------------------------------------------------------------
-- 1. CLEANUP: DROP PRE-EXISTING TRIGGERS, FUNCTIONS, AND TABLES
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
DROP FUNCTION IF EXISTS public.handle_new_user() CASCADE;

DROP TABLE IF EXISTS public.achievements CASCADE;
DROP TABLE IF EXISTS public.app_settings CASCADE;
DROP TABLE IF EXISTS public.cycle_logs CASCADE;
DROP TABLE IF EXISTS public.cycle_periods CASCADE;
DROP TABLE IF EXISTS public.diary_entries CASCADE;
DROP TABLE IF EXISTS public.mood_logs CASCADE;
DROP TABLE IF EXISTS public.user_accounts CASCADE;

-- ----------------------------------------------------------------------------
-- 2. TABLE CREATION: DEFINING STRICT SCHEMAS MATCHING ROOM & COMPOSABLE DTOS
-- ----------------------------------------------------------------------------

-- Table: user_accounts (using UUID id as Primary Identity, email as editable profile property)
CREATE TABLE public.user_accounts (
    id UUID NOT NULL PRIMARY KEY,
    email TEXT NOT NULL,
    password_key TEXT NOT NULL,
    display_name TEXT NOT NULL
);

-- Table: app_settings (scoped by UUID user_id)
CREATE TABLE public.app_settings (
    user_id UUID NOT NULL PRIMARY KEY REFERENCES public.user_accounts(id) ON DELETE CASCADE,
    color_theme TEXT NOT NULL DEFAULT 'Lavender Romance',
    dark_mode BOOLEAN NOT NULL DEFAULT FALSE,
    period_tracker_enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- Table: diary_entries (scoped by UUID user_id)
CREATE TABLE public.diary_entries (
    id TEXT NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.user_accounts(id) ON DELETE CASCADE,
    date TEXT NOT NULL,
    title TEXT,
    content TEXT NOT NULL,
    content_plain TEXT NOT NULL,
    mood TEXT NOT NULL,
    voice_note_path TEXT,
    image_uris TEXT,
    video_path TEXT,
    hashtags TEXT,
    ai_summary TEXT,
    ai_pattern TEXT,
    ai_next_step TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

-- Table: cycle_periods (scoped by UUID user_id)
CREATE TABLE public.cycle_periods (
    id TEXT NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.user_accounts(id) ON DELETE CASCADE,
    start_date TEXT NOT NULL,
    end_date TEXT,
    average_cycle_length INTEGER NOT NULL DEFAULT 28
);

-- Table: cycle_logs (scoped by UUID user_id)
CREATE TABLE public.cycle_logs (
    id TEXT NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.user_accounts(id) ON DELETE CASCADE,
    date TEXT NOT NULL,
    flow TEXT,
    pain_level INTEGER,
    mood TEXT,
    symptoms TEXT,
    emotional_score INTEGER,
    stress_score INTEGER,
    supported_score INTEGER,
    anxiety_score INTEGER,
    loved_score INTEGER,
    confidence_score INTEGER,
    energy_score INTEGER,
    water_ml INTEGER,
    exercise_min INTEGER,
    medication TEXT,
    notes TEXT,
    created_at BIGINT NOT NULL
);

-- Table: mood_logs (scoped by UUID user_id)
CREATE TABLE public.mood_logs (
    id TEXT NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.user_accounts(id) ON DELETE CASCADE,
    date TEXT NOT NULL,
    mood TEXT NOT NULL,
    created_at BIGINT NOT NULL
);

-- Table: achievements (scoped by UUID user_id)
CREATE TABLE public.achievements (
    id TEXT NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.user_accounts(id) ON DELETE CASCADE,
    unlocked_at BIGINT
);

-- ----------------------------------------------------------------------------
-- 3. INDEXES: OPTIMIZE QUERY PERFORMANCE & SOLVE UNINDEXED FOREIGN KEYS WARNINGS
-- ----------------------------------------------------------------------------
CREATE INDEX idx_app_settings_user_id ON public.app_settings(user_id);
CREATE INDEX idx_diary_entries_user_id ON public.diary_entries(user_id);
CREATE INDEX idx_cycle_periods_user_id ON public.cycle_periods(user_id);
CREATE INDEX idx_cycle_logs_user_id ON public.cycle_logs(user_id);
CREATE INDEX idx_mood_logs_user_id ON public.mood_logs(user_id);
CREATE INDEX idx_achievements_user_id ON public.achievements(user_id);

-- ----------------------------------------------------------------------------
-- 4. ROW LEVEL SECURITY (RLS): ENABLING DATA ISOLATION & ACCESS CONTROL
-- ----------------------------------------------------------------------------
ALTER TABLE public.user_accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.diary_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cycle_periods ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cycle_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.mood_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.achievements ENABLE ROW LEVEL SECURITY;

-- ----------------------------------------------------------------------------
-- 5. RLS POLICIES: COMPACT, ENFORCED SECURITY MAPPED TO JWT auth.uid() UUID
-- ----------------------------------------------------------------------------

-- Table: user_accounts policies
CREATE POLICY "user_accounts_all" ON public.user_accounts
    FOR ALL
    USING (id = auth.uid())
    WITH CHECK (id = auth.uid());

-- Table: app_settings policies
CREATE POLICY "app_settings_all" ON public.app_settings
    FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- Table: diary_entries policies
CREATE POLICY "diary_entries_all" ON public.diary_entries
    FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- Table: cycle_periods policies
CREATE POLICY "cycle_periods_all" ON public.cycle_periods
    FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- Table: cycle_logs policies
CREATE POLICY "cycle_logs_all" ON public.cycle_logs
    FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- Table: mood_logs policies
CREATE POLICY "mood_logs_all" ON public.mood_logs
    FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- Table: achievements policies
CREATE POLICY "achievements_all" ON public.achievements
    FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- ----------------------------------------------------------------------------
-- 6. AUTOMATION: AUTH SYNC TRIGGER WITH RESOLVED MUTABLE SEARCH PATH WARNING
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    -- 1. Create public.user_accounts record
    INSERT INTO public.user_accounts (id, email, password_key, display_name)
    VALUES (
        new.id,
        new.email,
        '', -- Password kept blank for OAuth or handled securely by GoTrue
        COALESCE(new.raw_user_meta_data->>'full_name', new.raw_user_meta_data->>'display_name', split_part(new.email, '@', 1))
    )
    ON CONFLICT (id) DO UPDATE 
    SET email = COALESCE(excluded.email, public.user_accounts.email),
        display_name = COALESCE(excluded.display_name, public.user_accounts.display_name);

    -- 2. Initialize default public.app_settings
    INSERT INTO public.app_settings (user_id, color_theme, dark_mode, period_tracker_enabled)
    VALUES (new.id, 'Lavender Romance', false, true)
    ON CONFLICT (user_id) DO NOTHING;

    RETURN new;
END;
$$;

-- Secure function execution context
REVOKE ALL ON FUNCTION public.handle_new_user() FROM public;

-- Register Trigger
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- ----------------------------------------------------------------------------
-- 7. STORAGE: INITIALIZATION AND ACCESS RULES
-- ----------------------------------------------------------------------------

-- Ensure 'user-media' bucket exists
INSERT INTO storage.buckets (id, name, public)
VALUES ('user-media', 'user-media', true)
ON CONFLICT (id) DO NOTHING;

-- Dropping prior storage object policies
DROP POLICY IF EXISTS "storage_select_policy" ON storage.objects;
DROP POLICY IF EXISTS "storage_insert_policy" ON storage.objects;
DROP POLICY IF EXISTS "storage_update_policy" ON storage.objects;
DROP POLICY IF EXISTS "storage_delete_policy" ON storage.objects;

-- Creating optimized storage policies for bucket 'user-media'
CREATE POLICY "storage_select_policy" ON storage.objects
    FOR SELECT
    USING (bucket_id = 'user-media');

CREATE POLICY "storage_insert_policy" ON storage.objects
    FOR INSERT
    WITH CHECK (bucket_id = 'user-media');

CREATE POLICY "storage_update_policy" ON storage.objects
    FOR UPDATE
    USING (bucket_id = 'user-media')
    WITH CHECK (bucket_id = 'user-media');

CREATE POLICY "storage_delete_policy" ON storage.objects
    FOR DELETE
    USING (bucket_id = 'user-media');

-- ============================================================================
-- END OF SCRIPT - SOULSYNC DATABASE INITIALIZED SUCCESSFULLY
-- ============================================================================
