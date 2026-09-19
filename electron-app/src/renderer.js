let token = '';
let challengeId = '';
let currentUser = '';

const $ = (id) => document.getElementById(id);
const base = () => (window.avai?.apiUrl || '').replace(/\/$/, '');

async function api(path, body, authToken = '') {
  if (!base() || base().includes('YOUR-RENDER-API')) throw new Error('آدرس API مرکزی در config.json تنظیم نشده است.');
  const headers = { 'Content-Type': 'application/json' };
  if (authToken) headers.Authorization = `Bearer ${authToken}`;
  const r = await fetch(base() + path, { method: 'POST', headers, body: JSON.stringify(body) });
  const data = await r.json().catch(() => ({}));
  if (!r.ok || data.success === false) throw new Error(data.message || 'خطا در ارتباط با سرور');
  return data;
}

$('toLogin').onclick = () => { $('registerPane').classList.add('hidden'); $('loginPane').classList.remove('hidden'); };
$('toRegister').onclick = () => { $('loginPane').classList.add('hidden'); $('registerPane').classList.remove('hidden'); };

$('registerBtn').onclick = async () => {
  $('registerMsg').textContent = 'در حال ثبت‌نام...';
  try {
    const data = await api('/api/auth/register', {
      username: $('regUsername').value,
      displayName: $('regDisplay').value,
      password: $('regPassword').value
    });
    $('registerMsg').textContent = data.message + ' حالا وارد شوید.';
    $('loginUsername').value = $('regUsername').value;
    $('registerPane').classList.add('hidden');
    $('loginPane').classList.remove('hidden');
  } catch (e) { $('registerMsg').textContent = e.message; }
};

$('requestCodeBtn').onclick = async () => {
  $('loginMsg').textContent = 'در حال دریافت کد...';
  try {
    const data = await api('/api/auth/request-code', {
      username: $('loginUsername').value,
      password: $('loginPassword').value
    });
    challengeId = data.data.challengeId;
    $('codePane').classList.remove('hidden');
    $('loginMsg').textContent = 'کد روی ویندوز نمایش داده شد.';
    window.alert(`کد تأیید AV AI\n\n${data.data.code}\n\nاعتبار: ۵ دقیقه`);
    $('codeInput').focus();
  } catch (e) { $('loginMsg').textContent = e.message; }
};

$('verifyBtn').onclick = async () => {
  $('loginMsg').textContent = 'در حال تأیید...';
  try {
    const data = await api('/api/auth/verify-code', { challengeId, code: $('codeInput').value });
    token = data.data.token;
    currentUser = data.data.displayName || data.data.username;
    $('authView').classList.add('hidden');
    $('chatView').classList.remove('hidden');
    $('welcome').textContent = `سلام ${currentUser}`;
    addMessage('ai', 'سلام! سؤال خودت را بپرس.');
  } catch (e) { $('loginMsg').textContent = e.message; }
};

$('askBtn').onclick = async () => {
  const question = $('question').value.trim();
  if (!question) return;
  addMessage('user', question);
  $('question').value = '';
  $('chatMsg').textContent = 'در حال دریافت پاسخ...';
  $('askBtn').disabled = true;
  try {
    const data = await api('/api/ai/ask', { question }, token);
    addMessage('ai', data.answer || 'پاسخی دریافت نشد.');
    $('chatMsg').textContent = '';
  } catch (e) { $('chatMsg').textContent = e.message; }
  finally { $('askBtn').disabled = false; }
};

$('question').addEventListener('keydown', (e) => {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); $('askBtn').click(); }
});

$('logoutBtn').onclick = () => {
  token = ''; challengeId = ''; currentUser = '';
  $('messages').innerHTML = '';
  $('chatView').classList.add('hidden');
  $('authView').classList.remove('hidden');
  $('codePane').classList.add('hidden');
  $('codeInput').value = '';
};

function addMessage(type, text) {
  const el = document.createElement('div');
  el.className = `message ${type}`;
  el.textContent = text;
  $('messages').appendChild(el);
  $('messages').scrollTop = $('messages').scrollHeight;
}
