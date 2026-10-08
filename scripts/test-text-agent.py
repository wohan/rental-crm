import os,json,threading,subprocess,time,urllib.request,urllib.error,uuid,base64,tempfile
from http.server import ThreadingHTTPServer,BaseHTTPRequestHandler
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
# Run only against a disposable test database. Never pass a production URL.
ROOT=Path(__file__).resolve().parents[1]
WORK=Path(tempfile.mkdtemp(prefix='rental-agent-smoke-'))
PORT=int(os.environ.get('AGENT_SMOKE_PORT','18191'))
PG_URL=os.environ['AGENT_TEST_PG_URL']
PG_USER=os.environ['AGENT_TEST_PG_USER']
PG_PASSWORD=os.environ['AGENT_TEST_PG_PASSWORD']
class Model(BaseHTTPRequestHandler):
 def log_message(self,*args):pass
 def do_POST(self):
  req=json.loads(self.rfile.read(int(self.headers['Content-Length'])))
  user=json.loads(req['messages'][-1]['content']);msg=user['message']
  op='listTenants' if msg=='list tenants' else 'createTenant'
  proposal={'operationId':op,'arguments':{} if op=='listTenants' else {'body':{'fullName':'Synthetic Durable Tenant','phone':'+15555550123'}}}
  if msg=='invalid email':proposal['arguments']['body']['email']='invalid'
  data=json.dumps({'choices':[{'finish_reason':'stop','message':{'content':json.dumps(proposal)}}]}).encode()
  self.send_response(200);self.send_header('Content-Type','application/json');self.end_headers();self.wfile.write(data)
model=ThreadingHTTPServer(('127.0.0.1',0),Model);threading.Thread(target=model.serve_forever,daemon=True).start()
env=dict(os.environ,SERVER_PORT=str(PORT),SPRING_PROFILES_INCLUDE='text-agent',SPRING_DATASOURCE_URL=PG_URL,SPRING_DATASOURCE_USERNAME=PG_USER,SPRING_DATASOURCE_PASSWORD=PG_PASSWORD,APP_FIELD_ENCRYPTION_KEY=base64.b64encode(bytes(range(32))).decode(),AGENT_STATE_ENCRYPTION_KEY=base64.b64encode(bytes(range(32))).decode(),APP_JWT_SECRET='test-only-secret-for-isolated-smoke-test-0123456789',AGENT_MODEL_ENDPOINT=f'http://127.0.0.1:{model.server_port}/model',AGENT_MODEL='stub',AGENT_API_KEY='test-only',AGENT_TARGET_BASE_URL=f'http://127.0.0.1:{PORT}',APP_DOCUMENT_STORAGE_DIR=str(WORK/'uploads'))
process=None;log=open(WORK/'application.log','w');print('Local smoke log:',WORK/'application.log',flush=True)
def api(path,data=None,token=None,expected=200):
 headers={'Content-Type':'application/json'}
 if token:headers['Authorization']='Bearer '+token
 req=urllib.request.Request(f'http://127.0.0.1:{PORT}'+path,data=json.dumps(data).encode() if data is not None else None,headers=headers)
 try:
  with urllib.request.urlopen(req,timeout=30) as response:status=response.status;raw=response.read()
 except urllib.error.HTTPError as e:status=e.code;raw=e.read()
 assert status==expected,(path,status,raw[:400])
 return json.loads(raw) if raw else None
def start():
 global process
 process=subprocess.Popen(['java','-jar',str(ROOT/'backend/target/rentcrm-backend-0.1.0.jar')],env=env,stdout=log,stderr=log)
 for _ in range(100):
  if process.poll() is not None:raise RuntimeError('App failed; inspect local application.log')
  try:
   if api('/actuator/health')['status']=='UP':return
  except Exception:pass
  time.sleep(.2)
 raise RuntimeError('Startup timeout')
def stop():
 global process
 if process is not None:
  process.terminate()
  try:process.wait(timeout=15)
  except subprocess.TimeoutExpired:process.kill();process.wait()
  process=None
def register():
 return api('/api/auth/register',{'email':str(uuid.uuid4())+'@example.test','password':'Synthetic-only-123!','fullName':'Synthetic User','accountName':'Synthetic account','termsAccepted':True,'privacyAccepted':True})['token']
try:
 start();api('/api/openapi',expected=403)
 token=register();other=register()
 inventory=api('/api/text-agent/operations',token=token);assert len(inventory['operations'])==66;assert sum(x['enabled'] for x in inventory['operations'])==19
 invalid=api('/api/text-agent/messages',{'message':'invalid email'},token);assert invalid['state']=='COLLECTING_FIELDS'
 api('/api/tenants',{'fullName':'Synthetic','phone':'123','email':'invalid'},token,400)
 ready=api('/api/text-agent/messages',{'message':'create tenant'},token);assert ready['state']=='READY_FOR_CONFIRMATION';assert api('/api/tenants',token=token)==[]
 stop();start()
 session='/api/text-agent/sessions/'+ready['sessionId']
 assert api(session,token=token)['state']=='READY_FOR_CONFIRMATION'
 api(session,token=other,expected=404)
 with ThreadPoolExecutor(max_workers=2) as pool:
  results=list(pool.map(lambda _:api(session+'/confirm',{'confirmationId':ready['confirmationId']},token),range(2)))
 assert all(r['state'] in ['DONE','EXECUTING'] for r in results),results
 result=api(session,token=token);assert result['state']=='DONE';assert result['result']['id'];assert len(api('/api/tenants',token=token))==1
 stop();start()
 assert api(session+'/confirm',{'confirmationId':ready['confirmationId']},token)['state']=='DONE'
 assert len(api('/api/tenants',token=token))==1;assert api('/api/tenants',token=other)==[]
 ready=api('/api/text-agent/messages',{'message':'list tenants'},token)
 result=api('/api/text-agent/sessions/'+ready['sessionId']+'/confirm',{'confirmationId':ready['confirmationId']},token)
 assert result['result']['total']==1;assert result['result']['items'][0]['fullName']=='Synthetic Durable Tenant'
 print('PASS: V10 startup, authenticated full catalogue, validation, pending-session restart, concurrent confirmation, completed-session restart, one created row, tenant isolation, safe list result',flush=True)
finally:stop();model.shutdown();log.close()
