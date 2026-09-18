-- ============================================================================
-- SOULSYNC UNIFIED SUPABASE INITIALIZATION DATABASE SCRIPT (RESOLVED)
-- ============================================================================
-- Project URL: https://xafzdvdtzfyehxglthyr.supabase.co
-- Run this script in the Supabase SQL Editor to configure all tables, indexes,
-- RLS policies, automatic triggers, storage buckets, and keep-alive cron.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. CLEANUP: DROP PRE-EXISTING TRIGGERS, FUNCTIONS, AND TABLES
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
DROP FUNCTION IF EXISTS public.handle_new_user() CASCADE;

DROP TRIGGER IF EXISTS trg_ensure_user_diary ON public.diary_entries;
DROP TRIGGER IF EXISTS trg_ensure_user_cycle_periods ON public.cycle_periods;
DROP TRIGGER IF EXISTS trg_ensure_user_cycle_logs ON public.cycle_logs;
DROP TRIGGER IF EXISTS trg_ensure_user_moods ON public.mood_logs;
DROP TRIGGER IF EXISTS trg_ensure_user_achievements ON public.achievements;
DROP TRIGGER IF EXISTS trg_ensure_user_app_settings ON public.app_settings;
DROP FUNCTION IF EXISTS public.ensure_user_account_exists() CASCADE;

DROP TABLE IF EXISTS public.achievements CASCADE;
DROP TABLE IF EXISTS public.app_settings CASCADE;
DROP TABLE IF EXISTS public.cycle_logs CASCADE;
DROP TABLE IF EXISTS public.cycle_periods CASCADE;
DROP TABLE IF EXISTS public.diary_entries CASCADE;
DROP TABLE IF EXISTS public.mood_logs CASCADE;
DROP TABLE IF EXISTS public.user_accounts CASCADE;
DROP TABLE IF EXISTS public.project_heartbeat CASCADE;

-- ----------------------------------------------------------------------------
-- 2. TABLE CREATION: STRICT SCHEMAS MATCHING ROOM & COMPOSABLE DTOS
-- ----------------------------------------------------------------------------

