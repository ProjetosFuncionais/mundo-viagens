import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/auth';
import { Button } from './ui/button';
import { Plane, User, LogOut, LayoutDashboard } from 'lucide-react';

export default function Layout() {
  const { user, profile, logout, sessionError } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    logout();
    navigate('/');
  };

  const isAdmin = profile?.role === 'ATENDENTE' || profile?.role === 'GERENTE' || profile?.role === 'DONA';

  return (
    <div className="min-h-screen bg-gray-50 font-sans flex flex-col">
      <header className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16 items-center">
            <div className="flex items-center gap-2">
              <Link to="/" className="flex items-center gap-2">
                <Plane className="h-6 w-6 text-blue-600" />
                <span className="text-base sm:text-xl font-bold text-gray-900">Mundo Viagens</span>
              </Link>
            </div>
            
            <nav className="flex items-center gap-1 sm:gap-4">
              {user ? (
                <>
                  <Link to="/profile">
                    <Button variant="ghost" className="flex items-center gap-2">
                      <User className="h-4 w-4" />
                      <span className="hidden sm:inline">{profile?.nome || 'Perfil'}</span>
                    </Button>
                  </Link>
                  {isAdmin && (
                    <Link to="/admin">
                      <Button variant="outline" className="flex items-center gap-2">
                        <LayoutDashboard className="h-4 w-4" />
                        <span className="hidden sm:inline">Painel Admin</span>
                      </Button>
                    </Link>
                  )}
                  <Button variant="ghost" size="icon" onClick={handleLogout} title="Sair">
                    <LogOut className="h-5 w-5 text-gray-500" />
                  </Button>
                </>
              ) : (
                <>
                  <Link to="/login">
                    <Button variant="ghost">Entrar</Button>
                  </Link>
                  <Link to="/register">
                    <Button>Cadastrar</Button>
                  </Link>
                </>
              )}
            </nav>
          </div>
        </div>
      </header>

      <main className="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-6 lg:p-8">
        {sessionError && <p role="alert" className="mb-4 text-red-600">{sessionError}</p>}
        <Outlet />
      </main>

      <footer className="bg-white border-t border-gray-200 py-8 text-center text-gray-500 text-sm">
        <p>&copy; {new Date().getFullYear()} Mundo Viagens. Todos os direitos reservados.</p>
      </footer>
    </div>
  );
}
