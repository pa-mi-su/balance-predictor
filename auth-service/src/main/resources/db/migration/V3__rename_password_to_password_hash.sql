-- Rename the existing column created by V1
ALTER TABLE public.users RENAME COLUMN password TO password_hash;

-- (optional) tighten length if you want
ALTER TABLE public.users ALTER COLUMN password_hash TYPE varchar(255);
