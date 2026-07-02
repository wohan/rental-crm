import { observer } from 'mobx-react-lite';
import { store } from './stores/appStore';
import { AuthPage } from './pages/auth/AuthPage';
import { DashboardPage } from './pages/dashboard/DashboardPage';
import { PublicMaintenancePage } from './pages/public-maintenance/PublicMaintenancePage';

export const App = observer(() => <div data-testid='app-ready'>{window.location.pathname === '/maintenance-request' ? <PublicMaintenancePage /> : store.authenticated ? <DashboardPage /> : <AuthPage />}</div>);
