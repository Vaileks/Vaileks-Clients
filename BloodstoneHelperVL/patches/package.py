from pathlib import Path
import zipfile,subprocess,json,hashlib,re,os
ROOT=Path(__file__).resolve().parent
src=Path(os.environ.get('BLOODHELPER_INPUT_DIR', str(ROOT/'inputs')));out=ROOT/'dist';out.mkdir(exist_ok=True)
j=str(Path(os.environ['JAVA_HOME'])/'bin/java');asm=(ROOT/'asm-path.txt').read_text().strip();tool=ROOT/'build/classes/java/main';helper=tool/'ru/bloodhelper/client/vl/VLRestrictions.class'
removed={'LootGlowMixin','LootColorMixin','LegendaryItemModelMixin','LegendaryItemRenderStateMixin'}
manifest=[]
for p in sorted(src.glob('BloodHelper-*.jar')):
 variant='26.3' if '26.3' in p.name else ('1.21.11-lunarclient' if 'lunarclient' in p.name else '1.21.11')
 target=out/variant/'BloodstoneHelperVL.jar';target.parent.mkdir(exist_ok=True);temp=out/(variant+'.tmp.jar')
 if temp.exists():temp.unlink()
 subprocess.run([j,'-cp',asm+':'+str(tool),'PatchBloodHelper',str(p),str(temp),str(helper)],check=True)
 with zipfile.ZipFile(p) as original,zipfile.ZipFile(temp) as zin,zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED) as zout:
  originalmeta=json.loads(original.read('fabric.mod.json'))
  for info in zin.infolist():
   b=zin.read(info.filename)
   if info.filename=='fabric.mod.json':
    m=json.loads(b);m['name']='BloodstoneHelperVL';m['authors']=['Vaileks'];m['description']=re.sub(r'https?://[^\s<>]+|(?:www\.)[^\s<>]+', '', originalmeta['description']).strip()+'\nDiscord: https://discord.gg/9kE6HNJEnZ';m['contact']={'discord':'https://discord.gg/9kE6HNJEnZ'};b=(json.dumps(m,ensure_ascii=False,indent=2)+'\n').encode()
   elif info.filename=='bloodhelper.mixins.json':
    m=json.loads(b);found=removed.intersection(m['client']);m['client']=[n for n in m['client'] if n not in removed];b=(json.dumps(m,indent=2)+'\n').encode()
   elif info.filename.startswith('assets/bloodhelper/lang/') and info.filename.endswith('.json'):
    m=json.loads(b)
    for k in ['key.category.bloodhelper.main','key.bloodhelper.menu','key.bloodhelper.menu_lunar']:
     if k in m:m[k]=m[k].replace('BLOODHELPER','BloodstoneHelperVL')
    b=(json.dumps(m,ensure_ascii=False,indent=2)+'\n').encode()
   if info.filename in {'ru/bloodhelper/client/mixin/'+n+'.class' for n in removed}:continue
   zout.writestr(info,b)
  zout.writestr('ru/bloodhelper/client/vl/PrivacyGuard.class',(tool/'ru/bloodhelper/client/vl/PrivacyGuard.class').read_bytes())
 with zipfile.ZipFile(target) as z,zipfile.ZipFile(p) as original:
  assert z.testzip() is None
  assert all(n not in json.loads(z.read('bloodhelper.mixins.json'))['client'] for n in removed)
  for n in original.namelist():
   if not n.endswith('.class') and n not in ['fabric.mod.json','bloodhelper.mixins.json','assets/bloodhelper/lang/en_us.json','assets/bloodhelper/lang/ru_ru.json']:assert z.read(n)==original.read(n),n
 manifest.append({'variant':variant,'source':str(p),'output':str(target.resolve()),'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'removed_mixins':sorted(found)})
 temp.unlink()
(out/'manifest.json').write_text(json.dumps(manifest,indent=2))
print('All three builds patched; descriptions and previous privacy changes preserved.')
