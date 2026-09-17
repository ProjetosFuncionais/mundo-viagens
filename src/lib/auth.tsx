import { createContext, useContext, useEffect, useState, useCallback, useRef, ReactNode } from 'react';
import { authApi, AuthResponse, RegisterInput, UserProfile } from './services';
import { clearToken, getToken, saveToken, SESSION_EXPIRED, TOKEN_KEY, errorMessage } from './api';
export type { UserRole, UserProfile } from './services';
interface AuthContextType {
  user: UserProfile | null;
  profile: UserProfile | null;
  loading: boolean;
  sessionError: string;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterInput) => Promise<void>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
}
const AuthContext = createContext<AuthContextType | undefined>(undefined);
export function AuthProvider({ children }: { children: ReactNode }) {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [sessionError, setSessionError] = useState('');
  const revision = useRef(0);
  const refreshProfile = useCallback(async () => {
    const current = ++revision.current;
    const token = getToken();
    if (!token) { setProfile(null); setLoading(false); return; }
    try {
      const user = await authApi.me();
      if (current === revision.current && token === getToken()) { setProfile(user); setSessionError(''); }
    } catch (error) {
      if (current === revision.current) { setProfile(null); setSessionError(errorMessage(error)); }
      throw error;
    } finally {
      if (current === revision.current) setLoading(false);
    }
  }, []);
  const logout = useCallback(() => {
    revision.current++;
    clearToken(); setProfile(null); setLoading(false); setSessionError('');
  }, []);
  useEffect(() => {
    void refreshProfile().catch(() => {});
    const expired = () => { logout(); setSessionError('Sua sessão expirou. Entre novamente.'); };
    const storage = (event: StorageEvent) => {
      if (event.key === TOKEN_KEY || event.key === null) void refreshProfile().catch(() => {});
    };
    window.addEventListener(SESSION_EXPIRED, expired);
    window.addEventListener('storage', storage);
    return () => {
      revision.current++;
      window.removeEventListener(SESSION_EXPIRED, expired);
      window.removeEventListener('storage', storage);
    };
  }, [logout, refreshProfile]);
  const accept = (response: AuthResponse) => {
    revision.current++;
    saveToken(response.token); setProfile(response.user); setLoading(false); setSessionError('');
  };
  return <AuthContext.Provider value={{ user: profile, profile, loading, sessionError, logout, refreshProfile,
    login: async (email, password) => accept(await authApi.login(email, password)),
    register: async data => accept(await authApi.register(data)),
  }}>{children}</AuthContext.Provider>;
}
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth deve ser usado dentro de AuthProvider');
  return context;
}
