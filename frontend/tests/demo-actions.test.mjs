import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';
import ts from 'typescript';

function load({ token='test-only', fail=false }={}) {
  const calls=[];const exports={};
  const source=fs.readFileSync(new URL('../src/app/actions/demo.ts',import.meta.url),'utf8');
  const requireStub=name=>{
    if(name==='next/navigation')return {redirect:path=>{throw new Error(`REDIRECT ${path}`);}};
    if(name==='next/cache')return {revalidatePath:()=>{}};
    if(name==='@/lib/auth')return {getToken:async()=>token};
    if(name==='@/lib/api/client')return {ApiClientError:class extends Error{},apiRequest:async(path,options)=>{calls.push({path,body:JSON.parse(options.body)});if(fail)throw new Error('Offline');return {run:{roadmapId:'demo-roadmap'}};}};
    throw new Error(name);
  };
  vm.runInNewContext(ts.transpileModule(source,{compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2022}}).outputText,{exports,require:requireStub});
  return {actions:exports,calls};
}
test('demo setup requires explicit synthetic confirmation before any API mutation',async()=>{
  const {actions,calls}=load();const result=await actions.startDemoAction({},new FormData());
  assert.match(result.error,/confirm/i);assert.equal(calls.length,0);
});
test('demo actions authenticate and keep outage errors recoverable',async()=>{
  const locked=load({token:null});const form=new FormData();form.set('confirmSynthetic','yes');
  await assert.rejects(locked.actions.startDemoAction({},form),/REDIRECT \/login/);assert.equal(locked.calls.length,0);
  const offline=load({fail:true});assert.match((await offline.actions.startDemoAction({},form)).error,/unavailable/i);
  assert.equal(offline.calls.length,1);
});
test('exam shortcut rejects absent or invalid revisions and needs confirmation',async()=>{
  const {actions,calls}=load();const form=new FormData();form.set('confirmExam','yes');
  assert.match((await actions.examDemoAction({},form)).error,/reload/i);assert.equal(calls.length,0);
  form.set('expectedRoadmapRevision','1');form.set('expectedPlanRevision','0');
  await assert.rejects(actions.examDemoAction({},form),/REDIRECT \/progress/);
  assert.deepEqual(calls[0].body,{expectedRoadmapRevision:1,expectedPlanRevision:0});
});
