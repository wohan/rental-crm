import { makeAutoObservable, runInAction } from "mobx";
import { api, Id } from "../api/client";
import { AuthStore } from "./modules/AuthStore";
import { BillingStore } from "./modules/BillingStore";
import { CrudStore } from "./modules/CrudStore";
import { DocumentStore } from "./modules/DocumentStore";
import { NotificationStore } from "./modules/NotificationStore";
import type { AuditLog, BillingInvoice, BillingSettings, Contract, Dashboard, DocumentItem, Expense, Lead, Listing, Maintenance, NotificationDelivery, NotificationSettings, Payment, RentalObject, Tenant } from "./types";
export type { AuditLog, BillingInvoice, BillingSettings, Contract, Dashboard, DocumentItem, Expense, Lead, Listing, Maintenance, NotificationDelivery, NotificationSettings, Payment, RentalObject, Tenant } from "./types";

export class AppStore {
  authStore = new AuthStore(this);
  crudStore = new CrudStore(this);
  documentStore = new DocumentStore(this);
  notificationStore = new NotificationStore(this);
  billingStore = new BillingStore(this);

  loading = false;
  actionInFlight = "";
  notice = "";
  error = "";
  verificationLink = "";
  resetLink = "";
  token = api.token;
  user: null | { email: string; fullName: string; role: string; emailVerified: boolean; account: { name: string; tariff: string; objectsLimit: number } } = null;
  dashboard: Dashboard | null = null;
  objects: RentalObject[] = [];
  tenants: Tenant[] = [];
  contracts: Contract[] = [];
  payments: Payment[] = [];
  maintenance: Maintenance[] = [];
  documents: DocumentItem[] = [];
  notificationSettings: NotificationSettings | null = null;
  notificationDeliveries: NotificationDelivery[] = [];
  expenses: Expense[] = [];
  listings: Listing[] = [];
  leads: Lead[] = [];
  billingSettings: BillingSettings | null = null;
  billingInvoices: BillingInvoice[] = [];
  auditLogs: AuditLog[] = [];

  constructor() {
    makeAutoObservable(this);
  }

  get authenticated() {
    return Boolean(this.token);
  }

  register(payload: { email: string; password: string; fullName: string; accountName: string; termsAccepted?: boolean; privacyAccepted?: boolean; notificationConsent?: boolean }) {
    return this.authStore.register(payload);
  }

  login(payload: { email: string; password: string }) {
    return this.authStore.login(payload);
  }

  forgotPassword(email: string) {
    return this.authStore.forgotPassword(email);
  }

  resetPassword(token: string, newPassword: string) {
    return this.authStore.resetPassword(token, newPassword);
  }

  logout() {
    this.authStore.logout();
  }

  async loadAll() {
    if (!api.token) return;
    this.loading = true;
    this.error = "";
    try {
      const [user, dashboard, objects, tenants, contracts, payments, maintenance, documents, notificationSettings, notificationDeliveries, expenses, listings, leads, billingSettings, billingInvoices, auditLogs] = await Promise.all([
        api.get<AppStore["user"]>("/me"),
        api.get<Dashboard>("/dashboard"),
        api.get<RentalObject[]>("/objects"),
        api.get<Tenant[]>("/tenants"),
        api.get<Contract[]>("/contracts"),
        api.get<Payment[]>("/payments"),
        api.get<Maintenance[]>("/maintenance"),
        api.get<DocumentItem[]>("/documents"),
        api.get<NotificationSettings>("/notifications/settings"),
        api.get<NotificationDelivery[]>("/notifications/deliveries"),
        api.get<Expense[]>("/expenses"),
        api.get<Listing[]>("/listings"),
        api.get<Lead[]>("/leads"),
        api.get<BillingSettings>("/billing/settings"),
        api.get<BillingInvoice[]>("/billing/invoices"),
        api.get<AuditLog[]>("/audit")
      ]);
      runInAction(() => {
        this.user = user;
        this.dashboard = dashboard;
        this.objects = objects;
        this.tenants = tenants;
        this.contracts = contracts;
        this.payments = payments;
        this.maintenance = maintenance;
        this.documents = documents;
        this.notificationSettings = notificationSettings;
        this.notificationDeliveries = notificationDeliveries;
        this.expenses = expenses;
        this.listings = listings;
        this.leads = leads;
        this.billingSettings = billingSettings;
        this.billingInvoices = billingInvoices;
        this.auditLogs = auditLogs;
      });
    } catch (err) {
      runInAction(() => {
        this.error = err instanceof Error ? err.message : "Ошибка загрузки";
        if (!api.token) {
          this.token = "";
          this.user = null;
        }
      });
    } finally {
      runInAction(() => this.loading = false);
    }
  }

  create(path: string, payload: unknown) {
    return this.crudStore.create(path, payload);
  }

  createContract(payload: Record<string, unknown>, file?: File | null) {
    return this.crudStore.createContract(payload, file);
  }

  update(path: string, payload: unknown, success = "Изменения сохранены") {
    return this.crudStore.update(path, payload, success);
  }

  remove(path: string) {
    return this.crudStore.remove(path);
  }

  markPaymentPaid(id: Id) {
    return this.crudStore.markPaymentPaid(id);
  }

  uploadDocument(formData: FormData) {
    return this.documentStore.uploadDocument(formData);
  }

  downloadDocument(document: DocumentItem) {
    return this.documentStore.downloadDocument(document);
  }

  saveNotificationSettings(payload: NotificationSettings) {
    return this.notificationStore.saveNotificationSettings(payload);
  }

  sendDueNotifications() {
    return this.notificationStore.sendDueNotifications();
  }

  exportWorkbook() {
    return this.billingStore.exportWorkbook();
  }

  saveBillingSettings(payload: BillingSettings) {
    return this.billingStore.saveBillingSettings(payload);
  }

  createCheckout(tariff: string) {
    return this.billingStore.createCheckout(tariff);
  }

  markInvoicePaid(id: Id) {
    return this.billingStore.markInvoicePaid(id);
  }

  async refresh() {
    await this.runAction("Обновляем", "Данные обновлены", async () => {
      await this.loadAll();
    });
  }

  async runAction(label: string, success: string, action: () => Promise<void>) {
    this.actionInFlight = label;
    this.notice = "";
    this.error = "";
    try {
      await action();
      runInAction(() => this.notice = success);
    } catch (err) {
      runInAction(() => this.error = err instanceof Error ? err.message : "Ошибка действия");
      throw err;
    } finally {
      runInAction(() => this.actionInFlight = "");
    }
  }
}

export const store = new AppStore();