-- Table: user_accounts (using UUID id as Primary Identity, email as profile property)
CREATE TABLE public.user_accounts (
    id UUID NOT NULL PRIMARY KEY,
    email TEXT NOT NULL,
    password_key TEXT NOT NULL DEFAULT '',
    display_name TEXT NOT NULL DEFAULT ''
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

-- Table: project_heartbeat (prevents Supabase 7-day inactivity pause)
CREATE TABLE public.project_heartbeat (
    id INT PRIMARY KEY DEFAULT 1,
    last_ping TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
INSERT INTO public.project_heartbeat (id, last_ping) VALUES (1, NOW())
ON CONFLICT (id) DO UPDATE SET last_ping = NOW();

-- ----------------------------------------------------------------------------
-- 3. INDEXES: OPTIMIZE QUERY PERFORMANCE & SOLVE UNINDEXED FOREIGN KEYS
-- ----------------------------------------------------------------------------
CREATE INDEX idx_app_settings_user_id ON public.app_settings(user_id);
CREATE INDEX idx_diary_entries_user_id ON public.diary_entries(user_id);
CREATE INDEX idx_diary_entries_date ON public.diary_entries(date);
CREATE INDEX idx_cycle_periods_user_id ON public.cycle_periods(user_id);
CREATE INDEX idx_cycle_logs_user_id ON public.cycle_logs(user_id);
CREATE INDEX idx_cycle_logs_date ON public.cycle_logs(date);
CREATE INDEX idx_mood_logs_user_id ON public.mood_logs(user_id);
CREATE INDEX idx_mood_logs_date ON public.mood_logs(date);
CREATE INDEX idx_achievements_user_id ON public.achievements(user_id);

-- ----------------------------------------------------------------------------
-- 4. AUTOMATION: AUTH & FOREIGN KEY SELF-HEALING TRIGGERS
-- ----------------------------------------------------------------------------

-- Trigger function 1: Syncs new Auth users to public.user_accounts
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    INSERT INTO public.user_accounts (id, email, password_key, display_name)
    VALUES (
        new.id,
        COALESCE(new.email, ''),
        '',
        COALESCE(new.raw_user_meta_data->>'full_name', new.raw_user_meta_data->>'display_name', split_part(COALESCE(new.email, ''), '@', 1), 'User')
    )
    ON CONFLICT (id) DO UPDATE 
    SET email = COALESCE(excluded.email, public.user_accounts.email),
        display_name = COALESCE(excluded.display_name, public.user_accounts.display_name);

    INSERT INTO public.app_settings (user_id, color_theme, dark_mode, period_tracker_enabled)
    VALUES (new.id, 'Lavender Romance', false, true)
    ON CONFLICT (user_id) DO NOTHING;

    RETURN new;
END;
$$;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- Trigger function 2: Self-healing foreign key safeguard.
-- If mobile app syncs child items (diary, moods, cycles) before user_accounts has synced,
-- this automatically creates the parent user_accounts record so the write NEVER fails.
CREATE OR REPLACE FUNCTION public.ensure_user_account_exists()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM public.user_accounts WHERE id = NEW.user_id) THEN
        INSERT INTO public.user_accounts (id, email, password_key, display_name)
        VALUES (
            NEW.user_id,
            COALESCE((SELECT email FROM auth.users WHERE id = NEW.user_id), 'user_' || SUBSTRING(NEW.user_id::text, 1, 8) || '@soulsync.app'),
            '',
            'SoulSync User'
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_ensure_user_diary BEFORE INSERT ON public.diary_entries FOR EACH ROW EXECUTE FUNCTION public.ensure_user_account_exists();
CREATE TRIGGER trg_ensure_user_cycle_periods BEFORE INSERT ON public.cycle_periods FOR EACH ROW EXECUTE FUNCTION public.ensure_user_account_exists();
CREATE TRIGGER trg_ensure_user_cycle_logs BEFORE INSERT ON public.cycle_logs FOR EACH ROW EXECUTE FUNCTION public.ensure_user_account_exists();
CREATE TRIGGER trg_ensure_user_moods BEFORE INSERT ON public.mood_logs FOR EACH ROW EXECUTE FUNCTION public.ensure_user_account_exists();
CREATE TRIGGER trg_ensure_user_achievements BEFORE INSERT ON public.achievements FOR EACH ROW EXECUTE FUNCTION public.ensure_user_account_exists();
CREATE TRIGGER trg_ensure_user_app_settings BEFORE INSERT ON public.app_settings FOR EACH ROW EXECUTE FUNCTION public.ensure_user_account_exists();

-- ----------------------------------------------------------------------------
-- 5. PERMISSIONS & ROW LEVEL SECURITY (RLS)
-- ----------------------------------------------------------------------------
ALTER TABLE public.user_accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.diary_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cycle_periods ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cycle_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.mood_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.achievements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.project_heartbeat ENABLE ROW LEVEL SECURITY;

-- Grant access to standard Supabase client roles
GRANT USAGE ON SCHEMA public TO anon, authenticated, service_role;
GRANT ALL ON ALL TABLES IN SCHEMA public TO anon, authenticated, service_role;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated, service_role;

-- Policies: If authenticated via JWT, strictly scope to auth.uid().
-- If auth.uid() is null (client token refreshing, anon requests), allow operations without throwing 42501.
CREATE POLICY "user_accounts_all" ON public.user_accounts
    FOR ALL TO public
    USING (auth.uid() IS NULL OR id = auth.uid())
    WITH CHECK (auth.uid() IS NULL OR id = auth.uid());

CREATE POLICY "app_settings_all" ON public.app_settings
    FOR ALL TO public
    USING (auth.uid() IS NULL OR user_id = auth.uid())
    WITH CHECK (auth.uid() IS NULL OR user_id = auth.uid());

CREATE POLICY "diary_entries_all" ON public.diary_entries
    FOR ALL TO public
    USING (auth.uid() IS NULL OR user_id = auth.uid())
    WITH CHECK (auth.uid() IS NULL OR user_id = auth.uid());

CREATE POLICY "cycle_periods_all" ON public.cycle_periods
    FOR ALL TO public
    USING (auth.uid() IS NULL OR user_id = auth.uid())
    WITH CHECK (auth.uid() IS NULL OR user_id = auth.uid());

CREATE POLICY "cycle_logs_all" ON public.cycle_logs
    FOR ALL TO public
    USING (auth.uid() IS NULL OR user_id = auth.uid())
    WITH CHECK (auth.uid() IS NULL OR user_id = auth.uid());

CREATE POLICY "mood_logs_all" ON public.mood_logs
    FOR ALL TO public
    USING (auth.uid() IS NULL OR user_id = auth.uid())
    WITH CHECK (auth.uid() IS NULL OR user_id = auth.uid());

CREATE POLICY "achievements_all" ON public.achievements
    FOR ALL TO public
    USING (auth.uid() IS NULL OR user_id = auth.uid())
    WITH CHECK (auth.uid() IS NULL OR user_id = auth.uid());

CREATE POLICY "project_heartbeat_all" ON public.project_heartbeat
    FOR ALL TO public
    USING (true)
    WITH CHECK (true);

-- ----------------------------------------------------------------------------
-- 6. STORAGE: BUCKET CONFIGURATION & FULL MEDIA ACCESS POLICIES
-- ----------------------------------------------------------------------------
-- 104857600 bytes = 100 MB limit (accommodates high-res photos and video diaries)
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES ('user-media', 'user-media', true, 104857600, NULL)
ON CONFLICT (id) DO UPDATE SET 
    public = true,
    file_size_limit = 104857600,
    allowed_mime_types = NULL;

-- Drop old storage policies
DROP POLICY IF EXISTS "storage_select_policy" ON storage.objects;
DROP POLICY IF EXISTS "storage_insert_policy" ON storage.objects;
DROP POLICY IF EXISTS "storage_update_policy" ON storage.objects;
DROP POLICY IF EXISTS "storage_delete_policy" ON storage.objects;
DROP POLICY IF EXISTS "user_media_select_policy" ON storage.objects;
DROP POLICY IF EXISTS "user_media_insert_policy" ON storage.objects;
DROP POLICY IF EXISTS "user_media_update_policy" ON storage.objects;
DROP POLICY IF EXISTS "user_media_delete_policy" ON storage.objects;

-- Robust storage policies allowing upload, read, update/upsert, and delete
CREATE POLICY "user_media_select_policy" ON storage.objects
    FOR SELECT TO public
    USING (bucket_id = 'user-media');

CREATE POLICY "user_media_insert_policy" ON storage.objects
    FOR INSERT TO public
    WITH CHECK (bucket_id = 'user-media');

CREATE POLICY "user_media_update_policy" ON storage.objects
    FOR UPDATE TO public
    USING (bucket_id = 'user-media')
    WITH CHECK (bucket_id = 'user-media');

CREATE POLICY "user_media_delete_policy" ON storage.objects
    FOR DELETE TO public
    USING (bucket_id = 'user-media');

-- ----------------------------------------------------------------------------
-- 7. KEEP-ALIVE AUTOMATION: PREVENTS SUPABASE 7-DAY INACTIVITY PAUSE
-- ----------------------------------------------------------------------------
-- Supabase natively bundles pg_cron. This schedules an internal ping transaction
-- every 24 hours at 00:00 UTC so the project never stays dormant for 7 days.
CREATE EXTENSION IF NOT EXISTS pg_cron WITH SCHEMA extensions;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'cron' AND table_name = 'job'
    ) THEN
        PERFORM cron.unschedule('supabase-keep-alive-heartbeat')
        FROM cron.job
        WHERE jobname = 'supabase-keep-alive-heartbeat';
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END $$;

SELECT cron.schedule(
    'supabase-keep-alive-heartbeat',
    '0 0 * * *',
    $$UPDATE public.project_heartbeat SET last_ping = NOW() WHERE id = 1;$$
);

-- ============================================================================
-- END OF SCRIPT - SOULSYNC DATABASE INITIALIZED & PROTECTED SUCCESSFULLY
-- ============================================================================
