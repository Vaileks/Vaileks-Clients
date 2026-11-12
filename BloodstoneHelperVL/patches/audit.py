from pathlib import Path
import zipfile,json,hashlib
out=Path(__file__).resolve().parent/'dist';src=Path(__import__('os').environ.get('BLOODHELPER_INPUT_DIR', 'inputs'));checks={}
for jar in sorted(out.glob('*/BloodstoneHelperVL.jar')):
 if jar.parent.name not in {'1.21.11','1.21.11-lunarclient','26.3'}:continue
 variant=jar.parent.name;original=src/('BloodHelper-1.3.8+'+variant+'-privacy.jar')
 with zipfile.ZipFile(jar) as z,zipfile.ZipFile(original) as old:
  assert z.testzip() is None
  metadata=json.loads(z.read('fabric.mod.json'));oldmeta=json.loads(old.read('fabric.mod.json'))
  assert metadata['name']=='BloodstoneHelperVL' and metadata['authors']==['Vaileks']
  assert metadata['description']==oldmeta['description']+'\nDiscord: https://discord.gg/9kE6HNJEnZ'
  assert metadata['contact']=={'discord':'https://discord.gg/9kE6HNJEnZ'}
  originalmixins=json.loads(old.read('bloodhelper.mixins.json'))['client'];mixins=json.loads(z.read('bloodhelper.mixins.json'))['client']
  assert set(originalmixins)-set(mixins)=={'LootGlowMixin','LootColorMixin','LegendaryItemModelMixin','LegendaryItemRenderStateMixin'}
  for name in ['LootGlowMixin','LootColorMixin','LegendaryItemModelMixin','LegendaryItemRenderStateMixin']:assert 'ru/bloodhelper/client/mixin/'+name+'.class' not in z.namelist()
  base='ru/bloodhelper/client/'
  for name in ['visuals/LegendaryVisuals','modules/OpponentCooldownTracker','modules/VisualHitboxes','modules/BetterNameVisibilityModule','circles/BloodstoneCircles','gui/BloodHelperScreen$Module','modules/BindManagerModel$Filter','access/ClientControlService','access/ServiceTransport','config/ConfigStore']:
   assert old.read(base+name+'.class')==z.read(base+name+'.class'),'Unintended change '+name
  screen=z.read(base+'gui/BloodHelperScreen.class')
  for url in [b'https://boosty.to/bloodhelper/',b'https://discord.gg/QWcgKf9Md7',b'https://modrinth.com/mod/bloodhelper/versions']:assert url not in screen
  assert b'https://discord.gg/9kE6HNJEnZ' in screen
  assert z.read(base+'discord/DiscordPresence.class')==old.read(base+'discord/DiscordPresence.class')
  assert z.read(base+'discord/DiscordPresenceModule.class')==old.read(base+'discord/DiscordPresenceModule.class')
  assert z.read(base+'visuals/MotionBlurRenderer.class')==old.read(base+'visuals/MotionBlurRenderer.class')
  assert base+'vl/PrivacyGuard.class' in z.namelist()
  assert old.read('bloodhelper/music-yandex.ps1')==z.read('bloodhelper/music-yandex.ps1')
  assert not any('/smoke/' in n for n in z.namelist())
  changed=[n for n in old.namelist() if n in z.namelist() and old.read(n)!=z.read(n)]
  checks[variant]={'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'bytes':jar.stat().st_size,'audit':'passed','changed_entries':changed,'removed_mixins':['LootGlowMixin','LootColorMixin','LegendaryItemModelMixin','LegendaryItemRenderStateMixin']}
(out/'checksums.json').write_text(json.dumps(checks,indent=2)+'\n')
print('PASS archive, metadata, links, retained modules, privacy and mixin audits for all 3 builds')
