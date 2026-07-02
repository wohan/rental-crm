import { expect, Page, test } from "@playwright/test";

const uniqueEmail = () => `e2e-${Date.now()}-${Math.round(Math.random() * 10000)}@example.ru`;

async function saveCard(page: Page, cardName: string) {
  const testId = `create-${cardName.toLowerCase()}`;
  await page.getByTestId(testId).click();
  if (cardName.toLowerCase() === "документ" || cardName.toLowerCase() === "договор") {
    await page.getByLabel(cardName.toLowerCase() === "договор" ? "Договор: Файл" : "Документ: Файл").setInputFiles({
      name: "lease-test.pdf",
      mimeType: "application/pdf",
      buffer: Buffer.from("%PDF-1.4\n% RentCRM test document\n")
    });
  }
  await page.getByTestId(`${testId}-save`).click();
  await expect(page.getByTestId("notice")).toContainText(cardName.toLowerCase() === "документ" ? "Документ загружен" : "Сохранено");
}

test("client can use the full RentCRM MVP flow", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByTestId("app-ready")).toBeVisible();

  await page.getByLabel("Название кабинета").fill("E2E RentCRM");
  await page.getByLabel("Ваше имя").fill("E2E Owner");
  await page.getByLabel("Email").fill(uniqueEmail());
  await page.getByLabel("Пароль").fill("password123");
  await page.getByTestId("auth-submit").click();

  await expect(page.getByText("E2E RentCRM")).toBeVisible();
  await expect(page.getByTestId("notice")).toContainText("Кабинет создан");

  await page.getByTestId("refresh-button").click();
  await expect(page.getByTestId("notice")).toContainText("Данные обновлены");

  await page.getByTestId("nav-objects").click();
  await expect(page.getByTestId("section-objects")).toBeVisible();
  await saveCard(page, "объект");
  await expect(page.getByText("Квартира на Ленина")).toBeVisible();
  await page.getByRole("button", { name: "Открыть" }).first().click();
  await page.getByLabel("Объект: Название").fill("Квартира на Ленина, 10");
  await page.getByRole("button", { name: "Сохранить" }).click();
  await expect(page.getByTestId("notice")).toContainText("Запись обновлена");
  await expect(page.getByText("Квартира на Ленина, 10")).toBeVisible();
  await expect(page.getByTestId("objects-table")).toBeVisible();
  await page.getByTestId("sort-objects-rent").click();
  await expect(page.getByTestId("sort-objects-rent")).toHaveAttribute("aria-sort", "ascending");
  await page.getByTestId("sort-objects-title").click();
  await expect(page.getByTestId("sort-objects-title")).toHaveAttribute("aria-sort", "ascending");
  const objectRow = page.locator(".object-row").filter({ hasText: "Квартира на Ленина, 10" });
  await expect(objectRow.locator(".table-actions")).toBeVisible();
  await expect(objectRow).not.toContainText("КУ");

  await page.getByTestId("nav-finance").click();
  await expect(page.getByText("Коммунальные платежи")).toBeVisible();
  await expect(page.getByText("авто")).toBeVisible();
  await page.getByRole("button", { name: "Открыть" }).first().click();
  await page.getByLabel("Расход: Сумма, ₽").fill("7300");
  await page.getByRole("button", { name: "Сохранить" }).click();
  await expect(page.getByTestId("notice")).toContainText("Запись обновлена");
  await expect(page.getByText(/7\s300 ₽/).first()).toBeVisible();

  await page.getByTestId("nav-objects").click();

  await saveCard(page, "арендатор");
  await expect(page.getByText("Анна Смирнова")).toBeVisible();
  const tenantRow = page.locator(".tenant-row").filter({ hasText: "Анна Смирнова" });
  await expect(tenantRow.locator(".row-actions")).toBeVisible();

  await saveCard(page, "договор");
  const today = new Date();
  const expectedContractNumber = `001-${String(today.getDate()).padStart(2, "0")}-${String(today.getMonth() + 1).padStart(2, "0")}-${today.getFullYear()}`;
  await expect(page.getByText(`№ ${expectedContractNumber}`)).toBeVisible();
  const contractDownload = page.waitForEvent("download");
  await page.locator(".row").filter({ hasText: `№ ${expectedContractNumber}` }).getByRole("button", { name: "Открыть" }).click();
  await expect(page.getByTestId("contract-documents")).toContainText("lease-test.pdf");
  await page.getByRole("button", { name: "Скачать" }).click();
  await expect(page.getByTestId("notice")).toContainText("Документ скачан");
  expect((await contractDownload).suggestedFilename()).toBe("lease-test.pdf");
  await page.getByLabel("Договор: Добавить файл").setInputFiles({
    name: "contract-extra.pdf",
    mimeType: "application/pdf",
    buffer: Buffer.from("%PDF-1.4\n% Extra contract document\n")
  });
  await page.getByRole("button", { name: "Загрузить в документы" }).click();
  await expect(page.getByTestId("notice")).toContainText("Документ загружен");
  await expect(page.getByTestId("contract-documents")).toContainText("contract-extra.pdf");
  await page.getByRole("button", { name: "Закрыть" }).click();

  const publicFormHref = await page.getByTestId("public-maintenance-link").first().getAttribute("href");
  expect(publicFormHref).toBeTruthy();
  await expect(page.getByTestId("public-maintenance-qr-button").first()).toBeVisible();
  await page.getByTestId("public-maintenance-qr-button").first().click();
  await expect(page.getByTestId("public-maintenance-qr")).toBeVisible();
  await page.getByRole("button", { name: "Закрыть QR-код" }).click();
  const publicPage = await page.context().newPage();
  await publicPage.goto(publicFormHref!);
  await publicPage.getByLabel("Название").fill("Публичная заявка E2E");
  await publicPage.getByLabel("Описание").fill("Проверить розетку");
  await publicPage.getByLabel("Ваше имя").fill("Анна Смирнова");
  await publicPage.getByLabel("Телефон").fill("+7 900 000-00-00");
  await publicPage.getByTestId("public-maintenance-submit").click();
  await expect(publicPage.getByTestId("public-maintenance-notice")).toContainText("Заявка принята");
  await publicPage.close();

  const download = page.waitForEvent("download");
  await page.getByTestId("export-button").click();
  await expect(page.getByTestId("notice")).toContainText("Экспорт скачан");
  expect((await download).suggestedFilename()).toBe("rentcrm-export.xlsx");

  await page.getByTestId("nav-payments").click();
  await expect(page.getByTestId("payments-table")).toBeVisible();
  await page.getByTestId("sort-payments-amount").click();
  await expect(page.getByTestId("sort-payments-amount")).toHaveAttribute("aria-sort", "ascending");
  await page.getByTestId("sort-payments-amount").click();
  await expect(page.getByTestId("sort-payments-amount")).toHaveAttribute("aria-sort", "descending");
  await expect(page.getByText("Квартира на Ленина, 10").first()).toBeVisible();
  await expect(page.getByText(`Аренда по договору № ${expectedContractNumber}`).first()).toBeVisible();
  await page.getByTestId("create-платеж").click();
  const commentBox = page.getByLabel("Платеж: Комментарий");
  const initialCommentHeight = await commentBox.evaluate(element => element.getBoundingClientRect().height);
  await commentBox.fill("Комментарий по платежу\n".repeat(8));
  const expandedCommentHeight = await commentBox.evaluate(element => element.getBoundingClientRect().height);
  expect(expandedCommentHeight).toBeGreaterThan(initialCommentHeight);
  await page.getByTestId("create-платеж").click();
  await saveCard(page, "платеж");
  await expect(page.getByText(/45\s000 ₽/).first()).toBeVisible();
  await page.getByRole("button", { name: "Оплачено" }).first().click();
  await expect(page.getByTestId("notice")).toContainText("Платеж отмечен");

  await page.getByTestId("nav-maintenance").click();
  await saveCard(page, "заявка");
  await expect(page.getByTestId("maintenance-table")).toBeVisible();
  await page.getByTestId("sort-maintenance-cost").click();
  await expect(page.getByTestId("sort-maintenance-cost")).toHaveAttribute("aria-sort", "ascending");
  await page.getByTestId("sort-maintenance-cost").click();
  await expect(page.getByTestId("sort-maintenance-cost")).toHaveAttribute("aria-sort", "descending");
  await expect(page.getByText("Проверить смеситель")).toBeVisible();
  await page.getByRole("button", { name: "В работу" }).first().click();
  await expect(page.getByTestId("notice")).toContainText("Статус заявки изменен");
  await page.getByRole("button", { name: "Закрыть" }).first().click();
  await expect(page.getByTestId("notice")).toContainText("Статус заявки изменен");

  await page.getByTestId("nav-documents").click();
  await saveCard(page, "документ");
  await expect(page.getByTestId("documents-table")).toBeVisible();
  await page.getByTestId("sort-documents-type").click();
  await expect(page.getByTestId("sort-documents-type")).toHaveAttribute("aria-sort", "ascending");
  await page.getByTestId("sort-documents-type").click();
  await expect(page.getByTestId("sort-documents-type")).toHaveAttribute("aria-sort", "descending");
  await expect(page.getByText("Договор аренды")).toBeVisible();
  await expect(page.getByText("contract-extra.pdf")).toBeVisible();
  const standaloneDocumentRow = page.locator(".table-row").filter({ hasText: "Договор аренды" });
  await expect(standaloneDocumentRow.getByText("lease-test.pdf")).toBeVisible();
  const documentDownload = page.waitForEvent("download");
  await standaloneDocumentRow.getByRole("button", { name: "Файл" }).click();
  await expect(page.getByTestId("notice")).toContainText("Документ скачан");
  expect((await documentDownload).suggestedFilename()).toBe("lease-test.pdf");

  await page.getByTestId("nav-finance").click();
  await saveCard(page, "расход");
  await expect(page.getByText("Ремонт")).toBeVisible();

  await page.getByTestId("nav-pipeline").click();
  await saveCard(page, "объявление");
  for (let i = 0; i < 5; i += 1) {
    await saveCard(page, "лид");
  }
  await expect(page.getByTestId("pipeline-table")).toBeVisible();
  await page.getByTestId("sort-pipeline-type").click();
  await expect(page.getByTestId("sort-pipeline-type")).toHaveAttribute("aria-sort", "ascending");
  await page.getByTestId("sort-pipeline-type").click();
  await expect(page.getByTestId("sort-pipeline-type")).toHaveAttribute("aria-sort", "descending");
  await expect(page.getByTestId("pagination-pipeline")).toContainText("Страница 1 из 2");
  await page.getByTestId("pagination-pipeline").getByRole("button", { name: "Вперед" }).click();
  await expect(page.getByTestId("pagination-pipeline")).toContainText("Страница 2 из 2");
  await page.getByTestId("sort-pipeline-name").click();
  await expect(page.getByTestId("pagination-pipeline")).toContainText("Страница 1 из 2");
  await page.getByTestId("sort-pipeline-type").click();
  await page.getByTestId("sort-pipeline-type").click();
  await expect(page.getByTestId("sort-pipeline-type")).toHaveAttribute("aria-sort", "descending");
  const pipelineTableWidth = await page.getByTestId("pipeline-table").evaluate(element => element.getBoundingClientRect().width);
  const pipelinePanelWidth = await page.getByTestId("pipeline-table").evaluate(element => element.closest(".panel")?.getBoundingClientRect().width ?? 0);
  expect(pipelineTableWidth).toBeGreaterThanOrEqual(pipelinePanelWidth - 40);
  await expect(page.getByText("Мария Иванова").first()).toBeVisible();
  await expect(page.getByText("Сдается квартира")).toBeVisible();
  await page.getByRole("button", { name: "Снять" }).first().click();
  await expect(page.getByTestId("notice")).toContainText("Статус объявления изменен");
  await page.getByRole("button", { name: "Связались" }).first().click();
  await expect(page.getByTestId("notice")).toContainText("Статус лида изменен");

  await page.getByTestId("nav-notifications").click();
  await expect(page.getByTestId("section-notifications")).toBeVisible();
  for (const [provider, expectedText] of [["SMS.RU", "api_id"], ["WhatsApp / GREEN-API", "idInstance"], ["Telegram Bot", "chat_id"]] as const) {
    const guide = page.locator(".provider-guide").filter({ hasText: provider });
    await expect(guide).toBeVisible();
    await guide.locator("summary").click();
    await expect(guide).toContainText(expectedText);
    await expect(guide.getByRole("link", { name: "Открыть официальную документацию" })).toBeVisible();
  }
  await page.getByRole("checkbox", { name: "Telegram" }).uncheck();
  await page.getByRole("checkbox", { name: "SMS.RU" }).uncheck();
  await page.getByRole("checkbox", { name: "WhatsApp webhook" }).uncheck();
  await page.getByRole("button", { name: "Сохранить" }).click();
  await expect(page.getByTestId("notice")).toContainText("Настройки уведомлений сохранены");
  await page.getByRole("button", { name: /Отправить сейчас/ }).click();
  await expect(page.getByTestId("notice")).toContainText("Отправка уведомлений выполнена");

  await page.getByTestId("nav-billing").click();
  await page.getByLabel("Провайдер").fill("GENERIC_PAYMENT_LINK");
  await page.getByLabel("Generic payment URL").fill("https://example.ru/pay");
  await page.getByRole("button", { name: "Сохранить оплату" }).click();
  await expect(page.getByTestId("notice")).toContainText("Настройки оплаты сохранены");
  await page.getByRole("button", { name: /START/ }).click();
  await expect(page.getByTestId("notice")).toContainText("Счет создан");
  await page.getByRole("button", { name: "Оплачено" }).last().click();
  await expect(page.getByTestId("notice")).toContainText("Тариф обновлен");
  await expect(page.getByText("Тариф START")).toBeVisible();
});

