export type RentalProperty = {
  id: number;
  name: string;
  type: string;
  address: string;
  city: string;
  area?: number;
  rooms?: number;
  monthlyRent: number;
  status: string;
};

export type Tenant = {
  id: number;
  fullName: string;
  phone: string;
  email?: string;
  legalType: string;
};

export type Lease = {
  id: number;
  property: RentalProperty;
  tenant: Tenant;
  startDate: string;
  endDate: string;
  monthlyRent: number;
  paymentDay: number;
  status: string;
  contractNumber?: string;
};

export type Payment = {
  id: number;
  lease: Lease;
  dueDate: string;
  paidAt?: string;
  amount: number;
  status: string;
  method: string;
  comment?: string;
};

export type MaintenanceRequest = {
  id: number;
  property: RentalProperty;
  tenant?: Tenant;
  title: string;
  description?: string;
  priority: string;
  status: string;
  costEstimate?: number;
  dueDate?: string;
};

export type Dashboard = {
  propertiesTotal: number;
  occupiedProperties: number;
  activeLeases: number;
  overduePayments: number;
  openMaintenanceRequests: number;
  paidRevenue: number;
  receivable: number;
  upcomingPayments: Payment[];
  expiringLeases: Lease[];
  maintenanceQueue: MaintenanceRequest[];
};

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:18080/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options?.headers ?? {})
    },
    ...options
  });
  if (!response.ok) {
    throw new Error(`API ${response.status}: ${await response.text()}`);
  }
  return response.json() as Promise<T>;
}

export const api = {
  dashboard: () => request<Dashboard>('/dashboard'),
  properties: () => request<RentalProperty[]>('/properties'),
  tenants: () => request<Tenant[]>('/tenants'),
  payments: () => request<Payment[]>('/payments'),
  maintenance: () => request<MaintenanceRequest[]>('/maintenance'),
  markPaid: (id: number) => request<Payment>(`/payments/${id}/paid`, {
    method: 'PATCH',
    body: JSON.stringify({ method: 'SBP', comment: 'Оплачено из кабинета' })
  })
};
