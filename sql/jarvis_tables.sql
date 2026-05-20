-- Project Jarvis Tables
-- These tables use the jarvis_ prefix to avoid collision with other projects.

CREATE TABLE IF NOT EXISTS public.jarvis_devices (
    device_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_name TEXT,
    manufacturer TEXT,
    model TEXT,
    android_version TEXT,
    api_level INTEGER,
    status TEXT DEFAULT 'Offline', -- Offline, Online, Sleeping
    last_seen TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.jarvis_sessions (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID REFERENCES public.jarvis_devices(device_id) ON DELETE CASCADE,
    webrtc_room_id TEXT,
    operator_url TEXT,
    status TEXT DEFAULT 'Pending', -- Pending, Active, Closed
    started_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ended_at TIMESTAMP WITH TIME ZONE
);

-- Enable RLS
ALTER TABLE public.jarvis_devices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.jarvis_sessions ENABLE ROW LEVEL SECURITY;

-- Policies for jarvis_devices
-- Devices (anon) can insert and update their own status
CREATE POLICY "Allow anon insert on jarvis_devices" ON public.jarvis_devices FOR INSERT TO anon WITH CHECK (true);
CREATE POLICY "Allow anon update on jarvis_devices" ON public.jarvis_devices FOR UPDATE TO anon USING (true);
CREATE POLICY "Allow service_role full access on jarvis_devices" ON public.jarvis_devices TO service_role USING (true) WITH CHECK (true);

-- Policies for jarvis_sessions
-- Devices (anon) can read sessions targeted at them, and update status
CREATE POLICY "Allow anon select on jarvis_sessions" ON public.jarvis_sessions FOR SELECT TO anon USING (true);
CREATE POLICY "Allow anon update on jarvis_sessions" ON public.jarvis_sessions FOR UPDATE TO anon USING (true);
CREATE POLICY "Allow service_role full access on jarvis_sessions" ON public.jarvis_sessions TO service_role USING (true) WITH CHECK (true);
