import { useState } from 'react';
import { CheckCircle2 } from 'lucide-react';
import { AutoTextarea } from '../../components/AutoTextarea';

export function PublicMaintenancePage() {
  const token = new URLSearchParams(window.location.search).get("token") ?? "";
  const [form, setForm] = useState({ title: "", description: "", contactName: "", contactPhone: "" });
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");
  const submit = async (event: React.SyntheticEvent) => {
    event.preventDefault();
    setError("");
    setNotice("");
    try {
      const response = await fetch(`/api/public/maintenance/${token}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form)
      });
      const body = await response.json().catch(() => ({}));
      if (!response.ok) throw new Error(body.error ?? "Не удалось отправить заявку");
      setNotice(body.message ?? "Заявка принята");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Ошибка отправки");
    }
  };
  return <main className="auth-shell">
    <section className="auth-panel">
      <div>
        <span className="brand">Rental Management</span>
        <h1>Заявка на обслуживание</h1>
        <p>Опишите проблему, и владелец увидит заявку в кабинете.</p>
      </div>
      <form className="form" onSubmit={submit}>
        <label>Название<input value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} /></label>
        <label>Описание<AutoTextarea label="Описание" value={form.description} onChange={value => setForm({ ...form, description: value })} /></label>
        <label>Ваше имя<input value={form.contactName} onChange={e => setForm({ ...form, contactName: e.target.value })} /></label>
        <label>Телефон<input value={form.contactPhone} onChange={e => setForm({ ...form, contactPhone: e.target.value })} /></label>
        {error && <p className="error">{error}</p>}
        {notice && <p className="notice" data-testid="public-maintenance-notice"><CheckCircle2 size={18} /> {notice}</p>}
        <button className="primary" type="submit" data-testid="public-maintenance-submit">Отправить заявку</button>
      </form>
    </section>
  </main>;
}

