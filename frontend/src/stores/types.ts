import type { Id } from '../api/client';

export type RentalObject = {
  id: Id;
  title: string;
  type: string;
  address: string;
  status: string;
  monthlyRent: number;
  monthlyUtilityAmount: number;
  depositAmount: number;
  notes?: string;
};

export type Tenant = { id: Id; fullName: string; phone: string; email?: string; telegramChatId?: string; whatsappPhone?: string; notificationsEnabled?: boolean; publicRequestToken?: string; notes?: string };
export type Contract = { id: Id; objectId: Id; tenantId: Id; number: string; startDate: string; endDate: string; rentAmount: number; paymentDay: number; depositAmount: number; status: string; documentUrl?: string };
export type Payment = { id: Id; objectId: Id; tenantId?: Id; contractId?: Id; dueDate: string; paidDate?: string; amount: number; paidAmount: number; status: string; type: string; method?: string; comment?: string };
export type Maintenance = { id: Id; objectId: Id; tenantId?: Id; title: string; description?: string; priority: string; status: string; cost: number; dueDate?: string };
export type DocumentItem = { id: Id; objectId?: Id; tenantId?: Id; contractId?: Id; name: string; type: string; fileUrl: string; originalFileName?: string; contentType?: string; sizeBytes?: number; expiresAt?: string };
export type NotificationSettings = {
  enabled: boolean;
  remindDaysBefore: number;
  reminderTime: string;
  telegramEnabled: boolean;
  telegramBotToken?: string;
  telegramDefaultChatId?: string;
  smsRuEnabled: boolean;
  smsRuApiId?: string;
  smsRuSender?: string;
  smsRuTestMode: boolean;
  whatsappEnabled: boolean;
  whatsappApiUrl?: string;
  whatsappToken?: string;
  whatsappDefaultRecipient?: string;
  messageTemplate: string;
};
export type NotificationDelivery = { id: Id; paymentId: Id; channel: string; recipient?: string; status: string; response?: string; createdAt: string };

export type Dashboard = {
  objectsTotal: number;
  objectsOccupied: number;
  paidThisMonth: number;
  expensesThisMonth: number;
  netThisMonth: number;
  overdueAmount: number;
  upcomingPayments: Payment[];
  expiringContracts: Contract[];
  openMaintenance: Maintenance[];
};
export type Expense = { id: Id; objectId: Id; expenseDate: string; category: string; amount: number; vendor?: string; documentUrl?: string; source?: string; periodMonth?: string; comment?: string };
export type Listing = { id: Id; objectId: Id; title: string; description: string; price: number; publicUrl?: string; published: boolean };
export type Lead = { id: Id; listingId?: Id; objectId?: Id; fullName: string; phone: string; email?: string; source?: string; status: string; comment?: string };
export type BillingSettings = { provider: string; yookassaShopId?: string; yookassaSecretKey?: string; cloudPaymentsPublicId?: string; cloudPaymentsApiSecret?: string; robokassaMerchantLogin?: string; robokassaPassword1?: string; genericPaymentUrl?: string; testMode: boolean };
export type BillingInvoice = { id: Id; tariff: string; amount: number; objectsLimit: number; provider: string; status: string; confirmationUrl?: string; createdAt: string };
export type AuditLog = { id: Id; action: string; entityType: string; entityId?: Id; actorEmail: string; summary?: string; createdAt: string };

