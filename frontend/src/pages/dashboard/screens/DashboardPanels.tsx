import React, { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { BadgePercent, Bell, Building2, CheckCircle2, FileText, History, Home, MessageCircle, Plus, RussianRuble, Send, Smartphone, TrendingDown, Users, Wrench } from 'lucide-react';
import { store } from '../../../stores/appStore';
import type { DocumentItem, Lead, Listing, Maintenance, Payment, RentalObject } from '../../../stores/appStore';
import { AutoTextarea } from '../../../components/AutoTextarea';
import { DataPanel } from '../../../components/DataPanel';
import { COMPACT_TABLE_PAGE_SIZE, SortHeader, TABLE_PAGE_SIZE, TablePagination, pageItems, sortItems } from '../../../components/DataTable';
import type { SortState } from '../../../components/DataTable';
import { DeleteIconButton } from '../../../components/DeleteIconButton';
import { MaintenanceAccess } from '../../../features/maintenance/MaintenanceAccess';
import type { EditorState, Field, Section } from '../../../shared/types';
import { DOCUMENT_ACCEPT, MAX_DOCUMENT_FILE_SIZE, documentTypeOptions, isLongTextField, money, status, str } from '../../../shared/constants';

const confirmAction = (message: string, action: () => void) => { if (window.confirm(message)) action(); };

const notificationGuides = [
  {
    key: 'sms',
    title: 'SMS.RU',
    subtitle: 'SMS по РФ через api_id',
    icon: <Smartphone size={18} />,
    steps: [
      'Зарегистрируйтесь в SMS.RU и подтвердите аккаунт.',
      'В личном кабинете заключите договор и создайте имя отправителя, если нужен бренд вместо номера.',
      'На главной странице кабинета скопируйте api_id.',
      'Включите SMS.RU ниже, вставьте api_id, при необходимости укажите согласованного отправителя.',
      'Для проверки включите SMS test=1, сохраните настройки и нажмите «Отправить сейчас».'
    ],
    docUrl: 'https://sms.ru/api/send'
  },
  {
    key: 'whatsapp',
    title: 'WhatsApp / GREEN-API',
    subtitle: 'Сообщения WhatsApp через URL инстанса',
    icon: <MessageCircle size={18} />,
    steps: [
      'Зарегистрируйтесь в GREEN-API и создайте WhatsApp-инстанс.',
      'Подключите телефон к инстансу по QR-коду в кабинете GREEN-API.',
      'Скопируйте apiUrl, idInstance и apiTokenInstance.',
      'Соберите URL вида apiUrl/waInstance{idInstance}/sendMessage/{apiTokenInstance} и вставьте его в поле WhatsApp API URL.',
      'В поле получателя используйте номер в формате 79990000000@c.us или телефон арендатора, если backend формирует chatId сам.'
    ],
    docUrl: 'https://green-api.com/docs/api/sending/SendMessage/'
  },
  {
    key: 'telegram',
    title: 'Telegram Bot',
    subtitle: 'Бесплатные уведомления через бота',
    icon: <Send size={18} />,
    steps: [
      'Откройте @BotFather в Telegram и создайте бота командой /newbot.',
      'Скопируйте bot token и вставьте его в поле Telegram bot token.',
      'Попросите арендатора написать боту любое сообщение или добавьте бота в нужный чат.',
      'Получите chat_id через getUpdates или сохраните chat_id арендатора в карточке арендатора.',
      'Включите Telegram, сохраните настройки и отправьте тестовое уведомление.'
    ],
    docUrl: 'https://core.telegram.org/bots/api#sendmessage'
  }
];

export const QuickForms = observer(function QuickForms({ section }: { section: Section }) {
  const objectOptions: Array<[string, string]> = store.objects.map(item => [item.id, item.title]);
  const tenantOptions: Array<[string, string]> = store.tenants.map(item => [item.id, item.fullName]);
  const objectId = objectOptions[0]?.[0] ?? "";
  const tenantId = tenantOptions[0]?.[0] ?? "";
  const needsObject = !objectId;
  const needsTenant = !tenantId;
  const commonObjectField: Field = { key: "objectId", label: "Объект", value: objectId, type: "select", options: objectOptions };
  const commonTenantField: Field = { key: "tenantId", label: "Арендатор", value: tenantId, type: "select", options: tenantOptions };
  const cards = [
    <CreateCard key="object" title="Объект" icon={<Home />} onSubmit={values => store.create("/objects", { title: values.title, type: values.type, address: values.address, status: "VACANT", monthlyRent: Number(values.amount), monthlyUtilityAmount: Number(values.utilityAmount || 0), depositAmount: Number(values.deposit), notes: values.notes })} fields={[
      { key: "title", label: "Название", value: "" },
      { key: "type", label: "Тип", value: "", type: "select", options: [["APARTMENT", "Квартира"], ["COMMERCIAL", "Коммерция"], ["WAREHOUSE", "Склад"], ["STUDIO", "Студия"], ["EQUIPMENT", "Техника"]] },
      { key: "address", label: "Адрес", value: "" },
      { key: "amount", label: "Аренда, ₽", value: "", type: "number" },
      { key: "utilityAmount", label: "Коммунальные, ₽/мес", value: "", type: "number" },
      { key: "deposit", label: "Депозит, ₽", value: "", type: "number" },
      { key: "notes", label: "Заметки", value: "" }
    ]} />,
    <CreateCard key="tenant" title="Арендатор" icon={<Building2 />} onSubmit={values => store.create("/tenants", { fullName: values.fullName, phone: values.phone, email: values.email, telegramChatId: values.telegramChatId, whatsappPhone: values.whatsappPhone, notificationsEnabled: values.notificationsEnabled === "true", notes: values.notes })} fields={[
      { key: "fullName", label: "ФИО", value: "" },
      { key: "phone", label: "Телефон", value: "" },
      { key: "email", label: "Email", value: "" },
      { key: "telegramChatId", label: "Telegram chat_id", value: "" },
      { key: "whatsappPhone", label: "WhatsApp телефон", value: "" },
      { key: "notificationsEnabled", label: "Отправлять уведомления", value: "false", type: "checkbox" },
      { key: "notes", label: "Заметки", value: "" }
    ]} />,
    <ContractCreateCard key="contract" objectOptions={objectOptions} tenantOptions={tenantOptions} objectId={objectId} tenantId={tenantId} disabled={needsObject || needsTenant} />,
    <CreateCard key="payment" title="Платеж" icon={<RussianRuble />} onSubmit={values => store.create("/payments", { objectId: values.objectId, tenantId: values.tenantId || undefined, dueDate: values.dueDate, amount: Number(values.amount), paidAmount: 0, status: "PLANNED", type: values.type, comment: values.comment })} fields={[
      commonObjectField,
      { ...commonTenantField, label: "Арендатор", value: tenantId },
      { key: "type", label: "Тип платежа", value: "", type: "select", options: [["RENT", "Аренда"], ["DEPOSIT", "Депозит"], ["UTILITY", "Коммунальные"], ["OTHER", "Другое"]] },
      { key: "dueDate", label: "Дата", value: "", type: "date" },
      { key: "amount", label: "Сумма, ₽", value: "", type: "number" },
      { key: "comment", label: "Комментарий", value: "" }
    ]} disabled={needsObject} disabledHint="Сначала добавьте объект." />,
    <CreateCard key="maintenance" title="Заявка" icon={<Wrench />} onSubmit={values => store.create("/maintenance", { objectId: values.objectId, tenantId: values.tenantId || undefined, title: values.title, description: values.description, priority: values.priority, status: "NEW", cost: Number(values.cost || 0), dueDate: values.dueDate || undefined })} fields={[
      commonObjectField,
      { ...commonTenantField, value: tenantId },
      { key: "title", label: "Название", value: "" },
      { key: "priority", label: "Приоритет", value: "", type: "select", options: [["LOW", "Низкий"], ["MEDIUM", "Средний"], ["HIGH", "Высокий"], ["URGENT", "Срочно"]] },
      { key: "description", label: "Описание", value: "" },
      { key: "cost", label: "Стоимость, ₽", value: "", type: "number" },
      { key: "dueDate", label: "Срок", value: "", type: "date" }
    ]} disabled={needsObject} disabledHint="Сначала добавьте объект." />,
    <DocumentUploadCard key="document" objectOptions={objectOptions} tenantOptions={tenantOptions} objectId={objectId} tenantId={tenantId} disabled={needsObject} />,
    <CreateCard key="expense" title="Расход" icon={<TrendingDown />} onSubmit={values => store.create("/expenses", { objectId: values.objectId, expenseDate: values.expenseDate, category: values.category, amount: Number(values.amount), vendor: values.vendor, documentUrl: values.documentUrl, comment: values.comment })} fields={[
      commonObjectField,
      { key: "expenseDate", label: "Дата", value: "", type: "date" },
      { key: "category", label: "Категория", value: "" },
      { key: "amount", label: "Сумма, ₽", value: "", type: "number" },
      { key: "vendor", label: "Подрядчик", value: "" },
      { key: "documentUrl", label: "Документ", value: "" },
      { key: "comment", label: "Комментарий", value: "" }
    ]} disabled={needsObject} disabledHint="Сначала добавьте объект." />,
    <CreateCard key="listing" title="Объявление" icon={<BadgePercent />} onSubmit={values => store.create("/listings", { objectId: values.objectId, title: values.title, description: values.description, price: Number(values.price), publicUrl: values.publicUrl, published: values.published === "true" })} fields={[
      commonObjectField,
      { key: "title", label: "Заголовок", value: "" },
      { key: "description", label: "Описание", value: "" },
      { key: "price", label: "Цена, ₽", value: "", type: "number" },
      { key: "publicUrl", label: "Ссылка", value: "" },
      { key: "published", label: "Опубликовано", value: "false", type: "checkbox" }
    ]} disabled={needsObject} disabledHint="Сначала добавьте объект." />,
    <CreateCard key="lead" title="Лид" icon={<Users />} onSubmit={values => store.create("/leads", { objectId: values.objectId, fullName: values.fullName, phone: values.phone, email: values.email, source: values.source, status: "NEW", comment: values.comment })} fields={[
      commonObjectField,
      { key: "fullName", label: "Имя", value: "" },
      { key: "phone", label: "Телефон", value: "" },
      { key: "email", label: "Email", value: "" },
      { key: "source", label: "Источник", value: "" },
      { key: "comment", label: "Комментарий", value: "" }
    ]} disabled={needsObject} disabledHint="Сначала добавьте объект." />
  ];
  const visible = {
    overview: cards.slice(0, store.objects.length ? 3 : 1),
    objects: cards.slice(0, 3),
    payments: [cards[3]],
    maintenance: [cards[4]],
    documents: [cards[5]],
    finance: [cards[6]],
    pipeline: [cards[7], cards[8]],
    notifications: [],
    billing: [],
    audit: []
  }[section];
  return <section className={`quick-grid compact-${Math.min(visible.length, 3)}`}>{visible}</section>;
});

function CreateCard({ title, icon, fields, onSubmit, disabled, disabledHint }: { title: string; icon: React.ReactNode; fields: Field[]; onSubmit: (values: Record<string, string>) => Promise<void> | void; disabled?: boolean; disabledHint?: string }) {
  const [open, setOpen] = useState(false);
  const [values, setValues] = useState<Record<string, string>>(Object.fromEntries(fields.map(field => [field.key, field.value])));
  const [saving, setSaving] = useState(false);
  const testId = `create-${title.toLowerCase()}`;
  const fieldsSignature = fields.map(field => `${field.key}:${field.value}:${field.options?.map(option => option.join("=")).join("|") ?? ""}`).join(";");
  useEffect(() => {
    if (!open) {
      setValues(Object.fromEntries(fields.map(field => [field.key, field.value])));
    }
  }, [fieldsSignature, open]);
  return <div className="card">
    <button className={`card-title ${open ? "active" : ""}`} onClick={() => setOpen(!open)} disabled={disabled} data-testid={testId}>
      {icon}<span>{title}</span><Plus size={18} />
    </button>
    {disabled && disabledHint && <p className="hint">{disabledHint}</p>}
    {open && <form className="mini-form" onSubmit={async event => {
      event.preventDefault();
      setSaving(true);
      try {
        await onSubmit(values);
        setOpen(false);
      } finally {
        setSaving(false);
      }
    }}>
      {fields.map(field => <label key={field.key}>{field.label}
        {field.type === "select"
          ? <select aria-label={`${title}: ${field.label}`} value={values[field.key] ?? ""} onChange={e => setValues({ ...values, [field.key]: e.target.value })}>
              <option value="">Не выбрано</option>
              {(field.options ?? []).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          : field.type === "checkbox"
            ? <input type="checkbox" aria-label={`${title}: ${field.label}`} checked={(values[field.key] ?? field.value) === "true"} onChange={e => setValues({ ...values, [field.key]: String(e.target.checked) })} />
            : isLongTextField(field)
              ? <AutoTextarea label={`${title}: ${field.label}`} value={values[field.key] ?? ""} onChange={value => setValues({ ...values, [field.key]: value })} />
              : <input type={field.type ?? "text"} aria-label={`${title}: ${field.label}`} value={values[field.key] ?? ""} onChange={e => setValues({ ...values, [field.key]: e.target.value })} />}
      </label>)}
      <button className="primary" type="submit" disabled={saving || Boolean(store.actionInFlight)} data-testid={`${testId}-save`}>
        {saving || store.actionInFlight ? "Сохраняем..." : "Сохранить"}
      </button>
    </form>}
  </div>;
}

function ContractCreateCard({ objectOptions, tenantOptions, objectId, tenantId, disabled }: { objectOptions: Array<[string, string]>; tenantOptions: Array<[string, string]>; objectId: string; tenantId: string; disabled?: boolean }) {
  const [open, setOpen] = useState(false);
  const [values, setValues] = useState({
    objectId,
    tenantId,
    number: "",
    startDate: "",
    endDate: "",
    amount: "",
    paymentDay: "",
    deposit: ""
  });
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!open) {
      setValues({ objectId, tenantId, number: "", startDate: "", endDate: "", amount: "", paymentDay: "", deposit: "" });
      setFile(null);
      setError("");
    }
  }, [objectId, tenantId, open]);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError("");
    if (file && file.size > MAX_DOCUMENT_FILE_SIZE) {
      setError("Файл больше 30 МБ");
      return;
    }
    setSaving(true);
    try {
      await store.createContract({
        objectId: values.objectId,
        tenantId: values.tenantId,
        number: values.number,
        startDate: values.startDate,
        endDate: values.endDate,
        rentAmount: Number(values.amount),
        paymentDay: Number(values.paymentDay),
        depositAmount: Number(values.deposit),
        status: "ACTIVE"
      }, file);
      setOpen(false);
    } finally {
      setSaving(false);
    }
  };

  return <div className="card">
    <button className={`card-title ${open ? "active" : ""}`} onClick={() => setOpen(!open)} disabled={disabled} data-testid="create-договор">
      <FileText /><span>Договор</span><Plus size={18} />
    </button>
    {disabled && <p className="hint">Сначала добавьте объект и арендатора.</p>}
    {open && <form className="mini-form" onSubmit={submit}>
      <label>Объект
        <select aria-label="Договор: Объект" value={values.objectId} onChange={e => setValues({ ...values, objectId: e.target.value })}>{objectOptions.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select>
      </label>
      <label>Арендатор
        <select aria-label="Договор: Арендатор" value={values.tenantId} onChange={e => setValues({ ...values, tenantId: e.target.value })}>{tenantOptions.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select>
      </label>
      <label>Номер<input aria-label="Договор: Номер" placeholder="Сгенерируется автоматически" value={values.number} onChange={e => setValues({ ...values, number: e.target.value })} /></label>
      <label>Начало<input aria-label="Договор: Начало" type="date" value={values.startDate} onChange={e => setValues({ ...values, startDate: e.target.value })} /></label>
      <label>Окончание<input aria-label="Договор: Окончание" type="date" value={values.endDate} onChange={e => setValues({ ...values, endDate: e.target.value })} /></label>
      <label>Аренда, ₽<input aria-label="Договор: Аренда, ₽" type="number" value={values.amount} onChange={e => setValues({ ...values, amount: e.target.value })} /></label>
      <label>День оплаты<input aria-label="Договор: День оплаты" type="number" value={values.paymentDay} onChange={e => setValues({ ...values, paymentDay: e.target.value })} /></label>
      <label>Депозит, ₽<input aria-label="Договор: Депозит, ₽" type="number" value={values.deposit} onChange={e => setValues({ ...values, deposit: e.target.value })} /></label>
      <label>Файл договора
        <input aria-label="Договор: Файл" type="file" accept={DOCUMENT_ACCEPT} onChange={e => setFile(e.target.files?.[0] ?? null)} />
      </label>
      <p className="hint">PDF, Microsoft Office, OpenDocument, RTF, TXT/CSV и изображения-сканы. Максимум 30 МБ.</p>
      {error && <p className="error">{error}</p>}
      <button className="primary" type="submit" disabled={saving || Boolean(store.actionInFlight)} data-testid="create-договор-save">
        {saving || store.actionInFlight ? "Сохраняем..." : "Сохранить"}
      </button>
    </form>}
  </div>;
}

function DocumentUploadCard({ objectOptions, tenantOptions, objectId, tenantId, disabled }: { objectOptions: Array<[string, string]>; tenantOptions: Array<[string, string]>; objectId: string; tenantId: string; disabled?: boolean }) {
  const [open, setOpen] = useState(false);
  const [values, setValues] = useState({ objectId, tenantId, name: "", type: "", expiresAt: "" });
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!open) {
      setValues({ objectId, tenantId, name: "", type: "", expiresAt: "" });
      setFile(null);
      setError("");
    }
  }, [objectId, tenantId, open]);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError("");
    if (!file) {
      setError("Выберите файл документа");
      return;
    }
    if (file.size > MAX_DOCUMENT_FILE_SIZE) {
      setError("Файл больше 30 МБ");
      return;
    }
    const formData = new FormData();
    formData.append("objectId", values.objectId);
    if (values.tenantId) formData.append("tenantId", values.tenantId);
    formData.append("name", values.name);
    formData.append("type", values.type);
    if (values.expiresAt) formData.append("expiresAt", values.expiresAt);
    formData.append("file", file);
    setSaving(true);
    try {
      await store.uploadDocument(formData);
      setOpen(false);
    } finally {
      setSaving(false);
    }
  };

  return <div className="card">
    <button className={`card-title ${open ? "active" : ""}`} onClick={() => setOpen(!open)} disabled={disabled} data-testid="create-документ">
      <FileText /><span>Документ</span><Plus size={18} />
    </button>
    {disabled && <p className="hint">Сначала добавьте объект.</p>}
    {open && <form className="mini-form" onSubmit={submit}>
      <label>Объект
        <select aria-label="Документ: Объект" value={values.objectId} onChange={e => setValues({ ...values, objectId: e.target.value })}>{objectOptions.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select>
      </label>
      <label>Арендатор
        <select aria-label="Документ: Арендатор" value={values.tenantId} onChange={e => setValues({ ...values, tenantId: e.target.value })}>
          <option value="">Не привязан</option>
          {tenantOptions.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
        </select>
      </label>
      <label>Название<input aria-label="Документ: Название" value={values.name} onChange={e => setValues({ ...values, name: e.target.value })} /></label>
      <label>Тип
        <select aria-label="Документ: Тип" value={values.type} onChange={e => setValues({ ...values, type: e.target.value })}>
          <option value="">Не выбрано</option>
          {documentTypeOptions.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
        </select>
      </label>
      <label>Файл
        <input aria-label="Документ: Файл" type="file" accept={DOCUMENT_ACCEPT} onChange={e => setFile(e.target.files?.[0] ?? null)} />
      </label>
      <p className="hint">PDF, Microsoft Office, OpenDocument, RTF, TXT/CSV и изображения-сканы. Максимум 30 МБ.</p>
      <label>Истекает<input aria-label="Документ: Истекает" type="date" value={values.expiresAt} onChange={e => setValues({ ...values, expiresAt: e.target.value })} /></label>
      {error && <p className="error">{error}</p>}
      <button className="primary" type="submit" disabled={saving || Boolean(store.actionInFlight)} data-testid="create-документ-save">
        {saving || store.actionInFlight ? "Сохраняем..." : "Сохранить"}
      </button>
    </form>}
  </div>;
}

function FieldInput({ scope, field, values, onChange }: { scope: string; field: Field; values: Record<string, string>; onChange: (values: Record<string, string>) => void }) {
  return <label>{field.label}
    {field.type === "select"
      ? <select aria-label={`${scope}: ${field.label}`} value={values[field.key] ?? field.value ?? ""} onChange={e => onChange({ ...values, [field.key]: e.target.value })}>{(field.options ?? []).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select>
      : field.type === "checkbox"
        ? <input type="checkbox" aria-label={`${scope}: ${field.label}`} checked={(values[field.key] ?? field.value) === "true"} onChange={e => onChange({ ...values, [field.key]: String(e.target.checked) })} />
        : isLongTextField(field)
          ? <AutoTextarea label={`${scope}: ${field.label}`} value={values[field.key] ?? field.value ?? ""} onChange={value => onChange({ ...values, [field.key]: value })} />
          : <input type={field.type ?? "text"} aria-label={`${scope}: ${field.label}`} value={values[field.key] ?? field.value ?? ""} onChange={e => onChange({ ...values, [field.key]: e.target.value })} />}
  </label>;
}

function editorFields(editor: EditorState): Field[] {
  if (!editor) return [];
  const objectOptions: Array<[string, string]> = store.objects.map(item => [item.id, item.title]);
  const tenantOptions: Array<[string, string]> = store.tenants.map(item => [item.id, item.fullName]);
  const item = editor.item;
  const objectField: Field = { key: "objectId", label: "Объект", value: str(item.objectId), type: "select", options: objectOptions };
  const tenantField: Field = { key: "tenantId", label: "Арендатор", value: str(item.tenantId), type: "select", options: tenantOptions };
  switch (editor.type) {
    case "object":
      return [
        { key: "title", label: "Название", value: str(item.title) },
        { key: "type", label: "Тип", value: str(item.type), type: "select", options: [["APARTMENT", "Квартира"], ["COMMERCIAL", "Коммерция"], ["WAREHOUSE", "Склад"], ["STUDIO", "Студия"], ["EQUIPMENT", "Техника"]] },
        { key: "address", label: "Адрес", value: str(item.address) },
        { key: "status", label: "Статус", value: str(item.status), type: "select", options: [["VACANT", "Свободен"], ["OCCUPIED", "Занят"], ["MAINTENANCE", "На обслуживании"]] },
        { key: "monthlyRent", label: "Аренда, ₽", value: str(item.monthlyRent), type: "number" },
        { key: "monthlyUtilityAmount", label: "Коммунальные, ₽/мес", value: str(item.monthlyUtilityAmount), type: "number" },
        { key: "depositAmount", label: "Депозит, ₽", value: str(item.depositAmount), type: "number" },
        { key: "notes", label: "Заметки", value: str(item.notes) }
      ];
    case "tenant":
      return [
        { key: "fullName", label: "ФИО", value: str(item.fullName) },
        { key: "phone", label: "Телефон", value: str(item.phone) },
        { key: "email", label: "Email", value: str(item.email) },
        { key: "telegramChatId", label: "Telegram chat_id", value: str(item.telegramChatId) },
        { key: "whatsappPhone", label: "WhatsApp телефон", value: str(item.whatsappPhone) },
        { key: "notificationsEnabled", label: "Отправлять уведомления", value: String(item.notificationsEnabled !== false), type: "checkbox" },
        { key: "notes", label: "Заметки", value: str(item.notes) }
      ];
    case "contract":
      return [
        objectField,
        tenantField,
        { key: "number", label: "Номер", value: str(item.number) },
        { key: "startDate", label: "Начало", value: str(item.startDate), type: "date" },
        { key: "endDate", label: "Окончание", value: str(item.endDate), type: "date" },
        { key: "rentAmount", label: "Аренда, ₽", value: str(item.rentAmount), type: "number" },
        { key: "paymentDay", label: "День оплаты", value: str(item.paymentDay), type: "number" },
        { key: "depositAmount", label: "Депозит, ₽", value: str(item.depositAmount), type: "number" },
        { key: "status", label: "Статус", value: str(item.status), type: "select", options: [["ACTIVE", "Активен"], ["CLOSED", "Закрыт"], ["DRAFT", "Черновик"]] }
      ];
    case "payment":
      return [
        objectField,
        tenantField,
        { key: "dueDate", label: "Дата платежа", value: str(item.dueDate), type: "date" },
        { key: "paidDate", label: "Дата оплаты", value: str(item.paidDate), type: "date" },
        { key: "amount", label: "Сумма, ₽", value: str(item.amount), type: "number" },
        { key: "paidAmount", label: "Оплачено, ₽", value: str(item.paidAmount), type: "number" },
        { key: "status", label: "Статус", value: str(item.status), type: "select", options: [["PLANNED", "Запланирован"], ["OVERDUE", "Просрочен"], ["PAID", "Оплачен"]] },
        { key: "type", label: "Тип", value: str(item.type), type: "select", options: [["RENT", "Аренда"], ["DEPOSIT", "Депозит"], ["UTILITY", "Коммунальные"], ["OTHER", "Другое"]] },
        { key: "method", label: "Метод оплаты", value: str(item.method) },
        { key: "comment", label: "Комментарий", value: str(item.comment) }
      ];
    case "maintenance":
      return [
        objectField,
        tenantField,
        { key: "title", label: "Название", value: str(item.title) },
        { key: "description", label: "Описание", value: str(item.description) },
        { key: "priority", label: "Приоритет", value: str(item.priority), type: "select", options: [["LOW", "Низкий"], ["MEDIUM", "Средний"], ["HIGH", "Высокий"], ["URGENT", "Срочно"]] },
        { key: "status", label: "Статус", value: str(item.status), type: "select", options: [["NEW", "Новая"], ["IN_PROGRESS", "В работе"], ["WAITING_TENANT", "Ждет арендатора"], ["DONE", "Закрыта"]] },
        { key: "cost", label: "Стоимость, ₽", value: str(item.cost), type: "number" },
        { key: "dueDate", label: "Срок", value: str(item.dueDate), type: "date" }
      ];
    case "document":
      return [
        objectField,
        tenantField,
        { key: "name", label: "Название", value: str(item.name) },
        { key: "type", label: "Тип", value: str(item.type), type: "select", options: documentTypeOptions },
        { key: "expiresAt", label: "Истекает", value: str(item.expiresAt), type: "date" }
      ];
    case "expense":
      return [
        objectField,
        { key: "expenseDate", label: "Дата", value: str(item.expenseDate), type: "date" },
        { key: "category", label: "Категория", value: str(item.category) },
        { key: "amount", label: "Сумма, ₽", value: str(item.amount), type: "number" },
        { key: "vendor", label: "Подрядчик", value: str(item.vendor) },
        { key: "documentUrl", label: "Документ", value: str(item.documentUrl) },
        { key: "comment", label: "Комментарий", value: str(item.comment) }
      ];
    case "listing":
      return [
        objectField,
        { key: "title", label: "Заголовок", value: str(item.title) },
        { key: "description", label: "Описание", value: str(item.description) },
        { key: "price", label: "Цена, ₽", value: str(item.price), type: "number" },
        { key: "publicUrl", label: "Ссылка", value: str(item.publicUrl) },
        { key: "published", label: "Опубликовано", value: String(Boolean(item.published)), type: "checkbox" }
      ];
    case "lead":
      return [
        objectField,
        { key: "fullName", label: "Имя", value: str(item.fullName) },
        { key: "phone", label: "Телефон", value: str(item.phone) },
        { key: "email", label: "Email", value: str(item.email) },
        { key: "source", label: "Источник", value: str(item.source) },
        { key: "status", label: "Статус", value: str(item.status), type: "select", options: [["NEW", "Новый"], ["CONTACTED", "Связались"], ["CLOSED", "Закрыт"]] },
        { key: "comment", label: "Комментарий", value: str(item.comment) }
      ];
  }
}

function editorPayload(editor: EditorState, values: Record<string, string>) {
  if (!editor) return values;
  const base = { ...editor.item, ...values };
  const numbers: Record<string, string[]> = {
    object: ["monthlyRent", "monthlyUtilityAmount", "depositAmount"],
    contract: ["rentAmount", "paymentDay", "depositAmount"],
    payment: ["amount", "paidAmount"],
    maintenance: ["cost"],
    expense: ["amount"],
    listing: ["price"],
    tenant: [],
    document: [],
    lead: []
  };
  for (const key of numbers[editor.type]) {
    base[key] = Number(values[key] || 0);
  }
  for (const key of ["objectId", "tenantId", "paidDate", "dueDate", "expiresAt", "documentUrl", "fileUrl", "publicUrl", "method", "comment"]) {
    if (base[key] === "") base[key] = undefined;
  }
  if (editor.type === "tenant") base.notificationsEnabled = values.notificationsEnabled === "true";
  if (editor.type === "listing") base.published = values.published === "true";
  return base;
}

export function EditDrawer({ editor, onClose }: { editor: EditorState; onClose: () => void }) {
  const fields = editorFields(editor);
  const [values, setValues] = useState<Record<string, string>>(Object.fromEntries(fields.map(field => [field.key, field.value])));
  const [contractFile, setContractFile] = useState<File | null>(null);
  const [contractFileError, setContractFileError] = useState("");
  const [selectedDocumentId, setSelectedDocumentId] = useState("");
  const signature = editor ? `${editor.type}:${str(editor.item.id)}` : "none";
  useEffect(() => {
    setValues(Object.fromEntries(fields.map(field => [field.key, field.value])));
    setContractFile(null);
    setContractFileError("");
    setSelectedDocumentId("");
  }, [signature]);
  if (!editor) return null;
  const contractDocuments = editor.type === "contract" ? store.documents.filter(document => document.contractId === str(editor.item.id)) : [];
  const availableContractDocuments = editor.type === "contract"
    ? store.documents.filter(document => !document.contractId || document.contractId === str(editor.item.id))
    : [];
  const uploadContractDocument = async () => {
    setContractFileError("");
    if (!contractFile) {
      setContractFileError("Выберите файл договора");
      return;
    }
    if (contractFile.size > MAX_DOCUMENT_FILE_SIZE) {
      setContractFileError("Файл больше 30 МБ");
      return;
    }
    const formData = new FormData();
    formData.append("objectId", str(editor.item.objectId));
    formData.append("tenantId", str(editor.item.tenantId));
    formData.append("contractId", str(editor.item.id));
    formData.append("name", `Договор № ${str(editor.item.number)}`);
    formData.append("type", "CONTRACT");
    if (editor.item.endDate) formData.append("expiresAt", str(editor.item.endDate));
    formData.append("file", contractFile);
    await store.uploadDocument(formData);
    setContractFile(null);
  };
  const linkExistingDocument = async () => {
    if (!selectedDocumentId) return;
    const document = store.documents.find(item => item.id === selectedDocumentId);
    if (!document) return;
    await store.update(`/documents/${document.id}`, {
      ...document,
      objectId: editor.item.objectId,
      tenantId: editor.item.tenantId,
      contractId: editor.item.id,
      type: document.type || "CONTRACT"
    }, "Документ привязан к договору");
    setSelectedDocumentId("");
  };
  return <div className="drawer-backdrop" role="dialog" aria-modal="true">
    <aside className="drawer">
      <header>
        <div>
          <h2>{editor.title}</h2>
          <p>Просмотр и редактирование записи</p>
        </div>
        <button className="ghost" onClick={onClose}>Закрыть</button>
      </header>
      <form className="drawer-form" onSubmit={async event => {
        event.preventDefault();
        const defaultValues = Object.fromEntries(fields.map(field => [field.key, field.value]));
        await store.update(editor.path, editorPayload(editor, { ...defaultValues, ...values }), "Запись обновлена");
        onClose();
      }}>
        {fields.map(field => <FieldInput key={field.key} scope={editor.title} field={field} values={values} onChange={setValues} />)}
        {editor.type === "contract" && <section className="linked-documents" data-testid="contract-documents">
          <h3>Файлы договора</h3>
          {contractDocuments.length === 0 && <p className="muted">Файлы к договору пока не добавлены.</p>}
          {contractDocuments.map(document => <div className="linked-document-row" key={document.id}>
            <span>{document.originalFileName ?? document.name}</span>
            <button type="button" onClick={() => store.downloadDocument(document)} disabled={Boolean(store.actionInFlight)}>Скачать</button>
          </div>)}
          <div className="contract-document-tools">
            <label>Добавить файл договора
              <input aria-label="Договор: Добавить файл" type="file" accept={DOCUMENT_ACCEPT} onChange={event => setContractFile(event.target.files?.[0] ?? null)} />
            </label>
            <button type="button" onClick={uploadContractDocument} disabled={Boolean(store.actionInFlight)}>Загрузить в документы</button>
            {contractFileError && <p className="error">{contractFileError}</p>}
          </div>
          {availableContractDocuments.length > 0 && <div className="contract-document-tools">
            <label>Выбрать из документов
              <select aria-label="Договор: Выбрать документ" value={selectedDocumentId} onChange={event => setSelectedDocumentId(event.target.value)}>
                <option value="">Не выбрано</option>
                {availableContractDocuments.map(document => <option key={document.id} value={document.id}>{document.originalFileName ?? document.name}</option>)}
              </select>
            </label>
            <button type="button" onClick={linkExistingDocument} disabled={!selectedDocumentId || Boolean(store.actionInFlight)}>Привязать</button>
          </div>}
        </section>}
        <div className="actions">
          <button className="primary" type="submit" disabled={Boolean(store.actionInFlight)}>Сохранить</button>
          <button className="ghost" type="button" onClick={onClose}>Отмена</button>
        </div>
      </form>
    </aside>
  </div>;
}

export const NotificationsPanel = observer(() => {
  const settings = store.notificationSettings;
  const [form, setForm] = useState(settings);
  useEffect(() => setForm(settings), [settings]);
  if (!form) return null;

  const change = (key: keyof typeof form, value: string | boolean | number) => setForm({ ...form, [key]: value });
  return <section className="panel settings-panel">
    <h2><Bell />Уведомления об оплате</h2>
    <div className="provider-guides">
      {notificationGuides.map(provider => <details className="provider-guide" key={provider.key}>
        <summary>
          <span className="provider-icon">{provider.icon}</span>
          <span><strong>{provider.title}</strong><em>{provider.subtitle}</em></span>
        </summary>
        <ol>
          {provider.steps.map(step => <li key={step}>{step}</li>)}
        </ol>
        <a href={provider.docUrl} target="_blank" rel="noreferrer">Открыть официальную документацию</a>
      </details>)}
    </div>
    <div className="settings-grid">
      <label className="check"><input type="checkbox" checked={form.enabled} onChange={e => change("enabled", e.target.checked)} /> Включить отправку</label>
      <label>За сколько дней<input type="number" min="0" value={form.remindDaysBefore} onChange={e => change("remindDaysBefore", Number(e.target.value))} /></label>
      <label>Время отправки<input value={form.reminderTime} onChange={e => change("reminderTime", e.target.value)} /></label>
      <label className="check"><input type="checkbox" checked={form.telegramEnabled} onChange={e => change("telegramEnabled", e.target.checked)} /> Telegram</label>
      <label>Telegram bot token<input value={form.telegramBotToken ?? ""} onChange={e => change("telegramBotToken", e.target.value)} /></label>
      <label>Default chat_id<input value={form.telegramDefaultChatId ?? ""} onChange={e => change("telegramDefaultChatId", e.target.value)} /></label>
      <label className="check"><input type="checkbox" checked={form.smsRuEnabled} onChange={e => change("smsRuEnabled", e.target.checked)} /> SMS.RU</label>
      <label>SMS.RU api_id<input value={form.smsRuApiId ?? ""} onChange={e => change("smsRuApiId", e.target.value)} /></label>
      <label>SMS отправитель<input value={form.smsRuSender ?? ""} onChange={e => change("smsRuSender", e.target.value)} /></label>
      <label className="check"><input type="checkbox" checked={form.smsRuTestMode} onChange={e => change("smsRuTestMode", e.target.checked)} /> SMS test=1</label>
      <label className="check"><input type="checkbox" checked={form.whatsappEnabled} onChange={e => change("whatsappEnabled", e.target.checked)} /> WhatsApp webhook</label>
      <label>WhatsApp API URL<input value={form.whatsappApiUrl ?? ""} onChange={e => change("whatsappApiUrl", e.target.value)} /></label>
      <label>WhatsApp token<input value={form.whatsappToken ?? ""} onChange={e => change("whatsappToken", e.target.value)} /></label>
      <label>WhatsApp default recipient<input value={form.whatsappDefaultRecipient ?? ""} onChange={e => change("whatsappDefaultRecipient", e.target.value)} /></label>
      <label className="wide">Шаблон сообщения<input value={form.messageTemplate} onChange={e => change("messageTemplate", e.target.value)} /></label>
    </div>
    <div className="actions">
      <button className="primary" onClick={() => store.saveNotificationSettings(form)} disabled={Boolean(store.actionInFlight)}>Сохранить</button>
      <button className="ghost" onClick={() => store.sendDueNotifications()} disabled={Boolean(store.actionInFlight)}><Send size={18} /> Отправить сейчас</button>
    </div>
    <div className="delivery-list">
      {store.notificationDeliveries.slice(0, 6).map(item => <div className="row" key={item.id}><span>{item.channel}</span><strong>{status(item.status)}</strong><em>{item.recipient ?? "нет получателя"}</em></div>)}
    </div>
  </section>;
});

export const BillingPanel = observer(() => {
  const settings = store.billingSettings;
  const [form, setForm] = useState(settings);
  useEffect(() => setForm(settings), [settings]);
  if (!form) return null;
  const change = (key: keyof typeof form, value: string | boolean) => setForm({ ...form, [key]: value });
  return <section className="panel settings-panel">
    <h2><RussianRuble />Подписка и прием оплаты</h2>
    <div className="tariffs">
      <div className="tariff">
        <strong>FREE</strong><span>3 объекта</span><em>0 ₽/мес</em>
      </div>
      {[
        ["START", "20 объектов", "300 ₽"],
        ["BUSINESS", "100 объектов", "1000 ₽"]
      ].map(([tariff, limit, price]) => <button key={tariff} className="tariff" onClick={() => store.createCheckout(tariff)} disabled={Boolean(store.actionInFlight)}>
        <strong>{tariff}</strong><span>{limit}</span><em>{price}/мес</em>
      </button>)}
    </div>
    <div className="settings-grid">
      <label>Провайдер<input value={form.provider} onChange={e => change("provider", e.target.value)} /></label>
      <label>ЮKassa shopId<input value={form.yookassaShopId ?? ""} onChange={e => change("yookassaShopId", e.target.value)} /></label>
      <label>ЮKassa secretKey<input value={form.yookassaSecretKey ?? ""} onChange={e => change("yookassaSecretKey", e.target.value)} /></label>
      <label>Generic payment URL<input value={form.genericPaymentUrl ?? ""} onChange={e => change("genericPaymentUrl", e.target.value)} /></label>
      <label className="check"><input type="checkbox" checked={form.testMode} onChange={e => change("testMode", e.target.checked)} /> Тестовый режим</label>
    </div>
    <div className="actions"><button className="primary" onClick={() => store.saveBillingSettings(form)} disabled={Boolean(store.actionInFlight)}>Сохранить оплату</button></div>
    <div className="delivery-list">
      {store.billingInvoices.slice(0, 4).map(item => <div className="row" key={item.id}><span>{item.tariff}</span><strong>{money(item.amount)}</strong><em>{status(item.status)}</em>{item.confirmationUrl && <a href={item.confirmationUrl}>Оплатить</a>}<button onClick={() => store.markInvoicePaid(item.id)} disabled={Boolean(store.actionInFlight)}>Оплачено</button></div>)}
    </div>
  </section>;
});

export const AuditPanel = observer(() => <section className="panel settings-panel">
  <h2><History />Журнал действий</h2>
  <p className="muted">Последние 100 действий в кабинете: создание, изменение, удаление, платежи и настройки.</p>
  <div className="delivery-list">
    {store.auditLogs.length === 0 && <p className="muted">Действий пока нет</p>}
    {store.auditLogs.map(item => <div className="row" key={item.id}>
      <span>{new Date(item.createdAt).toLocaleString("ru-RU")} · {item.summary ?? status(item.entityType)}</span>
      <strong>{status(item.action)}</strong>
      <em>{status(item.entityType)}</em>
      <small>{item.actorEmail}</small>
    </div>)}
  </div>
</section>);

export function Onboarding({ onNavigate }: { onNavigate: (section: Section) => void }) {
  const steps = [
    { done: store.objects.length > 0, title: "Добавьте первый объект", action: "Добавить объект", section: "objects" as Section },
    { done: store.tenants.length > 0, title: "Добавьте арендатора", action: "Добавить арендатора", section: "objects" as Section },
    { done: store.contracts.length > 0, title: "Создайте договор", action: "Создать договор", section: "objects" as Section },
    { done: store.payments.length > 0, title: "Проверьте платежный календарь", action: "Открыть платежи", section: "payments" as Section },
    { done: Boolean(store.notificationSettings?.enabled), title: "Настройте напоминания об оплате", action: "Настроить", section: "notifications" as Section }
  ];
  if (steps.every(step => step.done)) return null;
  return <section className="onboarding">
    <div>
      <h2>Быстрый старт</h2>
      <p>Пройдите несколько шагов, чтобы кабинет начал помогать с платежами и договорами.</p>
    </div>
    <div className="onboarding-steps">
      {steps.map(step => <button key={step.title} className={step.done ? "done" : ""} onClick={() => onNavigate(step.section)}>
        <CheckCircle2 size={18} /><span>{step.title}</span><strong>{step.done ? "Готово" : step.action}</strong>
      </button>)}
    </div>
  </section>;
}

export const PaymentsPanel = observer(({ compact = false, onEdit }: { compact?: boolean; onEdit: (editor: EditorState) => void }) => <DataPanel title="Ближайшие платежи" icon={<RussianRuble />} empty="Пока нет платежей">
  {store.payments.length > 0 ? <PaymentsTable compact={compact} onEdit={payment => onEdit({ type: 'payment', title: 'Платеж', path: `/payments/${payment.id}`, item: payment })} /> : null}
</DataPanel>);

export const ObjectsPanel = observer(({ onEdit }: { onEdit: (editor: EditorState) => void }) => <DataPanel title="Объекты" icon={<Home />} empty="Добавьте первый объект">
  {store.objects.length > 0 ? <ObjectsTable onEdit={object => onEdit({ type: 'object', title: 'Объект', path: `/objects/${object.id}`, item: object })} /> : null}
</DataPanel>);

const ObjectsTable = observer(function ObjectsTable({ onEdit }: { onEdit: (object: RentalObject) => void }) {
  type ObjectSortKey = "title" | "monthlyRent" | "status";
  const [sort, setSort] = useState<SortState<ObjectSortKey>>({ key: "title", direction: "asc" });
  const onSort = (key: ObjectSortKey) => setSort(current => current.key === key ? { key, direction: current.direction === "asc" ? "desc" : "asc" } : { key, direction: "asc" });
  const sortedRows = sortItems(store.objects, sort, {
    title: object => object.title,
    monthlyRent: object => object.monthlyRent,
    status: object => status(object.status)
  });

  return <div className="data-table objects-table" data-testid="objects-table">
    <div className="table-head">
      <SortHeader label="Название" sortKey="title" sort={sort} onSort={onSort} testId="sort-objects-title" />
      <SortHeader label="Аренда" sortKey="monthlyRent" sort={sort} onSort={onSort} testId="sort-objects-rent" />
      <SortHeader label="Статус" sortKey="status" sort={sort} onSort={onSort} testId="sort-objects-status" />
      <span>Действия</span>
    </div>
    {sortedRows.map(object => <div className="table-row object-row" key={object.id}>
      <span className="table-main" title={object.title}>{object.title}</span>
      <strong>{money(object.monthlyRent)}</strong>
      <em className={`status status-${object.status.toLowerCase()}`}>{status(object.status)}</em>
      <span className="table-actions">
        <button onClick={() => onEdit(object)}>Открыть</button>
        <button onClick={() => store.update(`/objects/${object.id}`, { ...object, status: object.status === 'OCCUPIED' ? 'VACANT' : 'OCCUPIED' }, 'Статус объекта изменен')} disabled={Boolean(store.actionInFlight)}>{object.status === 'OCCUPIED' ? 'Свободен' : 'Занят'}</button>
        <DeleteIconButton label={`Удалить объект ${object.title}`} onConfirm={() => confirmAction(`Удалить объект "${object.title}"?`, () => store.remove(`/objects/${object.id}`))} disabled={Boolean(store.actionInFlight)} />
      </span>
    </div>)}
  </div>;
});

export const TenantsPanel = observer(({ onEdit }: { onEdit: (editor: EditorState) => void }) => <DataPanel title="Арендаторы" icon={<Users />} empty="Добавьте арендатора">
  {store.tenants.map(t => <div className="row tenant-row" key={t.id}>
    <span>{t.fullName}</span>
    <strong>{t.phone}</strong>
    <em>{t.notificationsEnabled === false ? 'уведомления выкл.' : 'уведомления вкл.'}</em>
    <span className="row-actions">
      <button onClick={() => onEdit({ type: 'tenant', title: 'Арендатор', path: `/tenants/${t.id}`, item: t })}>Открыть</button>
      {t.publicRequestToken && <MaintenanceAccess token={t.publicRequestToken} />}
      <button onClick={() => store.update(`/tenants/${t.id}`, { ...t, notificationsEnabled: t.notificationsEnabled === false }, 'Настройки арендатора изменены')} disabled={Boolean(store.actionInFlight)}>{t.notificationsEnabled === false ? 'Включить' : 'Отключить'}</button>
      <DeleteIconButton label={`Удалить арендатора ${t.fullName}`} onConfirm={() => confirmAction(`Удалить арендатора "${t.fullName}"?`, () => store.remove(`/tenants/${t.id}`))} disabled={Boolean(store.actionInFlight)} />
    </span>
  </div>)}
</DataPanel>);

export const ContractsPanel = observer(({ onEdit }: { onEdit: (editor: EditorState) => void }) => <DataPanel title="Договоры" icon={<FileText />} empty="Создайте договор после объекта и арендатора">
  {store.contracts.map(c => <div className="row" key={c.id}><span>№ {c.number}</span><strong>до {c.endDate}</strong><em>{status(c.status)}</em><button onClick={() => onEdit({ type: 'contract', title: 'Договор', path: `/contracts/${c.id}`, item: c })}>Открыть</button></div>)}
</DataPanel>);

export const MaintenancePanel = observer(({ compact = false, onEdit }: { compact?: boolean; onEdit: (editor: EditorState) => void }) => <DataPanel title="Заявки" icon={<Wrench />} empty="Заявок нет">
  {store.maintenance.length > 0 ? <MaintenanceTable compact={compact} onEdit={request => onEdit({ type: 'maintenance', title: 'Заявка', path: `/maintenance/${request.id}`, item: request })} /> : null}
</DataPanel>);

export const DocumentsPanel = observer(({ compact = false, onEdit }: { compact?: boolean; onEdit: (editor: EditorState) => void }) => <DataPanel title="Документы" icon={<FileText />} empty="Документов нет">
  {store.documents.length > 0 ? <DocumentsTable compact={compact} onEdit={document => onEdit({ type: 'document', title: 'Документ', path: `/documents/${document.id}`, item: document })} /> : null}
</DataPanel>);

export const ExpensesPanel = observer(({ onEdit }: { onEdit: (editor: EditorState) => void }) => <DataPanel title="Расходы" icon={<TrendingDown />} empty="Расходов нет">
  {store.expenses.map(e => <div className="row" key={e.id}><span>{e.category}</span><strong>{money(e.amount)}</strong><em>{e.source === 'UTILITY_RECURRING' ? `авто · ${e.expenseDate}` : e.expenseDate}</em><button onClick={() => onEdit({ type: 'expense', title: 'Расход', path: `/expenses/${e.id}`, item: e })}>Открыть</button><DeleteIconButton label={`Удалить расход ${e.category}`} onConfirm={() => confirmAction(`Удалить расход "${e.category}"?`, () => store.remove(`/expenses/${e.id}`))} disabled={Boolean(store.actionInFlight)} /></div>)}
</DataPanel>);

export const PipelinePanel = observer(({ compact = false, onEdit }: { compact?: boolean; onEdit: (editor: EditorState) => void }) => <DataPanel title="Объявления и лиды" icon={<Users />} empty="Лидов пока нет">
  {(store.leads.length > 0 || store.listings.length > 0) ? <PipelineTable compact={compact} onEditLead={lead => onEdit({ type: 'lead', title: 'Лид', path: `/leads/${lead.id}`, item: lead })} onEditListing={listing => onEdit({ type: 'listing', title: 'Объявление', path: `/listings/${listing.id}`, item: listing })} /> : null}
</DataPanel>);

const PaymentsTable = observer(function PaymentsTable({ compact = false, onEdit }: { compact?: boolean; onEdit: (payment: Payment) => void }) {
  const objectTitle = (id: string) => store.objects.find(object => object.id === id)?.title ?? "Объект не найден";
  const tenantName = (id?: string) => id ? store.tenants.find(tenant => tenant.id === id)?.fullName : "";
  type PaymentSortKey = "object" | "purpose" | "dueDate" | "amount" | "status";
  const [sort, setSort] = useState<SortState<PaymentSortKey>>({ key: "dueDate", direction: "asc" });
  const [page, setPage] = useState(1);
  const onSort = (key: PaymentSortKey) => setSort(current => current.key === key ? { key, direction: current.direction === "asc" ? "desc" : "asc" } : { key, direction: "asc" });
  useEffect(() => setPage(1), [sort.key, sort.direction]);
  const sortedRows = sortItems(store.payments, sort, {
    object: payment => objectTitle(payment.objectId),
    purpose: payment => payment.comment ?? status(payment.type),
    dueDate: payment => payment.dueDate,
    amount: payment => payment.amount,
    status: payment => status(payment.status)
  });
  const paged = pageItems(sortedRows, page, compact ? COMPACT_TABLE_PAGE_SIZE : TABLE_PAGE_SIZE);

  return <div className={`data-table payments-table ${compact ? "compact-table" : ""}`} data-testid="payments-table">
    <div className="table-head">
      <SortHeader label="Объект" sortKey="object" sort={sort} onSort={onSort} testId="sort-payments-object" />
      <SortHeader label="Назначение" sortKey="purpose" sort={sort} onSort={onSort} className="optional-col" testId="sort-payments-purpose" />
      <SortHeader label="Дата" sortKey="dueDate" sort={sort} onSort={onSort} testId="sort-payments-date" />
      <SortHeader label="Сумма" sortKey="amount" sort={sort} onSort={onSort} testId="sort-payments-amount" />
      <SortHeader label="Статус" sortKey="status" sort={sort} onSort={onSort} testId="sort-payments-status" />
      <span>Действия</span>
    </div>
    {paged.rows.map(payment => <div className="table-row" key={payment.id}>
      <span className="table-main" title={objectTitle(payment.objectId)}>
        {objectTitle(payment.objectId)}
        {tenantName(payment.tenantId) && <small>{tenantName(payment.tenantId)}</small>}
      </span>
      <span className="optional-col" title={payment.comment ?? status(payment.type)}>{payment.comment ?? status(payment.type)}</span>
      <strong>{payment.dueDate}</strong>
      <strong>{money(payment.amount)}</strong>
      <em className={`status status-${payment.status.toLowerCase()}`}>{status(payment.status)}</em>
      <span className="table-actions">
        <button onClick={() => onEdit(payment)}>Открыть</button>
        {payment.status !== "PAID" && <button onClick={() => store.markPaymentPaid(payment.id)} disabled={Boolean(store.actionInFlight)}>Оплачено</button>}
        <DeleteIconButton label="Удалить платеж" onConfirm={() => confirmAction("Удалить этот платеж?", () => store.remove(`/payments/${payment.id}`))} disabled={Boolean(store.actionInFlight)} />
      </span>
    </div>)}
    <TablePagination page={paged.currentPage} totalPages={paged.totalPages} onPage={setPage} testId="pagination-payments" />
  </div>;
});

const MaintenanceTable = observer(function MaintenanceTable({ compact = false, onEdit }: { compact?: boolean; onEdit: (request: Maintenance) => void }) {
  const objectTitle = (id: string) => store.objects.find(object => object.id === id)?.title ?? "Объект не найден";
  type MaintenanceSortKey = "title" | "object" | "dueDate" | "cost" | "status";
  const [sort, setSort] = useState<SortState<MaintenanceSortKey>>({ key: "status", direction: "asc" });
  const [page, setPage] = useState(1);
  const onSort = (key: MaintenanceSortKey) => setSort(current => current.key === key ? { key, direction: current.direction === "asc" ? "desc" : "asc" } : { key, direction: "asc" });
  useEffect(() => setPage(1), [sort.key, sort.direction]);
  const sortedRows = sortItems(store.maintenance, sort, {
    title: request => request.title,
    object: request => objectTitle(request.objectId),
    dueDate: request => request.dueDate ?? "",
    cost: request => request.cost,
    status: request => status(request.status)
  });
  const paged = pageItems(sortedRows, page, compact ? COMPACT_TABLE_PAGE_SIZE : TABLE_PAGE_SIZE);

  return <div className={`data-table maintenance-table ${compact ? "compact-table" : ""}`} data-testid="maintenance-table">
    <div className="table-head">
      <SortHeader label="Заявка" sortKey="title" sort={sort} onSort={onSort} testId="sort-maintenance-title" />
      <SortHeader label="Объект" sortKey="object" sort={sort} onSort={onSort} className="optional-col" testId="sort-maintenance-object" />
      <SortHeader label="Срок" sortKey="dueDate" sort={sort} onSort={onSort} testId="sort-maintenance-date" />
      <SortHeader label="Стоимость" sortKey="cost" sort={sort} onSort={onSort} testId="sort-maintenance-cost" />
      <SortHeader label="Статус" sortKey="status" sort={sort} onSort={onSort} testId="sort-maintenance-status" />
      <span>Действия</span>
    </div>
    {paged.rows.map(request => <div className="table-row" key={request.id}>
      <span className="table-main" title={request.title}>
        {request.title}
        {request.description && <small>{request.description}</small>}
      </span>
      <span className="optional-col" title={objectTitle(request.objectId)}>{objectTitle(request.objectId)}</span>
      <strong>{request.dueDate ?? "без срока"}</strong>
      <strong>{money(request.cost)}</strong>
      <em className={`status status-${request.status.toLowerCase()}`}>{status(request.status)}</em>
      <span className="table-actions">
        <button onClick={() => onEdit(request)}>Открыть</button>
        {request.status !== "DONE" && <button onClick={() => store.update(`/maintenance/${request.id}`, { ...request, status: request.status === "NEW" ? "IN_PROGRESS" : "DONE" }, "Статус заявки изменен")} disabled={Boolean(store.actionInFlight)}>{request.status === "NEW" ? "В работу" : "Закрыть"}</button>}
        <DeleteIconButton label={`Удалить заявку ${request.title}`} onConfirm={() => confirmAction(`Удалить заявку "${request.title}"?`, () => store.remove(`/maintenance/${request.id}`))} disabled={Boolean(store.actionInFlight)} />
      </span>
    </div>)}
    <TablePagination page={paged.currentPage} totalPages={paged.totalPages} onPage={setPage} testId="pagination-maintenance" />
  </div>;
});

const DocumentsTable = observer(function DocumentsTable({ compact = false, onEdit }: { compact?: boolean; onEdit: (document: DocumentItem) => void }) {
  const objectTitle = (id?: string) => id ? store.objects.find(object => object.id === id)?.title : "";
  const fileLabel = (document: DocumentItem) => document.originalFileName ? document.originalFileName : "файл";
  type DocumentSortKey = "name" | "object" | "type" | "expiresAt";
  const [sort, setSort] = useState<SortState<DocumentSortKey>>({ key: "name", direction: "asc" });
  const [page, setPage] = useState(1);
  const onSort = (key: DocumentSortKey) => setSort(current => current.key === key ? { key, direction: current.direction === "asc" ? "desc" : "asc" } : { key, direction: "asc" });
  useEffect(() => setPage(1), [sort.key, sort.direction]);
  const sortedRows = sortItems(store.documents, sort, {
    name: document => document.name,
    object: document => objectTitle(document.objectId),
    type: document => status(document.type),
    expiresAt: document => document.expiresAt ?? ""
  });
  const paged = pageItems(sortedRows, page, compact ? COMPACT_TABLE_PAGE_SIZE : TABLE_PAGE_SIZE);

  return <div className={`data-table documents-table ${compact ? "compact-table" : ""}`} data-testid="documents-table">
    <div className="table-head">
      <SortHeader label="Документ" sortKey="name" sort={sort} onSort={onSort} testId="sort-documents-name" />
      <SortHeader label="Объект" sortKey="object" sort={sort} onSort={onSort} className="optional-col" testId="sort-documents-object" />
      <SortHeader label="Тип" sortKey="type" sort={sort} onSort={onSort} testId="sort-documents-type" />
      <SortHeader label="Срок" sortKey="expiresAt" sort={sort} onSort={onSort} testId="sort-documents-date" />
      <span>Действия</span>
    </div>
    {paged.rows.map(document => <div className="table-row" key={document.id}>
      <span className="table-main" title={document.name}>
        {document.name}
        <small>{[objectTitle(document.objectId), fileLabel(document)].filter(Boolean).join(" · ")}</small>
      </span>
      <span className="optional-col" title={objectTitle(document.objectId)}>{objectTitle(document.objectId) || "не привязан"}</span>
      <strong>{status(document.type)}</strong>
      <em>{document.expiresAt ?? "без срока"}</em>
      <span className="table-actions">
        <button onClick={() => onEdit(document)}>Открыть</button>
        {document.fileUrl && <button onClick={() => store.downloadDocument(document)} disabled={Boolean(store.actionInFlight)}>Файл</button>}
        <DeleteIconButton label={`Удалить документ ${document.name}`} onConfirm={() => confirmAction(`Удалить документ "${document.name}"?`, () => store.remove(`/documents/${document.id}`))} disabled={Boolean(store.actionInFlight)} />
      </span>
    </div>)}
    <TablePagination page={paged.currentPage} totalPages={paged.totalPages} onPage={setPage} testId="pagination-documents" />
  </div>;
});

const PipelineTable = observer(function PipelineTable({ compact = false, onEditLead, onEditListing }: { compact?: boolean; onEditLead: (lead: Lead) => void; onEditListing: (listing: Listing) => void }) {
  type PipelineSortKey = "name" | "secondary" | "type" | "status";
  type PipelineRow = { id: string; name: string; secondary: string; type: string; statusValue: string; lead?: Lead; listing?: Listing };
  const [sort, setSort] = useState<SortState<PipelineSortKey>>({ key: "name", direction: "asc" });
  const [page, setPage] = useState(1);
  const onSort = (key: PipelineSortKey) => setSort(current => current.key === key ? { key, direction: current.direction === "asc" ? "desc" : "asc" } : { key, direction: "asc" });
  useEffect(() => setPage(1), [sort.key, sort.direction]);
  const pipelineRows: PipelineRow[] = [
    ...store.leads.map(lead => ({ id: `lead-${lead.id}`, name: lead.fullName, secondary: lead.phone, type: "Лид", statusValue: status(lead.status), lead })),
    ...store.listings.map(listing => ({ id: `listing-${listing.id}`, name: listing.title, secondary: money(listing.price), type: "Объявление", statusValue: listing.published ? "Опубликовано" : "Черновик", listing }))
  ];
  const sortedRows = sortItems(pipelineRows, sort, {
    name: row => row.name,
    secondary: row => row.secondary,
    type: row => row.type,
    status: row => row.statusValue
  });
  const paged = pageItems(sortedRows, page, compact ? COMPACT_TABLE_PAGE_SIZE : TABLE_PAGE_SIZE);

  return <div className={`data-table pipeline-table ${compact ? "compact-table" : ""}`} data-testid="pipeline-table">
    <div className="table-head">
      <SortHeader label="Название" sortKey="name" sort={sort} onSort={onSort} testId="sort-pipeline-name" />
      <SortHeader label="Контакт / цена" sortKey="secondary" sort={sort} onSort={onSort} className="optional-col" testId="sort-pipeline-secondary" />
      <SortHeader label="Тип" sortKey="type" sort={sort} onSort={onSort} testId="sort-pipeline-type" />
      <SortHeader label="Статус" sortKey="status" sort={sort} onSort={onSort} testId="sort-pipeline-status" />
      <span>Действия</span>
    </div>
    {paged.rows.map(row => {
      if (row.lead) {
        const lead = row.lead;
        return <div className="table-row" key={row.id}>
          <span className="table-main" title={lead.fullName}>
            {lead.fullName}
            <small>{lead.email ?? lead.source ?? "лид"}</small>
          </span>
          <strong className="optional-col">{lead.phone}</strong>
          <strong>Лид</strong>
          <em className={`status status-${lead.status.toLowerCase()}`}>{status(lead.status)}</em>
          <span className="table-actions">
            <button onClick={() => onEditLead(lead)}>Открыть</button>
            <button onClick={() => store.update(`/leads/${lead.id}`, { ...lead, status: lead.status === "NEW" ? "CONTACTED" : "CLOSED" }, "Статус лида изменен")} disabled={Boolean(store.actionInFlight)}>{lead.status === "NEW" ? "Связались" : "Закрыть"}</button>
            <DeleteIconButton label={`Удалить лид ${lead.fullName}`} onConfirm={() => confirmAction(`Удалить лид "${lead.fullName}"?`, () => store.remove(`/leads/${lead.id}`))} disabled={Boolean(store.actionInFlight)} />
          </span>
        </div>;
      }
      const listing = row.listing!;
      return <div className="table-row" key={row.id}>
        <span className="table-main" title={listing.title}>
          {listing.title}
          <small>{listing.description}</small>
        </span>
        <strong className="optional-col">{money(listing.price)}</strong>
        <strong>Объявление</strong>
        <em>{listing.published ? "Опубликовано" : "Черновик"}</em>
        <span className="table-actions">
          <button onClick={() => onEditListing(listing)}>Открыть</button>
          <button onClick={() => store.update(`/listings/${listing.id}`, { ...listing, published: !listing.published }, "Статус объявления изменен")} disabled={Boolean(store.actionInFlight)}>{listing.published ? "Снять" : "Опубликовать"}</button>
          <DeleteIconButton label={`Удалить объявление ${listing.title}`} onConfirm={() => confirmAction(`Удалить объявление "${listing.title}"?`, () => store.remove(`/listings/${listing.id}`))} disabled={Boolean(store.actionInFlight)} />
        </span>
      </div>;
    })}
    <TablePagination page={paged.currentPage} totalPages={paged.totalPages} onPage={setPage} testId="pagination-pipeline" />
  </div>;
});