test("left navigation visibly switches sections", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByTestId("app-ready")).toBeVisible();

  await page.getByLabel("Название кабинета").fill("E2E Navigation");
  await page.getByLabel("Ваше имя").fill("Navigator");
  await page.getByLabel("Email").fill(uniqueEmail());
  await page.getByLabel("Пароль").fill("password123");
  await page.getByTestId("auth-submit").click();

  for (const section of ["objects", "payments", "maintenance", "documents", "finance", "pipeline"] as const) {
    await page.getByTestId(`nav-${section}`).click();
    await expect(page.getByTestId(`nav-${section}`)).toHaveClass(/active/);
    await expect(page.getByTestId(`section-${section}`)).toBeVisible();
  }

  for (const section of ["notifications", "billing"] as const) {
    await page.getByTestId(`nav-${section}`).click();
    await expect(page.getByTestId(`nav-${section}`)).toHaveClass(/active/);
  }
});

test("client can verify email and reset password", async ({ page }) => {
  const email = uniqueEmail();
  const oldPassword = "password123";
  const newPassword = "newPassword123";

  await page.goto("/");
  await expect(page.getByTestId("app-ready")).toBeVisible();

  await page.getByLabel("Название кабинета").fill("E2E Auth");
  await page.getByLabel("Ваше имя").fill("Auth Owner");
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Пароль").fill(oldPassword);
  await page.getByTestId("auth-submit").click();

  await expect(page.getByTestId("email-warning")).toBeVisible();
  const verifyLink = await page.getByTestId("verify-email-link").getAttribute("href");
  expect(verifyLink).toBeTruthy();

  await page.goto(verifyLink!);
  await expect(page.locator("body")).toContainText("Email подтвержден");

  await page.goto("/");
  await page.getByRole("button", { name: "Выйти" }).click();
  await page.getByRole("button", { name: "Забыли пароль?" }).click();
  await page.getByLabel("Email").fill(email);
  await page.getByTestId("auth-submit").click();

  const resetLink = await page.locator(".dev-link a").getAttribute("href");
  expect(resetLink).toBeTruthy();
  await page.goto(resetLink!);
  await page.getByLabel("Новый пароль").fill(newPassword);
  await page.getByTestId("auth-submit").click();
  await expect(page.getByText("Пароль изменен")).toBeVisible();

  await page.getByRole("button", { name: "Вход" }).click();
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Пароль").fill(newPassword);
  await page.getByTestId("auth-submit").click();
  await expect(page.getByText("E2E Auth")).toBeVisible();
});
