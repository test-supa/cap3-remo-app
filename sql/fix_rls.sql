-- Fix RLS policies for Jarvis tables so the app can work with the anon key

-- Drop existing policies first (if any)
DROP POLICY IF EXISTS "Allow anon insert on jarvis_devices" ON public.jarvis_devices;
DROP POLICY IF EXISTS "Allow anon update on jarvis_devices" ON public.jarvis_devices;
DROP POLICY IF EXISTS "Allow anon select on jarvis_devices" ON public.jarvis_devices;
DROP POLICY IF EXISTS "Allow anon select on jarvis_sessions" ON public.jarvis_sessions;
DROP POLICY IF EXISTS "Allow anon update on jarvis_sessions" ON public.jarvis_sessions;

-- Recreate policies for jarvis_devices
CREATE POLICY "anon_insert_jarvis_devices"
    ON public.jarvis_devices FOR INSERT TO anon
    WITH CHECK (true);

CREATE POLICY "anon_update_jarvis_devices"
    ON public.jarvis_devices FOR UPDATE TO anon
    USING (true);

CREATE POLICY "anon_select_jarvis_devices"
    ON public.jarvis_devices FOR SELECT TO anon
    USING (true);

-- Recreate policies for jarvis_sessions
CREATE POLICY "anon_select_jarvis_sessions"
    ON public.jarvis_sessions FOR SELECT TO anon
    USING (true);

CREATE POLICY "anon_update_jarvis_sessions"
    ON public.jarvis_sessions FOR UPDATE TO anon
    USING (true);
