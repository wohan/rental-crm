import type { Field } from './types';

export const money = (value: number | string | undefined) => `${Number(value ?? 0).toLocaleString("ru-RU")} ₽`;
export const MAX_DOCUMENT_FILE_SIZE = 30 * 1024 * 1024;
export const DOCUMENT_ACCEPT = '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.odt,.ods,.odp,.rtf,.txt,.csv,.jpg,.jpeg,.png,.heic';
export const documentTypeOptions: Array<[string, string]> = [['CONTRACT', 'Договор'], ['ACT', 'Акт'], ['RECEIPT', 'Платежный документ'], ['PASSPORT_COPY', 'Паспортные данные'], ['PHOTO', 'Фото/скан'], ['OTHER', 'Другое']];
export const today = () => new Date().toISOString().slice(0, 10);
export const nextYear = () => new Date(new Date().setFullYear(new Date().getFullYear() + 1)).toISOString().slice(0, 10);
export const longTextKeys = new Set(['comment', 'notes', 'description']);
export const isLongTextField = (field: Field) => longTextKeys.has(field.key);
export const statusLabels: Record<string, string> = {
  ACTIVE: 'Активен', ACT: 'Акт', CLOSED: 'Закрыт', CONTRACT: 'Договор', CONTACTED: 'Связались', DEPOSIT: 'Депозит', DONE: 'Закрыта', DRAFT: 'Черновик', FAILED: 'Ошибка', IN_PROGRESS: 'В работе', NEW: 'Новая', OCCUPIED: 'Занят', OVERDUE: 'Просрочен', PAID: 'Оплачен', PENDING: 'Ожидает оплаты', PLANNED: 'Запланирован', RENT: 'Аренда', SENT: 'Отправлено', OTHER: 'Другое', CREATE: 'Создание', DELETE: 'Удаление', MARK_PAID: 'Оплата', SEND: 'Отправка', UPDATE: 'Изменение', OBJECT: 'Объект', TENANT: 'Арендатор', MAINTENANCE: 'Заявка', DOCUMENT: 'Документ', EXPENSE: 'Расход', LISTING: 'Объявление', LEAD: 'Лид', NOTIFICATION: 'Уведомление', NOTIFICATION_SETTINGS: 'Настройки уведомлений', BILLING_SETTINGS: 'Настройки оплаты', BILLING_INVOICE: 'Счет', PASSPORT: 'Паспорт', PASSPORT_COPY: 'Паспортные данные', PAYMENT: 'Платежный документ', PHOTO: 'Фото/скан', RECEIPT: 'Платежный документ', UTILITY: 'Коммунальные', VACANT: 'Свободен', WAITING_TENANT: 'Ждет арендатора'
};
export const status = (value: string | undefined) => value ? statusLabels[value] ?? value : '';
export const str = (value: unknown) => value == null ? '' : String(value);
