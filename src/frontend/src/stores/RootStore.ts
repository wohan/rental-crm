import { makeAutoObservable, runInAction } from 'mobx';
import { api, Dashboard, MaintenanceRequest, Payment, RentalProperty, Tenant } from '../api/client';

export class RootStore {
  dashboard: Dashboard | null = null;
  properties: RentalProperty[] = [];
  tenants: Tenant[] = [];
  payments: Payment[] = [];
  maintenance: MaintenanceRequest[] = [];
  loading = false;
  error = '';

  constructor() {
    makeAutoObservable(this);
  }

  async load() {
    this.loading = true;
    this.error = '';
    try {
      const [dashboard, properties, tenants, payments, maintenance] = await Promise.all([
        api.dashboard(),
        api.properties(),
        api.tenants(),
        api.payments(),
        api.maintenance()
      ]);
      runInAction(() => {
        this.dashboard = dashboard;
        this.properties = properties;
        this.tenants = tenants;
        this.payments = payments;
        this.maintenance = maintenance;
        this.loading = false;
      });
    } catch (error) {
      runInAction(() => {
        this.error = error instanceof Error ? error.message : 'Ошибка загрузки';
        this.loading = false;
      });
    }
  }

  async markPaymentPaid(id: number) {
    await api.markPaid(id);
    await this.load();
  }
}

export const rootStore = new RootStore();
