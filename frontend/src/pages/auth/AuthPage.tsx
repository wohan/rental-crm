import { useState } from 'react';
import { observer } from 'mobx-react-lite';
import { CheckCircle2 } from 'lucide-react';
import { store } from '../../stores/appStore';

export const AuthPage = observer(() => {
  const resetToken = new URLSearchParams(window.location.search).get("token") ?? "";
  const [mode, setMode] = useState<"login" | "register" | "forgot">(resetToken ? "forgot" : "register");
  const [form, setForm] = useState({ email: "owner@example.ru", password: "password123", fullName: "Иван Петров", accountName: "Петров Аренда", termsAccepted: true, privacyAccepted: true, notificationConsent: true });
  const [newPassword, setNewPassword] = useState("");
  const submit = async (event?: React.SyntheticEvent) => {
    event?.preventDefault();
    if (store.actionInFlight) return;
    if (resetToken) {
      await store.resetPassword(resetToken, newPassword);
      setMode("login");
      window.history.replaceState(null, "", "/");
      return;
    }
    if (mode === "forgot") {
      await store.forgotPassword(form.email);
      return;
    }
    mode === "register" ? await store.register(form) : await store.login(form);
  };
  return <main className="auth-shell">
    <section className="auth-panel">
      <div>
        <span className="brand">Rental Management</span>
        <h1>Кабинет арендодателя</h1>
        <p>Платежи, договоры, заявки и доходность в одном рабочем экране.</p>
      </div>
      <form onSubmit={submit} className="form">
        {!resetToken && <div className="segmented">
          <button type="button" className={mode === "register" ? "active" : ""} onClick={() => setMode("register")}>Регистрация</button>
          <button type="button" className={mode === "login" ? "active" : ""} onClick={() => setMode("login")}>Вход</button>
        </div>}
        {resetToken && <>
          <label>Новый пароль<input type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)} /></label>
        </>}
        {!resetToken && mode === "register" && <>
          <label>Название кабинета<input value={form.accountName} onChange={e => setForm({ ...form, accountName: e.target.value })} /></label>
          <label>Ваше имя<input value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} /></label>
        </>}
        {!resetToken && <label>Email<input type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} /></label>}
        {!resetToken && mode !== "forgot" && <label>Пароль<input type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} /></label>}
        {!resetToken && mode === "register" && <>
          <label className="check"><input type="checkbox" checked={form.termsAccepted} onChange={e => setForm({ ...form, termsAccepted: e.target.checked })} /> Принимаю оферту</label>
          <label className="check"><input type="checkbox" checked={form.privacyAccepted} onChange={e => setForm({ ...form, privacyAccepted: e.target.checked })} /> Принимаю политику персональных данных</label>
          <label className="check"><input type="checkbox" checked={form.notificationConsent} onChange={e => setForm({ ...form, notificationConsent: e.target.checked })} /> Разрешаю отправлять сервисные уведомления</label>
        </>}
        {store.error && <p className="error">{store.error}</p>}
        {store.notice && <p className="notice"><CheckCircle2 size={18} /> {store.notice}</p>}
        {store.resetLink && <p className="dev-link">Dev reset link: <a href={store.resetLink}>{store.resetLink}</a></p>}
        {store.verificationLink && <p className="dev-link">Dev verify link: <a href={store.verificationLink}>{store.verificationLink}</a></p>}
        <button className="primary" type="button" onClick={event => submit(event)} disabled={Boolean(store.actionInFlight)} data-testid="auth-submit">
          {store.actionInFlight || (resetToken ? "Сменить пароль" : mode === "register" ? "Создать кабинет" : mode === "forgot" ? "Получить ссылку" : "Войти")}
        </button>
        {!resetToken && <button className="link-button" type="button" onClick={() => setMode(mode === "forgot" ? "login" : "forgot")}>
          {mode === "forgot" ? "Вернуться ко входу" : "Забыли пароль?"}
        </button>}
      </form>
    </section>
  </main>;
});

