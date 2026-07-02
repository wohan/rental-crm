import { useEffect, useState } from 'react';
import QRCode from 'qrcode';

export function MaintenanceAccess({ token }: { token: string }) {
  const path = `/maintenance-request?token=${token}`;
  const url = `${window.location.origin}${path}`;
  const [qr, setQr] = useState("");
  const [open, setOpen] = useState(false);

  useEffect(() => {
    let mounted = true;
    QRCode.toDataURL(url, { errorCorrectionLevel: "M", margin: 2, width: 260 })
      .then(dataUrl => { if (mounted) setQr(dataUrl); })
      .catch(() => { if (mounted) setQr(""); });
    return () => { mounted = false; };
  }, [url]);

  return <div className="maintenance-access">
    <a data-testid="public-maintenance-link" href={path}>Форма</a>
    <button className="qr-button" type="button" onClick={() => setOpen(true)} disabled={!qr} data-testid="public-maintenance-qr-button">QR</button>
    {open && <div className="modal-backdrop" role="dialog" aria-modal="true" aria-label="QR-код заявки на обслуживание" onClick={() => setOpen(false)}>
      <div className="qr-modal" onClick={event => event.stopPropagation()}>
        <header>
          <div>
            <h2>QR-код заявки</h2>
            <p className="muted">Арендатор откроет форму заявки после сканирования.</p>
          </div>
          <button className="ghost icon-button" type="button" aria-label="Закрыть QR-код" onClick={() => setOpen(false)}>×</button>
        </header>
        {qr && <img data-testid="public-maintenance-qr" src={qr} alt="QR-код заявки на обслуживание" />}
        <a href={path}>Открыть форму</a>
      </div>
    </div>}
  </div>;
}
